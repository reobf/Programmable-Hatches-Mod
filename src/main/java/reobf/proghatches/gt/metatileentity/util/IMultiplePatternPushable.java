package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.inventory.InventoryCrafting;

import appeng.api.networking.crafting.ICraftingPatternDetails;

public interface IMultiplePatternPushable {

    public int pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table, int maxTodo);

}
