package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.oredict.OreDictionary;

import ganymedes01.etfuturum.recipes.BlastFurnaceRecipes;
import ganymedes01.etfuturum.recipes.SmokerRecipes;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.SubTag;
import gregtech.api.objects.ItemData;
import gregtech.api.objects.MaterialStack;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.maps.EFRBlastingBackend;
import gregtech.api.recipe.maps.EFRSmokingBackend;
import gregtech.api.recipe.maps.FurnaceBackend;
import gregtech.api.recipe.maps.MicrowaveBackend;
import gregtech.api.recipe.maps.NonGTBackend;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;

/**
 * Synthetic recipes for GT's dynamic recipe maps. Those maps keep
 * {@link RecipeMap#getAllRecipes()} empty and resolve a recipe from a live registry when the
 * machine looks up an input, so the pattern generator has to enumerate the registry itself.
 * <p>
 * Only maps backed by a list that can be walked are supported. Wildcard-damage inputs are
 * skipped because an AE2 pattern cannot express "any damage".
 */
final class PatternDynamicRecipes {

    private static final int EUT = 4;
    private static final int FURNACE_DURATION = 128;
    private static final int MICROWAVE_DURATION = 32;
    private static final int EFR_DURATION = 64;

    private PatternDynamicRecipes() {}

    /**
     * @return recipes for a supported dynamic map, an empty list when the map is supported but
     *         currently has no recipes, or {@code null} when the backend is dynamic but has no
     *         enumerable source.
     */
    static List<GTRecipe> collect(RecipeMap<?> map) {
        if (map == null) return null;
        Object backend = map.getBackend();
        if (backend instanceof FurnaceBackend) return smelting(FURNACE_DURATION, false);
        if (backend instanceof MicrowaveBackend) return smelting(MICROWAVE_DURATION, true);
        if (backend instanceof EFRBlastingBackend) {
            return Mods.EtFuturumRequiem.isModLoaded() ? efrBlasting() : Collections.emptyList();
        }
        if (backend instanceof EFRSmokingBackend) {
            return Mods.EtFuturumRequiem.isModLoaded() ? efrSmoking() : Collections.emptyList();
        }
        return null;
    }

    /** Dynamic maps that are not covered by {@link #collect} use a dedicated status message. */
    static boolean isDynamic(RecipeMap<?> map) {
        return map != null && map.getBackend() instanceof NonGTBackend;
    }

    private static List<GTRecipe> smelting(int duration, boolean microwave) {
        Map<String, GTRecipe> recipes = new TreeMap<>();
        for (ItemStack input : concreteInputs(FurnaceRecipes.smelting().getSmeltingList().keySet())) {
            try {
                ItemStack output = GTModHandler.getSmeltingOutput(GTUtility.copyAmount(1, input), false, null);
                if (microwave && !microwaveSafe(input, output)) continue;
                add(recipes, input, output, duration);
            } catch (Throwable ignored) {
                // One malformed registry entry must not take the whole preview down.
            }
        }
        if (microwave) {
            ItemStack book = new ItemStack(Items.book);
            ItemStack output = GTUtility.getWrittenBook("Manual_Microwave", ItemList.Book_Written_03.get(1));
            add(recipes, book, output, duration);
        }
        return new ArrayList<>(recipes.values());
    }

    private static List<GTRecipe> efrBlasting() {
        return efr(BlastFurnaceRecipes.smelting().smeltingList.keySet(), true);
    }

    private static List<GTRecipe> efrSmoking() {
        return efr(SmokerRecipes.smelting().smeltingList.keySet(), false);
    }

    /**
     * EFR falls back to the vanilla smelting list for its auto-added blasting/smoking recipes,
     * so the union of both registries is the real candidate set. The GT helper applies EFR's
     * blacklist and its {@code canAdd} filter.
     */
    private static List<GTRecipe> efr(Collection<ItemStack> explicit, boolean blasting) {
        Map<String, GTRecipe> recipes = new TreeMap<>();
        List<ItemStack> candidates = new ArrayList<>(explicit);
        candidates.addAll(
            FurnaceRecipes.smelting()
                .getSmeltingList()
                .keySet());
        for (ItemStack input : concreteInputs(candidates)) {
            try {
                ItemStack output = blasting
                    ? GTModHandler.getEFRBlastingOutput(GTUtility.copyAmount(1, input), false, null)
                    : GTModHandler.getEFRSmokingOutput(GTUtility.copyAmount(1, input), false, null);
                add(recipes, input, output, EFR_DURATION);
            } catch (Throwable ignored) {}
        }
        return new ArrayList<>(recipes.values());
    }

    /**
     * Microwaves explode instead of smelting metals, explosives, flammable items, burnables and
     * a few special items. Mirror that check so no pattern promises a recipe the machine will
     * never run.
     */
    private static boolean microwaveSafe(ItemStack input, ItemStack output) {
        if (input == null || output == null) return false;
        for (ItemStack item : new ItemStack[] { GTUtility.getContainerItem(input, true), input,
            GTUtility.getContainerItem(output, true), output }) {
            if (item == null) continue;
            if (GTUtility.areStacksEqual(item, new ItemStack(Blocks.netherrack, 1, OreDictionary.WILDCARD_VALUE), true)
                || GTUtility.areStacksEqual(item, new ItemStack(Blocks.tnt, 1, OreDictionary.WILDCARD_VALUE), true)
                || GTUtility.areStacksEqual(item, new ItemStack(Items.egg, 1, OreDictionary.WILDCARD_VALUE), true)
                || GTUtility.areStacksEqual(
                    item,
                    new ItemStack(Items.firework_charge, 1, OreDictionary.WILDCARD_VALUE),
                    true)
                || GTUtility.areStacksEqual(item, new ItemStack(Items.fireworks, 1, OreDictionary.WILDCARD_VALUE), true)
                || GTUtility.areStacksEqual(
                    item,
                    new ItemStack(Items.fire_charge, 1, OreDictionary.WILDCARD_VALUE),
                    true)) {
                return false;
            }
            ItemData data = GTOreDictUnificator.getItemData(item);
            if (data != null) {
                if (isMicrowaveUnsafe(data.mMaterial)) return false;
                for (MaterialStack byproduct : data.mByProducts) {
                    if (isMicrowaveUnsafe(byproduct)) return false;
                }
            }
            if (TileEntityFurnace.getItemBurnTime(item) > 0) return false;
        }
        return true;
    }

    private static boolean isMicrowaveUnsafe(MaterialStack stack) {
        if (stack == null || stack.mMaterial == null) return false;
        Materials material = stack.mMaterial;
        return material.contains(SubTag.METAL) || material.contains(SubTag.EXPLOSIVE)
            || material.contains(SubTag.FLAMMABLE);
    }

    private static void add(Map<String, GTRecipe> recipes, ItemStack input, ItemStack output, int duration) {
        if (!enumerable(input) || output == null || output.getItem() == null || output.stackSize <= 0) return;
        try {
            GTRecipe recipe = GTValues.RA.stdBuilder()
                .itemInputs(GTUtility.copyAmount(1, input))
                .itemOutputs(output.copy())
                .duration(duration)
                .eut(EUT)
                .build()
                .orElse(null);
            if (recipe != null) recipes.put(PatternIngredient.itemKey(input, 1), recipe);
        } catch (Throwable ignored) {
            // One malformed registry entry must not take the whole preview down.
        }
    }

    private static List<ItemStack> concreteInputs(Collection<ItemStack> keys) {
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack input : keys) {
            if (enumerable(input)) inputs.add(input.copy());
        }
        // Deterministic order: the registries are hash maps, their iteration order is not stable.
        inputs.sort(Comparator.comparing(stack -> PatternIngredient.itemKey(stack, 1)));
        return inputs;
    }

    private static boolean enumerable(ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) return false;
        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null || !tag.getBoolean("*") || tag.func_150296_c()
            .size() != 1;
    }
}
