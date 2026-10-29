package reobf.proghatches.gt.metatileentity.cycle;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import reobf.proghatches.gt.metatileentity.cycle.CycleWarning.Code;

/** Dependency-free checks of the cyclic balancing solver; run with the testCyclicAnalysis Gradle task. */
public final class CycleAnalyzerTest {

    private static final List<CycleWarning<String>> NO_WARNINGS = Collections.emptyList();

    public static void main(String[] args) {
        t1Example1();
        t2TargetTooLow();
        t3Multiplier();
        t4Example2();
        t5ToleranceZero();
        t6ExactBalancing();
        t7ToleranceBoundary();
        t8Catalyst();
        t9NoProduct();
        t9bUnsolvable();
        t10TooManyPatterns();
        t11TooManyItems();
        t12BigAmounts();
        t13TieBreaks();
        t14Dedupe();
        t15NoNetInput();
        t16ThinBand();
        t17MergingAndValidation();
        t18Determinism();
        t19Kernel();
        t20Lp();
        t21Oracle();
        t22Sliced();
        System.out.println("CycleAnalyzerTest passed");
    }

    private static void t22Sliced() {
        Random rnd = new Random(20261024L);
        long[] budgets = { 1, 7, 1000 };
        CycleLimits[] limits = { null, new CycleLimits(8, 16, 400, 2_000_000L, 5_000L),
            new CycleLimits(8, 16, 400, 2_000_000L, 200_000L) };
        int runs = 0;
        for (int it = 0; it < 150; it++) {
            int n = 1 + rnd.nextInt(6), m = 2 + rnd.nextInt(5);
            List<CyclePattern<String>> ps = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                List<CycleAmount<String>> in = new ArrayList<>(), out = new ArrayList<>();
                for (int k : pick(rnd, m, 1 + rnd.nextInt(3))) in.add(amt("I" + k, 1 + rnd.nextInt(6)));
                for (int k : pick(rnd, m, 1 + rnd.nextInt(2))) out.add(amt("I" + k, 1 + rnd.nextInt(6)));
                ps.add(pat(i, in, out));
            }
            Map<String, CycleItemSetting> st = new HashMap<>();
            int[] tols = { 0, 5, 25, 100 };
            for (int k = 0; k < m; k++) st.put("I" + k, set(1 + rnd.nextInt(1000), rnd.nextBoolean(), tols[rnd.nextInt(4)]));
            for (CycleLimits lim : limits) {
                if (lim != null && n > lim.maxPatterns) continue;
                CycleAnalysis<String> whole = CycleAnalyzer.analyze(ps, st, 2, lim);
                for (long budget : budgets) {
                    CycleAnalyzer.Job<String> job = new CycleAnalyzer.Job<>(ps, st, 2, lim);
                    int steps = 0;
                    while (!job.step(budget)) steps++;
                    check(job.isDone() && job.result() != null, "T22 #" + it + " not done");
                    same(whole, job.result(), "T22 #" + it + " budget " + budget + " limits " + lim + " " + ps);
                    if (budget == 1) check(steps > 0 || whole.getPatternCount() == 0 || whole.getBalancings().isEmpty(),
                        "T22 #" + it + " finished in one step");
                    runs++;
                }
            }
        }
        System.out.println("T22 sliced: " + runs + " runs matched");
    }

    private static void same(CycleAnalysis<String> a, CycleAnalysis<String> b, String msg) {
        eq(a.getItems(), b.getItems(), msg + " items");
        eq(a.getCyclic(), b.getCyclic(), msg + " cyclic");
        eq(a.getProducts(), b.getProducts(), msg + " products");
        eq(a.getUnsolved(), b.getUnsolved(), msg + " unsolved");
        eq(a.getWarnings(), b.getWarnings(), msg + " warnings");
        eq(a.getNodesVisited(), b.getNodesVisited(), msg + " nodes");
        eq(a.getBalancings().size(), b.getBalancings().size(), msg + " balancings");
        for (int i = 0; i < a.getBalancings().size(); i++) {
            CycleBalancing<String> x = a.getBalancings().get(i), y = b.getBalancings().get(i);
            check(Arrays.equals(x.getRuns(), y.getRuns()), msg + " runs " + i);
            eq(x.getProducts(), y.getProducts(), msg + " balancing products " + i);
            eq(x.getNetInputs(), y.getNetInputs(), msg + " net inputs " + i);
            eq(x.getNetOutputs(), y.getNetOutputs(), msg + " net outputs " + i);
            eq(x.getSeedNeed(), y.getSeedNeed(), msg + " seed need " + i);
            check(x.isPublishable() == y.isPublishable(), msg + " publishable " + i);
        }
    }

    private static List<CyclePattern<String>> example1() {
        return pats(
            pat(0, l(amt("C", 1), amt("D", 1)), l(amt("E", 1))),
            pat(1, l(amt("E", 1), amt("F", 1)), l(amt("C", 2))));
    }

    private static CycleAnalysis<String> runExample1(long targetC, long targetE, long multiplier) {
        return CycleAnalyzer.analyze(example1(), map("C", set(targetC, true, 0), "E", set(targetE, true, 0)), multiplier, null);
    }

    private static void t1Example1() {
        CycleAnalysis<String> a = runExample1(10, 10, 1);
        eq(keys("C", "D", "E", "F"), a.getItems(), "T1 items");
        eq(keys("C", "E"), a.getCyclic(), "T1 cyclic");
        eq(keys("C", "E"), a.getProducts(), "T1 products");
        eq(2, a.getBalancings().size(), "T1 balancings");
        eq(NO_WARNINGS, a.getWarnings(), "T1 warnings");
        eq(0, a.getUnsolved().size(), "T1 unsolved");

        CycleBalancing<String> b0 = a.getBalancings().get(0);
        runs(b0, "T1 B0", 1, 1);
        eq(keys("C"), b0.getProducts(), "T1 B0 products");
        eq(l(amt("D", 1), amt("F", 1)), b0.getNetInputs(), "T1 B0 netInputs");
        eq(l(amt("C", 1)), b0.getNetOutputs(), "T1 B0 netOutputs");
        eq(l(), b0.getSurplus(), "T1 B0 surplus");
        eq(l(amt("C", 1), amt("E", 1)), b0.getSeeds(), "T1 B0 seeds");
        eq(l(amt("C", 1), amt("E", 1)), b0.getSeedNeed(), "T1 B0 seedNeed");
        check(b0.isPublishable(), "T1 B0 publishable");
        eq(2L, b0.getTotalRuns(), "T1 B0 totalRuns");
        check(Arrays.equals(new int[] { 0, 1 }, b0.getPatternIds()), "T1 B0 patternIds");

        CycleBalancing<String> b1 = a.getBalancings().get(1);
        runs(b1, "T1 B1", 2, 1);
        eq(keys("E"), b1.getProducts(), "T1 B1 products");
        eq(l(amt("D", 2), amt("F", 1)), b1.getNetInputs(), "T1 B1 netInputs");
        eq(l(amt("E", 1)), b1.getNetOutputs(), "T1 B1 netOutputs");
        eq(l(), b1.getSurplus(), "T1 B1 surplus");
        eq(l(amt("C", 2), amt("E", 1)), b1.getSeeds(), "T1 B1 seeds");
        check(b1.isPublishable(), "T1 B1 publishable");
        eq(2, a.getPublishable().size(), "T1 publishable");
    }

    private static void t2TargetTooLow() {
        CycleAnalysis<String> a = runExample1(1, 1, 1);
        eq(2, a.getBalancings().size(), "T2 balancings");
        check(a.getBalancings().get(0).isPublishable(), "T2 B0 publishable");
        check(!a.getBalancings().get(1).isPublishable(), "T2 B1 not publishable");
        eq(ws(w(Code.SEED_OVER_TARGET, "C", 2, 1)), a.getWarnings(), "T2 warnings");
        eq(1, a.getPublishable().size(), "T2 publishable");
    }

    private static void t3Multiplier() {
        CycleAnalysis<String> a = runExample1(3, 3, 3);
        CycleBalancing<String> b0 = a.getBalancings().get(0), b1 = a.getBalancings().get(1);
        eq(l(amt("C", 3), amt("E", 3)), b0.getSeedNeed(), "T3 B0 seedNeed");
        eq(l(amt("C", 1), amt("E", 1)), b0.getSeeds(), "T3 B0 seeds");
        check(b0.isPublishable(), "T3 B0 publishable");
        eq(l(amt("C", 6), amt("E", 3)), b1.getSeedNeed(), "T3 B1 seedNeed");
        check(!b1.isPublishable(), "T3 B1 not publishable");
        eq(ws(w(Code.SEED_OVER_TARGET, "C", 6, 3)), a.getWarnings(), "T3 warnings");
    }

    private static List<CyclePattern<String>> example2() {
        return pats(
            pat(0, l(amt("A", 1), amt("B", 1)), l(amt("C", 100))),
            pat(1, l(amt("C", 99)), l(amt("A", 2))));
    }

    private static void t4Example2() {
        CycleAnalysis<String> a = CycleAnalyzer
            .analyze(example2(), map("A", set(1, true, 0), "C", set(99, false, 25)), 1, null);
        eq(keys("A", "B", "C"), a.getItems(), "T4 items");
        eq(keys("A", "C"), a.getCyclic(), "T4 cyclic");
        eq(keys("A"), a.getProducts(), "T4 products");
        eq(1, a.getBalancings().size(), "T4 balancings");
        eq(NO_WARNINGS, a.getWarnings(), "T4 warnings");
        CycleBalancing<String> b = a.getBalancings().get(0);
        runs(b, "T4", 1, 1);
        eq(l(amt("B", 1)), b.getNetInputs(), "T4 netInputs");
        eq(l(amt("A", 1)), b.getNetOutputs(), "T4 netOutputs");
        eq(l(amt("C", 1)), b.getSurplus(), "T4 surplus");
        eq(l(amt("A", 1), amt("C", 99)), b.getSeeds(), "T4 seeds");
        check(b.isPublishable(), "T4 publishable");
    }

    /** Tolerance 0 makes 100 C = 99 C' an equality: the exact cycle needs 199 runs and is found directly. */
    private static void t5ToleranceZero() {
        CycleAnalysis<String> a = CycleAnalyzer
            .analyze(example2(), map("A", set(1, true, 0), "C", set(99, false, 0)), 1, CycleLimits.DEFAULT);
        eq(1, a.getBalancings().size(), "T5 balancings");
        eq(0, a.getUnsolved().size(), "T5 unsolved");
        CycleBalancing<String> b = a.getBalancings().get(0);
        runs(b, "T5", 99, 100);
        check(!b.isPublishable(), "T5 not publishable");
        eq(ws(w(Code.SEED_OVER_TARGET, "A", 99, 1), w(Code.SEED_OVER_TARGET, "C", 9900, 99)), a.getWarnings(), "T5 warnings");
        check(a.getNodesVisited() < 1000, "T5 nodes: " + a.getNodesVisited());
    }

    private static void t6ExactBalancing() {
        CycleAnalysis<String> a = CycleAnalyzer
            .analyze(example2(), map("A", set(99, true, 0), "C", set(9900, false, 0)), 1, null);
        eq(NO_WARNINGS, a.getWarnings(), "T6 warnings");
        eq(1, a.getBalancings().size(), "T6 balancings");
        CycleBalancing<String> b = a.getBalancings().get(0);
        runs(b, "T6", 99, 100);
        eq(l(amt("B", 99)), b.getNetInputs(), "T6 netInputs");
        eq(l(amt("A", 101)), b.getNetOutputs(), "T6 netOutputs");
        eq(l(), b.getSurplus(), "T6 surplus");
        eq(l(amt("A", 99), amt("C", 9900)), b.getSeeds(), "T6 seeds");
        check(b.isPublishable(), "T6 publishable");
    }

    private static void t7ToleranceBoundary() {
        CycleAnalysis<String> a = CycleAnalyzer
            .analyze(example2(), map("A", set(1, true, 0), "C", set(99, false, 1)), 1, null);
        eq(1, a.getBalancings().size(), "T7 balancings");
        runs(a.getBalancings().get(0), "T7", 1, 1);
        eq(l(amt("C", 1)), a.getBalancings().get(0).getSurplus(), "T7 surplus");
    }

    private static void t8Catalyst() {
        List<CyclePattern<String>> ps = pats(pat(0, l(amt("A", 1), amt("K", 1)), l(amt("B", 1), amt("K", 1))));
        CycleAnalysis<String> a = CycleAnalyzer.analyze(ps, null, 1, null);
        eq(keys("K"), a.getCyclic(), "T8 cyclic");
        eq(keys("B"), a.getProducts(), "T8 products");
        eq(1, a.getBalancings().size(), "T8 balancings");
        CycleBalancing<String> b = a.getBalancings().get(0);
        runs(b, "T8", 1);
        eq(l(amt("A", 1)), b.getNetInputs(), "T8 netInputs");
        eq(l(amt("B", 1)), b.getNetOutputs(), "T8 netOutputs");
        eq(l(amt("K", 1)), b.getSeeds(), "T8 seeds");
        check(!b.isPublishable(), "T8 not publishable");
        eq(ws(w(Code.SEED_OVER_TARGET, "K", 1, 0)), a.getWarnings(), "T8 warnings");

        a = CycleAnalyzer.analyze(ps, map("K", set(1, false, 0)), 1, null);
        check(a.getBalancings().get(0).isPublishable(), "T8 publishable with target 1");
        eq(NO_WARNINGS, a.getWarnings(), "T8 warnings with target 1");
    }

    private static List<CyclePattern<String>> swap() {
        return pats(pat(0, l(amt("X", 1)), l(amt("Y", 1))), pat(1, l(amt("Y", 1)), l(amt("X", 1))));
    }

    private static void t9NoProduct() {
        CycleAnalysis<String> a = CycleAnalyzer.analyze(swap(), null, 1, null);
        eq(keys("X", "Y"), a.getCyclic(), "T9 cyclic");
        eq(keys(), a.getProducts(), "T9 products");
        eq(ws(w(Code.NO_PRODUCT, null, 0, 0)), a.getWarnings(), "T9 warnings");
        eq(0, a.getBalancings().size(), "T9 balancings");
    }

    private static void t9bUnsolvable() {
        CycleAnalysis<String> a = CycleAnalyzer.analyze(swap(), map("X", set(0, true, 0)), 1, null);
        eq(keys("X"), a.getProducts(), "T9b products");
        eq(keys("X"), a.getUnsolved(), "T9b unsolved");
        eq(ws(w(Code.NO_BALANCING, "X", 0, 0)), a.getWarnings(), "T9b warnings");
        eq(0, a.getBalancings().size(), "T9b balancings");
    }

    private static void t10TooManyPatterns() {
        List<CyclePattern<String>> ps = new ArrayList<>();
        for (int i = 0; i < 7; i++) ps.add(pat(i, l(amt("A" + i, 1)), l(amt("B" + i, 1))));
        CycleAnalysis<String> a = CycleAnalyzer.analyze(ps, null, 1, null);
        eq(ws(w(Code.TOO_MANY_PATTERNS, null, 7, 6)), a.getWarnings(), "T10 warnings");
        eq(0, a.getBalancings().size(), "T10 balancings");
    }

    private static void t11TooManyItems() {
        List<CycleAmount<String>> in = new ArrayList<>();
        for (int i = 0; i < 16; i++) in.add(amt("I" + i, 1));
        CycleAnalysis<String> a = CycleAnalyzer.analyze(pats(pat(0, in, l(amt("O", 1)))), null, 1, null);
        eq(ws(w(Code.TOO_MANY_ITEMS, null, 17, 16)), a.getWarnings(), "T11 warnings");
        eq(0, a.getBalancings().size(), "T11 balancings");
    }

    private static void t12BigAmounts() {
        long big = 1L << 62;
        List<CyclePattern<String>> ps = pats(pat(0, l(amt("X", 1)), l(amt("C", big))), pat(1, l(amt("C", 1)), l(amt("Y", 1))));
        CycleAnalysis<String> a = CycleAnalyzer.analyze(ps, map("C", set(1, false, 100)), 1, null);
        eq(keys("Y"), a.getProducts(), "T12 products");
        eq(1, a.getBalancings().size(), "T12 balancings");
        CycleBalancing<String> b = a.getBalancings().get(0);
        runs(b, "T12", 1, 1);
        eq(l(amt("X", 1)), b.getNetInputs(), "T12 netInputs");
        eq(l(amt("Y", 1)), b.getNetOutputs(), "T12 netOutputs");
        eq(l(amt("C", big - 1)), b.getSurplus(), "T12 surplus");
        eq(l(amt("C", 1)), b.getSeeds(), "T12 seeds");
        check(b.isPublishable(), "T12 publishable");
        eq(NO_WARNINGS, a.getWarnings(), "T12 warnings");
    }

    private static void t13TieBreaks() {
        CycleAnalysis<String> a = CycleAnalyzer
            .analyze(pats(pat(0, l(amt("S", 1)), l(amt("T", 1))), pat(1, l(amt("R", 1)), l(amt("T", 1)))), null, 1, null);
        runs(a.getBalancings().get(0), "T13a", 0, 1);
        eq(l(amt("R", 1)), a.getBalancings().get(0).getNetInputs(), "T13a netInputs");

        a = CycleAnalyzer.analyze(
            pats(
                pat(0, l(amt("K", 1), amt("A", 1)), l(amt("K", 1), amt("T", 1))),
                pat(1, l(amt("K", 2), amt("B", 1)), l(amt("K", 2), amt("T", 1)))),
            map("K", set(10, false, 0)),
            1,
            null);
        runs(a.getBalancings().get(0), "T13b", 1, 0);

        a = CycleAnalyzer
            .analyze(pats(pat(0, l(amt("A", 1)), l(amt("T", 2))), pat(1, l(amt("B", 1)), l(amt("T", 1)))), null, 1, null);
        runs(a.getBalancings().get(0), "T13c", 1, 0);
    }

    private static void t14Dedupe() {
        CycleAnalysis<String> a = CycleAnalyzer.analyze(
            pats(pat(0, l(amt("C", 1), amt("E", 1), amt("X", 1)), l(amt("C", 2), amt("E", 2)))),
            map("C", set(10, true, 0), "E", set(10, true, 0)),
            1,
            null);
        eq(1, a.getBalancings().size(), "T14 balancings");
        CycleBalancing<String> b = a.getBalancings().get(0);
        eq(keys("C", "E"), b.getProducts(), "T14 products");
        eq(l(amt("X", 1)), b.getNetInputs(), "T14 netInputs");
        eq(l(amt("C", 1), amt("E", 1)), b.getNetOutputs(), "T14 netOutputs");
        eq(l(amt("C", 1), amt("E", 1)), b.getSeeds(), "T14 seeds");
        check(b.isPublishable(), "T14 publishable");
    }

    private static void t15NoNetInput() {
        CycleAnalysis<String> a = CycleAnalyzer
            .analyze(pats(pat(0, l(amt("K", 1)), l(amt("K", 2)))), map("K", set(10, true, 0)), 1, null);
        eq(1, a.getBalancings().size(), "T15 balancings");
        CycleBalancing<String> b = a.getBalancings().get(0);
        runs(b, "T15", 1);
        eq(l(amt("K", 1)), b.getNetOutputs(), "T15 netOutputs");
        check(!b.isPublishable(), "T15 not publishable");
        eq(ws(w(Code.NO_NET_INPUT, "K", 0, 0)), a.getWarnings(), "T15 warnings");
    }

    /** 1000 C per run against 1003 C with 1 percent tolerance: the smallest integer point of the band is (78, 77). */
    private static void t16ThinBand() {
        List<CyclePattern<String>> ps = pats(
            pat(0, l(amt("A", 1), amt("B", 1)), l(amt("C", 1000))),
            pat(1, l(amt("C", 1003)), l(amt("A", 2))));
        Map<String, CycleItemSetting> st = map("A", set(1_000_000, true, 0), "C", set(1_000_000, false, 1));
        long t0 = System.nanoTime();
        CycleAnalysis<String> a = CycleAnalyzer.analyze(ps, st, 1, null);
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("T16 thin band: " + ms + " ms, " + a.getNodesVisited() + " nodes");
        eq(NO_WARNINGS, a.getWarnings(), "T16 warnings");
        eq(1, a.getBalancings().size(), "T16 balancings");
        CycleBalancing<String> b = a.getBalancings().get(0);
        runs(b, "T16", 78, 77);
        eq(l(amt("B", 78)), b.getNetInputs(), "T16 netInputs");
        eq(l(amt("A", 76)), b.getNetOutputs(), "T16 netOutputs");
        eq(l(amt("C", 769)), b.getSurplus(), "T16 surplus");
        eq(l(amt("A", 78), amt("C", 77231)), b.getSeeds(), "T16 seeds");
        check(b.isPublishable(), "T16 publishable");

        a = CycleAnalyzer.analyze(ps, st, 1, new CycleLimits(6, 16, 10, 2_000_000L));
        eq(ws(w(Code.SEARCH_LIMIT, "A", 10, 0)), a.getWarnings(), "T16 limit warnings");
        eq(keys("A"), a.getUnsolved(), "T16 limit unsolved");
        eq(0, a.getBalancings().size(), "T16 limit balancings");

        a = CycleAnalyzer.analyze(ps, st, 1, new CycleLimits(6, 16, 400, 10));
        eq(ws(w(Code.NOT_MINIMAL, "A", 0, 0)), a.getWarnings(), "T16 enum limit warnings");
        eq(1, a.getBalancings().size(), "T16 enum limit balancings");
        runs(a.getBalancings().get(0), "T16 enum limit", 78, 77);
    }

    private static void t17MergingAndValidation() {
        CyclePattern<String> p = new CyclePattern<>(0, l(amt("A", 1), amt("A", 2)), l(amt("B", 1)));
        eq(l(amt("A", 3)), p.inputs, "T17 merged inputs");
        eq(l(amt("B", 1)), p.outputs, "T17 outputs");

        boolean thrown = false;
        try {
            new CycleAmount<>("A", 0);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check(thrown, "T17 zero amount rejected");

        thrown = false;
        try {
            CycleAnalyzer.analyze(example1(), null, 0, null);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check(thrown, "T17 multiplier 0 rejected");

        CycleAnalysis<String> a = CycleAnalyzer.analyze(new ArrayList<CyclePattern<String>>(), null, 1, null);
        eq(NO_WARNINGS, a.getWarnings(), "T17 empty warnings");
        eq(0, a.getBalancings().size(), "T17 empty balancings");
    }

    private static void t18Determinism() {
        CycleAnalysis<String> a = runExample1(10, 10, 1), b = runExample1(10, 10, 1);
        eq(a.getBalancings().size(), b.getBalancings().size(), "T18 balancings");
        for (int i = 0; i < a.getBalancings().size(); i++) check(
            Arrays.equals(a.getBalancings().get(i).getRuns(), b.getBalancings().get(i).getRuns()),
            "T18 runs of balancing " + i);
        eq(a.getWarnings(), b.getWarnings(), "T18 warnings");
    }

    private static void t19Kernel() {
        BigInteger[][] k = CycleAnalyzer.integerKernel(bigRows(new long[][] { { 100, -99 } }), 2);
        eq(1, k.length, "T19 kernel rank");
        check(k[0][0].abs().equals(BigInteger.valueOf(99)) && k[0][1].abs().equals(BigInteger.valueOf(100))
            && k[0][0].signum() == k[0][1].signum(), "T19 kernel vector: " + Arrays.toString(k[0]));

        k = CycleAnalyzer.integerKernel(bigRows(new long[][] { { 1, 1, 1 }, { 1, 1, 1 } }), 3);
        eq(2, k.length, "T19 duplicate row rank");
        for (BigInteger[] v : k) eq(BigInteger.ZERO, v[0].add(v[1]).add(v[2]), "T19 duplicate row kernel");

        k = CycleAnalyzer.integerKernel(bigRows(new long[][] { { 1, 0 }, { 0, 1 } }), 2);
        eq(0, k.length, "T19 full rank");

        k = CycleAnalyzer.integerKernel(bigRows(new long[][] { { 2, -3, 0 }, { 0, 5, -7 } }), 3);
        eq(1, k.length, "T19 chain rank");
        check(k[0][0].abs().equals(BigInteger.valueOf(21)) && k[0][1].abs().equals(BigInteger.valueOf(14))
            && k[0][2].abs().equals(BigInteger.valueOf(10)), "T19 chain kernel: " + Arrays.toString(k[0]));
    }

    private static void t20Lp() {
        // min x1 + x2 s.t. 100 x1 - 99 x2 >= 0, 99 x2 - 95 x1 >= 0, 2 x2 - x1 >= 1
        List<BigInteger[]> a = new ArrayList<>();
        List<BigInteger> b = new ArrayList<>();
        a.add(big(100, -99));
        b.add(BigInteger.ZERO);
        a.add(big(-95, 99));
        b.add(BigInteger.ZERO);
        a.add(big(-1, 2));
        b.add(BigInteger.ONE);
        ExactLP lp = new ExactLP(a, b, big(1, 1));
        check(lp.solve(), "T20 feasible");
        BigInteger[] x = lp.xNum();
        // optimum on 100 x1 = 99 x2 and 2 x2 - x1 = 1: x1 = 99/101, x2 = 100/101, objective 199/101
        BigInteger d = lp.den();
        eq(BigInteger.valueOf(99).multiply(d), x[0].multiply(BigInteger.valueOf(101)), "T20 x1");
        eq(BigInteger.valueOf(100).multiply(d), x[1].multiply(BigInteger.valueOf(101)), "T20 x2");
        eq(BigInteger.valueOf(199).multiply(d), lp.objNum().multiply(BigInteger.valueOf(101)), "T20 objective");

        a.add(big(-1, 0));
        b.add(BigInteger.ZERO);
        lp = new ExactLP(a, b, big(1, 1));
        check(!lp.solve(), "T20 infeasible with x1 <= 0");
    }

    /** Random small groups against the brute force layer search. */
    private static void t21Oracle() {
        Random rnd = new Random(20261023L);
        int compared = 0, withSolution = 0;
        for (int it = 0; it < 400; it++) {
            int n = 1 + rnd.nextInt(3), m = 2 + rnd.nextInt(3);
            List<CyclePattern<String>> ps = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                List<CycleAmount<String>> in = new ArrayList<>(), out = new ArrayList<>();
                for (int k : pick(rnd, m, 1 + rnd.nextInt(2))) in.add(amt("I" + k, 1 + rnd.nextInt(4)));
                for (int k : pick(rnd, m, 1 + rnd.nextInt(2))) out.add(amt("I" + k, 1 + rnd.nextInt(4)));
                ps.add(pat(i, in, out));
            }
            Map<String, CycleItemSetting> st = new HashMap<>();
            int[] tols = { 0, 5, 25, 100 };
            for (int k = 0; k < m; k++) st.put("I" + k, set(1_000_000_000L, rnd.nextBoolean(), tols[rnd.nextInt(4)]));
            Model md = model(ps, st);
            if (md.products.length == 0) continue;
            long[][] oracle = Brute.solve(md, 24, 20_000_000L);
            if (oracle == null) continue;
            CycleAnalysis<String> a = CycleAnalyzer.analyze(ps, st, 1, null);
            for (CycleWarning<String> w : a.getWarnings()) check(
                w.code != Code.SEARCH_LIMIT && w.code != Code.NOT_MINIMAL && w.code != Code.OVERFLOW,
                "T21 #" + it + " unexpected " + w + " for " + ps);
            for (int pi = 0; pi < md.products.length; pi++) {
                String key = md.prodKeys.get(pi);
                long[] got = null;
                for (CycleBalancing<String> b : a.getBalancings()) if (b.getProducts().contains(key)) got = b.getRuns();
                compared++;
                if (oracle[pi] != null) {
                    withSolution++;
                    check(got != null, "T21 #" + it + " " + key + ": oracle " + Arrays.toString(oracle[pi]) + ", solver none, " + ps + " " + st);
                    check(Arrays.equals(oracle[pi], got), "T21 #" + it + " " + key + ": oracle " + Arrays.toString(oracle[pi]) + ", solver " + Arrays.toString(got) + ", " + ps + " " + st);
                } else if (got != null) {
                    long sum = 0;
                    for (long v : got) sum += v;
                    check(sum > 24, "T21 #" + it + " " + key + ": oracle found nothing within 24 runs, solver " + Arrays.toString(got) + ", " + ps + " " + st);
                } else {
                    check(a.getUnsolved().contains(key), "T21 #" + it + " " + key + " neither solved nor unsolved");
                }
            }
        }
        System.out.println("T21 oracle: " + compared + " products compared, " + withSolution + " with a solution");
        check(withSolution > 200, "T21 too few solvable instances: " + withSolution);
    }

    private static int[] pick(Random rnd, int m, int count) {
        List<Integer> all = new ArrayList<>();
        for (int i = 0; i < m; i++) all.add(i);
        Collections.shuffle(all, rnd);
        int[] r = new int[Math.min(count, m)];
        for (int i = 0; i < r.length; i++) r[i] = all.get(i);
        return r;
    }

    static final class Model {

        int n, m;
        long[][] in, out, s;
        int[] cyc, products;
        CycleItemSetting[] set;
        List<String> items = new ArrayList<>(), prodKeys = new ArrayList<>();
    }

    private static Model model(List<CyclePattern<String>> ps, Map<String, CycleItemSetting> settings) {
        Model md = new Model();
        LinkedHashMap<String, Integer> index = new LinkedHashMap<>();
        for (CyclePattern<String> p : ps) {
            for (CycleAmount<String> c : p.inputs) if (!index.containsKey(c.key)) index.put(c.key, index.size());
            for (CycleAmount<String> c : p.outputs) if (!index.containsKey(c.key)) index.put(c.key, index.size());
        }
        md.items.addAll(index.keySet());
        md.n = ps.size();
        md.m = md.items.size();
        md.in = new long[md.m][md.n];
        md.out = new long[md.m][md.n];
        md.s = new long[md.m][md.n];
        for (int i = 0; i < md.n; i++) {
            for (CycleAmount<String> c : ps.get(i).inputs) md.in[index.get(c.key)][i] = c.amount;
            for (CycleAmount<String> c : ps.get(i).outputs) md.out[index.get(c.key)][i] = c.amount;
        }
        md.set = new CycleItemSetting[md.m];
        List<Integer> cyc = new ArrayList<>(), prod = new ArrayList<>();
        for (int k = 0; k < md.m; k++) {
            boolean consumed = false, produced = false;
            for (int i = 0; i < md.n; i++) {
                md.s[k][i] = md.out[k][i] - md.in[k][i];
                if (md.in[k][i] > 0) consumed = true;
                if (md.out[k][i] > 0) produced = true;
            }
            CycleItemSetting st = settings.get(md.items.get(k));
            md.set[k] = st == null ? CycleItemSetting.DEFAULT : st;
            boolean cyclic = consumed && produced;
            if (cyclic) cyc.add(k);
            if (cyclic ? md.set[k].output : produced) {
                prod.add(k);
                md.prodKeys.add(md.items.get(k));
            }
        }
        md.cyc = toArray(cyc);
        md.products = toArray(prod);
        return md;
    }

    /** Layer-by-layer enumeration, used as the reference answer. */
    static final class Brute {

        final int n, m;
        final long[][] in, out, s, posMax, nArr;
        final int[] cyc, products;
        final CycleItemSetting[] set;
        final long maxNodes;
        final long[] x;
        final long[][] solved, best;
        final long[] bestSeed, bestNp;
        long nodes;

        /** @return per product the runs of the first layer with a solution, or null if the node budget ran out */
        static long[][] solve(Model md, int maxLayer, long maxNodes) {
            Brute b = new Brute(md, maxNodes);
            int solvedCount = 0;
            for (int t = 1; t <= maxLayer; t++) {
                Arrays.fill(b.best, null);
                if (!b.enumerate(0, t)) return null;
                for (int pi = 0; pi < b.products.length; pi++) if (b.best[pi] != null && b.solved[pi] == null) {
                    b.solved[pi] = b.best[pi];
                    solvedCount++;
                }
                if (solvedCount == b.products.length) break;
            }
            return b.solved;
        }

        Brute(Model md, long maxNodes) {
            n = md.n;
            m = md.m;
            in = md.in;
            out = md.out;
            s = md.s;
            cyc = md.cyc;
            products = md.products;
            set = md.set;
            this.maxNodes = maxNodes;
            x = new long[n];
            nArr = new long[n + 1][m];
            solved = new long[products.length][];
            best = new long[products.length][];
            bestSeed = new long[products.length];
            bestNp = new long[products.length];
            posMax = new long[m][n + 1];
            for (int k : cyc) for (int j = n - 1; j >= 0; j--) posMax[k][j] = Math.max(posMax[k][j + 1], s[k][j]);
        }

        boolean enumerate(int j, long rem) {
            if (++nodes > maxNodes) return false;
            if (j == n - 1) {
                apply(j, rem);
                leaf();
                return true;
            }
            for (long v = rem; v >= 0; v--) {
                apply(j, v);
                if (!prune(j + 1, rem - v) && !enumerate(j + 1, rem - v)) return false;
            }
            return true;
        }

        void apply(int j, long v) {
            x[j] = v;
            for (int k = 0; k < m; k++) nArr[j + 1][k] = nArr[j][k] + s[k][j] * v;
        }

        boolean prune(int next, long rem) {
            for (int k : cyc) if (nArr[next][k] + rem * posMax[k][next] < 0) return true;
            return false;
        }

        void leaf() {
            long[] net = nArr[n];
            for (int k : cyc) if (net[k] < 0) return;
            long seedTotal = 0;
            for (int k : cyc) {
                if (set[k].output) continue;
                long produced = 0;
                for (int i = 0; i < n; i++) produced += out[k][i] * x[i];
                if (net[k] * 100 > (long) set[k].tolerancePercent * produced) return;
            }
            for (int k : cyc) for (int i = 0; i < n; i++) seedTotal += in[k][i] * x[i];
            for (int pi = 0; pi < products.length; pi++) {
                if (solved[pi] != null) continue;
                long np = net[products[pi]];
                if (np <= 0) continue;
                if (best[pi] == null || better(seedTotal, np, pi)) {
                    best[pi] = x.clone();
                    bestSeed[pi] = seedTotal;
                    bestNp[pi] = np;
                }
            }
        }

        boolean better(long seedTotal, long np, int pi) {
            if (seedTotal != bestSeed[pi]) return seedTotal < bestSeed[pi];
            if (np != bestNp[pi]) return np > bestNp[pi];
            for (int i = 0; i < n; i++) if (x[i] != best[pi][i]) return x[i] < best[pi][i];
            return false;
        }
    }

    private static int[] toArray(List<Integer> list) {
        int[] a = new int[list.size()];
        for (int i = 0; i < a.length; i++) a[i] = list.get(i);
        return a;
    }

    private static BigInteger[] big(long... v) {
        BigInteger[] r = new BigInteger[v.length];
        for (int i = 0; i < v.length; i++) r[i] = BigInteger.valueOf(v[i]);
        return r;
    }

    private static BigInteger[][] bigRows(long[][] rows) {
        BigInteger[][] r = new BigInteger[rows.length][];
        for (int i = 0; i < rows.length; i++) r[i] = big(rows[i]);
        return r;
    }

    private static CycleAmount<String> amt(String key, long amount) {
        return new CycleAmount<>(key, amount);
    }

    @SafeVarargs
    private static List<CycleAmount<String>> l(CycleAmount<String>... amounts) {
        return Arrays.asList(amounts);
    }

    private static CyclePattern<String> pat(int id, List<CycleAmount<String>> in, List<CycleAmount<String>> out) {
        return new CyclePattern<>(id, in, out);
    }

    @SafeVarargs
    private static List<CyclePattern<String>> pats(CyclePattern<String>... ps) {
        return Arrays.asList(ps);
    }

    private static CycleItemSetting set(long target, boolean output, int tol) {
        return new CycleItemSetting(target, output, tol);
    }

    private static Map<String, CycleItemSetting> map(Object... kv) {
        Map<String, CycleItemSetting> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], (CycleItemSetting) kv[i + 1]);
        return m;
    }

    private static List<String> keys(String... k) {
        return Arrays.asList(k);
    }

    private static CycleWarning<String> w(Code code, String key, long a, long b) {
        return new CycleWarning<>(code, key, -1, a, b);
    }

    @SafeVarargs
    private static List<CycleWarning<String>> ws(CycleWarning<String>... w) {
        return Arrays.asList(w);
    }

    private static void runs(CycleBalancing<String> b, String what, long... expected) {
        check(Arrays.equals(expected, b.getRuns()), what + " runs: expected " + Arrays.toString(expected) + ", got "
            + Arrays.toString(b.getRuns()));
    }

    private static void eq(Object expected, Object actual, String what) {
        check(expected.equals(actual), what + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }
}
