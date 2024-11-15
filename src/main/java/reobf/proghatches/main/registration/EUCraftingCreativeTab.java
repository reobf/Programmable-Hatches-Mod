package reobf.proghatches.main.registration;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import reobf.proghatches.main.MyMod;

public class EUCraftingCreativeTab extends CreativeTabs {

	public EUCraftingCreativeTab(String lable) {
		super(lable);

	}

	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {
		return new ItemStack(MyMod.block_euinterface);
	}

	@Override
	public Item getTabIconItem() {

		return Item.getItemFromBlock(MyMod.block_euinterface);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void displayAllReleventItems(List p_78018_1_) {
		
		
		super.displayAllReleventItems(p_78018_1_);
	}
}
