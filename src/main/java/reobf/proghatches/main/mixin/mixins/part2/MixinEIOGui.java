package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.enderio.core.api.client.gui.ITabPanel;

import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.gui.GuiExternalConnection;
import crazypants.enderio.conduit.gui.PowerSettings;
import crazypants.enderio.conduit.gui.TabFactory;
import crazypants.enderio.conduit.power.IPowerConduit;
import reobf.proghatches.eio.ICraftingMachineConduit;
import reobf.proghatches.eio.MASettings;

@Mixin(value=TabFactory.class,remap=false)
public class MixinEIOGui {
	@Inject( require = 1,method="createPanelForConduit",at = { @At("RETURN") },cancellable=true)
	  public void createPanelForConduit(GuiExternalConnection gui, IConduit con
			 ,CallbackInfoReturnable<ITabPanel>  a
			  ) {
		Class<? extends IConduit> baseType = con.getBaseConduitType();
		 if (baseType.isAssignableFrom(ICraftingMachineConduit.class)) {
	            a.setReturnValue( new MASettings(gui, con));
	             
		 
		 }
		  
		  
	  }
}
