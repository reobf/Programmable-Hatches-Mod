package reobf.proghatches.gt.metatileentity.util;

import net.minecraftforge.fluids.FluidStack;

/**
 * A {@link ListeningFluidTank} whose content is mirrored into one slot of a shared, contiguous
 * FluidStack[] backend, so "which slots are occupied" can be answered by scanning plain array
 * references (NullScan-able) instead of dereferencing every tank.
 * <p>
 * The tank's own {@code fluid} field stays the source of truth and ALL semantics are inherited
 * unchanged: in-place amount mutations (fill/drain on an existing stack) are visible through the
 * backend automatically because both alias the same FluidStack instance; only FIELD ASSIGNMENTS
 * need mirroring, and those happen exclusively inside the five methods overridden here (note that
 * fillDirect/setFluidDirect invoke super implementations via invokespecial, bypassing overridden
 * setFluid/fill — which is why they are each mirrored explicitly; readFromNBT arrives through
 * virtual setFluidDirect and needs nothing).
 */
public class BackedListeningFluidTank extends ListeningFluidTank {

    private final FluidStack[] back;
    private final int idx;

    public BackedListeningFluidTank(int capacity, IOnFillCallback thiz, FluidStack[] back, int idx) {
        super(capacity, thiz);
        this.back = back;
        this.idx = idx;
    }

    @Override
    public void setFluid(FluidStack f) {
        super.setFluid(f);
        back[idx] = this.fluid;
    }

    @Override
    public void setFluidDirect(FluidStack f) {
        super.setFluidDirect(f);
        back[idx] = this.fluid;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        try {
            return super.fill(resource, doFill);
        } finally {
            back[idx] = this.fluid;
        }
    }

    @Override
    public int fillDirect(FluidStack resource, boolean doFill) {
        try {
            return super.fillDirect(resource, doFill);
        } finally {
            back[idx] = this.fluid;
        }
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        try {
            return super.drain(maxDrain, doDrain);
        } finally {
            back[idx] = this.fluid;
        }
    }
}
