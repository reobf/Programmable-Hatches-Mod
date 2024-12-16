package reobf.proghatches.gt.cover;

import java.util.Optional;

import ggfab.mte.MTELinkedInputBus;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.CoverBehavior;
import gregtech.api.util.CoverBehaviorBase;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.gt.metatileentity.util.IProgrammingCoverBlacklisted;

public class LinkedBusSlaveCover extends CoverBehavior implements IProgrammer{
	
	
	@Override
	public int getTickRate(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity) {

		return 1;
	}

	@Override
	public void impl(ICoverable aTileEntity) {
		if(aTileEntity instanceof IGregTechTileEntity){
			IMetaTileEntity x = ((IGregTechTileEntity) aTileEntity).getMetaTileEntity();
			if(x instanceof MTELinkedInputBus){
				MTELinkedInputBus bus=(MTELinkedInputBus) x;
				try{
				ItemStack is=ProgrammingCover.sync(bus);
				bus.setInventorySlotContents(bus.getCircuitSlot(), is);
				}catch(RuntimeException e){
				//expected, do nothing	
				}
			}
			
		}
		
	}
	@Override
	public boolean isCoverPlaceable(ForgeDirection side, ItemStack aStack, ICoverable aTileEntity) {
		if (!Optional.of(aTileEntity).filter(s -> s instanceof IGregTechTileEntity)
				.map(s -> ((IGregTechTileEntity) s).getMetaTileEntity())
				.filter(s -> s instanceof MTELinkedInputBus).isPresent())
			return false;
		for(ForgeDirection d:	ForgeDirection.VALID_DIRECTIONS){
			CoverBehaviorBase<?> beh = aTileEntity.getCoverBehaviorAtSideNew(d);
			if(beh!=null&&beh.getClass()==ProgrammingCover.class){return false;}
		}
		return super.isCoverPlaceable(side, aStack, aTileEntity);
	}

	@Override
	public int doCoverThings(ForgeDirection side, byte aInputRedstone, int aCoverID, int aCoverVariable,
			ICoverable aTileEntity, long aTimer) {
		impl(aTileEntity);
		
		return aCoverVariable;
	}

}
