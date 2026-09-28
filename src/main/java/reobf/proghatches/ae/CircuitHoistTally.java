package reobf.proghatches.ae;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.v2.CraftingContext;
import appeng.crafting.v2.CraftingContext.RequestInProcessing;
import appeng.crafting.v2.CraftingRequest;
import appeng.crafting.v2.CraftingRequest.SubstitutionMode;
import appeng.crafting.v2.CraftingRequest.UsedResolverEntry;
import appeng.crafting.v2.resolvers.CraftableItemResolver.CraftFromPatternTask;
import appeng.crafting.v2.resolvers.CraftableItemResolver.RequestAndPerCraftAmount;
import appeng.crafting.v2.resolvers.CraftingTask;
import appeng.crafting.v2.resolvers.ExtractItemResolver.ExtractItemTask;
import appeng.crafting.v2.resolvers.SimulateMissingItemResolver.ConjureItemTask;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider.CircuitProviderPatternDetial;
import reobf.proghatches.gt.metatileentity.multi.LargeProgrammingCircuitProvider;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;

/**
 * Per-calculation bookkeeping that takes virtual circuits out of AE2's crafting calculator.
 * <p>
 * Why: AE2's v2 calculator creates one child request per pattern input per pattern application and
 * never merges requests across nodes. A virtual circuit therefore costs every GT pattern node three
 * finished tasks (ExtractItemTask, CheckOtherResolversTask, the provider's CraftFromPatternTask), all
 * of which count towards AEConfig.maxCraftingSteps. On the CPU side all of those provider crafts
 * already collapse into one entry (CraftingCPUCluster.addCrafting merges by pattern), and the CPU has
 * no tree ordering at all, so the per-node requests buy nothing at execution time.
 * <p>
 * How: while a task expands its inputs, {@link #strip} removes every virtual circuit and remembers
 * (task, stack, per-craft amount). Once the calculator's queue runs dry, {@link #settle} sums
 * totalCraftsDone x perCraft per circuit (final values, so refunds are already applied) and settles
 * each circuit type directly against ME storage and the circuit provider, without queueing anything,
 * so circuits take no calculation rounds. When the job is handed to a CPU, {@link #deliverInstantly}
 * lets a provider carrying the upgrade chip put the circuits straight into the CPU.
 * <p>
 * Only circuits listed as plain inputs of a processing pattern are covered. One in a workbench
 * pattern, or one that is also an output of its pattern (recursive input), stays where it is.
 */
public final class CircuitHoistTally {

    private static final class Entry {

        final CraftFromPatternTask task;
        final IAEStack<?> stack;
        final long perCraft;

        Entry(CraftFromPatternTask task, IAEStack<?> stack, long perCraft) {
            this.task = task;
            this.stack = stack;
            this.perCraft = perCraft;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private boolean settled;

    private CircuitHoistTally() {}

    private static CircuitHoistTally of(CraftingContext context) {
        return context.getUserCache(CircuitHoistTally.class, CircuitHoistTally::new);
    }

    private static ICircuitHoistTask view(CraftFromPatternTask task) {
        return (ICircuitHoistTask) (Object) task;
    }

    private static boolean isVirtualCircuit(IAEStack<?> stack) {
        return stack instanceof IAEItemStack && ((IAEItemStack) stack).getItem() == MyMod.progcircuit;
    }

    /**
     * Removes virtual circuits from a task's expanded input list, recording them for {@link #settle}.
     * Returns the list untouched when nothing was stripped, and strips nothing once settlement has
     * happened: a task that runs after it keeps its circuit as an ordinary child request, which is
     * always correct, instead of feeding a tally nobody reads any more.
     */
    public static List<IAEStack<?>> strip(CraftingContext context, CraftFromPatternTask task,
        List<IAEStack<?>> expanded) {
        CircuitHoistTally tally = of(context);
        if (tally.settled) return expanded;
        List<IAEStack<?>> kept = null;
        for (int i = 0; i < expanded.size(); i++) {
            IAEStack<?> stack = expanded.get(i);
            if (stack != null && stack.getStackSize() > 0 && isVirtualCircuit(stack)) {
                if (kept == null) kept = new ArrayList<>(expanded.subList(0, i));
                // copy: TunnelPatternExpander hands back the pattern's own stack instances
                tally.entries.add(new Entry(task, stack.copy(), stack.getStackSize()));
            } else if (kept != null) {
                kept.add(stack);
            }
        }
        return kept == null ? expanded : kept;
    }

    private static CraftingRequest rootOf(CraftingRequest request) {
        while (request.parentRequest != null) request = request.parentRequest;
        return request;
    }

    /**
     * Called from the tail of CraftingContext.doWork when the queue has run dry, with the context's own
     * bookkeeping lists. Settles every tallied circuit on the spot: nothing is queued, so circuits take
     * no calculation rounds at all, however many pattern nodes use them.
     * <p>
     * Sources, in order: ME storage (and byproducts), then a circuit provider. Nothing else. Ordinary
     * patterns that happen to output the circuit are never consulted, and whatever the two sources
     * cannot cover is reported as missing the way AE reports any missing ingredient.
     * <p>
     * The tasks are AE's own classes, run directly and appended to resolvedTasks, so extraction into
     * the CPU, the provider's craft count, the crafting plan and the tree view all work unchanged.
     */
    public static void settle(CraftingContext context, List<CraftingTask> resolvedTasks,
        List<RequestInProcessing> liveRequests) {
        CircuitHoistTally tally = of(context);
        if (tally.settled) return;
        tally.settled = true;
        if (tally.entries.isEmpty()) return;

        Map<IAEStack<?>, Long> totals = new LinkedHashMap<>();
        CraftingRequest root = null;
        for (Entry e : tally.entries) {
            long crafts = view(e.task).proghatches$totalCraftsDone();
            if (crafts <= 0) continue; // fully refunded node, contributes nothing
            totals.merge(e.stack, Math.multiplyExact(crafts, e.perCraft), (a, b) -> Math.addExact(a, b));
            if (root == null) root = rootOf(e.task.request);
        }
        if (totals.isEmpty()) return;

        // The final pattern: first root-level pattern task that actually crafts. Any node that still
        // has crafts left has such an ancestor (fullRefund cascades to children), so this exists
        // whenever totals is non-empty; if it ever does not, the circuits are still settled (bytes,
        // plan and CPU stay right) but the tree view cannot show them, which is worth a warning.
        CraftFromPatternTask rootTask = null;
        for (UsedResolverEntry used : root.usedResolvers) {
            if (used.task instanceof CraftFromPatternTask
                && view((CraftFromPatternTask) used.task).proghatches$totalCraftsDone() > 0) {
                rootTask = (CraftFromPatternTask) used.task;
                break;
            }
        }
        if (rootTask == null) {
            MyMod.LOG.warn(
                "circuit hoisting: no crafting pattern task under the root request for {}; the circuits will not appear in the crafting tree view",
                root.stack);
        }

        for (Map.Entry<IAEStack<?>, Long> total : totals.entrySet()) {
            IAEStack<?> stack = total.getKey()
                .copy();
            stack.setStackSize(total.getValue());
            CraftingRequest request = new CraftingRequest(
                root,
                stack,
                SubstitutionMode.PRECISE,
                true,
                root.craftingMode,
                s -> true,
                1);
            // perCraftAmount 0: never consumed by calculateOneStep (the root task is done), and the
            // refund loops multiply by it, so an accidental refund of the root asks this child for 0.
            if (rootTask != null) {
                view(rootTask).proghatches$childRequests()
                    .add(new RequestAndPerCraftAmount(request, 0));
            }
            liveRequests.add(new RequestInProcessing(request)); // the job's byte total is summed over these

            // 1. storage
            ExtractItemTask<?> extract = new ExtractItemTask<>(request);
            extract.calculateOneStep(context);
            resolvedTasks.add(extract);

            // 2. circuit provider
            if (request.remainingToProcess > 0) {
                for (ICraftingPatternDetails pattern : context.getPrecisePatternsFor(stack)) {
                    if (!(pattern instanceof CircuitProviderPatternDetial)) continue;
                    FastCircuitTask craft = new FastCircuitTask(
                        request,
                        pattern,
                        hasInstantProvider(context, pattern));
                    if (craft.getState() == CraftingTask.State.FAILURE) continue;
                    craft.settle(context);
                    resolvedTasks.add(craft);
                    break;
                }
            }

            // 3. nothing else: the remainder is missing
            if (request.remainingToProcess > 0) {
                ConjureItemTask missing = new ConjureItemTask(request);
                missing.calculateOneStep(context);
                resolvedTasks.add(missing);
            }

            if (Config.debug) {
                MyMod.LOG.info("settled {} x {} at the crafting root without queueing", total.getValue(), stack);
            }
        }
    }

    /**
     * True when an active Large Programming Circuit Provider carrying the upgrade chip serves this
     * provider pattern right now.
     */
    public static boolean hasInstantProvider(CraftingContext context, ICraftingPatternDetails pattern) {
        if (!(pattern instanceof CircuitProviderPatternDetial)) return false;
        if (!(context.craftingGrid instanceof CraftingGridCache)) return false;
        for (ICraftingMedium medium : ((CraftingGridCache) context.craftingGrid).getMediums(pattern)) {
            if (medium instanceof LargeProgrammingCircuitProvider) {
                LargeProgrammingCircuitProvider provider = (LargeProgrammingCircuitProvider) medium;
                if (provider.instant() && provider.getBaseMetaTileEntity() != null
                    && provider.getBaseMetaTileEntity()
                        .isActive()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Job start, provider craft task: when a Large Programming Circuit Provider carrying the upgrade
     * chip serves this pattern, put all the circuits into the CPU now instead of registering a
     * crafting task the CPU would have to push to the provider. Returns true when it did, in which
     * case the caller skips addCrafting.
     * <p>
     * Not used when the circuit is the job's own final output: that has to go through the CPU's
     * normal task and final-output accounting.
     */
    public static boolean deliverInstantly(CraftingContext context, CraftingCPUCluster cpu, CraftFromPatternTask task,
        long crafts) {
        if (crafts <= 0 || task.request == null || task.request.parentRequest == null) return false;
        if (!hasInstantProvider(context, task.pattern)) return false;
        IAEItemStack made = AEItemStack.create(((CircuitProviderPatternDetial) task.pattern).out);
        if (made == null) return false;
        made.setStackSize(Math.multiplyExact(made.getStackSize(), crafts));
        cpu.addStorage(made);
        return true;
    }
}
