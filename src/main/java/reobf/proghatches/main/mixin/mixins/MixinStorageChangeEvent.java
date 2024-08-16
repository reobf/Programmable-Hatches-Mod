package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkStorageEvent;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import reobf.proghatches.gt.metatileentity.util.IMEStorageChangeAwareness;

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
}
