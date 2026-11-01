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
 * Integer.MAX_VALUE while the CPU has already taken the full long amount, so the real size is read from the AE stack.
 * Pattern hatches take one logical AE slot per input; other hatches cut each input into chunks of their slot limit.
 */
public final class PushStaging {

    public final List<ItemStack> items = new ArrayList<>();
    public final List<FluidStack> fluids = new ArrayList<>();
    public final List<IAEItemStack> longItems = new ArrayList<>();
    public final List<IAEFluidStack> longFluids = new ArrayList<>();


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
                return -1;
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
            if (rest < 0) return null;
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


    public static PushStaging logical(InventoryCrafting table, long itemLimit, IntPredicate validItemSlot,
        long fluidLimit, int fluidSlots) {
        List<Part> parts = new ArrayList<>();
        for (int slot = 0; slot < table.getSizeInventory(); slot++) {
            ItemStack view = table.getStackInSlot(slot);
            if (view == null) continue;
            long amount = realSize(table, slot, view);
            if (view.getItem() instanceof ItemFluidPacket) {
                FluidStack fluid = ItemFluidPacket.getFluidStack(view);
                if (fluid == null || amount <= 0) return null;
                parts.add(Part.fluid(fluid, amount));
            } else {
                if (amount <= 0) return null;
                parts.add(Part.item(view, amount));
            }
        }
        return logicalParts(parts, itemLimit, validItemSlot, fluidLimit, fluidSlots);
    }

    public static PushStaging logicalParts(List<Part> parts, long itemLimit, IntPredicate validItemSlot,
        long fluidLimit, int fluidSlots) {
        PushStaging result = new PushStaging();
        for (Part p : parts) {
            if (p == null) continue;
            if (p.amount <= 0) return null;
            if (p.fluid != null) {
                if (p.amount > fluidLimit || result.longFluids.size() >= fluidSlots) return null;
                result.longFluids.add(LongStacks.fluid(p.fluid, p.amount));
            } else if (p.item != null) {
                if (p.amount > itemLimit || !validItemSlot.test(result.longItems.size())) return null;
                result.longItems.add(LongStacks.item(p.item, p.amount));
            } else return null;
        }
        return result;
    }

    public void stageInto(AEInputBuffer buffer) {
        if (!buffer.isEmpty()) throw new IllegalStateException("Input buffer is occupied");
        buffer.clear();
        for (int i = 0; i < longItems.size(); i++) buffer.items[i] = longItems.get(i).copy();
        for (int i = 0; i < longFluids.size(); i++) buffer.fluids[i] = longFluids.get(i).copy();
        buffer.refreshViews();
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
            longItems.add(LongStacks.item(is, chunk));
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
            longFluids.add(LongStacks.fluid(fs, chunk));
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
