package reobf.proghatches.item.patterngen;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.Recipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.recipe.RecipeCategory;
import gregtech.api.recipe.RecipeMap;
import gregtech.nei.GTNEIDefaultHandler;
import gregtech.nei.GTNEIDefaultHandler.CachedDefaultRecipe;

/**
 * Client-only bridge to NEI's specific-recipe API.
 * <p>
 * NEI can open a recipe by its {@link Recipe.RecipeId}, which contains the handler name, result
 * and ingredient list. The preview row already knows its {@code GTRecipe}; this finds the
 * matching GTNEI cache index and asks NEI to focus that exact recipe after opening the normal
 * item recipe/usage GUI.
 */
@SideOnly(Side.CLIENT)
public final class PatternGeneratorNei {

    private PatternGeneratorNei() {}

    public static boolean isRecipeKey(int keyCode) {
        int binding = NEIClientConfig.getKeyBinding("recipe.recipe");
        return binding >= 0 && keyCode == binding;
    }

    public static boolean isUsageKey(int keyCode) {
        int binding = NEIClientConfig.getKeyBinding("recipe.usage");
        return binding >= 0 && keyCode == binding;
    }

    public static boolean openRecipe(PatternRecipeEntry entry, ItemStack query) {
        return open(entry, query, false);
    }

    public static boolean openUsage(PatternRecipeEntry entry, ItemStack query) {
        return open(entry, query, true);
    }

    private static boolean open(PatternRecipeEntry entry, ItemStack query, boolean usage) {
        if (entry == null || query == null || query.getItem() == null) return false;
        Recipe.RecipeId recipeId = recipeId(entry);
        if (recipeId == null) return false;
        ItemStack copiedQuery = query.copy();
        Minecraft minecraft = Minecraft.getMinecraft();
        // Run after the current key event. If NEI also handles the key, this still wins and
        // focuses the row's exact recipe instead of the generic first recipe for the stack.
        minecraft.func_152344_a(() -> {
            boolean opened = usage ? GuiUsageRecipe.openRecipeGui("item", copiedQuery)
                : GuiCraftingRecipe.openRecipeGui("item", copiedQuery);
            if (!opened) return;
            GuiScreen screen = minecraft.currentScreen;
            if (screen instanceof GuiRecipe<?> gui) gui.openTargetRecipe(recipeId);
        });
        return true;
    }

    private static Recipe.RecipeId recipeId(PatternRecipeEntry entry) {
        RecipeMap<?> map = RecipeMap.ALL_RECIPE_MAPS.get(entry.mapId);
        if (map == null) return null;
        RecipeCategory category = map.getDefaultRecipeCategory();
        if (category == null) return null;
        GTNEIDefaultHandler handler = new GTNEIDefaultHandler(category);
        handler.loadCraftingRecipes(map.unlocalizedName);
        List<CachedDefaultRecipe> cache = handler.getCache();
        for (int i = 0; i < cache.size(); i++) {
            CachedDefaultRecipe cached = cache.get(i);
            if (cached != null && cached.mRecipe == entry.recipe) {
                return Recipe.RecipeId.of(handler, i);
            }
        }
        return null;
    }
}
