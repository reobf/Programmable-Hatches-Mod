package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.modularui.api.forge.IItemHandler;
import com.gtnewhorizons.modularui.api.forge.SlotItemHandler;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

@Mixin(value=BaseSlot.class,remap=false
)
public abstract class MixinBaseSlot extends SlotItemHandler{
	public MixinBaseSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Inject(cancellable=true,method = "isItemValid", at = @At(value = "RETURN"), require = 1)
	public void a(ItemStack s,CallbackInfoReturnable<Boolean> a) {
		if(a.getReturnValue()){
			ItemStack is=getStack();
			if(is!=null&&is.stackSize>is.getMaxStackSize()){
			a.setReturnValue(false);
			return;
			}
		}
		
		
	}
	
	 //@Shadow
	 //   public abstract ItemStack getStack() ;
}
