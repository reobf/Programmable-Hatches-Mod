package reobf.proghatches.gt.metatileentity.util;

import java.util.function.LongSupplier;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.ItemStackHandler;

import appeng.api.storage.data.IAEItemStack;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;

/** A stack-sized GUI window; ordinary automation continues using the hatch's inventory. */
public final class AEInputItemHandler extends ItemStackHandler implements IInterhandlerGroup {
    private final BufferedDualInputHatch hatch;
    private final LongSupplier capacity;
    private final ItemStack[] views;
    private final ItemStack[] previous;

    public AEInputItemHandler(BufferedDualInputHatch hatch, LongSupplier capacity) {
        super(hatch.getSizeInventory());
        this.hatch = hatch;
        this.capacity = capacity;
        views = new ItemStack[hatch.inputs().items.length];
        previous = new ItemStack[views.length];
    }

    public boolean isInput(int slot) {
        return slot >= 0 && slot < views.length;
    }

    private void sync(int slot) {
        if (!isInput(slot)) return;
        AEInputBuffer input = hatch.inputs();
        if (!hatch.isRemote()) {
            input.sync();
            ItemStack last = previous[slot], view = views[slot];
            IAEItemStack stored = input.items[slot];
            if (LongStacks.sameItem(last, view)) {
                long delta = (view == null ? 0 : view.stackSize) - (long) (last == null ? 0 : last.stackSize);
                if (stored != null && delta != 0) {
                    long next = Math.addExact(stored.getStackSize(), delta);
                    if (next < 0 || (delta > 0 && next > capacity.getAsLong()))
                        throw new IllegalStateException("Invalid input slot transfer");
                    input.items[slot] = next > 0 ? stored.setStackSize(next) : null;
                    hatch.onInputChanged();
                }
            } else if (last != null || view != null) {
                long hidden = LongStacks.amount(stored) - (last == null ? 0 : last.stackSize);
                if (view == null && hidden > 0) input.items[slot] = stored.setStackSize(hidden);
                else {
                    if (hidden > 0) throw new IllegalStateException("Replacing a nonempty long input");
                    if (view != null && view.stackSize > capacity.getAsLong())
                        throw new IllegalStateException("Input exceeds slot capacity");
                    input.items[slot] = view == null ? null : LongStacks.item(view, view.stackSize);
                }
                hatch.onInputChanged();
            }
            input.refreshViews();
        }
        refresh(slot);
    }

    private void refresh(int slot) {
        IAEItemStack stored = hatch.inputs().items[slot];
        ItemStack next = stored == null ? null : stored.getItemStack();
        if (next != null) next.stackSize = (int) Math.min(stored.getStackSize(), next.getMaxStackSize());
        if (!ItemStack.areItemStacksEqual(views[slot], next)) views[slot] = next;
        previous[slot] = next == null ? null : next.copy();
    }

    public long amount(int slot) {
        sync(slot);
        return LongStacks.amount(hatch.inputs().items[slot]);
    }

    public long room(int slot) {
        return Math.max(0, capacity.getAsLong() - amount(slot));
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (!isInput(slot)) return hatch.getStackInSlot(slot);
        sync(slot);
        return views[slot];
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (!isInput(slot)) {
            hatch.setInventorySlotContents(slot, stack);
            return;
        }
        views[slot] = stack;
        sync(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (!isInput(slot) || stack == null) return false;
        ItemStack current = getStackInSlot(slot);
        return current == null || LongStacks.sameItem(current, stack) || amount(slot) <= current.getMaxStackSize();
    }

    @Override
    public int getSlotLimit(int slot) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, capacity.getAsLong()));
    }

    public int workingLimit(int slot) {
        ItemStack view = getStackInSlot(slot);
        int visible = view == null ? 0 : view.stackSize;
        return (int) (visible + Math.min(Integer.MAX_VALUE - (long) visible, room(slot)));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null || stack.stackSize <= 0) return null;
        if (!isInput(slot)) return stack;
        ItemStack current = getStackInSlot(slot);
        if (current != null && !LongStacks.sameItem(current, stack)) return stack;
        int accepted = (int) Math.min(stack.stackSize, room(slot));
        if (!simulate && accepted > 0 && !hatch.isRemote()) {
            AEInputBuffer input = hatch.inputs();
            input.items[slot] = LongStacks.item(stack, Math.addExact(amount(slot), accepted));
            input.refreshViews();
            refresh(slot);
            hatch.onInputChanged();
        }
        if (accepted == stack.stackSize) return null;
        ItemStack rest = stack.copy();
        rest.stackSize -= accepted;
        return rest;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack current = getStackInSlot(slot);
        if (current == null || amount <= 0 || !isInput(slot)) return null;
        int count = (int) Math.min(amount(slot), Math.min(amount, current.getMaxStackSize()));
        ItemStack extracted = current.copy();
        extracted.stackSize = count;
        if (!simulate && !hatch.isRemote()) {
            IAEItemStack stored = hatch.inputs().items[slot];
            long left = stored.getStackSize() - count;
            hatch.inputs().items[slot] = left == 0 ? null : stored.setStackSize(left);
            hatch.inputs().refreshViews();
            refresh(slot);
            hatch.onInputChanged();
        }
        return extracted;
    }

    @Override
    public long handlerID() {
        return 1;
    }
}
