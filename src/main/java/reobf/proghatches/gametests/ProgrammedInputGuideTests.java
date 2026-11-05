package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import com.hfstudio.guidenh.guide.internal.item.RegionWandExporter;
import com.hfstudio.guidenh.guide.internal.structure.GuideTextNbtCodec;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEColor;
import appeng.tile.misc.TileInterface;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"guidenh"})
public class ProgrammedInputGuideTests {

    private static final TestPos HATCH = at(3, 1, 1), PROVIDER = at(1, 1, 2);
    private static final FluidStack[] NO_FLUIDS = {};

    @GameTest(template = "programmed_inputs", timeoutTicks = 300)
    public static void plainHatchUsesTheProvidedMold(GameTestHelper helper) {
        demonstrate(helper, false);
    }

    @GameTest(template = "programmed_inputs", timeoutTicks = 300)
    public static void bufferedHatchKeepsTheMoldWithItsRecipe(GameTestHelper helper) {
        demonstrate(helper, true);
    }

    private static void demonstrate(GameTestHelper h, boolean buffered) {
        ItemStack mold = ItemList.Shape_Extruder_Rod.get(1);
        ItemStack plateMold = ItemList.Shape_Extruder_Plate.get(1);
        ItemStack ingot = new ItemStack(Items.iron_ingot);
        ItemStack rod = GTOreDictUnificator.get(OrePrefixes.stick, Materials.Iron, 2);
        ItemStack plate = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Iron, 1);
        ItemStack circuit = ItemProgrammingCircuit.wrap(mold);
        int id = Config.metaTileEntityOffset + (buffered ? Registration.BufferedDualInputHatchOffset
            : Registration.DualInputHatchOffset) + 1;
        DualInputHatch hatch = (DualInputHatch) ChancePatternTests.place(h, HATCH, id, ForgeDirection.WEST).getMetaTileEntity();
        ProgrammingCircuitProvider provider = (ProgrammingCircuitProvider) ChancePatternTests.place(h, PROVIDER,
            Config.metaTileEntityOffset + Registration.CircuitProviderOffset, ForgeDirection.NORTH).getMetaTileEntity();
        TileInterface iface = h.assertTileEntityPresent(TileInterface.class, "interface");
        iface.setOrientation(ForgeDirection.NORTH, ForgeDirection.WEST);
        iface.getConfigManager().putSetting(Settings.BLOCK, YesNo.YES);
        TestPos cable = h.absolute(at(1, 1, 1));
        h.getWorld().setBlock(cable.x(), cable.y(), cable.z(),
            AEApi.instance().definitions().blocks().multiPart().maybeBlock().get(), 0, 3);
        IPartHost host = (IPartHost) h.getWorld().getTileEntity(cable.x(), cable.y(), cable.z());
        host.addPart(AEApi.instance().definitions().parts().cableGlass().stack(AEColor.Transparent, 1),
            ForgeDirection.UNKNOWN, null);
        h.setSlot("drive", 0, AEApi.instance().definitions().items().cell64k().maybeStack(1).get());
        Scene scene = new Scene(h, buffered ? "buffered_inputs" : "programmed_inputs");
        h.startSequence()
            .thenWaitUntil("wait for the circuit provider", 120,
                () -> h.assertTrue(provider.getProxy().isActive(), "The provider should have a channel"))
            .thenExecute("verify and capture the programming workflow", () -> {
                try {
                    scene.begin();
                    scene.frame(0, "overview", null);
                    h.assertTrue(provider.hasMui2Gui(), "The provider should expose its MUI2 GUI");
                    scene.frame(60, "providerGui", null);
                    provider.setInventorySlotContents(0, mold.copy());
                    scene.frame(100, "template", tag(provider.getBaseMetaTileEntity(), PROVIDER));
                    provider.setInventorySlotContents(1, plateMold.copy());
                    scene.frame(150, "secondTemplate", tag(provider.getBaseMetaTileEntity(), PROVIDER));
                    List<ICraftingPatternDetails> offered = new ArrayList<>();
                    provider.provideCrafting(new ICraftingProviderHelper() {
                        public void addCraftingOption(ICraftingMedium medium, ICraftingPatternDetails p) { offered.add(p); }
                        public void setEmitable(IAEItemStack s) {}
                        public void setEmitable(IAEStack<?> s) {}
                    });
                    ICraftingPatternDetails supplied = offered.stream()
                        .filter(p -> ItemStack.areItemStacksEqual(circuit, p.getOutputs()[0].getItemStack()))
                        .findFirst().orElseThrow(() -> new AssertionError("The template should offer its programming circuit"));
                    h.assertTrue(provider.pushPattern(supplied, new InventoryCrafting(null, 1, 1)), "The provider should supply a circuit");
                    provider.returnItems();
                    IAEItemStack produced = provider.getProxy().getStorage().getItemInventory().extractItems(
                        ChancePatternTests.ae(circuit, 1), Actionable.MODULATE, new appeng.api.networking.security.BaseActionSource());
                    h.assertNotNull(produced, "The generated circuit should reach ME storage");
                    h.assertTrue(ItemStack.areItemStacksEqual(mold, provider.getStackInSlot(0)), "The real mold template should stay");
                    scene.frame(200, "provided", null);
                    ItemStack pattern = PalladiumLineTests.Line.pattern(
                        new IAEStack<?>[] { ChancePatternTests.ae(ingot, 1), ChancePatternTests.ae(circuit, 1) },
                        new IAEStack<?>[] { ChancePatternTests.ae(rod, 2) }, false);
                    iface.getInventoryByName("patterns").setInventorySlotContents(0, pattern);
                    iface.getInventoryByName("patterns").setInventorySlotContents(1, PalladiumLineTests.Line.pattern(
                        new IAEStack<?>[] { ChancePatternTests.ae(ingot, 1), ChancePatternTests.ae(ItemProgrammingCircuit.wrap(plateMold), 1) },
                        new IAEStack<?>[] { ChancePatternTests.ae(plate, 1) }, false));
                    scene.patch(scene.frame(280, "pattern", null), at(2, 1, 1), tag(iface, at(2, 1, 1)));
                    GTRecipe recipe = RecipeMaps.extruderRecipes.getAllRecipes().stream()
                        .filter(r -> r.mOutputs.length == 1 && GTUtility.areStacksEqual(r.mOutputs[0], rod)
                            && r.mOutputs[0].stackSize == 2
                            && r.isRecipeInputEqual(false, NO_FLUIDS, ingot.copy(), mold.copy()))
                        .findFirst().orElseThrow(() -> new AssertionError("The guide's iron rod extrusion recipe should exist"));
                    h.assertTrue(Arrays.stream(recipe.mInputs).anyMatch(s -> s != null && s.stackSize == 0
                        && GTUtility.areStacksEqual(s, mold)), "The recipe should mark the mold non-consumable");
                    stage(hatch, ingot, circuit);
                    scene.frame(360, "delivered", tag(hatch.getBaseMetaTileEntity(), HATCH));
                    if (hatch instanceof BufferedDualInputHatch b) {
                        h.assertNotNull(b.classifyForce(), "One complete input set should enter a buffer");
                        h.assertTrue(b.isInputEmpty(), "The external input area should be clear");
                        scene.frame(440, "buffered", tag(hatch.getBaseMetaTileEntity(), HATCH));
                        stage(hatch, ingot, circuit);
                        h.assertNotNull(b.classifyForce(), "A repeated input set should join the same buffer");
                        h.assertEquals(2L, count(b.inv0.get(0).getItemInputs(), ingot), "The buffer should hold two iron ingots");
                        h.assertTrue(has(b.inv0.get(0).getItemInputs(), mold), "The buffer should contain the virtual mold");
                        h.assertFalse(has(b.inv0.get(0).getItemInputs(), circuit), "The wrapper should be consumed");
                        scene.frame(600, "accumulated", tag(hatch.getBaseMetaTileEntity(), HATCH));
                        ItemStack[] inputs = copy(b.inv0.get(0).getItemInputs());
                        h.assertTrue(recipe.isRecipeInputEqual(true, NO_FLUIDS, inputs), "The real recipe should use the buffer's inputs");
                        h.assertTrue(has(inputs, mold), "Recipe consumption should keep the virtual mold");
                        h.assertEquals(1L, count(inputs, ingot), "One recipe should consume one iron ingot");
                        scene.frame(760, "bufferResult", null);
                    } else {
                        hatch.program();
                        h.assertTrue(has(hatch.theInv.getItemInputs(), mold), "The hatch should expose the mold");
                        h.assertFalse(has(hatch.theInv.getItemInputs(), circuit), "The wrapper should be consumed");
                        scene.frame(440, "programmed", tag(hatch.getBaseMetaTileEntity(), HATCH));
                        h.assertTrue(recipe.isRecipeInputEqual(true, NO_FLUIDS, hatch.theInv.getItemInputs()),
                            "The real recipe should consume the programmed hatch's input");
                        hatch.updateSlots();
                        h.assertEquals(0L, count(hatch.theInv.getItemInputs(), ingot), "The iron ingot should be consumed");
                        h.assertTrue(has(hatch.theInv.getItemInputs(), mold), "The virtual mold should remain");
                        scene.frame(600, "plainResult", tag(hatch.getBaseMetaTileEntity(), HATCH));
                        stage(hatch, ingot, ItemProgrammingCircuit.wrap(plateMold));
                        hatch.program();
                        h.assertTrue(has(hatch.theInv.getItemInputs(), plateMold), "The next pattern should select its own mold");
                        h.assertFalse(has(hatch.theInv.getItemInputs(), mold), "The previous mold should be replaced");
                        scene.frame(760, "switched", tag(hatch.getBaseMetaTileEntity(), HATCH));
                    }
                    scene.frame(900, "done", null);
                    scene.finish(ingot, rod, plate, mold, plateMold, circuit);
                } catch (Exception e) {
                    throw new AssertionError("The programmed input tutorial should execute and export", e);
                }
            })
            .thenSucceed();
    }

    private static void stage(DualInputHatch hatch, ItemStack item, ItemStack circuit) {
        if (hatch instanceof BufferedDualInputHatch b) {
            b.inputs().items[0] = ChancePatternTests.ae(item, 1);
            b.inputs().items[1] = ChancePatternTests.ae(circuit, 1);
            b.inputs().refreshViews();
        } else {
            hatch.setInventorySlotContents(0, item.copy());
            hatch.setInventorySlotContents(1, circuit.copy());
        }
    }

    private static boolean has(ItemStack[] stacks, ItemStack type) {
        return Arrays.stream(stacks).anyMatch(s -> s != null && GTUtility.areStacksEqual(s, type));
    }

    private static long count(ItemStack[] stacks, ItemStack type) {
        return Arrays.stream(stacks).filter(s -> s != null && GTUtility.areStacksEqual(s, type)).mapToLong(s -> s.stackSize).sum();
    }

    private static ItemStack[] copy(ItemStack[] stacks) {
        return Arrays.stream(stacks).map(s -> s == null ? null : s.copy()).toArray(ItemStack[]::new);
    }

    private static NBTTagCompound tag(Object tile, TestPos pos) {
        NBTTagCompound nbt = new NBTTagCompound();
        ((TileEntity) tile).writeToNBT(nbt);
        nbt.setInteger("x", pos.x()); nbt.setInteger("y", pos.y()); nbt.setInteger("z", pos.z());
        return nbt;
    }

    private static String spec(ItemStack s) {
        return Item.itemRegistry.getNameForObject(s.getItem()) + ":" + s.getItemDamage()
            + (s.hasTagCompound() ? ":" + s.getTagCompound() : "");
    }

    private static final class Scene {

        final GameTestHelper helper;
        final String name;
        final JsonArray frames = new JsonArray();
        NBTTagCompound structure;

        Scene(GameTestHelper helper, String name) { this.helper = helper; this.name = name; }

        void begin() throws Exception {
            TestPos origin = helper.absolute(at(0, 0, 0));
            String snbt = RegionWandExporter.exportRegionAsStructureSnbt(helper.getWorld(), origin.x(), origin.y(), origin.z(), 5, 3, 4);
            structure = GuideTextNbtCodec.readStructureNbt(snbt.getBytes(StandardCharsets.UTF_8));
            NBTTagList blocks = structure.getTagList("blocks", 10);
            for (int i = 0; i < blocks.tagCount(); i++) {
                NBTTagCompound b = blocks.getCompoundTagAt(i);
                if (!b.hasKey("nbt")) continue;
                int[] p = pos(b);
                NBTTagCompound t = b.getCompoundTag("nbt");
                t.setInteger("x", p[0]); t.setInteger("y", p[1]); t.setInteger("z", p[2]);
            }
        }

        JsonObject frame(int time, String key, NBTTagCompound nbt) {
            JsonObject f = new JsonObject();
            f.addProperty("time", time);
            f.addProperty("labelKey", "proghatches.guide.input.label." + key);
            JsonObject text = new JsonObject();
            text.addProperty("type", "text");
            text.addProperty("textKey", "proghatches.guide.input." + key);
            text.addProperty("independent", true);
            text.addProperty("yOffset", 1000);
            text.addProperty("maxWidth", 250);
            text.addProperty("color", "0xFF7FC47F");
            JsonArray notes = new JsonArray();
            notes.add(text);
            if (key.equals("template")) {
                JsonObject click = new JsonObject();
                click.addProperty("type", "input");
                click.addProperty("x", 1.5); click.addProperty("y", 2.0); click.addProperty("z", 2.5);
                click.addProperty("inputType", "rmb");
                click.addProperty("item", spec(ItemList.Shape_Extruder_Rod.get(1)));
                notes.add(click);
            }
            f.add("annotations", notes);
            if (nbt != null) patch(f, key.equals("template") || key.equals("secondTemplate") ? PROVIDER : HATCH, nbt);
            frames.add(f);
            return f;
        }

        void patch(JsonObject frame, TestPos p, NBTTagCompound nbt) {
            JsonObject change = new JsonObject();
            change.addProperty("x", p.x()); change.addProperty("y", p.y()); change.addProperty("z", p.z());
            change.addProperty("nbt", GuideTextNbtCodec.writeTextSafeCompound(nbt));
            JsonArray ops = new JsonArray(); ops.add(change);
            frame.add("mergeTileNBT", ops);
            JsonArray replace = new JsonArray();
            for (String key : nbt.func_150296_c()) if (nbt.getTag(key) instanceof NBTTagCompound child) {
                JsonObject op = new JsonObject();
                op.addProperty("x", p.x()); op.addProperty("y", p.y()); op.addProperty("z", p.z());
                op.addProperty("path", key);
                op.addProperty("value", GuideTextNbtCodec.writeTextSafeCompound(child));
                replace.add(op);
            }
            frame.add("modifyTileNBT", replace);
        }

        void finish(ItemStack... items) throws Exception {
            String output = System.getProperty("ph.inputGuideExport");
            if (output == null) return;
            Path dir = Path.of(output); Files.createDirectories(dir);
            Files.writeString(dir.resolve(name + ".snbt"), GuideTextNbtCodec.writeStructureSnbt(structure), StandardCharsets.UTF_8);
            JsonObject root = new JsonObject();
            root.addProperty("$schema", "guidenh://schema/ponder.json");
            root.addProperty("totalTime", 960);
            root.add("keyframes", frames);
            var gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(dir.resolve(name + "_ponder.json"), gson.toJson(root) + "\n", StandardCharsets.UTF_8);
            JsonObject ids = new JsonObject();
            String[] names = { "ingot", "rod", "plate", "mold", "plateMold", "circuit" };
            for (int i = 0; i < names.length; i++) ids.addProperty(names[i], spec(items[i]));
            Files.writeString(dir.resolve(name + "_items.json"), gson.toJson(ids) + "\n", StandardCharsets.UTF_8);
        }

        private static int[] pos(NBTTagCompound b) {
            NBTBase raw = b.getTag("pos");
            if (raw instanceof NBTTagIntArray a) return a.func_150302_c();
            NBTTagList l = (NBTTagList) raw.copy();
            int[] p = new int[l.tagCount()];
            for (int i = 0; i < p.length; i++) p[i] = ((NBTBase.NBTPrimitive) l.removeTag(0)).func_150287_d();
            return p;
        }
    }
}
