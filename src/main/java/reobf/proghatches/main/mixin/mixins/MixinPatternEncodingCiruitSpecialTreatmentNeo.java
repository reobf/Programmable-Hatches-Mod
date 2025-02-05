package reobf.proghatches.main.mixin.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.glodblock.github.nei.FluidPatternTerminalRecipeTransferHandler;
import com.glodblock.github.nei.object.OrderStack;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import codechicken.nei.recipe.IRecipeHandler;
import reobf.proghatches.main.mixin.MixinCallback;

@Mixin(value = FluidPatternTerminalRecipeTransferHandler.class, remap = false)
public class MixinPatternEncodingCiruitSpecialTreatmentNeo {

    private static Class<?> gtDefaultClz;
    private static Class<?> gtAssLineClz;

    /*
     * @Shadow
     * private List<OrderStack<?>> inputs;
     * @Shadow private boolean isCraft;
     */
    @Inject(
        method = "overlayRecipe",

        at = @At(
            value = "INVOKE_ASSIGN",
            target = "shouldCraft(Lcodechicken/nei/recipe/IRecipeHandler;)Z",
            shift = Shift.AFTER),

        require = 1,
        cancellable = false)

    private void overlayRecipe(CallbackInfo c, @Local(name = "recipe") IRecipeHandler recipe,
        @Local(name = "in") LocalRef<List<OrderStack<?>>> mergedInputs, @Local(name = "craft") boolean craft) {
        if (canProcessRecipe(recipe) == false) {
            return;
        }
        if (craft) return;// don't mess with workbench recipe
        mergedInputs.set(MixinCallback.encodeCallback(mergedInputs.get()));;

    }

    private boolean init;

    private boolean canProcessRecipe(IRecipeHandler recipe) {
        if (!init) {
            Class<?> gtDH = null;
            Class<?> gtAL = null;
            try {
                gtDH = Class.forName("gregtech.nei.GTNEIDefaultHandler");
                gtAL = Class.forName("gregtech.nei.GT_NEI_AssLineHandler");
            } catch (ClassNotFoundException ignored) {}
            gtDefaultClz = gtDH;
            gtAssLineClz = gtAL;
            init = true;

        }

        return (gtDefaultClz != null && gtDefaultClz.isInstance(recipe))
            || (gtAssLineClz != null && gtAssLineClz.isInstance(recipe));
    }
}
