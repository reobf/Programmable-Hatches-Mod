package reobf.proghatches.main.mixin.mixins.part2;

import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import reobf.proghatches.ae.ICondenser;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinCraftingCondender {
	 /* 
	@Shadow
	private int remainingOperations;
	@Unique
	boolean skip;
	@Unique
	Reference2IntOpenHashMap<ICraftingPatternDetails> temp1=new Reference2IntOpenHashMap<ICraftingPatternDetails>();
	
	private long maxSkips;
	@Inject(at = @At(value = "RETURN"), method = "updateCraftingLogic")
	public void a(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cc, CallbackInfo it) {
		if(getMaxSkips()<=0)return;
		temp1.clear();
	
	}

	@ModifyVariable(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;remainingOperations"), method = "executeCrafting")

	public ICraftingPatternDetails a(ICraftingPatternDetails m) {
		if(getMaxSkips()<=0)return m;
		int now= temp1 .getOrDefault(m, 0);
		skip =getMaxSkips()>=now;
	
		if(now<Integer.MAX_VALUE-10)
			temp1.put(m,now+1);
			
		
		return m;
	}
	
	@WrapWithCondition(remap = false, at = {
			@At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;remainingOperations") }, method = {
					"executeCrafting" })
	private boolean b(CraftingCPUCluster thiz,int neo) {
		if(getMaxSkips()<=0)return true;
		
		
		return !skip;
	}
	private long getMaxSkips(){return maxSkips;}
	
	
	@Inject(at = @At(value = "RETURN"), method = "addTile")
	public void addTile(TileCraftingTile te, CallbackInfo it) {
		if(te instanceof ICondenser){
			ICondenser con=(ICondenser) te;
			if(con.isinf()){
				maxSkips=Integer.MAX_VALUE;
			}else{
				
				maxSkips=Long.max(maxSkips,maxSkips+con.getSkips());
			}
			
		}
	}*/
}
