package reobf.proghatches.gametests;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.MEInventoryCrafting;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.bufferutil.ItemStackG;
import reobf.proghatches.gt.metatileentity.util.AEInputItemHandler;
import reobf.proghatches.gt.metatileentity.util.LongStacks;
import reobf.proghatches.gt.metatileentity.util.PushStaging;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/** Ordinary buffered hatches cut a large input into slot-limit chunks; slots of one type share their capacity. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class SplitPushTests {

    private static final int AMOUNT = 1000;

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void cmPushSplitsAndSharesSameTypeCapacity(GameTestHelper h) {
        BufferedDualInputHatch hatch = hatch(h);
        ItemStack type = new ItemStack(Blocks.stained_hardened_clay, 1, 3);
        int limit = hatch.getInventoryStackLimit();
        long cap = hatch.itemLimit();
        int chunks = (AMOUNT + limit - 1) / limit;
        h.assertTrue(AMOUNT > cap && chunks <= 16, "The input needs several slots and fits the hatch");

        h.assertTrue(hatch.pushPatternCM(null, table(type), ForgeDirection.UNKNOWN),
            "CM mode accepts an input above one slot's capacity");
        DualInvBuffer buffer = hatch.inv0.get(0);
        IAEItemStack[] single = (IAEItemStack[]) LongInputBufferTests.field(buffer, "mStoredItemInternalSingle");
        ItemStackG[] stored = (ItemStackG[]) LongInputBufferTests.field(buffer, "mStoredItemInternal");
        for (int ix = 0; ix < chunks; ix++) h.assertEquals((long) Math.min(limit, AMOUNT - ix * limit),
            single[ix].getStackSize(), "Chunk " + ix + " is cut at the slot stack limit");
        h.assertNull(single[chunks], "No further slot is recorded");

        // GT drains the first slots first; any layout with the same total is still one run.
        stored[0] = LongStacks.itemGroup(single[0].copy().setStackSize(limit + stored[chunks - 1].stackSize()));
        stored[chunks - 1] = null;
        h.assertEquals(1L, copies(hatch, buffer), "Same-type slots are counted together");
        h.assertEquals(1, buffer.space(), "Same-type slots share their free space");

        h.assertTrue(hatch.pushPatternCM(null, table(type), ForgeDirection.UNKNOWN), "A second run merges");
        h.assertEquals(2L * AMOUNT, total(stored, chunks, cap, h), "The merged run is counted exactly");
        h.assertEquals(2L, copies(hatch, buffer), "The buffer holds two runs");

        long room = buffer.chanceRoom(LongStacks.item(type, 1));
        h.assertEquals(chunks * cap - 2L * AMOUNT, room, "A refill may use every slot of its type");
        h.assertTrue(buffer.chanceAdd(Collections.singletonList(LongStacks.item(type, room))), "The refill fits");
        h.assertEquals(chunks * cap, total(stored, chunks, cap, h), "The refill fills the type's slots exactly");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void laneRunsSplitAtSlotLimit(GameTestHelper h) {
        BufferedDualInputHatch hatch = hatch(h);
        ItemStack type = new ItemStack(Blocks.stained_hardened_clay, 1, 4);
        long cap = hatch.itemLimit();
        int chunks = (AMOUNT + hatch.getInventoryStackLimit() - 1) / hatch.getInventoryStackLimit();
        long taken = takeRuns(hatch, Collections.singletonList(PushStaging.Part.item(type, AMOUNT)), 3);
        h.assertEquals(2L, taken, "One run is staged and one more fits the shared capacity");
        ItemStackG[] stored = (ItemStackG[]) LongInputBufferTests.field(hatch.inv0.get(0), "mStoredItemInternal");
        h.assertEquals(2L * AMOUNT, total(stored, chunks, cap, h), "Both runs are in the buffer");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void superfluidCmChunksFitItsBufferSlots(GameTestHelper h) {
        BufferedDualInputHatch hatch = place(h, Config.metaTileEntityOffset + Registration.SuperFluidHatch);
        h.assertEquals(hatch.itemLimit(), (long) hatch.getInventoryStackLimit(),
            "An input slot holds what one buffer slot holds");
        ItemStack type = new ItemStack(Blocks.stained_hardened_clay, 1, 5);
        h.assertTrue(hatch.pushPatternCM(null, table(type, 128), ForgeDirection.UNKNOWN), "CM mode accepts the input");
        h.assertTrue(hatch.isInputEmpty(), "The input is filed into a buffer");
        assertRecorded(h, hatch, 64, 64);
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void superfluidInterfaceChunksFitItsBufferSlots(GameTestHelper h) {
        BufferedDualInputHatch hatch = place(h, Config.metaTileEntityOffset + Registration.SuperFluidHatch);
        ItemStack type = new ItemStack(Blocks.stained_hardened_clay, 1, 6);
        InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor(hatch.getBaseMetaTileEntity(), ForgeDirection.WEST);
        h.assertNull(adaptor.addItems(ChancePatternTests.sized(type, 128)), "A normal ME interface inserts the batch");
        h.assertNotNull(hatch.classifyForce(), "The inserted batch is filed into a buffer");
        h.assertTrue(hatch.isInputEmpty(), "Nothing stays in the input slots");
        assertRecorded(h, hatch, 64, 64);
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void guiUsesTheInputSlotCapacity(GameTestHelper h) {
        BufferedDualInputHatch hatch = hatch(h);
        AEInputItemHandler gui = (AEInputItemHandler) hatch.getInventoryHandler();
        int limit = hatch.getInventoryStackLimit();
        h.assertEquals(limit, gui.getSlotLimit(0), "The GUI slot holds what the input slot holds");
        ItemStack rest = gui.insertItem(0, ChancePatternTests.sized(new ItemStack(Blocks.stained_hardened_clay, 1, 7), 200),
            false);
        h.assertEquals(200 - limit, rest == null ? 0 : rest.stackSize, "The rest stays on the cursor");
        h.assertEquals((long) limit, gui.amount(0), "The slot is filled to its capacity");
        h.succeed();
    }

    // A recipe recorded with the same item in two slots keeps both slots growing by their own amount per run.
    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void extraRunsKeepTheRecordedSlotShares(GameTestHelper h) {
        BufferedDualInputHatch hatch = place(h, Config.metaTileEntityOffset + Registration.BufferedDualInputHatchOffset + 6);
        ItemStack wire = new ItemStack(Blocks.stained_hardened_clay, 1, 8), rod = new ItemStack(Blocks.stained_hardened_clay, 1, 9);
        List<PushStaging.Part> run = List.of(PushStaging.Part.item(wire, 64), PushStaging.Part.item(wire, 64),
            PushStaging.Part.item(rod, 1));
        h.assertEquals(10L, takeRuns(hatch, run, 10), "Ten runs fit the buffer");
        ItemStackG[] stored = (ItemStackG[]) LongInputBufferTests.field(hatch.inv0.get(0), "mStoredItemInternal");
        h.assertEquals(640L, stored[0].stackSize(), "The first wire slot holds its share of every run");
        h.assertEquals(640L, stored[1].stackSize(), "The second wire slot holds its share of every run");
        h.assertEquals(10L, stored[2].stackSize(), "The rod slot holds one per run");
        h.assertEquals(10L, copies(hatch, hatch.inv0.get(0)), "The buffer holds ten runs");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void extraRunsSpillOnlyPastASlotsCapacity(GameTestHelper h) {
        BufferedDualInputHatch hatch = hatch(h);
        ItemStack type = new ItemStack(Blocks.stained_hardened_clay, 1, 10);
        long cap = hatch.itemLimit();
        List<PushStaging.Part> run = List.of(PushStaging.Part.item(type, 8), PushStaging.Part.item(type, 56));
        h.assertEquals(6L, takeRuns(hatch, run, 6), "Six runs fit the shared capacity");
        ItemStackG[] stored = (ItemStackG[]) LongInputBufferTests.field(hatch.inv0.get(0), "mStoredItemInternal");
        h.assertEquals(cap, stored[1].stackSize(), "The large slot stops at its capacity");
        h.assertEquals(6L * 64 - cap, stored[0].stackSize(), "The small slot takes what the large one cannot");
        h.assertEquals(6L, copies(hatch, hatch.inv0.get(0)), "The buffer holds six runs");
        h.succeed();
    }

    private static void assertRecorded(GameTestHelper h, BufferedDualInputHatch hatch, long... chunks) {
        IAEItemStack[] single = (IAEItemStack[]) LongInputBufferTests.field(hatch.inv0.get(0), "mStoredItemInternalSingle");
        for (int ix = 0; ix < chunks.length; ix++)
            h.assertEquals(chunks[ix], LongStacks.amount(single[ix]), "Recorded slot " + ix);
        h.assertNull(single[chunks.length], "No further slot is recorded");
    }

    private static BufferedDualInputHatch hatch(GameTestHelper h) {
        return place(h, Config.metaTileEntityOffset + Registration.BufferedDualInputHatchOffset + 4);
    }

    private static BufferedDualInputHatch place(GameTestHelper h, int id) {
        ChancePatternTests.buildSifter(h, true, id);
        h.gtnh().multiBlockController(ChancePatternTests.SIFTER).getBaseMetaTileEntity().disableWorking();
        return (BufferedDualInputHatch) h.gtnh().metaTileEntity(ChancePatternTests.HATCH);
    }

    private static MEInventoryCrafting table(ItemStack type) {
        return table(type, AMOUNT);
    }

    private static MEInventoryCrafting table(ItemStack type, long amount) {
        MEInventoryCrafting table = new MEInventoryCrafting(new ContainerNull(), 1, 1);
        table.setInventorySlotContents(0, LongStacks.item(type, amount));
        return table;
    }

    private static long total(ItemStackG[] stored, int slots, long cap, GameTestHelper h) {
        long total = 0;
        for (int ix = 0; ix < slots; ix++) {
            long now = stored[ix] == null ? 0 : stored[ix].stackSize();
            h.assertTrue(now <= cap, "Slot " + ix + " stays within its capacity");
            total += now;
        }
        return total;
    }

    private static long copies(BufferedDualInputHatch hatch, DualInvBuffer buffer) {
        Object copies = hatch.getPossibleCopies(buffer);
        long num = (long) LongInputBufferTests.field(copies, "num"), den = (long) LongInputBufferTests.field(copies, "den");
        return den == 1 ? num : -1;
    }

    private static long takeRuns(BufferedDualInputHatch hatch, List<PushStaging.Part> run, long runs) {
        try {
            Method method = BufferedDualInputHatch.class.getDeclaredMethod("takeRuns", List.class, long.class);
            method.setAccessible(true);
            return (long) method.invoke(hatch, run, runs);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
