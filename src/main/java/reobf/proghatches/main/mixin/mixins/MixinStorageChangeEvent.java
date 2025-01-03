package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkStorageEvent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import reobf.proghatches.gt.metatileentity.util.IMEStorageChangeAwareness;
import reobf.proghatches.gt.metatileentity.util.IStoageCellUpdate;

@Mixin(value=BaseMetaTileEntity.class,remap=false)
public abstract class MixinStorageChangeEvent {
	
    @Shadow
    public abstract IMetaTileEntity getMetaTileEntity();
	private Boolean isValidStorageChangeEventReceiver;
    
    @MENetworkEventSubscribe
	public void storageChange_Mixin(MENetworkStorageEvent w){
		if(isValidStorageChangeEventReceiver==null){
			isValidStorageChangeEventReceiver=getMetaTileEntity() instanceof IMEStorageChangeAwareness;
		}
		if(isValidStorageChangeEventReceiver){
			IMetaTileEntity te = getMetaTileEntity();
			if(te!=null)((IMEStorageChangeAwareness)te).storageChange(w);
		}
	}
    
    
    private Boolean isMETank;
    @MENetworkEventSubscribe @Unique
    public void powerRender(final MENetworkPowerStatusChange w) {
       
    	if(isMETank==null){
    		isMETank=getMetaTileEntity() instanceof IStoageCellUpdate;
		}
		if(isMETank){
			IMetaTileEntity te = getMetaTileEntity();
			if(te!=null)((IStoageCellUpdate)te).cellUpdate();
		}
    }  
    @MENetworkEventSubscribe @Unique
    public void updateChannels(final MENetworkChannelsChanged w) {
    	if(isMETank==null){
    		isMETank=getMetaTileEntity() instanceof IStoageCellUpdate;
		}
		if(isMETank){
			IMetaTileEntity te = getMetaTileEntity();
			if(te!=null)((IStoageCellUpdate)te).cellUpdate();
		}
    }
    
    
}
