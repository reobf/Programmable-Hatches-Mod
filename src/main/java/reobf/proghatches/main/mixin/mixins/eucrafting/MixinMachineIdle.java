package reobf.proghatches.main.mixin.mixins.eucrafting;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import reobf.proghatches.eucrafting.IIdleStateProvider;


@Mixin(value=MTEMultiBlockBase.class,remap=false)
public class MixinMachineIdle implements IIdleStateProvider{
private boolean hasJob;
@Shadow int mMaxProgresstime;
@Shadow private  CheckRecipeResult checkRecipeResult ;
 public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
	 hasJob=mMaxProgresstime>0;    
	 if(jobdone>0)jobdone--;
	 fail=false;
}
int jobdone;	
boolean fail;
 
 @Inject(remap=false,method="checkRecipe", at = { @At("RETURN") })
 public void check(CallbackInfoReturnable<Boolean> c){
	 
	 if(hasJob&&!checkRecipeResult.wasSuccessful()){
		 jobdone=4;
	 }
	 if(!checkRecipeResult.wasSuccessful()){
		 
		 fail=true;
	 }
	 
 }

/*
@Override
public int getIdle() {
	return jobdone;
}
*/

@Override
public boolean failThisTick() {
	
	return fail;
}



 
	
}
