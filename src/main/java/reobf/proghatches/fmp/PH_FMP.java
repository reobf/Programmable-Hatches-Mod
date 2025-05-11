package reobf.proghatches.fmp;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry.IPartConverter;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class PH_FMP implements IPartConverter,IPartFactory {

	@Override
	public TMultiPart createPart(String arg0, boolean arg1) {
		
		return null;
	}

	@Override
	public Iterable<Block> blockTypes() {
		
		return null;
	}

	@Override
	public TMultiPart convert(World arg0, BlockCoord arg1) {
		
		return null;
	}

}
