package reobf.proghatches.eucrafting;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;

public interface IGuiProvidingPart {

	ModularWindow createWindow(UIBuildContext buildContext);

	void markDirty();

}
