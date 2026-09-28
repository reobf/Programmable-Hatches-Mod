package reobf.proghatches.main.mixin.mixins.part2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.crafting.MECraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.v2.CraftingContext;
import appeng.crafting.v2.resolvers.CraftableItemResolver.CraftFromPatternTask;
import appeng.crafting.v2.resolvers.CraftableItemResolver.RequestAndPerCraftAmount;
import appeng.me.cache.CraftingGridCache;
import reobf.proghatches.ae.CircuitHoistTally;
import reobf.proghatches.ae.ICircuitHoistTask;
import reobf.proghatches.main.Config;

/**
 * Half one of circuit hoisting (see {@link CircuitHoistTally}): drop provider-suppliable virtual
 * circuits from a processing pattern's inputs before the task turns them into child requests.
 * <p>
 * The wrap sits on the TunnelPatternExpander call because that is the one place the non-craftable
 * branch of calculateOneStep materialises its input list: it runs once per task (guarded by
 * requestedInputs), has the CraftingContext in scope (the constructor, where
 * MixinCraftFromPatternTaskPatch works, does not), and never sees workbench or recursive-input
 * handling, so nothing but GT-style processing patterns is affected.
 */
@Mixin(value = CraftFromPatternTask.class, remap = false)
public abstract class MixinCircuitHoistStrip implements ICircuitHoistTask {

    @Shadow
    protected long totalCraftsDone;

    @Shadow
    @Final
    protected ArrayList<RequestAndPerCraftAmount> childRequests;

    @Override
    public long proghatches$totalCraftsDone() {
        return totalCraftsDone;
    }

    @Override
    public List<RequestAndPerCraftAmount> proghatches$childRequests() {
        return childRequests;
    }

    @WrapOperation(
        method = "calculateOneStep",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/util/TunnelPatternExpander;expandInputs([Lappeng/api/storage/data/IAEStack;Lappeng/me/cache/CraftingGridCache;Ljava/util/Set;)Ljava/util/List;"),
        require = 1)
    private List<IAEStack<?>> proghatches$stripHoistableCircuits(IAEStack<?>[] inputs, CraftingGridCache cache,
        Set<ICraftingPatternDetails> parentPatterns, Operation<List<IAEStack<?>>> original,
        @Local(argsOnly = true) CraftingContext context) {
        List<IAEStack<?>> expanded = original.call(inputs, cache, parentPatterns);
        if (expanded == null || expanded.isEmpty() || !Config.hoistCircuitRequests) return expanded;
        return CircuitHoistTally.strip(context, (CraftFromPatternTask) (Object) this, expanded);
    }

    /**
     * Job start: a provider with the upgrade chip hands over all circuits at once, so the CPU gets them
     * as stock and no crafting task is registered for it to push one operation at a time.
     */
    @Inject(method = "startOnCpu", at = @At("HEAD"), cancellable = true, require = 1)
    private void proghatches$instantCircuits(CraftingContext context, CraftingCPUCluster cpuCluster,
        MECraftingInventory craftingInv, CallbackInfo ci) {
        if (CircuitHoistTally
            .deliverInstantly(context, cpuCluster, (CraftFromPatternTask) (Object) this, totalCraftsDone)) {
            ci.cancel();
        }
    }
}
