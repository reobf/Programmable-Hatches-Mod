package reobf.proghatches.gametests;

import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import gregtech.api.enums.ItemList;
import reobf.proghatches.item.patterngen.PatternGeneratorRecipes;
import reobf.proghatches.item.patterngen.PatternGeneratorSettings;
import reobf.proghatches.item.patterngen.PatternRecipeCollection;
import reobf.proghatches.item.patterngen.PatternRecipeEntry;
import reobf.proghatches.main.MyMod;

/** Dynamic recipe maps (furnace, microwave, EFR) are enumerated from their live registries. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class PatternGeneratorDynamicRecipeTests {

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void furnaceMapListsVanillaSmeltingRecipes(GameTestHelper h) {
        PatternGeneratorSettings settings = new PatternGeneratorSettings();
        settings.machine = ItemList.Machine_LV_E_Furnace.get(1);
        settings.recipeMapId = "gt.recipe.furnace";

        PatternRecipeCollection collection = PatternGeneratorRecipes.collect(settings);
        h.assertEquals(0, collection.error, "Electric furnace map resolves");
        h.assertEquals("gt.recipe.furnace", collection.mapId, "Furnace map id");
        h.assertTrue(!collection.all.isEmpty(), "Furnace recipes are listed");

        PatternRecipeEntry cobblestone = null;
        for (PatternRecipeEntry entry : collection.all) {
            if (entry.inputs.size() != 1 || entry.outputs.size() != 1) continue;
            ItemStack input = entry.inputs.get(0).item;
            ItemStack output = entry.outputs.get(0).item;
            if (input != null && output != null
                && input.getItem() == Item.getItemFromBlock(Blocks.cobblestone)
                && output.getItem() == Item.getItemFromBlock(Blocks.stone)) {
                cobblestone = entry;
                break;
            }
        }
        h.assertNotNull(cobblestone, "Cobblestone -> stone is listed");
        h.assertNotNull(PatternGeneratorRecipes.encode(cobblestone, null, settings), "Furnace recipe encodes");

        List<String> firstOrder = collection.allIds();
        PatternRecipeCollection again = PatternGeneratorRecipes.collect(settings);
        h.assertTrue(firstOrder.equals(again.allIds()), "Furnace recipe order is deterministic");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void microwaveListsItsBookRecipe(GameTestHelper h) {
        PatternGeneratorSettings settings = new PatternGeneratorSettings();
        settings.machine = ItemList.Machine_LV_Microwave.get(1);
        settings.recipeMapId = "gt.recipe.microwave";

        PatternRecipeCollection collection = PatternGeneratorRecipes.collect(settings);
        h.assertEquals(0, collection.error, "Microwave map resolves");
        h.assertTrue(!collection.all.isEmpty(), "Microwave recipes are listed");

        ItemStack manual = ItemList.Book_Written_03.get(1);
        PatternRecipeEntry book = null;
        for (PatternRecipeEntry entry : collection.all) {
            if (entry.inputs.size() != 1 || entry.outputs.size() != 1) continue;
            ItemStack input = entry.inputs.get(0).item;
            ItemStack output = entry.outputs.get(0).item;
            if (input != null && output != null && input.getItem() == Items.book
                && output.getItem() == manual.getItem()) {
                book = entry;
                break;
            }
        }
        h.assertNotNull(book, "Book -> written manual is listed");
        h.assertNotNull(PatternGeneratorRecipes.encode(book, null, settings), "Book recipe encodes");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void unenumerableDynamicMapReportsUnsupported(GameTestHelper h) {
        PatternGeneratorSettings settings = new PatternGeneratorSettings();
        settings.machine = ItemList.Machine_LV_Recycler.get(1);
        settings.recipeMapId = "ic.recipe.recycler";

        PatternRecipeCollection collection = PatternGeneratorRecipes.collect(settings);
        h.assertEquals(
            PatternGeneratorRecipes.ERR_DYNAMIC_UNSUPPORTED,
            collection.error,
            "Recycler has no enumerable recipe source");
        h.succeed();
    }
}
