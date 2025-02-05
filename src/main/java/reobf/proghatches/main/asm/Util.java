package reobf.proghatches.main.asm;

import com.gtnewhorizons.modularui.api.forge.IItemHandler;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;

import reobf.proghatches.gt.metatileentity.util.IInterhandlerGroup;

public class Util {

    public static boolean isSameGroup(BaseSlot o1, BaseSlot o2) {
        IItemHandler h1 = o1.getItemHandler();
        IItemHandler h2 = o2.getItemHandler();
        if (h1 instanceof IInterhandlerGroup && h2 instanceof IInterhandlerGroup) {

            long a = ((IInterhandlerGroup) h1).handlerID();
            long b = ((IInterhandlerGroup) h2).handlerID();
            if (a == b && a > 0) {
                return true;
            }

        }

        return false;
    }
}
