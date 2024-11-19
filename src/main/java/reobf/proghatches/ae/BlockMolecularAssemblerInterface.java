package reobf.proghatches.ae;

import appeng.tile.networking.TileController;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMolecularAssemblerInterface extends BlockContainer{

	public BlockMolecularAssemblerInterface( ) {
		super(Material.rock);
		setHardness(1);
		setHarvestLevel("pickaxe", 1);
		setBlockName("proghatch.ma_iface");
		setBlockTextureName("proghatches:me_iface");
	}

	@Override
	public TileMolecularAssemblerInterface createNewTileEntity(World worldIn, int meta) {
		
		return new TileMolecularAssemblerInterface();
	}
	

}
