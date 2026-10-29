package reobf.proghatches.gt.metatileentity.chance;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import baubles.api.BaublesApi;
import reobf.proghatches.gt.metatileentity.RecipeFilterCRIB;
import reobf.proghatches.main.MyMod;
import tconstruct.armor.player.TPlayerStats;

public final class ChanceMark {

    public static final String KEY = "phChance";

    private ChanceMark() {}

    public static boolean isMarked(ItemStack stack) {
        return stack != null && stack.hasTagCompound() && stack.getTagCompound().getBoolean(KEY);
    }

    /** Reads the mark off the pattern item behind the details, unwrapping the hatch's multiplier wrapper. */
    public static boolean isMarked(ICraftingPatternDetails details) {
        if (details == null) return false;
        try {
            return isMarked(RecipeFilterCRIB.basePattern(details).getPattern());
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean holdingToolkit(EntityPlayer player) {
        if (player == null) return false;
        if (scan(player.inventory)) return true;
        try {
            if (scan(BaublesApi.getBaubles(player))) return true;
        } catch (Exception ignored) {}
        try {
            TPlayerStats stats = TPlayerStats.get(player);
            if (stats != null && scan(stats.armor)) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private static boolean scan(IInventory inv) {
        if (inv == null) return false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (s != null && s.getItem() == MyMod.chance_toolkit && s.getItemDamage() == 1) return true;
        }
        return false;
    }
}
