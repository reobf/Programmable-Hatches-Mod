package reobf.proghatches.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IDefaultRunnable {
public default void run(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_){};
}
