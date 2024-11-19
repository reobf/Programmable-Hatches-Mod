package reobf.proghatches.ae;

import appeng.api.networking.GridFlags;
import appeng.tile.grid.AENetworkTile;
import net.minecraft.tileentity.TileEntity;

public class TileStockingCircuitRequestInterceptor extends AENetworkTile {

    public TileStockingCircuitRequestInterceptor() {
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

}
