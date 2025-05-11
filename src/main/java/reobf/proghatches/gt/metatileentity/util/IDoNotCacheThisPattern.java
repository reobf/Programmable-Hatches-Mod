package reobf.proghatches.gt.metatileentity.util;

import gregtech.common.tileentities.machines.IDualInputInventory;

public interface IDoNotCacheThisPattern {
	
	    default boolean shouldBeCached() {
	    	
	    	//if( /*this.butYouCanCacheThisInstead()!=null&&*/areYouSerious()){return false;}
	    	//return true;
	    	return false;
	    }
    //public boolean areYouSerious();

   /* default public IDualInputInventory butYouCanCacheThisInstead() {
        return null;
    }*/
}
