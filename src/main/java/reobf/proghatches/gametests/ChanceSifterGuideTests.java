package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import com.hfstudio.guidenh.guide.internal.item.RegionWandExporter;
import com.hfstudio.guidenh.guide.internal.structure.GuideTextNbtCodec;

import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import reobf.proghatches.ae.TileChanceController;
import reobf.proghatches.ae.TileChanceController.RefillMode;
import reobf.proghatches.gt.metatileentity.chance.ChanceData;
import reobf.proghatches.gt.metatileentity.chance.ChanceView;
import reobf.proghatches.gt.metatileentity.chance.ChanceViewNbt;
import reobf.proghatches.main.MyMod;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"guidenh"})
public class ChanceSifterGuideTests {

    private static final TestPos HATCH = ChancePatternTests.HATCH, CONTROL = at(0, 1, 3);
    private static final long PROMISED = 15;

    @GameTest(template = "sifter", timeoutTicks = 10000)
    public static void gravelShortfallIsRefilledAndSurplusIsStored(GameTestHelper helper) {
        ChancePatternTests.Fixture f = new ChancePatternTests.Fixture(helper,
            new ItemStack(Blocks.gravel), new ItemStack(Items.flint), 1024, true);
        Run run = new Run(helper, f);
        helper.startSequence()
            .thenWaitUntil("wait for the ME network and CPU", 120, f::assertCraftingReady)
            .thenExecute("prepare the gravel chance pattern", run::prepare)
            .thenWaitUntil("wait for the flint pattern", 200,
                () -> helper.assertTrue(f.offered(), "The flint pattern should be offered"))
            .thenExecute("start a real flint order", run::start)
            .thenWaitUntil("observe a real shortfall, refill and surplus", 9400, () -> {
                run.tick();
                helper.assertTrue(run.done, "Waiting for the recorded production sequence" + f.why());
            })
            .thenSucceed();
    }

    private static final class Run {

        final GameTestHelper h;
        final ChancePatternTests.Fixture f;
        final MTEMultiBlockBase machine;
        final Scene scene;
        int stage, trials, extra;
        boolean done;
        Map<TestPos, NBTTagCompound> ordered, shortage, refilled, settled, deposit;
        long firstProduced;

        Run(GameTestHelper h, ChancePatternTests.Fixture f) {
            this.h = h; this.f = f;
            machine = h.gtnh().multiBlockController(ChancePatternTests.SIFTER);
            scene = new Scene(h, f);
        }

        void prepare() {
            machine.getBaseMetaTileEntity().disableWorking();
            GTRecipe recipe = RecipeMaps.sifterRecipes.getAllRecipes().stream()
                .filter(r -> Arrays.stream(r.mInputs).anyMatch(s -> s != null && s.getItem() == f.input.getItem())
                    && r.mOutputs.length == 6 && Arrays.stream(r.mOutputs).allMatch(s -> s != null && s.getItem() == Items.flint))
                .findFirst().orElseThrow(() -> new AssertionError("The real gravel to flint recipe should exist"));
            double mean = 0;
            for (int i = 0; i < recipe.mOutputs.length; i++) mean += recipe.mOutputs[i].stackSize * recipe.getOutputChance(i) / 10000.0;
            h.assertTrue(Math.abs(mean - 3.88) < 1e-9, "The guide expects 3.88 flint per gravel, got " + mean);
            f.chanceController().setMode(RefillMode.NONE);
            scene.begin();
            f.install(f.pattern(4, (int) PROMISED, true));
        }

        void start() {
            h.assertTrue(++trials <= 64, "A natural shortfall should occur within 64 trials");
            f.chanceController().returnDeposits();
            f.drain(f.output);
            f.chanceController().resetStats();
            f.chanceController().setMode(RefillMode.NONE);
            machine.getBaseMetaTileEntity().disableWorking();
            f.submit(PROMISED);
            stage = 1;
        }

        boolean empty() {
            return f.hatch.inv0.stream().allMatch(b -> ChanceData.amountOf(b.chanceContents(), ChancePatternTests.ae(f.input, 1)) == 0);
        }

        boolean finished() {
            return !f.cpuBusy() && f.owed() == 0 && empty() && machine.mMaxProgresstime == 0 && f.onTheWay() == 0;
        }

        void tick() {
            if (done) return;
            switch (stage) {
                case 1 -> {
                    if (f.owed() != PROMISED || empty()) return;
                    ordered = scene.snapshot();
                    machine.getBaseMetaTileEntity().enableWorking();
                    stage = 2;
                }
                case 2 -> {
                    if (empty() && machine.mMaxProgresstime == 0 && f.owed() > 0 && f.entry() != null && f.entry().produced > 0) {
                        machine.getBaseMetaTileEntity().disableWorking();
                        firstProduced = f.entry().produced;
                        h.assertTrue(firstProduced < PROMISED, "The recorded batch should have a real shortfall");
                        h.assertEquals(PROMISED - firstProduced, f.owed(), "The deficit should equal the unproduced flint");
                        shortage = scene.snapshot();
                        f.chanceController().setMode(RefillMode.STOCK);
                        stage = 3;
                    } else if (finished()) start();
                }
                case 3 -> {
                    if (f.entry() == null || f.entry().refillCopies <= 0 || empty()) return;
                    refilled = scene.snapshot();
                    machine.getBaseMetaTileEntity().enableWorking();
                    stage = 4;
                }
                case 4 -> {
                    if (!finished()) return;
                    f.assertSettled();
                    h.assertEquals(PROMISED, f.stored(f.output), "The CPU should receive exactly the promised output");
                    settled = scene.snapshot();
                    if (f.deposit() > 0) finish();
                    else nextOrder();
                }
                case 5 -> {
                    if (!finished()) return;
                    h.assertEquals(PROMISED, f.stored(f.output), "Each additional order should finish");
                    if (f.deposit() > 0) finish();
                    else nextOrder();
                }
                default -> throw new AssertionError("Unexpected recording stage");
            }
        }

        void nextOrder() {
            h.assertTrue(++extra <= 32, "A natural surplus should occur within 32 additional orders");
            f.drain(f.output);
            f.submit(PROMISED);
            stage = 5;
        }

        void finish() {
            deposit = scene.snapshot();
            h.assertTrue(f.entry().refillCopies > 0, "The controller should have replenished gravel");
            h.assertTrue(f.deposit() > 0, "Surplus flint should be stored in the controller");
            scene.finish(ordered, shortage, refilled, settled, deposit);
            MyMod.LOG.info("[SifterGuide] {} trial(s), first produced {}, deficit {}, refill copies {}, deposit {}",
                trials, firstProduced, PROMISED - firstProduced, f.entry().refillCopies, f.deposit());
            done = true;
        }
    }

    private static final class Scene {

        final GameTestHelper h;
        final ChancePatternTests.Fixture f;
        NBTTagCompound structure;

        Scene(GameTestHelper h, ChancePatternTests.Fixture f) { this.h = h; this.f = f; }

        void begin() {
            try {
                TestPos o = h.absolute(at(0, 0, 0));
                String snbt = RegionWandExporter.exportRegionAsStructureSnbt(h.getWorld(), o.x(), o.y(), o.z(), 6, 6, 5);
                structure = GuideTextNbtCodec.readStructureNbt(snbt.getBytes(StandardCharsets.UTF_8));
                NBTTagList blocks = structure.getTagList("blocks", 10);
                for (int i = 0; i < blocks.tagCount(); i++) {
                    NBTTagCompound b = blocks.getCompoundTagAt(i);
                    if (!b.hasKey("nbt")) continue;
                    int[] p = pos(b); coordinates(b.getCompoundTag("nbt"), at(p[0], p[1], p[2]));
                }
            } catch (Exception e) { throw new AssertionError("The sifter scene should export", e); }
        }

        Map<TestPos, NBTTagCompound> snapshot() {
            Map<TestPos, NBTTagCompound> result = new LinkedHashMap<>();
            for (TestPos p : new TestPos[] { HATCH, CONTROL }) {
                TestPos a = h.absolute(p);
                TileEntity tile = h.getWorld().getTileEntity(a.x(), a.y(), a.z());
                NBTTagCompound tag = new NBTTagCompound(); tile.writeToNBT(tag); coordinates(tag, p);
                if (tile instanceof TileChanceController controller) {
                    ChanceView.Screen screen = controller.screen();
                    h.assertTrue(screen.equals(ChanceViewNbt.read(ChanceViewNbt.write(screen, 0, 0, 0))),
                        "The controller screen should survive an NBT round trip");
                    TestPos origin = h.absolute(at(0, 0, 0));
                    tag.setTag(ChanceViewNbt.KEY, ChanceViewNbt.write(screen, origin.x(), origin.y(), origin.z()));
                }
                result.put(p, tag);
            }
            return result;
        }

        void finish(Map<TestPos, NBTTagCompound> ordered, Map<TestPos, NBTTagCompound> shortage,
            Map<TestPos, NBTTagCompound> refilled, Map<TestPos, NBTTagCompound> settled, Map<TestPos, NBTTagCompound> deposit) {
            String output = System.getProperty("ph.sifterGuideExport");
            if (output == null) return;
            try {
                JsonArray frames = new JsonArray();
                frames.add(frame(0, "overview", null, true));
                frames.add(frame(100, "mark", null, true));
                frames.add(frame(200, "pattern", ordered, true));
                frames.add(frame(280, "promised", ordered, true));
                frames.add(frame(360, "ordered", ordered, true));
                frames.add(frame(480, "short", shortage, true));
                frames.add(frame(560, "controller", shortage, false));
                frames.add(frame(620, "mode", shortage, false));
                frames.add(frame(680, "applied", shortage, false));
                frames.add(frame(720, "refilled", refilled, false));
                frames.add(frame(840, "settled", settled, false));
                frames.add(frame(940, "deposit", deposit, false));
                frames.add(frame(1020, "done", deposit, true));
                JsonObject root = new JsonObject();
                root.addProperty("$schema", "guidenh://schema/ponder.json"); root.addProperty("totalTime", 1100);
                root.add("keyframes", frames);
                Path dir = Path.of(output); Files.createDirectories(dir);
                Files.writeString(dir.resolve("chance_sifter.snbt"), GuideTextNbtCodec.writeStructureSnbt(structure), StandardCharsets.UTF_8);
                Files.writeString(dir.resolve("chance_sifter_ponder.json"),
                    new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root) + "\n", StandardCharsets.UTF_8);
            } catch (Exception e) { throw new AssertionError("The recorded sifter scene should save", e); }
        }

        JsonObject frame(int time, String key, Map<TestPos, NBTTagCompound> state, boolean caption) {
            JsonObject frame = new JsonObject();
            frame.addProperty("time", time); frame.addProperty("labelKey", "proghatches.guide.sifter.label." + key);
            JsonArray annotations = new JsonArray();
            if (caption) {
                JsonObject note = new JsonObject(); note.addProperty("type", "text");
                note.addProperty("textKey", "proghatches.guide.sifter." + key);
                note.addProperty("independent", true); note.addProperty("yOffset", 1000);
                note.addProperty("maxWidth", 250); note.addProperty("color", "0xFF7FC47F"); annotations.add(note);
            }
            frame.add("annotations", annotations);
            if (state == null) return frame;
            JsonArray merge = new JsonArray(), replace = new JsonArray();
            for (Map.Entry<TestPos, NBTTagCompound> e : state.entrySet()) {
                TestPos p = e.getKey(); NBTTagCompound tag = (NBTTagCompound) e.getValue().copy();
                if (p.equals(CONTROL) && time < 1020) tag.setInteger("phMode", RefillMode.NONE.ordinal());
                JsonObject op = location(p);
                op.addProperty("nbt", GuideTextNbtCodec.writeTextSafeCompound(tag)); merge.add(op);
                for (String name : tag.func_150296_c()) if (tag.getTag(name) instanceof NBTTagCompound child) {
                    op = location(p); op.addProperty("path", name);
                    op.addProperty("value", GuideTextNbtCodec.writeTextSafeCompound(child)); replace.add(op);
                }
            }
            frame.add("mergeTileNBT", merge); frame.add("modifyTileNBT", replace);
            return frame;
        }

        static JsonObject location(TestPos p) {
            JsonObject o = new JsonObject(); o.addProperty("x", p.x()); o.addProperty("y", p.y()); o.addProperty("z", p.z()); return o;
        }

        static void coordinates(NBTTagCompound nbt, TestPos p) {
            nbt.setInteger("x", p.x()); nbt.setInteger("y", p.y()); nbt.setInteger("z", p.z());
        }

        static int[] pos(NBTTagCompound b) {
            NBTBase raw = b.getTag("pos");
            if (raw instanceof NBTTagIntArray a) return a.func_150302_c();
            NBTTagList list = (NBTTagList) raw.copy(); int[] p = new int[list.tagCount()];
            for (int i = 0; i < p.length; i++) p[i] = ((NBTBase.NBTPrimitive) list.removeTag(0)).func_150287_d();
            return p;
        }
    }
}
