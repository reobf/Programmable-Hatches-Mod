package reobf.proghatches.eucrafting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;

import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IReadOnlyCollection;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.covers.CoverContext;
import gregtech.api.covers.CoverPlacer;
import gregtech.api.gui.modularui.CoverUIBuildContext;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.covers.Cover;
//import gregtech.crossmod.waila.GregtechTEWailaDataProvider;
//import gregtech.crossmod.waila.GregtechWailaDataProvider;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.StackTraceUtil;

public class AECover extends CoverBehaviorBase<AECover.Data> {

    public static boolean mixinReady;// mixin will set this to true
    public static boolean getWailaBody;
    public static boolean getNBTData;

    public static interface IMemoryCardSensitive {
        // public boolean shiftClick(EntityPlayer entityPlayer);

        public default boolean memoryCard(EntityPlayer entityPlayer) {
            return false;
        };

    }

    /*
     * public AECover() {
     * this(InterfaceData.class);
     * }
     */

    @SuppressWarnings("unchecked")
    public AECover(CoverContext context, @NotNull Class<? extends Data> c, gregtech.api.interfaces.ITexture t) {

        super(context, (@NotNull Class<Data>) (clazz = c), t);

    }

    static Class<?> clazz;

    public static class DummyData implements Data {

        public IInterfaceHost getInterfaceOrNull() {
            return null;
        };

        public void setTag(NBTTagCompound tagCompound) {}

        public NBTTagCompound getTag() {
            return null;
        }

        @Override
        public boolean accept(ForgeDirection side, ICoverable aTileEntity, boolean onPlace) {
            return true;
        }

        @Override
        public ISer copy() {
            // TODO Auto-generated method stub
            return this;
        }

        @Override
        public NBTBase saveDataToNBT() {
            // TODO Auto-generated method stub
            return new NBTTagCompound();
        }

        @Override
        public void writeToByteBuf(ByteBuf aBuf) {
            // TODO Auto-generated method stub

        }

        @Override
        public void loadDataFromNBT(NBTBase aNBT) {

        }

        @Override
        public AENetworkProxy getProxy() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public DimensionalCoord getLocation() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void gridChanged() {
            // TODO Auto-generated method stub

        }

        @Override
        public IGridNode getGridNode(ForgeDirection dir) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AECableType getCableConnectionType(ForgeDirection dir) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void securityBreak() {
            // TODO Auto-generated method stub

        }

        @Override
        public AENetworkProxy getGridProxy() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setGridProxy(AENetworkProxy gridProxy) {
            // TODO Auto-generated method stub

        }

        @Override
        public ForgeDirection getSide() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setSide(ForgeDirection side) {
            // TODO Auto-generated method stub

        }

        @Override
        public DimensionalCoord getPos() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setPos(DimensionalCoord pos) {
            // TODO Auto-generated method stub

        }

        @Override
        public Data newInst() {
            // TODO Auto-generated method stub
            return this;
        }

        @Override
        public boolean firstUpdate() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public TileEntity fakeTile() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void readFromPacket(ByteArrayDataInput aBuf) {
            // TODO Auto-generated method stub

        }
    }

    static int h0 = getProviderClass().getName()
        .hashCode();

    public static interface Data extends ISer, IGridProxyable {

        default public ISer getCoverData(Cover c) {
            if (c instanceof AECover) {
                return ((AECover) c).coverData;
            }
            return null;

        }

        default public boolean isWailaCall() {
            if (FMLCommonHandler.instance()
                .getEffectiveSide() == Side.SERVER) {

                /*
                 * String s=t.fillInStackTrace().getStackTrace()[6].getMethodName();
                 * if (s.hashCode()==h0&&s.equals("getNBTData")) {
                 * return true;
                 * }
                 * s=t.fillInStackTrace().getStackTrace()[5].getMethodName();
                 * if (s.hashCode()==h0&&s.equals("getNBTData")) {
                 * return true;
                 * }
                 */
                if (mixinReady) {

                    return getNBTData;
                } else {
                    String s = StackTraceUtil.getCallerMethod(6);
                    if (s.hashCode() == h0 && s.equals(getProviderClass().getName())) {
                        return true;
                    }
                    s = StackTraceUtil.getCallerMethod(5);
                    if (s.hashCode() == h0 && s.equals(getProviderClass().getName())) {
                        return true;
                    }
                }

            }
            return false;
        }

        IInterfaceHost getInterfaceOrNull();

        default boolean hasModularGUI() {
            return false;
        }

        default String tagName() {
            NBTTagCompound tag = getTag();
            if (tag == null) return null;
            ItemStack is = new ItemStack(Items.apple);
            is.setTagCompound(tag);
            if (is.hasDisplayName()) return is.getDisplayName();
            return null;
        }

        default String name() {
            return null;
        }

        default boolean dualityName() {
            return false;
        }

        // AENetworkProxy gridProxy;
        // ForgeDirection side=ForgeDirection.UNKNOWN;
        // DimensionalCoord pos=new DimensionalCoord(0, 0, 0, 0);
        default ItemStack getVisual() {
            return new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE, 1);

        }

        default public boolean requireChannel() {
            return true;
        }

        default public AENetworkProxy getProxy() {

            if (getGridProxy() == null) {
                setGridProxy(new AENetworkProxy(this, "proxy", getVisual(), true));
                if (requireChannel()) getGridProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
                getGridProxy().setValidSides(EnumSet.of(getSide()));

            }

            // gridProxy.setOwner();

            return this.getGridProxy();
        }

        public AENetworkProxy getGridProxy();

        public void setGridProxy(AENetworkProxy gridProxy);

        public ForgeDirection getSide();

        public void setSide(ForgeDirection side);

        public DimensionalCoord getPos();

        public void setPos(DimensionalCoord pos);

        @Override
        default public ISer copy() {

            Data o = newInst();
            o.setSide(getSide());
            o.setPos(getPos().copy());

            return o;
        }

        default public Data newInst() {

            try {
                return this.getClass()
                    .newInstance();
            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;

        };

        @Override
        default public void writeToByteBuf(ByteBuf aBuf) {

            aBuf.writeInt(getSide().ordinal());
            DimensionalCoord loc = getLocation();
            aBuf.writeInt(loc.x);
            aBuf.writeInt(loc.y);
            aBuf.writeInt(loc.z);
            aBuf.writeInt(loc.getDimension());

        }

        public boolean firstUpdate();

        default World getW(int dim) {

            return getter.get(dim);
        }

        static Getter getter = new ClientGetter();

        public interface Getter {

            public default World get(int dim) {
                return DimensionManager.getWorld(dim);
            };
        }

        public class ClientGetter implements Getter {

            @SideOnly(Side.CLIENT)
            public World get(int dim) {
                if (FMLCommonHandler.instance()
                    .getEffectiveSide() == Side.SERVER) {
                    return Getter.super.get(dim);
                }
                return Minecraft.getMinecraft().theWorld;
            };
        }

        @Override
        default public NBTBase saveDataToNBT() {
            NBTTagCompound tag = new NBTTagCompound();

            Optional.ofNullable(getTag())
                .ifPresent(s -> tag.setTag("itemtag", s));
            NBTTagCompound ae = new NBTTagCompound();
            getProxy().writeToNBT(ae);
            tag.setTag("ae", ae);
            getPos().writeToNBT(tag);

            tag.setInteger("side", getSide().ordinal());
            return tag;
        }

        @Override
        default public void loadDataFromNBT(NBTBase aNBT) {
            // StackTraceUtil.getCallerMethod(1);

            NBTTagCompound tag = (NBTTagCompound) aNBT;

            setTag(tag.getCompoundTag("itemtag"));

            setPos(DimensionalCoord.readFromNBT(tag));
            setPos(new DimensionalCoord(getW(getPos().getDimension()), getPos().x, getPos().y, getPos().z));
            setSide(ForgeDirection.getOrientation(tag.getInteger("side")));
            getProxy().readFromNBT(tag.getCompoundTag("ae"));
        }

        @Override
        default public void readFromPacket(ByteArrayDataInput aBuf) {
            setSide(ForgeDirection.getOrientation(aBuf.readInt()));
            int x = aBuf.readInt();
            int y = aBuf.readInt();
            int z = aBuf.readInt();
            int dim = aBuf.readInt();
            this.setPos(new DimensionalCoord(getW(dim), x, y, z));
            if (fakeTile() != null) {
                fakeTile().xCoord = x;
                fakeTile().yCoord = y;
                fakeTile().zCoord = z;
                fakeTile().setWorldObj(getPos().getWorld());
            }
            // return this;
        }

        public TileEntity fakeTile();

        @Override
        default public IGridNode getGridNode(ForgeDirection dir) {

            return getProxy().getNode();
        }

        @Override
        default public AECableType getCableConnectionType(ForgeDirection dir) {

            return AECableType.SMART;
        }

        @Override
        default public void securityBreak() {

        }

        @Override
        default public DimensionalCoord getLocation() {

            return getPos();
        }

        @Override
        default public void gridChanged() {

        }

        default public void destroy() {
            // MyMod.LOG.info("Node destroy@" + getPos());

            try {
                if (this.getProxy()
                    .getNode() == null) return;
                IReadOnlyCollection<IGridConnection> col = this.getProxy()
                    .getNode()
                    .getConnections();
                Collection<IGridConnection> a = new ArrayList<>(col.size());// make
                                                                            // a
                                                                            // copy
                                                                            // or
                                                                            // get
                                                                            // ConcurrentModException
                col.forEach(a::add);
                a.forEach(ax -> ax.destroy());

                this.getProxy()
                    .invalidate();

                this.getProxy()
                    .getGrid()
                    .getCache(ITickManager.class)
                    .removeNode(
                        this.getProxy()
                            .getNode(),
                        this);
            } catch (Exception e) {

                // e.printStackTrace();
            }
        }

        default boolean supportFluid() {
            return false;
        }

        static DimensionalCoord unset_val = new DimensionalCoord(0, 0, 0, -1000);

        /**
         * @param side
         * @param aTileEntity
         * @param onPlace
         * @return
         */
        @SuppressWarnings("unchecked")
        default public boolean accept(ForgeDirection side, ICoverable aTileEntity, boolean onPlace) {
            if (getPos().equals(unset_val)) {
                // newly placed
            } else if (getPos().equals(new DimensionalCoord((TileEntity) aTileEntity))) {
                // normal chunk save+load
            } else {

                // cover on machine item place, drop it 'cause its data is broken
                UUID own = ((IGregTechTileEntity) aTileEntity).getOwnerUuid();
                if (aTileEntity instanceof IGregTechTileEntity) {
                    MinecraftServer.getServer()
                        .getConfigurationManager().playerEntityList.stream()
                            .filter(
                                s -> ((EntityPlayer) s).getUniqueID()
                                    .equals(own))
                            .findFirst()
                            .ifPresent(s -> {

                                ((EntityPlayer) s).addChatComponentMessage(
                                    new ChatComponentTranslation("programmable_hatches.cover.me.drop"));
                            });;

                }

                aTileEntity.dropCover(side, side);
                return false;

            }
            setPos(new DimensionalCoord((TileEntity) aTileEntity));
            setSide(side);
            Optional.ofNullable(aTileEntity.getCoverItemAtSide(side))
                .filter(s -> s.hasDisplayName())
                .ifPresent(s -> setCustomName(s.getDisplayName()));;
            return true;
        }

        public default void setCustomName(String s) {}

        public default void onReady() {}

        public default boolean shiftClick(ForgeDirection side, int aCoverID, Data aCoverVariable,
            ICoverable aTileEntity, EntityPlayer aPlayer) {
            return false;
        }

        public default boolean nonShiftClick(ForgeDirection side, int aCoverID, Data aCoverVariable,
            ICoverable aTileEntity, EntityPlayer aPlayer) {
            return false;
        }

        public default void update(ICoverable aTileEntity) {}

        public default void addUIWidgets(Builder builder, CoverUIBuildContext CoverUIBuildContext) {}

        default boolean hasAEGUI() {
            return true;
        }

        void setTag(NBTTagCompound tagCompound);

        NBTTagCompound getTag();
    }

    // private static Throwable t = new Throwable();

    @Override
    public Data initializeDataSer() {

        if (FMLCommonHandler.instance()
            .getEffectiveSide() == Side.CLIENT) {

            if (mixinReady) {
                if (getWailaBody) {
                    return new DummyData();
                }
            } else {
                String s = StackTraceUtil.getCallerMethod(6);
                if (s.hashCode() == h0 && s.equals(getProviderClass().getName())) {

                    // do not actually load cover data on client side
                    // or there'll be some performance issue
                    // this happens when waila trying to get cover info
                    return new DummyData();
                }

            }
        } ;

        try {
            return typeToken.newInstance();
        } catch (Exception e) {

            throw new AssertionError(e);
        }
    }

    /*
     * static Function<Object,Data> newInst;
     * static Field f;
     * static{
     * try {
     * f=CoverBehaviorBase.class.getDeclaredField("typeToken");
     * f.setAccessible(true);
     * newInst=s->{
     * try {
     * return (Data) ((Class)f.get(s)).newInstance();
     * } catch (Exception e) { e.printStackTrace();
     * throw new AssertionError(e);
     * }
     * };
     * } catch (Exception e) {e.printStackTrace();
     * throw new AssertionError(e);
     * }
     * }
     */
    /*
     * public void chunkUnload(Data t) {
     * t.getProxy()
     * .onChunkUnload();
     * }
     */
    @Override
    public void onCoverUnload() {
    	if(coverData.getProxy()!=null)coverData.getProxy()
            .onChunkUnload();

    }

    public static CoverPlacer placer() {
        return CoverPlacer.builder()
            .onlyPlaceIf(AECover::isCoverPlaceable)
            .build();

    }

    public static boolean isCoverPlaceable(ForgeDirection side, ItemStack aStack, ICoverable aTileEntity) {
        if (!Config.MECover && aTileEntity instanceof IGridProxyable
            && ((IGridProxyable) aTileEntity).getProxy() != null) {
            return false;
        }

        return true;
    }

    @Override
    public void onPlayerAttach(EntityPlayer player, ItemStack coverItem) {

        Data data = (Data) this.coverData;
        data.accept(coverSide, getTile(), false);
        data.getProxy()
            .setOwner(player);

    }

    public boolean onCoverRightClick(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity,
        EntityPlayer aPlayer, float aX, float aY, float aZ) {
        if (aCoverVariable.nonShiftClick(side, aCoverID, aCoverVariable, aTileEntity, aPlayer)) {
            return true;
        }
        openGUI(side, aCoverID, aCoverVariable, aTileEntity, aPlayer);
        return false;
    };

    @Override
    public void onCoverScrewdriverClick(EntityPlayer aPlayer, float aX, float aY, float aZ) {
        if (this.coverData.hasModularGUI()) GTUIInfos.openCoverUI(this.coveredTile.get(), aPlayer, coverSide);
        super.onCoverScrewdriverClick(aPlayer, aX, aY, aZ);
    }

    private boolean openGUI(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity,
        EntityPlayer aPlayer) {

        /*
         * if (aCoverVariable.hasModularGUI()&&Optional.ofNullable(aPlayer.getHeldItem()).map(ItemStack::getItem)
         * .orElse(null)==MyMod.eu_tool
         * ) {
         * GTUIInfos.openCoverUI(aTileEntity, aPlayer, side);
         * return true;
         * }
         */

        if (aCoverVariable.hasAEGUI() && !aPlayer.worldObj.isRemote) {
            aPlayer.openGui(
                MyMod.instance,
                side.ordinal(),
                aPlayer.getEntityWorld(),
                aTileEntity.getXCoord(),
                aTileEntity.getYCoord(),
                aTileEntity.getZCoord());
            return true;
        }
        return false;
    }

    @Override
    public boolean onCoverShiftRightClick(EntityPlayer aPlayer) {
        if (coverData.shiftClick(coverSide, this.coverID, coverData, coveredTile.get(), aPlayer)) {
            return true;
        } ;

        openGUI(coverSide, this.coverID, coverData, coveredTile.get(), aPlayer);

        /*
         * if (aCoverVariable.hasAEGUI() && !aPlayer.worldObj.isRemote) {
         * aPlayer.openGui(MyMod.instance, side.ordinal(), aPlayer.getEntityWorld(), aTileEntity.getXCoord(),
         * aTileEntity.getYCoord(), aTileEntity.getZCoord());
         * }
         */

        /*
         * NW.sendPacketToAllPlayersInRange(aPlayer.getEntityWorld(),
         * new GTPacketSendCoverData(side, aCoverID, aCoverVariable, aTileEntity), aTileEntity.getXCoord(),
         * aTileEntity.getZCoord());
         */

        return true;
    }

    @Override
    public boolean allowsTickRateAddition() {

        return false;
    }

  

    @Override
    public int getMinimumTickRate() {

        return 1;
    }
	@Override
	public int getDefaultTickRate() {
	
		return 1;
	}

    @Override
    public void onCoverRemoval() {
        this.coverData.destroy();

    }

    /*
     * @Override public void onBaseTEDestroyed(ForgeDirection side, int
     * aCoverID, Data data, ICoverable aTileEntity) {
     * data.getProxy().getNode().getConnections().forEach(s->s.destroy());
     * data.getProxy().invalidate(); try {
     * data.getProxy().getGrid().getCache(ITickManager.class).removeNode(data.
     * getProxy().getNode(), data); } catch (GridAccessException e) {
     * e.printStackTrace(); } }
     */
    static Method m;

    static {
        try {
            m = GridNode.class.getDeclaredMethod("isValidDirection", ForgeDirection.class);
            m.setAccessible(true);
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    static public boolean canConnect(Object obj, ForgeDirection dir) {

        if (obj instanceof GridNode) {
            GridNode node = (GridNode) obj;
            try {
                return (Boolean) m.invoke(node, dir);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return false;
        // private boolean canConnect(final GridNode from, final ForgeDirection
        // dir) {
    }

    @Override
    public void doCoverThings(byte aRedstone, long aTickTimer) {
        @NotNull
        Data data = this.coverData;
        ForgeDirection side = this.coverSide;
        ICoverable aTileEntity = this.coveredTile.get();
        if (data.firstUpdate()) if (!data.accept(side, aTileEntity, false)) {
            return;
        } ;

        if (!data.getProxy()
            .isReady()) {
            data.getProxy()
                .onReady();
            data.onReady();
        }
        data.update(aTileEntity);
        //
        AENetworkProxy host = null;
        if (aTileEntity instanceof IGridProxyable && (host = ((IGridProxyable) aTileEntity).getProxy()) != null) {
            IGridNode thiz = data.getProxy()
                .getNode();
            boolean found = false;
            try {
                Iterator<IGridConnection> it = thiz.getConnections()
                    .iterator();
                while (it.hasNext()) {
                    IGridConnection item = it.next();
                    if (item.a() == host.getNode() || item.b() == host.getNode()) {

                        found = true;
                        break;
                    } ;

                }
                if (!found) {
                    // MyMod.LOG.info("Node internal connect@" + data.getPos());
                    new GridConnection(thiz, host.getNode(), ForgeDirection.UNKNOWN);
                }
            } catch (FailedConnection e) {

                e.printStackTrace();
            }
        }
        //

        TileEntity te = aTileEntity.getTileEntityAtSide(side);
        lab: if (te != null && te instanceof IGridHost) {
            Iterator<IGridConnection> it = data.getProxy()
                .getNode()
                .getConnections()
                .iterator();
            IGridConnection item = null;
            IGridNode thenode = ((IGridHost) te).getGridNode(side.getOpposite());
            if (thenode == null) {
                break lab;
            } // this is possible if there're only parts no cable
            boolean found = false;

            boolean thisSideValid = canConnect(thenode, side.getOpposite());

            while (it.hasNext()) {
                item = it.next();
                if (item.a() == thenode || item.b() == thenode) {
                    if (thisSideValid == false) {
                        item.destroy();
                    } else found = true;
                    break;
                } ;

            }

            if (found == false && thisSideValid) {
                try {
                    IGridNode thiz = data.getProxy()
                        .getNode();
                    new GridConnection(thiz, thenode, side);

                    // MyMod.LOG.info("Node connect@" + data.getPos());
                } catch (FailedConnection e) {

                    // System.out.println(item.a());
                    // System.out.println(item.b());
                    // System.out.println(thenode);
                    e.printStackTrace();
                }

            } ;

        }

        super.doCoverThings(aRedstone, aTickTimer);
    }

    @Override
    public boolean letsEnergyIn() {
        return true;
    }

    @Override
    public boolean letsEnergyOut() {
        return true;
    }

    @Override
    public boolean letsFluidIn(Fluid fluid) {
        return true;
    }

    @Override
    public boolean letsFluidOut(Fluid fluid) {
        return true;
    }

    @Override
    public boolean letsItemsIn(int slot) {
        return true;
    }

    @Override
    public boolean letsItemsOut(int slot) {
        return true;
    }

    @Override
    public boolean letsRedstoneGoIn() {
        return true;
    }

    @Override
    public boolean letsRedstoneGoOut() {
        return true;
    }

    @Override
    public boolean hasCoverGUI() {

        return true;
    }

    // @Override
    public boolean useModularUI() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public ModularWindow createWindow(CoverUIBuildContext buildContext) {
        return new AECoverUIFactory(
            buildContext,
            (((AECover) buildContext.getTile()
                .getCoverAtSide(buildContext.getCoverSide())).coverData)).createWindow();
    }

    private class AECoverUIFactory extends CoverUIFactory<AECover> {

        public AECoverUIFactory(CoverUIBuildContext buildContext, Data d) {
            super(buildContext);
            this.data = d;
        }

        Data data;

        @Override
        public ModularWindow createWindow() {
            // TODO Auto-generated method stub
            return super.createWindow();
        }

        @Override
        public void addUIWidgets(Builder builder) {

            data.addUIWidgets(builder, getUIBuildContext());
        }
    }

    @Override
    public boolean allowsCopyPasteTool() {

        return false;// no!
    }

    public static ISer getCoverData(Cover c) {
        if (c instanceof AECover) {
            return ((AECover) c).coverData;
        }
        return null;
    }
    
    
    private static Class theClass;
    public static Class getProviderClass() {
    if(theClass==null) {
		    	try {
		    	theClass=Class.forName("gregtech.crossmod.waila.GregtechWailaDataProvider");
			} catch (ClassNotFoundException e) {}
		    
		    try {
			theClass=Class.forName("gregtech.crossmod.waila.GregtechTEWailaDataProvider");
			} catch (ClassNotFoundException e) {}
    }
    
    if(theClass==null)throw new AssertionError("null");
	return theClass;
    	
    	
    }
}
