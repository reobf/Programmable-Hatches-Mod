package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import reobf.proghatches.eucrafting.IInstantCompletable;

@Mixin(value = CraftingCPUCluster.class, remap = false, priority = 1)
public class MixinInstantComplete {
	//spotless:off
	@Inject(at = {@At(value = "INVOKE", shift = Shift.BEFORE, target = "markDirty()V") }, 
			method = "executeCrafting", require = 1, locals = LocalCapture.CAPTURE_FAILHARD
	)
	//spotless:on
	public void a(IEnergyGrid eg, CraftingGridCache cc, CallbackInfo ci, Iterator i, Map.Entry e,
			ICraftingPatternDetails details, InventoryCrafting ic, boolean pushedPattern, Iterator var8,
			ICraftingMedium m) {

		if (m instanceof IInstantCompletable) {
			((IInstantCompletable) m).complete();

		}

	}

}
