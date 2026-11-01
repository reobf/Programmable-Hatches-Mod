package reobf.proghatches.gametests;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.container.ContainerNull;
import gregtech.api.util.GTUtility;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import net.minecraft.tileentity.TileEntity;
import reobf.proghatches.gt.metatileentity.bufferutil.ItemStackG;
import reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class CyclicCircuitInputTests {

    @GameTest(template = "palladium", timeoutTicks = 800)
    public static void requiresExactCircuitInputs(GameTestHelper helper) {
        PalladiumLineTests.Line line = new PalladiumLineTests.Line(helper, false);
        helper.startSequence()
            .thenWaitUntil("wait for ME", 120, line::assertReady)
            .thenExecute("install the cycle", line::installCycle)
            .thenWaitUntil("wait for the published cycle", 600,
                () -> helper.assertTrue(line.offered(line.raw), "The cycle should be published"))
            .thenExecute("reject unsupplied circuits and accept the full batch", () -> {
                line.assertCircuitInputs();
                line.syncSeeds();
                ICraftingPatternDetails pattern = line.publishedCycle();
                Future<ICraftingJob> missing = null;
                try {
                    missing = line.crafting().beginCraftingJob(helper.getWorld(), line.cyclic.getProxy().getGrid(),
                        new BaseActionSource(), ChancePatternTests.ae(line.raw, 2), null);
                    helper.assertTrue(missing.get(5000, TimeUnit.MILLISECONDS).isSimulation(),
                        "AE must report missing circuits when neither stock nor a provider can supply them");
                } catch (Exception e) {
                    if (missing != null) missing.cancel(true);
                    throw new AssertionError("The missing-circuit plan should complete", e);
                }
                NBTTagCompound before = saved(line);
                for (String mode : Arrays.asList("missing", "wrong", "short", "unscaled", "extra")) {
                    helper.assertFalse(line.cyclic.pushPattern(pattern, table(pattern, 2, mode)),
                        "The cycle must reject " + mode + " programming circuits");
                    NBTTagCompound after = saved(line);
                    helper.assertEquals(before.getTag("cycSeeds"), after.getTag("cycSeeds"), "Rejected input must preserve seeds");
                    helper.assertEquals(before.getTag("cycDoses"), after.getTag("cycDoses"), "Rejected input must preserve queued doses");
                }
                helper.assertTrue(line.cyclic.pushPattern(pattern, table(pattern, 2, "complete")),
                    "Two fully supplied copies should be accepted without a provider");
                boolean one = false, nine = false;
                for (var buffer : line.cyclic.inv0) for (ItemStackG stored : (ItemStackG[]) field(buffer, "mStoredItemInternal")) {
                    if (stored == null) continue;
                    ItemStack item = stored.getStack();
                    helper.assertFalse(item.getItem() instanceof ItemProgrammingCircuit,
                        "Programming wrappers should be consumed by programming");
                    if (item.isItemEqual(GTUtility.getIntegratedCircuit(1))) one = true;
                    if (item.isItemEqual(GTUtility.getIntegratedCircuit(9))) nine = true;
                }
                helper.assertTrue(one && nine, "Each recipe buffer should retain its supplied circuit configuration");
            })
            .thenSucceed();
    }

    @GameTest(template = "palladium", timeoutTicks = 300)
    public static void pendingDosesSurviveReloadAndRefund(GameTestHelper helper) {
        PalladiumLineTests.Line line = new PalladiumLineTests.Line(helper, false);
        helper.startSequence()
            .thenWaitUntil("wait for ME", 120, line::assertReady)
            .thenExecute("restore paid queued inputs", () -> {
                restoreDose(line, true, 3, 1);
                NBTTagCompound saved = saved(line);
                helper.assertTrue(saved.getTagList("cycDoses", 10).getCompoundTagAt(0).getBoolean("circuitsPaid"),
                    "Paid circuit provenance should be saved");
                restoreSnapshot(line, saved);
                invoke(line.cyclic, "refundAll");
                assertRefunded(helper, line, 0, 0, 3);
                invoke(line.cyclic, "refundAll");
                assertRefunded(helper, line, 0, 0, 3);
            })
            .thenSucceed();
    }

    @GameTest(template = "palladium", timeoutTicks = 300)
    public static void legacyQueueDoesNotCreateOrRefundCircuits(GameTestHelper helper) {
        PalladiumLineTests.Line line = new PalladiumLineTests.Line(helper, false);
        helper.startSequence()
            .thenWaitUntil("wait for ME", 120, line::assertReady)
            .thenExecute("restore a legacy queue without circuit provenance", () -> {
                restoreDose(line, false, 3, 1);
                invoke(line.cyclic, "drainDoses");
                assertRefunded(helper, line, 0, 0, 3);
                helper.assertEquals(0L, (long) saved(line).getTagList("cycDoses", 10).tagCount(),
                    "The unpaid legacy dose should be removed");
            })
            .thenSucceed();
    }

    @GameTest(template = "palladium", timeoutTicks = 300)
    public static void circuitsAreConsumedWhenProgrammingIsDisabled(GameTestHelper helper) {
        PalladiumLineTests.Line line = new PalladiumLineTests.Line(helper, false);
        helper.startSequence()
            .thenWaitUntil("wait for ME", 120, line::assertReady)
            .thenExecute("dispatch with programming disabled", () -> {
                restoreDose(line, true, 2, 0);
                try {
                    Field program = DualInputHatch.class.getDeclaredField("program");
                    program.setAccessible(true);
                    program.setBoolean(line.cyclic, false);
                } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
                invoke(line.cyclic, "drainDoses");
                assertRefunded(helper, line, 0, 0, 0);
                helper.assertEquals(0L, (long) saved(line).getTagList("cycDoses", 10).tagCount(),
                    "Both runs should have entered the buffer");
                invoke(line.cyclic, "refundAll");
                assertRefunded(helper, line, 0, 0, 2);
            })
            .thenSucceed();
    }

    @GameTest(template = "palladium", timeoutTicks = 300)
    public static void breakingHatchReturnsQueuedMaterials(GameTestHelper helper) {
        PalladiumLineTests.Line line = new PalladiumLineTests.Line(helper, false);
        helper.startSequence()
            .thenWaitUntil("wait for ME", 120, line::assertReady)
            .thenExecute("refund queued materials on destruction", () -> {
                restoreDose(line, true, 3, 1);
                line.cyclic.onBlockDestroyed();
                assertRefunded(helper, line, 0, 0, 3);
                line.cyclic.onBlockDestroyed();
                assertRefunded(helper, line, 0, 0, 3);
            })
            .thenSucceed();
    }

    private static InventoryCrafting table(ICraftingPatternDetails pattern, int copies, String mode) {
        InventoryCrafting table = new InventoryCrafting(new ContainerNull(), 3, 3);
        int slot = 0;
        for (IAEStack<?> input : pattern.getCondensedAEInputs()) {
            boolean circuit = input.isItem() && ((IAEItemStack) input).getItem() instanceof ItemProgrammingCircuit;
            if (circuit && mode.equals("missing")) continue;
            long amount = input.getStackSize() * (circuit && mode.equals("unscaled") ? 1 : copies);
            if (input.equals(PalladiumLineTests.Line.circuit(1))) {
                if (mode.equals("short")) amount--;
                if (mode.equals("extra")) amount++;
            }
            ItemStack item;
            if (input.isFluid()) {
                IAEFluidStack fluid = ((IAEFluidStack) input).copy();
                fluid.setStackSize(amount);
                item = ItemFluidPacket.newStack(fluid);
            } else {
                item = input.equals(PalladiumLineTests.Line.circuit(9)) && mode.equals("wrong")
                    ? PalladiumLineTests.Line.circuit(2).getItemStack() : ((IAEItemStack) input).getItemStack().copy();
                item.stackSize = Math.toIntExact(amount);
            }
            table.setInventorySlotContents(slot++, item);
        }
        return table;
    }

    private static NBTTagCompound saved(PalladiumLineTests.Line line) {
        NBTTagCompound nbt = new NBTTagCompound();
        ((TileEntity) line.cyclic.getBaseMetaTileEntity()).writeToNBT(nbt);
        return nbt;
    }

    private static void restoreDose(PalladiumLineTests.Line line, boolean paid, long runs, int lane) {
        NBTTagCompound nbt = saved(line);
        NBTTagList doses = new NBTTagList(), inputs = new NBTTagList();
        inputs.appendTag(ChancePatternTests.ae(new ItemStack(Blocks.stone), 1).toNBTGeneric());
        IAEItemStack one = PalladiumLineTests.Line.circuit(1);
        one.setStackSize(2);
        inputs.appendTag(one.toNBTGeneric());
        inputs.appendTag(PalladiumLineTests.Line.circuit(9).toNBTGeneric());
        NBTTagCompound dose = new NBTTagCompound();
        dose.setTag("in", inputs);
        dose.setLong("runs", runs);
        dose.setInteger("lane", lane);
        if (paid) dose.setBoolean("circuitsPaid", true);
        doses.appendTag(dose);
        nbt.setTag("cycDoses", doses);
        restoreSnapshot(line, nbt);
    }

    @SuppressWarnings("unchecked")
    private static void restoreSnapshot(PalladiumLineTests.Line line, NBTTagCompound nbt) {
        BaseMetaTileEntity unloaded = new BaseMetaTileEntity();
        unloaded.setWorldObj(line.helper.getWorld());
        ((TileEntity) unloaded).readFromNBT((NBTTagCompound) nbt.copy());
        // Deserialize on an unconnected tile, then exercise the restored queue on the connected fixture.
        Collection<Object> restored = (Collection<Object>) field(unloaded.getMetaTileEntity(), "doses");
        Collection<Object> pending = (Collection<Object>) field(line.cyclic, "doses");
        pending.clear();
        pending.addAll(restored);
    }

    private static Object field(Object instance, String name) {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException e) { throw new AssertionError(e); }
        }
        throw new AssertionError("Missing field " + name);
    }

    private static void assertRefunded(GameTestHelper helper, PalladiumLineTests.Line line, long one, long nine, long stone) {
        helper.assertEquals(one, line.stored(PalladiumLineTests.Line.circuit(1).getItemStack()), "Returned circuit 1 count");
        helper.assertEquals(nine, line.stored(PalladiumLineTests.Line.circuit(9).getItemStack()), "Returned circuit 9 count");
        helper.assertEquals(stone, line.stored(new ItemStack(Blocks.stone)), "Returned material count");
    }

    private static void invoke(CyclicPatternDualInputHatch hatch, String name) {
        try {
            Method method = CyclicPatternDualInputHatch.class.getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(hatch);
        } catch (ReflectiveOperationException e) { throw new AssertionError(name, e); }
    }
}
