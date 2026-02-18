package reobf.proghatches.gt.metatileentity;

import gregtech.api.interfaces.IIconContainer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import reobf.proghatches.block.BlockIOHub;

public class Block2IIconContainer implements IIconContainer{

	public Block2IIconContainer(Block b, int meta) {
		this.b=b;
		this.meta=meta;
	}
	Block b; int meta;
	@Override
	public IIcon getIcon() {
		
		return b.getIcon(0, meta);
	}

	@Override
	public IIcon getOverlayIcon() {
		
		return null;
	}

	@Override
	public ResourceLocation getTextureFile() {
	
		return TextureMap.locationBlocksTexture;
	}

}
