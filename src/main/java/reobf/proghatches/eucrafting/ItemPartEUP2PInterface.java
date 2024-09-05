package reobf.proghatches.eucrafting;

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
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import reobf.proghatches.main.mixin.MixinPlugin;

public class ItemPartEUP2PInterface extends Item implements IPartItem {

	public ItemPartEUP2PInterface() {
		this.setMaxStackSize(64);
		// this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_EXPORT);
		AEApi.instance().partHelper().setItemBusRenderer(this);
	}

	@Nullable
	@Override
	public PartEUP2PInterface createPartFromItemStack(ItemStack is) {
		return new PartEUP2PInterface(is);
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

	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
		if(MixinPlugin.noEUMixin){p_77624_3_.add(
				StatCollector.translateToLocal("proghatch.eucrafting.warn")
				);}
		super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	}
}
