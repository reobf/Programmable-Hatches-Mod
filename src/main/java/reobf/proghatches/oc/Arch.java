package reobf.proghatches.oc;

import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.common.component.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
@Architecture.Name("?")
@Architecture.NoMemoryRequirements
public class Arch  implements Architecture{
	private li.cil.oc.server.machine.Machine machine;

	public Arch(Machine machine) {
        this.machine = (li.cil.oc.server.machine.Machine) machine;
    }
	@Override
	public boolean isInitialized() {
		
		return true;
	}

	@Override
	public boolean recomputeMemory(Iterable<ItemStack> components) {
		
		return true;
	}

	@Override
	public boolean initialize() {
		
		return true;
	}

	@Override
	public void close() {
	
		
	}

	@Override
	public void runSynchronized() {

			machine.node().network().nodes().forEach(s->{
				
				if(s.host() instanceof li.cil.oc.common.component.Screen){
					
					li.cil.oc.common.component.Screen sc=(Screen) s.host();
					sc.set(1, 1, "hello", false);
					
					
				};
				
				
				
			});
	}

	@Override
	public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
	
		return  new li.cil.oc.api.machine.ExecutionResult.SynchronizedCall();
	}

	@Override
	public void onSignal() {
		
		
	}

	@Override
	public void onConnect() {
		
		
	}

	@Override
	public void load(NBTTagCompound nbt) {
		
	}

	@Override
	public void save(NBTTagCompound nbt) {
	
		
	}

}
