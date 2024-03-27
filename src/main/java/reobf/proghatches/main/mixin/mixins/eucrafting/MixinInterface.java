package reobf.proghatches.main.mixin.mixins.eucrafting;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.misc.TileInterface;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.EUUtil;
@Mixin(value = TileInterface.class, remap = false, priority = 1)
public abstract  class MixinInterface  implements IEnergyConnected, ISegmentedInventory {

	@Override
	public byte getColorization() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte setColorization(byte aColor) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long injectEnergyUnits(ForgeDirection side, long aVoltage, long aAmperage) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean inputEnergyFrom(ForgeDirection side) {
	
		return false;
	}

	@Override
	public boolean outputsEnergyTo(ForgeDirection side) {
	
		return EUUtil.check(this);
	}

}
