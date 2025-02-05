package reobf.proghatches.main.mixin.mixins;

import java.util.Optional;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;

// spotless:off
/**
 * make it able to insert progcurcuit into input-buses with filter-mode on
 * If item is progcurcuit, treat it as the wrapped item
 * 
 * 
 */
//spotless:on
@Mixin(value = gregtech.api.recipe.RecipeMap.class, remap = false)
public abstract class MixinAddProgCircuitExemptToInputFilter {

    @Inject(method = "containsInput", at = @At("RETURN"), require = 1, cancellable = true)
    public void containsInput(ItemStack aStack, CallbackInfoReturnable<Boolean> c) {
        if (aStack == null) return;
        boolean ret = c.getReturnValueZ();
        if (ret) return;
        if (aStack.getItem() != MyMod.progcircuit) return;
        Optional<ItemStack> op = ItemProgrammingCircuit.getCircuit(aStack);
        c.setReturnValue(op.isPresent() ? containsInput(op.orElse(null)) : true);
        // containsInput(null)->false but it should be accepted, so return true
    }

    @Shadow
    abstract public boolean containsInput(ItemStack aStack);

}
