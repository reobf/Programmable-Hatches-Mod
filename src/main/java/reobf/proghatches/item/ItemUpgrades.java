package reobf.proghatches.item;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.CommonProxy;

public class ItemUpgrades extends Item{
	@Override
	public boolean getHasSubtypes() {
		// TODO Auto-generated method stub
		return true;
	}@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {

		IntStream
				.range(0,
						Integer.valueOf(StatCollector.translateToLocal(
								"item.proghatch.upgrades.tooltips." + p_77624_1_.getItemDamage())))
				.forEach(s -> p_77624_3_.add(LangManager.translateToLocal(
						"item.proghatch.upgrades.tooltips." + p_77624_1_.getItemDamage() + "." + s)));

		super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	}
	@SuppressWarnings("unchecked")
	@Override @SideOnly(Side.CLIENT)
	public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
		
		list.forEach(i->
		  p_150895_3_.add(new ItemStack(p_150895_1_, 1, i)));
	}
	@Override
	public String getItemStackDisplayName(ItemStack p_77653_1_) {

		return LangManager.translateToLocal("item.proghatch.upgrades." + p_77653_1_.getItemDamage());
	}
	@Override
	public IIcon getIconFromDamage(int p_77617_1_) {

		return icons[p_77617_1_];
	}
	public static IIcon[] icons = new IIcon[64];

	public ItemUpgrades() {
		this.setCreativeTab(CommonProxy.tab);
	}

	@SideOnly(Side.CLIENT)
	public static int mark(int i){
		if(list==null){list=new TreeSet<>();}
		list.add(i);
		return i;
	}

	@SideOnly(Side.CLIENT)
	public static TreeSet<Integer> list;

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister register) {
		icons[mark(0)] = register.registerIcon("proghatches:upgrade0");
		icons[mark(1)] = register.registerIcon("proghatches:upgrade1");
		icons[mark(2)] = register.registerIcon("proghatches:upgrade2");
		icons[mark(3)] = register.registerIcon("proghatches:upgrade3");
	}
}
