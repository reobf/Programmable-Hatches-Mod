package reobf.proghatches.eucrafting;

import java.util.EnumSet;

import appeng.api.util.IOrientable;
import appeng.client.render.BlockRenderInfo;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TileInterface;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class DummySuper extends Block{


	public DummySuper(Material mat) {
		super(mat);
	}

	protected boolean hasCustomRotation() {
		// TODO Auto-generated method stub
		return false;
	}
    protected BlockRenderInfo getRendererInstance() {
			// TODO Auto-generated method stub
			return null;
		}

		protected TileInterface getTileEntity(IBlockAccess world, int x, int y, int z) {
			// TODO Auto-generated method stub
			return null;
		}
	protected void customRotateBlock(IOrientable rotatable, ForgeDirection axis) {
		// TODO Auto-generated method stub
		
	}

	public void registerBlockIcons(IIconRegister i) {
		// TODO Auto-generated method stub
		
	}

	public void setFeature(EnumSet<AEFeature> f) {
		// TODO Auto-generated method stub
		
	}

	public void setTileEntity(Class<? extends TileEntity> clazz) {
		// TODO Auto-generated method stub
		
	}

	public Block setBlockName(String name) {
		return null;
		// TODO Auto-generated method stub
		
	}

}
