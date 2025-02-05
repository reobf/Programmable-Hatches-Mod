package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.crafting.ICraftingPatternDetails;

public interface ICraftingV2 {

    boolean pushPatternCM(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection);

    boolean acceptsPlansCM();

    boolean enableCM();
}
