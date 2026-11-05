package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;
import static reobf.proghatches.gametests.ChancePatternTests.place;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import gregtech.api.enums.ItemList;
import gregtech.api.metatileentity.implementations.MTEHatchEnergyDebug;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.multi.IngredientDistributor;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import tectech.thing.CustomItemList;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"guidenh"})
public class IngredientDistributorGuideTests {

    private static final TestPos CONTROL = at(5, 1, 1), INPUT = at(4, 0, 1);
    private static final TestPos[] BUSES = {at(3, 0, 1), at(2, 0, 1), at(1, 0, 1)};
    private static final TestPos[] FLUIDS = {at(3, 0, 3), at(2, 0, 3)};

    @GameTest(template = "ingredient_distributor", timeoutTicks = 500)
    public static void keepsRepeatedIngredientsInTheirOrderedOutputs(GameTestHelper h) {
        IngredientDistributor machine = (IngredientDistributor) place(h, CONTROL,
            Config.metaTileEntityOffset + Registration.IngredientDistributorOffset, ForgeDirection.NORTH).getMetaTileEntity();
        machine.construct(new ItemStack(Items.stick, 2), false);
        DualInputHatch input = (DualInputHatch) place(h, INPUT,
            Config.metaTileEntityOffset + Registration.QuadDualInputHatchOffset + 3, ForgeDirection.NORTH).getMetaTileEntity();
        place(h, at(6, 0, 1), CustomItemList.hatch_CreativeMaintenance, ForgeDirection.NORTH);
        var power = (MTEHatchEnergyDebug) place(h, at(6, 1, 1), ItemList.DebugEnergyHatch,
            ForgeDirection.NORTH).getMetaTileEntity();
        power.setVoltageTier(4); power.setAmperage(2); power.setEUVar(Long.MAX_VALUE);
        for (TestPos p : BUSES) place(h, p, ItemList.Hatch_Output_Bus_LV, ForgeDirection.NORTH);
        for (TestPos p : FLUIDS) place(h, p, ItemList.Hatch_Output_LV, ForgeDirection.SOUTH);
        h.assertTrue(h.gtnh().multiblock(CONTROL).forceStructureCheck(), "The distributor should form");
        machine.getBaseMetaTileEntity().disableWorking();
        GuideSceneRecorder scene = new GuideSceneRecorder(h, "ingredient_distributor", "proghatches.guide.distributor", 8, 3, 5);
        h.startSequence()
            .thenExecute("record the distributor structure", () -> {
                scene.capture();
                scene.frame(0, "overview", true);
                scene.buildStages(60, 80, p -> {
                    if (p.equals(INPUT)) return 3;
                    for (TestPos b : BUSES) if (p.equals(b)) return 3;
                    for (TestPos f : FLUIDS) if (p.equals(f)) return 3;
                    return p.x() >= 4 ? 0 : p.x() == 0 ? 2 : 1;
                }, "head", "outputs", "end", "hatches");
                input.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 2));
                input.setInventorySlotContents(1, new ItemStack(Items.gold_ingot, 3));
                input.setInventorySlotContents(2, new ItemStack(Items.iron_ingot, 5));
                h.assertEquals(1000, input.fill(ForgeDirection.NORTH, new FluidStack(FluidRegistry.WATER, 1000), true), "Water should enter the first tank");
                h.assertEquals(500, input.fill(ForgeDirection.NORTH, new FluidStack(FluidRegistry.LAVA, 500), true), "Lava should enter another tank");
                scene.patch(scene.frame(440, "input", true), INPUT);
                machine.getBaseMetaTileEntity().enableWorking();
            })
            .thenWaitUntil("distribute every input to its own output", 350, () -> {
                h.assertEquals(2, count(h, BUSES[0], Items.iron_ingot), "First iron slot");
                h.assertEquals(3, count(h, BUSES[1], Items.gold_ingot), "Middle gold slot");
                h.assertEquals(5, count(h, BUSES[2], Items.iron_ingot), "Repeated iron slot");
                var first = (MTEHatchOutput) h.gtnh().gtTile(FLUIDS[0]).getMetaTileEntity();
                var second = (MTEHatchOutput) h.gtnh().gtTile(FLUIDS[1]).getMetaTileEntity();
                h.assertTrue(first.getFluid() != null && first.getFluid().isFluidEqual(new FluidStack(FluidRegistry.WATER, 1000))
                    && first.getFluid().amount == 1000, "The first fluid output should contain the water");
                h.assertTrue(second.getFluid() != null && second.getFluid().isFluidEqual(new FluidStack(FluidRegistry.LAVA, 500))
                    && second.getFluid().amount == 500, "The second fluid output should contain the lava");
                h.assertTrue(java.util.Arrays.stream(input.theInv.getItemInputs()).noneMatch(s -> s != null && s.stackSize > 0)
                    && java.util.Arrays.stream(input.theInv.getFluidInputs()).noneMatch(s -> s != null && s.amount > 0), "The input should be consumed by the transfer");
            })
            .thenExecute("record ordered outputs", () -> {
                var frame = scene.frame(600, "distributed", true);
                scene.patch(frame, INPUT);
                for (TestPos p : BUSES) scene.patch(frame, p);
                for (TestPos p : FLUIDS) scene.patch(frame, p);
                scene.frame(760, "sameItem", true);
                scene.frame(900, "done", true);
                scene.save(980);
            })
            .thenSucceed();
    }

    private static int count(GameTestHelper h, TestPos p, net.minecraft.item.Item item) {
        var machine = h.gtnh().gtTile(p).getMetaTileEntity();
        int result = 0;
        for (int i = 0; i < machine.getSizeInventory(); i++) {
            ItemStack stack = machine.getStackInSlot(i);
            if (stack != null && stack.getItem() == item) result += stack.stackSize;
        }
        return result;
    }
}
