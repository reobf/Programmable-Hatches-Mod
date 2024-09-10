package reobf.proghatches.main.mixin.mixins;

import java.util.Iterator;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import codechicken.nei.InventoryCraftingDummy;
import net.minecraft.inventory.InventoryCrafting;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;

@SuppressWarnings("unused")
@Mixin(value = CraftingCPUCluster.class, remap = false, priority = 1)
public class MixinCanCraftExempt {

	public boolean shouldExempt;

	@ModifyVariable(method = "executeCrafting", at = @At(value = "INVOKE",  target = "getMediums(Lappeng/api/networking/crafting/ICraftingPatternDetails;)Ljava/util/List;"),require=1)
	private ICraftingPatternDetails executeCrafting(
			ICraftingPatternDetails details) {
		shouldExempt = (details instanceof ProgrammingCircuitProvider.CircuitProviderPatternDetial);
		return details;

	}

	

	

	@ModifyVariable(method = "executeCrafting", at = @At(value = "INVOKE",target = "isBusy()Z"), require = 1)
	private InventoryCrafting executeCrafting0(InventoryCrafting x) {

		return shouldExempt ? (new InventoryCraftingDummy()) : x;

	}
	
	
	
	
	
	@Inject(method = "canCraft", at = @At("RETURN"), cancellable = true, require = 1)
	private void canCraft(final ICraftingPatternDetails details, final IAEItemStack[] condensedInputs,
			CallbackInfoReturnable<Boolean> ci) {
		if ((details instanceof ProgrammingCircuitProvider.CircuitProviderPatternDetial)) {
			ci.setReturnValue(true);
		}
		}
}
