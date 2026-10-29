package reobf.proghatches.gametests;

import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;
import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizons.horizonqa.api.GameTestArguments;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import com.gtnewhorizons.horizonqa.api.annotation.MethodSource;
import com.gtnewhorizons.horizonqa.api.gt.Multiblock;
import com.gtnewhorizons.horizonqa.internal.GameTestSequence;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.container.ContainerNull;
import appeng.me.GridAccessException;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.TierEU;
import gregtech.api.interfaces.IItemContainer;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchEnergyDebug;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import reobf.proghatches.ae.TileChanceController;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.chance.ChanceData;
import reobf.proghatches.gt.metatileentity.chance.ChanceMark;
import reobf.proghatches.gt.metatileentity.chance.ChanceView;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import tectech.thing.CustomItemList;

/**
 * A Large Sifter fed by a pattern dual input hatch on an ME network with a chance pattern controller. Each case
 * sifts its own wool color into lapis blocks, because the cases run side by side and share the sifter's recipe map.
 * The patterns pushed into the hatch promise a different amount than the test recipe makes, so every shortfall and
 * surplus is known in advance.
 */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class ChancePatternTests {

    private static final TestPos SIFTER = at(3, 1, 0);
    private static final TestPos HATCH = at(1, 1, 2);
    private static final TestPos OUTPUT_BUS = at(1, 1, 1);
    private static final TestPos ENERGY = at(2, 0, 0);
    private static final TestPos MAINTENANCE = at(4, 0, 0);
    private static final TestPos MUFFLER = at(3, 5, 0);

    private static final long STOCK = 64;
    private static final BaseActionSource SOURCE = new BaseActionSource();

    // One run makes one output but the pattern promises two: the controller has to refill exactly one more run.
    @GameTest(template = "sifter", timeoutTicks = 800)
    public static void shortRunIsRefilledFromStorage(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 8, 1, 10000);
        ICraftingPatternDetails pattern = f.details(1, 2, true);

        helper.startSequence()
            .thenWaitUntil("wait for the ME network", 100, f::assertNetworkActive)
            .thenExecute("push one copy that owes two outputs", () -> {
                helper.assertTrue(f.push(pattern, 1), "The hatch should accept the push");
                helper.assertEquals(2L, f.owed(), "The push should owe two outputs");
            })
            .thenWaitUntil("wait for the refilled run", 600, () -> {
                helper.assertEquals(2L, f.stored(f.output), "Both promised outputs should reach the network" + f.why());
                helper.assertEquals(STOCK - 1, f.stored(f.input), "Exactly one run should be refilled" + f.why());
                f.assertSettled();
                ChanceView.Entry e = f.entry();
                helper.assertTrue(
                    e != null && Math.abs(e.recipeExpected - 2) < 1e-6 && e.produced == 2,
                    "Two runs of a sure recipe should expect and roll two" + f.why());
            })
            .thenExecuteFor(60, () -> {
                helper.assertEquals(2L, f.stored(f.output), "No run should follow once nothing is owed" + f.why());
                helper.assertEquals(STOCK - 1, f.stored(f.input), "No refill should follow" + f.why());
            })
            .thenSucceed();
    }

    // Two chance patterns with the same input go to separate buffers; the surplus of one may pay the other's debt.
    // Run at several phases against the hatch's refill tick and in both push orders.
    @GameTest(template = "sifter", timeoutTicks = 900)
    @MethodSource("phasesAndOrders")
    public static void surplusOfOneBufferPaysAnother(GameTestHelper helper, int phase, boolean surplusFirst) {
        Fixture f = new Fixture(helper, phase / 5 * 2 + (surplusFirst ? 0 : 1), 2, 10000);
        ICraftingPatternDetails surplus = f.details(1, 1, true);
        ICraftingPatternDetails shortfall = f.details(1, 3, true);

        GameTestSequence sequence = helper.startSequence()
            .thenWaitUntil("wait for the ME network", 100, f::assertNetworkActive);
        if (phase > 0) sequence = sequence.thenIdle(phase);
        sequence.thenExecute("push a pattern owing one and a pattern owing three", () -> {
            helper.assertTrue(f.push(surplusFirst ? surplus : shortfall, 1), "The hatch should take push 1");
            helper.assertTrue(f.push(surplusFirst ? shortfall : surplus, 1), "The hatch should take push 2");
            helper.assertEquals(2L, (long) f.chances().size(), "Each pattern should be booked in its own buffer");
            helper.assertEquals(4L, f.owed(), "The two pushes should owe four outputs together");
        })
            .thenWaitUntil("wait until nothing is owed", 800, () -> {
                f.assertSettled();
                long refills = STOCK - f.stored(f.input);
                long made = f.stored(f.output) + f.deposit();
                helper.assertTrue(refills <= 1, "At most one run should be refilled, saw " + refills + f.why());
                helper.assertEquals(2 * (2 + refills), made, "Every run should make two outputs" + f.why());
                helper.assertEquals(4L, f.stored(f.output), "Storage should hold what was expected" + f.why());
                helper.assertEquals(made - 4, f.deposit(), "The controller should hold the surplus" + f.why());
                helper.assertTrue(f.onTheWay() == 0, "Every surplus should have been taken in" + f.why());
            })
            .thenSucceed();
    }

    public static List<GameTestArguments> phasesAndOrders() {
        List<GameTestArguments> cases = new ArrayList<>();
        for (int phase = 0; phase < 20; phase += 5) {
            cases.add(GameTestArguments.named("phase-" + phase + "-surplus-first", phase, true));
            cases.add(GameTestArguments.named("phase-" + phase + "-shortfall-first", phase, false));
        }
        return cases;
    }

    // A pattern without the mark is never booked and never refilled.
    @GameTest(template = "sifter", timeoutTicks = 600)
    public static void unmarkedPatternIsNotTracked(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 9, 1, 10000);
        ICraftingPatternDetails pattern = f.details(1, 2, false);

        helper.startSequence()
            .thenWaitUntil("wait for the ME network", 100, f::assertNetworkActive)
            .thenExecute("push an unmarked pattern", () -> {
                helper.assertTrue(f.push(pattern, 1), "The hatch should accept the push");
                helper.assertTrue(f.chances().isEmpty(), "An unmarked pattern should not be booked");
            })
            .thenWaitUntil(
                "wait for the single run",
                400,
                () -> helper.assertEquals(1L, f.stored(f.output), "The run should make one output" + f.why()))
            .thenExecuteFor(80, () -> {
                helper.assertEquals(STOCK, f.stored(f.input), "Nothing should be refilled" + f.why());
                helper.assertEquals(1L, f.stored(f.output), "No further run should happen" + f.why());
            })
            .thenSucceed();
    }

    // A crafting CPU waits for the outputs the pattern promises; the refills make the job finish.
    @GameTest(template = "sifter", timeoutTicks = 1200)
    public static void craftingJobFinishesThroughRefills(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 10, 1, 10000);
        AtomicReference<ICraftingLink> link = new AtomicReference<>();

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, f::assertCraftingReady)
            .thenExecute("install the chance pattern", () -> f.install(f.pattern(1, 2, true)))
            .thenWaitUntil(
                "wait for the pattern to be offered",
                80,
                () -> helper.assertTrue(f.offered(), "The pattern should be offered"))
            .thenExecute("request four outputs", () -> link.set(f.submit(4)))
            .thenWaitUntil("wait for the job to finish", 1000, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + f.why());
                helper.assertFalse(f.cpuBusy(), "The crafting job should finish" + f.why());
                helper.assertFalse(f.requesting(), "Nothing should be requested any more" + f.why());
                helper.assertEquals(4L, f.stored(f.output), "The four outputs should be stored" + f.why());
                helper.assertEquals(STOCK - 4, f.stored(f.input), "Two pushed and two refilled runs" + f.why());
                f.assertSettled();
            })
            .thenSucceed();
    }

    // With a real 50% chance the job still finishes, and nothing beyond the request is made.
    @GameTest(template = "sifter", timeoutTicks = 2400)
    public static void craftingJobFinishesUnderRealChance(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 11, 1, 5000);
        AtomicReference<ICraftingLink> link = new AtomicReference<>();

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, f::assertCraftingReady)
            .thenExecute("install the chance pattern", () -> f.install(f.pattern(1, 1, true)))
            .thenWaitUntil(
                "wait for the pattern to be offered",
                80,
                () -> helper.assertTrue(f.offered(), "The pattern should be offered"))
            .thenExecute("request six outputs", () -> link.set(f.submit(6)))
            .thenWaitUntil("wait for the job to finish", 2200, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + f.why());
                helper.assertFalse(f.cpuBusy(), "The crafting job should finish" + f.why());
                helper.assertFalse(f.requesting(), "Nothing should be requested any more" + f.why());
                helper.assertEquals(6L, f.stored(f.output), "Exactly the six outputs should be stored" + f.why());
                helper.assertTrue(f.stored(f.input) <= STOCK - 6, "At least six runs should be used" + f.why());
                f.assertSettled();
            })
            .thenSucceed();
    }

    // A large order whose runs roll far below the pattern's promise needs many refills, and still finishes.
    @GameTest(template = "sifter", timeoutTicks = 2700)
    public static void largeOrderUnderLowChanceFinishes(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 12, 1, 2500, 2048);
        AtomicReference<ICraftingLink> link = new AtomicReference<>();

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, f::assertCraftingReady)
            .thenExecute("install the chance pattern", () -> f.install(f.pattern(1, 1, true)))
            .thenWaitUntil(
                "wait for the pattern to be offered",
                80,
                () -> helper.assertTrue(f.offered(), "The pattern should be offered"))
            .thenExecute("request 64 outputs", () -> link.set(f.submit(64)))
            .thenWaitUntil("wait for the job to finish", 2400, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + f.why());
                helper.assertFalse(f.cpuBusy(), "The crafting job should finish" + f.why());
                helper.assertFalse(f.requesting(), "Nothing should be requested any more" + f.why());
                helper.assertEquals(64L, f.stored(f.output), "Exactly the 64 outputs should be stored" + f.why());
                helper.assertEquals(0L, f.deposit(), "No refill should overshoot the debt" + f.why());
                f.assertSettled();
            })
            .thenSucceed();
    }

    // GT's own gravel recipe rolls six flint slots, and all of them count toward the pattern's single flint entry.
    // The pattern promises the expected yield, so short runs are refilled and long runs go to the deposit.
    @GameTest(template = "gravel", timeoutTicks = 2400)
    public static void realGravelSiftingFillsAnOrder(GameTestHelper helper) {
        Fixture f = new Fixture(helper, new ItemStack(Blocks.gravel), new ItemStack(Items.flint), 1024, true);
        AtomicReference<ICraftingLink> link = new AtomicReference<>();

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, f::assertCraftingReady)
            .thenExecute("install a pattern of one gravel for four flint", () -> f.install(f.pattern(1, 4, true)))
            .thenWaitUntil(
                "wait for the pattern to be offered",
                80,
                () -> helper.assertTrue(f.offered(), "The pattern should be offered"))
            .thenExecute("request 40 flint", () -> link.set(f.submit(40)))
            .thenWaitUntil("wait for the job to finish", 2200, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + f.why());
                helper.assertFalse(f.cpuBusy(), "The crafting job should finish" + f.why());
                helper.assertFalse(f.requesting(), "Nothing should be requested any more" + f.why());
                f.assertSettled();
                helper.assertEquals(40L, f.stored(f.output), "Storage should hold exactly the order" + f.why());
                helper.assertTrue(f.onTheWay() == 0, "Every surplus should have been taken in" + f.why());
                ChanceView.Entry e = f.entry();
                helper.assertNotNull(e, "The controller should keep totals for flint");
                long runs = f.stock - f.stored(f.input);
                helper.assertTrue(
                    Math.abs(e.recipeExpected - runs * 3.88) < 1e-6,
                    "The recipe expects 3.88 flint per run, saw " + e.recipeExpected + " for " + runs + f.why());
                helper.assertEquals(40 + f.deposit(), e.produced, "Every flint rolled should be counted" + f.why());
            })
            .thenExecute("return the deposits", () -> {
                f.chanceController()
                    .returnDeposits();
                helper.assertEquals(0L, f.deposit(), "The deposits should be given back" + f.why());
                helper.assertTrue(f.stored(f.output) >= 40, "Storage should hold the order plus deposits" + f.why());
            })
            .thenSucceed();
    }

    // With refills off, a short run leaves its expected amount standing and nothing is taken from storage.
    @GameTest(template = "sifter", timeoutTicks = 600)
    public static void refillOffLeavesTheExpectedAmount(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 15, 1, 10000);
        ICraftingPatternDetails pattern = f.details(1, 2, true);

        helper.startSequence()
            .thenWaitUntil("wait for the ME network", 100, f::assertNetworkActive)
            .thenExecute("switch refills off and push one copy that expects two", () -> {
                f.chanceController()
                    .setMode(TileChanceController.RefillMode.NONE);
                helper.assertTrue(f.push(pattern, 1), "The hatch should accept the push");
            })
            .thenWaitUntil(
                "wait for the run",
                300,
                () -> helper.assertEquals(1L, f.stored(f.output), "The run should make one output" + f.why()))
            .thenExecuteFor(100, () -> {
                helper.assertEquals(STOCK, f.stored(f.input), "Nothing should be taken from storage" + f.why());
                helper.assertEquals(1L, f.owed(), "One output should stay expected" + f.why());
            })
            .thenSucceed();
    }

    // In crafting mode the missing input is ordered from the crafting system, held, and refilled once it arrived.
    // The input itself is made by a second, plain pattern on the same sifter; storage holds only its raw material.
    @GameTest(template = "craft", timeoutTicks = 2600)
    public static void craftingModeOrdersTheInputs(GameTestHelper helper) {
        Fixture f = new Fixture(helper, 13, 1, 10000);
        ItemStack raw = new ItemStack(Blocks.wool, 1, 14);
        helper.gtnh()
            .withTestRecipe(
                f.sifter,
                GTValues.RA.stdBuilder()
                    .itemInputs(raw.copy())
                    .itemOutputs(f.input.copy())
                    .duration(20)
                    .eut(TierEU.RECIPE_LV));
        AtomicReference<ICraftingLink> link = new AtomicReference<>();

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPUs", 120, f::assertCraftingReady)
            .thenExecute("stock the raw material, install both patterns, switch to crafting refills", () -> {
                f.drain(f.input);
                f.put(raw, STOCK);
                f.install(0, f.pattern(1, 2, true));
                f.install(1, f.patternOf(raw, 1, f.input, 1, false));
                f.chanceController()
                    .setMode(TileChanceController.RefillMode.CRAFT);
            })
            .thenWaitUntil(
                "wait for the patterns to be offered",
                80,
                () -> helper.assertTrue(f.offered(), "The chance pattern should be offered"))
            .thenExecute("request four outputs", () -> link.set(f.submit(4)))
            .thenWaitUntil("wait for the job to finish", 2400, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + f.why());
                helper.assertFalse(f.cpuBusy(), "Every crafting job should finish" + f.why());
                helper.assertFalse(f.requesting(), "Nothing should be requested any more" + f.why());
                helper.assertEquals(4L, f.stored(f.output), "The four outputs should be stored" + f.why());
                helper.assertEquals(STOCK - 4, f.stored(raw), "Two pushed and two refilled inputs" + f.why());
                helper.assertEquals(0L, f.stored(f.input), "No input should be left in storage" + f.why());
                helper.assertEquals(0L, f.heldOf(f.input), "No input should be left in the controller" + f.why());
                f.assertSettled();
            })
            .thenSucceed();
    }

    // A multiplied pattern promises and consumes multiplied amounts; one copy of it is two runs here.
    @GameTest(template = "sifter", timeoutTicks = 1200)
    public static void multipliedChancePatternIsTracked(GameTestHelper helper) {
        Fixture f = new Fixture(helper, new ItemStack(Blocks.stained_glass, 1, 0), 1, 10000, STOCK, false);
        AtomicReference<ICraftingLink> link = new AtomicReference<>();

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, f::assertCraftingReady)
            .thenExecute("install the chance pattern doubled", () -> {
                f.install(f.pattern(1, 2, true));
                f.hatch.setPatternMultiplier(0, 2);
            })
            .thenWaitUntil(
                "wait for the pattern to be offered",
                80,
                () -> helper.assertTrue(f.offered(), "The pattern should be offered"))
            .thenExecute("request four outputs", () -> link.set(f.submit(4)))
            .thenWaitUntil("wait for the job to finish", 1000, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + f.why());
                helper.assertFalse(f.cpuBusy(), "The crafting job should finish" + f.why());
                helper.assertEquals(4L, f.stored(f.output), "The four outputs should be stored" + f.why());
                helper.assertEquals(STOCK - 4, f.stored(f.input), "Two pushed and two refilled runs" + f.why());
                f.assertSettled();
                ChanceView.Entry e = f.entry();
                helper.assertNotNull(e, "The controller should keep totals");
                helper.assertTrue(
                    Math.abs(e.refillCopies - 1) < 1e-9 && Math.abs(e.refillItems - 4) < 1e-9,
                    "Two refilled runs are one doubled copy promising four, saw " + e.refillCopies + " copies, "
                        + e.refillItems + " expected" + f.why());
                helper.assertTrue(
                    Math.abs(e.planned - 8) < 1e-9 && e.produced == 4,
                    "Four runs promise eight at the doubled rate and rolled four, saw " + e.planned + " and "
                        + e.produced + f.why());
            })
            .thenSucceed();
    }

    // A marked pattern inside a cyclic pattern hatch is booked when its runs are handed to the buffer and refilled.
    @GameTest(template = "sifter", timeoutTicks = 1600)
    public static void cyclicHatchTracksChancePatterns(GameTestHelper helper) {
        Fixture f = new Fixture(helper, new ItemStack(Blocks.stained_glass, 1, 1), 1, 10000, STOCK, true);
        AtomicReference<ICraftingLink> link = new AtomicReference<>();

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, f::assertCraftingReady)
            .thenExecute("install the chance pattern as a cycle member", () -> f.install(f.pattern(1, 2, true)))
            .thenWaitUntil(
                "wait for the balancing to be offered",
                400,
                () -> helper.assertTrue(f.offered(), "The cyclic hatch should offer the output"))
            .thenExecute("request four outputs", () -> link.set(f.submit(4)))
            .thenWaitUntil("wait for the job to finish", 1000, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + f.why());
                helper.assertFalse(f.cpuBusy(), "The crafting job should finish" + f.why());
                helper.assertEquals(4L, f.stored(f.output), "The four outputs should be stored" + f.why());
                helper.assertEquals(STOCK - 4, f.stored(f.input), "Two pushed and two refilled runs" + f.why());
                helper.assertFalse(f.hatch.isBusy(), "The cyclic hatch should have handed out every dose" + f.why());
                f.assertSettled();
                ChanceView.Entry e = f.entry();
                helper.assertNotNull(e, "The controller should keep totals");
                helper.assertTrue(
                    Math.abs(e.refillCopies - 2) < 1e-9 && e.produced == 4,
                    "Two runs should be refilled and four rolled, saw " + e.refillCopies + " and " + e.produced
                        + f.why());
            })
            .thenSucceed();
    }

    // An input the cyclic hatch is owed as a seed still reaches the controller's order: the CPU running that order
    // is served before the seed interceptor, so the refill goes through and the seed stays owed.
    @GameTest(template = "craft", timeoutTicks = 2600)
    public static void controllerOrdersAreServedBeforeSeeds(GameTestHelper helper) {
        Fixture f = new Fixture(helper, new ItemStack(Blocks.stained_glass, 1, 2), 1, 10000, STOCK, true);
        ItemStack raw = new ItemStack(Blocks.stained_glass, 1, 3);
        helper.gtnh()
            .withTestRecipe(
                f.sifter,
                GTValues.RA.stdBuilder()
                    .itemInputs(raw.copy())
                    .itemOutputs(f.input.copy())
                    .duration(20)
                    .eut(TierEU.RECIPE_LV));
        AtomicReference<ICraftingLink> link = new AtomicReference<>();
        Object[] seed = { null };

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPUs", 120, f::assertCraftingReady)
            .thenExecute("stock the raw material, install both patterns, switch to crafting refills", () -> {
                f.put(raw, STOCK);
                f.install(0, f.pattern(1, 2, true));
                f.install(1, f.patternOf(raw, 1, f.input, 1, false));
                f.chanceController()
                    .setMode(TileChanceController.RefillMode.CRAFT);
            })
            .thenWaitUntil(
                "wait for both balancings to be offered",
                400,
                () -> helper.assertTrue(f.offered() && f.offered(f.input), "Both patterns should be offered"))
            .thenExecute("owe the input as a seed, so the hatch takes it back from the network", () -> {
                seed[0] = seedOwed(f.hatch, f.input, 1000);
                f.put(f.input, 1);
                helper.assertEquals(999L, seedField(seed[0], "owed"), "The hatch should take a loose input as a seed");
                helper.assertEquals(STOCK, f.stored(f.input), "The seed should stay out of storage");
            })
            .thenExecute("request four outputs", () -> link.set(f.submit(4)))
            .thenWaitUntil("wait for the job to finish", 2400, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + f.why());
                helper.assertFalse(f.cpuBusy(), "Every crafting job should finish" + f.why());
                helper.assertEquals(4L, f.stored(f.output), "The four outputs should be stored" + f.why());
                helper.assertEquals(STOCK - 2, f.stored(f.input), "Two pushed runs come from storage" + f.why());
                helper.assertEquals(STOCK - 2, f.stored(raw), "Two refilled runs are crafted from raw" + f.why());
                helper.assertEquals(0L, f.heldOf(f.input), "No input should be left in the controller" + f.why());
                helper.assertEquals(
                    999L,
                    seedField(seed[0], "owed"),
                    "The ordered inputs should pass the seed interceptor" + f.why());
                f.assertSettled();
            })
            .thenSucceed();
    }

    // Auto seed keeps one craft's seeds for a plain cycle and four crafts' worth for a cycle that runs a chance pattern.
    @GameTest(template = "sifter", timeoutTicks = 600)
    public static void autoSeedKeepsOneCraftForPlainCycles(GameTestHelper helper) {
        autoSeedCase(helper, 4, 6, false, 1);
    }

    @GameTest(template = "sifter", timeoutTicks = 600)
    public static void autoSeedKeepsFourCraftsForChanceCycles(GameTestHelper helper) {
        autoSeedCase(helper, 5, 7, true, 4);
    }

    /** A cycle of two patterns in one group: raw and input make two outputs, an output makes an input. */
    private static void autoSeedCase(GameTestHelper helper, int inputMeta, int rawMeta, boolean chance, long expect) {
        Fixture f = new Fixture(helper, new ItemStack(Blocks.stained_glass, 1, inputMeta), 1, 10000, STOCK, true);
        ItemStack raw = new ItemStack(Blocks.stained_glass, 1, rawMeta);

        helper.startSequence()
            .thenWaitUntil("wait for the ME network", 120, f::assertNetworkActive)
            .thenExecute("install both members of the cycle in the first group", () -> {
                f.install(0, f.patternOf(new ItemStack[] { raw, sized(f.input, 1) }, f.output, 2, chance));
                f.install(4, f.patternOf(f.output, 1, f.input, 1, false));
            })
            .thenWaitUntil("wait for the seed targets and the balancing", 400, () -> {
                // Both cyclic items are products by default; the balancing for net input runs the output pattern
                // twice, and its two output seeds set the output's target.
                helper.assertEquals(expect, seedTarget(f.hatch, f.input), "The input's seed target");
                helper.assertEquals(2 * expect, seedTarget(f.hatch, f.output), "The output's seed target");
                helper.assertTrue(f.offered() && f.offered(f.input), "Both balancings should be published");
            })
            .thenSucceed();
    }

    /** The seed target the cyclic hatch's first group keeps for an item; -1 while the item is no cyclic item. */
    private static long seedTarget(PatternDualInputHatch hatch, ItemStack item) {
        try {
            Class<?> cyc = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch");
            Class<?> key = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$SKey");
            Method of = key.getDeclaredMethod("of", IAEStack.class);
            of.setAccessible(true);
            Field settings = cyc.getDeclaredField("settings");
            settings.setAccessible(true);
            Object s = ((Map<?, ?>) ((List<?>) settings.get(hatch)).get(0)).get(of.invoke(null, ae(item, 1)));
            if (s == null) return -1;
            Field t = s.getClass()
                .getDeclaredField("target");
            t.setAccessible(true);
            return t.getLong(s);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("The cyclic hatch's settings should be reachable", e);
        }
    }

    /** Books a seed the cyclic hatch is owed, so its interceptor takes that item from the network. */
    private static Object seedOwed(PatternDualInputHatch hatch, ItemStack seed, long owed) {
        try {
            Class<?> cyc = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch");
            Class<?> key = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$SKey");
            Class<?> state = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$SeedState");
            Method of = key.getDeclaredMethod("of", IAEStack.class);
            of.setAccessible(true);
            Constructor<?> make = state.getDeclaredConstructor();
            make.setAccessible(true);
            Object st = make.newInstance();
            Field f = state.getDeclaredField("owed");
            f.setAccessible(true);
            f.setLong(st, owed);
            Field seeds = cyc.getDeclaredField("seeds");
            seeds.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) seeds.get(hatch);
            map.put(of.invoke(null, ae(seed, 1)), st);
            for (String name : new String[] { "recountOwed", "registerInterceptor" }) {
                Method m = cyc.getDeclaredMethod(name);
                m.setAccessible(true);
                m.invoke(hatch);
            }
            return st;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("The cyclic hatch's seeds should be reachable", e);
        }
    }

    private static long seedField(Object state, String name) {
        try {
            Field f = state.getClass()
                .getDeclaredField(name);
            f.setAccessible(true);
            return f.getLong(state);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("The seed state should be readable", e);
        }
    }

    /** One built sifter with its network, its own input item and a trace of every change. */
    private static final class Fixture {

        final GameTestHelper helper;
        final ItemStack input;
        final ItemStack output;
        final long stock;
        final Multiblock sifter;
        final PatternDualInputHatch hatch;
        final List<String> trace = new ArrayList<>();

        Fixture(GameTestHelper helper, int color, int outputs, int chance) {
            this(helper, color, outputs, chance, STOCK);
        }

        Fixture(GameTestHelper helper, int color, int outputs, int chance, long stock) {
            this(helper, new ItemStack(Blocks.wool, 1, color), outputs, chance, stock, false);
        }

        /** Sifts the input into lapis blocks with a test recipe of its own, on a pattern hatch or a cyclic one. */
        Fixture(GameTestHelper helper, ItemStack input, int outputs, int chance, long stock, boolean cyclic) {
            this(helper, input, new ItemStack(Blocks.lapis_block), stock, false, cyclic);
            helper.gtnh()
                .withTestRecipe(
                    sifter,
                    GTValues.RA.stdBuilder()
                        .itemInputs(input.copy())
                        .itemOutputs(sized(output, outputs))
                        .outputChances(chance)
                        .duration(20)
                        .eut(TierEU.RECIPE_LV));
        }

        Fixture(GameTestHelper helper, ItemStack input, ItemStack output, long stock, boolean creative) {
            this(helper, input, output, stock, creative, false);
        }

        /** With creative set, a debug energy hatch and a creative maintenance hatch keep the sifter running alone. */
        Fixture(GameTestHelper helper, ItemStack input, ItemStack output, long stock, boolean creative,
            boolean cyclic) {
            this.helper = helper;
            this.input = input;
            this.output = output;
            this.stock = stock;
            this.sifter = buildSifter(helper, creative, cyclic);
            hatch = helper.assertInstanceOf(
                PatternDualInputHatch.class,
                helper.gtnh()
                    .metaTileEntity(HATCH),
                "The pattern hatch should be placed");
            helper.setSlot("drive", 0, filledCell());
            startTrace();
        }

        ItemStack pattern(int inputs, int outputs, boolean chance) {
            return patternOf(input, inputs, output, outputs, chance);
        }

        ItemStack patternOf(ItemStack from, int inputs, ItemStack to, int outputs, boolean chance) {
            return patternOf(new ItemStack[] { sized(from, inputs) }, to, outputs, chance);
        }

        ItemStack patternOf(ItemStack[] from, ItemStack to, int outputs, boolean chance) {
            ItemStack pattern = AEApi.instance()
                .definitions()
                .items()
                .encodedPattern()
                .maybeStack(1)
                .get();
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList in = new NBTTagList();
            NBTTagList out = new NBTTagList();
            for (ItemStack s : from) in.appendTag(itemTag(s));
            out.appendTag(itemTag(sized(to, outputs)));
            tag.setTag("in", in);
            tag.setTag("out", out);
            tag.setBoolean("crafting", false);
            tag.setBoolean("substitute", false);
            tag.setBoolean("beSubstitute", false);
            if (chance) tag.setBoolean(ChanceMark.KEY, true);
            pattern.setTagCompound(tag);
            return pattern;
        }

        ICraftingPatternDetails details(int inputs, int outputs, boolean chance) {
            ItemStack pattern = pattern(inputs, outputs, chance);
            ICraftingPatternDetails details = ((ICraftingPatternItem) pattern.getItem())
                .getPatternForItem(pattern, helper.getWorld());
            helper.assertNotNull(details, "The encoded pattern should decode");
            return details;
        }

        /** What the crafting CPU hands the hatch when it pushes the pattern once. */
        boolean push(ICraftingPatternDetails pattern, int inputs) {
            InventoryCrafting table = new InventoryCrafting(new ContainerNull(), 3, 3);
            table.setInventorySlotContents(0, sized(input, inputs));
            return hatch.pushPattern(pattern, table);
        }

        void install(ItemStack pattern) {
            install(0, pattern);
        }

        void install(int slot, ItemStack pattern) {
            hatch.getPatterns()
                .setInventorySlotContents(slot, pattern);
        }

        List<ChanceData> chances() {
            List<ChanceData> list = new ArrayList<>();
            for (DualInvBuffer b : hatch.inv0) if (b.chance != null) list.add(b.chance);
            return list;
        }

        long owed() {
            long sum = 0;
            for (ChanceData c : chances()) {
                int j = ChanceData.indexOf(c.outputs, ae(output, 1));
                if (j >= 0) sum += c.debt[j];
            }
            return sum;
        }

        void assertSettled() {
            for (ChanceData c : chances())
                helper.assertFalse(c.owed() || c.pending, "A buffer still owes outputs or waits for a refill" + why());
        }

        TileChanceController chanceController() {
            return helper.assertTileEntityPresent(TileChanceController.class, "chance_controller");
        }

        void assertNetworkActive() {
            IGridNode node = helper.assertInstanceOf(IGridHost.class, helper.assertTileEntityPresent("controller"))
                .getGridNode(ForgeDirection.UNKNOWN);
            helper.assertTrue(node != null && node.isActive(), "The ME controller should be active");
            helper.assertTrue(
                chanceController().getProxy()
                    .isActive(),
                "The chance controller should be on the network");
            helper.assertTrue(
                hatch.getProxy()
                    .isActive(),
                "The pattern hatch should be on the network");
        }

        void assertCraftingReady() {
            assertNetworkActive();
            helper.assertTrue(
                helper.assertTileEntityPresent(TileCraftingTile.class, "cpu_storage")
                    .isFormed(),
                "The crafting CPU should form");
        }

        long stored(ItemStack stack) {
            try {
                IAEItemStack s = chanceController().getProxy()
                    .getStorage()
                    .getItemInventory()
                    .extractItems(ae(stack, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE);
                return s == null ? 0 : s.getStackSize();
            } catch (GridAccessException e) {
                throw new AssertionError("The network storage should be reachable", e);
            }
        }

        long deposit() {
            return ChanceData.amountOf(chanceController().deposits(), ae(output, 1));
        }

        long onTheWay() {
            return ChanceData.amountOf(chanceController().pending(), ae(output, 1));
        }

        ChanceView.Entry entry() {
            for (ChanceView.Entry e : chanceController().screen().entries)
                if (e.shown.item != null && ItemStack.areItemStacksEqual(e.shown.item, sized(output, 1))) return e;
            return null;
        }

        long heldOf(ItemStack stack) {
            return ChanceData.amountOf(chanceController().held(), ae(stack, 1));
        }

        void drain(ItemStack stack) {
            try {
                chanceController().getProxy()
                    .getStorage()
                    .getItemInventory()
                    .extractItems(ae(stack, Long.MAX_VALUE), Actionable.MODULATE, SOURCE);
            } catch (GridAccessException e) {
                throw new AssertionError("The network storage should be reachable", e);
            }
        }

        void put(ItemStack stack, long amount) {
            try {
                helper.assertNull(
                    chanceController().getProxy()
                        .getStorage()
                        .getItemInventory()
                        .injectItems(ae(stack, amount), Actionable.MODULATE, SOURCE),
                    "Storage should take the stock");
            } catch (GridAccessException e) {
                throw new AssertionError("The network storage should be reachable", e);
            }
        }

        @SuppressWarnings("unchecked")
        ItemStack filledCell() {
            ItemStack cell = AEApi.instance()
                .definitions()
                .items()
                .cell64k()
                .maybeStack(1)
                .get();
            IMEInventoryHandler<IAEItemStack> inv = AEApi.instance()
                .registries()
                .cell()
                .getHandler(cell)
                .getCellInventory(cell, null, ITEM_STACK_TYPE);
            helper.assertNull(
                inv.injectItems(ae(input, stock), Actionable.MODULATE, SOURCE),
                "The cell should take the stock");
            return cell;
        }

        ICraftingGrid crafting() {
            try {
                return chanceController().getProxy()
                    .getCrafting();
            } catch (GridAccessException e) {
                throw new AssertionError("The crafting grid should be reachable", e);
            }
        }

        boolean offered() {
            return offered(output);
        }

        boolean offered(ItemStack stack) {
            return !crafting().getCraftingFor(ae(stack, 1), null, -1, helper.getWorld())
                .isEmpty();
        }

        boolean requesting() {
            return crafting().isRequesting(ae(output, 1));
        }

        /** A standalone job's link is never marked done, so the CPU itself tells when the job is over. */
        boolean cpuBusy() {
            for (ICraftingCPU cpu : crafting().getCpus()) if (cpu.isBusy()) return true;
            return false;
        }

        ICraftingLink submit(long amount) {
            TileChanceController c = chanceController();
            Future<ICraftingJob> future = null;
            try {
                future = crafting().beginCraftingJob(
                    c.getWorldObj(),
                    c.getProxy()
                        .getGrid(),
                    SOURCE,
                    ae(output, amount),
                    null);
                ICraftingJob job = future.get(5000, TimeUnit.MILLISECONDS);
                helper.assertFalse(job.isSimulation(), "The crafting job should be executable");
                ICraftingLink link = crafting().submitJob(job, null, null, false, SOURCE);
                helper.assertNotNull(link, "The crafting job should start on the CPU");
                return link;
            } catch (InterruptedException e) {
                Thread.currentThread()
                    .interrupt();
                throw new AssertionError("The crafting calculation was interrupted", e);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new AssertionError("The crafting calculation did not finish", e);
            } catch (Exception e) {
                throw new AssertionError("The crafting job should calculate and start", e);
            }
        }

        /** Every change of the buffers, the books, the stock, the deposit and the request, one line per tick. */
        private void startTrace() {
            String[] last = { "" };
            int[] tick = { 0 };
            helper.onEachTick("trace the chance books", () -> {
                tick[0]++;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < hatch.inv0.size(); i++) {
                    DualInvBuffer b = hatch.inv0.get(i);
                    List<IAEStack<?>> contents = b.chanceContents();
                    if (contents.isEmpty() && b.chance == null) continue;
                    sb.append(" b")
                        .append(i)
                        .append(contents);
                    if (b.chance != null) sb.append(" owe=")
                        .append(Arrays.toString(b.chance.debt))
                        .append(b.chance.pending ? " pending" : "")
                        .append(" refills=")
                        .append(b.chance.refilledRuns);
                }
                sb.append(" | stock=")
                    .append(stored(input))
                    .append(" out=")
                    .append(stored(output))
                    .append(" deposit=")
                    .append(deposit())
                    .append(" way=")
                    .append(onTheWay())
                    .append(" held=")
                    .append(heldOf(input))
                    .append(requesting() ? " requesting" : "")
                    .append(cpuBusy() ? " cpu-busy" : "");
                String now = sb.toString();
                if (!now.equals(last[0])) {
                    trace.add("t" + tick[0] + now);
                    last[0] = now;
                }
            });
        }

        String why() {
            int from = Math.max(0, trace.size() - 120);
            return (from > 0 ? "\n(" + from + " earlier lines)" : "") + "\n"
                + String.join("\n", trace.subList(from, trace.size()));
        }
    }

    private static Multiblock buildSifter(GameTestHelper helper, boolean creative, boolean cyclic) {
        IGregTechTileEntity controller = place(helper, SIFTER, ItemList.LargeSifter, ForgeDirection.NORTH);
        ((IConstructable) controller.getMetaTileEntity()).construct(ItemList.LargeSifter.get(1), false);
        place(
            helper,
            HATCH,
            Config.metaTileEntityOffset + (cyclic ? Registration.CyclicPatternOffset : Registration.PatternOffset),
            ForgeDirection.WEST);
        place(helper, OUTPUT_BUS, ItemList.Hatch_Output_Bus_ME, ForgeDirection.WEST);
        place(helper, ENERGY, creative ? ItemList.DebugEnergyHatch : ItemList.Hatch_Energy_LV, ForgeDirection.NORTH);
        place(
            helper,
            MAINTENANCE,
            creative ? CustomItemList.hatch_CreativeMaintenance : ItemList.Hatch_Maintenance,
            ForgeDirection.NORTH);
        place(helper, MUFFLER, ItemList.Hatch_Muffler_LV, ForgeDirection.UP);
        if (creative) {
            MTEHatchEnergyDebug energy = (MTEHatchEnergyDebug) helper.gtnh()
                .gtTile(ENERGY)
                .getMetaTileEntity();
            energy.setVoltageTier(3);
            energy.setAmperage(2);
            energy.setEUVar(Long.MAX_VALUE);
        }

        Multiblock sifter = helper.gtnh()
            .multiblock(SIFTER);
        helper.assertTrue(sifter.forceStructureCheck(), "The Large Sifter should form");
        if (!creative) helper.onEachTick("keep the sifter powered and maintained", () -> {
            MTEMultiBlockBase m = helper.gtnh()
                .multiBlockController(SIFTER);
            m.mWrench = m.mScrewdriver = m.mSoftMallet = m.mHardHammer = m.mSolderingTool = m.mCrowbar = true;
            IGregTechTileEntity energy = helper.gtnh()
                .gtTile(ENERGY);
            energy.increaseStoredEnergyUnits(energy.getEUCapacity() - energy.getStoredEU(), true);
        });
        return sifter;
    }

    private static IGregTechTileEntity place(GameTestHelper helper, TestPos pos, IItemContainer item,
        ForgeDirection facing) {
        return place(helper, pos, item.get(1)
            .getItemDamage(), facing);
    }

    private static IGregTechTileEntity place(GameTestHelper helper, TestPos pos, int id, ForgeDirection facing) {
        TestPos abs = helper.absolute(pos);
        World world = helper.getWorld();
        world.setBlock(
            abs.x(),
            abs.y(),
            abs.z(),
            GregTechAPI.sBlockMachines,
            GregTechAPI.METATILEENTITIES[id].getTileEntityBaseType(),
            3);
        IGregTechTileEntity te = (IGregTechTileEntity) world.getTileEntity(abs.x(), abs.y(), abs.z());
        te.setInitialValuesAsNBT(null, (short) id);
        te.setFrontFacing(facing);
        return te;
    }

    private static ItemStack sized(ItemStack stack, int size) {
        ItemStack s = stack.copy();
        s.stackSize = size;
        return s;
    }

    private static NBTTagCompound itemTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        Platform.writeItemStackToNBT(stack, tag);
        return tag;
    }

    private static IAEItemStack ae(ItemStack stack, long amount) {
        IAEItemStack s = AEItemStack.create(stack);
        s.setStackSize(amount);
        return s;
    }
}
