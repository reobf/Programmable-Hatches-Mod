package reobf.proghatches.util;

import java.lang.reflect.Constructor;

import com.google.common.base.Function;

import gregtech.common.render.GTCopiedBlockTextureRender;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class CTexture /*extends GTCopiedBlockTextureRender*/{
	public interface Color{public void set(int x);}
	static Function<Block,GTCopiedBlockTextureRender > k;
	public static GTCopiedBlockTextureRender CTexture(IIcon aBlock,int color){
		GTCopiedBlockTextureRender x=null;
		Block fake=new Block(Material.air){@Override
			
			public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
			return aBlock;
			}
			@Override
			public IIcon getIcon(int side, int meta) {
					
			return aBlock;
			}
			
			};
	
		if(k==null)
		try {
			Constructor<GTCopiedBlockTextureRender> ctr = GTCopiedBlockTextureRender.class.getDeclaredConstructor(Block .class, int .class, int .class, short[] .class, boolean .class)
			;ctr.setAccessible(true);
			k=a->{try {
				return ctr.newInstance(a,0,0, new short[4], true);
			} catch (Exception e) {
				throw new AssertionError(e);
			}};
		} catch (Exception e) {
		}
		if(k==null)
			try {
				Constructor<GTCopiedBlockTextureRender> ctr = GTCopiedBlockTextureRender.class.getDeclaredConstructor(Block .class, int .class, int .class, short[] .class)
				;ctr.setAccessible(true);
				k=a->{try {
					return ctr.newInstance(a,0,0, new short[4]);
				} catch (Exception e) {
					throw new AssertionError(e);
				}};
			} catch (Exception e) {
			}
		x=k.apply(fake);
		((Color)x).set(color);
		return x;
	}
	
	/*public int color;
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
	}*/

	
	
	
	
}
