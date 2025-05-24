package reobf.proghatches.item;

import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class ItemBadge extends Item{

	
	@Override
public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {

IntStream.range(0, Integer.valueOf(StatCollector.translateToLocal("item.proghatch_badge.tooltips")))
.forEach(s->{
	p_77624_3_.add(StatCollector.translateToLocal("item.proghatch_badge.tooltips."+s));
});
;
	super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
}
	
	
	
	
}
