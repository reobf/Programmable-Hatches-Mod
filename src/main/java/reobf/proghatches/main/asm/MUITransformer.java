package reobf.proghatches.main.asm;

import java.util.ArrayList;



import net.minecraft.launchwrapper.IClassTransformer;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassReader;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassWriter;
import reobf.proghatches.main.asm.repack.objectwebasm.Label;
import reobf.proghatches.main.asm.repack.objectwebasm.MethodVisitor;
import reobf.proghatches.main.asm.repack.objectwebasm.Opcodes;
import reobf.proghatches.main.asm.repack.objectwebasm.tree.ClassNode;
import reobf.proghatches.main.asm.repack.objectwebasm.tree.MethodNode;

public class MUITransformer  implements IClassTransformer {
boolean done;
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(!done)
		if(name.equals("com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer")){
			done=true;
			ClassReader classReader = new ClassReader(basicClass);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
			ClassNode n=new ClassNode(Opcodes.ASM5){
				
				@Override
				public MethodVisitor visitMethod(int access, String name, String desc, String signature,
						String[] exceptions) {
					if(name.equals("transferItem")){
						 MethodNode mn = new TheNode(access, name, desc, signature,exceptions){
					                
							 
							 
							 
							 
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
static class TheNode extends MethodNode{
boolean done;
	public TheNode(int access, String name, String desc, String signature, String[] exceptions) {
		super(Opcodes.ASM5,access, name, desc, signature, exceptions);
	}
	boolean ready;
	Label loop_entry;
	@Override
	public void visitLabel(Label label) {
		if(ready&&loop_entry==null)loop_entry=label;
		super.visitLabel(label);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
	if(name.equals("iterator")/*desc.equals("()Ljava/util/Iterator;")*/){
			ready=true;
		}
		if(arr.size()<=2)
		if(owner.equals("com/gtnewhorizons/modularui/common/internal/wrapper/BaseSlot")&&name.equals("getShiftClickPriority")){
			arr.add(lastALOAD);//just in case the index cheanges
		}
		
		
		
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}
	public void visitJumpInsn(int opcode, Label label) {
		
		
		if(arr.size()==2&&!done){
			Label l = new Label();
			
		
			
			done=true;
			super.visitVarInsn(Opcodes.ALOAD, arr.get(0));
			super.visitVarInsn(Opcodes.ALOAD, arr.get(1));
			super.visitMethodInsn(Opcodes.INVOKESTATIC, "reobf/proghatches/main/asm/Util", "isSameGroup"
					
					
					, "(Lcom/gtnewhorizons/modularui/common/internal/wrapper/BaseSlot;Lcom/gtnewhorizons/modularui/common/internal/wrapper/BaseSlot;)Z", false);
			
			super.visitJumpInsn(Opcodes.IFEQ, l);//result==false, pass     
			//result==true
			super.visitInsn(Opcodes.POP2);	//pop away top 2 ClickPriority to match stackmap
			super.visitJumpInsn(Opcodes.GOTO, loop_entry);//return to loop entry
			super.visitLabel(l);						   
			
		}
		super.visitJumpInsn(opcode, label);
	};
	ArrayList<Integer> arr=new ArrayList<>();
	int lastALOAD;
	@Override
	public void visitVarInsn(int opcode, int var) {
		if(opcode==Opcodes.ALOAD){lastALOAD=var;}
		super.visitVarInsn(opcode, var);
	}
	
	
	
	
	
	
}
}
