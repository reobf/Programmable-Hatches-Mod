package reobf.proghatches.main.asm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;
import reobf.proghatches.block.TileIOHub.OCApi;
import reobf.proghatches.eucrafting.BlockEUInterface;

public class EUInterfaceTransformer implements IClassTransformer {
	boolean done;

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if ((!done) && name.equals("reobf.proghatches.eucrafting.BlockEUInterface")) {
			done = true;
			ClassReader cr = null;

			cr = new ClassReader(basicClass);

			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
				@Override
				public int newUTF8(String value) {
					if (value.equals("reobf/proghatches/eucrafting/DummySuper")) {
						System.out.println("superclass replaced");
						return super.newUTF8("appeng/block/AEBaseTileBlock");
					}
					return super.newUTF8(value);
				}

			};
			ClassNode cn = new ClassNode();
			cr.accept(cn, 0);

			cn.accept(cw);

			return cw.toByteArray();

		}
		return basicClass;
	}
}
