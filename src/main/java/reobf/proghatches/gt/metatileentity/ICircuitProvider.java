package reobf.proghatches.gt.metatileentity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

public interface ICircuitProvider {
	public void clearDirty();

	public boolean patternDirty();

	public Collection<ItemStack> getCircuit();

	public default boolean checkLoop(HashSet<Object> blacklist) {

		return blacklist.add(this);

	};

}
