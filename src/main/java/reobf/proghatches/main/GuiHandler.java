package reobf.proghatches.main;

import java.lang.reflect.Field;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.client.gui.GuiDualInterface;
import com.glodblock.github.client.gui.container.ContainerDualInterface;

import appeng.api.AEApi;
import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.implementations.GuiPriority;
import appeng.client.gui.implementations.GuiRenamer;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.ITooltip;
import appeng.container.implementations.ContainerPriority;
import appeng.container.implementations.ContainerRenamer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotNormal;
import appeng.core.localization.GuiText;
import appeng.helpers.DualityInterface;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.parts.AEBasePart;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.common.covers.CoverInfo;
import reobf.proghatches.eucrafting.InterfaceData;
import reobf.proghatches.net.PriorityMessage;
import reobf.proghatches.net.RenameMessage;

public class GuiHandler implements IDefaultGuiHandler {

    public IInterfaceHost getHost(int ID, World world, int x, int y, int z) {
        try {
            CoverInfo info = ((ICoverable) world.getTileEntity(x, y, z))
                .getCoverInfoAtSide(ForgeDirection.getOrientation(ID & 0b111));

            return new FakeHost(world.getTileEntity(x, y, z), (IInterfaceHost) info.getCoverData());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean getTileOf(IInventory inventory) {
        if (inventory instanceof AppEngInternalInventory) try {
            Field f = AppEngInternalInventory.class.getDeclaredField("te");
            f.setAccessible(true);
            // System.out.println(((DualityInterface)f.get(inventory)).getHost());
            try {
                IUpgradeableHost host = ((DualityInterface) f.get(inventory)).getHost();// instanceof
                                                                                        // InterfaceData.DisabledInventory;

                if (host instanceof AEBasePart) {
                    return ((AEBasePart) host).getHost() instanceof InterfaceData.DisabledInventory;

                }
            } catch (ClassCastException e) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (inventory instanceof AppEngInternalAEInventory) try {
            Field f = AppEngInternalAEInventory.class.getDeclaredField("te");
            f.setAccessible(true);
            // System.out.println(((DualityInterface)f.get(inventory)).getHost());
            try {
                IUpgradeableHost host = ((DualityInterface) f.get(inventory)).getHost();// instanceof
                                                                                        // InterfaceData.DisabledInventory;
                if (host instanceof AEBasePart) {
                    return ((AEBasePart) host).getHost() instanceof InterfaceData.DisabledInventory;

                }
            } catch (ClassCastException e) {}
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean shouldRemove(Slot s) {
        AppEngSlot ss = (AppEngSlot) s;

        if (ss.inventory instanceof InterfaceData.DisabledInventory || ((getTileOf(ss.inventory))))

        {
            return true;
        }

        return false;
    }

    private Slot map(Slot s) {
        if (s instanceof SlotNormal || s instanceof SlotFake) {
            AppEngSlot ss = (AppEngSlot) s;

            if (ss.inventory instanceof InterfaceData.DisabledInventory || ((getTileOf(ss.inventory))))

            {
                return new SlotDisabled(ss.inventory, ss.getSlotIndex(), ss.getX(), ss.getY());
            }
        }

        return s;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        IInterfaceHost host = getHost(ID, world, x, y, z);
        if (host == null) return null;
        if ((ID & 0b1000) > 0) {
            if (host instanceof IPriorityHost) return new ContainerPriority(player.inventory, (IPriorityHost) host);
            return null;
        }
        if ((ID & 0b10000) > 0) {
            if (host instanceof ICustomNameObject)
                return new ContainerRenamer(player.inventory, (ICustomNameObject) host);
            return null;
        }

        return new ContainerDualInterface(player.inventory, host) {

            @Override
            protected Slot addSlotToContainer(Slot newSlot) {

                if (shouldRemove(newSlot)) {
                    return newSlot;
                }
                return super.addSlotToContainer(newSlot);
                // return super.addSlotToContainer(map(newSlot));
            }

        };
    }

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        IInterfaceHost host = getHost(ID, world, x, y, z);
        if (host == null) return null;
        if ((ID & 0b1000) > 0) {
            if (host instanceof IPriorityHost) return new GuiPriority(player.inventory, (IPriorityHost) host);
            return null;
        }
        if ((ID & 0b10000) > 0) {
            if (host instanceof ICustomNameObject) return new GuiRenamer(player.inventory, (ICustomNameObject) host);
            return null;
        }
        return new GuiDualInterface(player.inventory, host) {
            /*
             * @Override
             * protected String getGuiDisplayName(String in) {
             * return super.getGuiDisplayName("EU Interface");
             * }
             */

            GuiTabButton rename;

            @Override
            public void initGui() {
                super.initGui();
                this.buttonList.removeIf(
                    s -> {
                        return ((ITooltip) s).getMessage()
                            .equals(StatCollector.translateToLocal("ae2fc.tooltip.switch_fluid_interface"));
                    });
                // this.inventorySlots.inventorySlots.replaceAll(s ->map((Slot)
                // s));
                this.inventorySlots.inventorySlots.removeIf(s -> shouldRemove((Slot) s));

                rename = new GuiTabButton(
                    this.guiLeft + 132,
                    this.guiTop,
                    AEApi.instance()
                        .items().itemCertusQuartzKnife.stack(1),
                    "Rename" // StatCollector.translateToLocal("ae2fc.tooltip.switch_fluid_interface")
                ,
                    itemRender) {

                }

                ;
                this.buttonList.add(rename);

            }

            @Override
            protected void actionPerformed(GuiButton btn) {

                if (btn == rename) {

                    MyMod.net.sendToServer(new RenameMessage(x, y, z, ForgeDirection.getOrientation(ID & 0b111)));
                    return;
                }

                if (((ITooltip) btn).getMessage()
                    .equals(GuiText.Priority.getLocal())) {
                    MyMod.net.sendToServer(new PriorityMessage(x, y, z, ForgeDirection.getOrientation(ID & 0b111)));

                    return;

                }
                super.actionPerformed(btn);
            }

        };
    }
}
