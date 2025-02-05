package reobf.proghatches.eucrafting;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;

public interface IGuiProvidingPart {

    ModularWindow createWindow(UIBuildContext buildContext);
    // Container.markDirty will be reobfuscated to func_70296_d, use a different name

}
