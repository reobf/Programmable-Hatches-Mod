package reobf.proghatches.main;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.util.GT_ModHandler;
import reobf.proghatches.Tags;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.block.ItemBlockIOHub;
import reobf.proghatches.block.TileIOHub;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.item.ItemDedicatedCover;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.item.ItemProgrammingToolkit;
import reobf.proghatches.item.ItemSmartArm;
import reobf.proghatches.main.registration.PHRecipes;
import reobf.proghatches.main.registration.ProgHatchCreativeTab;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.oc.ItemAPICard;
import reobf.proghatches.oc.ItemGTRedstoneCard;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    @SuppressWarnings("deprecation")
	public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

        MyMod.LOG.info(Config.greeting);
        MyMod.LOG.info("I am " + Tags.MODNAME + " at version " + Tags.VERSION);

        GameRegistry.registerTileEntity(TileIOHub.class, "proghatches.iohub");
        
        GameRegistry.registerItem(
            MyMod.progcircuit = new ItemProgrammingCircuit().setUnlocalizedName("prog_circuit")
                .setTextureName("?"),
            "prog_circuit");
        GameRegistry.registerItem(
            MyMod.fakepattern = new ProgrammingCircuitProvider.FakePattern().setUnlocalizedName("fake_pattern")
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

        MyMod.iohub =GameRegistry.registerBlock(
        new BlockIOHub(),ItemBlockIOHub.class,"proghatches.iohub");
        
        
        li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.oc_redstone);
        li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.oc_api);
      //  li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Block)MyMod.iohub);
        GT_ModHandler.addToRecyclerBlackList(new ItemStack(MyMod.progcircuit));
    }
   
    public static ProgHatchCreativeTab tab;

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        tab = new ProgHatchCreativeTab("proghatches");
        new Registration().run();

    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {

        new PHRecipes().run();

    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
