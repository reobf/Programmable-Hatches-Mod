package reobf.proghatches.main.mixin.mixins.cpu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.ae.cpu.IExternalManager;
import reobf.proghatches.ae.cpu.IExternalManagerHolder;
import reobf.proghatches.ae.cpu.TileCPU;
import reobf.proghatches.main.asm.repack.objectwebasm.Opcodes;

@Mixin(value=CraftingCPUCluster.class,remap=false)
public class MixinCPU implements IExternalManagerHolder{
	
	
	
	@Inject(at = { @At("HEAD") },method="isDestroyed",cancellable=true)
	 public void isDestroyed(CallbackInfoReturnable<Boolean> ci) 
	{
		
		if(ex!=null){
			
			if(!ex.getClusters().contains(this)){ci.setReturnValue(true);}
			
			
		}
		
	 }
	
	
	
	@Unique
	private TileCPU ex;
	
	
	@Shadow
	private MachineSource machineSrc;
	
	
	@Shadow
	private int accelerator;
	@Shadow
    private long availableStorage;
	
	@Shadow
	private int remainingOperations;
	@Override
	public void acceptIExternalManager(TileCPU a) {
		ex=a;
		machineSrc=new MachineSource( a.fakeCPU());
	}

	@Override
	public final TileCPU getIExternalManager() {
		return ex;
	}

	@Inject(at = { @At("TAIL") },method="updateCraftingLogic")
	 public void endCrafting(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cc ,CallbackInfo ci,@Share("prev")  LocalRef<Integer> prev) 
	{
		
		if(ex!=null){
			
			ex.repRemOP(remainingOperations,prev.get());
			
			
		}
		
	 }
	/*@Override
    public int getCoProcessors() {
        return this.accelerator;
    }*/
	
	 
	 @Inject(at = { @At("HEAD") },method="getGrid",cancellable=true)
	 public void getGrid(CallbackInfoReturnable<IGrid> ci) 
	{
		
		if(ex!=null){
			
		try {
			ci.setReturnValue(ex.getProxy().getGrid());
		} catch (Exception e) {
		
		}
			
			
		}
		
	 }
	 
	@Inject(at = { @At("HEAD") },method="getCoProcessors",cancellable=true)
	 public void getCoProcessors(CallbackInfoReturnable<Integer> ci) 
	{
		
		if(ex!=null){
			
		ci.setReturnValue((int) Math.min(ex.totalAcc()-1,Integer.MAX_VALUE));
			
			
		}
		
	 }
	
	@Inject(at = { @At(shift = Shift.AFTER,value="FIELD",opcode=Opcodes.PUTFIELD, target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;remainingOperations:I") },method="updateCraftingLogic")
	 public void startCrafting(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cc ,CallbackInfo ci,@Share("prev")  LocalRef<Integer> prev) 
	{
		
		if(ex!=null){
			
			remainingOperations=ex.getRemOP();
			prev.set(remainingOperations);
			
			
		}
		
		
		
	 }

	@Override
	public void setStorage(long v) {
		availableStorage=v;
		
	}
	
	
	
	@Inject(at = { @At("HEAD") },method="submitJob")
	
	  public void submitJobPre(final IGrid g, final ICraftingJob job, final BaseActionSource src,
	            final ICraftingRequester requestingMachine,CallbackInfoReturnable donotcare,@Share("prevS")  LocalRef<Long> prev) {
	  if(ex!=null)
		this.availableStorage=ex.qureyStorage();
	  prev.set(availableStorage);
	  }
	  
	@Inject(at = { @At(
			ordinal=0,
			
			shift = Shift.BEFORE,value="FIELD",opcode=Opcodes.GETFIELD, target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;providers:Ljava/util/HashMap;") }
	
	,method="submitJob",cancellable=true)
	
	  public void submitJobPost(final IGrid g, final ICraftingJob job, final BaseActionSource src,
	            final ICraftingRequester requestingMachine,CallbackInfoReturnable c,@Share("prevS")  LocalRef<Long> prev) {
	  if(ex!=null)
		if(!ex.useStorage((CraftingCPUCluster)(Object)this,prev.get(),job.getByteTotal())){c.setReturnValue(null);};
	  }
	
	
	
	@Inject(at = { @At("HEAD") },method="updateCraftingLogic",cancellable=true)
	 public void start(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cc ,CallbackInfo ci) 
	{
		
		if(ex!=null){
			
			if(ex.isOn()==false)ci.cancel();
			
			
		}
		
	 }
	
	@Inject(at = { @At("HEAD") },method="getName",cancellable=true)
	
	  public void getName(CallbackInfoReturnable r) {
	  if(ex!=null)
		r.setReturnValue(
				
				ex.getName()+
				
				//(((CraftingCPUCluster)(Object)this).isBusy()?"(Allocated)":"(Submit to allocate)")
				//⧗⏳ ⏳
				(((CraftingCPUCluster)(Object)this).isBusy()?"(⌛)":"(+)")
				);
	  }
}
