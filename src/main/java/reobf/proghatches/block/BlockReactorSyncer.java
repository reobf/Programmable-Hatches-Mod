package reobf.proghatches.block;

import java.util.Random;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockReactorSyncer extends BlockContainer{

	public BlockReactorSyncer(Material p_i45386_1_) {
		super(p_i45386_1_);
		setHardness(1);
		setHarvestLevel("pickaxe", 1);
		setBlockName("proghatch.reactor_syncer");
	}
@Override
public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {

	return false;//false for all sides,in case the block gets powered and activate the reactor
}
@Override
public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
	switch(side){
	case -1:side=1;break;
	case 0:side=2;break;
	case 1:side=5;break;
	case 2:side=3;break;
	case 3:side=4;break;
	}
	
	
	int meta=world.getBlockMetadata(x, y, z);
	return (side==(meta^1));
}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileReactorSyncer();
	}
	  public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn)
	    {
	        int l = BlockPistonBase.determineOrientation(worldIn, x, y, z, placer);
	        worldIn.setBlockMetadataWithNotify(x, y, z, l, 2);

	       
	    }
	 @Override
	public int isProvidingWeakPower(IBlockAccess worldIn, int x, int y, int z, int side) {
		 if(worldIn.getBlockMetadata(x,  y,  z)==(side)){
			 TileReactorSyncer te=(TileReactorSyncer) worldIn.getTileEntity(x, y, z);
			 return te.power();
		 };
		
		 return 0;
	}
	 @Override
	public void updateTick(World worldIn, int x, int y, int z, Random random) {
			ForgeDirection dir=ForgeDirection.values()[worldIn.getBlockMetadata(x, y, z)].getOpposite();
		 worldIn.notifyBlockOfNeighborChange(x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ, this);
	}
@Override
public boolean getTickRandomly() {
	return false;
}

@SideOnly(Side.CLIENT)
@Override
public IIcon getIcon(int side, int meta) {
	if(side==(meta^1)){
		return GameRegistry.findBlock("IC2", "blockReactorRedstonePort").getIcon(0,0);
	}
	if(side==meta){
		return GameRegistry.findBlock("IC2", "blockGenerator").getIcon(ForgeDirection.UP.ordinal(), 5);
				//Blocks.redstone_block.getIcon(0, 0);
	}
	
	return Blocks.iron_block.getIcon(0, 0);
}

@Override
public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
		float subY, float subZ) {
	
	
	b: {
		if (NetworkUtils.isClient())
			break b;
		UIInfos.TILE_MODULAR_UI.open(player, worldIn, x, y, z);
	}

	return true;
}

}
