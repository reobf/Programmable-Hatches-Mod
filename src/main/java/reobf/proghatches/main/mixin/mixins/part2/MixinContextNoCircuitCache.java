package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.v2.CraftingContext;
import appeng.util.item.AEItemStack;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import reobf.proghatches.ae.IIsExtractFromInvAllowed;
import reobf.proghatches.ae.TileStockingCircuitRequestInterceptor;
import reobf.proghatches.main.MyMod;

@Mixin(value=CraftingContext
.class,remap=false)
public class MixinContextNoCircuitCache implements IIsExtractFromInvAllowed {

	
	@Unique
	private Object2BooleanMap<IAEStack> mapcache=new Object2BooleanOpenHashMap<IAEStack>();
	
	
	@Override
	public boolean isAllowed(IAEStack stack) {
		Boolean b=mapcache.get(stack);
		if(b!=null)return b;
		for(IGridNode te:meGrid.getMachines(TileStockingCircuitRequestInterceptor.class)){
			
			if(false==((TileStockingCircuitRequestInterceptor)te.getMachine())
			.isAllowed(stack)){
				System.out.println("cachef");
				mapcache.put(stack.copy(), false);
				return false;}
			
			
			
		}
		System.out.println("cachet");
	mapcache.put(stack.copy(), true);
	return true;
	}
	
	//Boolean isAllowedCache;
	@Shadow
	  public  IGrid meGrid;
	
}
