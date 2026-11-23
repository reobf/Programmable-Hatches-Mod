package reobf.proghatches.item.patterngen;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/** Capacity-safe insertion shared by direct fill and its tests. */
public final class PatternGeneratorDelivery {

    private PatternGeneratorDelivery() {}

    /**
     * Re-reads the inventory size on every call. PH's super pattern hatch grows its pattern
     * inventory when the current page fills up, so a cached capacity would stop one page early.
     */
    public static int findSlot(IInventory inventory, ItemStack pattern) {
        return findSlot(inventory, pattern, Integer.MAX_VALUE);
    }

    public static int findSlot(IInventory inventory, ItemStack pattern, int maxSlotsExclusive) {
        if (inventory == null || pattern == null || maxSlotsExclusive <= 0) return -1;
        int size;
        try {
            size = Math.min(inventory.getSizeInventory(), maxSlotsExclusive);
        } catch (Throwable ignored) {
            return -1;
        }
        for (int slot = 0; slot < size; slot++) {
            try {
                if (inventory.getStackInSlot(slot) != null) continue;
                if (!inventory.isItemValidForSlot(slot, pattern)) continue;
                return slot;
            } catch (Throwable ignored) {
                // A single broken addon slot must not abort the whole scan.
            }
        }
        return -1;
    }

    public static boolean insertAt(IInventory inventory, int slot, ItemStack pattern) {
        if (inventory == null || pattern == null || slot < 0) return false;
        try {
            if (slot >= inventory.getSizeInventory() || inventory.getStackInSlot(slot) != null) return false;
            ItemStack copy = pattern.copy();
            copy.stackSize = 1;
            inventory.setInventorySlotContents(slot, copy);
            inventory.markDirty();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean insert(IInventory inventory, ItemStack pattern) {
        return insertAt(inventory, findSlot(inventory, pattern), pattern);
    }

    /**
     * Compares only the recipe payload, not the author/timestamp metadata, so a queue retried
     * after an interrupted save cannot insert the same pattern twice.
     */
    public static boolean containsEquivalentPattern(IInventory inventory, ItemStack pattern) {
        if (inventory == null || pattern == null) return false;
        NBTTagCompound wanted = pattern.getTagCompound();
        if (wanted == null) return false;
        String wantedIn = PatternIngredient.canonicalNbt(wanted.getTag("in"));
        String wantedOut = PatternIngredient.canonicalNbt(wanted.getTag("out"));
        int size;
        try {
            size = inventory.getSizeInventory();
        } catch (Throwable ignored) {
            return false;
        }
        for (int slot = 0; slot < size; slot++) {
            ItemStack existing;
            try {
                existing = inventory.getStackInSlot(slot);
            } catch (Throwable ignored) {
                continue;
            }
            if (existing == null || existing.getItem() != pattern.getItem()) continue;
            NBTTagCompound tag = existing.getTagCompound();
            if (tag == null) continue;
            if (wantedIn.equals(PatternIngredient.canonicalNbt(tag.getTag("in")))
                && wantedOut.equals(PatternIngredient.canonicalNbt(tag.getTag("out")))) {
                return true;
            }
        }
        return false;
    }

    public static void markTargetDirty(TileEntity tile, IInventory inventory) {
        try {
            if (inventory != null) inventory.markDirty();
        } catch (Throwable ignored) {}
        try {
            if (tile != null) tile.markDirty();
        } catch (Throwable ignored) {}
    }
}
