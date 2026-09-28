package reobf.proghatches.main.mixin.mixins.part2;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.crafting.v2.CraftingContext;
import appeng.crafting.v2.CraftingContext.RequestInProcessing;
import appeng.crafting.v2.resolvers.CraftingTask;
import reobf.proghatches.ae.CircuitHoistTally;

/**
 * Half two of circuit hoisting (see {@link CircuitHoistTally}): when doWork reports that the task
 * queue has run dry, settle the tallied circuits in place. Nothing is queued and the return value is
 * left alone, so the circuits cost no calculation rounds.
 * <p>
 * RETURN covers all four exits of doWork; only a SUCCESS return means "queue empty". FAILURE and the
 * step-limit exception never reach this, and the tally settles at most once.
 */
@Mixin(value = CraftingContext.class, remap = false)
public class MixinCircuitHoistFlush {

    @Shadow
    @Final
    private List<CraftingTask> resolvedTasks;

    @Shadow
    @Final
    private List<RequestInProcessing> liveRequests;

    @Inject(method = "doWork", at = @At("RETURN"), require = 1)
    private void proghatches$settleHoistedCircuits(CallbackInfoReturnable<CraftingTask.State> cir) {
        if (cir.getReturnValue() != CraftingTask.State.SUCCESS) return;
        CircuitHoistTally.settle((CraftingContext) (Object) this, resolvedTasks, liveRequests);
    }
}
