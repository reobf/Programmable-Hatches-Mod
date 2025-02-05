package reobf.proghatches.eucrafting;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.main.mixin.MixinPlugin;

public class ItemBlockEUInterface extends AEBaseItemBlock {
    // private final Block blockType;

    public ItemBlockEUInterface(Block id) {
        super(id);
        // blockType = (Block) id;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_,
        boolean p_77624_4_) {
        if (MixinPlugin.noEUMixin) {
            p_77624_3_.add(StatCollector.translateToLocal("proghatch.eucrafting.warn"));
        }
        super.addCheckedInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
    }
}
