package reobf.proghatches.ae;

import java.util.List;

import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import appeng.core.Api;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;


public class ItemPartCtrlInvertedTerminal extends Item implements IPartItem {
	 @SideOnly(value = Side.CLIENT)
	    @Override
	    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
	        p_77624_3_.add(StatCollector.translateToLocal("item.ctrlinvertedterminal.name.tooltip.0"));
	        p_77624_3_.add(StatCollector.translateToLocal("item.ctrlinvertedterminal.name.tooltip.1"));


	        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	    }
    public ItemPartCtrlInvertedTerminal() {
        this.setMaxStackSize(64);

        AEApi.instance()
            .partHelper()
            .setItemBusRenderer(this);
    }

    @Nullable
    @Override
    public PartCtrlInvertedTerminal createPartFromItemStack(ItemStack is) {
        return new PartCtrlInvertedTerminal(is);
    }



    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float xOffset, float yOffset, float zOffset) {
        return AEApi.instance()
            .partHelper()
            .placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

  
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister _iconRegister) {
    	 itemIcon = _iconRegister.registerIcon("proghatches:amountmaintainer");
    }

    @Override
    public IIcon getIconIndex(ItemStack p_77650_1_) {
     
        return super.getIconIndex(p_77650_1_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }

}
