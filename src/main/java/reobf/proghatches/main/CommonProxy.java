package reobf.proghatches.main;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

import appeng.api.AEApi;
import appeng.block.AEBaseItemBlock;
import appeng.block.crafting.BlockMolecularAssembler;
import appeng.core.AEConfig;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.util.GTModHandler;
import mcp.mobius.waila.api.IWailaRegistrar;
import reobf.proghatches.Tags;
import reobf.proghatches.ae.BlockAutoFillerMKII;
import reobf.proghatches.ae.BlockCraftingCondenser;
import reobf.proghatches.ae.BlockCyclicPatternSubmitter;
import reobf.proghatches.ae.BlockMolecularAssemblerInterface;
import reobf.proghatches.ae.BlockStockingCircuitRequestInterceptor;
import reobf.proghatches.ae.BlockStorageProxy;
import reobf.proghatches.ae.ItemPartAmountMaintainer;
import reobf.proghatches.ae.ItemPartMAP2P;
import reobf.proghatches.ae.ItemPartStockingExportBus;
import reobf.proghatches.ae.ItemPartStorageProxy;
import reobf.proghatches.ae.ItemPartSubnetExciter;
import reobf.proghatches.ae.TileAutoFillerMKII;
import reobf.proghatches.ae.TileCraftingCondenser;
import reobf.proghatches.ae.TileCyclicPatternSubmitter;
import reobf.proghatches.ae.TileMolecularAssemblerInterface;
import reobf.proghatches.ae.TileStockingCircuitRequestInterceptor;
import reobf.proghatches.ae.TileStorageProxy;
import reobf.proghatches.block.BlockAnchorAlert;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.block.BlockReactorSyncer;
import reobf.proghatches.block.ItemBlockAnchorAlert;
import reobf.proghatches.block.ItemBlockIOHub;
import reobf.proghatches.block.ItemBlockReactorSyncer;
import reobf.proghatches.block.ItemBlockTooltip;
import reobf.proghatches.block.TileAnchorAlert;
import reobf.proghatches.block.TileIOHub;
import reobf.proghatches.block.TileReactorSyncer;
import reobf.proghatches.eio.ItemMAConduit;
import reobf.proghatches.eucrafting.BlockEUInterface;
import reobf.proghatches.eucrafting.EUUtil;
import reobf.proghatches.eucrafting.ItemBlockEUInterface;
import reobf.proghatches.eucrafting.ItemEUToken;
import reobf.proghatches.eucrafting.ItemPartEUP2PInterface;
import reobf.proghatches.eucrafting.ItemPartEUSource;
import reobf.proghatches.eucrafting.ItemPartLazerP2P;
import reobf.proghatches.eucrafting.TileFluidInterface_EU;
import reobf.proghatches.item.ItemBookTutorial;
import reobf.proghatches.item.ItemDedicatedCover;
import reobf.proghatches.item.ItemFakePattern;
import reobf.proghatches.item.ItemFixer;
import reobf.proghatches.item.ItemMEPlunger;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.item.ItemProgrammingToolkit;
import reobf.proghatches.item.ItemSmartArm;
import reobf.proghatches.item.ItemUpgrades;
import reobf.proghatches.main.registration.EUCraftingCreativeTab;
import reobf.proghatches.main.registration.PHRecipes;
import reobf.proghatches.main.registration.ProgHatchCreativeTab;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.oc.BlockCardReader;
import reobf.proghatches.oc.ItemAPICard;
import reobf.proghatches.oc.ItemCPU;
import reobf.proghatches.oc.ItemGTRedstoneCard;
import reobf.proghatches.oc.ItemWirelessPeripheralCard;
import reobf.proghatches.oc.TileCardReader;
import reobf.proghatches.oc.TileCoprocessor;
import reobf.proghatches.oc.TileWirelessPeripheralStation;
import thaumcraft.client.renderers.block.BlockCandleRenderer;

public class CommonProxy {

	@SuppressWarnings("deprecation")
	public void preInit(FMLPreInitializationEvent event) {
		Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());

		MyMod.LOG.info(Config.greeting);
		MyMod.LOG.info("I am " +  MyMod.MODNAME + " at version " + Tags.VERSION);

		GameRegistry.registerTileEntity(TileIOHub.class, "proghatches.iohub");
		GameRegistry.registerTileEntity(TileWirelessPeripheralStation.class, "proghatches.peripheral_station");
		GameRegistry.registerTileEntity(TileCoprocessor.class, "proghatches.coprocessor");
		GameRegistry.registerTileEntity(TileAnchorAlert.class, "proghatches.chunk_loading_alert");
		
		GameRegistry.registerTileEntity(TileFluidInterface_EU.class, "proghatches.euinterface");
		GameRegistry.registerTileEntity(TileCyclicPatternSubmitter.class, "proghatches.submitter");
		GameRegistry.registerTileEntity(TileCardReader.class, "proghatches.card_reader");
		GameRegistry.registerTileEntity(TileReactorSyncer.class, "proghatches.reactor_syncer");
		GameRegistry.registerTileEntity(TileStorageProxy.class, "proghatches.proxy");
		GameRegistry.registerTileEntity(TileMolecularAssemblerInterface.class, "proghatches.ma_inface");
		GameRegistry.registerTileEntity(TileStockingCircuitRequestInterceptor.class, "proghatches.circuit_interceptor");
		GameRegistry.registerTileEntity(TileAutoFillerMKII.class, "proghatches.autofillerMKII");
		
		ItemMEPlunger a=new ItemMEPlunger(100000);
		
	
		
		GameRegistry.registerItem(
				MyMod.plunger =a.setUnlocalizedName("proghatch_me_plunger").setTextureName("proghatches:plunger"),
				"proghatch_me_plunger");
		
		
		
		
		GameRegistry.registerItem(
				MyMod.progcircuit = new ItemProgrammingCircuit().setUnlocalizedName("prog_circuit").setTextureName("?"),
				"prog_circuit");
		GameRegistry.registerItem(
				MyMod.fakepattern = new ItemFakePattern().setUnlocalizedName("fake_pattern").setTextureName("?"),
				"fake_pattern");
		GameRegistry.registerItem(
				MyMod.fixer = new ItemFixer().setUnlocalizedName("proghatch_circuit_fixer").setTextureName("ic2:itemToolWrench"),
				"proghatch_circuit_fixer");
		
		
		GameRegistry.registerItem(
				MyMod.toolkit = new ItemProgrammingToolkit().setUnlocalizedName("prog_toolkit").setTextureName("?"),
				"prog_toolkit");
		GameRegistry.registerItem(
				MyMod.smartarm = new ItemSmartArm().setUnlocalizedName("proghatches.smartarm").setTextureName("?"),
				"proghatches.smartarm");
		GameRegistry.registerItem(
				MyMod.cover = new ItemDedicatedCover().setUnlocalizedName("proghatches.cover").setTextureName("?"),
				"proghatches.cover");
		GameRegistry.registerItem(
				MyMod.oc_redstone = new ItemGTRedstoneCard().setMaxStackSize(1)
						.setUnlocalizedName("proghatches.oc.redstone").setTextureName("proghatches:gtredstonecard"),
				"proghatches.oc.redstone");
		GameRegistry.registerItem(MyMod.oc_api = new ItemAPICard().setMaxStackSize(1)
				.setUnlocalizedName("proghatches.oc.api").setTextureName("proghatches:APIcard"), "proghatches.oc.api");
		GameRegistry.registerItem(MyMod.pitem = new ItemWirelessPeripheralCard().setMaxStackSize(1)
				.setUnlocalizedName("proghatches.oc.peripheral_card").setTextureName("proghatches:peripheral_card"),
				"proghatches.oc.peripheral_card");
		GameRegistry.registerItem(MyMod.eu_token = new ItemEUToken().setUnlocalizedName("eu_token").setTextureName("?"),
				"eu_token");

		GameRegistry.registerItem(MyMod.book = new ItemBookTutorial()
				.setUnlocalizedName("writtenBook")
				.setTextureName("book_written").setMaxStackSize(16), "book_tutorial");
		GameRegistry.registerItem(
				MyMod.upgrades = new ItemUpgrades().setUnlocalizedName("prog_upgrades").setTextureName("?"),
				"prog_upgrades");
		
		GameRegistry.registerItem(MyMod.cpu = new ItemCPU().setUnlocalizedName("test_cpu").setTextureName("?"),
				"test_cpu");
		
		
		MyMod.storageproxy = GameRegistry.registerBlock(new BlockStorageProxy(),ItemBlockTooltip.class, "proghatches.proxy",new Object[]{"tile.proghatches.proxy.tooltip"});
		
		MyMod.iohub = GameRegistry.registerBlock(new BlockIOHub(), ItemBlockIOHub.class, "proghatches.iohub");
		MyMod.alert = GameRegistry.registerBlock(new BlockAnchorAlert(Material.rock),ItemBlockAnchorAlert.class, "proghatches.chunk_loading_alert");
		MyMod.submitter = GameRegistry.registerBlock(new BlockCyclicPatternSubmitter(Material.rock), ItemBlockTooltip.class,"proghatches.submitter",new Object[]{"tile.proghatches.submitter.tooltip"});
		
		MyMod.pstation = GameRegistry.registerBlock(new TileWirelessPeripheralStation.Block(),
				TileWirelessPeripheralStation.ItemBlock.class, "proghatches.peripheral_station");
		
		MyMod.reader = GameRegistry.registerBlock(new BlockCardReader(Material.rock),
				 "proghatches.card_reader");
		MyMod.reactorsyncer = GameRegistry.registerBlock(new BlockReactorSyncer(Material.rock),
				ItemBlockReactorSyncer.class
				,
				 "proghatches.reactor_syncer");
		li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.oc_redstone);
		li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.oc_api);
		li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.pitem);
		li.cil.oc.server.driver.Registry.add((li.cil.oc.api.driver.Item) MyMod.cpu);
		
		GTModHandler.addToRecyclerBlackList(new ItemStack(MyMod.progcircuit));
		FMLInterModComms.sendMessage("Waila", "register", "reobf.proghatches.main.CommonProxy.callbackRegister");
		//System.out.println(AEConfig.instance);
		//System.out.println("xxxxxxxxxxxxxxxx");
		GameRegistry.registerBlock(
				MyMod.block_euinterface = new BlockEUInterface(Material.iron, "proghatches.euinterface"),
				ItemBlockEUInterface.class, "proghatches.euinterface");
		GameRegistry.registerItem(MyMod.eu_source_part = new ItemPartEUSource()
				.setUnlocalizedName("proghatches.part.eu.source").setTextureName("?"), "proghatches.part.eu.source");
		GameRegistry.registerItem(MyMod.lazer_p2p_part = new ItemPartLazerP2P()
				.setUnlocalizedName("proghatches.part.lazer.p2p").setTextureName("?"), "proghatches.part.lazer.p2p");
		GameRegistry.registerItem(MyMod.ma_p2p_part = new ItemPartMAP2P()
				.setUnlocalizedName("proghatches.part.ma.p2p").setTextureName("?"), "proghatches.part.ma.p2p");

		GameRegistry
				.registerItem(
						MyMod.euinterface_p2p = new ItemPartEUP2PInterface()
								.setUnlocalizedName("proghatches.euinterface.p2p").setTextureName("?"),
						"proghatches.euinterface.p2p");
		GameRegistry
		.registerItem(
				MyMod.amountmaintainer = new ItemPartAmountMaintainer()
						.setUnlocalizedName("proghatches.amountmaintainer").setTextureName("?"),
				"amountmaintainer");
		GameRegistry
		.registerItem(
				MyMod.partproxy = new ItemPartStorageProxy()
						.setUnlocalizedName("proghatches.storageproxy.part").setTextureName("?"),
				"proghatches.storageproxy.part");
		GameRegistry
		.registerItem(
				MyMod.exciter = new ItemPartSubnetExciter()
						.setUnlocalizedName("proghatches.exciter").setTextureName("?"),
				"proghatches.exciter");
		GameRegistry
		.registerItem(
				MyMod.stockingexport = new ItemPartStockingExportBus()
						.setUnlocalizedName("proghatches.stockingexport").setTextureName("?"),
				"stockingexport");
		
		a();
		
		MyMod.ma_conduit =ItemMAConduit.create();
		GameRegistry
		.registerBlock(
				MyMod.circuit_interceptor = new BlockStockingCircuitRequestInterceptor(),ItemBlockTooltip.class,
						/*.setUnlocalizedName("proghatches.circuit_interceptor").setTextureName("?")*/
				"circuit_interceptor",new Object[]{""});
		GameRegistry
		.registerBlock(
				MyMod.autofiller = new BlockAutoFillerMKII(),ItemBlockTooltip.class,
				"autofillerMKII",new Object[]{""});
		
		
		GameRegistry.registerTileEntity(TileCraftingCondenser.class, "proghatches.craftingdumper");
		GameRegistry.registerBlock(MyMod.ma_iface = new BlockMolecularAssemblerInterface()
				,ItemBlockTooltip.class
				
				, "proghatches.ma_iface",new Object[]{""});
		
	}
 static public class ToolTipAEBaseItemBlock extends AEBaseItemBlock{
				 public ToolTipAEBaseItemBlock(Block id) {
					 super(id);
					bt=(BlockCraftingCondenser) id;
				}  BlockCraftingCondenser bt;
				@SideOnly(Side.CLIENT)
				@Override
				public void addCheckedInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip,
						boolean advancedToolTips) {
					//toolTip.add("proghatches.condenser.tooltip");
					bt.addTips(toolTip);
				}
			 }
	private static void a(){
		for(int i=0;i<=8;i++){
			 
			
		MyMod.condensers[i] = GameRegistry.registerBlock(new BlockCraftingCondenser(i),ToolTipAEBaseItemBlock.class, "proghatches.craftingdumper."+ i);
		}
		
	}
	
	
	
	public static ProgHatchCreativeTab tab;

	public void init(FMLInitializationEvent event) {
		tab = new ProgHatchCreativeTab("proghatches");
		//new EUCraftingCreativeTab("proghatches.eucrafting");
		// AEApi.instance().registries().gridCache().registerGridCache(null,
		// null);;
		// AEApi.instance().registries().interfaceTerminal().register(TileFluidInterface.class);
		new Registration().run();
		EUUtil.register();

	}

	public void postInit(FMLPostInitializationEvent event) {
		// cannot be done in preinit
		

		new PHRecipes().run();

	}

	public static void callbackRegister(IWailaRegistrar registrar) {
		registrar.registerBodyProvider(TileWirelessPeripheralStation.provider, MyMod.pstation.getClass());
		registrar.registerNBTProvider(TileWirelessPeripheralStation.provider, MyMod.pstation.getClass());
		registrar.registerBodyProvider(TileFluidInterface_EU.provider, BlockEUInterface.class);
		registrar.registerNBTProvider(TileFluidInterface_EU.provider, BlockEUInterface.class);
		registrar.registerBodyProvider(TileAnchorAlert.provider, BlockAnchorAlert.class);
		registrar.registerNBTProvider(TileAnchorAlert.provider, BlockAnchorAlert.class);
		registrar.registerBodyProvider(TileCyclicPatternSubmitter.provider, BlockCyclicPatternSubmitter.class);
		registrar.registerNBTProvider(TileCyclicPatternSubmitter.provider, BlockCyclicPatternSubmitter.class);
		
		
		
	}

	// register server commands in this event handler (Remove if not needed)
	public void serverStarting(FMLServerStartingEvent event) {
	}
}
