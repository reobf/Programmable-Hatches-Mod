package reobf.proghatches.gametests;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;
import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;
import static com.gtnewhorizons.horizonqa.api.TestPos.at;
import static reobf.proghatches.gametests.ChancePatternTests.ae;
import static reobf.proghatches.gametests.ChancePatternTests.place;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Future;
import appeng.api.networking.crafting.ICraftingJob;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import com.hfstudio.guidenh.guide.internal.item.RegionWandExporter;
import com.hfstudio.guidenh.guide.internal.structure.GuideTextNbtCodec;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEColor;
import appeng.util.item.AEFluidStack;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.metatileentity.implementations.MTEHatchEnergyDebug;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import tectech.thing.CustomItemList;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"guidenh"})
public class FluidStockingGuideTests {

    private static final TestPos EBF = at(3, 0, 0), HATCH = at(2, 0, 2);
    private static final long STOCK = 100_000;
    private static final BaseActionSource SOURCE = new BaseActionSource();

    @GameTest(template = "fluid_stocking", timeoutTicks = 2400)
    public static void furnaceChangesGasWithoutChangingThePattern(GameTestHelper helper) {
        Run r = new Run(helper);
        helper.startSequence()
            .thenWaitUntil("wait for the ME network and pattern", 200, r::ready)
            .thenExecute("install the upgrade and order the gas-free pattern", r::begin)
            .thenWaitUntil("calculate the nitrogen order", 200, r::trySubmit)
            .thenWaitUntil("wait for the solid inputs", 200, () ->
                helper.assertTrue(r.hatch.inv0.stream().anyMatch(b -> !b.chanceContents().isEmpty()),
                    "The CPU should deliver the solids without requesting gas"))
            .thenExecuteFor(40, () -> {
                helper.assertEquals(0, r.machine.mMaxProgresstime, "The furnace should wait for a marked gas");
                r.gases(STOCK, STOCK);
            })
            .thenExecute("mark nitrogen", () -> r.mark(Materials.Nitrogen.getGas(1), 280, "nitrogen"))
            .thenWaitUntil("finish the nitrogen recipe", 850, () -> r.finished(1, STOCK - 8000, STOCK))
            .thenExecute("record nitrogen and switch only the mark", () -> {
                r.scene.frame(440, "nitrogenDone", r.snapshot(), true);
                r.mark(Materials.Argon.getGas(1), 560, "argon");
                r.submit();
            })
            .thenWaitUntil("calculate the argon order", 200, r::trySubmit)
            .thenWaitUntil("finish the argon recipe", 850, () -> r.finished(2, STOCK - 8000, STOCK - 6800))
            .thenExecute("export the gas switching tutorial", () -> {
                r.scene.frame(720, "argonDone", r.snapshot(), true);
                r.scene.frame(820, "samePattern", r.snapshot(), true);
                r.scene.frame(940, "done", null, true);
                r.scene.finish(r);
            })
            .thenSucceed();
    }

    private static final class Run {
        final GameTestHelper h;
        final PatternDualInputHatch hatch;
        final MTEMultiBlockBase machine;
        final ProgrammingCircuitProvider provider;
        final ItemStack output = ItemList.Circuit_Silicon_Ingot2.get(1);
        final GTRecipe nitrogen, argon;
        final ItemStack pattern;
        final Scene scene;
        Future<ICraftingJob> pendingJob;

        Run(GameTestHelper h) {
            this.h = h;
            nitrogen = recipe(Materials.Nitrogen.getGas(1));
            argon = recipe(Materials.Argon.getGas(1));
            h.assertEquals(8000, nitrogen.mFluidInputs[0].amount, "Nitrogen use");
            h.assertEquals(6800, argon.mFluidInputs[0].amount, "Argon use");
            h.assertEquals(12000, nitrogen.mDuration, "Nitrogen base duration");
            h.assertEquals(9600, argon.mDuration, "Argon base duration");
            h.assertTrue(Arrays.equals(nitrogen.mInputs, argon.mInputs)
                || sameInputs(nitrogen.mInputs, argon.mInputs), "Both gas recipes should have identical solid inputs");
            var controller = place(h, EBF, ItemList.Machine_Multi_BlastFurnace, ForgeDirection.NORTH);
            ((IConstructable) controller.getMetaTileEntity()).construct(ItemList.Machine_Multi_BlastFurnace.get(1), false);
            for (int y = 1; y <= 2; y++) for (int x = 2; x <= 4; x++) for (int z = 0; z <= 2; z++)
                if (x != 3 || z != 1) h.setBlock(x, y, z, GregTechAPI.sBlockCasings5, 1);
            hatch = (PatternDualInputHatch) place(h, HATCH, Config.metaTileEntityOffset + Registration.PatternOffset,
                ForgeDirection.WEST).getMetaTileEntity();
            place(h, at(2, 0, 1), ItemList.Hatch_Output_Bus_ME, ForgeDirection.WEST);
            var energy = (MTEHatchEnergyDebug) place(h, at(4, 0, 0), ItemList.DebugEnergyHatch,
                ForgeDirection.NORTH).getMetaTileEntity();
            energy.setVoltageTier(8); energy.setAmperage(2); energy.setEUVar(Long.MAX_VALUE);
            place(h, at(4, 0, 2), CustomItemList.hatch_CreativeMaintenance, ForgeDirection.EAST);
            place(h, at(3, 3, 1), ItemList.Hatch_Muffler_LV, ForgeDirection.UP);
            for (TestPos p : new TestPos[] { at(1, 0, 1), at(1, 0, 2) }) {
                TestPos a = h.absolute(p);
                h.getWorld().setBlock(a.x(), a.y(), a.z(), AEApi.instance().definitions().blocks().multiPart().maybeBlock().get(), 0, 3);
                ((IPartHost) h.getWorld().getTileEntity(a.x(), a.y(), a.z())).addPart(
                    AEApi.instance().definitions().parts().cableGlass().stack(AEColor.Transparent, 1), ForgeDirection.UNKNOWN, null);
            }
            provider = (ProgrammingCircuitProvider) place(h, at(0, 0, 3),
                Config.metaTileEntityOffset + Registration.CircuitProviderOffset, ForgeDirection.NORTH).getMetaTileEntity();
            provider.setInventorySlotContents(0, GTUtility.getIntegratedCircuit(3));
            h.assertTrue(h.gtnh().multiblock(EBF).forceStructureCheck(), "The real EBF should form");
            machine = h.gtnh().multiBlockController(EBF);
            ItemStack cell = AEApi.instance().definitions().items().cell64k().maybeStack(1).get();
            @SuppressWarnings("unchecked")
            IMEInventoryHandler<IAEItemStack> items = AEApi.instance().registries().cell().getHandler(cell)
                .getCellInventory(cell, null, ITEM_STACK_TYPE);
            ArrayList<IAEStack<?>> inputs = new ArrayList<>();
            for (ItemStack s : nitrogen.mInputs) {
                if (GTUtility.isAnyIntegratedCircuit(s)) {
                    inputs.add(PalladiumLineTests.Line.circuit(s.getItemDamage()));
                } else {
                    inputs.add(ae(s, s.stackSize));
                    h.assertNull(items.injectItems(ae(s, s.stackSize * 4L), Actionable.MODULATE, SOURCE), "Stock should fit");
                }
            }
            pattern = PalladiumLineTests.Line.pattern(inputs.toArray(new IAEStack<?>[0]), new IAEStack<?>[] { ae(output, 1) }, false);
            hatch.getPatterns().setInventorySlotContents(0, pattern.copy());
            h.setSlot("drive", 0, cell);
            h.setSlot("drive", 1, fluidCell(Materials.Nitrogen.getGas(STOCK)));
            h.setSlot("drive", 2, fluidCell(Materials.Argon.getGas(STOCK)));
            scene = new Scene(h);
        }

        GTRecipe recipe(FluidStack gas) {
            return RecipeMaps.blastFurnaceRecipes.getAllRecipes().stream()
                .filter(r -> r.mOutputs.length == 1 && GTUtility.areStacksEqual(r.mOutputs[0], output)
                    && r.mFluidInputs.length == 1 && r.mFluidInputs[0].isFluidEqual(gas))
                .findFirst().orElseThrow(() -> new AssertionError("The silicon recipe with " + gas.getLocalizedName() + " should exist"));
        }

        boolean sameInputs(ItemStack[] a, ItemStack[] b) {
            if (a.length != b.length) return false;
            for (int i = 0; i < a.length; i++) if (!ItemStack.areItemStacksEqual(a[i], b[i])) return false;
            return true;
        }

        @SuppressWarnings("unchecked")
        ItemStack fluidCell(FluidStack fluid) {
            ItemStack cell = new ItemStack(ItemAndBlockHolder.CELL64K);
            IMEInventoryHandler<IAEFluidStack> inv = AEApi.instance().registries().cell().getHandler(cell)
                .getCellInventory(cell, null, FLUID_STACK_TYPE);
            h.assertNull(inv.injectItems(AEFluidStack.create(fluid), Actionable.MODULATE, SOURCE), "Gas should fit");
            return cell;
        }

        void ready() {
            try {
                h.assertTrue(hatch.getProxy().isActive() && provider.getProxy().isActive(), "The input hatch and provider need channels");
                h.assertFalse(hatch.getProxy().getCrafting().getCpus().isEmpty(), "The CPU should form");
                h.assertFalse(hatch.getProxy().getCrafting().getCraftingFor(ae(output, 1), null, -1, h.getWorld()).isEmpty(), "The pattern should be offered");
            } catch (Exception e) { throw new AssertionError(e); }
        }

        void begin() {
            scene.begin();
            scene.frame(0, "overview", null, true);
            ItemStack upgrade = new ItemStack(MyMod.upgrades, 1, 2);
            hatch.shared.install(upgrade);
            h.assertEquals(0, upgrade.stackSize, "Installing the fluid upgrade should consume it");
            h.assertEquals(1, hatch.shared.fluidMEUpgrades, "One fluid marking slot should be available");
            scene.frame(100, "install", snapshot(), true);
            scene.frame(180, "pattern", snapshot(), true);
            submit();
        }

        void submit() {
            try {
                var craft = hatch.getProxy().getCrafting();
                pendingJob = craft.beginCraftingJob(h.getWorld(), hatch.getProxy().getGrid(), SOURCE, ae(output, 1), null);
            } catch (Exception e) { throw new AssertionError(e); }
        }

        void trySubmit() {
            h.assertTrue(pendingJob.isDone(), "Waiting for AE crafting calculation");
            try {
                var craft = hatch.getProxy().getCrafting();
                var job = pendingJob.get();
                h.assertFalse(job.isSimulation(), "Gas-free solid pattern should be executable");
                h.assertNotNull(craft.submitJob(job, null, null, false, SOURCE), "The CPU should accept the order");
            } catch (Exception e) { throw new AssertionError(e); }
        }

        void mark(FluidStack gas, int time, String label) {
            var tank = hatch.shared.createTankForFluidStack(0, 1);
            tank.drain(Integer.MAX_VALUE, true);
            if (time == 560) scene.frame(500, "clear", snapshot(), true);
            h.assertEquals(1, tank.fill(gas.copy(), true), "The phantom slot should record the gas");
            hatch.markDirty();
            scene.frame(time, label, snapshot(), true);
        }

        long gas(FluidStack type) {
            try {
                var query = AEFluidStack.create(type); query.setStackSize(Long.MAX_VALUE);
                var found = hatch.getProxy().getStorage().getFluidInventory().extractItems(query, Actionable.SIMULATE, SOURCE);
                return found == null ? 0 : found.getStackSize();
            } catch (Exception e) { throw new AssertionError(e); }
        }

        void gases(long n, long a) {
            h.assertEquals(n, gas(Materials.Nitrogen.getGas(1)), "Nitrogen stock should match actual consumption");
            h.assertEquals(a, gas(Materials.Argon.getGas(1)), "Argon stock should match actual consumption");
        }

        void finished(int count, long n, long a) {
            try {
                IAEItemStack found = hatch.getProxy().getStorage().getItemInventory().extractItems(ae(output, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE);
                h.assertEquals((long) count, found == null ? 0 : found.getStackSize(), "The finished silicon crystal should return to ME");
                for (ICraftingCPU cpu : hatch.getProxy().getCrafting().getCpus()) h.assertFalse(cpu.isBusy(), "The CPU should finish");
                h.assertEquals(0, machine.mMaxProgresstime, "The furnace should finish");
                gases(n, a);
                h.assertTrue(ItemStack.areItemStacksEqual(pattern, hatch.getPatterns().getStackInSlot(0)), "The pattern NBT should remain unchanged");
                for (var tank : hatch.mStoredFluid) h.assertTrue(tank.getFluidAmount() == 0, "Gas should come from ME, not the input tanks");
            } catch (Exception e) { throw new AssertionError(e); }
        }

        NBTTagCompound snapshot() {
            NBTTagCompound tag = new NBTTagCompound();
            hatch.getBaseMetaTileEntity().writeToNBT(tag);
            coordinates(tag, HATCH);
            NBTTagCompound stocks = new NBTTagCompound();
            FluidStack marked = hatch.shared.createTankForFluidStack(0, 1).getFluid();
            if (marked != null) {
                var stock = AEFluidStack.create(marked); stock.setStackSize(gas(marked));
                NBTTagCompound value = new NBTTagCompound(); stock.writeToNBT(value);
                stocks.setTag("i0", value);
            }
            tag.setTag("phGuideFluidStock", stocks);
            return tag;
        }
    }

    private static final class Scene {
        final GameTestHelper h;
        final JsonArray frames = new JsonArray();
        NBTTagCompound structure;
        Scene(GameTestHelper h) { this.h = h; }

        void begin() {
            try {
                TestPos o = h.absolute(at(0, 0, 0));
                String snbt = RegionWandExporter.exportRegionAsStructureSnbt(h.getWorld(), o.x(), o.y(), o.z(), 5, 4, 4);
                structure = GuideTextNbtCodec.readStructureNbt(snbt.getBytes(StandardCharsets.UTF_8));
                NBTTagList blocks = structure.getTagList("blocks", 10);
                for (int i = 0; i < blocks.tagCount(); i++) {
                    NBTTagCompound b = blocks.getCompoundTagAt(i);
                    if (!b.hasKey("nbt")) continue;
                    NBTBase raw = b.getTag("pos");
                    int[] p;
                    if (raw instanceof NBTTagIntArray a) p = a.func_150302_c();
                    else {
                        NBTTagList list = (NBTTagList) raw.copy(); p = new int[list.tagCount()];
                        for (int j = 0; j < p.length; j++) p[j] = ((NBTBase.NBTPrimitive) list.removeTag(0)).func_150287_d();
                    }
                    coordinates(b.getCompoundTag("nbt"), at(p[0], p[1], p[2]));
                }
            } catch (Exception e) { throw new AssertionError(e); }
        }

        void frame(int tick, String label, NBTTagCompound tag, boolean caption) {
            JsonObject f = new JsonObject();
            f.addProperty("time", tick); f.addProperty("labelKey", "proghatches.guide.fluid.label." + label);
            JsonArray notes = new JsonArray();
            if (caption) {
                JsonObject note = new JsonObject();
                note.addProperty("type", "text"); note.addProperty("textKey", "proghatches.guide.fluid." + label);
                note.addProperty("independent", true); note.addProperty("yOffset", 1000);
                note.addProperty("maxWidth", 250); note.addProperty("color", "0xFF7FC47F"); notes.add(note);
            }
            if (label.equals("install")) {
                JsonObject click = new JsonObject(); click.addProperty("type", "input");
                click.addProperty("x", 2.0); click.addProperty("y", .5); click.addProperty("z", 2.5);
                click.addProperty("inputType", "rmb"); click.addProperty("item", "programmablehatches:prog_upgrades:2"); notes.add(click);
            }
            f.add("annotations", notes);
            if (tag != null) {
                JsonArray merge = new JsonArray(), replace = new JsonArray();
                JsonObject op = location(); op.addProperty("nbt", GuideTextNbtCodec.writeTextSafeCompound(tag)); merge.add(op);
                for (String key : tag.func_150296_c()) if (tag.getTag(key) instanceof NBTTagCompound child) {
                    op = location(); op.addProperty("path", key); op.addProperty("value", GuideTextNbtCodec.writeTextSafeCompound(child)); replace.add(op);
                }
                f.add("mergeTileNBT", merge); f.add("modifyTileNBT", replace);
            }
            frames.add(f);
        }

        JsonObject location() {
            JsonObject o = new JsonObject(); o.addProperty("x", HATCH.x()); o.addProperty("y", HATCH.y()); o.addProperty("z", HATCH.z()); return o;
        }

        void finish(Run r) {
            String output = System.getProperty("ph.fluidGuideExport");
            if (output == null) return;
            try {
                Path dir = Path.of(output); Files.createDirectories(dir);
                Files.writeString(dir.resolve("fluid_stocking.snbt"), GuideTextNbtCodec.writeStructureSnbt(structure), StandardCharsets.UTF_8);
                JsonObject root = new JsonObject(); root.addProperty("$schema", "guidenh://schema/ponder.json");
                root.addProperty("totalTime", 1020); root.add("keyframes", frames);
                var gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                Files.writeString(dir.resolve("fluid_stocking_ponder.json"), gson.toJson(root) + "\n", StandardCharsets.UTF_8);
                JsonObject ids = new JsonObject();
                ids.addProperty("output", net.minecraft.item.Item.itemRegistry.getNameForObject(r.output.getItem()) + ":" + r.output.getItemDamage());
                JsonArray in = new JsonArray();
                for (ItemStack s : r.nitrogen.mInputs) in.add(new com.google.gson.JsonPrimitive(s.getDisplayName() + " x" + s.stackSize
                    + " = " + net.minecraft.item.Item.itemRegistry.getNameForObject(s.getItem()) + ":" + s.getItemDamage()));
                ids.add("inputs", in);
                Files.writeString(dir.resolve("items.json"), gson.toJson(ids), StandardCharsets.UTF_8);
            } catch (Exception e) { throw new AssertionError(e); }
        }
    }

    private static void coordinates(NBTTagCompound tag, TestPos p) {
        tag.setInteger("x", p.x()); tag.setInteger("y", p.y()); tag.setInteger("z", p.z());
    }
}
