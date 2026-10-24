package reobf.proghatches.main.mixin.mixins;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.v2.resolvers.CraftableItemResolver.CraftFromPatternTask;
import reobf.proghatches.eucrafting.IInputMightBeEmptyPattern;

@Mixin(value = CraftFromPatternTask.class, remap = true)
public class MixinCraftFromPatternTaskPatch {

    // this is always true for 2.7.x
    /*
     * private static Boolean shouldPatch=null;
     * private boolean shouldPatch(){
     * if(shouldPatch!=null)return shouldPatch;
     * try {
     * //if method CraftFromPatternTask#calculateRecursiveInputs exists, AE is using a new way to calculate IO, do patch
     * CraftFromPatternTask.class.getDeclaredMethod("calculateRecursiveInputs", IAEItemStack[] .class,
     * IAEItemStack[].class);
     * shouldPatch=true;
     * } catch (NoSuchMethodException e) {
     * //method missing means okay to skip patch
     * shouldPatch=false;
     * }
     * return shouldPatch();
     * }
     */
    @WrapOperation(
        method = "<init>(Lappeng/crafting/v2/CraftingRequest;Lappeng/api/networking/crafting/ICraftingPatternDetails;IZZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/networking/crafting/ICraftingPatternDetails;getCondensedAEInputs()[Lappeng/api/storage/data/IAEStack;"),
        remap = false,
        require = 1)
    private IAEStack<?>[] dropEmptyInputs(ICraftingPatternDetails pattern, Operation<IAEStack<?>[]> original) {
        IAEStack<?>[] inputs = original.call(pattern);
        if (!(pattern instanceof IInputMightBeEmptyPattern)) return inputs;
        return Arrays.stream(inputs)
            .filter(s -> s != null && s.getStackSize() > 0)
            .toArray(IAEStack<?>[]::new);
    }

    @WrapOperation(
        method = "<init>(Lappeng/crafting/v2/CraftingRequest;Lappeng/api/networking/crafting/ICraftingPatternDetails;IZZ)V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/crafting/v2/resolvers/CraftableItemResolver$CraftFromPatternTask;hasZeroSizedStack([Lappeng/api/storage/data/IAEStack;)Z"),
        remap = false,
        require = 0,
        expect = 0)
    private boolean allowEmptyInputPattern(IAEStack<?>[] stacks, Operation<Boolean> original,
        @Local(argsOnly = true) ICraftingPatternDetails pattern) {
        return !(pattern instanceof IInputMightBeEmptyPattern) && original.call((Object) stacks);
    }

}
