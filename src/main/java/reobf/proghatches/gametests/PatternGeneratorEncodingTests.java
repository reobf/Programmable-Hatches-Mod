package reobf.proghatches.gametests;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import gregtech.api.util.GTRecipe;
import reobf.proghatches.item.patterngen.PatternGeneratorRecipes;
import reobf.proghatches.item.patterngen.PatternGeneratorSettings;
import reobf.proghatches.item.patterngen.PatternRecipeEntry;
import reobf.proghatches.main.MyMod;

/** Encoder checks that need real ItemStack, FluidStack and AE2 pattern classes. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class PatternGeneratorEncodingTests {

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void removeByproductsKeepsTheFirstGuaranteedOutput(GameTestHelper h) {
        ItemStack first = new ItemStack(Items.iron_ingot, 2);
        ItemStack second = new ItemStack(Items.gold_ingot, 3);
        GTRecipe recipe = new GTRecipe(
            false,
            new ItemStack[] { new ItemStack(Blocks.stone) },
            new ItemStack[] { first, second },
            null,
            new int[] { 10000, 10000 },
            null,
            null,
            null,
            new FluidStack[] { new FluidStack(FluidRegistry.WATER, 1000) },
            new FluidStack[] { new FluidStack(FluidRegistry.LAVA, 500) },
            20,
            30,
            0);
        PatternRecipeEntry entry = PatternRecipeEntry.of(recipe, "test.map", "Test map");
        h.assertNotNull(entry, "Recipe entry");

        PatternGeneratorSettings settings = new PatternGeneratorSettings();
        settings.author = "Tester";

        ItemStack all = PatternGeneratorRecipes.encode(entry, null, settings);
        h.assertNotNull(all, "Full pattern encodes");
        h.assertEquals(3, outputCount(all), "Toggle off keeps two items and one fluid");

        settings.firstOutputOnly = true;
        ItemStack firstOnly = PatternGeneratorRecipes.encode(entry, null, settings);
        h.assertNotNull(firstOnly, "Remove-byproducts pattern encodes");
        h.assertEquals(1, outputCount(firstOnly), "Toggle on keeps one output");
        NBTTagCompound tag = firstOnly.getTagCompound();
        h.assertEquals("Tester", tag.getString("author"), "Author metadata is written");
        h.assertTrue(tag.hasKey("encodedAt"), "Encoded-at metadata is written");
        h.assertEquals(
            2L,
            tag.getTagList("out", 10)
                .getCompoundTagAt(0)
                .getLong("Cnt"),
            "The first guaranteed item output is kept");
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void fluidOnlyRecipeKeepsTheFirstFluid(GameTestHelper h) {
        GTRecipe recipe = new GTRecipe(
            false,
            new ItemStack[0],
            new ItemStack[0],
            null,
            new int[0],
            null,
            null,
            null,
            new FluidStack[] { new FluidStack(FluidRegistry.WATER, 1000) },
            new FluidStack[] {
                new FluidStack(FluidRegistry.LAVA, 500),
                new FluidStack(FluidRegistry.WATER, 250) },
            20,
            30,
            0);
        PatternRecipeEntry entry = PatternRecipeEntry.of(recipe, "test.fluid", "Test fluid map");
        h.assertNotNull(entry, "Fluid recipe entry");

        PatternGeneratorSettings settings = new PatternGeneratorSettings();
        settings.firstOutputOnly = true;
        ItemStack pattern = PatternGeneratorRecipes.encode(entry, null, settings);
        h.assertNotNull(pattern, "Fluid-only pattern encodes");
        h.assertEquals(1, outputCount(pattern), "Toggle on keeps one fluid output");
        NBTTagList outputs = pattern.getTagCompound()
            .getTagList("out", 10);
        h.assertEquals(
            500L,
            outputs.getCompoundTagAt(0)
                .getLong("Cnt"),
            "The first fluid output is kept");
        h.succeed();
    }

    private static int outputCount(ItemStack pattern) {
        return pattern.getTagCompound()
            .getTagList("out", 10)
            .tagCount();
    }
}
