package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;

import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;

import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.modularui.IAddGregtechLogo;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_TooltipDataCache;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class DualInputHatch extends GT_MetaTileEntity_Hatch_InputBus
		implements IConfigurationCircuitSupport, IAddGregtechLogo, IAddUIWidgets, IDualInputHatch,
		IProgrammingCoverBlacklisted, IRecipeProcessingAwareDualHatch {

	static java.text.DecimalFormat format = new java.text.DecimalFormat("#,###");
	public boolean mMultiFluid;

	public DualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, String... optional) {
		this(id, name, nameRegional, tier, getSlots(tier) + 1, mMultiFluid, optional);

	}

	public DualInputHatch(int id, String name, String nameRegional, int tier, int slot, boolean mMultiFluid,
			String... optional) {

		super(id, name, nameRegional, tier, slot, (optional.length > 0 ? optional
				: /*
					 * defaultObj(
					 * 
					 * ArrayExt.of( "Item/Fluid Input for Multiblocks",
					 * "Contents are always separated with other bus/hatch",
					 * "Programming Cover function integrated", "Capacity: " +
					 * format.format((int) (4000 * Math.pow(2, tier)) /
					 * (mMultiFluid ? 4 : 1)) + "L" + (mMultiFluid ?
					 * " x4 types of fluid" : ""), Math.min(16, (1 + tier) *
					 * (tier + 1)) + "Slots", LangManager.translateToLocal(
					 * "programmable_hatches.addedby")), ArrayExt.of(
					 * "多方块机器的物品/流体输入", "总是与其它输入仓/输入总线隔离", "自带编程覆盖板功能", "容量: " +
					 * format.format((int) (4000 * Math.pow(2, tier) /
					 * (mMultiFluid ? 4 : 1))) + "L" + (mMultiFluid ? " x4种流体" :
					 * ""), Math.min(16, (1 + tier) * (tier + 1)) + "格",
					 * LangManager.translateToLocal(
					 * "programmable_hatches.addedby")))
					 */
				reobf.proghatches.main.Config.get("DH", ImmutableMap.of(

						"cap", format.format((int) (4000 * Math.pow(2, tier) / (mMultiFluid ? 4 : 1))), "mMultiFluid",
						mMultiFluid, "slots", Math.min(16, (1 + tier) * (tier + 1))

				))

		)

		);
		this.disableSort = true;
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, id));
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();
	}

	public void initTierBasedField() {

		// setInventorySlotContents(aIndex, aStack);

		if (mMultiFluid) {

			mStoredFluid = new ListeningFluidTank[] {

					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this)

			};

		} else {

			mStoredFluid = new ListeningFluidTank[] { new ListeningFluidTank((int) (4000 * Math.pow(2, mTier)), this) };

		}

	}

	public DualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
			boolean mMultiFluid) {
		super(mName, mTier, mDescriptionArray, mTextures);
		this.disableSort = true;
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();
	}

	public DualInputHatch(String aName, int aTier, int aSlots, String[] aDescription, ITexture[][][] aTextures,
			boolean mMultiFluid) {
		super(aName, aTier, aSlots, aDescription, aTextures);
		this.disableSort = true;
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();

	}

	public ListeningFluidTank[] mStoredFluid;

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		super.saveNBTData(aNBT);
		aNBT.setBoolean("program", program);
		aNBT.setBoolean("mMultiFluid", mMultiFluid);
		if (mStoredFluid != null) {
			for (int i = 0; i < mStoredFluid.length; i++) {
				if (mStoredFluid[i] != null)
					aNBT.setTag("mFluid" + i, mStoredFluid[i].writeToNBT(new NBTTagCompound()));
			}
		}
	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		super.loadNBTData(aNBT);
		program = aNBT.getBoolean("program");
		mMultiFluid = aNBT.getBoolean("mMultiFluid");
		if (mStoredFluid != null) {
			for (int i = 0; i < mStoredFluid.length; i++) {
				if (aNBT.hasKey("mFluid" + i)) {
					mStoredFluid[i].readFromNBT(aNBT.getCompoundTag("mFluid" + i));
				}
			}
		}
	}

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new DualInputHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid);
	}

	private Widget createButton(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
			Supplier<GT_TooltipDataCache.TooltipData> tooltipDataSupplier, int offset) {
		return new CycleButtonWidget().setToggle(getter, setter).setStaticTexture(picture)
				.setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(7 + offset * 18, 62).setSize(18, 18).setGTTooltip(tooltipDataSupplier);
	}

	private Widget createButtonForbidden(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
			Supplier<GT_TooltipDataCache.TooltipData> tooltipDataSupplier, int offset) {
		return new CycleButtonWidget() {

			public com.gtnewhorizons.modularui.api.widget.Interactable.ClickResult onClick(int buttonId,
					boolean doubleClick) {
				return ClickResult.SUCCESS;
			};
		}.setTextureGetter(s -> IDrawable.EMPTY).setToggle(getter, setter)
				// .setStaticTexture(picture)

				.setBackground(GT_UITextures.BUTTON_STANDARD_PRESSED, picture, GT_UITextures.OVERLAY_BUTTON_FORBIDDEN)

				.setTooltipShowUpDelay(TOOLTIP_DELAY).setPos(7 + offset * 18, 62).setSize(18, 18)
				.setGTTooltip(tooltipDataSupplier)
		// .setfo(GT_UITextures.OVERLAY_BUTTON_FORBIDDEN)
		;
	}

	ItemStackHandler bridge;// = new ItemStackHandler(mInventory);

	@Override
	public ItemStackHandler getInventoryHandler() {
		if (bridge == null)
			bridge = new InventoryItemHandler(mInventory, this);
		return bridge;
	}

	public int moveButtons() {
		return 1;

	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {

		if (moveButtons() == 1) {
			super.addUIWidgets(builder, buildContext);
		} else
			addUIWidgets0(builder, buildContext);
		builder.widget(createButton(() -> !disableFilter, val -> {
			disableFilter = !val;
			updateSlots();
		}, GT_UITextures.OVERLAY_BUTTON_INVERT_FILTER,
				() -> mTooltipCache.getData("programmable_hatches.gt.filtermode"), 0).setPos(7,
						62 - moveButtons() * 18));

		builder.widget(createButton(() -> program, val -> {
			program = val;
			updateSlots();
		}, GT_UITextures.OVERLAY_SLOT_CIRCUIT, () -> mTooltipCache.getData("programmable_hatches.gt.program"), 0)
				.setPos(7, 62 - 18 - moveButtons() * 18));

		builder.widget(createButtonForbidden(() -> true, val -> {
			;
			updateSlots();
		}, GT_UITextures.OVERLAY_BUTTON_INPUT_SEPARATION_ON_DISABLED,
				() -> mTooltipCache.getData("programmable_hatches.gt.separate"), 1).setPos(7 + 1 * 18,
						62 - moveButtons() * 18));
		Pos2d[] p = new Pos2d[] { new Pos2d(79 + 18 * 1, 34), new Pos2d(70 + 18 * 2, 25), new Pos2d(61 + 18 * 3, 16),
				new Pos2d(52 + 18 * 4, 7) };
		Pos2d position = p[Math.min(3, slotTierOverride(this.mTier))];

		if (mStoredFluid.length > 1) {
			position = new Pos2d(position.getX(), p[3].getY());

		}

		{
			Pos2d position0 = new Pos2d(0, 0);
			//int countInRow = 0;
			final Scrollable scrollable = new Scrollable().setVerticalScroll();
			for (int i = 0; i < mStoredFluid.length; i++) {
				position0 = new Pos2d((i % fluidSlotsPerRow()) * 18, (i / fluidSlotsPerRow()) * 18);
				scrollable.widget(new FluidSlotWidget(mStoredFluid[i]).setBackground(ModularUITextures.FLUID_SLOT)
						.setPos(position0));

			}

			builder.widget(scrollable.setSize(18 * fluidSlotsPerRow(), 18 * 4).setPos(position));
		} /*
			 * for (int i = 0; i < mStoredFluid.length; i++) { builder.widget(
			 * new
			 * FluidSlotWidget(mStoredFluid[i]).setBackground(ModularUITextures.
			 * FLUID_SLOT) .setPos(position)); position=new
			 * Pos2d(position.getX(),position.getY()).add(0, 18); }
			 */

	}

	@Override
	public boolean justUpdated() {

		return false;
	}

	@SuppressWarnings("rawtypes")
	final static Iterator emptyItr = new Iterator() {

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new NoSuchElementException();
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<? extends IDualInputInventory> inventories() {
		if (theInv.isEmpty())
			return emptyItr;
		return Arrays.asList(theInv).iterator();
	}

	@Override
	public Optional<IDualInputInventory> getFirstNonEmptyInventory() {
		boolean empty = true;

		for (FluidStack f : theInv.getFluidInputs()) {
			if (f != null && f.amount > 0)
				empty = false;
			break;
		}
		for (ItemStack f : theInv.getItemInputs()) {
			if (f != null && f.stackSize > 0)
				empty = false;
			break;
		}

		if (empty)
			return Optional.empty();
		return Optional.of(theInv);
	}

	@Override
	public boolean supportsFluids() {

		return true;
	}

	private ItemStack[] dualItem() {
		return filterStack.apply(mInventory);
	}

	private FluidStack[] dualFluid() {
		return asFluidStack.apply(mStoredFluid);
	}

	public DualInv theInv = new DualInv();

	public class DualInv implements IDualInputInventory {

		public boolean isEmpty() {

			for (FluidTank f : mStoredFluid) {
				if (f.getFluidAmount() > 0) {
					return false;
				}
			}
			for (ItemStack i : getItemInputs()) {

				if (i != null && i.stackSize > 0) {
					return false;
				}
			}
			return true;
		}

		@Override
		public ItemStack[] getItemInputs() {

			return dualItem();
		}

		@Override
		public FluidStack[] getFluidInputs() {

			return dualFluid();
		}
	}

	Function<FluidTank[], FluidStack[]> asFluidStack = (s) -> Arrays.stream(s).map(FluidTank::getFluid)
			.filter(a -> a != null && a.amount > 0).toArray(FluidStack[]::new);
	Function<ItemStack[], ItemStack[]> filterStack = (s) -> Arrays.stream(s).filter(a -> a != null)
			.toArray(ItemStack[]::new);

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		FluidTankInfo[] FTI = new FluidTankInfo[mStoredFluid.length];
		for (int i = 0; i < mStoredFluid.length; i++) {
			FTI[i] = new FluidTankInfo(mStoredFluid[i]);
		}
		return FTI;
	}

	///////////
	@Override
	public boolean displaysStackSize() {
		return true;
	}

	public FluidStack[] getStoredFluid() {
		return asFluidStack.apply(mStoredFluid);
	}

	/*
	 * @Override public ITexture[] getTexturesActive(ITexture aBaseTexture) {
	 * return new ITexture[] { aBaseTexture,
	 * TextureFactory.of(OVERLAY_INPUT_HATCH_2x2) }; }
	 * 
	 * @Override public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
	 * return new ITexture[] { aBaseTexture,
	 * TextureFactory.of(OVERLAY_INPUT_HATCH_2x2) }; }
	 */
	public int getMaxType() {
		return mStoredFluid.length;
	}

	@Override
	public FluidStack getFluid() {
		for (FluidTank tFluid : mStoredFluid) {
			if (tFluid != null && tFluid.getFluidAmount() > 0)
				return tFluid.getFluid();
		}
		return null;
	}

	public FluidStack getFluid(int aSlot) {
		if (aSlot < 0 || aSlot >= getMaxType())
			return null;
		return mStoredFluid[aSlot].getFluid();
	}

	@Override
	public int getFluidAmount() {
		if (getFluid() != null) {
			return getFluid().amount;
		}
		return 0;
	}

	@Override
	public int getCapacity() {
		return mStoredFluid[0].getCapacity();
	}

	public int getFirstEmptySlot() {
		for (int i = 0; i < mStoredFluid.length; i++) {
			if (mStoredFluid[i].getFluidAmount() == 0)
				return i;
		}
		return -1;
	}

	public boolean hasFluid(FluidStack aFluid) {
		if (aFluid == null)
			return false;
		for (FluidTank tFluid : mStoredFluid) {
			if (aFluid.isFluidEqual(tFluid.getFluid()))
				return true;
		}
		return false;
	}

	public int getFluidSlot(FluidStack tFluid) {
		if (tFluid == null)
			return -1;
		for (int i = 0; i < mStoredFluid.length; i++) {
			if (tFluid.equals(mStoredFluid[i].getFluid()))
				return i;
		}
		return -1;
	}

	public int getFluidAmount(FluidStack tFluid) {
		int tSlot = getFluidSlot(tFluid);
		if (tSlot != -1) {
			return mStoredFluid[tSlot].getFluid().amount;
		}
		return 0;
	}

	public void setFluid(FluidStack aFluid, int aSlot) {
		if (aSlot < 0 || aSlot >= getMaxType())
			return;
		mStoredFluid[aSlot].setFluid(aFluid);

	}

	public void addFluid(FluidStack aFluid, int aSlot) {
		if (aSlot < 0 || aSlot >= getMaxType())
			return;
		if (aFluid.equals(mStoredFluid[aSlot].getFluid()))
			mStoredFluid[aSlot].fill(aFluid, true);
		if (mStoredFluid[aSlot].getFluid() == null)
			mStoredFluid[aSlot].setFluid(aFluid.copy());
	}

	@Override
	public boolean canTankBeFilled() {
		return true;
	}

	@Override
	public boolean canTankBeEmptied() {
		return true;
	};

	@Override
	public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		if (aBaseMetaTileEntity.isServerSide()) {
			mFluid = getFluid();
		}
		super.onPreTick(aBaseMetaTileEntity, aTick);
	}

	public void program() {
		for (int slot : this.getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal())) {
			ItemStack is = getStackInSlot(slot);
			if (is == null)
				continue;
			if (is.getItem() != MyMod.progcircuit)
				continue;
			if (decrStackSize(slot, 64).stackSize == 0) {
				continue;
			}
			markDirty();
			setInventorySlotContents(getCircuitSlot(), ItemProgrammingCircuit.getCircuit(is).orElse(null));
		}
	}

	@Override
	public int getCircuitSlot() {
		return getSlots(slotTierOverride(mTier));
	}

	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
		/*
		 * this.mInventory[this.getCircuitSlot() ]=new ItemStack(Items.apple);
		 */
		super.onFirstTick(aBaseMetaTileEntity);
	}

	boolean program = true;// default: ON

	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.onPostTick(aBaseMetaTileEntity, aTick);
		if (aBaseMetaTileEntity.getWorld().isRemote)
			return;

		if (program)
			program();

	}

	@Override
	public void setInventorySlotContents(int aIndex, ItemStack aStack) {
		super.setInventorySlotContents(aIndex, aStack);
		if (program)
			program();
	}

	public void onFill() {
	}

	@Override
	public int fill(FluidStack aFluid, boolean doFill) {
		if (aFluid == null || aFluid.getFluid().getID() <= 0 || aFluid.amount <= 0 || !canTankBeFilled()
				|| !isFluidInputAllowed(aFluid))
			return 0;
		if (!hasFluid(aFluid) && getFirstEmptySlot() != -1) {
			int tFilled = Math.min(aFluid.amount, mStoredFluid[0].getCapacity());
			if (doFill) {
				FluidStack tFluid = aFluid.copy();
				tFluid.amount = tFilled;
				addFluid(tFluid, getFirstEmptySlot());
				getBaseMetaTileEntity().markDirty();
			}
			return tFilled;
		}
		if (hasFluid(aFluid)) {
			int tLeft = mStoredFluid[0].getCapacity() - getFluidAmount(aFluid);
			int tFilled = Math.min(tLeft, aFluid.amount);
			if (doFill) {
				FluidStack tFluid = aFluid.copy();
				tFluid.amount = tFilled;
				addFluid(tFluid, getFluidSlot(tFluid));
				getBaseMetaTileEntity().markDirty();
			}
			return tFilled;
		}
		return 0;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (getFluid() == null || !canTankBeEmptied())
			return null;
		if (getFluid().amount <= 0 && isFluidChangingAllowed()) {
			setFluid(null, getFluidSlot(getFluid()));
			getBaseMetaTileEntity().markDirty();
			return null;
		}
		FluidStack tRemove = getFluid().copy();
		tRemove.amount = Math.min(maxDrain, tRemove.amount);
		if (doDrain) {
			getFluid().amount -= tRemove.amount;
			getBaseMetaTileEntity().markDirty();
		}
		if (getFluid() == null || getFluid().amount <= 0 && isFluidChangingAllowed()) {
			setFluid(null, getFluidSlot(getFluid()));
			getBaseMetaTileEntity().markDirty();
		}
		return tRemove;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack aFluid, boolean doDrain) {
		if (aFluid == null || !hasFluid(aFluid))
			return null;
		FluidStack tStored = mStoredFluid[getFluidSlot(aFluid)].getFluid();
		if (tStored.amount <= 0 && isFluidChangingAllowed()) {
			setFluid(null, getFluidSlot(tStored));
			getBaseMetaTileEntity().markDirty();
			return null;
		}
		FluidStack tRemove = tStored.copy();
		tRemove.amount = Math.min(aFluid.amount, tRemove.amount);
		if (doDrain) {
			tStored.amount -= tRemove.amount;
			getBaseMetaTileEntity().markDirty();
		}
		if (tStored.amount <= 0 && isFluidChangingAllowed()) {
			setFluid(null, getFluidSlot(tStored));
			getBaseMetaTileEntity().markDirty();
		}
		return tRemove;
	}

	@Override
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {
		if (mMultiFluid)
			return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_INPUT_HATCH_2x2) };
		return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN) };

	}

	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		if (mMultiFluid)
			return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_INPUT_HATCH_2x2) };
		return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN) };

	}

	@Override
	public boolean isFluidInputAllowed(FluidStack aFluid) {

		if (disableFilter)
			return true;
		return versionInsensitiveContainsInput(aFluid);
	}

	/*
	 * @Override public void onScrewdriverRightClick(ForgeDirection side,
	 * EntityPlayer aPlayer, float aX, float aY, float aZ) { boolean
	 * prev=disableFilter; super.onScrewdriverRightClick(side, aPlayer, aX, aY,
	 * aZ); if(prev==true&&disableFilter==false){ GT_Utility
	 * .sendChatToPlayer(aPlayer, defaultName(
	 * "Filter mode of this hatch might not work well", "过滤模式可能无法正常生效") ); } }
	 */

	private static HashMap<Class<?>, MethodHandle> cache0 = new HashMap<>();
	private static HashMap<Class<?>, MethodHandle> cache1 = new HashMap<>();

	/**
	 * 2.4.0 compat no longer support 2.4.0 just leave it unchanged
	 */
	public boolean versionInsensitiveContainsInput(Object o) {

		MethodHandle mh = cache0.get(o.getClass());
		if (mh != null) {
			try {
				Object map;
				return ((map = cache1.get(o.getClass()).invoke(this)) == null) || // this.mRecipeMap==null
																					// ||
						(boolean) mh.invoke(map, o); // this.mRecipeMap.containsInput(0)
			} catch (Throwable e) {
				throw new RuntimeException("failed to access mRecipeMap", e);
			}

		}

		try {
			Class<?> recipeMapClass = this.getClass().getField("mRecipeMap").getType();
			MethodHandle mhr = MethodHandles.lookup().findVirtual(recipeMapClass, "containsInput",
					MethodType.methodType(boolean.class, o.getClass()));
			cache0.put(o.getClass(), mhr);
			cache1.put(o.getClass(), MethodHandles.lookup().findGetter(this.getClass(), "mRecipeMap", recipeMapClass));
			return versionInsensitiveContainsInput(o);
		} catch (Throwable e) {
			throw new RuntimeException("cannot get mRecipeMap", e);
		}

	}

	public void setFilter(RecipeMap<?> recipemap) {

		this.mRecipeMap = recipemap;

	}

	@Override
	public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		if (!(aPlayer instanceof EntityPlayerMP))
			return;

		ItemStack dataStick = aPlayer.inventory.getCurrentItem();
		if (!ItemList.Tool_DataStick.isStackEqual(dataStick, false, true))
			return;

		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("type", "ProgHatchesDualInput");
		tag.setInteger("x", aBaseMetaTileEntity.getXCoord());
		tag.setInteger("y", aBaseMetaTileEntity.getYCoord());
		tag.setInteger("z", aBaseMetaTileEntity.getZCoord());

		dataStick.stackTagCompound = tag;
		dataStick.setStackDisplayName("ProgHatches Dual Input Hatch Link Data Stick (" + aBaseMetaTileEntity.getXCoord()
				+ ", " + aBaseMetaTileEntity.getYCoord() + ", " + aBaseMetaTileEntity.getZCoord() + ")");
		aPlayer.addChatMessage(new ChatComponentText("Saved Link Data to Data Stick"));
	}

	public int slotTierOverride(int mTier) {
		return mTier;
	}

	private void addUIWidgets0(ModularWindow.Builder builder, UIBuildContext buildContext) {
		// buildContext.addCloseListener(() -> uiButtonCount = 0);
		// addSortStacksButton(builder);
		// addOneStackLimitButton(builder);
		switch (slotTierOverride(mTier)) {
		case 0:
			getBaseMetaTileEntity().add1by1Slot(builder);
			break;
		case 1:
			getBaseMetaTileEntity().add2by2Slots(builder);
			break;
		case 2:
			getBaseMetaTileEntity().add3by3Slots(builder);
			break;
		default:
			getBaseMetaTileEntity().add4by4Slots(builder);
		}
	}

	public int fluidSlotsPerRow() {
		return 1;
	}

	@Override
	public void startRecipeProcessing() {

	}

	@Override
	public void updateSlots() {
		if (this.getBaseMetaTileEntity().isServerSide()) {
			for (int i = 0; i < getMaxType(); i++) {
				if (mStoredFluid[i].getFluid() != null && mStoredFluid[i].getFluidAmount() <= 0) {
					mStoredFluid[i].setFluid(null);

				}
			}
		}
		super.updateSlots();
	}

	@Override
	public CheckRecipeResult endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
		this.markDirty();
		updateSlots();
		return CheckRecipeResultRegistry.SUCCESSFUL;
	}

}
