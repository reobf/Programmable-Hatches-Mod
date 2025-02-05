package reobf.proghatches.ae.part2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import net.minecraft.client.renderer.RenderBlocks;
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

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.util.AECableType;
import appeng.client.texture.CableBusTextures;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.PartBasicState;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.fmp.ICraftingMachinePart;

public class PartRequestTunnel extends PartBasicState
    implements ICraftingMachinePart, ISidedInventory, IFluidHandler, IGridTickable {

    public static class WailaDataProvider extends appeng.integration.modules.waila.part.BasePartWailaDataProvider {

        @Override
        public List<String> getWailaBody(IPart part, List<String> currenttip, IWailaDataAccessor accessor,
            IWailaConfigHandler config) {
            if (!(part instanceof PartRequestTunnel)) {
                return currenttip;
            }
            try {
                /*
                 * if( accessor.getSide()==((TileRequestTunnel)accessor.getTileEntity()).getUp().getOpposite()){
                 * currenttip.add("Auto-output to this side");
                 * };
                 */

                NBTTagCompound data = accessor.getNBTData();

                ArrayList<ItemStack> cacheR = new ArrayList<>();
                {
                    NBTTagList t = (NBTTagList) data.getTag("cacheR");
                    if (t != null) for (int i = 0; i < t.tagCount(); i++) {
                        cacheR.add(ItemStack.loadItemStackFromNBT(t.getCompoundTagAt(i)));

                    }
                }
                ArrayList<FluidStack> cacheFR = new ArrayList<>();
                {
                    NBTTagList t = (NBTTagList) data.getTag("cacheFR");
                    if (t != null) for (int i = 0; i < t.tagCount(); i++) {
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
                if (t != null) for (int i = 0; i < t.tagCount(); i++) {
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
        public NBTTagCompound getNBTData(EntityPlayerMP player, IPart part, TileEntity te, NBTTagCompound data,
            World world, int x, int y, int z) {

            if (!(part instanceof PartRequestTunnel)) {
                return data;
            }
            PartRequestTunnel thiz = (PartRequestTunnel) part;
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

    }

    public PartRequestTunnel(ItemStack is) {
        super(is);

    }

    RequestTunnel internal = new RequestTunnel() {

        public PartRequestTunnel getThis() {
            return PartRequestTunnel.this;
        }

        @Override
        public IGridNode getActionableNode() {
            // TODO Auto-generated method stub
            return getThis().getActionableNode();
        }

        @Override
        public IGridNode getGridNode(ForgeDirection dir) {

            return getThis().getGridNode(dir);
        }

        @Override
        public AECableType getCableConnectionType(ForgeDirection dir) {
            // TODO Auto-generated method stub
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
        public IMEInventory getInv(StorageChannel ch) {
            // TODO Auto-generated method stub
            return getThis().getInv(ch);
        }

        @Override
        public AENetworkProxy getProxy() {
            // TODO Auto-generated method stub
            return getThis().getProxy();
        }

        @Override
        public World getWorldObj() {
            // TODO Auto-generated method stub
            return getThis().getHost()
                .getTile()
                .getWorldObj();
        }
    };

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection) {

        return internal.pushPattern(patternDetails, table, ejectionDirection);
    }

    HashMap<StorageChannel, IMEInventory> inv = new HashMap();
    HashMap<StorageChannel, Integer> handlerHash = new HashMap();
    private BaseActionSource source = new MachineSource(this);

    protected IMEInventory getInv(StorageChannel ch) {
        if (getSide() == ForgeDirection.UNKNOWN) return null;
        final TileEntity self = this.getTile();
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

    public void markDirty() {

    }

    @Override
    public boolean acceptsPlans(ForgeDirection ejectionDirection) {

        return true;
    }

    //
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
    public TickingRequest getTickingRequest(IGridNode node) {

        return new TickingRequest(1, 1, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        internal.update();
        return TickRateModulation.SAME;
    }
    //

    @Override
    public void readFromNBT(NBTTagCompound data) {
        internal.readFromNBT_AENetworkX(data);
        super.readFromNBT(data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        internal.writeToNBT_AENetworkX(data);
        super.writeToNBT(data);
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

}
