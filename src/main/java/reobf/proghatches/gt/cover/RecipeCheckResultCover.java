package reobf.proghatches.gt.cover;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.util.GT_CoverBehavior;
import net.minecraftforge.common.util.ForgeDirection;

public class RecipeCheckResultCover extends GT_CoverBehavior{
	 @Override
	    public int getTickRate(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity) {

	        return 1;
	    }
	 
	 
	 public int doCoverThings(ForgeDirection side, byte aInputRedstone, int aCoverID, int aCoverVariable,
				ICoverable aTileEntity, long aTimer) {
			
		 
		 aTileEntity.setOutputRedstoneSignal(side, (byte) ((aTimer%2==0)?15:0));
			
			
			 b: if(aTileEntity instanceof IGregTechTileEntity ){
				  
				  IGregTechTileEntity te=(IGregTechTileEntity) aTileEntity;
				  IMetaTileEntity mte = te.getMetaTileEntity();
				  if(mte==null)break b;
				  if(mte instanceof GT_MetaTileEntity_MultiBlockBase){
					  GT_MetaTileEntity_MultiBlockBase mb=(GT_MetaTileEntity_MultiBlockBase) mte;
					  
					  
				  }
				  
				  
				  
				  
				  
			  }
			
			
			
			return aCoverVariable;
			
	 
	 }  @Override
	    public boolean manipulatesSidedRedstoneOutput(ForgeDirection side, int aCoverID, int aCoverVariable,
		        ICoverable aTileEntity) {
		        return true;
		    }
}
