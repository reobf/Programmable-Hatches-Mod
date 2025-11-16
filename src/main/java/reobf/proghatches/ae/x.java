package reobf.proghatches.ae;

import java.io.File;

import codechicken.core.CommonUtils;
import net.minecraftforge.common.DimensionManager;

public class x {
	   public static File getSaveLocation(int dim) {
		   if(dim==0) return DimensionManager.getCurrentSaveRootDirectory();
	        return CommonUtils.getSaveLocation(DimensionManager.getWorld(dim));
	    }
}
