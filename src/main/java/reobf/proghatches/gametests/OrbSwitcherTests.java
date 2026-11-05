package reobf.proghatches.gametests;

import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;
import static com.gtnewhorizons.horizonqa.api.TestPos.at;
import static reobf.proghatches.gametests.ChancePatternTests.ae;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.util.item.AEItemStack;
import gregtech.api.enums.ItemList;
import gregtech.api.util.GTUtility;
import gregtech.common.items.behaviors.BehaviourDataOrb;
import reobf.proghatches.ae.TileOrbSwitcher;
import reobf.proghatches.gt.metatileentity.AutoDataOrbHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class OrbSwitcherTests {

    private static final TestPos SWITCHER = at(1, 1, 1), HATCH = at(1, 1, 2), TARGET_BUS = at(2, 1, 1),
        TARGET_CONTROLLER = at(2, 2, 1), ORB_HATCH = at(3, 1, 1);
    private static final BaseActionSource SOURCE = new BaseActionSource();

    private static ItemStack orb(String name) {
        ItemStack s = ItemList.Tool_DataOrb.get(1);
        BehaviourDataOrb.setDataTitle(s, "Elemental-Scan");
        BehaviourDataOrb.setDataName(s, name);
        return s;
    }

    private static void set(TileOrbSwitcher tile, String name, Object value) {
        try {
            Field f = TileOrbSwitcher.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(tile, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void nbtMatchingComparesTags(GameTestHelper h) {
        TileOrbSwitcher tile = new TileOrbSwitcher();
        ItemStack iron = orb("Iron"), gold = orb("Gold");
        IAEItemStack stored = AEItemStack.create(iron);
        h.assertTrue(tile.same(stored, iron.copy()), "An orb matches itself with NBT matching off");
        h.assertTrue(tile.same(stored, gold.copy()), "Orbs differ only by NBT, so they match with NBT matching off");
        set(tile, "nbt", true);
        h.assertTrue(tile.same(stored, iron.copy()), "An orb matches itself with NBT matching on");
        h.assertFalse(tile.same(stored, gold.copy()), "Two different orbs differ with NBT matching on");
        h.succeed();
    }

    @GameTest(template = "palladium", timeoutTicks = 800)
    public static void orbFollowsTheProgrammedHatch(GameTestHelper h) {
        ItemStack iron = orb("Iron"), gold = orb("Gold");
        h.setSlot("drive", 0, cell(h, iron, gold));
        DualInputHatch hatch = (DualInputHatch) ChancePatternTests
            .place(h, HATCH, Config.metaTileEntityOffset + Registration.DualInputHatchOffset + 1, ForgeDirection.NORTH)
            .getMetaTileEntity();
        AutoDataOrbHatch orbHatch = (AutoDataOrbHatch) ChancePatternTests
            .place(h, ORB_HATCH, Config.metaTileEntityOffset + Registration.DataOrbOffset, ForgeDirection.EAST)
            .getMetaTileEntity();
        orbHatch.setInventorySlotContents(orbHatch.getCircuitSlot(), GTUtility.getIntegratedCircuit(1));
        buildTargetNetwork(h);
        TileOrbSwitcher switcher = placeSwitcher(h);

        h.startSequence()
            .thenWaitUntil("wait for the stock network", 120, () -> h.assertEquals(1L, stored(h, iron), "Iron should be stocked"))
            .thenExecute("program the iron orb", () -> program(hatch, iron))
            .thenWaitUntil("wait for the iron orb", 300, () -> {
                h.assertTrue(same(orbHatch.getOrbByCircuit(), iron), "The orb hatch should hold the iron orb");
                h.assertEquals(0L, stored(h, iron), "The iron orb should leave the stock network");
                h.assertEquals(1L, stored(h, gold), "The gold orb should stay in the stock network");
            })
            .thenExecute("program the gold orb", () -> program(hatch, gold))
            .thenWaitUntil("wait for the gold orb", 300, () -> {
                h.assertTrue(same(orbHatch.getOrbByCircuit(), gold), "The orb hatch should hold the gold orb");
                h.assertEquals(1L, stored(h, iron), "The iron orb should return to the stock network");
                h.assertEquals(0L, stored(h, gold), "The gold orb should leave the stock network");
            })
            .thenSucceed();
    }

    private static void program(DualInputHatch hatch, ItemStack orb) {
        hatch.setInventorySlotContents(0, ItemProgrammingCircuit.wrap(orb));
        hatch.program();
    }

    private static boolean same(ItemStack a, ItemStack b) {
        return a != null && ItemStack.areItemStackTagsEqual(a, b);
    }

    private static TileOrbSwitcher placeSwitcher(GameTestHelper h) {
        TestPos pos = h.absolute(SWITCHER);
        h.getWorld().setBlock(pos.x(), pos.y(), pos.z(), MyMod.orbswitcher, 0, 3);
        TileOrbSwitcher tile = (TileOrbSwitcher) h.getWorld().getTileEntity(pos.x(), pos.y(), pos.z());
        tile.facing = ForgeDirection.SOUTH;
        set(tile, "dirorb", ForgeDirection.EAST);
        set(tile, "dirstocking", ForgeDirection.WEST);
        set(tile, "nbt", true);
        return tile;
    }

    private static void buildTargetNetwork(GameTestHelper h) {
        Block cableBus = AEApi.instance().definitions().blocks().multiPart().maybeBlock().get();
        TestPos bus = h.absolute(TARGET_BUS), controller = h.absolute(TARGET_CONTROLLER);
        h.getWorld().setBlock(bus.x(), bus.y(), bus.z(), cableBus, 0, 3);
        IPartHost host = h.assertInstanceOf(
            IPartHost.class,
            h.getWorld().getTileEntity(bus.x(), bus.y(), bus.z()),
            "The target network should host a storage bus");
        h.assertEquals(
            ForgeDirection.UNKNOWN,
            host.addPart(AEApi.instance().definitions().parts().cableGlass().stack(AEColor.Transparent, 1), ForgeDirection.UNKNOWN, null),
            "The cable should be placed");
        h.assertEquals(
            ForgeDirection.EAST,
            host.addPart(AEApi.instance().definitions().parts().storageBus().maybeStack(1).get(), ForgeDirection.EAST, null),
            "The storage bus should face the orb hatch");
        h.getWorld().setBlock(
            controller.x(),
            controller.y(),
            controller.z(),
            Block.getBlockFromName("appliedenergistics2:tile.BlockCreativeEnergyController"),
            1,
            3);
    }

    @SuppressWarnings("unchecked")
    private static ItemStack cell(GameTestHelper h, ItemStack... orbs) {
        ItemStack cell = AEApi.instance().definitions().items().cell64k().maybeStack(1).get();
        IMEInventoryHandler<IAEItemStack> inv = AEApi.instance().registries().cell().getHandler(cell)
            .getCellInventory(cell, null, ITEM_STACK_TYPE);
        for (ItemStack o : orbs) h.assertNull(inv.injectItems(ae(o, 1), Actionable.MODULATE, SOURCE), "The cell should take the orb");
        return cell;
    }

    private static long stored(GameTestHelper h, ItemStack orb) {
        TileEntity te = h.assertTileEntityPresent("controller");
        IGridNode node = h.assertInstanceOf(IGridHost.class, te).getGridNode(ForgeDirection.UNKNOWN);
        h.assertNotNull(node, "The stock network should have a node");
        IStorageGrid storage = node.getGrid().getCache(IStorageGrid.class);
        IAEItemStack s = storage.getItemInventory().extractItems(ae(orb, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE);
        return s == null ? 0 : s.getStackSize();
    }
}
