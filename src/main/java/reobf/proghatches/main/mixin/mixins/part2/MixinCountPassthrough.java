package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.storage.data.IItemList;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.util.inv.ItemListIgnoreCrafting;
//import reobf.proghatches.ae.TileFluidDiscretizerMKII;


@Mixin(remap=false,value=MEMonitorPassThrough.class)
public class MixinCountPassthrough {

	/*
@Inject(method="getAvailableItems", at = { @At("HEAD") })
	    public void getAvailableItemsH(final IItemList out, int iterator,CallbackInfoReturnable _) {
	TileFluidDiscretizerMKII.count++;
	    }
	 
@Inject(method="getAvailableItems", at = { @At("RETURN") })
public void getAvailableItemsR(final IItemList out, int iterator,CallbackInfoReturnable _) {
	TileFluidDiscretizerMKII.count--;
} 
	 */
	 
	 
	 
	
}
