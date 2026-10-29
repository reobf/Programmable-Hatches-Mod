package reobf.proghatches.gt.metatileentity.chance;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.GTRecipe;
import gregtech.common.tileentities.machines.IDualInputInventory;
import reobf.proghatches.ae.TileChanceController;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;

/** Controller-side hooks: which chance buffer a recipe check consumed, and what the machine rolled for it. */
public final class ChanceTracker {

    private static GTRecipe applied;
    private static double appliedChanceMult = 1;

    private ChanceTracker() {}

    /** The recipe a processing logic is applying, with the chance multiplier it rolls with. */
    public static void applied(GTRecipe recipe, double chanceMult) {
        applied = recipe;
        appliedChanceMult = chanceMult;
    }

    public static Object before(IDualInputInventory slot) {
        if (!(slot instanceof DualInvBuffer b) || b.chance == null) return null;
        applied = null;
        appliedChanceMult = 1;
        return b.chanceContents();
    }

    @SuppressWarnings("unchecked")
    public static Object after(IDualInputInventory slot, Object snapshot, int parallels) {
        DualInvBuffer b = (DualInvBuffer) slot;
        if (b.chance == null) return null;
        List<IAEStack<?>> before = (List<IAEStack<?>>) snapshot, now = b.chanceContents(), diff = new ArrayList<>();
        for (IAEStack<?> s : before) {
            long n = s.getStackSize() - ChanceData.amountOf(now, s);
            if (n <= 0) continue;
            IAEStack<?> d = s.copy();
            d.setStackSize(n);
            diff.add(d);
        }
        b.chance.onConsumed(diff, parallels);
        b.chance.lastRecipe = applied;
        b.chance.lastChanceMult = appliedChanceMult;
        return b;
    }

    public static void settle(MTEMultiBlockBase controller, Object token) {
        if (!(token instanceof DualInvBuffer b)) return;
        ChanceData c = b.chance;
        if (c == null) return;
        List<IAEStack<?>> produced = new ArrayList<>();
        if (controller.mOutputItems != null)
            for (ItemStack s : controller.mOutputItems) ChanceData.add(produced, ChanceData.of(s));
        if (controller.mOutputFluids != null)
            for (FluidStack f : controller.mOutputFluids) ChanceData.add(produced, ChanceData.of(f));
        List<IAEStack<?>> surplus = c.settle(produced);
        BufferedDualInputHatch hatch = b.hatch();
        TileChanceController t = findController(hatch);
        if (t != null) {
            t.recordRuns(c, c.lastRuns, produced, c.lastRecipe, c.lastChanceMult);
            t.expectSurplus(surplus);
        }
        c.lastRecipe = null;
        c.pending = c.needsRefill(b.chanceContents());
        if (!c.owed()) {
            c.refilledRuns = 0;
            c.idle = 0;
        }
        hatch.markDirty();
    }

    /** Whether the hatch's ME network is up, so that a controller missing from it is really missing. */
    public static boolean online(BufferedDualInputHatch hatch) {
        return !(hatch instanceof IGridProxyable gp) || gp.getProxy()
            .isActive();
    }

    public static TileChanceController findController(BufferedDualInputHatch hatch) {
        if (!(hatch instanceof IGridProxyable gp)) return null;
        try {
            for (IGridNode n : gp.getProxy()
                .getGrid()
                .getMachines(TileChanceController.class))
                if (n.getMachine() instanceof TileChanceController t && t.getProxy()
                    .isActive()) return t;
        } catch (GridAccessException ignored) {}
        return null;
    }
}
