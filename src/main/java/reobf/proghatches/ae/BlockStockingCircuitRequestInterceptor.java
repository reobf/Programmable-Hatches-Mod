package reobf.proghatches.ae;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockStockingCircuitRequestInterceptor extends BlockContainer{

	public BlockStockingCircuitRequestInterceptor() {
		
		super(Material.rock);
		setHardness(1);
		setHarvestLevel("pickaxe", 1);
		setBlockName("proghatches.circuit_interceptor");
		setBlockTextureName("proghatches:circuit_interceptor");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
	
		return new TileStockingCircuitRequestInterceptor();
	}

}
