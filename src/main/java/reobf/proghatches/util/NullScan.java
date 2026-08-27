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
 *
 * <h2>Why the Unsafe calls live in an embedded class file</h2>
 * The memory-access methods are terminally deprecated (JEP 471), so calling them inline raised a
 * javac {@code [removal]} warning per call site, and Eclipse additionally flags the type under its
 * "discouraged access / not API" rule, which {@code @SuppressWarnings} cannot silence once that
 * rule is set to error. So {@link Raw}'s implementation is compiled separately and its class file
 * is embedded below as base64; {@link #RAW} defines it at runtime through a child ClassLoader. No
 * compiler that builds the mod ever sees sun.misc.Unsafe, while the bytecode that actually runs is
 * a plain {@code invokevirtual sun/misc/Unsafe.getLong} — the very instruction this class used to
 * emit inline. {@code RAW} is {@code static final} and the implementation is the only one loaded,
 * so C2 devirtualizes the interface call and inlines it back down to the same intrinsic.
 * <p>
 * <b>MethodHandles were tried first and are not usable here.</b> Binding the four methods into
 * {@code static final} MethodHandles and calling {@code invokeExact} compiles and behaves fine on
 * JDK 17, 21 and 25, but reproducibly crashes HotSpot on JDK 8 (EXCEPTION_ACCESS_VIOLATION inside
 * {@code MethodHandle::linkToSpecial}, once C2 compiles the scan loop). 1.7.10 still runs on Java 8
 * for a lot of players, so that route is closed. Plain reflection is out for the opposite reason:
 * it is per-element work in a hot loop.
 *
 * <h2>Regenerating {@link #IMPL}</h2>
 * The embedded class is compiled from this source (kept out of the tree on purpose):
 *
 * <pre>
 * package reobf.proghatches.util;
 *
 * public final class NullScanRaw implements NullScan.Raw {
 *
 *     private static final sun.misc.Unsafe U = akka.util.Unsafe.instance;
 *
 *     public long base()                        { return U.arrayBaseOffset(Object[].class); }
 *     public int  scale()                       { return U.arrayIndexScale(Object[].class); }
 *     public long getLong(Object o, long off)   { return U.getLong(o, off); }
 *     public int  getInt (Object o, long off)   { return U.getInt (o, off); }
 * }
 * </pre>
 *
 * built with a JDK 8 javac (major 52 loads on every JDK from 8 up) against akka-actor and
 * scala-library, then base64'd:
 *
 * <pre>
 * javac -XDignore.symbol.file -cp akka-actor_2.11-2.3.3.jar;scala-library-2.11.5.jar \
 *       -d out NullScan.java NullScanRaw.java
 * base64 -w0 out/reobf/proghatches/util/NullScanRaw.class
 * </pre>
 *
 * {@code akka.util.Unsafe.instance} is just a public forward to {@code scala.concurrent.util
 * .Unsafe.instance}; both jars are in Forge 1.7.10's own library manifest (that is how FML supports
 * Scala mods), so they are on every client and server, and the public field hands the singleton
 * over without {@code setAccessible} — the part modern JDKs keep tightening.
 */
public final class NullScan {

    /** Implemented only by the embedded class file below. */
    public interface Raw {

        long base();

        int scale();

        long getLong(Object o, long off);

        int getInt(Object o, long off);
    }

    /** base64 of NullScanRaw.class; see the class javadoc for the source and build command. */
    private static final String IMPL =
        "yv66vgAAADQANAoACgAdCQAJAB4HAB8KACAAIQoAIAAiCgAgACMKACAAJAkAJQAmBwAnBwAoBwAqAQABVQEAEUxzdW4vbWlz"
            + "Yy9VbnNhZmU7AQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEABGJhc2UBAAMoKUoBAAVzY2FsZQEA"
            + "AygpSQEAB2dldExvbmcBABYoTGphdmEvbGFuZy9PYmplY3Q7SilKAQAGZ2V0SW50AQAWKExqYXZhL2xhbmcvT2JqZWN0O0op"
            + "SQEACDxjbGluaXQ+AQAKU291cmNlRmlsZQEAEE51bGxTY2FuUmF3LmphdmEMAA4ADwwADAANAQATW0xqYXZhL2xhbmcvT2Jq"
            + "ZWN0OwcALQwALgAvDAAwAC8MABYAFwwAGAAZBwAxDAAyAA0BACJyZW9iZi9wcm9naGF0Y2hlcy91dGlsL051bGxTY2FuUmF3"
            + "AQAQamF2YS9sYW5nL09iamVjdAcAMwEAI3Jlb2JmL3Byb2doYXRjaGVzL3V0aWwvTnVsbFNjYW4kUmF3AQADUmF3AQAMSW5u"
            + "ZXJDbGFzc2VzAQAPc3VuL21pc2MvVW5zYWZlAQAPYXJyYXlCYXNlT2Zmc2V0AQAUKExqYXZhL2xhbmcvQ2xhc3M7KUkBAA9h"
            + "cnJheUluZGV4U2NhbGUBABBha2thL3V0aWwvVW5zYWZlAQAIaW5zdGFuY2UBAB9yZW9iZi9wcm9naGF0Y2hlcy91dGlsL051"
            + "bGxTY2FuADEACQAKAAEACwABABoADAANAAAABgABAA4ADwABABAAAAAdAAEAAQAAAAUqtwABsQAAAAEAEQAAAAYAAQAAAAoA"
            + "AQASABMAAQAQAAAAIgACAAEAAAAKsgACEgO2AASFrQAAAAEAEQAAAAYAAQAAAA8AAQAUABUAAQAQAAAAIQACAAEAAAAJsgAC"
            + "EgO2AAWsAAAAAQARAAAABgABAAAAEwABABYAFwABABAAAAAhAAQABAAAAAmyAAIrILYABq0AAAABABEAAAAGAAEAAAAXAAEA"
            + "GAAZAAEAEAAAACEABAAEAAAACbIAAisgtgAHrAAAAAEAEQAAAAYAAQAAABsACAAaAA8AAQAQAAAAHwABAAAAAAAHsgAIswAC"
            + "sQAAAAEAEQAAAAYAAQAAAAwAAgAbAAAAAgAcACwAAAAKAAEACwApACsGCQ==";

    /** Defines exactly one class, delegating everything else (Raw, Unsafe, akka) to the parent. */
    private static final class Defining extends ClassLoader {

        Defining(ClassLoader parent) {
            super(parent);
        }

        Class<?> define(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    private static final Raw RAW;
    private static final long BASE;
    private static final boolean USABLE;

    static {
        Raw raw = null;
        long base = 0;
        boolean ok = false;
        try {
            raw = (Raw) new Defining(NullScan.class.getClassLoader())
                .define("reobf.proghatches.util.NullScanRaw", java.util.Base64.getDecoder().decode(IMPL))
                .getDeclaredConstructor()
                .newInstance();
            base = raw.base();
            int scale = raw.scale();
            if (scale == 4 && (base & 7) == 0) {
                // self-probe: raw null must read as 0, raw non-null as != 0, pairwise via getLong
                Object[] probe = { new Object(), null, null, new Object() };
                long w0 = raw.getLong(probe, base);
                long w1 = raw.getLong(probe, base + 8);
                ok = w0 != 0 && raw.getInt(probe, base + 4L) == 0 // [obj, null]
                    && raw.getInt(probe, base + 8L) == 0
                    && w1 != 0; // [null, obj]
            }
        } catch (Throwable t) {
            ok = false;
        }
        RAW = ok ? raw : null;
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
            if (RAW.getLong(arr, BASE + (((long) (s >> 1)) << 3)) != 0) {
                if (arr[s] != null) return s;
                if (arr[s - 1] != null) return s - 1;
            }
        }
        return -1;
    }
}
