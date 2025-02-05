package reobf.proghatches.main.asm;

import net.minecraft.launchwrapper.IClassTransformer;

import reobf.proghatches.main.asm.repack.objectwebasm.ClassReader;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassWriter;
import reobf.proghatches.main.asm.repack.objectwebasm.MethodVisitor;
import reobf.proghatches.main.asm.repack.objectwebasm.Opcodes;
import reobf.proghatches.main.asm.repack.objectwebasm.tree.ClassNode;
import reobf.proghatches.main.asm.repack.objectwebasm.tree.MethodNode;

public class MFUTransformer implements IClassTransformer {

    boolean done;
    boolean done2;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!done) if (name.equals("li.cil.oc.server.component.UpgradeMF")) {
            done = true;
            ClassReader classReader = new ClassReader(basicClass);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ClassNode n = new ClassNode(Opcodes.ASM5) {

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                    String[] exceptions) {
                    if (name.equals("updateBoundState")) {
                        MethodNode mn = new TheNode(access, name, desc, signature, exceptions) {

                        };
                        methods.add(mn);
                        return mn;

                    }
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            };

            classReader.accept(n, ClassReader.EXPAND_FRAMES);
            n.accept(classWriter);

            return classWriter.toByteArray();

        }

        if (!done2) if (name.equals("li.cil.oc.common.tileentity.Adapter")) {
            done2 = true;
            ClassReader classReader = new ClassReader(basicClass);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ClassNode n = new ClassNode(Opcodes.ASM5) {

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                    String[] exceptions) {
                    if (name.equals("neighborChanged")) {
                        MethodNode mn = new TheNode(access, name, desc, signature, exceptions) {

                        };
                        methods.add(mn);
                        return mn;

                    }
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
            };

            classReader.accept(n, ClassReader.EXPAND_FRAMES);
            n.accept(classWriter);

            return classWriter.toByteArray();

        }

        return basicClass;
    }

    static class TheNode extends MethodNode {

        public TheNode(int access, String name, String desc, String signature, String[] exceptions) {
            super(Opcodes.ASM5, access, name, desc, signature, exceptions);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (opcode == Opcodes.INSTANCEOF && type.equals("li/cil/oc/api/network/Environment")) {

                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "reobf/proghatches/main/asm/ASMCallbacks",
                    "checkIsRealEnvironment",
                    "(Ljava/lang/Object;)Z",
                    false);
                return;

            }
            super.visitTypeInsn(opcode, type);
        }

    }

}
