package reobf.proghatches.gt.metatileentity.util;

import java.util.Collection;

import gregtech.common.tileentities.machines.IDualInputInventory;

public interface IDoNotCacheThisPattern {
	
public boolean areYouSerious();
default public IDualInputInventory butYouCanCacheThisInstead() {
	return null;
}
}
