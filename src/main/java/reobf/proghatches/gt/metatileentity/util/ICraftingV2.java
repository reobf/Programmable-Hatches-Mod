package reobf.proghatches.gt.metatileentity.util;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;

public interface ICraftingV2 {

	boolean pushPatternCM(ICraftingPatternDetails patternDetails, InventoryCrafting table,
			ForgeDirection ejectionDirection);

	boolean acceptsPlansCM();
	boolean enableCM();
}
