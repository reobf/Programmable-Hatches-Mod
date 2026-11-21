package reobf.proghatches.item.patterngen;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * Dependency-light checks for the pattern generator's pure logic. Run with
 * {@code ./gradlew testPatternGeneratorLogic}.
 */
public final class PatternGeneratorLogicTest {

    public static void main(String[] args) throws Exception {
        recipeIdsAreStable();
        selectionIsPruned();
        regexSectionsWork();
        settingsRoundTrip();
        legacySettingsMigrate();
        schemaTwoSettingsMigrate();
        encoderOutputSelection();
        voltageBoundsWork();
        presetsRoundTrip();
        System.out.println("PatternGeneratorLogicTest passed");
    }

    private static void recipeIdsAreStable() {
        List<String> inputs = Arrays.asList("I|minecraft:iron_ingot|0|1|{}", "F|water|1000|{}");
        List<String> outputs = Arrays.asList("I|minecraft:iron_plate|0|1|{}");
        String id = PatternRecipeId.of("gt.recipe.macerator", -1, inputs, outputs);
        eq(20, id.length(), "id length");
        eq(id, PatternRecipeId.of("gt.recipe.macerator", -1, inputs, outputs), "same content");
        eq(
            id,
            PatternRecipeId.of(
                "gt.recipe.macerator",
                -1,
                Arrays.asList("F|water|1000|{}", "I|minecraft:iron_ingot|0|1|{}"),
                outputs),
            "input order is normalized");
        check(
            !id.equals(PatternRecipeId.of("gt.recipe.macerator", 1, inputs, outputs)),
            "circuit changes the id");
        check(
            !id.equals(PatternRecipeId.of("gt.recipe.compressor", -1, inputs, outputs)),
            "map changes the id");
        check(
            !id.equals(
                PatternRecipeId.of(
                    "gt.recipe.macerator",
                    -1,
                    inputs,
                    Arrays.asList("I|minecraft:iron_plate|0|2|{}"))),
            "output amount changes the id");
    }

    private static void selectionIsPruned() {
        LinkedHashSet<String> selected = new LinkedHashSet<>(Arrays.asList("a", "stale", "b", "a"));
        LinkedHashSet<String> valid = new LinkedHashSet<>(Arrays.asList("b", "a"));
        eq(Arrays.asList("a", "b"), new ArrayList<>(PatternRecipeSelection.prune(selected, valid)), "pruned");
        eq(2, PatternRecipeSelection.countMatching(selected, Arrays.asList("a", "b", "c")), "count");
        eq(0, PatternRecipeSelection.countMatching(selected, Arrays.asList("c")), "no match count");
    }

    private static void regexSectionsWork() {
        PatternFilterRules.Compiled iron = PatternFilterRules.compile("iron");
        eq(1, iron.ruleCount(), "one rule");
        check(iron.matchesAny(Arrays.asList("Iron Ingot")), "case-insensitive by default");
        check(iron.matchesAny(Arrays.asList("iron ore")), "regex search, not full match");
        check(!iron.matchesAny(Arrays.asList("Gold Ingot")), "non-matching name");

        PatternFilterRules.Compiled sensitive = PatternFilterRules.compile("(?-i)Iron");
        check(!sensitive.matchesAny(Arrays.asList("iron ingot")), "inline flag disables default flags");
        check(sensitive.matchesAny(Arrays.asList("Iron ingot")), "inline flag keeps exact case");

        PatternFilterRules.Compiled multiline = PatternFilterRules.compile("iron\n\n copper \n");
        eq(2, multiline.ruleCount(), "blank lines ignored");
        check(multiline.matchesAny(Arrays.asList("Copper Ingot")), "lines are ORed");

        PatternFilterRules.Compiled invalid = PatternFilterRules.compile("iron\n[unclosed");
        check(invalid.hasInvalid(), "invalid regex detected");
        eq("[unclosed", invalid.invalidPattern(), "offending pattern retained");

        check(
            PatternFilterRules.accepts(
                PatternFilterRules.compile("iron"),
                PatternFilterRules.compile(""),
                PatternFilterRules.compile("plate"),
                PatternFilterRules.compile(""),
                Arrays.asList("Iron Ingot"),
                Arrays.asList("Iron Plate")),
            "include sections must both match");
        check(
            !PatternFilterRules.accepts(
                PatternFilterRules.compile("iron"),
                PatternFilterRules.compile(""),
                PatternFilterRules.compile("gear"),
                PatternFilterRules.compile(""),
                Arrays.asList("Iron Ingot"),
                Arrays.asList("Iron Plate")),
            "output include rejects");
        check(
            !PatternFilterRules.accepts(
                PatternFilterRules.compile(""),
                PatternFilterRules.compile("iron"),
                PatternFilterRules.compile(""),
                PatternFilterRules.compile(""),
                Arrays.asList("Iron Ingot"),
                Arrays.asList("Iron Plate")),
            "input exclude rejects");
        check(
            !PatternFilterRules.accepts(
                PatternFilterRules.compile(""),
                PatternFilterRules.compile(""),
                PatternFilterRules.compile(""),
                PatternFilterRules.compile("plate"),
                Arrays.asList("Iron Ingot"),
                Arrays.asList("Iron Plate")),
            "output exclude rejects");
        check(
            PatternFilterRules.accepts(
                PatternFilterRules.compile(""),
                PatternFilterRules.compile("gear"),
                PatternFilterRules.compile(""),
                PatternFilterRules.compile("gear"),
                Arrays.asList("Iron Ingot"),
                Arrays.asList("Iron Plate")),
            "empty include sections impose no restriction");

        // Name and OreDict are just different candidate lists in the matcher.
        PatternFilterRules.Compiled oreRule = PatternFilterRules.compile("ingotI");
        check(!oreRule.matchesAny(Arrays.asList("Iron Ingot")), "name mode does not see ore names");
        check(oreRule.matchesAny(Arrays.asList("ingotIron")), "OreDict mode sees ore names");

        eq("Iron Ingot", PatternFilterRules.quoteLiteral("Iron Ingot"), "plain literal needs no escapes");
        eq("Iron \\(Ingot\\)", PatternFilterRules.quoteLiteral("Iron (Ingot)"), "regex metacharacters escaped");
        check(
            PatternFilterRules.compile(PatternFilterRules.quoteLiteral("a.b"))
                .matchesAny(Arrays.asList("a.b")),
            "quoted literal matches itself");
    }

    private static void settingsRoundTrip() {
        PatternGeneratorSettings settings = new PatternGeneratorSettings();
        settings.recipeMapId = "gt.recipe.macerator";
        settings.circuit = 7;
        settings.inputInclude = "iron\ncopper";
        settings.inputExclude = "dust";
        settings.outputInclude = "plate";
        settings.outputExclude = "tiny";
        settings.inputIncludeMode = PatternGeneratorSettings.FilterMode.OREDICT;
        settings.inputExcludeMode = PatternGeneratorSettings.FilterMode.NAME;
        settings.outputIncludeMode = PatternGeneratorSettings.FilterMode.NAME;
        settings.outputExcludeMode = PatternGeneratorSettings.FilterMode.OREDICT;
        settings.onlyConflict = true;
        settings.firstOutputOnly = true;
        settings.minVoltage = 32;
        settings.maxVoltage = 2048;
        settings.author = "Steve";
        settings.fillArmed = true;
        settings.pendingRecipes.add("pending123");
        settings.pendingTotal = 3;
        settings.setPendingTarget(0, 10, 20, 30);
        settings.selectedRecipes.add("abc123");
        settings.selectedRecipes.add("def456");

        PatternGeneratorSettings loaded = PatternGeneratorSettings.fromTag(settings.toTag());
        eq("gt.recipe.macerator", loaded.recipeMapId, "map round-trip");
        eq(7, loaded.circuit, "circuit round-trip");
        eq("iron\ncopper", loaded.inputInclude, "input include round-trip");
        eq("dust", loaded.inputExclude, "input exclude round-trip");
        eq("plate", loaded.outputInclude, "output include round-trip");
        eq("tiny", loaded.outputExclude, "output exclude round-trip");
        eq(PatternGeneratorSettings.FilterMode.OREDICT, loaded.inputIncludeMode, "input include mode");
        eq(PatternGeneratorSettings.FilterMode.NAME, loaded.inputExcludeMode, "input exclude mode");
        eq(PatternGeneratorSettings.FilterMode.NAME, loaded.outputIncludeMode, "output include mode");
        eq(PatternGeneratorSettings.FilterMode.OREDICT, loaded.outputExcludeMode, "output exclude mode");
        check(loaded.onlyConflict, "only-conflict round-trip");
        check(loaded.firstOutputOnly, "first-output-only round-trip");
        eq(32, loaded.minVoltage, "minimum voltage round-trip");
        eq(2048, loaded.maxVoltage, "maximum voltage round-trip");
        eq("Steve", loaded.author, "author round-trip");
        check(loaded.fillArmed, "armed round-trip");
        eq(Arrays.asList("pending123"), loaded.pendingRecipes, "pending recipes round-trip");
        eq(3, loaded.pendingTotal, "pending total round-trip");
        check(loaded.hasPendingTarget(), "pending target round-trip");
        eq(10, loaded.pendingX, "pending x");
        eq(20, loaded.pendingY, "pending y");
        eq(30, loaded.pendingZ, "pending z");
        eq(0, loaded.pendingDim, "pending dimension");
        eq(Arrays.asList("abc123", "def456"), loaded.selectedRecipeList(), "selected recipes round-trip");
        check(loaded.machine == null, "no machine tag");
    }

    private static void legacySettingsMigrate() {
        NBTTagCompound legacy = new NBTTagCompound();
        legacy.setInteger("schema", 1);
        legacy.setString("recipeMap", "gt.recipe.macerator");
        legacy.setInteger("circuit", 3);
        legacy.setString("inputFilter", "Iron Ingot");
        legacy.setString("outputFilter", "Copper*");
        PatternGeneratorSettings loaded = PatternGeneratorSettings.fromTag(legacy);
        eq("gt.recipe.macerator", loaded.recipeMapId, "legacy map");
        eq(3, loaded.circuit, "legacy circuit");
        eq(
            PatternFilterRules.quoteLiteral("Iron Ingot"),
            loaded.inputInclude,
            "legacy input becomes a quoted literal");
        eq(
            PatternFilterRules.quoteLiteral("Copper*"),
            loaded.outputInclude,
            "legacy output becomes a quoted literal");
        eq(PatternGeneratorSettings.FilterMode.NAME, loaded.inputIncludeMode, "legacy mode is Name");
        check(!loaded.onlyConflict, "legacy only-conflict defaults to off");
        check(!loaded.firstOutputOnly, "legacy first-output-only defaults to off");
        eq(-1, loaded.minVoltage, "legacy minimum voltage defaults to any");
        eq(-1, loaded.maxVoltage, "legacy maximum voltage defaults to any");
        check(loaded.selectedRecipes.isEmpty(), "legacy selection is empty");
        check(loaded.pendingRecipes.isEmpty(), "legacy pending queue is empty");
        eq(
            PatternGeneratorSettings.CURRENT_SCHEMA,
            loaded.toTag()
                .getInteger("schema"),
            "migration writes the current schema");
    }

    private static void schemaTwoSettingsMigrate() {
        NBTTagCompound v2 = new NBTTagCompound();
        v2.setInteger("schema", 2);
        v2.setString("recipeMap", "gt.recipe.compressor");
        v2.setInteger("circuit", 5);
        v2.setString("inputInclude", "iron");
        v2.setString("outputInclude", "plate");
        v2.setBoolean("onlyConflict", true);
        PatternGeneratorSettings loaded = PatternGeneratorSettings.fromTag(v2);
        eq("gt.recipe.compressor", loaded.recipeMapId, "schema-2 map");
        eq(5, loaded.circuit, "schema-2 circuit");
        eq("iron", loaded.inputInclude, "schema-2 include");
        check(loaded.onlyConflict, "schema-2 only-conflict");
        check(!loaded.firstOutputOnly, "schema-2 first-output-only defaults to off");
        eq(-1, loaded.minVoltage, "schema-2 minimum voltage defaults to any");
        eq(-1, loaded.maxVoltage, "schema-2 maximum voltage defaults to any");
        eq("", loaded.author, "schema-2 author defaults to empty");
        check(loaded.pendingRecipes.isEmpty(), "schema-2 pending queue is empty");
        eq(
            PatternGeneratorSettings.CURRENT_SCHEMA,
            loaded.toTag()
                .getInteger("schema"),
            "schema-2 migration writes the current schema");
    }

    private static void encoderOutputSelection() {
        Item item = new Item();
        PatternIngredient first = PatternIngredient.ofItem(new ItemStack(item, 2), 2);
        PatternIngredient second = PatternIngredient.ofItem(new ItemStack(item, 3), 3);
        PatternIngredient fluidOutput = fluidIngredient(500);
        List<PatternIngredient> all = Arrays.asList(first, second, fluidOutput);

        eq(all, PatternGeneratorRecipes.outputsForEncoding(all, false), "toggle off keeps every output");
        List<PatternIngredient> firstOnly = PatternGeneratorRecipes.outputsForEncoding(all, true);
        eq(1, firstOnly.size(), "toggle on keeps one item output");
        check(firstOnly.get(0) == first, "first item output wins over later items and fluids");

        List<PatternIngredient> fluidsOnly = new ArrayList<>(Arrays.asList(fluidOutput));
        List<PatternIngredient> fluidFirst = PatternGeneratorRecipes.outputsForEncoding(fluidsOnly, true);
        eq(1, fluidFirst.size(), "fluid-only recipe keeps one fluid output");
        check(fluidFirst.get(0) == fluidOutput, "the first fluid output is kept");

        List<PatternIngredient> single = Arrays.asList(first);
        eq(single, PatternGeneratorRecipes.outputsForEncoding(single, true), "single output stays unchanged");
        check(
            PatternGeneratorRecipes.outputsForEncoding(new ArrayList<>(), true)
                .isEmpty(),
            "empty output list stays empty");
    }

    /**
     * A real FluidStack constructor initializes FluidRegistry and therefore all vanilla blocks.
     * The selection logic only needs a fluid marker, so allocate the stack without running that
     * constructor and build the ingredient through its private constructor.
     */
    private static PatternIngredient fluidIngredient(long amount) {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            Object stack = unsafeClass.getMethod("allocateInstance", Class.class)
                .invoke(unsafe, FluidStack.class);
            java.lang.reflect.Field fluidField = FluidStack.class.getDeclaredField("fluid");
            fluidField.setAccessible(true);
            fluidField.set(stack, new Fluid("pattern_generator_test_fluid"));
            FluidStack.class.getField("amount")
                .setInt(stack, (int) amount);
            java.lang.reflect.Constructor<PatternIngredient> constructor = PatternIngredient.class
                .getDeclaredConstructor(ItemStack.class, FluidStack.class, long.class);
            constructor.setAccessible(true);
            return constructor.newInstance(null, stack, amount);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("could not build a test fluid ingredient", e);
        }
    }

    private static void voltageBoundsWork() {
        check(PatternGeneratorRecipes.matchesVoltage(32, -1, -1), "any bounds accept everything");
        check(PatternGeneratorRecipes.matchesVoltage(0, -1, 0), "none upper bound accepts only 0 EU/t");
        check(!PatternGeneratorRecipes.matchesVoltage(8, -1, 0), "none upper bound rejects ULV");
        check(PatternGeneratorRecipes.matchesVoltage(8, 0, 8), "none to ULV accepts ULV");
        check(!PatternGeneratorRecipes.matchesVoltage(32, 0, 8), "none to ULV rejects LV");
        check(PatternGeneratorRecipes.matchesVoltage(-32, 32, 32), "negative generator EU/t uses its absolute value");
        check(!PatternGeneratorRecipes.matchesVoltage(16, 32, 128), "below minimum rejects");
        check(!PatternGeneratorRecipes.matchesVoltage(256, 32, 128), "above maximum rejects");
        check(PatternGeneratorRecipes.matchesVoltage(128, 32, 128), "bounds are inclusive");
    }

    private static void presetsRoundTrip() throws Exception {
        File dir = Files.createTempDirectory("ph-pattern-generator-presets")
            .toFile();
        try {
            PatternGeneratorSettings settings = new PatternGeneratorSettings();
            settings.recipeMapId = "gt.recipe.macerator";
            settings.circuit = 4;
            settings.inputInclude = "iron";
            settings.outputExclude = "dust";
            settings.inputIncludeMode = PatternGeneratorSettings.FilterMode.OREDICT;
            settings.onlyConflict = true;
            settings.firstOutputOnly = true;
            settings.minVoltage = 32;
            settings.maxVoltage = 2048;
            settings.author = "Tester";
            settings.selectedRecipes.add("abc");
            settings.selectedRecipes.add("def");

            PatternGeneratorPresets.Preset preset = PatternGeneratorPresets.Preset
                .fromSettings("Iron Plates", settings, 3);
            check(PatternGeneratorPresets.save(dir, preset), "preset saved");

            PatternGeneratorPresets.Preset loaded = PatternGeneratorPresets.load(dir, "Iron Plates");
            check(loaded != null, "preset loaded");
            eq("Iron Plates", loaded.name, "preset name");
            eq("gt.recipe.macerator", loaded.settings.recipeMapId, "preset map");
            eq(4, loaded.settings.circuit, "preset circuit");
            eq("iron", loaded.settings.inputInclude, "preset filter");
            eq(PatternGeneratorSettings.FilterMode.OREDICT, loaded.settings.inputIncludeMode, "preset mode");
            check(loaded.settings.onlyConflict, "preset only-conflict");
            check(loaded.settings.firstOutputOnly, "preset first-output-only");
            eq(32, loaded.settings.minVoltage, "preset minimum voltage");
            eq(2048, loaded.settings.maxVoltage, "preset maximum voltage");
            eq("Tester", loaded.settings.author, "preset author");
            eq(Arrays.asList("abc", "def"), loaded.settings.selectedRecipeList(), "preset selection");
            eq(3, loaded.machineMode, "preset machine mode");
            check(!loaded.settings.hasPending(), "preset never stores a fill queue");

            check(PatternGeneratorPresets.rename(dir, "Iron Plates", "Steel Plates"), "preset renamed");
            check(PatternGeneratorPresets.load(dir, "Iron Plates") == null, "old name gone");
            PatternGeneratorPresets.Preset renamed = PatternGeneratorPresets.load(dir, "Steel Plates");
            check(renamed != null, "renamed preset loads");
            eq("Steel Plates", renamed.name, "renamed preset name");

            check(PatternGeneratorPresets.delete(dir, "Steel Plates"), "preset deleted");
            check(PatternGeneratorPresets.load(dir, "Steel Plates") == null, "deleted preset gone");
            check(
                PatternGeneratorPresets.list(dir)
                    .isEmpty(),
                "no presets left");

            Files.write(new File(dir, "broken.dat").toPath(), new byte[] { 1, 2, 3, 4 });
            NBTTagCompound wrongSchema = new NBTTagCompound();
            wrongSchema.setInteger("schema", 2);
            wrongSchema.setString("name", "old");
            try (FileOutputStream out = new FileOutputStream(new File(dir, "wrong.dat"))) {
                CompressedStreamTools.writeCompressed(wrongSchema, out);
            }
            check(
                PatternGeneratorPresets.list(dir)
                    .isEmpty(),
                "malformed and unknown-schema files are skipped");
        } finally {
            deleteTree(dir);
        }
    }

    private static void deleteTree(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) deleteTree(child);
        }
        file.delete();
    }

    private static void eq(Object expected, Object actual, String what) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }

    private static void check(boolean condition, String what) {
        if (!condition) throw new AssertionError(what);
    }
}
