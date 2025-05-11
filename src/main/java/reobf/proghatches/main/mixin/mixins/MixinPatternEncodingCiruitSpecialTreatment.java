package reobf.proghatches.main.mixin.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.glodblock.github.nei.object.OrderStack;
import com.glodblock.github.network.CPacketTransferRecipe;

import io.netty.buffer.ByteBuf;
import reobf.proghatches.main.mixin.MixinCallback;
@Deprecated
@Mixin(value = CPacketTransferRecipe.class, remap = false)
public class MixinPatternEncodingCiruitSpecialTreatment {

    @Shadow
    private List<OrderStack<?>> inputs;
    @Shadow
    private boolean isCraft;

    @Inject(method = "toBytes", at = @At(value = "HEAD"), require = 1, cancellable = true)

    private void toBytes(ByteBuf buf, CallbackInfo c) {
        if (isCraft) return;// don't mess with workbench recipe
        inputs = MixinCallback.encodeCallback(inputs);

    }
}
