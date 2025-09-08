package reobf.proghatches.main.mixin.mixins.cpu;

import java.util.Collection;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingStorageTile;
import reobf.proghatches.ae.cpu.ICraftingCacheAccessor;
import reobf.proghatches.ae.cpu.IExternalManager;

@Mixin(value=CraftingGridCache.class,remap=false)
public class MixinCache implements ICraftingCacheAccessor {
	@Shadow
	 private boolean updateList; 
	@Shadow
	private  IGrid grid;
	@Shadow
	  private  Set<CraftingCPUCluster> craftingCPUClusters;
	@Inject(at = { @At("TAIL") },method="updateCPUClusters")
	 public void updateCPUClusters(CallbackInfo ci) 
	{
		  for (Object cls : StreamSupport.stream(grid.getMachinesClasses().spliterator(), false)
	                .filter(IExternalManager.class::isAssignableFrom).toArray()) {
			  
			  for (final IGridNode cst : this.grid.getMachines((Class) cls)) {
				  IGridHost machine = cst.getMachine();
				  if(machine instanceof IExternalManager){
					  
					  
					  craftingCPUClusters.addAll(((IExternalManager) machine).getClusters());
					  
				  }
				  
				  
			  }
			  
		  }
		
		
	 }
	
	public final void markForUpdate(){
		 updateList=true;
    }
	
	
	@Inject(at = { @At("TAIL") },method="addNode")
	 public void addNode(final IGridNode gridNode, final IGridHost machine,CallbackInfo ci) {
		 if(machine instanceof IExternalManager){
			 updateList=true;
		 }
	 }
}
