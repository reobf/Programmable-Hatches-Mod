package reobf.proghatches.block;

import java.util.List;

import com.gtnewhorizons.modularui.api.KeyboardUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import reobf.proghatches.lang.LangManager;

public class ItemBlockTooltip extends ItemBlock {

	public ItemBlockTooltip(Block p_i45328_1_,String key) {
		super(p_i45328_1_);this.key=key;
	}
	String key;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_,  List p_77624_3_, boolean p_77624_4_) {
		
		if(field_150939_a instanceof INameAndTooltips){
			((INameAndTooltips) field_150939_a).addInformation(p_77624_1_, p_77624_3_);
		}
		
		
		
		super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	}
@Override
public int getMetadata(int p_77647_1_) {

	return (p_77647_1_);
}
@Override
public boolean getHasSubtypes() {
	
	return true;
}
@Override
public String getUnlocalizedName(ItemStack stack) {
	if(field_150939_a instanceof INameAndTooltips){
		return ((INameAndTooltips) field_150939_a).getName(stack);
	}
	return super.getUnlocalizedName();
}
}

