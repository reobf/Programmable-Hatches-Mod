package reobf.proghatches.main;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import reobf.proghatches.Tags;
import reobf.proghatches.block.TileIOHub.OCApi;
import reobf.proghatches.eucrafting.BlockEUInterface;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.net.OpenPartGuiMessage;
import reobf.proghatches.oc.WirelessPeripheralManager;
import reobf.proghatches.util.ProghatchesUtil;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]"
/*
 * ,dependencies=
 * "required-after:neenergistics;"
 */
)
public class MyMod {
	public static SimpleNetworkWrapper net=new SimpleNetworkWrapper(Tags.MODID);
    public static Item progcircuit;
    public static Item toolkit;
    public static final Logger LOG = LogManager.getLogger(Tags.MODID);
   static {CraftingCPUCluster.class.getDeclaredFields();}
    @SidedProxy(clientSide = "reobf.proghatches.main.ClientProxy", serverSide = "reobf.proghatches.main.CommonProxy")
    public static CommonProxy proxy;
    public static Item fakepattern;
    public static Item smartarm;
    public static Item cover;
    public static Item oc_redstone;
    public static Item oc_api;
	public static Block iohub;
	public static Block pstation;
	public static Item pitem;
	public static Item euupgrade;
	
	public static Item eu_token;
	public static Item eu_source_part;
	public static BlockEUInterface block_euinterface;
    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
    	net.registerMessage(new OpenPartGuiMessage.Handler(), OpenPartGuiMessage.class, 0, Side.SERVER);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

@SubscribeEvent
public void overrideTutorialBookClickBehaviour(PlayerInteractEvent ev){
	 if(Optional.ofNullable(ev.entityPlayer.getHeldItem()).map(ItemStack::getItem).orElse(null)==Items.written_book){
		if( Optional.ofNullable(
		 ev.entityPlayer.getHeldItem().stackTagCompound
		 ).map(s->s.getString("proghatchesSpecialTag"))
		 .isPresent()){
		 
		 ev.setCanceled(true);
	  if(ev.world.isRemote){
		  ev.entityPlayer.displayGUIBook(tutorial());//actual contents are localized on client side
		}  
		  
	  
	}
	   
	 }
	  
}
    @SubscribeEvent
    public void giveBook(PlayerLoggedInEvent e) {
         if(e.player.getEntityData().hasKey("ProgrammableHatchesTutorialGet")==false)
        {
            e.player.getEntityData()
                .setBoolean("ProgrammableHatchesTutorialGet", true);

            e.player.getEntityWorld()
                .spawnEntityInWorld(
                    new EntityItem(e.player.getEntityWorld(), e.player.posX, e.player.posY, e.player.posZ

                        , 
                        Optional.of(tutorial())
                        .map(s->{s.stackTagCompound.setString("proghatchesSpecialTag", "true");  return s;})
                        .get()

                    ));
        } ;

    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
        WirelessPeripheralManager.stations.clear();
        WirelessPeripheralManager.cards.clear();
    }

    public static ItemStack tutorial() {

        ArrayList<String> pages = new ArrayList<>();
        int size = Integer.valueOf(LangManager.translateToLocalFormatted("programmable_hatches.tutorial.pages"));
        for (int i = 0; i < size; i++) pages.add(
            LangManager.translateToLocalFormatted("programmable_hatches.tutorial.pages." + i)
                .replace("\\n", "\n"));

        ItemStack is = ProghatchesUtil.getWrittenBook(
            "ProgrammableHatchesTutorial",
            LangManager.translateToLocal("programmable_hatches.tutorial"),
            "programmable_hatches",
            pages.toArray(new String[0]));

        return is;

    }

}
