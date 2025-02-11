package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.inventory.InventoryCrafting;

import appeng.api.networking.crafting.ICraftingPatternDetails;

public interface IMultiplePatternPushable {
//return array.length==1 -> array[0] pushed&consumed parallels
//return array.length==2 -> array[0] pushed array[1]consumed parallels
public int[] pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table, int maxTodo);

}
