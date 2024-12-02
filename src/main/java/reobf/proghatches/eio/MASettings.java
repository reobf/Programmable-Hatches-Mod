package reobf.proghatches.eio;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.api.client.render.IWidgetMap;
import com.enderio.core.client.gui.button.ColorButton;
import com.enderio.core.client.render.ColorUtil;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.DyeColor;

import crazypants.enderio.EnderIO;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.gui.BaseSettingsPanel;
import crazypants.enderio.conduit.gui.GuiExternalConnection;
import crazypants.enderio.conduit.oc.IOCConduit;
import crazypants.enderio.conduit.packet.PacketConnectionMode;
import crazypants.enderio.conduit.packet.PacketOCConduitSignalColor;
import crazypants.enderio.gui.IconEIO;
import crazypants.enderio.network.PacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.net.ConnectionModeMessage;

public class MASettings  extends BaseSettingsPanel {
	
	static public IWidgetIcon icon;
	public static class Holder{
		
		
	
	static public IWidgetIcon getMAIcon(){
	if(icon==null)
		icon =new IWidgetIcon(){

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWidth() {
		
		return 	 16;
	}

	@Override
	public int getHeight() {
		
		return 	 16;
	}

	@Override
	public IWidgetIcon getOverlay() {
		return null;
	}

	@Override
	public IWidgetMap getMap() {
		  
		return new IWidgetMap.WidgetMapImpl(16, 
				
				new ResourceLocation("minecraft", "textures/blocks/crafting_table_top.png"))
				
		
		{		
		
			@Override
	        public void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel,
	                boolean doDraw, boolean flipY) {
			//一个个画能有多大性能损失？ 既然非要共用单个纹理 还假惺惺弄个ResourceLocation参数干啥 真的会谢
			Tessellator tessellator = Tessellator.instance;
		
			if(!doDraw)tessellator.draw();//如果是连续绘制 先把之前的画了
			int old= GL11.glGetInteger(GL11.GL_TEXTURE_2D);
			 RenderUtil.bindTexture(this.getTexture());
			super.render(widget, x, y, width, height, zLevel, true,flipY);
		    GL11.glBindTexture(GL11.GL_TEXTURE_2D, old);
		    if(!doDraw)tessellator.startDrawingQuads();//再重新开始画
		   
		}
			
			
			
				}	
				
				;}};
	return icon;
	}
	
	}
	
	
    public MASettings(GuiExternalConnection gui, IConduit con) {
        super(Holder.getMAIcon()
        		, EnderIO.lang.localize("itemMEConduit.name"), gui, con);
    }
    protected boolean hasInOutModes() {
        return true;
    } 
    static final int ID_INSERT_ENABLED = 327;
    static final int ID_EXTRACT_ENABLED = 328;
   
    
    static Field insertEnabled;
    static Field extractEnabled;
    static{
    	try {
    		insertEnabled=	BaseSettingsPanel.class.getDeclaredField("insertEnabled");
    		extractEnabled=BaseSettingsPanel.class.getDeclaredField("extractEnabled");
    		insertEnabled.setAccessible(true);
    		extractEnabled.setAccessible(true);
    	} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	
    }
    private void updateConnectionMode() {
    	try{  ConnectionMode mode = ConnectionMode.DISABLED;
        if ((boolean)insertEnabled.get(this) && (boolean)extractEnabled.get(this) ) {
            mode = ConnectionMode.IN_OUT;
        } else if ((boolean)insertEnabled.get(this)) {
            mode = ConnectionMode.OUTPUT;
        } else if ((boolean)extractEnabled.get(this)) {
            mode = ConnectionMode.INPUT;
        }
        con.setConnectionMode(gui.getDir(), mode);
        
    	MyMod.net.sendToServer(new ConnectionModeMessage(con, gui.getDir()));
    	}catch(Exception e){e.printStackTrace();}
       // PacketHandler.INSTANCE.sendToServer(new PacketConnectionMode(con, gui.getDir()));
    }

    @Override
    public void actionPerformed(@Nonnull GuiButton guiButton) {
        try{if (guiButton.id == ID_INSERT_ENABLED) {
            insertEnabled .set(this,  !(boolean)insertEnabled.get(this));
            updateConnectionMode();
        } else if (guiButton.id == ID_EXTRACT_ENABLED) {
        	extractEnabled .set(this,  !(boolean)extractEnabled.get(this));
            updateConnectionMode();
        } }catch(Exception e){e.printStackTrace();}
        
        /*else if (guiButton.id == ID_ENABLED) {
            enabled = !enabled;
            updateConnectionMode();
        }*/
    }
   /* @Override
    public void onGuiInit(int leftIn, int topIn, int widthIn, int heightIn) {
      
    	
    	Collection old=null;
		try {
			Field f;
			f = GuiScreen.class.getDeclaredField("buttonList");
			f.setAccessible(true);
			old=new ArrayList<>((Collection)f.get(gui));
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
		
		
		super.onGuiInit(leftIn, topIn, widthIn, heightIn);
    	
		try {//the button is glitchy! kill it
			Field f;
			f = GuiScreen.class.getDeclaredField("buttonList");
			f.setAccessible(true);
			f.set(gui, old);
		} catch (Exception e) {
			e.printStackTrace();
		}
    
    	
    }*/
}
