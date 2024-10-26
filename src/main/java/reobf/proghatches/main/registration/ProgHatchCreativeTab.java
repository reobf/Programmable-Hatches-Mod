package reobf.proghatches.main.registration;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
import gregtech.api.util.GT_Utility;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;
import thaumcraft.common.config.ConfigItems;

public class ProgHatchCreativeTab extends CreativeTabs {

	public ProgHatchCreativeTab(String lable) {
		super(lable);
		// TODO Auto-generated constructor stub
	}

	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {
		return ItemProgrammingCircuit.wrap(GT_Utility.getIntegratedCircuit(0));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void displayAllReleventItems(List p_78018_1_) {
		p_78018_1_.addAll(Registration.items);

		p_78018_1_.add(ItemProgrammingCircuit.wrap(null));
		GregTech_API.getConfigurationCircuitList(100).stream().map(ItemProgrammingCircuit::wrap)
				.forEach(p_78018_1_::add);
		p_78018_1_.add(new ItemStack(MyMod.fixer));
		p_78018_1_.add(new ItemStack(MyMod.toolkit));
		for (int i = 0; i < 15; i++) {
			p_78018_1_.add(new ItemStack(MyMod.smartarm, 1, i));
		}
		p_78018_1_.add(new ItemStack(MyMod.cover, 1, 4));
		p_78018_1_.add(new ItemStack(MyMod.cover, 1, 0));
		p_78018_1_.add(new ItemStack(MyMod.cover, 1, 1));
		// p_78018_1_.add(new ItemStack(MyMod.cover, 1, 2));
		p_78018_1_.add(new ItemStack(MyMod.oc_api, 1));
		p_78018_1_.add(new ItemStack(MyMod.oc_redstone, 1));
		p_78018_1_.add(new ItemStack(MyMod.iohub, 1));
		p_78018_1_.add(new ItemStack(MyMod.pitem, 1));
		p_78018_1_.add(new ItemStack(MyMod.pstation, 1));
		p_78018_1_.add(new ItemStack(MyMod.plunger, 1));
		
		p_78018_1_.add(new ItemStack(MyMod.upgrades, 1, 0));
		p_78018_1_.add(new ItemStack(MyMod.upgrades, 1, 1));
		p_78018_1_.add(new ItemStack(MyMod.upgrades, 1, 2));
		//p_78018_1_.add(new ItemStack(MyMod.alert, 1));
		p_78018_1_.add(new ItemStack(MyMod.lazer_p2p_part));
		p_78018_1_.add(new ItemStack(ConfigItems.itemGolemCore,1,120));
		p_78018_1_.add(new ItemStack(MyMod.amountmaintainer));
		p_78018_1_.add(new ItemStack(MyMod.submitter));
		p_78018_1_.add(new ItemStack(MyMod.reader));
		p_78018_1_.add(new ItemStack(MyMod.reactorsyncer));
		p_78018_1_.add(new ItemStack(MyMod.partproxy,1,0));
		p_78018_1_.add(new ItemStack(MyMod.partproxy,1,1));
		p_78018_1_.add(new ItemStack(MyMod.partproxy,1,2));
		p_78018_1_.add(new ItemStack(MyMod.storageproxy,1,0));
		p_78018_1_.add(new ItemStack(MyMod.storageproxy,1,1));
		p_78018_1_.add(new ItemStack(MyMod.storageproxy,1,2));
		p_78018_1_.add(new ItemStack(MyMod.exciter,1,0));
		//p_78018_1_.add(new ItemStack(MyMod.storageproxy));
		// p_78018_1_.add(new ItemStack(MyMod.euupgrade, 1));
	}

	@Override
	public Item getTabIconItem() {

		return MyMod.progcircuit;
	}

}
