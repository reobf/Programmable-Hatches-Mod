package reobf.proghatches.gt.metatileentity.util;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;

public interface IMEHatchOverrided {
	 public default boolean override(){return true;}
    public default void overridedBehoviour(int minPull){
    	
    	throw new AssertionError("should not happen");
    };

    public default IAEStack overridedExtract(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src){
    	
    	return  thiz.extractItems(request, mode, src);
    }

	default public IAEStack qureyStorage(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src){
		return  this.overridedExtract(thiz,request, mode, src);
		
		
	};
}
