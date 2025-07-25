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

@Mixin(remap = false, value = { MTEHatchInputBusME.class, MTEHatchInputME.class,

})
public class MixinMEBusOverride {

    private static Field minAutoPullStackSize;
    private static Field minAutoPullAmount;
    static {

        try {
            minAutoPullStackSize = MTEHatchInputBusME.class.getDeclaredField("minAutoPullStackSize");
            minAutoPullAmount = MTEHatchInputME.class.getDeclaredField("minAutoPullAmount");

            minAutoPullAmount.setAccessible(true);
            minAutoPullStackSize.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @Inject(require = 1, method = { "refreshItemList", "refreshFluidList" }, cancellable = true, at = { @At("HEAD") })
    private void refreshItemList(CallbackInfo ci) {
        if (this instanceof IMEHatchOverrided) {
            try {if(((IMEHatchOverrided) this).override()==false){return;}
                ((IMEHatchOverrided) this).overridedBehoviour(
                    ((Object) this instanceof MTEHatchInputBusME ? minAutoPullStackSize : minAutoPullAmount)

                        .getInt(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            ci.cancel();
        }

    }

    @Redirect(require = 0,expect=0, method = "endRecipeProcessing"//"/^\\w/"// "endRecipeProcessing"
        ,at = @At(value = "INVOKE", target = "extractItems", remap = false))
    private IAEStack extractItemsOrOverride(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src) {
        if (this instanceof IMEHatchOverrided) {
            return ((IMEHatchOverrided) this).overridedExtract(thiz, request, mode, src);

        }
        return (IAEStack) thiz.extractItems(request, mode, src);

    }
    @SuppressWarnings("unchecked")
	@Redirect(require = 0,expect=0, method = "endRecipeProcessing"//"/^\\w/"// "endRecipeProcessing"
            ,at = @At(value = "INVOKE", target = "poweredExtraction", remap = false))
        private IAEStack extractItemsOrOverrideNeo(IEnergySource xx,IMEInventory thiz, IAEStack request, BaseActionSource src) {
            if (this instanceof IMEHatchOverrided) { 
            	
            	Actionable mode=Actionable.MODULATE;
                return ((IMEHatchOverrided) this).overridedExtract((IMEMonitor) thiz, request, mode, src);

            }
            return Platform.poweredExtraction(xx, thiz, request, src);

        }   
    
    @SuppressWarnings("rawtypes")
	@Redirect(require = 1, method = "updateInformationSlot"// "endRecipeProcessing"
            ,

            at = @At(value = "INVOKE", target = "extractItems", remap = false))
    private IAEStack qureyStorage(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src){
    	
    	   if (this instanceof IMEHatchOverrided) {
               return ((IMEHatchOverrided) this).qureyStorage(thiz, request, mode, src);

           }

           return (IAEStack) thiz.extractItems(request, mode, src);
    	
    	
    }
    
    
    //ME Bus simulates extraction in getStackInSlot?
    //ME Hatch has no such behavior in getStoredFluid
    //so it's optional(require = -1)
    @SuppressWarnings("rawtypes")
	@Redirect(require = 0,expect=0, method = "getStackInSlot"
            ,at = @At(value = "INVOKE", target = "extractItems", remap = false))
    private IAEStack qureyStorageX(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src){
    	
    	   if (this instanceof IMEHatchOverrided) {
               return ((IMEHatchOverrided) this).qureyStorage(thiz, request, mode, src);

           }

           return (IAEStack) thiz.extractItems(request, mode, src);
    	
    	
    }
    

}
