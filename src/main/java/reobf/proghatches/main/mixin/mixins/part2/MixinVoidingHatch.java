package reobf.proghatches.main.mixin.mixins.part2;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.VoidOutputBus;
import reobf.proghatches.gt.metatileentity.VoidOutputHatch;

@Mixin(value = MTEMultiBlockBase.class,remap=false)
public class MixinVoidingHatch {
	  @Shadow
	  public ArrayList<MTEHatchOutput> mOutputHatches;
	  
	
	  @Shadow
	  public ArrayList<MTEHatchOutputBus> mOutputBusses;

	long lastNoVoidBusTick=-1;
	@Inject( require = 1,method="addOutput(Lnet/minecraft/item/ItemStack;)Z",at = { @At("HEAD") },cancellable=true)
	public void addOutput(ItemStack aStack,CallbackInfoReturnable<Boolean> a)
	{	
		MTEMultiBlockBase y=(MTEMultiBlockBase)((Object)this);
		
		long thisTick=((CoverableTileEntity)y.getBaseMetaTileEntity()).mTickTimer;
		if(thisTick==lastNoVoidBusTick){
			return;//we have checked perviously that no void bus is present, just skip it
		}
		lastNoVoidBusTick=thisTick;
		for(MTEHatchOutputBus xx:GTUtility.filterValidMTEs(mOutputBusses)){
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
	@Inject( require = 1,method="addOutput(Lnet/minecraftforge/fluids/FluidStack;)Z",at = { @At("HEAD") },cancellable=true)
	public void addOutput(FluidStack aStack,CallbackInfoReturnable<Boolean> a)
	{
		MTEMultiBlockBase y=(MTEMultiBlockBase)((Object)this);
		long thisTick=((CoverableTileEntity)y.getBaseMetaTileEntity()).mTickTimer;
		if(thisTick==lastNoVoidHatchTick){
			return;
		}
		lastNoVoidHatchTick=thisTick;
		for(MTEHatchOutput xx:GTUtility.filterValidMTEs(mOutputHatches)){
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
