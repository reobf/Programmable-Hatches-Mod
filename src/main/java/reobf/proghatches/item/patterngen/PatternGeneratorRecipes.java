package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEStack;
import appeng.helpers.UltimatePatternHelper;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.GTRecipe;
import gregtech.common.items.ItemIntegratedCircuit;
import reobf.proghatches.item.ItemProgrammingCircuit;

/**
 * Recipe selection, filtering and AE2 pattern encoding for the pattern generator.
 * <p>
 * Circuits and NC items are encoded as PH's virtual {@link ItemProgrammingCircuit}; the
 * underlying item is supplied by a Programming Circuit Provider and is not consumed.
 */
public final class PatternGeneratorRecipes {

    public static final int ERR_NO_MACHINE = -1;
    public static final int ERR_NO_MAP = -2;
    public static final int ERR_NO_RECIPES = -3;
    public static final int ERR_INVALID_FILTER = -4;
    public static final int ERR_NOTHING_SELECTED = -5;

    private PatternGeneratorRecipes() {}

    public static final class GenerationResult {

        public final int error;
        public final int generated;
        public final int requested;
        public final int skipped;
        public final PatternRecipeCollection.FilterProblem filterProblem;

        private GenerationResult(int error, int generated, int requested, int skipped,
            PatternRecipeCollection.FilterProblem filterProblem) {
            this.error = error;
            this.generated = generated;
            this.requested = requested;
            this.skipped = skipped;
            this.filterProblem = filterProblem;
        }

        public static GenerationResult error(int error) {
            return new GenerationResult(error, 0, 0, 0, null);
        }

        public static GenerationResult filterProblem(PatternRecipeCollection.FilterProblem problem) {
            return new GenerationResult(ERR_INVALID_FILTER, 0, 0, 0, problem);
        }
    }

    public static PatternRecipeCollection collect(ItemStack tool) {
        return collect(PatternGeneratorSettings.load(tool));
    }

    public static PatternRecipeCollection collect(PatternGeneratorSettings settings) {
        if (!PatternMachineResolver.isMachineItem(settings.machine)) return PatternRecipeCollection.error(ERR_NO_MACHINE);
        List<RecipeMap<?>> maps = PatternMachineResolver.resolveMaps(settings.machine);
        if (maps.isEmpty()) return PatternRecipeCollection.error(ERR_NO_MAP);
        RecipeMap<?> map = PatternMachineResolver.selectMap(maps, settings.recipeMapId);
        if (map == null || map.unlocalizedName == null) return PatternRecipeCollection.error(ERR_NO_MAP);

        String mapName = PatternMachineResolver.mapDisplayName(map.unlocalizedName);
        List<PatternRecipeEntry> all = new ArrayList<>();
        Collection<GTRecipe> recipes;
        try {
            recipes = map.getAllRecipes();
        } catch (Throwable ignored) {
            recipes = null;
        }
        if (recipes != null) {
            for (GTRecipe recipe : recipes) {
                try {
                    if (!isUsable(recipe)) continue;
                    PatternRecipeEntry entry = PatternRecipeEntry.of(recipe, map.unlocalizedName, mapName);
                    if (entry != null) all.add(entry);
                } catch (Throwable ignored) {
                    // A malformed addon recipe must not take the whole preview down.
                }
            }
        }

        List<PatternRecipeCollection.FilterProblem> problems = new ArrayList<>();
        FilterSet filters = FilterSet.compile(settings, problems);
        List<PatternRecipeEntry> filtered = new ArrayList<>();
        if (problems.isEmpty()) {
            for (PatternRecipeEntry entry : all) {
                if (!matchesCircuit(entry.circuit, settings.circuit)) continue;
                if (!filters.accepts(entry)) continue;
                filtered.add(entry);
            }
            if (settings.onlyConflict) filtered = onlyConflicts(filtered);
        }
        return new PatternRecipeCollection(
            0,
            map.unlocalizedName,
            mapName,
            all,
            filtered,
            problems);
    }

    /**
     * Keeps recipes whose output is also produced by another recipe and sorts them by the
     * first shared output. Amounts are ignored when comparing outputs; duplicate outputs in
     * one recipe only count once.
     */
    private static List<PatternRecipeEntry> onlyConflicts(List<PatternRecipeEntry> entries) {
        Map<String, Integer> counts = new HashMap<>();
        List<Set<String>> keysPerEntry = new ArrayList<>(entries.size());
        for (PatternRecipeEntry entry : entries) {
            Set<String> keys = new LinkedHashSet<>();
            for (PatternIngredient output : entry.outputs) {
                String key = output.typeKey();
                if (!key.isEmpty()) keys.add(key);
            }
            keysPerEntry.add(keys);
            for (String key : keys) counts.put(key, counts.getOrDefault(key, 0) + 1);
        }

        List<ConflictEntry> conflicts = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            String conflictKey = null;
            for (String key : keysPerEntry.get(i)) {
                if (counts.getOrDefault(key, 0) < 2) continue;
                if (conflictKey == null || key.compareTo(conflictKey) < 0) conflictKey = key;
            }
            if (conflictKey != null) conflicts.add(new ConflictEntry(entries.get(i), conflictKey));
        }
        conflicts.sort(
            Comparator.comparing((ConflictEntry conflict) -> conflict.key)
                .thenComparing(conflict -> conflict.entry.id));

        List<PatternRecipeEntry> result = new ArrayList<>(conflicts.size());
        for (ConflictEntry conflict : conflicts) result.add(conflict.entry);
        return result;
    }

    private static final class ConflictEntry {

        private final PatternRecipeEntry entry;
        private final String key;

        private ConflictEntry(PatternRecipeEntry entry, String key) {
            this.entry = entry;
            this.key = key;
        }
    }

    public static int countMatches(ItemStack tool) {
        PatternRecipeCollection collection = collect(tool);
        if (collection.error != 0) return collection.error;
        return collection.filtered.size();
    }

    public static GenerationResult generate(EntityPlayerMP player, ItemStack tool, boolean selectedOnly) {
        return generate(player, tool, PatternGeneratorSettings.load(tool), selectedOnly);
    }

    public static GenerationResult generate(EntityPlayerMP player, ItemStack tool, PatternGeneratorSettings settings,
        boolean selectedOnly) {
        PatternRecipeCollection collection = collect(settings);
        if (collection.error != 0) return GenerationResult.error(collection.error);
        if (collection.hasFilterProblem()) return GenerationResult.filterProblem(collection.firstFilterProblem());

        List<PatternRecipeEntry> entries = new ArrayList<>();
        boolean selectionChanged = false;
        if (selectedOnly) {
            if (settings.selectedRecipes.isEmpty()) return GenerationResult.error(ERR_NOTHING_SELECTED);
            LinkedHashSet<String> pruned = PatternRecipeSelection.prune(settings.selectedRecipes, collection.allIds());
            if (!pruned.equals(settings.selectedRecipes)) {
                settings.selectedRecipes.clear();
                settings.selectedRecipes.addAll(pruned);
                selectionChanged = true;
            }
            if (pruned.isEmpty()) {
                if (selectionChanged) persist(settings, tool, player);
                return GenerationResult.error(ERR_NOTHING_SELECTED);
            }
            for (PatternRecipeEntry entry : collection.all) {
                if (pruned.contains(entry.id)) entries.add(entry);
            }
        } else {
            entries.addAll(collection.filtered);
        }

        if (entries.isEmpty()) {
            if (selectionChanged) persist(settings, tool, player);
            return GenerationResult.error(ERR_NO_RECIPES);
        }

        int generated = 0;
        int skipped = 0;
        for (PatternRecipeEntry entry : entries) {
            ItemStack pattern;
            try {
                pattern = encode(entry.recipe, player);
            } catch (Throwable ignored) {
                pattern = null;
            }
            if (pattern == null) {
                skipped++;
                continue;
            }
            if (!player.inventory.addItemStackToInventory(pattern)) {
                player.dropPlayerItemWithRandomChoice(pattern, false);
            }
            generated++;
        }
        if (selectionChanged) persist(settings, tool, player);
        return new GenerationResult(0, generated, entries.size(), skipped, null);
    }

    private static void persist(PatternGeneratorSettings settings, ItemStack tool, EntityPlayerMP player) {
        settings.save(tool);
        if (player == null) return;
        player.inventory.markDirty();
        if (player.inventoryContainer != null) player.inventoryContainer.detectAndSendChanges();
    }

    private static boolean isUsable(GTRecipe recipe) {
        if (recipe == null || !recipe.mEnabled || recipe.mFakeRecipe || recipe.mHidden) return false;
        if (!hasInput(recipe) || !hasGuaranteedOutput(recipe)) return false;
        return !hasWildcard(recipe);
    }

    private static boolean matchesCircuit(int recipeCircuit, int wanted) {
        if (wanted < 0) return true;
        return wanted == 0 ? recipeCircuit < 0 : recipeCircuit == wanted;
    }

    private static final class FilterSet {

        private final PatternFilterRules.Compiled inputInclude;
        private final PatternFilterRules.Compiled inputExclude;
        private final PatternFilterRules.Compiled outputInclude;
        private final PatternFilterRules.Compiled outputExclude;
        private final boolean inputIncludeOre;
        private final boolean inputExcludeOre;
        private final boolean outputIncludeOre;
        private final boolean outputExcludeOre;

        private FilterSet(PatternGeneratorSettings settings, PatternFilterRules.Compiled inputInclude,
            PatternFilterRules.Compiled inputExclude, PatternFilterRules.Compiled outputInclude,
            PatternFilterRules.Compiled outputExclude) {
            this.inputInclude = inputInclude;
            this.inputExclude = inputExclude;
            this.outputInclude = outputInclude;
            this.outputExclude = outputExclude;
            this.inputIncludeOre = settings.inputIncludeMode == PatternGeneratorSettings.FilterMode.OREDICT;
            this.inputExcludeOre = settings.inputExcludeMode == PatternGeneratorSettings.FilterMode.OREDICT;
            this.outputIncludeOre = settings.outputIncludeMode == PatternGeneratorSettings.FilterMode.OREDICT;
            this.outputExcludeOre = settings.outputExcludeMode == PatternGeneratorSettings.FilterMode.OREDICT;
        }

        static FilterSet compile(PatternGeneratorSettings settings,
            List<PatternRecipeCollection.FilterProblem> problems) {
            PatternFilterRules.Compiled inputInclude = PatternFilterRules.compile(settings.inputInclude);
            PatternFilterRules.Compiled inputExclude = PatternFilterRules.compile(settings.inputExclude);
            PatternFilterRules.Compiled outputInclude = PatternFilterRules.compile(settings.outputInclude);
            PatternFilterRules.Compiled outputExclude = PatternFilterRules.compile(settings.outputExclude);
            addProblem(problems, PatternRecipeCollection.FilterSection.INPUT_INCLUDE, inputInclude);
            addProblem(problems, PatternRecipeCollection.FilterSection.INPUT_EXCLUDE, inputExclude);
            addProblem(problems, PatternRecipeCollection.FilterSection.OUTPUT_INCLUDE, outputInclude);
            addProblem(problems, PatternRecipeCollection.FilterSection.OUTPUT_EXCLUDE, outputExclude);
            return new FilterSet(settings, inputInclude, inputExclude, outputInclude, outputExclude);
        }

        private static void addProblem(List<PatternRecipeCollection.FilterProblem> problems,
            PatternRecipeCollection.FilterSection section, PatternFilterRules.Compiled compiled) {
            if (compiled.hasInvalid()) {
                problems.add(new PatternRecipeCollection.FilterProblem(section, compiled.invalidPattern()));
            }
        }

        boolean accepts(PatternRecipeEntry entry) {
            if (!this.inputInclude.isEmpty() && !matches(this.inputInclude, entry.inputs, this.inputIncludeOre)) {
                return false;
            }
            if (!this.outputInclude.isEmpty() && !matches(this.outputInclude, entry.outputs, this.outputIncludeOre)) {
                return false;
            }
            if (!this.inputExclude.isEmpty() && matches(this.inputExclude, entry.inputs, this.inputExcludeOre)) {
                return false;
            }
            return this.outputExclude.isEmpty() || !matches(this.outputExclude, entry.outputs, this.outputExcludeOre);
        }

        private static boolean matches(PatternFilterRules.Compiled rules, List<PatternIngredient> ingredients,
            boolean oreDict) {
            if (rules.isEmpty()) return false;
            for (PatternIngredient ingredient : ingredients) {
                if (rules.matchesAny(ingredient.candidates(oreDict))) return true;
            }
            return false;
        }
    }

    private static boolean hasInput(GTRecipe recipe) {
        if (matchesItemsNonEmpty(recipe.mInputs)) return true;
        return matchesFluidsNonEmpty(recipe.mFluidInputs);
    }

    private static boolean hasGuaranteedOutput(GTRecipe recipe) {
        if (recipe.mOutputs != null) {
            for (int i = 0; i < recipe.mOutputs.length; i++) {
                ItemStack stack = recipe.mOutputs[i];
                if (stack != null && stack.stackSize > 0 && recipe.getOutputChance(i) >= 10000) return true;
            }
        }
        return matchesFluidsNonEmpty(recipe.mFluidOutputs);
    }

    private static boolean isWildcard(ItemStack stack) {
        return stack != null && stack.getItemDamage() == OreDictionary.WILDCARD_VALUE;
    }

    private static boolean hasWildcard(GTRecipe recipe) {
        if (recipe.mInputs != null) {
            for (ItemStack stack : recipe.mInputs) {
                if (isWildcard(stack)) return true;
            }
        }
        if (recipe.mOutputs != null) {
            for (ItemStack stack : recipe.mOutputs) {
                if (isWildcard(stack)) return true;
            }
        }
        for (ItemStack stack : specialItems(recipe.mSpecialItems)) {
            if (isWildcard(stack)) return true;
        }
        return false;
    }

    private static boolean matchesItemsNonEmpty(ItemStack[] stacks) {
        if (stacks == null) return false;
        for (ItemStack stack : stacks) {
            if (stack != null && stack.stackSize > 0) return true;
        }
        return false;
    }

    private static boolean matchesFluidsNonEmpty(FluidStack[] fluids) {
        if (fluids == null) return false;
        for (FluidStack fluid : fluids) {
            if (fluid != null && fluid.amount > 0) return true;
        }
        return false;
    }

    public static ItemStack encode(GTRecipe recipe, EntityPlayer player) {
        if (recipe == null) return null;

        NBTTagList inputs = new NBTTagList();
        NBTTagList outputs = new NBTTagList();

        if (recipe.mInputs != null) {
            for (ItemStack stack : recipe.mInputs) {
                if (stack == null || stack.getItem() == null) continue;
                if (isWildcard(stack)) return null;
                if (stack.stackSize <= 0 || stack.getItem() instanceof ItemIntegratedCircuit) {
                    appendVirtual(inputs, stack);
                } else {
                    appendItem(inputs, stack, stack.stackSize);
                }
            }
        }
        for (ItemStack stack : specialItems(recipe.mSpecialItems)) {
            if (stack == null || stack.getItem() == null) continue;
            if (isWildcard(stack)) return null;
            appendVirtual(inputs, stack);
        }
        if (recipe.mFluidInputs != null) {
            for (FluidStack fluid : recipe.mFluidInputs) {
                if (fluid != null && fluid.amount > 0) appendFluid(inputs, fluid, fluid.amount);
            }
        }

        if (recipe.mOutputs != null) {
            for (int i = 0; i < recipe.mOutputs.length; i++) {
                ItemStack stack = recipe.mOutputs[i];
                if (stack == null || stack.getItem() == null || stack.stackSize <= 0) continue;
                if (isWildcard(stack)) return null;
                // AE2 patterns cannot express random outputs.
                if (recipe.getOutputChance(i) < 10000) continue;
                appendItem(outputs, stack, stack.stackSize);
            }
        }
        if (recipe.mFluidOutputs != null) {
            for (FluidStack fluid : recipe.mFluidOutputs) {
                if (fluid != null && fluid.amount > 0) appendFluid(outputs, fluid, fluid.amount);
            }
        }

        if (inputs.tagCount() == 0 || outputs.tagCount() == 0) return null;

        ItemStack pattern = AEApi.instance()
            .definitions()
            .items()
            .encodedUltimatePattern()
            .maybeStack(1)
            .orNull();
        if (pattern == null) return null;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("in", inputs);
        tag.setTag("out", outputs);
        tag.setBoolean("crafting", false);
        tag.setBoolean("substitute", false);
        tag.setBoolean("beSubstitute", false);
        if (player != null) {
            tag.setString("author", player.getCommandSenderName());
            tag.setLong("encodedAt", System.currentTimeMillis());
        }
        pattern.setTagCompound(tag);

        // Parse it once before handing it out; an invalid pattern is worse than no pattern.
        try {
            new UltimatePatternHelper(pattern);
        } catch (Throwable ignored) {
            return null;
        }
        return pattern;
    }

    private static void appendVirtual(NBTTagList list, ItemStack stack) {
        appendItem(list, ItemProgrammingCircuit.wrap(stack, 1, false), 1);
    }

    private static void appendItem(NBTTagList list, ItemStack stack, long amount) {
        if (stack == null || stack.getItem() == null || amount <= 0) return;
        IAEStack<?> aeStack = AEItemStack.create(stack.copy())
            .setStackSize(amount);
        list.appendTag(aeStack.toNBTGeneric());
    }

    private static void appendFluid(NBTTagList list, FluidStack fluid, long amount) {
        if (fluid == null || fluid.getFluid() == null || amount <= 0) return;
        FluidStack copy = fluid.copy();
        copy.amount = 1;
        IAEStack<?> aeStack = AEFluidStack.create(copy)
            .setStackSize(amount);
        list.appendTag(aeStack.toNBTGeneric());
    }

    static List<ItemStack> specialItems(Object special) {
        List<ItemStack> result = new ArrayList<>();
        if (special instanceof ItemStack stack) {
            result.add(stack);
        } else if (special instanceof ItemStack[] stacks) {
            for (ItemStack stack : stacks) {
                if (stack != null) result.add(stack);
            }
        } else if (special instanceof Iterable<?> iterable) {
            for (Object value : iterable) {
                if (value instanceof ItemStack stack) result.add(stack);
            }
        }
        return result;
    }

    public static String statusKey(int value) {
        if (value == ERR_NO_MACHINE) return "proghatches.pattern_generator.status.no_machine";
        if (value == ERR_NO_MAP) return "proghatches.pattern_generator.status.no_map";
        if (value == ERR_NO_RECIPES) return "proghatches.pattern_generator.status.no_recipes";
        if (value == ERR_INVALID_FILTER) return "proghatches.pattern_generator.filter.invalid.status";
        if (value == ERR_NOTHING_SELECTED) return "proghatches.pattern_generator.status.none_selected";
        return null;
    }

    public static String circuitLabel(int circuit) {
        if (circuit < 0) return StatCollector.translateToLocal("proghatches.pattern_generator.circuit.any");
        if (circuit == 0) return StatCollector.translateToLocal("proghatches.pattern_generator.circuit.none");
        return StatCollector.translateToLocalFormatted("proghatches.pattern_generator.circuit.value", circuit);
    }
}
