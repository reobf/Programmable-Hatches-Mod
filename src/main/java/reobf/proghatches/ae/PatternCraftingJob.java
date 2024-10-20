package reobf.proghatches.ae;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.MECraftingInventory;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import net.minecraft.item.ItemStack;

public class PatternCraftingJob implements ICraftingJob{
	public PatternCraftingJob(ICraftingPatternDetails a, IStorageGrid b){
		target=a;
		context =b;
	}public  boolean supportsCPUCluster(final ICraftingCPU cluster) {
        return true;
    }
	int times=1;
	ICraftingPatternDetails target;
	@Override
	public boolean isSimulation() {
		
		return false;
	}

	@Override
	public long getByteTotal() {
		
		return 1;//don't care
	}

	@Override
	public void populatePlan(IItemList<IAEItemStack> plan) {
		
		
	}

	@Override
	public IAEItemStack getOutput() {
		for(IAEItemStack is:target.getOutputs()){
			if(is!=null)return is.copy().setStackSize(times*is.getStackSize());
		}
		return null;
	}

	@Override
	public boolean simulateFor(int milli) {
	
		return false;
	}

	@Override
	public Future<ICraftingJob> schedule() {
	
		return CompletableFuture.completedFuture(this);
	}
	
public int canBeDone(AENetworkProxy gg,MachineSource src){
	int max=Integer.MAX_VALUE;
	try{
		IStorageGrid g=gg.getStorage();
		gg.getCrafting().getCraftingPatterns().containsValue(target);
	for(IAEItemStack is:target.getCondensedInputs())
	{
		IAEItemStack ext = g.getItemInventory().extractItems(is.copy().setStackSize(Integer.MAX_VALUE)
				, Actionable.SIMULATE,src);
		if(ext==null){return 0;}
		if(ext.getStackSize()>=is.getStackSize()){
			max=Math.min((int) (ext.getStackSize()/is.getStackSize()),max);
		}else return 0;
		
	}
	return max;
	}catch(
			GridAccessException E){return 0;}
}
public final IStorageGrid context;
@Override
public void startCrafting(MECraftingInventory storage, ICraftingCPU craftingCPUCluster, BaseActionSource src) {
	((CraftingCPUCluster) craftingCPUCluster).addCrafting(target, times);
	IAEItemStack failing=null;
	boolean success=true;
	LinkedList<IAEItemStack> all=new LinkedList<>();
	for(IAEItemStack is:target.getCondensedInputs())
	{
		is=is.copy().setStackSize(is.getStackSize()*times);
		
		
		IAEItemStack ext = context.getItemInventory().extractItems(is, Actionable.MODULATE,src);
		if(ext==null){success=false;failing=is;}else
		if(ext.getStackSize()!=is.getStackSize()){success=false;failing=is;}
		
		if(ext!=null)
			all.add(ext);
		
		if(!success)break;
	}
	
	
	
	if(!success){
	all.forEach(s->context.getItemInventory().injectItems(s, Actionable.MODULATE,src));
		
	throw new CraftBranchFailure(failing, 0);
	}else{
		all.forEach(s->((CraftingCPUCluster) craftingCPUCluster).addStorage(s));
		
	}
	
}
/*
private ArrayList<IAEItemStack> getExtractItems(IAEItemStack ingredient, ICraftingPatternDetails patternDetails) {
    ArrayList<IAEItemStack> list = new ArrayList<>();
  {
        final IAEItemStack extractItems = this.inventory
                .extractItems(ingredient, Actionable.SIMULATE, this.machineSrc);
        final ItemStack is = extractItems == null ? null : extractItems.getItemStack();
        if (is != null && is.stackSize == ingredient.getStackSize()) {
            list.add(extractItems);
            return list;
        }
    }
    return list;
}

private boolean canCraft(final ICraftingPatternDetails details, final IAEItemStack[] condensedInputs) {
    for (IAEItemStack g : condensedInputs) {
        if (getExtractItems(g, details).isEmpty()) {
            return false;
        }
    }
    return true;
}*/
}
