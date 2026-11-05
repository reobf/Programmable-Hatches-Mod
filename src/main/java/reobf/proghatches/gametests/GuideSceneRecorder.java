package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

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
import com.hfstudio.guidenh.guide.internal.item.RegionWandExporter;
import com.hfstudio.guidenh.guide.internal.structure.GuideTextNbtCodec;

final class GuideSceneRecorder {

    final GameTestHelper helper;
    final String name, prefix;
    final int sx, sy, sz;
    final JsonArray frames = new JsonArray();
    NBTTagCompound structure;

    GuideSceneRecorder(GameTestHelper helper, String name, String prefix, int sx, int sy, int sz) {
        this.helper = helper; this.name = name; this.prefix = prefix;
        this.sx = sx; this.sy = sy; this.sz = sz;
    }

    void capture() {
        try {
            TestPos origin = helper.absolute(at(0, 0, 0));
            String text = RegionWandExporter.exportRegionAsStructureSnbt(helper.getWorld(),
                origin.x(), origin.y(), origin.z(), sx, sy, sz);
            structure = GuideTextNbtCodec.readStructureNbt(text.getBytes(StandardCharsets.UTF_8));
            NBTTagList blocks = structure.getTagList("blocks", 10);
            for (int i = 0; i < blocks.tagCount(); i++) {
                NBTTagCompound b = blocks.getCompoundTagAt(i);
                if (b.hasKey("nbt")) coordinates(b.getCompoundTag("nbt"), position(b));
            }
        } catch (Exception e) { throw new AssertionError("Guide structure should export: " + name, e); }
    }

    NBTTagCompound snapshot(TestPos position) {
        TestPos absolute = helper.absolute(position);
        TileEntity tile = helper.getWorld().getTileEntity(absolute.x(), absolute.y(), absolute.z());
        helper.assertNotNull(tile, "Guide tile should exist at " + position);
        NBTTagCompound tag = new NBTTagCompound(); tile.writeToNBT(tag);
        coordinates(tag, position);
        return tag;
    }

    JsonObject frame(int time, String key, boolean caption) {
        JsonObject frame = new JsonObject();
        frame.addProperty("time", time); frame.addProperty("labelKey", prefix + ".label." + key);
        JsonArray notes = new JsonArray();
        if (caption) {
            JsonObject text = new JsonObject();
            text.addProperty("type", "text"); text.addProperty("textKey", prefix + "." + key);
            text.addProperty("independent", true); text.addProperty("yOffset", 1000);
            text.addProperty("maxWidth", 250); text.addProperty("color", "0xFF7FC47F"); notes.add(text);
        }
        frame.add("annotations", notes); frames.add(frame);
        return frame;
    }

    void patch(JsonObject frame, TestPos position) { patch(frame, position, snapshot(position)); }

    void patch(JsonObject frame, TestPos position, NBTTagCompound tag) {
        JsonArray merge = array(frame, "mergeTileNBT"), replace = array(frame, "modifyTileNBT");
        JsonObject change = location(position);
        change.addProperty("nbt", GuideTextNbtCodec.writeTextSafeCompound(tag)); merge.add(change);
        for (String key : tag.func_150296_c()) if (tag.getTag(key) instanceof NBTTagCompound child) {
            change = location(position);
            change.addProperty("path", key);
            change.addProperty("value", GuideTextNbtCodec.writeTextSafeCompound(child)); replace.add(change);
        }
    }

    void buildStages(int start, int interval, ToIntFunction<TestPos> stage, String... keys) {
        Map<Integer, JsonArray> changes = new LinkedHashMap<>();
        JsonArray clear = new JsonArray();
        NBTTagList blocks = structure.getTagList("blocks", 10), palette = structure.getTagList("palette", 10);
        for (int i = 0; i < blocks.tagCount(); i++) {
            NBTTagCompound block = blocks.getCompoundTagAt(i);
            TestPos p = position(block);
            JsonObject air = location(p);
            air.addProperty("block", "minecraft:air"); air.addProperty("particles", false); clear.add(air);
            JsonObject add = location(p);
            add.addProperty("block", palette.getCompoundTagAt(block.getInteger("state")).getString("Name"));
            if (block.hasKey("meta")) add.addProperty("meta", block.getInteger("meta"));
            if (block.hasKey("nbt")) add.addProperty("nbt", GuideTextNbtCodec.writeTextSafeCompound(
                GuideTextNbtCodec.decodeCompound(block.getCompoundTag("nbt"))));
            changes.computeIfAbsent(stage.applyAsInt(p), ignored -> new JsonArray()).add(add);
        }
        frame(start, "clear", true).add("blockChanges", clear);
        for (int i = 0; i < keys.length; i++)
            frame(start + interval * (i + 1), keys[i], true).add("blockChanges", changes.getOrDefault(i, new JsonArray()));
    }

    void save(int totalTime) {
        String output = System.getProperty("ph.guideExpansionExport");
        if (output == null) return;
        try {
            Path folder = Path.of(output); Files.createDirectories(folder);
            Files.writeString(folder.resolve(name + ".snbt"), GuideTextNbtCodec.writeStructureSnbt(structure), StandardCharsets.UTF_8);
            JsonObject ponder = new JsonObject();
            ponder.addProperty("$schema", "guidenh://schema/ponder.json");
            ponder.addProperty("totalTime", totalTime); ponder.add("keyframes", frames);
            Files.writeString(folder.resolve(name + "_ponder.json"),
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(ponder) + "\n", StandardCharsets.UTF_8);
        } catch (Exception e) { throw new AssertionError("Guide recording should save: " + name, e); }
    }

    private static JsonArray array(JsonObject object, String name) {
        if (!object.has(name)) object.add(name, new JsonArray());
        return object.getAsJsonArray(name);
    }

    private static JsonObject location(TestPos p) {
        JsonObject o = new JsonObject(); o.addProperty("x", p.x()); o.addProperty("y", p.y()); o.addProperty("z", p.z()); return o;
    }

    private static TestPos position(NBTTagCompound block) {
        NBTBase raw = block.getTag("pos");
        if (raw instanceof NBTTagIntArray a) {
            int[] p = a.func_150302_c(); return at(p[0], p[1], p[2]);
        }
        NBTTagList list = (NBTTagList) raw.copy();
        return at(((NBTBase.NBTPrimitive) list.removeTag(0)).func_150287_d(),
            ((NBTBase.NBTPrimitive) list.removeTag(0)).func_150287_d(),
            ((NBTBase.NBTPrimitive) list.removeTag(0)).func_150287_d());
    }

    private static void coordinates(NBTTagCompound tag, TestPos p) {
        tag.setInteger("x", p.x()); tag.setInteger("y", p.y()); tag.setInteger("z", p.z());
    }
}
