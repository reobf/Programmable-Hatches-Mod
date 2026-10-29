package reobf.proghatches.gt.metatileentity.cycle;

import java.util.Objects;

public final class CycleWarning<K> {

    public enum Severity {
        RED,
        YELLOW
    }

    public enum Code {

        TOO_MANY_PATTERNS(Severity.RED),
        TOO_MANY_ITEMS(Severity.RED),
        NO_PRODUCT(Severity.YELLOW),
        NO_BALANCING(Severity.RED),
        SEARCH_LIMIT(Severity.RED),
        NOT_MINIMAL(Severity.YELLOW),
        OVERFLOW(Severity.YELLOW),
        NO_NET_INPUT(Severity.RED),
        SEED_OVER_TARGET(Severity.RED),
        NEED_OVERFLOW(Severity.RED),

        INVALID_PATTERN(Severity.YELLOW),
        SUBSTITUTION(Severity.YELLOW),
        TOO_LARGE_TO_STAGE(Severity.YELLOW),
        STOCK_LOW(Severity.YELLOW),
        ROWS_HIDDEN(Severity.YELLOW),
        LANE_UNBOUND(Severity.RED);

        public final Severity severity;

        Code(Severity s) {
            severity = s;
        }
    }

    public final Code code;
    public final Severity severity;
    public final K key;
    public final int patternId;
    public final long a, b;

    public CycleWarning(Code code, K key, int patternId, long a, long b) {
        if (code == null) throw new NullPointerException("code");
        this.code = code;
        this.severity = code.severity;
        this.key = key;
        this.patternId = patternId;
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CycleWarning)) return false;
        CycleWarning<?> w = (CycleWarning<?>) o;
        return code == w.code && patternId == w.patternId && a == w.a && b == w.b && Objects.equals(key, w.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, key, patternId, a, b);
    }

    @Override
    public String toString() {
        return code + "(key=" + key + ", pattern=" + patternId + ", a=" + a + ", b=" + b + ")";
    }
}
