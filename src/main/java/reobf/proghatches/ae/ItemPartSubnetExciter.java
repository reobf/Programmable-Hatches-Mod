package reobf.proghatches.ae;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import appeng.core.Api;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPartSubnetExciter extends Item implements IPartItem {

    public ItemPartSubnetExciter() {
        this.setMaxStackSize(64);

        AEApi.instance()
            .partHelper()
            .setItemBusRenderer(this);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs p_150895_2_, List list) {

        list.add(new ItemStack(itemIn, 1, 0));

    }

    @Override
    public boolean getHasSubtypes() {

        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {

        return super.getUnlocalizedName(stack) + "." + stack.getItemDamage();
    }

    @Nullable
    @Override
    public PartSubnetExciter createPartFromItemStack(ItemStack is) {
        return new PartSubnetExciter(is);
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
        p_77624_3_.add(StatCollector.translateToLocal("item.exciter.name.tooltip.0"));
        p_77624_3_.add(StatCollector.translateToLocal("item.exciter.name.tooltip.1"));
        p_77624_3_.add(StatCollector.translateToLocal("item.exciter.name.tooltip.2"));
        p_77624_3_.add(StatCollector.translateToLocal("item.exciter.name.tooltip.3"));
        // p_77624_3_.add(StatCollector.translateToLocal("item.amountmaintainer.name.tooltip.1"));
        // p_77624_3_.add(StatCollector.translateToLocal("item.amountmaintainer.name.tooltip.2"));

        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float xOffset, float yOffset, float zOffset) {
        return AEApi.instance()
            .partHelper()
            .placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    /*
     * public ItemPartEUSource register() { //if (!Config.fluidIOBus) return
     * null; GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_EXPORT,
     * FluidCraft.MODID); //setCreativeTab(FluidCraftingTabs.INSTANCE); return
     * this; }
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister _iconRegister) {

        itemIcon = _iconRegister.registerIcon("minecraft:stone");

    }

    @Override
    public IIcon getIconIndex(ItemStack p_77650_1_) {

        return Api.INSTANCE.blocks().blockFluix.block()
            .getIcon(0, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }

}
