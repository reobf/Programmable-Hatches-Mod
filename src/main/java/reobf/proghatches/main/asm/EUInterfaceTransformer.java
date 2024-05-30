package reobf.proghatches.main.asm;





import java.io.File;
import java.io.FileOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class EUInterfaceTransformer implements IClassTransformer {
	boolean done;

	@SuppressWarnings("unused")
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
	if(2>1)
	return basicClass;
		
		if ((!done) && name.equals("reobf.proghatches.eucrafting.BlockEUInterface")) {
			done = true;
			
			
		    
		
			ClassReader cr = null;

			cr = new ClassReader(basicClass);
		cr.getSuperName();
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
				/*
				@Override
				public int newConst(Object cst) {
					System.out.println(cst);
					
					return super.newConst(cst);
				}
				@Override
				public int newUTF8(String value) {
					System.out.println(value);
					if (value.contains("reobf/proghatches/eucrafting/DummySuper")) {
						System.out.println("superclass replaced");
						return super.newUTF8(value.replace(
								"reobf/proghatches/eucrafting/DummySuper",
								
								"appeng/block/AEBaseTileBlock"));
					}
					return super.newUTF8(value);
				}*/

			};
			//ClassNode cn = new ClassNode();
			//cr.accept(cn, 0);
			cr.accept(cw, 0);
			System.out.println(cw.toByteArray().length);
			return cw.toByteArray();

		}
		return basicClass;
	}
}
