package reobf.proghatches.gametests;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import bartworks.system.material.WerkstoffLoader;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/** The manual stocking upgrades add shared item and fluid slots that recipes draw from alongside the hatch's own. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class ManualStockingUpgradeTests {

    @GameTest(template = "palladium", timeoutTicks = 600)
    public static void manualFluidSlotFeedsTheRecipe(GameTestHelper helper) {
        int id = Config.metaTileEntityOffset + Registration.DualInputHatchOffset + 3;
        PalladiumLineTests.buildReactor(helper, id);
        DualInputHatch hatch = helper.assertInstanceOf(
            DualInputHatch.class,
            helper.gtnh()
                .metaTileEntity(PalladiumLineTests.CYCLIC),
            "The dual input hatch should be placed");
        helper.setSlot("drive", 0, new ItemStack(ItemAndBlockHolder.CELL64K));

        hatch.shared.install(new ItemStack(MyMod.upgrades, 1, 4));
        hatch.shared.install(new ItemStack(MyMod.upgrades, 1, 5));
        ItemStack over = new ItemStack(MyMod.upgrades, 3, 5);
        hatch.shared.install(over);
        hatch.shared.install(over);
        hatch.shared.install(over);
        helper.assertEquals(3, hatch.shared.manualFluidUpgrades, "The fluid upgrades share the limit of four");
        helper.assertEquals(1, over.stackSize, "The upgrade beyond the limit stays in the hand");
        helper.assertEquals(3, hatch.shared.manualFluids.size(), "Every fluid upgrade adds a slot");

        hatch.shared.manualFluids.set(0, Materials.Ammonia.getGas(1000));
        NBTTagCompound saved = hatch.shared.ser();
        hatch.shared.manualFluids.set(0, null);
        hatch.shared.deser(saved);
        helper.assertEquals(1000, amount(hatch.shared.manualFluids.get(0)), "The slot's fluid is saved");

        helper.startSequence()
            .thenWaitUntil("wait for the ME network", 120, () -> {
                IGridNode node = helper
                    .assertInstanceOf(IGridHost.class, helper.assertTileEntityPresent("controller"))
                    .getGridNode(ForgeDirection.UNKNOWN);
                helper.assertTrue(node != null && node.isActive(), "The ME controller should be active");
            })
            .thenExecute("load the hatch: powder in its own slot, ammonia in the shared fluid slot", () -> {
                hatch.setInventorySlotContents(hatch.getCircuitSlot(), GTUtility.getIntegratedCircuit(1));
                hatch.setInventorySlotContents(0, copy(WerkstoffLoader.PDMetallicPowder.get(OrePrefixes.dust), 1));
            })
            .thenWaitUntil("wait for the reactor to enrich the ammonia", 400, () -> {
                helper.assertEquals(0, size(hatch.getStackInSlot(0)), "The hatch's own powder should be used");
                helper.assertEquals(0, amount(hatch.shared.manualFluids.get(0)), "The shared ammonia should be used");
            })
            .thenExecute(
                "the emptied fluid slot is cleared",
                () -> helper.assertNull(hatch.shared.manualFluids.get(0), "An emptied manual fluid slot holds nothing"))
            .thenSucceed();
    }

    private static int amount(FluidStack fluid) {
        return fluid == null ? 0 : fluid.amount;
    }

    @GameTest(template = "sifter", timeoutTicks = 400)
    public static void manualSlotFeedsTheRecipe(GameTestHelper helper) {
        int id = Config.metaTileEntityOffset + Registration.DualInputHatchOffset + 3;
        ChancePatternTests.buildSifter(helper, true, id);
        DualInputHatch hatch = helper.assertInstanceOf(
            DualInputHatch.class,
            helper.gtnh()
                .metaTileEntity(ChancePatternTests.HATCH),
            "The dual input hatch should be placed");
        ItemStack salt = WerkstoffLoader.PDSalt.get(OrePrefixes.dust);

        for (int i = 0; i < 4; i++) hatch.shared.install(new ItemStack(MyMod.upgrades, 1, 4));
        ItemStack fifth = new ItemStack(MyMod.upgrades, 1, 4);
        hatch.shared.install(fifth);
        helper.assertEquals(4, hatch.shared.manualUpgrades, "Four stocking upgrades fit");
        helper.assertEquals(1, fifth.stackSize, "A fifth stocking upgrade stays in the hand");
        helper.assertEquals(4, hatch.shared.manualItems.size(), "Every upgrade adds a slot");

        hatch.shared.manualItems.set(0, copy(salt, 3));
        NBTTagCompound saved = hatch.shared.ser();
        hatch.shared.manualItems.set(0, null);
        hatch.shared.deser(saved);
        helper.assertEquals(3, size(hatch.shared.manualItems.get(0)), "The slot's items are saved");

        hatch.setInventorySlotContents(0, copy(salt, 1));

        helper.startSequence()
            .thenWaitUntil("wait for the sifter to use every salt", 300, () -> {
                helper.assertEquals(0, size(hatch.getStackInSlot(0)), "The hatch's own salt should be used");
                helper.assertEquals(0, size(hatch.shared.manualItems.get(0)), "The shared salt should be used");
            })
            .thenExecute(
                "the emptied slot is cleared",
                () -> helper.assertNull(hatch.shared.manualItems.get(0), "An emptied manual slot holds nothing"))
            .thenSucceed();
    }

    private static ItemStack copy(ItemStack stack, int size) {
        ItemStack c = stack.copy();
        c.stackSize = size;
        return c;
    }

    private static int size(ItemStack stack) {
        return stack == null ? 0 : stack.stackSize;
    }
}
