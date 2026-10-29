package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.IDualInputInventory;
import reobf.proghatches.gt.metatileentity.chance.ChanceTracker;

@Mixin(value = MTEMultiBlockBase.class, remap = false)
public abstract class MixinChanceTrack {

    @Unique
    private Object ph$chanceSlot;

    @WrapOperation(
        method = "doCheckRecipe",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/logic/ProcessingLogic;process()Lgregtech/api/recipe/check/CheckRecipeResult;",
            ordinal = 0),
        require = 1)
    private CheckRecipeResult ph$trackDual(ProcessingLogic logic, Operation<CheckRecipeResult> original,
        @Local(ordinal = 0) IDualInputInventory slot) {
        Object snapshot = ChanceTracker.before(slot);
        CheckRecipeResult r = original.call(logic);
        if (snapshot != null && r.wasSuccessful())
            ph$chanceSlot = ChanceTracker.after(slot, snapshot, logic.getCurrentParallels());
        return r;
    }

    @Inject(method = "startRecipeProcessing", at = @At("HEAD"), require = 1)
    private void ph$start(CallbackInfo ci) {
        ph$chanceSlot = null;
    }

    @Inject(method = "endRecipeProcessing", at = @At("RETURN"), require = 1)
    private void ph$end(CallbackInfo ci) {
        if (ph$chanceSlot == null) return;
        Object token = ph$chanceSlot;
        ph$chanceSlot = null;
        ChanceTracker.settle((MTEMultiBlockBase) (Object) this, token);
    }
}
