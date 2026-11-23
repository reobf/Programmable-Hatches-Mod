package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.storage.data.IAEStack;
import appeng.api.util.IInterfaceViewable;
import appeng.util.item.AEItemStack;
import reobf.proghatches.item.patterngen.PatternGeneratorDelivery;
import reobf.proghatches.item.patterngen.PatternGeneratorTarget;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/** Direct-fill insertion across PH's unlocked pattern pages and at full capacity. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class PatternGeneratorDeliveryTests {

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void insertionCrossesTheUnlockedPageBoundary(GameTestHelper h) {
        IInventory patterns = patterns(h, 2);
        h.assertEquals(36, patterns.getSizeInventory(), "Pattern hatch starts with one page");
        for (int i = 0; i < 36; i++) {
            h.assertTrue(PatternGeneratorDelivery.insert(patterns, pattern()), "Insert pattern " + i);
        }
        h.assertEquals(72, patterns.getSizeInventory(), "Filling the first page unlocks the second");
        h.assertTrue(PatternGeneratorDelivery.insert(patterns, pattern()), "Insert across the page boundary");
        h.assertTrue(patterns.getStackInSlot(36) != null, "The new page's first slot is used");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 400)
    public static void fullTargetRefusesFurtherInsertion(GameTestHelper h) {
        IInventory patterns = patterns(h, 3);
        for (int i = 0; i < 360; i++) {
            h.assertTrue(PatternGeneratorDelivery.insert(patterns, pattern()), "Fill pattern " + i);
        }
        h.assertEquals(360, patterns.getSizeInventory(), "All ten pages are unlocked");
        h.assertFalse(PatternGeneratorDelivery.insert(patterns, pattern()), "A full target refuses insertion");
        h.assertTrue(patterns.getStackInSlot(0) != null, "The existing pattern is not overwritten");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void phPagesAreNotCappedAtTheInputBusCircuitSlot(GameTestHelper h) {
        TestPos pos = at(4, 1, 4);
        ChancePatternTests.place(
            h,
            pos,
            Config.metaTileEntityOffset + Registration.SuperPatternOffset,
            ForgeDirection.NORTH);
        TestPos absolute = h.absolute(pos);
        PatternGeneratorTarget target = PatternGeneratorTarget
            .resolve(h.getWorld(), absolute.x(), absolute.y(), absolute.z(), 0.5f, 0.5f, 0.5f);
        h.assertNotNull(target, "Super pattern hatch target");
        h.assertEquals(36, target.patternCapacity(), "Circuit-slot cap must not shrink PH's 36-slot page");
        for (int i = 0; i < 36; i++) {
            h.assertTrue(PatternGeneratorDelivery.insert(target.inventory, pattern()), "Fill pattern " + i);
        }
        h.assertEquals(72, target.patternCapacity(), "PH capacity grows after the page fills");
        h.succeed();
    }

    private static IInventory patterns(GameTestHelper h, int z) {
        Object hatch = ChancePatternTests
            .place(
                h,
                at(4, 1, z),
                Config.metaTileEntityOffset + Registration.SuperPatternOffset,
                ForgeDirection.NORTH)
            .getMetaTileEntity();
        return ((IInterfaceViewable) hatch).getPatterns();
    }

    private static ItemStack pattern() {
        IAEStack<?>[] input = { AEItemStack.create(new ItemStack(net.minecraft.init.Blocks.stone)) };
        IAEStack<?>[] output = { AEItemStack.create(new ItemStack(net.minecraft.init.Blocks.glass)) };
        return PalladiumLineTests.Line.pattern(input, output, false);
    }
}
