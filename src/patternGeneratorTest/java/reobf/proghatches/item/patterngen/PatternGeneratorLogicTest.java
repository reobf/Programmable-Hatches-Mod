package reobf.proghatches.item.patterngen;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Dependency-light checks for the pattern generator's pure logic. Run with
 * {@code ./gradlew testPatternGeneratorLogic}.
 */
public final class PatternGeneratorLogicTest {

    public static void main(String[] args) {
        recipeIdsAreStable();
        selectionIsPruned();
        regexSectionsWork();
        settingsRoundTrip();
        legacySettingsMigrate();
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
        eq(Arrays.asList("a", "b"), new java.util.ArrayList<>(PatternRecipeSelection.prune(selected, valid)), "pruned");
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
        check(loaded.selectedRecipes.isEmpty(), "legacy selection is empty");
        eq(2, loaded.toTag()
            .getInteger("schema"), "migration writes the current schema");
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
