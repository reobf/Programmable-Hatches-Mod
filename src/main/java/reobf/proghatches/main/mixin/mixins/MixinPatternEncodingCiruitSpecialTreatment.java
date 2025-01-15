package reobf.proghatches.main.mixin.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.glodblock.github.nei.object.OrderStack;
import com.glodblock.github.network.CPacketTransferRecipe;

import io.netty.buffer.ByteBuf;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.item.ItemProgrammingToolkit;
import reobf.proghatches.main.mixin.MixinCallback;

@Mixin(value = CPacketTransferRecipe.class, remap = false)
public class MixinPatternEncodingCiruitSpecialTreatment {

	@Shadow
	private List<OrderStack<?>> inputs;
	@Shadow private boolean isCraft;
	
	@Inject(method = "toBytes", at = @At(value = "HEAD"), require = 1, cancellable = true)

	private void toBytes(ByteBuf buf, CallbackInfo c) {
		if(isCraft)return;//don't mess with workbench recipe 
		inputs=MixinCallback.encodeCallback( inputs);

	}
}
