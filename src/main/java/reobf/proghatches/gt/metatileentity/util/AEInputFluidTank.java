package reobf.proghatches.gt.metatileentity.util;

import java.util.function.LongSupplier;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import appeng.api.storage.data.IAEFluidStack;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;

/** A bounded container-transfer window over a long fluid input. */
public final class AEInputFluidTank implements IFluidTank {
    private final BufferedDualInputHatch hatch;
    private final int slot;
    private final LongSupplier capacity;

    public AEInputFluidTank(BufferedDualInputHatch hatch, int slot, LongSupplier capacity) {
        this.hatch = hatch;
        this.slot = slot;
        this.capacity = capacity;
    }

    public long amount() {
        if (!hatch.isRemote()) hatch.syncInputViews();
        return LongStacks.amount(hatch.inputs().fluids[slot]);
    }

    public long capacity() {
        return capacity.getAsLong();
    }

    @Override
    public FluidStack getFluid() {
        long amount = amount();
        if (amount <= 0) return null;
        FluidStack view = hatch.inputs().fluids[slot].getFluidStack();
        view.amount = (int) Math.min(Integer.MAX_VALUE / 2, amount);
        return view;
    }

    @Override
    public int getFluidAmount() {
        return (int) Math.min(Integer.MAX_VALUE / 2, amount());
    }

    @Override
    public int getCapacity() {
        int visible = getFluidAmount();
        long room = Math.max(0, capacity() - amount());
        return (int) (visible + Math.min(Integer.MAX_VALUE - (long) visible, room));
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) return 0;
        long amount = amount();
        IAEFluidStack stored = hatch.inputs().fluids[slot];
        if (amount > 0 && stored.getFluid() != resource.getFluid()) return 0;
        int accepted = (int) Math.min(resource.amount, Math.max(0, capacity() - amount));
        if (doFill && accepted > 0 && !hatch.isRemote()) {
            hatch.inputs().fluids[slot] = LongStacks.fluid(resource, Math.addExact(amount, accepted));
            hatch.inputs().refreshViews();
            hatch.onInputChanged();
        }
        return accepted;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        long amount = amount();
        if (maxDrain <= 0 || amount <= 0) return null;
        int taken = (int) Math.min(amount, maxDrain);
        IAEFluidStack stored = hatch.inputs().fluids[slot];
        FluidStack drained = stored.getFluidStack();
        drained.amount = taken;
        if (doDrain && !hatch.isRemote()) {
            hatch.inputs().fluids[slot] = amount == taken ? null : stored.setStackSize(amount - taken);
            hatch.inputs().refreshViews();
            hatch.onInputChanged();
        }
        return drained;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(getFluid(), getCapacity());
    }
}
