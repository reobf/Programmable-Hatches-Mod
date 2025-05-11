package reobf.proghatches.ae.part2;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridConnection;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.PartBasicState;
import appeng.parts.p2p.IPartGT5Power;
import appeng.tile.networking.TileCableBus;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.graphs.GenerateNodeMap;
import gregtech.api.graphs.GenerateNodeMapPower;
import gregtech.api.graphs.Node;
import gregtech.api.graphs.consumers.ConsumerNode;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.implementations.MTECable;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.fmp.IUpdatable;

public class PartCoW extends PartBasicState implements IGridTickable, IPartGT5Power, IPowerChannelState, IUpdatable {

    public PartCoW(ItemStack is) {
        super(is);
        this.proxy.setFlags(GridFlags.DENSE_CAPACITY);// no channel
        setInWorld(this.proxy);
    }

    Field f;

    public void setInWorld(AENetworkProxy w) {

        try {

            if (f == null) {
                f = w.getClass()
                    .getDeclaredField("worldNode");
                f.setAccessible(true);
                Field modifiers = f.getClass()
                    .getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            }

            f.set(w, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AENetworkProxy getProxy() {
        // TODO Auto-generated method stub
        return super.getProxy();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {

        return new TickingRequest(1, 1, false, false);
    }

    public MTECable get() {
        TileEntity te = this.host.getTile()
            .getWorldObj()
            .getTileEntity(
                this.host.getLocation().x + this.side.offsetX,
                this.host.getLocation().y + this.side.offsetY,
                this.host.getLocation().z + this.side.offsetZ);
        if (te instanceof IGregTechTileEntity) {
            IGregTechTileEntity gte = (IGregTechTileEntity) te;
            IMetaTileEntity mte = gte.getMetaTileEntity();
            if (mte instanceof MTECable) {
                MTECable c = (MTECable) mte;
                // final BaseMetaPipeEntity tBase = (BaseMetaPipeEntity)
                // c.getBaseMetaTileEntity();
                return c;
            }

        }
        return null;

    }

    int count;

    @Override
    public void onNeighborChanged() {
        MTECable c = get();
        if (c != null) {
            if (!c.isConnectedAtSide(this.side.getOpposite())) {
                return;
            }
            // cannot wait for 20tick to setup! we need it built instantly
            final int time = MinecraftServer.getServer()
                .getTickCounter();
            BaseMetaPipeEntity pipe = (((BaseMetaPipeEntity) c.getBaseMetaTileEntity()));
            final Node node = pipe.getNode();
            if (node == null) {
                new GenerateNodeMapPower(pipe);
            } else if (node.mCreationTime != time) {
                GenerateNodeMap.clearNodeMap(node, -1);
                new GenerateNodeMapPower(pipe);
            }
        }
    }

    long channel;
    CoWGridConnection connection;

    public class CoWGridConnection extends GridConnection {

        public CoWGridConnection(IGridNode aNode, IGridNode bNode, ForgeDirection fromAtoB) throws FailedConnection {
            super(aNode, bNode, fromAtoB);
        }

        public boolean destroyed;

        @Override
        public void destroy() {
            destroyed = true;
            super.destroy();
        }

    }

    @Override
    public TickRateModulation tickingRequest(IGridNode nodexx, int TicksSinceLastCallxx) {
        count++;
        boolean thisTick;
        synchronized (this) {
            thisTick = (update & 1) != 0;
            update = update >> 1;
        }
        if (thisTick) {
            onNeighborChanged();

        }

        if (count < 3) {
            onNeighborChanged();
        }
        if (count % 20 != 5) return TickRateModulation.SAME;
        updateConn();

        return TickRateModulation.SAME;
    }

    public void updateConn() {
        MTECable c = get();
        if (c != null && c.isConnectedAtSide(
            this.getSide()
                .getOpposite())) {
            BaseMetaPipeEntity pipe = (((BaseMetaPipeEntity) c.getBaseMetaTileEntity()));
            ArrayList<PartCoW> list = new ArrayList<>();
            final Node node = pipe.getNode();
            if (node != null) for (ConsumerNode n : node.mConsumers) {

                TileEntity te = (n.mTileEntity);
                if (te instanceof TileCableBus) {
                    TileCableBus cable = (TileCableBus) te;
                    IPart pt = cable.getPart(n.mSide);
                    if (pt instanceof PartCoW && pt != this) {
                        if (((PartCoW) pt).channel == this.channel && channel != 0) list.add((PartCoW) pt);
                    }
                    // System.out.println(pt);
                }
            }

            PartCoW other = null;
            if (connection != null) {
                if (connection.destroyed) {
                    connection = null;
                }

            }

            if (connection != null) {
                other = (PartCoW) connection.getOtherSide(this.getGridNode())
                    .getMachine();
                boolean shouldDestroy = false;
                if (list.size() > 1) {
                    shouldDestroy = true;
                } else if (list.size() == 0) {
                    shouldDestroy = true;
                } else if (channel == 0) shouldDestroy = true;
                else if (other != null && other.channel == 0) shouldDestroy = true;
                else if (other != null && other.channel != channel) shouldDestroy = true;

                if (shouldDestroy) {
                    connection.destroy();
                    connection = null;
                    other.connection = null;
                }

            }
            if (list.size() > 1) {
                securityBreak();
            }
            if (list.size() == 1 && connection == null) {

                try {
                    connection = new CoWGridConnection(
                        this.getGridNode(),
                        list.get(0)
                            .getGridNode(),
                        ForgeDirection.UNKNOWN);
                    list.get(0).connection = connection;

                } catch (FailedConnection e) {

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }

            /*
             * if(list.size()==0){ if(connection!=null){ PartCoW other=(PartCoW)
             * connection.getOtherSide(this.getGridNode()).getMachine();
             * other.connection=null; connection=null; connection.destroy(); } }
             */

        } else {

            if (connection != null) {
                PartCoW other = (PartCoW) connection.getOtherSide(this.getGridNode())
                    .getMachine();
                connection.destroy();
                connection = null;
                other.connection = null;

            }
        }

    }

    @Override
    public boolean canBePlacedOn(BusSupport what) {

        return what != BusSupport.NO_PARTS;
    }

    @Override
    public long injectEnergyUnits(long voltage, long amperage) {

        return 0;
    }

    @Override
    public boolean inputEnergy() {

        return true;
    }

    @Override
    public boolean outputsEnergy() {

        return false;
    }

    public boolean useStandardMemoryCard() {
        return false;
    }

    @Override
    public boolean onPartShiftActivate(final EntityPlayer player, final Vec3 pos) {
        final ItemStack is = player.inventory.getCurrentItem();
        if (is != null && is.getItem() instanceof IMemoryCard) {
            IMemoryCard mc = (IMemoryCard) is.getItem();
            if (ForgeEventFactory.onItemUseStart(player, is, 1) <= 0) return false;

            if (channel == 0) {
                channel = System.currentTimeMillis();
            }

            final NBTTagCompound data = this.getMemoryCardData();
            final ItemStack p2pItem = this.getItemStack(PartItemStack.Wrench);
            final String type = p2pItem.getUnlocalizedName();

            p2pItem.writeToNBT(data);

            mc.setMemoryCardContents(is, type + ".name", data);
            mc.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {
        final ItemStack is = player.inventory.getCurrentItem();

        // UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(
        // is.getItem() );
        // AELog.info( "ID:" + id.toString() + " : " + is.getItemDamage() );

        // final TunnelType tt =
        // AEApi.instance().registries().p2pTunnel().getTunnelTypeByItem(is);
        if (is != null && is.getItem() instanceof IMemoryCard) {
            IMemoryCard mc = (IMemoryCard) is.getItem();
            if (ForgeEventFactory.onItemUseStart(player, is, 1) <= 0) return false;

            final NBTTagCompound data = mc.getData(is);

            final ItemStack newType = ItemStack.loadItemStackFromNBT(data);
            final long freq = data.getLong("freq");

            if (newType != null) {
                if (newType.getItem() instanceof IPartItem) {
                    final IPart testPart = ((IPartItem) newType.getItem()).createPartFromItemStack(newType);
                    if (testPart instanceof PartCoW) {
                        this.getHost()
                            .removePart(this.getSide(), true);
                        final ForgeDirection dir = this.getHost()
                            .addPart(newType, this.getSide(), player);
                        final IPart newBus = this.getHost()
                            .getPart(dir);

                        if (newBus instanceof PartCoW) {
                            PartCoW newTunnel = (PartCoW) newBus;

                            newTunnel.channel = freq;
                            updateConn();
                        }

                        mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                        return true;
                    }
                }
            }
            mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
        }

        return false;
    }

    public NBTTagCompound getMemoryCardData() {
        final NBTTagCompound output = new NBTTagCompound();

        if (this.hasCustomName()) {
            final NBTTagCompound dsp = new NBTTagCompound();
            dsp.setString("Name", this.getCustomName());
            output.setTag("display", dsp);
        }
        output.setLong("freq", channel);

        return output;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(5, 5, 12, 11, 11, 13);
        bch.addBox(3, 3, 13, 13, 13, 14);
        bch.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(this.getTypeTexture());

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        rh.setTexture(
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.BlockP2PTunnel2.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.PartTunnelSides.getIcon());

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);
    }

    /**
     * @return If enabled it returns the icon of an AE quartz block, else
     *         vanilla quartz block icon
     */
    public IIcon getTypeTexture() {
        final Optional<Block> maybeBlock = AEApi.instance()
            .definitions()
            .blocks()
            .quartz()
            .maybeBlock();
        if (maybeBlock.isPresent()) {
            return maybeBlock.get()
                .getIcon(0, 0);
        } else {
            return Blocks.quartz_block.getIcon(0, 0);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
        final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
        rh.setTexture(this.getTypeTexture());

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.BlockP2PTunnel2.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.PartTunnelSides.getIcon());

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(3, 3, 13, 13, 13, 14);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(CableBusTextures.BlockP2PTunnel3.getIcon());

        rh.setBounds(6, 5, 12, 10, 11, 13);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(5, 6, 12, 11, 10, 13);
        rh.renderBlock(x, y, z, renderer);

        this.renderLights(x, y, z, rh, renderer);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        data.setLong("channel", channel);
        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        channel = data.getLong("channel");
        super.readFromNBT(data);
    }

    public static class WailaDataProvider extends appeng.integration.modules.waila.part.BasePartWailaDataProvider {

        @Override
        public List<String> getWailaBody(IPart part, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
            if (!(part instanceof PartCoW)) {
                return currenttip;
            }
            try {

                final String freqTooltip = String.format(
                    "%X",
                    accessor.getNBTData()
                        .getLong("channel"))
                    .replaceAll("(.{4})", "$0 ")
                    .trim();

                currenttip.add("frequency:" + freqTooltip);
                return currenttip;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return currenttip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, IPart part, TileEntity te, NBTTagCompound data,
            World world, int x, int y, int z) {

            if (!(part instanceof PartCoW)) {
                return data;
            }
            PartCoW thiz = (PartCoW) part;
            data.setLong("channel", thiz.channel);
            return data;
        }

    }

    int update;

    public void update() {

        synchronized (this) {
            update = update | 0x1010;// update after 4 ticks and 12 ticks
        }
    }

}
