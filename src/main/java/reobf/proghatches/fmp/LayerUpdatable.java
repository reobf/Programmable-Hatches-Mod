package reobf.proghatches.fmp;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.ae.part2.PartCoW;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import gregtech.api.interfaces.tileentity.IMachineBlockUpdateable;

public class LayerUpdatable extends LayerBase implements IMachineBlockUpdateable {

	@Override
	public void onMachineBlockUpdate() {
	
		
		for(ForgeDirection side:ForgeDirection.VALID_DIRECTIONS){
		
		IPart pt = this.getPart(side);
		if(pt instanceof IUpdatable){
			
			IUpdatable cw=(IUpdatable) pt;
			cw.update();
			
		}
		}
		
		
	}
@Override
public boolean isMachineBlockUpdateRecursive() {
	
	return false;
}
  

}
