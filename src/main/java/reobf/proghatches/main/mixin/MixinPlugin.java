package reobf.proghatches.main.mixin;

import static java.nio.file.Files.walk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import reobf.proghatches.Tags;
import reobf.proghatches.main.MyMod;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.gtnewhorizon.gtnhmixins.MinecraftURLClassPath;

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

//spotless:off
String cfg=
"#disable those optional mixins if it breaks something"+System.lineSeparator()+
"#set to true to disable otherwise apply"+System.lineSeparator()+
"noPatternEncodingMixin=false"+System.lineSeparator()+
"noFixTossBug=false"+System.lineSeparator()+
"noRecipeFilterForDualHatch=false"+System.lineSeparator()+
"noRemoveUnusedCacheInModularUIContainer=false"+System.lineSeparator()+
//"noFixRecursiveCraft=false"+System.lineSeparator()+
"noEUCraftingMixins=false"+System.lineSeparator()+
"noAEItemSortMixins=false"+System.lineSeparator()

;
public static boolean noEUMixin;
static public ArrayList<String> retLate = new ArrayList<>();

//spotless:on
	@SuppressWarnings("unused")
	@Override
	public List<String> getMixins() {
		
		
		boolean ff=true;
		
		File f = new File(System.getProperty("user.dir") + File.separator + "config", "proghatches.mixin.properties");
		if (f.exists() == false) {
			try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try (FileOutputStream s = new FileOutputStream(f)) {

				s.write(cfg.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Properties pp = null;
		int off = 0;
		try (FileInputStream fi = new FileInputStream(f)) {
			pp = new Properties();
			pp.load(fi);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("corrupted config, try removing proghatches.mixin.properties", e);
		} catch (Exception e) {
			throw new RuntimeException("unable to read config", e);
		} finally {
		}

		System.out.println(pp);
		

		System.out.println("following warnings like 'Error loading class: xxxx' is normal and safe to ignore");

		
		// load all jars that mixin involves
		HashMap<String,String> map=	new HashMap<>(6);
		/*map.put( "com/github/vfyjxf/nee/NotEnoughEnergistics.class",  "NotEnoughEnergistics");
		map.put( "gregtech/GT_Mod.class", "GT5U");
		map.put( "appeng/api/IAppEngApi.class", "ae2");
		map.put( "com/gtnewhorizons/modularui/ModularUI.class", "ModularUI");
		map.put( "com/glodblock/github/FluidCraft.class", "ae2fc");
		map.put( "codechicken/nei/NEIModContainer.class", "NEI");*/
		loadJarOf(map);
	
		
		ArrayList<String> ret = new ArrayList<>();
		retLate.add("eucrafting." + "MixinWailaProvider");
		retLate.add("eucrafting." + "MixinInstantComplete");
		

		
		//if (!"true".equals(pp.get("noFixRecursiveCraft")))
			
		if (!"true".equals(pp.get("noEUCraftingMixins"))) {
			if(ff)retLate.add("eucrafting." + "MixinMachineIdle");
			if(ff)retLate.add("eucrafting." + "MixinMachineIdle2");
			if(ff)retLate.add("eucrafting." + "MixinCpuClusterEUAutoRequest");
			if(ff)retLate.add("eucrafting." + "MixinRemoveExcessiveEU");
			if(ff)retLate.add("eucrafting." + "MixinCraftingRecursiveWorkaround");
		}	else{
			
			
			noEUMixin=true;
		}
		
		
		
		
		if(ff)retLate.add("eucrafting." + "MixinEUSourceCoverChunkUnloadNotification");
		if(ff)	retLate.add("eucrafting." + "MixinCoverInsertion");
		if (FMLLaunchHandler.side().isClient()) {
			if(ff)retLate.add("eucrafting." + "MixinWirelessRename");
			}
		if(ff)retLate.add("eucrafting." + "MixinInvTracker");
		
		if(ff)retLate.add("MixinFixPipeCoverBug");	
		if(ff)retLate.add("MixinFixPipeCoverBug2");
		
		if(ff)retLate.add("MixinAEAdaptorSkipStackSizeCheck");
		if(ff)retLate.add("MixinAwarenessForDualHatch");
		
		
		/*if (!"true".equals(pp.get("noRemoveUnusedCacheInModularUIContainer")))
			if(ff)retLate.add("MixinRemoveUnunsedItemStackCache");
		
		*/
		
		if(ff)retLate.add("MixinAE2FCCompat");

		if (!"true".equals(pp.get("noRecipeFilterForDualHatch"))) {
			if(ff)retLate.add("MixinGTRecipeFilter");
			// GT Multiblock will not set recipe filter of DualInputHatch, set
			// it via mixin
			if(ff)retLate.add("MixinAddProgCircuitExemptToInputFilter");
		}
		// Crafting CPU cannot recognize empty-input pattern
		// bypass the check anyway
		retLate.add("MixinCanCraftExempt");
		if(ff)retLate.add("MixinNoFuzzyForProgrammingCircuit");
		if(ff)retLate.add("MixinHandleProgrammingOnRecipeStart");
		retLate.add("MixinCraftFromPatternTaskPatch");
		retLate.add("MixinGolemCore");
		retLate.add("MixinGolem");
		retLate.add("MixinStorageChangeEvent");
		retLate.add("MixinOptimize");
		retLate.add("part2.MixinIsWailaCall");
		retLate.add("part2.MixinPresetsInject");
		retLate.add("part2.MixinOC");
		retLate.add("part2.MixinRecursiveSlotClickProtection");
		retLate.add("part2.MixinMultiPattern");
		//retLate.add("part2.MixinSplitDetect");
		retLate.add("part2.MixinMEBusOverride");
		retLate.add("part2.MixinCraftingCondender");
		retLate.add("part2.MixinVoidingHatch");
		retLate.add("part2.MixinCraftingV2");
		if (FMLLaunchHandler.side().isClient()) {
			if (!"true".equals(pp.get("noAEItemSortMixins")))
				if(ff)retLate.add("MixinAEItemStackCompare");
			if (!"true".equals(pp.get("noFixTossBug")))
				if(ff)ret.add("MixinFixTossWhenClickSlot");
			
		
			
			if (!"true".equals(pp.get("noPatternEncodingMixin"))) {
				if(ff)retLate.add("MixinPatternEncodingCiruitSpecialTreatment");// For
																		// ae2fc
																		// pattern
																		// encoder
				if(ff)retLate.add("MixinPatternEncodingCiruitSpecialTreatment2"); // For
																		// nee
																		// pattern
																		// encoder
				// no version for wireless version, WIP
			}

		}
		;

		return ret;

	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	public static boolean hasTrait(Path p,Map<String, String> classTrait) {
		// System.err.println(p);
		try (ZipInputStream zs = new ZipInputStream(Files.newInputStream(p))) {

			ZipEntry entry = null;
			while ((entry = zs.getNextEntry()) != null) {
				String entryName = entry.getName();

				/*
				 * if(p.toString().contains("NotEnoughEnergistics-1.4.2.jar")){
				 * System.err.println(p); }
				 */
				boolean bingo = false;
				if (classTrait.remove(entryName)!=null) {
					
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

	private boolean loadJarOf( Map<String, String> classTrait) {
		if(classTrait.isEmpty())return true;
		try {
			List<File> jarl = findJarOf( classTrait);
			
			classTrait.forEach((a,b)->{
				LOG.info("Jar not found for " + b+", are you in dev environment?");
				
				
			});
			
			List<URL> url=Arrays.asList(Launch.classLoader.getURLs());
			for(File jar:jarl){
			
			LOG.info("Attempting to add " + jar + " to the URL Class Path");
			
			if (!jar.exists()) {
				throw new FileNotFoundException(jar.toString());
			}

			if (/*!MinecraftURLClassPath.findJarInClassPath(
					com.google.common.io.Files.getNameWithoutExtension(jar.getName()))&&*/
				!url.contains(jar.toURI().toURL())
					) {
				MinecraftURLClassPath.addJar(jar);
				LOG.info("Not in URL Class Path, adding now.");
			} else {
				LOG.info("Already in URL Class Path ... pass.");
			}
			}
			
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static final Logger LOG = LogManager.getLogger( MyMod.MODID + "Mixin");
	private static final Path MODS_DIRECTORY_PATH = new File(Launch.minecraftHome, "mods/").toPath();

	public static List<File> findJarOf(final Map<String, String> classTrait) {
		try {
			return walk(MODS_DIRECTORY_PATH).filter(s->{
				return hasTrait(s, classTrait);
			}).map(Path::toFile).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
