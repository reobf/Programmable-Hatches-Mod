package cpw.mods.fml.common.patcher;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import net.minecraft.launchwrapper.LaunchClassLoader;

import cpw.mods.fml.relauncher.Side;

public class ClassPatchManager {
    public static final ClassPatchManager INSTANCE = null;

    public static final boolean dumpPatched = false;
    public static final boolean DEBUG = false;



    public byte[] getPatchedResource(String name, String mappedName, LaunchClassLoader loader) throws IOException
    {
    	  return null;
    }
    public byte[] applyPatch(String name, String mappedName, byte[] inputData)
    {
      
        return null;
    }

    public void setup(Side side)
    {
       
    }

    private ClassPatch readPatch(JarEntry patchEntry, JarInputStream jis)
    {
      
        return null;
    }
}