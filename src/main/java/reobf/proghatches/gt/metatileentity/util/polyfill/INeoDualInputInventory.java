package reobf.proghatches.gt.metatileentity.util.polyfill;

import net.minecraft.item.ItemStack;
import gregtech.api.objects.GTDualInputPattern;

import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;
import reobf.proghatches.gt.metatileentity.util.IDoNotCacheThisPattern;

public interface INeoDualInputInventory extends IDualInputInventoryWithPattern, IDoNotCacheThisPattern {

    @Override
    default boolean isEmpty() {
        boolean empty = true;
        for (ItemStack is : getItemInputs()) {
            if (is.stackSize > 0) empty = false;
        }
        return getFluidInputs().length == 0 && empty;
    }

    @Override
    default GTDualInputPattern getPatternInputs() {
    	GTDualInputPattern in = new GTDualInputPattern();
        in.inputFluid = getFluidInputs();
        in.inputItems = getItemInputs();
        return in;
    }

    /*@Override
    default boolean areYouSerious() {
        return true;
    }*/
    
    @Override
    default boolean shouldBeCached() {
    	
    	//if( /*this.butYouCanCacheThisInstead()!=null&&*/areYouSerious()){return false;}
    	//return true;
    	return false;
    }

}
