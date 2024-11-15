package reobf.proghatches.fmp;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;

public interface ICraftingMachinePart {
	boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
            ForgeDirection ejectionDirection);

    boolean acceptsPlans( ForgeDirection ejectionDirection);
}
