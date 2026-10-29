package reobf.proghatches.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.lang.LangManager;

public class ItemChanceToolkit extends Item implements IBauble {

    public static final int MODES = 2;

    private final IIcon[] icons = new IIcon[MODES];

    {
        maxStackSize = 1;
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register) {
        icons[0] = register.registerIcon("proghatches:chancekit0");
        icons[1] = register.registerIcon("proghatches:chancekit1");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int damage) {
        return icons[damage >= 0 && damage < MODES ? damage : 0];
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        list.add(LangManager.translateToLocal("item.chance_toolkit.mode." + (stack.getItemDamage() % MODES)));
        for (int i = 0; i < 3; i++) list.add(LangManager.translateToLocal("item.chance_toolkit.tooltip." + i));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isSneaking()) stack.setItemDamage((stack.getItemDamage() + 1) % MODES);
        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.UNIVERSAL;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {}

    @Override
    public void onEquipped(ItemStack stack, EntityLivingBase player) {}

    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {}

    @Override
    public boolean canEquip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack stack, EntityLivingBase player) {
        return true;
    }

    @Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack stack) {
        return stack;
    }
}
