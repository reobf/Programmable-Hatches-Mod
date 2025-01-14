package reobf.proghatches.gt.metatileentity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.PrecisePriorityList;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.MTEHatchOutputBusME;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.gt.metatileentity.SuperChestME.UnlimitedWrapper;
import reobf.proghatches.main.registration.Registration;
import tectech.util.TTUtility;

public class StorageOutputBus extends MTEHatchOutputBusME implements ICellContainer,IGridProxyable {

	public StorageOutputBus(int aID, String aName, String aNameRegional) {
		super(aID, aName, aNameRegional);
		Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
		TTUtility.setTier(5,this);
	}
@Override
public String[] getDescription() {
	
	return mydesc;
}
String[] mydesc=reobf.proghatches.main.Config.get("SOB", ImmutableMap.of());

	public StorageOutputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
		super(aName, aTier, aDescription, aTextures);

	}
boolean wasActive;
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		boolean active=this.getProxy().isActive();
		if(!aBaseMetaTileEntity.getWorld().isRemote){
		if(wasActive!=active){
			wasActive=active;
			
			try {
				this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
			} catch (GridAccessException e) {
				
			}
		}
		}
		super.onPostTick(aBaseMetaTileEntity, aTick);
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
				gridProxy = new AENetworkProxy( this, "proxy", ItemList.Hatch_Output_Bus_ME.get(1),
						true);
				gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
				updateValidGridProxySides();
				if (getBaseMetaTileEntity().getWorld() != null)
					gridProxy.setOwner(getBaseMetaTileEntity().getWorld()
							.getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
			}
		}
		return this.gridProxy;
	}
    private long getCachedAmount() {
        long itemAmount = 0;
        for (IAEItemStack item : itemCache) {
            itemAmount += item.getStackSize();
        }
        return itemAmount;
    }
    private static final long DEFAULT_CAPACITY = 1_600;
    private long getCacheCapacity() {
        ItemStack upgradeItemStack = mInventory[0];
        if (upgradeItemStack != null && upgradeItemStack.getItem() instanceof ItemBasicStorageCell) {
            return ((ItemBasicStorageCell) upgradeItemStack.getItem()).getBytesLong(upgradeItemStack) * 8;
        }
        return DEFAULT_CAPACITY;
    }

   
    public boolean canAcceptItem() {
        return getCachedAmount() < getCacheCapacity();
    }
 int lastInputTick,tickCounter;
	public int store(final ItemStack stack) {
		
		if (canAcceptItem() || (lastInputTick == tickCounter)) {
		
		try {
			AEItemStack is = AEItemStack.create(stack);
			is= (AEItemStack) ((CraftingGridCache) getProxy().getCrafting())
					.injectItems(is, Actionable.MODULATE, new MachineSource(this));
					
			this.getProxy().getStorage()
			.postAlterationOfStoredItems(StorageChannel.ITEMS, 
					ImmutableList.of(is)
					,new MachineSource(this));
		itemCache.addStorage(is);
		
		
		} catch (GridAccessException e) {
			
		} 
		
		lastInputTick = tickCounter;
		
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
	IMEInventory<AEItemStack> cache=
	new IMEInventory<AEItemStack>() {

		@Override
		public AEItemStack injectItems(AEItemStack input, Actionable type, BaseActionSource src) {
			return input;
		}

		@Override
		public AEItemStack extractItems(AEItemStack request, Actionable mode, BaseActionSource src) {
			AEItemStack get = (AEItemStack) itemCache.findPrecise(request);
			if(get==null){return null;}
			long t=Math.min(get.getStackSize(),request.getStackSize());
			if(mode==Actionable.MODULATE)get.decStackSize(t);
			
			return  (AEItemStack) get.copy().setStackSize(t);
		}

		public IItemList<AEItemStack> getAvailableItems(
				IItemList<AEItemStack> out) {
			itemCache.forEach(s->out.addStorage((AEItemStack) s));
			return out;
		};

		public AEItemStack getAvailableItem(AEItemStack request) {
			
			return (AEItemStack) itemCache.findPrecise(request);
		};

		@Override
		public StorageChannel getChannel() {

			return StorageChannel.ITEMS;
		}};
	
	IMEInventoryHandler<AEItemStack> handler = new MEInventoryHandler(cache, StorageChannel.ITEMS);
	
	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {

		if(channel==StorageChannel.ITEMS)
			return ImmutableList.of(handler);
			else
				return ImmutableList.of();
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
		if(coord==null)coord=new DimensionalCoord((TileEntity)this.getBaseMetaTileEntity());
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
   
    aNBT.setTag("cachedItemsPH", items);
	super.saveNBTData(aNBT);
}



@Override
public void loadNBTData(NBTTagCompound aNBT) {
	 NBTBase t = aNBT.getTag("cachedItemsPH");
     if (t instanceof NBTTagList) {
    	 NBTTagList l=(NBTTagList) t;
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
	super.loadNBTData(aNBT);
}/*
@Override
public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
	store(new ItemStack(Items.apple));
	return super.onRightclick(aBaseMetaTileEntity, aPlayer);
}*/

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
}
