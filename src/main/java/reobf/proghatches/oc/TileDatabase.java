package reobf.proghatches.oc;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.IGridNode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.server.component.UpgradeDatabase;
import reobf.proghatches.gt.metatileentity.util.ArrayListInv;

public class TileDatabase extends TileEntity implements li.cil.oc.api.network.Environment, IGridProxyable {

    static class Database extends UpgradeDatabase {

        public Database(IInventory data) {
            super(data);

        }
    }

    List<ItemStack> list;
    ArrayListInv inv = new ArrayListInv(list);
    Database data = new Database(inv);

    @Override
    public Node node() {

        return data.node();
    }

    @Override
    public void onConnect(Node node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDisconnect(Node node) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMessage(Message message) {
        // TODO Auto-generated method stub

    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void securityBreak() {
        // TODO Auto-generated method stub

    }

    @Override
    public AENetworkProxy getProxy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DimensionalCoord getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void gridChanged() {
        // TODO Auto-generated method stub

    }

    IItemList snapshot = StorageChannel.ITEMS.createList();

    @Override
    public void updateEntity() {
        try {
            snapshot.resetStatus();
            getProxy().getStorage()
                .getItemInventory()
                .getAvailableItems(snapshot);
        } catch (GridAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.updateEntity();
    }

}
