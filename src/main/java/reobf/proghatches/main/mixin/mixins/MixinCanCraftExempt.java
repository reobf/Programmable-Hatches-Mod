package reobf.proghatches.main.mixin.mixins;

import net.minecraft.inventory.InventoryCrafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import codechicken.nei.InventoryCraftingDummy;
import reobf.proghatches.eucrafting.IInputMightBeEmptyPattern;

@SuppressWarnings("unused")
@Mixin(value = CraftingCPUCluster.class, remap = false, priority = 1)
public class MixinCanCraftExempt {

    // public boolean shouldExempt;

    @ModifyVariable(
        method = "executeCrafting",
        at = @At(
            value = "INVOKE",
            target = "getMediums(Lappeng/api/networking/crafting/ICraftingPatternDetails;)Ljava/util/List;"),
        require = 1)
    private ICraftingPatternDetails executeCrafting(ICraftingPatternDetails details,
        @Share("arg") LocalBooleanRef shouldExempt) {
        shouldExempt.set((details instanceof IInputMightBeEmptyPattern));
        return details;

    }

    @ModifyVariable(method = "executeCrafting", at = @At(value = "INVOKE", target = "isBusy()Z"), require = 1)
    private InventoryCrafting executeCrafting0(InventoryCrafting x, @Share("arg") LocalBooleanRef shouldExempt) {

        return shouldExempt.get() ? (new InventoryCraftingDummy()) : x;

    }

    @Inject(method = "canCraft", at = @At("RETURN"), cancellable = true, require = 1)
    private void canCraft(final ICraftingPatternDetails details, final IAEItemStack[] condensedInputs,
        CallbackInfoReturnable<Boolean> ci) {
        if ((details instanceof IInputMightBeEmptyPattern)) {
            ci.setReturnValue(true);
        }
    }
}
