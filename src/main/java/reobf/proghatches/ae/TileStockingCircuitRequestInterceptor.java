package reobf.proghatches.ae;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.GridFlags;
import appeng.api.storage.data.IAEStack;
import appeng.tile.grid.AENetworkTile;
import appeng.util.item.AEItemStack;
import reobf.proghatches.main.MyMod;

public class TileStockingCircuitRequestInterceptor extends AENetworkTile {

    public TileStockingCircuitRequestInterceptor() {
        this.getProxy()
            .setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public TileStockingCircuitRequestInterceptor(int type) {
        this.getProxy()
            .setFlags(GridFlags.REQUIRE_CHANNEL);
        this.type = type;
    }

    int type;

    public boolean isAllowed(IAEStack stack) {
        if (stack == null) return true;
        if (type == 0) return !(stack.isItem() && ((AEItemStack) stack).getItem() == MyMod.progcircuit);

        if (type == 1 && mark[0] != null) {
            return !stack.equals(mark[0]);
        }
        if (type == 2) {
            return true;
        }
        return true;
    }

    ItemStack[] mark = new ItemStack[1];

    @Override
    public void writeToNBT_AENetwork(NBTTagCompound data) {
        data.setInteger("Interceptor_type", type);
        super.writeToNBT_AENetwork(data);
    }

    @Override
    public void readFromNBT_AENetwork(NBTTagCompound data) {
        type = data.getInteger("Interceptor_type");
        super.readFromNBT_AENetwork(data);
    }
}
