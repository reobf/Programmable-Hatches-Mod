package reobf.proghatches.eucrafting;

import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

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
	}

}
