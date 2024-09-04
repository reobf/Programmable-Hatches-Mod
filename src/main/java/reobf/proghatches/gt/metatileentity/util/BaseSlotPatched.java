package reobf.proghatches.gt.metatileentity.util;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class BaseSlotPatched extends BaseSlot {

	public BaseSlotPatched(IItemHandlerModifiable inventory, int index) {
		super(inventory, index);
		
	}
	
	/*public BaseSlotPatched(IItemHandlerModifiable inventory, int index, boolean phantom) {
		super(inventory, index, phantom);
	
	}*/
@Override
public boolean canTakeStack(EntityPlayer playerIn) {
	//skip simulate take test, this is always true
	return true;
}

	public static Function<Integer,BaseSlot> newInst(IItemHandlerModifiable inventory){
		
		return s->new  BaseSlotPatched(inventory,s);
		
	}
	
	
	
	
	@Override
	public boolean isItemValid(@NotNull ItemStack stack) {
		boolean b = super.isItemValid(stack);
		if (b) {
			ItemStack is = getStack();
			if (is != null && is.stackSize > is.getMaxStackSize()) {
				return false;
			}
		}
		return b;
	}

}
