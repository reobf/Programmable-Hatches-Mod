package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.misc.TileInterface;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import net.minecraftforge.common.util.ForgeDirection;
@Mixin(value = TileInterface.class, remap = false, priority = 1)
public  class MixinInterface implements IEnergyConnected {

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
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean outputsEnergyTo(ForgeDirection side) {
		// TODO Auto-generated method stub
		return true;
	}

}
