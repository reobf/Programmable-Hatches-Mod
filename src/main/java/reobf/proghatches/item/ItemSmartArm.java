package reobf.proghatches.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.GT_Values;
import reobf.proghatches.gt.cover.SmartArmCover;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;

public class ItemSmartArm extends Item {

    @Override
    public boolean requiresMultipleRenderPasses() {

        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses(int metadata) {
        return 2;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamageForRenderPass(int p_77618_1_, int p_77618_2_) {
        if (p_77618_2_ == 1) return overlay;

        return super.getIconFromDamageForRenderPass(p_77618_1_, p_77618_2_);
    }

    IIcon[] gt = new IIcon[16];
    IIcon overlay;// =new IIcon[16];

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register) {
        overlay = register.registerIcon("proghatches:spark");
        for (int i = 0; i < 14; i++) {
            int ii = (650 + i);
            if (ii >= 660) ii = ii - (660 - 33);
            gt[i + 1] = register.registerIcon("gregtech:gt.metaitem.01/" + ii);
        }
        gt[0] = register.registerIcon("gregtech:gt.metaitem.01/654");

        super.registerIcons(register);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int i) {

        return gt[i];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getItemStackDisplayName(ItemStack p_77653_1_) {

        return LangManager.translateToLocal("item.proghatch.smartarm.name") + " ("
            + GT_Values.VN[p_77653_1_.getItemDamage()]
            + ")";

    }

    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack p_77636_1_) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
        for (int i = 0; i < 15; i++) {
            p_150895_3_.add(new ItemStack(MyMod.smartarm, 1, i));
        }
    }

    @Override
    public boolean getHasSubtypes() {

        return true;
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {

        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);

        int a = SmartArmCover.tier[p_77624_1_.getItemDamage()][0];
        String sec = a >= 20 ? ("" + (a / 20)) : ("1/" + 20 / a);
        String am = SmartArmCover.tier[p_77624_1_.getItemDamage()][1] + "";
        int size = Integer.valueOf(LangManager.translateToLocalFormatted("item.proghatch.smartarm.name.tooltip"));
        for (int i = 0; i < size; i++) p_77624_3_
            .add(LangManager.translateToLocalFormatted("item.proghatch.smartarm.name.tooltip." + i, sec, am));

    }

    @Override
    public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_,
        int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        return super.onItemUse(
            p_77648_1_,
            p_77648_2_,
            p_77648_3_,
            p_77648_4_,
            p_77648_5_,
            p_77648_6_,
            p_77648_7_,
            p_77648_8_,
            p_77648_9_,
            p_77648_10_);
    }
@SideOnly(Side.CLIENT)
    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {

        if (worldIn.isRemote == true && player.isSneaking()) {
            int size = Integer
                .valueOf(LangManager.translateToLocalFormatted("programmable_hatches.cover.smart.desc"));
            for (int i = 0; i < size; i++) player.addChatMessage(
                new ChatComponentText(LangManager.translateToLocal("programmable_hatches.cover.smart.desc." + i)));
        }
        // player.addChatMessage(new
        // ChatComponentText(LangManager.translateToLocal("programmable_hatches.cover.smart.desc")));

        return super.onItemRightClick(itemStackIn, worldIn, player);
    }

}
