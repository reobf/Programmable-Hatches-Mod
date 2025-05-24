package reobf.proghatches.main.mixin.mixins.part2;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.util.item.GhostCircuitItemStackHandler;
import gregtech.common.items.ItemIntegratedCircuit;
import net.minecraft.item.ItemStack;

@Mixin(value=GhostCircuitItemStackHandler.class,remap=false)
public class MixinMUI2CircuitSlot {
	@Inject(method="isItemValid", at = { @At("RETURN") },cancellable=true)
    public void isItemValid(int slot, @Nullable ItemStack stack,CallbackInfoReturnable<Boolean> cb) {
       
		cb.setReturnValue(stack !=null);
    }
}
