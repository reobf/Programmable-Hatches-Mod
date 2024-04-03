package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import reobf.proghatches.main.mixin.MixinCallback;

@SuppressWarnings("unused")
@Mixin(value = GT_MetaTileEntity_MultiBlockBase.class, remap = false)
public abstract class MixinGTRecipeFilter {

    @Inject(method = "addInputBusToMachineList", at = @At("RETURN"), require = 1)
    public void addInputBusToMachineList0(IGregTechTileEntity aTileEntity, int aBaseCasingIndex,
        CallbackInfoReturnable c) {
        MixinCallback.handleAddedToMachineList(aTileEntity, this);
    }

    @Inject(method = "addToMachineList", at = @At("RETURN"), require = 1)
    public void addToMachineList0(IGregTechTileEntity aTileEntity, int aBaseCasingIndex, CallbackInfoReturnable c) {
        MixinCallback.handleAddedToMachineList(aTileEntity, this);
    }

    
}
