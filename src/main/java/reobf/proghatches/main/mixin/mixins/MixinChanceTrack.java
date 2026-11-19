package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import reobf.proghatches.gt.metatileentity.chance.ChanceTracker;

@Mixin(value = MTEMultiBlockBase.class, remap = false)
public abstract class MixinChanceTrack {

    @Shadow
    @Final
    protected ProcessingLogic processingLogic;

    @Unique
    private Object ph$chanceSlot;

    @Inject(method = "doCheckRecipe", at = @At("HEAD"), require = 0)
    private void ph$trackBegin(CallbackInfoReturnable<CheckRecipeResult> cir) {
        ChanceTracker.begin();
    }

    @Inject(method = "doCheckRecipe", at = @At("RETURN"), require = 0)
    private void ph$trackEnd(CallbackInfoReturnable<CheckRecipeResult> cir) {
        Object consumed = ChanceTracker.end(cir.getReturnValue().wasSuccessful(), processingLogic.getCurrentParallels());
        if (consumed != null) ph$chanceSlot = consumed;
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
