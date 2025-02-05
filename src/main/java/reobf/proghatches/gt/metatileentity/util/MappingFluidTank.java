package reobf.proghatches.gt.metatileentity.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public class MappingFluidTank implements IFluidTank {

    public Consumer<FluidStack> set;
    public Supplier<FluidStack> get;

    public MappingFluidTank(Consumer<FluidStack> set, Supplier<FluidStack> get) {
        this.set = set;
        this.get = get;

    }

    public void setFluid(FluidStack fluid) {
        set.accept(fluid);
    }

    public void setCapacity(int capacity) {

    }

    /* IFluidTank */
    @Override
    public FluidStack getFluid() {
        return get.get();
    }

    @Override
    public int getFluidAmount() {
        if (get.get() == null) {
            return 0;
        }
        return get.get().amount;
    }

    @Override
    public int getCapacity() {
        return 1;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null) {
            return 0;
        }
        doFill = true;
        if (!doFill) {
            if (get.get() == null) {
                return Math.min(1, resource.amount);
            }

            if (!get.get()
                .isFluidEqual(resource)) {
                return 0;
            }

            return Math.min(1 - get.get().amount, resource.amount);
        }

        if (get.get() == null) {
            set.accept(new FluidStack(resource, Math.min(1, resource.amount)));

            return get.get().amount;
        }

        if (!get.get()
            .isFluidEqual(resource)) {
            return 0;
        }
        int filled = 1 - get.get().amount;

        if (resource.amount < filled) {
            get.get().amount += resource.amount;
            filled = resource.amount;
        } else {
            get.get().amount = 1;
        }

        return filled;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (get.get() == null) {
            return null;
        }
        doDrain = true;
        int drained = maxDrain;
        if (get.get().amount < drained) {
            drained = get.get().amount;
        }

        FluidStack stack = new FluidStack(get.get(), drained);
        if (doDrain) {
            get.get().amount -= drained;
            if (get.get().amount <= 0) {
                set.accept(null);
            }

        }
        return stack;
    }
}
