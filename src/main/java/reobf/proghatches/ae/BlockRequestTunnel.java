package reobf.proghatches.ae;

import java.util.List;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.block.INameAndTooltips;

public class BlockRequestTunnel extends BlockContainer implements IOrientableBlock,INameAndTooltips{

	public BlockRequestTunnel() {
		
		super(Material.rock);
		setHardness(1);
		setHarvestLevel("pickaxe", 1);
		setBlockName("proghatches.request_tunnel");
		setBlockTextureName("proghatches:request_tunnel");
	}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
	
		return new TileRequestTunnel();
	}
	@Override
	public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
			float subY, float subZ) {
		/*if(worldIn.isRemote==false){
			TileRequestTunnel te=(TileRequestTunnel) worldIn.getTileEntity(x, y, z);
			te.injectCraftedItems(null, AEItemStack.create(new ItemStack(Items.apple)), null);
			
		}*/
		return super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
	}
	@Override
	public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
		// TODO Auto-generated method stub
		super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
		try{
			((TileRequestTunnel)worldIn.getTileEntity(x, y, z)
			).getProxy().setOwner((EntityPlayer) placer);
			}catch(Exception e){e.printStackTrace();}
	}
	@Override
	public boolean usesMetadata() {
		
		return false;
	}
	
	IIcon fr;
	IIcon ba;
	@Override
	public void registerBlockIcons(IIconRegister reg) {
		
		IIcon c=reg.registerIcon("proghatches:tunnel");
		IIcon d=reg.registerIcon("proghatches:tunnel_1");
		IIcon a=reg.registerIcon("proghatches:tunnel_2");
		IIcon b=reg.registerIcon("proghatches:tunnel_3");
		fr=reg.registerIcon("proghatches:tunnel_front");
		ba=reg.registerIcon("proghatches:tunnel_back");
		IIcon x=null;
		tab=new IIcon[][]
				{
			{x,x,a,a,a,a},
			{x,x,c,c,c,c},
			{c,c,x,x,b,d},
			{a,a,x,x,d,b},
			{b,b,d,b,x,x},
			{d,d,b,d,x,x},	
				};
		
		
		
				 this.blockIcon=fr;
	}
	static IIcon[][] tab;//={};
	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileRequestTunnel te=	(TileRequestTunnel)world.getTileEntity(x, y, z);
		if(te.getUp()==ForgeDirection.UNKNOWN){return fr; }
		
		if(te.getUp().getOpposite().ordinal()==side){
			
			return ba;
	    }
		
		
		if(te.getUp().ordinal()==side){
			
			return fr;
	    }
		
		
		return tab[te.getUp().ordinal()][side];
	}
	@Override
	public IOrientable getOrientable(IBlockAccess world, int x, int y, int z) {
		
		return (TileRequestTunnel)world.getTileEntity(x, y, z);
	}

	    protected void customRotateBlock(final IOrientable rotatable, final ForgeDirection axis) {
	        if (rotatable instanceof TileRequestTunnel) {
	            ((TileRequestTunnel) rotatable).setSide(axis);
	        }
	    }
	 @Override
	    public final boolean rotateBlock(final World w, final int x, final int y, final int z, final ForgeDirection axis) {
	        final IOrientable rotatable = this.getOrientable(w, x, y, z);

	        if (rotatable != null && rotatable.canBeRotated()) {
	           
	                this.customRotateBlock(rotatable, axis);
	                return true;
	          
	        }

	        return super.rotateBlock(w, x, y, z, axis);
	    }
	 @Override
		public void addInformation(ItemStack p_77624_1_, List l) {
			
		
		
			l.add(StatCollector.translateToLocal("proghatch.request_tunnel.tooltip.0"));
			l.add(StatCollector.translateToLocal("proghatch.request_tunnel.tooltip.1"));
			l.add(StatCollector.translateToLocal("proghatch.request_tunnel.tooltip.2"));
			l.add(StatCollector.translateToLocal("proghatch.request_tunnel.tooltip.3"));
		}
	@Override
	public String getName(ItemStack p_77624_1_) {
		// TODO Auto-generated method stub
		return null;
	}
}
