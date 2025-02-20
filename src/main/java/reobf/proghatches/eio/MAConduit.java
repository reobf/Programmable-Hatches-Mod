package reobf.proghatches.eio;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.HashMultimap;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartHost;
import appeng.helpers.IInterfaceHost;
import crazypants.enderio.conduit.AbstractConduit;
import crazypants.enderio.conduit.AbstractConduitNetwork;
import crazypants.enderio.conduit.ConduitUtil;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.RaytraceResult;
import crazypants.enderio.conduit.geom.CollidableComponent;
import crazypants.enderio.conduit.geom.ConnectionModeGeometry;
import crazypants.enderio.conduit.geom.Offset;
import crazypants.enderio.conduit.item.ItemConduit;
import crazypants.enderio.conduit.render.ConduitBundleRenderer;
import crazypants.enderio.conduit.render.ConduitRenderer;
import crazypants.enderio.conduit.render.DefaultConduitRenderer;
import crazypants.enderio.tool.ToolUtil;
import reobf.proghatches.ae.PartMAP2P;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.fmp.LayerCraftingMachine.StateHolder;
import reobf.proghatches.main.MyMod;

public class MAConduit extends AbstractConduit implements ICraftingMachineConduit {

    public static class MAConduitNetwork
        extends AbstractConduitNetwork<ICraftingMachineConduit, ICraftingMachineConduit>

    {

        public MAConduitNetwork(Class<ICraftingMachineConduit> implClass,
            Class<ICraftingMachineConduit> baseConduitClass) {
            super(implClass, baseConduitClass);
        }

        @Override
        public void doNetworkTick() {
            super.doNetworkTick();

        }

        @Override
        public void addConduit(ICraftingMachineConduit con) {
            super.addConduit(con);
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
                if (((MAConduit) con).containsExternalConnection(dir)
                    && ((MAConduit) con).getConnectionMode(dir) != ConnectionMode.DISABLED)
                    conn.put((MAConduit) con, dir);
        }

        public void updateConn(MAConduit con) {
            conn.removeAll(con);
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
                if (((MAConduit) con).containsExternalConnection(dir)
                    && ((MAConduit) con).getConnectionMode(dir) != ConnectionMode.DISABLED)
                    conn.put((MAConduit) con, dir);

        }

        HashMultimap<MAConduit, ForgeDirection> conn = HashMultimap.create();

    }

    public void setConnectionMode(ForgeDirection dir, ConnectionMode mode) {

        super.setConnectionMode(dir, mode);

        if (net != null) net.updateConn(this);
        else updateLater = true;
    };

    boolean updateLater;

    MAConduitNetwork net;

    @Override
    public Class<? extends IConduit> getBaseConduitType() {

        return ICraftingMachineConduit.class;
    }

    @Override
    public ItemStack createItem() {

        return new ItemStack(MyMod.ma_conduit);
    }

    @Override
    public AbstractConduitNetwork<?, ?> getNetwork() {

        return net;
    }

    @Override
    public boolean setNetwork(AbstractConduitNetwork<?, ?> network) {

        this.net = (MAConduitNetwork) network;
        if (updateLater && net != null) {
            net.updateConn(this);
            updateLater = false;
        }
        return true;
    }

    @Override
    public IIcon getTextureForState(CollidableComponent component) {

        return MyMod.iohub.getIcon(0, BlockIOHub.magicNO_ma);
    }

    @Override
    public IIcon getTransmitionTextureForState(CollidableComponent component) {

        return null;
    }

    int tick;

    ForgeDirection candidateDir;
    private WeakReference<ICraftingMachine> candidate = new WeakReference<>(null);;

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection) {
        if (this.net == null) return false;
        try {

            if (PartMAP2P.chain.contains(this.net)) {
                return false;
            }
            PartMAP2P.chain.add(this.net);

            if (tick == MinecraftServer.getServer()
                .getTickCounter()) {
                ICraftingMachine val;
                if ((val = candidate.get()) != null && val.pushPattern(patternDetails, table, candidateDir)) {
                    return true;
                }
            }

            if ((getExternalConnections().contains(StateHolder.state))
                && (getConnectionMode(StateHolder.state).acceptsInput())) {

                if (net != null) {

                    for (Entry<MAConduit, ForgeDirection> ent : net.conn.entries()) {

                        try {
                            TileEntity thiz = (TileEntity) getBundle.invoke(ent.getKey());
                            TileEntity te = getTarget(thiz, ent.getValue());
                            if (te == null) {
                                continue;
                            }

                            if (te instanceof ICraftingMachine) {
                                ForgeDirection old = StateHolder.state;
                                StateHolder.state = ent.getValue()
                                    .getOpposite();
                                if (((ICraftingMachine) te).pushPattern(
                                    patternDetails,
                                    table,
                                    ent.getValue()
                                        .getOpposite())) {
                                    StateHolder.state = old;
                                    return true;
                                }
                                StateHolder.state = old;
                            }

                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            e.printStackTrace();
                        }

                    }

                }

                return false;

            } ;

            return false;

        } finally {

            PartMAP2P.chain.remove(this.net);
        }
    }

    private TileEntity getTarget(TileEntity thiz, ForgeDirection side) {

        final TileEntity te = thiz.getWorldObj()
            .getTileEntity(thiz.xCoord + side.offsetX, thiz.yCoord + side.offsetY, thiz.zCoord + side.offsetZ);

        return te;
    }

    @Override
    public boolean acceptsPlans() {
        if (this.net == null) return false;
        // System.out.println(StateHolder.state);
        // System.out.println(getConnectionMode(StateHolder.state).acceptsInput());
        try {

            if (PartMAP2P.chain.contains(this.net)) {

                return false;
            }
            PartMAP2P.chain.add(this.net);

            if ((getExternalConnections().contains(StateHolder.state))
                && (getConnectionMode(StateHolder.state).acceptsInput())) {

                if (net != null) {

                    for (Entry<MAConduit, ForgeDirection> ent : net.conn.entries()) {

                        try {
                            TileEntity thiz = (TileEntity) getBundle.invoke(ent.getKey());
                            TileEntity te = getTarget(thiz, ent.getValue());
                            if (te == null) {
                                continue;
                            }

                            if (te instanceof ICraftingMachine) {
                                ForgeDirection old = StateHolder.state;
                                StateHolder.state = ent.getValue()
                                    .getOpposite();
                                if (((ICraftingMachine) te).acceptsPlans()) {
                                    StateHolder.state = old;

                                    tick = MinecraftServer.getServer()
                                        .getTickCounter();
                                    candidate = new WeakReference<ICraftingMachine>((ICraftingMachine) te);
                                    candidateDir = ent.getValue()
                                        .getOpposite();

                                    return true;
                                }
                                StateHolder.state = old;
                            }

                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            e.printStackTrace();
                        }

                    }

                }

                return false;

            } ;

            return false;

        } finally {

            PartMAP2P.chain.remove(this.net);
        }
    }

    @Override
    public ConduitRenderer getRenderer() {

        return ConduitRenderer.instance.get();
    }

    static Method getBundle;
    static Method getOffset;

    public TileEntity getTE() {
        if (getBundle == null) for (Method m : this.getClass()
            .getMethods()) {
                if (m.getName()
                    .equals("getBundle")) {

                    getBundle = m;

                }

            }
        try {
            return (TileEntity) getBundle.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getBundle();
        throw new AssertionError();
    }

    @Override
    public void externalConnectionAdded(ForgeDirection fromDirection) {
        super.externalConnectionAdded(fromDirection);
        ConnectionMode mode = ConnectionMode.DISABLED;

        TileEntity te = getLocation().getLocation(fromDirection)
            .getTileEntity(getTE().getWorldObj());
        if (clz.isInstance(te)) {
            setConnectionMode(fromDirection, ConnectionMode.DISABLED);
            return;
        }

        int bit = 0;
        if (te instanceof ICraftingMachine) {
            StateHolder.state = fromDirection.getOpposite();
            if (((ICraftingMachine) te).acceptsPlans()) bit = bit | 1;

        }
        if (te instanceof IInterfaceHost) {
            bit = bit | 2;
        }
        if (te instanceof IPartHost) {

            if (((IPartHost) te).getPart(fromDirection.getOpposite()) instanceof IInterfaceHost) bit = bit | 2;

            if (((IPartHost) te).getPart(fromDirection.getOpposite()) instanceof PartMAP2P) {
                PartMAP2P p2p = (PartMAP2P) ((IPartHost) te).getPart(fromDirection.getOpposite());
                if (p2p.output) {

                    bit = bit | 2;
                } else {
                    bit = bit | 1;

                }

            }

        }

        if (bit == 1) mode = ConnectionMode.OUTPUT;
        else if (bit == 2) mode = ConnectionMode.INPUT;
        else if (bit == 3) mode = ConnectionMode.IN_OUT;

        setConnectionMode(fromDirection, mode);

    }

    static Class clz;
    static {
        try {
            clz = Class.forName("crazypants.enderio.conduit.IConduitBundle");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        }
    }

    @Override
    public boolean canConnectToExternal(ForgeDirection direction, boolean ignoreConnectionMode) {
        TileEntity te = getLocation().getLocation(direction)
            .getTileEntity(getTE().getWorldObj());

        if (clz.isInstance(te)) {

            return false;
        }

        ignoreConnectionMode = true;
        a: if (te instanceof ICraftingMachine) {
            if (ignoreConnectionMode == false && !getConnectionMode(direction).acceptsOutput()) {
                break a;
            }
            return true;
        }
        a: if (te instanceof IInterfaceHost) {
            if (ignoreConnectionMode == false && !getConnectionMode(direction).acceptsInput()) {
                break a;
            }
            return true;
        }
        a: if (te instanceof IPartHost) {
            if (ignoreConnectionMode == false && !getConnectionMode(direction).acceptsInput()) {
                break a;
            }
            return ((IPartHost) te).getPart(direction.getOpposite()) instanceof IInterfaceHost;

        }
        return false;
    }

    @Override
    public boolean onBlockActivated(EntityPlayer player, RaytraceResult res, List<RaytraceResult> all) {

        /*
         * if (!getTE().getWorldObj().isRemote) {
         * System.out.println(net.conn.toString());
         * System.out.println(net.getConduits());
         * }
         */
        if (ToolUtil.isToolEquipped(player)) {
            if (!getTE().getWorldObj().isRemote) {
                if (res != null && res.component != null) {
                    ForgeDirection connDir = res.component.dir;
                    ForgeDirection faceHit = ForgeDirection.getOrientation(res.movingObjectPosition.sideHit);
                    if (connDir == ForgeDirection.UNKNOWN || connDir == faceHit) {
                        if (getConnectionMode(faceHit) == ConnectionMode.DISABLED) {
                            setConnectionMode(faceHit, getNextConnectionMode(faceHit));
                            return true;
                        }
                        // Attempt to join networks
                        return ConduitUtil.joinConduits(this, faceHit);
                    } else if (externalConnections.contains(connDir)) {
                        setConnectionMode(connDir, getNextConnectionMode(connDir));
                        return true;
                    } else if (containsConduitConnection(connDir)) {
                        ConduitUtil.disconectConduits(this, connDir);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static class ConduitRenderer extends DefaultConduitRenderer {

        public static final ThreadLocal<ConduitRenderer> instance = ThreadLocal.withInitial(ConduitRenderer::new);

        @Override
        public void renderEntity(ConduitBundleRenderer conduitBundleRenderer, IConduitBundle te, IConduit conduit,
            double x, double y, double z, float partialTick, float worldLight, RenderBlocks rb) {
            super.renderEntity(conduitBundleRenderer, te, conduit, x, y, z, partialTick, worldLight, rb);

            Map<String, IIcon> ICONS = null;
            try {
                Field f = null;
                f = ItemConduit.class.getDeclaredField("ICONS");
                f.setAccessible(true);
                ICONS = (Map<String, IIcon>) f.get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            IConduit pc = (IConduit) conduit;
            for (ForgeDirection dir : conduit.getExternalConnections()) {

                IIcon inTex = null;
                IIcon outTex = null;
                boolean render = true;
                if (conduit.getConnectionMode(dir) == ConnectionMode.INPUT) {
                    inTex = ICONS.get(ItemConduit.ICON_KEY_INPUT);
                    // inChannel = pc.getInputColor(dir);
                } else if (conduit.getConnectionMode(dir) == ConnectionMode.OUTPUT) {
                    outTex = ICONS.get(ItemConduit.ICON_KEY_OUTPUT);// pc.getTextureForOutputMode();
                    // outChannel = pc.getOutputColor(dir);
                } else if (conduit.getConnectionMode(dir) == ConnectionMode.IN_OUT) {
                    inTex = ICONS.get(ItemConduit.ICON_KEY_IN_OUT_IN);
                    outTex = ICONS.get(ItemConduit.ICON_KEY_IN_OUT_OUT);
                    // inChannel = pc.getInputColor(dir);
                    // outChannel = pc.getOutputColor(dir);
                } else {
                    render = false;
                }
                if (getOffset == null) for (Method m : ((Object) te).getClass()
                    .getMethods()) {
                        if (m.getName()
                            .equals("getOffset")) {

                            getOffset = m;

                        }

                    }
                if (render && !rb.hasOverrideBlockTexture()) {
                    Offset offset = null;

                    try {
                        offset = (Offset) getOffset.invoke(te, ICraftingMachineConduit.class, dir);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ConnectionModeGeometry
                        .renderModeConnector(dir, offset, ICONS.get(ItemConduit.ICON_KEY_IN_OUT_BG), true);

                    if (inTex != null) {
                        Tessellator.instance.setColorOpaque_I(ItemDye.field_150922_c[12]);// 12->bright red
                        ConnectionModeGeometry.renderModeConnector(dir, offset, inTex, false);
                    }
                    if (outTex != null) {
                        Tessellator.instance.setColorOpaque_I(ItemDye.field_150922_c[12]);
                        ConnectionModeGeometry.renderModeConnector(dir, offset, outTex, false);
                    }

                    Tessellator.instance.setColorOpaque_F(1f, 1f, 1f);
                }
            }
        }
    }

    /*
     * @Override
     * public void updateNetwork() {
     * World world = getBundle().getEntity().getWorldObj();
     * if (world != null) {
     * updateNetwork(world);
     * }
     * }
     */
    boolean neighbourDirty;

    @Override
    public void updateEntity(World world) {
        super.updateEntity(world);

        if (!world.isRemote && neighbourDirty) {
            net.destroyNetwork();
            updateNetwork(world);
            neighbourDirty = false;
        }
    }

    @Override
    public void externalConnectionRemoved(ForgeDirection fromDirection) {

        super.externalConnectionRemoved(fromDirection);

        if (net != null) net.updateConn(this);
    }

    @Override
    public boolean onNeighborBlockChange(Block block) {
        boolean B = super.onNeighborBlockChange(block);
        if (net != null) {
            net.updateConn(this);
        }

        return B;
    }

}
