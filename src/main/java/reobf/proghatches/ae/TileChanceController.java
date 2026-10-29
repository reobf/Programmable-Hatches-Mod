package reobf.proghatches.ae;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;
import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.google.common.collect.ImmutableSet;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.CraftingAllow;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.api.AEApi;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTRecipe;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.chance.ChanceData;
import reobf.proghatches.gt.metatileentity.chance.ChanceView;

/**
 * Keeps the outputs chance patterns roll beyond what is expected, taking them in as they reach the network, and
 * refills the buffers whose remaining inputs cannot make what they are expected to: from ME storage, by ordering the
 * inputs from the crafting system and holding them until a run's worth has arrived, or both.
 */
public class TileChanceController extends AENetworkTile
    implements IGuiHolder<PosGuiData>, ICellContainer, ICraftingRequester {

    public enum RefillMode {

        NONE,
        STOCK,
        CRAFT,
        STOCK_THEN_CRAFT;

        public boolean stock() {
            return this == STOCK || this == STOCK_THEN_CRAFT;
        }

        public boolean craft() {
            return this == CRAFT || this == STOCK_THEN_CRAFT;
        }
    }

    /** Just below the crafting CPUs, which take what they wait for first. */
    private static final int PRIORITY = Integer.MAX_VALUE - 1;
    /** Ticks before an input whose order could not be calculated or placed is ordered again. */
    private static final int RETRY_TICKS = 100;

    private RefillMode mode = RefillMode.STOCK;
    private int pendingMode;
    private final List<IAEStack<?>> deposits = new ArrayList<>();
    private final List<IAEStack<?>> pending = new ArrayList<>();
    private final List<IAEStack<?>> held = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Stat> stats = new ArrayList<>();
    private final BaseActionSource source = new MachineSource(this);
    private final Intercept items = new Intercept(ITEM_STACK_TYPE), fluids = new Intercept(FLUID_STACK_TYPE);
    private boolean releasing, wasActive;
    private long ordersTicked = -1;

    /** Totals of one output: what its runs make at the pattern's rate, what they rolled, and what was refilled. */
    private static final class Stat {

        final IAEStack<?> type;
        double planned, recipeExpected, refillCopies, refillItems;
        long produced;

        Stat(IAEStack<?> type) {
            this.type = type.copy();
            this.type.setStackSize(1);
        }
    }

    /** One input on order: being calculated, running on a CPU, or waiting to be tried again. */
    private static final class Order {

        final IAEStack<?> type;
        long amount, delivered;
        Future<ICraftingJob> future;
        ICraftingLink link;
        long retryAt;

        Order(IAEStack<?> type, long amount) {
            this.type = type.copy();
            this.type.setStackSize(1);
            this.amount = amount;
        }

        boolean active(long now) {
            return link != null || future != null || now < retryAt;
        }

        long onTheWay() {
            return Math.max(0, amount - delivered);
        }

        void cancel() {
            if (future != null) future.cancel(true);
            if (link != null) link.cancel();
            future = null;
            link = null;
            retryAt = 0;
        }
    }

    public TileChanceController() {
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public List<IAEStack<?>> deposits() {
        return deposits;
    }

    public List<IAEStack<?>> pending() {
        return pending;
    }

    public List<IAEStack<?>> held() {
        return held;
    }

    public RefillMode mode() {
        return mode;
    }

    public void setMode(RefillMode mode) {
        this.mode = mode;
        pendingMode = mode.ordinal();
        markDirty();
    }

    /** The surplus a recipe check rolled; it is taken in once it reaches the network. */
    public void expectSurplus(List<IAEStack<?>> surplus) {
        if (surplus.isEmpty()) return;
        for (IAEStack<?> s : surplus) ChanceData.add(pending, s);
        markDirty();
    }

    public void recordRuns(ChanceData c, int runs, List<IAEStack<?>> produced, GTRecipe recipe, double chanceMult) {
        for (int j = 0; j < c.outputs.size(); j++) {
            Stat s = stat(c.outputs.get(j));
            s.planned += c.promised(j, runs);
            s.recipeExpected += recipeExpectation(recipe, chanceMult, c.outputs.get(j)) * runs;
            s.produced = ChanceData.sat(s.produced, ChanceData.amountOf(produced, c.outputs.get(j)));
        }
        markDirty();
    }

    /** What one run of the recipe makes of the output on average, every slot of that output counted. */
    private static double recipeExpectation(GTRecipe recipe, double chanceMult, IAEStack<?> type) {
        if (recipe == null) return 0;
        double sum = 0;
        if (recipe.mOutputs != null) for (int i = 0; i < recipe.mOutputs.length; i++) {
            ItemStack s = recipe.mOutputs[i];
            if (s != null && ChanceData.same(type, ChanceData.of(s)))
                sum += s.stackSize * ((int) (recipe.getOutputChance(i) * chanceMult)) / 10000.0;
        }
        if (recipe.mFluidOutputs != null) for (int i = 0; i < recipe.mFluidOutputs.length; i++) {
            FluidStack f = recipe.mFluidOutputs[i];
            if (f != null && ChanceData.same(type, ChanceData.of(f)))
                sum += f.amount * ((int) (recipe.getFluidOutputChance(i) * chanceMult)) / 10000.0;
        }
        return sum;
    }

    private void recordRefill(ChanceData c, long runs) {
        for (int j = 0; j < c.outputs.size(); j++) {
            Stat s = stat(c.outputs.get(j));
            s.refillCopies += c.copies(runs);
            s.refillItems += c.promised(j, runs);
        }
    }

    private Stat stat(IAEStack<?> type) {
        for (Stat s : stats) if (ChanceData.same(s.type, type)) return s;
        Stat s = new Stat(type);
        stats.add(s);
        return s;
    }

    private long wanted(IAEStack<?> s) {
        return releasing ? 0 : ChanceData.amountOf(pending, s);
    }

    private void capture(IAEStack<?> s, long n) {
        take(pending, s, n);
        IAEStack<?> d = s.copy();
        d.setStackSize(n);
        ChanceData.add(deposits, d);
        markDirty();
    }

    private static void take(List<IAEStack<?>> list, IAEStack<?> type, long n) {
        int i = ChanceData.indexOf(list, type);
        if (i < 0) return;
        long rest = list.get(i)
            .getStackSize() - n;
        if (rest > 0) list.get(i)
            .setStackSize(rest);
        else list.remove(i);
    }

    @SuppressWarnings("rawtypes")
    private static IMEMonitor inv(IStorageGrid sg, IAEStack<?> s) {
        return s.isItem() ? sg.getItemInventory() : sg.getFluidInventory();
    }

    /** Puts n of a stack into the network; returns how many went in. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private long insert(IEnergyGrid eg, IStorageGrid sg, IAEStack<?> type, long n) {
        IAEStack s = type.copy();
        s.setStackSize(n);
        IAEStack rest;
        releasing = true;
        try {
            rest = Platform.poweredInsert(eg, inv(sg, type), s, source);
        } finally {
            releasing = false;
        }
        return n - (rest == null ? 0 : rest.getStackSize());
    }

    /** Puts n of a deposit into the network, where a crafting CPU waiting for it takes it first; returns how many went in. */
    private long release(IEnergyGrid eg, IStorageGrid sg, IAEStack<?> type, long n) {
        long moved = insert(eg, sg, type, n);
        if (moved > 0) {
            take(deposits, type, moved);
            markDirty();
        }
        return moved;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void tryRefill(BufferedDualInputHatch hatch, DualInvBuffer b) {
        ChanceData c = b.chance;
        if (c == null || !c.pending) return;
        IStorageGrid sg;
        IEnergyGrid eg;
        ICraftingGrid cg;
        try {
            sg = getProxy().getStorage();
            eg = getProxy().getEnergy();
            cg = getProxy().getCrafting();
        } catch (GridAccessException e) {
            return;
        }
        tickOrders(cg);
        for (int j = 0; j < c.debt.length; j++) {
            if (c.debt[j] <= 0) continue;
            IAEStack<?> out = c.outputs.get(j);
            long have = ChanceData.amountOf(deposits, out);
            if (have <= 0) continue;
            long moved = release(eg, sg, out, Math.min(c.debt[j], have));
            if (moved <= 0) continue;
            c.debt[j] -= moved;
            c.idle = 0;
        }
        if (!c.owed()) {
            c.pending = false;
            c.refilledRuns = 0;
            c.idle = 0;
            hatch.markDirty();
            return;
        }
        if (mode == RefillMode.NONE) return;
        List<IAEStack<?>> perRun = c.refillAmounts(1);
        if (perRun.isEmpty()) {
            c.pending = false;
            c.clearDebt();
            hatch.markDirty();
            return;
        }
        long want = c.runsToAdd(b.chanceContents());
        for (IAEStack<?> u : perRun) want = Math.min(want, b.chanceRoom(u) / u.getStackSize());
        if (want <= 0) return;
        long runs = want;
        for (IAEStack<?> u : perRun) {
            long have = ChanceData.amountOf(held, u);
            if (mode.stock()) {
                IAEStack req = u.copy();
                req.setStackSize(ChanceData.satMul(u.getStackSize(), runs));
                IAEStack sim = Platform.poweredExtraction(eg, inv(sg, u), req, source, Actionable.SIMULATE);
                have = ChanceData.sat(have, sim == null ? 0 : sim.getStackSize());
            }
            runs = Math.min(runs, have / u.getStackSize());
        }
        if (runs > 0 && refill(hatch, b, c, eg, sg, c.refillAmounts(runs), runs)) want -= runs;
        if (want > 0 && mode.craft()) order(perRun, want, cg);
    }

    /** Adds the given runs' inputs to the buffer, from the held inputs first and then from storage; all or nothing. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean refill(BufferedDualInputHatch hatch, DualInvBuffer b, ChanceData c, IEnergyGrid eg,
        IStorageGrid sg, List<IAEStack<?>> want, long runs) {
        List<IAEStack<?>> fromHeld = new ArrayList<>(), fromStorage = new ArrayList<>(), got = new ArrayList<>();
        boolean ok = true;
        for (IAEStack<?> w : want) {
            long n = w.getStackSize();
            long h = Math.min(n, ChanceData.amountOf(held, w));
            if (h > 0) {
                IAEStack<?> s = w.copy();
                s.setStackSize(h);
                take(held, w, h);
                fromHeld.add(s);
                ChanceData.add(got, s);
            }
            if (n > h) {
                IAEStack req = w.copy();
                req.setStackSize(n - h);
                IAEStack g = Platform.poweredExtraction(eg, inv(sg, w), req, source);
                if (g != null && g.getStackSize() > 0) {
                    fromStorage.add(g);
                    ChanceData.add(got, g);
                }
                if (g == null || g.getStackSize() < n - h) {
                    ok = false;
                    break;
                }
            }
        }
        if (ok) ok = b.chanceAdd(got);
        if (!ok) {
            for (IAEStack<?> s : fromHeld) ChanceData.add(held, s);
            for (IAEStack<?> s : fromStorage) insert(eg, sg, s, s.getStackSize());
            markDirty();
            return false;
        }
        c.pending = false;
        c.refilledRuns = ChanceData.sat(c.refilledRuns, runs);
        c.idle = ChanceData.sat(c.idle, runs);
        recordRefill(c, runs);
        hatch.markDirty();
        markDirty();
        return true;
    }

    /** Moves calculated orders onto a CPU and drops the finished ones; once per tick. */
    private void tickOrders(ICraftingGrid cg) {
        long now = worldObj.getTotalWorldTime();
        if (now == ordersTicked) return;
        ordersTicked = now;
        for (Iterator<Order> it = orders.iterator(); it.hasNext();) {
            Order o = it.next();
            if (o.link != null && (o.link.isDone() || o.link.isCanceled())) o.link = null;
            if (o.future != null && o.future.isDone()) {
                Future<ICraftingJob> f = o.future;
                o.future = null;
                ICraftingJob job = null;
                try {
                    job = f.get();
                } catch (Exception ignored) {}
                if (job != null && !job.isSimulation()) o.link = cg.submitJob(job, this, null, false, source);
                if (o.link == null) o.retryAt = now + RETRY_TICKS;
                markDirty();
            }
            if (!o.active(now)) it.remove();
        }
    }

    /** How much of an input the orders still running will deliver. */
    private long onTheWay(IAEStack<?> type) {
        long now = worldObj.getTotalWorldTime(), sum = 0;
        for (Order o : orders) if (o.active(now) && ChanceData.same(o.type, type)) sum = ChanceData.sat(sum, o.onTheWay());
        return sum;
    }

    private static boolean freeCpu(ICraftingGrid cg) {
        for (ICraftingCPU cpu : cg.getCpus()) {
            if (cpu.isBusy()) continue;
            if (cpu instanceof CraftingCPUCluster cl
                && (!cl.isActive() || cl.getCraftingAllowMode() == CraftingAllow.ONLY_PLAYER)) continue;
            return true;
        }
        return false;
    }

    /** Orders the first input the held stock and the orders on the way do not cover; one material of a buffer at a time. */
    private void order(List<IAEStack<?>> perRun, long runs, ICraftingGrid cg) {
        for (IAEStack<?> u : perRun) {
            long need = ChanceData.satMul(u.getStackSize(), runs) - ChanceData.amountOf(held, u) - onTheWay(u);
            if (need <= 0) continue;
            if (cg.canEmitFor(u) || !cg.getCraftingMultiPatterns()
                .containsKey(u)) continue;
            if (!freeCpu(cg)) return;
            Order o = new Order(u, need);
            IAEStack<?> req = u.copy();
            req.setStackSize(need);
            try {
                o.future = cg.beginCraftingJob(
                    worldObj,
                    getProxy().getGrid(),
                    source,
                    req,
                    null);
            } catch (GridAccessException e) {
                return;
            }
            orders.add(o);
            markDirty();
            return;
        }
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        List<ICraftingLink> links = new ArrayList<>();
        for (Order o : orders) if (o.link != null) links.add(o.link);
        return ImmutableSet.copyOf(links);
    }

    @Override
    public IAEStack<?> injectCraftedItems(ICraftingLink link, IAEStack<?> stack, Actionable mode) {
        if (stack == null) return null;
        IAEStack<?> s = stack;
        if (s instanceof IAEItemStack i && ItemFluidDrop.isFluidStack(i)) s = ItemFluidDrop.getAeFluidStack(i);
        if (mode == Actionable.MODULATE) {
            ChanceData.add(held, s);
            for (Order o : orders) if (o.link == link) o.delivered = ChanceData.sat(o.delivered, s.getStackSize());
            markDirty();
        }
        return null;
    }

    /** Whether a running crafting job is one of this controller's orders. */
    public boolean owns(ICraftingLink link) {
        if (link == null) return false;
        for (Order o : orders) if (o.link != null && o.link.getCraftingID()
            .equals(link.getCraftingID())) return true;
        return false;
    }

    /** Every 20 ticks: held inputs beyond what the buffers in debt are short of go back to the network, their orders are cancelled. */
    @TileEvent(TileEventType.TICK)
    public void tick_ChanceController() {
        if (worldObj.isRemote || worldObj.getTotalWorldTime() % 20 != 0 || (held.isEmpty() && orders.isEmpty())
            || !getProxy().isActive()) return;
        IStorageGrid sg;
        IEnergyGrid eg;
        List<IAEStack<?>> need = new ArrayList<>();
        try {
            sg = getProxy().getStorage();
            eg = getProxy().getEnergy();
            tickOrders(getProxy().getCrafting());
            if (mode != RefillMode.NONE) for (IGridNode n : getProxy().getGrid()
                .getNodes()) {
                if (!(n.getMachine() instanceof BufferedDualInputHatch h)) continue;
                for (DualInvBuffer b : h.inv0) {
                    ChanceData c = b.chance;
                    if (c == null || !c.owed()) continue;
                    for (IAEStack<?> u : c.refillAmounts(c.runsShort(b.chanceContents()))) ChanceData.add(need, u);
                }
            }
        } catch (GridAccessException e) {
            return;
        }
        long now = worldObj.getTotalWorldTime();
        List<IAEStack<?>> types = new ArrayList<>();
        for (IAEStack<?> s : held) index(types, s);
        for (Order o : orders) if (o.active(now)) index(types, o.type);
        boolean changed = false;
        for (IAEStack<?> t : types) {
            long gap = ChanceData.amountOf(need, t) - ChanceData.amountOf(held, t);
            if (gap > 0) continue;
            for (Order o : orders) if (o.active(now) && ChanceData.same(o.type, t)) {
                o.cancel();
                changed = true;
            }
            if (gap < 0) {
                long moved = insert(eg, sg, t, -gap);
                if (moved > 0) {
                    take(held, t, moved);
                    changed = true;
                }
            }
        }
        orders.removeIf(o -> !o.active(now));
        if (changed) markDirty();
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        for (Order o : orders) if (o.link == link) o.link = null;
        markDirty();
    }

    /** Puts every deposit and held input back into the network, drops the surplus still expected and cancels orders. */
    public void returnDeposits() {
        IStorageGrid sg;
        IEnergyGrid eg;
        try {
            sg = getProxy().getStorage();
            eg = getProxy().getEnergy();
        } catch (GridAccessException e) {
            return;
        }
        for (IAEStack<?> d : new ArrayList<>(deposits)) release(eg, sg, d, d.getStackSize());
        for (IAEStack<?> h : new ArrayList<>(held)) take(held, h, insert(eg, sg, h, h.getStackSize()));
        pending.clear();
        for (Order o : orders) o.cancel();
        orders.clear();
        markDirty();
    }

    public void resetStats() {
        stats.clear();
        markDirty();
    }

    /** Takes in the surplus still expected, as it reaches the network; it is never offered back to the network. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private final class Intercept implements IMEInventoryHandler<IAEStack> {

        private final IAEStackType<?> type;

        Intercept(IAEStackType<?> type) {
            this.type = type;
        }

        @Override
        public AccessRestriction getAccess() {
            return AccessRestriction.WRITE;
        }

        @Override
        public boolean isPrioritized(IAEStack input) {
            return wanted(input) > 0;
        }

        @Override
        public boolean canAccept(IAEStack input) {
            return wanted(input) > 0;
        }

        @Override
        public int getPriority() {
            return PRIORITY;
        }

        @Override
        public int getSlot() {
            return 0;
        }

        @Override
        public boolean validForPass(int i) {
            return i == 1;
        }

        @Override
        public IAEStack injectItems(IAEStack input, Actionable mode, BaseActionSource src) {
            long n = Math.min(wanted(input), input.getStackSize());
            if (n <= 0) return input;
            if (mode == Actionable.MODULATE) capture(input, n);
            if (n == input.getStackSize()) return null;
            IAEStack rest = input.copy();
            rest.setStackSize(input.getStackSize() - n);
            return rest;
        }

        @Override
        public IAEStack extractItems(IAEStack request, Actionable mode, BaseActionSource src) {
            return null;
        }

        @Override
        public IItemList<IAEStack> getAvailableItems(IItemList<IAEStack> out, int iteration) {
            return out;
        }

        @Override
        public IAEStack getAvailableItem(IAEStack request, int iteration) {
            return null;
        }

        @Override
        public IAEStackType<?> getStackType() {
            return type;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<IMEInventoryHandler> getCellArray(IAEStackType<?> type) {
        if (type == ITEM_STACK_TYPE) return Collections.singletonList(items);
        if (type == FLUID_STACK_TYPE) return Collections.singletonList(fluids);
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void saveChanges(IMEInventory cellInventory) {}

    @MENetworkEventSubscribe
    public void onPowerChange(MENetworkPowerStatusChange e) {
        updateActive();
    }

    @MENetworkEventSubscribe
    public void onChannelsChange(MENetworkChannelsChanged e) {
        updateActive();
    }

    private void updateActive() {
        boolean now = getProxy().isActive();
        if (now == wasActive) return;
        wasActive = now;
        try {
            getProxy().getGrid()
                .postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException ignored) {}
    }

    /** One entry per output: its totals, its deposit, and every buffer on the network expecting it. */
    public ChanceView.Screen screen() {
        List<IAEStack<?>> types = new ArrayList<>();
        for (Stat s : stats) index(types, s.type);
        for (IAEStack<?> s : deposits) index(types, s);
        for (IAEStack<?> s : pending) index(types, s);
        List<List<ChanceView.Line>> lines = new ArrayList<>();
        List<Long> expected = new ArrayList<>();
        try {
            for (IGridNode n : getProxy().getGrid()
                .getNodes()) {
                if (!(n.getMachine() instanceof BufferedDualInputHatch h) || h.getBaseMetaTileEntity() == null)
                    continue;
                IGregTechTileEntity te = h.getBaseMetaTileEntity();
                for (int i = 0; i < h.inv0.size(); i++) {
                    ChanceData c = h.inv0.get(i).chance;
                    if (c == null) continue;
                    for (int j = 0; j < c.outputs.size(); j++) {
                        int k = index(types, c.outputs.get(j));
                        while (lines.size() <= k) {
                            lines.add(new ArrayList<>());
                            expected.add(0L);
                        }
                        expected.set(k, ChanceData.sat(expected.get(k), c.debt[j]));
                        lines.get(k)
                            .add(
                                new ChanceView.Line(
                                    te.getXCoord(),
                                    te.getYCoord(),
                                    te.getZCoord(),
                                    i,
                                    c.debt[j],
                                    c.copies(c.refilledRuns),
                                    c.promised(j, c.refilledRuns),
                                    c.pending));
                    }
                }
            }
        } catch (GridAccessException ignored) {}
        List<ChanceView.Entry> entries = new ArrayList<>();
        for (int k = 0; k < types.size(); k++) {
            IAEStack<?> type = types.get(k);
            Stat s = null;
            for (Stat x : stats) if (ChanceData.same(x.type, type)) s = x;
            entries.add(
                new ChanceView.Entry(
                    ChanceView.Shown.of(type),
                    k < expected.size() ? expected.get(k) : 0,
                    ChanceData.amountOf(deposits, type),
                    ChanceData.amountOf(pending, type),
                    s == null ? 0 : s.planned,
                    s == null ? 0 : s.recipeExpected,
                    s == null ? 0 : s.produced,
                    s == null ? 0 : s.refillCopies,
                    s == null ? 0 : s.refillItems,
                    k < lines.size() ? lines.get(k) : Collections.emptyList()));
        }
        List<IAEStack<?>> inputs = new ArrayList<>();
        for (IAEStack<?> s : held) index(inputs, s);
        long now = worldObj.getTotalWorldTime();
        for (Order o : orders) if (o.active(now)) index(inputs, o.type);
        List<ChanceView.Held> helds = new ArrayList<>();
        for (IAEStack<?> type : inputs)
            helds.add(new ChanceView.Held(ChanceView.Shown.of(type), ChanceData.amountOf(held, type), onTheWay(type)));
        return new ChanceView.Screen(getProxy().isActive(), entries, helds);
    }

    private static int index(List<IAEStack<?>> types, IAEStack<?> s) {
        int i = ChanceData.indexOf(types, s);
        if (i >= 0) return i;
        IAEStack<?> t = s.copy();
        t.setStackSize(1);
        types.add(t);
        return types.size() - 1;
    }

    private static final int W = 248, H = 214;

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel panel = ModularPanel.defaultPanel("chance_controller", W, H);
        ChanceView.Screen[] view = { ChanceView.Screen.EMPTY };
        int[] selected = { 0 };
        pendingMode = mode.ordinal();
        GenericSyncValue<ChanceView.Screen, ?> sync = GenericSyncValue.builder(ChanceView.Screen.class)
            .getter(() -> worldObj.isRemote ? view[0] : screen())
            .setter(v -> view[0] = v)
            .adapter(ChanceView.Screen.ADAPTER)
            .copyImmutable()
            .build();
        syncManager.syncValue("screen", sync);
        IntSyncValue applied = new IntSyncValue(() -> mode.ordinal());
        syncManager.syncValue("mode", applied);
        IntSyncValue chosen = new IntSyncValue(() -> pendingMode, v -> pendingMode = v).allowC2S();
        syncManager.syncValue("chosen", chosen);
        Supplier<ChanceView.Entry> current = () -> {
            List<ChanceView.Entry> es = view[0].entries;
            return es.isEmpty() ? null : es.get(Math.min(selected[0], es.size() - 1));
        };

        panel.child(
            new TextWidget<>(IKey.lang("tile.proghatches.chance_controller.name")).pos(7, 6)
                .size(150, 10));
        panel.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> StatCollector.translateToLocal(
                        view[0].online ? "proghatches.chance.gui.online" : "proghatches.chance.gui.offline")))
                .alignment(Alignment.CenterRight)
                .pos(W - 87, 6)
                .size(80, 10));

        ListWidget<IWidget, ?> icons = new ListWidget<>();
        icons.pos(7, 18)
            .size(26, H - 68)
            .onUpdateListener(w -> {
                while (w.getChildren()
                    .size() < view[0].entries.size())
                    w.child(
                        iconButton(
                            w.getChildren()
                                .size(),
                            view,
                            selected));
            });
        panel.child(icons);

        panel.child(((IDrawable) (ctx, x, y, w, h, theme) -> {
            ChanceView.Entry e = current.get();
            if (e != null) e.shown.icon()
                .draw(ctx, x, y, w, h, theme);
        }).asWidget()
            .pos(38, 18)
            .size(16, 16));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> {
                ChanceView.Entry e = current.get();
                return e == null ? StatCollector.translateToLocal("proghatches.chance.gui.none") : e.shown.name();
            })).pos(58, 22)
                .size(W - 65, 10));
        row(panel, current, 38, e -> fmt("proghatches.chance.gui.expected", e.expected));
        row(panel, current, 48, e -> fmt("proghatches.chance.gui.deposit", e.deposit, e.pending));
        row(panel, current, 58, e -> fmt("proghatches.chance.gui.planned", ChanceView.num(e.planned)));
        row(
            panel,
            current,
            68,
            e -> fmt(
                "proghatches.chance.gui.produced",
                e.produced,
                e.planned > 0 ? String.format("%.1f%%", e.produced * 100 / e.planned) : "-"));
        row(panel, current, 78, e -> fmt("proghatches.chance.gui.recipe", ChanceView.num(e.recipeExpected), e.ratio()));
        row(
            panel,
            current,
            88,
            e -> fmt("proghatches.chance.gui.refills", ChanceView.num(e.refillCopies), ChanceView.num(e.refillItems)));
        row(panel, current, 100, e -> fmt("proghatches.chance.gui.buffers", e.lines.size()));

        ListWidget<IWidget, ?> lines = new ListWidget<>();
        lines.pos(38, 111)
            .size(W - 45, H - 153)
            .onUpdateListener(w -> {
                ChanceView.Entry e = current.get();
                int n = e == null ? 0 : e.lines.size();
                while (w.getChildren()
                    .size() < n)
                    w.child(
                        lineWidget(
                            w.getChildren()
                                .size(),
                            current));
            });
        panel.child(lines);

        panel.child(
            new ButtonWidget<>().pos(38, H - 38)
                .size(112, 14)
                .overlay(IKey.dynamic(() -> StatCollector.translateToLocal("proghatches.chance.gui.mode." + chosen.getIntValue())))
                .onMousePressed(button -> {
                    chosen.setIntValue((chosen.getIntValue() + (button == 1 ? 3 : 1)) % RefillMode.values().length);
                    return true;
                })
                .tooltipDynamic(t -> {
                    lines(t, StatCollector.translateToLocal("proghatches.chance.gui.mode.tip." + chosen.getIntValue()));
                    if (chosen.getIntValue() >= 2) lines(t, StatCollector.translateToLocal("proghatches.chance.gui.craft.rules"));
                    lines(t, StatCollector.translateToLocal("proghatches.chance.gui.mode.click"));
                    if (!view[0].held.isEmpty()) {
                        t.addLine(fmt("proghatches.chance.gui.held", view[0].held.size()));
                        for (ChanceView.Held h : view[0].held) t.add(
                            h.shown.icon()
                                .asIcon()
                                .size(8))
                            .add(" " + fmt("proghatches.chance.gui.held.line", h.shown.name(), h.held, h.ordered))
                            .newLine();
                    }
                })
                .tooltipAutoUpdate(true));
        panel.child(
            new ButtonWidget<>().pos(154, H - 38)
                .size(48, 14)
                .overlay(IKey.lang("proghatches.chance.gui.apply"))
                .tooltipDynamic(t -> lines(t, StatCollector.translateToLocal("proghatches.chance.gui.apply.tooltip")))
                .setEnabledIf(w -> chosen.getIntValue() != applied.getIntValue())
                .syncHandler(new InteractionSyncHandler().setOnMousePressed(m -> {
                    if (!m.isClient()) setMode(RefillMode.values()[Math.floorMod(pendingMode, RefillMode.values().length)]);
                })));
        panel.child(
            new ButtonWidget<>().pos(38, H - 22)
                .size(80, 14)
                .overlay(IKey.lang("proghatches.chance.gui.return"))
                .tooltipDynamic(t -> lines(t, StatCollector.translateToLocal("proghatches.chance.gui.return.tooltip")))
                .syncHandler(new InteractionSyncHandler().setOnMousePressed(m -> {
                    if (!m.isClient()) returnDeposits();
                })));
        panel.child(
            new ButtonWidget<>().pos(122, H - 22)
                .size(80, 14)
                .overlay(IKey.lang("proghatches.chance.gui.reset"))
                .tooltipDynamic(t -> lines(t, StatCollector.translateToLocal("proghatches.chance.gui.reset.tooltip")))
                .syncHandler(new InteractionSyncHandler().setOnMousePressed(m -> {
                    if (!m.isClient() && m.shift) resetStats();
                })));
        return panel;
    }

    private static String fmt(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(key, args);
    }

    private static void lines(RichTooltip t, String text) {
        for (String line : text.split("\\\\n")) t.addLine(line);
    }

    private static void row(ModularPanel panel, Supplier<ChanceView.Entry> current, int y,
        Function<ChanceView.Entry, String> text) {
        panel.child(new TextWidget<>(IKey.dynamic(() -> {
            ChanceView.Entry e = current.get();
            return e == null ? "" : text.apply(e);
        })).pos(38, y)
            .size(W - 45, 10));
    }

    private static IWidget iconButton(int i, ChanceView.Screen[] view, int[] selected) {
        return new ButtonWidget<>().size(20, 20)
            .background(GuiTextures.SLOT_ITEM)
            .overlay((IDrawable) (ctx, x, y, w, h, theme) -> {
                if (i >= view[0].entries.size()) return;
                view[0].entries.get(i).shown.icon()
                    .draw(ctx, x + 2, y + 2, 16, 16, theme);
                if (selected[0] == i) GuiDraw.drawRect(x, y, w, h, 0x60FFFFFF);
            })
            .onMousePressed(b -> {
                selected[0] = i;
                return true;
            })
            .tooltipDynamic(t -> {
                if (i >= view[0].entries.size()) return;
                ChanceView.Entry e = view[0].entries.get(i);
                t.addLine(e.shown.name());
                t.addLine(fmt("proghatches.chance.gui.expected", e.expected));
                t.addLine(fmt("proghatches.chance.gui.deposit", e.deposit, e.pending));
                t.addLine(fmt("proghatches.chance.gui.recipe", ChanceView.num(e.recipeExpected), e.ratio()));
            })
            .tooltipAutoUpdate(true)
            .setEnabledIf(w -> i < view[0].entries.size());
    }

    private static IWidget lineWidget(int i, Supplier<ChanceView.Entry> current) {
        return new TextWidget<>(IKey.dynamic(() -> {
            ChanceView.Entry e = current.get();
            if (e == null || i >= e.lines.size()) return "";
            ChanceView.Line l = e.lines.get(i);
            String head = fmt("proghatches.chance.gui.buffer", l.x, l.y, l.z, l.index)
                + (l.pending ? " " + StatCollector.translateToLocal("proghatches.chance.pending") : "");
            return head + "\n"
                + fmt("proghatches.chance.gui.buffer.line", l.expected, ChanceView.num(l.copies), ChanceView.num(l.items));
        })).size(W - 53, 20)
            .setEnabledIf(w -> {
                ChanceView.Entry e = current.get();
                return e != null && i < e.lines.size();
            });
    }

    private static NBTTagList writeList(List<IAEStack<?>> list) {
        NBTTagList l = new NBTTagList();
        for (IAEStack<?> s : list) l.appendTag(ChanceData.write(s));
        return l;
    }

    private static void readList(NBTTagCompound data, String key, List<IAEStack<?>> into) {
        into.clear();
        NBTTagList l = data.getTagList(key, 10);
        for (int i = 0; i < l.tagCount(); i++) {
            IAEStack<?> s = ChanceData.load(l.getCompoundTagAt(i));
            if (s != null && s.getStackSize() > 0) into.add(s);
        }
    }

    private void writeStats(NBTTagCompound data) {
        NBTTagList l = new NBTTagList();
        for (Stat s : stats) {
            NBTTagCompound e = ChanceData.write(s.type);
            e.setDouble("phPlanned", s.planned);
            e.setDouble("phRecipeExpected", s.recipeExpected);
            e.setLong("phProduced", s.produced);
            e.setDouble("phRefillCopies", s.refillCopies);
            e.setDouble("phRefillItems", s.refillItems);
            l.appendTag(e);
        }
        data.setTag("phStats", l);
    }

    private void readStats(NBTTagCompound data) {
        stats.clear();
        NBTTagList l = data.getTagList("phStats", 10);
        for (int i = 0; i < l.tagCount(); i++) {
            NBTTagCompound e = l.getCompoundTagAt(i);
            IAEStack<?> type = ChanceData.load(e);
            if (type == null) continue;
            Stat s = new Stat(type);
            s.planned = e.getDouble("phPlanned");
            s.recipeExpected = e.getDouble("phRecipeExpected");
            s.produced = e.getLong("phProduced");
            s.refillCopies = e.getDouble("phRefillCopies");
            s.refillItems = e.getDouble("phRefillItems");
            stats.add(s);
        }
    }

    private void writeOrders(NBTTagCompound data) {
        NBTTagList l = new NBTTagList();
        for (Order o : orders) {
            if (o.link == null) continue;
            NBTTagCompound e = ChanceData.write(o.type);
            e.setLong("phAmount", o.amount);
            e.setLong("phDelivered", o.delivered);
            NBTTagCompound link = new NBTTagCompound();
            o.link.writeToNBT(link);
            e.setTag("phLink", link);
            l.appendTag(e);
        }
        data.setTag("phOrders", l);
    }

    private void readOrders(NBTTagCompound data) {
        orders.clear();
        NBTTagList l = data.getTagList("phOrders", 10);
        for (int i = 0; i < l.tagCount(); i++) {
            NBTTagCompound e = l.getCompoundTagAt(i);
            IAEStack<?> type = ChanceData.load(e);
            if (type == null) continue;
            Order o = new Order(type, e.getLong("phAmount"));
            o.delivered = e.getLong("phDelivered");
            o.link = AEApi.instance()
                .storage()
                .loadCraftingLink(e.getCompoundTag("phLink"), this);
            if (o.link != null) orders.add(o);
        }
    }

    /** The deposits, held inputs and totals a dropped controller carries; empty when there is nothing to keep. */
    public NBTTagCompound dropTag() {
        NBTTagCompound tag = new NBTTagCompound();
        if (!deposits.isEmpty()) tag.setTag("phDeposits", writeList(deposits));
        if (!held.isEmpty()) tag.setTag("phHeld", writeList(held));
        if (!stats.isEmpty()) writeStats(tag);
        if (mode != RefillMode.STOCK) tag.setInteger("phMode", mode.ordinal());
        return tag;
    }

    public void loadDropTag(NBTTagCompound tag) {
        readList(tag, "phDeposits", deposits);
        readList(tag, "phHeld", held);
        readStats(tag);
        if (tag.hasKey("phMode")) setMode(RefillMode.values()[Math.floorMod(tag.getInteger("phMode"), RefillMode.values().length)]);
        markDirty();
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBT_ChanceController(NBTTagCompound data) {
        data.setTag("phDeposits", writeList(deposits));
        data.setTag("phPending", writeList(pending));
        data.setTag("phHeld", writeList(held));
        data.setInteger("phMode", mode.ordinal());
        writeStats(data);
        writeOrders(data);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBT_ChanceController(NBTTagCompound data) {
        readList(data, "phDeposits", deposits);
        readList(data, "phPending", pending);
        readList(data, "phHeld", held);
        mode = RefillMode.values()[Math.floorMod(data.getInteger("phMode"), RefillMode.values().length)];
        pendingMode = mode.ordinal();
        readStats(data);
        readOrders(data);
    }
}
