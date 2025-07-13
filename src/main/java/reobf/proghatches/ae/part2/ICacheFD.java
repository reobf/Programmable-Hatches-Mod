package reobf.proghatches.ae.part2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.glodblock.github.api.FluidCraftAPI;
import com.glodblock.github.common.item.ItemFluidDrop;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.me.cache.GridStorageCache;
import appeng.me.storage.MEInventoryHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.ae.TileFluidDiscretizerMKII;


public interface ICacheFD extends  IGridCache,ICellProvider{
	
	
public class CacheFD implements ICacheFD
{

	

    private final BaseActionSource ownActionSource = new MachineSource(new IActionHost() {
		
		@Override
		public void securityBreak() {
		
			
		}
		
		@Override
		public IGridNode getGridNode(ForgeDirection dir) {
		
			return getActionableNode();
		}
		
		@Override
		public AECableType getCableConnectionType(ForgeDirection dir) {
		
			return AECableType.NONE;
		}
		
		@Override
		public IGridNode getActionableNode() {
			
			return myGrid.getPivot();
		}
	});
    private final FluidDiscretizingInventory fluidDropInv = new FluidDiscretizingInventory();
    private boolean prevActiveState = false;
	private IGrid myGrid;

    public CacheFD(final IGrid g) {
        this.myGrid = g;
    	//gridChanged();
        
    }




    @Override
    @SuppressWarnings("rawtypes")
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
        if (channel == StorageChannel.ITEMS ) {
            return Collections.singletonList(fluidDropInv.invHandler);
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    
    @MENetworkEventSubscribe
    public void afterCacheConstruction(final MENetworkPostCacheConstruction cacheConstruction) {
       
    	gridChanged();
        ((GridStorageCache)myGrid.getCache(IStorageGrid.class)).registerCellProvider(this);
    }
 
    public void gridChanged() {
        IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
        if (fluidGrid != null) {
            fluidGrid.addListener(fluidDropInv, fluidGrid);
        }
      
    }
/*
    @Override
    public void saveChanges(@SuppressWarnings("rawtypes") IMEInventory cellInventory) {
      
    }*/

    private IMEMonitor<IAEFluidStack> getFluidGrid() {
      
            return myGrid.<IStorageGrid>getCache(IStorageGrid.class).getFluidInventory();
       
    }

    private IEnergyGrid getEnergyGrid() {
        return myGrid.getCache(IEnergyGrid.class);
    }

    private void updateState() {
        
               // getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
           
    }

    @MENetworkEventSubscribe
    public void onPowerUpdate(MENetworkPowerStatusChange event) {
        updateState();
    }

    @MENetworkEventSubscribe
    public void onChannelUpdate(MENetworkChannelsChanged event) {
        updateState();
    }

    @MENetworkEventSubscribe
    public void onStorageUpdate(MENetworkStorageEvent event) {
        updateState();
    }

    private class FluidDiscretizingInventory
            implements IMEInventory<IAEItemStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {

        private final MEInventoryHandler<IAEItemStack> invHandler = new MEInventoryHandler<>(this, getChannel());
        private IItemList<IAEItemStack> itemCache = null;

        FluidDiscretizingInventory() {
            invHandler.setPriority(Integer.MAX_VALUE);
        }
        private boolean checkSameGrid(BaseActionSource src){
        	IActionHost msrc = null;
        	if(src instanceof PlayerSource){
        		msrc=((PlayerSource) src).via;
        	}else
        	if(src instanceof MachineSource){
        		msrc=((MachineSource) src).via;
        	}else{}
        	
        	
        	if(msrc!=null){
        		return msrc.getActionableNode().getGrid()==myGrid;
			}
        	
        	
        	return true;
        }
        @Override
        public IAEItemStack injectItems(IAEItemStack request, Actionable type, BaseActionSource src) {
            IAEFluidStack fluidStack = ItemFluidDrop.getAeFluidStack(request);
            if (fluidStack == null) {
                return request;
            }
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return request;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return request;
            }
            if (!checkSameGrid(src)) {
            	
                return request;
                
            }
            if (type == Actionable.SIMULATE) {
                return ItemFluidDrop.newAeStack(fluidGrid.injectItems(fluidStack.copy(), Actionable.SIMULATE, src));
            } else {
                return ItemFluidDrop.newAeStack(fluidGrid.injectItems(fluidStack.copy(), Actionable.MODULATE, src));
            }
        }

        @Override
        public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src) {
            IAEFluidStack fluidStack = ItemFluidDrop.getAeFluidStack(request);
            if (fluidStack == null) {
                return null;
            }
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return null;
            }
            IEnergyGrid energyGrid = getEnergyGrid();
            if (energyGrid == null) {
                return null;
            }
            if (!checkSameGrid(src)) {
            	
                return null;
                
            }
            if (mode == Actionable.SIMULATE) {
                return ItemFluidDrop.newAeStack(fluidGrid.extractItems(fluidStack.copy(), Actionable.SIMULATE, src));
            } else {
                return ItemFluidDrop.newAeStack(fluidGrid.extractItems(fluidStack.copy(), Actionable.MODULATE, src));
            }
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out, int iteration) {
        	if(TileFluidDiscretizerMKII.count>0){return out;}
        	if (itemCache == null) {
                itemCache = AEApi.instance().storage().createItemList();
                IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
                if (fluidGrid != null) {
                    for (IAEFluidStack fluid : fluidGrid.getStorageList()) {
                        IAEItemStack stack = ItemFluidDrop.newAeStack(fluid);
                        if (stack != null
                                && !FluidCraftAPI.instance().isBlacklistedInDisplay(fluid.getFluid().getClass())) {
                            itemCache.add(stack);
                        }
                    }
                }
            }
            for (IAEItemStack stack : itemCache) {
                out.addStorage(stack);
            }
            return out;
        }
       
        @Override
        public IAEItemStack getAvailableItem(@Nonnull IAEItemStack request, int iteration) {
        	if(TileFluidDiscretizerMKII.count>0){return null;}
        	IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            if (fluidGrid == null) {
                return null;
            }
            IAEFluidStack fluidRequest = ItemFluidDrop.getAeFluidStack(request);
            if (fluidRequest == null) {
                return null;
            }
            IAEFluidStack availableFluid = fluidGrid.getAvailableItem(fluidRequest, iteration);
            if (availableFluid == null || availableFluid.getFluid() == null
                    || FluidCraftAPI.instance().isBlacklistedInDisplay(availableFluid.getFluid().getClass())) {
                return null;
            }
            return ItemFluidDrop.newAeStack(availableFluid);
        }

        @Override
        public StorageChannel getChannel() {
            return StorageChannel.ITEMS;
        }

        @Override
        public boolean isValid(Object verificationToken) {
            IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
            return fluidGrid != null && fluidGrid == verificationToken;
        }

        @Override
        public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change,
                BaseActionSource actionSource) {
            itemCache = null;
            List<IAEItemStack> mappedChanges = new ArrayList<>();
			for (IAEFluidStack fluidStack : change) {
			    IAEItemStack itemStack = ItemFluidDrop.newAeStack(fluidStack);
			    if (itemStack != null
			            && !FluidCraftAPI.instance().isBlacklistedInDisplay(fluidStack.getFluid().getClass())) {
			        mappedChanges.add(itemStack);
			    }
			}
			myGrid.<IStorageGrid>getCache(IStorageGrid.class)
			        .postAlterationOfStoredItems(getChannel(), mappedChanges, ownActionSource);
        }

        @Override
        public void onListUpdate() {
            // NO-OP
        }
    }

	@Override
	public void onUpdateTick() {
	
		
	}




	@Override
	public void removeNode(IGridNode gridNode, IGridHost machine) {
		gridChanged();
		
	}




	@Override
	public void addNode(IGridNode gridNode, IGridHost machine) {
		gridChanged();
		
	}




	@Override
	public void onSplit(IGridStorage destinationStorage) {
		gridChanged();
	}




	@Override
	public void onJoin(IGridStorage sourceStorage) {
		gridChanged();
		
	}




	@Override
	public void populateGridStorage(IGridStorage destinationStorage) {
		gridChanged();
		
	}

	
	
	
	
}

}
