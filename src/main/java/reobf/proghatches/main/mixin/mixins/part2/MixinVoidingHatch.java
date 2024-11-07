package reobf.proghatches.main.mixin.mixins.part2;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.VoidOutputBus;
import reobf.proghatches.gt.metatileentity.VoidOutputHatch;

@Mixin(value = GT_MetaTileEntity_MultiBlockBase.class,remap=false)
public class MixinVoidingHatch {
	  @Shadow
	  public ArrayList<GT_MetaTileEntity_Hatch_Output> mOutputHatches;
	  
	
	  @Shadow
	  public ArrayList<GT_MetaTileEntity_Hatch_OutputBus> mOutputBusses;

	long lastNoVoidBusTick=-1;
	@Inject(method="addOutput(Lnet/minecraft/item/ItemStack;)Z",at = { @At("HEAD") },cancellable=true)
	public void addOutput(ItemStack aStack,CallbackInfoReturnable<Boolean> a)
	{	
		GT_MetaTileEntity_MultiBlockBase y=(GT_MetaTileEntity_MultiBlockBase)((Object)this);
		
		long thisTick=((CoverableTileEntity)y.getBaseMetaTileEntity()).mTickTimer;
		if(thisTick==lastNoVoidBusTick){
			return;//we have checked perviously that no void bus is present, just skip it
		}
		lastNoVoidBusTick=thisTick;
		for(GT_MetaTileEntity_Hatch_OutputBus xx:GT_Utility.filterValidMTEs(mOutputBusses)){
			lastNoVoidBusTick=-1;
			if(xx instanceof VoidOutputBus){
				if(((VoidOutputBus)xx).dump(aStack)){
					a.setReturnValue(true);//if it accepts...
					return;
				};
				
			}
			
		};
		
	}
	long lastNoVoidHatchTick=-1;
	@Inject(method="addOutput(Lnet/minecraftforge/fluids/FluidStack;)Z",at = { @At("HEAD") },cancellable=true)
	public void addOutput(FluidStack aStack,CallbackInfoReturnable<Boolean> a)
	{
		GT_MetaTileEntity_MultiBlockBase y=(GT_MetaTileEntity_MultiBlockBase)((Object)this);
		long thisTick=((CoverableTileEntity)y.getBaseMetaTileEntity()).mTickTimer;
		if(thisTick==lastNoVoidHatchTick){
			return;
		}
		lastNoVoidHatchTick=thisTick;
		for(GT_MetaTileEntity_Hatch_Output xx:GT_Utility.filterValidMTEs(mOutputHatches)){
			if(xx instanceof VoidOutputHatch){
				lastNoVoidHatchTick=-1;
				if(((VoidOutputHatch)xx).dump(aStack)){
					a.setReturnValue(true);
					return;
				};
				
			}
			
		};
		
		
	}
	
	
	
	
	
	
}
