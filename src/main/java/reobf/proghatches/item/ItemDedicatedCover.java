package reobf.proghatches.item;

import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.CommonProxy;

public class ItemDedicatedCover extends Item {

    public  static  IIcon[] icons = new IIcon[64];

    public ItemDedicatedCover() {
        this.setCreativeTab(CommonProxy.tab);
    }
    @Override
    public boolean getHasSubtypes() {
    	// TODO Auto-generated method stub
    	return true;
    }
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register) {
        icons[0] = register.registerIcon("proghatches:cover0");
        icons[1] = register.registerIcon("proghatches:cover1");
        icons[2] = register.registerIcon("proghatches:cover2");
        icons[3] = register.registerIcon("proghatches:cover2");
        icons[32] = register.registerIcon("proghatches:cover32");
        icons[33] = register.registerIcon("proghatches:cover33");
        icons[34] = register.registerIcon("proghatches:cover34");
        icons[35] = register.registerIcon("proghatches:cover35");
        icons[36] = register.registerIcon("proghatches:cover36");
        icons[37] = register.registerIcon("proghatches:cover37");
        
      
    }

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
       
    	IntStream.range(0,Integer.valueOf( StatCollector.translateToLocal("item.proghatch.cover.dedicated.tooltips." + p_77624_1_.getItemDamage()))
    	).forEach(s->
    	p_77624_3_.add(LangManager.translateToLocal("item.proghatch.cover.dedicated.tooltips." + p_77624_1_.getItemDamage()+"."+s)));
       
    	
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
