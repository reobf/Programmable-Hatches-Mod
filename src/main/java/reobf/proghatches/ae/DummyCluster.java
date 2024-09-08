/*
 * This file is part of Applied Energistics 2. Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved. Applied
 * Energistics 2 is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Applied Energistics 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details. You should have received a copy of the GNU Lesser General Public License along with
 * Applied Energistics 2. If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package reobf.proghatches.ae;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.api.util.WorldCoord;
import appeng.container.ContainerNull;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingWatcher;
import appeng.crafting.MECraftingInventory;
import appeng.helpers.DualityInterface;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.IAECluster;
import appeng.tile.AEBaseTile;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;

public final class DummyCluster implements IAECluster, ICraftingCPU {

    private static final String LOG_MARK_AS_COMPLETE = "Completed job for %s.";

    private final WorldCoord min;
    private final WorldCoord max;
    private final int[] usedOps = new int[3];
    private final Map<ICraftingPatternDetails, TaskProgress> tasks = new HashMap<>();
    private Map<ICraftingPatternDetails, TaskProgress> workableTasks = new HashMap<>();
    private HashSet<ICraftingMedium> knownBusyMediums = new HashSet<>();
    // INSTANCE sate
    private final LinkedList<TileCraftingTile> tiles = new LinkedList<>();
    private final LinkedList<TileCraftingTile> storage = new LinkedList<>();
    private final LinkedList<TileCraftingMonitorTile> status = new LinkedList<>();
    private final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();
    private final HashMap<IAEItemStack, List<DimensionalCoord>> providers = new HashMap<>();
    private ICraftingLink myLastLink;
    private String myName = "";
    private boolean isDestroyed = false;
    /**
     * crafting job info
     */
    private MECraftingInventory inventory = new MECraftingInventory();

    private IAEItemStack finalOutput;
    private boolean waiting = false;
    private IItemList<IAEItemStack> waitingFor = AEApi.instance().storage().createItemList();
    private long availableStorage = 0;
    private MachineSource machineSrc = null;
    private int accelerator = 0;
    private boolean isComplete = true;
    private int remainingOperations;
    private boolean somethingChanged;

    private long lastTime;
    private long elapsedTime;
    private long startItemCount;
    private long remainingItemCount;

    public DummyCluster(final WorldCoord min, final WorldCoord max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public IAEItemStack getFinalOutput() {
        return finalOutput;
    }

    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    public ICraftingLink getLastCraftingLink() {
        return this.myLastLink;
    }

    /**
     * add a new Listener to the monitor, be sure to properly remove yourself when your done.
     */
    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEItemStack> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    /**
     * remove a Listener to the monitor.
     */
    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEItemStack> l) {
        this.listeners.remove(l);
    }

    public IMEInventory<IAEItemStack> getInventory() {
        return this.inventory;
    }

    @Override
    public void updateStatus(final boolean updateGrid) {
        for (final TileCraftingTile r : this.tiles) {
            r.updateMeta(true);
        }
    }

    @Override
    public void destroy() {
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;

        boolean posted = false;

        for (final TileCraftingTile r : this.tiles) {
            final IGridNode n = r.getActionableNode();
            if (n != null && !posted) {
                final IGrid g = n.getGrid();
                if (g != null) {
                    g.postEvent(new MENetworkCraftingCpuChange(n));
                    posted = true;
                }
            }

            r.updateStatus(null);
        }
    }

    @Override
    public Iterator<IGridHost> getTiles() {
        return (Iterator) this.tiles.iterator();
    }

    void addTile(final TileCraftingTile te) {
        if (this.machineSrc == null || te.isCoreBlock()) {
            this.machineSrc = new MachineSource(te);
        }

        te.setCoreBlock(false);
        te.markDirty();
        this.tiles.push(te);

        if (te.isStorage()) {
            long additionalStorage = te.getStorageBytes();
            if (Long.MAX_VALUE - additionalStorage >= this.availableStorage) {
                // Safe to add as it does not cause overflow
                this.availableStorage += additionalStorage;
                this.storage.add(te);
            } else {
                // Prevent form CPU if storage overflowed
                this.tiles.remove(te);
            }
        } else if (te.isStatus()) {
            this.status.add((TileCraftingMonitorTile) te);
        } else if (te.isAccelerator()) {
            this.accelerator += te.acceleratorValue();
        }
    }

    public boolean canAccept(final IAEStack input) {
        if (input instanceof IAEItemStack) {
            final IAEItemStack is = this.waitingFor.findPrecise((IAEItemStack) input);
            if (is != null && is.getStackSize() > 0) {
                return true;
            }
        }
        return false;
    }

    public IAEStack injectItems(final IAEStack input, final Actionable type, final BaseActionSource src) {
        if (!(input instanceof IAEItemStack)) {
            return input;
        }

        final IAEItemStack what = (IAEItemStack) input.copy();
        final IAEItemStack is = this.waitingFor.findPrecise(what);

        if (type == Actionable.SIMULATE) // causes crafting to lock up?
        {
            if (is != null && is.getStackSize() > 0) {
                if (is.getStackSize() >= what.getStackSize()) {
                    if (Objects.equals(this.finalOutput, what)) {
                        if (this.myLastLink != null) {
                            return ((CraftingLink) this.myLastLink).injectItems(what.copy(), type);
                        }

                        return what; // ignore it.
                    }

                    return null;
                }

                final IAEItemStack leftOver = what.copy();
                leftOver.decStackSize(is.getStackSize());

                final IAEItemStack used = what.copy();
                used.setStackSize(is.getStackSize());

                if (Objects.equals(finalOutput, what)) {
                    if (this.myLastLink != null) {
                        leftOver.add(((CraftingLink) this.myLastLink).injectItems(used.copy(), type));
                        return leftOver;
                    }

                    return what; // ignore it.
                }

                return leftOver;
            }
        } else if (type == Actionable.MODULATE) {
            if (is != null && is.getStackSize() > 0) {
                this.waiting = false;

                this.postChange(what, src);

                if (is.getStackSize() >= what.getStackSize()) {
                    is.decStackSize(what.getStackSize());

                    this.updateElapsedTime(what);
                    this.markDirty();
                    this.postCraftingStatusChange(is);

                    if (Objects.equals(finalOutput, what)) {
                        IAEStack leftover = what;

                        this.finalOutput.decStackSize(what.getStackSize());

                        if (this.myLastLink != null) {
                            leftover = ((CraftingLink) this.myLastLink).injectItems(what, type);
                        }

                        if (this.finalOutput.getStackSize() <= 0) {
                            this.completeJob();
                        }

                        this.updateCPU();

                        return leftover; // ignore it.
                    }

                    // 2000
                    return this.inventory.injectItems(what, type, src);
                }

                final IAEItemStack insert = what.copy();
                insert.setStackSize(is.getStackSize());
                what.decStackSize(is.getStackSize());

                is.setStackSize(0);

                if (Objects.equals(finalOutput, insert)) {
                    IAEStack leftover = input;

                    this.finalOutput.decStackSize(insert.getStackSize());

                    if (this.myLastLink != null) {
                        what.add(((CraftingLink) this.myLastLink).injectItems(insert.copy(), type));
                        leftover = what;
                    }

                    if (this.finalOutput.getStackSize() <= 0) {
                        this.completeJob();
                    }

                    this.updateCPU();
                    this.markDirty();

                    return leftover; // ignore it.
                }

                this.inventory.injectItems(insert, type, src);
                this.markDirty();

                return what;
            }
        }

        return input;
    }

    private void postChange(final IAEItemStack diff, final BaseActionSource src) {
        final Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.getListeners();

        // protect integrity
        if (i.hasNext()) {
            final ImmutableList<IAEItemStack> single = ImmutableList.of(diff.copy());

            while (i.hasNext()) {
                final Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> o = i.next();
                final IMEMonitorHandlerReceiver<IAEItemStack> receiver = o.getKey();

                if (receiver.isValid(o.getValue())) {
                    receiver.postChange(null, single, src);
                } else {
                    i.remove();
                }
            }
        }
    }

    private void markDirty() {
        this.getCore().markDirty();
    }

    private void postCraftingStatusChange(final IAEItemStack diff) {
        if (this.getGrid() == null) {
            return;
        }

        final CraftingGridCache sg = this.getGrid().getCache(ICraftingGrid.class);

        if (sg.getInterestManager().containsKey(diff)) {
            final Collection<CraftingWatcher> list = sg.getInterestManager().get(diff);

            if (!list.isEmpty()) {
                for (final CraftingWatcher iw : list) {

                    iw.getHost().onRequestChange(sg, diff);
                }
            }
        }
    }

    private void completeJob() {
        if (this.myLastLink != null) {
            ((CraftingLink) this.myLastLink).markDone();
        }

        if (AELog.isCraftingLogEnabled()) {
            final IAEItemStack logStack = this.finalOutput.copy();
            logStack.setStackSize(this.startItemCount);
            AELog.crafting(LOG_MARK_AS_COMPLETE, logStack);
        }

        this.remainingItemCount = 0;
        this.startItemCount = 0;
        this.lastTime = 0;
        this.elapsedTime = 0;
        this.isComplete = true;
    }

    private void updateCPU() {
        IAEItemStack send = this.finalOutput;

        if (this.finalOutput != null && this.finalOutput.getStackSize() <= 0) {
            send = null;
        }

        for (final TileCraftingMonitorTile t : this.status) {
            t.setJob(send);
        }
    }

    private Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> getListeners() {
        return this.listeners.entrySet().iterator();
    }

    private TileCraftingTile getCore() {
        return (TileCraftingTile) this.machineSrc.via;
    }

    private IGrid getGrid() {
        for (final TileCraftingTile r : this.tiles) {
            final IGridNode gn = r.getActionableNode();
            if (gn != null) {
                final IGrid g = gn.getGrid();
                if (g != null) {
                    return r.getActionableNode().getGrid();
                }
            }
        }

        return null;
    }

    private ArrayList<IAEItemStack> getExtractItems(IAEItemStack ingredient, ICraftingPatternDetails patternDetails) {
        ArrayList<IAEItemStack> list = new ArrayList<>();
        if (patternDetails.canSubstitute()) {
            for (IAEItemStack fuzz : this.inventory.getItemList().findFuzzy(ingredient, FuzzyMode.IGNORE_ALL)) {
                if (!patternDetails.isCraftable() && fuzz.getStackSize() <= 0) continue;
                if (patternDetails.isCraftable()) {
                    final IAEItemStack[] inputSlots = patternDetails.getInputs();
                    final IAEItemStack finalIngredient = ingredient; // have to copy because of Java lambda capture
                                                                     // rules here
                    final int matchingSlot = IntStream.range(0, inputSlots.length)
                            .filter(idx -> inputSlots[idx] != null && Objects.equals(inputSlots[idx], finalIngredient))
                            .findFirst().orElse(-1);
                    if (matchingSlot < 0) {
                        continue;
                    }
                    if (!patternDetails.isValidItemForSlot(matchingSlot, fuzz.getItemStack(), getWorld())) {
                        // Skip invalid fuzzy matches
                        continue;
                    }
                }
                fuzz = fuzz.copy();
                fuzz.setStackSize(ingredient.getStackSize());
                final IAEItemStack ais = this.inventory.extractItems(fuzz, Actionable.SIMULATE, this.machineSrc);
                final ItemStack is = ais == null ? null : ais.getItemStack();

                if (is != null && is.stackSize == ingredient.getStackSize()) {
                    list.add(ais);
                    return list;
                } else if (is != null && patternDetails.isCraftable()) {
                    ingredient = ingredient.copy();
                    ingredient.decStackSize(is.stackSize);
                    list.add(ais);
                }
            }
        } else {
            final IAEItemStack extractItems = this.inventory
                    .extractItems(ingredient, Actionable.SIMULATE, this.machineSrc);
            final ItemStack is = extractItems == null ? null : extractItems.getItemStack();
            if (is != null && is.stackSize == ingredient.getStackSize()) {
                list.add(extractItems);
                return list;
            }
        }
        return list;
    }

    private boolean canCraft(final ICraftingPatternDetails details, final IAEItemStack[] condensedInputs) {
        for (IAEItemStack g : condensedInputs) {
            if (getExtractItems(g, details).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void cancel() {
        if (this.myLastLink != null) {
            this.myLastLink.cancel();
        }

        final IItemList<IAEItemStack> list;
        this.getListOfItem(list = AEApi.instance().storage().createItemList(), CraftingItemList.ALL);
        for (final IAEItemStack is : list) {
            this.postChange(is, this.machineSrc);
        }

        this.isComplete = true;
        this.myLastLink = null;
        this.tasks.clear();
        this.providers.clear();
        final ImmutableSet<IAEItemStack> items = ImmutableSet.copyOf(this.waitingFor);

        this.waitingFor.resetStatus();

        for (final IAEItemStack is : items) {
            this.postCraftingStatusChange(is);
        }

        this.finalOutput = null;
        this.updateCPU();

        this.storeItems(); // marks dirty
    }

    public void updateCraftingLogic(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cc) {
        if (!this.getCore().isActive()) {
            return;
        }

        if (this.myLastLink != null) {
            if (this.myLastLink.isCanceled()) {
                this.myLastLink = null;
                this.cancel();
            }
        }

        if (this.isComplete) {
            if (this.inventory.getItemList().isEmpty()) {
                return;
            }

            this.storeItems();
            return;
        }

        this.waiting = false;
        if (this.waiting || this.tasks.isEmpty()) // nothing to do here...
        {
            return;
        }

        this.remainingOperations = this.accelerator + 1 - (this.usedOps[0] + this.usedOps[1] + this.usedOps[2]);
        final int started = this.remainingOperations;

        // Shallow copy tasks so we may remove them after visiting
        this.workableTasks = new HashMap<>(this.tasks);
        this.knownBusyMediums.clear();
        if (this.remainingOperations > 0) {
            do {
                this.somethingChanged = false;
                this.executeCrafting(eg, cc);
            } while (this.somethingChanged && this.remainingOperations > 0);
        }
        this.usedOps[2] = this.usedOps[1];
        this.usedOps[1] = this.usedOps[0];
        this.usedOps[0] = started - this.remainingOperations;

        this.workableTasks.clear();
        this.knownBusyMediums.clear();

        if (this.remainingOperations > 0 && !this.somethingChanged) {
            this.waiting = true;
        }
    }

    private void executeCrafting(final IEnergyGrid eg, final CraftingGridCache cc) {
        final Iterator<Entry<ICraftingPatternDetails, TaskProgress>> i = this.workableTasks.entrySet().iterator();

        while (i.hasNext()) {
            final Entry<ICraftingPatternDetails, TaskProgress> e = i.next();

            if (e.getValue().value <= 0) {
                this.tasks.remove(e.getKey());
                i.remove();
                continue;
            }

            final ICraftingPatternDetails details = e.getKey();
            if (!this.canCraft(details, details.getCondensedInputs())) {
                i.remove(); // No need to revisit this task on next executeCrafting this tick
                continue;
            }

            InventoryCrafting ic = null;
            boolean pushedPattern = false;

            for (final ICraftingMedium m : cc.getMediums(e.getKey())) {
                if (e.getValue().value <= 0 || knownBusyMediums.contains(m)) {
                    continue;
                }

                if (m.isBusy()) {
                    knownBusyMediums.add(m);
                    continue;
                }

                double sum = 0;
                if (ic == null) {
                    final IAEItemStack[] input = details.getInputs();

                    for (final IAEItemStack anInput : input) {
                        if (anInput != null) {
                            sum += anInput.getStackSize();
                        }
                    }
                    // upgraded interface uses more power
                    if (m instanceof DualityInterface)
                        sum *= Math.pow(4.0, ((DualityInterface) m).getInstalledUpgrades(Upgrades.PATTERN_CAPACITY));

                    // check if there is enough power
                    if (eg.extractAEPower(sum, Actionable.SIMULATE, PowerMultiplier.CONFIG) < sum - 0.01) continue;

                    ic = details.isCraftable() ? new InventoryCrafting(new ContainerNull(), 3, 3)
                            : new InventoryCrafting(new ContainerNull(), details.getInputs().length, 1);

                    boolean found = false;
                    for (int x = 0; x < input.length; x++) {
                        if (input[x] != null) {
                            found = false;
                            for (IAEItemStack ias : getExtractItems(input[x], details)) {
                                if (details.isCraftable()
                                        && !details.isValidItemForSlot(x, ias.getItemStack(), this.getWorld())) {
                                    continue;
                                }
                                final IAEItemStack ais = this.inventory
                                        .extractItems(ias, Actionable.MODULATE, this.machineSrc);
                                final ItemStack is = ais == null ? null : ais.getItemStack();
                                if (is == null) continue;
                                found = true;
                                ic.setInventorySlotContents(x, is);
                                if (!details.canBeSubstitute() && is.stackSize == input[x].getStackSize()) {
                                    this.postChange(input[x], this.machineSrc);
                                    break;
                                } else {
                                    this.postChange(AEItemStack.create(is), this.machineSrc);
                                }
                            }
                            if (!found) {
                                break;
                            }
                        }
                    }

                    if (!found) {
                        // put stuff back..
                        for (int x = 0; x < ic.getSizeInventory(); x++) {
                            final ItemStack is = ic.getStackInSlot(x);
                            if (is != null) {
                                this.inventory
                                        .injectItems(AEItemStack.create(is), Actionable.MODULATE, this.machineSrc);
                            }
                        }
                        ic = null;
                        break;
                    }
                }

                if (m.pushPattern(details, ic)) {
                    eg.extractAEPower(sum, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    this.somethingChanged = true;
                    this.remainingOperations--;
                    pushedPattern = true;

                    for (final IAEItemStack out : details.getCondensedOutputs()) {
                        this.postChange(out, this.machineSrc);
                        this.waitingFor.add(out.copy());
                        this.postCraftingStatusChange(out.copy());
                        providers.computeIfAbsent(out, k -> new ArrayList<>());
                        List<DimensionalCoord> list = providers.get(out);
                        if (m instanceof ICraftingProvider) {
                            TileEntity tile = this.getTile(m);
                            if (tile == null) continue;
                            DimensionalCoord tileDimensionalCoord = new DimensionalCoord(tile);
                            boolean isAdded = false;
                            for (DimensionalCoord dimensionalCoord : list) {
                                if (dimensionalCoord.isEqual(tileDimensionalCoord)) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                list.add(tileDimensionalCoord);
                            }
                        }
                    }

                    if (details.isCraftable()) {
                        FMLCommonHandler.instance().firePlayerCraftingEvent(
                                Platform.getPlayer((WorldServer) this.getWorld()),
                                details.getOutput(ic, this.getWorld()),
                                ic);

                        for (int x = 0; x < ic.getSizeInventory(); x++) {
                            final ItemStack output = Platform.getContainerItem(ic.getStackInSlot(x));
                            if (output != null) {
                                final IAEItemStack cItem = AEItemStack.create(output);
                                this.postChange(cItem, this.machineSrc);
                                this.waitingFor.add(cItem);
                                this.postCraftingStatusChange(cItem);
                            }
                        }
                    }

                    ic = null; // hand off complete!
                    this.markDirty();

                    e.getValue().value--;
                    if (e.getValue().value <= 0) {
                        continue;
                    }

                    if (this.remainingOperations == 0) {
                        return;
                    }
                }
            }

            if (!pushedPattern) {
                // No need to revisit this task on next executeCrafting this tick
                i.remove();
            }

            if (ic != null) {
                // put stuff back..
                for (int x = 0; x < ic.getSizeInventory(); x++) {
                    final ItemStack is = ic.getStackInSlot(x);
                    if (is != null) {
                        this.inventory.injectItems(AEItemStack.create(is), Actionable.MODULATE, this.machineSrc);
                    }
                }
            }
        }
    }

    private void storeItems() {
        final IGrid g = this.getGrid();

        if (g == null) {
            return;
        }

        final IStorageGrid sg = g.getCache(IStorageGrid.class);
        final IMEInventory<IAEItemStack> ii = sg.getItemInventory();

        for (IAEItemStack is : this.inventory.getItemList()) {
            is = this.inventory.extractItems(is.copy(), Actionable.MODULATE, this.machineSrc);

            if (is != null) {
                this.postChange(is, this.machineSrc);
                is = ii.injectItems(is, Actionable.MODULATE, this.machineSrc);
            }

            if (is != null) {
                this.inventory.injectItems(is, Actionable.MODULATE, this.machineSrc);
            }
        }

        if (this.inventory.getItemList().isEmpty()) {
            this.inventory = new MECraftingInventory();
        }

        this.markDirty();
    }

    public ICraftingLink submitJob(final IGrid g, final ICraftingJob job, final BaseActionSource src,
            final ICraftingRequester requestingMachine) {
        if (!this.tasks.isEmpty() || !this.waitingFor.isEmpty()) {
            return null;
        }

        if (this.isBusy() || !this.isActive() || this.availableStorage < job.getByteTotal()) {
            return null;
        }

        if (!job.supportsCPUCluster(this)) {
            return null;
        }
        this.providers.clear();
        final IStorageGrid sg = g.getCache(IStorageGrid.class);
        final IMEInventory<IAEItemStack> storage = sg.getItemInventory();
        final MECraftingInventory ci = new MECraftingInventory(storage, true, false, false);

        try {
            this.waitingFor.resetStatus();
            job.startCrafting(ci, this, src);
            if (ci.commit(src)) {
                if (job.getOutput() != null) {
                    this.finalOutput = job.getOutput();
                    this.waiting = false;
                    this.isComplete = false;
                    this.markDirty();

                    this.updateCPU();
                    final String craftID = this.generateCraftingID();

                    this.myLastLink = new CraftingLink(
                            this.generateLinkData(craftID, requestingMachine == null, false),
                            this);

                    this.prepareElapsedTime();

                    if (requestingMachine == null) {
                        return this.myLastLink;
                    }

                    final ICraftingLink whatLink = new CraftingLink(
                            this.generateLinkData(craftID, false, true),
                            requestingMachine);

                    this.submitLink(this.myLastLink);
                    this.submitLink(whatLink);

                    final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();
                    this.getListOfItem(list, CraftingItemList.ALL);
                    for (final IAEItemStack ge : list) {
                        this.postChange(ge, this.machineSrc);
                    }

                    return whatLink;
                }
            } else {
                this.tasks.clear();
                this.providers.clear();
                this.inventory.getItemList().resetStatus();
            }
        } catch (final CraftBranchFailure e) {

            if (src instanceof PlayerSource) {
                try {
                    EntityPlayer player = ((PlayerSource) src).player;
                    if (player != null) {
                        final IAEItemStack missingStack = e.getMissing();
                        String missingName = "?";
                        IChatComponent missingDisplayName = new ChatComponentText("?");
                        long missingCount = -1;
                        if (missingStack != null && missingStack.getItem() != null) {
                            missingName = missingStack.getItemStack().getUnlocalizedName();
                            if (StatCollector.canTranslate(missingName + ".name")
                                    && StatCollector.translateToLocal(missingName + ".name")
                                            .equals(missingStack.getItemStack().getDisplayName()))
                                missingDisplayName = new ChatComponentTranslation(missingName + ".name");
                            else missingDisplayName = new ChatComponentText(
                                    missingStack.getItemStack().getDisplayName());
                            missingCount = missingStack.getStackSize();
                        }
                        player.addChatMessage(
                                new ChatComponentTranslation(
                                        PlayerMessages.CraftingItemsWentMissing.getName(),
                                        missingCount,
                                        missingName).appendText(" (").appendSibling(missingDisplayName)
                                                .appendText(")"));
                    }
                } catch (Exception ex) {
                    AELog.error(ex, "Could not notify player of crafting failure");
                }
            }

            this.tasks.clear();
            this.providers.clear();
            this.inventory.getItemList().resetStatus();
            // AELog.error( e );
        }

        return null;
    }

    @Override
    public boolean isBusy() {

        this.tasks.entrySet().removeIf(
                iCraftingPatternDetailsTaskProgressEntry -> iCraftingPatternDetailsTaskProgressEntry.getValue().value
                        <= 0);

        return !this.tasks.isEmpty() || !this.waitingFor.isEmpty();
    }

    @Override
    public BaseActionSource getActionSource() {
        return this.machineSrc;
    }

    @Override
    public long getAvailableStorage() {
        return this.availableStorage;
    }

    @Override
    public int getCoProcessors() {
        return this.accelerator;
    }

    @Override
    public String getName() {
        return this.myName;
    }

    public boolean isActive() {
        final TileCraftingTile core = this.getCore();

        if (core == null) {
            return false;
        }

        final IGridNode node = core.getActionableNode();
        if (node == null) {
            return false;
        }

        return node.isActive();
    }

    private String generateCraftingID() {
        final long now = System.currentTimeMillis();
        final int hash = System.identityHashCode(this);
        final int hmm = this.finalOutput == null ? 0 : this.finalOutput.hashCode();

        return Long.toString(now, Character.MAX_RADIX) + '-'
                + Integer.toString(hash, Character.MAX_RADIX)
                + '-'
                + Integer.toString(hmm, Character.MAX_RADIX);
    }

    private NBTTagCompound generateLinkData(final String craftingID, final boolean standalone, final boolean req) {
        final NBTTagCompound tag = new NBTTagCompound();

        tag.setString("CraftID", craftingID);
        tag.setBoolean("canceled", false);
        tag.setBoolean("done", false);
        tag.setBoolean("standalone", standalone);
        tag.setBoolean("req", req);

        return tag;
    }

    private void submitLink(final ICraftingLink myLastLink2) {
        if (this.getGrid() != null) {
            final CraftingGridCache cc = this.getGrid().getCache(ICraftingGrid.class);
            cc.addLink((CraftingLink) myLastLink2);
        }
    }

    public void getListOfItem(final IItemList<IAEItemStack> list, final CraftingItemList whichList) {
        switch (whichList) {
            case ACTIVE : {
                for (final IAEItemStack ais : this.waitingFor) {
                    list.add(ais);
                }
            }break;
            case PENDING : {
                for (final Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet()) {
                    for (IAEItemStack ais : t.getKey().getCondensedOutputs()) {
                        ais = ais.copy();
                        ais.setStackSize(ais.getStackSize() * t.getValue().value);
                        list.add(ais);
                    }
                }
            }break;
            case STORAGE : this.inventory.getAvailableItems(list);
            default : {
                this.inventory.getAvailableItems(list);
                for (final IAEItemStack ais : this.waitingFor) {
                    list.add(ais);
                }
                for (final Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet()) {
                    for (IAEItemStack ais : t.getKey().getCondensedOutputs()) {
                        ais = ais.copy();
                        ais.setStackSize(ais.getStackSize() * t.getValue().value);
                        list.add(ais);
                    }
                }
            }break;
        }
    }

    public void addStorage(final IAEItemStack extractItems) {
        this.inventory.injectItems(extractItems, Actionable.MODULATE, null);
    }

    public void addEmitable(final IAEItemStack i) {
        this.waitingFor.add(i);
        this.postCraftingStatusChange(i);
    }

    public void addCrafting(final ICraftingPatternDetails details, final long crafts) {
        TaskProgress i = this.tasks.get(details);

        if (i == null) {
            this.tasks.put(details, i = new TaskProgress());
        }

        i.value += crafts;
    }

    public IAEItemStack getItemStack(final IAEItemStack what, final CraftingItemList storage2) {
        IAEItemStack is;
        switch (storage2) {
            case STORAGE : is = this.inventory.getItemList().findPrecise(what);break;
            case ACTIVE : is = this.waitingFor.findPrecise(what);break;
            case PENDING : {
                CraftingGridCache cache = null;
                if (this.getGrid() != null) {
                    cache = this.getGrid().getCache(ICraftingGrid.class);
                }
                is = what.copy();
                is.setStackSize(0);
                for (final Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet()) {
                    for (final IAEItemStack ais : t.getKey().getCondensedOutputs()) {
                        if (Objects.equals(ais, is)) {
                            is.setStackSize(is.getStackSize() + ais.getStackSize() * t.getValue().value);
                            if (cache != null) {
                                List<ICraftingMedium> craftingProviders = cache.getMediums(t.getKey());
                                List<DimensionalCoord> dimensionalCoords = new ArrayList<>();
                                for (ICraftingMedium craftingProvider : craftingProviders) {
                                    final TileEntity tile = this.getTile(craftingProvider);
                                    if (tile != null) dimensionalCoords.add(new DimensionalCoord(tile));
                                }
                                this.providers.put(is, dimensionalCoords);
                            }
                        }
                    }
                }
            }break;
            default : throw new IllegalStateException("Invalid Operation");
        }

        if (is != null) {
            return is.copy();
        }

        is = what.copy();
        is.setStackSize(0);
        return is;
    }

    public void writeToNBT(final NBTTagCompound data) {
        data.setTag("finalOutput", this.writeItem(this.finalOutput));
        data.setTag("inventory", this.writeList(this.inventory.getItemList()));
        data.setBoolean("waiting", this.waiting);
        data.setBoolean("isComplete", this.isComplete);

        if (this.myLastLink != null) {
            final NBTTagCompound link = new NBTTagCompound();
            this.myLastLink.writeToNBT(link);
            data.setTag("link", link);
        }

        NBTTagList list = new NBTTagList();
        for (final Entry<ICraftingPatternDetails, TaskProgress> e : this.tasks.entrySet()) {
            final NBTTagCompound item = this.writeItem(AEItemStack.create(e.getKey().getPattern()));
            item.setLong("craftingProgress", e.getValue().value);
            list.appendTag(item);
        }
        data.setTag("tasks", list);

        data.setTag("waitingFor", this.writeList(this.waitingFor));

        data.setLong("elapsedTime", this.getElapsedTime());
        data.setLong("startItemCount", this.getStartItemCount());
        data.setLong("remainingItemCount", this.getRemainingItemCount());

        list = new NBTTagList();
        for (final Entry<IAEItemStack, List<DimensionalCoord>> e : this.providers.entrySet()) {
            NBTTagCompound tmp = new NBTTagCompound();
            tmp.setTag("item", this.writeItem(e.getKey()));
            DimensionalCoord.writeListToNBT(tmp, e.getValue());
            list.appendTag(tmp);
        }
        data.setTag("providers", list);
    }

    private NBTTagCompound writeItem(final IAEItemStack finalOutput2) {
        final NBTTagCompound out = new NBTTagCompound();

        if (finalOutput2 != null) {
            finalOutput2.writeToNBT(out);
        }

        return out;
    }

    private NBTTagList writeList(final IItemList<IAEItemStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEItemStack ais : myList) {
            out.appendTag(this.writeItem(ais));
        }

        return out;
    }

    void done() {
        final TileCraftingTile core = this.getCore();

        core.setCoreBlock(true);

        if (core.getPreviousState() != null) {
            this.readFromNBT(core.getPreviousState());
            core.setPreviousState(null);
        }

        this.updateCPU();
        this.updateName();
    }

    public void readFromNBT(final NBTTagCompound data) {
        this.finalOutput = AEItemStack.loadItemStackFromNBT((NBTTagCompound) data.getTag("finalOutput"));
        for (final IAEItemStack ais : this.readList((NBTTagList) data.getTag("inventory"))) {
            this.inventory.injectItems(ais, Actionable.MODULATE, this.machineSrc);
        }

        this.waiting = data.getBoolean("waiting");
        this.isComplete = data.getBoolean("isComplete");

        if (data.hasKey("link")) {
            final NBTTagCompound link = data.getCompoundTag("link");
            this.myLastLink = new CraftingLink(link, this);
            this.submitLink(this.myLastLink);
        }

        NBTTagList list = data.getTagList("tasks", 10);
        for (int x = 0; x < list.tagCount(); x++) {
            final NBTTagCompound item = list.getCompoundTagAt(x);
            final IAEItemStack pattern = AEItemStack.loadItemStackFromNBT(item);
            if (pattern != null && pattern.getItem() instanceof ICraftingPatternItem ) {
            	ICraftingPatternItem cpi= (ICraftingPatternItem) pattern.getItem();
                final ICraftingPatternDetails details = cpi.getPatternForItem(pattern.getItemStack(), this.getWorld());
                if (details != null) {
                    final TaskProgress tp = new TaskProgress();
                    tp.value = item.getLong("craftingProgress");
                    this.tasks.put(details, tp);
                }
            }
        }

        this.waitingFor = this.readList((NBTTagList) data.getTag("waitingFor"));
        for (final IAEItemStack is : this.waitingFor) {
            this.postCraftingStatusChange(is.copy());
        }

        this.lastTime = System.nanoTime();
        this.elapsedTime = data.getLong("elapsedTime");
        this.startItemCount = data.getLong("startItemCount");
        this.remainingItemCount = data.getLong("remainingItemCount");

        list = data.getTagList("providers", 10);
        for (int x = 0; x < list.tagCount(); x++) {
            final NBTTagCompound pro = list.getCompoundTagAt(x);
            this.providers.put(
                    AEItemStack.loadItemStackFromNBT(pro.getCompoundTag("item")),
                    DimensionalCoord.readAsListFromNBT(pro));
        }
    }

    public void updateName() {
        this.myName = "";
        for (final TileCraftingTile te : this.tiles) {

            if (te.hasCustomName()) {
                if (this.myName.length() > 0) {
                    this.myName += ' ' + te.getCustomName();
                } else {
                    this.myName = te.getCustomName();
                }
            }
        }
    }

    private IItemList<IAEItemStack> readList(final NBTTagList tag) {
        final IItemList<IAEItemStack> out = AEApi.instance().storage().createItemList();

        if (tag == null) {
            return out;
        }

        for (int x = 0; x < tag.tagCount(); x++) {
            final IAEItemStack ais = AEItemStack.loadItemStackFromNBT(tag.getCompoundTagAt(x));
            if (ais != null) {
                out.add(ais);
            }
        }

        return out;
    }

    private World getWorld() {
        return this.getCore().getWorldObj();
    }

    public boolean isMaking(final IAEItemStack what) {
        final IAEItemStack wat = this.waitingFor.findPrecise(what);
        return wat != null && wat.getStackSize() > 0;
    }

    public void breakCluster() {
        final TileCraftingTile t = this.getCore();

        if (t != null) {
            t.breakCluster();
        }
    }

    private void prepareElapsedTime() {
        this.lastTime = System.nanoTime();
        this.elapsedTime = 0;

        final IItemList<IAEItemStack> list = AEApi.instance().storage().createItemList();

        this.getListOfItem(list, CraftingItemList.ACTIVE);
        this.getListOfItem(list, CraftingItemList.PENDING);

        long itemCount = 0;
        for (final IAEItemStack ge : list) {
            itemCount += ge.getStackSize();
        }

        this.startItemCount = itemCount;
        this.remainingItemCount = itemCount;
    }

    private void updateElapsedTime(final IAEItemStack is) {
        final long nextStartTime = System.nanoTime();
        this.elapsedTime = this.getElapsedTime() + nextStartTime - this.lastTime;
        this.lastTime = nextStartTime;
        this.remainingItemCount = this.getRemainingItemCount() - is.getStackSize();
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    @Override
    public long getRemainingItemCount() {
        return this.remainingItemCount;
    }

    @Override
    public long getStartItemCount() {
        return this.startItemCount;
    }

    @SuppressWarnings("unchecked")
    public List<DimensionalCoord> getProviders(IAEItemStack is) {
        return this.providers.getOrDefault(is, Collections.EMPTY_LIST);
    }

    private TileEntity getTile(ICraftingMedium craftingProvider) {
        if (craftingProvider instanceof DualityInterface) {
            return ((DualityInterface) craftingProvider).getHost().getTile();
        } else if (craftingProvider instanceof AEBaseTile) {
            return ((AEBaseTile) craftingProvider).getTile();
        } else if (craftingProvider instanceof IInterfaceViewable ) {
        	IInterfaceViewable interfaceViewable =(IInterfaceViewable) craftingProvider;
            return interfaceViewable.getTileEntity();
        }
        try {
            Method method = craftingProvider.getClass().getMethod("getTile");
            return (TileEntity) method.invoke(craftingProvider);
        } catch (Exception ignored) {
            return null;
        }

    }

    public int getRemainingOperations() {
        if (this.isComplete) {
            return 0;
        } else {
            return this.remainingOperations;
        }
    }

    private static class TaskProgress {

        private long value;
    }
}
