package reobf.proghatches.gt.metatileentity.util;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;

/** Long quantities with bounded, mutable views for inventory and tank interfaces. */
public final class AEInputBuffer {
    public final IAEItemStack[] items;
    public final IAEFluidStack[] fluids;
    private final ItemStack[] itemViews;
    private final ListeningFluidTank[] fluidViews;
    private final ItemStack[] lastItems;
    private final FluidStack[] lastFluids;

    public AEInputBuffer(ItemStack[] itemViews, ListeningFluidTank[] fluidViews, int itemSlots) {
        this.itemViews = itemViews;
        this.fluidViews = fluidViews;
        items = new IAEItemStack[itemSlots];
        fluids = new IAEFluidStack[fluidViews.length];
        lastItems = new ItemStack[itemSlots];
        lastFluids = new FluidStack[fluidViews.length];
        sync();
    }

    public void sync() {
        for (int i = 0; i < items.length; i++) {
            ItemStack view = itemViews[i], last = lastItems[i];
            if (LongStacks.sameItem(view, last)) {
                long delta = (view == null ? 0 : view.stackSize) - (long) (last == null ? 0 : last.stackSize);
                if (items[i] != null && delta != 0)
                    items[i].setStackSize(Math.max(0, Math.addExact(items[i].getStackSize(), delta)));
            } else if (view == null && items[i] != null) {
                long left = items[i].getStackSize() - (last == null ? 0 : last.stackSize);
                items[i] = left > 0 ? items[i].setStackSize(left) : null;
            } else {
                if (LongStacks.amount(items[i]) > (last == null ? 0 : last.stackSize))
                    throw new IllegalStateException("Replacing an occupied long input slot");
                items[i] = view == null ? null : LongStacks.item(view, view.stackSize);
            }
        }
        for (int i = 0; i < fluids.length; i++) {
            FluidStack view = fluidViews[i].getFluid(), last = lastFluids[i];
            if (LongStacks.sameFluid(view, last)) {
                long delta = (view == null ? 0 : view.amount) - (long) (last == null ? 0 : last.amount);
                if (fluids[i] != null && delta != 0)
                    fluids[i].setStackSize(Math.max(0, Math.addExact(fluids[i].getStackSize(), delta)));
            } else if (view == null && fluids[i] != null) {
                long left = fluids[i].getStackSize() - (last == null ? 0 : last.amount);
                fluids[i] = left > 0 ? fluids[i].setStackSize(left) : null;
            } else {
                if (LongStacks.amount(fluids[i]) > (last == null ? 0 : last.amount))
                    throw new IllegalStateException("Replacing an occupied long fluid slot");
                fluids[i] = view == null ? null : LongStacks.fluid(view, view.amount);
            }
        }
        refreshViews();
    }

    public void refreshViews() {
        for (int i = 0; i < items.length; i++) {
            ItemStack next = LongStacks.view(items[i]);
            if (!ItemStack.areItemStacksEqual(itemViews[i], next)) itemViews[i] = next;
            lastItems[i] = next == null ? null : next.copy();
        }
        for (int i = 0; i < fluids.length; i++) {
            FluidStack next = LongStacks.view(fluids[i]);
            FluidStack old = fluidViews[i].getFluid();
            if (!LongStacks.sameFluid(old, next) || (old != null && old.amount != next.amount))
                fluidViews[i].setFluidDirect(next);
            lastFluids[i] = next == null ? null : next.copy();
        }
    }

    public boolean isEmpty() {
        sync();
        for (IAEItemStack item : items) if (LongStacks.amount(item) > 0) return false;
        for (IAEFluidStack fluid : fluids) if (LongStacks.amount(fluid) > 0) return false;
        return true;
    }

    public void clear() {
        Arrays.fill(items, null);
        Arrays.fill(fluids, null);
        refreshViews();
    }

    public NBTTagCompound save() {
        sync();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("version", 1);
        for (int i = 0; i < items.length; i++)
            if (items[i] != null) tag.setTag("item" + i, LongStacks.write(items[i]));
        for (int i = 0; i < fluids.length; i++)
            if (fluids[i] != null) tag.setTag("fluid" + i, LongStacks.write(fluids[i]));
        return tag;
    }

    public void load(NBTTagCompound tag) {
        Arrays.fill(items, null);
        Arrays.fill(fluids, null);
        for (int i = 0; i < items.length; i++)
            if (tag.hasKey("item" + i)) items[i] = AEItemStack.loadItemStackFromNBT(tag.getCompoundTag("item" + i));
        for (int i = 0; i < fluids.length; i++)
            if (tag.hasKey("fluid" + i)) fluids[i] = AEFluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid" + i));
        refreshViews();
    }
}
