package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.lang.reflect.Field;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEStack;
import appeng.util.item.AEItemStack;
import codechicken.nei.ItemStackMap;
import gregtech.api.metatileentity.MetaTileEntity;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.util.ISpecialOptimize;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/** The pattern optimizer's bit shifts on a pattern hatch and on an ME mapping mirror, past int and on the pattern. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class PatternOptimizeTests {

    private static final int TOP = 1 << 30;

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void hatchMultiplierStopsAtTheIntLimit(GameTestHelper h) throws Exception {
        check(h, place(h, Registration.PatternOffset, 2));
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void mirrorMultiplierStopsAtTheIntLimit(GameTestHelper h) throws Exception {
        check(h, place(h, Registration.PatternMappingSlaveOffset, 3));
    }

    private static void check(GameTestHelper h, Object hatch) throws Exception {
        IInventory inv = (IInventory) hatch.getClass().getMethod("getPatterns").invoke(hatch);
        int[] multiplier = (int[]) LongInputBufferTests.field(hatch, "multiplier");
        ISpecialOptimize opt = (ISpecialOptimize) hatch;

        int[][] shifts = { { 1, 31, TOP }, { 1, 33, TOP }, { 1 << 20, 11, TOP }, { 3, 30, 3 << 29 }, { TOP, 1, TOP },
            { 4, -1, 2 }, { 4, -31, 1 } };
        for (int[] c : shifts) {
            ItemStack pattern = pattern(1, 4);
            inv.setInventorySlotContents(0, pattern);
            multiplier[0] = c[0];
            optimize(h, opt, hatch, pattern, c[0], c[1]);
            h.assertEquals(c[2], multiplier[0], "Multiplier " + c[0] + " shifted by " + c[1]);
        }

        ItemStack first = pattern(2, 4), second = pattern(3, 4);
        inv.setInventorySlotContents(0, first);
        inv.setInventorySlotContents(1, second);
        multiplier[0] = multiplier[1] = 1;
        ItemStackMap<Pair<Object, Integer>> lookup = new ItemStackMap<>();
        lookup.put(first, Pair.of(hatch, 0));
        lookup.put(second, Pair.of(hatch, 1));
        opt.optimize(lookup);
        h.assertEquals(1, multiplier[0], "A zero shift leaves its slot alone");
        h.assertEquals(2, multiplier[1], "The slots after a zero shift are still optimized");
        inv.setInventorySlotContents(1, null);

        setBoolean(hatch, "normalopt", true);
        ItemStack half = pattern(4, 4);
        inv.setInventorySlotContents(0, half);
        multiplier[0] = 1;
        optimize(h, opt, hatch, half, 1, -1);
        h.assertEquals(2L, amount(h, inv.getStackInSlot(0)), "Rewriting the pattern honours a halving");

        ItemStack scaled = pattern(5, 4);
        inv.setInventorySlotContents(0, scaled);
        multiplier[0] = 4;
        optimize(h, opt, hatch, scaled, 4, 1);
        ItemStack now = inv.getStackInSlot(0);
        h.assertTrue(now.getItem() == scaled.getItem(), "The slot keeps the real pattern item");
        h.assertEquals(8L, amount(h, now), "The real pattern is doubled");
        h.assertEquals(4, multiplier[0], "The multiplier stays");
        h.succeed();
    }

    private static void optimize(GameTestHelper h, ISpecialOptimize opt, Object hatch, ItemStack pattern, int multiplier,
        int bits) {
        ItemStack key = multiplier > 1 ? new PatternDualInputHatch.DA(details(h, pattern), multiplier).getPattern() : pattern;
        ItemStackMap<Pair<Object, Integer>> lookup = new ItemStackMap<>();
        lookup.put(key, Pair.of(hatch, bits));
        opt.optimize(lookup);
    }

    private static Object place(GameTestHelper h, int offset, int z) {
        return ChancePatternTests.place(h, at(4, 1, z), Config.metaTileEntityOffset + offset, ForgeDirection.NORTH)
            .getMetaTileEntity();
    }

    private static ItemStack pattern(int meta, int amount) {
        return PalladiumLineTests.Line.pattern(
            new IAEStack<?>[] { AEItemStack.create(new ItemStack(Blocks.stained_glass, amount, meta)) },
            new IAEStack<?>[] { AEItemStack.create(new ItemStack(Blocks.glass, amount, 0)) },
            false);
    }

    private static ICraftingPatternDetails details(GameTestHelper h, ItemStack pattern) {
        return ((ICraftingPatternItem) pattern.getItem()).getPatternForItem(pattern, h.getWorld());
    }

    private static long amount(GameTestHelper h, ItemStack pattern) {
        return details(h, pattern).getCondensedAEInputs()[0].getStackSize();
    }

    private static void setBoolean(Object target, String name, boolean value) throws ReflectiveOperationException {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) try {
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            f.setBoolean(target, value);
            return;
        } catch (NoSuchFieldException ignored) {}
        throw new NoSuchFieldException(name);
    }
}
