package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static reobf.proghatches.gt.metatileentity.DualInputHatch.INSERTION;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.gson.internal.Streams;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.api.widget.Widget.ClickData;
import com.gtnewhorizons.modularui.common.fluid.FluidStackTank;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.core.Api;
import appeng.helpers.IInterfaceHost;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import appeng.util.item.AEStack;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.SoundResource;
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
import gregtech.api.util.GT_Utility;
import gregtech.api.util.GT_TooltipDataCache.TooltipData;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.gt.metatileentity.util.BaseSlotPatched;
import reobf.proghatches.gt.metatileentity.util.IMultiCircuitSupport;
import reobf.proghatches.gt.metatileentity.util.IProgrammingCoverBlacklisted;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;
import reobf.proghatches.gt.metatileentity.util.ISkipStackSizeCheck;
import reobf.proghatches.gt.metatileentity.util.InventoryItemHandler;
import reobf.proghatches.gt.metatileentity.util.ListeningFluidTank;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.net.UpgradesMessage;

public class DualInputHatch extends GT_MetaTileEntity_Hatch_InputBus
		implements IConfigurationCircuitSupport, IAddGregtechLogo, IAddUIWidgets, IDualInputHatch,
		IProgrammingCoverBlacklisted, IRecipeProcessingAwareDualHatch,ISkipStackSizeCheck/*,IMultiCircuitSupport*/ {

	static java.text.DecimalFormat format = new java.text.DecimalFormat("#,###");
	public boolean mMultiFluid;

	public DualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, String... optional) {
		this(id, name, nameRegional, tier, getSlots(tier) + 1, mMultiFluid, optional);
		
	}
	//boolean extraCircuit;
	public DualInputHatch(int id, String name, String nameRegional, int tier,  boolean mMultiFluid,boolean extraCircuit,
			String... optional) {

		super(id, name, nameRegional, tier, getSlots(tier) + 4, (optional.length > 0 ? optional
				: 
				reobf.proghatches.main.Config.get("MCDH", ImmutableMap.of(

						"cap", format.format(getInventoryFluidLimit(tier,mMultiFluid)), "mMultiFluid",
						mMultiFluid, "slots", Math.min(16, (1 + tier) * (tier + 1)),
						"fluidSlots", fluidSlots(tier),
						"stackLimit",getInventoryStackLimit(tier)
				))

		)

		);
		if(!extraCircuit){throw new RuntimeException("wrong ctr!");}
		//this.extraCircuit=true;
		this.disableSort = true;
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, id));
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();
	}
	public DualInputHatch(int id, String name, String nameRegional, int tier, int slot, boolean mMultiFluid,
			String... optional) {
		
		super(id, name, nameRegional, tier, slot, (optional.length > 0 ? optional
				: 
				reobf.proghatches.main.Config.get("DH", ImmutableMap.of(

						"cap", format.format(getInventoryFluidLimit(tier,mMultiFluid)), "mMultiFluid",
						mMultiFluid, "slots", Math.min(16, (1 + tier) * (tier + 1)),
						"fluidSlots", fluidSlots(tier),
						"stackLimit",getInventoryStackLimit(tier)
				))

		)

		);
		this.disableSort = true;
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, id));
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();
		
	}
	public int fluidSlots(){
		return Math.max(4, mTier-1);
		
	}
	public static int fluidSlots(int mTier){
		return Math.max(4, mTier-1);
		
	}
	public void initTierBasedField() {

		

		if (mMultiFluid) {

			mStoredFluid =Stream.generate(()->(new ListeningFluidTank(getInventoryFluidLimit(), this)))
					.limit(fluidSlots())
					.toArray(ListeningFluidTank[]::new)
					;
			
		

		} else {

			mStoredFluid = new ListeningFluidTank[] { new ListeningFluidTank(getInventoryFluidLimit(), this) };

		}

	}
public void reinitTierBasedField() {

		this.markDirty();

		if (mMultiFluid) {
			ListeningFluidTank[] old = mStoredFluid;
			mStoredFluid =Stream.generate(()->(new ListeningFluidTank(getInventoryFluidLimit(), this)))
					.limit(fluidSlots())
					.toArray(ListeningFluidTank[]::new)
					;
			for(int i=0;i<mStoredFluid.length;i++){
				mStoredFluid[i]=old[i];
			}
		

		} else {
			ListeningFluidTank[] old = mStoredFluid;
			mStoredFluid = new ListeningFluidTank[] { new ListeningFluidTank(getInventoryFluidLimit(), this) };
			for(int i=0;i<mStoredFluid.length;i++){
				mStoredFluid[i]=old[i];
			}
		}

	}
	/*public DualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
			boolean mMultiFluid,boolean ex) {
		super(mName, mTier,getSlots(mTier)+4, mDescriptionArray, mTextures);
		this.extraCircuit=true;
		this.disableSort = true;
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();
	}*/
	public DualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
			boolean mMultiFluid) {
		super(mName, mTier, mDescriptionArray, mTextures);
		this.disableSort = true;
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();shared.reinit();
	}

	public DualInputHatch(String aName, int aTier, int aSlots, String[] aDescription, ITexture[][][] aTextures,
			boolean mMultiFluid) {
		super(aName, aTier, aSlots, aDescription, aTextures);
		this.disableSort = true;
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();shared.reinit();

	}
	public NBTTagCompound writeToNBT(ItemStack is, NBTTagCompound tag) {
		is.writeToNBT(tag);
		tag.setInteger("ICount", is.stackSize);
		
		return tag;
	}

	
	public ItemStack loadItemStackFromNBT(NBTTagCompound tag) {

		ItemStack is = ItemStack.loadItemStackFromNBT(tag);
		is.stackSize = tag.getInteger("ICount");
		return is;
	}
	public ListeningFluidTank[] mStoredFluid=new ListeningFluidTank[0];

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		super.saveNBTData(aNBT);
		aNBT.setTag("shared", shared.ser());
		aNBT.setBoolean("fluidLimit", fluidLimit);
		aNBT.setBoolean("program", program);
		aNBT.setBoolean("mMultiFluid", mMultiFluid);
		if (mStoredFluid != null) {
			for (int i = 0; i < mStoredFluid.length; i++) {
				if (mStoredFluid[i] != null)
					aNBT.setTag("mFluid" + i, mStoredFluid[i].writeToNBT(new NBTTagCompound()));
			}
		}
		
		NBTTagList greggy=aNBT.getTagList("Inventory", 10);
		for(int i=0;i<mInventory.length;i++){
		
			if( mInventory[i]!=null){	
				NBTTagCompound t;
				t=((NBTTagCompound)greggy.getCompoundTagAt(i));
				if(t!=null)t.setInteger("Count", mInventory[i].stackSize);}
			
		}
		
		
		
		
		
		
		
	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		super.loadNBTData(aNBT);
		shared.deser(aNBT.getCompoundTag("shared"));
		fluidLimit= aNBT.getBoolean("fluidLimit");
		program = aNBT.getBoolean("program");
		mMultiFluid = aNBT.getBoolean("mMultiFluid");
		if (mStoredFluid != null) {
			for (int i = 0; i < mStoredFluid.length; i++) {
				if (aNBT.hasKey("mFluid" + i)) {
					mStoredFluid[i].readFromNBT(aNBT.getCompoundTag("mFluid" + i));
				}
			}
		}
		if(loadOldVer){try{
		NBTTagList greggy=aNBT.getTagList("Inventory", 10);
		for(int i=0;i<mInventory.length;i++){
			if(aNBT.hasKey("IntegerStackSize"+i)){
			int realsize=aNBT.getInteger("IntegerStackSize"+i);
			ItemStack is= ItemStack.loadItemStackFromNBT(greggy.getCompoundTagAt(i));
			is.stackSize=realsize;
			mInventory[i]=is;
			}
		}
		}catch(Exception e){
			//meh
		}
		}
	}
boolean loadOldVer=true;
	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		DualInputHatch neo =
				
				/*extraCircuit?
				new DualInputHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid,true)
						:*/
				new DualInputHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid);
	
	
		return neo;
	}

	private Widget createButton(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
			Supplier<GT_TooltipDataCache.TooltipData> tooltipDataSupplier, int offset) {
		return new CycleButtonWidget().setToggle(getter, setter).setStaticTexture(picture)
				.setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(7 + offset * 18, 62).setSize(18, 18).setGTTooltip(tooltipDataSupplier);
	}
	private Widget createButton(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
			List<String> tooltip, int offset) {
		return new CycleButtonWidget().setToggle(getter, setter).setStaticTexture(picture)
				.setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(7 + offset * 18, 62).setSize(18, 18).addTooltips(tooltip);
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
			bridge = new InventoryItemHandler(mInventory, this).id(1);
		return bridge;
	}

	public int moveButtons() {
		return 1;

	} 
	private static final String SORTING_MODE_TOOLTIP = "GT5U.machines.sorting_mode.tooltip";
    private static final String ONE_STACK_LIMIT_TOOLTIP = "GT5U.machines.one_stack_limit.tooltip";
    private static final int BUTTON_SIZE = 18;
	private Widget createToggleButton(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
	        Supplier<GT_TooltipDataCache.TooltipData> tooltipDataSupplier,int uiButtonCount) {
	        return new CycleButtonWidget().setToggle(getter, setter)
	            .setStaticTexture(picture)
	            .setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE)
	            .setTooltipShowUpDelay(TOOLTIP_DELAY)
	            .setPos(7 + (uiButtonCount * BUTTON_SIZE), 62)
	            .setSize(BUTTON_SIZE, BUTTON_SIZE)
	            .setGTTooltip(tooltipDataSupplier);
	    }
    private void addSortStacksButton(ModularWindow.Builder builder) {
        builder.widget(
            createToggleButton(
                () -> !disableSort,
                val -> disableSort = !val,
                GT_UITextures.OVERLAY_BUTTON_SORTING_MODE,
                () -> mTooltipCache.getData(SORTING_MODE_TOOLTIP),0));
    }

    private void addOneStackLimitButton(ModularWindow.Builder builder) {
        builder.widget(createToggleButton(() -> !disableLimited, val -> {
            disableLimited = !val;
            updateSlots();
        }, GT_UITextures.OVERLAY_BUTTON_ONE_STACK_LIMIT, () -> mTooltipCache.getData(ONE_STACK_LIMIT_TOOLTIP),1));
    }
    boolean createInsertion(){return true;}
	
    public static class MarkerWidget extends Widget{
    	public DualInputHatch thiz;
	public MarkerWidget(DualInputHatch dualInputHatch) {
		thiz=dualInputHatch;
	}
	}
    @Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		builder.widget(new MarkerWidget(this));
		buildContext.addSyncedWindow(INSERTION , (s) -> createInsertionWindow(buildContext));
		buildContext.addSyncedWindow(SHARED_ITEM , (s) -> createSharedItemWindow(buildContext));
		if(createInsertion())builder.widget(createButtonInsertion());
		if (moveButtons() == 1) {
			//super.addUIWidgets(builder, buildContext);
			addSortStacksButton(builder);
	        addOneStackLimitButton(builder);
		}
		addUIWidgets0(builder, buildContext);
		if(!allowSelectCircuit())
		builder.widget(
		createButtonSharedItem());
		
		builder.widget(new SlotWidget(new BaseSlot(getInventoryHandler(), getCircuitSlot())).setEnabledForce(false));
		
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
		if(mMultiFluid==true&&showFluidLimit())
		builder.widget(createButton(() -> 
		fluidLimit
				, val -> {
			fluidLimit = val;
			//updateSlots();
		}, GT_UITextures.OVERLAY_BUTTON_CHECKMARK, 
		ImmutableList.of(
				StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.0"),
				StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.1")
				)
	
		
		, 0)
				.setPos(7+ 1 * 18, 62 - 18 - moveButtons() * 18));

		
		
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
///
		/*if(extraCircuit)
		{
		for(int i=1;i<4;i++)
			builder.widget(
		new SlotWidget(new BaseSlot(inventoryHandler, getCircuitSlot()+i) {

			public int getSlotStackLimit() {
				return 0;
			};

		}

		) {

			@Override
			public List<String> getExtraTooltip() {
				return Arrays
						.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"));
			}
		}.disableShiftInsert().setHandlePhantomActionClient(true).setGTTooltip(() -> new TooltipData(
				Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
						LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")),
				Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
						LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")))).setPos(
								getCircuitSlotX()-1,getCircuitSlotY()-18 * i-1)
		
					);
		
	}*/
	}
	
	public SlotWidget catalystSlot(IItemHandlerModifiable inventory, int slot){
		return (SlotWidget) new SlotWidget(new BaseSlot(inventory, slot) {

			public int getSlotStackLimit() {
				return 0;
			};

		}

		) {

			@Override
			public List<String> getExtraTooltip() {
				return Arrays
						.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"));
			}
		}.disableShiftInsert().setHandlePhantomActionClient(true).setGTTooltip(() -> new TooltipData(
				Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
						LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")),
				Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
						LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"))));
	}
	public Widget circuitSlot(IItemHandlerModifiable inventory, int slot) {

		List<String> tooltip = Arrays.asList(
				EnumChatFormatting.DARK_GRAY + (LangManager.translateToLocal("GT5U.machines.select_circuit.tooltip.1")),

				EnumChatFormatting.DARK_GRAY
						+ (LangManager.translateToLocal("GT5U.machines.select_circuit.tooltip.3")));

		return new SlotWidget(new BaseSlot(inventory, slot, true)) {

			@Override
			protected void phantomClick(ClickData clickData, ItemStack cursorStack) {
				final ItemStack newCircuit;
				if (clickData.shift) {
					if (clickData.mouseButton == 0) {
						// if (NetworkUtils.isClient() && !dialogOpened.get()) {
						// openSelectCircuitDialog(getContext(), dialogOpened);
						// }
						return;
					} else {
						newCircuit = null;
					}
				} else {
					final List<ItemStack> tCircuits =DualInputHatch.this.getConfigurationCircuits();
					final int index = GT_Utility.findMatchingStackInList(tCircuits, cursorStack);
					if (index < 0) {
						int curIndex = GT_Utility.findMatchingStackInList(tCircuits, inventory.getStackInSlot(slot))
								+ 1;
						if (clickData.mouseButton == 0) {
							curIndex += 1;
						} else {
							curIndex -= 1;
						}
						curIndex = Math.floorMod(curIndex, tCircuits.size() + 1) - 1;
						newCircuit = curIndex < 0 ? null : tCircuits.get(curIndex);
					} else {
						// set to whatever it is
						newCircuit = tCircuits.get(index);
					}
				}
				inventory.setStackInSlot(slot, GT_Utility.copyAmount(0, newCircuit));

			}

			@Override
			protected void phantomScroll(int direction) {
				phantomClick(new ClickData(direction > 0 ? 1 : 0, false, false, false));
			}

			@Override
			public List<String> getExtraTooltip() {
				return tooltip;
			}

		}.setOverwriteItemStackTooltip(list -> {
			list.removeIf(line -> line.contains(LangManager.translateToLocal("gt.integrated_circuit.tooltip.0"))
					|| line.contains(LangManager.translateToLocal("gt.integrated_circuit.tooltip.1")));
			return list;
		}).disableShiftInsert().setHandlePhantomActionClient(true).setGTTooltip(() -> new TooltipData(tooltip, tooltip))
				.setBackground(getGUITextureSet().getItemSlot(), GT_UITextures.OVERLAY_SLOT_INT_CIRCUIT);
	}
	private ModularWindow createSharedItemWindow(UIBuildContext buildContext) {
		
		
		
		ModularWindow.Builder builder = ModularWindow.builder(36+18*3, 36+18*4);
		builder.setBackground(GT_UITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
		builder.setGuiTint(getGUIColorization());
		builder.setDraggable(true);
		builder.widget(circuitSlot(this.getInventoryHandler(), getCircuitSlot())
				.setPos(8-1, 8-1)
				);
		for(int i=0;i<shared.circuitUpgrades;i++)
		builder.widget(catalystSlot(new ItemStackHandler(shared.circuitInv), i).setPos(8-1, 8-1+18+18*i));
				
		
		int posoffset=0;
		for(int i=0;i<shared.itemMEUpgrades;i++){
			final int fi=i;
			builder.widget(SlotWidget.phantom(new ItemStackHandler(shared.markedItems), i)
					.addTooltips(
							(this instanceof BufferedDualInputHatch)?
							ImmutableList.of(
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.0"),
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.1"),
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.2")
											):	
							ImmutableList.of(
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.0"),
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.1")
							)
							
							
							)
					.setPos(8-1+18+4, 8-1+18*posoffset));
			builder.widget(new DrawableWidget().setDrawable(ModularUITextures.ARROW_RIGHT)
					 
					 .setPos(8-1+18*2+4, 8-1+18*posoffset).setSize(18,18));
			builder.widget(new SlotWidget(new BaseSlot(new ItemStackHandler(1), 0)){
				int cd=0;
				public void detectAndSendChanges(boolean init) {
					
					if(cd--<0)
					{cd=10;
					ItemStack is=null;
					Net net = getNetwork();
					if(net!=null){
						IStorageGrid cahce = net.g.getCache(IStorageGrid.class);
					 if(cahce!=null){
						IAEItemStack aeis = cahce.getItemInventory().getStorageList().findPrecise(AEItemStack.create(shared.markedItems.get(fi)));
						 if(aeis!=null)is=aeis.getItemStack();
					 }
					}
					((ItemStackHandler)((BaseSlot)this.getMcSlot()).getItemHandler()).setStackInSlot(this.getMcSlot().getSlotIndex(), 
							
							is);
					}
					
					
					super.detectAndSendChanges(init);
				};
				
			}.disableInteraction().setPos(8-1+18*3+4, 8-1+18*posoffset));
			posoffset++;
		}
		
		for(int i=0;i<shared.fluidMEUpgrades;i++){
			final int fi=i;
			builder.widget(new FluidSlotWidget(shared.createTankForFluidStack(i,1))
					{{setPhantom(true);}
				 @Override
                 protected void tryClickPhantom(ClickData clickData, ItemStack cursorStack) {
                     if (clickData.mouseButton != 0 ) return;

                     FluidStack heldFluid = getFluidForPhantomItem(cursorStack);
                     if (cursorStack == null) {
                    	 shared.markedFluid.set(fi, null);
                     } else {
                      
                    	 shared.markedFluid.set(fi, heldFluid);
                     }
                     if (getBaseMetaTileEntity().isServerSide()) {
                     
                         detectAndSendChanges(false);
                     }
                 }

                 @Override
                 protected void tryScrollPhantom(int direction) {}
				
					}
					
					.addTooltips(ImmutableList.of(
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.0"),
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.1"))
							)
					.setPos(8-1+18+4, 8-1+18*posoffset));
			builder.widget(new DrawableWidget().setDrawable(ModularUITextures.ARROW_RIGHT)
					 
					 .setPos(8-1+18*2+4, 8-1+18*posoffset).setSize(18,18));
			FluidTank ft;
			builder.widget(new FluidSlotWidget(ft=new FluidTank(Integer.MAX_VALUE)){
				int cd=0;{setPhantom(true);}
				@Override
                public void buildTooltip(List<Text> tooltip) {
                    FluidStack fluid = getContent();
                    if (fluid != null) {
                        addFluidNameInfo(tooltip, fluid);
                        tooltip.add(Text.localised("modularui.fluid.phantom.amount", fluid.amount));
                        addAdditionalFluidInfo(tooltip, fluid);
                        if (!Interactable.hasShiftDown()) {
                            tooltip.add(Text.EMPTY);
                            tooltip.add(Text.localised("modularui.tooltip.shift"));
                        }
                    } else {
                        tooltip.add(
                            Text.localised("modularui.fluid.empty")
                                .format(EnumChatFormatting.WHITE));
                    }
                }
				  @Override
                  protected void tryClickPhantom(ClickData clickData, ItemStack cursorStack) {}

                  @Override
                  protected void tryScrollPhantom(int direction) {}

				public void detectAndSendChanges(boolean init) {
					
					if(cd--<0)
					{cd=10;
					FluidStack is=null;
					Net net = getNetwork();
					if(net!=null){
						IStorageGrid cahce = net.g.getCache(IStorageGrid.class);
					 if(cahce!=null){
						IAEFluidStack aeis = cahce.getFluidInventory().getStorageList().findPrecise(AEFluidStack.create(shared.markedFluid.get(fi)));
						 if(aeis!=null)is=aeis.getFluidStack();
					 }
					}
					ft.setFluid(is);
					//todo
					//this.setfl
					
					}
					
					
					super.detectAndSendChanges(init);
				};
				
			}
			
			.setPos(8-1+18*3+4, 8-1+18*posoffset));
			posoffset++;
		}
		
		
		
		
		
		return builder.build();
	}
	boolean showFluidLimit() {
	
		return true;
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
		return filterStack.apply(mInventory,shared.getItems());
	}

	private FluidStack[] dualFluid() {
		return asFluidStack.apply(mStoredFluid,shared.getFluid());
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
			ItemStack[] is=dualItem();
			//System.out.println(Arrays.toString(is));
			return is;
		}

		@Override
		public FluidStack[] getFluidInputs() {

			return dualFluid();
		}
	}
	public interface VargsFunction<T, R> {
			R apply(T... t);
	}
	VargsFunction<Object[], FluidStack[]> asFluidStack = (s) -> Arrays.stream(s).flatMap( Arrays::stream).map(f->{
		if(f instanceof FluidTank){return ((FluidTank) f).getFluid();}
		else if(f instanceof FluidStack){return (FluidStack)f;}
		else if(f==null){/*ignore*/return null;}
		else
		{throw new RuntimeException("only FluidStack or FluidTank are accepted");}
	})
			.filter(a -> a != null && a.amount > 0).toArray(FluidStack[]::new);
	VargsFunction<ItemStack[], ItemStack[]> filterStack = (s) -> Arrays.stream(s).flatMap( Arrays::stream).filter(a -> a != null)
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
		if(mStoredFluid.length==0)return 0;
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
		else
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
		if(shared.isDummy())
		{
			//for (int slot : this.getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal())) {
			for (int slot :(Iterable<Integer>)()->IntStream.range(0, getSlots(mTier)).iterator()) {	
				ItemStack is = getStackInSlot(slot);
				if (is == null)
					continue;
				if (is.getItem() != MyMod.progcircuit)
					continue;
				if (decrStackSize(slot, 64).stackSize == 0) {
					continue;
				}
				markDirty();
				super.setInventorySlotContents(getCircuitSlot(), ItemProgrammingCircuit.getCircuit(is).orElse(null));
			}
			return;
		}
		
		
		DualInputHatch meta = this;
		ArrayList<ItemStack> isa = new ArrayList<>();
		int[] slots = (this).getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal());
		for (int slot :(Iterable<Integer>)()->IntStream.range(0, getSlots(mTier)).iterator()) {
			ItemStack is = this.mInventory[slot];
			if (is == null)
				continue;
			if (is.getItem() != MyMod.progcircuit)
				continue;
			if (decrStackSize(slot, 64).stackSize == 0) {
				continue;
			}
			isa.add(GT_Utility.copyAmount(0, ItemProgrammingCircuit.getCircuit(is).orElse(null)));
			
			
		}
		if(isa.isEmpty()==false){	
		
			shared.clearCircuit();
			
				for (int i = 0; i < shared.sizeCircuit(); i++) {
					if (i < isa.size()) {
						shared.setCircuit(
								i
								,isa.get(i));
					} else {
						shared.setCircuit(
								i
								,null);
					}
	
				}
	
				
				
				
			
		}
		/*
		IGregTechTileEntity tile = this.getBaseMetaTileEntity();
		DualInputHatch meta = this;
		ArrayList<ItemStack> isa = new ArrayList<>();
		int[] slots = (this).getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal());
		for (int slot : slots) {
			ItemStack is = ((ISidedInventory) tile).getStackInSlot(slot);
			if (is == null)
				continue;
			if (is.getItem() != MyMod.progcircuit)
				continue;

			
			if (((ISidedInventory) tile).decrStackSize(slot, 64).stackSize == 0) {
				continue;
			}
			isa.add(GT_Utility.copyAmount(0, ItemProgrammingCircuit.getCircuit(is).orElse(null)));
			
			
		}
		if(isa.isEmpty()==false){	
			if(meta instanceof IMultiCircuitSupport){
				int[] aslots=((IMultiCircuitSupport) meta).getCircuitSlots();
				for (int i = 0; i < aslots.length; i++) {
					if (i < isa.size()) {
						((IInventory) tile).setInventorySlotContents(
								((IMultiCircuitSupport) meta).getCircuitSlots()[i]
								,isa.get(i));
					} else {
						((IInventory) tile).setInventorySlotContents(
								((IMultiCircuitSupport) meta).getCircuitSlots()[i]
								,null);
					}
	
				}
	
				
				
				
			}else{
			
			throw new AssertionError("impossible");
			}
		}
		*/
		
		
		
		
		
		
		
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

	@SuppressWarnings("unchecked")
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.onPostTick(aBaseMetaTileEntity, aTick);
		if (aBaseMetaTileEntity.getWorld().isRemote)
			return;

		if (program)
			program();
		
		/*IGrid a = getNetwork();
		if(a!=null){
			IStorageGrid g=a.getCache(IStorageGrid.class);
			g.getItemInventory().getAvailableItems(new appeng.util.item.ItemList()).forEach(s->{
				System.out.println(	s);
				
			});
		
		}*/
		
	}

	@Override
	public void setInventorySlotContents(int aIndex, ItemStack aStack) {
		super.setInventorySlotContents(aIndex, aStack);
		/*if (program)
			program();*/
	}

	public void onFill() {
	}
 boolean fluidLimit=true;
	@Override
	public int fill(FluidStack aFluid, boolean doFill) {
		if (aFluid == null || aFluid.getFluid().getID() <= 0 || aFluid.amount <= 0 || !canTankBeFilled()
				|| !isFluidInputAllowed(aFluid))
			return 0;
		
		if(!fluidLimit){int oldamount=aFluid.amount;
			aFluid=aFluid.copy();
			for(ListeningFluidTank tk:this.mStoredFluid){
				if(tk.getFluidAmount()==0)tk.setFluid(null);
				if((aFluid.amount-=tk.fill(aFluid, doFill))<=0){
					break;
					};
				
			}
			return oldamount-aFluid.amount;
		}
		
		
		if(fluidLimit){
		
		if (!hasFluid(aFluid) && getFirstEmptySlot() != -1) {
			int tFilled = Math.min(aFluid.amount, getCapacity());
			if (doFill) {
				FluidStack tFluid = aFluid.copy();
				tFluid.amount = tFilled;
				addFluid(tFluid, getFirstEmptySlot());
				getBaseMetaTileEntity().markDirty();
			}
			return tFilled;
		}
		if (hasFluid(aFluid)) {
			int tLeft = getCapacity() - getFluidAmount(aFluid);
			int tFilled = Math.min(tLeft, aFluid.amount);
			if (doFill) {
				FluidStack tFluid = aFluid.copy();
				tFluid.amount = tFilled;
				addFluid(tFluid, getFluidSlot(tFluid));
				getBaseMetaTileEntity().markDirty();
			}
			return tFilled;
		}
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

	//private static HashMap<Class<?>, MethodHandle> cache0 = new HashMap<>();
	//private static HashMap<Class<?>, MethodHandle> cache1 = new HashMap<>();

	/**
	 * 2.4.0 compat 
	 * no longer support 2.4.0 just leave it unchanged
	 */
	public boolean versionInsensitiveContainsInput(FluidStack aFluid) {
        if(mRecipeMap==null)return true;
		return this.mRecipeMap.containsInput(aFluid);
        /*
        MethodHandle mh = cache0.get(aFluid.getClass());
		if (mh != null) {
			try {
				Object map;
				return ((map = cache1.get(aFluid.getClass()).invoke(this)) == null) || // this.mRecipeMap==null
																					// ||
						(boolean) mh.invoke(map, aFluid); // this.mRecipeMap.containsInput(0)
			} catch (Throwable e) {
				throw new RuntimeException("failed to access mRecipeMap", e);
			}

		}

		try {
			Class<?> recipeMapClass = this.getClass().getField("mRecipeMap").getType();
			MethodHandle mhr = MethodHandles.lookup().findVirtual(recipeMapClass, "containsInput",
					MethodType.methodType(boolean.class, aFluid.getClass()));
			cache0.put(aFluid.getClass(), mhr);
			cache1.put(aFluid.getClass(), MethodHandles.lookup().findGetter(this.getClass(), "mRecipeMap", recipeMapClass));
			return versionInsensitiveContainsInput(aFluid);
		} catch (Throwable e) {
			throw new RuntimeException("cannot get mRecipeMap", e);
		}
*/
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
			add1by1Slot(builder);
			break;
		case 1:
			add2by2Slots(builder);
			break;
		case 2:
			add3by3Slots(builder);
			break;
		default:
			add4by4Slots(builder);
		}
	}

	public int fluidSlotsPerRow() {
		return 1;
	}
	
	boolean recipe;
	
	@Override
	public final void startRecipeProcessing() {
		
		if(recipe){return;}
		recipe=true;
		startRecipeProcessingImpl();
	}
	
	public  void startRecipeProcessingImpl() {
		if (program)
			program();
		shared.startRecipeProcessing();
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
	/*	if(extraCircuit){
			for (int i = 0; i < mInventory.length - 4; i++)
		    if (mInventory[i] != null && mInventory[i].stackSize <= 0) mInventory[i] = null;
			 if (!disableSort)fillStacksIntoFirstSlotsExtraCircuit();
		}else*/
		super.updateSlots();
	}
	private void fillStacksIntoFirstSlotsExtraCircuit() {
	    final int L = mInventory.length - 4;
	    HashMap<GT_Utility.ItemId, Integer> slots = new HashMap<>(L);
	    HashMap<GT_Utility.ItemId, ItemStack> stacks = new HashMap<>(L);
	    List<GT_Utility.ItemId> order = new ArrayList<>(L);
	    List<Integer> validSlots = new ArrayList<>(L);
	    for (int i = 0; i < L; i++) {
	        if (!isValidSlot(i)) continue;
	        validSlots.add(i);
	        ItemStack s = mInventory[i];
	        if (s == null) continue;
	        GT_Utility.ItemId sID = GT_Utility.ItemId.createNoCopy(s);
	        slots.merge(sID, s.stackSize, Integer::sum);
	        if (!stacks.containsKey(sID)) stacks.put(sID, s);
	        order.add(sID);
	        mInventory[i] = null;
	    }
	    int slotindex = 0;
	    for (GT_Utility.ItemId sID : order) {
	        int toSet = slots.get(sID);
	        if (toSet == 0) continue;
	        int slot = validSlots.get(slotindex);
	        slotindex++;
	        mInventory[slot] = stacks.get(sID)
	            .copy();
	        toSet = Math.min(toSet, mInventory[slot].getMaxStackSize());
	        mInventory[slot].stackSize = toSet;
	        slots.merge(sID, toSet, (a, b) -> a - b);
	    }
	}
	CheckRecipeResult lastresult;
	@Override
	public final CheckRecipeResult  endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
	
		
		if(recipe){recipe=false;
			return lastresult=endRecipeProcessingImpl(controller);
		}
		
		return lastresult;
	}

	public CheckRecipeResult endRecipeProcessingImpl(GT_MetaTileEntity_MultiBlockBase controller) {
		this.markDirty();
		updateSlots();
		boolean success=shared.endRecipeProcessing(controller);
		if(!success)	return CheckRecipeResultRegistry.CRASH;
		return CheckRecipeResultRegistry.SUCCESSFUL;
	}
@Override
public int getInventoryStackLimit() {
	
	return 64+64*Math.max(0,mTier-3);
}


public static int getInventoryStackLimit(int mTier) {
	
	return 64+64*Math.max(0,mTier-3);
}

public int fluidBuff(){
	
	return 1<<shared.fluidCapUpgrades;
	
}
public int getInventoryFluidLimit() {
	
	long val= fluidBuff()*(int) (4000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1))
			;
			
		return	(int) Math.min(val, Integer.MAX_VALUE);
}


public static int getInventoryFluidLimit(int mTier,boolean mMultiFluid) {
	
	return (int) (4000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1));
}

public void add1by1Slot(ModularWindow.Builder builder, IDrawable... background) {
    final ItemStackHandler inventoryHandler = getInventoryHandler();
    if (inventoryHandler == null) return;

    if (background.length == 0) {
        background = new IDrawable[] { getGUITextureSet().getItemSlot() };
    }
    builder.widget(
        SlotGroup.ofItemHandler(inventoryHandler, 1)
        .slotCreator(BaseSlotPatched.newInst(inventoryHandler))
            .startFromSlot(0)
            .endAtSlot(0)
            .background(background)
            .build()
            .setPos(79, 34));
}


public void add2by2Slots(ModularWindow.Builder builder, IDrawable... background) {
    final ItemStackHandler inventoryHandler = getInventoryHandler();
    if (inventoryHandler == null) return;

    if (background.length == 0) {
        background = new IDrawable[] { getGUITextureSet().getItemSlot() };
    }
    builder.widget(
        SlotGroup.ofItemHandler(inventoryHandler, 2)
        .slotCreator(BaseSlotPatched.newInst(inventoryHandler))
            .startFromSlot(0)
            .endAtSlot(3)
            .background(background)
            .build()
            .setPos(70, 25));
}


public void add3by3Slots(ModularWindow.Builder builder, IDrawable... background) {
    final ItemStackHandler inventoryHandler = getInventoryHandler();
    if (inventoryHandler == null) return;

    if (background.length == 0) {
        background = new IDrawable[] { getGUITextureSet().getItemSlot() };
    }
    builder.widget(
        SlotGroup.ofItemHandler(inventoryHandler, 3)
        .slotCreator(BaseSlotPatched.newInst(inventoryHandler))
            .startFromSlot(0)
            .endAtSlot(8)
            .background(background)
            .build()
            .setPos(61, 16));
}


public void add4by4Slots(ModularWindow.Builder builder, IDrawable... background) {
    final ItemStackHandler inventoryHandler = getInventoryHandler();
    if (inventoryHandler == null) return;

    if (background.length == 0) {
        background = new IDrawable[] { getGUITextureSet().getItemSlot() };
    }
    builder.widget(
        SlotGroup.ofItemHandler(inventoryHandler, 4)
        .slotCreator(BaseSlotPatched.newInst(inventoryHandler))
            .startFromSlot(0)
            .endAtSlot(15)
            .background(background)
            .build()
            .setPos(52, 7));
}

//insertion
protected static final int INSERTION = 2001;
protected ModularWindow createInsertionWindow(UIBuildContext buildContext) {
	int len = (int) Math.round(Math.sqrt(mInventory.length-1));
	final int WIDTH = 18 * len + 6;
	final int HEIGHT = 18 * (len+1) + 6;
	ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
	builder.setBackground(GT_UITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
	builder.setGuiTint(getGUIColorization());
	builder.setDraggable(true);
	
	final IItemHandlerModifiable inventoryHandler = new ItemStackHandler(mInventory.length-(1));
	IDrawable[] background = new IDrawable[] { getGUITextureSet().getItemSlot() };
	
	builder.widget(SlotGroup.ofItemHandler(inventoryHandler,len).widgetCreator(SlotWidget::new).startFromSlot(0)
			.endAtSlot(/*mInventory.length-2*/9999).background(background).build().setPos(3, 3)

	);
	
	builder.widget(new SyncedWidget() {
		ArrayList<Integer> toclear=new ArrayList<>(1);
		@Override
		public void detectAndSendChanges(boolean init) {
			toclear.clear();
		for(int i=0;i<inventoryHandler.getSlots();i++)
		{	
		int fi=i;
		
			Optional.ofNullable(
			inventoryHandler.getStackInSlot(i)
			).filter(s->s.stackSize>0).ifPresent(s->{
				markDirty();
				if(mInventory[fi]!=null){
					int oldsize=s.stackSize;
					s.stackSize=mInventory[fi].stackSize;
					boolean eq=ItemStack.areItemStacksEqual(s, mInventory[fi]);
					s.stackSize=oldsize;
					if(eq){
						int canInject=Math.min(oldsize, getInventoryStackLimit()-mInventory[fi].stackSize);
						s.stackSize-=canInject;
						mInventory[fi].stackSize+=canInject;
						
					}
					
					
				}else{
					//guaranteed!
					mInventory[fi]=s.copy();
					s.stackSize=0;
					
				}
				if(s.stackSize==0)toclear.add(fi);
				
			});
			
			}
		toclear.forEach(s->{
		markDirty();
			ItemStack is = inventoryHandler.getStackInSlot(s);
			if(is!=null&&is.stackSize<=0){inventoryHandler.setStackInSlot(s, null);}
		});
			
		}
		@Override
	public void onDestroy() {
	
		inventoryHandler.getStacks().forEach(s->{
			if(s!=null){
				if(buildContext.getPlayer().worldObj.isRemote)return;
				EntityItem entityitem = buildContext.getPlayer().dropPlayerItemWithRandomChoice(
						s.copy(),false);
				if(entityitem!=null){
				entityitem.delayBeforeCanPickup = 0;
				entityitem.func_145797_a( buildContext.getPlayer().getCommandSenderName());
				}
			}
			
		});;
		
		
		
	}

		@Override
		public void readOnClient(int id, PacketBuffer buf) throws IOException {
			
		}

		@Override
		public void readOnServer(int id, PacketBuffer buf) throws IOException {

		}
	});
	
	
	ArrayList<String> tt=new ArrayList<>(10);
	int i = 0;
	while (true) {
		String k = "programmable_hatches.gt.insertion.tooltip";
		if (LangManager.translateToLocal(k).equals(Integer.valueOf(i).toString())) {
			break;
		}
		String key = k + "." + i;
		String trans = LangManager.translateToLocalFormatted(key,getInventoryStackLimit());

		tt.add(trans);
		i++;

	}
	builder.widget(new DrawableWidget().setDrawable(new ItemDrawable(new ItemStack(Blocks.hopper)))
			.addTooltips(tt)
			.setPos(WIDTH/2-16/2, HEIGHT-16-3)
			.setSize(16,16)
			);
	
	
	
	return builder.build();
}	public final static int SHARED_ITEM=654321;
protected Widget createButtonSharedItem() {
	return new ButtonWidget(){{this.setTicker(s->{
		
	setBackground(GT_UITextures.BUTTON_STANDARD, 
			
			mInventory[getCircuitSlot()]==null?
					GT_UITextures.OVERLAY_SLOT_CIRCUIT
					:
			
			new ItemDrawable(
		mInventory[getCircuitSlot()]
			//new ItemStack(Blocks.hopper)
			
			));
		
		
	});}}.setOnClick((clickData, widget) -> {
		if (clickData.mouseButton == 0) {
			if (!widget.isClient())
				widget.getContext().openSyncedWindow(SHARED_ITEM);
		}
	}).setPlayClickSound(true)
			.setBackground(GT_UITextures.BUTTON_STANDARD, new ItemDrawable(new ItemStack(Blocks.hopper)))
			
			.setEnabled(s -> {
				return !s.getContext().isWindowOpen(SHARED_ITEM);
			})
			
			
			.addTooltips(ImmutableList.of(LangManager.translateToLocal("programmable_hatches.gt.shared")))
			.setPos(/*extraCircuit?
					new Pos2d(
					getCircuitSlotX()-18,getCircuitSlotY()
				
					):*/
					new Pos2d(
							getCircuitSlotX()-1,getCircuitSlotY()/*-18*/-1
					)
					
					).setSize(18, 18);

}
@Override
public boolean allowSelectCircuit() {

	return shared.isDummy();
}
protected Widget createButtonInsertion() {
	return new ButtonWidget().setOnClick((clickData, widget) -> {
		if (clickData.mouseButton == 0) {
			if (!widget.isClient())
				widget.getContext().openSyncedWindow(INSERTION);
		}
	}).setPlayClickSound(true)
			.setBackground(GT_UITextures.BUTTON_STANDARD, new ItemDrawable(new ItemStack(Blocks.hopper)))
			.setEnabled(s -> {
				return !s.getContext().isWindowOpen(INSERTION);

			}).addTooltips(ImmutableList.of(LangManager.translateToLocal("programmable_hatches.gt.insertion")))
			.setPos(/*extraCircuit?
					new Pos2d(
					getCircuitSlotX()-18,getCircuitSlotY()
				
					):*/
					new Pos2d(
							getCircuitSlotX()-1,getCircuitSlotY()-18*2-1
						//getGUIWidth() - 18 - 3, 30
					)
					
					).setSize(18, 18);

}
@Override
public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {
	// TODO Auto-generated method stub
	return super.canInsertItem(aIndex, aStack, ordinalSide);
}
@Override
public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {
	// TODO Auto-generated method stub
	return super.canExtractItem(aIndex, aStack, ordinalSide);
}

@Override
public boolean isValidSlot(int aIndex) {
    return aIndex < getCircuitSlot();
}


@Override
public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
    ItemStack aStack) {
    if (aIndex >= getCircuitSlot()) return false;
    return side == getBaseMetaTileEntity().getFrontFacing();
}



@Override
public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
    ItemStack aStack) {
    return side == getBaseMetaTileEntity().getFrontFacing() && aIndex < getCircuitSlot()
        && (mRecipeMap == null || disableFilter || mRecipeMap.containsInput(aStack))
        && (disableLimited || limitedAllowPutStack(aIndex, aStack));
}

public OptioanlSharedContents shared=new OptioanlSharedContents();
public class OptioanlSharedContents{
	
	public int fluidCapUpgrades;
	ArrayList<ItemStack> shadowItems=new ArrayList<>();
	ArrayList<FluidStack> shadowFluid=new ArrayList<>();
	ArrayList<ItemStack> cachedItems=new ArrayList<>();
	ArrayList<FluidStack> cachedFluid=new ArrayList<>();
	ArrayList<ItemStack> markedItems=new ArrayList<>();
	ArrayList<FluidStack> markedFluid=new ArrayList<>();	
	
	public void reinit(){
		
		while(circuitInv.size()<circuitUpgrades)circuitInv.add(null);
		while(markedItems.size()<itemMEUpgrades)markedItems.add(null);
		while(markedFluid.size()<fluidMEUpgrades)markedFluid.add(null);
		
	}
	
	public FluidStackTank createTankForFluidStack( int slotIndex, int capacity) {
        return new FluidStackTank(() -> markedFluid.get(slotIndex), (stack) -> {
           

        	markedFluid.set(slotIndex, stack);
        }, capacity);
    }
	public boolean endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
		if(isDummy())return true;
		boolean storageMissing=false;
		Net net = getNetwork();
		IStorageGrid cahce=null;
		if(net==null)storageMissing=true;
		else{
		 cahce=net.g.getCache(IStorageGrid.class);
		 if(cahce==null)storageMissing=true;
		}
		
		for(int i=0;i<markedItems.size();i++){
			ItemStack sis=shadowItems.get(i);
			ItemStack cis=cachedItems.get(i);
			if(sis!=null){
				int consumed=sis.stackSize-Optional.ofNullable(cis).map(s->s.stackSize).orElse(0);
				if(consumed>0&&storageMissing)return false;
				ItemStack extract=sis.copy();
				extract.stackSize=consumed;
				
				IAEItemStack whatweget=cahce.getItemInventory().extractItems(AEItemStack.create(extract), Actionable.MODULATE, new  MachineSource( net.h));
				int numweget=0;
				if(whatweget!=null)numweget=(int) whatweget.getStackSize();
				if(numweget!=consumed){return false;}
			}
		}
		for(int i=0;i<markedFluid.size();i++){
			FluidStack sis=shadowFluid.get(i);
			FluidStack cis=cachedFluid.get(i);
			if(sis!=null){
				int consumed=sis.amount-Optional.ofNullable(cis).map(s->s.amount).orElse(0);
				if(consumed>0&&storageMissing)return false;
				FluidStack extract=sis.copy();
				extract.amount=consumed;
				
				IAEFluidStack whatweget=cahce.getFluidInventory().extractItems(AEFluidStack.create(extract), Actionable.MODULATE, new  MachineSource( net.h));
				int numweget=0;
				if(whatweget!=null)numweget=(int) whatweget.getStackSize();
				if(numweget!=consumed){return false;}
			}
		}
		
		
		
		
		return true;
	}
	public void startRecipeProcessing() {
		if(isDummy())return;
		
		IStorageGrid cahce= (IStorageGrid) Optional.ofNullable(getNetwork())
		.map(s->s.g)
		.map(s->s.getCache(IStorageGrid.class)).orElse(null);
		
		shadowItems.clear();
		shadowFluid.clear();
		cachedItems.clear();
		cachedFluid.clear();
		HashSet<Object> dup=new HashSet<>();
		for(int i=0;i<markedItems.size();i++){
			ItemStack is=markedItems.get(i);
			if(is==null||cahce==null){
				shadowItems.add(null);
				cachedItems.add(null);
			}else{
				AEItemStack toextract=AEItemStack.create(is);
				toextract.setStackSize(1);
				if(!dup.add(toextract)){
					shadowItems.add(null);
					cachedItems.add(null);
					markedItems.set(i, null);
					continue;};
				ItemStack ris=(Optional.ofNullable(
						cahce.getItemInventory().getStorageList().findPrecise(toextract)
						).map(IAEItemStack::getItemStack).orElse(null));
				shadowItems.add(ris);	
				cachedItems.add(Optional.ofNullable(ris).map(s->s.copy()).orElse(null));
			}
		}
		dup.clear();
		for(int i=0;i<markedFluid.size();i++){
			FluidStack is=markedFluid.get(i);
			if(is==null||cahce==null){
				shadowFluid.add(null);
				cachedFluid.add(null);
			}else{
				AEFluidStack toextract=AEFluidStack.create(is);
				toextract.setStackSize(1);
				if(!dup.add(toextract)){
					shadowFluid.add(null);
					cachedFluid.add(null);
					markedFluid.set(i, null);
					continue;};
				FluidStack ris=(Optional.ofNullable(
						cahce.getFluidInventory().getStorageList().findPrecise(toextract)
						).map(IAEFluidStack::getFluidStack).orElse(null));
				shadowFluid.add(ris);	
				cachedFluid.add(Optional.ofNullable(ris).map(s->s.copy()).orElse(null));
			}
		}
		
		
		
		
		
	}
	public void onDestroy() {IGregTechTileEntity te = getBaseMetaTileEntity();
		if(circuitUpgrades>0)
			te.getWorld().spawnEntityInWorld(
					new EntityItem(te.getWorld(),te.getXCoord(),te.getYCoord(),te.getZCoord(), new ItemStack(MyMod.upgrades,circuitUpgrades,0))
					);
		if(itemMEUpgrades>0)
			te.getWorld().spawnEntityInWorld(
					new EntityItem(te.getWorld(),te.getXCoord(),te.getYCoord(),te.getZCoord(), new ItemStack(MyMod.upgrades,itemMEUpgrades,1))
					);	
		if(fluidMEUpgrades>0)
			te.getWorld().spawnEntityInWorld(
					new EntityItem(te.getWorld(),te.getXCoord(),te.getYCoord(),te.getZCoord(), new ItemStack(MyMod.upgrades,fluidMEUpgrades,2))
					);	
	}
	public NBTTagCompound serList(ArrayList<ItemStack> ls){
		NBTTagCompound tag=new NBTTagCompound();
		tag.setInteger("len", ls.size());
		for(int i=0;i<ls.size();i++){
			if( ls.get(i)!=null)
			tag.setTag("i"+i, ls.get(i).writeToNBT(new NBTTagCompound()));
		}
		return tag;
	}
	public NBTTagCompound serListF(ArrayList<FluidStack> ls){
		NBTTagCompound tag=new NBTTagCompound();
		tag.setInteger("len", ls.size());
		for(int i=0;i<ls.size();i++){
			if( ls.get(i)!=null)
			tag.setTag("i"+i, ls.get(i).writeToNBT(new NBTTagCompound()));
		}
		return tag;
	}
	public ArrayList<ItemStack> deserList(NBTTagCompound tag){
		 ArrayList<ItemStack> ls=new  ArrayList<ItemStack>();
		 int len=tag.getInteger("len");
		 for(int i=0;i<len;i++){
			 ls.add(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("i"+i)));
		}
		return ls;
	}
	public ArrayList<FluidStack> deserListF(NBTTagCompound tag){
		 ArrayList<FluidStack> ls=new  ArrayList<FluidStack>();
		 int len=tag.getInteger("len");
		 for(int i=0;i<len;i++){
			 ls.add(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("i"+i)));
		}
		return ls;
	}
	
	
	
	public ItemStack[] getItems(){
		ArrayList<ItemStack> all=new ArrayList<>();
		all.addAll(circuitInv);
		all.addAll(cachedItems);
		all.add(mInventory[getCircuitSlot()]);
		return all.toArray(new ItemStack[0]);
		}
	
	public FluidStack[] getFluid(){
		ArrayList<FluidStack> all=new ArrayList<>();
		all.addAll(cachedFluid);
		return all.toArray(new FluidStack[0]);}
	public boolean isDummy(){return circuitUpgrades+itemMEUpgrades+fluidMEUpgrades==0;}
	
	
	public int circuitUpgrades;
	public int itemMEUpgrades;
	public int fluidMEUpgrades;
	
	public ArrayList<ItemStack> circuitInv=new ArrayList<>();
	public void clearCircuit() {
		mInventory[0]=null;
		circuitInv.clear();
		for(int i=0;i<circuitUpgrades;i++){circuitInv.add(null);}
	}
	public void setCircuit(int index,ItemStack is){
		if(index==0){mInventory[getCircuitSlot()]=is;return;}
		circuitInv.set(index-1,is);
	}
	public int sizeCircuit(){return circuitUpgrades+1;}
	
	
	public NBTTagCompound ser(){
		NBTTagCompound tag=new NBTTagCompound();
		tag.setInteger("fluidCapUpgrades",fluidCapUpgrades);
		tag.setInteger("circuitUpgrades", circuitUpgrades);
		tag.setInteger("itemMEUpgrades", itemMEUpgrades);
		tag.setInteger("fluidMEUpgrades", fluidMEUpgrades);
		tag.setTag("circuitInv", serList(circuitInv));
		tag.setTag("markedItems", serList(markedItems));
		tag.setTag("markedFluid", serListF(markedFluid));
		return tag;}
	public void deser(NBTTagCompound tag){
		fluidCapUpgrades=tag.getInteger("fluidCapUpgrades");
		circuitUpgrades=tag.getInteger("circuitUpgrades");
		itemMEUpgrades=tag.getInteger("itemMEUpgrades");
		fluidMEUpgrades=tag.getInteger("fluidMEUpgrades");
		circuitInv=deserList(tag.getCompoundTag("circuitInv"));
		markedItems=deserList(tag.getCompoundTag("markedItems"));
		markedFluid=deserListF(tag.getCompoundTag("markedFluid"));
		while(circuitInv.size()>circuitUpgrades)circuitInv.remove(circuitInv.size()-1);
		while(circuitInv.size()<circuitUpgrades)circuitInv.add(null);
		while(markedItems.size()>itemMEUpgrades)markedItems.remove(markedItems.size()-1);
		while(markedItems.size()<itemMEUpgrades)markedItems.add(null);
		while(markedFluid.size()>fluidMEUpgrades)markedFluid.remove(markedFluid.size()-1);
		while(markedFluid.size()<fluidMEUpgrades)markedFluid.add(null);
		
	}
	public void install(ItemStack heldItem) {
	int damage=heldItem.getItemDamage();
		if(damage==0){
			if(circuitUpgrades<3){
				
				circuitUpgrades++;
				heldItem.stackSize--;
				successInstall();
				
			}
		}
		if(damage==1){
			if(itemMEUpgrades+fluidMEUpgrades<4){
				
				itemMEUpgrades++;
				heldItem.stackSize--;
				successInstall();
				
			}
		}
		if(damage==2){
			if(itemMEUpgrades+fluidMEUpgrades<4){
				
				fluidMEUpgrades++;
				heldItem.stackSize--;
				successInstall();
				
			}
		}
		
		
		
		
	}
	@SuppressWarnings("unchecked")
	public void successInstall(){
				reinit();
				GT_Utility.sendSoundToPlayers(getBaseMetaTileEntity().getWorld(), SoundResource.IC2_TOOLS_WRENCH, 1.0F, -1,
						getBaseMetaTileEntity().getXCoord(),getBaseMetaTileEntity().getYCoord(),getBaseMetaTileEntity().getZCoord());
		    //close all GUIs of this hatch, because they have to be re-generated with new context
			try{
			if(!getBaseMetaTileEntity().getWorld().isRemote)
			getBaseMetaTileEntity().getWorld().playerEntities.forEach(s -> {
				EntityPlayer player = (EntityPlayer) s;
				if (player.openContainer instanceof ModularUIContainer) {
					ModularUIContainer m = (ModularUIContainer) player.openContainer;
					for (Widget w : m.getContext().getMainWindow().getChildren()) {
						if (w instanceof MarkerWidget&&player instanceof EntityPlayerMP) {
							if (((MarkerWidget) w).thiz == DualInputHatch.this) {
								((EntityPlayerMP) player).closeContainer();break;
							}

						}

					}

				}

			});
		    }catch(Exception e){e.printStackTrace();}
				
		
	}
	
}
@Override
public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
		float aX, float aY, float aZ) {
	if(aPlayer.getHeldItem()!=null&&aPlayer.getHeldItem().getItem()==MyMod.upgrades){
	shared.install(aPlayer.getHeldItem());
	return true;
	
}
	return super.onRightclick(aBaseMetaTileEntity, aPlayer, side, aX, aY, aZ);
}
@Override
public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
	  if (!aBaseMetaTileEntity.isClientSide()) 
	if(!shared.isDummy())
		  MyMod.net.sendTo(new UpgradesMessage(
			aBaseMetaTileEntity.getXCoord(),
			aBaseMetaTileEntity.getYCoord(),
			aBaseMetaTileEntity.getZCoord(),
		this), (EntityPlayerMP) aPlayer);
	return super.onRightclick(aBaseMetaTileEntity, aPlayer);
}

@Override
public void onBlockDestroyed() {
	shared.onDestroy();
	
}


public static class Net{
	public IGrid g;
	public IActionHost h;
	public Net(IGrid g,IActionHost h){
		this.g=g;
		this.h=h;
	}
}

public Net getNetwork(){
	//DO NOT do the same way that StorageBus does,to skip permission check
	IGregTechTileEntity self = getBaseMetaTileEntity();
	  final TileEntity te = self.getWorld().getTileEntity(
              self.getXCoord() + self.getFrontFacing().offsetX,
              self.getYCoord() + self.getFrontFacing().offsetY,
              self.getZCoord() + self.getFrontFacing().offsetZ);

		
	  Net g = null;
	if(te instanceof IPartHost){
		
	try {
		final Object part =((IPartHost) te).getPart(self.getFrontFacing().getOpposite());
		 if (part instanceof IInterfaceHost) {
			
          g=new Net(((IInterfaceHost) part).getGridNode(ForgeDirection.UP).getGrid(),
        		  (IInterfaceHost) part
        		  
        		  );
          
         }
	
	} catch (Exception e) {
	
	}
	}
	
	if(g==null){
	if(te instanceof IInterfaceHost){
		
		
			
			IGridNode n=((IInterfaceHost) te).getGridNode(ForgeDirection.UP);
			if(n!=null)
			g=new Net(n.getGrid(),((IInterfaceHost) te));
			
		
		}
		
	}
	
	if(g==null){
		Object optCover=this.getBaseMetaTileEntity().getComplexCoverDataAtSide(this.getBaseMetaTileEntity().getFrontFacing());
		if(optCover instanceof AECover.Data){
			
			IInterfaceHost iface = ((AECover.Data) optCover).getInterfaceOrNull();
			if(iface!=null){
				g=new Net(iface.getGridNode(ForgeDirection.UP).getGrid(),iface);
				
			}
		}
			
	}
	
	
	return g;

	
	
}


}
