package reobf.proghatches.gt.metatileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.gtnewhorizons.modularui.api.forge.ItemHandlerHelper;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;

/**
 * original ItemStackHandler directly sets the mInventory's content
 * call IInventory.setInventorySlotContents instead, to notify the classifying process
 */
public class InventoryItemHandler extends ItemStackHandler {

    public InventoryItemHandler(ItemStack[] mInventory, IInventory dualInputHatch) {
        super(mInventory);
        inv = dualInputHatch;
    }

    IInventory inv;

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null) {
            return null;
        } else {
            this.validateSlotIndex(slot);
            ItemStack existing = (ItemStack) this.stacks.get(slot);
            int limit = this.getStackLimit(slot, stack);
            if (existing != null) {
                if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                    return stack;
                }

                limit -= existing.stackSize;
            }

            if (limit <= 0) {
                return stack;
            } else {
                boolean reachedLimit = stack.stackSize > limit;
                if (!simulate) {
                    if (existing == null) {
                        this.setStackInSlot(
                            slot,
                            reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
                        // notify
                    } else {
                        existing.stackSize += reachedLimit ? limit : stack.stackSize;
                        this.setStackInSlot(slot, existing);// notify
                    }

                    this.onContentsChanged(slot);
                }

                return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - limit) : null;
            }
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {

        inv.setInventorySlotContents(slot, stack);
    }

}
