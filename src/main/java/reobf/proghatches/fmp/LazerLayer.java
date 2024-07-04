package reobf.proghatches.fmp;

import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import gregtech.api.interfaces.tileentity.IColoredTileEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.ILazer;

public class LazerLayer extends LayerBase implements ILazer ,IColoredTileEntity{

	@Override
	public boolean canConnect(ForgeDirection side) {
	IPart pt = getPart(side);
	if(pt instanceof ILazer){return ((ILazer) pt).canConnect(side);}
		return false;
	}

	@Override
	public byte getColorization() {
		for(ForgeDirection side:ForgeDirection.VALID_DIRECTIONS){
		IPart pt = getPart(side);
		if(pt instanceof ILazer){return ((ILazer) pt).getColorization();}
		}
		
		return -1;
	}

	@Override
	public byte setColorization(byte aColor) {
		
		for(ForgeDirection side:ForgeDirection.VALID_DIRECTIONS){
			IPart pt = getPart(side);
			if(pt instanceof ILazer){return ((ILazer) pt).setColorization(aColor);}
			}
			
		return -1;
	}

}
