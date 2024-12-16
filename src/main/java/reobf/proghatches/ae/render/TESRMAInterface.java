package reobf.proghatches.ae.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockBeacon;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import reobf.proghatches.ae.TileMolecularAssemblerInterface;
import reobf.proghatches.main.MyMod;
import team.chisel.config.Configurations;

public class TESRMAInterface extends TileEntitySpecialRenderer{
	RenderBlocks r=new RenderBlocks();
	//ResourceLocation rl=new ResourceLocation("proghatches","textures/blocks/me_iface.png");
	@Override
	public void renderTileEntityAt(
			TileEntity tile, double x, double y, double z, float timeSinceLastTick) {
	//boolean b=	GL11.glGetBoolean(GL11.GL_CULL_FACE);
	
		GL11.glDisable(GL11.GL_CULL_FACE);
	
		
		
		
		  long l=System.currentTimeMillis();
		 double rx=(l%2000)/2000d*3.14159*2;
	       double b1 =Math.cos(rx)+1;
	       double b2 =Math.cos(rx+3.14159*0.666)+1;
	       double b3 =Math.cos(rx-3.14159*0.666)+1;
		
		GL11.glColor3d((b3+2)/3, (b2+2)/3, (b1+2)/3);
		TileMolecularAssemblerInterface ifc=(TileMolecularAssemblerInterface) tile;
		
				 Tessellator tessellator = Tessellator.instance;
				// tessellator.startDrawingQuads();
				 tessellator.setColorOpaque_I(0Xffffff);
			        tessellator.setBrightness(0xF000F0);
		 		GL11.glEnable(GL11.GL_TEXTURE_2D);
		 		//bindTexture(rl);
		 		bindTexture(TextureMap.locationBlocksTexture);
		        r.setOverrideBlockTexture(MyMod.ma_iface.getIcon(0, 0));
		        r.enableAO=false;
		       
		        
		        
		        r.setRenderBounds(0,0,0, 1.0D, 1.0D, 1.0D);
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, -1.0F, 0.0F);
		        r.renderFaceYNeg(null, x,y+3/16f,z, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, 1.0F, 0.0F);
		        r.renderFaceYPos(null, x,y-3/16f,z, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, 0.0F, -1.0F);
		        r.renderFaceZNeg(null, x,y,z+3/16f, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, 0.0F, 1.0F);
		        r.renderFaceZPos(null, x,y,z-3/16f, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		        r.renderFaceXNeg(null, x+3/16f,y,z, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(1.0F, 0.0F, 0.0F);
		        r.renderFaceXPos(null, x-3/16f,y,z, null);
		        tessellator.draw();
		        
		        
		        r.setRenderBounds(0,0,0, 1.0D, 1.0D, 1.0D);
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, -1.0F, 0.0F);
		        r.renderFaceYNeg(null, x,y+0.99,z, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, 1.0F, 0.0F);
		        r.renderFaceYPos(null, x,y-0.99,z, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, 0.0F, -1.0F);
		        r.renderFaceZNeg(null, x,y,z+0.99, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(0.0F, 0.0F, 1.0F);
		        r.renderFaceZPos(null, x,y,z-0.99, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		        r.renderFaceXNeg(null, x+0.99,y,z, null);
		        tessellator.draw();
		        tessellator.startDrawingQuads();
		        tessellator.setNormal(1.0F, 0.0F, 0.0F);
		        r.renderFaceXPos(null, x-0.99,y,z, null);
		        tessellator.draw();
		        
		        
		        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		        GL11.glDisable(GL11.GL_TEXTURE_2D);
		       	GL11.glDisable(GL11.GL_LIGHTING);
		    	
		       	if(ifc.yfx>0.01){
		       	ifc.yfx=ifc.yfx*0.99f; 	
		       	GL11.glDisable(GL11.GL_CULL_FACE);
		        GL11.glBegin(GL11.GL_QUADS); 
		      
		       
		        GL11.glColor3d(b1/2,b2/2, b3/2);
		        GL11.glVertex3d(x+0.5+ifc.yfx, y, z+0.5);
		        GL11.glVertex3d(x+0.5+ifc.yfx, y+1, z+0.5);
		        GL11.glVertex3d(x+0.5-ifc.yfx, y+1, z+0.5);
		        GL11.glVertex3d(x+0.5-ifc.yfx, y, z+0.5);
		        GL11.glVertex3d(x+0.5, y, z+0.5+ifc.yfx);
		        GL11.glVertex3d(x+0.5, y+1, z+0.5+ifc.yfx);
		        GL11.glVertex3d(x+0.5, y+1, z+0.5-ifc.yfx);
		        GL11.glVertex3d(x+0.5, y, z+0.5-ifc.yfx);
		    	GL11.glEnd();
		    	}
		    	if(ifc.xfx>0.01){
			       	ifc.xfx=ifc.xfx*0.99f;
			        GL11.glDisable(GL11.GL_CULL_FACE);
			        GL11.glBegin(GL11.GL_QUADS);
			      
			      
			        GL11.glColor3d(b1/2,b2/2, b3/2);
			        GL11.glVertex3d(x,y+0.5+ifc.xfx,  z+0.5);
			        GL11.glVertex3d( x+1,y+0.5+ifc.xfx, z+0.5);
			        GL11.glVertex3d(x+1,y+0.5-ifc.xfx,  z+0.5);
			        GL11.glVertex3d(x,y+0.5-ifc.xfx,  z+0.5);
			        GL11.glVertex3d( x,y+0.5, z+0.5+ifc.xfx);
			        GL11.glVertex3d( x+1,y+0.5, z+0.5+ifc.xfx);
			        GL11.glVertex3d( x+1,y+0.5, z+0.5-ifc.xfx);
			        GL11.glVertex3d(x,y+0.5,  z+0.5-ifc.xfx);
			    	GL11.glEnd();
			    	}
		    	if(ifc.zfx>0.01){
			       	ifc.zfx=ifc.zfx*0.99f;
			        GL11.glDisable(GL11.GL_CULL_FACE);
			        GL11.glBegin(GL11.GL_QUADS);
			     
			      
			        GL11.glColor3d(b1/2,b2/2, b3/2);
			        GL11.glVertex3d(x+0.5+ifc.zfx,  y+0.5,z);
			        GL11.glVertex3d( x+0.5+ifc.zfx, y+0.5,z+1);
			        GL11.glVertex3d(x+0.5-ifc.zfx,  y+0.5,z+1);
			        GL11.glVertex3d(x+0.5-ifc.zfx,  y+0.5,z);
			        GL11.glVertex3d( x+0.5, y+0.5+ifc.zfx,z);
			        GL11.glVertex3d(x+0.5, y+0.5+ifc.zfx,z+1);
			        GL11.glVertex3d( x+0.5, y+0.5-ifc.zfx,z+1);
			        GL11.glVertex3d(x+0.5,  y+0.5-ifc.zfx,z);
			    	GL11.glEnd();
			    	}

		        
		        
		        
		        
		    	GL11.glPopAttrib();
		    	//GL11.glEnable(GL11.GL_LIGHTING);
		    	GL11.glEnable(GL11.GL_TEXTURE_2D);
		    	
		    	
		    	
		    	
		        GL11.glColor3f(1, 1, 1);
		        
		        
		        //tessellator.draw();
		       /* 
		        r.renderStandardBlock(p_147797_1_, p_147797_2_, p_147797_3_, p_147797_4_);
		        r.renderAllFaces = true;
		        r.setOverrideBlockTexture(this.getBlockIcon(Blocks.obsidian));
		        r.setRenderBounds(0.125D, 0.0062500000931322575D, 0.125D, 0.875D, (double)f, 0.875D);
		        r.renderStandardBlock(p_147797_1_, p_147797_2_, p_147797_3_, p_147797_4_);
		        r.setOverrideBlockTexture(this.getBlockIcon(Blocks.beacon));
		        r.setRenderBounds(0.1875D, (double)f, 0.1875D, 0.8125D, 0.875D, 0.8125D);
		        r.renderStandardBlock(p_147797_1_, p_147797_2_, p_147797_3_, p_147797_4_);
		        r.renderAllFaces = false;*/
		        
		        
		        r.clearOverrideBlockTexture();
		     
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		
	}

}
