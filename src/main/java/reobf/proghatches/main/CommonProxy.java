package reobf.proghatches.main;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import com.glodblock.github.common.item.FCBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidAutoFiller;
import com.glodblock.github.util.NameConst;
import com.google.common.base.Optional;

import appeng.api.AEApi;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.util.GT_ModHandler;
import mcp.mobius.waila.api.IWailaRegistrar;
import reobf.proghatches.Tags;
import reobf.proghatches.block.BlockIOHub;

import reobf.proghatches.block.ItemBlockIOHub;
import reobf.proghatches.block.TileIOHub;
import reobf.proghatches.eucrafting.BlockEUInterface;
import reobf.proghatches.eucrafting.EUUtil;
import reobf.proghatches.eucrafting.ItemBlockEUInterface;
import reobf.proghatches.eucrafting.ItemEUToken;
import reobf.proghatches.eucrafting.ItemPartEUSource;
import reobf.proghatches.eucrafting.TileFluidInterface_EU;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.item.ItemDedicatedCover;
import reobf.proghatches.item.ItemEUUpgradeModule;
import reobf.proghatches.item.ItemFakePattern;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.item.ItemProgrammingToolkit;
import reobf.proghatches.item.ItemSmartArm;
import reobf.proghatches.main.registration.PHRecipes;
import reobf.proghatches.main.registration.ProgHatchCreativeTab;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.oc.ItemAPICard;
import reobf.proghatches.oc.ItemGTRedstoneCard;
import reobf.proghatches.oc.ItemWirelessPeripheralCard;
import reobf.proghatches.oc.TileCoprocessor;
import reobf.proghatches.oc.TileWirelessPeripheralStation;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    @SuppressWarnings("deprecation")
	public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        MyMod.LOG.info(Config.greeting);
        MyMod.LOG.info("I am " + Tags.MODNAME + " at version " + Tags.VERSION);

        GameRegistry.registerTileEntity(TileIOHub.class, "proghatches.iohub");
        GameRegistry.registerTileEntity(TileWirelessPeripheralStation.class, "proghatches.peripheral_station");
        GameRegistry.registerTileEntity(TileCoprocessor.class, "proghatches.coprocessor");
       
        GameRegistry.registerTileEntity(TileFluidInterface_EU.class, "proghatches.euinterface");
       
        
        // setCreativeTab(FluidCraftingTabs.INSTANCE);
        
        
        GameRegistry.registerItem(
                MyMod.euupgrade = new ItemEUUpgradeModule().setUnlocalizedName("proghatches.eu_upgrade_module")
                    .setTextureName("?"),
                "proghatches.eu_upgrade_module");
        
        GameRegistry.registerItem(
            MyMod.progcircuit = new ItemProgrammingCircuit().setUnlocalizedName("prog_circuit")
                .setTextureName("?"),
            "prog_circuit");
        GameRegistry.registerItem(
            MyMod.fakepattern = new ItemFakePattern().setUnlocalizedName("fake_pattern")
                .setTextureName("?"),
            "fake_pattern");
        GameRegistry.registerItem(
            MyMod.toolkit = new ItemProgrammingToolkit().setUnlocalizedName("prog_toolkit")
                .setTextureName("?"),
            "prog_toolkit");
        GameRegistry.registerItem(
            MyMod.smartarm = new ItemSmartArm().setUnlocalizedName("proghatches.smartarm")
                .setTextureName("?"),
            "proghatches.smartarm");
        GameRegistry.registerItem(
            MyMod.cover = new ItemDedicatedCover().setUnlocalizedName("proghatches.cover")
                .setTextureName("?"),
            "proghatches.cover");
        GameRegistry.registerItem(
            MyMod.oc_redstone = new ItemGTRedstoneCard().setMaxStackSize(1)
                .setUnlocalizedName("proghatches.oc.redstone")
                .setTextureName("proghatches:gtredstonecard"),
            "proghatches.oc.redstone");
        GameRegistry.registerItem(
            MyMod.oc_api = new ItemAPICard().setMaxStackSize(1)
                .setUnlocalizedName("proghatches.oc.api")
                .setTextureName("proghatches:APIcard"),
            "proghatches.oc.api");
        GameRegistry.registerItem(
        		MyMod.pitem = new ItemWirelessPeripheralCard().setMaxStackSize(1)
                .setUnlocalizedName("proghatches.oc.peripheral_card")
                .setTextureName("proghatches:peripheral_card"),
            "proghatches.oc.peripheral_card");
        GameRegistry.registerItem(
                MyMod.eu_token = new ItemEUToken().setUnlocalizedName("eu_token")
                    .setTextureName("?"),
                "eu_token");
       
        
        
        
        
        MyMod.iohub =GameRegistry.registerBlock(
        new BlockIOHub(),ItemBlockIOHub.class,"proghatches.iohub");
        MyMod.pstation =GameRegistry.registerBlock(
                new TileWirelessPeripheralStation.Block(),TileWirelessPeripheralStation.ItemBlock.class,"proghatches.peripheral_station");
        
      
        
        li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.oc_redstone);
        li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.oc_api);
        li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.pitem);
        GT_ModHandler.addToRecyclerBlackList(new ItemStack(MyMod.progcircuit));
        FMLInterModComms.sendMessage("Waila", "register", "reobf.proghatches.main.CommonProxy.callbackRegister");

    }
   
    public static ProgHatchCreativeTab tab;

    public void init(FMLInitializationEvent event) {
        tab = new ProgHatchCreativeTab("proghatches");
       
        //AEApi.instance().registries().gridCache().registerGridCache(null, null);;
       // AEApi.instance().registries().interfaceTerminal().register(TileFluidInterface.class);
        new Registration().run();
        EUUtil.register();
       
    }

      public void postInit(FMLPostInitializationEvent event) {
    	//cannot be done in preinit
    	GameRegistry.registerBlock(
    			 MyMod.block_euinterface =
    			 new BlockEUInterface(Material.iron,"proghatches.euinterface"), ItemBlockEUInterface.class, "proghatches.euinterface");
    	 GameRegistry.registerItem(
                 MyMod.eu_source_part = new ItemPartEUSource().setUnlocalizedName("proghatches.part.eu.source")
                     .setTextureName("?"),
                 "proghatches.part.eu.source");
        new PHRecipes().run();
       
    }
    public static void callbackRegister(IWailaRegistrar registrar) {
    	registrar.registerBodyProvider(TileWirelessPeripheralStation.provider, MyMod.pstation.getClass());
    	registrar.registerNBTProvider(TileWirelessPeripheralStation.provider,  MyMod.pstation.getClass());
    	registrar.registerBodyProvider(TileFluidInterface_EU.provider,BlockEUInterface.class);
    	registrar.registerNBTProvider(TileFluidInterface_EU.provider,BlockEUInterface.class);
    	
    }
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
