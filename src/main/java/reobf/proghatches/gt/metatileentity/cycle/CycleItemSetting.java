package reobf.proghatches.gt.metatileentity.cycle;

public final class CycleItemSetting {

    public static final CycleItemSetting DEFAULT = new CycleItemSetting(0, false, 0);

    public final long target;
    public final boolean output;
    public final int tolerancePercent;

    public CycleItemSetting(long target, boolean output, int tolerancePercent) {
        this.target = Math.max(0, target);
        this.output = output;
        this.tolerancePercent = Math.max(0, Math.min(100, tolerancePercent));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CycleItemSetting)) return false;
        CycleItemSetting other = (CycleItemSetting) o;
        return target == other.target && output == other.output && tolerancePercent == other.tolerancePercent;
    }

    @Override
    public int hashCode() {
        return (Long.hashCode(target) * 31 + (output ? 1 : 0)) * 31 + tolerancePercent;
    }

    @Override
    public String toString() {
        return "target=" + target + (output ? ", output" : "") + ", tol=" + tolerancePercent + "%";
    }
}
