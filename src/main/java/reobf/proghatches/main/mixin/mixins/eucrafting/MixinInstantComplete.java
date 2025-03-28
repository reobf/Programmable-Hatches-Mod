package reobf.proghatches.main.mixin.mixins.eucrafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import reobf.proghatches.eucrafting.IInstantCompletable;

@Mixin(value = CraftingCPUCluster.class, remap = false, priority = 1)
public class MixinInstantComplete {

    @Inject(at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "markDirty"), method = "executeCrafting")

    public void MixinInstantComplete_executeCrafting(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci,

        @Local ICraftingMedium temp) {

        if (temp instanceof IInstantCompletable) {
            ((IInstantCompletable) temp).complete();

        }
        temp = null;

    }

}
