package reobf.proghatches.gametests;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.mojang.authlib.GameProfile;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraft.world.WorldServer;
import reobf.proghatches.gt.metatileentity.util.AEInputItemHandler;
import reobf.proghatches.gt.metatileentity.util.AEInputSlot;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import appeng.api.AEApi;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.IGridHost;
import net.minecraftforge.common.util.ForgeDirection;
import com.glodblock.github.loader.ItemAndBlockHolder;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.util.AEInputBuffer;
import reobf.proghatches.gt.metatileentity.util.LongStacks;
import reobf.proghatches.gt.metatileentity.util.PushStaging;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import org.apache.commons.lang3.tuple.Pair;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEStack;
import appeng.container.ContainerNull;
import appeng.util.inv.MEInventoryCrafting;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import codechicken.nei.ItemStackMap;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.GTRecipe;
import gtPlusPlus.core.material.MaterialsElements;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.bufferutil.FluidTankG;
import reobf.proghatches.gt.metatileentity.bufferutil.ItemStackG;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class LongInputBufferTests {

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void belowIntLimit(GameTestHelper h) { run(h, 8192, 1, false); }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void firstPowerOfTwoAboveIntLimit(GameTestHelper h) { run(h, 16384, 1, false); }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void screenshotMultiplier(GameTestHelper h) { run(h, 32768, 1, false); }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void screenshotMultiplierMultiPush(GameTestHelper h) { run(h, 32768, 3, false); }

    static void run(GameTestHelper h, int multiplier, int copies, boolean legacyClamp) {
        ChancePatternTests.buildSifter(h, true, Config.metaTileEntityOffset + Registration.ExPatternOffset);
        PatternDualInputHatch hatch = (PatternDualInputHatch) h.gtnh().metaTileEntity(ChancePatternTests.HATCH);
        MTEMultiBlockBase controller = h.gtnh().multiBlockController(ChancePatternTests.SIFTER);
        controller.getBaseMetaTileEntity().disableWorking();
        hatch.skipActiveCheck = true;

        // Pack-only lenses are represented by distinct wool variants; all recipe quantities are unchanged.
        int[] counts = {8, 8, 8, 4, 6, 6, 6, 6, 36, 6, 6, 6};
        ItemStack[] items = new ItemStack[counts.length];
        FluidStack[] fluids = {
            MaterialsElements.STANDALONE.RHUGNOR.getFluidStack(2304),
            Materials.Creon.getMolten(2304),
            MaterialsElements.STANDALONE.ADVANCED_NITINOL.getFluidStack(147456)
        };
        List<IAEStack<?>> inputs = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            items[i] = new ItemStack(Blocks.wool, counts[i], i);
            inputs.add(AEItemStack.create(items[i]));
        }
        for (FluidStack f : fluids) {
            h.assertNotNull(f, "The real recipe fluid should exist");
            inputs.add(AEFluidStack.create(f));
        }
        ItemStack raw = PalladiumLineTests.Line.pattern(inputs.toArray(new IAEStack<?>[0]),
            new IAEStack<?>[] {AEItemStack.create(new ItemStack(Blocks.glass))}, false);
        ICraftingPatternDetails base = ((ICraftingPatternItem) raw.getItem()).getPatternForItem(raw, h.getWorld());
        h.assertNotNull(base, "The fluid pattern should decode");
        hatch.getPatterns().setInventorySlotContents(0, raw);
        hatch.setPatternMultiplier(0, 1);
        ItemStackMap<Pair<Object, Integer>> lookup = new ItemStackMap<>();
        lookup.put(raw, Pair.of((Object) hatch, Integer.numberOfTrailingZeros(multiplier)));
        hatch.optimize(lookup);
        int actualMultiplier = ((int[]) field(hatch, "multiplier"))[0];
        h.assertEquals(multiplier, actualMultiplier, "The real optimizer should apply the requested power of two");
        ICraftingPatternDetails pattern = new PatternDualInputHatch.DA(base, actualMultiplier);
        IAEStack<?>[] pushed = pattern.getCondensedAEInputs();
        MEInventoryCrafting table = new MEInventoryCrafting(new ContainerNull(), pushed.length, 1);
        long packetFull = 0, packetInt = 0;
        for (int i = 0; i < pushed.length; i++) {
            table.setInventorySlotContents(i, pushed[i].copy());
            ItemStack view = table.getStackInSlot(i);
            if (view != null && view.getItem() instanceof ItemFluidPacket) {
                long full = ItemFluidPacket.getFluidAmount(view);
                FluidStack clipped = ItemFluidPacket.getFluidStack(view);
                if (clipped.isFluidEqual(fluids[2])) {
                    packetFull = pushed[i].getStackSize();
                    packetInt = clipped.amount;
                }
                // Reproduce the exact amount p27 read from getFluidStack, without swapping the running mod.
                if (legacyClamp) table.setInventorySlotContents(i, AEFluidStack.create(clipped));
            }
        }
        h.assertEquals(147456L * multiplier, packetFull, "The original AE fluid stack should retain the long amount");
        int accepted;
        if (copies == 1) {
            accepted = hatch.pushPattern(pattern, table) ? 1 : 0;
        } else {
            int[] result = hatch.pushPatternMulti(pattern, table, copies);
            accepted = result.length == 0 ? 0 : result[0];
        }
        h.assertEquals(copies, accepted, "The hatch should accept all pushed copies");
        DualInvBuffer buffer = hatch.inv0.stream().filter(b -> !b.isEmpty()).findFirst().orElseThrow();
        FluidTankG[] stored = (FluidTankG[]) field(buffer, "mStoredFluidInternal");
        IAEFluidStack[] single = (IAEFluidStack[]) field(buffer, "mStoredFluidInternalSingle");
        long expectedNitinol = (legacyClamp ? packetInt : packetFull) * copies;
        h.assertEquals(expectedNitinol, total(stored, fluids[2]), "Pushed fluid should be accounted exactly");
        NBTTagCompound before = ratio(hatch, buffer);
        h.assertEquals(0L, before.getLong("broken"), "Freshly pushed inputs agree with their recorded recipe");

        GTRecipe recipe = GTValues.RA.stdBuilder().itemInputs(items).fluidInputs(fluids)
            .itemOutputs(new ItemStack(Blocks.glass)).duration(200).eut(8).build().get();
        consume(h, hatch, controller, buffer, recipe, 1);
        h.assertEquals(expectedNitinol - 147456L, total(stored, fluids[2]), "One run consumes exactly one recipe");
        NBTTagCompound after = ratio(hatch, buffer);
        h.assertEquals(0L, after.getLong("broken"), "Consuming part of a multiplied pattern must preserve its ratio");
        h.assertEquals((long) multiplier * copies - 1, after.getLong("num"), "The remaining numerator is exact");
        h.assertEquals((long) multiplier, after.getLong("den"), "The denominator is the pattern multiplier");
        h.assertEquals(147456L * multiplier, single[2].getStackSize(), "One logical fluid slot holds the entire recipe amount");
        h.assertNull(single[3], "The large fluid must not occupy additional logical slots");
        NBTTagCompound snapshot = buffer.toTag();
        buffer.fromTag(snapshot);
        h.assertEquals(0L, ratio(hatch, buffer).getLong("broken"), "The remaining ratio survives save/load");
        stored = (FluidTankG[]) field(buffer, "mStoredFluidInternal");
        long remaining = legacyClamp ? packetInt / 147456L - 1 : (long) multiplier * copies - 1;
        consume(h, hatch, controller, buffer, recipe, (int) remaining);
        long finalFluid = total(stored, fluids[2]);
        long finalItems = 0;
        for (ItemStackG s : (ItemStackG[]) field(buffer, "mStoredItemInternal")) {
            if (s != null) finalItems += s.stackSize();
        }
        if (!legacyClamp) {
            h.assertEquals(0L, finalFluid, "All supplied fluid should be consumed without loss");
            h.assertEquals(0L, finalItems, "All supplied items should be consumed");
        } else {
            h.assertTrue(finalItems > 0, "Clamping leaves unmatched item inputs");
        }
        StringBuilder chunks = new StringBuilder();
        for (IAEFluidStack t : single) if (t != null && t.getFluidStack().isFluidEqual(fluids[2]))
            chunks.append(t.getStackSize()).append(",");
        System.out.println("[LongInputBuffer] multiplier=" + multiplier + " copies=" + copies + " legacyClamp=" + legacyClamp
            + " packetLong=" + packetFull + " packetInt=" + packetInt + " nitinolLockedChunks=" + chunks
            + " initial=" + before + " afterOne=" + after + " finalNitinol=" + finalFluid + " finalItemCount=" + finalItems);
        h.succeed();
    }


    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void legacySplitRecipeKeepsRemaindersAndMatchesNewPush(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        DualInvBuffer buffer = hatch.inv0.get(0);
        NBTTagCompound legacy = new NBTTagCompound();
        legacy.setInteger("i", buffer.i);
        legacy.setInteger("f", buffer.f);
        legacy.setBoolean("recipeLocked", true);
        legacy.setBoolean("lock", true);
        ItemStack item = new ItemStack(Blocks.wool, 8 * 32768, 14);
        legacy.setTag("mStoredItemInternalSingle0", DualInputHatch.writeToNBT(item, new NBTTagCompound()));
        item.stackSize -= 8;
        legacy.setTag("mStoredItemInternal0", ItemStackG.neo(item).writeToNBT(new NBTTagCompound()));
        ItemStack circuit = GTUtility.getIntegratedCircuit(9);
        circuit.stackSize = 0;
        legacy.setTag("mStoredItemInternal" + buffer.i, ItemStackG.neo(circuit).writeToNBT(new NBTTagCompound()));
        FluidStack fluid = MaterialsElements.STANDALONE.ADVANCED_NITINOL.getFluidStack(1);
        int[] amounts = {Integer.MAX_VALUE, Integer.MAX_VALUE, 536870914};
        for (int i = 0; i < amounts.length; i++) {
            FluidStack chunk = fluid.copy();
            chunk.amount = amounts[i];
            legacy.setTag("mStoredFluidInternalSingle" + i, new FluidTank(chunk, Integer.MAX_VALUE).writeToNBT(new NBTTagCompound()));
            if (i == 0) chunk.amount -= 147456;
            FluidTankG actual = new FluidTankG();
            actual.setFluid(chunk);
            legacy.setTag("mStoredFluidInternal" + i, actual.writeToNBT(new NBTTagCompound()));
        }
        buffer.fromTag(legacy);
        h.assertTrue(buffer.lock && buffer.recipeLocked, "Old locked flags survive migration");
        IAEFluidStack[] singles = (IAEFluidStack[]) field(buffer, "mStoredFluidInternalSingle");
        h.assertEquals(4831838208L, singles[0].getStackSize(), "The old split recipe is joined");
        h.assertNull(singles[1], "Old split slots are freed");
        h.assertEquals(4831838208L - 147456, total((FluidTankG[]) field(buffer, "mStoredFluidInternal"), fluid),
            "Only saved remaining fluid is migrated");
        h.assertEquals(32767L, ratio(hatch, buffer).getLong("num"), "The partly consumed old recipe is consistent");
        ItemStackG[] actualItems = (ItemStackG[]) field(buffer, "mStoredItemInternal");
        h.assertEquals(0L, actualItems[buffer.i].stackSize(), "The non-consumable circuit stays zero-sized");
        h.assertEquals(9, actualItems[buffer.i].getStack().getItemDamage(), "The circuit configuration survives");
        NBTTagCompound once = buffer.toTag();
        buffer.fromTag(once);
        h.assertEquals(once, buffer.toTag(), "Migrating and reloading is idempotent");

        item.stackSize = 8 * 32768;
        IAEStack<?>[] inputs = { AEItemStack.create(item), LongStacks.fluid(fluid, 4831838208L) };
        ICraftingPatternDetails details = details(h, inputs);
        h.assertTrue(hatch.pushPattern(details, table(inputs)), "The migrated locked buffer accepts the new logical recipe");
        h.assertTrue(hatch.inv0.get(0) == buffer && hatch.inv0.get(1).isEmpty(), "The same old buffer is reused");
        h.assertEquals(65535L, ratio(hatch, buffer).getLong("num"), "The new batch is added without recreating consumed input");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void oldClippedRecipeIsNotRepairedByInventingFluid(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        DualInvBuffer buffer = hatch.inv0.get(0);
        NBTTagCompound legacy = new NBTTagCompound();
        legacy.setInteger("i", buffer.i);
        legacy.setInteger("f", buffer.f);
        legacy.setBoolean("recipeLocked", true);
        ItemStack item = new ItemStack(Blocks.wool, 262144 - 8, 14);
        legacy.setTag("mStoredItemInternal0", ItemStackG.neo(item).writeToNBT(new NBTTagCompound()));
        item.stackSize = 262144;
        legacy.setTag("mStoredItemInternalSingle0", DualInputHatch.writeToNBT(item, new NBTTagCompound()));
        FluidStack fluid = MaterialsElements.STANDALONE.ADVANCED_NITINOL.getFluidStack(Integer.MAX_VALUE);
        legacy.setTag("mStoredFluidInternalSingle0", new FluidTank(fluid, Integer.MAX_VALUE).writeToNBT(new NBTTagCompound()));
        fluid.amount -= 147456;
        FluidTankG actual = new FluidTankG();
        actual.setFluid(fluid);
        legacy.setTag("mStoredFluidInternal0", actual.writeToNBT(new NBTTagCompound()));
        buffer.fromTag(legacy);
        h.assertEquals(Integer.MAX_VALUE - 147456L,
            total((FluidTankG[]) field(buffer, "mStoredFluidInternal"), fluid), "Clipped data is not replenished during migration");
        h.assertEquals(1L, ratio(hatch, buffer).getLong("broken"), "A genuine old shortage remains inconsistent");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void largeItemsAndPendingInputsSurviveViewsAndReload(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        ItemStack item = new ItemStack(Blocks.wool, 1, 15);
        item.setTagCompound(new NBTTagCompound());
        item.getTagCompound().setString("identity", "long-item");
        FluidStack fluid = MaterialsElements.STANDALONE.ADVANCED_NITINOL.getFluidStack(1);
        long amount = 4831838208L;
        IAEStack<?>[] batch = {LongStacks.item(item, amount), LongStacks.fluid(fluid, amount)};
        PushStaging.logical(table(batch), Long.MAX_VALUE, slot -> slot < 32, Long.MAX_VALUE, 32).stageInto(hatch.inputs());
        AEInputBuffer pending = hatch.inputs();
        h.assertEquals(amount, pending.items[0].getStackSize(), "External item input is a long quantity");
        h.assertEquals(amount, pending.fluids[0].getStackSize(), "External fluid input is a long quantity");
        NBTTagCompound saved = pending.save();
        pending.clear();
        pending.load(saved);
        pending.sync();
        h.assertEquals(saved, pending.save(), "Reloading must not add the bounded legacy view a second time");
        ItemStack taken = hatch.getInventoryHandler().extractItem(0, 64, false);
        h.assertEquals(64, taken.stackSize, "The inventory adapter extracts a bounded stack");
        hatch.mStoredFluid[0].drain(1000, true);
        pending.sync();
        h.assertEquals(amount - 64, pending.items[0].getStackSize(), "Item extraction deducts from the full amount");
        h.assertEquals(amount - 1000, pending.fluids[0].getStackSize(), "Tank draining deducts from the full amount");
        h.assertEquals("long-item", pending.items[0].getItemStack().getTagCompound().getString("identity"), "Item NBT is retained");
        pending.clear();

        ICraftingPatternDetails details = details(h, batch);
        h.assertTrue(hatch.pushPattern(details, table(batch)), "A long item batch should be accepted");
        DualInvBuffer buffer = hatch.inv0.get(0);
        IAEItemStack[] singles = (IAEItemStack[]) field(buffer, "mStoredItemInternalSingle");
        h.assertEquals(amount, singles[0].getStackSize(), "The locked item recipe stores a long count");
        h.assertNull(singles[1], "A long item occupies one logical slot");
        NBTTagCompound snapshot = buffer.toTag();
        buffer.fromTag(snapshot);
        h.assertEquals(snapshot, buffer.toTag(), "Internal chunks and long item recipe survive reload");
        h.assertEquals(amount, ((ItemStackG[]) field(buffer, "mStoredItemInternal"))[0].stackSize(),
            "All item chunks are retained");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void rejectsUnrepresentableInputsWithoutMutation(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        hatch.restrictToInt = true;
        IAEStack<?>[] batch = { LongStacks.item(new ItemStack(Blocks.wool), 2147483648L) };
        ICraftingPatternDetails details = details(h, batch);
        h.assertFalse(hatch.pushPattern(details, table(batch)), "The configured int limit rejects a larger logical slot");
        h.assertTrue(hatch.isInputEmpty(), "Rejected input leaves no staged stock");
        hatch.restrictToInt = false;
        IAEStack<?>[] tooMany = new IAEStack<?>[33];
        for (int i = 0; i < tooMany.length; i++) {
            ItemStack item = new ItemStack(Blocks.wool);
            item.setTagCompound(new NBTTagCompound());
            item.getTagCompound().setInteger("slot", i);
            tooMany[i] = AEItemStack.create(item);
        }
        h.assertFalse(hatch.pushPattern(details, table(tooMany)), "Too many distinct inputs are rejected atomically");
        h.assertTrue(hatch.isInputEmpty(), "Rejected input preserves the empty staging area");
        h.succeed();
    }


    @GameTest(template = "sifter", timeoutTicks = 600)
    public static void realMultiblockConsumesLongInventory(GameTestHelper h) {
        var sifter = ChancePatternTests.buildSifter(h, true, Config.metaTileEntityOffset + Registration.ExPatternOffset);
        PatternDualInputHatch hatch = (PatternDualInputHatch) h.gtnh().metaTileEntity(ChancePatternTests.HATCH);
        hatch.skipActiveCheck = true;
        ItemStack item = new ItemStack(Blocks.stained_glass, 8, 5);
        FluidStack fluid = MaterialsElements.STANDALONE.ADVANCED_NITINOL.getFluidStack(147456);
        h.gtnh().withTestRecipe(sifter, GTValues.RA.stdBuilder().itemInputs(item).fluidInputs(fluid)
            .itemOutputs(new ItemStack(Blocks.glass)).duration(20).eut(8));
        IAEStack<?>[] input = {LongStacks.item(item, 8L * 32768), LongStacks.fluid(fluid, 147456L * 32768)};
        h.assertTrue(hatch.pushPattern(details(h, input), table(input)), "The multiplied recipe is accepted");
        DualInvBuffer buffer = hatch.inv0.get(0);
        h.startSequence().thenWaitUntil("wait for real GT processing", 500, () -> {
            long left = ((ItemStackG[]) field(buffer, "mStoredItemInternal"))[0].stackSize();
            h.assertTrue(left < 8L * 32768, "The actual multiblock must consume input");
            long runs = (8L * 32768 - left) / 8;
            h.assertEquals(147456L * (32768 - runs),
                total((FluidTankG[]) field(buffer, "mStoredFluidInternal"), fluid), "GT consumes the matching fluid quantity");
            h.assertEquals(0L, ratio(hatch, buffer).getLong("broken"), "Real GT consumption preserves the recorded ratio");
        }).thenSucceed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void wholeTileLoadsOldAndNewExternalInventory(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        long amount = 4831838208L;
        ItemStack item = new ItemStack(Blocks.wool, 1, 12);
        FluidStack fluid = MaterialsElements.STANDALONE.ADVANCED_NITINOL.getFluidStack(1);
        PushStaging.logicalParts(java.util.Arrays.asList(PushStaging.Part.item(item, amount),
            PushStaging.Part.fluid(fluid, amount)), Long.MAX_VALUE, i -> i < 32, Long.MAX_VALUE, 32).stageInto(hatch.inputs());
        NBTTagCompound saved = new NBTTagCompound();
        ((net.minecraft.tileentity.TileEntity) hatch.getBaseMetaTileEntity()).writeToNBT(saved);
        PatternDualInputHatch restored = restore(h, saved);
        h.assertEquals(amount, restored.inputs().items[0].getStackSize(), "Whole-tile loading retains long external items");
        h.assertEquals(amount, restored.inputs().fluids[0].getStackSize(), "Whole-tile loading retains long external fluids");
        NBTTagCompound second = new NBTTagCompound();
        ((net.minecraft.tileentity.TileEntity) restored.getBaseMetaTileEntity()).writeToNBT(second);
        PatternDualInputHatch twice = restore(h, second);
        h.assertEquals(amount, twice.inputs().items[0].getStackSize(), "Repeated whole-tile loading cannot duplicate the int view");

        // Emulate an old tile: Inventory and mFluid are its only external storage.
        saved.removeTag("aeInputBuffer");
        saved.setTag("Inventory", new net.minecraft.nbt.NBTTagList());
        int[] chunks = {Integer.MAX_VALUE, Integer.MAX_VALUE, 536870914};
        for (int i = 0; i < chunks.length; i++) {
            ItemStack chunk = item.copy();
            chunk.stackSize = chunks[i];
            NBTTagCompound entry = chunk.writeToNBT(new NBTTagCompound());
            entry.setInteger("IntSlot", i);
            entry.setInteger("Count", chunk.stackSize);
            saved.getTagList("Inventory", 10).appendTag(entry);
            FluidStack fs = fluid.copy();
            fs.amount = chunks[i];
            saved.setTag("mFluid" + i, new FluidTank(fs, Integer.MAX_VALUE).writeToNBT(new NBTTagCompound()));
        }
        PatternDualInputHatch old = restore(h, saved);
        h.assertEquals(amount, old.inputs().items[0].getStackSize(), "Old external item chunks are imported and joined");
        h.assertEquals(amount, old.inputs().fluids[0].getStackSize(), "Old external fluid chunks are imported and joined");
        h.assertNull(old.inputs().items[1], "Joined legacy item slots are cleared");
        h.assertNull(old.inputs().fluids[1], "Joined legacy fluid slots are cleared");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 500)
    public static void refundPreservesRejectedLongFluids(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        long amount = 4831838208L;
        FluidStack fluid = MaterialsElements.STANDALONE.ADVANCED_NITINOL.getFluidStack(1);
        ItemStack cell = new ItemStack(ItemAndBlockHolder.CELL64K);
        h.setSlot("drive", 0, cell);
        h.startSequence().thenWaitUntil("wait for ME", 120, () -> {
            h.assertTrue(hatch.getProxy().isActive(), "The hatch must be on the creative ME network");
        }).thenExecute("refund full and partial long batches", () -> {
            PushStaging.logicalParts(java.util.Arrays.asList(PushStaging.Part.fluid(fluid, amount)),
                Long.MAX_VALUE, i -> i < 32, Long.MAX_VALUE, 32).stageInto(hatch.inputs());
            refund(hatch);
            long inME = storedFluid(hatch, fluid), pending = LongStacks.amount(hatch.inputs().fluids[0]);
            h.assertTrue(inME > 0 && pending > 0, "A limited cell should accept part of the long refund");
            h.assertEquals(amount, inME + pending, "A partial external refund retains every rejected mB");
            refund(hatch);
            h.assertEquals(amount, storedFluid(hatch, fluid) + LongStacks.amount(hatch.inputs().fluids[0]),
                "Retrying a full network must not duplicate or discard fluid");

            hatch.clearInputBuffer();
            IAEStack<?>[] batch = {LongStacks.fluid(fluid, amount)};
            h.assertTrue(hatch.pushPattern(details(h, batch), table(batch)), "Internal fluid-only batch is accepted");
            DualInvBuffer buffer = hatch.inv0.get(0);
            long before = storedFluid(hatch, fluid) + total((FluidTankG[]) field(buffer, "mStoredFluidInternal"), fluid);
            refund(hatch);
            h.assertEquals(before, storedFluid(hatch, fluid) + total((FluidTankG[]) field(buffer, "mStoredFluidInternal"), fluid),
                "Rejected internal fluid chunks also remain available");
        }).thenSucceed();
    }

    private static PatternDualInputHatch restore(GameTestHelper h, NBTTagCompound saved) {
        BaseMetaTileEntity tile = new BaseMetaTileEntity();
        tile.setWorldObj(h.getWorld());
        ((net.minecraft.tileentity.TileEntity) tile).readFromNBT((NBTTagCompound) saved.copy());
        return (PatternDualInputHatch) tile.getMetaTileEntity();
    }

    private static void refund(PatternDualInputHatch hatch) {
        try {
            Method method = PatternDualInputHatch.class.getDeclaredMethod("refundAll");
            method.setAccessible(true);
            method.invoke(hatch);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static long storedFluid(PatternDualInputHatch hatch, FluidStack fluid) {
        try {
            var stack = hatch.getProxy().getStorage().getFluidInventory().getStorageList().findPrecise(AEFluidStack.create(fluid));
            return stack == null ? 0 : stack.getStackSize();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }


    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void muiClickUsesRemainingCapacity(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        AEInputItemHandler input = new AEInputItemHandler(hatch, () -> 100);
        FakePlayer player = player(h);
        ModularContainer gui = container(input, player);
        ItemStack type = new ItemStack(Blocks.stained_glass, 1, 8);
        for (int stock : new int[] {40, 80, 100}) {
            hatch.clearInputBuffer();
            input.getStackInSlot(0);
            input.insertItem(0, ChancePatternTests.sized(type, stock), false);
            player.inventory.setItemStack(ChancePatternTests.sized(type, 64));
            gui.slotClick(0, 0, 0, player);
            h.assertEquals(100L, input.amount(0), "A click should fill the actual remaining capacity from stock " + stock);
            h.assertEquals(stock + 64 - 100, player.inventory.getItemStack().stackSize, "The cursor retains uninserted items");
        }
        player.inventory.setItemStack(null);
        gui.slotClick(0, 0, 0, player);
        h.assertEquals(64, player.inventory.getItemStack().stackSize, "Pickup takes at most an ordinary stack");
        h.assertEquals(36L, input.amount(0), "The rest stays in the input");
        player.inventory.setItemStack(ChancePatternTests.sized(type, 64));
        gui.slotClick(0, 1, 0, player);
        h.assertEquals(37L, input.amount(0), "Right click inserts one item");
        h.assertEquals(63, player.inventory.getItemStack().stackSize, "Right click keeps the rest on the cursor");
        player.inventory.setItemStack(null);
        gui.slotClick(0, 1, 0, player);
        h.assertEquals(19, player.inventory.getItemStack().stackSize, "Right-click pickup takes half of the visible stack");
        h.assertEquals(18L, input.amount(0), "Right-click pickup preserves the remainder");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void muiShiftTransferPreservesLongInventory(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        long[] cap = {100};
        AEInputItemHandler input = new AEInputItemHandler(hatch, () -> cap[0]);
        FakePlayer player = player(h);
        ModularContainer gui = container(input, player);
        ItemStack type = new ItemStack(Blocks.stained_glass, 1, 9);
        input.insertItem(0, ChancePatternTests.sized(type, 40), false);
        player.inventory.setInventorySlotContents(0, ChancePatternTests.sized(type, 64));
        gui.slotClick(1, 0, 1, player);
        h.assertEquals(100L, input.amount(0), "Shift insertion fills the actual capacity");
        h.assertEquals(4, player.inventory.getStackInSlot(0).stackSize, "Shift insertion leaves four items in player inventory");

        java.util.Arrays.fill(player.inventory.mainInventory, null);
        cap[0] = Long.MAX_VALUE;
        hatch.inputs().items[0] = LongStacks.item(type, 4831838208L);
        hatch.inputs().refreshViews();
        input.getStackInSlot(0);
        gui.slotClick(0, 0, 1, player);
        long moved = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack == null) continue;
            h.assertTrue(stack.stackSize <= stack.getMaxStackSize(), "Player slots must not receive oversized stacks");
            moved += stack.stackSize;
        }
        h.assertTrue(moved > 0, "Shift extraction actually moves items");
        h.assertEquals(4831838208L, moved + input.amount(0), "Shift extraction conserves the entire long quantity");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void normalAutomationAndGuiShareExactCounts(GameTestHelper h) {
        // Ordinary buffered hatch, with no encoded-pattern inventory or direct pattern push.
        int id = Config.metaTileEntityOffset + Registration.BufferedDualInputHatchOffset + 3;
        ChancePatternTests.place(h, ChancePatternTests.HATCH, id, ForgeDirection.WEST);
        BufferedDualInputHatch hatch = (BufferedDualInputHatch) h.gtnh().metaTileEntity(ChancePatternTests.HATCH);
        hatch.getBaseMetaTileEntity().disableWorking();
        ItemStack type = new ItemStack(Blocks.stained_glass, 1, 10);
        hatch.setInventorySlotContents(0, ChancePatternTests.sized(type, 40));
        h.assertEquals(40L, hatch.inputs().items[0].getStackSize(), "setInventorySlotContents imports a real logistics stack");
        hatch.getStackInSlot(0).stackSize += 20;
        hatch.syncInputViews();
        h.assertEquals(60L, hatch.inputs().items[0].getStackSize(), "In-place stackSize edits are observed");
        hatch.decrStackSize(0, 7);
        hatch.syncInputViews();
        h.assertEquals(53L, hatch.inputs().items[0].getStackSize(), "decrStackSize is counted exactly once");
        hatch.setInventorySlotContents(0, ChancePatternTests.sized(type, 12));
        hatch.syncInputViews();
        h.assertEquals(12L, hatch.inputs().items[0].getStackSize(), "Replacing a same-type logistics stack sets its quantity");
        AEInputItemHandler gui = new AEInputItemHandler(hatch, () -> 100);
        gui.insertItem(0, ChancePatternTests.sized(type, 64), false);
        h.assertEquals(76, hatch.getStackInSlot(0).stackSize, "Automation sees the full int-range quantity, not the GUI window");
        ItemStack view = hatch.getStackInSlot(0);
        view.stackSize -= 9;
        h.assertEquals(67L, gui.amount(0), "GUI observes direct logistics mutations");
        hatch.setInventorySlotContents(0, new ItemStack(Blocks.stone, 3));
        h.assertEquals(3L, gui.amount(0), "Replacing the item type does not retain stale quantities");
        h.assertTrue(gui.getStackInSlot(0).getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.stone),
            "GUI observes replacement item types");
        hatch.setInventorySlotContents(0, null);
        h.assertEquals(0L, gui.amount(0), "Clearing through IInventory clears the long backend");
        var adaptor = appeng.util.InventoryAdaptor.getAdaptor(hatch.getBaseMetaTileEntity(), ForgeDirection.WEST,
            appeng.util.InventoryAdaptor.ALLOW_ITEMS | appeng.util.InventoryAdaptor.FOR_INSERTS);
        h.assertNotNull(adaptor, "A normal ME interface can obtain the hatch's logistics adaptor");
        h.assertNull(adaptor.simulateAdd(ChancePatternTests.sized(type, 40)), "Normal interface simulation accepts the batch");
        h.assertTrue(hatch.isInputEmpty(), "Simulation must not insert items");
        h.assertNull(adaptor.addItems(ChancePatternTests.sized(type, 40)), "Normal interface inserts via the logistics adaptor");
        hatch.syncInputViews();
        h.assertEquals(40L, gui.amount(0), "Interface insertion and GUI refer to the same stock");
        h.succeed();
    }


    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void fluidContainerWindowPreservesLongContents(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        long[] capacity = {100};
        var tank = new reobf.proghatches.gt.metatileentity.util.AEInputFluidTank(hatch, 0, () -> capacity[0]);
        FluidStack type = MaterialsElements.STANDALONE.ADVANCED_NITINOL.getFluidStack(40);
        h.assertEquals(40, tank.fill(type, true), "Initial fluid is inserted");
        type.amount = 64;
        h.assertEquals(60, tank.fill(type, false), "Simulation reports exact remaining capacity");
        h.assertEquals(40L, tank.amount(), "Simulating does not insert fluid");
        h.assertEquals(60, tank.fill(type, true), "Only sixty of sixty-four fluid units fit");
        capacity[0] = Long.MAX_VALUE;
        hatch.inputs().fluids[0] = LongStacks.fluid(type, 4831838208L);
        hatch.inputs().refreshViews();
        type.amount = 1000;
        h.assertTrue(tank.getCapacity() > tank.getFluidAmount(), "A long fluid slot still advertises container insertion room");
        h.assertEquals(1000, tank.fill(type, true), "A filled container can add fluid above int.max");
        h.assertEquals(1000, tank.drain(1000, true).amount, "An empty container removes the same amount");
        h.assertEquals(4831838208L, tank.amount(), "Long fluid remains exact after container transfers");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void breakingHatchDropsAllLongItemChunks(GameTestHelper h) {
        PatternDualInputHatch hatch = hatch(h);
        ItemStack type = new ItemStack(Blocks.stained_glass, 1, 11);
        long amount = 4831838208L;
        IAEStack<?>[] batch = {LongStacks.item(type, amount)};
        h.assertTrue(hatch.pushPattern(details(h, batch), table(batch)), "Internal long items are accepted");
        PushStaging.logical(table(batch), Long.MAX_VALUE, i -> i < 32, Long.MAX_VALUE, 32).stageInto(hatch.inputs());
        var tile = hatch.getBaseMetaTileEntity();
        net.minecraft.util.AxisAlignedBB bounds = net.minecraft.util.AxisAlignedBB.getBoundingBox(
            tile.getXCoord(), tile.getYCoord(), tile.getZCoord(), tile.getXCoord() + 1, tile.getYCoord() + 1, tile.getZCoord() + 1);
        hatch.onBlockDestroyed();
        long dropped = 0;
        for (Object entity : h.getWorld().getEntitiesWithinAABB(net.minecraft.entity.item.EntityItem.class, bounds)) {
            ItemStack stack = ((net.minecraft.entity.item.EntityItem) entity).getEntityItem();
            if (LongStacks.sameItem(stack, type)) dropped += stack.stackSize;
        }
        h.assertEquals(amount * 2, dropped, "Both internal and external long inventories drop every chunk");
        h.assertTrue(hatch.isInputEmpty(), "Dropped external stock is cleared");
        hatch.onBlockDestroyed();
        long repeated = 0;
        for (Object entity : h.getWorld().getEntitiesWithinAABB(net.minecraft.entity.item.EntityItem.class, bounds)) {
            ItemStack stack = ((net.minecraft.entity.item.EntityItem) entity).getEntityItem();
            if (LongStacks.sameItem(stack, type)) repeated += stack.stackSize;
        }
        h.assertEquals(dropped, repeated, "Repeated destruction does not duplicate stock");
        h.succeed();
    }

    private static FakePlayer player(GameTestHelper h) {
        return new FakePlayer((WorldServer) h.getWorld(), new GameProfile(java.util.UUID.randomUUID(), "LongInputTest"));
    }

    private static ModularContainer container(AEInputItemHandler input, FakePlayer player) {
        ModularContainer gui = new ModularContainer() {
            @Override
            public void detectAndSendChanges() {}
        };
        SlotGroup machine = new SlotGroup("machine", 1, true);
        SlotGroup players = new SlotGroup("players", 0, true);
        gui.registerSlot("test", new AEInputSlot(input, 0).slotGroup(machine));
        PlayerMainInvWrapper playerInventory = new PlayerMainInvWrapper(player.inventory);
        for (int i = 0; i < playerInventory.getSlots(); i++)
            gui.registerSlot("test", new ModularSlot(playerInventory, i).slotGroup(players));
        return gui;
    }

    private static PatternDualInputHatch hatch(GameTestHelper h) {
        ChancePatternTests.buildSifter(h, true, Config.metaTileEntityOffset + Registration.ExPatternOffset);
        h.gtnh().multiBlockController(ChancePatternTests.SIFTER).getBaseMetaTileEntity().disableWorking();
        PatternDualInputHatch hatch = (PatternDualInputHatch) h.gtnh().metaTileEntity(ChancePatternTests.HATCH);
        hatch.skipActiveCheck = true;
        return hatch;
    }

    private static ICraftingPatternDetails details(GameTestHelper h, IAEStack<?>[] inputs) {
        ItemStack raw = PalladiumLineTests.Line.pattern(inputs,
            new IAEStack<?>[] {AEItemStack.create(new ItemStack(Blocks.glass))}, false);
        return ((ICraftingPatternItem) raw.getItem()).getPatternForItem(raw, h.getWorld());
    }

    private static MEInventoryCrafting table(IAEStack<?>[] inputs) {
        MEInventoryCrafting table = new MEInventoryCrafting(new ContainerNull(), inputs.length, 1);
        for (int i = 0; i < inputs.length; i++) table.setInventorySlotContents(i, inputs[i].copy());
        return table;
    }

    static void consume(GameTestHelper h, PatternDualInputHatch hatch, MTEMultiBlockBase controller,
        DualInvBuffer buffer, GTRecipe recipe, int runs) {
        hatch.startRecipeProcessing();
        try {
            h.assertTrue(recipe.isRecipeInputEqual(true, false, runs, buffer.getFluidInputs(), buffer.getItemInputs()),
                "The GT recipe consumer should process " + runs + " runs");
        } finally {
            hatch.endRecipeProcessing(controller);
        }
    }

    static long total(FluidTankG[] tanks, FluidStack fluid) {
        long amount = 0;
        for (FluidTankG tank : tanks)
            if (tank.getFluid() != null && tank.getFluid().isFluidEqual(fluid)) amount += tank.getFluidAmount();
        return amount;
    }

    static NBTTagCompound ratio(PatternDualInputHatch hatch, DualInvBuffer buffer) {
        Object copies = hatch.getPossibleCopies(buffer);
        NBTTagCompound result = new NBTTagCompound();
        long n = (long) field(copies, "num"), d = (long) field(copies, "den");
        result.setLong("num", n);
        result.setLong("den", d);
        result.setLong("broken", n < 0 ? 1 : 0);
        return result;
    }

    static Object field(Object target, String name) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) try {
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (NoSuchFieldException e) {
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        throw new AssertionError("No field " + name);
    }
}
