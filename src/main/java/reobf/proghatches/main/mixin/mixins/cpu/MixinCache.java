package reobf.proghatches.main.mixin.mixins.cpu;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.crafting.CraftingLink;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingStorageTile;
import reobf.proghatches.ae.cpu.ICraftingCacheAccessor;
import reobf.proghatches.ae.cpu.IExternalManager;
import reobf.proghatches.gt.metatileentity.RecipeFilterCRIB;

@Mixin(value=CraftingGridCache.class,remap=false)
public class MixinCache implements ICraftingCacheAccessor {
	@Shadow
	 public void addLink(final CraftingLink link){};
	 @Shadow
	 private boolean updateList;
	@Shadow
	private  IGrid grid;
	@Shadow
	  private  Set<CraftingCPUCluster> craftingCPUClusters;
	@Shadow
	@Final
	protected Map<ICraftingPatternDetails, List<ICraftingMedium>> craftingMethods;

	@Unique
	private volatile Map<ICraftingPatternDetails, List<ICraftingMedium>> proghatches$byBasePattern;

	@Inject(at = @At("TAIL"), method = "setPatternsFromCraftingMethods", require = 1)
	private void proghatches$indexBasePatterns(CallbackInfo ci) {
		proghatches$byBasePattern = RecipeFilterCRIB.indexByBasePattern(craftingMethods);
	}

	@Inject(at = @At("RETURN"), method = "getMediums", cancellable = true, require = 1)
	private void proghatches$staleMultiplier(ICraftingPatternDetails key,
		CallbackInfoReturnable<List<ICraftingMedium>> ci) {
		Map<ICraftingPatternDetails, List<ICraftingMedium>> index = proghatches$byBasePattern;
		if (index == null || !ci.getReturnValue().isEmpty()) return;
		List<ICraftingMedium> mediums = index.get(RecipeFilterCRIB.basePattern(key));
		if (mediums != null) ci.setReturnValue(mediums);
	}
	@Inject(at = { @At("TAIL") },method="updateCPUClusters")
	 public void updateCPUClusters(CallbackInfo ci) 
	{
		  for (Object cls : StreamSupport.stream(grid.getMachinesClasses().spliterator(), false)
	                .filter(IExternalManager.class::isAssignableFrom).toArray()) {
			  
			  for (final IGridNode cst : this.grid.getMachines((Class) cls)) {
				  IGridHost machine = cst.getMachine();
				  if(machine instanceof IExternalManager){
					  
					  
					  craftingCPUClusters.addAll(((IExternalManager) machine).getClusters());
					 
					  ((IExternalManager) machine).getClusters().stream().map(
							  s->s.getLastCraftingLink()
							  ).forEach(s->{if(s!=null)addLink((CraftingLink)s);});
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
