package reobf.proghatches.main.mixin.mixins.part2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.util.GTRecipe;
import gregtech.common.tileentities.machines.IDualInputInventory;
import reobf.proghatches.gt.metatileentity.util.IDoNotCacheThisPattern;

@Mixin(value = ProcessingLogic.class, remap = false)
public class MixinProcessLogicDoNotCache {
/*
    @Shadow
    protected Map<IDualInputInventory, Set<GTRecipe>> craftingPatternRecipeCache = new HashMap<>();
    @Shadow
    protected IDualInputInventory craftingPattern;

    @Inject(
        at = { @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;") },
        method = { "process" })
    public void process(CallbackInfoReturnable x) {
        if (craftingPattern instanceof IDoNotCacheThisPattern) {
            IDoNotCacheThisPattern p = (IDoNotCacheThisPattern) craftingPattern;
            //if (p.areYouSerious()) {//true
                Set<GTRecipe> removed = craftingPatternRecipeCache.remove(craftingPattern);
                craftingPattern = null;
                IDualInputInventory instead = p.butYouCanCacheThisInstead();
                if (instead != null) {
                    craftingPatternRecipeCache.put(instead, removed);
                }
            //}
        }

    }*/

    /*
     * public boolean craftingPatternHandler(IDualInputInventory slot) {
     * if (craftingPatternRecipeCache.containsKey(slot)) {
     * craftingPattern = slot;
     * return true;
     * }
     * GTDualInputs inputs = slot.getPatternInputs();
     * setInputItems(inputs.inputItems);
     * setInputFluids(inputs.inputFluid);
     * Set<GTRecipe> recipes = findRecipeMatches(getCurrentRecipeMap()).collect(Collectors.toSet());
     * setInputItems();
     * setInputFluids();
     * if (!recipes.isEmpty()) {
     * craftingPatternRecipeCache.put(slot, recipes);
     * craftingPattern = slot;
     * return true;
     * }
     * return false;
     * }
     */
}
