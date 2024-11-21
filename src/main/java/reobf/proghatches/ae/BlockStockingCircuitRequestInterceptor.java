package reobf.proghatches.ae;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import reobf.proghatches.block.INameAndTooltips;

public class BlockStockingCircuitRequestInterceptor extends BlockContainer implements INameAndTooltips{

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

	@Override
	public void addInformation(ItemStack p_77624_1_, List l) {
		l.add(StatCollector.translateToLocal("proghatch.circuit_interceptor.tooltip.0"));
		l.add(StatCollector.translateToLocal("proghatch.circuit_interceptor.tooltip.1"));
	}

	@Override
	public String getName(ItemStack p_77624_1_) {
		// TODO Auto-generated method stub
		return null;
	}

}
