package reobf.proghatches.gametests;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import gregtech.api.enums.ItemList;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/**
 * The Interface Terminal names a pattern hatch on the client: the hatch hands it the item key of the machine it
 * feeds and a component carrying the circuits, marks and fluids, and both travel through the terminal's packet.
 */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class InterfaceTerminalNameTests {

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void hatchNameTranslatesOnTheClient(GameTestHelper helper) {
        int id = Config.metaTileEntityOffset + Registration.ExPatternOffset;
        ChancePatternTests.buildSifter(helper, true, id);
        PatternDualInputHatch hatch = helper.assertInstanceOf(
            PatternDualInputHatch.class,
            helper.gtnh()
                .metaTileEntity(ChancePatternTests.HATCH),
            "The pattern hatch should be placed");
        hatch.setInventorySlotContents(hatch.getCircuitSlot(), GTUtility.getIntegratedCircuit(7));

        ItemStack sifter = ItemList.LargeSifter.get(1);
        helper.assertEquals(sifter.getUnlocalizedName(), hatch.getRawName(), "The raw name is the controller's key");
        helper.assertEquals(
            sifter.getDisplayName(),
            StatCollector.translateToLocal(hatch.getRawName() + ".name"),
            "The key translates to the controller's name");
        helper.assertEquals(" - 7", text(hatch.getNameSuffix()), "The suffix lists the circuit");
        helper.assertEquals(" - 7", text(roundTrip(hatch.getNameSuffix())), "The suffix survives the packet");
        helper.assertEquals(sifter.getDisplayName() + " - 7", hatch.getName(), "The server-side name reads the same");
        helper.assertEquals(
            id,
            hatch.getSelfRep()
                .getItemDamage(),
            "The hatch shows its own item");

        hatch.setCustomName("mk2");
        helper.assertEquals("mk2", hatch.getRawName(), "A custom name is sent as it is");
        helper.assertNull(hatch.getNameSuffix(), "A custom name has no suffix");
        helper.succeed();
    }

    private static String text(IChatComponent component) {
        return component == null ? null : component.getUnformattedText();
    }

    private static IChatComponent roundTrip(IChatComponent component) {
        return IChatComponent.Serializer.func_150699_a(IChatComponent.Serializer.func_150696_a(component));
    }
}
