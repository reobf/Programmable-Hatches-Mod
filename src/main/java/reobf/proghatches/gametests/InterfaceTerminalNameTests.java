package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import gregtech.api.enums.ItemList;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatchInventoryMappingSlave;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/**
 * The Interface Terminal names a pattern hatch on the client: the hatch hands it the item key of the machine it
 * feeds, a component carrying the circuits, marks and fluids, and the machine's item, which names the entry when the
 * client has no translation for the key. All of them travel through the terminal's packet.
 */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class InterfaceTerminalNameTests {

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void hatchNameTranslatesOnTheClient(GameTestHelper helper) {
        int id = Config.metaTileEntityOffset + Registration.ExPatternOffset;
        ChancePatternTests.buildSifter(helper, true, id);
        PatternDualInputHatch hatch = hatch(helper);
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
        helper.assertEquals(
            sifter.getItemDamage(),
            hatch.getDisplayRep()
                .getItemDamage(),
            "The display rep is the controller's item");
        helper.assertEquals(
            sifter.getDisplayName(),
            clientName(hatch.getRawName(), hatch.getDisplayRep()),
            "The client names the entry after the controller");

        hatch.setCustomName("mk2");
        helper.assertEquals("mk2", hatch.getRawName(), "A custom name is sent as it is");
        helper.assertNull(hatch.getNameSuffix(), "A custom name has no suffix");
        helper.assertEquals(
            "mk2",
            clientName(hatch.getRawName(), hatch.getDisplayRep()),
            "The client shows the custom name");
        helper.assertEquals(
            sifter.getDisplayName(),
            hatch.getCrafterIcon()
                .getDisplayName(),
            "The custom name stays off the controller's icon");
        helper.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void machineWithoutAKeyIsNamedByItsItem(GameTestHelper helper) {
        ChancePatternTests.buildSifter(helper, true, Config.metaTileEntityOffset + Registration.PatternOffset);
        PatternDualInputHatch hatch = hatch(helper);

        ItemStack bus = ItemList.Hatch_Input_Bus_LV.get(1);
        hatch.updateCraftingIcon(bus, null);
        String raw = hatch.getRawName();
        helper.assertEquals(bus.getUnlocalizedName(), raw, "The raw name is the machine's key");
        helper.assertFalse(
            StatCollector.canTranslate(raw) || StatCollector.canTranslate(raw + ".name"),
            "GT registers no key for a tiered hatch");
        helper.assertEquals(
            bus.getItemDamage(),
            hatch.getDisplayRep()
                .getItemDamage(),
            "The display rep is the machine's item");
        helper.assertEquals(
            bus.getDisplayName(),
            clientName(raw, hatch.getDisplayRep()),
            "The client names the entry after the machine's item");
        helper.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 200)
    public static void mirrorIsNamedAfterItsMaster(GameTestHelper helper) {
        ChancePatternTests.buildSifter(helper, true, Config.metaTileEntityOffset + Registration.PatternOffset);
        PatternDualInputHatch hatch = hatch(helper);
        PatternDualInputHatchInventoryMappingSlave<?> mirror = helper.assertInstanceOf(
            PatternDualInputHatchInventoryMappingSlave.class,
            ChancePatternTests
                .place(
                    helper,
                    at(4, 1, 3),
                    Config.metaTileEntityOffset + Registration.PatternMappingSlaveOffset,
                    ForgeDirection.NORTH)
                .getMetaTileEntity(),
            "The mirror should be placed");
        TestPos master = helper.absolute(ChancePatternTests.HATCH);
        helper.assertNotNull(
            mirror.trySetMasterFromCoord(master.x(), master.y(), master.z()),
            "The mirror links to the hatch");

        hatch.setInventorySlotContents(hatch.getCircuitSlot(), GTUtility.getIntegratedCircuit(7));
        ItemStack sifter = ItemList.LargeSifter.get(1);
        helper.assertEquals(hatch.getRawName(), mirror.getRawName(), "The mirror sends its master's key");
        helper.assertEquals(" - 7(Mapped)", text(mirror.getNameSuffix()), "The mirror's suffix lists the circuit once");
        helper.assertEquals(
            sifter.getDisplayName() + " - 7(Mapped)",
            mirror.getName(),
            "The mirror's server-side name reads the same");
        helper.assertEquals(
            sifter.getItemDamage(),
            mirror.getDisplayRep()
                .getItemDamage(),
            "The mirror shows its master's controller");
        helper.assertEquals(
            sifter.getDisplayName(),
            clientName(mirror.getRawName(), mirror.getDisplayRep()),
            "The client names the mirror after the controller");

        mirror.setCustomName("mirror");
        helper.assertEquals(
            "mirror",
            clientName(mirror.getRawName(), mirror.getDisplayRep()),
            "The client shows the mirror's custom name");
        helper.assertEquals(
            sifter.getDisplayName(),
            clientName(hatch.getRawName(), hatch.getDisplayRep()),
            "The mirror's custom name stays off the hatch");
        helper.succeed();
    }

    private static PatternDualInputHatch hatch(GameTestHelper helper) {
        return helper.assertInstanceOf(
            PatternDualInputHatch.class,
            helper.gtnh()
                .metaTileEntity(ChancePatternTests.HATCH),
            "The pattern hatch should be placed");
    }

    private static String clientName(String raw, ItemStack rep) {
        if (StatCollector.canTranslate(raw)) return StatCollector.translateToLocal(raw);
        if (StatCollector.canTranslate(raw + ".name")) return StatCollector.translateToLocal(raw + ".name");
        return rep != null ? rep.getDisplayName() : StatCollector.translateToFallback(raw);
    }

    private static String text(IChatComponent component) {
        return component == null ? null : component.getUnformattedText();
    }

    private static IChatComponent roundTrip(IChatComponent component) {
        return IChatComponent.Serializer.func_150699_a(IChatComponent.Serializer.func_150696_a(component));
    }
}
