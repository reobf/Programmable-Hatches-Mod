package reobf.proghatches.util;

import gregtech.common.render.GTCopiedBlockTextureRender;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class CTexture extends GTCopiedBlockTextureRender{
	public int color;
	public CTexture(IIcon aBlock,int color) {
		super(new Block(Material.air){@Override
		
		public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
		return aBlock;
		}
		@Override
		public IIcon getIcon(int side, int meta) {
				
		return aBlock;
		}
		
		}, 0, 0, new short[4], true);this.color=color;
	}

	
	
	
	
}
