package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.MECraftingInventory;
import appeng.crafting.v2.CraftingContext;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import appeng.util.item.HashBasedItemList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.PatternDetail;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.WrappedPatternDetail;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.mixin.MixinCallback;
import reobf.proghatches.util.ProghatchesUtil;


@Mixin(value = CraftingCPUCluster.class, remap = false, priority = 0)
public class MixinCpuClusterEUAutoRequest {
	@Shadow  private MachineSource machineSrc;
	 Map<IAEItemStack,Long> storage=new HashMap<>();
	 Map<IAEItemStack,Long> needed=new HashMap<>();
	 @Shadow   private MECraftingInventory inventory;
	 
	@Shadow Map<ICraftingPatternDetails, Object>  tasks;

	
/**
if (!this.canCraft(details, details.getCondensedInputs())) {
   //INJECT HERE Shift.BY.3
    i.remove();
}
*/
	//collect failed WrappedPatternDetail
@ModifyVariable(at = @At(value="INVOKE",target="canCraft(Lappeng/api/networking/crafting/ICraftingPatternDetails;[Lappeng/api/storage/data/IAEItemStack;)Z"
,shift=Shift.BY,by=3),method = "executeCrafting")
private ICraftingPatternDetails executeCrafting2(ICraftingPatternDetails pattern
		){
		if(pattern instanceof WrappedPatternDetail){
			WrappedPatternDetail p=(WrappedPatternDetail) pattern;
			
			
			
			
			
			needed.put(p.extraIn.copy(),p.extraIn0.stackSize+0l);
			
		}	
	
	return pattern;
}
	
	
	@Inject(at = @At("HEAD"),method = "executeCrafting",cancellable=true)
	private void executeCrafting3(final IEnergyGrid eg, final CraftingGridCache cc,CallbackInfo RE
			){
		
		
	}
	
@Inject(at = @At("RETURN"),method = "executeCrafting",cancellable=true)
private void executeCrafting1(final IEnergyGrid eg, final CraftingGridCache cc,CallbackInfo RE
		){
	if(needed.isEmpty()){storage.clear();;return;}
	
	
inventory.getItemList().forEach(s->{
		
		if(s.getItem()==MyMod.eu_token){
			if(s.getItemDamage()==1){
				
				IAEItemStack u=s.copy().setStackSize(1);
				if(storage.get(u)==null){storage.put(u, 0l);};
				storage.compute(u, (a,b)->b+s.getStackSize());
			}
			
			
		}
		
	});

tasks.entrySet().forEach(s->{
	
	
	if(s.getKey() instanceof PatternDetail){
		PatternDetail d=(PatternDetail) s.getKey();
		if(d.out.getItemDamage()==1){
			
			
			storage.computeIfPresent(d.o[0].copy().setStackSize(1), 
					(a,b)->b+MixinCallback.getter.apply(s.getValue())			
					);
			
		}
		
		
	}
	
	
	
	
});
	needed.entrySet().forEach(s->{
		
		long num=Optional.ofNullable(storage.get(s.getKey())).orElse(0l);
		long missing=s.getValue()-num;
		if(missing<=0)return;
		
		CraftingCPUCluster j;
		inventory.injectItems(
		s.getKey().copy().setStackSize(missing),
		Actionable.MODULATE,machineSrc
				
				);
		
		
	});
	
	
	storage.clear();
	needed.clear();
	
}











}
