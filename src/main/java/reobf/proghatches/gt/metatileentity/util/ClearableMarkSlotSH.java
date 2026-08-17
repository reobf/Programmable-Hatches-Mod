package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

/**
 * Phantom slot sync handler for "marking" slots whose contents are stored with stackSize 0.
 * <p>
 * ModularUI2's default left-click path on an occupied phantom slot is
 * {@code incrementStackCount(-1)}, which computes {@code max(0, oldAmount - 1)}. With a
 * zero-sized mark that is {@code max(0, -1) == 0 == oldAmount}, so nothing changes and the mark
 * can never be removed by clicking - only shift+left-click (which calls putStack(null)) worked.
 * <p>
 * Here a plain left click on an occupied slot clears it, which is what a marking slot should do.
 * Holding a DIFFERENT item still replaces the mark, matching the vanilla phantom behaviour.
 */
public class ClearableMarkSlotSH extends PhantomItemSlotSH {

    public ClearableMarkSlotSH(ModularSlot slot) {
        super(slot);
    }

    @Override
    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        ItemStack slotStack = getSlot().getStack();
        if (slotStack != null && mouseData.mouseButton == 0
            && (cursorStack == null || ItemHandlerHelper.canItemStacksStack(cursorStack, slotStack))) {
            getSlot().putStack(null);
            return;
        }
        super.phantomClick(mouseData, cursorStack);
    }
}
