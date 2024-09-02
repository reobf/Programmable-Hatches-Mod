package reobf.proghatches.oc;


import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemCPU extends Item implements   li.cil.oc.api.driver.item.HostAware,
li.cil.oc.api.driver.item.Processor{

	@Override
	public boolean worksWith(ItemStack stack) {
		
		return stack.getItem() instanceof ItemCPU;
	}

	@Override
	public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
		
		return null;
	}

	@Override
	public String slot(ItemStack stack) {
	
		return Slot.CPU;
	}

	@Override
	public int tier(ItemStack stack) {
		
		return 0;
	}

	@Override
	public NBTTagCompound dataTag(ItemStack stack) {
	
		return null;
	}

	@Override
	public int supportedComponents(ItemStack stack) {
		
		return 100;
	}

	@Override
	public Class<? extends Architecture> architecture(ItemStack stack) {
	
		return  Arch.class;
	}

	@Override
	public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {
		
		return stack.getItem() instanceof ItemCPU;
	}

}
