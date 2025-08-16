package reobf.proghatches.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import gregtech.api.interfaces.ITexture;
import gregtech.api.util.LightingHelper;
import gregtech.common.render.GTCopiedBlockTextureRender;
import gregtech.common.render.GTTextureBase;

public class IIconTexture extends GTTextureBaseLegacy implements ITextureLegacy {
	
	
	static ThreadLocal<Integer> captured=new ThreadLocal();

	public static boolean isNew;
	static{try {
		Class.forName("gregtech.api.render.SBRContextBase");
		isNew=true;
	} catch (ClassNotFoundException e) {
	}}
	public static  HashMap<String,Method> mmap=new HashMap();
	public  static ITexture genFuckingBridge(IIcon aBlock, int rgb){
		IIconTexture wrapped =new IIconTexture(aBlock, rgb);
		InvocationHandler handler=new InvocationHandler(){
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				
				String call=method.getName();
				if(call.equals("isValidTexture"))return wrapped.isValidTexture();
				if(call.equals("isOldTexture"))return wrapped.isOldTexture();
				if(call.equals("startDrawingQuads")){ wrapped.startDrawingQuads((RenderBlocks)args[0], (float)args[1],  (float)args[2],  (float)args[3]);
				return null;}
				if(call.equals("draw")){ wrapped.draw((RenderBlocks)args[0]);
				return null;}
				
				if(call.startsWith("render")){
					Block  b;
					RenderBlocks r;
					int x,y,z;
					if(isNew){
					Class c=args[0].getClass();
					 r=(RenderBlocks) c.getField("renderer").get(args[0]);
					  b=(Block ) c.getField("block").get(args[0]);
					  x=(int ) c.getField("x").get(args[0]);
					  y=(int ) c.getField("y").get(args[0]);
					  z=(int ) c.getField("z").get(args[0]);
					
					}else{
						r=(RenderBlocks) args[0];
						b=(Block) args[1];
						x=(int) args[2];
						y=(int) args[3];
						z=(int) args[4];
					}
					
					
					if(call.equals("renderXNeg"))wrapped.renderXNeg(r, b, x, y, z);
					else if(call.equals("renderYNeg"))wrapped.renderYNeg(r, b, x, y, z);
					else if(call.equals("renderZNeg"))wrapped.renderZNeg(r, b, x, y, z);
					else if(call.equals("renderXPos"))wrapped.renderXPos(r, b, x, y, z);
					else if(call.equals("renderYPos"))wrapped.renderYPos(r, b, x, y, z);
					else if(call.equals("renderZPos"))wrapped.renderZPos(r, b, x, y, z);
					
					
					
					return null;
				}
				
				
				
				System.out.println(method);
				throw new AssertionError("bruh");
			}};
		
		
		
		Object ret=Proxy.newProxyInstance(IIconTexture.class.getClassLoader(), new Class[]{ITexture.class}, handler);
				
		
		
		return (ITexture) ret;
		
		
	}
    private final IIcon mBlock;
    // private final byte mSide, mMeta;
    private int rgb;

    public IIconTexture(IIcon aBlock, int rgb) {
        this.rgb = rgb;
        mBlock = aBlock;

    }

    @Override
    public boolean isOldTexture() {
        return false;
    }

    private IIcon getIcon(int ordinalSide) {

        return mBlock;
    }

    @Override
    public void renderXPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
        final IIcon aIcon = getIcon(ForgeDirection.EAST.ordinal());
        aRenderer.field_152631_f = true;
        startDrawingQuads(aRenderer, 1.0f, 0.0f, 0.0f);
        new LightingHelper(aRenderer).setupLightingXPos(aBlock, aX, aY, aZ)
            .setupColor(ForgeDirection.EAST, rgb);
        aRenderer.renderFaceXPos(aBlock, aX, aY, aZ, aIcon);
        draw(aRenderer);
        aRenderer.field_152631_f = false;
    }

    @Override
    public void renderXNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
        startDrawingQuads(aRenderer, -1.0f, 0.0f, 0.0f);
        final IIcon aIcon = getIcon(ForgeDirection.WEST.ordinal());
        new LightingHelper(aRenderer).setupLightingXNeg(aBlock, aX, aY, aZ)
            .setupColor(ForgeDirection.WEST, rgb);
        aRenderer.renderFaceXNeg(aBlock, aX, aY, aZ, aIcon);
        draw(aRenderer);
    }

    @Override
    public void renderYPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
        startDrawingQuads(aRenderer, 0.0f, 1.0f, 0.0f);
        final IIcon aIcon = getIcon(ForgeDirection.UP.ordinal());
        new LightingHelper(aRenderer).setupLightingYPos(aBlock, aX, aY, aZ)
            .setupColor(ForgeDirection.UP, rgb);
        aRenderer.renderFaceYPos(aBlock, aX, aY, aZ, aIcon);
        draw(aRenderer);
    }

    @Override
    public void renderYNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
        startDrawingQuads(aRenderer, 0.0f, -1.0f, 0.0f);
        final IIcon aIcon = getIcon(ForgeDirection.DOWN.ordinal());
        new LightingHelper(aRenderer).setupLightingYNeg(aBlock, aX, aY, aZ)
            .setupColor(ForgeDirection.DOWN, rgb);
        aRenderer.renderFaceYNeg(aBlock, aX, aY, aZ, aIcon);
        draw(aRenderer);
    }

    @Override
    public void renderZPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
        startDrawingQuads(aRenderer, 0.0f, 0.0f, 1.0f);
        final IIcon aIcon = getIcon(ForgeDirection.SOUTH.ordinal());
        new LightingHelper(aRenderer).setupLightingZPos(aBlock, aX, aY, aZ)
            .setupColor(ForgeDirection.SOUTH, rgb);
        aRenderer.renderFaceZPos(aBlock, aX, aY, aZ, aIcon);
        draw(aRenderer);
    }

    @Override
    public void renderZNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
        startDrawingQuads(aRenderer, 0.0f, 0.0f, -1.0f);
        final IIcon aIcon = getIcon(ForgeDirection.NORTH.ordinal());
        aRenderer.field_152631_f = true;
        new LightingHelper(aRenderer).setupLightingZNeg(aBlock, aX, aY, aZ)
            .setupColor(ForgeDirection.NORTH, rgb);
        aRenderer.renderFaceZNeg(aBlock, aX, aY, aZ, aIcon);
        draw(aRenderer);
        aRenderer.field_152631_f = false;
    }

    @Override
    public boolean isValidTexture() {
        return mBlock != null;
    }

}
