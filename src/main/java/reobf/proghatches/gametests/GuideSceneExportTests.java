package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import com.hfstudio.guidenh.guide.internal.item.RegionWandExporter;
import com.hfstudio.guidenh.guide.internal.structure.GuideTextNbtCodec;

import reobf.proghatches.main.MyMod;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"guidenh"})
public class GuideSceneExportTests {

    private static final int SX = 6, SY = 6, SZ = 10;
    private static final List<TestPos> REACTOR_HATCHES = Arrays
        .asList(at(1, 1, 7), at(1, 1, 6), at(1, 1, 8), at(3, 1, 6), at(2, 0, 6));
    private static final List<TestPos> SIFTER_HATCHES = Arrays.asList(
        ChancePatternTests.HATCH,
        ChancePatternTests.OUTPUT_BUS,
        ChancePatternTests.ENERGY,
        ChancePatternTests.MAINTENANCE,
        ChancePatternTests.MUFFLER);

    private static final Map<String, String> FALLBACK = new LinkedHashMap<>();

    static {
        FALLBACK.put("overview", "The palladium line. Press play to see it built step by step.");
        FALLBACK.put("me", "ME network: controller, drive, crafting CPU, Chance Pattern Controller and Programming Circuit Provider");
        FALLBACK.put("reactor", "Large Chemical Reactor, running the ammonia recycling and precipitation patterns");
        FALLBACK.put("reactorHatches", "Cyclic Pattern Crafting Input Buffer, next to the ME output bus and hatch");
        FALLBACK.put("sifter", "Large Sifter, running the sift pattern");
        FALLBACK.put("sifterHatches", "Programmable Crafting Input Buffer, receiving the sift pattern through a lane");
        FALLBACK.put("link", "Fluix ME glass cables join the hatches to one ME network");
        FALLBACK.put("recordTarget", "Left-click to record coordinates");
        FALLBACK.put("recordedTarget", "Coordinates recorded; use this data stick to bind lane 1");
        FALLBACK.put("guiMain", "Open the Cyclic Pattern Crafting Input Buffer, then click the plus button");
        FALLBACK.put("guiOpen", "Open the pattern popup: the precipitation pattern is in the first slot");
        FALLBACK.put("guiSifter", "Place the chance sifting pattern in the next slot of the same group");
        FALLBACK.put("guiEnrich", "Place the ammonia recycling pattern in the third slot");
        FALLBACK.put("guiLanes", "All three patterns start on lane 0, running in this hatch");
        FALLBACK.put("guiLaneHint", "Scroll or click to change lanes; left-click the sifting pattern to select lane 1");
        FALLBACK.put("guiLaneSet", "The sifting slot turns blue for lane 1; both chemical patterns remain on lane 0");
        FALLBACK.put("guiBind", "Open lane bindings; lane 1 is waiting for a target input hatch");
        FALLBACK.put("guiBindHint", "The data stick stores the sifter input hatch position; carry it onto the lane 1 binding slot");
        FALLBACK.put("guiBindDone", "Lane 1 is bound; its slot shows the target icon and the connected state");
        FALLBACK.put("guiLanesBound", "Sifting goes to the sifter through blue lane 1; precipitation and ammonia recycling run in this hatch");
        FALLBACK.put("guiConfig", "The configuration page shows the cycle multiplier and chance seed multiplier");
        FALLBACK.put("guiClose", "The popup closes; the three patterns remain in the cyclic hatch");
    }

    /** Builds the palladium line and writes it as a guide scene, when -Dph.guideExport names the output folder. */
    @GameTest(template = "palladium", timeoutTicks = 1000, required = false)
    public static void exportPalladiumLine(GameTestHelper helper) {
        if (System.getProperty("ph.guideExport") == null) {
            helper.succeed();
            return;
        }
        PalladiumLineTests.Line line = new PalladiumLineTests.Line(helper);
        helper.startSequence()
            .thenWaitUntil("wait for the ME network", 120, line::assertReady)
            .thenExecute("install the tutorial patterns", line::installCycle)
            .thenWaitUntil("wait for balancing", 600,
                () -> helper.assertTrue(line.offered(line.raw), "The tutorial cycle should be offered"))
            .thenExecute("load seeds", line::syncSeeds)
            .thenExecute("export", () -> {
                try {
                    export(helper);
                } catch (Exception e) {
                    throw new AssertionError("export failed", e);
                }
            })
            .thenSucceed();
    }

    private static void export(GameTestHelper helper) throws Exception {
        TestPos o = helper.absolute(at(0, 0, 0));
        String snbt = RegionWandExporter.exportRegionAsStructureSnbt(helper.getWorld(), o.x(), o.y(), o.z(), SX, SY, SZ);
        helper.assertNotNull(snbt, "the region should export");
        File dir = new File(System.getProperty("ph.guideExport"));
        dir.mkdirs();
        NBTTagCompound root = GuideTextNbtCodec.readStructureNbt(snbt.getBytes(StandardCharsets.UTF_8));
        NBTTagList palette = root.getTagList("palette", 10);
        NBTTagList blocks = root.getTagList("blocks", 10);
        Map<String, List<JsonObject>> stages = new LinkedHashMap<>();
        for (String s : new String[] { "me", "reactor", "reactorHatches", "sifter", "sifterHatches", "link" })
            stages.put(s, new ArrayList<>());
        List<JsonObject> clear = new ArrayList<>();
        NBTTagCompound patternSlots = null;
        NBTTagCompound laneState = new NBTTagCompound();
        StringBuilder census = new StringBuilder();
        for (int i = 0; i < blocks.tagCount(); i++) {
            NBTTagCompound b = blocks.getCompoundTagAt(i);
            int[] pos = posOf(b);
            if (Arrays.equals(pos, new int[] { 1, 1, 7 })) {
                NBTTagCompound tile = b.getCompoundTag("nbt");
                // PH distinguishes placed-tile NBT from item NBT by the presence of x.
                tile.setInteger("x", pos[0]);
                tile.setInteger("y", pos[1]);
                tile.setInteger("z", pos[2]);
                patternSlots = tile.getCompoundTag("patternSlots");
                laneState.setTag("cycLanes", tile.getTag("cycLanes").copy());
                laneState.setTag("cycLaneOffsets", tile.getTag("cycLaneOffsets").copy());
            }
            String name = palette.getCompoundTagAt(b.getInteger("state"))
                .getString("Name");
            JsonObject add = new JsonObject();
            add.addProperty("x", pos[0]);
            add.addProperty("y", pos[1]);
            add.addProperty("z", pos[2]);
            add.addProperty("block", name);
            if (b.hasKey("meta")) add.addProperty("meta", b.getInteger("meta"));
            if (b.hasKey("nbt")) add.addProperty(
                "nbt",
                GuideTextNbtCodec.writeTextSafeCompound(GuideTextNbtCodec.decodeCompound(b.getCompoundTag("nbt"))));
            String stage = stageOf(pos);
            stages.get(stage)
                .add(add);
            census.append(stage)
                .append(' ')
                .append(Arrays.toString(pos))
                .append(' ')
                .append(name)
                .append(':')
                .append(b.getInteger("meta"))
                .append('\n');
            if (!"me".equals(stage)) {
                JsonObject air = new JsonObject();
                air.addProperty("x", pos[0]);
                air.addProperty("y", pos[1]);
                air.addProperty("z", pos[2]);
                air.addProperty("block", "minecraft:air");
                air.addProperty("particles", false);
                clear.add(air);
            }
        }
        Files.write(new File(dir, "census.txt").toPath(), census.toString().getBytes(StandardCharsets.UTF_8));
        Files.write(new File(dir, "palladium_line.snbt").toPath(),
            GuideTextNbtCodec.writeStructureSnbt(root).getBytes(StandardCharsets.UTF_8));

        JsonArray keyframes = new JsonArray();
        keyframes.add(frame(0, "overview", null, text("overview", 3.0, 7.0, 5.0, null)));
        keyframes.add(frame(60, "me", clear, text("me", 0.5, 2.3, 2.0, new double[] { 0, 0, 0, 1, 3, 4 })));
        keyframes.add(
            frame(140, "reactor", stages.get("reactor"), text("reactor", 2.5, 3.3, 7.5, new double[] { 1, 0, 6, 4, 3, 9 })));
        keyframes.add(
            frame(
                220,
                "reactorHatches",
                stages.get("reactorHatches"),
                text("reactorHatches", 1.5, 2.3, 7.5, new double[] { 1, 1, 7, 2, 2, 8 })));
        keyframes.add(
            frame(300, "sifter", stages.get("sifter"), text("sifter", 3.5, 6.3, 2.5, new double[] { 1, 0, 0, 6, 6, 5 })));
        keyframes.add(
            frame(
                380,
                "sifterHatches",
                stages.get("sifterHatches"),
                text("sifterHatches", 1.5, 2.3, 2.5, new double[] { 1, 1, 2, 2, 2, 3 })));
        keyframes.add(camera(frame(460, "link", stages.get("link"),
            text("link", 0.5, 2.3, 7.0, new double[] { 0, 1, 4, 1, 2, 10 })), 90, 20));
        JsonObject recordTarget = frame(500, "recordTarget", null,
            text("recordTarget", 1.5, 2.8, 2.5, new double[] { 1, 1, 2, 2, 2, 3 }));
        JsonObject input = new JsonObject();
        input.addProperty("type", "input");
        input.addProperty("x", 1.0);
        input.addProperty("y", 1.5);
        input.addProperty("z", 2.5);
        input.addProperty("inputType", "lmb");
        input.addProperty("item", "gregtech:gt.metaitem.01:32708");
        recordTarget.getAsJsonArray("annotations").add(input);
        keyframes.add(recordTarget);
        keyframes.add(frame(580, "recordedTarget", null,
            text("recordedTarget", 1.5, 2.8, 2.5, new double[] { 1, 1, 2, 2, 2, 3 })));
        helper.assertNotNull(patternSlots, "The cyclic hatch should be in the exported structure");
        for (int slot : new int[] { 0, 4, 8 })
            helper.assertTrue(patternSlots.hasKey("i" + slot), "The tutorial pattern should be installed");
        NBTTagCompound first = new NBTTagCompound();
        first.setTag("i0", patternSlots.getCompoundTag("i0").copy());
        JsonObject opening = guiFrame(620, "guiMain", "patternSlots", first);
        NBTTagCompound initialLanes = new NBTTagCompound();
        initialLanes.setIntArray("cycLanes", new int[36]);
        initialLanes.setTag("cycLaneOffsets", new NBTTagList());
        guiPatch(opening, initialLanes);
        keyframes.add(opening);
        keyframes.add(camera(frame(680, "guiOpen", null, caption("guiOpen")), 135, 20));
        keyframes.add(guiFrame(740, "guiSifter", "patternSlots.i4", patternSlots.getCompoundTag("i4")));
        keyframes.add(guiFrame(800, "guiEnrich", "patternSlots.i8", patternSlots.getCompoundTag("i8")));
        keyframes.add(frame(880, "guiLanes", null, caption("guiLanes")));
        keyframes.add(frame(940, "guiLaneHint", null, caption("guiLaneHint")));
        keyframes.add(frame(980, "guiLaneSet", null, caption("guiLaneSet")));
        keyframes.add(frame(1060, "guiBind", null, caption("guiBind")));
        keyframes.add(frame(1120, "guiBindHint", null, caption("guiBindHint")));
        keyframes.add(frame(1180, "guiBindDone", null, caption("guiBindDone")));
        keyframes.add(frame(1260, "guiLanesBound", null, caption("guiLanesBound")));
        keyframes.add(frame(1360, "guiConfig", null, caption("guiConfig")));
        JsonObject closing = frame(1480, "guiClose", null, text("guiClose", 1.5, 2.3, 7.5, null));
        guiPatch(closing, laneState);
        keyframes.add(closing);
        JsonObject ponder = new JsonObject();
        ponder.addProperty("$schema", "guidenh://schema/ponder.json");
        ponder.addProperty("totalTime", 1560);
        ponder.add("keyframes", keyframes);
        Files.write(
            new File(dir, "palladium_line_ponder.json").toPath(),
            new GsonBuilder().setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .toJson(ponder)
                .getBytes(StandardCharsets.UTF_8));
    }

    private static void guiPatch(JsonObject frame, NBTTagCompound patch) {
        JsonObject change = new JsonObject();
        change.addProperty("x", 1);
        change.addProperty("y", 1);
        change.addProperty("z", 7);
        change.addProperty("nbt", GuideTextNbtCodec.writeTextSafeCompound(patch));
        JsonArray changes = frame.has("mergeTileNBT") ? frame.getAsJsonArray("mergeTileNBT") : new JsonArray();
        changes.add(change);
        frame.add("mergeTileNBT", changes);
    }

    private static JsonObject guiFrame(int time, String key, String path, NBTTagCompound value) {
        JsonObject frame = frame(time, key, null, caption(key));
        JsonObject change = new JsonObject();
        change.addProperty("x", 1);
        change.addProperty("y", 1);
        change.addProperty("z", 7);
        change.addProperty("path", path);
        change.addProperty("value", GuideTextNbtCodec.writeTextSafeCompound(value));
        JsonArray changes = new JsonArray();
        changes.add(change);
        frame.add("modifyTileNBT", changes);
        return frame;
    }

    private static int[] posOf(NBTTagCompound b) {
        NBTBase raw = b.getTag("pos");
        if (raw instanceof NBTTagIntArray) return ((NBTTagIntArray) raw).func_150302_c();
        NBTTagList l = (NBTTagList) raw.copy();
        int[] p = new int[l.tagCount()];
        for (int i = 0; i < p.length; i++) p[i] = ((NBTBase.NBTPrimitive) l.removeTag(0)).func_150287_d();
        return p;
    }

    private static String stageOf(int[] p) {
        TestPos t = at(p[0], p[1], p[2]);
        if (p[0] == 0) return p[2] <= 3 ? "me" : "link";
        if (REACTOR_HATCHES.contains(t)) return "reactorHatches";
        if (SIFTER_HATCHES.contains(t)) return "sifterHatches";
        return p[2] >= 5 ? "reactor" : "sifter";
    }

    private static JsonObject frame(int time, String key, List<JsonObject> changes, JsonObject annotation) {
        JsonObject k = new JsonObject();
        k.addProperty("time", time);
        k.addProperty("labelKey", "proghatches.guide.pd.label." + key);
        if (changes != null && !changes.isEmpty()) {
            JsonArray a = new JsonArray();
            for (JsonObject c : changes) a.add(c);
            k.add("blockChanges", a);
        }
        JsonArray notes = new JsonArray();
        notes.add(annotation);
        k.add("annotations", notes);
        return k;
    }

    private static JsonObject camera(JsonObject frame, float rotY, int easeTicks) {
        JsonObject camera = new JsonObject();
        camera.addProperty("rotY", rotY);
        frame.add("camera", camera);
        frame.addProperty("cameraEaseTicks", easeTicks);
        return frame;
    }

    private static JsonObject caption(String key) {
        JsonObject t = new JsonObject();
        t.addProperty("type", "text");
        t.addProperty("textKey", "proghatches.guide.pd." + key);
        t.addProperty("text", FALLBACK.get(key));
        t.addProperty("independent", true);
        t.addProperty("yOffset", 1000);
        t.addProperty("maxWidth", 250);
        t.addProperty("color", "0xFF7FC47F");
        return t;
    }

    private static JsonObject text(String key, double x, double y, double z, double[] box) {
        JsonObject t = new JsonObject();
        t.addProperty("type", "text");
        t.addProperty("textKey", "proghatches.guide.pd." + key);
        t.addProperty("text", FALLBACK.get(key));
        t.addProperty("x", x);
        t.addProperty("y", y);
        t.addProperty("z", z);
        t.addProperty("maxWidth", 160);
        t.addProperty("color", "0xFF7FC47F");
        if (box != null) {
            t.addProperty("hlMinX", box[0]);
            t.addProperty("hlMinY", box[1]);
            t.addProperty("hlMinZ", box[2]);
            t.addProperty("hlMaxX", box[3]);
            t.addProperty("hlMaxY", box[4]);
            t.addProperty("hlMaxZ", box[5]);
            t.addProperty("highlightColor", "0x607FC47F");
        }
        return t;
    }
}
