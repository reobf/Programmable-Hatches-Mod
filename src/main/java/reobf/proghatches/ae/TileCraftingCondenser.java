
package reobf.proghatches.ae;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.block.crafting.BlockAdvancedCraftingStorage;
import appeng.block.crafting.BlockSingularityCraftingStorage;
import appeng.tile.crafting.TileCraftingTile;

public class TileCraftingCondenser extends TileCraftingTile implements ICondenser {


	public TileCraftingCondenser(){
		
		
	}

    @Override
    public boolean isAccelerator() {
        return false;
    }

    @Override
    public boolean isStorage() {
        return false;
    }
@Override
public boolean isinf() {
	// TODO Auto-generated method stub
	return  getSkips()==Integer.MAX_VALUE;
}
	@Override
	public int getSkips() {
	
		return ((BlockCraftingCondenser)getBlockType()).getSkips();
	}

   
}
