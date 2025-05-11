package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.v2.CraftingContext;
import appeng.util.item.HashBasedItemList;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.WrappedPatternDetail;
import reobf.proghatches.main.MyMod;

// spotless:off
/*	getPrecisePatternsFor will ignore recursive outputs, but CraftFromPatternTask will not
 * results in...
 * [Server thread/WARN] [AE2:S]: Error encountered when trying to generate the list of CraftingTasks for crafting {}
 * 
 * is it a bug?
 * this is surely not my fault... this warning shows on normal recursive patterns too
 * 
 * 
 * 
 * 
 * this mixin hides recursive outputs from AE to workaround
 * , in case CraftFromPatternTask will throw an Exception and terminates further pattern search
 * */
//spotless:on
@Deprecated
@Mixin(value = CraftingContext.class, remap = false, priority = 0)
public class MixinCraftingRecursiveWorkaround {

    @Final
    @Shadow
    Map<IAEItemStack, List<ICraftingPatternDetails>> precisePatternCache;
    @Final
    @Shadow
    ImmutableMap<IAEItemStack, ImmutableList<ICraftingPatternDetails>> availablePatterns;

    @Inject(at = @At("HEAD"), method = "getPrecisePatternsFor", cancellable = true)
    public void getPrecisePatternsFor(@Nonnull IAEItemStack stack,
        CallbackInfoReturnable<List<ICraftingPatternDetails>> RE) {
        try {
            List<ICraftingPatternDetails> o = precisePatternCache.compute(stack, (key, value) -> {
                if (value == null) {
                    if (stack.getItem() != MyMod.eu_token)// do not 'fix' other
                                                          // patterns
                        return availablePatterns.getOrDefault(stack, ImmutableList.of());

                    List<ICraftingPatternDetails> l = availablePatterns.getOrDefault(stack, ImmutableList.of())
                        .stream()
                        .filter(s -> {
                            if (!(s instanceof WrappedPatternDetail)) return true;// do not 'fix' other patterns

                            // IAEItemStack sta =
                            // calculatePatternIO(s).findPrecise(stack);
                            // boolean a=sta!=null&&sta.getStackSize()>0;
                            boolean b = calculatePatternIOs(s, stack);
                            // if(a!=b){

                            // System.out.println(123);

                            // }
                            return b;
                            //

                        })
                        .collect(Collectors.toList());

                    return l;

                } else {
                    return value;
                }
            });

            RE.setReturnValue(o);
        } catch (Exception e) {
            MyMod.LOG.error("caught error in mixin", e);
        }
    }

    private static boolean calculatePatternIOs(ICraftingPatternDetails pattern, IAEItemStack stack) {
        HashBasedItemList pInputs = new HashBasedItemList();
        HashBasedItemList pOutputs = new HashBasedItemList();
        Arrays.stream(pattern.getInputs())
            .filter(Objects::nonNull)
            .forEach(pInputs::add);
        Arrays.stream(pattern.getOutputs())
            .filter(Objects::nonNull)
            .forEach(pOutputs::add);
        IAEItemStack output = pOutputs.findPrecise(stack);
        IAEItemStack input = pInputs.findPrecise(stack);

        return Optional.ofNullable(output)
            .map(IAEItemStack::getStackSize)
            .orElse(0l)
            > Optional.ofNullable(input)
                .map(IAEItemStack::getStackSize)
                .orElse(0l);
    }
}
