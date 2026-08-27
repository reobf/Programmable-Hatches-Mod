package reobf.proghatches.util;

import reobf.proghatches.main.asm.repack.objectwebasm.ClassWriter;
import reobf.proghatches.main.asm.repack.objectwebasm.MethodVisitor;
import reobf.proghatches.main.asm.repack.objectwebasm.Opcodes;
import reobf.proghatches.main.asm.repack.objectwebasm.Type;

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
 * <h2>Why the Unsafe calls are generated instead of written</h2>
 * The memory-access methods are terminally deprecated (JEP 471), so calling them inline raised a
 * javac {@code [removal]} warning per call site, and Eclipse additionally flags the type under its
 * "discouraged access / not API" rule, which {@code @SuppressWarnings} cannot silence once that
 * rule is set to error. So {@link Raw}'s implementation is emitted at runtime by {@link #emit()}
 * with the mod's repacked ASM and defined through a child ClassLoader. No compiler that builds the
 * mod ever sees sun.misc.Unsafe, while the bytecode that actually runs is a plain
 * {@code invokevirtual sun/misc/Unsafe.getLong} — the very instruction this class used to emit
 * inline. {@link #RAW} is {@code static final} and its class is the only implementation ever
 * loaded, so C2 devirtualizes the interface call and inlines it back down to the same intrinsic;
 * measured throughput is unchanged from the hand-written version on both JDK 8 and 21.
 * <p>
 * The generated class is equivalent to:
 *
 * <pre>
 * public final class NullScanRaw implements NullScan.Raw {
 *
 *     private static final sun.misc.Unsafe U = akka.util.Unsafe.instance;
 *
 *     public long base()                      { return U.arrayBaseOffset(Object[].class); }
 *     public int  scale()                     { return U.arrayIndexScale(Object[].class); }
 *     public long getLong(Object o, long off) { return U.getLong(o, off); }
 *     public int  getInt (Object o, long off) { return U.getInt (o, off); }
 * }
 * </pre>
 *
 * {@code akka.util.Unsafe.instance} is just a public forward to
 * {@code scala.concurrent.util.Unsafe.instance}; both jars are in Forge 1.7.10's own library
 * manifest (that is how FML supports Scala mods), so they are on every client and server, and the
 * public field hands the singleton over without {@code setAccessible} — the part modern JDKs keep
 * tightening.
 *
 * <h2>Do not "simplify" this into MethodHandles</h2>
 * Binding the four methods into {@code static final} MethodHandles and calling {@code invokeExact}
 * is the obvious tidier alternative. It works on JDK 17, 21 and 25 — and reproducibly crashes the
 * JVM on JDK 8, which plenty of 1.7.10 players still run. Narrowed down:
 * <ul>
 * <li>a MethodHandle to an <i>ordinary</i> Java method of the same signature, in the same hot loop,
 * is fine — so it is not "MethodHandles are broken";
 * <li>{@code -Xint}, {@code -XX:TieredStopAtLevel=1} and {@code =3} all survive, only C2 crashes;
 * <li>{@code -XX:+UnlockDiagnosticVMOptions -XX:DisableIntrinsic=_getLong} makes it survive.
 * </ul>
 * So JDK 8's C2 mishandles a MethodHandle call site whose target is an intrinsified Unsafe native:
 * rather than expanding the intrinsic it leaves a call to be resolved, and the resolution stub
 * (~RuntimeStub::resolve_opt_virtual_call, reached from the compiled scan loop) dereferences null.
 * Reproduced on Zulu 8u392 and 8u472 alike. Plain reflection is out for the opposite reason: it is
 * per-element work in a hot loop.
 */
public final class NullScan {

    /** Implemented only by the class {@link #emit()} generates. */
    public interface Raw {

        long base();

        int scale();

        long getLong(Object o, long off);

        int getInt(Object o, long off);
    }

    private static final String IMPL = "reobf/proghatches/util/NullScanRaw";
    private static final String UNSAFE = "sun/misc/Unsafe";
    private static final String UNSAFE_DESC = "Lsun/misc/Unsafe;";

    /** Emits the {@link Raw} implementation shown in the class javadoc. */
    private static byte[] emit() {
        // COMPUTE_MAXS is enough, and COMPUTE_FRAMES is deliberately avoided: computing frames
        // makes ASM load common superclasses to merge types, and nothing here branches, so a class
        // file carrying no StackMapTable entries verifies fine.
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER,
            IMPL,
            null,
            "java/lang/Object",
            new String[] { Type.getInternalName(Raw.class) });
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "U", UNSAFE_DESC, null, null)
            .visitEnd();

        // static { U = akka.util.Unsafe.instance; }
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, "akka/util/Unsafe", "instance", UNSAFE_DESC);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, IMPL, "U", UNSAFE_DESC);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // long base() { return U.arrayBaseOffset(Object[].class); }   (int result widened)
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "base", "()J", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, IMPL, "U", UNSAFE_DESC);
        mv.visitLdcInsn(Type.getType("[Ljava/lang/Object;"));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, UNSAFE, "arrayBaseOffset", "(Ljava/lang/Class;)I", false);
        mv.visitInsn(Opcodes.I2L);
        mv.visitInsn(Opcodes.LRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // int scale() { return U.arrayIndexScale(Object[].class); }
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "scale", "()I", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, IMPL, "U", UNSAFE_DESC);
        mv.visitLdcInsn(Type.getType("[Ljava/lang/Object;"));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, UNSAFE, "arrayIndexScale", "(Ljava/lang/Class;)I", false);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        emitAccessor(cw, "getLong", "(Ljava/lang/Object;J)J", Opcodes.LRETURN);
        emitAccessor(cw, "getInt", "(Ljava/lang/Object;J)I", Opcodes.IRETURN);

        cw.visitEnd();
        return cw.toByteArray();
    }

    /** {@code public T name(Object o, long off) { return U.name(o, off); }} */
    private static void emitAccessor(ClassWriter cw, String name, String desc, int returnOp) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, name, desc, null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, IMPL, "U", UNSAFE_DESC);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.LLOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, UNSAFE, name, desc, false);
        mv.visitInsn(returnOp);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

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
            raw = (Raw) new Defining(NullScan.class.getClassLoader()).define(IMPL.replace('/', '.'), emit())
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
