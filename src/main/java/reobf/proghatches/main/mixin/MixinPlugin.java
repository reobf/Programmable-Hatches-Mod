package reobf.proghatches.main.mixin;

import static java.nio.file.Files.walk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.minecraft.launchwrapper.Launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import reobf.proghatches.Tags;
import ru.timeconqueror.spongemixins.MinecraftURLClassPath;

public class MixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getRefMapperConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // TODO Auto-generated method stub

    }
String cfg=
"#disable those optional mixins if it breaks someting"+System.lineSeparator()+
"#set to true to disable otherwise apply"+System.lineSeparator()+
"noPatternEncodingMixin=false"+System.lineSeparator()+
"noFixTossBug=false"+System.lineSeparator()+
"noRecipeFilterForDualHatch=false"+System.lineSeparator()+
"noRemoveUnusedCacheInModularUIContainer=fasle"+System.lineSeparator()+
"noFixRecursiveCraft=false"+System.lineSeparator()+
"noEUCraftingMixins=false"+System.lineSeparator()
;
    @SuppressWarnings("unused")
    @Override
    public List<String> getMixins() {
       
          File f=new File( System.getProperty("user.dir")+File.separator+"config","proghatches.mixin.properties");
          if(f.exists()==false){
          try {
          f.createNewFile();
          } catch (IOException e1) {
          e1.printStackTrace();
          }
          try (FileOutputStream s=new FileOutputStream(f)){
        	 
          s.write(cfg.getBytes());
          } catch (Exception e) {
          e.printStackTrace();
          }
          }
          Properties pp=null;
         int off=0;
          try(FileInputStream fi=new FileInputStream(f)){
        	  pp= new Properties();
        	  pp.load(fi);
          } catch (IllegalArgumentException e) {
        	  throw new RuntimeException("corrupted config, try removing proghatches.mixin.properties",e);
          }
          catch (Exception e) {
			throw new RuntimeException("unable to read config",e);
          }finally{}
          
          System.out.println(pp);
         // System.out.println("ccccccccccccccccccccccccccc");
          
          System.out.println("following warnings like 'Error loading class: xxxx' is normal and safe to ignore");
          
        // NEE is neither coremod nor mixinmod thus it's not in URL path, so add it to path or mixin will fail
        loadJarOf(MixinPlugin::hasTrait,"NotEnoughEnergistics");

       

        ArrayList<String> ret = new ArrayList<>();
        ret.add("eucrafting."+"MixinWailaProvider");
        //ret.add("eucrafting."+"MixinRecipeStateDetect");
       // ret.add("eucrafting."+"MixinCpuClusterAccess");
       
        ret.add("eucrafting."+"MixinInstantComplete");
        if(!"true".equals(pp.get("noFixRecursiveCraft")))
        ret.add("eucrafting."+"MixinCraftingRecursiveWorkaround");
        if(!"true".equals(pp.get("noEUCraftingMixins"))){
        	
        ret.add("eucrafting."+"MixinCpuClusterEUAutoRequest");
        ret.add("eucrafting."+"MixinRemoveExcessiveEU");
        ret.add("eucrafting."+"MixinCoverInsertion");
        ret.add("eucrafting."+ "MixinEUSourceCoverChunkUnloadNotification");
        }
        
        ret.add("MixinAwarenessForDualHatch");
        if(!"true".equals(pp.get("noRemoveUnusedCacheInModularUIContainer")))
        ret.add("MixinRemoveUnunsedItemStackCache");
        ret.add("MixinAE2FCCompat");
        
        if(!"true".equals(pp.get("noRecipeFilterForDualHatch"))){
    	ret.add("MixinGTRecipeFilter");
        // GT Multiblock will not set recipe filter of DualInputHatch, set it via mixin
         ret.add("MixinAddProgCircuitExemptToInputFilter");}
        // Crafting CPU cannot recognize empty-input pattern
        // bypass the check anyway
        ret.add("MixinCanCraftExempt");
      
        ret.add("MixinHandleProgrammingOnRecipeStart"); 
     
       
        if (FMLLaunchHandler.side()
            .isClient()) {
        	
            if(!"true".equals(pp.get("noFixTossBug")))
            	  ret.add("MixinFixTossWhenClickSlot");
            
        	if(!"true".equals(pp.get("noPatternEncodingMixin"))){
        	ret.add("MixinPatternEncodingCiruitSpecialTreatment");// For ae2fc pattern encoder
            ret.add("MixinPatternEncodingCiruitSpecialTreatment2"); // For nee pattern encoder
            // no version for wireless version, WIP
        	}
          

        } ;

        return ret;

    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    public static boolean hasTrait(Path p) {
        // System.err.println(p);
        try (ZipInputStream zs = new ZipInputStream(Files.newInputStream(p))) {

            ZipEntry entry = null;
            while ((entry = zs.getNextEntry()) != null) {
                String entryName = entry.getName();

                /*
                 * if(p.toString().contains("NotEnoughEnergistics-1.4.2.jar")){
                 * System.err.println(p);
                 * }
                 */
                boolean bingo = false;
                if (entryName.contains("com/github/vfyjxf/nee/NotEnoughEnergistics.class")) {
                    bingo = true;

                }

                zs.closeEntry();
                if (bingo) {
                    return bingo;
                }

            }

        } catch (Exception e) {
            return false;
        }
        return false;

    }

    @SuppressWarnings("deprecation")
	private boolean loadJarOf(final Predicate<Path> mod,String trace) {
        try {
            File jar = findJarOf(mod);
            if (jar == null) {
                LOG.info("Jar not found for " + trace);
                return false;
            }

            LOG.info("Attempting to add " + jar + " to the URL Class Path");
            if (!jar.exists()) {
                throw new FileNotFoundException(jar.toString());
            }
            MinecraftURLClassPath.addJar(jar);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static final Logger LOG = LogManager.getLogger(Tags.MODID + "Mixin");
    private static final Path MODS_DIRECTORY_PATH = new File(Launch.minecraftHome, "mods/").toPath();

    public static File findJarOf(final Predicate<Path> mod) {
        try {
            return walk(MODS_DIRECTORY_PATH).filter(mod)
                .map(Path::toFile)
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
