package reobf.proghatches.block;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.ICustomNameObject;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.gui.modularui.GUITextureSet;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.internal.MultiTank;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.integration.appeng.NetworkControl;
import li.cil.oc.integration.appeng.NetworkControl$class;
import li.cil.oc.server.component.traits.*;
import li.cil.oc.util.BlockPosition;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassReader;
import reobf.proghatches.main.asm.repack.objectwebasm.ClassWriter;
import reobf.proghatches.main.asm.repack.objectwebasm.tree.ClassNode;
import scala.Option;
import scala.Some;
import scala.Tuple2;
import scala.collection.immutable.IndexedSeq;
import scala.collection.mutable.Buffer;
import scala.reflect.ClassTag;

public class TileIOHub extends TileEntity implements li.cil.oc.api.network.Environment, SidedEnvironment,
    /*
     * WorldInventoryAnalytics,WorldTankAnalytics,
     * WorldFluidContainerAnalytics, TankInventoryControl,
     * InventoryAnalytics, MultiTank, InventoryTransfer,
     * FluidContainerTransfer, InventoryControl, TankControl,
     * ItemInventoryControl, InventoryWorldControlMk2, NetworkControl ,
     */
    IInventory, IFluidHandler, IGridProxyable, ITileWithModularUI, ICustomNameObject, IActionHost {

    public TileIOHub() {

    }

    private int offset = 0;

    @Override
    public Node node() {

        return node;
    }

    @Override
    public void onMessage(Message message) {

        // subapi.forEach((__,s)->s.onMessage(message));
    }

    @Override
    public void onConnect(Node node) {

    }

    @Override
    public void onDisconnect(Node node) {

    }

    public void markDirty() {
        final Chunk chunk = worldObj.getChunkFromBlockCoords(xCoord, zCoord);
        if (chunk != null) {
            chunk.setChunkModified();
        }
        // this.worldObj.markTileEntityChunkModified(this.xCoord, this.yCoord,
        // this.zCoord, this);

    }

    boolean init;

    public void updateEntity() {

        dead = false;
        super.updateEntity();

        if (init == false) {
            getProxy().onReady();
            init = true;

            if (node != null) {
                li.cil.oc.api.Network.joinOrCreateNetwork(this);
            }
        }
        if (node != null && node.network() != null) {
            subapi.values()
                .stream()
                .filter(
                    s -> s.node()
                        .network() == null)
                .forEach(s -> node.connect(s.node()));
        }

    }

    boolean dead;

    public void onChunkUnload() {
        dead = true;
        super.onChunkUnload();
        if (node != null) node.remove();
        this.getProxy()
            .onChunkUnload();
    }

    public void invalidate() {
        super.invalidate();
        if (node != null) node.remove();
        this.getProxy()
            .invalidate();
    }

    @Override
    public boolean canUpdate() {

        return true;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {

        return getProxy().getNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {

        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public AENetworkProxy getProxy() {

        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", new ItemStack(MyMod.iohub, 1), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            gridProxy.setValidSides(EnumSet.range(ForgeDirection.DOWN, ForgeDirection.EAST));

        }

        // gridProxy.setOwner();

        return this.gridProxy;
    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord(this);
    }

    private AENetworkProxy gridProxy;

    @Override
    public void gridChanged() {

    }

    Map<String, li.cil.oc.api.network.Environment> subapi = new HashMap<>();
    static private HashMap<String, BiFunction<String, TileEntity, Object>> cache = new HashMap<>();
    Connector node;
    {
        node = li.cil.oc.api.Network.newNode(this, Visibility.Network)
            .withConnector()
            .
            // withComponent("iohub").
            create();

        subapi.put("all", new OCApi("iohub"));

        /*
         * subapi.put("item", (li.cil.oc.api.network.Environment)
         * getFilteredClass("item").apply("iohub_item", this));
         * subapi.put("fluid", (li.cil.oc.api.network.Environment)
         * getFilteredClass("fluid").apply("iohub_fluid", this));
         * subapi.put("ae", (li.cil.oc.api.network.Environment)
         * getFilteredClass("ae").apply("iohub_ae", this));
         */}

    @SuppressWarnings("unused")
    private static BiFunction<String, TileEntity, Object> getFilteredClass(String filter) {
        if (cache.containsKey(filter)) {
            return cache.get(filter);
        }
        Class<?> ev = filterAPI(filter);

        cache.put(filter, (arg, arg2) -> {
            try {
                return ev.getConstructor(TileIOHub.class, String.class)
                    .newInstance(arg2, arg);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        });

        return getFilteredClass(filter);

    }

    @Retention(value = RetentionPolicy.RUNTIME)
    public static @interface APIType {

        String[] value();
    }
    // Function<byte[],Class<?>> loader=null;

    private static Class<?> load(byte b[], String ent, String clz) {

        LaunchClassLoader cl = (LaunchClassLoader) TileIOHub.class.getClassLoader();

        try {
            URLStreamHandler handler = new URLStreamHandler() {

                /*
                 * NEI will try converting this to URI, return something of
                 * legal URI format
                 */
                @Override
                protected String toExternalForm(URL u) {
                    return "file:/nonexist";// no real file anyway, just to make
                                            // NEI happy
                }

                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    return new URLConnection(u) {

                        @Override
                        public void connect() {}

                        @Override
                        public InputStream getInputStream() throws IOException {

                            // System.err.println(u.getPath().substring(u.getPath().indexOf('/')+1));
                            if (!u.getPath()
                                .substring(
                                    u.getPath()
                                        .indexOf('/') + 1)
                                .equals(ent)) throw new IOException();

                            // System.err.println(u.getPath());
                            return new ByteArrayInputStream(b);
                        }
                    };
                }
            };
            cl.addURL(new URL("dyngenclassproghatch", null, -1, Math.abs(ent.hashCode()) + "/", handler));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            return cl.findClass(clz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    private static Class<?> filterAPI(String filter) {

        ClassReader cr = null;
        try {
            cr = new ClassReader(
                OCApi.class.getResourceAsStream(
                    "/" + OCApi.class.getName()
                        .replace('.', '/') + ".class"));
        } catch (IOException e) {
            throw new RuntimeException("failed to read .class that's not normal", e);
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {

            @Override
            public int newUTF8(String value) {
                value = value.replace(
                    "reobf/proghatches/block/TileIOHub$OCApi",
                    "reobf/proghatches/block/TileIOHub$OCApi" + filter);
                return super.newUTF8(value);
            }

        };
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        cn.methods.removeIf(s -> {
            if (s.visibleAnnotations != null) {
                long l = s.visibleAnnotations.stream()
                    .filter(c -> {

                        boolean b = c.desc.equals("Lreobf/proghatches/block/TileIOHub$APIType;")
                            && (!((List<?>) c.values.get(1)).contains(filter));
                        return b;
                    })
                    .count();
                /*
                 * if(l>0){ System.out.println("remove "+s.name); }
                 */
                return l > 0;
            }
            return false;
        });

        cr.accept(cn, 0);

        return load(
            cw.toByteArray(),
            "reobf/proghatches/block/TileIOHub$OCApi" + filter + ".class",
            "reobf.proghatches.block.TileIOHub$OCApi" + filter

        );
    }

    /*
     * public static class test{
     * @Callback(doc = "function()")
     * public Object[] test(final Context context, final Arguments args) {
     * return new Object[] { };
     * }
     * }
     */

    //
    // begin of oc
    public class OCApi/* extends test */ implements li.cil.oc.api.network.Environment, WorldInventoryAnalytics,
        WorldTankAnalytics, WorldFluidContainerAnalytics, TankInventoryControl, InventoryAnalytics, MultiTank,
        InventoryTransfer, FluidContainerTransfer, InventoryControl, TankControl, ItemInventoryControl,
        InventoryWorldControlMk2, TankWorldControl, NetworkControl<TileIOHub> {

        @Callback(
            doc = "function():string -- Returns the custom name of this IO Hub, or 'IOHub' if absent. Use quartz cutter to (re-)name.")
        public Object[] getCustomName(final Context context, final Arguments args) {
            return new Object[] { TileIOHub.this.getInventoryName() };
        }

        @Override
        public Object[] getItemsInNetworkById(Context arg0, Arguments arg1) {
            // TODO Auto-generated method stub
            return NetworkControl$class.getItemsInNetworkById(this, arg0, arg1);
        }

        @Callback(
            doc = "function(address:string):boolean -- Swap the inventory between this IO Hub and another IO Hub. Return whether operation successes.")
        public Object[] swap(final Context context, final Arguments args) {
            markDirty();
            final String address = args.checkString(0);
            // final int entry = args.checkInteger(1);
            // final int amount = args.optInteger(2, 1000);

            final Node n = node().network()
                .node(address);
            if (n == null) {
                return new Object[] { false, "no such component" };
                // throw new IllegalArgumentException("no such component");
            }
            if (!(n instanceof Component)) {
                return new Object[] { false, "no such component" };
                // throw new IllegalArgumentException("no such component");
            }
            final li.cil.oc.api.network.Environment env = n.host();
            if (!(env instanceof TileIOHub.OCApi)) {
                return new Object[] { false, "not a iohub" };
                // throw new IllegalArgumentException("not a iohub");
            }
            final TileIOHub.OCApi database = (TileIOHub.OCApi) env;
            database.markDirty();
            // do not directly swap ref just in case...
            ItemStack[] ia = inv.clone();
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] != null) ia[i] = ia[i].copy();
            }
            NBTTagCompound[] fa = Arrays.stream(ft)
                .map(s -> s.writeToNBT(new NBTTagCompound()))
                .toArray(NBTTagCompound[]::new);
            // ItemStack[] ib =
            // Arrays.stream(database.TileIOHubthis().inv).map(ItemStack::copy).toArray(ItemStack[]::new);
            ItemStack[] ib = database.TileIOHubthis().inv.clone();
            for (int i = 0; i < ib.length; i++) {
                if (ib[i] != null) ib[i] = ib[i].copy();
            }
            NBTTagCompound[] fb = Arrays.stream(database.TileIOHubthis().ft)
                .map(s -> s.writeToNBT(new NBTTagCompound()))
                .toArray(NBTTagCompound[]::new);

            for (int i = 0; i < ib.length; i++) {
                database.TileIOHubthis().inv[i] = (ItemStack) ia[i];
                inv[i] = (ItemStack) ib[i];
            }
            for (int i = 0; i < fb.length; i++) {
                database.TileIOHubthis().ft[i].readFromNBT((NBTTagCompound) fa[i]);
                ft[i].readFromNBT((NBTTagCompound) fb[i]);
            }

            return new Object[] { true };

            // throw new UnsupportedOperationException("NYI");
        }

        private TileIOHub TileIOHubthis() {
            return TileIOHub.this;
        }

        private void markDirty() {
            TileIOHub.this.markDirty();
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(tankSide:number, inventorySide:number, inventorySlot:number [, count:number [, sourceTank:number [, outputSide:number[, outputSlot:number]]]]):boolean, number -- Transfer some fluid from the tank to the container. Returns operation result and filled amount")
        public Object[] transferFluidFromTankToContainer(final Context context, final Arguments args) {
            markDirty();
            return FluidContainerTransfer$class.transferFluidFromTankToContainer(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(inventorySide:number, inventorySlot:number, tankSide:number [, count:number [, outputSide:number[, outputSlot:number]]]):boolean, number -- Transfer some fluid from the container to the tank. Returns operation result and filled amount")
        public Object[] transferFluidFromContainerToTank(final Context context, final Arguments args) {
            markDirty();
            return FluidContainerTransfer$class.transferFluidFromContainerToTank(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(sourceSide:number, sourceSlot:number, sinkSide:number, sinkSlot:number[, count:number [, sourceOutputSide:number[, sinkOutputSide:number[, sourceOutputSlot:number[, sinkOutputSlot:number]]]]]):boolean, number -- Transfer some fluid from a container to another container. Returns operation result and filled amount")
        public Object[] transferFluidBetweenContainers(final Context context, final Arguments args) {
            markDirty();
            return FluidContainerTransfer$class.transferFluidBetweenContainers(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(sourceSide:number, sinkSide:number[, count:number[, sourceSlot:number[, sinkSlot:number]]]):number -- Transfer some items between two inventories.")
        public Object[] transferItem(final Context context, final Arguments args) {
            markDirty();
            return InventoryTransfer$class.transferItem(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(sourceSide:number, sinkSide:number[, count:number [, sourceTank:number]]):boolean, number -- Transfer some fluid between two tanks. Returns operation result and filled amount")
        public Object[] transferFluid(final Context context, final Arguments args) {
            markDirty();
            return InventoryTransfer$class.transferFluid(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(side:number, slot:number):number -- Get the capacity of the fluid container in the specified slot of the inventory on the specified side of the device.")
        public Object[] getContainerCapacityInSlot(final Context context, final Arguments args) {
            return WorldFluidContainerAnalytics$class.getContainerCapacityInSlot(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(side:number, slot:number):number -- Get the capacity the fluid container in the specified slot of the inventory on the specified side of the device.")
        public Object[] getContainerLevelInSlot(final Context context, final Arguments args) {
            return WorldFluidContainerAnalytics$class.getContainerLevelInSlot(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(side:number, slot:number):table -- Get a description of the fluid in the fluid container in the specified slot of the inventory on the specified side of the device.")
        public Object[] getFluidInContainerInSlot(final Context context, final Arguments args) {
            return WorldFluidContainerAnalytics$class.getFluidInContainerInSlot(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(side:number [, tank:number]):number -- Get the amount of fluid in the specified tank on the specified side.")
        public Object[] getTankLevel(final Context context, final Arguments args) {
            return WorldTankAnalytics$class.getTankLevel(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(side:number [, tank:number]):number -- Get the capacity of the specified tank on the specified side.")
        public Object[] getTankCapacity(final Context context, final Arguments args) {
            return WorldTankAnalytics$class.getTankCapacity(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(side:number [, tank:number]):table -- Get a description of the fluid in the the specified tank on the specified side.")
        public Object[] getFluidInTank(final Context context, final Arguments args) {
            return WorldTankAnalytics$class.getFluidInTank(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(doc = "function(side:number):number -- Get the number of tanks available on the specified side.")
        public Object[] getTankCount(final Context context, final Arguments args) {
            return WorldTankAnalytics$class.getTankCount(this, context, args);
        }

        @SuppressWarnings("unchecked")
        public Tuple2<Object, String> blockContent(final ForgeDirection side) {
            return (Tuple2<Object, String>) WorldAware$class.blockContent(this, side);
        }

        @SuppressWarnings("unchecked")
        public <Type extends Entity> Buffer<Type> entitiesInBounds(final AxisAlignedBB bounds,
            final ClassTag<Type> evidence$1) {
            return (Buffer<Type>) WorldAware$class.entitiesInBounds(this, bounds, evidence$1);
        }

        @SuppressWarnings("unchecked")
        public <Type extends Entity> Buffer<Type> entitiesInBlock(final BlockPosition blockPos,
            final ClassTag<Type> evidence$2) {
            return (Buffer<Type>) WorldAware$class.entitiesInBlock(this, blockPos, evidence$2);
        }

        @SuppressWarnings("unchecked")
        public <Type extends Entity> Buffer<Type> entitiesOnSide(final ForgeDirection side,
            final ClassTag<Type> evidence$3) {
            return (Buffer<Type>) WorldAware$class.entitiesOnSide(this, side, evidence$3);
        }

        @SuppressWarnings("unchecked")
        public <Type extends Entity> Option<Type> closestEntity(final ForgeDirection side,
            final ClassTag<Type> evidence$4) {
            return (Option<Type>) WorldAware$class.closestEntity(this, side, evidence$4);
        }

        @Override
        public EntityPlayer fakePlayer() {

            return WorldAware$class.fakePlayer(this);
        }

        @Override
        public boolean mayInteract(BlockPosition arg0, ForgeDirection arg1) {

            return true;
        }

        @Override
        public BlockPosition position() {
            return BlockPosition.apply(xCoord, yCoord, zCoord, this.world());
        }

        @Override
        public World world() {

            return worldObj;
        }

        @Override
        public ForgeDirection checkSideForAction(Arguments arg0, int arg1) {
            return li.cil.oc.util.ExtendedArguments$.MODULE$.extendedArguments(arg0)
                .checkSideAny(arg1);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number):number -- Get the number of slots in the inventory on the specified side of the device.")
        public Object[] getInventorySize(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.getInventorySize(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number, slot:number):number -- Get number of items in the specified slot of the inventory on the specified side of the device.")
        public Object[] getSlotStackSize(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.getSlotStackSize(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number, slot:number):number -- Get the maximum number of items in the specified slot of the inventory on the specified side of the device.")
        public Object[] getSlotMaxStackSize(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.getSlotMaxStackSize(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number, slotA:number, slotB:number[, checkNBT:boolean=false]):boolean -- Get whether the items in the two specified slots of the inventory on the specified side of the device are of the same type.")
        public Object[] compareStacks(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.compareStacks(this, context, args);
        }

        @APIType({ "item" })

        @Callback(
            doc = "function(side:number, slot:number, dbAddress:string, dbSlot:number[, checkNBT:boolean=false]):boolean -- Compare an item in the specified slot in the inventory on the specified side with one in the database with the specified address.")
        public Object[] compareStackToDatabase(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.compareStackToDatabase(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number, slotA:number, slotB:number):boolean -- Get whether the items in the two specified slots of the inventory on the specified side of the device are equivalent (have shared OreDictionary IDs).")
        public Object[] areStacksEquivalent(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.areStacksEquivalent(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number, slot:number, label:string):boolean -- Change the display name of the stack in the inventory on the specified side of the device.")
        public Object[] setStackDisplayName(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.setStackDisplayName(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number, slot:number):table -- Get a description of the stack in the inventory on the specified side of the device.")
        public Object[] getStackInSlot(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.getStackInSlot(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number):userdata -- Get a description of all stacks in the inventory on the specified side of the device.")
        public Object[] getAllStacks(final Context context, final Arguments args) {
            try {
                return WorldInventoryAnalytics$class.getAllStacks(this, context, args);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;

            }

        }

        @Callback(
            doc = "function(side:number):string -- Get the the name of the inventory on the specified side of the device.")
        public Object[] getInventoryName(final Context context, final Arguments args) {
            return WorldInventoryAnalytics$class.getInventoryName(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(side:number, slot:number, dbAddress:string, dbSlot:number):boolean -- Store an item stack description in the specified slot of the database with the specified address.")
        public Object[] store(final Context context, final Arguments args) {
            markDirty();
            return WorldInventoryAnalytics$class.store(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function([slot:number]):number -- Get the amount of fluid in the tank item in the specified slot or the selected slot.")
        public Object[] getTankLevelInSlot(final Context context, final Arguments args) {
            return TankInventoryControl$class.getTankLevelInSlot(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function([slot:number]):number -- Get the capacity of the tank item in the specified slot of the robot or the selected slot.")
        public Object[] getTankCapacityInSlot(final Context context, final Arguments args) {
            return TankInventoryControl$class.getTankCapacityInSlot(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function([slot:number]):table -- Get a description of the fluid in the tank item in the specified slot or the selected slot.")
        public Object[] getFluidInTankInSlot(final Context context, final Arguments args) {
            return TankInventoryControl$class.getFluidInTankInSlot(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function([tank:number]):table -- Get a description of the fluid in the tank in the specified slot or the selected slot.")
        public Object[] getFluidInInternalTank(final Context context, final Arguments args) {
            return TankInventoryControl$class.getFluidInInternalTank(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function([amount:number]):boolean -- Transfers fluid from a tank in the selected inventory slot to the selected tank.")
        public Object[] drain(final Context context, final Arguments args) {
            markDirty();
            return TankInventoryControl$class.drain(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function([amount:number]):boolean -- Transfers fluid from the selected tank to a tank in the selected inventory slot.")
        public Object[] fill(final Context context, final Arguments args) {
            markDirty();
            return TankInventoryControl$class.fill(this, context, args);
        }/*
          * @APIType({ "fluid" })
          * @Callback(doc =
          * "function([amount:number]):boolean -- Transfers fluid from the selected tank to a tank in the selected inventory slot."
          * )
          * public Object[] fillRobot(final Context context, final Arguments args) {
          * markDirty();
          * return TankInventoryControl$class.fill(this, context, args);
          * }
          */

        @APIType({ "item" })
        @Callback(
            doc = "function([slot:number]):table -- Get a description of the stack in the specified slot or the selected slot.")
        public Object[] getStackInInternalSlot(final Context context, final Arguments args) {
            return InventoryAnalytics$class.getStackInInternalSlot(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(otherSlot:number):boolean -- Get whether the stack in the selected slot is equivalent to the item in the specified slot (have shared OreDictionary IDs).")
        public Object[] isEquivalentTo(final Context context, final Arguments args) {
            return InventoryAnalytics$class.isEquivalentTo(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(slot:number, dbAddress:string, dbSlot:number):boolean --  an item stack description in the specified slot of the database with the specified address.")
        public Object[] storeInternal(final Context context, final Arguments args) {
            markDirty();
            return InventoryAnalytics$class.storeInternal(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(slot:number, dbAddress:string, dbSlot:number[, checkNBT:boolean=false]):boolean -- Compare an item in the specified slot with one in the database with the specified address.")
        public Object[] compareToDatabase(final Context context, final Arguments args) {
            return InventoryAnalytics$class.compareToDatabase(this, context, args);
        }

        @SuppressWarnings("unchecked")
        @Override
        public IndexedSeq<Object> insertionSlots() {
            return InventoryAware$class.insertionSlots(this);
        }

        @Override
        public IInventory inventory() {
            return TileIOHub.this;
        }

        @Override
        public int optSlot(Arguments args, int n) {
            return InventoryAware$class.optSlot(this, args, n);
        }

        public int selectedSlotIndex() {
            return slotselected - offset;
        }

        @Override
        public int selectedSlot() {

            return slotselected;
        }

        @Override
        public void selectedSlot_$eq(int arg0) {
            markDirty();
            if (arg0 < 0 + offset || arg0 >= inv.length + offset) {
                throw new RuntimeException("invalid slot");
            }
            slotselected = arg0;

        }

        @Override
        public Option<ItemStack> stackInSlot(int arg0) {
            if (arg0 < 0 || arg0 >= inv.length) throw new RuntimeException("invalid slot");
            return new Some<>(TileIOHub.this.getStackInSlot(arg0));
        }

        @SuppressWarnings("unchecked")
        @Override
        public Option<FluidStack> fluidInTank(int arg0) {
            return TankAware$class.fluidInTank(this, arg0);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Option<IFluidTank> getTank(int arg0) {
            return TankAware$class.getTank(this, arg0);
        }

        @Override
        public boolean haveSameFluidType(FluidStack arg0, FluidStack arg1) {
            return TankAware$class.haveSameFluidType(this, arg0, arg1);
        }

        @Override
        public int optTank(Arguments arg0, int arg1) {
            return TankAware$class.optTank(this, arg0, arg1);
        }

        public int selectedTankIndex() {
            return tankselected - offset;
        }

        @Override
        public int selectedTank() {

            return tankselected;
        }

        @Override
        public void selectedTank_$eq(int arg0) {
            markDirty();
            if (arg0 < 0 + offset || arg0 >= ft.length + offset) {
                throw new RuntimeException("invalid slot");
            }
            tankselected = arg0;

        }

        @Override
        public MultiTank tank() {

            return this;
        }

        @Override
        public int tankCount() {

            return ft.length;
        }

        @Override
        public IFluidTank getFluidTank(int index) {

            return ft[index];
        }

        /// robot

        @APIType({ "fluid" })
        @Callback(doc = "function():number -- The number of tanks installed in the device.")
        public Object[] tankCount(final Context context, final Arguments args) {
            return TankControl$class.tankCount(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function([index:number]):number -- Select a tank and/or get the number of the currently selected tank.")
        public Object[] selectTank(final Context context, final Arguments args) {
            int i = optTank(args, 0);
            this.selectedTank_$eq(i);
            return new Object[] { i + 1 };
        }

        @APIType({ "fluid" })
        @Callback(
            direct = true,
            doc = "function([index:number]):number -- Get the fluid amount in the specified or selected tank.")
        public Object[] tankLevel(final Context context, final Arguments args) {
            return TankControl$class.tankLevel(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            direct = true,
            doc = "function([index:number]):number -- Get the remaining fluid capacity in the specified or selected tank.")
        public Object[] tankSpace(final Context context, final Arguments args) {
            return TankControl$class.tankSpace(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(index:number):boolean -- Compares the fluids in the selected and the specified tank. Returns true if equal.")
        public Object[] compareFluidTo(final Context context, final Arguments args) {
            return TankControl$class.compareFluidTo(this, context, args);
        }

        @APIType({ "fluid" })
        @Callback(
            doc = "function(index:number[, count:number=1000]):boolean -- Move the specified amount of fluid from the selected tank into the specified tank.")
        public Object[] transferFluidTo(final Context context, final Arguments args) {
            markDirty();
            return TankControl$class.transferFluidTo(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function([slot:number]):number -- Get the currently selected slot; set the selected slot if specified.")
        public Object[] select(final Context context, final Arguments args) {
            int i = optSlot(args, 0);
            this.selectedSlot_$eq(i);
            return new Object[] { i + 1 };
        }

        @APIType({ "item" })
        @Callback(
            direct = true,
            doc = "function([slot:number]):number -- Get the number of items in the specified slot, otherwise in the selected slot.")
        public Object[] count(final Context context, final Arguments args) {
            return InventoryControl$class.count(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            direct = true,
            doc = "function([slot:number]):number -- Get the remaining space in the specified slot, otherwise in the selected slot.")
        public Object[] space(final Context context, final Arguments args) {

            System.out.println(
                this.inventory()
                    .getInventoryStackLimit());
            return InventoryControl$class.space(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(otherSlot:number[, checkNBT:boolean=false]):boolean -- Compare the contents of the selected slot to the contents of the specified slot.")
        public Object[] compareTo(final Context context, final Arguments args) {
            return InventoryControl$class.compareTo(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(toSlot:number[, amount:number]):boolean -- Move up to the specified amount of items from the selected slot into the specified slot.")
        public Object[] transferTo(final Context context, final Arguments args) {
            markDirty();
            return InventoryControl$class.transferTo(this, context, args);
        }

        @APIType({ "item" })
        @Callback(doc = "function():number -- The size of this device's internal inventory.")
        public Object[] inventorySize(final Context context, final Arguments args) {
            return InventoryControl$class.inventorySize(this, context, args);
        }

        @APIType({ "item" })
        @Callback(doc = "function(slot:number):number -- The size of an item inventory in the specified slot.")
        public Object[] getItemInventorySize(final Context context, final Arguments args) {
            return ItemInventoryControl$class.getItemInventorySize(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(inventorySlot:number, slot:number[, count:number=64]):number -- Drops an item into the specified slot in the item inventory.")
        public Object[] dropIntoItemInventory(final Context context, final Arguments args) {
            markDirty();
            return ItemInventoryControl$class.dropIntoItemInventory(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(inventorySlot:number, slot:number[, count:number=64]):number -- Sucks an item out of the specified slot in the item inventory.")
        public Object[] suckFromItemInventory(final Context context, final Arguments args) {
            markDirty();
            return ItemInventoryControl$class.suckFromItemInventory(this, context, args);
        }

        @APIType({ "item" })

        @Callback(
            doc = "function(facing:number, slot:number[, count:number[, fromSide:number]]):boolean -- Drops the selected item stack into the specified slot of an inventory.")
        public Object[] dropIntoSlot(final Context context, final Arguments args) {
            markDirty();
            return InventoryWorldControlMk2$class.dropIntoSlot(this, context, args);
        }

        @APIType({ "item" })
        @Callback(
            doc = "function(facing:number, slot:number[, count:number[, fromSide:number]]):boolean -- Sucks items from the specified slot of an inventory.")
        public Object[] suckFromSlot(final Context context, final Arguments args) {
            markDirty();
            return InventoryWorldControlMk2$class.suckFromSlot(this, context, args);
        }

        // AE COMPAT
        @APIType({ "ae" })
        @Callback(doc = "function():boolean -- Always return true since it needs no Security Terminal.")
        public Object[] isLinked(final Context context, final Arguments args) {
            return new Object[] { true };
        }

        @APIType({ "ae" })
        @Callback(doc = "function():table -- Get a list of tables representing the available CPUs in the network.")
        public Object[] getCpus(final Context context, final Arguments args) {
            return NetworkControl$class.getCpus(this, context, args);
        }

        @APIType({ "ae" })
        @Callback(
            doc = "function([filter:table]):table -- Get a list of known item recipes. These can be used to issue crafting requests.")
        public Object[] getCraftables(final Context context, final Arguments args) {
            return NetworkControl$class.getCraftables(this, context, args);
        }

        @APIType({ "item", "ae" })
        @Callback(doc = "function([filter:table]):table -- Get a list of the stored items in the network.")
        public Object[] getItemsInNetwork(final Context context, final Arguments args) {
            return NetworkControl$class.getItemsInNetwork(this, context, args);
        }

        @APIType({ "item", "ae" })
        @Callback(doc = "function():userdata -- Get an iterator object for the list of the items in the network.")
        public Object[] allItems(final Context context, final Arguments args) {
            return NetworkControl$class.allItems(this, context, args);
        }

        @APIType({ "item", "ae" })
        @Callback(
            doc = "function(filter:table, dbAddress:string[, startSlot:number[, count:number]]): Boolean -- Store items in the network matching the specified filter in the database with the specified address.")
        public Object[] storeAE(final Context context, final Arguments args) {
            markDirty();
            return NetworkControl$class.store(this, context, args);
        }

        @APIType({ "fluid", "ae" })
        @Callback(doc = "function():table -- Get a list of the stored fluids in the network.")
        public Object[] getFluidsInNetwork(final Context context, final Arguments args) {
            return NetworkControl$class.getFluidsInNetwork(this, context, args);
        }

        @APIType({ "ae" })
        @Callback(doc = "function():number -- Get the average power injection into the network.")
        public Object[] getAvgPowerInjection(final Context context, final Arguments args) {
            return NetworkControl$class.getAvgPowerInjection(this, context, args);
        }

        @APIType({ "ae" })
        @Callback(doc = "function():number -- Get the average power usage of the network.")
        public Object[] getAvgPowerUsage(final Context context, final Arguments args) {
            return NetworkControl$class.getAvgPowerUsage(this, context, args);
        }

        @APIType({ "ae" })
        @Callback(doc = "function():number -- Get the idle power usage of the network.")
        public Object[] getIdlePowerUsage(final Context context, final Arguments args) {
            return NetworkControl$class.getIdlePowerUsage(this, context, args);
        }

        @APIType({ "ae" })
        @Callback(doc = "function():number -- Get the maximum stored power in the network.")
        public Object[] getMaxStoredPower(final Context context, final Arguments args) {
            return NetworkControl$class.getMaxStoredPower(this, context, args);
        }

        @APIType({ "ae" })
        @Callback(doc = "function():number -- Get the stored power in the network. ")
        public Object[] getStoredPower(final Context context, final Arguments args) {
            return NetworkControl$class.getStoredPower(this, context, args);
        }

        @Override
        public Option<String> onTransferContents() {

            return Some.empty();
        }

        @Override
        public TileIOHub tile() {

            return TileIOHub.this;
        }

        public IMEMonitor<IAEItemStack> getItemInventory() {
            IGrid grid = null;
            try {
                grid = getProxy().getGrid();
            } catch (GridAccessException e) {
                throw new RuntimeException("Access Denied");
            }
            if (grid == null) {
                return null;
            }
            final IStorageGrid storage = (IStorageGrid) grid.getCache(IStorageGrid.class);
            if (storage == null) {
                return null;
            }
            return (IMEMonitor<IAEItemStack>) storage.getItemInventory();
        }

        @APIType({ "item", "ae" })
        @Callback(doc = "function([number:amount]):number -- Transfer selected items to your ae system.")
        public Object[] sendItems(final Context context, final Arguments args) {
            markDirty();

            final IInventory invRobot = TileIOHub.this;
            if (invRobot.getSizeInventory() <= 0) {
                return new Object[] { 0 };
            }
            final ItemStack stack = invRobot.getStackInSlot(selectedSlotIndex());
            final IMEMonitor<IAEItemStack> inv = this.getItemInventory();
            if (stack == null || inv == null) {
                return new Object[] { 0 };
            }
            final int amount = Math.min(args.optInteger(0, 64), stack.stackSize);
            final ItemStack stack2 = stack.copy();
            stack2.stackSize = amount;
            final IAEItemStack notInjected = (IAEItemStack) inv.injectItems(
                AEApi.instance()
                    .storage()
                    .createItemStack(stack2),
                Actionable.MODULATE,
                new MachineSource((IActionHost) this.tile()));
            Object[] array;
            if (notInjected == null) {
                stack.stackSize -= amount;
                if (stack.stackSize <= 0) {
                    invRobot.setInventorySlotContents(selectedSlotIndex(), (ItemStack) null);
                } else {
                    invRobot.setInventorySlotContents(selectedSlotIndex(), stack);
                }
                array = new Object[] { amount };
            } else {
                stack.stackSize = stack.stackSize - amount + (int) notInjected.getStackSize();
                if (stack.stackSize <= 0) {
                    invRobot.setInventorySlotContents(selectedSlotIndex(), (ItemStack) null);
                } else {
                    invRobot.setInventorySlotContents(selectedSlotIndex(), stack);
                }
                array = new Object[] { stack2.stackSize - notInjected.getStackSize() };
            }
            return array;
        }

        @APIType({ "item", "ae" })
        @Callback(
            doc = "function(database:address, entry:number[, number:amount]):number -- Get items from your ae system.")
        public Object[] requestItems(final Context context, final Arguments args) {
            markDirty();
            final String address = args.checkString(0);
            final int entry = args.checkInteger(1);
            final int amount = args.optInteger(2, 64);
            final int selected = selectedSlotIndex();
            final IInventory invRobot = TileIOHub.this;
            if (invRobot.getSizeInventory() <= 0) {
                return new Object[] { 0 };
            }
            final IMEMonitor<IAEItemStack> inv = this.getItemInventory();
            if (inv == null) {
                return new Object[] { 0 };
            }
            final Node n = this.node()
                .network()
                .node(address);
            if (n == null) {
                throw new IllegalArgumentException("no such component");
            }
            if (!(n instanceof Component)) {
                throw new IllegalArgumentException("no such component");
            }
            final li.cil.oc.api.network.Environment env = n.host();
            if (!(env instanceof Database)) {
                throw new IllegalArgumentException("not a database");
            }
            final Database database = (Database) env;
            final ItemStack sel = invRobot.getStackInSlot(selected);
            final int inSlot = (sel == null) ? 0 : sel.stackSize;
            final int maxSize = (sel == null) ? 64 : sel.getMaxStackSize();
            final ItemStack stack = database.getStackInSlot(entry - 1);
            if (stack == null) {
                return new Object[] { 0 };
            }
            stack.stackSize = Math.min(amount, maxSize - inSlot);
            final ItemStack stack2 = stack.copy();
            stack2.stackSize = 1;
            ItemStack itemStack;
            if (sel == null) {
                itemStack = null;
            } else {
                final ItemStack sel2 = sel.copy();
                sel2.stackSize = 1;
                itemStack = sel2;
            }
            final ItemStack sel3 = itemStack;
            if (sel != null && !ItemStack.areItemStacksEqual(sel3, stack2)) {
                return new Object[] { 0 };
            }
            final IAEItemStack extracted = (IAEItemStack) inv.extractItems(
                AEApi.instance()
                    .storage()
                    .createItemStack(stack),
                Actionable.MODULATE,
                (BaseActionSource) new MachineSource((IActionHost) this.tile()));
            if (extracted == null) {
                return new Object[] { 0 };
            }
            final int ext = (int) extracted.getStackSize();
            stack.stackSize = inSlot + ext;
            invRobot.setInventorySlotContents(selected, stack);
            return new Object[] { ext };
        }

        public IMEMonitor<IAEFluidStack> getFluidInventory() {
            IGrid grid = null;
            try {
                grid = getProxy().getGrid();
            } catch (GridAccessException e) {
                throw new RuntimeException("Access Denied");
            }
            if (grid == null) {
                return null;
            }
            final IStorageGrid storage = (IStorageGrid) grid.getCache(IStorageGrid.class);
            if (storage == null) {
                return null;
            }
            return (IMEMonitor<IAEFluidStack>) storage.getFluidInventory();
        }

        @APIType({ "fluid", "ae" })
        @Callback(doc = "function([number:amount]):number -- Transfer selected fluid to your ae system.")
        public Object[] sendFluids(final Context context, final Arguments args) {
            markDirty();
            final int selected = selectedTankIndex();
            final MultiTank tanks = tank();
            if (tanks.tankCount() <= 0) {
                return new Object[] { 0 };
            }
            final IFluidTank tank = tanks.getFluidTank(selected);
            final IMEMonitor<IAEFluidStack> inv = this.getFluidInventory();
            if (tank == null || inv == null || tank.getFluid() == null) {
                return new Object[] { 0 };
            }
            final int amount = Math.min(args.optInteger(0, tank.getCapacity()), tank.getFluidAmount());
            final FluidStack fluid = tank.getFluid();
            final FluidStack fluid2 = fluid.copy();
            fluid2.amount = amount;
            final IAEFluidStack notInjected = (IAEFluidStack) inv.injectItems(
                AEApi.instance()
                    .storage()
                    .createFluidStack(fluid2),
                Actionable.MODULATE,
                (BaseActionSource) new MachineSource((IActionHost) this.tile()));
            Object[] array;
            if (notInjected == null) {
                tank.drain(amount, true);
                array = new Object[] { amount };
            } else {
                tank.drain(amount - (int) notInjected.getStackSize(), true);
                array = new Object[] { amount - notInjected.getStackSize() };
            }
            return array;
        }

        @APIType({ "fluid", "ae" })
        @Callback(
            doc = "function(database:address, entry:number[, number:amount]):number -- Get fluid from your ae system.")
        public Object[] requestFluids(final Context context, final Arguments args) {
            markDirty();
            final String address = args.checkString(0);
            final int entry = args.checkInteger(1);
            final int amount = args.optInteger(2, 1000);
            final MultiTank tanks = tank();
            final int selected = selectedTankIndex();
            if (tanks.tankCount() <= 0) {
                return new Object[] { 0 };
            }
            final IFluidTank tank = tanks.getFluidTank(selected);
            final IMEMonitor<IAEFluidStack> inv = this.getFluidInventory();
            if (tank == null || inv == null) {
                return new Object[] { 0 };
            }
            final Node n = node().network()
                .node(address);
            if (n == null) {
                throw new IllegalArgumentException("no such component");
            }
            if (!(n instanceof Component)) {
                throw new IllegalArgumentException("no such component");
            }
            final li.cil.oc.api.network.Environment env = n.host();
            if (!(env instanceof Database)) {
                throw new IllegalArgumentException("not a database");
            }
            final Database database = (Database) env;
            final FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(database.getStackInSlot(entry - 1));
            fluid.amount = amount;
            final FluidStack fluid2 = fluid.copy();
            fluid2.amount = tank.fill(fluid, false);
            if (fluid2.amount == 0) {
                return new Object[] { 0 };
            }
            final IAEFluidStack extracted = (IAEFluidStack) inv.extractItems(
                AEApi.instance()
                    .storage()
                    .createFluidStack(fluid2),
                Actionable.MODULATE,
                (BaseActionSource) new MachineSource((IActionHost) this.tile()));
            if (extracted == null) {
                return new Object[] { 0 };
            }
            return new Object[] { tank.fill(extracted.getFluidStack(), true) };
        }

        public OCApi(String s) {

            name = s;
            node = li.cil.oc.api.Network.newNode(this, Visibility.Network)
                .withComponent(name)
                .

                create();

        }

        final String name;

        @Override
        public Node node() {

            return node;
        }

        Component node;

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

        @Callback(
            doc = "function(side:number [, tank:number]):boolean -- Compare the fluid in the selected tank with the fluid in the specified tank on the specified side. Returns true if equal.")
        @Override
        public Object[] compareFluid(Context arg0, Arguments arg1) {
            TankWorldControl$class.compareFluid(this, arg0, arg1);
            return null;
        }

        @Callback(
            doc = "function(side:number[, amount:number=1000]):boolean, number of string -- Eject the specified amount of fluid to the specified side. Returns the amount ejected or an error message.")
        public Object[] fillRobot(Context arg0, Arguments arg1) {
            return TankWorldControl$class.fill(this, arg0, arg1);
        }

        @Callback(
            doc = "function(side:boolean[, amount:number=1000]):boolean, number or string -- Drains the specified amount of fluid from the specified side. Returns the amount drained, or an error message.")
        public Object[] drainRobot(Context arg0, Arguments arg1) {
            return TankWorldControl$class.drain(this, arg0, arg1);
        }

        // end of oc

    }

    @Override
    public void validate() {
        super.validate();
        this.getProxy()
            .validate();
    }

    ///////////////////////////////////////
    ItemStack[] inv = new ItemStack[32];
    FluidTank[] ft = new FluidTank[8];
    {
        for (int i = 0; i < ft.length; i++) {
            ft[i] = new FluidTank(256_000);
        }
    }

    @Override
    public int getSizeInventory() {

        return inv.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {

        return inv[slotIn];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {

        return Optional.ofNullable(inv[index])
            .map(s -> s.splitStack(count))
            .orElse(null);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {

        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {

        inv[index] = stack;
    }

    @Override
    public String getInventoryName() {

        return customName == null ? "IOHub" : customName;
    }

    @Override
    public boolean hasCustomInventoryName() {

        return customName != null;
    }

    @Override
    public int getInventoryStackLimit() {

        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {

        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {

        return true;
    }

    private boolean sameFluid(FluidStack fs1, FluidStack fs2) {
        if (fs1 == null || fs2 == null) {
            return false;
        }

        return fs1.getFluid() == fs2.getFluid();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        for (FluidTank f : ft) {
            if (sameFluid(f.getFluid(), resource)) {
                int suc = f.fill(resource, doFill);
                if (suc > 0) return suc;
            }
        }

        for (FluidTank f : ft) {
            int suc = f.fill(resource, doFill);
            if (suc > 0) return suc;
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        int suc = 0;
        int todo = resource.amount;
        for (FluidTank f : ft) {
            if (sameFluid(f.getFluid(), resource)) {
                int tmp;
                suc += (tmp = f.drain(todo, doDrain).amount);
                todo -= tmp;
            }
        }
        return new FluidStack(resource.getFluid(), suc);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        for (FluidTank f : ft) {
            if (f.getFluidAmount() > 0) return f.drain(maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {

        return fill(from, new FluidStack(fluid, 1), false) > 0;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {

        return drain(from, new FluidStack(fluid, 1), false).amount > 0;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return Arrays.stream(ft)
            .map(s -> new FluidTankInfo(s))
            .toArray(FluidTankInfo[]::new);

    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

        IntStream.range(0, inv.length)
            .forEach(s -> {
                Optional.ofNullable(compound.getTag("i" + s))
                    .ifPresent(ss -> inv[s] = ItemStack.loadItemStackFromNBT((NBTTagCompound) ss));
            });;
        for (int i = 0; i < ft.length; i++) {
            ft[i].readFromNBT((NBTTagCompound) compound.getTag("f" + i));
        }

        slotselected = compound.getInteger("slotselected");
        tankselected = compound.getInteger("tankselected");
        getProxy().readFromNBT(compound);

        NBTTagCompound nd = (NBTTagCompound) compound.getTag("mainNode");
        if (nd.hasNoTags() != false && node != null) node.load(nd);
        for (Entry<String, li.cil.oc.api.network.Environment> ent : subapi.entrySet()) {
            nd = (NBTTagCompound) compound.getTag(ent.getKey());
            if (nd != null && ent.getValue()
                .node() != null) ent.getValue()
                    .node()
                    .load(nd);
        }
        super.readFromNBT(compound);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {

        for (int i = 0; i < inv.length; i++) {
            final int ii = i;
            Optional.ofNullable(inv[i])
                .ifPresent(s -> compound.setTag("i" + ii, s.writeToNBT(new NBTTagCompound())));
        }
        for (int i = 0; i < ft.length; i++) {
            compound.setTag("f" + i, ft[i].writeToNBT(new NBTTagCompound()));
        }
        compound.setInteger("slotselected", slotselected);
        compound.setInteger("tankselected", tankselected);

        getProxy().writeToNBT(compound);

        final NBTTagCompound nd = new NBTTagCompound();
        compound.setTag("mainNode", nd);

        Optional.ofNullable(node)
            .ifPresent(s -> s.save(nd));

        for (Entry<String, li.cil.oc.api.network.Environment> ent : subapi.entrySet()) {
            final NBTTagCompound nd0 = new NBTTagCompound();
            compound.setTag(ent.getKey(), nd0);
            Optional.ofNullable(
                ent.getValue()
                    .node())
                .ifPresent(s -> s.save(nd0));
        }

        super.writeToNBT(compound);
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {

        return new UIFactory(buildContext).createWindow();
    }

    protected class UIFactory {

        private final UIBuildContext uiBuildContext;

        public UIFactory(UIBuildContext buildContext) {
            this.uiBuildContext = buildContext;
        }

        public ModularWindow createWindow() {
            ModularWindow.Builder builder = ModularWindow.builder(getGUIWidth(), getGUIHeight());
            builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
            // builder.setGuiTint(getUIBuildContext().getGuiColorization());
            if (doesBindPlayerInventory()) {
                builder.bindPlayerInventory(getUIBuildContext().getPlayer());
            }
            // builder.bindPlayerInventory(builder.getPlayer(), 7,
            // getGUITextureSet().getItemSlot());

            addTitleToUI(builder);
            addUIWidgets(builder);
            /*
             * if (getUIBuildContext().isAnotherWindow()) { builder.widget(
             * ButtonWidget.closeWindowButton(true) .setPos(getGUIWidth() - 15,
             * 3)); }
             */

            /*
             * final CoverInfo coverInfo = uiBuildContext.getTile()
             * .getCoverInfoAtSide(uiBuildContext.getCoverSide()); final
             * CoverBehaviorBase<?> behavior = coverInfo.getCoverBehavior();
             * if (coverInfo.getMinimumTickRate() > 0 &&
             * behavior.allowsTickRateAddition()) { builder.widget( new
             * GT_CoverTickRateButton(coverInfo, builder).setPos(getGUIWidth() -
             * 24, getGUIHeight() - 24)); }
             */
            return builder.build();
        }

        /**
         * Override this to add widgets for your UI.
         */

        // IItemHandlerModifiable fakeInv=new ItemHandlerModifiable();

        protected void addUIWidgets(ModularWindow.Builder builder) {
            final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv, 0, 32);
            Scrollable sc = new Scrollable().setVerticalScroll();
            builder.widget(new FakeSyncWidget.IntegerSyncer(() -> slotselected, s -> slotselected = s));
            builder.widget(new FakeSyncWidget.IntegerSyncer(() -> tankselected, s -> tankselected = s));
            final IDrawable[] background = new IDrawable[] { GUITextureSet.DEFAULT.getItemSlot() };
            final IDrawable[] special = new IDrawable[] { GUITextureSet.DEFAULT.getItemSlot(),
                GTUITextures.OVERLAY_SLOT_ARROW_ME };
            sc.widget(
                SlotGroup.ofItemHandler(inventoryHandler, 4)

                    .startFromSlot(0)
                    .endAtSlot(31)
                    .background(background)
                    .widgetCreator((h) -> (SlotWidget) new SlotWidget(h) {

                        public IDrawable[] getBackground() {
                            // System.out.println(h.getSlotIndex()+"
                            // "+(slotselected-1));
                            if (h.getSlotIndex() == slotselected - offset) {
                                return special;
                            } ;
                            return background;
                        };
                    })

                    .build()

            );
            builder.widget(
                sc.setPos(3 + 4, 3 + 8)
                    .setSize(18 * 4, 18 * 4));
            sc = new Scrollable().setVerticalScroll();

            final IDrawable[] background0 = new IDrawable[] { GUITextureSet.DEFAULT.getFluidSlot() };
            final IDrawable[] special0 = new IDrawable[] { GUITextureSet.DEFAULT.getFluidSlot(),
                GTUITextures.OVERLAY_SLOT_ARROW_ME };

            sc.widget(
                SlotGroup.ofFluidTanks(Arrays.asList(ft), 1)

                    .startFromSlot(0)
                    .endAtSlot(7)
                    .background(background0)
                    .widgetCreator((h, s) -> (FluidSlotWidget) new FluidSlotWidget(s) {

                        public IDrawable[] getBackground() {
                            // System.out.println(h.getSlotIndex()+"
                            // "+(slotselected-1));
                            if (h == tankselected - offset) {
                                return special0;
                            } ;
                            return background0;
                        };
                    })

                    .build()

            );

            builder.widget(
                sc.setPos(3 + 18 * 4 + 4, 3 + 8)
                    .setSize(18, 18 * 4));
            builder.widget(

                TextWidget.dynamicString(() -> getInventoryName())
                    .setSynced(true)
                    .setMaxWidth(999)
                    .setPos(3 + 4, 3)

            );

        }

        public UIBuildContext getUIBuildContext() {
            return uiBuildContext;
        }

        /*
         * public boolean isCoverValid() { return !getUIBuildContext().getTile()
         * .isDead() && getUIBuildContext().getTile()
         * .getCoverBehaviorAtSideNew(getUIBuildContext().getCoverSide()) !=
         * GregTechAPI.sNoBehavior; }
         */

        protected void addTitleToUI(ModularWindow.Builder builder) {
            /*
             * ItemStack coverItem =
             * GTUtility.intToStack(getUIBuildContext().getCoverID()); if
             * (coverItem != null) { builder.widget( new
             * ItemDrawable(coverItem).asWidget() .setPos(5, 5) .setSize(16,
             * 16)) .widget( new
             * TextWidget(coverItem.getDisplayName()).setDefaultColor(
             * COLOR_TITLE.get()) .setPos(25, 9)); }
             */
        }

        protected int getGUIWidth() {
            return 176;
        }

        protected int getGUIHeight() {
            return 107 + 18 * 3 + 18;
        }

        protected boolean doesBindPlayerInventory() {
            return true;
        }

        protected int getTextColorOrDefault(String textType, int defaultColor) {
            return defaultColor;
        }

        protected final Supplier<Integer> COLOR_TITLE = () -> getTextColorOrDefault("title", 0x222222);
        protected final Supplier<Integer> COLOR_TEXT_GRAY = () -> getTextColorOrDefault("text_gray", 0x555555);
        protected final Supplier<Integer> COLOR_TEXT_WARN = () -> getTextColorOrDefault("text_warn", 0xff0000);
    }

    @Override
    public String getCustomName() {

        return customName;
    }

    String customName;

    @Override
    public boolean hasCustomName() {

        return customName != null;
    }

    @Override
    public void setCustomName(String name) {
        customName = name;

    }

    int slotselected = offset;;
    int tankselected = offset;;

    @Override
    public IGridNode getActionableNode() {

        return getProxy().getNode();
    }

    @Override
    public Node sidedNode(ForgeDirection side) {

        return node();
    }

    @Override
    public boolean canConnect(ForgeDirection side) {

        return true;
    }

}
