package reobf.proghatches.main.mixin.mixins.eucrafting;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import reobf.proghatches.eucrafting.IIdleStateProvider;


@Mixin(value=MTEBasicMachine.class,remap=false)
public abstract class MixinMachineIdle2 implements IIdleStateProvider{

@Shadow int mMaxProgresstime;
boolean fail;
 public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
	
	a(aBaseMetaTileEntity);fail=false;
}

 
 @Inject(remap=false,method="onPostTick", at = { @At("RETURN") })
 public void check(IGregTechTileEntity aBaseMetaTileEntity, long aTick,CallbackInfo p){
	 if(shouldCheck&&mMaxProgresstime<=0){fail=true;}
	 
 }

@Override
	public boolean failThisTick() {
		
		return fail;
	}




@Shadow
public abstract boolean allowToCheckRecipe();
@Shadow
protected abstract boolean hasEnoughEnergyToCheckRecipe();

public void a(IGregTechTileEntity aBaseMetaTileEntity){
	shouldCheck=false;
	  if (allowToCheckRecipe()) {
          if (mMaxProgresstime <= 0 && aBaseMetaTileEntity.isAllowedToWork()
              /*&& (tRemovedOutputFluid || tSucceeded
                  || aBaseMetaTileEntity.hasInventoryBeenModified()
                  || aTick % 600 == 0
                  || aBaseMetaTileEntity.hasWorkJustBeenEnabled())*/
              && hasEnoughEnergyToCheckRecipe()) {
        	  
        	  
        	  shouldCheck=true;
          }
          
	  }
}
boolean shouldCheck;
 
	
}
