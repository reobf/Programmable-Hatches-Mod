package reobf.proghatches.main.mixin;

import appeng.util.inv.MEInventoryCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class MEInvDummy extends MEInventoryCrafting{

	public MEInvDummy() {
		super(new Container() {
			
			@Override
			public boolean canInteractWith(EntityPlayer player) {
			
				return false;
			}
		}, 3, 3);
	
	}

}
