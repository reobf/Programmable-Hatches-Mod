package reobf.proghatches.main;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import reobf.proghatches.client.CircuitSpecialRenderer;
import reobf.proghatches.lang.LangManager;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.
    @Override
    public void preInit(FMLPreInitializationEvent event) {

        super.preInit(event);
        MinecraftForgeClient.registerItemRenderer(MyMod.progcircuit, new CircuitSpecialRenderer());
        MinecraftForge.EVENT_BUS.register(this);
       // TextureStitchEvent.Pre
        
    }
    @SubscribeEvent
    public void handle(TextureStitchEvent.Pre e){
    	
    	//System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    	
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
    	/* 
    	 *  ((IReloadableResourceManager) Minecraft.getMinecraft()
    	            .getResourceManager()).registerReloadListener(s->{
    	            	Minecraft.getMinecraft().thePlayer.addChatComponentMessage(
    	            	new ChatComponentTranslation("")
    	            	
    	            	);
    	            	
    	            	
    	            });
    	            */
    	super.postInit(event);
    }
}
