package reobf.proghatches.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

/**
 * OmniOcular (the Waila addon) registers a catch-all Waila provider on Block.class whose
 * getNBTData does a full {@code te.writeToNBT(tag)} — for data-heavy MTEs like the buffered dual
 * input hatch that means huge sync packets every 250ms plus screens of meaningless "noise" lines
 * rendered from the raw NBT.
 * <p>
 * Waila's ModuleRegistrar provider tables are public, so instead of ASM we locate OmniOcular's
 * registered providers and wrap them in-place with a filtering delegate: any tile entity (or GT
 * meta tile entity) implementing {@link IDoNotShowInOO} is skipped entirely — no OO body lines,
 * no full-NBT dump into the Waila sync packet. Everything else is delegated untouched.
 * <p>
 * Installation is retried lazily (Waila only invokes IMC registration callbacks in its own
 * loadComplete, which may run after ours) from EntityJoinWorldEvent on both sides until
 * OmniOcular's providers are found and wrapped.
 */
public class OmniOcularSuppressor {

    /** Marker: MTEs / TileEntities implementing this are never shown (nor NBT-dumped) by OmniOcular. */
    public interface IDoNotShowInOO {}

    private static boolean done = false;

    public static void tryInstall() {
        if (done) return;
        if (!Loader.isModLoaded("Waila") || !Loader.isModLoaded("OmniOcular")) {
            done = true;
            return;
        }
        try {
            done = Installer.install();
        } catch (Throwable t) {
            done = true; // don't retry-spam on unexpected registry shape
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (!done && event.entity instanceof EntityPlayer) tryInstall();
    }

    static boolean isHidden(TileEntity te) {
        if (te == null) return false;
        if (te instanceof IDoNotShowInOO) return true;
        if (te instanceof IGregTechTileEntity) {
            try {
                return ((IGregTechTileEntity) te).getMetaTileEntity() instanceof IDoNotShowInOO;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /** All Waila class references isolated here so the outer class loads without Waila. */
    private static class Installer {

        static boolean install() {
            mcp.mobius.waila.api.impl.ModuleRegistrar reg = mcp.mobius.waila.api.impl.ModuleRegistrar.instance();
            boolean found = false;
            found |= wrap(reg.bodyBlockProviders);
            found |= wrap(reg.NBTDataProviders);
            return found;
        }

        static boolean wrap(LinkedHashMap<Class, ArrayList<mcp.mobius.waila.api.IWailaDataProvider>> map) {
            boolean found = false;
            for (ArrayList<mcp.mobius.waila.api.IWailaDataProvider> list : map.values()) {
                if (list == null) continue;
                for (int i = 0; i < list.size(); i++) {
                    mcp.mobius.waila.api.IWailaDataProvider p = list.get(i);
                    if (p != null && p.getClass()
                        .getName()
                        .startsWith("me.exz.omniocular.")) {
                        list.set(i, new Filtering(p));
                        found = true;
                    }
                }
            }
            return found;
        }
    }

    private static class Filtering implements mcp.mobius.waila.api.IWailaDataProvider {

        private final mcp.mobius.waila.api.IWailaDataProvider delegate;

        Filtering(mcp.mobius.waila.api.IWailaDataProvider delegate) {
            this.delegate = delegate;
        }

        @Override
        public ItemStack getWailaStack(mcp.mobius.waila.api.IWailaDataAccessor accessor,
            mcp.mobius.waila.api.IWailaConfigHandler config) {
            return delegate.getWailaStack(accessor, config);
        }

        @Override
        public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip,
            mcp.mobius.waila.api.IWailaDataAccessor accessor, mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (isHidden(accessor.getTileEntity())) return currenttip;
            return delegate.getWailaHead(itemStack, currenttip, accessor, config);
        }

        @Override
        public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip,
            mcp.mobius.waila.api.IWailaDataAccessor accessor, mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (isHidden(accessor.getTileEntity())) return currenttip;
            return delegate.getWailaBody(itemStack, currenttip, accessor, config);
        }

        @Override
        public boolean hasWailaAdvancedBody(ItemStack itemStack, mcp.mobius.waila.api.IWailaDataAccessor accessor,
            mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (isHidden(accessor.getTileEntity())) return false;
            return delegate.hasWailaAdvancedBody(itemStack, accessor, config);
        }

        @Override
        public List<String> getWailaAdvancedBody(ItemStack itemStack, List<String> currenttip,
            mcp.mobius.waila.api.IWailaDataAccessor accessor, mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (isHidden(accessor.getTileEntity())) return currenttip;
            return delegate.getWailaAdvancedBody(itemStack, currenttip, accessor, config);
        }

        @Override
        public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip,
            mcp.mobius.waila.api.IWailaDataAccessor accessor, mcp.mobius.waila.api.IWailaConfigHandler config) {
            if (isHidden(accessor.getTileEntity())) return currenttip;
            return delegate.getWailaTail(itemStack, currenttip, accessor, config);
        }

        @Override
        public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
            int y, int z) {
            // skipping the delegate here is what keeps te.writeToNBT() (the whole buffer contents)
            // out of the Waila sync packet
            if (isHidden(te)) return tag;
            return delegate.getNBTData(player, te, tag, world, x, y, z);
        }
    }
}
