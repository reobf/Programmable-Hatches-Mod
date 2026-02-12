package cpw.mods.fml.common.patcher;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import net.minecraft.launchwrapper.LaunchClassLoader;

import cpw.mods.fml.relauncher.Side;
/**
 * This is a dummy class to keep eclipse JDT compiler shut about complaining generated rfg sources.
 * ClassPatchManager refers to Pack200.java which only exists on JDK8.
 * If this java file causes some problems in IDEA or obfuscated env, try to exclude it or remove it.
 * 
 * */
public class ClassPatchManager {
	public static final ClassPatchManager INSTANCE = new ClassPatchManager();

	private ClassPatchManager() {

	}

	public byte[] getPatchedResource(String name, String mappedName, LaunchClassLoader loader) throws IOException {
		throw new RuntimeException("stub");
	}

	public byte[] applyPatch(String name, String mappedName, byte[] inputData) {
		throw new RuntimeException("stub");
	}

	public void setup(Side side) {
		throw new RuntimeException("stub");
	}

	private ClassPatch readPatch(JarEntry patchEntry, JarInputStream jis) {
		throw new RuntimeException("stub");
	}
}