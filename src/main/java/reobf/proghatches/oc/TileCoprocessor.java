package reobf.proghatches.oc;

import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;

public class TileCoprocessor extends TileEntity implements li.cil.oc.api.network.Environment {

    Node node_ = li.cil.oc.api.Network.newNode(this, Visibility.Network)
        .withComponent("coprocessor")
        .create();

    @Override
    public Node node() {

        return node_;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        Optional.ofNullable(node_)
            .ifPresent(s -> s.save(compound));
        super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        Optional.ofNullable(node_)
            .ifPresent(s -> s.load(compound));
        super.readFromNBT(compound);
    }

    @Override
    public void onConnect(Node node) {

    }

    @Override
    public void onDisconnect(Node node) {

    }

    @Override
    public void onMessage(Message message) {

    }

}
