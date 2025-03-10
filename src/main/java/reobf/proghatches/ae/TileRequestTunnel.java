package reobf.proghatches.ae;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.util.BlockPos;
import com.google.common.collect.ImmutableSet;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IOrientable;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import reobf.proghatches.ae.part2.RequestTunnel;

public class TileRequestTunnel extends AENetworkTile
    implements ICraftingMachine, ICraftingRequester, IOrientable, ISidedInventory, IFluidHandler,ITileWithModularUI {

    RequestTunnel internal = new RequestTunnel() {

        public TileRequestTunnel getThis() {
            return TileRequestTunnel.this;
        }

        @Override
        public IGridNode getActionableNode() {

            return getThis().getActionableNode();
        }

        @Override
        public IGridNode getGridNode(ForgeDirection dir) {

            return getThis().getGridNode(dir);
        }

        @Override
        public AECableType getCableConnectionType(ForgeDirection dir) {

            return getThis().getCableConnectionType(dir);
        }

        @Override
        public void securityBreak() {
            getThis().securityBreak();

        }

        @Override
        public void markDirty() {
            getThis().markDirty();

        }

        @Override
        public AENetworkProxy getProxy() {

            return getThis().getProxy();
        }

        @Override
        public World getWorldObj() {

            return getThis().getWorldObj();
        }

        @Override
        public IMEInventory getInv(StorageChannel ch) {

            return getThis().getInv(ch);
        }
    };

    public static IWailaDataProvider provider = new IWailaDataProvider() {

        @Override
        public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {

            return null;
        }

        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

            return null;
        }

        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
            try {
                if (accessor.getSide() == ((TileRequestTunnel) accessor.getTileEntity()).getUp()
                    .getOpposite()) {

                    currenttip.add("Auto-output to this side");

                } ;

                NBTTagCompound data = accessor.getNBTData();

                ArrayList<ItemStack> cacheR = new ArrayList<>();
                {
                    NBTTagList t = (NBTTagList) data.getTag("cacheR");
                    for (int i = 0; i < t.tagCount(); i++) {
                        cacheR.add(ItemStack.loadItemStackFromNBT(t.getCompoundTagAt(i)));

                    }
                }
                ArrayList<FluidStack> cacheFR = new ArrayList<>();
                {
                    NBTTagList t = (NBTTagList) data.getTag("cacheFR");
                    for (int i = 0; i < t.tagCount(); i++) {
                        cacheFR.add(FluidStack.loadFluidStackFromNBT(t.getCompoundTagAt(i)));

                    }
                }

                List<String> ret = currenttip;

                if (cacheR.isEmpty() == false || cacheFR.isEmpty() == false) ret.add("Sending:");

                cacheR.forEach(s -> {

                    ret.add("" + s.getDisplayName() + " x" + s.stackSize);

                });

                cacheFR.forEach(s -> {

                    ret.add(
                        "" + s.getFluid()
                            .getLocalizedName(s) + " x" + s.amount + "mB");

                });
                NavigableMap<AEItemStack, Long> waiting = new TreeMap<>();
                NBTTagList t = (NBTTagList) data.getTag("waiting");
                for (int i = 0; i < t.tagCount(); i++) {
                    NBTTagCompound tag = t.getCompoundTagAt(i);

                    AEItemStack key = (AEItemStack) AEItemStack.loadItemStackFromNBT(tag.getCompoundTag("key"));
                    long value = (tag.getLong("value"));
                    waiting.put(key, value);
                }
                ret.add("Waiting for:");
                waiting.entrySet()
                    .forEach(s -> {

                        ret.add(
                            "" + s.getKey()
                                .getDisplayName() + " x" + s.getValue());

                    });

                return ret;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return currenttip;
        }

        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {

            return null;
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound data, World world, int x,
            int y, int z) {

            TileRequestTunnel thiz = (TileRequestTunnel) te;
            {
                NBTTagList listR = new NBTTagList();
                thiz.internal.cacheR.stream()
                    .map(s -> s.writeToNBT(new NBTTagCompound()))
                    .forEach(s -> listR.appendTag(s));
                data.setTag("cacheR", listR);
                NBTTagList listFR = new NBTTagList();
                thiz.internal.cacheFR.stream()
                    .map(s -> s.writeToNBT(new NBTTagCompound()))
                    .forEach(s -> listFR.appendTag(s));
                data.setTag("cacheFR", listFR);
            }
            NBTTagList listR = new NBTTagList();
            thiz.internal.waiting.entrySet()
                .stream()
                .map(s -> {
                    NBTTagCompound t = new NBTTagCompound();
                    NBTTagCompound k = new NBTTagCompound();
                    s.getKey()
                        .writeToNBT(k);
                    t.setTag("key", k);
                    t.setLong("value", s.getValue());

                    return t;
                })
                .forEach(s -> listR.appendTag(s));
            data.setTag("waiting", listR);;
            return data;
        }
    };

    public TileRequestTunnel() {
        this.getProxy()
            .setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection) {

        return internal.pushPattern(patternDetails, table, ejectionDirection);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBT_AENetworkX(NBTTagCompound data) {
        internal.readFromNBT_AENetworkX(data);

    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBT_AENetworkX(NBTTagCompound data) {
        internal.writeToNBT_AENetworkX(data);
    }

    @Override
    public boolean acceptsPlans() {

        return internal.acceptsPlans();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return internal.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {

        return internal.injectCraftedItems(link, items, mode);
    }

    HashMap<StorageChannel, IMEInventory> inv = new HashMap();
    HashMap<StorageChannel, Integer> handlerHash = new HashMap();
    // public ItemStack[] mark=new ItemStack[1];
    private BaseActionSource source = new MachineSource(this);

    ForgeDirection prevDir;

    public IMEInventory getInv(StorageChannel ch) {
        if (prevDir != getUp()) {
            prevDir = getUp();
            this.inv.clear();
        }
        if (getSide() == ForgeDirection.UNKNOWN) return null;
        final TileEntity self = this;
        final TileEntity target = new BlockPos(self).getOffSet(this.getSide())
            .getTileEntity();

        final int newHandlerHash = Platform.generateTileHash(target);
        if (newHandlerHash != 0 && newHandlerHash == this.handlerHash.getOrDefault(ch, 0)) {
            return this.inv.get(ch);
        }

        final IExternalStorageHandler esh = AEApi.instance()
            .registries()
            .externalStorage()
            .getHandler(
                target,
                this.getSide()
                    .getOpposite(),
                ch,
                this.source);
        if (esh != null) {
            final IMEInventory<?> inv = esh.getInventory(
                target,
                this.getSide()
                    .getOpposite(),
                ch,
                this.source);
            this.inv.put(ch, inv);
            handlerHash.put(ch, newHandlerHash);
            return inv;
        } else {

            handlerHash.put(ch, 0);
        }
        return null;

    }

    private ForgeDirection getSide() {

        return getUp().getOpposite();
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        internal.jobStateChange(link);

    }

    @TileEvent(TileEventType.TICK)
    public void update() {
        internal.update();

    }

    int mode;

    public boolean canBeRotated() {
        return true;
    }

    public void setSide(ForgeDirection axis) {
        if (Platform.isClient()) {
            return;
        } // axis=axis.getOpposite();
        ForgeDirection pointAt = getUp().getOpposite();
        if (pointAt == axis.getOpposite()) {
            pointAt = axis;
        } else if (pointAt == axis || pointAt == axis.getOpposite()) {
            pointAt = ForgeDirection.UNKNOWN;
        } else if (pointAt == ForgeDirection.UNKNOWN) {
            pointAt = axis.getOpposite();
        } else {
            pointAt = Platform.rotateAround(pointAt, axis);
        }

        if (ForgeDirection.UNKNOWN == pointAt) {
            this.setOrientation(pointAt, pointAt);
        } else {
            this.setOrientation(pointAt.offsetY != 0 ? ForgeDirection.SOUTH : ForgeDirection.UP, pointAt.getOpposite());
        }

        this.markForUpdate();
        this.markDirty();
    }
    /*
     * ForgeDirection getForward() {
     * return null;
     * }
     * ForgeDirection getUp() {
     * return null;
     * }
     */

    public void setOrientation(ForgeDirection Forward, ForgeDirection Up) {
        super.setOrientation(Forward, Up);
        ForgeDirection pointAt = Up.getOpposite();
        this.getProxy()
            .setValidSides(EnumSet.complementOf(EnumSet.of(pointAt)));
    }

    @Override
    public int getSizeInventory() {

        return internal.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {

        return internal.getStackInSlot(slotIn);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return internal.decrStackSize(index, count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        internal.setInventorySlotContents(index, stack);

    }

    @Override
    public String getInventoryName() {

        return "";
    }

    @Override
    public boolean hasCustomInventoryName() {

        return false;
    }

    @Override
    public int getInventoryStackLimit() {

        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {

        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {

        return internal.isItemValidForSlot(index, stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return internal.getAccessibleSlotsFromSide(p_94128_1_);
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {

        return internal.canInsertItem(p_102007_1_, p_102007_2_, p_102007_3_);
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {

        return internal.canExtractItem(p_102008_1_, p_102008_2_, p_102008_3_);
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {

        return internal.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {

        return internal.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {

        return internal.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {

        return internal.canFill(from, fluid);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {

        return internal.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {

        return internal.getTankInfo(from);
    }

	@Override
	public ModularWindow createWindow(UIBuildContext buildContext) {
		// TODO Auto-generated method stub
		return internal.createWindow(buildContext);
	}
	

}
