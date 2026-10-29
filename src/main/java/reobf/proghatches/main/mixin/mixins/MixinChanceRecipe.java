package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.OverclockCalculator;
import gregtech.api.util.ParallelHelper;
import reobf.proghatches.gt.metatileentity.chance.ChanceTracker;

@Mixin(value = ProcessingLogic.class, remap = false)
public abstract class MixinChanceRecipe {

    @Inject(method = "applyRecipe", at = @At("HEAD"), require = 1)
    private void ph$applied(GTRecipe recipe, ParallelHelper helper, OverclockCalculator calculator,
        CheckRecipeResult result, CallbackInfoReturnable<CheckRecipeResult> cir) {
        ChanceTracker.applied(recipe, ((ParallelHelperAccessor) helper).ph$chanceMultiplier());
    }
}
