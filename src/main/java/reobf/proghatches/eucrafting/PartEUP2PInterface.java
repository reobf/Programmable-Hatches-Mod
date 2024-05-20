package reobf.proghatches.eucrafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import reobf.proghatches.eucrafting.IEUManager.IDrain;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.SISOPatternDetail;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.WrappedPatternDetail;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.DualityFluidInterface;
import com.glodblock.github.util.Util;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.integration.modules.waila.part.BasePartWailaDataProvider;
import appeng.integration.modules.waila.part.IPartWailaDataProvider;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.parts.automation.UpgradeInventory;
import appeng.parts.p2p.IPartGT5Power;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.parts.p2p.PartP2PTunnelStatic;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cofh.api.energy.IEnergyReceiver;
import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.gui.modularui.GT_CoverUIBuildContext;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.api.util.GT_Utility;
import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;
import ic2.api.energy.tile.IEnergySink;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

//modified from PartFluidP2PInterface
public class PartEUP2PInterface extends PartP2PTunnelStatic<PartEUP2PInterface> implements IGridTickable,
		IStorageMonitorable, IInventoryDestination, IDualHost, ISidedInventory, IAEAppEngInventory,
		ITileStorageMonitorable, IPriorityHost, IInterfaceHost, IPartGT5Power, IGuiProvidingPart, IDrain {
	public static class WailaDataProvider extends BasePartWailaDataProvider {

		@Override
		public NBTTagCompound getNBTData(EntityPlayerMP player, IPart part, TileEntity te, NBTTagCompound tag,
				World world, int x, int y, int z) {
			if (PartEUP2PInterface.class.isInstance(part)) {
				PartEUP2PInterface pt = PartEUP2PInterface.class.cast(part);

				ProghatchesUtil.ser(tag, pt.id, "ID");
				ProghatchesUtil.ser(tag, pt.inputid, "INPUT_ID");
				tag.setBoolean("p2p_out", pt.isP2POut());
				Optional.ofNullable(pt.getInput()).ifPresent(s -> {
					tag.setInteger("INPUT_X", s.getTile().xCoord);
					tag.setInteger("INPUT_Y", s.getTile().yCoord);
					tag.setInteger("INPUT_Z", s.getTile().zCoord);
					tag.setInteger("INPUT_DIM", s.getTile().getWorldObj().provider.dimensionId);
				});

				PartEUP2PInterface in = pt.isP2POut() ? pt.getInput() : pt;
				Optional.ofNullable(in).ifPresent(s -> {
					tag.setLong("V", in.voltage);
					tag.setLong("A", in.amp);
					tag.setLong("EA", in.expectedamp);
					tag.setDouble("AA", in.averageamp);
					tag.setDouble("AAL", pt.averageamp_local);
				});
				;

			}

			return tag;
		}

		@Override
		public List<String> getWailaBody(IPart part, List<String> currentToolTip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {
			if (PartEUP2PInterface.class.isInstance(part)) {
				currentToolTip.add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.V",
						accessor.getNBTData().getLong("V")));
				currentToolTip.add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.EA",
						accessor.getNBTData().getLong("EA")));

				currentToolTip.add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.AA",
						String.format("%.2f", accessor.getNBTData().getDouble("AA")),
						accessor.getNBTData().getLong("A")));
				// if(pt)
				{
					double d1 = accessor.getNBTData().getDouble("AAL");
					double d2 = accessor.getNBTData().getDouble("AA");

					currentToolTip.add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.AAL",
							String.format("%.2f", (d1 > 0.01 && d1 <= d2) ? d1 / d2 * 100 : 0)));
				}

				if (accessor.getNBTData().getBoolean("p2p_out")) {

					UUID id = ProghatchesUtil.deser(accessor.getNBTData(), "INPUT_ID");
					if (id.equals(zero)) {
						return super.getWailaBody(part, currentToolTip, accessor, config);
					}
					if (accessor.getNBTData().hasKey("INPUT_DIM")) {
						for (int i = 0; i < 4; i++)
							currentToolTip.add(StatCollector.translateToLocalFormatted(
									"proghatches.eu.interface.waila.UUID.out." + i, id.toString(),
									accessor.getNBTData().getInteger("INPUT_X"),
									accessor.getNBTData().getInteger("INPUT_Y"),
									accessor.getNBTData().getInteger("INPUT_Z"),
									accessor.getNBTData().getInteger("INPUT_DIM")));
					} else {

						currentToolTip
								.add(StatCollector.translateToLocal("proghatches.eu.interface.waila.inputmissing"));
					}

				} else {
					UUID id = ProghatchesUtil.deser(accessor.getNBTData(), "ID");
					if (id.equals(zero)) {
						return super.getWailaBody(part, currentToolTip, accessor, config);
					}
					currentToolTip.add(StatCollector.translateToLocalFormatted("proghatches.eu.interface.waila.UUID",
							id.toString())

					);

				}

			}
			return super.getWailaBody(part, currentToolTip, accessor, config);
		}

	}

	private final DualityInterface duality = new DualityInterface(this.getProxy(), this) {

		@Override
		public void updateCraftingList() {
			if (!isOutput()) {
				initTokenTemplate();
				super.updateCraftingList();
				try {
					for (PartEUP2PInterface p2p : getOutputs()) {

						p2p.duality.updateCraftingList();
					}
				} catch (GridAccessException e) {
					// ?
				}
			} else {
				PartEUP2PInterface p2p = getInput();
				if (p2p == null)
					return;
				voltage = p2p.voltage;
				inputid = p2p.id;// id is unique, so do not modify it
				expectedamp = p2p.expectedamp;
				initTokenTemplate();
				if (p2p != null) {
					this.craftingList = p2p.duality.craftingList;

					try {
						this.gridProxy.getGrid()
								.postEvent(new MENetworkCraftingPatternChange(this, this.gridProxy.getNode()));
					} catch (final GridAccessException e) {
						// :P
					}
				}
			}

		}

		@Override
		public int getInstalledUpgrades(Upgrades u) {
			if (isOutput() && u == Upgrades.PATTERN_CAPACITY)
				return -1;
			return super.getInstalledUpgrades(u);
		}
	};
	private final DualityFluidInterface dualityFluid = new DualityFluidInterface(this.getProxy(), this);
	private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 6);
	private final BaseActionSource ownActionSource = new MachineSource(this);
	private boolean updatev;
	private long instantamp;
	private double averageamp;
	private double averageamp_local;
	private long amp;
	private long minv;
	private long maxv;
	private long voltage;
	private long expectedamp;
	private ItemStack token;
	private ItemStack blank_token;
	private UUID id;
	private long accepted;
	private int redstoneticks;
	private ArrayList<ItemStack> is = new ArrayList<>();;

	public PartEUP2PInterface(ItemStack is) {
		super(is);
		id = UUID.randomUUID();
		initTokenTemplate();
	}

	@MENetworkEventSubscribe
	public void stateChange(final MENetworkChannelsChanged c) {
		dualityFluid.onChannelStateChange(c);
		duality.notifyNeighbors();
	}

	@MENetworkEventSubscribe
	public void stateChange(final MENetworkPowerStatusChange c) {
		dualityFluid.onPowerStateChange(c);
		duality.notifyNeighbors();
	}

	@Override
	public void gridChanged() {
		super.gridChanged();
		dualityFluid.gridChanged();
	}

	@Override
	public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {

		if (player.isSneaking()) {
			TileEntity t = this.getTile();
			if (!t.getWorldObj().isRemote) {

				/*
				 * if(this.isOutput()){ PartEUP2PInterface p2p=this.getInput();
				 * 
				 * if(p2p!=null){TileEntity te = p2p.getTileEntity();
				 * if(p2p.data==null) EUUtil.open(player,
				 * player.getEntityWorld(),te.xCoord,te.yCoord,te.zCoord,
				 * p2p.getSide(),false);
				 * 
				 * 
				 * }else{
				 * 
				 * player.addChatComponentMessage(new ChatComponentTranslation(
				 * "proghatches.eucreafting.p2p.connection"));
				 * 
				 * } } else
				 */
				EUUtil.open(player, player.getEntityWorld(), t.xCoord, t.yCoord, t.zCoord, getSide(), isP2POut());

			}

			return true;
		}
		AppEngInternalInventory patterns = (AppEngInternalInventory) duality.getPatterns();
		if (super.onPartActivate(player, pos)) {
			ArrayList<ItemStack> drops = new ArrayList<>();
			for (int i = 0; i < patterns.getSizeInventory(); i++) {
				if (patterns.getStackInSlot(i) == null)
					continue;
				drops.add(patterns.getStackInSlot(i));
			}
			final IPart tile = this.getHost().getPart(this.getSide());
			if (tile instanceof PartEUP2PInterface) {
				PartEUP2PInterface dualTile = (PartEUP2PInterface) tile;
				DualityInterface newDuality = dualTile.duality;
				// Copy interface storage, upgrades, and settings over
				UpgradeInventory upgrades = (UpgradeInventory) duality.getInventoryByName("upgrades");
				dualTile.duality.getStorage();
				UpgradeInventory newUpgrade = (UpgradeInventory) newDuality.getInventoryByName("upgrades");
				for (int i = 0; i < upgrades.getSizeInventory(); ++i) {
					newUpgrade.setInventorySlotContents(i, upgrades.getStackInSlot(i));
				}
				IInventory storage = duality.getStorage();
				IInventory newStorage = newDuality.getStorage();
				for (int i = 0; i < storage.getSizeInventory(); ++i) {
					newStorage.setInventorySlotContents(i, storage.getStackInSlot(i));
				}
				IConfigManager config = duality.getConfigManager();
				config.getSettings().forEach(
						setting -> newDuality.getConfigManager().putSetting(setting, config.getSetting(setting)));
			}
			TileEntity te = getTileEntity();
			Platform.spawnDrops(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, drops);

			return true;
		}

		if (player.isSneaking()) {
			return false;
		}

		if (Platform.isServer()) {
			InventoryHandler.openGui(player, this.getHost().getTile().getWorldObj(),
					new BlockPos(this.getHost().getTile()), Objects.requireNonNull(this.getSide()),
					GuiType.DUAL_INTERFACE);
		}

		return true;
	}

	@Override
	public IIcon getTypeTexture() {
		return MyMod.block_euinterface.getIcon(0, 0);
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src) {
		return duality.getMonitorable(side, src, this);
	}

	public boolean redstone() {
		int power;// =0;
		if (data != null) {

			return data.getRedstone();
		}
		final int x = this.getTile().xCoord + this.getSide().offsetX;
		final int y = this.getTile().yCoord + this.getSide().offsetY;
		final int z = this.getTile().zCoord + this.getSide().offsetZ;

		final Block b = this.getTile().getWorldObj().getBlock(x, y, z);
		if (b != null) {
			int srcSide = this.getSide().ordinal();
			if (b instanceof BlockRedstoneWire) {
				srcSide = 1;
			}
			power = b.isProvidingStrongPower(this.getTile().getWorldObj(), x, y, z, srcSide);
			power = Math.max(power, b.isProvidingWeakPower(this.getTile().getWorldObj(), x, y, z, srcSide));
			return power > 0;
		} else {
			return false;
		}

	}

	public boolean refund(IMEInventory<IAEItemStack> inv, IMEInventory<IAEItemStack> recver) {
		IAEItemStack ret = inv.extractItems(AEItemStack.create(token).setStackSize(amp)

				, Actionable.SIMULATE, new MachineSource(this));

		if (ret != null) {

			if (ret.getStackSize() == amp) {
				inv.extractItems(AEItemStack.create(token).setStackSize(amp), Actionable.MODULATE,
						new MachineSource(this));

				recver.injectItems(AEItemStack.create(blank_token).setStackSize(amp), Actionable.MODULATE,
						new MachineSource(this));

				return true;
			}

		}
		return false;
	}

	boolean prevPower;

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {

		returnItems();
		/*
		 * if(redstone()){ if(this.isOutput()) Optional.ofNullable(
		 * this.getInput()).ifPresent( s->s.redstoneticks++ ); else
		 * this.redstoneticks++; }
		 */
		boolean ok = false;
		if (!this.isOutput()) {

			boolean red = this.redstone();
			try {
				for (PartEUP2PInterface o : this.getOutputs()) {
					red = red | o.redstone();
				}
			} catch (GridAccessException e) {

				e.printStackTrace();
			}

			if (prevPower == true && red == false) {
				ok = true;

			}

			prevPower = red;

		}

		if (ok || redstoneticks > 0) {

			try {

				IMEMonitor<IAEItemStack> store = getProxy().getStorage().getItemInventory();

				for (ICraftingCPU cluster : getProxy().getCrafting().getCpus()) {
					if (cluster instanceof CraftingCPUCluster == false) {
						continue;
					}

					IMEInventory<IAEItemStack> inv = ((CraftingCPUCluster) cluster).getInventory();
					long prevamp = amp;

					if (refund(inv, store)) {

						/*
						 * ((CraftingCPUCluster)cluster).addCrafting(new
						 * PatternDetail(blank_token.copy(), token.copy()),
						 * prevamp);
						 * 
						 * 
						 * 
						 * ((CraftingCPUCluster)cluster).addEmitable(AEItemStack
						 * .create(blank_token.copy()).setStackSize(prevamp));
						 */redstoneticks = 0;
						amp = 0;
						break;
					}
				}

				if (refund(store, store)) {
					amp = 0;
					redstoneticks = 0;
				}
			} catch (GridAccessException e) {
				e.printStackTrace();
			}

		}
		TickRateModulation item = duality.tickingRequest(node, ticksSinceLastCall);
		TickRateModulation fluid = dualityFluid.tickingRequest(node, ticksSinceLastCall);
		/*
		 * if (item.ordinal() >= fluid.ordinal()) { return item; } else { return
		 * fluid; }
		 */
		return TickRateModulation.SAME;
	}

	@Override
	public int getInstalledUpgrades(Upgrades u) {
		return duality.getInstalledUpgrades(u);
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory() {
		return duality.getItemInventory();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory() {
		return duality.getFluidInventory();
	}

	@Override
	public int getPriority() {
		return duality.getPriority();
	}

	@Override
	public void setPriority(int newValue) {
		duality.setPriority(newValue);
	}

	@Override
	public void onTunnelNetworkChange() {
		duality.updateCraftingList();
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation op, ItemStack removedStack,
			ItemStack newStack) {
		duality.onChangeInventory(inv, slot, op, removedStack, newStack);
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		return duality.canInsert(stack);
	}

	@Override
	public int getSizeInventory() {
		return duality.getStorage().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slotIn) {
		return duality.getStorage().getStackInSlot(slotIn);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return duality.getStorage().decrStackSize(index, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int index) {
		return duality.getStorage().getStackInSlotOnClosing(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		duality.getStorage().setInventorySlotContents(index, stack);
	}

	@Override
	public IInventory getInventoryByName(String name) {
		return duality.getInventoryByName(name);
	}

	@Override
	public IConfigManager getConfigManager() {
		return duality.getConfigManager();
	}

	@Override
	public IIcon getBreakingTexture() {
		return getItemStack().getIconIndex();
	}

	@Override
	public String getInventoryName() {
		return duality.getStorage().getInventoryName();
	}

	@Override
	public boolean hasCustomInventoryName() {
		return duality.getStorage().hasCustomInventoryName();
	}

	@Override
	public int getInventoryStackLimit() {
		return duality.getStorage().getInventoryStackLimit();
	}

	@Override
	public void markDirty() {
		duality.markDirty();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return duality.getStorage().isUseableByPlayer(player);
	}

	@Override
	public void openInventory() {
		duality.getStorage().openInventory();
	}

	@Override
	public void closeInventory() {
		duality.getStorage().closeInventory();
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return duality.getStorage().isItemValidForSlot(index, stack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		return duality.getAccessibleSlotsFromSide(side);
	}

	@Override
	public boolean canInsertItem(int p_102007_1_, ItemStack itemStack, int p_102007_3_) {
		return true;
	}

	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
		return true;
	}

	private IMEMonitor<IAEFluidStack> getFluidGrid() {
		try {
			return getProxy().getGrid().<IStorageGrid> getCache(IStorageGrid.class).getFluidInventory();
		} catch (GridAccessException e) {
			return null;
		}
	}

	private IEnergyGrid getEnergyGrid() {
		try {
			return getProxy().getGrid().getCache(IEnergyGrid.class);
		} catch (GridAccessException e) {
			return null;
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
		IEnergyGrid energyGrid = getEnergyGrid();
		if (energyGrid == null || fluidGrid == null || resource == null)
			return 0;
		int ori = resource.amount;
		IAEFluidStack remove;
		if (doFill) {
			remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.MODULATE, ownActionSource);
		} else {
			remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.SIMULATE, ownActionSource);
		}
		return remove == null ? ori : (int) (ori - remove.getStackSize());
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return dualityFluid.drain(from, resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return dualityFluid.drain(from, maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return dualityFluid.getTankInfo(from);
	}

	@Override
	public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
		saveChanges();
		getTileEntity().markDirty();
		dualityFluid.onFluidInventoryChanged(inv, slot);
	}

	@Override
	public AEFluidInventory getInternalFluid() {
		return dualityFluid.getInternalFluid();
	}

	@Override
	public DualityFluidInterface getDualityFluid() {
		return dualityFluid;
	}

	@Override
	public AppEngInternalAEInventory getConfig() {
		Util.mirrorFluidToPacket(config, dualityFluid.getConfig());
		return config;
	}

	@Override
	public void setConfig(int id, IAEFluidStack fluid) {
		if (id >= 0 && id < 6) {
			config.setInventorySlotContents(id,
					ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack()));
			dualityFluid.getConfig().setFluidInSlot(id, dualityFluid.getStandardFluid(fluid));
		}
	}

	@Override
	public void setFluidInv(int id, IAEFluidStack fluid) {
		if (id >= 0 && id < 6) {
			dualityFluid.getInternalFluid().setFluidInSlot(id, fluid);
		}
	}

	@Override
	public DualityInterface getInterfaceDuality() {
		return duality;
	}

	@Override
	public EnumSet<ForgeDirection> getTargets() {
		return EnumSet.of(this.getSide());
	}

	@Override
	public TileEntity getTileEntity() {
		return super.getHost().getTile();
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		ICraftingProviderHelper collector = new ICraftingProviderHelper() {

			@Override
			public void addCraftingOption(ICraftingMedium medium, ICraftingPatternDetails api) {
				ItemStack a = token.copy();

				ItemStack b = token.copy();
				ItemStack c = blank_token.copy();
				if (expectedamp > 0) {
					c.stackSize = a.stackSize = b.stackSize = (int) expectedamp;

					/*
					 * craftingTracker.addCraftingOption(PartEUP2PInterface.this
					 * ,TileFluidInterface_EU.wrap(api, c, b,0 ));
					 */
					craftingTracker.addCraftingOption(PartEUP2PInterface.this,
							TileFluidInterface_EU.wrap(api, a, b, Integer.MAX_VALUE - 1));

				} else {
					craftingTracker.addCraftingOption(medium, api);
				}
			}

			@Override
			public void setEmitable(IAEItemStack what) {
				craftingTracker.setEmitable(what);
			}
		};
		this.duality.provideCrafting(collector);

		// if(!this.isOutput())
		craftingTracker.addCraftingOption(this, new SISOPatternDetail(blank_token.copy(), token.copy()));

		/*
		 * craftingTracker.addCraftingOption(this, new
		 * PatternDetail(blank_token.copy(), token.copy() ));
		 */

	}

	private void returnItems() {
		is.removeIf(s -> {

			try {
				return Optional.ofNullable(

						this.getProxy().getStorage().getItemInventory().injectItems(AEItemStack.create(s)

								, Actionable.MODULATE, new MachineSource(this))

				).map(IAEItemStack::getStackSize).orElse(0L) == 0L

				;

			} catch (GridAccessException e) {
				return false;
			}
		});

	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		returnItems();
		if (patternDetails instanceof SISOPatternDetail) {
			is.add(((SISOPatternDetail) patternDetails).out);
			// do not call returnItems() here, or items returned will not be
			// considered
			// as output
			return true;
		}
		if (patternDetails instanceof WrappedPatternDetail) {

			WrappedPatternDetail p = (WrappedPatternDetail) patternDetails;
			int[] count = new int[1];
			int size = table.getSizeInventory();
			for (int i = 0; i < size; i++) {
				ItemStack is = table.getStackInSlot(i);
				if (is != null && is.getItem() == MyMod.eu_token && is.stackSize > 0) {
					count[0] += is.stackSize;
					is.stackSize = 0;
					table.setInventorySlotContents(i, is);

					break;
				}
				;
			}
			PartEUP2PInterface face = this;
			if (isP2POut()) {
				face = this.getInput();
			}
			boolean succ = duality.pushPattern(p.original, table);
			if (succ) {
				if (face != null)
					face.amp = Math.max(face.amp, count[0]);
				is.add(p.extraOut0);
			}

			return succ;
		}
		return duality.pushPattern(patternDetails, table);

	}

	@Override
	public boolean isBusy() {
		return duality.isBusy();
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs() {
		return duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
		return duality.injectCraftedItems(link, items, mode);
	}

	@Override
	public void jobStateChange(ICraftingLink link) {
		duality.jobStateChange(link);
	}

	final static UUID zero = new UUID(0, 0);

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		duality.readFromNBT(data);
		id = ProghatchesUtil.deser(data, "EUFI");
		if (id.equals(zero)) {
			id = UUID.randomUUID();
		}
		;
		inputid = ProghatchesUtil.deser(data, "EUFI_INPUT");

		amp = data.getLong("amp");
		voltage = data.getLong("voltage");
		accepted = data.getLong("accepted");
		averageamp = data.getDouble("averageamp");
		redstoneticks = data.getInteger("redstoneticks");
		expectedamp = data.getLong("expectedamp");
		redstoneOverride = data.getBoolean("redstoneOverride");
		is.clear();
		IntStream.range(0, data.getInteger("pending_size")).forEach(s -> {

			is.add(ItemStack.loadItemStackFromNBT((NBTTagCompound) data.getTag("pending_" + s)));

		});

		initTokenTemplate();
	}

	public boolean redstoneOverride;

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		duality.writeToNBT(data);
		ProghatchesUtil.ser(data, id, "EUFI");
		ProghatchesUtil.ser(data, inputid, "EUFI_INPUT");
		data.setLong("amp", amp);
		data.setLong("voltage", voltage);
		data.setLong("accepted", accepted);
		data.setInteger("pending_size", is.size());
		data.setDouble("averageamp", averageamp);
		data.setInteger("redstoneticks", redstoneticks);
		data.setLong("expectedamp", expectedamp);
		data.setBoolean("redstoneOverride", redstoneOverride);
		for (int i = 0; i < is.size(); i++) {
			data.setTag("pending_" + i, is.get(i).writeToNBT(new NBTTagCompound()));
		}
		;
	}

	@Override
	public NBTTagCompound getMemoryCardData() {
		final NBTTagCompound output = super.getMemoryCardData();
		this.duality.getConfigManager().writeToNBT(output);
		return output;
	}

	@Override
	public void pasteMemoryCardData(PartP2PTunnel newTunnel, NBTTagCompound data) throws GridAccessException {
		this.duality.getConfigManager().readFromNBT(data);
		super.pasteMemoryCardData(newTunnel, data);

		onChange();

	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		this.duality.initialize();
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		super.getDrops(drops, wrenched);
		duality.addDrops(drops);
	}

	@Override
	public boolean shouldDisplay() {
		return IInterfaceHost.super.shouldDisplay() && !isOutput();
	}

	@Override
	public IInventory getPatterns() {
		// TODO Auto-generated method stub
		return duality.getPatterns();
	}

	@Override
	public String getName() {

		return duality.getTermName();
	}

	@Override
	public long injectEnergyUnits(long voltage, long amperage) {

		return 0;
	}

	@Override
	public boolean inputEnergy() {

		return false;
	}

	@Override
	public boolean outputsEnergy() {

		return true;
	}

	public boolean isP2POut() {
		return this.isOutput();
	}

	@Override
	public ModularWindow createWindow(UIBuildContext buildContext) {
		NBTTagCompound tag = buildContext.getPlayer().getEntityData();

		return tag.getBoolean("extraarg") ? createWindowOut(buildContext, 0) : createWindowIn(buildContext);
	}

	public ModularWindow createWindowIn(UIBuildContext buildContext) {
		ModularWindow.Builder builder = ModularWindow.builder(176, 107);
		// builder.setGuiTint(buildContext.getGuiColorization);
		builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
		return addWidgets(builder, 0).build();
	}

	public ModularWindow.Builder addWidgetsOut(ModularWindow.Builder builder, int yshift) {

		builder.widget(TextWidget.localised("proghatches.eu.interface.hint.input.title").setPos(58, 30));
		builder.widget(TextWidget.dynamicString(() -> {
			// PartEUP2PInterface in = PartEUP2PInterface.this.getInput();
			// if(in==null)return "Connection Lost";

			return instantamp_local + "/" + String.format("%.2f", averageamp_local) + "/" + amp + "A";
		}).setSynced(true).setPos(58, 60).addTooltips(

				ImmutableList.of(LangManager.translateToLocal("proghatches.eu.interface.hint.amp.local")

				)));
		builder.widget(TextWidget.dynamicString(() -> {
			PartEUP2PInterface in = PartEUP2PInterface.this.getInput();
			if (in == null)
				return "Connection Lost";

			return in.voltage + "V" + "/" + in.expectedamp + "A";
		}).setSynced(true).setPos(58, 70).addTooltips(

				ImmutableList.of(LangManager.translateToLocal("proghatches.eu.interface.hint.input")

				)));
		builder.widget(TextWidget.dynamicString(() -> {
			PartEUP2PInterface in = PartEUP2PInterface.this.getInput();
			if (in == null)
				return "Connection Lost";

			return in.instantamp + "/" + String.format("%.2f", in.averageamp) + "/" + in.amp + "A";
		}).setSynced(true).setPos(58, 80).addTooltips(

				ImmutableList.of(LangManager.translateToLocal("proghatches.eu.interface.hint.amp")

				)));

		builder.widget(TextWidget.dynamicString(() -> {
			PartEUP2PInterface in = PartEUP2PInterface.this.getInput();
			if (in == null)
				return "Connection Lost";
			return String.format("[%d~%d]V", in.minv, in.maxv);
		}).setSynced(true).setPos(58, 90).addTooltips(

				ImmutableList.of(LangManager.translateToLocal("proghatches.eu.interface.hint.volt")
				// ,
				// LangManager.translateToLocal("proghatches.eu.interface.hint")

				)));

		return builder;
	}

	public ModularWindow.Builder addWidgets(ModularWindow.Builder builder, int yshift) {

		updatev = true;

		builder.widget(((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(true, true))
				.setGetter(() -> (redstoneticks > 0) ? 1 : 0).setSetter(s -> redstoneticks = s).setLength(2)
				.setTextureGetter(i -> {
					if (i == 1)
						return GT_UITextures.OVERLAY_BUTTON_CHECKMARK;

					return GT_UITextures.OVERLAY_BUTTON_CROSS;
				})

				.addTooltip(0, LangManager.translateToLocal("proghatches.eucreafting.finish.false"))
				.addTooltip(1, LangManager.translateToLocal("proghatches.eucreafting.finish.true"))

				.setPos(8, 80)

		);
		/*
		 * builder.widget( ((CycleButtonWidget) new
		 * CoverCycleButtonWidget().setSynced(false, true)) .setGetter(() ->
		 * recycle ? 1 : 0) .setSetter(s -> recycle = s == 1) .setLength(2)
		 * .setTextureGetter(i -> { if (i == 1) return
		 * GT_UITextures.OVERLAY_BUTTON_CHECKMARK; return
		 * GT_UITextures.OVERLAY_BUTTON_CROSS; })
		 * 
		 * .addTooltip(0,
		 * LangManager.translateToLocal("proghatches.part.recycle.false"))
		 * .addTooltip(1,
		 * LangManager.translateToLocal("proghatches.part.recycle.true"))
		 * .setPos(8, 80)
		 * 
		 * ); builder.widget( ((CycleButtonWidget) new
		 * CoverCycleButtonWidget().setSynced(false, true)) .setGetter(() ->
		 * onoff ? 1 : 0) .setSetter(s -> {onoff = s == 1;postEvent();})
		 * .setLength(2) .setTextureGetter(i -> { if (i == 1) return
		 * GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_ON; return
		 * GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF; })
		 * 
		 * .addTooltip(0,
		 * LangManager.translateToLocal("proghatches.part.onoff.false"))
		 * .addTooltip(1,
		 * LangManager.translateToLocal("proghatches.part.onoff.true"))
		 * .setPos(8+16, 80)
		 * 
		 * );
		 */
		builder.widget(
				TextWidget.dynamicString(() -> instantamp + "/" + String.format("%.2f", averageamp) + "/" + amp + "A")
						.setSynced(true).setPos(58, 80).addTooltips(

								ImmutableList.of(LangManager.translateToLocal("proghatches.eu.interface.hint.amp"),
										LangManager.translateToLocal("proghatches.eu.interface.hint")

								)));
		builder.widget(TextWidget.dynamicString(() -> String.format("[%d~%d]V", minv, maxv)).setSynced(true)
				.setPos(58, 90).addTooltips(

						ImmutableList.of(LangManager.translateToLocal("proghatches.eu.interface.hint.volt")
						// ,
						// LangManager.translateToLocal("proghatches.eu.interface.hint")

						)));

		BiFunction<Supplier<Long>, Consumer<Long>, Widget> gen = (a, b) -> {

			TextFieldWidget o = Optional.of(new TextFieldWidget()).filter(s -> {
				s.setText((a.get() + ""));
				return true;
			}).get();
			o.setValidator(val -> {
				if (val == null) {
					val = "";
				}
				return val;
			}).setSynced(true, true).setGetter(() -> {
				return a.get() + "";
			}).setSetter(s -> {
				try {
					b.accept(Math.max(Long.valueOf(s), 0l));
				} catch (Exception e) {
					b.accept(0l);
				}
				onChange();
				o.notifyTooltipChange();
			}).setPattern(BaseTextFieldWidget.NATURAL_NUMS).setMaxLength(50).setScrollBar().setFocusOnGuiOpen(false)
					.setTextColor(Color.WHITE.dark(1))

					.setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2)).setPos(16, 16 + yshift)
					.setSize(16 * 8, 16);

			return o;
		};
		builder.widget(gen.apply(() -> this.voltage, a -> {
			this.voltage = a;
		}).setPos(16, 16 + yshift).dynamicTooltip(() -> {
			String a = GT_Values.VN[0], b;
			long v = 8;
			for (int i = 0; i < GT_Values.V.length - 1; i++) {

				if (voltage > GT_Values.V[i]) {
					a = GT_Values.VN[i + 1];
					v = GT_Values.V[i + 1];
				}
			}
			b = Math.floor((100 * (float) voltage / v)) + "%";

			return ImmutableList.of(StatCollector.translateToLocal("proghatches.eu.interface.tooltip.volt.0"),
					StatCollector.translateToLocalFormatted("proghatches.eu.interface.tooltip.volt.1", a, b));
		}

		));
		builder.widget(gen
				.apply(() -> this.expectedamp,
						a -> this.expectedamp = Math.min(a,
								Integer.MAX_VALUE/* stacksize limit */))
				.setPos(16, 16 + 16 + 2 + yshift)
				.addTooltip(StatCollector.translateToLocal("proghatches.eu.interface.tooltip.amp")));
		builder.widget(new TextWidget("V:").setPos(8, 16 + yshift));
		builder.widget(new TextWidget("A:").setPos(8, 16 + 16 + 2 + yshift));
		// builder.bindPlayerInventory(buildContext.getPlayer());

		return builder;
	}

	private void onChange() {
		IEUManager e;
		try {
			e = this.getProxy().getGrid().getCache(IEUManager.class);
			e.removeNode(this.getProxy().getNode(), this);
			e.addNode(this.getProxy().getNode(), this);

		} catch (GridAccessException e1) {
		}

		initTokenTemplate();
		if (this.isP2POut()) {
			PartEUP2PInterface p2p = this.getInput();
			if (p2p != null) {
				voltage = p2p.voltage;
				inputid = p2p.id;// id is unique, so do not modify it
				expectedamp = p2p.expectedamp;
			}
		} else {

			try {
				getOutputs().forEach(s -> s.onChange());
			} catch (GridAccessException e1) {
				e1.printStackTrace();
			}

		}
		postEvent();

	}

	UUID inputid = zero;

	private void initTokenTemplate() {
		token = Optional.of(new ItemStack(MyMod.eu_token, 1, 1)).map(s -> {
			s.stackTagCompound = new NBTTagCompound();
			ProghatchesUtil.ser(s.stackTagCompound, isOutput() ? inputid : id, "EUFI");
			s.stackTagCompound.setLong("voltage", voltage);

			return s;
		}).get();

		blank_token = Optional.of(new ItemStack(MyMod.eu_token, 1, 0)).map(s -> {
			s.stackTagCompound = new NBTTagCompound();
			// ProghatchesUtil.ser(s.stackTagCompound, id, "EUFI");
			s.stackTagCompound.setLong("voltage", voltage);

			return s;
		}).get();

	}

	private boolean postEvent() {
		try {
			this.getProxy().getGrid()
					.postEvent(new MENetworkCraftingPatternChange(this, getGridNode(ForgeDirection.UNKNOWN)));
			return true;
		} catch (GridAccessException ignored) {
			return false;
		}
	}

	public ModularWindow createWindowOut(UIBuildContext buildContext, int yshift) {
		ModularWindow.Builder builder = ModularWindow.builder(176, 107);
		builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);

		return addWidgetsOut(builder, yshift).build();
	}

	@Override
	public TickingRequest getTickingRequest(final IGridNode node) {
		return new TickingRequest(1, 1, false, false);
	}

	@Override
	public long getVoltage() {
		return voltage;
	}

	TileEntity getTarget(ForgeDirection side) {
		TileEntity te = this.getTile();
		return te.getWorldObj().getTileEntity(te.xCoord + side.offsetX, te.yCoord + side.offsetY,
				te.zCoord + side.offsetZ);

	}

	private long injectEnergy(IEnergyConnected te, ForgeDirection oppositeSide, long aVoltage, long aAmperage) {

		return te.injectEnergyUnits(oppositeSide, aVoltage, aAmperage);

	}

	/* modified from PartP2PGT5Power */
	private long doOutput(long aVoltage, long aAmperage, ForgeDirection side) {

		{
			if (data != null) {

				return data.doOutput(aVoltage, aAmperage);
			}

			TileEntity te = this.getTarget(side);
			if (te == null) {
				return 0L;
			} else {
				ForgeDirection oppositeSide = side.getOpposite();
				if (te instanceof IEnergyConnected) {
					return injectEnergy((IEnergyConnected) te, oppositeSide, aVoltage, aAmperage);
				} else {
					if (te instanceof IEnergySink) {
						if (((IEnergySink) te).acceptsEnergyFrom(this.getTile(), oppositeSide)) {
							long rUsedAmperes = 0L;
							while (aAmperage > rUsedAmperes && ((IEnergySink) te).getDemandedEnergy() > 0.0D
									&& ((IEnergySink) te).injectEnergy(oppositeSide, (double) aVoltage,
											(double) aVoltage) < (double) aVoltage)
								++rUsedAmperes;

							return rUsedAmperes;
						}
					} else if (GregTech_API.mOutputRF && te instanceof IEnergyReceiver) {
						int rfOut = (int) (aVoltage * (long) GregTech_API.mEUtoRF / 100L);
						if (((IEnergyReceiver) te).receiveEnergy(oppositeSide, rfOut, true) == rfOut) {
							((IEnergyReceiver) te).receiveEnergy(oppositeSide, rfOut, false);
							return 1L;
						}

						if (GregTech_API.mRFExplosions && GregTech_API.sMachineExplosions
								&& ((IEnergyReceiver) te).getMaxEnergyStored(oppositeSide) < rfOut * 600
								&& rfOut > 32 * GregTech_API.mEUtoRF / 100) {
							float tStrength = (long) rfOut < GT_Values.V[0] ? 1.0F
									: ((long) rfOut < GT_Values.V[1] ? 2.0F
											: ((long) rfOut < GT_Values.V[2] ? 3.0F
													: ((long) rfOut < GT_Values.V[3] ? 4.0F
															: ((long) rfOut < GT_Values.V[4] ? 5.0F
																	: ((long) rfOut < GT_Values.V[4] * 2L ? 6.0F
																			: ((long) rfOut < GT_Values.V[5] ? 7.0F
																					: ((long) rfOut < GT_Values.V[6]
																							? 8.0F
																							: ((long) rfOut < GT_Values.V[7]
																									? 9.0F
																									: 10.0F))))))));
							int tX = te.xCoord;
							int tY = te.yCoord;
							int tZ = te.zCoord;
							World tWorld = te.getWorldObj();
							GT_Utility.sendSoundToPlayers(tWorld, GregTech_API.sSoundList.get(209), 1.0F, -1.0F, tX, tY,
									tZ);
							tWorld.setBlock(tX, tY, tZ, Blocks.air);
							if (GregTech_API.sMachineExplosions) {
								tWorld.createExplosion(null, (double) tX + 0.5D, (double) tY + 0.5D, (double) tZ + 0.5D,
										tStrength, true);
							}
						}
					}

					return 0L;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public long doInject(long a, long v) {
		if (a == 0)
			return 0;
		if (updatev) {
			minv = maxv = v;
			updatev = false;
		} else {
			minv = Math.min(minv, v);
			maxv = Math.max(maxv, v);
		}
		long olda = a;

		alldone: if (!this.isP2POut()) {
			Collection<PartEUP2PInterface> coll = new ArrayList<>();

			try {
				this.getOutputs().forEach(coll::add);
			} catch (GridAccessException e) {
			}

			coll.add(this);
			// ArrayList<PartEUP2PInterface> con=new ArrayList<>(coll.size());

			ArrayList<PartEUP2PInterface> dead = new ArrayList<>();
			while (true) {
				for (PartEUP2PInterface face : coll) {// try to deliver fairly
					long consume = face.doOutput(v, Math.max(1, a / coll.size()), face.getSide());
					a -= consume;
					if (consume == 0)
						dead.add(face);
					injectedamp += consume;
					face.injectedamp_local += consume;
					if (a == 0)
						break alldone;

				}
				coll.removeAll(dead);
				if (coll.size() == 0)
					break alldone;
				dead.clear();
			}

		}

		// doOutput()

		return olda - a;
	}

	@Override
	public long expectedAmp() {
		return amp - accepted;
	}

	@Override
	public void accept(long a) {
		accepted += a;
	}

	long injectedamp;
	long injectedamp_local;
	long instantamp_local;

	@Override
	public void reset() {
		instantamp = injectedamp;
		averageamp = (injectedamp) / 32.0 + averageamp * 31 / 32;
		injectedamp = 0;
		instantamp_local = injectedamp_local;
		averageamp_local = (injectedamp_local) / 32.0 + averageamp_local * 31 / 32;
		injectedamp_local = 0;

		accepted = 0;

	}

	@Override
	public String getCustomName() {
		if (super.getCustomName() == null) {
			return "EU Interface";
		}
		return super.getCustomName();
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public void setCustomName(String name) {
		// setCustomName(name);
		if (super.getCustomName() == null) {
			setCustomName("EU Interface");
		}
	}

	@Override
	public UUID getUUID() {
		if (this.isOutput())
			return zero;
		return this.id;
	}

	@Override
	public void refund(long amp) {
		this.amp -= amp;

	}

	@Override
	public ItemStack getCrafterIcon() {
		return new ItemStack(MyMod.euinterface_p2p);
	}

	InterfaceP2PEUData data;

	public PartEUP2PInterface markCoverData(InterfaceP2PEUData interfaceP2PEUData) {
		data = interfaceP2PEUData;
		return this;
	}

	@Override
	public ItemStack getSelfRep() {
		// TODO Auto-generated method stub
		return new ItemStack(MyMod.euinterface_p2p);
	}

}
