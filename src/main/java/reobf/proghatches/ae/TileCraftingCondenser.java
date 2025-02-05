
package reobf.proghatches.ae;

import appeng.tile.crafting.TileCraftingTile;

public class TileCraftingCondenser extends TileCraftingTile implements ICondenser {

    public TileCraftingCondenser() {

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
        return getSkips() == Integer.MAX_VALUE;
    }

    @Override
    public int getSkips() {

        return ((BlockCraftingCondenser) getBlockType()).getSkips();
    }

}
