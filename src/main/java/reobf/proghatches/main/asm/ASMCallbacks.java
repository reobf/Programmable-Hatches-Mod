package reobf.proghatches.main.asm;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

import gregtech.api.metatileentity.CommonMetaTileEntity;
import reobf.proghatches.oc.IActualEnvironment;

public class ASMCallbacks {

    public static boolean checkIsRealEnvironment(Object o) {

        if (o instanceof CommonMetaTileEntity) {
        	
        	CommonMetaTileEntity mte = (CommonMetaTileEntity) o;
            if (mte.getMetaTileEntity() instanceof IActualEnvironment) {

                return o instanceof li.cil.oc.api.network.Environment;
            } else {
            	
                return false;
            }

        }

        return o instanceof li.cil.oc.api.network.Environment;
    }
}
