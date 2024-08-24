package reobf.proghatches.ae;



import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
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
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import net.minecraftforge.event.ForgeEventFactory;

public class BlockCyclicPatternSubmitter extends BlockContainer{

	public BlockCyclicPatternSubmitter(Material p_i45386_1_) {
		super(p_i45386_1_);

		setHardness(1);
		setHarvestLevel("pickaxe", 1);
		setBlockName("proghatch.submitter");
	}
@Override
public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
	
	super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
	
	if(placer instanceof EntityPlayer){
		if(!(placer instanceof FakePlayer))
	((TileCyclicPatternSubmitter)worldIn.getTileEntity(x, y, z)).mark((EntityPlayer) placer);
	}
	  int l = determineOrientation(worldIn, x, y, z, placer);
      worldIn.setBlockMetadataWithNotify(x, y, z, l, 2);
	
}
public static int determineOrientation(World p_150071_0_, int p_150071_1_, int p_150071_2_, int p_150071_3_, EntityLivingBase p_150071_4_)
{
    if (MathHelper.abs((float)p_150071_4_.posX - (float)p_150071_1_) < 2.0F && MathHelper.abs((float)p_150071_4_.posZ - (float)p_150071_3_) < 2.0F)
    {
        double d0 = p_150071_4_.posY + 1.82D - (double)p_150071_4_.yOffset;

        if (d0 - (double)p_150071_2_ > 2.0D)
        {
            return 1;
        }

        if ((double)p_150071_2_ - d0 > 0.0D)
        {
            return 0;
        }
    }

    int l = MathHelper.floor_double((double)(p_150071_4_.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
    return l == 0 ? 2 : (l == 1 ? 5 : (l == 2 ? 3 : (l == 3 ? 4 : 0)));
}
@Override
public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
		float subY, float subZ) {
	/*if(worldIn.isRemote)return true;
	TileCyclicPatternSubmitter a=((TileCyclicPatternSubmitter)worldIn.getTileEntity(x, y, z));
	a.inv[0]=player.getCurrentEquippedItem();*/
	/*if(player.getUniqueID().equals(a.owner)==false){
		player.addChatComponentMessage(new ChatComponentTranslation("proghatch.chunk_loading_alert.owner"));
	return false;}
	a.mode++;
	if(a.mode>3)a.mode=0;
	a.markDirty();
	player.addChatComponentMessage(new ChatComponentTranslation("proghatch.chunk_loading_alert.mode."+a.mode));

	
	*/
	TileEntity te = worldIn.getTileEntity(x, y, z);
	/*
	 * boolean suc= ((TileIOHub) te) .onRightclick(player,
	 * ForgeDirection.getOrientation(side), subX, subY, subZ); if(suc)return
	 * suc;
	 */
	final ItemStack is = player.inventory.getCurrentItem();
	/*if (is != null && is.getItem() instanceof ToolQuartzCuttingKnife) {
		if (ForgeEventFactory.onItemUseStart(player, is, 1) <= 0)
			return false;

		// System.out.println(te);
		player.openGui(AppEng.instance(), GuiBridge.GUI_RENAMER.ordinal() << 5 | (side), te.getWorldObj(),
				te.xCoord, te.yCoord, te.zCoord);
		return true;
	}*/
	b: {
		if (NetworkUtils.isClient())
			break b;
		UIInfos.TILE_MODULAR_UI.open(player, worldIn, x, y, z);
	}

	return true;
}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		
		return new TileCyclicPatternSubmitter();
	}

@SideOnly(value=Side.CLIENT)
@Override
public IIcon getIcon(int side, int meta) {

	if(side==4)return  top;
	return this.side;
}
@SideOnly(Side.CLIENT)
public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side)
{

	
	if(side==(worldIn.getBlockMetadata(x, y, z)&0b111)){return  
			
			
			((worldIn.getBlockMetadata(x, y, z)&0b1000)==0)?
			top_off:top;
	
	}



return this.side;
}
@SideOnly(value=Side.CLIENT)
IIcon side;
@SideOnly(value=Side.CLIENT)
IIcon top;
@SideOnly(value=Side.CLIENT)
IIcon top_off;
@SideOnly(value=Side.CLIENT)
@Override
public void registerBlockIcons(IIconRegister reg) {
	side=reg.registerIcon("ae2fc:level_maintainer");
	top=reg.registerIcon("proghatches:submitter");
	top_off=reg.registerIcon("proghatches:submitter_off");
	super.registerBlockIcons(reg);
}
/* (non-Javadoc)
 * @see net.minecraft.block.Block#rotateBlock(net.minecraft.world.World, int, int, int, net.minecraftforge.common.util.ForgeDirection)
 */
@Override
public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis) {
	int old=worldObj.getBlockMetadata(x, y, z);
	
	
	worldObj.setBlockMetadataWithNotify(x,y,z,
	 ForgeDirection.values()[old&0b111].getRotation(axis).ordinal()|(old&0b1000)
	 ,
	 3
	 )
	 ;
return true;

}  

}
