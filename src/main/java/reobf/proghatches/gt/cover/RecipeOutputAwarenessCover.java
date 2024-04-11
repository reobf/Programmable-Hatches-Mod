package reobf.proghatches.gt.cover;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_BasicMachine;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.util.GT_CoverBehavior;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
@Deprecated
public class RecipeOutputAwarenessCover  extends GT_CoverBehavior{
	  @Override
	    public int getTickRate(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity) {

	        return 1;
	    }
	  
	  public boolean check(FluidStack[] mOutputFluids, ItemStack[] mOutputItems){
		  boolean ok=false;
		  for(FluidStack f:mOutputFluids){
			  if(f!=null&&f.amount>0)ok=true;
		  }
		  for(ItemStack f:mOutputItems){
			  if(f!=null&&f.stackSize>0)ok=true;
		  }
		  return ok;}
	  
	  @Override
	public int doCoverThings(ForgeDirection side, byte aInputRedstone, int aCoverID, int aCoverVariable,
			ICoverable aTileEntity, long aTimer) {
		boolean ok=false;
		  b:if(aTileEntity instanceof IGregTechTileEntity ){
			  IGregTechTileEntity te=(IGregTechTileEntity) aTileEntity;
			  IMetaTileEntity mte = te.getMetaTileEntity();
			  if(mte==null)break b;
		  if(mte instanceof GT_MetaTileEntity_BasicMachine){
			
			  ok=ok||check( new  FluidStack[]{((GT_MetaTileEntity_BasicMachine) mte).mOutputFluid},
					  ((GT_MetaTileEntity_BasicMachine) mte).mOutputItems
					  );
		 
		  }
		
		  if(mte instanceof GT_MetaTileEntity_MultiBlockBase){
			  ok=ok||check(  ((GT_MetaTileEntity_MultiBlockBase) mte).mOutputFluids,
					  ((GT_MetaTileEntity_MultiBlockBase) mte).mOutputItems
					  );
		  }
		  
		  
		  
		  }
	  
	  
	  
	  
		aTileEntity.setOutputRedstoneSignal(side, (byte) (ok?15:0));
	  
		return aCoverVariable;
	}
	  
	  @Override
	    public boolean manipulatesSidedRedstoneOutput(ForgeDirection side, int aCoverID, int aCoverVariable,
	        ICoverable aTileEntity) {
	        return true;
	    }

	  
	  
}
