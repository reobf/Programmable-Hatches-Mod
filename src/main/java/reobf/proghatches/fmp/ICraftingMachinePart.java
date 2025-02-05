package reobf.proghatches.fmp;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.crafting.ICraftingPatternDetails;

public interface ICraftingMachinePart {

    boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection);

    boolean acceptsPlans(ForgeDirection ejectionDirection);
}
