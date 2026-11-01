package reobf.proghatches.gametests;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;
import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.tuple.Pair;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.tile.crafting.TileCraftingTile;
import codechicken.nei.ItemStackMap;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.GTValues;
import gregtech.api.metatileentity.implementations.MTEHatchEnergyDebug;
import gregtech.api.util.GTRecipe;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.chance.ChanceData;
import reobf.proghatches.gt.metatileentity.util.LongStacks;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import tectech.thing.block.BlockGodforgeGlass;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"dreamcraft"})
public class GravitationalLensTests {
    private static final int MULTIPLIER = 32768;
    private static final BaseActionSource SOURCE = new BaseActionSource();

    @GameTest(template = "sifter", timeoutTicks = 20000)
    public static void realRecipeFinishesOneOptimizedBatch(GameTestHelper h) {
        ItemStack output = new ItemStack(BlockGodforgeGlass.INSTANCE);
        GTRecipe.RecipeAssemblyLine recipe = GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes.stream()
            .filter(r -> r.mOutput != null && r.mOutput.isItemEqual(output))
            .findFirst().orElseThrow(() -> new AssertionError("The pack must register the real gravitational lens recipe"));
        h.assertEquals(12, recipe.mInputs.length, "The real recipe has twelve item inputs");
        h.assertEquals(3, recipe.mFluidInputs.length, "The real recipe has three fluid inputs");
        int[] counts = {8, 8, 8, 4, 6, 6, 6, 6, 36, 6, 6, 6};
        for (int i = 0; i < counts.length; i++)
            h.assertEquals(counts[i], recipe.mInputs[i].stackSize, "Original item amount " + i);
        h.assertEquals(2304, recipe.mFluidInputs[0].amount, "Original Rhugnor amount");
        h.assertEquals(2304, recipe.mFluidInputs[1].amount, "Original Creon amount");
        h.assertEquals(147456, recipe.mFluidInputs[2].amount, "Original Advanced Nitinol amount");
        for (int i = 4; i <= 6; i++)
            h.assertEquals("dreamcraft", GameRegistry.findUniqueIdentifierFor(recipe.mInputs[i].getItem()).modId,
                "The lens must be the real CoreMod item");

        var sifter = ChancePatternTests.buildSifter(h, true, Config.metaTileEntityOffset + Registration.ExPatternOffset);
        PatternDualInputHatch hatch = (PatternDualInputHatch) h.gtnh().metaTileEntity(ChancePatternTests.HATCH);
        var controller = h.gtnh().multiBlockController(ChancePatternTests.SIFTER);
        controller.getBaseMetaTileEntity().disableWorking();
        MTEHatchEnergyDebug energy = (MTEHatchEnergyDebug) h.gtnh().metaTileEntity(ChancePatternTests.ENERGY);
        energy.setVoltageTier(GTValues.V.length - 1);
        energy.setAmperage(64);
        energy.setEUVar(Long.MAX_VALUE);

        ItemStack[] items = new ItemStack[recipe.mInputs.length];
        FluidStack[] fluids = new FluidStack[recipe.mFluidInputs.length];
        List<IAEStack<?>> inputs = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            items[i] = recipe.mInputs[i].copy();
            inputs.add(LongStacks.item(items[i], items[i].stackSize));
            MyMod.LOG.info("[LensTest] item {}: {} x {}", i, items[i].getDisplayName(), (long) items[i].stackSize * MULTIPLIER);
        }
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = recipe.mFluidInputs[i].copy();
            inputs.add(LongStacks.fluid(fluids[i], fluids[i].amount));
            MyMod.LOG.info("[LensTest] fluid {}: {} x {} mB", i, fluids[i].getLocalizedName(), (long) fluids[i].amount * MULTIPLIER);
        }
        h.gtnh().withTestRecipe(sifter, GTValues.RA.stdBuilder().itemInputs(items).fluidInputs(fluids)
            .itemOutputs(recipe.mOutput.copy()).duration(recipe.mDuration).eut(recipe.mEUt));
        MyMod.LOG.info("[LensTest] copied registered recipe: hash={}, duration={}, EU/t={}, output={}",
            recipe.getPersistentHash(), recipe.mDuration, recipe.mEUt, recipe.mOutput.getDisplayName());

        ItemStack cpu = AEApi.instance().definitions().blocks().craftingStorageSingularity().maybeStack(1).get();
        var cpuPos = h.absolute(com.gtnewhorizons.horizonqa.api.TestPos.at(0, 1, 0));
        h.getWorld().setBlock(cpuPos.x(), cpuPos.y(), cpuPos.z(), Block.getBlockFromItem(cpu.getItem()), cpu.getItemDamage(), 3);
        stock(h, items, fluids);
        ItemStack encoded = PalladiumLineTests.Line.pattern(inputs.toArray(new IAEStack<?>[0]),
            new IAEStack<?>[] {LongStacks.item(recipe.mOutput, recipe.mOutput.stackSize)}, false);
        AtomicReference<ICraftingLink> link = new AtomicReference<>();
        DualInvBuffer[] accepted = new DualInvBuffer[1];
        long[] previousRuns = {-1}, lastLogged = {-1};
        int[] observed = {0};

        h.onEachTick("check every remaining recipe ratio", () -> {
            DualInvBuffer b = accepted[0];
            if (b == null || b.isEmpty()) return;
            List<IAEStack<?>> remaining = b.chanceContents();
            long first = ChanceData.amountOf(remaining, inputs.get(0));
            h.assertEquals(0L, first % counts[0], "Remaining glass must represent whole machine runs");
            long runs = first / counts[0];
            h.assertTrue(runs <= MULTIPLIER, "The machine cannot create input");
            if (runs != previousRuns[0]) {
                for (IAEStack<?> input : inputs)
                    h.assertEquals(input.getStackSize() * runs, ChanceData.amountOf(remaining, input),
                        "The real materials must remain in the original recipe ratio: " + ChanceData.name(input));
                NBTTagCompound ratio = LongInputBufferTests.ratio(hatch, b);
                h.assertEquals(0L, ratio.getLong("broken"), "The hatch must not report inconsistent recipe quantities");
                previousRuns[0] = runs;
                observed[0]++;
                if (lastLogged[0] < 0 || lastLogged[0] - runs >= 4096) {
                    lastLogged[0] = runs;
                    MyMod.LOG.info("[LensTest] remaining runs={}, Advanced Nitinol={} mB, ratio={}", runs,
                        ChanceData.amountOf(remaining, inputs.get(inputs.size() - 1)), ratio);
                }
            }
        });

        h.startSequence()
            .thenWaitUntil("wait for ME and crafting CPU", 300, () -> {
                h.assertTrue(hatch.getProxy().isActive(), "The hatch must be connected");
                h.assertTrue(h.assertTileEntityPresent(TileCraftingTile.class, "cpu_storage").isFormed(), "CPU must form");
            })
            .thenExecute("optimize the real pattern to 32768 copies", () -> {
                hatch.getPatterns().setInventorySlotContents(0, encoded);
                hatch.setPatternMultiplier(0, 1);
                ItemStackMap<Pair<Object, Integer>> map = new ItemStackMap<>();
                map.put(encoded, Pair.of((Object) hatch, 15));
                hatch.optimize(map);
            })
            .thenWaitUntil("wait for the optimized pattern", 300, () -> {
                try {
                    var offers = hatch.getProxy().getCrafting().getCraftingFor(LongStacks.item(output, 1), null, -1, h.getWorld());
                    h.assertFalse(offers.isEmpty(), "AE must see the optimized pattern");
                    ICraftingPatternDetails details = offers.iterator().next();
                    h.assertEquals(4831838208L, ChanceData.amountOf(java.util.Arrays.asList(details.getCondensedAEInputs()),
                        inputs.get(inputs.size() - 1)), "AE must plan the full long fluid quantity");
                } catch (Exception e) { throw new AssertionError(e); }
            })
            .thenExecute("submit the full ME crafting order", () -> {
                Future<ICraftingJob> future = null;
                try {
                    var crafting = hatch.getProxy().getCrafting();
                    future = crafting.beginCraftingJob(h.getWorld(), hatch.getProxy().getGrid(), SOURCE,
                        LongStacks.item(output, (long) recipe.mOutput.stackSize * MULTIPLIER), null);
                    ICraftingJob job = future.get(10000, TimeUnit.MILLISECONDS);
                    h.assertFalse(job.isSimulation(), "Real stored materials must cover the full crafting order");
                    link.set(crafting.submitJob(job, null, null, false, SOURCE));
                    h.assertNotNull(link.get(), "The CPU must accept the job");
                } catch (Exception e) {
                    if (future != null) future.cancel(true);
                    throw new AssertionError(e);
                }
            })
            .thenWaitUntil("wait for the full batch in the input hatch", 1000, () -> {
                DualInvBuffer b = hatch.inv0.stream().filter(x -> !x.isEmpty()).findFirst()
                    .orElseThrow(() -> new AssertionError("AE has not pushed the pattern yet"));
                List<IAEStack<?>> remaining = b.chanceContents();
                for (IAEStack<?> input : inputs)
                    h.assertEquals(input.getStackSize() * MULTIPLIER, ChanceData.amountOf(remaining, input),
                        "The whole input must reach the hatch: " + ChanceData.name(input));
                accepted[0] = b;
            })
            .thenExecute("run the physical machine", () -> {
                MyMod.LOG.info("[LensTest] full 32768-copy batch received; starting physical sifter");
                controller.getBaseMetaTileEntity().enableWorking();
            })
            .thenWaitUntil("wait for all 32768 real lens blocks", 18000, () -> {
                h.assertFalse(link.get().isCanceled(), "The crafting job must not be cancelled");
                try {
                    var storage = hatch.getProxy().getStorage();
                    IAEItemStack result = storage.getItemInventory().extractItems(
                        LongStacks.item(output, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE);
                    long made = result == null ? 0 : result.getStackSize();
                    h.assertEquals((long) recipe.mOutput.stackSize * MULTIPLIER, made, "The exact number of lens blocks must reach ME");
                    for (ICraftingCPU c : hatch.getProxy().getCrafting().getCpus())
                        h.assertFalse(c.isBusy(), "The crafting CPU must finish");
                    for (DualInvBuffer b : hatch.inv0) h.assertTrue(b.isEmpty(), "No input may be left behind");
                    for (ItemStack item : items) h.assertNull(storage.getItemInventory().extractItems(
                        LongStacks.item(item, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE), "All stocked input items are used");
                    for (FluidStack fluid : fluids) h.assertNull(storage.getFluidInventory().extractItems(
                        LongStacks.fluid(fluid, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE), "All stocked fluids are used");
                    h.assertTrue(observed[0] > 1, "Several real machine batches must have been observed");
                } catch (Exception e) { throw new AssertionError(e); }
            })
            .thenExecute("report the complete real-material run", () ->
                MyMod.LOG.info("[LensTest] SUCCESS: 32768 lens blocks in ME, no leftover inputs, {} intermediate ratios checked", observed[0]))
            .thenSucceed();
    }

    @SuppressWarnings("unchecked")
    private static void stock(GameTestHelper h, ItemStack[] items, FluidStack[] fluids) {
        ItemStack cell = AEApi.instance().definitions().items().cell16384k().maybeStack(1).get();
        IMEInventoryHandler<IAEItemStack> itemInventory = AEApi.instance().registries().cell().getHandler(cell)
            .getCellInventory(cell, null, ITEM_STACK_TYPE);
        for (ItemStack input : items)
            h.assertNull(itemInventory.injectItems(LongStacks.item(input, (long) input.stackSize * MULTIPLIER),
                Actionable.MODULATE, SOURCE), "The item cell must hold all real ingredients");
        h.setSlot("drive", 0, cell);
        for (int i = 0; i < fluids.length; i++) {
            ItemStack fluidCell = new ItemStack(ItemAndBlockHolder.CELL16384K);
            IMEInventoryHandler<IAEFluidStack> fluidInventory = AEApi.instance().registries().cell().getHandler(fluidCell)
                .getCellInventory(fluidCell, null, FLUID_STACK_TYPE);
            h.assertNull(fluidInventory.injectItems(LongStacks.fluid(fluids[i], (long) fluids[i].amount * MULTIPLIER),
                Actionable.MODULATE, SOURCE), "The fluid cell must hold the full long amount");
            h.setSlot("drive", i + 1, fluidCell);
        }
    }
}
