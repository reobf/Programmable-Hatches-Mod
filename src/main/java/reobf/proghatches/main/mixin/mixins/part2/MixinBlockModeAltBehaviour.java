package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizon.gtnhlib.util.Callback;

import appeng.helpers.BlockingModeIgnoreList;
import appeng.helpers.DualityInterface;
import appeng.util.InventoryAdaptor;
import net.minecraft.item.ItemStack;

@Mixin(value=DualityInterface.class)
public class MixinBlockModeAltBehaviour {
	@Redirect(remap=false,method="gtMachineHasOnlyCircuit", require = 1, at = @At(value="INVOKE",target=
			
			"Lappeng/helpers/BlockingModeIgnoreList;isIgnored(Lnet/minecraft/item/ItemStack;)Z"))
public boolean isIgnored(ItemStack is){
	
	
		if(is ==null)return true;
		
	return	BlockingModeIgnoreList.isIgnored(is);
}
}
