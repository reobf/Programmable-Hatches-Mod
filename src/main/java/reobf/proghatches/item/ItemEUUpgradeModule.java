package reobf.proghatches.item;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemEUUpgradeModule extends Item implements IUpgradeModule{

	@Override
	public Upgrades getType(ItemStack itemstack) {
		return Upgrades.REDSTONE;//Upgrades.class is enum
	}

}
