package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.util.GTRecipe;
import gregtech.common.items.ItemIntegratedCircuit;

/** A recipe with its stable id, display ingredients and circuit. */
public final class PatternRecipeEntry {

    public final GTRecipe recipe;
    public final String id;
    public final String mapId;
    public final String mapName;
    public final int circuit;
    public final String ncLabel;
    public final List<PatternIngredient> inputs;
    public final List<PatternIngredient> outputs;

    private PatternRecipeEntry(GTRecipe recipe, String id, String mapId, String mapName, int circuit, String ncLabel,
        List<PatternIngredient> inputs, List<PatternIngredient> outputs) {
        this.recipe = recipe;
        this.id = id;
        this.mapId = mapId;
        this.mapName = mapName;
        this.circuit = circuit;
        this.ncLabel = ncLabel;
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public static PatternRecipeEntry of(GTRecipe recipe, String mapId, String mapName) {
        if (recipe == null) return null;
        int circuit = recipeCircuit(recipe);

        List<PatternIngredient> inputs = new ArrayList<>();
        List<String> inputKeys = new ArrayList<>();
        List<String> ncNames = new ArrayList<>();
        if (recipe.mInputs != null) {
            for (ItemStack stack : recipe.mInputs) {
                if (stack == null || stack.getItem() == null) continue;
                inputKeys.add(PatternIngredient.itemKey(stack, stack.stackSize));
                if (stack.getItem() instanceof ItemIntegratedCircuit) continue;
                if (stack.stackSize <= 0) {
                    String name = PatternIngredient.displayName(stack);
                    if (!name.isEmpty() && !ncNames.contains(name)) ncNames.add(name);
                    PatternIngredient ingredient = PatternIngredient.ofItem(stack, 1);
                    if (ingredient != null) inputs.add(ingredient);
                    continue;
                }
                PatternIngredient ingredient = PatternIngredient.ofItem(stack, stack.stackSize);
                if (ingredient != null) inputs.add(ingredient);
            }
        }
        for (ItemStack stack : PatternGeneratorRecipes.specialItems(recipe.mSpecialItems)) {
            if (stack == null || stack.getItem() == null) continue;
            inputKeys.add(PatternIngredient.itemKey(stack, Math.max(1, stack.stackSize)));
            String name = PatternIngredient.displayName(stack);
            if (!name.isEmpty() && !ncNames.contains(name)) ncNames.add(name);
            PatternIngredient ingredient = PatternIngredient.ofItem(stack, Math.max(1, stack.stackSize));
            if (ingredient != null) inputs.add(ingredient);
        }
        if (recipe.mFluidInputs != null) {
            for (FluidStack fluid : recipe.mFluidInputs) {
                if (fluid == null || fluid.getFluid() == null || fluid.amount <= 0) continue;
                inputKeys.add(PatternIngredient.fluidKey(fluid, fluid.amount));
                PatternIngredient ingredient = PatternIngredient.ofFluid(fluid, fluid.amount);
                if (ingredient != null) inputs.add(ingredient);
            }
        }

        List<PatternIngredient> outputs = new ArrayList<>();
        List<String> outputKeys = new ArrayList<>();
        if (recipe.mOutputs != null) {
            for (int i = 0; i < recipe.mOutputs.length; i++) {
                ItemStack stack = recipe.mOutputs[i];
                if (stack == null || stack.getItem() == null) continue;
                int chance = recipe.getOutputChance(i);
                outputKeys.add(PatternIngredient.itemKey(stack, stack.stackSize) + "|c" + chance);
                if (stack.stackSize <= 0 || chance < 10000) continue;
                PatternIngredient ingredient = PatternIngredient.ofItem(stack, stack.stackSize);
                if (ingredient != null) outputs.add(ingredient);
            }
        }
        if (recipe.mFluidOutputs != null) {
            for (FluidStack fluid : recipe.mFluidOutputs) {
                if (fluid == null || fluid.getFluid() == null || fluid.amount <= 0) continue;
                outputKeys.add(PatternIngredient.fluidKey(fluid, fluid.amount));
                PatternIngredient ingredient = PatternIngredient.ofFluid(fluid, fluid.amount);
                if (ingredient != null) outputs.add(ingredient);
            }
        }

        String ncLabel = String.join(", ", ncNames);
        return new PatternRecipeEntry(
            recipe,
            PatternRecipeId.of(mapId, circuit, inputKeys, outputKeys),
            mapId,
            mapName,
            circuit,
            ncLabel,
            inputs,
            outputs);
    }

    public static int recipeCircuit(GTRecipe recipe) {
        if (recipe.mInputs == null) return -1;
        for (ItemStack stack : recipe.mInputs) {
            if (stack != null && stack.getItem() instanceof ItemIntegratedCircuit) {
                int circuit = stack.getItemDamage();
                return circuit > 0 ? circuit : -1;
            }
        }
        return -1;
    }
}
