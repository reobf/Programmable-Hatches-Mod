package reobf.proghatches.ae;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.networking.GridFlags;
import appeng.api.storage.data.IAEStack;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
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

    // AE2 dispatches these via @TileEvent found through getMethods()+getAnnotation(); method
    // annotations are NOT inherited in Java, so an un-annotated override SHADOWS the annotated
    // super method and unregisters BOTH hooks entirely — after a restart the tile reloaded with
    // type=0 (and no proxy NBT), which is why the interceptor "stopped working". The overrides
    // must re-declare @TileEvent. The marked item was additionally never persisted at all.
    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    @Override
    public void writeToNBT_AENetwork(NBTTagCompound data) {
        data.setInteger("Interceptor_type", type);
        if (mark[0] != null) data.setTag("Interceptor_mark", mark[0].writeToNBT(new NBTTagCompound()));
        super.writeToNBT_AENetwork(data);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    @Override
    public void readFromNBT_AENetwork(NBTTagCompound data) {
        type = data.getInteger("Interceptor_type");
        if (data.hasKey("Interceptor_mark")) mark[0] = ItemStack.loadItemStackFromNBT(data.getCompoundTag("Interceptor_mark"));
        super.readFromNBT_AENetwork(data);
    }
}
