package reobf.proghatches.ae;

import java.util.List;

import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import reobf.proghatches.eucrafting.PartEUP2PInterface;

public class ItemPartAmountMaintainer  extends Item implements IPartItem {

	public ItemPartAmountMaintainer() {
		this.setMaxStackSize(64);
		
		AEApi.instance().partHelper().setItemBusRenderer(this);
	}

	@Nullable
	@Override
	public PartAmountMaintainer createPartFromItemStack(ItemStack is) {
		return new PartAmountMaintainer(is);
	}
@SideOnly(value=Side.CLIENT)
@Override
public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
	p_77624_3_.add(StatCollector.translateToLocal("item.amountmaintainer.name.tooltip.0"));
	p_77624_3_.add(StatCollector.translateToLocal("item.amountmaintainer.name.tooltip.1"));
	p_77624_3_.add(StatCollector.translateToLocal("item.amountmaintainer.name.tooltip.2"));
	
	super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
}
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
			float xOffset, float yOffset, float zOffset) {
		return AEApi.instance().partHelper().placeBus(player.getHeldItem(), x, y, z, side, player, world);
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
		// PartEUSource.registerIcons(_iconRegister);
		itemIcon=_iconRegister.registerIcon("proghatches:amountmaintainer");
	}
@Override
public IIcon getIconIndex(ItemStack p_77650_1_) {
	// TODO Auto-generated method stub
	return super.getIconIndex(p_77650_1_);
}
	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}

}

