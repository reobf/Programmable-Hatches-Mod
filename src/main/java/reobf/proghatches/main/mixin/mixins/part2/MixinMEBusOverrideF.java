package reobf.proghatches.main.mixin.mixins.part2;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import reobf.proghatches.gt.metatileentity.util.IMEHatchOverrided;

@Mixin(remap = true, value = {  MTEHatchInputME.class,

})
public class MixinMEBusOverrideF {

   

    @Inject(remap=false,require = 1, method = { "refreshItemList", "refreshFluidList" }, cancellable = true, at = { @At("HEAD") })
    private void refreshItemList(CallbackInfo ci) {
        if (this instanceof IMEHatchOverrided) {
            try {if(((IMEHatchOverrided) this).override()==false){return;}
                ((IMEHatchOverrided) this).overridedBehoviour(
                		((IMEHatchOverrided) this).minAutoPull());
            } catch (Exception e) {
                e.printStackTrace();
            }
            ci.cancel();
        }

    }


    @SuppressWarnings("unchecked")
	@Redirect(remap=false,require = 1, method = "endRecipeProcessing"//"/^\\w/"// "endRecipeProcessing"
            ,at = @At(value = "INVOKE", target = "poweredExtraction", remap = false))
        private IAEStack extractItemsOrOverrideNeo(IEnergySource xx,IMEInventory thiz, IAEStack request, BaseActionSource src) {
            if (this instanceof IMEHatchOverrided) { 
            	
            	Actionable mode=Actionable.MODULATE;
                return ((IMEHatchOverrided) this).overridedExtract((IMEMonitor) thiz, request, mode, src);

            }
            return Platform.poweredExtraction(xx, thiz, request, src);

        }   
    
    @SuppressWarnings("rawtypes")
	@Redirect(remap=false,require = 1, method = "updateInformationSlot"// "endRecipeProcessing"
            ,

            at = @At(value = "INVOKE", target = "extractItems", remap = false))
    private IAEStack qureyStorage(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src){
    	
    	   if (this instanceof IMEHatchOverrided) {
               return ((IMEHatchOverrided) this).qureyStorage(thiz, request, mode, src);

           }

           return (IAEStack) thiz.extractItems(request, mode, src);
    	
    	
    }
    
  
    

}
