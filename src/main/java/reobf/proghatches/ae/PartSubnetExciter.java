package reobf.proghatches.ae;

import java.io.IOException;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.loader.ChannelLoader;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkControllerChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.client.render.effects.ChargedOreFX;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.PartBasicState;
import appeng.parts.networking.PartQuartzFiber;
import appeng.tile.misc.TileCharger;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public class PartSubnetExciter extends PartBasicState implements IAEPowerStorage

{

    public PartSubnetExciter(ItemStack is) {
        super(is);

    }

    @Override
    public AECableType getCableConnectionType(final ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public double injectAEPower(final double amt, final Actionable mode) {
        return 0;
    }

    @Override
    public double getAEMaxPower() {
        return 0;// ok?Long.MAX_VALUE / 10000:0;
    }

    @Override
    public double getAECurrentPower() {
        return ok ? Long.MAX_VALUE / 10000 : 0;
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    @Override
    public double extractAEPower(final double amt, final Actionable mode, final PowerMultiplier pm) {
        return ok ? amt : 0;
    }

    boolean ok;

    @MENetworkEventSubscribe
    public void change(MENetworkChannelChanged w) {
        gridChanged();
    }

    @MENetworkEventSubscribe
    public void change(MENetworkChannelsChanged w) {
        gridChanged();
    }

    @MENetworkEventSubscribe
    public void change(MENetworkControllerChange w) {
        gridChanged();
    }

    @Override
    public void gridChanged() {

        check(null);
    }

    public void check(@Nullable IGridNode exept/* this node is not checked */) {

        boolean prevok = ok;
        super.gridChanged();
        try {
            ok = true;
            for (IGridNode n : this.getProxy()
                .getGrid()
                .getNodes()) {
                if (n == exept) continue;
                if (n == this.getGridNode()) continue;
                IGridHost mach = n.getMachine();
                fibchek: if (mach instanceof PartQuartzFiber) {
                    ok = false;
                    /*
                     * PartQuartzFiber fib = (PartQuartzFiber)mach;
                     * IGrid a = fib.getExternalFacingNode().getGrid();
                     * IGrid b = fib.getGridNode().getGrid();
                     * IGrid thiz=getGridNode().getGrid();
                     * if(a==null||b==null)break fibchek;//?
                     * if(a==b)break fibchek;//same net, pass
                     * if((a==thiz&&b.getNodes().size()>1)||(b==thiz&&a.getNodes().size()>1)){
                     * //size>1 means the network has node(s) other than the fiber
                     * ok=false;//un oh
                     * }
                     */

                }

                if (mach instanceof TileCharger) {
                    ok = false;
                }
                if (mach instanceof IAEPowerStorage) {
                    IAEPowerStorage pw = (IAEPowerStorage) mach;
                    if (pw.isAEPublicPowerStorage()) {
                        ok = false;

                    }
                }

            }

        } catch (GridAccessException e) {
            ok = false;

        }
        // if(ok==false)this
        if (prevok != ok) {
            try {

                this.getProxy()
                    .getGrid()
                    .postEvent(new MENetworkPowerStatusChange());
                this.getProxy()
                    .getGrid()
                    .postEvent(new MENetworkPowerStorage(this, PowerEventType.PROVIDE_POWER));
                World w = getHost().getTile()
                    .getWorldObj();
                if (!ok) w.playSoundEffect(
                    (double) (getHost().getTile().xCoord + 0.5F),
                    (double) (getHost().getTile().yCoord + 0.5F),
                    (double) (getHost().getTile().zCoord + 0.5F),
                    "random.fizz",
                    0.5F,
                    2.6F + (w.rand.nextFloat() - w.rand.nextFloat()) * 0.8F);
                // if(ok)w .playSoundEffect((double)(getHost().getTile().xCoord + 0.5F),
                // (double)(getHost().getTile().yCoord + 0.5F), (double)(getHost().getTile().zCoord + 0.5F),
                // "random.orb", 0.5F, 2.6F + (w.rand.nextFloat() - w.rand.nextFloat()) * 0.8F);

                ChannelLoader.sendPacketToAllPlayers(
                    this.host.getTile()
                        .getDescriptionPacket(),
                    this.host.getTile()
                        .getWorldObj());

            } catch (GridAccessException e) {}
        }

    }

    int a = 2 + 3;
    int b = 14 - 3;

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

        rh.setBounds(a, a, 14, b, b, 16);
        rh.renderInventoryBox(renderer);

        rh.setBounds(5, 5, 12, 11, 11, 13);
        rh.renderInventoryBox(renderer);

        rh.setBounds(5, 5, 13, 11, 11, 14);
        rh.renderInventoryBox(renderer);
    }

    @Override
    public boolean requireDynamicRender() {

        return true;
    }

    private Random ran = new Random();

    boolean prevok;

    @SideOnly(Side.CLIENT)
    @Override
    public void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer) {
        super.renderDynamic(x, y, z, rh, renderer);
        if (prevok == true && ok == false) {
            if (Minecraft.getMinecraft()
                .isGamePaused()) {
                return;
            }
            for (int i = 0; i < 16; i++) Minecraft.getMinecraft().theWorld.spawnParticle(
                "smoke",
                this.getHost()
                    .getTile().xCoord + 0.5
                    + this.getSide().offsetX * 0.5
                    + ran.nextDouble() * 0.3
                    - ran.nextDouble() * 0.3,
                this.getHost()
                    .getTile().yCoord + 0.5
                    + this.getSide().offsetY * 0.5
                    + ran.nextDouble() * 0.3
                    - ran.nextDouble() * 0.3,
                this.getHost()
                    .getTile().zCoord + 0.5
                    + this.getSide().offsetZ * 0.5
                    + ran.nextDouble() * 0.3
                    - ran.nextDouble() * 0.3,
                0.0f,
                0.0f,
                0.0f);

        }

        if (prevok == false && ok == true) {

            for (int i = 0; i < 16; i++) Minecraft.getMinecraft().theWorld.spawnParticle(
                "reddust",
                this.getHost()
                    .getTile().xCoord + 0.5
                    + this.getSide().offsetX * 0.5
                    + ran.nextDouble() * 0.3
                    - ran.nextDouble() * 0.3,
                this.getHost()
                    .getTile().yCoord + 0.5
                    + this.getSide().offsetY * 0.5
                    + ran.nextDouble() * 0.3
                    - ran.nextDouble() * 0.3,
                this.getHost()
                    .getTile().zCoord + 0.5
                    + this.getSide().offsetZ * 0.5
                    + ran.nextDouble() * 0.3
                    - ran.nextDouble() * 0.3,
                0.0f,
                0.0f,
                0.0f);

        }

        prevok = ok;
        final ChargedOreFX fx = new ChargedOreFX(
            Minecraft.getMinecraft().theWorld,

            this.getHost()
                .getTile().xCoord + 0.5
                + this.getSide().offsetX * 0.5
                + ran.nextDouble() * 0.2
                - ran.nextDouble() * 0.2,
            this.getHost()
                .getTile().yCoord + 0.5
                + this.getSide().offsetY * 0.5
                + ran.nextDouble() * 0.2
                - ran.nextDouble() * 0.2,
            this.getHost()
                .getTile().zCoord + 0.5
                + this.getSide().offsetZ * 0.5
                + ran.nextDouble() * 0.2
                - ran.nextDouble() * 0.2

            ,
            0.0f,
            0.0f,
            0.0f);
        if (ok && Minecraft.getMinecraft().thePlayer.ticksExisted % 3 == 2)
            Minecraft.getMinecraft().effectRenderer.addEffect(fx);
    }

    @Override
    public void writeToStream(ByteBuf data) throws IOException {
        data.writeBoolean(ok);
        super.writeToStream(data);
    }

    @Override
    public boolean readFromStream(ByteBuf data) throws IOException {
        ok = data.readBoolean();
        return super.readFromStream(data);
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

        rh.setBounds(a, a, 14, b, b, 16);
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

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(a, a, 14, b, b, 16);
        // bch.addBox(5, 5, 12, 11, 11, 14);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

}
