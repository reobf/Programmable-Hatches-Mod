package reobf.proghatches.eucrafting;

import java.util.List;

import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Textures;
import gregtech.api.enums.Textures.BlockIcons.CustomIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import reobf.proghatches.main.mixin.MixinPlugin;

public class ItemPartEUSource extends Item implements IPartItem {

	/**
	 * 
	 */
	public ItemPartEUSource() {
		this.setMaxStackSize(64);
		// this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_EXPORT);
		AEApi.instance().partHelper().setItemBusRenderer(this);
		setHasSubtypes(true);
	}

	@Nullable
	@Override
	public PartEUSource createPartFromItemStack(ItemStack is) {
		return new PartEUSource(is);
	}
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		int damage=stack.getItemDamage();
		if(damage>=1&&damage<=15){
			return StatCollector.translateToLocalFormatted("item.proghatches.part.eu.source.superconduct.name",GTValues.VN[damage-1]);
		}	
		if(damage>=16&&damage<=30){
			return StatCollector.translateToLocalFormatted("item.proghatches.part.eu.source.normal.name",GTValues.VN[damage-16]);
		}	
		return super.getItemStackDisplayName(stack);
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
		PartEUSource.registerIcons(_iconRegister);
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}
@SideOnly(Side.CLIENT)
@Override
public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
	if(MixinPlugin.noEUMixin){p_77624_3_.add(
			StatCollector.translateToLocal("proghatch.eucrafting.warn")
			);}
	super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
}
}
