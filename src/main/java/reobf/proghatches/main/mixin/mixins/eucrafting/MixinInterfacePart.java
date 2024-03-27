package reobf.proghatches.main.mixin.mixins.eucrafting;

import org.spongepowered.asm.mixin.Mixin;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPart;
import appeng.parts.PartBasicState;
import appeng.parts.misc.PartInterface;
import appeng.parts.p2p.IPartGT5Power;
import appeng.tile.misc.TileInterface;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.EUUtil;
import reobf.proghatches.eucrafting.IEUSource;
@Mixin(value = PartInterface.class, remap = false, priority = 1)
public abstract class MixinInterfacePart implements IPartGT5Power, ISegmentedInventory,IPart{

	

	@Override
	public long injectEnergyUnits(long voltage, long amperage) {
		
		return 0;
	}

	@Override
	public boolean inputEnergy() {
	
		return false;
	}

	@Override
	public boolean outputsEnergy() {
		//System.out.println(getGridNode().getGrid().getCache(IEUSource.class).toString());
		return EUUtil.check(this);
	}
	
}
