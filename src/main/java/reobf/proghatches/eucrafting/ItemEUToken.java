package reobf.proghatches.eucrafting;

import java.util.List;
import java.util.Optional;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import reobf.proghatches.util.ProghatchesUtil;

public class ItemEUToken extends Item{
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
		Optional.ofNullable(p_77624_1_.stackTagCompound)
		.ifPresent(s->{
			Optional.of(ProghatchesUtil.deser(s, "EUFI"))
			.filter(ss->ss.getLeastSignificantBits()!=0&&ss.getMostSignificantBits()!=0)
			.ifPresent(ss->
			p_77624_3_.add("Host UUID:"+ss.toString()));
			if(p_77624_1_.stackSize==0)
			p_77624_3_.add(s.getLong("voltage")+"V");//this is possible on crafting terminal
				else
			p_77624_3_.add(s.getLong("voltage")+"V, "+p_77624_1_.stackSize+"*1A");
			
			
		});
	
	}
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if(stack.getItemDamage()==1){
			
			return super.getUnlocalizedName(stack)+".1";
		}
		return super.getUnlocalizedName(stack);
	}
}
