package reobf.proghatches.gametests;

import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;
import static com.gtnewhorizons.horizonqa.api.TestPos.at;
import static reobf.proghatches.gametests.ChancePatternTests.ae;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import reobf.proghatches.ae.PartAmountMaintainer;
import reobf.proghatches.main.MyMod;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"guidenh"})
public class StockMaintainerGuideTests {

    private static final TestPos CABLE = at(1, 1, 1), CHEST = at(2, 1, 1);
    private static final BaseActionSource SOURCE = new BaseActionSource();

    @GameTest(template = "stock_control", timeoutTicks = 600)
    public static void restoresTheTargetAndReturnsExcessWithoutTouchingOtherItems(GameTestHelper h) {
        TileEntityChest chest = h.assertTileEntityPresent(TileEntityChest.class, "chest");
        chest.setInventorySlotContents(8, new ItemStack(Items.gold_ingot, 3));
        TestPos a = h.absolute(CABLE);
        h.getWorld().setBlock(a.x(), a.y(), a.z(), AEApi.instance().definitions().blocks().multiPart().maybeBlock().get(), 0, 3);
        IPartHost host = (IPartHost) h.getWorld().getTileEntity(a.x(), a.y(), a.z());
        host.addPart(AEApi.instance().definitions().parts().cableGlass().stack(AEColor.Transparent, 1), ForgeDirection.UNKNOWN, null);
        host.addPart(new ItemStack(MyMod.amountmaintainer), ForgeDirection.EAST, null);
        PartAmountMaintainer maintainer = (PartAmountMaintainer) host.getPart(ForgeDirection.EAST);
        configure(maintainer, 16);
        ItemStack cell = AEApi.instance().definitions().items().cell64k().maybeStack(1).get();
        @SuppressWarnings("unchecked")
        IMEInventoryHandler<IAEItemStack> inventory = AEApi.instance().registries().cell().getHandler(cell).getCellInventory(cell, null, ITEM_STACK_TYPE);
        h.assertNull(inventory.injectItems(ae(new ItemStack(Items.iron_ingot), 128), Actionable.MODULATE, SOURCE), "The cell should take 128 iron");
        h.setSlot("drive", 0, cell);
        GuideSceneRecorder scene = new GuideSceneRecorder(h, "stock_control", "proghatches.guide.stock", 4, 3, 3);
        h.startSequence()
            .thenWaitUntil("fill the initial target", 150, () -> expect(h, chest, 16))
            .thenExecute("record the setup and change the target to 64", () -> {
                scene.capture(); scene.frame(0, "overview", true);
                scene.frame(120, "mark", true); scene.frame(180, "target", false);
                configure(maintainer, 64);
                scene.patch(scene.frame(260, "configured", false), CABLE);
            })
            .thenWaitUntil("fill to 64", 100, () -> expect(h, chest, 64))
            .thenExecute("take 16 iron from the chest", () -> {
                scene.patch(scene.frame(360, "filled", true), CHEST);
                h.assertEquals(16, chest.decrStackSize(0, 16).stackSize, "Take exactly 16 iron");
                scene.patch(scene.frame(440, "used", true), CHEST);
            })
            .thenWaitUntil("restore the target after extraction", 100, () -> expect(h, chest, 64))
            .thenExecute("put an extra 16 iron into the chest", () -> {
                scene.patch(scene.frame(540, "restored", true), CHEST);
                chest.setInventorySlotContents(1, new ItemStack(Items.iron_ingot, 16));
                scene.patch(scene.frame(620, "excess", true), CHEST);
            })
            .thenWaitUntil("return excess iron", 100, () -> {
                expect(h, chest, 64);
                try {
                    var stock = maintainer.getProxy().getStorage().getItemInventory().extractItems(
                        ae(new ItemStack(Items.iron_ingot), Long.MAX_VALUE), Actionable.SIMULATE, SOURCE);
                    h.assertNotNull(stock, "Remaining iron should be in ME");
                    h.assertEquals(64L, stock.getStackSize(), "ME should hold exactly 64 remaining iron");
                } catch (Exception e) { throw new AssertionError(e); }
            })
            .thenExecute("save the maintenance recording", () -> {
                scene.patch(scene.frame(720, "returned", true), CHEST);
                scene.frame(820, "other", true); scene.frame(920, "done", true); scene.save(1000);
            })
            .thenSucceed();
    }

    private static void configure(PartAmountMaintainer part, long amount) {
        NBTTagCompound settings = new NBTTagCompound();
        part.getConfigManager().writeToNBT(settings);
        settings.setTag("mark", new ItemStack(Items.iron_ingot).writeToNBT(new NBTTagCompound()));
        settings.setLong("amount", amount); settings.setInteger("interval", 1);
        settings.setInteger("mode", 0); settings.setInteger("rsmode", 0);
        part.getConfigManager().readFromNBT(settings);
    }

    private static void expect(GameTestHelper h, TileEntityChest chest, int expected) {
        int iron = 0, gold = 0;
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            ItemStack s = chest.getStackInSlot(i);
            if (s == null) continue;
            if (s.getItem() == Items.iron_ingot) iron += s.stackSize;
            if (s.getItem() == Items.gold_ingot) gold += s.stackSize;
        }
        h.assertEquals(expected, iron, "The chest should contain the target iron amount");
        h.assertEquals(3, gold, "Unmarked gold must remain untouched");
    }
}
