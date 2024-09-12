package reobf.proghatches.eucrafting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.common.Config;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.PartFluidExportBus;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.parts.PartFluidP2PInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.inventory.IDualHost;

import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;

import appeng.api.config.FuzzyMode;
import appeng.api.config.InsertionMode;
import appeng.api.config.Upgrades;
import appeng.api.parts.IPart;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.p2p.PartP2PLiquids;
import appeng.tile.misc.TileInterface;
import appeng.tile.networking.TileCableBus;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import cofh.api.transport.IItemDuct;
import crazypants.enderio.conduit.item.IItemConduit;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaPipeEntity;

public class CoverToMachineAdaptor<T extends TileEntity&ICoverable> extends InventoryAdaptor {
	private static class SlotIterator implements Iterator<ItemSlot> {

        private final FluidTankInfo[] tanks;
        private final Iterator<ItemSlot> itemSlots;
        private int nextSlotIndex = 0;

        SlotIterator(FluidTankInfo[] tanks, Iterator<ItemSlot> itemSlots) {
            this.tanks = tanks;
            this.itemSlots = itemSlots;
        }

        @Override
        public boolean hasNext() {
            return itemSlots.hasNext() || nextSlotIndex < tanks.length;
        }

        @Override
        public ItemSlot next() {
            if (nextSlotIndex < tanks.length) {
                FluidStack fluid = tanks[nextSlotIndex].fluid;
                ItemSlot slot = new ItemSlot();
                slot.setSlot(nextSlotIndex++);
                slot.setItemStack(fluid != null ? ItemFluidPacket.newStack(fluid) : null);
                Ae2Reflect.setItemSlotExtractable(slot, false);
                return slot;
            } else {
                ItemSlot slot = itemSlots.next();
                slot.setSlot(nextSlotIndex++);
                return slot;
            }
        }
    }
	public CoverToMachineAdaptor(T te,@Nullable InventoryAdaptor invItems,
            @Nullable IFluidHandler invFluids,ForgeDirection fd){
		
		  this.invItems = invItems;
	        this.invFluids = invFluids;
		this.te=te;
		this.fd=fd;
	}T te;ForgeDirection fd;    private final InventoryAdaptor invItems;
    private final IFluidHandler invFluids;
	@Override
	public Iterator<ItemSlot> iterator() {
        FluidTankInfo[] info = null;
        if (invFluids != null) {
            info = invFluids.getTankInfo(fd);
        }
        // Null check is needed because some tank infos return null (EIO conduits...)
        if (info == null) {
            info = new FluidTankInfo[0];
        }
        return new SlotIterator(info, invItems != null ? invItems.iterator() : Collections.emptyIterator());
 
	}

	@Override
	public ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination) {
		 return invItems != null ? invItems.removeItems(amount, filter, destination) : null;
	}

	 @Override
	    public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination) {
	        return invItems != null ? invItems.simulateRemove(amount, filter, destination) : null;
	    }

	    @Override
	    public ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode,
	            IInventoryDestination destination) {
	        return invItems != null ? invItems.removeSimilarItems(amount, filter, fuzzyMode, destination) : null;
	    }

	    @Override
	    public ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode,
	            IInventoryDestination destination) {
	        return invItems != null ? invItems.simulateSimilarRemove(amount, filter, fuzzyMode, destination) : null;
	    }
	    
	    private int checkItemFluids(IFluidHandler tank, InventoryAdaptor inv, ForgeDirection direction) {
	        if (tank == null && inv == null) {
	            return 2;
	        }
	       
	        if (tank != null && tank.getTankInfo(direction) != null) {
	            List<FluidTankInfo[]> tankInfos = new LinkedList<>();
	            {
	                tankInfos.add(tank.getTankInfo(direction));
	            }
	            boolean hasTank = false;
	            for (FluidTankInfo[] tankInfoArray : tankInfos) {
	                for (FluidTankInfo tankInfo : tankInfoArray) {
	                    hasTank = true;
	                    FluidStack fluid = tankInfo.fluid;
	                    if (fluid != null && fluid.amount > 0) {
	                        return 1;
	                    }
	                }
	            }
	            if (!hasTank && inv == null) {
	                return 2;
	            }
	        }
	        
	            return gtMachineCircuitCheck(inv);
	        
	        //return inv != null && inv.containsItems() ? 1 : 0;
	    }
	    
	    
	    private int gtMachineCircuitCheck(InventoryAdaptor ad) {
	        if (ad == null) {
	            return 0;
	        }
	        for (ItemSlot i : ad) {
	            ItemStack is = i.getItemStack();
	            if (is == null || Objects.requireNonNull(is.getItem()).getUnlocalizedName().equals("gt.integrated_circuit"))
	                continue;
	            return 1;
	        }
	        return 0;
	    }
	
	@Override
    public ItemStack addItems(ItemStack toBeAdded) {
        return addItems(toBeAdded, InsertionMode.DEFAULT);
    }
@Override
public ItemStack simulateAdd(ItemStack toBeSimulated) {
    return simulateAdd(toBeSimulated, InsertionMode.DEFAULT);
}
	@Override
	public boolean containsItems() {
		  return checkItemFluids(this.invFluids, this.invItems, this.fd) > 0;
	}
	
	
	@Override
	public ItemStack addItems(ItemStack toBeAdded, InsertionMode insertionMode) {

		 FluidStack fluid = Util.getFluidFromVirtual(toBeAdded);
		 boolean packetOrDrop =toBeAdded.getItem() instanceof ItemFluidPacket;
	            
	            if (fluid != null) {
	                int filled = fillSideFluid(fluid, this.invFluids, this.fd, true);
	                fluid.amount -= filled;
	                return packetOrDrop?
	                		ItemFluidPacket.newStack(fluid)
	                		:ItemFluidDrop.newStack(fluid);
	            } else {
	                ItemStack notFilled = fillSideItem(toBeAdded, this.invItems, insertionMode, true);
	                if (notFilled != null) {
	                  
	                    return notFilled;
	                            
	                }
	                return null;
	            }
	        
	        
	}
		
	@Override
	public ItemStack simulateAdd(ItemStack toBeSimulated, InsertionMode insertionMode) {
		
		 FluidStack fluid = Util.getFluidFromVirtual(toBeSimulated);
	      boolean packetOrDrop =toBeSimulated.getItem() instanceof ItemFluidPacket;
	            
	            if (fluid != null) {
	                int filled = fillSideFluid(fluid, this.invFluids, this.fd, false);
	                fluid.amount -= filled;
	                return packetOrDrop?
	                		ItemFluidPacket.newStack(fluid)
	                		:ItemFluidDrop.newStack(fluid);
	            } else {
	                
	                  return fillSideItem(toBeSimulated, this.invItems, insertionMode, false);
	                
	            }
	       
	}  
	private int fillSideFluid(FluidStack fluid, IFluidHandler tank, ForgeDirection direction, boolean doFill) {
        if (tank != null) {
            return tank.fill(direction, fluid, doFill);
        }
        return 0;
    }
	 private ItemStack fillSideItem(ItemStack item, InventoryAdaptor inv, InsertionMode mode, boolean doFill) {
	        if (inv != null) {
	            if (doFill) {
	                return inv.addItems(item, mode);
	            } else {
	                return inv.simulateAdd(item, mode);
	            }
	        }
	        return item;
	    }
}
