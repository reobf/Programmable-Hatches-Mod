package reobf.proghatches.eucrafting;

import com.glodblock.github.common.block.FCBaseBlock;

import appeng.block.AEBaseItemBlock;
import net.minecraft.block.Block;

public class ItemBlockEUInterface  extends AEBaseItemBlock{
	 private final Block blockType;

	    public ItemBlockEUInterface(Block id) {
	        super(id);
	        blockType = (Block) id;
	    }
}
