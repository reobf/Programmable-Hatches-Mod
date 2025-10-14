package reobf.proghatches.gt.metatileentity;

import java.lang.reflect.Field;
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

import org.spongepowered.asm.mixin.Unique;

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
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.item.AEItemStack;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.IOutputBus;
import gregtech.api.interfaces.IOutputBusTransaction;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.MTEHatchOutputBusME;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import reobf.proghatches.gt.metatileentity.multi.IngredientDistributor;
import reobf.proghatches.gt.metatileentity.util.IStoageCellUpdate;
import reobf.proghatches.main.registration.Registration;
import tectech.util.TTUtility;

public class StorageOutputBus extends MTEHatchOutputBusME
    implements ICellContainer, IGridProxyable, IStoageCellUpdate, IPowerChannelState {
public  void dump(){
	IMEMonitor<IAEItemStack> inv;
	try {
		inv = getProxy().getStorage().getItemInventory();
	
	itemCache.forEach(s->{
		long old=s.getStackSize();
		IAEItemStack rest = inv.injectItems(s, Actionable.MODULATE, new MachineSource(this));
		if(rest==null){s.setStackSize(0);}
		else{
			s.setStackSize(rest.getStackSize());
		}
		long neo=s.getStackSize();
		 try {
				this.getProxy()
				.getStorage()
				.postAlterationOfStoredItems(
				    StorageChannel.ITEMS,
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


    public StorageOutputBus(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
        TTUtility.setTier(5, this);
    }

    @Override
    public String[] getDescription() {

        return mydesc;
    }

    String[] mydesc = reobf.proghatches.main.Config.get("SOB", ImmutableMap.of());

    public StorageOutputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
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

    public void updateValidGridProxySides() {

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

    public long getCachedAmount() {
        long itemAmount = 0;
        for (IAEItemStack item : itemCache) {
            itemAmount += item.getStackSize();
        }
        return itemAmount;
    }

    private static final long DEFAULT_CAPACITY = 1_600;

    public long getCacheCapacity() {
        ItemStack upgradeItemStack = mInventory[0];
        if (upgradeItemStack != null && upgradeItemStack.getItem() instanceof ItemBasicStorageCell) {
            return ((ItemBasicStorageCell) upgradeItemStack.getItem()).getBytesLong(upgradeItemStack) * 8;
        }
        return DEFAULT_CAPACITY;
    }

    public boolean canAcceptItem() {
        return getCachedAmount() < getCacheCapacity();
    }

    long lastInputTick, tickCounter;

    public int store(final ItemStack stack) {

        if (canAcceptItem() || (lastInputTick == tickCounter)) {

            try {
                AEItemStack is = AEItemStack.create(stack);
                is = (AEItemStack) ((CraftingGridCache) getProxy().getCrafting())
                    .injectItems(is, Actionable.MODULATE, new MachineSource(this));
                if (is != null) {
                    /*this.getProxy()
                        .getStorage()
                        .postAlterationOfStoredItems(
                            StorageChannel.ITEMS,
                            ImmutableList.of(is),
                            new MachineSource(this));*/
                    itemCache.addStorage(is);
                }
            } catch (GridAccessException e) {

            }

            lastInputTick = tickCounter;
            return 0;
        }
        return stack.stackSize;
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

    final IItemList<IAEItemStack> itemCache = AEApi.instance()
        .storage()
        .createItemList();
    IMEInventory<AEItemStack> cache = new IMEInventory<AEItemStack>() {

        @Override
        public AEItemStack injectItems(AEItemStack input, Actionable type, BaseActionSource src) {

            if (IngredientDistributor.flag) {
                if (type == Actionable.SIMULATE) {
                    return null;
                }
                AEItemStack ret = (AEItemStack) input.copy()
                    .setStackSize(store(input.getItemStack()));
                if (ret.getStackSize() <= 0) ret = null;
                return ret;
            }
            return input;
        }

        @Override
        public AEItemStack extractItems(AEItemStack request, Actionable mode, BaseActionSource src) {
            AEItemStack get = (AEItemStack) itemCache.findPrecise(request);
            if (get == null) {
                return null;
            }
            long t = Math.min(get.getStackSize(), request.getStackSize());
            if (mode == Actionable.MODULATE) get.decStackSize(t);

            return (AEItemStack) get.copy()
                .setStackSize(t);
        }

        public IItemList<AEItemStack> getAvailableItems(IItemList<AEItemStack> out) {
            itemCache.forEach(s -> out.addStorage((AEItemStack) s.copy()));
            return out;
        };

        public AEItemStack getAvailableItem(AEItemStack request) {

            return (AEItemStack) itemCache.findPrecise(request);
        };

        @Override
        public StorageChannel getChannel() {

            return StorageChannel.ITEMS;
        }
    };

    @SuppressWarnings({ "rawtypes", "unchecked" })
    IMEInventoryHandler<AEItemStack> handler = new MEInventoryHandler(cache, StorageChannel.ITEMS);

    @Override
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {

        if (channel == StorageChannel.ITEMS) return ImmutableList.of(handler);
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

        return new StorageOutputBus(mName, mTier, mDescriptionArray, mTextures);
    }

    DimensionalCoord coord;

    @Override
    public DimensionalCoord getLocation() {
        if (coord == null) coord = new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity());
        return coord;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        NBTTagList items = new NBTTagList();
        for (IAEItemStack s : itemCache) {
            if (s.getStackSize() == 0) continue;
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("itemStack", GTUtility.saveItem(s.getItemStack()));
            tag.setLong("size", s.getStackSize());
            items.appendTag(tag);
        }
        aNBT.setBoolean("additionalConnectionPH", additionalConnection);
        aNBT.setTag("cachedItemsPH", items);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        NBTBase t = aNBT.getTag("cachedItemsPH");
        if (t instanceof NBTTagList) {
            NBTTagList l = (NBTTagList) t;
            for (int i = 0; i < l.tagCount(); ++i) {
                NBTTagCompound tag = l.getCompoundTagAt(i);
                NBTTagCompound tagItemStack = tag.getCompoundTag("itemStack");
                final IAEItemStack s = AEApi.instance()
                    .storage()
                    .createItemStack(GTUtility.loadItem(tagItemStack));
                if (s != null) {
                    s.setStackSize(tag.getLong("size"));
                    itemCache.add(s);
                } else {
                    GTMod.GT_FML_LOGGER.warn(
                        "An error occurred while loading contents of ME Output Bus. This item has been voided: "
                            + tagItemStack);
                }
            }
        }
        additionalConnection = aNBT.getBoolean("additionalConnectionPH");
        super.loadNBTData(aNBT);
    }/*
      * @Override
      * public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
      * store(new ItemStack(Items.apple));
      * return super.onRightclick(aBaseMetaTileEntity, aPlayer);
      * }
      */

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

        IAEItemStack[] stacks = itemCache.toArray(new IAEItemStack[0]);

        Arrays.sort(
            stacks,
            Comparator.comparingLong(IAEItemStack::getStackSize)
                .reversed());

        if (stacks.length > 10) {
            stacks = Arrays.copyOf(stacks, 10);
        }

        NBTTagList tagList = new NBTTagList();
        tag.setTag("stacks", tagList);

        for (IAEItemStack stack : stacks) {
            NBTTagCompound stackTag = new NBTTagCompound();
            stack.writeToNBT(stackTag);
            tagList.appendTag(stackTag);
        }
    }

    @Override
    public void cellUpdate() {
        try {
            this.getProxy()
                .getGrid()
                .postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException e) {}

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
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack is) {
        additionalConnection = !additionalConnection;
        updateValidGridProxySides();
        aPlayer.addChatComponentMessage(
            new ChatComponentTranslation("GT5U.hatch.additionalConnection." + additionalConnection));
        return true;
    }

    static Field f1;
    static {

        try {
            f1 = MTEHatchOutputBusME.class.getDeclaredField("lockedItems");
            f1.setAccessible(true);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public List<ItemStack> get() {
        try {
            return (List<ItemStack>) f1.get(this);
        } catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    @Override
    public boolean storePartial(ItemStack stack, boolean simulate) {
        if (!get().isEmpty()) {
            boolean isOk = false;

            for (ItemStack lockedItem : get()) {
                if (lockedItem.isItemEqual(stack)) {
                    isOk = true;

                    break;
                }
            }

            if (!isOk) {
                return false;
            }
        }

        // Always allow insertion on the same tick so we can output the entire recipe
        if (canAcceptItem() || (lastInputTick == tickCounter)) {
            if (!simulate) {
            	  try {
      				AEItemStack rem = (AEItemStack) ((CraftingGridCache) getProxy().getCrafting())
      				        .injectItems(AEItemStack.create(stack), Actionable.MODULATE, new MachineSource(StorageOutputBus.this));
      				stack.stackSize=(int) (rem==null?0:rem.getStackSize());
                  } catch (GridAccessException e1) {
      			}
                itemCache.add(
                    AEApi.instance()
                        .storage()
                        .createItemStack(stack));
                try {
					this.getProxy()
					.getStorage()
					.postAlterationOfStoredItems(
					    StorageChannel.ITEMS,
					    ImmutableList.of(AEItemStack.create(stack)),
					    new MachineSource(this));
				} catch (GridAccessException e) {}
					
				
                lastInputTick = tickCounter;
            }
            stack.stackSize = 0;
            return true;
        }

        return false;
    }

    @Override
    public boolean isPowered() {

        return getProxy().isPowered();
    }

    @Override
    public boolean isActive() {

        return getProxy().isActive();
    }
    
    
    @Override
    	public IOutputBusTransaction createTransaction() {
    		
    		return new MEOutputBusTransactionX();
    	}
    @Override
    	protected void flushCachedStack() {
    		//NO we don't
    		//super.flushCachedStack();
    	}
    
    class MEOutputBusTransactionX implements IOutputBusTransaction {

        private final Object2LongOpenHashMap<GTUtility.ItemId> pendingItems = new Object2LongOpenHashMap<>();
        private final long initialStored, capacity, tick;
        private long currentStored;
        private boolean active = true;

        public MEOutputBusTransactionX() {
            initialStored = getCachedAmount();
            capacity = getCacheCapacity();
            // We don't want to mutate lastInputTick, so we'll keep a simulated version of it.
            // This transaction assumes that something will be ejected into this bus, so we can just use the current
            // tick if this bus still has space.
            tick = initialStored >= capacity ? lastInputTick : tickCounter;
        }

        @Override
        public IOutputBus getBus() {
            return StorageOutputBus.this;
        }

        @Override
        public boolean hasAvailableSpace() {
            // There's really no reason for the tick counter, it's just more accurate to the real bus's behaviour.
            // Transactions should never be kept around long enough for it to matter, but in case someone does something
            // stupid it's here to make sure nothing breaks.
            // This condition should always return true unless this transaction is kept around for more than one tick.
            return initialStored + currentStored < capacity || tickCounter == tick;
        }

        @Override
        public boolean storePartial(GTUtility.ItemId id, ItemStack stack) {
            if (!active) throw new IllegalStateException("Cannot add to a transaction after committing it");

            if (!hasAvailableSpace()) return false;
            if (isFiltered() && !isFilteredToItem(id)) return false;
            
            try {
				AEItemStack rem = (AEItemStack) ((CraftingGridCache) getProxy().getCrafting())
				        .injectItems(AEItemStack.create(stack), Actionable.MODULATE, new MachineSource(StorageOutputBus.this));
				stack.stackSize=(int) (rem==null?0:rem.getStackSize());
            } catch (GridAccessException e1) {
			}
            
            
            
            pendingItems.addTo(id, stack.stackSize);
            currentStored += stack.stackSize;
            
            
            
            itemCache.add(
                    AEApi.instance()
                        .storage()
                        .createItemStack(stack));
            
            
            try {
					getProxy()
					.getStorage()
					.postAlterationOfStoredItems(
					    StorageChannel.ITEMS,
					    ImmutableList.of(AEItemStack.create(stack)),
					    new MachineSource(StorageOutputBus.this));
				} catch (GridAccessException e) {}
					
				
                lastInputTick = tickCounter;
            
            
            stack.stackSize = 0;

            return true;
        }

        @Override
        public void completeItem(GTUtility.ItemId id) {
            // Do nothing
        }

        @Override
        public void commit() {
            // spotless:off
            Object2LongMaps.fastForEach(pendingItems, e -> {
                itemCache.add(AEItemStack.create(e.getKey().getItemStack()).setStackSize(e.getLongValue()));
            });
            // spotless:on

            StorageOutputBus.this.markDirty();

            active = false;
        }
    }
    
    
    
}
