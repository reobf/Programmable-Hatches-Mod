package reobf.proghatches.main.mixin.mixins.part2;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_InputBus_ME;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_Input_ME;
import reobf.proghatches.gt.metatileentity.util.IMEHatchOverrided;

@Mixin(remap=false,value={GT_MetaTileEntity_Hatch_InputBus_ME.class,
	GT_MetaTileEntity_Hatch_Input_ME.class,

})
public class MixinMEBusOverride {
	private static Field  minAutoPullStackSize;
	private static Field  minAutoPullAmount;
	static {
		
			try {
				minAutoPullStackSize=	GT_MetaTileEntity_Hatch_InputBus_ME.class.getDeclaredField("minAutoPullStackSize");
				minAutoPullAmount=	GT_MetaTileEntity_Hatch_Input_ME.class.getDeclaredField("minAutoPullAmount");
			
				minAutoPullAmount.setAccessible(true);
				minAutoPullStackSize.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
			
		}
		
	}
	
	@Inject(method={"refreshItemList"
			,"refreshFluidList"
	},cancellable=true,at = { @At("HEAD") })
	 private void refreshItemList(CallbackInfo ci) {
		if(this instanceof IMEHatchOverrided){
			try {
				((IMEHatchOverrided)this).overridedBehoviour(
					(	(Object)this instanceof GT_MetaTileEntity_Hatch_InputBus_ME?
						minAutoPullStackSize:minAutoPullAmount)
						
						.getInt(this));
			} catch (Exception e) {
				e.printStackTrace();
			}
			ci.cancel();
		}
		
	}
	
	@Redirect(method="endRecipeProcessing"
			,
			
			at = @At(value="INVOKE",
			 target = "extractItems",
	            remap = false
			)) 
	private  IAEStack r(IMEMonitor thiz,IAEStack request, Actionable mode, BaseActionSource src) {
		if(this instanceof IMEHatchOverrided){
			return ((IMEHatchOverrided)this).overridedExtract(thiz,request,mode,src);
		
		}
			
			return (IAEStack) thiz.extractItems(request, mode, src);
		
		
	}
	
}
