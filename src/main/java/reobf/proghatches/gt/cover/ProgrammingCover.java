package reobf.proghatches.gt.cover;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Optional;

import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IMachineProgress;
import gregtech.api.util.GT_CoverBehavior;
import reobf.proghatches.gt.metatileentity.IProgrammingCoverBlacklisted;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;

public class ProgrammingCover extends GT_CoverBehavior {

	@Override
	public int getTickRate(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity) {

		return 1;
	}

	public void impl(ICoverable aTileEntity) {

		if ((((aTileEntity instanceof IMachineProgress)) && (!((IMachineProgress) aTileEntity).isAllowedToWork()))) {
			return;
		}
		TileEntity tile = (TileEntity) aTileEntity;

		if (!(tile instanceof ISidedInventory)) {
			return;
		}
		if (!(tile instanceof IGregTechTileEntity)) {
			return;
		}
		if (((IGregTechTileEntity) tile).getMetaTileEntity() instanceof IProgrammingCoverBlacklisted) {

			return;
		}

		IMetaTileEntity meta = ((IGregTechTileEntity) tile).getMetaTileEntity();

		if (!(meta instanceof IConfigurationCircuitSupport)) {
			return;
		}

		int[] slots = ((ISidedInventory) tile).getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal());
		for (int slot : slots) {
			ItemStack is = ((ISidedInventory) tile).getStackInSlot(slot);
			if (is == null)
				continue;
			if (is.getItem() != MyMod.progcircuit)
				continue;

			/*
			 * if(((ISidedInventory)tile).canExtractItem(slot, is,
			 * ForgeDirection.UNKNOWN.ordinal()) ==false)continue;
			 */
			if (((ISidedInventory) tile).decrStackSize(slot, 64).stackSize == 0) {
				continue;
			}

			;
			((IInventory) tile).setInventorySlotContents(((IConfigurationCircuitSupport) meta).getCircuitSlot(),
					ItemProgrammingCircuit.getCircuit(is).orElse(null)

			// new ItemStack(Items.apple)

			);

		}

	}

	@Override
	public boolean isCoverPlaceable(ForgeDirection side, ItemStack aStack, ICoverable aTileEntity) {
		if (Optional.of(aTileEntity).filter(s -> s instanceof IGregTechTileEntity)
				.map(s -> ((IGregTechTileEntity) s).getMetaTileEntity())
				.filter(s -> s instanceof IProgrammingCoverBlacklisted).isPresent())
			return false;

		return super.isCoverPlaceable(side, aStack, aTileEntity);
	}

	@Override
	public int doCoverThings(ForgeDirection side, byte aInputRedstone, int aCoverID, int aCoverVariable,
			ICoverable aTileEntity, long aTimer) {
		impl(aTileEntity);

		return aCoverVariable;
	}

}
