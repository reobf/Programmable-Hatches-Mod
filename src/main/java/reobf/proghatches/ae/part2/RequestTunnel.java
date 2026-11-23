package reobf.proghatches.ae.part2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import reobf.proghatches.gt.metatileentity.util.PushStaging;
import appeng.api.storage.data.IAEStack;
import java.util.LinkedHashMap;
import java.util.Map;

import com.glodblock.github.common.item.ItemFluidDrop;

import com.google.common.collect.ImmutableSet;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;

import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEInventory;

import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingLink;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.item.AEFluidStack;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import gregtech.api.gui.modularui.GTUITextures;


public abstract class RequestTunnel implements ICraftingMachine, ICraftingRequester, ISidedInventory, IFluidHandler {
	final IActionHost parent;final private BaseActionSource source ;
    public RequestTunnel(IActionHost parent) {
this.parent=parent;source = new MachineSource(this.parent);
    }

    private final Map<IAEStack<?>, Long> pendingInputs = new LinkedHashMap<>();
    public HashMap<AEItemStack, Long> waiting = new HashMap<>();
    public ArrayList<ItemStack> cacheR = new ArrayList<>();
    public ArrayList<FluidStack> cacheFR = new ArrayList<>();

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection) {

        PushStaging staged = PushStaging.logical(table, Long.MAX_VALUE, slot -> true, Long.MAX_VALUE, Integer.MAX_VALUE);
        if (staged == null) return false;
        Map<IAEStack<?>, Long> inputs = new LinkedHashMap<>(pendingInputs);
        Map<AEItemStack, Long> outputs = new HashMap<>(waiting);
        try {
            for (IAEItemStack item : staged.longItems) inputs.merge(item, item.getStackSize(), Math::addExact);
            for (IAEFluidStack fluid : staged.longFluids) inputs.merge(fluid, fluid.getStackSize(), Math::addExact);
            for (IAEStack<?> out : patternDetails.getCondensedAEOutputs()) {
                if (out == null) continue;
                IAEItemStack key = Platform.stackConvert(out);
                if (!(key instanceof AEItemStack item)) return false;
                outputs.merge(item, out.getStackSize(), Math::addExact);
            }
        } catch (ArithmeticException overflow) {
            return false;
        }
        pendingInputs.putAll(inputs);
        waiting.putAll(outputs);
        dump();
        return true;
    }

    // @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBT_AENetworkX(NBTTagCompound data) {
    	if(data.hasKey("returnAll"))
    	returnAll=data.getBoolean("returnAll");
    	if(data.hasKey("mode"))
      	mode=data.getByte("mode" );
        pendingInputs.clear();
        NBTTagList inputs = data.getTagList("pendingInputs", 10);
        for (int i = 0; i < inputs.tagCount(); i++) restoreInput(IAEStack.fromNBTGeneric(inputs.getCompoundTagAt(i)));
        NBTTagList oldItems = data.getTagList("cache", 10);
        for (int i = 0; i < oldItems.tagCount(); i++)
            restoreInput(AEItemStack.create(Platform.loadItemStackFromNBT(oldItems.getCompoundTagAt(i))));
        NBTTagList oldFluids = data.getTagList("cacheF", 10);
        for (int i = 0; i < oldFluids.tagCount(); i++)
            restoreInput(AEFluidStack.create(FluidStack.loadFluidStackFromNBT(oldFluids.getCompoundTagAt(i))));

        cacheR.clear();
        {
            NBTTagList t = (NBTTagList) data.getTag("cacheR");
            for (int i = 0; i < t.tagCount(); i++) {
                cacheR.add(Platform.loadItemStackFromNBT(t.getCompoundTagAt(i)));

            }
        }
        cacheFR.clear();
        {
            NBTTagList t = (NBTTagList) data.getTag("cacheFR");
            for (int i = 0; i < t.tagCount(); i++) {
                cacheFR.add(FluidStack.loadFluidStackFromNBT(t.getCompoundTagAt(i)));

            }
        }

        waiting.clear();

        NBTTagList t = (NBTTagList) data.getTag("waiting");
        for (int i = 0; i < t.tagCount(); i++) {
            NBTTagCompound tag = t.getCompoundTagAt(i);

            AEItemStack key = (AEItemStack) AEItemStack.loadItemStackFromNBT(tag.getCompoundTag("key"));
            long value = (tag.getLong("value"));
            waiting.put(key, value);
        }
        NBTTagCompound tag = data.getCompoundTag("link");
        if (tag.hasNoTags() == false) {
            tag.setBoolean("req", true);
            last = new CraftingLink(tag, this);

        }

    }

    // @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBT_AENetworkX(NBTTagCompound data) {
    	data.setBoolean("returnAll", returnAll);
    	data.setByte("mode", (byte) mode);
        NBTTagList inputs = new NBTTagList();
        pendingInputs.forEach((key, amount) -> inputs.appendTag(key.copy().setStackSize(amount).toNBTGeneric()));
        data.setTag("pendingInputs", inputs);
        data.removeTag("cache");
        data.removeTag("cacheF");
        {
            NBTTagList listR = new NBTTagList();
            cacheR.stream()
                .map(s -> Platform.writeItemStackToNBT(s, new NBTTagCompound()))
                .forEach(s -> listR.appendTag(s));
            data.setTag("cacheR", listR);
            NBTTagList listFR = new NBTTagList();
            cacheFR.stream()
                .map(s -> s.writeToNBT(new NBTTagCompound()))
                .forEach(s -> listFR.appendTag(s));
            data.setTag("cacheFR", listFR);
        }

        NBTTagList listR = new NBTTagList();
        waiting.entrySet()
            .stream()
            .map(s -> {
                NBTTagCompound t = new NBTTagCompound();
                NBTTagCompound k = new NBTTagCompound();
                s.getKey()
                    .writeToNBT(k);
                t.setTag("key", k);
                t.setLong("value", s.getValue());

                return t;
            })
            .forEach(s -> listR.appendTag(s));
        data.setTag("waiting", listR);
        if (last != null) {
            NBTTagCompound tag = new NBTTagCompound();
            last.writeToNBT(tag);
            data.setTag("link", tag);
        }

    }

    private void restoreInput(IAEStack<?> stack) {
        if (stack != null && stack.getStackSize() > 0)
            pendingInputs.merge(stack, stack.getStackSize(), Math::addExact);
    }

    @SuppressWarnings("unchecked")
    private void dump() {
        if (pendingInputs.isEmpty()) return;
        try {
            var grid = getProxy().getStorage();
            var it = pendingInputs.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                IAEStack<?> stack = entry.getKey().copy().setStackSize(entry.getValue());
                IMEInventory store = grid.getMEMonitor(stack.getStackType());
                if (store == null) continue;
                IAEStack<?> left = store.injectItems(stack, Actionable.MODULATE, source);
                if (left == null || left.getStackSize() == 0) it.remove();
                else entry.setValue(left.getStackSize());
            }
        } catch (GridAccessException ignored) {}
    }

    @Override
    public boolean acceptsPlans() {

        return true;
    }

    ICraftingLink last;

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return last == null ? ImmutableSet.of() : ImmutableSet.of(last);
    }
boolean returnAll=true;

    /**
     * New-signature overload actually called by CraftingLink after AE2U's IAEStack generalization; the
     * interface default returns null ("accepted everything") and would silently void products (see #319).
     * Route item stacks to the legacy logic below, reject anything else so it falls through to storage.
     */
    @Override
    public appeng.api.storage.data.IAEStack<?> injectCraftedItems(ICraftingLink link,
        appeng.api.storage.data.IAEStack<?> items, Actionable mode) {
        if (items instanceof IAEItemStack) {
            return injectCraftedItems(link, (IAEItemStack) items, mode);
        }
        // A crafted FLUID lands here now that AE2 stores and crafts fluids natively, and used to be
        // handed straight back as unwanted - which made the ItemFluidDrop branch in the legacy overload
        // below unreachable, so a fluid this tunnel had requested was never collected into cacheFR.
        // `waiting` is keyed on the legacy drop view (pushPattern fills it from the deprecated
        // getCondensedOutputs), so convert to that view, run the same accounting, and convert whatever
        // is left back to a fluid for the caller.
        IAEItemStack legacy = Platform.stackConvert(items);
        if (legacy != null) {
            IAEItemStack rest = injectCraftedItems(link, legacy, mode);
            return rest == null ? null : Platform.convertStack(rest);
        }
        return items;
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        long owed = waiting.getOrDefault(items, 0L);
        long take = Math.min(Integer.MAX_VALUE, returnAll ? items.getStackSize() : Math.min(owed, items.getStackSize()));
        if (take <= 0) return items;
        if (mode == Actionable.MODULATE) {
            IAEItemStack accepted = items.copy().setStackSize(take);
            if (items.getItem() instanceof ItemFluidDrop) cacheFR.add(ItemFluidDrop.getAeFluidStack(accepted).getFluidStack());
            else cacheR.add(accepted.getItemStack());
            if (owed <= take) waiting.remove(items);
            else waiting.put((AEItemStack) items, owed - take);
        }
        return take == items.getStackSize() ? null : items.copy().setStackSize(items.getStackSize() - take);
    }

    HashMap<StorageChannel, IMEInventory> inv = new HashMap();
    HashMap<StorageChannel, Integer> handlerHash = new HashMap();
    public ItemStack[] mark = new ItemStack[1];


    ForgeDirection prevDir;

    public abstract IMEInventory getInv(StorageChannel ch);

    /*
     * @SuppressWarnings("unchecked")
     * public IAEItemStack complete(IAEItemStack todo){
     * try{
     * todo=(IAEItemStack) getInv(StorageChannel.ITEMS).injectItems(todo, Actionable.MODULATE, source);
     * }catch(NullPointerException e){}
     * return todo;
     * }
     */

    @Override
    public void jobStateChange(ICraftingLink link) {

    }

    int cd;
    int tick;
    Future<ICraftingJob> job;

    // @TileEvent(TileEventType.TICK)
    @SuppressWarnings("unchecked")
	public void update() {
        tick++;
        if (getWorldObj().isRemote) return;

        if (dirty) {
            dirty = false;
            cacheR.removeIf(s -> s.stackSize <= 0);
        }
        if (dirtyF) {
            dirtyF = false;
            cacheFR.removeIf(s -> s.amount <= 0);
        }
        boolean clear = false;

        IMEInventory ch = getInv(StorageChannel.ITEMS);
        if (ch != null) for (ItemStack todo : cacheR) {
            AEItemStack itodo = (AEItemStack) ch.injectItems(AEItemStack.create(todo), Actionable.MODULATE, source);
            todo.stackSize = (int) (itodo == null ? 0 : itodo.getStackSize());
            if (todo.stackSize == 0) clear = true;
        }
        if (clear) cacheR.removeIf(s -> s.stackSize == 0);

        ch = getInv(StorageChannel.FLUIDS);
        clear = false;
        if (ch != null) for (FluidStack todo : cacheFR) {
            AEFluidStack itodo = (AEFluidStack) getInv(StorageChannel.FLUIDS)
                .injectItems(AEFluidStack.create(todo), Actionable.MODULATE, source);
            todo.amount = (int) (itodo == null ? 0 : itodo.getStackSize());
            if (todo.amount == 0) clear = true;
        }
        if (clear) cacheFR.removeIf(s -> s.amount == 0);

        IAEItemStack req = null;

        if (waiting.isEmpty() == false) {

            if ((mode & 1) == 0 /*&& last == null*/) useExisting();
        }
        if (this.tick % 40 == 2) {
        	dump();
            fillStacksIntoFirstSlots(cacheR);
            fillStacksIntoFirstSlotsF(cacheFR);

        }
        if (waiting.isEmpty() == false && job == null) {
            Entry<AEItemStack, Long> ent = waiting.entrySet()
                .iterator()
                .next();
            req = ent.getKey()
                .copy()
                .setStackSize(ent.getValue());
        }
        if((mode & 2) == 0)
        try {
            if (last != null) {
                if (last.isDone() || last.isCanceled()) {
                    last = null;

                }
            }
            if (last == null) {
                if (job == null) {
                    if (req != null) {
                    	if(cd>=-100)cd--;//5 charges
                        if (cd-- <= 0) {
                            job = getProxy().getCrafting()
                                .beginCraftingJob(
                                    getWorldObj(),
                                    getProxy().getGrid(),
                                    new MachineSource(this.parent),
                                    req,
                                    null);
                            cd += 20;
                        }
                    }
                } else if (job.isDone() && !job.isCancelled()) {
                    last = getProxy().getCrafting()
                        .submitJob(job.get(), this, null, false, new MachineSource(this.parent));
                    job = null;
                } else if (job.isCancelled()) {
                    last = null;
                }

            } else {

            }
        } catch (Exception e) {e.printStackTrace();}

    }

    protected void fillStacksIntoFirstSlotsF(ArrayList<FluidStack> mInventory) {
        compact(mInventory, AEFluidStack::create, IAEFluidStack::getFluidStack);
    }

    protected void fillStacksIntoFirstSlots(ArrayList<ItemStack> mInventory) {
        compact(mInventory, AEItemStack::create, IAEItemStack::getItemStack);
    }

    private static <S, A extends IAEStack<A>> void compact(List<S> slots, Function<S, A> encode, Function<A, S> decode) {
        Map<A, Long> amounts = new LinkedHashMap<>();
        for (S slot : slots) {
            A stack = encode.apply(slot);
            if (stack != null && stack.getStackSize() > 0) amounts.merge(stack, stack.getStackSize(), Long::sum);
        }
        slots.clear();
        amounts.forEach((stack, total) -> {
            for (long left = total; left > 0;) {
                long chunk = Math.min(left, Integer.MAX_VALUE);
                slots.add(decode.apply(stack.copy().setStackSize(chunk)));
                left -= chunk;
            }
        });
    }

    int mode;

    private void useExisting() {
        for (Iterator<Entry<AEItemStack, Long>> itr = waiting.entrySet()
            .iterator(); itr.hasNext();) {

            Entry<AEItemStack, Long> e = itr.next();
            if (e.getValue() <= 0) {
                itr.remove();
                continue;
            }

            IAEItemStack val = e.getKey()
                .copy()
                .setStackSize(Math.min(Integer.MAX_VALUE, e.getValue()));

            if (val.getItem() instanceof ItemFluidDrop) try {
                IAEFluidStack ext = getProxy().getStorage()
                    .getFluidInventory()
                    .extractItems(ItemFluidDrop.getAeFluidStack(val), Actionable.MODULATE, source);
                if (ext != null) {
                    e.setValue(e.getValue() - ext.getStackSize());
                    cacheFR.add(
                        ext.copy()
                            .getFluidStack());
                    if (e.getValue() <= 0) {
                        itr.remove();
                    }
                }

            } catch (GridAccessException e1) {}

            else try {
                IAEItemStack ext = getProxy().getStorage()
                    .getItemInventory()
                    .extractItems(val, Actionable.MODULATE, source);
                if (ext != null) {
                    e.setValue(e.getValue() - ext.getStackSize());
                    cacheR.add(
                        ext.copy()
                            .getItemStack());
                    if (e.getValue() <= 0) {
                        itr.remove();
                    }
                }

            } catch (GridAccessException e1) {}

        }

    }

    @Override
    public int getSizeInventory() {

        return cacheR.size() + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        if (slotIn == cacheR.size()) return null;
        dirty = true;
        return cacheR.get(slotIn);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == cacheR.size()) return null;
        dirty = true;
        if (cacheR.get(index) == null) {
            return null;
        }
        count = Math.min(count, cacheR.get(index).stackSize);
        cacheR.get(index).stackSize -= count;
        ItemStack ret = cacheR.get(index)
            .copy();
        ret.stackSize = count;
        return ret;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == cacheR.size()) return;
        dirty = true;
        if (stack == null || stack.stackSize == 0) {
            cacheR.get(index).stackSize = 0;
        } else {
            cacheR.set(index, stack);
        }

    }

    @Override
    public String getInventoryName() {

        return "";
    }

    @Override
    public boolean hasCustomInventoryName() {

        return false;
    }

    @Override
    public int getInventoryStackLimit() {

        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {

        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {

        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        // ForgeDirection pointAt =getUp().getOpposite();
        // if(pointAt.ordinal()==p_94128_1_){
        return IntStream.range(0, cacheR.size() + 1)
            .toArray();
        // }
        // return new int[0];
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {

        return false;
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {

        return true;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {

        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        int remaining = resource.amount;
        if (remaining <= 0) return null;
        for (FluidStack stack : cacheFR) {
            if (!stack.isFluidEqual(resource) || stack.amount <= 0) continue;
            int taken = Math.min(remaining, stack.amount);
            if (doDrain) {
                stack.amount -= taken;
                dirtyF = true;
            }
            remaining -= taken;
            if (remaining == 0) break;
        }
        if (remaining == resource.amount) return null;
        FluidStack cp = resource.copy();
        cp.amount -= remaining;
        return cp;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        for (FluidStack fluid : cacheFR) {
            if (fluid.amount <= 0) continue;
            FluidStack request = fluid.copy();
            request.amount = maxDrain;
            return drain(from, request, doDrain);
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {

        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {

        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {

        return cacheFR.stream()
            .map(s -> new FluidTankInfo(s, 1))
            .toArray(FluidTankInfo[]::new);
    }

    boolean dirty, dirtyF;

    public abstract AENetworkProxy getProxy();

    public abstract World getWorldObj();

	public ModularWindow createWindow(UIBuildContext buildContext) {
		 ModularWindow.Builder builder = ModularWindow.builder(176, 107 + 20);

	        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
	        builder.bindPlayerInventory(buildContext.getPlayer());

	        builder.widget(new CycleButtonWidget().setGetter(() -> mode)
	                .setSetter(s -> mode = s)
	                .setLength(3)
	                .setTextureGetter(s -> {
	                    if (s == 0) return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ITEM;
	                    if (s == 1) return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_FLUID;
	                    return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ALL;
	                })
	              .addTooltip(0, StatCollector.translateToLocal("proghatches.requesttunnel.both"))
	                .addTooltip(1, StatCollector.translateToLocal("proghatches.requesttunnel.craft"))
	                .addTooltip(2, StatCollector.translateToLocal("proghatches.requesttunnel.stock"))
	               // .addTooltip(0, StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.0"))
	                //.addTooltip(1, StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.1"))
	                .setBackground(() -> {
	                    {
	                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD, };
	                    }
	                })

	                .setSize(18, 18)
	                .setPos(120 + 20, 3));

	      /*  builder.widget(new CycleButtonWidget().setGetter(() -> returnAll?1:0)
	                .setSetter(s -> returnAll = s==1)
	                .setLength(2)
	                .setTextureGetter(s -> {
	                    if (s == 0) return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ITEM;
	                    if (s == 1) return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_FLUID;
	                    return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ALL;
	                }) .addTooltip(0, StatCollector.translateToLocal("returnAll0"))
	                .addTooltip(1, StatCollector.translateToLocal("returnAll1"))
	               // .addTooltip(0, StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.0"))
	               // .addTooltip(1, StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.1"))
	                .setBackground(() -> {
	                    {
	                        return new IDrawable[] { GTUITextures.BUTTON_STANDARD, };
	                    }
	                })

	                .setSize(18, 18)
	                .setPos(  20, 3)); */



		return builder.build();
	}

}
