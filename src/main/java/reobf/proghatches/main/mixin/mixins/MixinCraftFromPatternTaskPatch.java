package reobf.proghatches.main.mixin.mixins;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.v2.CraftingContext;
import appeng.crafting.v2.CraftingRequest;
import appeng.crafting.v2.resolvers.CraftableItemResolver.CraftFromPatternTask;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeModContainer;
import reobf.proghatches.eucrafting.IInputMightBeEmptyPattern;

@Mixin(value=CraftFromPatternTask.class,remap=true)
public class MixinCraftFromPatternTaskPatch {

	private static Boolean shouldPatch=null;
	private boolean shouldPatch(){
		if(shouldPatch!=null)return shouldPatch;
		
		try {
			//if method CraftFromPatternTask#calculateRecursiveInputs exists, AE is using a new way to calculate IO, do patch
			CraftFromPatternTask.class.getDeclaredMethod("calculateRecursiveInputs", IAEItemStack[] .class, IAEItemStack[].class);
			shouldPatch=true;
		} catch (NoSuchMethodException e) {
			//method missing means okay to skip patch
			shouldPatch=false;
		}
		return shouldPatch();
	}
	@Shadow 
	  protected  IAEItemStack[] patternInputs;
	@Inject(method="<init>",at = { @At("RETURN") })
	public void calculateOneStep(CraftingRequest<IAEItemStack> request, ICraftingPatternDetails pattern,
            int priority, boolean allowSimulation, boolean isComplex,CallbackInfo xx){
		if(shouldPatch())
		if(pattern instanceof IInputMightBeEmptyPattern){
			
			patternInputs=Arrays.stream(patternInputs).filter(s->s.getStackSize()>0).toArray(IAEItemStack[]::new);
			
		}
	     
		
		
	}

	
	
	
	
}
