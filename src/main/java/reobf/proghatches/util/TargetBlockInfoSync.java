package reobf.proghatches.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;

import cpw.mods.fml.common.Loader;

/**
 * MUI2 SyncHandler that, while the GUI is open, periodically resolves the linked target block
 * SERVER-side into its {@link Block#getPickBlock} ItemStack plus the full WAILA tooltip
 * (head/body/tail providers, run against the live server TE so the data is authoritative even when
 * the chunk is not loaded client-side), pushes both to the client, and renders them as a 16x16 item
 * widget whose hover tooltip is the WAILA text.
 * <p>
 * Server-side inputs are two suppliers so the same class serves the remote hatches/buses (linked
 * x/y/z, may be unloaded/absent) and the mapping-slave mirrors (host machine position).
 */
public class TargetBlockInfoSync extends SyncHandler {

    /** Server: world to look in; may return null. */
    private final Supplier<World> world;
    /** Server: target position as {x,y,z}, or null when there is no valid target right now. */
    private final Supplier<int[]> pos;

    // client-side mirror
    private ItemStack clientStack;
    private final List<String> clientTip = new ArrayList<>();
    private final ItemDrawable drawable = new ItemDrawable();

    // server-side change detection
    private int cooldown;
    private ItemStack lastStack;
    private List<String> lastTip;

    public TargetBlockInfoSync(Supplier<World> world, Supplier<int[]> pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (!init && cooldown-- > 0) return;
        cooldown = 20;
        ItemStack stack = null;
        List<String> tip = new ArrayList<>();
        try {
            World w = world.get();
            int[] p = pos.get();
            if (w != null && p != null
                && w.getChunkProvider()
                    .chunkExists(p[0] >> 4, p[2] >> 4)) {
                stack = gather(w, p[0], p[1], p[2], getSyncManager().getPlayer(), tip);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (!init && ItemStack.areItemStacksEqual(stack, lastStack) && tip.equals(lastTip)) return;
        lastStack = stack == null ? null : stack.copy();
        lastTip = tip;
        final ItemStack fs = stack;
        final List<String> ft = tip;
        syncToClient(1, buf -> {
            NetworkUtils.writeItemStack(buf, fs);
            buf.writeVarIntToBuffer(ft.size());
            for (String s : ft) NetworkUtils.writeStringSafe(buf, s);
        });
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id != 1) return;
        clientStack = NetworkUtils.readItemStack(buf);
        clientTip.clear();
        int n = buf.readVarIntFromBuffer();
        for (int i = 0; i < n; i++) clientTip.add(NetworkUtils.readStringSafe(buf));
        drawable.setItem(clientStack);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {}

    /**
     * Client-side display name of the resolved target, or null when unknown. Resolved server-side
     * via Waila's stack providers / getPickBlock, so GT machines (whose identity lives in the tile
     * entity, not in the block metadata) get their real name instead of the generic block name.
     */
    public String clientDisplayName() {
        try {
            return clientStack == null ? null : clientStack.getDisplayName();
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * The 16x16 item widget rendering the synced pick-block stack; hovering shows the WAILA lines
     * (or just the item name when WAILA yielded nothing). Renders nothing while there is no target.
     */
    public Widget<?> createWidget() {
        return new Widget<>().overlay(drawable)
            .size(16, 16)
            .tooltipAutoUpdate(true)
            .tooltipDynamic(t -> {
                if (clientStack == null) return;
                if (clientTip.isEmpty()) {
                    t.addLine(IKey.str(clientStack.getDisplayName()));
                    return;
                }
                for (String line : clientTip) t.addLine(IKey.str(line));
            });
    }

    // ==================== server-side gathering ====================

    /** Returns the display stack (never runs client code); fills {@code tip} with WAILA lines. */
    public static ItemStack gather(World w, int x, int y, int z, EntityPlayer player, List<String> tip) {
        Block b = w.getBlock(x, y, z);
        if (b == null || b.isAir(w, x, y, z)) return null;
        MovingObjectPosition mop = new MovingObjectPosition(x, y, z, 1, Vec3.createVectorHelper(x + 0.5, y + 1, z + 0.5));
        ItemStack stack = null;
        if (Loader.isModLoaded("Waila")) {
            try {
                stack = WailaCompat.gatherStack(w, player, mop);
            } catch (Throwable ignored) {}
        }
        if (stack == null) {
            try {
                stack = b.getPickBlock(mop, w, x, y, z, player);
            } catch (Throwable ignored) {}
        }
        if (stack == null) {
            try {
                stack = new ItemStack(b, 1, w.getBlockMetadata(x, y, z));
            } catch (Throwable ignored) {}
        }
        if (stack != null && stack.getItem() == null) stack = null;
        if (Loader.isModLoaded("Waila")) {
            try {
                WailaCompat.gatherText(w, player, mop, stack, tip);
            } catch (Throwable ignored) {}
        }
        return stack;
    }

    /** All WAILA class references isolated here so the outer class loads without Waila. */
    private static class WailaCompat {

        static ItemStack gatherStack(World w, EntityPlayer player, MovingObjectPosition mop) {
            mcp.mobius.waila.api.impl.DataAccessorCommon acc = new mcp.mobius.waila.api.impl.DataAccessorCommon();
            acc.set(w, player, mop);
            mcp.mobius.waila.api.impl.ModuleRegistrar reg = mcp.mobius.waila.api.impl.ModuleRegistrar.instance();
            mcp.mobius.waila.api.IWailaConfigHandler cfg = mcp.mobius.waila.api.impl.ConfigHandler.instance();
            Block block = acc.getBlock();
            if (block instanceof mcp.mobius.waila.api.IWailaBlock) {
                try {
                    ItemStack r = ((mcp.mobius.waila.api.IWailaBlock) block).getWailaStack(acc, cfg);
                    if (r != null) return r;
                } catch (Throwable ignored) {}
            }
            List<Object> keys = new ArrayList<>();
            keys.add(block);
            if (acc.getTileEntity() != null) keys.add(acc.getTileEntity());
            for (Object k : keys) {
                if (!reg.hasStackProviders(k)) continue;
                for (List<mcp.mobius.waila.api.IWailaDataProvider> list : reg.getStackProviders(k)
                    .values()) {
                    for (mcp.mobius.waila.api.IWailaDataProvider p : list) {
                        try {
                            ItemStack r = p.getWailaStack(acc, cfg);
                            if (r != null) return r;
                        } catch (Throwable ignored) {}
                    }
                }
            }
            return null;
        }

        static void gatherText(World w, EntityPlayer player, MovingObjectPosition mop, ItemStack stack,
            List<String> tip) {
            mcp.mobius.waila.api.impl.DataAccessorCommon acc = new mcp.mobius.waila.api.impl.DataAccessorCommon();
            acc.set(w, player, mop);
            mcp.mobius.waila.api.IWailaConfigHandler cfg = mcp.mobius.waila.api.impl.ConfigHandler.instance();
            Block block = acc.getBlock();
            if (stack == null) stack = acc.getStack();
            // head, body, tail — same dispatch as Waila's MetaDataProvider, minus the client-only
            // advanced-body key handling
            for (int layout = 0; layout < 3; layout++) {
                List<String> cur = new ArrayList<>();
                if (block instanceof mcp.mobius.waila.api.IWailaBlock) {
                    mcp.mobius.waila.api.IWailaBlock iw = (mcp.mobius.waila.api.IWailaBlock) block;
                    try {
                        cur = layout == 0 ? iw.getWailaHead(stack, cur, acc, cfg)
                            : layout == 1 ? iw.getWailaBody(stack, cur, acc, cfg) : iw.getWailaTail(stack, cur, acc, cfg);
                    } catch (Throwable ignored) {}
                } else {
                    for (mcp.mobius.waila.api.IWailaDataProvider p : collect(layout, block, acc.getTileEntity())) {
                        try {
                            cur = layout == 0 ? p.getWailaHead(stack, cur, acc, cfg)
                                : layout == 1 ? p.getWailaBody(stack, cur, acc, cfg)
                                    : p.getWailaTail(stack, cur, acc, cfg);
                        } catch (Throwable ignored) {}
                    }
                }
                if (cur != null) tip.addAll(cur);
            }
        }

        private static List<mcp.mobius.waila.api.IWailaDataProvider> collect(int layout, Block block, TileEntity te) {
            mcp.mobius.waila.api.impl.ModuleRegistrar reg = mcp.mobius.waila.api.impl.ModuleRegistrar.instance();
            Map<Integer, List<mcp.mobius.waila.api.IWailaDataProvider>> merged = new TreeMap<>();
            for (Object k : new Object[] { block, te }) {
                if (k == null) continue;
                Map<Integer, List<mcp.mobius.waila.api.IWailaDataProvider>> m = layout == 0
                    ? (reg.hasHeadProviders(k) ? reg.getHeadProviders(k) : null)
                    : layout == 1 ? (reg.hasBodyProviders(k) ? reg.getBodyProviders(k) : null)
                        : (reg.hasTailProviders(k) ? reg.getTailProviders(k) : null);
                if (m != null) merged.putAll(m);
            }
            List<mcp.mobius.waila.api.IWailaDataProvider> flat = new ArrayList<>();
            for (List<mcp.mobius.waila.api.IWailaDataProvider> l : merged.values()) flat.addAll(l);
            return flat;
        }
    }
}
