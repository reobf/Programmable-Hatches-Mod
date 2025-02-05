package reobf.proghatches.gt.metatileentity.util;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;

public interface IMEHatchOverrided {

    public void overridedBehoviour(int minPull);

    public IAEStack overridedExtract(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src);
}
