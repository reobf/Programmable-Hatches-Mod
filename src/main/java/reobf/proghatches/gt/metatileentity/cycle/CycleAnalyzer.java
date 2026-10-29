package reobf.proghatches.gt.metatileentity.cycle;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import reobf.proghatches.gt.metatileentity.cycle.CycleWarning.Code;

/**
 * Per product: fewest total runs by LP branch and bound on the integer kernel lattice of the tolerance-0 rows,
 * then the tie-break (least seeds, most product, lexicographic) by enumerating that one layer of total runs.
 */
public final class CycleAnalyzer {

    /** Work charged per tableau cell of a simplex set-up or pivot; an enumeration step costs one per item. */
    static final long LP_CELL_WORK = 64;

    private CycleAnalyzer() {}

    public static <K> CycleAnalysis<K> analyze(List<CyclePattern<K>> patterns, Map<K, CycleItemSetting> settings,
        long multiplier, CycleLimits limits) {
        Job<K> job = new Job<>(patterns, settings, multiplier, limits);
        while (!job.step(Long.MAX_VALUE)) {}
        return job.result();
    }

    /** The analysis in bounded steps; however it is sliced, it ends with the result {@link #analyze} returns. */
    public static final class Job<K> {

        private final List<CyclePattern<K>> patterns;
        private final Map<K, CycleItemSetting> settings;
        private final long multiplier;
        private final CycleLimits limits;
        private CycleAnalysis<K> result;
        private long work;
        private int phase, pi;

        private int n, m, np;
        private List<K> items, cyclicKeys, productKeys, unsolved;
        private LinkedHashMap<K, Integer> index;
        private long[][] in, out, s, runsOf;
        private long[] total;
        private int[] ids, cyc, products;
        private boolean[] cyclic, notMinimal, done, wanted;
        private CycleItemSetting[] set;
        private List<CycleWarning<K>> warnings;
        private Solver sv;
        private Solver.Run run;
        private Search sr;
        private long enumBudget, enumNodes;

        public Job(List<CyclePattern<K>> patterns, Map<K, CycleItemSetting> settings, long multiplier,
            CycleLimits limits) {
            if (patterns == null) throw new NullPointerException("patterns");
            if (multiplier < 1) throw new IllegalArgumentException("multiplier must be >= 1: " + multiplier);
            this.patterns = patterns;
            this.settings = settings;
            this.multiplier = multiplier;
            this.limits = limits == null ? CycleLimits.DEFAULT : limits;
        }

        public boolean isDone() {
            return result != null;
        }

        public CycleAnalysis<K> result() {
            return result;
        }

        public long getWork() {
            return work;
        }

        /** Spends about {@code budget} more work, less once complete; true when the result is ready. */
        public boolean step(long budget) {
            long stop = budget > Long.MAX_VALUE - work ? Long.MAX_VALUE : work + budget;
            while (result == null && work < stop) {
                if (phase == 0) init();
                else if (phase == 1) solveStep();
                else if (phase == 2) searchStep(stop);
                else finish();
            }
            return result != null;
        }

        private void init() {
            work++;
            n = patterns.size();
            List<K> none = Collections.emptyList();
            List<CycleBalancing<K>> noBal = Collections.emptyList();
            warnings = new ArrayList<>();
            if (n == 0) {
                result = new CycleAnalysis<>(none, none, none, noBal, none, warnings, 0, 0);
                return;
            }
            if (n > limits.maxPatterns) {
                warnings.add(new CycleWarning<K>(Code.TOO_MANY_PATTERNS, null, -1, n, limits.maxPatterns));
                result = new CycleAnalysis<>(none, none, none, noBal, none, warnings, 0, n);
                return;
            }

            index = new LinkedHashMap<>();
            for (CyclePattern<K> p : patterns) {
                for (CycleAmount<K> c : p.inputs) if (!index.containsKey(c.key)) index.put(c.key, index.size());
                for (CycleAmount<K> c : p.outputs) if (!index.containsKey(c.key)) index.put(c.key, index.size());
            }
            items = new ArrayList<>(index.keySet());
            m = items.size();
            if (m > limits.maxItems) {
                warnings.add(new CycleWarning<K>(Code.TOO_MANY_ITEMS, null, -1, m, limits.maxItems));
                result = new CycleAnalysis<>(none, none, none, noBal, none, warnings, 0, n);
                return;
            }

            in = new long[m][n];
            out = new long[m][n];
            s = new long[m][n];
            ids = new int[n];
            for (int i = 0; i < n; i++) {
                CyclePattern<K> p = patterns.get(i);
                ids[i] = p.id;
                for (CycleAmount<K> c : p.inputs) in[index.get(c.key)][i] = c.amount;
                for (CycleAmount<K> c : p.outputs) out[index.get(c.key)][i] = c.amount;
            }
            cyclic = new boolean[m];
            boolean[] produced = new boolean[m];
            set = new CycleItemSetting[m];
            cyclicKeys = new ArrayList<>();
            List<Integer> cycIdx = new ArrayList<>();
            productKeys = new ArrayList<>();
            List<Integer> prodIdx = new ArrayList<>();
            for (int k = 0; k < m; k++) {
                boolean consumed = false;
                for (int i = 0; i < n; i++) {
                    s[k][i] = Math.subtractExact(out[k][i], in[k][i]);
                    if (in[k][i] > 0) consumed = true;
                    if (out[k][i] > 0) produced[k] = true;
                }
                cyclic[k] = consumed && produced[k];
                CycleItemSetting st = settings == null ? null : settings.get(items.get(k));
                set[k] = st == null ? CycleItemSetting.DEFAULT : st;
                if (cyclic[k]) {
                    cyclicKeys.add(items.get(k));
                    cycIdx.add(k);
                }
                if (cyclic[k] ? set[k].output : produced[k]) {
                    productKeys.add(items.get(k));
                    prodIdx.add(k);
                }
            }
            if (productKeys.isEmpty()) {
                warnings.add(new CycleWarning<K>(Code.NO_PRODUCT, null, -1, 0, 0));
                result = new CycleAnalysis<>(items, cyclicKeys, productKeys, noBal, none, warnings, 0, n);
                return;
            }

            cyc = toArray(cycIdx);
            products = toArray(prodIdx);
            np = products.length;
            sv = new Solver(n, out, s, cyc, set, limits);
            runsOf = new long[np][];
            total = new long[np];
            notMinimal = new boolean[np];
            unsolved = new ArrayList<>();
            work += (long) m * n + sv.setupWork;
            phase = 1;
        }

        private void solveStep() {
            if (pi == np) {
                sr = new Search(n, m, in, out, s, cyc, products, set);
                enumBudget = limits.maxEnumNodes;
                done = new boolean[np];
                pi = 0;
                phase = 2;
                work += (long) m * n;
                return;
            }
            if (run == null) {
                run = sv.new Run(products[pi]);
                work++;
            }
            if (!run.done && work >= limits.maxWork) run.abort();
            if (!run.done) {
                work += run.step();
                return;
            }
            Solver.Result r = run.res;
            run = null;
            int p = pi++;
            K key = productKeys.get(p);
            work += (long) m * n;
            if (r.x == null) {
                unsolved.add(key);
                warnings.add(
                    r.aborted ? new CycleWarning<>(Code.SEARCH_LIMIT, key, -1, sv.lpSolves, 0)
                        : new CycleWarning<>(Code.NO_BALANCING, key, -1, 0, 0));
                return;
            }
            long[] x = toLongs(r.x);
            long t = -1;
            if (x != null) try {
                t = 0;
                for (long v : x) t = Math.addExact(t, v);
                for (int k = 0; k < m; k++) {
                    long net = 0, si = 0;
                    for (int i = 0; i < n; i++) {
                        net = Math.addExact(net, Math.multiplyExact(s[k][i], x[i]));
                        if (cyclic[k]) si = Math.addExact(si, Math.multiplyExact(in[k][i], x[i]));
                    }
                }
            } catch (ArithmeticException e) {
                x = null;
            }
            if (x == null) {
                unsolved.add(key);
                warnings.add(new CycleWarning<>(Code.OVERFLOW, key, -1, 0, 0));
                return;
            }
            runsOf[p] = x;
            total[p] = t;
            notMinimal[p] = r.aborted;
        }

        private void searchStep(long stop) {
            if (wanted == null) {
                while (pi < np && (runsOf[pi] == null || done[pi])) pi++;
                if (pi == np) {
                    phase = 3;
                    return;
                }
                wanted = new boolean[np];
                for (int pj = pi; pj < np; pj++)
                    if (runsOf[pj] != null && !done[pj] && total[pj] == total[pi]) wanted[pj] = true;
                sr.begin(total[pi], wanted, enumBudget);
                work++;
            }
            long room = Math.min(stop, limits.maxWork) - work;
            if (room > 0) work += sr.run(room);
            if (!sr.finished && work < limits.maxWork) return;
            boolean complete = sr.finished && sr.complete;
            enumBudget -= sr.nodes;
            enumNodes += sr.nodes;
            for (int pj = pi; pj < np; pj++) {
                if (!wanted[pj]) continue;
                done[pj] = true;
                if (sr.best[pj] != null) runsOf[pj] = sr.best[pj].x.clone();
                if (!complete) notMinimal[pj] = true;
            }
            wanted = null;
        }

        private void finish() {
            work += (long) np * m * n;
            for (int i = 0; i < np; i++) if (runsOf[i] != null && notMinimal[i])
                warnings.add(new CycleWarning<>(Code.NOT_MINIMAL, productKeys.get(i), -1, 0, 0));

            List<long[]> xs = new ArrayList<>();
            List<List<K>> prods = new ArrayList<>();
            for (int i = 0; i < np; i++) {
                long[] x = runsOf[i];
                if (x == null) continue;
                int found = -1;
                for (int b = 0; b < xs.size(); b++) if (Arrays.equals(xs.get(b), x)) {
                    found = b;
                    break;
                }
                if (found < 0) {
                    xs.add(x);
                    prods.add(new ArrayList<K>());
                    found = xs.size() - 1;
                }
                prods.get(found)
                    .add(productKeys.get(i));
            }

            List<CycleBalancing<K>> balancings = new ArrayList<>();
            for (int b = 0; b < xs.size(); b++) {
                long[] x = xs.get(b);
                long[] net = new long[m], seedIn = new long[m];
                for (int k = 0; k < m; k++) for (int i = 0; i < n; i++) {
                    net[k] = Math.addExact(net[k], Math.multiplyExact(s[k][i], x[i]));
                    if (cyclic[k]) seedIn[k] = Math.addExact(seedIn[k], Math.multiplyExact(in[k][i], x[i]));
                }
                List<CycleAmount<K>> netIn = new ArrayList<>(), netOut = new ArrayList<>(), surplus = new ArrayList<>();
                for (int k = 0; k < m; k++) {
                    K key = items.get(k);
                    if (net[k] < 0) netIn.add(new CycleAmount<>(key, -net[k]));
                    else if (net[k] > 0) {
                        if (!cyclic[k] || set[k].output) netOut.add(new CycleAmount<>(key, net[k]));
                        else surplus.add(new CycleAmount<>(key, net[k]));
                    }
                }
                List<CycleAmount<K>> seeds = new ArrayList<>(), need = new ArrayList<>();
                List<CycleWarning<K>> balWarnings = new ArrayList<>();
                boolean publishable = true;
                for (int k : cyc) {
                    if (seedIn[k] <= 0) continue;
                    K key = items.get(k);
                    seeds.add(new CycleAmount<>(key, seedIn[k]));
                    long v;
                    try {
                        v = Math.multiplyExact(seedIn[k], multiplier);
                    } catch (ArithmeticException e) {
                        v = Long.MAX_VALUE;
                        publishable = false;
                        balWarnings.add(new CycleWarning<>(Code.NEED_OVERFLOW, key, -1, 0, 0));
                    }
                    need.add(new CycleAmount<>(key, v));
                }
                if (netIn.isEmpty()) {
                    publishable = false;
                    balWarnings.add(new CycleWarning<>(Code.NO_NET_INPUT, prods.get(b).get(0), -1, 0, 0));
                }
                for (CycleAmount<K> c : need) {
                    long target = set[index.get(c.key)].target;
                    if (c.amount > target) {
                        publishable = false;
                        balWarnings.add(new CycleWarning<>(Code.SEED_OVER_TARGET, c.key, -1, c.amount, target));
                    }
                }
                warnings.addAll(balWarnings);
                balancings.add(new CycleBalancing<>(x, ids, netIn, netOut, surplus, seeds, need, prods.get(b), publishable));
            }
            result = new CycleAnalysis<>(items, cyclicKeys, productKeys, balancings, unsolved, warnings, sv.lpSolves + enumNodes, n);
        }
    }

    private static int[] toArray(List<Integer> list) {
        int[] a = new int[list.size()];
        for (int i = 0; i < a.length; i++) a[i] = list.get(i);
        return a;
    }

    private static long[] toLongs(BigInteger[] x) {
        long[] r = new long[x.length];
        for (int i = 0; i < x.length; i++) {
            if (x[i].bitLength() > 63) return null;
            r[i] = x[i].longValue();
        }
        return r;
    }

    static long satAdd(long a, long b) {
        long r = a + b;
        if (((a ^ r) & (b ^ r)) < 0) return a < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
        return r;
    }

    /** Both arguments are >= 0. */
    static long satMul(long a, long b) {
        if (a == 0 || b == 0) return 0;
        return a > Long.MAX_VALUE / b ? Long.MAX_VALUE : a * b;
    }

    /** nk >= 0; inclusive: nk * 100 <= tol * produced. */
    static boolean tolOk(long nk, long produced, int tol) {
        if (nk == 0) return true;
        try {
            return Math.multiplyExact(nk, 100L) <= Math.multiplyExact((long) tol, produced);
        } catch (ArithmeticException e) {
            return BigInteger.valueOf(nk)
                .multiply(BigInteger.valueOf(100))
                .compareTo(BigInteger.valueOf(tol).multiply(BigInteger.valueOf(produced))) <= 0;
        }
    }

    /** Lattice basis of {x in Z^n : eq * x = 0}, as rows; empty if only x = 0 solves it. */
    static BigInteger[][] integerKernel(BigInteger[][] eq, int n) {
        int q = eq.length;
        BigInteger[][] mt = new BigInteger[n][q], u = new BigInteger[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < q; j++) mt[i][j] = eq[j][i];
            for (int j = 0; j < n; j++) u[i][j] = i == j ? BigInteger.ONE : BigInteger.ZERO;
        }
        int r = 0;
        for (int c = 0; c < q && r < n; c++) {
            while (true) {
                int piv = -1;
                for (int i = r; i < n; i++) if (mt[i][c].signum() != 0 && (piv < 0 || mt[i][c].abs()
                    .compareTo(mt[piv][c].abs()) < 0)) piv = i;
                if (piv < 0) break;
                boolean rest = false;
                for (int i = r; i < n; i++) {
                    if (i == piv || mt[i][c].signum() == 0) continue;
                    BigInteger f = mt[i][c].divide(mt[piv][c]);
                    for (int j = 0; j < q; j++) mt[i][j] = mt[i][j].subtract(f.multiply(mt[piv][j]));
                    for (int j = 0; j < n; j++) u[i][j] = u[i][j].subtract(f.multiply(u[piv][j]));
                    if (mt[i][c].signum() != 0) rest = true;
                }
                if (!rest) {
                    BigInteger[] tm = mt[r];
                    mt[r] = mt[piv];
                    mt[piv] = tm;
                    BigInteger[] tu = u[r];
                    u[r] = u[piv];
                    u[piv] = tu;
                    r++;
                    break;
                }
            }
        }
        return Arrays.copyOfRange(u, r, n);
    }

    /** Smallest multiple of g that is >= num / den; num >= 0, den > 0, g > 0. */
    static BigInteger ceilMul(BigInteger num, BigInteger den, BigInteger g) {
        BigInteger dg = den.multiply(g);
        return num.add(dg)
            .subtract(BigInteger.ONE)
            .divide(dg)
            .multiply(g);
    }

    static boolean isMultiple(BigInteger num, BigInteger den, BigInteger g) {
        if (g.signum() == 0) return num.signum() == 0;
        return num.mod(den.multiply(g))
            .signum() == 0;
    }

    private static final class Node {

        final BigInteger[] lo, hi;

        Node(int n) {
            lo = new BigInteger[n];
            hi = new BigInteger[n];
        }

        Node(BigInteger[] lo, BigInteger[] hi) {
            this.lo = lo;
            this.hi = hi;
        }

        Node with(int i, boolean upper, BigInteger v) {
            BigInteger[] l = lo.clone(), h = hi.clone();
            if (upper) h[i] = v;
            else l[i] = v;
            return new Node(l, h);
        }
    }

    private static final class Solver {

        static final class Result {

            BigInteger[] x;
            boolean aborted;
        }

        private static final BigInteger HUNDRED = BigInteger.valueOf(100);

        final int n;
        final long[][] s;
        final CycleLimits limits;
        final List<BigInteger[]> rows = new ArrayList<>();
        final BigInteger[] ones, gVar;
        final BigInteger gSum;
        final boolean lattice, emptyKernel;
        final long setupWork;
        int lpSolves;

        Solver(int n, long[][] out, long[][] s, int[] cyc, CycleItemSetting[] set, CycleLimits limits) {
            this.n = n;
            this.s = s;
            this.limits = limits;
            ones = new BigInteger[n];
            Arrays.fill(ones, BigInteger.ONE);
            List<BigInteger[]> eq = new ArrayList<>();
            for (int k : cyc) {
                BigInteger[] sk = big(s[k]);
                rows.add(sk);
                if (set[k].output) continue;
                int tol = set[k].tolerancePercent;
                if (tol == 0) {
                    eq.add(sk);
                    BigInteger[] neg = new BigInteger[n];
                    for (int i = 0; i < n; i++) neg[i] = sk[i].negate();
                    rows.add(neg);
                } else if (tol < 100) {
                    BigInteger[] r = new BigInteger[n];
                    for (int i = 0; i < n; i++) r[i] = BigInteger.valueOf(out[k][i])
                        .multiply(BigInteger.valueOf(tol))
                        .subtract(sk[i].multiply(HUNDRED));
                    rows.add(r);
                }
            }
            gVar = new BigInteger[n];
            if (eq.isEmpty()) {
                Arrays.fill(gVar, BigInteger.ONE);
                gSum = BigInteger.ONE;
                lattice = false;
                emptyKernel = false;
            } else {
                BigInteger[][] ker = integerKernel(eq.toArray(new BigInteger[0][]), n);
                lattice = true;
                emptyKernel = ker.length == 0;
                for (int i = 0; i < n; i++) {
                    BigInteger g = BigInteger.ZERO;
                    for (BigInteger[] v : ker) g = g.gcd(v[i]);
                    gVar[i] = g;
                }
                BigInteger gs = BigInteger.ZERO;
                for (BigInteger[] v : ker) {
                    BigInteger sum = BigInteger.ZERO;
                    for (BigInteger e : v) sum = sum.add(e);
                    gs = gs.gcd(sum);
                }
                gSum = gs.signum() == 0 ? BigInteger.ONE : gs;
            }
            setupWork = (long) eq.size() * n * (eq.size() + n) * LP_CELL_WORK;
        }

        private static BigInteger[] big(long[] v) {
            BigInteger[] r = new BigInteger[v.length];
            for (int i = 0; i < v.length; i++) r[i] = BigInteger.valueOf(v[i]);
            return r;
        }

        /** Branch and bound for one product; every {@link #step} is one simplex set-up or one pivot. */
        final class Run {

            final Result res = new Result();
            boolean done;
            private final List<BigInteger[]> a = new ArrayList<>(rows);
            private final List<BigInteger> b = new ArrayList<>();
            private Deque<Node> stack;
            private BigInteger[] best;
            private BigInteger bestObj;
            private ExactLP lp;
            private Node node;

            Run(int p) {
                if (emptyKernel) {
                    done = true;
                    return;
                }
                for (int i = 0; i < rows.size(); i++) b.add(BigInteger.ZERO);
                a.add(big(s[p]));
                b.add(BigInteger.ONE);
                if (lattice && gSum.compareTo(BigInteger.ONE) > 0) {
                    if (lpSolves >= limits.maxLpSolves) {
                        res.aborted = true;
                        done = true;
                        return;
                    }
                    lp = new ExactLP(a, b, ones);
                    lpSolves++;
                } else {
                    stack = new ArrayDeque<>();
                    stack.push(new Node(n));
                }
            }

            /** @return the work spent */
            long step() {
                if (lp == null) {
                    if (stack.isEmpty()) {
                        finish(false);
                        return 1;
                    }
                    if (lpSolves >= limits.maxLpSolves) {
                        finish(true);
                        return 1;
                    }
                    node = stack.pop();
                    List<BigInteger[]> na = new ArrayList<>(a);
                    List<BigInteger> nb = new ArrayList<>(b);
                    for (int i = 0; i < n; i++) {
                        if (node.lo[i] != null) {
                            BigInteger[] r = new BigInteger[n];
                            Arrays.fill(r, BigInteger.ZERO);
                            r[i] = BigInteger.ONE;
                            na.add(r);
                            nb.add(node.lo[i]);
                        }
                        if (node.hi[i] != null) {
                            BigInteger[] r = new BigInteger[n];
                            Arrays.fill(r, BigInteger.ZERO);
                            r[i] = BigInteger.ONE.negate();
                            na.add(r);
                            nb.add(node.hi[i].negate());
                        }
                    }
                    lp = new ExactLP(na, nb, ones);
                    lpSolves++;
                    return lp.cells() * LP_CELL_WORK;
                }
                int st = lp.pivotStep();
                long cost = lp.cells() * LP_CELL_WORK;
                if (st < 0) return cost;
                ExactLP solved = lp;
                lp = null;
                if (stack == null) {
                    if (st == 0) {
                        done = true;
                        return cost;
                    }
                    a.add(ones.clone());
                    b.add(ceilMul(solved.objNum(), solved.den(), gSum));
                    stack = new ArrayDeque<>();
                    stack.push(new Node(n));
                    return cost;
                }
                if (st > 0) branch(solved);
                return cost;
            }

            private void branch(ExactLP solved) {
                BigInteger den = solved.den();
                BigInteger lb = ceilMul(solved.objNum(), den, gSum);
                if (best != null && lb.compareTo(bestObj) >= 0) return;
                BigInteger[] xn = solved.xNum();
                int bi = -1;
                for (int i = 0; i < n; i++) if (!isMultiple(xn[i], den, gVar[i])) {
                    bi = i;
                    break;
                }
                if (bi < 0) {
                    best = new BigInteger[n];
                    bestObj = BigInteger.ZERO;
                    for (int i = 0; i < n; i++) {
                        best[i] = xn[i].divide(den);
                        bestObj = bestObj.add(best[i]);
                    }
                    return;
                }
                BigInteger g = gVar[bi].signum() == 0 ? BigInteger.ONE : gVar[bi];
                BigInteger lo = xn[bi].divide(den.multiply(g))
                    .multiply(g);
                stack.push(node.with(bi, false, lo.add(g)));
                stack.push(node.with(bi, true, lo));
            }

            void abort() {
                finish(true);
            }

            private void finish(boolean aborted) {
                res.aborted |= aborted;
                res.x = best;
                lp = null;
                done = true;
            }
        }
    }

    private static final class Candidate {

        final long[] x;
        final long seedTotal;
        final long np;

        Candidate(long[] x, long seedTotal, long np) {
            this.x = x;
            this.seedTotal = seedTotal;
            this.np = np;
        }

        boolean betterThan(Candidate o) {
            if (seedTotal != o.seedTotal) return seedTotal < o.seedTotal;
            if (np != o.np) return np > o.np;
            for (int i = 0; i < x.length; i++) if (x[i] != o.x[i]) return x[i] < o.x[i];
            return false;
        }
    }

    /**
     * Enumerates one layer of total runs depth first, with an explicit stack so it can pause between steps, and keeps
     * the best candidate of every wanted product.
     */
    private static final class Search {

        final int n, m;
        final long[][] in, out, s, posMax, nArr;
        final int[] cyc, products;
        final CycleItemSetting[] set;
        final long[] x, remAt, vAt;
        final Candidate[] best;
        final long stepWork, leafWork;
        boolean[] wanted;
        long nodes, maxNodes, layer;
        int depth;
        boolean started, finished, complete;

        Search(int n, int m, long[][] in, long[][] out, long[][] s, int[] cyc, int[] products,
            CycleItemSetting[] set) {
            this.n = n;
            this.m = m;
            this.in = in;
            this.out = out;
            this.s = s;
            this.cyc = cyc;
            this.products = products;
            this.set = set;
            this.x = new long[n];
            this.remAt = new long[n];
            this.vAt = new long[n];
            this.nArr = new long[n + 1][m];
            this.best = new Candidate[products.length];
            this.posMax = new long[m][n + 1];
            for (int k : cyc) for (int j = n - 1; j >= 0; j--) posMax[k][j] = Math.max(posMax[k][j + 1], s[k][j]);
            stepWork = m + cyc.length + 1;
            leafWork = (long) cyc.length * (n + 1) + products.length;
        }

        void begin(long t, boolean[] wanted, long budget) {
            this.wanted = wanted;
            Arrays.fill(best, null);
            nodes = 0;
            maxNodes = budget;
            layer = t;
            started = false;
            finished = false;
            complete = true;
        }

        /** Enumerates for about {@code room} work; returns the work spent. {@link #finished} once the layer is done. */
        long run(long room) {
            long spent = 0;
            if (!started) {
                started = true;
                depth = -1;
                spent += n == 1 ? 1 + leafWork : 1;
                if (!enter(0, layer)) return spent;
            }
            while (true) {
                while (depth >= 0 && vAt[depth] < 0) depth--;
                if (depth < 0) {
                    finished = true;
                    return spent;
                }
                if (spent >= room) return spent;
                int j = depth;
                long v = vAt[j]--;
                spent += stepWork;
                if (!apply(j, v)) continue;
                long rem = remAt[j] - v;
                if (prune(j + 1, rem)) continue;
                spent += j + 2 == n ? 1 + leafWork : 1;
                if (!enter(j + 1, rem)) return spent;
            }
        }

        /** A node of the search tree: a leaf is scored at once, an inner node becomes the level to iterate. */
        private boolean enter(int j, long rem) {
            if (++nodes > maxNodes) {
                complete = false;
                finished = true;
                return false;
            }
            if (j == n - 1) {
                if (apply(j, rem)) leaf();
                return true;
            }
            remAt[j] = rem;
            vAt[j] = rem;
            depth = j;
            return true;
        }

        boolean apply(int j, long v) {
            x[j] = v;
            long[] from = nArr[j], to = nArr[j + 1];
            try {
                for (int k = 0; k < m; k++) to[k] = Math.addExact(from[k], Math.multiplyExact(s[k][j], v));
            } catch (ArithmeticException e) {
                return false;
            }
            return true;
        }

        boolean prune(int next, long rem) {
            for (int k : cyc) if (satAdd(nArr[next][k], satMul(rem, posMax[k][next])) < 0) return true;
            return false;
        }

        void leaf() {
            long[] net = nArr[n];
            for (int k : cyc) if (net[k] < 0) return;
            long seedTotal = 0;
            try {
                for (int k : cyc) {
                    if (set[k].output) continue;
                    long produced = 0;
                    for (int i = 0; i < n; i++) produced = Math.addExact(produced, Math.multiplyExact(out[k][i], x[i]));
                    if (!tolOk(net[k], produced, set[k].tolerancePercent)) return;
                }
                for (int k : cyc) for (int i = 0; i < n; i++)
                    seedTotal = Math.addExact(seedTotal, Math.multiplyExact(in[k][i], x[i]));
            } catch (ArithmeticException e) {
                return;
            }
            long[] snap = null;
            for (int pi = 0; pi < products.length; pi++) {
                if (!wanted[pi]) continue;
                long np = net[products[pi]];
                if (np <= 0) continue;
                if (snap == null) snap = x.clone();
                Candidate c = new Candidate(snap, seedTotal, np);
                if (best[pi] == null || c.betterThan(best[pi])) best[pi] = c;
            }
        }
    }
}
