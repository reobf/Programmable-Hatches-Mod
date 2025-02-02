package reobf.proghatches.ae.part2;

import java.util.List;

import javax.annotation.Nullable;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
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
import reobf.proghatches.ae.PartMAP2P;

public class ItemPartRequestTunnel extends Item implements IPartItem{
	@SideOnly(Side.CLIENT)
	private IIcon icon;


	public ItemPartRequestTunnel() {
		this.setMaxStackSize(64);
		
		AEApi.instance().partHelper().setItemBusRenderer(this);
		setHasSubtypes(true);
	}

	@Nullable
	@Override
	public IPart createPartFromItemStack(ItemStack is) {
		return new PartRequestTunnel(is);
	}
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		/*int damage=stack.getItemDamage();
		if(damage>=1&&damage<=15){
			return StatCollector.translateToLocalFormatted("item.proghatches.part.eu.source.superconduct.name",GTValues.VN[damage-1]);
		}	
		if(damage>=16&&damage<=30){
			return StatCollector.translateToLocalFormatted("item.proghatches.part.eu.source.normal.name",GTValues.VN[damage-16]);
		}	*/
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
	public void registerIcons(IIconRegister register) {
		
		   this.icon = register.registerIcon("proghatches:tunnel_back");
	}
	@SideOnly(Side.CLIENT)
	@Override
    public IIcon getIconFromDamage(int dmg) {
        return icon;
    }
	/*
@Override
public IIcon getIconIndex(ItemStack p_77650_1_) {
	
	return PartEUSource.a;
}*/
	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}
@SideOnly(value=Side.CLIENT)
@Override
public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
	//p_77624_3_.add(StatCollector.translateToLocal("item.proghatches.part.ma.p2p.tooltips.0"));
	//p_77624_3_.add(StatCollector.translateToLocal("item.proghatches.part.ma.p2p.tooltips.1"));
	//p_77624_3_.add(StatCollector.translateToLocal("item.proghatches.part.ma.p2p.tooltips.2"));
	//
	super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
}

}
