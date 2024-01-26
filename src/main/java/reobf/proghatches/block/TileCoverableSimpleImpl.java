package reobf.proghatches.block;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.objects.GT_ItemStack;
import gregtech.api.util.GT_CoverBehavior;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.ISerializableObject;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

public abstract class TileCoverableSimpleImpl extends CoverableTileEntity  {
	@Override
	public boolean isUniversalEnergyStored(long aEnergyAmount) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
		// TODO Auto-generated method stub
		return true;
	}



	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public void issueBlockUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte getComparatorValue(ForgeDirection side) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTimer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setLightValue(byte aLightValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasInventoryBeenModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValidSlot(int aIndex) {
		
		return this.getSizeInventory()>=aIndex||aIndex<0;
	}

	
	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
		
		return IntStream.range(0,  this.getSizeInventory()-1).toArray();
	}

	


	

	@Override
	public long getUniversalEnergyStored() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getUniversalEnergyCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getOutputAmperage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getOutputVoltage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getInputAmperage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getInputVoltage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean decreaseStoredEnergyUnits(long aEnergy, boolean aIgnoreTooLessEnergy) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean increaseStoredEnergyUnits(long aEnergy, boolean aIgnoreTooMuchEnergy) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drainEnergyUnits(ForgeDirection side, long aVoltage, long aAmperage) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getAverageElectricInput() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getAverageElectricOutput() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getStoredEU() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getEUCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long injectEnergyUnits(ForgeDirection side, long aVoltage, long aAmperage) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean inputEnergyFrom(ForgeDirection side) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean outputsEnergyTo(ForgeDirection side) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte getColorization() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte setColorization(byte aColor) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean shouldJoinIc2Enet() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStillValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean allowCoverOnSide(ForgeDirection side, GT_ItemStack aCoverID) {
		// TODO Auto-generated method stub
		return true;
	}
	  @Override
	    public boolean addStackToSlot(int slotIndex, ItemStack stack) {
	        if (GT_Utility.isStackInvalid(stack)) return true;
	        if (slotIndex < 0 || slotIndex >= getSizeInventory()) return false;
	        final ItemStack toStack = getStackInSlot(slotIndex);
	        if (GT_Utility.isStackInvalid(toStack)) {
	            setInventorySlotContents(slotIndex, stack);
	            return true;
	        }
	        final ItemStack fromStack = GT_OreDictUnificator.get(stack);
	        if (GT_Utility.areStacksEqual(toStack, fromStack) && toStack.stackSize + fromStack.stackSize
	            <= Math.min(fromStack.getMaxStackSize(), getInventoryStackLimit())) {
	            toStack.stackSize += fromStack.stackSize;
	            markDirty();
	            return true;
	        }
	        return false;
	    }

	    @Override
	    public boolean addStackToSlot(int aIndex, ItemStack aStack, int aAmount) {
	        return addStackToSlot(aIndex, GT_Utility.copyAmount(aAmount, aStack));
	    }
}
