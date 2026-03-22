package reobf.proghatches.gt.metatileentity.util;

import java.util.Collection;
import java.util.HashSet;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface ICircuitProvider {

    public void clearDirty();

    public boolean patternDirty();

    public Collection<ItemStack> getCircuit();

    public default boolean checkLoop(HashSet<Object> blacklist) {

        return blacklist.add(this);

    };
    public default TileEntity getTile() {
    	if(this instanceof MetaTileEntity mte) {
    		if(mte.getBaseMetaTileEntity() instanceof TileEntity te) {
    			return te;
    		}
    		
    	}
    	
    	return null;
    }

}
