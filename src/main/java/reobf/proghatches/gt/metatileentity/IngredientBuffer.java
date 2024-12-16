package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.FLUID_TRANSFER_TOOLTIP;
import static gregtech.api.metatileentity.BaseTileEntity.ITEM_TRANSFER_TOOLTIP;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.util.GTUtility.moveMultipleItemStacks;
import java.util.Arrays;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.common.fluid.FluidStackTank;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.gui.modularui.GUITextureSet;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTETieredMachineBlock;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.main.registration.Registration;

public class IngredientBuffer extends MTETieredMachineBlock implements IAddUIWidgets {

	public static int T0 = 3;
	public static int T1 = 5;
	public static int[][] tiers = { { 0, T0 }, { 1, T1 } };

	public static int get(int tier, int ift0, int ift1) {

		if (tier == T0) {
			return ift0;
		}
		if (tier == T1) {
			return ift1;
		}
		// throw new RuntimeException();
		return ift0;
	}

	public IngredientBuffer(int aID, /* int aSlot, */ String aName, String aNameRegional,
			int aTier, /* int aInvSlotCount, */
			String[] aDescription) {
		super(aID, aName, aNameRegional, aTier, get(aTier, 9, 27),
				reobf.proghatches.main.Config.get("IB", ImmutableMap.of("item", get(aTier, 4, 7), "fluid",
						get(aTier, 9, 27), "cap", getCapacityPerTank(aTier, 0), "int format", "#,###"))

		);
		int aSlot = get(aTier, 4, 7);
		this.mStoredFluid = new FluidStack[aSlot];
		fluidTanks = new FluidStackTank[aSlot];
		mCapacityPer = getCapacityPerTank(aTier, aSlot);
		Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
	}

	public IngredientBuffer(String aName, int aSlot, int aTier, int aInvSlotCount, String[] aDescription,
			ITexture[][][] aTextures) {
		super(aName, aTier, aInvSlotCount, aDescription, aTextures);
		this.mStoredFluid = new FluidStack[aSlot];
		fluidTanks = new FluidStackTank[aSlot];
		mCapacityPer = getCapacityPerTank(aTier, aSlot);
		for (int i = 0; i < aSlot; i++) {
			final int index = i;
			fluidTanks[i] = new FluidStackTank(() -> mStoredFluid[index], fluid -> mStoredFluid[index] = fluid,
					mCapacityPer);
		}
	}

	private static int getCapacityPerTank(int aTier, int aSlot) {

		return get(aTier, 16000, 64000);
	}

	private FluidStack[] mStoredFluid;
	private FluidStackTank[] fluidTanks;
	public int mCapacityPer;

	@Override
	public boolean isFacingValid(ForgeDirection facing) {

		return true;
	}

	@Override
	public boolean isAccessAllowed(EntityPlayer aPlayer) {

		return true;
	}

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new IngredientBuffer(mName, getMaxType(), mTier, getSizeInventory(), mDescriptionArray, mTextures);
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		// TODO
		if (mStoredFluid != null) {
			for (int i = 0; i < mStoredFluid.length; i++) {
				if (mStoredFluid[i] != null)
					aNBT.setTag("mFluid" + i, mStoredFluid[i].writeToNBT(new NBTTagCompound()));
			}
		}
		aNBT.setBoolean("inputFromFront", inputFromFront);
		aNBT.setBoolean("mItemTransfer", mItemTransfer);
		aNBT.setBoolean("mFluidTransfer", mFluidTransfer);
		if (mFluid != null)
			aNBT.setTag("mFluid", mFluid.writeToNBT(new NBTTagCompound()));
		aNBT.setInteger("mMainFacing", mMainFacing.ordinal());
	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		if(aNBT.hasKey("x")==false)return;
		if (mStoredFluid != null) {
			for (int i = 0; i < mStoredFluid.length; i++) {
				if (aNBT.hasKey("mFluid" + i)) {
					mStoredFluid[i] = FluidStack.loadFluidStackFromNBT(aNBT.getCompoundTag("mFluid" + i));
				}
			}
		}
		inputFromFront = aNBT.getBoolean("inputFromFront");
		mItemTransfer = aNBT.getBoolean("mItemTransfer");
		mFluidTransfer = aNBT.getBoolean("mFluidTransfer");
		mFluid = FluidStack.loadFluidStackFromNBT(aNBT.getCompoundTag("mFluid"));
		mMainFacing = ForgeDirection.getOrientation(aNBT.getInteger("mMainFacing"));

	}

	@Override
	public void onValueUpdate(byte aValue) {
		mMainFacing = ForgeDirection.getOrientation(aValue);
	}

	@Override
	public byte getUpdateData() {
		return (byte) mMainFacing.ordinal();
	}

	public byte mMachineBlock = 0;

	private byte mTexturePage = 0;
	private byte actualTexture = 0;

	@Override
	public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
			int colorIndex, boolean aActive, boolean redstoneLevel) {
		int texturePointer = (byte) (actualTexture & 0x7F); // just to be sure,
															// from my testing
															// the 8th bit
															// cannot be
															// set clientside
		/*
		int textureIndex = texturePointer | (mTexturePage << 7); // Shift seven
																	// since one
																	// page is
																	// 128
			*/														// textures!
		try {
			if (side == aFacing) {
				return getTexturesOutput(Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1]);
			} else if (mMainFacing == side) {
				return getTexturesMainFacing(Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1]);

			} else {

				return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1] };

			}

		} catch (NullPointerException npe) {
			return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[0][0] };
		}
	}

	public FluidStack[] getStoredFluid() {
		return mStoredFluid;
	}

	public ITexture[] getTexturesOutput(ITexture aBaseTexture) {

		return new ITexture[] { aBaseTexture, TextureFactory.of(BlockIcons.OVERLAY_PIPE_IN) };
	}

	public ITexture[] getTexturesMainFacing(ITexture aBaseTexture) {
		ITexture tex = this.mTier == T0 ? TextureFactory.of(ItemAndBlockHolder.BUFFER)
				: TextureFactory.of(ItemAndBlockHolder.LARGE_BUFFER);
		return new ITexture[] { tex/* aBaseTexture *//*
														 * , TextureFactory.of(
														 * OVERLAY_INPUT_HATCH_2x2)
														 */ };
	}

	public int getMaxType() {
		return mStoredFluid.length;
	}

	@Override
	public FluidStack getFluid() {
		for (FluidStack tFluid : mStoredFluid) {
			if (tFluid != null && tFluid.amount > 0)
				return tFluid;
		}
		return null;
	}

	public FluidStack getFluid(int aSlot) {
		if (mStoredFluid == null || aSlot < 0 || aSlot >= getMaxType())
			return null;
		return mStoredFluid[aSlot];
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
		return mCapacityPer;
	}

	public int getFirstEmptySlot() {
		for (int i = 0; i < mStoredFluid.length; i++) {
			if (mStoredFluid[i] == null)
				return i;
		}
		return -1;
	}

	public boolean hasFluid(FluidStack aFluid) {
		if (aFluid == null)
			return false;
		for (FluidStack tFluid : mStoredFluid) {
			if (aFluid.isFluidEqual(tFluid))
				return true;
		}
		return false;
	}

	public int getFluidSlot(FluidStack tFluid) {
		if (tFluid == null)
			return -1;
		for (int i = 0; i < mStoredFluid.length; i++) {
			if (tFluid.equals(mStoredFluid[i]))
				return i;
		}
		return -1;
	}

	public int getFluidAmount(FluidStack tFluid) {
		int tSlot = getFluidSlot(tFluid);
		if (tSlot != -1) {
			return mStoredFluid[tSlot].amount;
		}
		return 0;
	}

	public void setFluid(FluidStack aFluid, int aSlot) {
		if (aSlot < 0 || aSlot >= getMaxType())
			return;
		mStoredFluid[aSlot] = aFluid;
	}

	public void addFluid(FluidStack aFluid, int aSlot) {
		if (aSlot < 0 || aSlot >= getMaxType())
			return;
		if (aFluid.equals(mStoredFluid[aSlot]))
			mStoredFluid[aSlot].amount += aFluid.amount;
		if (mStoredFluid[aSlot] == null)
			mStoredFluid[aSlot] = aFluid.copy();
	}

	@Override
	public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		if (aBaseMetaTileEntity.isServerSide()) {
			mFluid = getFluid();
		}
		super.onPreTick(aBaseMetaTileEntity, aTick);
	}

	@Override
	public int fill(FluidStack aFluid, boolean doFill) {
		if (aFluid == null || aFluid.getFluid().getID() <= 0 || aFluid.amount <= 0 || !canTankBeFilled()
				|| !isFluidInputAllowed(aFluid))
			return 0;
		if (!hasFluid(aFluid) && getFirstEmptySlot() != -1) {
			int tFilled = Math.min(aFluid.amount, mCapacityPer);
			if (doFill) {
				FluidStack tFluid = aFluid.copy();
				tFluid.amount = tFilled;
				addFluid(tFluid, getFirstEmptySlot());
				getBaseMetaTileEntity().markDirty();
			}
			return tFilled;
		}
		if (hasFluid(aFluid)) {
			int tLeft = mCapacityPer - getFluidAmount(aFluid);
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

	private boolean isFluidInputAllowed(FluidStack aFluid) {

		return true;
	}

	private boolean canTankBeFilled() {

		return true;
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

	private boolean canTankBeEmptied() {

		return true;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack aFluid, boolean doDrain) {
		if (aFluid == null || !hasFluid(aFluid))
			return null;
		FluidStack tStored = mStoredFluid[getFluidSlot(aFluid)];
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

	private boolean isFluidChangingAllowed() {

		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		FluidTankInfo[] FTI = new FluidTankInfo[getMaxType()];
		for (int i = 0; i < getMaxType(); i++) {
			FTI[i] = new FluidTankInfo(mStoredFluid[i], mCapacityPer);
		}
		return FTI;
	}

	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		if (aBaseMetaTileEntity.isServerSide() && mStoredFluid != null) {
			for (int i = 0; i < getMaxType(); i++) {
				if (mStoredFluid[i] != null && mStoredFluid[i].amount <= 0) {
					mStoredFluid[i] = null;
				}
			}
		}

		ForgeDirection face = aBaseMetaTileEntity.getFrontFacing();

		if (mFluidTransfer && face != ForgeDirection.UNKNOWN && aBaseMetaTileEntity.isServerSide()
				&& aBaseMetaTileEntity.isAllowedToWork()) {
			IFluidHandler tTileEntity = aBaseMetaTileEntity
					.getITankContainerAtSide(aBaseMetaTileEntity.getFrontFacing());
			if (tTileEntity != null) {

				for (FluidStack fluid : this.mStoredFluid) {
					if (fluid == null) {
						continue;
					}
					FluidStack tDrained = aBaseMetaTileEntity.drain(aBaseMetaTileEntity.getFrontFacing(), fluid, false);
					if (tDrained != null) {
						int tFilledAmount = tTileEntity.fill(aBaseMetaTileEntity.getBackFacing(), tDrained, false);
						if (tFilledAmount > 0) {
							FluidStack cp = fluid.copy();
							cp.amount = tFilledAmount;
							tTileEntity.fill(aBaseMetaTileEntity.getBackFacing(),
									aBaseMetaTileEntity.drain(aBaseMetaTileEntity.getFrontFacing(), cp, true), true);
						}
					}
				}
			}
		}
		if (mItemTransfer && face != ForgeDirection.UNKNOWN && aBaseMetaTileEntity.isServerSide()
				&& aBaseMetaTileEntity.isAllowedToWork() && (aTick & 0x7) == 0) {
			final IInventory tTileEntity = aBaseMetaTileEntity.getIInventoryAtSide(face);
			if (tTileEntity != null) {
				moveMultipleItemStacks(aBaseMetaTileEntity, tTileEntity, aBaseMetaTileEntity.getFrontFacing(),
						aBaseMetaTileEntity.getBackFacing(), null, false, (byte) 64, (byte) 1, (byte) 64, (byte) 1,
						mInventory.length);
				for (int i = 0; i < mInventory.length; i++)
					if (mInventory[i] != null && mInventory[i].stackSize <= 0)
						mInventory[i] = null;
			}

		}

		super.onPostTick(aBaseMetaTileEntity, aTick);
	}

	@Override
	public boolean isValidSlot(int aIndex) {
		return super.isValidSlot(aIndex);
	}

	//@Override
	public boolean useModularUI() {
		return true;
	}

	/*
	 * @Override public void addUIWidgets(ModularWindow.Builder builder,
	 * UIBuildContext buildContext) { final int SLOT_NUMBER = 4; final Pos2d[]
	 * positions = new Pos2d[] { new Pos2d(70, 25), new Pos2d(88, 25), new
	 * Pos2d(70, 43), new Pos2d(88, 43), };
	 * 
	 * for (int i = 0; i < SLOT_NUMBER; i++) { builder.widget( new
	 * FluidSlotWidget(fluidTanks[i]).setBackground(ModularUITextures.
	 * FLUID_SLOT) .setPos(positions[i])); } }
	 */
	@Override
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {

		return true;
	}

	@Override
	public ITexture[][][] getTextureSet(ITexture[] aTextures) {
		return new ITexture[0][0][0];
	}

	///

	FluidStack mFluid;

	public FluidStack getFillableStack() {
		return mFluid;
	}

	public FluidStack setFillableStack(FluidStack aFluid) {
		mFluid = aFluid;
		return mFluid;
	}

	/**
	 * If you override this and change the field returned, be sure to override
	 * {@link #isDrainableStackSeparate()} as well!
	 */
	public FluidStack getDrainableStack() {
		return mFluid;
	}

	public FluidStack setDrainableStack(FluidStack aFluid) {
		mFluid = aFluid;
		return mFluid;
	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		builder.widget(createFluidAutoOutputButton());
		builder.widget(createItemAutoOutputButton());
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(this.mInventory, 0,
				this.getSizeInventory());
		Scrollable sc = new Scrollable().setVerticalScroll();

		final IDrawable[] background = new IDrawable[] { GUITextureSet.DEFAULT.getItemSlot() };
		// final IDrawable[] special = new IDrawable[] {
		// GUITextureSet.DEFAULT.getItemSlot(),
		// GTUITextures.OVERLAY_SLOT_ARROW_ME };
		sc.widget(SlotGroup.ofItemHandler(inventoryHandler, 5)

				.startFromSlot(0).endAtSlot(this.getSizeInventory() - 1).background(background).build()

		);
		builder.widget(sc.setPos(3 + 4, 3 + 8).setSize(18 * 5, 18 * 2));
		sc = new Scrollable().setVerticalScroll();

		final IDrawable[] background0 = new IDrawable[] { GUITextureSet.DEFAULT.getFluidSlot() };
		// final IDrawable[] special0 = new IDrawable[] {
		// GUITextureSet.DEFAULT.getFluidSlot(),
		// GTUITextures.OVERLAY_SLOT_ARROW_ME };

		sc.widget(SlotGroup.ofFluidTanks(Arrays.asList(fluidTanks), 2)

				.startFromSlot(0).endAtSlot(fluidTanks.length - 1).background(background0)

				.build()

		);

		builder.widget(sc.setPos(3 + 18 * 5 + 4, 3 + 8).setSize(18 * 2, 18 * 4));
		/*
		 * builder.widget(
		 * 
		 * TextWidget.dynamicString(()->getInventoryName()) .setSynced(true)
		 * .setMaxWidth(999) .setPos(3+4,3)
		 * 
		 * );
		 */

	}

	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {

		GTUIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
		return true;
	}

	protected boolean isValidMainFacing(ForgeDirection side) {

		return true;// (side.flag & (UP.flag | DOWN.flag | UNKNOWN.flag)) == 0;
					// // Horizontal
	}

	public boolean onWrenchRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer entityPlayer,
			float aX, float aY, float aZ) {
		if (entityPlayer.isSneaking()) {
			if (setMainFacing(wrenchingSide)) {
				return true;
			}
			return false;
		}

		if (getBaseMetaTileEntity().isValidFacing(wrenchingSide)) {
			getBaseMetaTileEntity().setFrontFacing(wrenchingSide);
			if (getBaseMetaTileEntity().getFrontFacing() == mMainFacing) {
				mMainFacing = wrenchingSide.getOpposite();
				onFacingChange();
				onMachineBlockUpdate();
			}
			return true;
		}

		return false;
	};

	public void onFacingChange() {
		super.onFacingChange();
		// System.out.println(mMainFacing);
		// System.out.println(getBaseMetaTileEntity().getFrontFacing());
		if (getBaseMetaTileEntity().getFrontFacing() == mMainFacing) {
			mMainFacing = getBaseMetaTileEntity().getFrontFacing().getOpposite();
			onMachineBlockUpdate();
		}

	};

	ForgeDirection mMainFacing = ForgeDirection.UP;

	public boolean setMainFacing(ForgeDirection side) {
		if (!isValidMainFacing(side))
			return false;
		mMainFacing = side;
		if (getBaseMetaTileEntity().getFrontFacing() == mMainFacing) {
			getBaseMetaTileEntity().setFrontFacing(side.getOpposite());
		}
		onFacingChange();
		onMachineBlockUpdate();
		return true;
	}

	boolean mItemTransfer, mFluidTransfer;

	protected CycleButtonWidget createItemAutoOutputButton() {
		return (CycleButtonWidget) new CycleButtonWidget().setToggle(() -> mItemTransfer, val -> mItemTransfer = val)
				.setStaticTexture(GTUITextures.OVERLAY_BUTTON_AUTOOUTPUT_ITEM)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
				.setGTTooltip(() -> mTooltipCache.getData(ITEM_TRANSFER_TOOLTIP)).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(25, 62).setSize(18, 18);
	}

	protected CycleButtonWidget createFluidAutoOutputButton() {
		return (CycleButtonWidget) new CycleButtonWidget().setToggle(() -> mFluidTransfer, val -> mFluidTransfer = val)
				.setStaticTexture(GTUITextures.OVERLAY_BUTTON_AUTOOUTPUT_FLUID)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
				.setGTTooltip(() -> mTooltipCache.getData(FLUID_TRANSFER_TOOLTIP)).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(7, 62).setSize(18, 18);
	}

	@Override
	public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
		if (side == getBaseMetaTileEntity().getFrontFacing() || side == mMainFacing) {
			if (aPlayer.isSneaking()) {
				/*
				 * mDisableFilter = !mDisableFilter;
				 * GTUtility.sendChatToPlayer( aPlayer,
				 * StatCollector.translateToLocal("GT5U.hatch.disableFilter." +
				 * mDisableFilter));
				 */} else {
				inputFromFront = !inputFromFront;
				GTUtility.sendChatToPlayer(aPlayer,
						inputFromFront ? GTUtility.trans("095", "Input from Output Side allowed")
								: GTUtility.trans("096", "Input from Output Side forbidden"));
			}
		}
	}

	private boolean inputFromFront = false;

	@Override
	public boolean isLiquidInput(ForgeDirection side) {
		// TODO Auto-generated method stub

		return inputFromFront || side != getBaseMetaTileEntity().getFrontFacing();
	}

	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {

		return inputFromFront || side != getBaseMetaTileEntity().getFrontFacing();
	}


}
