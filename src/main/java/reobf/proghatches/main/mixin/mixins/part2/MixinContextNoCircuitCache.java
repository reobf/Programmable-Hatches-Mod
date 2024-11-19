package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import appeng.api.networking.IGrid;
import appeng.crafting.v2.CraftingContext;
import reobf.proghatches.ae.IIsExtractFromInvAllowed;
import reobf.proghatches.ae.TileStockingCircuitRequestInterceptor;

@Mixin(value=CraftingContext
.class,remap=false)
public class MixinContextNoCircuitCache implements IIsExtractFromInvAllowed {

	@Override
	public boolean isAllowed() {
	if(isAllowedCache==null){
		isAllowedCache=meGrid.getMachines(TileStockingCircuitRequestInterceptor.class).isEmpty();
		//isAllowedCache=false;
	}
		return isAllowedCache;
	}
	
	Boolean isAllowedCache;
	@Shadow
	  public  IGrid meGrid;
}
