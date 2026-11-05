package reobf.proghatches.gametests;

import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;
import static com.gtnewhorizons.horizonqa.api.TestPos.at;
import static reobf.proghatches.gametests.ChancePatternTests.ae;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import appeng.tile.crafting.TileCraftingTile;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.TierEU;
import reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.cycle.CycleAmount;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/**
 * A Large Chemical Reactor fed by a cyclic pattern hatch: one A and a B make two A, an A and a D make a C. The
 * published pattern turns a B and a D into a C on one seed of A; each case uses its own clay colors.
 */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class CyclicGrowthTests {

    private static final long ORDER = 16;
    private static final BaseActionSource SOURCE = new BaseActionSource();

    @GameTest(template = "palladium", timeoutTicks = 3000)
    public static void fullGrowthMultipliesTheSeedFirst(GameTestHelper helper) {
        Line line = new Line(helper, 0, 0);
        line.order(100, () -> helper.assertTrue(
            line.peakStock > ORDER / 2,
            "The seed should multiply before the conversions, peak " + line.peakStock + line.why()));
    }

    @GameTest(template = "palladium", timeoutTicks = 3000)
    public static void zeroGrowthTakesOneCraftAtATime(GameTestHelper helper) {
        Line line = new Line(helper, 4, 0);
        line.order(0, () -> helper.assertTrue(
            line.peakStock <= 2,
            "One craft at a time should keep at most two seeds, peak " + line.peakStock + line.why()));
    }

    @GameTest(template = "palladium", timeoutTicks = 3000)
    public static void growthTargetBorrowsFromMe(GameTestHelper helper) {
        Line line = new Line(helper, 8, 64);
        line.order(50, () -> helper.assertTrue(
            line.peakBorrowed > 0,
            "The growth target should borrow seeds from ME" + line.why()));
    }

    // The growth run waits on an unbound lane, so the conversion of its round waits as well although the stock above
    // the growth reserve would pay it. Returning seeds keeps what they still take, and the stranded lane's refund
    // takes the whole round back to ME.
    @GameTest(template = "palladium", timeoutTicks = 600)
    public static void strandedGrowthRefundsItsWholeBalancing(GameTestHelper helper) {
        Line line = new Line(helper, 12, 0);
        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, line::assertReady)
            .thenExecute("queue one round and return the seeds", () -> {
                line.queueRound(2, 1);
                line.invoke("returnSeeds");
                helper.assertEquals(2L, line.seed("stock"), "The queued round should keep its seeds" + line.why());
                helper.assertEquals(1L, line.stored(line.a), "No seed should leave for ME" + line.why());
            })
            .thenWaitUntil("wait for the stranded lane's refund", 500, () -> {
                helper.assertEquals(0, ((Collection<?>) line.get("doses")).size(), "Both doses should be refunded" + line.why());
                helper.assertEquals(2L, line.seed("stock"), "The seeds should stay" + line.why());
                helper.assertEquals(0L, line.seed("owed"), "No seed should stay owed" + line.why());
                helper.assertEquals(ORDER + 1, line.stored(line.b), "The growth input should be back in ME" + line.why());
                helper.assertEquals(ORDER + 1, line.stored(line.d), "The conversion input should be back in ME" + line.why());
                helper.assertEquals(0L, line.stored(line.c), "No conversion should run" + line.why());
            })
            .thenSucceed();
    }

    // The growth run's lane holds fewer items than the run; the conversion of its round stays queued although the
    // stock above the growth reserve would pay it, and the round goes back to ME.
    @GameTest(template = "palladium", timeoutTicks = 400)
    public static void unfitGrowthRefundsItsWholeBalancing(GameTestHelper helper) {
        Line line = new Line(helper, Blocks.wool, 0, 0);
        long big = 100_000;
        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, line::assertReady)
            .thenExecute("queue a round whose growth run does not fit its lane, then hand it out", () -> {
                line.bindSmallLane(3);
                line.queueRound(2, big);
                line.invoke("drainDoses");
                helper.assertEquals(0, ((Collection<?>) line.get("doses")).size(), "Both doses should be refunded" + line.why());
                helper.assertEquals(2L, line.seed("stock"), "Both seeds should stay" + line.why());
                helper.assertEquals(0L, line.seed("owed"), "No seed should stay owed" + line.why());
                helper.assertEquals(ORDER + big, line.stored(line.b), "The growth input should be back in ME" + line.why());
                helper.assertEquals(ORDER + 1, line.stored(line.d), "The conversion input should be back in ME" + line.why());
            })
            .thenSucceed();
    }

    static final class Line {

        final GameTestHelper helper;
        final ItemStack a, b, d, c;
        final long spare;
        final CyclicPatternDualInputHatch cyclic;
        final List<String> trace = new ArrayList<>();
        long peakStock, peakBorrowed;

        Line(GameTestHelper helper, int color, long spare) {
            this(helper, Blocks.stained_hardened_clay, color, spare);
        }

        Line(GameTestHelper helper, Block block, int color, long spare) {
            this.helper = helper;
            this.spare = spare;
            a = new ItemStack(block, 1, color);
            b = new ItemStack(block, 1, color + 1);
            d = new ItemStack(block, 1, color + 2);
            c = new ItemStack(block, 1, color + 3);
            PalladiumLineTests.buildReactor(helper, Config.metaTileEntityOffset + Registration.CyclicPatternOffset);
            cyclic = helper.assertInstanceOf(
                CyclicPatternDualInputHatch.class,
                helper.gtnh()
                    .metaTileEntity(PalladiumLineTests.CYCLIC),
                "The cyclic hatch should be placed");
            helper.gtnh()
                .withTestRecipe(
                    helper.gtnh()
                        .multiblock(PalladiumLineTests.REACTOR),
                    GTValues.RA.stdBuilder()
                        .itemInputs(a.copy(), b.copy())
                        .itemOutputs(ChancePatternTests.sized(a, 2))
                        .duration(20)
                        .eut(TierEU.RECIPE_LV));
            helper.gtnh()
                .withTestRecipe(
                    helper.gtnh()
                        .multiblock(PalladiumLineTests.REACTOR),
                    GTValues.RA.stdBuilder()
                        .itemInputs(a.copy(), d.copy())
                        .itemOutputs(c.copy())
                        .duration(20)
                        .eut(TierEU.RECIPE_LV));
            helper.setSlot("drive", 0, cell());
            startTrace();
        }

        @SuppressWarnings("unchecked")
        ItemStack cell() {
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
            helper.assertNull(inv.injectItems(ae(a, 1 + spare), Actionable.MODULATE, SOURCE), "The cell should take A");
            helper.assertNull(inv.injectItems(ae(b, ORDER), Actionable.MODULATE, SOURCE), "The cell should take B");
            helper.assertNull(inv.injectItems(ae(d, ORDER), Actionable.MODULATE, SOURCE), "The cell should take D");
            return cell;
        }

        void order(int growth, Runnable check) {
            AtomicReference<ICraftingLink> link = new AtomicReference<>();
            helper.startSequence()
                .thenWaitUntil("wait for the ME network and the CPU", 120, this::assertReady)
                .thenExecute("install the growth and the conversion pattern", () -> {
                    setting(ae(a, 1));
                    set("growthPercent", growth);
                    cyclic.getPatterns()
                        .setInventorySlotContents(
                            0,
                            PalladiumLineTests.Line
                                .pattern(new IAEStack<?>[] { ae(a, 1), ae(b, 1) }, new IAEStack<?>[] { ae(a, 2) }, false));
                    cyclic.getPatterns()
                        .setInventorySlotContents(
                            4,
                            PalladiumLineTests.Line
                                .pattern(new IAEStack<?>[] { ae(a, 1), ae(d, 1) }, new IAEStack<?>[] { ae(c, 1) }, false));
                })
                .thenWaitUntil(
                    "wait for the balancing to be offered",
                    600,
                    () -> helper.assertTrue(offered(), "The cycle should offer C" + why()))
                .thenExecute("pull one seed", () -> {
                    invoke("syncSeeds");
                    helper.assertEquals(1L, seed("stock"), "One A should be the seed" + why());
                    helper.assertEquals(spare, stored(a), "The rest of A should stay in ME" + why());
                })
                .thenExecute("order C", () -> link.set(submit(ORDER)))
                .thenWaitUntil("wait for the job to finish", 2700, () -> {
                    helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + why());
                    helper.assertFalse(cpuBusy(), "The crafting job should finish" + why());
                    helper.assertEquals(ORDER, stored(c), "Every C should be in storage" + why());
                    helper.assertEquals(0L, stored(b), "Every B should be used" + why());
                    helper.assertEquals(0L, stored(d), "Every D should be used" + why());
                    helper.assertEquals(spare, stored(a), "Borrowed A should be back in ME" + why());
                    helper.assertEquals(1L, seed("stock"), "The seed should be back" + why());
                    helper.assertEquals(0L, seed("owed"), "No seed should be owed" + why());
                    helper.assertEquals(0L, seed("borrowed"), "No seed should stay borrowed" + why());
                    helper.assertEquals(0, ((Collection<?>) get("doses")).size(), "Every dose should be handed out" + why());
                    check.run();
                })
                .thenSucceed();
        }

        void bindSmallLane(int lane) {
            TestPos pos = at(4, 1, 2);
            ChancePatternTests.place(
                helper,
                pos,
                Config.metaTileEntityOffset + Registration.BufferedDualInputHatchOffset,
                ForgeDirection.WEST);
            TestPos t = helper.absolute(pos), me = helper.absolute(PalladiumLineTests.CYCLIC);
            ((int[][]) get("laneOffset"))[lane] = new int[] { t.x() - me.x(), t.y() - me.y(), t.z() - me.z() };
        }

        @SuppressWarnings("unchecked")
        void queueRound(long seeds, long growthInput) {
            try {
                String outer = "reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch";
                Method of = Class.forName(outer + "$SKey")
                    .getDeclaredMethod("of", IAEStack.class);
                of.setAccessible(true);
                Object key = of.invoke(null, ae(a, 1));
                Constructor<?> state = Class.forName(outer + "$SeedState")
                    .getDeclaredConstructor();
                state.setAccessible(true);
                Object st = state.newInstance();
                Field stock = st.getClass()
                    .getDeclaredField("stock"),
                    owed = st.getClass()
                        .getDeclaredField("owed");
                stock.setAccessible(true);
                owed.setAccessible(true);
                stock.setLong(st, seeds);
                owed.setLong(st, 2);
                ((Map<Object, Object>) get("seeds")).put(key, st);
                Constructor<?> dose = Class.forName(outer + "$Dose")
                    .getDeclaredConstructor(
                        List.class,
                        long.class,
                        int.class,
                        ItemStack.class,
                        boolean.class,
                        boolean.class,
                        List.class,
                        List.class,
                        String.class);
                dose.setAccessible(true);
                List<CycleAmount<Object>> one = List.of(new CycleAmount<>(key, 1)),
                    two = List.of(new CycleAmount<>(key, 2));
                Collection<Object> doses = (Collection<Object>) get("doses");
                doses.add(dose.newInstance(List.of(ae(a, 1), ae(b, growthInput)), 1L, 3, null, true, true, one, two, "test"));
                doses.add(dose.newInstance(List.of(ae(a, 1), ae(d, 1)), 1L, 0, null, true, false, one, List.of(), "test"));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's queue should be reachable", e);
            }
        }

        void assertReady() {
            IGridNode node = helper.assertInstanceOf(IGridHost.class, helper.assertTileEntityPresent("controller"))
                .getGridNode(ForgeDirection.UNKNOWN);
            helper.assertTrue(node != null && node.isActive(), "The ME controller should be active");
            helper.assertTrue(
                cyclic.getProxy()
                    .isActive(),
                "The cyclic hatch should be on the network");
            helper.assertTrue(
                helper.assertTileEntityPresent(TileCraftingTile.class, "cpu_storage")
                    .isFormed(),
                "The crafting CPU should form");
        }

        @SuppressWarnings("unchecked")
        void setting(IAEStack<?> stack) {
            try {
                Class<?> key = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$SKey");
                Method of = key.getDeclaredMethod("of", IAEStack.class);
                of.setAccessible(true);
                Class<?> setting = Class
                    .forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$CycSetting");
                Constructor<?> make = setting.getDeclaredConstructor();
                make.setAccessible(true);
                Object s = make.newInstance();
                Field out = setting.getDeclaredField("output");
                out.setAccessible(true);
                out.setBoolean(s, false);
                ((Map<Object, Object>) ((List<?>) get("settings")).get(0)).put(of.invoke(null, stack), s);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's settings should be reachable", e);
            }
        }

        Object get(String name) {
            try {
                Field f = CyclicPatternDualInputHatch.class.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(cyclic);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's " + name + " should be reachable", e);
            }
        }

        void set(String name, int value) {
            try {
                Field f = CyclicPatternDualInputHatch.class.getDeclaredField(name);
                f.setAccessible(true);
                f.setInt(cyclic, value);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's " + name + " should be reachable", e);
            }
        }

        void invoke(String name) {
            try {
                Method m = CyclicPatternDualInputHatch.class.getDeclaredMethod(name);
                m.setAccessible(true);
                m.invoke(cyclic);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's " + name + " should be reachable", e);
            }
        }

        long seed(String field) {
            try {
                Class<?> key = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$SKey");
                Method of = key.getDeclaredMethod("of", IAEStack.class);
                of.setAccessible(true);
                Object st = ((Map<?, ?>) get("seeds")).get(of.invoke(null, ae(a, 1)));
                if (st == null) return -1;
                Field f = st.getClass()
                    .getDeclaredField(field);
                f.setAccessible(true);
                return f.getLong(st);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's seeds should be reachable", e);
            }
        }

        IStorageGrid storage() {
            try {
                return cyclic.getProxy()
                    .getStorage();
            } catch (GridAccessException e) {
                throw new AssertionError("The network storage should be reachable", e);
            }
        }

        ICraftingGrid crafting() {
            try {
                return cyclic.getProxy()
                    .getCrafting();
            } catch (GridAccessException e) {
                throw new AssertionError("The crafting grid should be reachable", e);
            }
        }

        long stored(ItemStack stack) {
            IAEItemStack s = storage().getItemInventory()
                .extractItems(ae(stack, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE);
            return s == null ? 0 : s.getStackSize();
        }

        boolean offered() {
            return !crafting().getCraftingFor(ae(c, 1), null, -1, helper.getWorld())
                .isEmpty();
        }

        boolean cpuBusy() {
            for (ICraftingCPU cpu : crafting().getCpus()) if (cpu.isBusy()) return true;
            return false;
        }

        ICraftingLink submit(long amount) {
            Future<ICraftingJob> future = null;
            try {
                future = crafting().beginCraftingJob(
                    helper.getWorld(),
                    cyclic.getProxy()
                        .getGrid(),
                    SOURCE,
                    ae(c, amount),
                    null);
                ICraftingJob job = future.get(5000, TimeUnit.MILLISECONDS);
                helper.assertFalse(job.isSimulation(), "The crafting job should be executable" + why());
                ICraftingLink link = crafting().submitJob(job, null, null, false, SOURCE);
                helper.assertNotNull(link, "The crafting job should start on the CPU" + why());
                return link;
            } catch (InterruptedException e) {
                Thread.currentThread()
                    .interrupt();
                throw new AssertionError("The crafting calculation was interrupted", e);
            } catch (Exception e) {
                if (future != null) future.cancel(true);
                throw new AssertionError("The crafting job should calculate and start" + why(), e);
            }
        }

        private void startTrace() {
            String[] last = { "" };
            int[] tick = { 0 };
            helper.onEachTick("trace the growth line", () -> {
                tick[0]++;
                long stock = seed("stock"), borrowed = seed("borrowed");
                peakStock = Math.max(peakStock, stock);
                peakBorrowed = Math.max(peakBorrowed, borrowed);
                String now = " A=" + stored(a)
                    + " B="
                    + stored(b)
                    + " D="
                    + stored(d)
                    + " C="
                    + stored(c)
                    + " seed="
                    + stock
                    + "/"
                    + seed("owed")
                    + "/"
                    + seed("flight")
                    + "/"
                    + borrowed
                    + " doses="
                    + doses()
                    + (cpuBusy() ? " cpu-busy" : "");
                if (!now.equals(last[0])) {
                    trace.add("t" + tick[0] + now);
                    last[0] = now;
                }
            });
        }

        String doses() {
            StringBuilder sb = new StringBuilder("[");
            for (Object dose : (Collection<?>) get("doses")) {
                try {
                    Field runs = dose.getClass()
                        .getDeclaredField("runs"),
                        growth = dose.getClass()
                            .getDeclaredField("growth");
                    runs.setAccessible(true);
                    growth.setAccessible(true);
                    sb.append(growth.getBoolean(dose) ? " g" : " c")
                        .append(runs.getLong(dose));
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError("The cyclic hatch's doses should be readable", e);
                }
            }
            return sb.append(" ]")
                .toString();
        }

        String why() {
            int from = Math.max(0, trace.size() - 60);
            return (from > 0 ? "\n(" + from + " earlier lines)" : "") + "\n"
                + String.join("\n", trace.subList(from, trace.size()));
        }
    }
}
