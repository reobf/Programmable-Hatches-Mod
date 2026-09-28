package reobf.proghatches.ae;

import java.io.IOException;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.crafting.MECraftingInventory;
import appeng.crafting.v2.CraftingContext;
import appeng.crafting.v2.CraftingRequest;
import appeng.crafting.v2.CraftingTreeSerializer;
import appeng.crafting.v2.ITreeSerializable;
import appeng.crafting.v2.resolvers.CraftableItemResolver.CraftFromPatternTask;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import reobf.proghatches.main.MyMod;

/**
 * The node every virtual-circuit craft is shown under in the crafting tree.
 * <p>
 * Circuits are taken out of the calculator and settled in one go (see {@link CircuitHoistTally}), so
 * the tree no longer has a circuit child under each pattern. This task is what stands in for all of
 * them: one per circuit type, under the final pattern, with a tooltip saying which channel was used.
 * <p>
 * It extends AE's CraftFromPatternTask on purpose. The tree GUI picks icon and description with an
 * instanceof chain over AE's own task classes, and the optimize-patterns container and the crafting
 * plan read CraftFromPatternTask fields, so a subclass is drawn and counted like any pattern craft
 * while a standalone CraftingTask would show up blank.
 * <p>
 * instant == true means a provider carrying the upgrade chip was on the grid at calculation time: the
 * circuits are put straight into the CPU at job start and the CPU never pushes anything for them. If
 * that provider is gone by the time the job starts, the task falls back to an ordinary provider craft.
 */
public class FastCircuitTask extends CraftFromPatternTask {

    /** Registered with CraftingTreeSerializer on both sides; part of the tree packet format. */
    public static final String SERIAL_ID = "proghatches:fastcircuit";

    private final boolean instant;

    public FastCircuitTask(CraftingRequest request, ICraftingPatternDetails pattern, boolean instant) {
        super(request, pattern, 0, false, false);
        this.instant = instant;
    }

    /** Client side, rebuilt from the tree packet. */
    @SuppressWarnings("unused")
    public FastCircuitTask(CraftingTreeSerializer serializer, ITreeSerializable parent) throws IOException {
        super(serializer, parent);
        this.instant = serializer.getBuffer()
            .readBoolean();
    }

    @Override
    public List<? extends ITreeSerializable> serializeTree(CraftingTreeSerializer serializer) throws IOException {
        List<? extends ITreeSerializable> children = super.serializeTree(serializer);
        serializer.getBuffer()
            .writeBoolean(instant);
        return children;
    }

    /**
     * Runs both passes in place. The provider pattern has no inputs, so the first pass asks for nothing
     * and the second fulfils the request. Afterwards the node icon is switched to the upgrade chip when
     * the instant channel applies, so the two channels can be told apart at a glance.
     */
    public void settle(CraftingContext context) {
        calculateOneStep(context);
        if (getState() == State.NEEDS_MORE_WORK) calculateOneStep(context);
        if (instant && MyMod.chip != null) {
            this.craftingMachine = AEItemStack.create(new ItemStack(MyMod.chip));
        }
    }

    @Override
    public void startOnCpu(CraftingContext context, CraftingCPUCluster cpuCluster, MECraftingInventory craftingInv) {
        if (instant && CircuitHoistTally.deliverInstantly(context, cpuCluster, this, totalCraftsDone)) return;
        super.startOnCpu(context, cpuCluster, craftingInv);
    }

    @Override
    public String getTooltipText() {
        return StatCollector.translateToLocal("proghatches.fastcircuit.title") + "\n "
            + StatCollector
                .translateToLocal(instant ? "proghatches.fastcircuit.instant" : "proghatches.fastcircuit.provider")
            + "\n"
            + super.getTooltipText();
    }
}
