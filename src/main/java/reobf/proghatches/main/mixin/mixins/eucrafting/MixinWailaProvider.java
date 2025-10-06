package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.integration.modules.waila.PartWailaDataProvider;
import appeng.integration.modules.waila.part.IPartWailaDataProvider;
import reobf.proghatches.ae.part2.PartCoW;
import reobf.proghatches.ae.part2.PartRequestTunnel;


@Mixin(value = PartWailaDataProvider.class, remap = false, priority = 1)
public class MixinWailaProvider {

    @Shadow
    private List<IPartWailaDataProvider> providers;

    @Inject(method = "<init>", at = @At(value = "RETURN"), require = 1)
    public void constructor(CallbackInfo a) {

     
       
        providers.add(new PartRequestTunnel.WailaDataProvider());
        providers.add(new PartCoW.WailaDataProvider());
    }

}
