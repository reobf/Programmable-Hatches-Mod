package reobf.proghatches.main.mixin.mixins.part2;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import appeng.api.util.IInterfaceViewable;
import appeng.container.implementations.ContainerOptimizePatterns;

import codechicken.nei.ItemStackMap;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import reobf.proghatches.gt.metatileentity.util.ISpecialOptimize;

@Mixin(value = ContainerOptimizePatterns.class, remap = false)
public class MixinOptimize {//, @Local InventoryCrafting local, @Share("inv") LocalRef<InventoryCrafting> inv
	 
	
	private static final IInventory EMPTY=new IInventory() {
		
		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			
			
		}
		
		@Override
		public void openInventory() {
		
			
		}
		
		@Override
		public void markDirty() {
			
			
		}
		
		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			
			return true;
		}
		
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
		
			return true;
		}
		
		@Override
		public boolean hasCustomInventoryName() {
		
			return false;
		}
		
		@Override
		public ItemStack getStackInSlotOnClosing(int index) {
		
			return null;
		}
		
		@Override
		public ItemStack getStackInSlot(int slotIn) {
		
			return null;
		}
		
		@Override
		public int getSizeInventory() {
		
			return 0;
		}
		
		@Override
		public int getInventoryStackLimit() {
			
			return 0;
		}
		
		@Override
		public String getInventoryName() {
			
			return "";
		}
		
		@Override
		public ItemStack decrStackSize(int index, int count) {
		
			return null;
		}
		
		@Override
		public void closeInventory() {
		
			
		}
	};
	
	
	@Inject(
		        require = 1,
		        method = "optimizePatterns",
		        at = @At(shift=Shift.BY,by=2,
		            value = "INVOKE",
		            target = "Lappeng/api/util/IInterfaceViewable;getPatterns()Lnet/minecraft/inventory/IInventory;"))
		    public void optimizePatterns(HashMap<Integer, Integer> hashCodeToMultipliers, CallbackInfo x,@Local LocalRef<IInventory> inv,@Local IInterfaceViewable m
		    		
		    		,@Local ItemStackMap<Pair<Object, Integer>> lookupMap 
		    		) {
		  if(m instanceof ISpecialOptimize){
			  
			  ((ISpecialOptimize) m).optimize(lookupMap);
			  
			  inv.set(EMPTY);//prevent further operation
		  }
		     
		    }
}
