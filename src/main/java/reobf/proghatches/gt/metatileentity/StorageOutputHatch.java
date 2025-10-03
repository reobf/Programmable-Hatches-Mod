package reobf.proghatches.gt.metatileentity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Unique;

import com.glodblock.github.common.item.FCBaseItemCell;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.item.AEFluidStack;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.MTEHatchOutputME;
import reobf.proghatches.gt.metatileentity.multi.IngredientDistributor;
import reobf.proghatches.gt.metatileentity.util.IStoageCellUpdate;
import reobf.proghatches.main.registration.Registration;
import tectech.util.TTUtility;

public class StorageOutputHatch extends MTEHatchOutputME
    implements ICellContainer, IGridProxyable, IStoageCellUpdate, IPowerChannelState {
	public  void dump(){
		IMEMonitor<IAEFluidStack> inv;
		try {
			inv = getProxy().getStorage().getFluidInventory();
		
		itemCache.forEach(s->{
			long old=s.getStackSize();
			IAEFluidStack rest = inv.injectItems(s, Actionable.MODULATE, new MachineSource(this));
			if(rest==null){s.setStackSize(0);}
			else{
				s.setStackSize(rest.getStackSize());
			}
			long neo=s.getStackSize();
			 try {
					this.getProxy()
					.getStorage()
					.postAlterationOfStoredItems(
					    StorageChannel.FLUIDS,
					    ImmutableList.of(s.copy().setStackSize(neo-old)),
					    new MachineSource(this));
				} catch (GridAccessException e) {}
		});
		} catch (GridAccessException e) {}
		
		
		
	}
	@Override
		public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
				ItemStack aTool) {
		
		dump();
		aPlayer.addChatMessage(new ChatComponentText("Dumped"));
		
		}

    public StorageOutputHatch(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
        TTUtility.setTier(5, this);
    }

    @Override
    public String[] getDescription() {

        return mydesc;
    }

    String[] mydesc = reobf.proghatches.main.Config.get("SOH", ImmutableMap.of());

    public StorageOutputHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);

    }

    boolean wasActive;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {

        if (getBaseMetaTileEntity().isServerSide()) {
            tickCounter = aTick;
        }
        boolean active = this.getProxy()
            .isActive();
        if (!aBaseMetaTileEntity.getWorld().isRemote) {
            if (facingJustChanged) {
                facingJustChanged = false;
                try {
                    this.getProxy()
                        .getGrid()
                        .postEvent(new MENetworkCellArrayUpdate());
                } catch (GridAccessException e) {}
                post();

            }
            if (wasActive != active) {
                wasActive = active;

                try {
                    this.getProxy()
                        .getGrid()
                        .postEvent(new MENetworkCellArrayUpdate());
                } catch (GridAccessException e) {

                }
            }
        }
        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    private void post() {
        // TODO Auto-generated method stub

    }

    boolean additionalConnection;

    private void updateValidGridProxySides() {

        if (additionalConnection) {
            getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));
        } else {
            getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
        }
    }

    AENetworkProxy gridProxy;

    @Override
    public AENetworkProxy getProxy() {

        if (gridProxy == null) {
            if (getBaseMetaTileEntity() instanceof IGridProxyable) {
                gridProxy = new AENetworkProxy(this, "proxy", ItemList.Hatch_Output_Bus_ME.get(1), true);
                gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
                updateValidGridProxySides();
                if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                    getBaseMetaTileEntity().getWorld()
                        .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
            }
        }
        return this.gridProxy;
    }

    private long getCachedAmount() {
        long fluidAmount = 0;
        for (IAEFluidStack fluid : itemCache) {
            fluidAmount += fluid.getStackSize();
        }
        return fluidAmount;
    }

    private long getCacheCapacity() {
        ItemStack upgradeFluidStack = mInventory[0];
        if (upgradeFluidStack != null && upgradeFluidStack.getItem() instanceof IStorageFluidCell) {
            return ((FCBaseItemCell) upgradeFluidStack.getItem()).getBytes(upgradeFluidStack) * 2048;
        }
        return 128_000;
    }

    /**
     * Check if the internal cache can still fit more fluids in it
     */
    /*
     * public boolean canAcceptItem() {
     * return getCachedAmount() < getCacheCapacity() || lastInputTick == tickCounter;
     * }
     */
    @Override
    public boolean canAcceptFluid() {
        return getCachedAmount() < getCacheCapacity() || lastInputTick == tickCounter;
    }

    @Override
    public boolean canFillFluid() {
        return canAcceptFluid() || lastInputTick == tickCounter;
    }

    long lastInputTick, tickCounter;

    public int tryFillAE(final FluidStack stack) {

        if (canAcceptFluid() || (lastInputTick == tickCounter)) {

            try {
                AEFluidStack is = AEFluidStack.create(stack);

                is = (AEFluidStack) ItemFluidDrop.getAeFluidStack(
                    (IAEItemStack) ((CraftingGridCache) getProxy().getCrafting())
                        .injectItems(ItemFluidDrop.newAeStack(is), Actionable.MODULATE, new MachineSource(this)));
                if (is != null) {
                  
                    itemCache.addStorage(is);
                }
            } catch (GridAccessException e) {

            }

            lastInputTick = tickCounter;
            return 0;
        }
        return stack.amount;
    }

    @Override
    public IGridNode getActionableNode() {
        return getProxy().getNode();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return getProxy().getNode();
    }

    @Override
    public void securityBreak() {

    }

    final IItemList<IAEFluidStack> itemCache = AEApi.instance()
        .storage()
        .createFluidList();
    IMEInventory<AEFluidStack> cache = new IMEInventory<AEFluidStack>() {

        @Override
        public AEFluidStack injectItems(AEFluidStack input, Actionable type, BaseActionSource src) {

            if (IngredientDistributor.flag) {
                if (type == Actionable.SIMULATE) {
                    return null;
                }
                AEFluidStack ret = (AEFluidStack) input.copy()
                    .setStackSize(tryFillAE(input.getFluidStack()));
                if (ret.getStackSize() <= 0) ret = null;
                return ret;
            }
            return input;
        }

        @Override
        public AEFluidStack extractItems(AEFluidStack request, Actionable mode, BaseActionSource src) {
            AEFluidStack get = (AEFluidStack) itemCache.findPrecise(request);
            if (get == null) {
                return null;
            }
            long t = Math.min(get.getStackSize(), request.getStackSize());
            if (mode == Actionable.MODULATE) get.decStackSize(t);

            return (AEFluidStack) get.copy()
                .setStackSize(t);
        }

        public IItemList<AEFluidStack> getAvailableItems(IItemList<AEFluidStack> out) {
            itemCache.forEach(s -> out.addStorage((AEFluidStack) s.copy()));
            return out;
        };

        public AEFluidStack getAvailableItem(AEFluidStack request) {

            return (AEFluidStack) itemCache.findPrecise(request);
        };

        @Override
        public StorageChannel getChannel() {

            return StorageChannel.FLUIDS;
        }
    };

    @SuppressWarnings({ "rawtypes", "unchecked" })
    IMEInventoryHandler<AEFluidStack> handler = new MEInventoryHandler(cache, StorageChannel.FLUIDS);

    @Override
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {

        if (channel == StorageChannel.FLUIDS) return ImmutableList.of(handler);
        else return ImmutableList.of();
    }

    @Override
    public int getPriority() {

        return 0;
    }

    @Override
    public void saveChanges(IMEInventory cellInventory) {

    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new StorageOutputHatch(mName, mTier, mDescriptionArray, mTextures);
    }

    DimensionalCoord coord;

    @Override
    public DimensionalCoord getLocation() {
        if (coord == null) coord = new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity());
        return coord;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        NBTTagList fluids = new NBTTagList();
        for (IAEFluidStack s : itemCache) {
            if (s.getStackSize() == 0) continue;
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagCompound tagFluidStack = new NBTTagCompound();
            s.getFluidStack()
                .writeToNBT(tagFluidStack);
            tag.setTag("fluidStack", tagFluidStack);
            tag.setLong("size", s.getStackSize());
            fluids.appendTag(tag);
        }
        aNBT.setBoolean("additionalConnectionPH", additionalConnection);
        aNBT.setTag("cachedFluidsPH", fluids);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {

        NBTBase t = aNBT.getTag("cachedFluidsPH");
        if (t instanceof NBTTagList) {
            NBTTagList l = (NBTTagList) t;
            for (int i = 0; i < l.tagCount(); ++i) {
                NBTTagCompound tag = l.getCompoundTagAt(i);
                NBTTagCompound tagFluidStack = tag.getCompoundTag("fluidStack");
                final IAEFluidStack s = AEApi.instance()
                    .storage()
                    .createFluidStack(GTUtility.loadFluid(tagFluidStack));
                if (s != null) {
                    s.setStackSize(tag.getLong("size"));
                    itemCache.add(s);
                } else {
                    GTMod.GT_FML_LOGGER.warn(
                        "An error occurred while loading contents of ME Output Hatch. This fluid has been voided: "
                            + tagFluidStack);
                }
            }
        }
        additionalConnection = aNBT.getBoolean("additionalConnectionPH");
        super.loadNBTData(aNBT);
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        // store(new FluidStack(Items.apple));
        return super.onRightclick(aBaseMetaTileEntity, aPlayer);
    }

    boolean facingJustChanged;

    @Override
    public void onFacingChange() {
        updateValidGridProxySides();
        facingJustChanged = true;
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.setLong("cacheCapacity", getCacheCapacity());
        tag.setInteger("stackCount", itemCache.size());

        IAEFluidStack[] stacks = itemCache.toArray(new IAEFluidStack[0]);

        Arrays.sort(
            stacks,
            Comparator.comparingLong(IAEFluidStack::getStackSize)
                .reversed());

        if (stacks.length > 10) {
            stacks = Arrays.copyOf(stacks, 10);
        }

        NBTTagList tagList = new NBTTagList();
        tag.setTag("stacks", tagList);

        for (IAEFluidStack stack : stacks) {
            NBTTagCompound stackTag = new NBTTagCompound();
            stack.getFluidStack()
                .writeToNBT(stackTag);
            stackTag.setLong("Amount", stack.getStackSize());
            tagList.appendTag(stackTag);
        }
    }

    @MENetworkEventSubscribe
    @Unique
    public void powerRender(final MENetworkPowerStatusChange w) {

        cellUpdate();

    }

    @MENetworkEventSubscribe
    @Unique
    public void updateChannels(final MENetworkChannelsChanged w) {
        cellUpdate();

    }

    @Override
    public void cellUpdate() {
        try {
            this.getProxy()
                .getGrid()
                .postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException e) {}

    }

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack is) {
        additionalConnection = !additionalConnection;
        updateValidGridProxySides();
        aPlayer.addChatComponentMessage(
            new ChatComponentTranslation("GT5U.hatch.additionalConnection." + additionalConnection));
        return true;
    }

    @Override
    public boolean isPowered() {

        return getProxy().isPowered();
    }

    @Override
    public boolean isActive() {

        return getProxy().isActive();
    }
}
