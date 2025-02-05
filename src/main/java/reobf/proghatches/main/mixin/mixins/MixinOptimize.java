package reobf.proghatches.main.mixin.mixins;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import reobf.proghatches.gt.metatileentity.util.IDisallowOptimize;

@Mixin(targets = "appeng.container.implementations.ContainerOptimizePatterns", remap = false)
public class MixinOptimize {

    private Field f;

    private HashSet<ICraftingPatternDetails> patternDetails(Object pt) {
        if (f == null) {

            try {
                f = pt.getClass()
                    .getDeclaredField("patternDetails");
                f.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
                throw new AssertionError("cannot continue", e);
            }

        }

        try {
            return (HashSet<ICraftingPatternDetails>) f.get(pt);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("cannot continue", e);
        }

    }

    @Shadow(remap = false)
    HashMap<IAEItemStack, Object> patterns = new HashMap<>();

    @Inject(
        method = "setResult",
        at = { @At(value = "NEW", target = "appeng/core/sync/packets/PacketMEInventoryUpdate", shift = Shift.BEFORE) },
        remap = false)
    public void setResult(ICraftingJob result, CallbackInfo ci) {

        this.patterns.entrySet()
            .removeIf(
                entry -> patternDetails(entry.getValue()).stream()
                    .filter(s -> s instanceof IDisallowOptimize)
                    .count() > 0);
    }

}
