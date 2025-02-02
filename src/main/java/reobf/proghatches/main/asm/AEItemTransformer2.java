package reobf.proghatches.main.asm;





import java.io.File;
import java.io.FileOutputStream;




import net.minecraft.launchwrapper.IClassTransformer;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassReader;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassWriter;

public class AEItemTransformer2 implements IClassTransformer {
	boolean done;

	@SuppressWarnings("unused")
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
	
		if ((!done) && name.equals("reobf.proghatches.ae.ItemEmitterPattern")) {
			done = true;
			
			
		    
			//asm for java17 is not working(for me)!
			//so I packed one copy of asm for java8
			ClassReader cr = null;
			cr = new ClassReader(basicClass);
		    cr.getSuperName();
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
				
				
				@Override
				public int newUTF8(String value) {
					
					if (value.contains("reobf/proghatches/item/DummySuper2")) {
						return super.newUTF8(value.replace(
								"reobf/proghatches/item/DummySuper2",
								
								"appeng/items/misc/ItemEncodedPattern"));
					}
					return super.newUTF8(value);
				}

			};
		
			cr.accept(cw, 0);
			
			return cw.toByteArray();

		}
		return basicClass;
	}
}
