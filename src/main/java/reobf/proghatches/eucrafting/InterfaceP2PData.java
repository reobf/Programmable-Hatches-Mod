package reobf.proghatches.eucrafting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import com.glodblock.github.common.parts.PartFluidP2PInterface;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.exceptions.FailedConnection;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.SelectedPart;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IInterfaceViewable;
import appeng.helpers.DualityInterface;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.GridConnection;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.gui.modularui.GT_CoverUIBuildContext;
import gregtech.api.interfaces.tileentity.ICoverable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import reobf.proghatches.eucrafting.AECover.Data;

import reobf.proghatches.main.MyMod;

public class InterfaceP2PData implements AECover.IMemoryCardSensitive, Data, IInterfaceHost, IGridTickable,
		IUpgradeableHost, ICustomNameObject, IConfigurableObject, IPriorityHost , IActualSideProvider{
	public void setTag(NBTTagCompound tagCompound) {
		tag = tagCompound;
	}
	public IInterfaceHost getInterfaceOrNull(){return duality;};
	public NBTTagCompound getTag() {
		return tag;
	}

	NBTTagCompound tag;

	public boolean memoryCard(EntityPlayer entityPlayer) {
		entityPlayer.addChatComponentMessage(new ChatComponentTranslation("programmable_hatches.cover.ae.memorycard"));

		return false;
	};

	public InterfaceP2PData() {
	}

	private TileEntity faketile = new TileEntity();

	public boolean supportFluid() {
		return true;
	}

	IPartHost fakehost = new Host();

	public class Host implements IPartHost, IActualSideProvider, InterfaceData.DisabledInventory {

		@Override
		public SelectedPart selectPart(Vec3 pos) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void removePart(ForgeDirection side, boolean suppressUpdate) {
			// TODO Auto-generated method stub

		}

		@Override
		public void partChanged() {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyNeighbors() {
			// TODO Auto-generated method stub

		}

		@Override
		public void markForUpdate() {
			// TODO Auto-generated method stub

		}

		@Override
		public void markForSave() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isInWorld() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isBlocked(ForgeDirection side) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean hasRedstone(ForgeDirection side) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public TileEntity getTile() {
			// TODO Auto-generated method stub
			return InterfaceP2PData.this.getTileEntity();
		}

		@Override
		public IPart getPart(ForgeDirection side) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DimensionalCoord getLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<LayerFlags> getLayerFlags() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IFacadeContainer getFacadeContainer() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AEColor getColor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void clearContainer() {
			// TODO Auto-generated method stub

		}

		@Override
		public void cleanup() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean canAddPart(ItemStack part, ForgeDirection side) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ForgeDirection addPart(ItemStack is, ForgeDirection side, EntityPlayer owner) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ForgeDirection getActualSide() {
			// TODO Auto-generated method stub
			return side;
		}

	};;

	AENetworkProxy gridProxy;
	ForgeDirection side = ForgeDirection.UNKNOWN;
	DimensionalCoord pos = new DimensionalCoord(0, 0, 0, 0);

	public AENetworkProxy getGridProxy() {
		return gridProxy;
	}

	public void setGridProxy(AENetworkProxy gridProxy) {
		this.gridProxy = gridProxy;
	}

	public ForgeDirection getSide() {
		return side;
	}

	public void setSide(ForgeDirection side) {
		this.side = side;
	}

	public DimensionalCoord getPos() {
		return pos;
	}

	public void setPos(DimensionalCoord pos) {
		this.pos = pos;
	}

	public void onReady() {
		duality.addToWorld();
	};

	PartFluidP2PInterface duality = new PartFluidP2PInterface(new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE_P2P));

	{
		duality.setPartHostInfo(ForgeDirection.UNKNOWN, fakehost, getTileEntity());
	}

	/*
	 * { public TileEntity getTileEntity() {return
	 * InterfaceP2PData.this.getTileEntity();}; public TileEntity getTile() {
	 * return this.getTileEntity();}; public IPartHost getHost() { return
	 * fakehost;};
	 * 
	 * };
	 */
	@MENetworkEventSubscribe
	public void stateChange(final MENetworkChannelsChanged c) {
		this.duality.getInterfaceDuality().notifyNeighbors();
	}

	@Override
	public int getInstalledUpgrades(final Upgrades u) {
		return this.duality.getInstalledUpgrades(u);
	}

	@MENetworkEventSubscribe
	public void stateChange(final MENetworkPowerStatusChange c) {
		this.duality.getInterfaceDuality().notifyNeighbors();
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		this.duality.provideCrafting(craftingTracker);

	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		// TODO Auto-generated method stub
		return this.duality.pushPattern(patternDetails, table);
	}

	@Override
	public boolean isBusy() {
		// TODO Auto-generated method stub
		return this.duality.isBusy();
	}

	@Override
	public void gridChanged() {
		this.duality.gridChanged();
	}

	@Override
	public TileEntity getTile() {
		// TODO Auto-generated method stub
		return getTileEntity();
	}

	@Override
	public IConfigManager getConfigManager() {
		// TODO Auto-generated method stub
		return this.duality.getConfigManager();
	}

	@Override
	public IInventory getInventoryByName(String name) {
		// TODO Auto-generated method stub
		return this.duality.getInventoryByName(name);
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs() {
		// TODO Auto-generated method stub
		return this.duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
		// TODO Auto-generated method stub
		return this.duality.injectCraftedItems(link, items, mode);
	}

	@Override
	public void jobStateChange(ICraftingLink link) {
		this.duality.jobStateChange(link);

	}

	@Override
	public IGridNode getActionableNode() {

		return this.getProxy().getNode();
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {

		return this.getProxy().getNode();
	}

	@Override
	public void securityBreak() {

	}

	@Override
	public DimensionalCoord getLocation() {

		return this.pos;
	}

	@Override
	public IInventory getPatterns() {

		return this.duality.getPatterns();
	}

	@Override
	public String getName() {

		return getCustomName();
	}

	@Override
	public boolean shouldDisplay() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public DualityInterface getInterfaceDuality() {
		// TODO Auto-generated method stub
		return this.duality.getInterfaceDuality();
	}

	@Override
	public EnumSet<ForgeDirection> getTargets() {
		// TODO Auto-generated method stub
		return EnumSet.of(ForgeDirection.UNKNOWN);
	}

	@Override
	public TileEntity getTileEntity() {

		if (faketile.getWorldObj() == null) {

			faketile.setWorldObj(pos.getWorld());
			faketile.xCoord = pos.x;
			faketile.yCoord = pos.y;
			faketile.zCoord = pos.z;
		}
		;

		return faketile;
	}

	@Override
	public void saveChanges() {
		duality.saveChanges();

	}

	@Override
	public NBTBase saveDataToNBT() {
		NBTBase t = Data.super.saveDataToNBT();
		((NBTTagCompound) t).setInteger("p", p);
		duality.writeToNBT((NBTTagCompound) t);
		if(duality.hasCustomName())((NBTTagCompound) t).setString("realName", duality.getCustomName());
		
		return t;
	}

	@Override
	public void loadDataFromNBT(NBTBase aNBT) {

		Data.super.loadDataFromNBT(aNBT);
		// System.out.println(pos.getWorld());
		p = ((NBTTagCompound) aNBT).getInteger("p");
		faketile.xCoord = pos.x;
		faketile.yCoord = pos.y;
		faketile.zCoord = pos.z;
		faketile.setWorldObj(pos.getWorld());
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {

			duality.readFromNBT((NBTTagCompound) aNBT);
		}
		;
		if(((NBTTagCompound) aNBT).hasKey("realName"))duality.setCustomName(((NBTTagCompound) aNBT).getString("realName"));
	
	}

	public void update(ICoverable aTileEntity) {

		Iterator<IGridConnection> it = getProxy().getNode().getConnections().iterator();
		IGridConnection item = null;
		IGridNode thenode = this.duality.getProxy().getNode();
		boolean found = false;
		while (it.hasNext()) {
			item = it.next();
			if (item.b() == thenode || item.a() == thenode) {
				found = true;
				break;
			}
			;

		}

		if (found == false) {
			try {
				new GridConnection(getProxy().getNode(), thenode, side);

				MyMod.LOG.info("Internal Node connect@" + getPos());
			} catch (FailedConnection e) {
				e.printStackTrace();
			}

		}
		;
	
		
		
	};

	/*@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		//
		return duality.getTickingRequest(node);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
		if (first)
			return TickRateModulation.SAME;
		

		
		return duality.tickingRequest(node, TicksSinceLastCall);
	}*/@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		
		return new TickingRequest(100, 100, false, false);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
		return TickRateModulation.SAME;
	}
	public String getCustomName() {
		if(duality.hasCustomName())return duality.getCustomName();
		return nameOverride();
	}
	private String nameOverride(){
		return "P2P - Dual ME Interface";
	}
	public boolean hasCustomName() {
		  return duality.hasCustomName();
	}

	
	public void setCustomName(String name) {
		
		/*if(name==null||name.isEmpty()){
			NBTTagCompound tg = this.getTag();
			tg.removeTag("display");
			this.setTag(tg);
			return;
		}*/
		
		duality.setCustomName(name);
		
		
	

	}
	

	public int getPriority() {
		return p;
	};

	int p;

	public void setPriority(int newValue) {
		p = newValue;
	};

	@Override
	public void destroy() {
		final ArrayList<ItemStack> drops = new ArrayList<>();

		for (String s : new String[] { "patterns", "upgrades" }) {
			IInventory inv = duality.getInventoryByName(s);
			for (int l = 0; l < inv.getSizeInventory(); l++) {
				final ItemStack is = inv.getStackInSlot(l);
				inv.setInventorySlotContents(l, null);
				if (is != null) {
					drops.add(is);
				}
			}
		}

		Platform.spawnDrops(pos.getWorld(), pos.x, pos.y, pos.z, drops);

		Data.super.destroy();
	}

	@Override
	public boolean firstUpdate() {
	
		if (first) {
			first = false;
			return true;
		}
		return false;
	}

	boolean first = true;

	@Override
	public void accept(ForgeDirection side, ICoverable aTileEntity,boolean b) {
		Data.super.accept(side, aTileEntity,b);
		this.duality.setPartHostInfo(ForgeDirection.UNKNOWN, fakehost, getTile());
		
		String s=tagName();
		if(s!=null&&!s.equals("")){
			duality.setCustomName(s);
			setTag(null);
		}
		
	
	
	
	}

	public boolean click(EntityPlayer player) {
		final ItemStack is = player.inventory.getCurrentItem();

		if (is != null && is.getItem() instanceof IMemoryCard) {
			IMemoryCard mc = (IMemoryCard) is.getItem();
			if (ForgeEventFactory.onItemUseStart(player, is, 1) <= 0)
				return false;

			final NBTTagCompound data = mc.getData(is);

			final ItemStack newType = ItemStack.loadItemStackFromNBT(data);

			if (newType != null) {
				if (newType.getItem() instanceof IPartItem) {
					IPartItem partItem = (IPartItem) newType.getItem();
					final IPart testPart = partItem.createPartFromItemStack(newType);
					if (testPart != null && testPart instanceof PartFluidP2PInterface) {
						// this.getHost().removePart(this.getSide(), true);
						// final ForgeDirection dir =
						// this.getHost().addPart(newType, this.getSide(),
						// player);
						// final IPart newBus = duality;

						/* if (newBus instanceof PartP2PTunnel<?>newTunnel) */ {

							ArrayList<ItemStack> drops = new ArrayList<>();
							for (int i = 0; i < duality.getPatterns().getSizeInventory(); i++) {
								if (duality.getPatterns().getStackInSlot(i) == null)
									continue;
								drops.add(duality.getPatterns().getStackInSlot(i));
								duality.getPatterns().setInventorySlotContents(i, null);
							}
							Platform.spawnDrops(pos.getWorld(), pos.x, pos.y, pos.z, drops);

							try {
								Field method;
								method = PartP2PTunnel.class.getDeclaredField("output");
								method.setAccessible(true);
								method.set(duality, true);
							} catch (Exception e1) {

								e1.printStackTrace();
							}

							// IConfigManager config =
							// duality.getConfigManager();

							try {
								duality.pasteMemoryCardData(duality, data);
							} catch (final GridAccessException e) {
								// :P
							}

							/*
							 * config.getSettings().forEach( setting ->
							 * duality.getConfigManager().putSetting(setting,
							 * config.getSetting(setting)));
							 */

							duality.onTunnelNetworkChange();
						}

						mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
						return true;
					}
				}
			}
			mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
		}
		return false;

	}

	@Override
	public boolean nonShiftClick(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity,
			EntityPlayer aPlayer) {

		// if(aPlayer.getHeldItem()!=null&&aPlayer.getHeldItem().getItem()!=null&&aPlayer.getHeldItem().getItem()
		// instanceof IMemoryCard){
		if (Optional.ofNullable(aPlayer.getHeldItem()).map(ItemStack::getItem).filter(S -> S instanceof IMemoryCard)
				.isPresent()) {
			if (click(aPlayer)) {
				return true;
			}
		}

		return Data.super.nonShiftClick(side, aCoverID, aCoverVariable, aTileEntity, aPlayer);
	}

	@Override
	public ItemStack getVisual() {

		return new ItemStack(MyMod.cover, 1, 35);
	}

	@Override
	public TileEntity fakeTile() {
		return faketile;
	}

	public boolean requireChannel() {
		return false;
	}// internal node will require
	@Override
	public ForgeDirection getActualSide() {
		// TODO Auto-generated method stub
		return side;
	}
	
	@Override
	public boolean dualityName() {
		return true;
	}
	@Override
	public String name() {
	      if(duality.hasCustomName())return duality.getCustomName();
		return null;
	}
	@Override
	public void addUIWidgets(Builder builder, GT_CoverUIBuildContext gt_CoverUIBuildContext) {
		if (hasAEGUI() && !gt_CoverUIBuildContext.getPlayer().getEntityWorld().isRemote) {
			gt_CoverUIBuildContext.getPlayer()
		.openGui(MyMod.instance, side.ordinal(), 	
				gt_CoverUIBuildContext.getPlayer().getEntityWorld(), this.getPos().x,
				 this.getPos().y, this.getPos().z);}
	}
	
}
