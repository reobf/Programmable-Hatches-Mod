package reobf.proghatches.gt.metatileentity.chance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import gregtech.api.util.GTRecipe;
import reobf.proghatches.main.MyMod;

/** Chance accounting of one buffer: what one pushed copy promises, what is still owed, and what one run uses. */
public final class ChanceData {

    /** Copies one refill adds at most, and copies refilled without an owed output before the debt is dropped. */
    public static final long ROUND_COPIES = 64;

    public final List<IAEStack<?>> outputs = new ArrayList<>();
    public final List<IAEStack<?>> inputs = new ArrayList<>();
    public long[] debt = new long[0];
    public long[] unit = new long[0];
    public boolean pending;
    /** Runs refilled since nothing was last owed. */
    public long refilledRuns;
    /** Runs refilled since an owed output was last produced or paid from the deposit. */
    public long idle;
    /** Runs of the latest recipe check that used this buffer, the recipe it ran and its chance multiplier. */
    public transient int lastRuns;
    public transient GTRecipe lastRecipe;
    public transient double lastChanceMult = 1;

    public static ChanceData of(ICraftingPatternDetails d) {
        ChanceData c = new ChanceData();
        IAEStack<?>[] out = d.getCondensedAEOutputs();
        if (out != null) for (IAEStack<?> s : out) add(c.outputs, s);
        IAEStack<?>[] in = d.getCondensedAEInputs();
        if (in != null) for (IAEStack<?> s : in) add(c.inputs, s);
        c.debt = new long[c.outputs.size()];
        c.unit = new long[c.inputs.size()];
        return c;
    }

    public boolean sameOutputs(ChanceData o) {
        if (o == null || o.outputs.size() != outputs.size()) return false;
        for (IAEStack<?> s : outputs) if (amountOf(o.outputs, s) != s.getStackSize()) return false;
        return true;
    }

    public void onPush(long copies) {
        for (int j = 0; j < debt.length; j++) debt[j] = sat(debt[j], satMul(outputs.get(j).getStackSize(), copies));
    }

    /** Learns what one run takes from the inputs used by one recipe check. */
    public void onConsumed(List<IAEStack<?>> diff, int parallels) {
        lastRuns = Math.max(1, parallels);
        for (int k = 0; k < inputs.size(); k++) {
            long n = amountOf(diff, inputs.get(k));
            long u = parallels > 0 ? n / parallels : n;
            if (u > 0) unit[k] = u;
        }
    }

    /** Pays the debt with what the machine rolled; returns the part nobody was waiting for. */
    public List<IAEStack<?>> settle(List<IAEStack<?>> produced) {
        List<IAEStack<?>> surplus = new ArrayList<>();
        for (IAEStack<?> p : produced) {
            long r = p.getStackSize();
            if (r <= 0) continue;
            int j = indexOf(outputs, p);
            if (j < 0) continue;
            long take = Math.min(debt[j], r);
            debt[j] -= take;
            if (take > 0) idle = 0;
            if (r > take) {
                IAEStack<?> s = p.copy();
                s.setStackSize(r - take);
                surplus.add(s);
            }
        }
        return surplus;
    }

    /** Whether the outputs still owed exceed what the runs left in the buffer are expected to make. */
    public boolean needsRefill(List<IAEStack<?>> contents) {
        return owed() && runsForDebt() > runsIn(contents);
    }

    public boolean owed() {
        for (long d : debt) if (d > 0) return true;
        return false;
    }

    public void clearDebt() {
        Arrays.fill(debt, 0);
    }

    /** What one run takes of input k: the observed use, or a whole copy while no run has been observed. */
    private long runUse(int k) {
        for (long u : unit) if (u > 0) return unit[k];
        return isCircuit(inputs.get(k)) ? 0 : inputs.get(k).getStackSize();
    }

    /** Runs one copy of the pattern feeds, judged by its largest input. */
    private double runsPerCopyExact() {
        double rpc = 1;
        long bestPer = -1;
        for (int k = 0; k < inputs.size(); k++) {
            long use = runUse(k), per = inputs.get(k).getStackSize();
            if (use > 0 && per > bestPer) {
                bestPer = per;
                rpc = per / (double) use;
            }
        }
        return rpc;
    }

    public long runsPerCopy() {
        return Math.max(1, (long) Math.ceil(runsPerCopyExact() - 1e-9));
    }

    /** Pattern copies the given runs make up. */
    public double copies(long runs) {
        return runs / runsPerCopyExact();
    }

    /** Output j that the given runs make at the pattern's promised rate. */
    public double promised(int j, long runs) {
        return outputs.get(j)
            .getStackSize() * copies(runs);
    }

    /** Whole runs the inputs in the buffer still feed. */
    public long runsIn(List<IAEStack<?>> contents) {
        long runs = Long.MAX_VALUE;
        for (int k = 0; k < inputs.size(); k++) {
            long use = runUse(k);
            if (use > 0) runs = Math.min(runs, amountOf(contents, inputs.get(k)) / use);
        }
        return runs == Long.MAX_VALUE ? 0 : runs;
    }

    /** Runs whose promised outputs cover the debt. */
    private long runsForDebt() {
        double rpc = runsPerCopyExact();
        long runs = 0;
        for (int j = 0; j < debt.length; j++) {
            long out = outputs.get(j).getStackSize();
            if (debt[j] <= 0 || out <= 0) continue;
            double need = Math.ceil(debt[j] * rpc / out - 1e-9);
            runs = Math.max(runs, need >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) need);
        }
        return runs;
    }

    /** Runs one refill round may add at most. */
    public long roundRuns() {
        return satMul(ROUND_COPIES, runsPerCopy());
    }

    /** Runs the buffer is short of covering the debt, within one round. */
    public long runsShort(List<IAEStack<?>> contents) {
        return Math.max(0, Math.min(runsForDebt() - runsIn(contents), roundRuns()));
    }

    /** Runs to add so that the runs in the buffer are expected to cover the debt, within one round. */
    public long runsToAdd(List<IAEStack<?>> contents) {
        return Math.max(1, runsShort(contents));
    }

    /** Whether a full round's worth of refilled runs has gone by without an owed output. */
    public boolean fruitless() {
        return idle >= roundRuns();
    }

    /** The inputs of the given number of runs, leaving out those a run does not use up. */
    public List<IAEStack<?>> refillAmounts(long runs) {
        List<IAEStack<?>> list = new ArrayList<>();
        for (int k = 0; k < inputs.size(); k++) {
            long use = runUse(k);
            if (use <= 0) continue;
            IAEStack<?> s = inputs.get(k)
                .copy();
            s.setStackSize(satMul(use, runs));
            list.add(s);
        }
        return list;
    }

    public NBTTagCompound toTag() {
        NBTTagCompound t = new NBTTagCompound();
        NBTTagList out = new NBTTagList();
        for (int j = 0; j < outputs.size(); j++) {
            NBTTagCompound e = write(outputs.get(j));
            e.setLong("phD", debt[j]);
            out.appendTag(e);
        }
        NBTTagList in = new NBTTagList();
        for (int k = 0; k < inputs.size(); k++) {
            NBTTagCompound e = write(inputs.get(k));
            e.setLong("phU", unit[k]);
            in.appendTag(e);
        }
        t.setTag("out", out);
        t.setTag("in", in);
        t.setBoolean("pending", pending);
        t.setLong("refilled", refilledRuns);
        t.setLong("idle", idle);
        return t;
    }

    public static ChanceData fromTag(NBTTagCompound t) {
        ChanceData c = new ChanceData();
        NBTTagList out = t.getTagList("out", 10);
        List<Long> d = new ArrayList<>();
        for (int j = 0; j < out.tagCount(); j++) {
            NBTTagCompound e = out.getCompoundTagAt(j);
            IAEStack<?> s = load(e);
            if (s == null) continue;
            c.outputs.add(s);
            d.add(e.getLong("phD"));
        }
        NBTTagList in = t.getTagList("in", 10);
        List<Long> us = new ArrayList<>();
        for (int k = 0; k < in.tagCount(); k++) {
            NBTTagCompound e = in.getCompoundTagAt(k);
            IAEStack<?> s = load(e);
            if (s == null) continue;
            c.inputs.add(s);
            us.add(e.getLong("phU"));
        }
        c.debt = unbox(d);
        c.unit = unbox(us);
        c.pending = t.getBoolean("pending");
        c.refilledRuns = t.getLong("refilled");
        c.idle = t.getLong("idle");
        return c;
    }

    public static NBTTagCompound write(IAEStack<?> s) {
        NBTTagCompound e = new NBTTagCompound();
        s.writeToNBT(e);
        e.setBoolean("phF", s.isFluid());
        return e;
    }

    public static IAEStack<?> load(NBTTagCompound e) {
        return e.getBoolean("phF") ? AEFluidStack.loadFluidStackFromNBT(e) : AEItemStack.loadItemStackFromNBT(e);
    }

    private static long[] unbox(List<Long> l) {
        long[] a = new long[l.size()];
        for (int i = 0; i < a.length; i++) a[i] = l.get(i);
        return a;
    }

    public static IAEStack<?> of(ItemStack s) {
        return s == null ? null : AEItemStack.create(s);
    }

    public static IAEStack<?> of(FluidStack f) {
        return f == null ? null : AEFluidStack.create(f);
    }

    public static boolean same(IAEStack<?> a, IAEStack<?> b) {
        return a != null && b != null && a.isItem() == b.isItem() && a.isSameType(b);
    }

    public static int indexOf(List<IAEStack<?>> list, IAEStack<?> s) {
        for (int i = 0; i < list.size(); i++) if (same(list.get(i), s)) return i;
        return -1;
    }

    public static long amountOf(List<IAEStack<?>> list, IAEStack<?> type) {
        int i = indexOf(list, type);
        return i < 0 ? 0 : list.get(i).getStackSize();
    }

    public static void add(List<IAEStack<?>> list, IAEStack<?> s) {
        if (s == null || s.getStackSize() <= 0) return;
        int i = indexOf(list, s);
        if (i < 0) list.add(s.copy());
        else list.get(i).setStackSize(sat(list.get(i).getStackSize(), s.getStackSize()));
    }

    public static String name(IAEStack<?> s) {
        if (s instanceof IAEItemStack i && i.getItemStack() != null) return i.getItemStack().getDisplayName();
        if (s instanceof IAEFluidStack f && f.getFluidStack() != null) return f.getFluidStack().getLocalizedName();
        return String.valueOf(s);
    }

    static boolean isCircuit(IAEStack<?> s) {
        return s instanceof IAEItemStack i && i.getItem() == MyMod.progcircuit;
    }

    public static long sat(long a, long b) {
        long r = a + b;
        return r < a ? Long.MAX_VALUE : r;
    }

    public static long satMul(long a, long b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }
}
