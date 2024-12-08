package reobf.proghatches.main.mixin.mixins.part2;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Sets;

import codechicken.nei.recipe.StackInfo;
import gregtech.api.GregTech_API;
import net.minecraft.item.ItemStack;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

@Mixin( //require = 1,
		targets="codechicken.nei.CollapsibleItems"
		,remap=false
		)

public class MixinPresetsInject {
	
	private static int[][] range={
			{Config.metaTileEntityOffset+ Registration.DualInputHatchOffset,
		    Config.metaTileEntityOffset+ Registration.DualInputHatchOffset+16},
			{Config.metaTileEntityOffset+ Registration.QuadDualInputHatchOffset,
			Config.metaTileEntityOffset+ Registration.QuadDualInputHatchOffset+16},
			{Config.metaTileEntityOffset+ Registration.BufferedDualInputHatchOffset,
			Config.metaTileEntityOffset+ Registration.BufferedDualInputHatchOffset+16},
			{Config.metaTileEntityOffset+ Registration.BufferedQuadDualInputHatchMKIIOffset,
			Config.metaTileEntityOffset+ Registration.BufferedQuadDualInputHatchMKIIOffset+16},
			{Config.metaTileEntityOffset+ Registration.TenaciousOffset,
			Config.metaTileEntityOffset+ Registration.FilterOffset+4},			
			{Config.metaTileEntityOffset+ Registration.IngBufferOffset,
			Config.metaTileEntityOffset+ Registration.IngBufferOffset+2},					
			{Config.metaTileEntityOffset+ Registration.BufferedQuadDualInputHatchOffset,
			Config.metaTileEntityOffset+ Registration.BufferedQuadDualInputHatchOffset+16},		
			{Config.metaTileEntityOffset+ Registration.MEChest,
			Config.metaTileEntityOffset+ Registration.MEChest+10},	
			{Config.metaTileEntityOffset+ Registration.METank,
			Config.metaTileEntityOffset+ Registration.METank+10},	
			{Config.metaTileEntityOffset+ Registration.MultiCircuitBusOffset,
			Config.metaTileEntityOffset+ Registration.MultiCircuitBusOffset+4},	
			{Config.metaTileEntityOffset+ Registration.PrefabOffset,
			Config.metaTileEntityOffset+ Registration.PrefabOffset+6},				
			
	};
	
	private static List<Object> temp=new ArrayList<>();
	@Inject(method="load",at = { @At(value="RETURN") }) private  static void b(CallbackInfo C)
	{	
		try{
		Class<?> c=Class.forName("codechicken.nei.PresetsList");
		Collection m=(Collection) c.getDeclaredField("presets").get(null);
		m.removeAll(temp);
		temp.clear();
		
		}catch(Exception e){}
	}
@Inject(method="load",at = { @At(value="HEAD") }) private  static void a(CallbackInfo C)
{
	
	try {
		Class<?> c=Class.forName("codechicken.nei.PresetsList");
		Collection m=(Collection) c.getDeclaredField("presets").get(null);
		Constructor<?> cst=Class.forName("codechicken.nei.PresetsList$Preset").getConstructor();
		BiConsumer<String ,Set<String>> add=(a,b)->{
			Object o;
			try {
			o = cst.newInstance();
			o.getClass().getDeclaredField("name").set(o,a);
			o.getClass().getDeclaredField("items").set(o,b);
			o.getClass().getDeclaredField("mode").set(o,
					Class.forName("codechicken.nei.PresetsList$PresetMode").getDeclaredField("GROUP").get(null)
					);
			temp.add(o);
			m.add(o);
			} catch (Exception e) {
			e.printStackTrace();
			}
		};
		for(int r[]:range)
		add.accept("",
		IntStream.range(r[0], r[1])
		.mapToObj(s->StackInfo.getItemStackGUID(new ItemStack(GregTech_API.sBlockMachines,1,s)))
		.collect(Collectors.toSet())
				
		)
		;
		
		
		
		add.accept("",
				IntStream.range(0, 9).mapToObj(s->MyMod.condensers[s])
				.map(s->new ItemStack(s))
				.map(StackInfo::getItemStackGUID
						).collect(Collectors.toSet())
						
				)
				;
		
		
		
		
		
	
	
	} catch (Exception e) {
		MyMod.LOG.info("Failed to add collapseable item info to NEI. Safe to ignore if you are playing with 2.6.1 or less.");
		e.printStackTrace();
		//270 only, nothing to worry about~
	}

}
}
