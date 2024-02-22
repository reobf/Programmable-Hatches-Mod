package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;

import appeng.parts.misc.PartInterface;
import appeng.parts.p2p.IPartGT5Power;
import appeng.tile.misc.TileInterface;
@Mixin(value = PartInterface.class, remap = false, priority = 1)
public class MixinInterfacePart implements IPartGT5Power{

	@Override
	public long injectEnergyUnits(long voltage, long amperage) {
		
		return 0;
	}

	@Override
	public boolean inputEnergy() {
	
		return true;
	}

	@Override
	public boolean outputsEnergy() {
	
		return true;
	}

}
