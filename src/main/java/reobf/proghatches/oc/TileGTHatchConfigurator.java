package reobf.proghatches.oc;

import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.machine.Value;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;

public class TileGTHatchConfigurator extends TileEntity implements li.cil.oc.api.network.Environment {

    public TileGTHatchConfigurator() {}

    boolean init;

    @Override
    public void updateEntity() {
        if (init == false) if (node != null) {
            init = true;
            li.cil.oc.api.Network.joinOrCreateNetwork(this);
        }
        super.updateEntity();
    }

    private Node node = li.cil.oc.api.Network.newNode(this, Visibility.Network)

        .withComponent("configurator")
        .create();

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagCompound t = new NBTTagCompound();
        Optional.ofNullable(node())
            .ifPresent(s -> s.save(t));
        nbt.setTag("node", t);
        // nbt.setString("UUID", thisUUID.toString());
        // nbt.setBoolean("inrange", inrange);
        // nbt.setBoolean("oneComputer", oneComputer);
        super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        Optional.ofNullable(nbt.getTag("node"))
            .ifPresent(s -> { if (node() != null) node().load((NBTTagCompound) s); });
        // thisUUID = UUID.fromString(nbt.getString("UUID"));
        // inrange = nbt.getBoolean("inrange");
        // oneComputer = nbt.getBoolean("oneComputer");
        super.readFromNBT(nbt);
    }

    @Override
    public Node node() {
        return node;
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

    public static class Val implements Value {

        public Val() {

        }

        public Val(String addr) {
            this.addr = addr;
        }

        String addr;

        @Callback(doc = "address")
        public Object[] address(final Context context, final Arguments args) {
            validate(context);
            return new Object[] { addr };
        }

        @Override
        public void load(NBTTagCompound nbt) {
            addr = nbt.getString("addr");
        }

        @Override
        public void save(NBTTagCompound nbt) {
            nbt.setString("addr", addr);

        }

        private void validate(Context context) {
            /*
             * Node n= context.node().network().node(addr);
             * n.host().
             */
            /*
             * Environment host = n.host();
             * CompoundBlockEnvironment c=(CompoundBlockEnvironment) host;
             * scala.collection.JavaConverters.asJavaCollectionConverter(c.environments().toList())
             * .asJavaCollection().forEach((a)->{
             * li.cil.oc.api.driver.SidedBlock blk= (SidedBlock) a._2;
             * blk.
             * });;
             */
            // System.out.println(host);

        }

        @Override
        public Object apply(Context context, Arguments arguments) {
            return null;
        }

        @Override
        public void unapply(Context context, Arguments arguments) {}

        @Override
        public Object[] call(Context context, Arguments arguments) {
            return null;
        }

        @Override
        public void dispose(Context context) {}

    }

    @Callback(doc = "proxy")
    public Object[] proxy(final Context context, final Arguments args) {
        String addr = args.checkString(0);
        Node n = node().network()
            .node(addr);

        if (n != null) return new Object[] { new Val(addr) };

        return new Object[] { new Val(addr) };
    }
}
