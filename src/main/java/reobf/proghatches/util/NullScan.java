package reobf.proghatches.util;

/**
 * Descending "last non-null element" scan over Object[] using sun.misc.Unsafe: reference slots are
 * read as raw integers purely for ZERO-TESTING (null encodes as all-zero bits under every HotSpot
 * oop flavor — compressed or not, and colored ZGC/Shenandoah pointers are still non-zero for
 * non-null), so with 4-byte compressed oops one aligned getLong tests two slots at once. The raw
 * values are never decoded into references.
 * <p>
 * A static self-probe verifies the layout assumptions (scale 4 with 8-aligned base, raw(null)==0,
 * raw(non-null)!=0) and any failure — missing Unsafe, denied memory-access methods on future JDKs,
 * scale 8, exotic VMs — permanently drops to the plain loop fallback, which is also the reference
 * semantics: index of the last non-null element at or below {@code from}, or -1.
 */
public final class NullScan {

    private static final sun.misc.Unsafe U;
    private static final long BASE;
    private static final boolean USABLE;

    static {
        sun.misc.Unsafe u = null;
        long base = 0;
        boolean ok = false;
        try {
            java.lang.reflect.Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (sun.misc.Unsafe) f.get(null);
            base = u.arrayBaseOffset(Object[].class);
            int scale = u.arrayIndexScale(Object[].class);
            if (scale == 4 && (base & 7) == 0) {
                // self-probe: raw null must read as 0, raw non-null as != 0, pairwise via getLong
                Object[] probe = { new Object(), null, null, new Object() };
                long w0 = u.getLong(probe, base);
                long w1 = u.getLong(probe, base + 8);
                ok = w0 != 0 && u.getInt(probe, base + 4L) == 0 // [obj, null]
                    && u.getInt(probe, base + 8L) == 0
                    && w1 != 0; // [null, obj]
            }
        } catch (Throwable t) {
            ok = false;
        }
        U = u;
        BASE = base;
        USABLE = ok;
    }

    private NullScan() {}

    /** Index of the last non-null element at or below {@code from} (clamped), or -1. */
    public static int lastNonNull(Object[] arr, int from) {
        int s = from >= arr.length ? arr.length - 1 : from;
        if (s < 0) return -1;
        if (!USABLE) {
            for (; s >= 0; s--) if (arr[s] != null) return s;
            return -1;
        }
        // peel an even-index top element so the remaining pairs (s-1, s) sit fully inside the
        // array and on one aligned long (never reads past the last element)
        if ((s & 1) == 0) {
            if (arr[s] != null) return s;
            s--;
        }
        for (; s >= 1; s -= 2) {
            if (U.getLong(arr, BASE + (((long) (s >> 1)) << 3)) != 0) {
                if (arr[s] != null) return s;
                if (arr[s - 1] != null) return s - 1;
            }
        }
        return -1;
    }
}
