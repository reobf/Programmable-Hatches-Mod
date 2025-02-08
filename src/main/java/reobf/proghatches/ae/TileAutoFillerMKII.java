package reobf.proghatches.ae;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.MutablePair;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.tile.TileFluidAutoFiller;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.item.AEItemStack;

public class TileAutoFillerMKII extends TileFluidAutoFiller/* implements IInstantCompletable */ {

    public TileAutoFillerMKII() {
        super();
    }

    public void updatePattern() {
        updateMark = true;
        delay = 0;

        super.updatePattern();
    }

    private final Item encodedPattern = AEApi.instance()
        .definitions()
        .items()
        .encodedPattern()
        .maybeItem()
        .orNull();

    private ItemStack getPattern(ItemStack emptyContainer, ItemStack filledContainer) {
        NBTTagList in = new NBTTagList();
        NBTTagList out = new NBTTagList();
        in.appendTag(emptyContainer.writeToNBT(new NBTTagCompound()));
        ItemStack fluidDrop = ItemFluidDrop.newStack(Util.FluidUtil.getFluidFromContainer(filledContainer));
        in.appendTag(createItemTag(fluidDrop));
        out.appendTag(filledContainer.writeToNBT(new NBTTagCompound()));
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setTag("in", in);
        itemTag.setTag("out", out);
        itemTag.setBoolean("crafting", false);
        ItemStack pattern = new ItemStack(this.encodedPattern);
        pattern.setTagCompound(itemTag);
        return pattern;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        super.provideCrafting(craftingTracker);
        try {
            if (previousCraftable != null) Arrays.stream(previousCraftable)
                .filter(s -> s.getItem() instanceof ItemFluidDrop)
                .forEach(s -> {

                    FluidStack fluidStack = ItemFluidDrop.getFluidStack(s.getItemStack());
                    Fluid fluid = fluidStack.getFluid();
                    if (fluid == null) return;
                    int maxCapacity = Util.FluidUtil.getCapacity(this.getContainerItem(), fluid);
                    if (maxCapacity == 0) return;
                    MutablePair<Integer, ItemStack> filled = Util.FluidUtil.fillStack(
                        this.getContainerItem()
                            .copy(),
                        new FluidStack(fluid, maxCapacity));
                    if (filled.right == null) return;
                    ItemStack pattern = getPattern(this.getContainerItem(), filled.right);
                    ICraftingPatternItem patter = (ICraftingPatternItem) pattern.getItem();
                    craftingTracker.addCraftingOption(this, patter.getPatternForItem(pattern, getWorldObj()));

                });

            ;
            // previousCraftableUseless=true;

        } catch (Exception e) {

        }
        /*
         * IStorageGrid storage = getStorageGrid();
         * if (storage == null) return;
         * IItemList<IAEFluidStack> fluidStorage = this.fluids.isEmpty() ? storage.getFluidInventory().getStorageList()
         * : this.fluids;
         * for (IAEFluidStack fluidStack : fluidStorage) {
         * Fluid fluid = fluidStack.getFluid();
         * if (fluid == null) continue;
         * int maxCapacity = Util.FluidUtil.getCapacity(this.getContainerItem(), fluid);
         * if (maxCapacity == 0) continue;
         * MutablePair<Integer, ItemStack> filled = Util.FluidUtil
         * .fillStack(this.getContainerItem().copy(), new FluidStack(fluid, maxCapacity));
         * if (filled.right == null) continue;
         * ItemStack pattern = getPattern(this.getContainerItem(), filled.right);
         * ICraftingPatternItem patter = (ICraftingPatternItem) pattern.getItem();
         * craftingTracker.addCraftingOption(this, patter.getPatternForItem(pattern, getWorldObj()));
         * }
         */
    }

    IAEItemStack[] previousCraftable;
    // boolean previousCraftableUseless;
    int delay;

    @TileEvent(value = TileEventType.TICK)
    public void update() {

        if (updateMark) {
            if (delay++ < 5) {
                return;
            }
            delay = 0;

            try {// previousCraftableUseless=false;
                previousCraftable = this.getProxy()
                    .getCrafting()
                    .getCraftingPatterns()
                    .keySet()
                    .stream()
                    .filter(s -> s.getItem() instanceof ItemFluidDrop)
                    .toArray(IAEItemStack[]::new);

                this.getProxy()
                    .getGrid()
                    .postEvent(new MENetworkCraftingPatternChange(this, getGridNode(ForgeDirection.UNKNOWN)));

            } catch (GridAccessException ignored) {}

        }
        updateMark = false;

    }

    boolean updateMark = true;

    @MENetworkEventSubscribe
    public void onPatternUpdate(MENetworkCraftingPatternChange e) {
        if (e.provider instanceof TileAutoFillerMKII) return;
        updateMark = true;
        delay = 0;
    }

    List<IAEItemStack> returnStackArr = new LinkedList<>();

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (this.getStorageGrid() == null) {
            return TickRateModulation.SLOWER;
        }

        return complete() ? TickRateModulation.SLEEP : TickRateModulation.SLOWER;

    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        this.returnStackArr.add(
            AEApi.instance()
                .storage()
                .createItemStack(patternDetails.getCondensedOutputs()[0].getItemStack()));
        try {
            this.getProxy()
                .getTick()
                .alertDevice(
                    this.getProxy()
                        .getNode());
        } catch (GridAccessException ignored) {

        }
        return true;
    }

    private IStorageGrid getStorageGrid() {
        try {
            return this.getProxy()
                .getGrid()
                .getCache(IStorageGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }

    private final BaseActionSource source = new MachineSource(this);

    @Override
    public boolean isBusy() {
        return false;
    }

    // @Override
    public boolean complete() {
        boolean allClear = true;
        IAEItemStack item;
        for (Iterator<IAEItemStack> it = returnStackArr.iterator(); it.hasNext();) {
            item = it.next();
            IAEItemStack left = getStorageGrid().getItemInventory()
                .injectItems(item, Actionable.MODULATE, this.source);
            if (left != null && left.getStackSize() > 0) {
                item.setStackSize(left.getStackSize());
                allClear = false;
            } else {
                it.remove();
            }
        }
        return allClear;

    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEventX(NBTTagCompound data) {
        try {
            while (true) {
                int[] a = new int[1];
                returnStackArr.clear();
                NBTTagCompound tag = (NBTTagCompound) data.getTag("cache@" + (a[0]++));
                if (tag == null) break;

                IAEItemStack item = AEItemStack.loadItemStackFromNBT(tag);
                if (item == null) {
                    break;
                }
                returnStackArr.add(item);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEventX(NBTTagCompound data) {
        int[] a = new int[1];
        returnStackArr.forEach(s -> {
            NBTTagCompound tag = new NBTTagCompound();
            s.writeToNBT(tag);
            data.setTag("cache@" + (a[0]++), data);

        });

        return data;
    }
    public void mark(EntityPlayer placer) {
        getProxy().setOwner((EntityPlayer) placer);
     
    }


}
