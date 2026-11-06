package reobf.proghatches.gametests;

import net.minecraft.item.ItemStack;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import gregtech.api.GregTechAPI;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/** Recipes of the items a player reaches through the guide and of the top circuit tiers. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class HatchRecipeTests {

    private static final int[] BUFFERED = { Registration.BufferedDualInputHatchOffset,
        Registration.BufferedQuadDualInputHatchOffset, Registration.BufferedQuadDualInputHatchMKIIOffset };

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void cyclicHatchIsAssembledFromThePatternHatch(GameTestHelper h) {
        GTRecipe recipe = recipe(RecipeMaps.assemblerRecipes, machine(Registration.CyclicPatternOffset));
        h.assertNotNull(recipe, "The assembler makes the cyclic pattern hatch");
        h.assertTrue(lists(recipe.mInputs, machine(Registration.PatternOffset)), "The pattern hatch is an input");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void guideItemsHaveRecipes(GameTestHelper h) {
        GTRecipe autogen = recipe(RecipeMaps.assemblerRecipes, machine(Registration.ExPatternOffsetAutogen));
        h.assertNotNull(autogen, "The assembler makes the recipe map hatch");
        h.assertTrue(lists(autogen.mInputs, machine(Registration.ExPatternOffset)), "The MK.II hatch is an input");
        h.assertNotNull(
            recipe(RecipeMaps.assemblerRecipes, new ItemStack(MyMod.orbswitcher)),
            "The assembler makes the catalyst item switcher");
        h.assertNotNull(
            recipe(RecipeMaps.assemblerRecipes, new ItemStack(MyMod.exciter)),
            "The assembler makes the ad-hoc network exciter");
        h.assertNotNull(
            recipe(RecipeMaps.formingPressRecipes, new ItemStack(MyMod.upgrades, 1, 3)),
            "The forming press makes the infinity buffer upgrade");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void topTiersHaveTheirHatchesAndArms(GameTestHelper h) {
        for (int tier = 12; tier <= 14; tier++) {
            for (int family : BUFFERED) {
                h.assertNotNull(
                    recipe(RecipeMaps.assemblerRecipes, machine(family + tier)),
                    "Hatch family " + family + " has tier " + tier);
            }
            h.assertTrue(lineMakes(new ItemStack(MyMod.smartarm, 1, tier)), "The assembly line makes arm " + tier);
        }
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void advancedHatchesTakeTheNextCircuit(GameTestHelper h) {
        for (int tier = 0; tier <= 14; tier++) {
            GTRecipe recipe = recipe(
                RecipeMaps.assemblerRecipes,
                machine(Registration.BufferedQuadDualInputHatchMKIIOffset + tier));
            h.assertNotNull(recipe, "Advanced hatch tier " + tier + " has a recipe");
            for (ItemStack input : recipe.mInputs) h.assertNotNull(input, "Tier " + tier + " names every input");
        }
        h.succeed();
    }

    private static ItemStack machine(int offset) {
        return new ItemStack(GregTechAPI.sBlockMachines, 1, Config.metaTileEntityOffset + offset);
    }

    private static GTRecipe recipe(RecipeMap<?> map, ItemStack output) {
        for (GTRecipe candidate : map.getAllRecipes()) {
            if (lists(candidate.mOutputs, output)) return candidate;
        }
        return null;
    }

    private static boolean lineMakes(ItemStack output) {
        for (GTRecipe.RecipeAssemblyLine line : GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes) {
            if (same(line.mOutput, output)) return true;
        }
        return false;
    }

    private static boolean lists(ItemStack[] stacks, ItemStack wanted) {
        for (ItemStack stack : stacks) {
            if (same(stack, wanted)) return true;
        }
        return false;
    }

    private static boolean same(ItemStack a, ItemStack b) {
        return a != null && a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage();
    }
}
