package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.util.Optional;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.IAEAppEngInventory;
import net.minecraft.item.ItemStack;
import reobf.proghatches.main.MyMod;

@Mixin(value = StackUpgradeInventory.class, remap = false, priority = 1)
public abstract class MixinUpgrade extends UpgradeInventory{

	public MixinUpgrade() {
		super(null, 0);
	}
	
	 /* @Inject(method = "isItemValidForSlot",
		        at = @At(value = "HEAD"), cancellable = true
		        )*/
	 public boolean isItemValidForSlot(final int ix, final ItemStack itemstack) {
		 if(itemstack!=null){
			 if(itemstack.getItem()==MyMod.euupgrade){
				 
				 
					for(int i=0;i<getSizeInventory();i++){
						if(
						Optional.ofNullable(getStackInSlot(i))
							.map(ItemStack::getItem)
							.filter(s->s==MyMod.euupgrade)
							.isPresent()
						){
							return false;
						}

						
						
					}
				 
				return true;
			}
		 }
		 return super.isItemValidForSlot(ix, itemstack);
	 }
}
