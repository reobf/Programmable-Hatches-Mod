package reobf.proghatches.main.registration;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.util.GT_Utility;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;

public  class EUCraftingCreativeTab extends CreativeTabs {

	public EUCraftingCreativeTab(String lable) {
		super(lable);
		// TODO Auto-generated constructor stub
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
		p_78018_1_.addAll(Registration.items_eucrafting);
		p_78018_1_.add(new ItemStack(MyMod.eu_source_part));
		p_78018_1_.add(new ItemStack(MyMod.block_euinterface));
	   // p_78018_1_.add(new ItemStack(MyMod.cover, 1, 2));
	    p_78018_1_.add(new ItemStack(MyMod.euinterface_p2p));
	    p_78018_1_.add(new ItemStack(MyMod.cover, 1, 3));
	    p_78018_1_.add(new ItemStack(MyMod.cover, 1, 32));
	    p_78018_1_.add(new ItemStack(MyMod.cover, 1, 33));
	    p_78018_1_.add(new ItemStack(MyMod.cover, 1, 34));
	    p_78018_1_.add(new ItemStack(MyMod.cover, 1, 35));
	    p_78018_1_.add(new ItemStack(MyMod.cover, 1, 36));
		super.displayAllReleventItems(p_78018_1_);
	}
}
