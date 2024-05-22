package reobf.proghatches.gt.metatileentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;

import appeng.api.AEApi;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.render.TextureFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class ProviderChainer extends GT_MetaTileEntity_Hatch
		implements IPowerChannelState, IGridProxyable, ICircuitProvider {

	public ProviderChainer(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount) {
		super(aID, aName, aNameRegional, aTier, aInvSlotCount,
				reobf.proghatches.main.Config.get("PC", ImmutableMap.of())

		);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
	}

	private void updateValidGridProxySides() {
		if (disabled) {
			getProxy().setValidSides(EnumSet.noneOf(ForgeDirection.class));
			return;
		}
		getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

	}

	@Override
	public boolean isFacingValid(ForgeDirection facing) {

		return true;
	}

	@Override
	public void onFacingChange() {
		updateValidGridProxySides();
	}

	public ProviderChainer(String aName, int aTier, int aInvSlotCount, String[] aDescription,
			ITexture[][][] aTextures) {
		super(aName, aTier, aInvSlotCount, aDescription, aTextures);

	}

	// item returned in ae tick will not be recognized, delay to the next
	// onPostTick() call

	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {

		super.onPostTick(aBaseMetaTileEntity, aTick);

		// if(this.isActive()&&this.isPowered()){}
	}

	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

		super.onFirstTick(aBaseMetaTileEntity);
		getProxy().onReady();
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		return getProxy().getNode();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {

		return AECableType.DENSE;
	}

	@Override
	public void securityBreak() {

	}

	@Override
	public boolean isAccessAllowed(EntityPlayer aPlayer) {

		return true;
	}

	/*
	 * @Override public IInventory getPatterns() { // TODO Auto-generated method
	 * stub return null; }
	 */

	/*
	 * @Override public String getCustomName() { // TODO Auto-generated method
	 * stub return null; }
	 * 
	 * @Override public boolean hasCustomName() { // TODO Auto-generated method
	 * stub return false; }
	 * 
	 * @Override public void setCustomName(String name) { // TODO Auto-generated
	 * method stub }
	 */
	AENetworkProxy gridProxy;

	@Override
	public AENetworkProxy getProxy() {

		if (gridProxy == null) {
			gridProxy = new AENetworkProxy(this, "proxy", ItemList.Hatch_CraftingInput_Bus_ME.get(1), true);
			gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
			updateValidGridProxySides();
			if (getBaseMetaTileEntity().getWorld() != null)
				gridProxy.setOwner(getBaseMetaTileEntity().getWorld()
						.getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
		}

		return this.gridProxy;
	}

	@Override
	public DimensionalCoord getLocation() {
		return new DimensionalCoord(getBaseMetaTileEntity().getWorld(), getBaseMetaTileEntity().getXCoord(),
				getBaseMetaTileEntity().getYCoord(), getBaseMetaTileEntity().getZCoord());
	}

	@Override
	public void gridChanged() {

	}

	private IStorageGrid getStorageGrid() {
		try {
			return this.getProxy().getGrid().getCache(IStorageGrid.class);
		} catch (GridAccessException e) {
			return null;
		}
	}

	private final static Item encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem()
			.orNull();
	boolean patternDirty;

	// no need to save to NBT
	ItemStack[] snapshot;

	private void doSnapshot() {
		snapshot = mInventory.clone();
		for (int i = 0; i < snapshot.length; i++) {
			snapshot[i] = Optional.ofNullable(snapshot[i]).map(ItemStack::copy).orElse(null);
		}
	}

	public static class CircuitProviderPatternDetial implements ICraftingPatternDetails {
		@Nonnull
		final public ItemStack out;
		@Nonnull
		final int hash;

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof CircuitProviderPatternDetial)) {
				return false;
			}
			return ItemStack.areItemStacksEqual(out, ((CircuitProviderPatternDetial) obj).out);
		}

		@Override
		public int hashCode() {
			if (out == null)
				return 0;
			return hash;
			/*
			 * Optional.ofNullable(out.stackTagCompound).map(Object::hashCode).
			 * orElse(0)^
			 * Integer.valueOf(Item.getIdFromItem(out.getItem())).hashCode()^
			 * Integer.valueOf(out.getItemDamage());
			 */
		}

		public CircuitProviderPatternDetial(@Nonnull ItemStack o) {
			if (o == null)
				throw new IllegalArgumentException("null");
			this.out = o;
			hash = AEItemStack.create(out).hashCode() ^ 0x1234abcd;
			/*
			 * if(out ==null){ Thread.dumpStack();
			 * 
			 * System.exit(0);}
			 */
		}

		@Override
		public ItemStack getPattern() {
			return Optional.of(new ItemStack(MyMod.fakepattern)).map(s -> {

				s.stackTagCompound = out.writeToNBT(new NBTTagCompound());

				return s;
			}).get();

		}

		@Override
		public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
			// glad I don't have to implement this
			throw new IllegalStateException("workbench crafting");
		}

		@Override
		public boolean isCraftable() {

			return false;
		}

		/**
		 * if return zero-sized input, ae2fc coremodhooks will crash so use one
		 * zero-stacksized item to workaround
		 */
		@Override
		public IAEItemStack[] getInputs() {

			return new IAEItemStack[] { AEApi.instance().storage().createItemStack(new ItemStack(Items.apple, 0)) };
		}

		@Override
		public IAEItemStack[] getCondensedInputs() {
			return getInputs();
		}

		@Override
		public IAEItemStack[] getCondensedOutputs() {

			return new IAEItemStack[] { AEApi.instance().storage().createItemStack(out) };
		}

		@Override
		public IAEItemStack[] getOutputs() {

			return getCondensedOutputs();
		}

		@Override
		public boolean canSubstitute() {

			return false;
		}

		@Override
		public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
			return out;
		}

		@Override
		public int getPriority() {

			return Integer.MIN_VALUE;
		}

		@Override
		public void setPriority(int priority) {

		}

	}

	@Override
	public boolean isPowered() {
		return getProxy() != null && getProxy().isPowered();
	}

	@Override
	public boolean isActive() {
		return getProxy() != null && getProxy().isActive();
	}

	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new ProviderChainer(mName, mTier, 0, mDescriptionArray, mTextures);
	}

	@Override
	public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
			int colorIndex, boolean aActive, boolean redstoneLevel) {

		return super.getTexture(aBaseMetaTileEntity, side, aFacing, colorIndex, aActive, redstoneLevel);

	}

	@Override
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {

		return new ITexture[] { aBaseTexture, TextureFactory.builder()
				.setFromBlock(MyMod.iohub, BlockIOHub.magicNO_provider_in_active_overlay).glow().build() };
	}

	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {

		return new ITexture[] { aBaseTexture, TextureFactory.of(MyMod.iohub, BlockIOHub.magicNO_provider_in_overlay) };
	}

	@Override
	public boolean useModularUI() {

		return false;
	}

	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		// GT_UIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
		return true;
	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(this.mInventory, 0, 1);
		if (inventoryHandler == null)
			return;

		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 1).startFromSlot(0).endAtSlot(0)
				.background(new IDrawable[] { getGUITextureSet().getItemSlot() }).build().setPos(3, 3));
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {

		super.saveNBTData(aNBT);

		int[] count = new int[1];
		getProxy().writeToNBT(aNBT);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadNBTData(NBTTagCompound aNBT) {

		super.loadNBTData(aNBT);

		getProxy().readFromNBT(aNBT);
		;
	}

	boolean disabled;

	public void disable() {
		disabled = true;
		updateValidGridProxySides();
	}

	@Override
	public boolean checkLoop(HashSet<Object> blacklist) {
		boolean[] test = new boolean[1];
		if (!ICircuitProvider.super.checkLoop(blacklist))
			return false;

		return !(forEachMachines(s -> test[0] = test[0] || (!s.checkLoop(blacklist)), true) && test[0]);

	}

	@Override
	public Collection<ItemStack> getCircuit() {
		if (this.isActive() && this.isPowered()) {
		} else {
			return ImmutableList.of();
		}

		ArrayList<ItemStack> list = new ArrayList<>();
		if (forEachMachines(s -> list.addAll(s.getCircuit()), true)) {
			return list;
		}
		;

		return ImmutableList.of();
	}

	@SuppressWarnings("unchecked")
	private boolean forEachMachines(Consumer<ICircuitProvider> forEach, boolean other) {
		try {

			IGrid g = this.getProxy().getGrid();
			g.getMachinesClasses().forEach(s -> {

				if (ICircuitProvider.class.isAssignableFrom(s)
						&& ((s != this.getClass() && other) || (s == this.getClass() && !other))) {

					g.getMachines(s).forEach(c -> forEach.accept((ICircuitProvider) c.getMachine()));
				}

			});

		} catch (GridAccessException e) {
			return false;
		}
		return true;

	}

	public void clearDirty() {
		patternDirty = false;
	};

	boolean shutdown;

	public boolean patternDirty() {
		if (this.isActive() && this.isPowered()) {
			if (shutdown) {
				shutdown = false;
				patternDirty = true;
				return patternDirty;
			}
		} else {
			if (!shutdown) {
				shutdown = true;
				patternDirty = true;
				return patternDirty;
			}
		}

		boolean[] test = new boolean[1];
		if (forEachMachines(s -> {
			test[0] = test[0] || s.patternDirty();
			s.clearDirty();
		}, true) && test[0]) {
			forEachMachines(s -> ((ProviderChainer) s).patternDirty = true, false);
		}
		;

		return patternDirty;
	}

}
