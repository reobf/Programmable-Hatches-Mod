package reobf.proghatches.ae;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.MECraftingInventory;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;

public class PatternCraftingJob implements ICraftingJob {

    public PatternCraftingJob(ICraftingPatternDetails a, IStorageGrid b) {
        target = a;
        context = b;
        st=new MECraftingInventory( b,false,false,false);
    }
    MECraftingInventory st;
    public MECraftingInventory getStorageAtBeginning(){

    	return st;

    };
    public boolean supportsCPUCluster(final ICraftingCPU cluster) {
        return true;
    }

    int times = 1;
    ICraftingPatternDetails target;

    @Override
    public boolean isSimulation() {

        return false;
    }

    @Override
    public long getByteTotal() {

        return 1;// don't care
    }

    @Override
    public void populatePlan(IItemList plan) {

    }

    @Override
    public IAEStack<?> getOutput() {
        for (IAEStack<?> stack : target.getAEOutputs()) {
            if (stack != null) return stack.copy().setStackSize(Math.multiplyExact(stack.getStackSize(), times));
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

    public int canBeDone(AENetworkProxy gg, MachineSource src) {
        int max = Integer.MAX_VALUE;
        try {
            IStorageGrid g = gg.getStorage();
            for (IAEStack<?> out : target.getCondensedAEOutputs()) {
                if (out != null && out.getStackSize() > 0)
                    max = (int) Math.min(max, Long.MAX_VALUE / out.getStackSize());
            }
            for (IAEStack<?> in : target.getCondensedAEInputs()) {
                if (in == null || in.getStackSize() == 0) continue;
                if (in.getStackSize() < 0) return 0;
                IMEInventory store = g.getMEMonitor(in.getStackType());
                if (store == null) return 0;
                IAEStack<?> available = store.extractItems(in.copy().setStackSize(Long.MAX_VALUE), Actionable.SIMULATE, src);
                if (available == null) return 0;
                max = (int) Math.min(max, available.getStackSize() / in.getStackSize());
            }
            return max;
        } catch (GridAccessException E) {
            return 0;
        }
    }

    public final IStorageGrid context;

    @Override
    public void startCrafting(MECraftingInventory storage, ICraftingCPU craftingCPUCluster, BaseActionSource src) {
        List<IAEStack<?>> withdrawn = new ArrayList<>();
        try {
            for (IAEStack<?> in : target.getCondensedAEInputs()) {
                if (in == null || in.getStackSize() == 0) continue;
                IAEStack<?> need = in.copy().setStackSize(Math.multiplyExact(in.getStackSize(), times));
                IMEInventory store = context.getMEMonitor(in.getStackType());
                IAEStack<?> got = store == null ? null : store.extractItems(need, Actionable.MODULATE, src);
                if (got != null) withdrawn.add(got);
                if (got == null || got.getStackSize() != need.getStackSize()) throw new CraftBranchFailure(need, 0);
            }
        } catch (RuntimeException e) {
            for (IAEStack<?> s : withdrawn) {
                IMEInventory store = context.getMEMonitor(s.getStackType());
                store.injectItems(s, Actionable.MODULATE, src);
            }
            throw e;
        }
        CraftingCPUCluster cpu = (CraftingCPUCluster) craftingCPUCluster;
        for (IAEStack<?> s : withdrawn) cpu.addStorage(s);
        cpu.addCrafting(target, times);
    }
}
