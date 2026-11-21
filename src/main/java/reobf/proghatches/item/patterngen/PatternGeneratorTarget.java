package reobf.proghatches.item.patterngen;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.helpers.IInterfaceHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

/**
 * Resolves a clicked block/part to the pattern inventory used by direct fill.
 * <p>
 * Anything the Interface Terminal can show is an {@link IInterfaceViewable}; that covers PH
 * pattern hatches, GT crafting input buffers and AE2 interfaces. A plain inventory is accepted
 * as a fallback so chests and similar containers are not silently rejected.
 */
public final class PatternGeneratorTarget {

    public final IInventory inventory;
    public final TileEntity tile;
    public final IInterfaceViewable holder;

    private PatternGeneratorTarget(IInventory inventory, TileEntity tile, IInterfaceViewable holder) {
        this.inventory = inventory;
        this.tile = tile;
        this.holder = holder;
    }

    /**
     * Number of writable pattern slots. AE2 interfaces always own a 36-slot hidden pattern
     * inventory; the installed pattern-capacity cards only expose {@code numSlots()} of it, so
     * filling the rest makes patterns pop out when the interface GUI opens. GT's combined
     * crafting input buffer exposes the MTE itself and needs the circuit-slot cap. PH's dynamic
     * hatch has a separate inventory that grows past its input-bus circuit index.
     */
    public int patternCapacity() {
        int size;
        try {
            size = this.inventory.getSizeInventory();
        } catch (Throwable ignored) {
            return 0;
        }
        if (this.holder instanceof IInterfaceHost) {
            try {
                int declared = this.holder.numSlots();
                if (declared > 0) size = Math.min(size, declared);
            } catch (Throwable ignored) {}
        } else if (this.holder != null && this.inventory == this.holder
            && this.holder instanceof IConfigurationCircuitSupport support) {
                try {
                    int circuitSlot = support.getCircuitSlot();
                    if (circuitSlot > 0) size = Math.min(size, circuitSlot);
                } catch (Throwable ignored) {}
            }
        return Math.max(0, size);
    }

    public static PatternGeneratorTarget resolve(World world, int x, int y, int z, float hitX, float hitY,
        float hitZ) {
        if (world == null) return null;
        return resolve(world.getTileEntity(x, y, z), hitX, hitY, hitZ);
    }

    public static PatternGeneratorTarget resolve(TileEntity te, float hitX, float hitY, float hitZ) {
        if (te == null) return null;
        Object clicked = te;
        try {
            if (te instanceof IPartHost host) {
                SelectedPart selected = host.selectPart(Vec3.createVectorHelper(hitX, hitY, hitZ));
                if (selected != null && selected.part != null) clicked = selected.part;
            } else if (te instanceof IGregTechTileEntity gregtech) {
                Object meta = gregtech.getMetaTileEntity();
                if (meta != null) clicked = meta;
            }
        } catch (Throwable ignored) {}

        if (clicked instanceof IInterfaceViewable viewable) {
            IInventory patterns = null;
            try {
                patterns = viewable.getPatterns();
            } catch (Throwable ignored) {}
            if (patterns != null) {
                TileEntity tile = null;
                try {
                    tile = viewable.getTileEntity();
                } catch (Throwable ignored) {}
                return new PatternGeneratorTarget(patterns, tile == null ? te : tile, viewable);
            }
        }
        if (clicked instanceof IInventory inventory) {
            return new PatternGeneratorTarget(inventory, te, null);
        }
        return null;
    }

    /** Same two checks AE uses before it lets a player edit an interface. */
    public boolean mayEdit(EntityPlayer player) {
        if (player == null || player.worldObj == null) return false;
        try {
            DimensionalCoord location = null;
            if (this.holder != null) location = this.holder.getLocation();
            if (location == null && this.tile != null) location = new DimensionalCoord(this.tile);
            if (location != null && location.getWorld() != null && !Platform.hasPermissions(location, player)) {
                return false;
            }
            IGridNode node = nodeOf(this.holder) != null ? nodeOf(this.holder) : nodeOf(this.tile);
            if (node == null) return true;
            IGrid grid = node.getGrid();
            if (grid == null) return true;
            ISecurityGrid security = grid.getCache(ISecurityGrid.class);
            return security == null || security.hasPermission(player, SecurityPermissions.BUILD);
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static IGridNode nodeOf(Object device) {
        if (device == null) return null;
        try {
            if (device instanceof IGridProxyable proxyable) {
                AENetworkProxy proxy = proxyable.getProxy();
                IGridNode node = proxy == null ? null : proxy.getNode();
                if (node != null) return node;
            }
            if (device instanceof IGridHost host) {
                for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                    IGridNode node = host.getGridNode(direction);
                    if (node != null) return node;
                }
                return host.getGridNode(ForgeDirection.UNKNOWN);
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
