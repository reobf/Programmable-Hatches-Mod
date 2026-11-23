package reobf.proghatches.item.patterngen;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

/**
 * Survival blank-pattern cost for generated patterns.
 * <p>
 * The player inventory is always used first so a nearby ME network is not drained while the
 * player is still carrying blanks. Only direct fill can fall back to a network, because it is
 * the only path that has a pattern storage with a known AE grid.
 */
public final class PatternGeneratorBlankPatterns {

    private PatternGeneratorBlankPatterns() {}

    public static boolean costs(EntityPlayer player) {
        return player != null && player.capabilities != null && !player.capabilities.isCreativeMode;
    }

    public static int countInventory(EntityPlayer player) {
        if (player == null || player.inventory == null) return 0;
        return count(player.inventory.mainInventory, blankPattern());
    }

    /** Consumes one blank from the player inventory, or does nothing in creative mode. */
    public static boolean tryConsumeInventory(EntityPlayer player) {
        if (!costs(player)) return true;
        if (player.inventory == null) return false;
        boolean consumed = consume(player.inventory.mainInventory, blankPattern());
        if (consumed) player.inventory.markDirty();
        return consumed;
    }

    /**
     * Consumes one blank for direct fill: player inventory first, then the AE grid of the
     * pattern storage being filled.
     */
    public static boolean tryConsume(EntityPlayer player, PatternGeneratorTarget target) {
        if (!costs(player)) return true;
        if (tryConsumeInventory(player)) return true;
        return tryConsumeFromGrid(player, target);
    }

    private static boolean tryConsumeFromGrid(EntityPlayer player, PatternGeneratorTarget target) {
        if (target == null) return false;
        IStorageGrid storage = target.storageGrid();
        if (storage == null) return false;
        IMEMonitor<IAEItemStack> monitor = storage.getItemInventory();
        if (monitor == null) return false;
        ItemStack blank = blankPattern();
        if (blank == null) return false;
        try {
            IAEItemStack request = AEItemStack.create(blank);
            IAEItemStack extracted = monitor
                .extractItems(request, Actionable.MODULATE, new PlayerSource(player, target.actionHost()));
            return extracted != null && extracted.getStackSize() > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static ItemStack blankPattern() {
        try {
            return AEApi.instance()
                .definitions()
                .materials()
                .blankPattern()
                .maybeStack(1)
                .orNull();
        } catch (Throwable ignored) {
            return null;
        }
    }

    static int count(ItemStack[] slots, ItemStack blank) {
        if (slots == null || blank == null) return 0;
        int count = 0;
        for (ItemStack stack : slots) {
            if (isBlank(stack, blank)) count += stack.stackSize;
        }
        return count;
    }

    static boolean consume(ItemStack[] slots, ItemStack blank) {
        if (slots == null || blank == null) return false;
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = slots[i];
            if (!isBlank(stack, blank)) continue;
            stack.stackSize--;
            if (stack.stackSize <= 0) slots[i] = null;
            return true;
        }
        return false;
    }

    private static boolean isBlank(ItemStack stack, ItemStack blank) {
        return stack != null && stack.stackSize > 0
            && stack.getItem() != null
            && stack.getItem() == blank.getItem()
            && stack.getItemDamage() == blank.getItemDamage();
    }
}
