package reobf.proghatches.ae;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;

import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.PartBasicState;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.eucrafting.EUUtil;
import reobf.proghatches.eucrafting.IGuiProvidingPart;

public class PartStorageProxy extends PartBasicState implements

    IGridProxyable, IActionHost, IStorageMonitorable, ITileStorageMonitorable, IGuiProvidingPart {

    TileStorageProxy internal;// =new TileStorageProxy();

    int damage;

    public PartStorageProxy(ItemStack is) {
        super(is);
        damage = is.getItemDamage();
        internal = new TileStorageProxy();
        if (damage == 1) internal.fluid = true;
        if (damage == 2) internal.noAdvConfig = true;
        internal.gridProxy = this.getProxy();
        internal.isProxyExternal = true;

    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        internal.writeToNBT(data);
        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        internal.readFromNBT(data);
        super.readFromNBT(data);
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {

        return internal.createWindow(buildContext);
    }

    @Override
    public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src) {

        return internal.getMonitorable(side, src);
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {

        return internal.getItemInventory();
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {

        return internal.getFluidInventory();
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorSides.getIcon());

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        rh.setBounds(5, 5, 12, 11, 11, 13);
        rh.renderInventoryBox(renderer);

        rh.setBounds(5, 5, 13, 11, 11, 14);
        rh.renderInventoryBox(renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
        final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
        rh.setTexture(
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorSides.getIcon());

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartMonitorSides.getIcon(),
            CableBusTextures.PartMonitorSides.getIcon());

        rh.setBounds(5, 5, 12, 11, 11, 13);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorSidesStatus.getIcon());

        rh.setBounds(5, 5, 13, 11, 11, 14);
        rh.renderBlock(x, y, z, renderer);

        // this.renderLights(x, y, z, rh, renderer);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPartActivate(EntityPlayer player, Vec3 pos) {

        if (player.isSneaking()) return false;
        TileEntity t = this.getTile();

        EUUtil.open(player, player.getEntityWorld(), t.xCoord, t.yCoord, t.zCoord, getSide());

        return true;
    }

    @Override
    public void gridChanged() {
        internal.gridChanged();
        super.gridChanged();
    }
}
