package reobf.proghatches.item;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.CommonProxy;

public class ItemDedicatedCover extends Item {

	public static IIcon[] icons = new IIcon[200];

	public ItemDedicatedCover() {
		this.setCreativeTab(CommonProxy.tab);
	}

	@Override
	public boolean getHasSubtypes() {
		// TODO Auto-generated method stub
		return true;
	}
@SuppressWarnings("unchecked")
@Override @SideOnly(Side.CLIENT)
public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
	
	list.forEach(i->
	  p_150895_3_.add(new ItemStack(p_150895_1_, 1, i)));
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
		icons[mark(0)] = register.registerIcon("proghatches:cover0");
		icons[mark(1)] = register.registerIcon("proghatches:cover1");
		icons[2] = register.registerIcon("proghatches:cover2");//disabled
		icons[mark(3)] = register.registerIcon("proghatches:cover2");
		icons[mark(4)] = register.registerIcon("proghatches:cover0");
		icons[mark(32)] = register.registerIcon("proghatches:cover32");
		icons[mark(33)] = register.registerIcon("proghatches:cover33");
		icons[mark(34)] = register.registerIcon("proghatches:cover34");
		icons[mark(35)] = register.registerIcon("proghatches:cover35");
		icons[mark(36)] = register.registerIcon("proghatches:cover36");
		icons[mark(37)] = register.registerIcon("proghatches:cover37");
		icons[mark(100)] = register.registerIcon("proghatches:cover37");
		
		icons[mark(90)] = register.registerIcon("proghatches:circuitholder");
		icons[mark(91)] = register.registerIcon("proghatches:circuitholder1");
		icons[mark(92)] = register.registerIcon("proghatches:circuitholder2");
		icons[mark(93)] = register.registerIcon("proghatches:circuitholder3");
		icons[mark(94)] = register.registerIcon("proghatches:circuitholder4");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {

		IntStream
				.range(0,
						Integer.valueOf(StatCollector.translateToLocal(
								"item.proghatch.cover.dedicated.tooltips." + p_77624_1_.getItemDamage())))
				.forEach(s -> p_77624_3_.add(LangManager.translateToLocal(
						"item.proghatch.cover.dedicated.tooltips." + p_77624_1_.getItemDamage() + "." + s)));

		super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	}

	@Override
	public String getItemStackDisplayName(ItemStack p_77653_1_) {

		return LangManager.translateToLocalFormatted("item.proghatch.cover.dedicated." + p_77653_1_.getItemDamage());
	}

	@Override
	public IIcon getIconFromDamage(int p_77617_1_) {

		return icons[p_77617_1_];
	}

}
