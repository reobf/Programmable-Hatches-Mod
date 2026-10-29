package reobf.proghatches.gt.metatileentity.chance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.FluidDrawable;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;

/** What the client shows of chance patterns: immutable snapshots that travel from server to client. */
public final class ChanceView {

    private ChanceView() {}

    /** An item or a fluid, for its icon and its name. */
    public static final class Shown {

        public final ItemStack item;
        public final FluidStack fluid;
        private IDrawable icon;

        private Shown(ItemStack item, FluidStack fluid) {
            this.item = item;
            this.fluid = fluid;
        }

        public static Shown of(IAEStack<?> s) {
            if (s instanceof IAEItemStack i) {
                ItemStack st = i.getItemStack();
                st.stackSize = 1;
                return new Shown(st, null);
            }
            FluidStack f = ((IAEFluidStack) s).getFluidStack();
            f.amount = 1;
            return new Shown(null, f);
        }

        public String name() {
            return item != null ? item.getDisplayName() : fluid.getLocalizedName();
        }

        public IDrawable icon() {
            if (icon == null) icon = item != null ? new ItemDrawable(item) : new FluidDrawable(fluid);
            return icon;
        }

        void write(PacketBuffer b) throws IOException {
            b.writeBoolean(item != null);
            if (item != null) NetworkUtils.writeItemStack(b, item);
            else NetworkUtils.writeFluidStack(b, fluid);
        }

        static Shown read(PacketBuffer b) throws IOException {
            return b.readBoolean() ? new Shown(NetworkUtils.readItemStack(b), null)
                : new Shown(null, NetworkUtils.readFluidStack(b));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Shown s)) return false;
            if (item != null) return s.item != null && ItemStack.areItemStacksEqual(item, s.item);
            return s.fluid != null && fluid.isFluidStackIdentical(s.fluid);
        }

        @Override
        public int hashCode() {
            return item != null ? item.getItem()
                .hashCode() * 31 + item.getItemDamage() : fluid.getFluidID();
        }
    }

    /** One output of a chance buffer. */
    public static final class Out {

        public final Shown shown;
        public final long expected;
        public final double refilled;

        public Out(Shown shown, long expected, double refilled) {
            this.shown = shown;
            this.expected = expected;
            this.refilled = refilled;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Out x && x.shown.equals(shown) && x.expected == expected && x.refilled == refilled;
        }

        @Override
        public int hashCode() {
            return Objects.hash(shown, expected, refilled);
        }
    }

    /** A chance buffer as its hatch shows it: the copies refilled this round and what each output still expects. */
    public static final class Buffer {

        public static final Buffer NONE = new Buffer(false, 0, Collections.emptyList());

        public final boolean pending;
        public final double copies;
        public final List<Out> outs;

        public Buffer(boolean pending, double copies, List<Out> outs) {
            this.pending = pending;
            this.copies = copies;
            this.outs = outs;
        }

        public boolean isChance() {
            return !outs.isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Buffer x && x.pending == pending && x.copies == copies && x.outs.equals(outs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pending, copies, outs);
        }

        public static final IByteBufAdapter<Buffer> ADAPTER = new IByteBufAdapter<Buffer>() {

            @Override
            public Buffer deserialize(PacketBuffer b) throws IOException {
                boolean pending = b.readBoolean();
                double copies = b.readDouble();
                int n = b.readVarIntFromBuffer();
                List<Out> outs = new ArrayList<>(n);
                for (int i = 0; i < n; i++) outs.add(new Out(Shown.read(b), b.readLong(), b.readDouble()));
                return new Buffer(pending, copies, outs);
            }

            @Override
            public void serialize(PacketBuffer b, Buffer v) throws IOException {
                b.writeBoolean(v.pending);
                b.writeDouble(v.copies);
                b.writeVarIntToBuffer(v.outs.size());
                for (Out o : v.outs) {
                    o.shown.write(b);
                    b.writeLong(o.expected);
                    b.writeDouble(o.refilled);
                }
            }

            @Override
            public boolean areEqual(Buffer a, Buffer b) {
                return Objects.equals(a, b);
            }
        };
    }

    /** One chance buffer expecting the output an entry is about. */
    public static final class Line {

        public final int x, y, z, index;
        public final long expected;
        public final double copies, items;
        public final boolean pending;

        public Line(int x, int y, int z, int index, long expected, double copies, double items, boolean pending) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.index = index;
            this.expected = expected;
            this.copies = copies;
            this.items = items;
            this.pending = pending;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Line l && l.x == x
                && l.y == y
                && l.z == z
                && l.index == index
                && l.expected == expected
                && l.copies == copies
                && l.items == items
                && l.pending == pending;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, index, expected, copies, items, pending);
        }
    }

    /** Everything the controller knows about one output on its network. */
    public static final class Entry {

        public final Shown shown;
        public final long expected, deposit, pending, produced;
        public final double planned, recipeExpected, refillCopies, refillItems;
        public final List<Line> lines;

        public Entry(Shown shown, long expected, long deposit, long pending, double planned, double recipeExpected,
            long produced, double refillCopies, double refillItems, List<Line> lines) {
            this.shown = shown;
            this.expected = expected;
            this.deposit = deposit;
            this.pending = pending;
            this.planned = planned;
            this.recipeExpected = recipeExpected;
            this.produced = produced;
            this.refillCopies = refillCopies;
            this.refillItems = refillItems;
            this.lines = lines;
        }

        /** The rolled amount against the recipe's expectation, as a percentage; "-" before any run. */
        public String ratio() {
            return recipeExpected > 0 ? String.format("%.1f%%", produced * 100 / recipeExpected) : "-";
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Entry e && e.shown.equals(shown)
                && e.expected == expected
                && e.deposit == deposit
                && e.pending == pending
                && e.produced == produced
                && e.planned == planned
                && e.recipeExpected == recipeExpected
                && e.refillCopies == refillCopies
                && e.refillItems == refillItems
                && e.lines.equals(lines);
        }

        @Override
        public int hashCode() {
            return Objects.hash(shown, expected, deposit, pending, produced, planned, recipeExpected, lines);
        }
    }

    /** An input the controller holds for refills, or has on order. */
    public static final class Held {

        public final Shown shown;
        public final long held, ordered;

        public Held(Shown shown, long held, long ordered) {
            this.shown = shown;
            this.held = held;
            this.ordered = ordered;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Held h && h.shown.equals(shown) && h.held == held && h.ordered == ordered;
        }

        @Override
        public int hashCode() {
            return Objects.hash(shown, held, ordered);
        }
    }

    /** The controller screen: whether the network is up, one entry per output, and the inputs held or on order. */
    public static final class Screen {

        public static final Screen EMPTY = new Screen(false, Collections.emptyList(), Collections.emptyList());

        public final boolean online;
        public final List<Entry> entries;
        public final List<Held> held;

        public Screen(boolean online, List<Entry> entries, List<Held> held) {
            this.online = online;
            this.entries = entries;
            this.held = held;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Screen s && s.online == online && s.entries.equals(entries) && s.held.equals(held);
        }

        @Override
        public int hashCode() {
            return Objects.hash(online, entries, held);
        }

        public static final IByteBufAdapter<Screen> ADAPTER = new IByteBufAdapter<Screen>() {

            @Override
            public Screen deserialize(PacketBuffer b) throws IOException {
                boolean online = b.readBoolean();
                int h = b.readVarIntFromBuffer();
                List<Held> held = new ArrayList<>(h);
                for (int i = 0; i < h; i++) held.add(new Held(Shown.read(b), b.readLong(), b.readLong()));
                int n = b.readVarIntFromBuffer();
                List<Entry> entries = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    Shown shown = Shown.read(b);
                    long expected = b.readLong(), deposit = b.readLong(), pending = b.readLong();
                    double planned = b.readDouble(), recipeExpected = b.readDouble();
                    long produced = b.readLong();
                    double refillCopies = b.readDouble(), refillItems = b.readDouble();
                    int m = b.readVarIntFromBuffer();
                    List<Line> lines = new ArrayList<>(m);
                    for (int k = 0; k < m; k++) lines.add(
                        new Line(
                            b.readInt(),
                            b.readInt(),
                            b.readInt(),
                            b.readVarIntFromBuffer(),
                            b.readLong(),
                            b.readDouble(),
                            b.readDouble(),
                            b.readBoolean()));
                    entries.add(
                        new Entry(
                            shown,
                            expected,
                            deposit,
                            pending,
                            planned,
                            recipeExpected,
                            produced,
                            refillCopies,
                            refillItems,
                            lines));
                }
                return new Screen(online, entries, held);
            }

            @Override
            public void serialize(PacketBuffer b, Screen v) throws IOException {
                b.writeBoolean(v.online);
                b.writeVarIntToBuffer(v.held.size());
                for (Held h : v.held) {
                    h.shown.write(b);
                    b.writeLong(h.held);
                    b.writeLong(h.ordered);
                }
                b.writeVarIntToBuffer(v.entries.size());
                for (Entry e : v.entries) {
                    e.shown.write(b);
                    b.writeLong(e.expected);
                    b.writeLong(e.deposit);
                    b.writeLong(e.pending);
                    b.writeDouble(e.planned);
                    b.writeDouble(e.recipeExpected);
                    b.writeLong(e.produced);
                    b.writeDouble(e.refillCopies);
                    b.writeDouble(e.refillItems);
                    b.writeVarIntToBuffer(e.lines.size());
                    for (Line l : e.lines) {
                        b.writeInt(l.x);
                        b.writeInt(l.y);
                        b.writeInt(l.z);
                        b.writeVarIntToBuffer(l.index);
                        b.writeLong(l.expected);
                        b.writeDouble(l.copies);
                        b.writeDouble(l.items);
                        b.writeBoolean(l.pending);
                    }
                }
            }

            @Override
            public boolean areEqual(Screen a, Screen b) {
                return Objects.equals(a, b);
            }
        };
    }

    /** Whole numbers without decimals, everything else with at most two. */
    public static String num(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-6) return Long.toString((long) Math.rint(v));
        String s = String.format("%.2f", v);
        while (s.endsWith("0")) s = s.substring(0, s.length() - 1);
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }
}
