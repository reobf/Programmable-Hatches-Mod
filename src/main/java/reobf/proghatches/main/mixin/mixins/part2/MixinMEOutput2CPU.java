package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.Platform;
import gregtech.common.tileentities.machines.outputme.base.MTEHatchOutputMEBase;
import gregtech.common.tileentities.machines.outputme.util.AECacheCounter;
import reobf.proghatches.gt.metatileentity.StorageOutputBus;
import reobf.proghatches.gt.metatileentity.StorageOutputHatch;
import reobf.proghatches.gt.metatileentity.util.IMEHatchOverrided;

@Mixin(value=MTEHatchOutputMEBase.class,remap  =false)
public class MixinMEOutput2CPU {
	
	@Shadow
	 private  gregtech.common.tileentities.machines.outputme.base.MTEHatchOutputMEBase.Environment env;
	@Shadow @Final
	 protected  AECacheCounter<IAEStack<?>> cache;
	@Shadow
	  public AENetworkProxy getProxy() {
		return null;}
	
	@Inject(
	        method = "flushCachedStack",
	        at = @At(
	            value = "INVOKE"
	            
	            ,target = "getActionSource",shift = Shift.BY
	          ,remap  =false,by = 2
	        ),
	    expect = 1,
	        remap  =false                 
	    )
	    private void x(CallbackInfo x,@Local BaseActionSource source) {

		
		if(env instanceof StorageOutputBus||env instanceof StorageOutputHatch)
		cache.updateAll((stack, amount) -> {
	            stack.setStackSize(amount);
	            try {
					CraftingGridCache cGc=getProxy().getGrid().getCache(ICraftingGrid.class);
					stack= cGc.injectItems(stack, Actionable.MODULATE, source);
	            if(stack==null)return 0;
	            } catch (GridAccessException e) {
				}
	            return stack.getStackSize();
	        });
	
		
	}
}
