package reobf.proghatches.main.mixin.mixins;

import java.util.Collection;
import java.util.Collections;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import reobf.proghatches.main.MyMod;

@Mixin(value = ItemList.class, remap = false)
public abstract class MixinNoFuzzyForProgrammingCircuit implements IItemList<IAEItemStack> {

	@Inject(cancellable = true, method = "findFuzzy", at = {
			@At(value = "INVOKE", target = "Lappeng/util/item/AEItemStack;isOre()Z", shift = Shift.BEFORE) }, require = 1)
	public void prevent(final IAEItemStack filter, final FuzzyMode fuzzy,
			CallbackInfoReturnable<Collection<IAEItemStack>> xx) {
		if ((filter.getItem() == MyMod.progcircuit)) {

			IAEItemStack obj = findPrecise(filter);
			xx.setReturnValue(obj == null ? Collections.emptyList() : Collections.singletonList(obj)

			);
		}

	}

}
