package reobf.proghatches.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumicenergistics.common.storage.AEEssentiaStack;
import thaumicenergistics.common.storage.AEEssentiaStackType;

/**
 * ME Plunger essentia support. Kept in its own class so Thaumcraft/ThE classes only load when the
 * caller has verified thaumicenergistics is present. Uses the generalized AE stack-type API:
 * essentia is a first-class stack type in GTNH's ThE (AEEssentiaStackType.ESSENTIA_STACK_TYPE), so
 * the network monitor comes from the same getMEMonitor(type) entry point items and fluids use.
 */
public class PlungerEssentiaCompat {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean tryClearEssentia(IStorageGrid grid, ItemStack aStack, ItemMEPlunger plunger,
        EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ, BaseActionSource src) {
        TileEntity te = aWorld.getTileEntity(aX, aY, aZ);
        if (!(te instanceof IAspectContainer)) return false;
        IMEMonitor mon = grid.getMEMonitor(AEEssentiaStackType.ESSENTIA_STACK_TYPE);
        if (mon == null) return false;

        IAspectContainer cont = (IAspectContainer) te;
        AspectList list = cont.getAspects();
        if (list == null) return false;
        Aspect[] aspects = list.getAspects();

        boolean moved = false;
        boolean paid = false;
        for (Aspect a : aspects) {
            if (a == null) continue;
            int amount = list.getAmount(a);
            if (amount <= 0) continue;

            IAEStack notadd = mon.injectItems(new AEEssentiaStack(a, amount), Actionable.SIMULATE, src);
            if (notadd != null && notadd.getStackSize() > 0) continue; // all-or-nothing per aspect

            if (!paid) { // one durability point per plunge, like the other paths
                if (!(aPlayer.capabilities.isCreativeMode || plunger.damage(aStack))) return moved;
                paid = true;
            }
            mon.injectItems(new AEEssentiaStack(a, amount), Actionable.MODULATE, src);
            cont.takeFromContainer(a, amount);
            moved = true;
        }
        if (moved) te.markDirty();
        return moved;
    }
}
