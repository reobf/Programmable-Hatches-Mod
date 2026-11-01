package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

/** MUI's usual validation temporarily empties a slot; long inventories must never use that path. */
public final class AEInputSlot extends ModularSlot {
    private final AEInputItemHandler input;
    private final int index;

    public AEInputSlot(AEInputItemHandler input, int index) {
        super(input, index);
        this.input = input;
        this.index = index;
        ignoreMaxStackSize(true);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return isCanPut() && stack != null && getFilter().test(stack) && input.isItemValid(index, stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return input.workingLimit(index);
    }

    @Override
    public int getSlotStackLimit() {
        return input.workingLimit(index);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return isCanTake();
    }

    @Override
    public void putStack(ItemStack stack) {
        input.setStackInSlot(index, stack);
    }

    @Override
    public void onSlotChanged() {
        input.amount(index);
    }
}
