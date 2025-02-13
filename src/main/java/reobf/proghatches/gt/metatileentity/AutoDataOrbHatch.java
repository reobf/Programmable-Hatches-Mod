package reobf.proghatches.gt.metatileentity;

import com.google.common.collect.ImmutableMap;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gtPlusPlus.api.objects.Logger;
import gtPlusPlus.core.util.minecraft.ItemUtils;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.MTEHatchElementalDataOrbHolder;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.main.registration.Registration;

public class AutoDataOrbHatch extends MTEHatchElementalDataOrbHolder{
	 @Override
		public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
			
			return new AutoDataOrbHatch(mName,mTier, mDescriptionArray, mTextures);
		}
	public AutoDataOrbHatch(int aID, String aName, String aNameRegional, int aTier) {
		super(aID, aName, aNameRegional, aTier);
		 Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
	}
	String[] descCache;
	@Override
	public String[] getDescription() {

	    return descCache==null?(descCache=reobf.proghatches.main.Config.get("ADOH", ImmutableMap.of())):descCache;
	}
	public AutoDataOrbHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
		super(aName, aTier, aDescription, aTextures);
		
	} @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
            ItemStack aStack) {
           
            return aIndex < mInventory.length - 1 
            	&&ItemList.Tool_DataOrb.isStackEqual(aStack, false, true);
                //&& side == getBaseMetaTileEntity().getFrontFacing();
        }

        @Override
        public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
            ItemStack aStack) {
         
            return aIndex < mInventory.length - 1 
                	&&ItemList.Tool_DataOrb.isStackEqual(aStack, false, true);
                   // && side == getBaseMetaTileEntity().getFrontFacing();
        }
        @Override
        public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {
           
            return aIndex < mInventory.length - 1 
                	&&ItemList.Tool_DataOrb.isStackEqual(aStack, false, true)
                   ;
        }

        @Override
        public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {
        	 return aIndex < mInventory.length - 1 
                 	&&ItemList.Tool_DataOrb.isStackEqual(aStack, false, true)
                    ;
        }

}
