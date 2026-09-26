package reobf.proghatches.ae;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.factory.SidedTileEntityGuiFactory;

import appeng.api.parts.IPartHost;

// MUI2 counterpart of EUUtil.PART_MODULAR_UI: opens the gui of any AE part that is an IGuiHolder
public class PartGuiFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final PartGuiFactory INSTANCE = new PartGuiFactory();

    private PartGuiFactory() {
        super("proghatches:part");
    }

    public void open(EntityPlayer player, TileEntity t, ForgeDirection side) {
        if (player.worldObj.isRemote || player instanceof FakePlayer) {
            return;
        }
        GuiManager.open(this, new SidedPosGuiData(player, t.xCoord, t.yCoord, t.zCoord, side), (EntityPlayerMP) player);
    }

    @Override
    public IGuiHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        TileEntity t = data.getTileEntity();
        if (t instanceof IPartHost) {
            return castGuiHolder(((IPartHost) t).getPart(data.getSide()));
        }
        return null;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, SidedPosGuiData data) {
        return player == data.getPlayer() && getGuiHolder(data) != null && data.getSquaredDistance(player) <= 64;
    }

    @Override
    public void writeGuiData(SidedPosGuiData data, PacketBuffer buffer) {
        SidedTileEntityGuiFactory.INSTANCE.writeGuiData(data, buffer);
    }

    @Override
    public SidedPosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return SidedTileEntityGuiFactory.INSTANCE.readGuiData(player, buffer);
    }
}
