package reobf.proghatches.gt.metatileentity.util.polyfill;

import net.minecraft.item.ItemStack;

import gregtech.api.objects.GTDualInputs;
import gregtech.common.tileentities.machines.IDualInputInventory;
import reobf.proghatches.gt.metatileentity.util.IDoNotCacheThisPattern;

public interface INeoDualInputInventory extends IDualInputInventory, IDoNotCacheThisPattern {

    @Override
    default boolean isEmpty() {
        boolean empty = true;
        for (ItemStack is : getItemInputs()) {
            if (is.stackSize > 0) empty = false;
        }
        return getFluidInputs().length == 0 && empty;
    }

    @Override
    default GTDualInputs getPatternInputs() {
        GTDualInputs in = new GTDualInputs();
        in.inputFluid = getFluidInputs();
        in.inputItems = getItemInputs();
        return in;
    }

    @Override
    default boolean areYouSerious() {
        return true;
    }

}
