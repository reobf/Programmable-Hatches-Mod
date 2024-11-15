package reobf.proghatches.fmp;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.ILazer;

public class LayerCraftingMachine extends LayerBase implements ICraftingMachine{

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
			ForgeDirection ejectionDirection) {
		  IPart part = this.getPart(ejectionDirection);
		  if(part!=null){
			  if(part instanceof ICraftingMachinePart){
				  
				return   ((ICraftingMachinePart) part).pushPattern(patternDetails, table, ejectionDirection) ;
			 }
		 }
		return false;
	}
	public static class StateHolder{
		
		static public ForgeDirection state=ForgeDirection.UNKNOWN;
		
	}
	
	
	@Override
	public boolean acceptsPlans() {
		//System.out.println(state);
		  IPart part = this.getPart(StateHolder.state);
		  if(part!=null){
			  if(part instanceof ICraftingMachinePart){
				return   ((ICraftingMachinePart) part).acceptsPlans(StateHolder.state);
			 }
		 }
		  
		  
		return false;
	}

}
