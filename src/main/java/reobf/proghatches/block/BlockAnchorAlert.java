package reobf.proghatches.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

public class BlockAnchorAlert extends BlockContainer{

	public BlockAnchorAlert(Material p_i45386_1_) {
		super(p_i45386_1_);

		setHardness(1);
		setHarvestLevel("pickaxe", 1);
		setBlockName("proghatch.chunk_loading_alert");
	}
@Override
public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
	
	super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
	
	if(placer instanceof EntityPlayer){
		if(!(placer instanceof FakePlayer))
	((TileAnchorAlert)worldIn.getTileEntity(x, y, z)).mark((EntityPlayer) placer);
	}
	
	
}@Override
public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
		float subY, float subZ) {
	if(worldIn.isRemote)return true;
	TileAnchorAlert a=((TileAnchorAlert)worldIn.getTileEntity(x, y, z));
	
	if(player.getUniqueID().equals(a.owner)==false){
		player.addChatComponentMessage(new ChatComponentTranslation("proghatch.chunk_loading_alert.owner"));
	return false;}
	a.mode++;
	if(a.mode>3)a.mode=0;
	a.markDirty();
	player.addChatComponentMessage(new ChatComponentTranslation("proghatch.chunk_loading_alert.mode."+a.mode));

	
	
	
	return true;
}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		
		return new TileAnchorAlert();
	}

@SideOnly(value=Side.CLIENT)
@Override
public IIcon getIcon(int side, int meta) {

	if(side<=1)return  top;
	
	return this.side;
}
@SideOnly(value=Side.CLIENT)
IIcon side;
@SideOnly(value=Side.CLIENT)
IIcon top;
@SideOnly(value=Side.CLIENT)
@Override
public void registerBlockIcons(IIconRegister reg) {
	side=reg.registerIcon("proghatches:speech_box_front");
	top=reg.registerIcon("proghatches:BlockSpatialPylon_dim");
	
	super.registerBlockIcons(reg);
}

}
