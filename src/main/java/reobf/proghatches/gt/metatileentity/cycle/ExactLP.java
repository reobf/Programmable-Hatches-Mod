package reobf.proghatches.gt.metatileentity.cycle;

import java.math.BigInteger;
import java.util.List;

/**
 * min c*x s.t. a_i*x >= b_i, x >= 0, with c >= 0. Dual simplex from the slack basis, Bland's rule, integer pivoting:
 * every entry is kept as an integer, the whole tableau is divided by {@code d}.
 */
final class ExactLP {

    private static final int MAX_PIVOTS = 100_000;

    private final int n, m, rhs;
    private final BigInteger[][] t;
    private final int[] basis, rowOf;
    private BigInteger d = BigInteger.ONE;
    int pivots;

    ExactLP(List<BigInteger[]> a, List<BigInteger> b, BigInteger[] c) {
        n = c.length;
        m = a.size();
        rhs = n + m;
        t = new BigInteger[m + 1][rhs + 1];
        basis = new int[m];
        rowOf = new int[rhs];
        for (int i = 0; i < m; i++) {
            BigInteger[] ai = a.get(i);
            BigInteger[] ti = t[i];
            for (int j = 0; j < n; j++) ti[j] = ai[j].negate();
            for (int j = n; j < rhs; j++) ti[j] = BigInteger.ZERO;
            ti[n + i] = BigInteger.ONE;
            ti[rhs] = b.get(i)
                .negate();
            basis[i] = n + i;
        }
        for (int j = 0; j < n; j++) {
            t[m][j] = c[j].negate();
            rowOf[j] = -1;
        }
        for (int j = n; j <= rhs; j++) t[m][j] = BigInteger.ZERO;
        for (int i = 0; i < m; i++) rowOf[n + i] = i;
    }

    /** @return false if infeasible */
    boolean solve() {
        int st;
        while ((st = pivotStep()) < 0) {}
        return st > 0;
    }

    /** @return -1 after one pivot, 1 once optimal, 0 if infeasible */
    int pivotStep() {
        int r = -1;
        for (int i = 0; i < m; i++) if (t[i][rhs].signum() < 0 && (r < 0 || basis[i] < basis[r])) r = i;
        if (r < 0) return 1;
        BigInteger[] tr = t[r], tm = t[m];
        int p = -1;
        for (int j = 0; j < rhs; j++) {
            if (tr[j].signum() >= 0) continue;
            if (p < 0 || tm[j].multiply(tr[p])
                .compareTo(tm[p].multiply(tr[j])) < 0) p = j;
        }
        if (p < 0) return 0;
        if (++pivots > MAX_PIVOTS) throw new IllegalStateException("simplex does not terminate");
        pivot(r, p);
        return -1;
    }

    long cells() {
        return (long) (m + 1) * (rhs + 1);
    }

    private void pivot(int r, int p) {
        BigInteger[] tr = t[r];
        BigInteger piv = tr[p];
        for (int i = 0; i <= m; i++) {
            if (i == r) continue;
            BigInteger[] ti = t[i];
            BigInteger f = ti[p];
            if (f.signum() == 0) {
                for (int j = 0; j <= rhs; j++) ti[j] = ti[j].multiply(piv)
                    .divide(d);
            } else {
                for (int j = 0; j <= rhs; j++) ti[j] = ti[j].multiply(piv)
                    .subtract(f.multiply(tr[j]))
                    .divide(d);
            }
        }
        rowOf[basis[r]] = -1;
        rowOf[p] = r;
        basis[r] = p;
        d = piv;
        if (d.signum() < 0) {
            d = d.negate();
            for (BigInteger[] ti : t) for (int j = 0; j <= rhs; j++) ti[j] = ti[j].negate();
        }
    }

    BigInteger den() {
        return d;
    }

    BigInteger objNum() {
        return t[m][rhs];
    }

    BigInteger[] xNum() {
        BigInteger[] x = new BigInteger[n];
        for (int j = 0; j < n; j++) x[j] = rowOf[j] < 0 ? BigInteger.ZERO : t[rowOf[j]][rhs];
        return x;
    }
}
