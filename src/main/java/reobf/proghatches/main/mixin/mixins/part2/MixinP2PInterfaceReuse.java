package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.parts.p2p.PartP2PInterface;
import appeng.parts.p2p.PartP2PTunnel;
import reobf.proghatches.eucrafting.IReusedP2PHost;

@Mixin(value = PartP2PInterface.class, remap = false)
public abstract class MixinP2PInterfaceReuse {

    // cover hosts hand the same part back from replacePartInWorld, so it gets copied onto itself
    @Inject(method = "copyContents", at = @At("HEAD"), cancellable = true, require = 1)
    private void proghatches$copyOntoItself(PartP2PTunnel<?> from, CallbackInfo ci) {
        if (from != (Object) this || !(from.getHost() instanceof IReusedP2PHost host)) return;
        ci.cancel();
        host.afterPartReused();
    }
}
