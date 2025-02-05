package reobf.proghatches.block;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import reobf.proghatches.block.ChunkTrackingGridCahce.ChunkInfo;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

public class TileAnchorAlert extends TileEntity implements IGridProxyable {

    public static final int ALL = 0;
    public static final int DIM = 1;
    public static final int OWNER = 2;
    public static IWailaDataProvider provider = new IWailaDataProvider() {

        @Override
        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {

            return null;
        }

        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

            return currenttip;
        }

        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

            currenttip.add(
                StatCollector.translateToLocal(
                    "proghatch.chunk_loading_alert.mode." + accessor.getNBTData()
                        .getInteger("mode")));
            currenttip.add(
                StatCollector.translateToLocalFormatted(
                    "proghatch.chunk_loading_alert.count",
                    accessor.getNBTData()
                        .getInteger("loaded"),
                    accessor.getNBTData()
                        .getInteger("count")

                ));

            return currenttip;
        }

        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

            return currenttip;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
            int y, int z) {
            tag.setInteger("mode", ((TileAnchorAlert) te).mode);

            try {
                tag.setInteger(
                    "count",
                    ((ChunkTrackingGridCahce) ((TileAnchorAlert) te).getProxy()
                        .getGrid()
                        .getCache(IChunkTrackingGridCahce.class)).track.size()

                );

            } catch (Exception e) {}

            try {
                tag.setInteger(
                    "loaded",

                    ((ChunkTrackingGridCahce) ((TileAnchorAlert) te).getProxy()
                        .getGrid()
                        .getCache(IChunkTrackingGridCahce.class)).improperlyUnloaded.size()

                );
            } catch (GridAccessException e) {

            }

            return tag;
        }
    };

    int mode;

    // 2 inform owner only
    // 1 inform all players in same dim
    // 0 inform all online player
    @Override
    public IGridNode getGridNode(ForgeDirection dir) {

        return createProxy().getNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {

        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public AENetworkProxy getProxy() {

        return createProxy();
    }

    AENetworkProxy proxy;

    protected AENetworkProxy createProxy() {
        if (proxy != null) return proxy;

        proxy = new AENetworkProxy(this, "proxy", new ItemStack(MyMod.alert), true);

        // proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
        proxy.setValidSides(EnumSet.range(ForgeDirection.DOWN, ForgeDirection.EAST));
        return proxy;
    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {

    }

    UUID owner;

    public void mark(EntityPlayer placer) {
        createProxy().setOwner((EntityPlayer) placer);
        owner = placer.getUniqueID();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        mode = compound.getInteger("m");
        owner = ProghatchesUtil.deser(compound, "OWNER_UUID");
        if (owner.getLeastSignificantBits() == 0 && owner.getMostSignificantBits() == 0) owner = null;
        createProxy().readFromNBT(compound);
        super.readFromNBT(compound);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("m", mode);
        if (owner != null) ProghatchesUtil.ser(compound, owner, "OWNER_UUID");
        createProxy().writeToNBT(compound);
        super.writeToNBT(compound);
    }

    @Override
    public void updateEntity() {
        ticksSinceLoaded++;
        super.updateEntity();
        if (!getProxy().isReady()) getProxy().onReady();
    }

    public void onChunkUnload() {

        this.getProxy()
            .onChunkUnload();
        super.onChunkUnload();
    }

    public void invalidate() {

        this.getProxy()
            .invalidate();
        super.invalidate();
    }

    @Override
    public void validate() {
        this.getProxy()
            .validate();
        super.validate();
    }

    int loadedChunksCache;
    int lastUpdate = -50;
    int ticksSinceLoaded;

    public int getLoadedChunks() {
        if (lastUpdate + 20 > ticksSinceLoaded) {
            // update every 2sec
            return loadedChunksCache;
        }

        lastUpdate = ticksSinceLoaded;
        try {
            loadedChunksCache = (int) ((ChunkTrackingGridCahce) ((TileAnchorAlert) this).getProxy()
                .getGrid()
                .getCache(IChunkTrackingGridCahce.class)).track.keySet()

                    .stream()
                    .filter(s -> {

                        WorldServer wd = DimensionManager.getWorld(s.dim);
                        if (wd == null) return false;
                        return wd.getChunkProvider()
                            .chunkExists(s.chunkx, s.chunky);
                    })
                    .count();

            ;

        } catch (GridAccessException e) {
            loadedChunksCache = 0;

        }

        return loadedChunksCache;
    }

    public void printUnloaded(EntityPlayer player) {
        try {
            boolean any[] = new boolean[1];
            ((ChunkTrackingGridCahce) ((TileAnchorAlert) this).getProxy()
                .getGrid()
                .getCache(IChunkTrackingGridCahce.class)).improperlyUnloaded.forEach((s) -> {

                    /*
                     * WorldServer wd = DimensionManager.getWorld(s.dim);
                     * if(wd==null||
                     * false==wd.getChunkProvider().chunkExists(s.chunkx,s.chunky)){
                     */ any[0] = true;
                    ChunkInfo info = s;
                    player.addChatComponentMessage(
                        new ChatComponentTranslation(
                            "proghatch.chunk_loading_alert.info",
                            "X:" + info.chunkx
                                + ",Z:"
                                + info.chunky
                                + ",dim:"
                                + info.dim
                                + " "
                                + "Center:"
                                + ((info.chunkx << 4) + 8)
                                + ","
                                + ((info.chunky << 4) + 8)

                        ));
                    /*
                     * };
                     */
                });;

            if (any[0] == false) {
                player.addChatComponentMessage(new ChatComponentTranslation("proghatch.chunk_loading_alert.info.none"));

            }
        } catch (GridAccessException e) {
            e.printStackTrace();
        }

    }

}
