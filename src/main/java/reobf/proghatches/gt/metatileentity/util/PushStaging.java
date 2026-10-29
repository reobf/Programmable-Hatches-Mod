package reobf.proghatches.gt.metatileentity.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.inv.MEInventoryCrafting;

/**
 * Stages a pushed pattern into a hatch's input slots. The ItemStack view of an MEInventoryCrafting is clamped to
 * Integer.MAX_VALUE while the CPU has already taken the full long amount, so the real size is read from the AE stack
 * and anything above a slot's limit is cut into several slots, full chunks first, so the same pattern always stages
 * the same way.
 */
public final class PushStaging {

    public final List<ItemStack> items = new ArrayList<>();
    public final List<FluidStack> fluids = new ArrayList<>();

    private PushStaging() {}

    public static long realSize(InventoryCrafting table, int slot, ItemStack view) {
        IAEStack<?> ae = table instanceof MEInventoryCrafting ? ((MEInventoryCrafting) table).getAEStackInSlot(slot)
            : null;
        if (view.getItem() instanceof ItemFluidPacket) {
            if (ae instanceof IAEFluidStack) return ae.getStackSize();
            // a packet carried as an item: getFluidStack clamps to int, the packet's NBT holds the long amount
            long packets = ae instanceof IAEItemStack ? ae.getStackSize() : view.stackSize;
            try {
                return Math.multiplyExact(ItemFluidPacket.getFluidAmount(view), Math.max(1, packets));
            } catch (ArithmeticException e) {
                return Long.MAX_VALUE;
            }
        }
        if (ae instanceof IAEItemStack && ((IAEItemStack) ae).isSameType(view)) return ae.getStackSize();
        return view.stackSize;
    }

    /**
     * @return null when a slot needs more than one chunk on a side that may not split, or the chunks need more item
     *         slots than validItemSlot allows or more than fluidSlots tanks; nothing has been staged then
     */
    public static PushStaging split(InventoryCrafting table, int itemLimit, boolean splitItems,
        IntPredicate validItemSlot, int fluidLimit, boolean splitFluids, int fluidSlots) {
        PushStaging ret = new PushStaging();
        for (int slot = 0; slot < table.getSizeInventory(); slot++) {
            ItemStack view = table.getStackInSlot(slot);
            if (view == null) continue;
            long rest = realSize(table, slot, view);
            if (view.getItem() instanceof ItemFluidPacket) {
                FluidStack type = ItemFluidPacket.getFluidStack(view);
                if (type == null) continue;
                if (!ret.addFluidChunks(type, rest, fluidLimit, splitFluids, fluidSlots)) return null;
            } else {
                if (!ret.addItemChunks(view, rest, itemLimit, splitItems, validItemSlot)) return null;
            }
        }
        return ret;
    }

    public static final class Part {

        public final ItemStack item;
        public final FluidStack fluid;
        public final long amount;

        private Part(ItemStack item, FluidStack fluid, long amount) {
            this.item = item;
            this.fluid = fluid;
            this.amount = amount;
        }

        public static Part item(ItemStack type, long amount) {
            return new Part(type, null, amount);
        }

        public static Part fluid(FluidStack type, long amount) {
            return new Part(null, type, amount);
        }
    }

    /**
     * Same chunking as split(..., splitItems=true, ..., splitFluids=true, ...). Parts are staged in list order.
     *
     * @return null if the chunks need more item slots than validItemSlot allows or more than fluidSlots tanks
     */
    public static PushStaging splitParts(List<Part> parts, int itemLimit, IntPredicate validItemSlot, int fluidLimit,
        int fluidSlots) {
        PushStaging ret = new PushStaging();
        for (Part p : parts) {
            if (p == null) continue;
            if (p.fluid != null) {
                if (!ret.addFluidChunks(p.fluid, p.amount, fluidLimit, true, fluidSlots)) return null;
            } else if (p.item != null) {
                if (!ret.addItemChunks(p.item, p.amount, itemLimit, true, validItemSlot)) return null;
            }
        }
        return ret;
    }

    private boolean addItemChunks(ItemStack type, long rest, int itemLimit, boolean split, IntPredicate valid) {
        while (rest > 0) {
            int chunk = (int) Math.min(rest, itemLimit);
            if (chunk <= 0) return false;
            rest -= chunk;
            if (!split && rest > 0) return false;
            if (!valid.test(items.size())) return false;
            ItemStack is = type.copy();
            is.stackSize = chunk;
            items.add(is);
        }
        return true;
    }

    private boolean addFluidChunks(FluidStack type, long rest, int fluidLimit, boolean split, int fluidSlots) {
        while (rest > 0) {
            int chunk = (int) Math.min(rest, fluidLimit);
            if (chunk <= 0) return false;
            rest -= chunk;
            if (!split && rest > 0) return false;
            if (fluids.size() >= fluidSlots) return false;
            FluidStack fs = type.copy();
            fs.amount = chunk;
            fluids.add(fs);
        }
        return true;
    }

    public void stageInto(ItemStack[] inventory, ListeningFluidTank[] tanks) {
        for (int i = 0; i < items.size(); i++) inventory[i] = items.get(i);
        for (int i = 0; i < fluids.size(); i++) tanks[i].setFluidDirect(fluids.get(i));
    }

    /** Empties the slots stageInto filled; only valid while they still hold exactly what was staged. */
    public void unstageFrom(ItemStack[] inventory, ListeningFluidTank[] tanks) {
        for (int i = 0; i < items.size(); i++) inventory[i] = null;
        for (int i = 0; i < fluids.size(); i++) tanks[i].setFluidDirect(null);
    }
}
