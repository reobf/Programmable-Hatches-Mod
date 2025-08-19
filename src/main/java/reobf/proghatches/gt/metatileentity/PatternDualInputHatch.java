package reobf.proghatches.gt.metatileentity;

import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.input.Keyboard;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizon.gtnhlib.util.parsing.MathExpressionParser;
import com.gtnewhorizon.gtnhlib.util.parsing.MathExpressionParser.Context;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.NumberFormatMUI;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.MultiChildWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TabButton;
import com.gtnewhorizons.modularui.common.widget.TabContainer;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomNameObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.DualInputHatch.Net;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch.DA;
import reobf.proghatches.gt.metatileentity.bufferutil.ItemStackG;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;

public class PatternDualInputHatch extends BufferedDualInputHatch implements ICraftingProvider, IGridProxyable,
		ICustomNameObject, IInterfaceViewable, IPowerChannelState, IActionHost, IMultiplePatternPushable {

	public PatternDualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
			boolean mMultiFluid, int bufferNum) {
		super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

	}

	@Override
	public int rows() {
		return 4;
	}

	@Override
	public int rowSize() {
		return 9;
	}

	@Override
	public IInventory getPatterns() {
		return patternMapper;
	}

	IInventory patternMapper = new IInventory() {

		@Override
		public int getSizeInventory() {

			return pattern.length;
		}

		@Override
		public ItemStack getStackInSlot(int slotIn) {

			return pattern[slotIn];
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {

			try {
				if (pattern[index] != null) {
					ItemStack itemstack;

					if (pattern[index].stackSize <= count) {
						itemstack = pattern[index];
						pattern[index] = null;
						this.markDirty();
						return itemstack;
					} else {
						itemstack = pattern[index].splitStack(count);

						if (pattern[index].stackSize == 0) {
							pattern[index] = null;
						}

						this.markDirty();
						return itemstack;
					}
				} else {
					return null;
				}
			} finally {

				onPatternChange();
			}
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int index) {

			return null;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			pattern[index] = stack;
			onPatternChange();
		}

		@Override
		public String getInventoryName() {

			return "";
		}

		@Override
		public boolean hasCustomInventoryName() {

			return false;
		}

		// @Override
		// public int stack

		@Override
		public void markDirty() {

		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {

			return true;
		}

		@Override
		public void openInventory() {
		}

		@Override
		public void closeInventory() {
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {

			return true;
		}

		@Override
		public int getInventoryStackLimit() {

			return 1;
		}
	};

	@Override
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {

		return new ITexture[] { aBaseTexture, TextureFactory.of(supportsFluids()
				? BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER : BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUS) };

	}

	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		return new ITexture[] { aBaseTexture, TextureFactory.of(supportsFluids()
				? BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER : BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUS) };

	}

	public PatternDualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, int bufferNum,
			boolean sf, String... optional) {

		super(id, name, nameRegional, tier, mMultiFluid, bufferNum, (optional.length > 0 ? optional
				: reobf.proghatches.main.Config.get("PDIH" + (sf ? "" : "B"), ImmutableMap.of("bufferNum", bufferNum,
						"fluidSlots",
						16/* fluidSlots() */, /*
												 * "cap", format.format((int)
												 * (4000 * Math.pow(4, tier) /
												 * (mMultiFluid ? 4 : 1))),
												 */
						"mMultiFluid", mMultiFluid, "slots", Math.min(16, (1 + tier) * (tier
								+ 1))/*
										 * , "stacksize", (int) (64 *
										 * Math.pow(2, Math.max(tier - 3, 0)))
										 */))

		));
		if (sf != supportsFluids()) {

			throw new AssertionError();
		}
	}

	public int fluidSlots() {
		return supportsFluids()?16:0;

	}

	ItemStack[] pattern = new ItemStack[36];

	ButtonWidget createRefundButton(IWidgetBuilder<?> builder) {

		Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {

			PatternDualInputHatch.this.dirty = true;
			try {
				PatternDualInputHatch.this.refundAll();
			} catch (Exception e) {

				// e.printStackTrace();
			}
		}).setPlayClickSound(true).setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_EXPORT)

				.addTooltips(ImmutableList.of("Return all internally stored items back to AE"))

				.setPos(new Pos2d(getGUIWidth() - 18 - 3, 5 + 16 + 2)).setSize(16, 16);
		return (ButtonWidget) button;
	}

	MachineSource requestSource;

	private BaseActionSource getRequest() {

		if (requestSource == null)
			requestSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
		return requestSource;
	}

	private void refundAll() throws Exception {
		markDirty();
		dirty = true;
		BaseActionSource src = getRequest();
		IMEMonitor<IAEItemStack> sg = getProxy().getStorage().getItemInventory();
		abstract class Inv {

			abstract ItemStack[] geti();

			abstract FluidStack[] getf();
		}
		Consumer<Inv> consumer = inv -> {
			try {
				for (ItemStack itemStack : inv.geti()) {
					if (itemStack == null || itemStack.stackSize == 0)
						continue;
					IAEItemStack rest = Platform.poweredInsert(getProxy().getEnergy(), sg,
							AEApi.instance().storage().createItemStack(itemStack), src);
					itemStack.stackSize = rest != null && rest.getStackSize() > 0 ? (int) rest.getStackSize() : 0;
				}
				IMEMonitor<IAEFluidStack> fsg = getProxy().getStorage().getFluidInventory();
				for (FluidStack fluidStack : inv.getf()) {
					if (fluidStack == null || fluidStack.amount == 0)
						continue;
					IAEFluidStack rest = Platform.poweredInsert(getProxy().getEnergy(), fsg,
							AEApi.instance().storage().createFluidStack(fluidStack), src);
					fluidStack.amount = 0;
				}
				;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		inv0.stream().map(s -> new Inv() {

			@Override
			ItemStack[] geti() {
				return flat(s.mStoredItemInternal);
			}

			@Override
			FluidStack[] getf() {
				return flat(s.mStoredFluidInternal);
			}
		}).forEach(consumer);
		;
		consumer.accept(new Inv() {

			@Override
			ItemStack[] geti() {

				return mInventory;
			}

			@Override
			FluidStack[] getf() {

				return Arrays.stream(mStoredFluid).map(s -> s.getFluid()).toArray(FluidStack[]::new);
			}
		});

	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		buildContext.addSyncedWindow(88, this::createPatternWindow);

		builder.widget(createRefundButton(builder));
		builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
			if (widget.getContext().isClient() == false)
				widget.getContext().openSyncedWindow(88);
		}).setPlayClickSound(true).setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_PLUS_LARGE)
				.addTooltips(ImmutableList.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.pattern")))
				.setSize(16, 16)
				// .setPos(10 + 16 * 9, 3 + 16 * 2)
				.setPos(new Pos2d(getGUIWidth() - 18 - 3, 5 + 16 + 2 + 16 + 2)));

		super.addUIWidgets(builder, buildContext);
	}
	static AdaptableUITexture mode0 = AdaptableUITexture.of("proghatches", "gui/restrict_mode0", 18, 18, 1);
    
	int[] multiplier = new int[36];
	{Arrays.fill(multiplier, 1);}
public void refresh(){
	for(int i=0;i<patternItemCache.length;i++){
		patternItemCache[i]=null;
	}
	postMEPatternChange();
	
	
}
	protected ModularWindow createPatternWindow(final EntityPlayer player) {
		final int WIDTH = 18 * 4 + 6;
		final int HEIGHT = 18 * 9 + 6;
		final int PARENT_WIDTH = getGUIWidth();
		final int PARENT_HEIGHT = getGUIHeight();
		ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
		IDrawable tab1 = new ItemDrawable(  Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).get())
				.withFixedSize(18, 18, 4, 4);
		IDrawable tab2 = GTUITextures.OVERLAY_BUTTON_BATCH_MODE_OFF.withFixedSize(18, 18, 4, 4);;
		
		/*new ItemDrawable(GTOreDictUnificator.get(OrePrefixes.gearGt, Materials.Iron, 1))
				.withFixedSize(18, 18, 4, 4);*/
		IDrawable tab3 = GTUITextures.OVERLAY_BUTTON_BATCH_MODE_ON.withFixedSize(18, 18, 4, 4);;/*new ItemDrawable(GTOreDictUnificator.get(OrePrefixes.gearGt, Materials.Gold, 1))
				.withFixedSize(18, 18, 4, 4);*/
		
		
		
		
		
		
		
		TabContainer tab;
		builder.widget(tab = new TabContainer().setButtonSize(28, 32)
				.addTabButton(new TabButton(0)
						.setBackground(true,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 0f, 1f, 1 / 3f).getSubArea(0, 0,
										0.5f, 1f),
								tab1)
						.setBackground(false,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 0f, 1f, 1 / 3f).getSubArea(0.5f, 0,
										1f, 1f),
								tab1)
						.setPos(WIDTH - 3, -1).addTooltip("Patterns"))
				.addTabButton(new TabButton(1)
						.setBackground(true,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 1 / 3f, 1f, 2 / 3f).getSubArea(0, 0,
										0.5f, 1f),
								tab2)
						.setBackground(false,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 1 / 3f, 1f, 2 / 3f).getSubArea(0.5f,
										0, 1f, 1f),
								tab2)
						.setPos(WIDTH - 3, 28 - 1).addTooltip("Individual Multiplier Op."))
				.addTabButton(new TabButton(2)
						.setBackground(true,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 1 / 3f, 1f, 2 / 3f).getSubArea(0, 0,
										0.5f, 1f),
								tab3)
						.setBackground(false, ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 1 / 3f, 1f, 2 / 3f)
								.getSubArea(0.5f, 0, 1f, 1f), tab3)
						.setPos(WIDTH - 3, 56 - 1).addTooltip("Batch Multiplier Op.")));

		builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
		builder.setGuiTint(getGUIColorization());
		builder.setDraggable(true);
		builder.setPos((a, b) -> new Pos2d(PARENT_WIDTH + b.getPos().getX(), PARENT_HEIGHT * 0 + b.getPos().getY()));
		MultiChildWidget page1 = new MultiChildWidget();
		tab.addPage(page1);
		MultiChildWidget page2 = new MultiChildWidget();
		tab.addPage(page2);
		MultiChildWidget page3 = new MultiChildWidget();
		tab.addPage(page3);
		
		page3.addChild(new ButtonWidget().setOnClick((buttonId, doubleClick)->{
			for(int i=0;i<36;i++)
			{multiplier[i]*=2;
			multiplier[i]=Math.max(multiplier[i], 1);
			}
			refresh();
		}) .setSize(16, 16).setPos(3,3).setBackground(GTUITextures.BUTTON_STANDARD).addTooltip("x2")
		);
		page3.addChild(TextWidget.dynamicString(()->"x2").setPos(3+3, 3));
		page3.addChild(new ButtonWidget().setOnClick((buttonId, doubleClick)->{
			for(int i=0;i<36;i++)
			multiplier[i]=1;
			refresh();
		}) .setSize(16, 16).setPos(3+16,3).setBackground(GTUITextures.BUTTON_STANDARD).addTooltip("=1")
		);
		page3.addChild(TextWidget.dynamicString(()->"=2").setPos(3+3+16,3));
		page3.addChild(new ButtonWidget().setOnClick((buttonId, doubleClick)->{
			for(int i=0;i<36;i++)
			{multiplier[i]*=n;
			multiplier[i]=Math.max(multiplier[i], 1);
			}
			refresh();
		}) .setSize(16, 16).setPos(3,3+32).setBackground(GTUITextures.BUTTON_STANDARD).addTooltip("xN")
		);
		page3.addChild(TextWidget.dynamicString(()->"x"+n).setPos(3+3,3+32));
		page3.addChild(new ButtonWidget().setOnClick((buttonId, doubleClick)->{
			for(int i=0;i<36;i++)
			multiplier[i]=n;
			refresh();
		}) .setSize(16, 16).setPos(3+16,3+32).setBackground(GTUITextures.BUTTON_STANDARD).addTooltip("=N")
		);
		page3.addChild(TextWidget.dynamicString(()->"="+n).setPos(3+3+16,3+32));
		TextFieldWidget text_n;
		page3.addChild( (text_n=new TextFieldWidget()).setValidator(s->{
			try{
			Integer.valueOf(s);}catch(Exception e){return "1";}
			return s;
		}).setSetter(s -> {
			
			n = Integer.valueOf(s);
		
		refresh();})
				
				.setGetter(() -> n+"")
				
               .setTextAlignment(Alignment.Center)
                .setTextColor(Color.WHITE.normal).addTooltip("N=")
                .setSize(60, 18)
                .setPos(3, 3+32+18)
                .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD));
		
		page3.addChild(
				new TextWidget().setStringSupplier(()->{
					if(text_n==text_n.getContext().getCursor().getFocused()){return "Enter <Space> to update value";}
					return "";
				}).setPos(3, 3+32+18+18)
				
				
				/*
				TextWidget.dynamicString(()->{
					//if(text_n.isFocused()){return "Enter <Space> to update value";}
					System.out.println(text_n.getContext().getCursor().getFocused());
					System.out.println(text_n);
					return "";}).setPos(3, 3+32+18+18)*/
				);
		
		
		MappingItemHandler shared_handler = new MappingItemHandler(pattern, 0, 36);
		// use shared handler
		// or shift clicking a pattern in pattern slot will just transfer it to
		// another pattern slot
		// instead of player inventory!
		for (int i = 0; i < 36; i++) {
			final int ii = i;
			
			page2.addChild(new SlotWidget(new BaseSlot(shared_handler, i)) {
				@Override
				protected ItemStack getItemStackForRendering(Slot slotIn) {
					ItemStack stack = slotIn.getStack();
					if (stack == null || !(stack.getItem() instanceof ItemEncodedPattern)) {
						return stack;
					}
					ItemStack output = ((ItemEncodedPattern) stack.getItem()).getOutput(stack);
					return output != null ? output : stack;

				}
			}.disableInteraction().setPos((i % 4) * 18 + 3, (i / 4) * 18 + 3).setBackground(GTUITextures.SLOT_DARK_GRAY,
					GTUITextures.OVERLAY_SLOT_PATTERN_ME));

			page2.addChild(new TextFieldWidget()
					
					.setValidator(s->{
				try{
				Integer.valueOf(s);}catch(Exception e){return "1";}
				return s;
			}).setSetter(s -> {
				
				multiplier[ii] = Integer.valueOf(s);
			
			refresh();})
					
					.setGetter(() -> multiplier[ii]+"").setTextColor(Color.RED.bright(0))
					.setMaxLength(999)
				
					.setScrollBar()
					
					
					.setPos((i % 4) * 18 + 3, (i / 4) * 18 + 1)
					
					
					
					.setSize(18, 16)
					.setBackground());
			page1.addChild(new SlotWidget(new BaseSlot(shared_handler, i)

			) {

				@Override
				protected ItemStack getItemStackForRendering(Slot slotIn) {
					ItemStack stack = slotIn.getStack();
					if (stack == null || !(stack.getItem() instanceof ItemEncodedPattern)) {
						return stack;
					}
					ItemStack output = ((ItemEncodedPattern) stack.getItem()).getOutput(stack);
					return output != null ? output : stack;

				}
			}.setShiftClickPriority(-1).setFilter(itemStack -> itemStack.getItem() instanceof ICraftingPatternItem)
					.setChangeListener(() -> {
						onPatternChange();
					}).setPos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
					.setBackground(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_PATTERN_ME));
				
			page1.addChild(TextWidget
					.dynamicString(() -> 
					{
						
						String s=multiplier[ii]==1?"":(ps(multiplier[ii])+"");
						if(pattern[ii]==null)return s="§7"+s;
						
					return s;
					}
							)
					.setTextAlignment(Alignment.TopLeft)
					.setDefaultColor(Color.WHITE.normal)
					.setPos((i % 4) * 18 + 3, (i / 4) * 18 + 2)
					
					
					
					.setSize(36, 16)
					.setBackground());
		}

		return builder.build();
	}
	private static String ps(int amount){
		return numberFormatx.formatWithSuffix(amount);
		
	}
	  private static final NumberFormatMUI numberFormatx = new NumberFormatMUI();
	boolean needPatternSync;

	private void onPatternChange() {
		if (!getBaseMetaTileEntity().isServerSide())
			return;
		// we do not refund 'cause it's impossible to trace the item

		needPatternSync = true;
	}

	@Override
	public void gridChanged() {
		needPatternSync = true;
	}

	@Override
	public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {

		return false;
	}

	@Override
	public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {

		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection side, Fluid aFluid) {

		return false;
	}

	@Override
	public boolean canFill(ForgeDirection side, Fluid aFluid) {

		return false;
	}

	public class Inst extends PatternDualInputHatch {

		public Inst(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures, boolean mMultiFluid,
				int bufferNum) {
			super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
		}

		@Override
		public boolean supportsFluids() {
			return PatternDualInputHatch.this.supportsFluids();
		}

	}

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new Inst(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
	}

	@Override
	public void initTierBasedField() {
		if (supportsFluids())
			super.initTierBasedField();
	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		if (aNBT.hasKey("x") == false)
			return;
		additionalConnection = aNBT.getBoolean("additionalConnection");
		NBTTagCompound tag = aNBT.getCompoundTag("patternSlots");
		if (tag != null)
			for (int i = 0; i < pattern.length; i++) {
				pattern[i] = Optional.ofNullable(tag.getCompoundTag("i" + i)).map(ItemStack::loadItemStackFromNBT)
						.orElse(null);
			}
		customName = aNBT.getString("customName");

		getProxy().readFromNBT(aNBT);
		saved = aNBT.getLong("saved");
		super.loadNBTData(aNBT);
		multiplier=aNBT.getIntArray("multiplier" );
		if(multiplier.length<36)multiplier=new int[36];
		for(int i=0;i<multiplier.length;i++){
			multiplier[i]=Math.max(multiplier[i], 1);
		} restrictToInt=aNBT.getBoolean("restrictToInt" );
		updateValidGridProxySides();
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		aNBT.setBoolean("additionalConnection", additionalConnection);
		NBTTagCompound tag = new NBTTagCompound();// aNBT.getCompoundTag("patternSlots");

		for (int i = 0; i < pattern.length; i++) {
			final int ii = i;
			Optional.ofNullable(pattern[i]).map(s -> s.writeToNBT(new NBTTagCompound()))
					.ifPresent(s -> tag.setTag("i" + ii, s));
		}
		aNBT.setTag("patternSlots", tag);
		Optional.ofNullable(customName).ifPresent(s -> aNBT.setString("customName", s));
		getProxy().writeToNBT(aNBT);
		aNBT.setLong("saved", saved);
		aNBT.setIntArray("multiplier", multiplier);
		aNBT.setBoolean("restrictToInt", restrictToInt);
		super.saveNBTData(aNBT);
	}

	private void clearInv() {

		for (int i = 0; i < 16; i++)
			mInventory[i] = null;
		for (int i = 0; i < this.fluidSlots(); i++)
			mStoredFluid[i].setFluid(null);
		;

	}

	private boolean postMEPatternChange() {
		// don't post until it's active
		if (!getProxy().isActive())
			return false;
		try {
			getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, getProxy().getNode()));
		} catch (GridAccessException ignored) {
			return false;
		}
		return true;
	}

	long lastSync;

	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
		super.onPostTick(aBaseMetaTileEntity, aTimer);

		if (getBaseMetaTileEntity().isServerSide()) {
			if (needPatternSync && aTimer > lastSync + 100) {
				needPatternSync = !postMEPatternChange();
				lastSync = aTimer;
			}
			if (aTimer % 20 == 0) {
				getBaseMetaTileEntity().setActive(isActive());
			}
		}
	}

	@Override
	public int getInventoryStackLimit() {

		return Integer.MAX_VALUE;
	}
	public int n=1;
	public boolean skipActiveCheck;

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		if (!isActive() && !skipActiveCheck)
			return false;
		if (!isEmpty())
			return false;
		if (!supportsFluids()) {
			for (int i = 0; i < table.getSizeInventory(); ++i) {
				ItemStack itemStack = table.getStackInSlot(i);
				if (itemStack == null)
					continue;
				if (itemStack.getItem() instanceof ItemFluidPacket)
					return false;
			}
		}

		int items = 0;
		int fluids = 0;
		int size = table.getSizeInventory();
		for (int i = 0; i < size; i++) {
			ItemStack itemStack = table.getStackInSlot(i);
			if (itemStack == null)
				continue;
			if (itemStack.getItem() instanceof ItemFluidPacket) {
				fluids++;
				if (fluids > this.fluidSlots()) {
					clearInv();
					return false;
				}

				mStoredFluid[fluids - 1].setFluidDirect(ItemFluidPacket.getFluidStack(itemStack));

			} else {
				items++;
				if (items > 16) {
					clearInv();
					return false;
				}
				mInventory[items - 1] = itemStack;

			}
		}
		markDirty();
		dirty = true;
		// inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory)
		// classify();

		/*
		 * for (DualInvBuffer inv0 : this.sortByEmpty()) { if (inv0.full() ==
		 * false) if(inv0.recordRecipeOrClassify(this.mStoredFluid,
		 * mInventory)|| inv0.classify(this.mStoredFluid,
		 * mInventory,true))break; }
		 */

		/*
		 * Integer check = detailmap.get(patternDetails); if(check==null){
		 * currentID++; detailmap.put(patternDetails,currentID );
		 * check=currentID; }
		 */

		DualInvBuffer theBuffer = /* ((BufferedDualInputHatch) master). */classifyForce();
		if (theBuffer != null) {
			recordRecipe(theBuffer);
			theBuffer.onChange();
		}
		justHadNewItems = true;
		return true;
	}

	private boolean isEmpty() {
		for (ItemStack is : mInventory) {
			if (is != null && is.stackSize > 0)
				return false;
		}
		for (FluidTank is : mStoredFluid) {
			if (is.getFluidAmount() > 0)
				return false;
		}
		return true;
	}

	@Override
	public boolean isBusy() {
		return !isEmpty();
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		return getProxy().getNode();
	}

	private AENetworkProxy gridProxy;

	@Override
	public AENetworkProxy getProxy() {
		if (gridProxy == null) {
			gridProxy = new AENetworkProxy(this, "proxy",
					new ItemStack(GregTechAPI.sBlockMachines, 1, this.getBaseMetaTileEntity().getMetaTileID()), true);
			gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
			updateValidGridProxySides();
			if (getBaseMetaTileEntity().getWorld() != null)
				gridProxy.setOwner(getBaseMetaTileEntity().getWorld()
						.getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
		}

		return this.gridProxy;
	}

	@Override
	public void securityBreak() {
		// no op
	}

	@Override
	public String getName() {

		if (hasCustomName()) {
			return getCustomName();
		}
		StringBuilder name = new StringBuilder();
		if (getCrafterIcon() != null) {
			name.append(getCrafterIcon().getDisplayName());
		} else {
			name.append(getLocalName());// getinventoryname()
		}

		/*
		 * if (mInventory[SLOT_CIRCUIT] != null) { name.append(" - ");
		 * name.append(mInventory[SLOT_CIRCUIT].getItemDamage()); }
		 */

		for (ItemStack is : this.shared.getDisplayItems()) {
			name.append(" - ");

			if (is.getItem() != GTUtility.getIntegratedCircuit(0).getItem()) {
				name.append(is.getDisplayName());
				if (is.getItemDamage() > 0) {
					name.append("@" + is.getItemDamage());
				}
			} else {
				name.append(is.getItemDamage());
			}

			// if(is.stackSize>0){name.append("*"+is.stackSize);}
		}

		for (FluidStack is : this.shared.getDisplayFluid()) {
			name.append(" - ");
			name.append(is.getLocalizedName());
			// if(is.amount>0){name.append("*"+is.amount);}
		}

		return name.toString();
	}

	@Override
	public TileEntity getTileEntity() {
		return (TileEntity) getBaseMetaTileEntity();
	}

	@Override
	public boolean shouldDisplay() {

		return true;
	}

	String customName;
	private boolean additionalConnection;

	@Override
	public String getCustomName() {

		return customName;
	}

	@Override
	public boolean hasCustomName() {

		return customName != null && (!customName.equals(""));
	}

	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
			float aX, float aY, float aZ) {
		final ItemStack is = aPlayer.inventory.getCurrentItem();
		if (is != null && is.getItem() instanceof ToolQuartzCuttingKnife) {
			if (ForgeEventFactory.onItemUseStart(aPlayer, is, 1) <= 0)
				return false;
			IGregTechTileEntity te = getBaseMetaTileEntity();
			aPlayer.openGui(AppEng.instance(), GuiBridge.GUI_RENAMER.ordinal() << 5 | (side.ordinal()), te.getWorld(),
					te.getXCoord(), te.getYCoord(), te.getZCoord());
			return true;
		}
		return super.onRightclick(aBaseMetaTileEntity, aPlayer, side, aX, aY, aZ);
	}

	@Override
	public void setCustomName(String name) {
		customName = name;

	}

	@Override
	public DimensionalCoord getLocation() {

		return new DimensionalCoord(getTileEntity());
	}

	@Override
	public boolean isActive() {
		return getProxy() != null && getProxy().isActive();
	}

	ItemStack[] patternItemCache = new ItemStack[36];
	ICraftingPatternDetails[] patternDetailCache = new ICraftingPatternDetails[36];
public static class DA implements ICraftingPatternDetails{
public DA(ICraftingPatternDetails p,
int m){
	if(p==null)throw new NullPointerException();
	this.p=p;
	this.m=m;
	if(m<1)m=1;
}
	ICraftingPatternDetails p;
	int m;
	@Override
	public ItemStack getPattern() {
	
		ItemStack is = new ItemStack(MyMod.fakepattern);
		is.setTagCompound(new NBTTagCompound());
		is.getTagCompound().setByte("type", (byte) 3);
		is.getTagCompound().setTag("p", p.getPattern().writeToNBT(new NBTTagCompound()));
		is.getTagCompound().setInteger("m", m);
		 return is;
	}

	@Override
	public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
	
		return p.isValidItemForSlot(slotIndex, itemStack, world);
	}

	@Override
	public boolean isCraftable() {
	
		return p.isCraftable();
	}
	IAEItemStack[] i;
	
	public  IAEItemStack[] mul(IAEItemStack[] in){
		IAEItemStack[] ret=new IAEItemStack[in.length];
		for(int k=0;k<ret.length;k++){
			ret[k]=in[k];
			if(ret[k]!=null){
				ret[k]=ret[k].copy().setStackSize(ret[k].getStackSize()*m);
			}
			
		}
		return ret;
	}
	
	@Override
	public IAEItemStack[] getInputs() {
		if(i==null){i=mul(p.getInputs());
		
		}
		return i;
	}
	IAEItemStack[] ci;
	@Override
	public IAEItemStack[] getCondensedInputs() {
		if(ci==null){ci=mul(p.getCondensedInputs());}
		return ci;
	}IAEItemStack[] co;

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		if(co==null){co=mul(p.getCondensedOutputs());}
		return co;
	}
	IAEItemStack[] o;
	@Override
	public IAEItemStack[] getOutputs() {
		if(o==null){o=mul(p.getOutputs());}
		return o;
	}

	@Override
	public boolean canSubstitute() {
	
		return p.canBeSubstitute();
	}
	ItemStack so;
	@Override
	public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
		if(so==null){
			so=p.getOutput(craftingInv, world);
		if(so!=null){
			so=so.copy();
			so.stackSize*=m;
		}
		}
		return so;
	}

	@Override
	public int getPriority() {
		
		return p.getPriority();
	}

	@Override
	public void setPriority(int priority) {
		p.setPriority(priority);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final DA other = (DA) obj;
        if (this.p != null && other.p != null) {
            return this.p.equals(other.p) && this.m == other.m;
        }
        return false;
		
		}
		
		
		
			
		
	@Override
		public int hashCode() {
			
			return p.hashCode()+31*(m-1);
		}
}
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		if (!isActive())
			return;

		for (int index = 0; index < pattern.length; index++) {
			ItemStack slot = pattern[index];

			if (slot == null) {
				patternItemCache[index] = null;
				patternDetailCache[index] = null;
				continue;
			}

			if (patternItemCache[index] == pattern[index]) {// just compare
															// object id
				craftingTracker.addCraftingOption(this, patternDetailCache[index]);
				continue;
			}

			ICraftingPatternDetails details = null;
			try {
				details = ((ICraftingPatternItem) slot.getItem()).getPatternForItem(slot,
						this.getBaseMetaTileEntity().getWorld());
			} catch (Exception e) {
			}
			if (details == null) {
				GTMod.GT_FML_LOGGER.warn("Found an invalid pattern at " + getBaseMetaTileEntity().getCoords()
						+ " in dim " + getBaseMetaTileEntity().getWorld().provider.dimensionId);
				continue;
			}
			patternItemCache[index] = pattern[index];
			
			
			
			patternDetailCache[index] = multiplier[index]==1?details:new DA(details, multiplier[index]);
            craftingTracker.addCraftingOption(this,  patternDetailCache[index] );
			
		}

	}

	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
		super.onFirstTick(aBaseMetaTileEntity);
		getProxy().onReady();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
		return isOutputFacing(forgeDirection) ? AECableType.SMART : AECableType.NONE;
	}

	private void updateValidGridProxySides() {
		if (additionalConnection) {
			getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));
		} else {
			getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
		}
		// getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

	}

	@Override
	public void onFacingChange() {
		updateValidGridProxySides();
	}

	@Override
	public boolean isPowered() {
		return getProxy() != null && getProxy().isPowered();
	}

	@Override
	public ItemStack getCrafterIcon() {
		ItemStack is = this.getMachineCraftingIcon();
		return is == null ? new ItemStack(GregTechAPI.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID()) : is;
	}

	@Override
	public void onBlockDestroyed() {
		try {
			refundAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onBlockDestroyed();

		IGregTechTileEntity te = this.getBaseMetaTileEntity();
		World aWorld = te.getWorld();
		int aX = te.getXCoord();
		short aY = te.getYCoord();
		int aZ = te.getZCoord();

		for (int i = 0; i < pattern.length; i++) {
			final ItemStack tItem = pattern[i];
			if ((tItem != null) && (tItem.stackSize > 0)) {
				final EntityItem tItemEntity = new EntityItem(aWorld, aX + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
						aY + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F, aZ + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
						new ItemStack(tItem.getItem(), tItem.stackSize, tItem.getItemDamage()));
				if (tItem.hasTagCompound()) {
					tItemEntity.getEntityItem().setTagCompound((NBTTagCompound) tItem.getTagCompound().copy());
				}
				tItemEntity.motionX = (XSTR_INSTANCE.nextGaussian() * 0.05D);
				tItemEntity.motionY = (XSTR_INSTANCE.nextGaussian() * 0.25D);
				tItemEntity.motionZ = (XSTR_INSTANCE.nextGaussian() * 0.05D);
				aWorld.spawnEntityInWorld(tItemEntity);
				tItem.stackSize = 0;
				pattern[i] = null;
			}
		}
	}
	public long singleSlotLimit() {
		return restrictToInt?Integer.MAX_VALUE:Long.MAX_VALUE;//limitToIntMax ? Integer.MAX_VALUE : Long.MAX_VALUE;
	}
	 public boolean restrictToInt;

	    public long fluidLimit() {

	        return restrictToInt?Integer.MAX_VALUE:Long.MAX_VALUE;
	    }

	    public long itemLimit() {

	        return restrictToInt?Integer.MAX_VALUE:Long.MAX_VALUE;
	    }

	boolean createInsertion() {
		return false;
	}

	boolean showFluidLimit() {

		return false;
	}

	@Override
	public int getInventoryFluidLimit() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
			float aX, float aY, float aZ) {
		additionalConnection = !additionalConnection;
		updateValidGridProxySides();
		aPlayer.addChatComponentMessage(
				new ChatComponentTranslation("GT5U.hatch.additionalConnection." + additionalConnection));
		return true;
	}

	public Net getNetwork() {
try{
		return new Net(this.getGridNode(ForgeDirection.UP).getGrid(), this);}catch(Exception e){
			return null;
		}
	}

	@Override
	public IGridNode getActionableNode() {

		return this.getGridNode(ForgeDirection.UP);
	}

	public Object getTile() {
		return this.getBaseMetaTileEntity();
	}

	@Override
	public boolean allowsPatternOptimization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table, int maxTodo) {
		if (Config.fastPatternDualInput == false)
			return AZERO;
		if (maxTodo <= 0)
			return AZERO;
		if (!isActive() && !skipActiveCheck)
			return AZERO;
		if (!isEmpty())
			return AZERO;
		if (!supportsFluids()) {
			for (int i = 0; i < table.getSizeInventory(); ++i) {
				ItemStack itemStack = table.getStackInSlot(i);
				if (itemStack == null)
					continue;
				if (itemStack.getItem() instanceof ItemFluidPacket)
					return AZERO;
			}
		}

		int items = 0;
		int fluids = 0;
		int size = table.getSizeInventory();
		for (int i = 0; i < size; i++) {
			ItemStack itemStack = table.getStackInSlot(i);
			if (itemStack == null)
				continue;
			if (itemStack.getItem() instanceof ItemFluidPacket) {
				fluids++;
				if (fluids > this.fluidSlots()) {
					clearInv();
					return AZERO;
				}

				mStoredFluid[fluids - 1].setFluidDirect(ItemFluidPacket.getFluidStack(itemStack));

			} else {
				items++;
				if (items > 16) {
					clearInv();
					return AZERO;
				}
				mInventory[items - 1] = itemStack;

			}
		}
		markDirty();
		dirty = true;
		// classify();
		int suc = 0;

		// DualInvBuffer theBuffer=classifyForce();

		/*
		 * Integer check = detailmap.get(patternDetails); if(check==null){
		 * currentID++; detailmap.put(patternDetails,currentID );
		 * check=currentID; }
		 */

		DualInvBuffer theBuffer = /* ((BufferedDualInputHatch) master). */classifyForce();
		if (theBuffer != null)
			recordRecipe(theBuffer);

		// if(theBuffer!=null){
		suc++;
		maxTodo--;
		// }
		/*
		 * for (DualInvBuffer inv0 : this.sortByEmpty()) { if (inv0.full() ==
		 * false) if(inv0.recordRecipeOrClassify(this.mStoredFluid,
		 * mInventory)|| inv0.classify(this.mStoredFluid, mInventory, true) ){
		 * theBuffer=inv0;suc++;maxTodo--; break;} }
		 */
		if (theBuffer != null) {// if succeeded, it's safe to simply add to
								// stacksize to push more patterns
			int todo = Math.min(theBuffer.space()
			/*
			 * space() will return correct result here it assumes item/fluid
			 * type is correct
			 */
					, maxTodo);

			if (todo > 0) {
				for (int ix = 0; ix < theBuffer.i; ix++) {
					if (theBuffer.mStoredItemInternalSingle[ix] != null) {
						if (theBuffer.mStoredItemInternal[ix] == null) {
							theBuffer.mStoredItemInternal[ix] = ItemStackG
									.neo(theBuffer.mStoredItemInternalSingle[ix].copy());
							theBuffer.mStoredItemInternal[ix].stackSize(0);// circuit?
						}
						theBuffer.mStoredItemInternal[ix]
								.stackSizeInc(theBuffer.mStoredItemInternalSingle[ix].stackSize * todo);
					}
				}

				for (int ix = 0; ix < theBuffer.f; ix++) {
					if (theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount() > 0) {
						if (theBuffer.mStoredFluidInternal[ix].getFluidAmount() <= 0) {
							FluidStack zerof = theBuffer.mStoredFluidInternalSingle[ix].getFluid().copy();
							zerof.amount = 0;
							theBuffer.mStoredFluidInternal[ix].setFluid(zerof);

						}
						theBuffer.mStoredFluidInternal[ix]
								.amountAcc(theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount() * 1l * todo);
					}
				}
				suc += todo;
			}
			theBuffer.onChange();
		}

		saved += suc;

		justHadNewItems = true;
		return new int[] { suc };
	}

	long saved;

	@SideOnly(Side.CLIENT)
	@Override
	public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {

		super.getWailaBody(itemStack, currenttip, accessor, config);
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {

			currenttip.add(

					StatCollector.translateToLocalFormatted("proghatch.saved.statistic",
							accessor.getNBTData().getLong("saved")));
		}

	}

	@Override
	public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
			int z) {

		super.getWailaNBTData(player, tile, tag, world, x, y, z);
		tag.setLong("saved", saved);
	}

	@Override
	public boolean isInfBuffer() {

		return true;
	}
	@Override
	protected Builder createWindowEx(EntityPlayer player) {
		
		Builder builder = super.createWindowEx(player);
		
		builder.widget(new CycleButtonWidget().setToggle(() -> restrictToInt, (s) -> {
			restrictToInt = s;

		}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(/*TOOLTIP_DELAY*/5)
				.setPos(3 + 18 * 0, 3 + 18 * 1).setSize(18, 18)
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.restrictToInt.0"))
				.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.restrictToInt.1"))
			);
		return builder;
	}
}
