package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



import crazypants.enderio.conduit.AbstractConduit;
import crazypants.enderio.conduit.AbstractConduitNetwork;

import reobf.proghatches.eio.ICraftingMachineConduit;
import reobf.proghatches.eio.MAConduit;
import reobf.proghatches.eio.MASettings;

@Mixin(value=AbstractConduit.class,remap=false)
public class MixinEIOInit {
	
	@Inject( require = 1,method="createNetworkForType",at = { @At("RETURN") },cancellable=true)
	  public void createPanelForConduit(CallbackInfoReturnable<AbstractConduitNetwork>  a
			  ) {
		 Class type = this.getClass();
		 if (ICraftingMachineConduit.class.isAssignableFrom(type)) {
	            a.setReturnValue( new MAConduit.MAConduitNetwork(
	            		ICraftingMachineConduit.class, ICraftingMachineConduit.class));
	        } 
		  
	  }

}
