package reobf.proghatches.main;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.glodblock.github.client.gui.GuiDualInterface;
import com.glodblock.github.client.gui.container.ContainerDualInterface;
import com.glodblock.github.common.parts.PartFluidP2PInterface;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.implementations.GuiPriority;
import appeng.client.gui.widgets.ITooltip;
import appeng.container.implementations.ContainerPriority;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotNormal;
import appeng.core.Api;
import appeng.core.features.registries.InterfaceTerminalRegistry;
import appeng.core.features.registries.RegistryContainer;
import appeng.core.localization.GuiText;
import appeng.helpers.BlockingModeIgnoreList;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.items.tools.ToolMemoryCard;
import appeng.parts.AEBasePart;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import codechicken.multipart.MultipartGenerator;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.enums.GT_Values;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.net.GT_Packet_SendCoverData;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.api.util.shutdown.SimpleShutDownReason;
import gregtech.common.blocks.GT_Block_Machines;
import gregtech.common.covers.CoverInfo;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_HeatExchanger;
import reobf.proghatches.Tags;
import reobf.proghatches.eucrafting.BlockEUInterface;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.eucrafting.AECover.IMemoryCardSensitive;
import reobf.proghatches.eucrafting.InterfaceData;
import reobf.proghatches.eucrafting.PartEUP2PInterface;
import reobf.proghatches.eucrafting.TileFluidInterface_EU;

import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProviderPrefabricated;
import reobf.proghatches.item.ItemBookTutorial;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.mixin.mixins.MixinFixPipeCoverBug;
import reobf.proghatches.net.OpenPartGuiMessage;
import reobf.proghatches.net.PriorityMessage;
import reobf.proghatches.net.RenameMessage;
import reobf.proghatches.oc.WirelessPeripheralManager;
import reobf.proghatches.util.ProghatchesUtil;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.7.10]",
dependencies = "required-after:appliedenergistics2;required-after:gregtech;"
,acceptableRemoteVersions="*"
/*
 * ,dependencies= "required-after:neenergistics;"
 */
)
public class MyMod {
	public static MyMod instance;
	{GT_MetaTileEntity_HeatExchanger.class.getDeclaredFields();
	//	BaseMetaPipeEntity.class.getDeclaredFields();
		instance = this;
	}
	public static Deque<Runnable> scheduled=new ArrayDeque<Runnable>();
	//public static ShutDownReason ACCESS_LOOP=new SimpleShutDownReason("proghatch.access_loop", true){public String getID() {return "proghatch.access_loop";};};
	public static SimpleNetworkWrapper net = new SimpleNetworkWrapper(Tags.MODID);
	public static Item progcircuit;
	public static Item toolkit;
	public static final Logger LOG = LogManager.getLogger(Tags.MODID);
	// static {CraftingCPUCluster.class.getDeclaredFields();}
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
	@Nullable
	public static Item euupgrade;

	public static Item eu_token;
	public static Item eu_source_part;
	public static BlockEUInterface block_euinterface;
	public static Item euinterface_p2p;
	public static Item book;
	public static Item fixer;
	//public static Item eu_tool;
	public static Item plunger;
	public static Item lazer_p2p_part;

	@Mod.EventHandler
	// preInit "Run before anything else. Read your config, create blocks,
	// items, etc, and register them with the
	// GameRegistry." (Remove if not needed)
	public void preInit(FMLPreInitializationEvent event) {
		FluidConvertingInventoryAdaptor.class.getFields();
		net.registerMessage(new OpenPartGuiMessage.Handler(), OpenPartGuiMessage.class, 0, Side.CLIENT);
		net.registerMessage(new PriorityMessage.Handler(), PriorityMessage.class, 1, Side.SERVER);
		net.registerMessage(new RenameMessage.Handler(), RenameMessage.class, 2, Side.SERVER);
		proxy.preInit(event);
	}

	@Mod.EventHandler
	
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		AEApi.instance().partHelper().registerNewLayer("reobf.proghatches.fmp.LazerLayer", "reobf.proghatches.eucrafting.ILazer");
		
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void overrideTutorialBookClickBehaviour(PlayerInteractEvent ev) {
		if (Optional.ofNullable(ev.entityPlayer.getHeldItem()).map(ItemStack::getItem)
				.orElse(Items.apple) instanceof ItemBookTutorial) {
			if (Optional.ofNullable(ev.entityPlayer.getHeldItem().stackTagCompound)
					.filter(s -> s.hasKey("proghatchesSpecialTag")).isPresent()) {

				ev.setCanceled(true);
				if (ev.world.isRemote) {
					ev.entityPlayer.displayGUIBook(tutorial(Items.written_book,
							ev.entityPlayer.getHeldItem().getTagCompound().getString("title")));
					// actual contents are localized on client side
				}

			}

		}

	}

	@SubscribeEvent
	public void join(PlayerLoggedInEvent e) {
		//if(Config.fixCircuit)
		//e.player.addChatComponentMessage(new ChatComponentTranslation("proghatch.join.fixCircuit"));
		
		if (e.player.getEntityData().hasKey("ProgrammableHatchesTutorialGet3") == false) {
			e.player.getEntityData().setBoolean("ProgrammableHatchesTutorialGet3", true);

			EntityItem entityitem = e.player.dropPlayerItemWithRandomChoice(
					Optional.of(tutorial("programmable_hatches.eucreafting.tutorial")).map(s -> {
						s.stackTagCompound.setString("proghatchesSpecialTag", "true");
						return s;
					}).get(), false);
			entityitem.delayBeforeCanPickup = 0;
			entityitem.func_145797_a(e.player.getCommandSenderName());
			entityitem = e.player.dropPlayerItemWithRandomChoice(Optional.of(tutorial()).map(s -> {
				s.stackTagCompound.setString("proghatchesSpecialTag", "true");
				return s;
			}).get(), false);
			entityitem.delayBeforeCanPickup = 0;
			entityitem.func_145797_a(e.player.getCommandSenderName());

		}
		;

	}

	@Mod.EventHandler
	
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
		//Api.INSTANCE.registries().p2pTunnel().addNewAttunement(null, null);
		//ShutDownReasonRegistry.register(ACCESS_LOOP);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		for (ItemStack s : new ItemStack[] { new ItemStack(block_euinterface), new ItemStack(euinterface_p2p) })

		{
			Upgrades.CRAFTING.registerItem(s, 1);

			Upgrades.PATTERN_CAPACITY.registerItem(s, 3);

			// Upgrades.CRAFTING.registerItem(s, 1);

			Upgrades.ADVANCED_BLOCKING.registerItem(s, 1);
		}
		InterfaceTerminalRegistry.instance().register(InterfaceData.class);
		InterfaceTerminalRegistry.instance().register(InterfaceData.FluidInterfaceData_TileFluidInterface.class);
		InterfaceTerminalRegistry.instance().register(PartEUP2PInterface.class);
		InterfaceTerminalRegistry.instance().register(PartFluidP2PInterface.class);
		InterfaceTerminalRegistry.instance().register(TileFluidInterface_EU.class);
		InterfaceTerminalRegistry.instance().register(PatternDualInputHatch.Inst.class);
		
	//	ItemList list=new ItemList();
	//	list.add(AEItemStack.create(ItemProgrammingCircuit.wrap(new ItemStack(Blocks.cactus))));
	//	list.findFuzzy(AEItemStack.create(ItemProgrammingCircuit.wrap(new ItemStack(Blocks.bed))), FuzzyMode.IGNORE_ALL);
	}
	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public void tick(final TickEvent.ServerTickEvent event) {
		while(scheduled.isEmpty()==false)
		scheduled.removeLast().run();
	}
	@SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = false)
	public void playerInteract(final PlayerInteractEvent event) {

		a: if (event.action == Action.RIGHT_CLICK_BLOCK && !event.world.isRemote) {
			TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
			if (te == null || !(te instanceof ICoverable))
				break a;
			ICoverable tileEntity = (ICoverable) te;

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				if (tileEntity.getCoverBehaviorAtSideNew(side) instanceof AECover)
					GT_Values.NW.sendToPlayer(
							new GT_Packet_SendCoverData(side, tileEntity.getCoverIDAtSide(side),
									tileEntity.getComplexCoverDataAtSide(side), tileEntity),
							(EntityPlayerMP) event.entityPlayer);

			}

		}

		if (event.action == Action.RIGHT_CLICK_BLOCK && event.entityPlayer.isSneaking()) {
			if (Optional.ofNullable(event.entityPlayer.getHeldItem()).map(s -> s.getItem())
					.filter(s -> s instanceof ToolMemoryCard).isPresent()) {
				IMemoryCardSensitive cv = Optional.ofNullable(event.world.getTileEntity(event.x, event.y, event.z))
						.map(s -> s instanceof ICoverable ? (ICoverable) s : null)
						.map(s -> s.getComplexCoverDataAtSide(ForgeDirection.getOrientation(event.face)))
						.map(s -> s instanceof AECover.IMemoryCardSensitive ? (AECover.IMemoryCardSensitive) s : null)
						.orElse(null);

				if (cv != null) {
					cv.memoryCard(event.entityPlayer);

					event.setCanceled(true);
					return;

				}

			}

		}

	}

	@Mod.EventHandler
	// register server commands in this event handler (Remove if not needed)
	public void serverStarting(FMLServerStartingEvent event) {
		proxy.serverStarting(event);
		WirelessPeripheralManager.stations.clear();
		WirelessPeripheralManager.cards.clear();
		// call.clear();
		// call2.clear();
	}

	public static ItemStack tutorial() {
		return tutorial(book, "programmable_hatches.tutorial");
	}

	public static ItemStack tutorial(Item it) {
		return tutorial(book, "programmable_hatches.tutorial");

	}

	public static ItemStack tutorial(String key) {
		return tutorial(book, key);
	}

	public static ItemStack tutorial(Item it, String key) {

		ArrayList<String> pages = new ArrayList<>();
		int size = Integer.valueOf(LangManager.translateToLocalFormatted(key + ".pages"));
		for (int i = 0; i < size; i++){
			//System.out.println(LangManager.translateToLocalFormatted(key + ".pages." + i));
			pages.add(LangManager.translateToLocalFormatted(key + ".pages." + i).replace("\\n", "\n"));
		}
		ItemStack is = ProghatchesUtil.getWrittenBook(it, "ProgrammableHatchesTutorial", key, "programmable_hatches",
				pages.toArray(new String[0]));
		is.stackTagCompound.setString("proghatchesSpecialTag", "true");
		return is;

	}

	/*
	 * public static WeakHashMap<World,Collection<Runnable>> call=new
	 * WeakHashMap<>(); public static WeakHashMap<Chunk,Collection<Runnable>>
	 * call2=new WeakHashMap<>();
	 * 
	 * @SubscribeEvent public void onWorldUnload(WorldEvent.Unload event) {
	 * if(event.world.isRemote)return;
	 * call.get(event.world).forEach(Runnable::run); call.remove(event.world);
	 * 
	 * }
	 * 
	 * @SubscribeEvent public void onChunkUnload(ChunkEvent.Unload event) {
	 * if(event.world.isRemote)return;
	 * call2.get(event.getChunk()).forEach(Runnable::run);
	 * call2.remove(event.getChunk()); } public static void reg(TileEntity
	 * host,Runnable cb){ call.computeIfAbsent(host.getWorldObj(), s->new
	 * ArrayList<>()); call.get(host).add(cb); call2.computeIfAbsent(host.getc,
	 * s->new ArrayList<>()); call2.get(host).add(cb);
	 * 
	 * 
	 * 
	 * 
	 * }
	 */
	@SubscribeEvent
	public void breakBlock(BlockEvent.BreakEvent b) {
		// System.out.println(b.block);

		if (b.block instanceof GT_Block_Machines) {
			TileEntity te = b.world.getTileEntity(b.x, b.y, b.z);
			if (te instanceof ICoverable) {
				ICoverable c = (ICoverable) te;
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					Optional.ofNullable(c.getComplexCoverDataAtSide(dir)).ifPresent(s -> {
						if (s instanceof AECover.Data) {
							((AECover.Data) s).destroy();

						}

					});

				}
			}

		}

	}

}
