package reobf.proghatches.fmp;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import appeng.api.util.AEColor;
import appeng.fmp.CableBusPart;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.AEBasePart;
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
		if(pt instanceof ILazer){
			if(pt instanceof AEBasePart){
			AEColor col = ((AEBasePart) pt).getHost().getColor();
			if(col==AEColor.Transparent)return -1;
				
				return (byte) (15-col.ordinal());
			}
			
	
			
		}
		}
	
		
		return -1;
	}

	@Override
	public byte setColorization(byte aColor) {
		
		
		return -1;
	}
	
	public  boolean isHost() {
		return true;
	}
	public ILazer getLazerP2PIn(ForgeDirection dir) {
		IPart pt = getPart(dir);
		if(pt instanceof ILazer){
		return 	((ILazer) pt).getLazerP2PIn(dir);
		}
		
		return null;
	}
	public ForgeDirection getLazerDir() {
		return null;
	}
	public List<ILazer> getLazerP2POuts() {
		return null;
	}
}
