package reobf.proghatches.gt.metatileentity.util;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import net.minecraft.inventory.InventoryCrafting;

public interface IMultiplePatternPushable {
	public int pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table
			,int maxTodo);
			
			
			
}
