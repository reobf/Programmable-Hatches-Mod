package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import reobf.proghatches.gt.metatileentity.bufferutil.FluidTankG;
import reobf.proghatches.gt.metatileentity.bufferutil.ItemStackG;
import reobf.proghatches.gt.metatileentity.bufferutil.LongWrapper;

public final class LongStacks {
    private LongStacks() {}

    public static long amount(IAEStack<?> stack) {
        return stack == null ? 0 : stack.getStackSize();
    }

    public static IAEItemStack item(ItemStack type, long count) {
        if (type == null) return null;
        ItemStack copy = type.copy();
        copy.stackSize = 1;
        return AEItemStack.create(copy).setStackSize(count);
    }

    public static IAEFluidStack fluid(FluidStack type, long count) {
        if (type == null) return null;
        FluidStack copy = type.copy();
        copy.amount = 1;
        return AEFluidStack.create(copy).setStackSize(count);
    }

    public static ItemStack view(IAEItemStack stack) {
        if (stack == null) return null;
        ItemStack view = stack.getItemStack();
        view.stackSize = (int) Math.min(Integer.MAX_VALUE, stack.getStackSize());
        return view;
    }

    public static FluidStack view(IAEFluidStack stack) {
        if (stack == null) return null;
        FluidStack view = stack.getFluidStack();
        view.amount = (int) Math.min(Integer.MAX_VALUE, stack.getStackSize());
        return view;
    }

    public static boolean sameItem(ItemStack a, ItemStack b) {
        return a == null || b == null ? a == b
            : a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
                && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static boolean sameFluid(FluidStack a, FluidStack b) {
        return a == null || b == null ? a == b : a.isFluidEqual(b);
    }

    public static NBTTagCompound write(IAEStack<?> stack) {
        NBTTagCompound tag = new NBTTagCompound();
        stack.writeToNBT(tag);
        return tag;
    }

    public static ItemStackG itemGroup(IAEItemStack stack) {
        if (stack == null) return null;
        ItemStack type = stack.getItemStack();
        type.stackSize = 0;
        ItemStackG group = ItemStackG.neo(type);
        group.stackSizeInc(new LongWrapper(stack.getStackSize()));
        return group;
    }

    public static FluidTankG fluidGroup(IAEFluidStack stack) {
        FluidTankG group = new FluidTankG();
        if (stack != null) {
            FluidStack type = stack.getFluidStack();
            type.amount = 0;
            group.setFluid(type);
            group.amountAcc(stack.getStackSize());
        }
        return group;
    }
    /** Rejoin consecutive full-int chunks emitted by the old pattern input staging. */
    public static void joinLegacyItems(IAEItemStack[] recipe, ItemStackG[] stored) {
        int out = 0;
        for (int ix = 0; ix < recipe.length; ix++) {
            IAEItemStack next = recipe[ix];
            boolean merge = out > 0 && next != null && recipe[out - 1] != null
                && recipe[out - 1].getStackSize() >= Integer.MAX_VALUE
                && sameItem(recipe[out - 1].getItemStack(), next.getItemStack());
            if (merge && stored != null) {
                ItemStackG a = stored[out - 1], b = stored[ix];
                merge = (a == null || a.stackSize() == 0 || sameItem(a.getStack(), next.getItemStack()))
                    && (b == null || b.stackSize() == 0 || sameItem(b.getStack(), next.getItemStack()));
            }
            if (merge) {
                IAEItemStack previous = recipe[out - 1];
                previous.setStackSize(Math.addExact(previous.getStackSize(), next.getStackSize()));
                if (stored != null && stored[ix] != null) {
                    long count = stored[ix].stackSize();
                    if (stored[out - 1] != null) count = Math.addExact(count, stored[out - 1].stackSize());
                    stored[out - 1] = itemGroup(previous.copy().setStackSize(count));
                }
            } else {
                recipe[out] = next;
                if (stored != null) stored[out] = stored[ix];
                out++;
            }
        }
        for (int ix = out; ix < recipe.length; ix++) {
            recipe[ix] = null;
            if (stored != null) stored[ix] = null;
        }
    }

    public static void joinLegacyFluids(IAEFluidStack[] recipe, FluidTankG[] stored) {
        int out = 0;
        for (int ix = 0; ix < recipe.length; ix++) {
            IAEFluidStack next = recipe[ix];
            boolean merge = out > 0 && next != null && recipe[out - 1] != null
                && recipe[out - 1].getStackSize() >= Integer.MAX_VALUE
                && recipe[out - 1].getFluid() == next.getFluid();
            if (merge && stored != null) {
                FluidTankG a = stored[out - 1], b = stored[ix];
                merge = (a.getFluidAmount() == 0 || a.getFluid().getFluid() == next.getFluid())
                    && (b.getFluidAmount() == 0 || b.getFluid().getFluid() == next.getFluid());
            }
            if (merge) {
                IAEFluidStack previous = recipe[out - 1];
                previous.setStackSize(Math.addExact(previous.getStackSize(), next.getStackSize()));
                if (stored != null) {
                    long count = Math.addExact(stored[out - 1].getFluidAmount(), stored[ix].getFluidAmount());
                    stored[out - 1] = fluidGroup(previous.copy().setStackSize(count));
                }
            } else {
                recipe[out] = next;
                if (stored != null) stored[out] = stored[ix];
                out++;
            }
        }
        for (int ix = out; ix < recipe.length; ix++) {
            recipe[ix] = null;
            if (stored != null) stored[ix] = new FluidTankG();
        }
    }

}
