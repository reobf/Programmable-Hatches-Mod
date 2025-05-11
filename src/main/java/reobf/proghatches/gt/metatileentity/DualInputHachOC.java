package reobf.proghatches.gt.metatileentity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.inventory.MEMonitorIFluidHandler;
import com.google.common.collect.ImmutableMap;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEIInventoryWrapper;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.IDualInputInventory;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.server.component.UpgradeDatabase;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.main.Config;

public class DualInputHachOC extends DualInputHatch
    implements reobf.proghatches.oc.IActualEnvironment, Environment, SidedEnvironment, IGridProxyable, IActionHost {

    public DualInputHachOC(int id, String name, String nameRegional, int tier, boolean mMultiFluid) {
        super(
            id,
            name,
            nameRegional,
            tier,
            17,
            mMultiFluid,

            Config.get("DIHOC", ImmutableMap.of()));

    }

    public DualInputHachOC(String aName, int aTier, int aSlots, String[] aDescription, ITexture[][][] aTextures,
        boolean mMultiFluid) {
        super(aName, aTier, aSlots, aDescription, aTextures, mMultiFluid);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {

        if (accessor.getNBTData()
            .hasKey("tasks")) {
            ByteArrayInputStream bi = new ByteArrayInputStream(
                accessor.getNBTData()
                    .getByteArray("tasks"));
            try {
                int i = 0;
                List<Task> tasks = (List<Task>) new ObjectInputStream(bi).readObject();
                for (Task task : tasks) {
                    currenttip.add((i++) + ":" + task.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.getWailaBody(itemStack, currenttip, accessor, config);
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            synchronized (tasks) {
                new ObjectOutputStream(bo).writeObject(tasks);
            }
            tag.setByteArray("tasks", bo.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
    }

    public gregtech.api.metatileentity.MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new DualInputHachOC(mName, mTier, 17, mDescriptionArray, mTextures, mMultiFluid);

    };

    public boolean canDrain(ForgeDirection side, net.minecraftforge.fluids.Fluid aFluid) {
        return false;
    };

    public boolean canExtractItem(int aIndex, net.minecraft.item.ItemStack aStack, int ordinalSide) {
        return false;
    };

    public boolean canFill(ForgeDirection side, net.minecraftforge.fluids.Fluid aFluid) {
        return false;
    };

    public boolean canInsertItem(int aIndex, net.minecraft.item.ItemStack aStack, int ordinalSide) {
        return false;
    };

    Node node = li.cil.oc.api.Network.newNode(this, Visibility.Network)
        .withComponent("dualhatch")
        .create();

    @Override
    public Node sidedNode(ForgeDirection side) {
        if (getBaseMetaTileEntity().getFrontFacing() == side) return node;
        return null;
    }

    @Override
    public boolean canConnect(ForgeDirection side) {
        if (getBaseMetaTileEntity().getFrontFacing() == side) return true;
        return false;
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

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {

        super.saveNBTData(aNBT);
        if (node != null) {
            NBTTagCompound tnode = new NBTTagCompound();
            node.save(tnode);
            aNBT.setTag("node", tnode);
        }
        getProxy().writeToNBT(aNBT);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(bo).writeObject(tasks);
            aNBT.setByteArray("tasks", bo.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        aNBT.setInteger("idcounter", idcounter.get());
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        super.loadNBTData(aNBT);
        if (node != null) {
            NBTTagCompound tnode = aNBT.getCompoundTag("node");
            if (tnode.hasNoTags() == false) {
                node.load(tnode);
            }

        }
        getProxy().readFromNBT(aNBT);

        ByteArrayInputStream bi = new ByteArrayInputStream(aNBT.getByteArray("tasks"));
        try {
            tasks = (List<Task>) new ObjectInputStream(bi).readObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        idcounter.set(aNBT.getInteger("idcounter"));
    }

    private void updateValidGridProxySides() {

        getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

    }

    AENetworkProxy gridProxy;

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return getProxy().getNode();
    }

    @Override
    public AENetworkProxy getProxy() {

        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(
                this,
                "proxy",
                new ItemStack(
                    GregTechAPI.sBlockMachines,
                    1,
                    this.getBaseMetaTileEntity()
                        .getMetaTileID()),
                true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity());
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
        if (node != null) {
            li.cil.oc.api.Network.joinOrCreateNetwork((TileEntity) this.getBaseMetaTileEntity());
        }
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {

        super.onPostTick(aBaseMetaTileEntity, aTick);
        // item.injectItems(AEItemStack.create(new ItemStack(Items.apple)), Actionable.MODULATE, source);
        // fluid.injectItems(AEFluidStack.create(new FluidStack(FluidRegistry.WATER,1)), Actionable.MODULATE, source);
        tasks.forEach(s -> s.update(node()));

    }

    @Override
    public void onFacingChange() {
        updateValidGridProxySides();
    }

    /*
     * public Object[] help(final Context context, final Arguments args) {
     * if(args.checkString(0).equals("type")){
     * return new Object[]{
     * "\"now\": Execute task the next world update."
     * +"\"never\": Just add task, do nothing. Call executeTask to execute it."
     * +"\"blocking\": Exexute if inventory is empty."
     * };
     * }
     * return new Object[]{};
     * }
     */

    public static class AEStackHolder implements Externalizable {

        public AEStackHolder(IAEStack stack) {
            this.stack = stack;
        }

        IAEStack stack;

        public AEStackHolder() {}

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            byte[] b = CompressedStreamTools.compress(tag);
            out.writeInt(b.length);
            out.write(b);
            out.writeInt(stack instanceof AEItemStack ? 1 : 0);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            byte b[] = new byte[in.readInt()];
            // if(in.available()!=b.length){throw new IOException();}
            in.readFully(b);
            NBTTagCompound tag = CompressedStreamTools.func_152457_a(b, new NBTSizeTracker(Long.MAX_VALUE));
            IAEStack ch = in.readInt() == 1 ? AEItemStack.loadItemStackFromNBT(tag) : loadFluidStackFromNBT(tag);
            stack = ch;
        }

        public static IAEFluidStack loadFluidStackFromNBT(final NBTTagCompound i) {
            final FluidStack itemstack = FluidStack.loadFluidStackFromNBT(i);
            if (itemstack == null) {
                return null;
            }
            final AEFluidStack fluid = AEFluidStack.create(itemstack);
            // fluid.priority = i.getInteger( "Priority" );
            fluid.setStackSize(i.getLong("Cnt"));
            fluid.setCountRequestable(i.getLong("Req"));
            fluid.setCraftable(i.getBoolean("Craft"));
            fluid.setCountRequestableCrafts(i.getLong("ReqMade"));
            fluid.setUsedPercent(i.getFloat("UsedPercent"));
            return fluid;
        }
    }

    public static class TargetStack implements Serializable {

        private static final long serialVersionUID = -6805372705502580992L;
        String addr;
        int index;
        boolean itemtype;// false->item true->fluid
        int amount;
        AEStackHolder item;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (item != null) {

                if (item.stack instanceof AEFluidStack) {
                    sb.append(
                        ((AEFluidStack) item.stack).getFluid()
                            .getName() + "*" + item.stack.getStackSize());
                } else if (item.stack instanceof AEItemStack) {
                    sb.append(((AEItemStack) item.stack).getDisplayName() + "*" + item.stack.getStackSize());
                }

            } else {
                sb.append("?");
            }
            return sb.toString();
        }

        public void validate(Node n, Task task) {
            if (task.state == 0) {
                Node data = n.network()
                    .node(addr);
                if (data == null) {
                    task.state = 2;
                    return;
                }
                if (data.host() instanceof UpgradeDatabase) {

                    UpgradeDatabase base = (UpgradeDatabase) data.host();
                    if (index < 0 || index >= base.size()) {
                        task.state = 3;
                        return;
                    }
                    ItemStack is = base.getStackInSlot(index);
                    if (is == null) {
                        task.state = 3;
                        return;
                    }

                    if (itemtype) {

                        FluidStack fs = GTUtility.getFluidForFilledItem(is, true);
                        if (fs == null) {
                            task.state = 4;
                            return;
                        }
                        item = new AEStackHolder(AEFluidStack.create(fs));

                    } else {
                        item = new AEStackHolder(AEItemStack.create(is));
                    }
                    item.stack.setStackSize(amount);
                    task.state = 1;
                    return;

                } else {
                    task.state = 2;
                    return;
                }

            }

        }

    }

    public static class Task implements Serializable {

        private static final long serialVersionUID = 4868145313081267898L;
        int id;

        public String toString() {
            return "id:" + id + " " + item.toString() + " " + state();
        };

        final TargetStack item;
        // 0->submitted
        // 1->validated
        // 2->data base missing
        // 3->item invalid
        // 4->item not fluid
        // 5->cancelled
        // 6->executed
        // 7->not enough materials
        // 8->me access unavailable
        // 9->partial executed
        // 10->

        // 1x->failed to execute due to x
        int state;

        // 0->never
        // 1->empty
        // int mode;

        // int idcount;

        public Task(String addr, int index, int amount, boolean itemtype, int id) {
            TargetStack ts = new TargetStack();
            ts.addr = addr;
            ts.index = index;
            ts.itemtype = itemtype;
            ts.amount = amount;
            item = ts;
            this.id = id;
        }

        void update(Node n) {
            if (state == 0) item.validate(n, this);
        }

        @SuppressWarnings("unchecked")
        void submit(IMEInventory f, IMEInventory i, AENetworkProxy storage, BaseActionSource source) {
            if (state != 1) return;
            if (item == null) return;
            try {
                IStorageGrid st = storage.getStorage();
                IMEInventory src = item.itemtype ? st.getFluidInventory() : st.getItemInventory();
                IMEInventory dest = item.itemtype ? f : i;

                IAEStack sim = src.extractItems(item.item.stack, Actionable.SIMULATE, source);
                if (sim == null || sim.getStackSize() != item.item.stack.getStackSize()) {
                    state = 7;
                    return;
                }

                IAEStack ext = src.extractItems(item.item.stack, Actionable.MODULATE, source);
                if (ext == null) {
                    state = 7;
                    return;
                }
                boolean same = ext.getStackSize() == item.item.stack.getStackSize();
                IAEStack remain = dest.injectItems(ext, Actionable.MODULATE, source);
                if (remain != null) {
                    src.injectItems(remain, Actionable.MODULATE, source);
                    state = 9;
                    return;
                }
                if (!same) {
                    state = 9;
                    return;
                }

            } catch (GridAccessException e) {
                state = 8;
                return;
            }

            state = 6;
        }

        public String state() {
            switch (state) {
                case 0:
                    return "added";
                case 1:
                    return "ready to submit";
                case 2:
                    return "data base missing";
                case 3:
                    return "item invalid";
                case 4:
                    return "item is not fluid container";
                case 5:
                    return "cancelled";
                case 6:
                    return "executed";
                case 7:
                    return "me storage not sufficient";
                case 8:
                    return "me network disconnected";
                case 9:
                    return "partially executed";
            }
            return "error";
        }

    }

    @Callback(
        doc = "function(data:address,index:number,amount:number[,isFluid:boolean]):table -- Append a task of extracting materials from ME Storage into the hatch. Call submitTask to submit the task. This function returns instantly."

        ,
        direct = true)
    public Object[] addTask(final Context context, final Arguments args) {
        try {
            synchronized (tasks) {
                if (tasks.size() >= 127) {
                    throw new IllegalArgumentException("Too many unsubmitted tasks(127)");
                }
                Task task = new Task(
                    args.checkString(0),
                    args.checkInteger(1),
                    args.checkInteger(2),
                    args.optBoolean(3, false),
                    idcounter.addAndGet(1));
                tasks.add(task);

                TreeMap map = new TreeMap<>();
                map.put("id", task.id);
                map.put("state", "added");

                return new Object[] { map };
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0];
        }
    }

    @Callback(doc = "function(taskID:number):table -- Cancel the task. This function returns instantly.", direct = true)
    public Object[] cancelTask(final Context context, final Arguments args) {
        try {
            TreeMap map = new TreeMap<>();
            Task task = null;
            synchronized (tasks) {
                for (Iterator<Task> itr = tasks.iterator(); itr.hasNext();) {
                    Task task0 = itr.next();
                    if (task0.id == args.checkInteger(0)) {
                        itr.remove();
                        task = task0;
                        break;
                    }

                }
            }

            if (task != null) {
                map.put("id", task.id);
                map.put("state", "cancelled");
            }

            return new Object[] { map };
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0];
        }
    }

    @Callback(
        doc = "function(taskID:number):table -- Get the state of task. This function returns instantly.",
        direct = true)
    public Object[] state(final Context context, final Arguments args) {
        try {
            TreeMap map = new TreeMap<>();
            Task task = null;
            synchronized (tasks) {
                for (Iterator<Task> itr = tasks.iterator(); itr.hasNext();) {
                    Task task0 = itr.next();
                    if (task0.id == args.checkInteger(0)) {

                        task = task0;
                        break;
                    }

                }
            }

            if (task != null) {
                map.put("id", task.id);
                map.put("state", task.state());
            }

            return new Object[] { map };
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0];
        }
    }

    @Callback(
        doc = "function():table -- Submit all tasks added via addTask. Return an array of state of those tasks. This function blocks until world update, then execute ALL added tasks in one tick.")
    public Object[] submitTask(final Context context, final Arguments args) {
        try {
            ArrayList arr = new ArrayList<>();
            synchronized (tasks) {
                while (tasks.isEmpty() == false) {

                    Task t = tasks.remove(0);
                    t.update(node());
                    t.submit(fluid, item, getProxy(), source);
                    TreeMap map = new TreeMap<>();
                    map.put("id", t.id);
                    map.put("state", t.state());
                    arr.add(map);
                }

            }

            return new Object[] { arr };

        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0];
        }

    }

    @Callback(
        doc = "function(data:address,index:number,amount:number[,isFluid:boolean]):table -- Exexute the task. This function blocks until world update.")
    public Object[] executeTask(final Context context, final Arguments args) {
        Task task = new Task(
            args.checkString(0),
            args.checkInteger(1),
            args.checkInteger(2),
            args.optBoolean(3, false),
            idcounter.addAndGet(1));
        task.update(node());
        task.submit(fluid, item, getProxy(), source);
        TreeMap map = new TreeMap<>();
        map.put("id", task.id);
        map.put("state", task.state());
        return new Object[] { map };
    }

    AtomicInteger idcounter = new AtomicInteger(0);

    List<Task> tasks = (List<Task>) Collections.synchronizedList(new ArrayList<Task>());
    private BaseActionSource source = new MachineSource(this);

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        // TODO Auto-generated method stub
        return super.fill(from, resource, doFill);
    }

    @Override
    public IGridNode getActionableNode() {

        return getProxy().getNode();
    }

    MEIInventoryWrapper item = new MEIInventoryWrapper(this, new AdaptorIInventory(this, Integer.MAX_VALUE));
    MEMonitorIFluidHandler fluid = new MEMonitorIFluidHandler(this);

    @Callback(doc = "function():boolean -- Return if the hatch is empty.")
    public Object[] isEmpty(final Context context, final Arguments args) {
        return new Object[] { !this.getFirstNonEmptyInventory()
            .isPresent() };
    }

    @Callback(
        doc = "function():boolean -- Refund all materials back to ME Storage. Return if all materials are transfered.")
    public Object[] refund(final Context context, final Arguments args) {
        boolean leftsomething = false;
        try {
            Optional<IDualInputInventory> inv = this.getFirstNonEmptyInventory();
            if (inv.isPresent() == false) return new Object[0];
            IMEMonitor<IAEItemStack> sg = getProxy().getStorage()
                .getItemInventory();
            IMEMonitor<IAEFluidStack> fsg = getProxy().getStorage()
                .getFluidInventory();
            for (ItemStack itemStack : inv.get()
                .getItemInputs()) {
                if (itemStack == null || itemStack.stackSize == 0) continue;
                IAEItemStack rest = Platform.poweredInsert(
                    getProxy().getEnergy(),
                    sg,
                    AEApi.instance()
                        .storage()
                        .createItemStack(itemStack),
                    source);
                itemStack.stackSize = rest != null && rest.getStackSize() > 0 ? (int) rest.getStackSize() : 0;
                if (itemStack.stackSize > 0) leftsomething = true;
            }

            for (FluidStack fluidStack : inv.get()
                .getFluidInputs()) {
                if (fluidStack == null || fluidStack.amount == 0) continue;
                IAEFluidStack rest = Platform.poweredInsert(
                    getProxy().getEnergy(),
                    fsg,
                    AEApi.instance()
                        .storage()
                        .createFluidStack(fluidStack),
                    source);
                fluidStack.amount = (Optional.ofNullable(rest)
                    .map(s -> s.getStackSize())
                    .orElse(0L)).intValue();
                if (fluidStack.amount > 0) leftsomething = true;
            } ;
            updateSlots();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Object[] { !leftsomething };
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
        int colorIndex, boolean aActive, boolean redstoneLevel) {
        int texturePointer = (byte) (getUpdateData() & 0x7F);

        int textureIndex = texturePointer | (ReflectionsPH.getTexturePage(this) << 7);

        Block b = /* Blocks.cactus; */GameRegistry.findBlock("OpenComputers", "raid");
        ITexture tex = TextureFactory.of(b, 0, ForgeDirection.UP);

        if (textureIndex > 0) {

            tex = Textures.BlockIcons.casingTexturePages[ReflectionsPH.getTexturePage(this)][texturePointer];

        }
        if (side == aFacing) {
            return new ITexture[] { tex, TextureFactory.of(BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER) };
        }
        return new ITexture[] { tex };

    }

}
