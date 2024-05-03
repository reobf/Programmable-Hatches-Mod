package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
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
import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.MECraftingInventory;
import appeng.crafting.v2.CraftingContext;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.item.AEItemStack;
import appeng.util.item.HashBasedItemList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import reobf.proghatches.eucrafting.IEUManager;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.SISOPatternDetail;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.WrappedPatternDetail;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.mixin.MixinCallback;
import reobf.proghatches.util.ProghatchesUtil;


@Mixin(value = CraftingCPUCluster.class, remap = false, priority = 0)
public abstract class MixinCpuClusterEUAutoRequest {
	@Shadow  private MachineSource machineSrc;
	 Map<IAEItemStack,Long> storage=new HashMap<>();
	 Map<IAEItemStack,Long> needed=new HashMap<>();
	 @Shadow   private MECraftingInventory inventory;
	 @Shadow   private final LinkedList<TileCraftingTile> tiles = new LinkedList<>();
	@Shadow Map<ICraftingPatternDetails, Object>  tasks;

private HashMap<WrappedPatternDetail, int[]> cooldown=new HashMap<>();
@Shadow   abstract boolean canCraft(final ICraftingPatternDetails details, final IAEItemStack[] condensedInputs) ;

@Inject(at = @At(value="HEAD"),method = {"cancel","completeJob"})
private void endJob(CallbackInfo __){
		
	
	cooldown.clear(); 
	
}


/**
if (!this.canCraft(details, details.getCondensedInputs())) {
   //INJECT HERE Shift.BY.3
    i.remove();
}
*/
	
@ModifyVariable(at = @At(value="INVOKE",target="canCraft(Lappeng/api/networking/crafting/ICraftingPatternDetails;[Lappeng/api/storage/data/IAEItemStack;)Z"
,shift=Shift.BY,by=3),method = "executeCrafting")
private ICraftingPatternDetails executeCrafting2(ICraftingPatternDetails pattern
		){//collect failed WrappedPatternDetail
		if(pattern instanceof WrappedPatternDetail){
			WrappedPatternDetail p=(WrappedPatternDetail) pattern;
			int cd[]=cooldown.computeIfAbsent(p, (s)->new int[2]);
			if(cd[0]>0){cd[0]--;return pattern;}
			boolean isOnlyEUTokenMissing=false;
			  //cannot craft, but original one can, means that only eu token is missing
			if (this.canCraft(p.original, p.original.getCondensedInputs())) {isOnlyEUTokenMissing=true;}
			
			if(isOnlyEUTokenMissing)
			{
			needed.put(p.extraIn.copy().setStackSize(1),p.extraIn0.stackSize+0l);
			cooldown.remove(p);
			}
			else{
			cooldown.get(p)[0]+=Math.min((10+2*cooldown.get(p)[1]++),100);
			
			//MyMod.LOG.info("Cannot craft, blacklist for "+cooldown.get(p)[0]+" ticks:"+p.extraIn0.getTagCompound());	
			}
		}	
	
	return pattern;
}
	
	
	@Inject(at = @At("HEAD"),method = "executeCrafting",cancellable=true)
	private void executeCrafting3(final IEnergyGrid eg, final CraftingGridCache cc,CallbackInfo RE
			){
		
		
	}
private static AEItemStack type=AEItemStack.create(new ItemStack(MyMod.eu_token,1,1));
@Inject(at = @At("RETURN"),method = "executeCrafting",cancellable=true)
private void executeCrafting1(final IEnergyGrid eg, final CraftingGridCache cc,CallbackInfo RE
		){
	if(needed.isEmpty()){storage.clear();return;}
	
	//ArrayList<Object> arr=new ArrayList();
	
	//inventory.getItemList().forEach(arr::add);
	//System.out.println(arr);
	
	
inventory.getItemList().findFuzzy(type, FuzzyMode.IGNORE_ALL).forEach(s->{
		
		if(s.getItem()==MyMod.eu_token){
			if(s.getItemDamage()==1){
				
				IAEItemStack u=s.copy().setStackSize(1);
			
				storage.merge(u,s.getStackSize(),Long::sum);
			}
			
			
		}
		
	});

tasks.entrySet().forEach(s->{
	//TODO remove 
	
	if(s.getKey() instanceof SISOPatternDetail){
		SISOPatternDetail d=(SISOPatternDetail) s.getKey();
		if(d.out.getItemDamage()==1){
			IAEItemStack key = d.o[0].copy().setStackSize(1);
			
			storage.merge(key, MixinCallback.getter.apply(s.getValue())	,
					Long::sum
					);
			
		}
		
		
	}
	
	
	
	
});
//System.out.println(storage);

	needed.entrySet().forEach(s->{
		
		long num=Optional.ofNullable(storage.get(s.getKey())).orElse(0l);
		long missing=s.getValue()-num;
		if(missing<=0)return;
		//Object o=this;
		//CraftingCPUCluster thiz=(CraftingCPUCluster) o;
		//System.out.println(s.getValue()+" "+num);
		//System.out.println(missing);
		
		if(tiles.isEmpty()){return;}
		try {
			IEUManager man=tiles.get(0).getProxy().getGrid().getCache(IEUManager.class);
			long get=man.request(
					s.getKey().getTagCompound().getNBTTagCompoundCopy().getLong("voltage")
					, missing);
			
			inventory.injectItems(
			s.getKey().copy().setStackSize(get),
			Actionable.MODULATE,machineSrc);
			 MyMod.LOG.info("Auto Request:"+get+"*"+s.getKey().getTagCompound().getNBTTagCompoundCopy());	
				
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	/*	
		*/
		
	});
	
	
	storage.clear();
	needed.clear();
	
}











}
