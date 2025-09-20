package reobf.proghatches.util;

import static gregtech.api.util.GTRecipeBuilder.WILDCARD;

import gregtech.api.objects.GTItemStack;
import gregtech.api.util.GTUtility;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
/*
 * SID = String as ID
 * */
public class SIDItemStack {
	
    public final String mItem;
  
    public final short mMetaData;

    public SIDItemStack(Item aItem, long aStackSize, long aMetaData) {
        mItem = Item.itemRegistry.getNameForObject(aItem);
       
        mMetaData = (short) aMetaData;
    }

    public SIDItemStack(ItemStack aStack) {
        this(aStack, false);
    }

    public SIDItemStack(ItemStack aStack, boolean wildcard) {
        this(
            aStack == null ? null : aStack.getItem(),
            aStack == null ? 0 : aStack.stackSize,
            aStack == null ? 0 : wildcard ? WILDCARD : Items.feather.getDamage(aStack));
    }




   
    @Override
    public boolean equals(Object aStack) {
        if (aStack == this) return true;
        if (aStack instanceof SIDItemStack) {
            return ((SIDItemStack) aStack).mItem.equals(mItem) && ((SIDItemStack) aStack).mMetaData == mMetaData;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mMetaData*38219+mItem.hashCode();
    }

}

