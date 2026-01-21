package reobf.proghatches.gt.metatileentity;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gtnhlanth.common.hatch.MTEBusInputFocus;
import reobf.proghatches.gt.metatileentity.SuperChestME.UnlimitedWrapper;
import reobf.proghatches.gt.metatileentity.util.IStoageCellUpdate;
import reobf.proghatches.main.registration.Registration;

public class NBTHatchMEFocus extends MTEBusInputFocus implements ICellContainer, IGridProxyable, IStoageCellUpdate {

    public NBTHatchMEFocus(int id, String name, String nameRegional) {
        super(id, name, nameRegional);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id));
    }

    public NBTHatchMEFocus(String name, String[] descriptionArray, ITexture[][][] textures) {
        super(name, descriptionArray, textures);

    }

    String[] descCache;

    @Override
    public String[] getDescription() {

        return descCache == null ? (descCache = reobf.proghatches.main.Config.get("HMEF", ImmutableMap.of()))
            : descCache;
    }

    /*
     * @Override public boolean isItemValidForUsageSlot(ItemStack aStack) {
     * return false; }
     */
    @Override
    public IGridNode getActionableNode() {

        return getProxy().getNode();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {

        return getProxy().getNode();
    }

    @Override
    public void securityBreak() {}

    public int getIndex() {

        return getInputSlotCount();
    }

    IMEInventoryHandler<AEItemStack> handler = new MEInventoryHandler(new UnlimitedWrapper(), StorageChannel.ITEMS);

    public class UnlimitedWrapper implements IMEInventory<IAEItemStack> {

        public UnlimitedWrapper() {

        }

        @Override
        public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src) {
            if (type != Actionable.SIMULATE) post();

            if (input == null) {
                return input;
            }
            if (!isItemValidForInputSlot(input.getItemStack())) {
                return input;
            }

            try {
                long l = input.getStackSize();
                long compl = 0;
                if (l > Integer.MAX_VALUE) {
                    compl = l - Integer.MAX_VALUE;
                }
                ItemStack in = input.getItemStack();
                ItemStack thiz = mInventory[getIndex()];
                if (thiz != null && !Platform.isSameItem(in, thiz)) return input;
                if (thiz == null) {
                    thiz = in.copy();
                    thiz.stackSize = 0;
                }
                int space = Math.max(0, cap() - thiz.stackSize);
                int transfer = Math.min(space, in.stackSize);
                if (type == Actionable.SIMULATE) {
                    in.stackSize -= transfer;
                    if (in.stackSize <= 0 && compl == 0) in = null;
                    AEItemStack ret = AEItemStack.create(in);
                    if (ret != null) ret.incStackSize(compl);
                    return ret;
                }
                if (type == Actionable.MODULATE) {
                    thiz.stackSize += transfer;
                    mInventory[getIndex()] = thiz;
                    in.stackSize -= transfer;
                    if (in.stackSize <= 0 && compl == 0) in = null;
                    AEItemStack ret = AEItemStack.create(in);
                    if (ret != null) ret.incStackSize(compl);
                    return ret;

                }

                return null;
            } finally {
                last = AEItemStack.create(mInventory[getIndex()]);

            }
        }

        @Override
        public IAEItemStack extractItems(IAEItemStack input, Actionable type, BaseActionSource src) {
            try {

                if (type != Actionable.SIMULATE) post();

                ItemStack in = input.getItemStack();
                ItemStack thiz = mInventory[getIndex()];
                if (thiz != null && !Platform.isSameItem(in, thiz)) return null;
                if (thiz == null) {
                    return null;
                } // thiz=in.copy(); }
                int transfer = Math.min(in.stackSize, thiz.stackSize);
                if (transfer == 0) return null;
                if (type == Actionable.SIMULATE) {
                    in.stackSize = transfer;
                    return AEItemStack.create(in);

                }
                if (type == Actionable.MODULATE) {
                    thiz.stackSize -= transfer;
                    if (thiz.stackSize <= 0) thiz = null;
                    mInventory[getIndex()] = thiz;
                    in.stackSize = transfer;
                    return AEItemStack.create(in);
                }

                return null;
            } finally {

                last = AEItemStack.create(mInventory[getIndex()]);
            }
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
            if (mInventory[getIndex()] != null) out.addStorage(AEItemStack.create(mInventory[getIndex()]));
            return out;
        }

        @Override
        public StorageChannel getChannel() {

            return StorageChannel.ITEMS;
        }
    }

    private AEItemStack last;

    private void post() {

        try {

            // this.getProxy().getGrid().postEvent(new
            // MENetworkCellArrayUpdate());
            // this.getProxy().getGrid().postEvent(new
            // MENetworkStorageEvent(handler0, StorageChannel.ITEMS));
            try {

                if (last != null) {
                    if (mInventory[getIndex()] != null) {
                        if (last.equals(mInventory[getIndex()])) {
                            if (last.getStackSize() == mInventory[getIndex()].stackSize) {
                                return;
                            } else {

                                this.getProxy()
                                    .getStorage()
                                    .postAlterationOfStoredItems(
                                        StorageChannel.ITEMS,
                                        ImmutableList.of(
                                            last.copy()
                                                .setStackSize(mInventory[getIndex()].stackSize - last.getStackSize())),
                                        new MachineSource(this));
                                last = AEItemStack.create(mInventory[getIndex()]);
                                return;
                            }
                        }

                    } ;

                    this.getProxy()
                        .getStorage()
                        .postAlterationOfStoredItems(
                            StorageChannel.ITEMS,
                            ImmutableList.of(
                                last.copy()
                                    .setStackSize(-last.getStackSize())),
                            new MachineSource(this));
                }
                last = AEItemStack.create(mInventory[getIndex()]);
                if (last != null) {
                    this.getProxy()
                        .getStorage()
                        .postAlterationOfStoredItems(
                            StorageChannel.ITEMS,
                            ImmutableList.of(
                                last.copy()
                                    .setStackSize(last.getStackSize())),
                            new MachineSource(this));
                }

            } catch (final GridAccessException ignore) {}
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
        if (channel == StorageChannel.FLUIDS) return ImmutableList.of();

        return ImmutableList.of(handler);
    }

    @Override
    public int getPriority() {

        return 0;
    }

    @Override
    public void saveChanges(IMEInventory cellInventory) {
        markDirty();

    }

    @Override
    public void cellUpdate() {
        markDirty();
        update = true;
    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity());
    }

    AENetworkProxy gridProxy;

    @Override
    public AENetworkProxy getProxy() {

        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", visualStack(), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    private ItemStack visualStack() {
        return new ItemStack(GregTechAPI.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID());
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
        // onColorChangeServer(aBaseMetaTileEntity.getColorization());
        post();
    }

    boolean wasActive;
    boolean update;
    int rep;

    public void updateStatus() {

        post();

    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {

        boolean active = this.getProxy()
            .isActive();
        if (!aBaseMetaTileEntity.getWorld().isRemote) {
            if (aTick % 40 == 1) {
                post();
            }

            if (rep > 0) {
                rep--;
                update = true;
            }
            if (this.getBaseMetaTileEntity()
                .hasInventoryBeenModified()) {
                update = true;
                rep = 1;
            } ;

            if (update) {
                update = false;
                updateStatus();
            }
            if (wasActive != active) {
                wasActive = active;

                try {
                    this.getProxy()
                        .getGrid()
                        .postEvent(new MENetworkCellArrayUpdate());
                } catch (GridAccessException e) {

                }
                post();
            }

            if (!aBaseMetaTileEntity.getWorld().isRemote && (aTick & 16) != 0) {
                this.getBaseMetaTileEntity()
                    .setActive(
                        this.getProxy()
                            .isPowered() && active);

            }

        }
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (aBaseMetaTileEntity.getWorld().isRemote) {
            return;
        }

        boolean needToSort = false;
        for (int i = 1; i < mInventory.length; i++) {
            ItemStack is = mInventory[i];
            if (is == null) continue;
            markDirty();
            if (mInventory[getIndex()] == null) {
                mInventory[getIndex()] = is.copy();
                mInventory[i] = null;
            } else if (cap() - is.stackSize >= mInventory[getIndex()].stackSize) {
                mInventory[getIndex()].stackSize += is.stackSize;
                mInventory[i] = null;
            } else {
                int to = Math.min(cap() - mInventory[getIndex()].stackSize, is.stackSize);
                mInventory[getIndex()].stackSize += to;
                mInventory[i].stackSize -= to;
                needToSort = true;
            }
        }
        if (needToSort) updateSlots();// fillStacksIntoFirstSlots();

    }

    private int cap() {

        return 1;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new NBTHatchMEFocus(mName, mDescriptionArray, mTextures);
    }

    private void updateValidGridProxySides() {
        /*
         * if (disabled) {
         * getProxy().setValidSides(EnumSet.noneOf(ForgeDirection.class));
         * return; }
         */
        getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));

    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        getProxy().writeToNBT(aNBT);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        getProxy().readFromNBT(aNBT);
        super.loadNBTData(aNBT);
    }
}
