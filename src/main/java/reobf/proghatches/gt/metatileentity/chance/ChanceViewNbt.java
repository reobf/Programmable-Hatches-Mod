package reobf.proghatches.gt.metatileentity.chance;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

/** Screen snapshots stored with guide scene NBT. */
public final class ChanceViewNbt {

    public static final String KEY = "phGuideChanceView";

    private ChanceViewNbt() {}

    public static NBTTagCompound write(ChanceView.Screen screen, int dx, int dy, int dz) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("online", screen.online);
        NBTTagList entries = new NBTTagList(), held = new NBTTagList();
        for (ChanceView.Entry e : screen.entries) {
            NBTTagCompound t = shown(e.shown);
            t.setLong("expected", e.expected); t.setLong("deposit", e.deposit); t.setLong("pending", e.pending);
            t.setLong("produced", e.produced); t.setDouble("planned", e.planned);
            t.setDouble("recipeExpected", e.recipeExpected); t.setDouble("refillCopies", e.refillCopies);
            t.setDouble("refillItems", e.refillItems);
            NBTTagList lines = new NBTTagList();
            for (ChanceView.Line l : e.lines) {
                NBTTagCompound n = new NBTTagCompound();
                n.setInteger("x", l.x - dx); n.setInteger("y", l.y - dy); n.setInteger("z", l.z - dz);
                n.setInteger("index", l.index); n.setLong("expected", l.expected);
                n.setDouble("copies", l.copies); n.setDouble("items", l.items); n.setBoolean("pending", l.pending);
                lines.appendTag(n);
            }
            t.setTag("lines", lines); entries.appendTag(t);
        }
        for (ChanceView.Held h : screen.held) {
            NBTTagCompound t = shown(h.shown);
            t.setLong("held", h.held); t.setLong("ordered", h.ordered); held.appendTag(t);
        }
        tag.setTag("entries", entries); tag.setTag("held", held);
        return tag;
    }

    public static ChanceView.Screen read(NBTTagCompound tag) {
        List<ChanceView.Entry> entries = new ArrayList<>();
        List<ChanceView.Held> held = new ArrayList<>();
        NBTTagList list = tag.getTagList("entries", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            List<ChanceView.Line> lines = new ArrayList<>();
            NBTTagList raw = t.getTagList("lines", 10);
            for (int j = 0; j < raw.tagCount(); j++) {
                NBTTagCompound n = raw.getCompoundTagAt(j);
                lines.add(new ChanceView.Line(n.getInteger("x"), n.getInteger("y"), n.getInteger("z"),
                    n.getInteger("index"), n.getLong("expected"), n.getDouble("copies"),
                    n.getDouble("items"), n.getBoolean("pending")));
            }
            entries.add(new ChanceView.Entry(shown(t), t.getLong("expected"), t.getLong("deposit"),
                t.getLong("pending"), t.getDouble("planned"), t.getDouble("recipeExpected"), t.getLong("produced"),
                t.getDouble("refillCopies"), t.getDouble("refillItems"), lines));
        }
        list = tag.getTagList("held", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            held.add(new ChanceView.Held(shown(t), t.getLong("held"), t.getLong("ordered")));
        }
        return new ChanceView.Screen(tag.getBoolean("online"), entries, held);
    }

    private static NBTTagCompound shown(ChanceView.Shown s) {
        NBTTagCompound t = new NBTTagCompound();
        if (s.item != null) t.setTag("item", s.item.writeToNBT(new NBTTagCompound()));
        else t.setTag("fluid", s.fluid.writeToNBT(new NBTTagCompound()));
        return t;
    }

    private static ChanceView.Shown shown(NBTTagCompound t) {
        return ChanceView.Shown.of(t.hasKey("item")
            ? ChanceData.of(ItemStack.loadItemStackFromNBT(t.getCompoundTag("item")))
            : ChanceData.of(FluidStack.loadFluidStackFromNBT(t.getCompoundTag("fluid"))));
    }
}
