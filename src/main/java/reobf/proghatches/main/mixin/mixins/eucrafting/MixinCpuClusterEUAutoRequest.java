package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.item.AEItemStack;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.WrappedPatternDetail;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.mixin.MixinCallback;
@Deprecated
@Mixin(value = CraftingCPUCluster.class, remap = false, priority = 0)
public abstract class MixinCpuClusterEUAutoRequest {

    @Shadow
    private MachineSource machineSrc;
    Map<IAEItemStack, Long> storage = new HashMap<>();
    Map<IAEItemStack, Long> needed = new HashMap<>();
    @Shadow
    private MECraftingInventory inventory;
    @Shadow
    private final LinkedList<TileCraftingTile> tiles = new LinkedList<>();
    @Shadow
    Map<ICraftingPatternDetails, Object> tasks;

    private HashMap<WrappedPatternDetail, int[]> cooldown = new HashMap<>();

    @Shadow
    abstract boolean canCraft(final ICraftingPatternDetails details, final IAEItemStack[] condensedInputs);

    @Inject(at = @At(value = "HEAD"), method = { "cancel", "completeJob" })
    private void endJob(CallbackInfo __) {

        cooldown.clear();

    }

    // spotless:off
/**
if (!this.canCraft(details, details.getCondensedInputs())) {
   //INJECT HERE Shift.BY.3
    i.remove();
}
*/
//spotless:on
    @ModifyVariable(
        at = @At(
            value = "INVOKE",
            target = "canCraft(Lappeng/api/networking/crafting/ICraftingPatternDetails;[Lappeng/api/storage/data/IAEItemStack;)Z",
            shift = Shift.BY,
            by = 3),
        method = "executeCrafting")
    private ICraftingPatternDetails executeCrafting2(ICraftingPatternDetails pattern) {// collect
                                                                                       // failed
                                                                                       // WrappedPatternDetail
        try {
            if (pattern instanceof WrappedPatternDetail) {
                WrappedPatternDetail p = (WrappedPatternDetail) pattern;
                int cd[] = cooldown.computeIfAbsent(p, (s) -> new int[2]);
                if (cd[0] > 0) {
                    cd[0]--;
                    return pattern;
                }
                boolean isOnlyEUTokenMissing = false;
                // spotless:off
			//cannot craft, but original one can, means that only eu token is missing
			//spotless:on
                if (this.canCraft(p.original, p.original.getCondensedInputs())) {
                    isOnlyEUTokenMissing = true;
                }

                if (isOnlyEUTokenMissing) {
                    long exist = needed.getOrDefault(
                        p.extraIn.copy()
                            .setStackSize(1),
                        0l);
                    if (exist == 0) needed.put(
                        p.extraIn.copy()
                            .setStackSize(1),
                        p.extraIn0.stackSize + 0l);
                    cooldown.remove(p);
                } else {
                    cooldown.get(p)[0] += Math.min((10 + 2 * cooldown.get(p)[1]++), 100);

                    // MyMod.LOG.info("Cannot craft, blacklist for
                    // "+cooldown.get(p)[0]+" ticks:"+p.extraIn0.getTagCompound());
                }
            }

        } catch (Exception e) {
            MyMod.LOG.error("caught error in mixin", e);
        }
        return pattern;
    }

    /*
     * @Inject(at = @At("HEAD"), method = "executeCrafting", cancellable = true)
     * private void executeCrafting3(final IEnergyGrid eg, final CraftingGridCache cc, CallbackInfo RE) {
     * }
     */
    private static AEItemStack type = AEItemStack.create(new ItemStack(MyMod.eu_token, 1, 1));

    @Inject(at = @At("RETURN"), method = "executeCrafting", cancellable = true)
    private void executeCrafting1(final IEnergyGrid eg, final CraftingGridCache cc, CallbackInfo RE) {

        MixinCallback.cb(eg, cc, RE, needed, storage, inventory, tasks, tiles, machineSrc);

    }

}
