package reobf.proghatches.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import com.gtnewhorizons.modularui.api.KeyboardUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.lang.LangManager;

public class ItemBlockIOHub extends ItemBlock {

    public ItemBlockIOHub(Block p_i45328_1_) {
        super(p_i45328_1_);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
        int i = 0;
        while (true) {
            String k = KeyboardUtil.isShiftKeyDown() ? "tile.iohub.tooltip.shift" : "tile.iohub.tooltip";

            if (LangManager.translateToLocal(k)
                .equals(
                    Integer.valueOf(i)
                        .toString())) {
                break;
            }
            String key = k + "." + i;
            String trans = LangManager.translateToLocal(key);

            p_77624_3_.add(trans);
            i++;

        }
        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
    }

}
