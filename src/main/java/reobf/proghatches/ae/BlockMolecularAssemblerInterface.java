package reobf.proghatches.ae;

import java.util.List;

import appeng.tile.networking.TileController;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import reobf.proghatches.block.INameAndTooltips;

public class BlockMolecularAssemblerInterface extends BlockContainer implements INameAndTooltips{

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
	@Override
	public boolean isOpaqueCube() {
		
		return false;
	}

	@Override
	public void addInformation(ItemStack p_77624_1_, List l) {
		l.add(StatCollector.translateToLocal("proghatch.ma_iface.tooltip.0"));
		l.add(StatCollector.translateToLocal("proghatch.ma_iface.tooltip.1"));
		l.add(StatCollector.translateToLocal("proghatch.ma_iface.tooltip.2"));
	}

	@Override
	public String getName(ItemStack p_77624_1_) {
		
		return null;
	}

}
