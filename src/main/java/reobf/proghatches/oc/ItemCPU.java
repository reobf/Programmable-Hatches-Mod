package reobf.proghatches.oc;

import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.server.machine.Callbacks;

public class ItemCPU extends Item implements li.cil.oc.api.driver.item.HostAware, li.cil.oc.api.driver.item.Processor {

    @Override
    public boolean worksWith(ItemStack stack) {

        return stack.getItem() instanceof ItemCPU;
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {

        return new APIEnv(stack);
    }

    public class APIEnv implements ManagedEnvironment {

        @Override
        public void update() {
            node().network()
                .nodes()
                .forEach(s -> {

                    ;
                    System.out.println(Callbacks.apply(s.host()));
                    System.out.println(
                        Callbacks.fromClass(
                            s.host()
                                .getClass()));

                });

        }

        // public RedstoneEnv(EnvironmentHost
        // env){this.env=env;};EnvironmentHost env;
        private Node _node = Network.newNode(this, Visibility.Network)

            .create();

        public APIEnv(ItemStack stack) {
            this.stack = stack;
        }

        ItemStack stack;

        @Override
        public Node node() {
            return _node;

        }

        @Override
        public void onConnect(Node node) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDisconnect(Node node) {

        }

        @Override
        public void onMessage(Message message) {

        }

        @Override
        public void load(NBTTagCompound nbt) {
            Optional.ofNullable(nbt.getTag("node"))
                .ifPresent(s -> { if (node() != null) node().load((NBTTagCompound) s); });

        }

        @Override
        public void save(NBTTagCompound nbt) {
            NBTTagCompound t = new NBTTagCompound();
            Optional.ofNullable(node())
                .ifPresent(s -> s.save(t));
            nbt.setTag("node", t);
        }

        @Override
        public boolean canUpdate() {

            return true;
        }

    }

    @Override
    public String slot(ItemStack stack) {

        return Slot.CPU;
    }

    @Override
    public int tier(ItemStack stack) {

        return 0;
    }

    @Override
    public NBTTagCompound dataTag(ItemStack stack) {

        return null;
    }

    @Override
    public int supportedComponents(ItemStack stack) {

        return 100;
    }

    @Override
    public Class<? extends Architecture> architecture(ItemStack stack) {

        return Arch.class;
    }

    @Override
    public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {

        return stack.getItem() instanceof ItemCPU;
    }

}
