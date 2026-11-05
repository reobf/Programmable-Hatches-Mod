package reobf.proghatches.gametests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.hfstudio.guidenh.guide.scene.ponder.PonderKeyframe;
import com.hfstudio.guidenh.guide.scene.ponder.PonderNbtPath;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import com.hfstudio.guidenh.guide.compiler.FrontmatterNavigation;
import com.hfstudio.guidenh.guide.compiler.PageCompiler;
import com.hfstudio.guidenh.guide.compiler.GuideMarkdownOptions;
import com.hfstudio.guidenh.guide.compiler.MdxBlockTagSourceExtractor;
import com.hfstudio.guidenh.guide.compiler.tags.DetailsContentExtractor;
import com.hfstudio.guidenh.guide.internal.markdown.MdAstToMdxConverter;
import com.hfstudio.guidenh.libs.mdast.MdAst;
import com.hfstudio.guidenh.guide.compiler.ParsedGuidePage;
import com.hfstudio.guidenh.libs.mdast.model.MdAstParent;
import com.hfstudio.guidenh.libs.mdast.model.MdAstText;
import com.hfstudio.guidenh.libs.mdast.mdx.model.MdxJsxElementFields;

import bartworks.system.material.Werkstoff;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.GregTechAPI;
import reobf.proghatches.main.MyMod;

/** The guide pages parse with GuideNH's own parser, and every item, machine, texture and parent they name exists. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"guidenh"})
public class GuidePageTests {

    private static final String[] LANGUAGES = { "zh_cn", "en_us" };
    private static final String[] PAGES = {
        "index.md", "cyclic_patterns.md", "programmed_inputs.md", "chance_sifter.md",
        "fluid_stocking.md", "large_provider.md", "recipe_map_inputs.md", "stock_control.md",
        "input_routing.md", "network_tools.md",
        "ma_routing.md", "assembly_line.md", "smart_arm.md", "special_inputs.md",
        "crafting_throughput.md", "storage_outputs.md", "tools_upgrades.md", "oc_automation.md",
        "reactor_control.md", "pattern_submitter.md", "buffer_tuning.md"
    };
    private static final Set<String> TAGS = new HashSet<>(
        Arrays.asList(
            "ItemLink",
            "ItemImage",
            "FloatingImage",
            "RecipeFor",
            "RecipeUsage",
            "SubPages",
            "Mermaid",
            "Flowchart",
            "FlowNode",
            "FlowEdge",
            "NodeContent",
            "GameScene",
            "ImportStructure",
            "ImportPonder",
            "MUI2Gui",
            "MUI2Click"));
    private static final Pattern MERMAID = Pattern.compile("<Mermaid[^>]*>([\\s\\S]*?)</Mermaid>");
    private static final Pattern NODE_CONTENT = Pattern.compile("<NodeContent id=\"([^\"]+)\">[\\s\\S]*?</NodeContent>");

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void guidePagesParseAndResolve(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        for (String lang : LANGUAGES) for (String page : PAGES) {
            String path = guidePath(MyMod.MODID, lang, page);
            try {
                String source = read(helper, path);
                Matcher links = Pattern.compile("]\\(([^)#]+\\.md)(?:#[^)]*)?\\)").matcher(source);
                while (links.find()) helper.assertTrue(
                    exists(guidePath(MyMod.MODID, lang, links.group(1))), path + ": missing linked page " + links.group(1));
                ParsedGuidePage parsed = PageCompiler
                    .parse(MyMod.MODID, lang, new ResourceLocation(MyMod.MODID, page), source);
                helper.assertNull(parsed.getParseFailureMessage(), path + " should parse");
                FrontmatterNavigation nav = parsed.getFrontmatter()
                    .navigationEntry();
                helper.assertNotNull(nav, path + " should be in the navigation");
                if (nav.iconItemId() != null) assertItem(helper, path, nav.iconItemId() + ":" + nav.iconItemMeta());
                ResourceLocation parent = nav.parent();
                if (parent != null) helper.assertTrue(
                    exists(guidePath(parent.getResourceDomain(), lang, parent.getResourcePath())),
                    path + ": parent " + parent + " should exist");
                Object ids = parsed.getFrontmatter()
                    .additionalProperties()
                    .get("item_ids");
                if (ids instanceof List<?>list) for (Object id : list) assertItem(helper, path, String.valueOf(id));
                Map<String, Integer> seen = new HashMap<>();
                walk(helper, path, source, parsed.getAstRoot(), seen);
                Matcher diagram = MERMAID.matcher(source);
                while (diagram.find()) {
                    String body = diagram.group(1);
                    String nodes = NODE_CONTENT.matcher(body)
                        .replaceAll("");
                    Matcher content = NODE_CONTENT.matcher(body);
                    while (content.find()) {
                        String id = content.group(1);
                        helper.assertTrue(
                            Pattern.compile("(?m)^\\s*" + Pattern.quote(id) + "[\\[({]")
                                .matcher(nodes)
                                .find(),
                            path + ": NodeContent " + id + " should match a node of its diagram");
                    }
                }
                for (String tag : TAGS) {
                    Matcher m = Pattern.compile("<" + tag + "[\\s/>]")
                        .matcher(source);
                    int written = 0;
                    while (m.find()) written++;
                    helper.assertTrue(
                        written == seen.getOrDefault(tag, 0),
                        path + ": every <" + tag + "> written should be checked, written " + written
                            + ", checked "
                            + seen.getOrDefault(tag, 0));
                }
            } catch (AssertionError | RuntimeException e) {
                failures.add(path + ": " + e.getMessage());
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("\n", failures));
        helper.succeed();
    }

    private static String guidePath(String domain, String lang, String page) {
        return "/assets/" + domain + "/guidenh/_" + lang + "/" + page;
    }

    private static boolean exists(String path) {
        try (InputStream in = GuidePageTests.class.getResourceAsStream(path)) {
            return in != null;
        } catch (IOException e) {
            return false;
        }
    }

    private static String read(GameTestHelper helper, String path) {
        try (InputStream in = GuidePageTests.class.getResourceAsStream(path)) {
            helper.assertNotNull(in, path + " should be packaged");
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError(path + " should be readable", e);
        }
    }

    private static void walk(GameTestHelper helper, String path, String source, Object node, Map<String, Integer> seen) {
        if (node instanceof MdAstText text) helper.assertFalse(text.value.contains("**"),
            path + ": bold markers are shown as text near \"" + text.value.trim() + "\"");
        if (node instanceof MdxJsxElementFields el) {
            checkTag(helper, path, el, seen);
            if ("GameScene".equals(el.name())) {
                String body = MdxBlockTagSourceExtractor.extractRawBody(el, source);
                helper.assertNotNull(body, path + ": GameScene source should be available");
                // SceneTagCompiler reparses its body with tag indentation removed.
                body = DetailsContentExtractor.dedent(body).replaceAll("(?m)^[ \t]+(?=</?[A-Za-z])", "");
                var sceneAst = MdAst.fromMarkdown(body, GuideMarkdownOptions.runtime());
                MdAstToMdxConverter.convert(sceneAst, Map.of());
                walk(helper, path, body, sceneAst, seen);
                return;
            }
        }
        if (node instanceof MdAstParent<?> parent)
            for (Object child : parent.children()) walk(helper, path, source, child, seen);
    }

    private static void checkTag(GameTestHelper helper, String path, MdxJsxElementFields el,
        Map<String, Integer> seen) {
        String name = el.name();
        if (name == null || name.isEmpty() || Character.isLowerCase(name.charAt(0))) return;
        helper.assertTrue(TAGS.contains(name), path + ": <" + name + "> is outside the tags this guide uses");
        seen.merge(name, 1, Integer::sum);
        boolean recipe = "RecipeFor".equals(name) || "RecipeUsage".equals(name);
        if ("ItemLink".equals(name) || "ItemImage".equals(name) || recipe)
            assertItem(helper, path, el.getAttributeString("id", ""));
        if ("FlowNode".equals(name) && el.hasAttribute("item"))
            assertItem(helper, path, el.getAttributeString("item", ""));
        if (recipe) for (String filter : new String[] { "input", "output" })
            if (el.hasAttribute(filter)) for (String term : el.getAttributeString(filter, "").split("[,&]"))
                assertItem(helper, path, term.trim().replaceFirst("^!", ""));
        if ("FloatingImage".equals(name)) {
            String src = el.getAttributeString("src", "");
            int colon = src.indexOf(':');
            helper.assertTrue(
                colon > 0 && exists("/assets/" + src.substring(0, colon) + "/" + src.substring(colon + 1)),
                path + ": texture " + src + " should exist");
        }
        if ("ImportStructure".equals(name) || "ImportPonder".equals(name)) {
            String src = el.getAttributeString("src", "");
            String file = src.startsWith("/") ? "/assets/" + MyMod.MODID + "/guidenh" + src
                : path.substring(0, path.lastIndexOf('/') + 1) + src;
            helper.assertTrue(exists(file), path + ": " + src + " should exist");
            if ("ImportPonder".equals(name)) checkPonder(helper, file);
        }
    }

    /** Every block a Ponder timeline places is registered, and every label and text key is translated. */
    private static void checkPonder(GameTestHelper helper, String file) {
        Set<String> zh = langKeys(helper, "zh_CN"), en = langKeys(helper, "en_US");
        JsonObject root = new JsonParser().parse(read(helper, file))
            .getAsJsonObject();
        for (JsonElement k : root.getAsJsonArray("keyframes")) {
            JsonObject frame = k.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : frame.entrySet()) {
                String field = entry.getKey();
                try { PonderKeyframe.class.getDeclaredField(field); }
                catch (NoSuchFieldException e) { throw new AssertionError(file + ": unknown Ponder keyframe field " + field, e); }
            }
            try {
                if (frame.has("mergeTileNBT")) for (JsonElement op : frame.getAsJsonArray("mergeTileNBT"))
                    PonderNbtPath.parseCompound(op.getAsJsonObject().get("nbt").getAsString());
                if (frame.has("modifyTileNBT")) for (JsonElement op : frame.getAsJsonArray("modifyTileNBT"))
                    PonderNbtPath.parseValue(op.getAsJsonObject().get("value").getAsString());
            } catch (Exception e) { throw new AssertionError(file + ": Ponder NBT should decode", e); }
            List<String> keys = new ArrayList<>();
            if (frame.has("labelKey")) keys.add(
                frame.get("labelKey")
                    .getAsString());
            if (frame.has("annotations")) for (JsonElement a : frame.getAsJsonArray("annotations")) if (a
                .getAsJsonObject()
                .has("textKey"))
                keys.add(
                    a.getAsJsonObject()
                        .get("textKey")
                        .getAsString());
            if (frame.has("annotations")) for (JsonElement a : frame.getAsJsonArray("annotations")) {
                JsonObject annotation = a.getAsJsonObject();
                if (annotation.has("item")) assertItem(helper, file, annotation.get("item").getAsString());
            }
            for (String key : keys) helper
                .assertTrue(zh.contains(key) && en.contains(key), file + ": " + key + " should be translated");
            if (frame.has("blockChanges")) for (JsonElement c : frame.getAsJsonArray("blockChanges")) {
                String block = c.getAsJsonObject()
                    .get("block")
                    .getAsString();
                int colon = block.indexOf(':');
                helper.assertTrue(
                    "minecraft:air".equals(block) || colon > 0
                        && GameRegistry.findBlock(block.substring(0, colon), block.substring(colon + 1)) != null,
                    file + ": block " + block + " should exist");
            }
        }
    }

    private static Set<String> langKeys(GameTestHelper helper, String lang) {
        Set<String> keys = new HashSet<>();
        for (String line : read(helper, "/assets/proghatches/lang/" + lang + ".lang").split("\n")) {
            int eq = line.indexOf('=');
            if (eq > 0) keys.add(
                line.substring(0, eq)
                    .trim());
        }
        return keys;
    }

    private static void assertItem(GameTestHelper helper, String path, String spec) {
        String s = spec.trim();
        if (s.startsWith("<") && s.endsWith(">")) s = s.substring(1, s.length() - 1);
        NBTTagCompound tag = null;
        int brace = s.indexOf('{');
        if (brace >= 0) {
            try {
                tag = (NBTTagCompound) JsonToNBT.func_150315_a(s.substring(brace));
            } catch (NBTException | ClassCastException e) {
                throw new AssertionError(path + ": " + spec + " should carry valid SNBT", e);
            }
            s = s.substring(0, brace);
            if (s.endsWith(":")) s = s.substring(0, s.length() - 1);
            if (tag.hasKey("Fluid")) helper.assertNotNull(
                FluidRegistry.getFluid(
                    tag.getString("Fluid")
                        .toLowerCase()),
                path + ": fluid " + tag.getString("Fluid") + " should exist");
        }
        String[] parts = s.split(":");
        helper.assertTrue(parts.length >= 2, path + ": " + spec + " should name an item");
        Item item = GameRegistry.findItem(parts[0], parts[1]);
        helper.assertNotNull(item, path + ": item " + spec + " should exist");
        if (parts.length < 3 || "*".equals(parts[2])) return;
        int meta = Integer.parseInt(parts[2]);
        if (item == Item.getItemFromBlock(GregTechAPI.sBlockMachines)) {
            helper.assertTrue(
                meta >= 0 && meta < GregTechAPI.METATILEENTITIES.length && GregTechAPI.METATILEENTITIES[meta] != null,
                path + ": machine " + spec + " should exist");
            return;
        }
        if (parts[1].startsWith("gt.bwMetaGenerated")) helper
            .assertNotNull(Werkstoff.werkstoffHashMap.get((short) meta), path + ": material " + spec + " should exist");
        String display;
        try {
            ItemStack stack = new ItemStack(item, 1, meta);
            if (tag != null) stack.setTagCompound(tag);
            display = stack.getDisplayName();
        } catch (RuntimeException e) {
            display = null;
        }
        helper.assertTrue(
            display != null && !display.isEmpty() && !display.endsWith(".name"),
            path + ": " + spec + " should have a name, got " + display);
    }
}
