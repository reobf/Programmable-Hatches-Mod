package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_INPUT_HATCH_ACTIVE;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUS;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_INPUT_FLUID_HATCH_ACTIVE;
import static kubatech.api.Variables.numberFormat;
import static kubatech.api.Variables.numberFormatScientific;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.api.widget.Widget.ClickData;
import com.gtnewhorizons.modularui.common.fluid.FluidStackTank;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.DynamicTextWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.MultiChildWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TabButton;
import com.gtnewhorizons.modularui.common.widget.TabContainer;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import codechicken.nei.NEIClientUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.enums.MaterialsUEVplus;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.objects.GTDualInputs;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.gui.modularui.widget.AESlotWidget;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import kubatech.api.enums.ItemList;
import kubatech.api.tea.TeaNetwork;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class StockingDualInputHatchME extends MTEHatchInputBus
		implements IDualInputHatch, IRecipeProcessingAwareDualHatch, IPowerChannelState,IGridProxyable

{

	public StockingDualInputHatchME(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures, boolean allowAuto2) {
		super(aName, aTier, 1, aDescription, aTextures);
		allowAuto=allowAuto2;
	}

	public StockingDualInputHatchME(int id, String name, String nameRegional, int tier,boolean a) {
		super(id, name, nameRegional, tier, 1, reobf.proghatches.main.Config.get(
                "SDIHME",  ImmutableMap.of()));
		Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id));
		allowAuto=a;
	}
	boolean allowAuto;

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new StockingDualInputHatchME(mName, mTier, mDescriptionArray, mTextures,allowAuto);
	}

	@Override
	public int getCircuitSlot() {

		return 0;
	}

	@Override
	public int getCircuitSlotX() {
		// TODO Auto-generated method stub
		return 153 - 1 - 18 * 4;
	}

	@Override
	public int getCircuitSlotY() {
		// TODO Auto-generated method stub
		return super.getCircuitSlotY() + 1;
	}

	private boolean autoPullItemList;
	private long minAutoPullStackSize;
	private int interval=1;

	public ItemStack updateInformationSlot(int aIndex, ItemStack aStack) {

		if (aStack == null) {
			inventoryHandlerDisplay.setStackInSlot(aIndex, null);
		} else {
			AENetworkProxy proxy = getProxy();
			if (!proxy.isActive()) {
				inventoryHandlerDisplay.setStackInSlot(aIndex, null);
				return null;
			}
			try {
				IMEMonitor<IAEItemStack> sg = proxy.getStorage().getItemInventory();
				IAEItemStack request = AEItemStack.create(i_mark[aIndex]);
				request.setStackSize(Integer.MAX_VALUE);
				IAEItemStack result = sg.extractItems(request, Actionable.SIMULATE, source);
				ItemStack s = (result != null) ? result.getItemStack() : null;
				// We want to track changes in any ItemStack to notify any
				// connected controllers to make a recipe
				// check early
				/*
				 * if (expediteRecipeCheck) { ItemStack previous =
				 * getStackInSlot(aIndex + SLOT_COUNT); if (s != null) {
				 * justHadNewItems = !ItemStack.areItemStacksEqual(s, previous);
				 * } }
				 */
				inventoryHandlerDisplay.setStackInSlot(aIndex, s);
				return s;
			} catch (final GridAccessException ignored) {
			}
		}

		return null;
	}

	public void updateInformationSlotF(int index) {

		FluidStack fluidStack = f_mark[index];
		if (fluidStack == null) {
			f_display[index] = null;
			return;
		}

		AENetworkProxy proxy = getProxy();
		if (proxy == null || !proxy.isActive()) {
			f_display[index] = null;
			return;
		}

		try {
			IMEMonitor<IAEFluidStack> sg = proxy.getStorage().getFluidInventory();
			IAEFluidStack request = AEFluidStack.create(fluidStack);
			request.setStackSize(Integer.MAX_VALUE);
			IAEFluidStack result = sg.extractItems(request, Actionable.SIMULATE, source);
			FluidStack resultFluid = (result != null) ? result.getFluidStack() : null;
			// We want to track if any FluidStack is modified to notify any
			// connected controllers to make a recipe check
			// early
			/*
			 * if (expediteRecipeCheck) { FluidStack previous =
			 * storedInformationFluids[index]; if (resultFluid != null) {
			 * justHadNewFluids = !resultFluid.isFluidEqual(previous); } }
			 */
			f_display[index] = resultFluid;
		} catch (final GridAccessException ignored) {
		}

	}

	private FluidStackTank createTankForFluidStack(FluidStack[] fluidStacks, int slotIndex, int capacity) {
		return new FluidStackTank(() -> fluidStacks[slotIndex], (stack) -> {
			if (getBaseMetaTileEntity().isServerSide()) {
				return;
			}

			fluidStacks[slotIndex] = stack;
		}, capacity);
	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		final SlotWidget[] aeSlotWidgets = new SlotWidget[16];
		builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);

		IDrawable tab1 = new ItemDrawable(gregtech.api.enums.ItemList.Hatch_Input_Bus_ME_Advanced.get(1))
				.withFixedSize(18, 18, 4, 4);
		IDrawable tab2 = new ItemDrawable(gregtech.api.enums.ItemList.Hatch_Input_ME_Advanced.get(1))
				.withFixedSize(18, 18, 4, 4);
		IDrawable tab3 = new ItemDrawable(GTOreDictUnificator.get(OrePrefixes.gearGt, Materials.Iron, 1))
				.withFixedSize(18, 18, 4, 4);

		builder.widget(new TabContainer().setButtonSize(28, 32)
				.addTabButton(new TabButton(0)
						.setBackground(true,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 0f, 1f, 1 / 3f).getSubArea(0, 0,
										0.5f, 1f),
								tab1)
						.setBackground(false,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 0f, 1f, 1 / 3f).getSubArea(0.5f, 0,
										1f, 1f),
								tab1)
						.setPos(getGUIWidth() - 4, 0))
				.addTabButton(new TabButton(1)
						.setBackground(true,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 1 / 3f, 1f, 2 / 3f).getSubArea(0, 0,
										0.5f, 1f),
								tab2)
						.setBackground(false,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 1 / 3f, 1f, 2 / 3f).getSubArea(0.5f,
										0, 1f, 1f),
								tab2)
						.setPos(getGUIWidth() - 4, 28))
				.addTabButton(new TabButton(2)
						.setBackground(true,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 1 / 3f, 1f, 2 / 3f).getSubArea(0, 0,
										0.5f, 1f),
								tab3)
						.setBackground(false,
								ModularUITextures.VANILLA_TAB_RIGHT.getSubArea(0f, 1 / 3f, 1f, 2 / 3f).getSubArea(0.5f,
										0, 1f, 1f),
								tab3)
						.setPos(getGUIWidth() - 4, 56))
				.addPage(
						new MultiChildWidget()
								.addChild(
										SlotGroup.ofItemHandler(inventoryHandlerDisplay, 4).startFromSlot(0)
												.endAtSlot(15).phantom(true).background(GTUITextures.SLOT_DARK_GRAY)
												.widgetCreator(slot -> aeSlotWidgets[slot
														.getSlotIndex()] = new AESlotWidget(slot).disableInteraction())
												.build().setPos(97, 9))

								.addChild(SlotGroup.ofItemHandler(inventoryHandlerMark, 4).startFromSlot(0)
										.endAtSlot(15).phantom(true)
										.slotCreator(index -> new BaseSlot(inventoryHandlerMark, index, true) {

											@Override
											public boolean isEnabled() {
												return !autoPullItemList && super.isEnabled();
											}
										}).widgetCreator(slot -> (SlotWidget) new SlotWidget(slot) {

											@Override
											protected void phantomClick(ClickData clickData, ItemStack cursorStack) {
												if (clickData.mouseButton != 0 || !getMcSlot().isEnabled())
													return;
												final int aSlotIndex = getMcSlot().getSlotIndex();
												if (cursorStack == null) {
													getMcSlot().putStack(null);
												} else {
													if (containsSuchStack(cursorStack))
														return;
													getMcSlot().putStack(GTUtility.copyAmount(1, cursorStack));
												}
												if (getBaseMetaTileEntity().isServerSide()) {
													final ItemStack newInfo = updateInformationSlot(aSlotIndex,
															cursorStack);
													aeSlotWidgets[getMcSlot().getSlotIndex()].getMcSlot()
															.putStack(newInfo);
												}
											}

											@Override
											public IDrawable[] getBackground() {
												IDrawable slot;
												if (autoPullItemList) {
													slot = GTUITextures.SLOT_DARK_GRAY;
												} else {
													slot = ModularUITextures.ITEM_SLOT;
												}
												return new IDrawable[] { slot, GTUITextures.OVERLAY_SLOT_ARROW_ME };
											}

											@Override
											public List<String> getExtraTooltip() {
												if (autoPullItemList) {
													return Collections.singletonList(StatCollector.translateToLocal(
															"GT5U.machines.stocking_bus.cannot_set_slot"));
												} else {
													return Collections.singletonList(StatCollector
															.translateToLocal("modularui.phantom.single.clear"));
												}
											}

											private boolean containsSuchStack(ItemStack tStack) {
												for (int i = 0; i < 16; ++i) {
													if (GTUtility.areStacksEqual(i_mark[i], tStack, false))
														return true;
												}
												return false;
											}
										}.dynamicTooltip(() -> {
											if (autoPullItemList) {
												return Collections.singletonList(StatCollector.translateToLocal(
														"GT5U.machines.stocking_bus.cannot_set_slot"));
											} else {
												return Collections.emptyList();
											}
										}).setUpdateTooltipEveryTick(true)).build().setPos(7, 9))

				).addPage(new MultiChildWidget().addChild(

						SlotGroup
								.ofFluidTanks(IntStream.range(0, 16)
										.mapToObj(index -> createTankForFluidStack(f_mark, index, 1))
										.collect(Collectors.toList()), 4)
								.phantom(true)
								.widgetCreator((slotIndex, h) -> (FluidSlotWidget) new FluidSlotWidget(h) {

									@Override
									protected void tryClickPhantom(ClickData clickData, ItemStack cursorStack) {
										if (clickData.mouseButton != 0 || autoPullItemList)
											return;

										FluidStack heldFluid = getFluidForPhantomItem(cursorStack);
										if (cursorStack == null) {
											f_mark[slotIndex] = null;
										} else {
											if (containsSuchStack(heldFluid))
												return;
											f_mark[slotIndex] = heldFluid;
										}
										if (getBaseMetaTileEntity().isServerSide()) {
											updateInformationSlotF(slotIndex);
											detectAndSendChanges(false);
										}
									}

									private boolean containsSuchStack(FluidStack tStack) {
										for (int i = 0; i < 16; ++i) {
											if (GTUtility.areFluidsEqual(f_mark[i], tStack, false)) {
												return true;
											}
										}
										return false;
									}

									@Override
									protected void tryScrollPhantom(int direction) {
									}

									@Override
									public IDrawable[] getBackground() {
										IDrawable slot;
										if (autoPullItemList) {
											slot = GTUITextures.SLOT_DARK_GRAY;
										} else {
											slot = ModularUITextures.FLUID_SLOT;
										}
										return new IDrawable[] { slot, GTUITextures.OVERLAY_SLOT_ARROW_ME };
									}

									@Override
									public void buildTooltip(List<Text> tooltip) {
										FluidStack fluid = getContent();
										if (fluid != null) {
											addFluidNameInfo(tooltip, fluid);

											if (!autoPullItemList) {
												tooltip.add(Text.localised("modularui.phantom.single.clear"));
											}
										} else {
											tooltip.add(Text.localised("modularui.fluid.empty")
													.format(EnumChatFormatting.WHITE));
										}

										if (autoPullItemList) {
											tooltip.add(Text.localised("GT5U.machines.stocking_bus.cannot_set_slot"));
										}
									}
								}.setUpdateTooltipEveryTick(true)).build().setPos(new Pos2d(7, 9))

				).addChild(SlotGroup.ofFluidTanks(IntStream.range(0, 16).mapToObj(index -> createTankForFluidStack(f_display, index, Integer.MAX_VALUE)).collect(Collectors.toList()), 4).phantom(true).widgetCreator((slotIndex, h) -> (FluidSlotWidget) new FluidSlotWidget(h) {

					@Override
					protected void tryClickPhantom(ClickData clickData, ItemStack cursorStack) {
					}

					@Override
					protected void tryScrollPhantom(int direction) {
					}

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
							tooltip.add(Text.localised("modularui.fluid.empty").format(EnumChatFormatting.WHITE));
						}
					}
				}.setUpdateTooltipEveryTick(true)).background(GTUITextures.SLOT_DARK_GRAY).controlsAmount(true).build()
						.setPos(new Pos2d(97, 9)))

				).addPage(
					new MultiChildWidget().addChild(TextWidget.localised("GT5U.machines.stocking_bus.refresh_time").setPos(3, 42).setSize(74, 14)).addChild(new NumericWidget().setSetter(val -> interval = (int) val).setGetter(() -> interval).setBounds(1, Integer.MAX_VALUE).setScrollValues(1, 4, 64).setTextAlignment(Alignment.Center).setTextColor(Color.WHITE.normal).setSize(70, 18).setPos(3, 3).setBackground(GTUITextures.BACKGROUND_TEXT_FIELD))
					.addChild(new ButtonWidget().setOnClick((clickData, widget) -> {
					if (clickData.mouseButton == 0) {
						if(allowAuto)
						setAutoPullItemList(!autoPullItemList);
					} else if (clickData.mouseButton == 1 && !widget.isClient()) {
						/*
						 * widget.getContext()
						 * .openSyncedWindow(CONFIG_WINDOW_ID);
						 */
					}
				}).setBackground(() -> {
					if (autoPullItemList) {
						return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
								GTUITextures.OVERLAY_BUTTON_AUTOPULL_ME };
					} else {
						return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
								GTUITextures.OVERLAY_BUTTON_AUTOPULL_ME_DISABLED };
					}
				}).addTooltips(
						Arrays.asList(StatCollector.translateToLocal("GT5U.machines.stocking_bus.auto_pull.tooltip.1"),
								StatCollector.translateToLocal("GT5U.machines.stocking_bus.auto_pull.tooltip.2")))
						.setSize(16, 16).setPos(80, 3))
					.addChild(new ButtonWidget().setOnClick((clickData, widget) -> {
						if (clickData.mouseButton == 0) {
						
							program=!program;
						} else if (clickData.mouseButton == 1 && !widget.isClient()) {
							/*
							 * widget.getContext()
							 * .openSyncedWindow(CONFIG_WINDOW_ID);
							 */
						}
					}).setBackground(() -> {
						if (program) {
							return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
									new ItemDrawable(GTUtility.getIntegratedCircuit(0))
									/*GTUITextures.OVERLAY_BUTTON_AUTOPULL_ME*/ };
						} else {
							return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
									new ItemDrawable(GTUtility.getIntegratedCircuit(0))};
						}
					}).addTooltips(
							Arrays.asList(StatCollector.translateToLocal("hatch.dualinput.stocking.autopull.program")
									))
							.setSize(16, 16).setPos(80, 3+20))
					
					
					
					
						
						))

		;
		builder.widget(new FakeSyncWidget.BooleanSyncer(() -> program,
				s->{program=s;}));
		builder.widget(new FakeSyncWidget.BooleanSyncer(() -> autoPullItemList,
				StockingDualInputHatchME.this::setAutoPullItemList));
		// return builder;
	}

	protected void setAutoPullItemList(boolean pullItemList) {

		autoPullItemList = pullItemList;
		if (!autoPullItemList) {
			for (int i = 0; i < 16; i++) {
				i_mark[i] = null;
				f_mark[i] = null;
			}
		} else {
			refreshItemList();
			refreshItemListF();
		}
		updateAllInformationSlots();
	}

	protected void refreshItemList() {
		AENetworkProxy proxy = getProxy();
		try {
			IMEMonitor<IAEItemStack> sg = proxy.getStorage().getItemInventory();
			Iterator<IAEItemStack> iterator = sg.getStorageList().iterator();
			int index = 0;
			while (iterator.hasNext() && index < 16) {
				IAEItemStack currItem = iterator.next();
				if (currItem.getStackSize() >= minAutoPullStackSize) {
					ItemStack itemstack = GTUtility.copyAmount(1, currItem.getItemStack());
					/*
					 * if (expediteRecipeCheck) { ItemStack previous =
					 * this.mInventory[index]; if (itemstack != null) {
					 * justHadNewItems =
					 * !ItemStack.areItemStacksEqual(itemstack, previous); } }
					 */
					this.i_mark[index] = itemstack;
					index++;
				}
			}
			for (int i = index; i < 16; i++) {
				i_mark[i] = null;
			}

		} catch (final GridAccessException ignored) {
		}
	}

	protected void refreshItemListF() {
		AENetworkProxy proxy = getProxy();
		try {
			IMEMonitor<IAEFluidStack> sg = proxy.getStorage().getFluidInventory();
			Iterator<IAEFluidStack> iterator = sg.getStorageList().iterator();
			int index = 0;
			while (iterator.hasNext() && index < 16) {
				IAEFluidStack currItem = iterator.next();
				if (currItem.getStackSize() >= minAutoPullStackSize) {
					FluidStack itemstack = GTUtility.copyAmount(1, currItem.getFluidStack());
					/*
					 * if (expediteRecipeCheck) { ItemStack previous =
					 * this.mInventory[index]; if (itemstack != null) {
					 * justHadNewItems =
					 * !ItemStack.areItemStacksEqual(itemstack, previous); } }
					 */
					this.f_mark[index] = itemstack;
					index++;
				}
			}
			for (int i = index; i < 16; i++) {
				f_mark[i] = null;
			}

		} catch (final GridAccessException ignored) {
		}
	}

	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
		if (getBaseMetaTileEntity().isServerSide()) {program();
			if (aTimer % interval == 0 && autoPullItemList) {
				refreshItemList();
				refreshItemListF();
			}
			if (aTimer % 20 == 0) {
				getBaseMetaTileEntity().setActive(isActive());
			}
		}

		super.onPostTick(aBaseMetaTileEntity, aTimer);
	};
	boolean program;
public void program(){
	
	if(program)
	try {
	
		
		for(IAEItemStack s:getProxy().getStorage().getItemInventory().getStorageList())
		{
		IAEItemStack ext = getProxy().getStorage().getItemInventory().extractItems(s, Actionable.MODULATE, source);
		if(ext!=null&&ext.getStackSize()>0){
			ItemStack item = ext.getItemStack();
			item.stackSize=0;
			
			ItemStack circuit = ItemProgrammingCircuit.getCircuit(item).orElse(null);
			this.setInventorySlotContents(getCircuitSlot(),circuit);
			
			this.getProxy().getNode().getGrid().getMachines(this.getClass()).forEach(m->{
				StockingDualInputHatchME thiz=(StockingDualInputHatchME) m.getMachine();
				if(thiz.program){
					thiz.setInventorySlotContents(thiz.getCircuitSlot(),circuit);
					
				}
				
				
				
			});;
			
		}
		}
		

	
	} catch (GridAccessException e) {
	}
}
	MachineSource source = new MachineSource(((IActionHost) getBaseMetaTileEntity()));
	boolean recipe;

	@Override
	public void startRecipeProcessing() {
		recipe = true;program();
		for (int i = 0; i < 16; i++) {
			i_shadow[i] = null;
			i_saved[i] = 0;
			if (i_mark[i] != null) {

				try {
					IAEItemStack possible = getProxy().getStorage().getItemInventory().extractItems(
							AEItemStack.create(i_mark[i]).setStackSize(Integer.MAX_VALUE), Actionable.SIMULATE, source);
					i_shadow[i] = possible == null ? null : possible.getItemStack();
					if (i_shadow[i] != null)
						i_saved[i] = i_shadow[i].stackSize;

				} catch (GridAccessException e) {
				}
			}
		}
		for (int i = 0; i < 16; i++) {
			f_shadow[i] = null;
			f_saved[i] = 0;
			if (f_mark[i] != null) {

				try {
					IAEFluidStack possible = getProxy().getStorage().getFluidInventory().extractItems(
							AEFluidStack.create(f_mark[i]).setStackSize(Integer.MAX_VALUE), Actionable.SIMULATE,
							source);
					f_shadow[i] = possible == null ? null : possible.getFluidStack();
					if (f_shadow[i] != null)
						f_saved[i] = f_shadow[i].amount;

				} catch (GridAccessException e) {
				}
			}
		}
	}

	@Override
	public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
		recipe = false;
		for (int i = 0; i < 16; i++) {
			int current = i_shadow[i] == null ? 0 : i_shadow[i].stackSize;
			int original = i_saved[i];
			if (current > original) {
				throw new AssertionError("?");
			}
			if (current < 0) {
				throw new AssertionError("??");
			}
			if (current < original) {
				int delta = original - current;
				if (i_mark[i] == null) {
					MyMod.LOG.fatal("marked item missing!");
					controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
					return SimpleCheckRecipeResult.ofFailurePersistOnShutdown("stocking_bus_fail_extraction");
				}
				IAEItemStack toextract = AEItemStack.create(i_mark[i]).setStackSize(delta);
				try {
					IAEItemStack get = getProxy().getStorage().getItemInventory().extractItems(toextract,
							Actionable.MODULATE, source);
					if (get == null || get.getStackSize() != get.getStackSize()) {
						MyMod.LOG.fatal("cannot extract!");
						controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
						return SimpleCheckRecipeResult.ofFailurePersistOnShutdown("stocking_bus_fail_extraction");
					}
				} catch (GridAccessException e) {
					e.printStackTrace();
					controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
					return SimpleCheckRecipeResult.ofFailurePersistOnShutdown("stocking_bus_fail_extraction");
				}

			}

		}

		for (int i = 0; i < 16; i++) {
			int current = f_shadow[i] == null ? 0 : f_shadow[i].amount;
			int original = f_saved[i];
			if (current > original) {
				throw new AssertionError("?");
			}
			if (current < 0) {
				throw new AssertionError("??");
			}
			if (current < original) {
				int delta = original - current;
				if (f_mark[i] == null) {
					MyMod.LOG.fatal("marked fluid missing!");
					controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
					return SimpleCheckRecipeResult.ofFailurePersistOnShutdown("stocking_bus_fail_extraction");
				}
				IAEFluidStack toextract = AEFluidStack.create(f_mark[i]).setStackSize(delta);
				try {
					IAEFluidStack get = getProxy().getStorage().getFluidInventory().extractItems(toextract,
							Actionable.MODULATE, source);
					if (get == null || get.getStackSize() != get.getStackSize()) {
						MyMod.LOG.fatal("cannot extract!");
						controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
						return SimpleCheckRecipeResult.ofFailurePersistOnShutdown("stocking_bus_fail_extraction");
					}
				} catch (GridAccessException e) {
					e.printStackTrace();
					controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
					return SimpleCheckRecipeResult.ofFailurePersistOnShutdown("stocking_bus_fail_extraction");
				}

			}

		}

		updateAllInformationSlots();
		return CheckRecipeResultRegistry.SUCCESSFUL;
	}

	protected void updateAllInformationSlots() {
		for (int index = 0; index < 16; index++) {
			updateInformationSlot(index, i_mark[index]);
		}

		for (int index = 0; index < 16; index++) {
			updateInformationSlotF(index/* , f_mark[index] */);
		}

	}

	ItemStack[] i_mark = new ItemStack[16];
	ItemStack[] i_shadow = new ItemStack[16];
	ItemStack[] i_display = new ItemStack[16];
	int[] i_saved = new int[16];
	FluidStack[] f_mark = new FluidStack[16];
	FluidStack[] f_shadow = new FluidStack[16];
	FluidStack[] f_display = new FluidStack[16];
	int[] f_saved = new int[16];

	@Override
	public boolean justUpdated() {
		return false;
	}

	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

		super.onFirstTick(aBaseMetaTileEntity);
		getProxy().onReady();

	}

	@Override
	public Iterator<? extends IDualInputInventory> inventories() {
		if (!recipe) {
			return (Iterator<? extends IDualInputInventory>) new ArrayList().iterator();// huh...
		}

		return isEmpty() ? (Iterator<? extends IDualInputInventory>) new ArrayList().iterator() : getItr();
	}

	public boolean isEmpty() {
		for (ItemStack i : i_shadow) {
			if (i != null && i.stackSize > 0)
				return false;
		}
		for (FluidStack i : f_shadow) {
			if (i != null && i.amount > 0)
				return false;
		}
		return true;
	}

	private Iterator<? extends IDualInputInventory> getItr() {

		IDualInputInventory xx = new IDualInputInventory() {

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public ItemStack[] getItemInputs() {
				return DualInputHatch.filterStack.apply(i_shadow, new ItemStack[] { getStackInSlot(0) });
			}

			@Override
			public FluidStack[] getFluidInputs() {
				return DualInputHatch.asFluidStack.apply(f_shadow);
			}

			@Override
			public GTDualInputs getPatternInputs() {
				return new GTDualInputs() {
					{
						inputItems = getItemInputs();
					}
					{
						inputFluid = getFluidInputs();
					}
				};
			}
		};

		return ImmutableSet.of(xx).iterator();
	}

	@Override
	public Optional<IDualInputInventory> getFirstNonEmptyInventory() {
		if (!recipe) {
			return Optional.empty();// huh...
		}
		Iterator<? extends IDualInputInventory> x = inventories();

		return x.hasNext() ? Optional.of(x.next()) : Optional.empty();
	}

	@Override
	public boolean supportsFluids() {
		return true;
	}

	@Override
	public ItemStack[] getSharedItems() {

		return new ItemStack[0];
	}

	@Override
	public void setProcessingLogic(ProcessingLogic pl) {
	}

	AENetworkProxy gridProxy;

	@Override
	public AENetworkProxy getProxy() {

		if (gridProxy == null) {
			if (getBaseMetaTileEntity() instanceof IGridProxyable) {
				gridProxy = new AENetworkProxy(this, "proxy",
						new ItemStack(GregTechAPI.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID()), true);
				gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
				updateValidGridProxySides();
				if (getBaseMetaTileEntity().getWorld() != null)
					gridProxy.setOwner(getBaseMetaTileEntity().getWorld()
							.getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
			}
		}
		return this.gridProxy;
	}

	@Override
	public boolean isPowered() {

		return getProxy().isPowered();
	}

	@Override
	public boolean isActive() {

		return getProxy().isActive();
	}

	boolean additionalConnection;

	protected void updateValidGridProxySides() {

		if (additionalConnection) {
			getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));
		} else {
			getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
		}
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {aNBT.setBoolean("allowAuto",allowAuto);
		getProxy().writeToNBT(aNBT);
		super.saveNBTData(aNBT);
		NBTTagList nbtTagList = new NBTTagList();
		for (int i = 0; i < 16; i++) {
			FluidStack fluidStack = f_mark[i];
			if (fluidStack == null) {
				continue;
			}
			NBTTagCompound fluidTag = fluidStack.writeToNBT(new NBTTagCompound());
			if (f_mark[i] != null)
				fluidTag.setInteger("informationAmount", f_mark[i].amount);
			nbtTagList.appendTag(fluidTag);
		}
		aNBT.setTag("storedFluids", nbtTagList);

		nbtTagList = new NBTTagList();
		for (int i = 0; i < 16; i++) {
			ItemStack fluidStack = i_mark[i];
			if (fluidStack == null) {
				continue;
			}
			NBTTagCompound fluidTag = fluidStack.writeToNBT(new NBTTagCompound());
			if (i_mark[i] != null)
				fluidTag.setInteger("informationAmount", i_mark[i].stackSize);
			nbtTagList.appendTag(fluidTag);
		}
		aNBT.setTag("storedItems", nbtTagList);

		int[] sizesf = new int[16];
		for (int i = 0; i < 16; ++i)
			sizesf[i] = f_display[i] == null ? 0 : f_display[i].amount;
		aNBT.setIntArray("sizesF", sizesf);
		int[] sizes = new int[16];
		for (int i = 0; i < 16; ++i)
			sizes[i] = i_display[i] == null ? 0 : i_display[i].stackSize;
		aNBT.setIntArray("sizes", sizes);
		aNBT.setBoolean("program", program);
		aNBT.setBoolean("autoPull", autoPullItemList);
		aNBT.setInteger("interval", interval);

		getProxy().writeToNBT(aNBT);
	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {allowAuto=aNBT.getBoolean("allowAuto" );
		getProxy().readFromNBT(aNBT);
		super.loadNBTData(aNBT);
		if (aNBT.hasKey("storedFluids")) {
			NBTTagList nbtTagList = aNBT.getTagList("storedFluids", 10);
			int c = Math.min(nbtTagList.tagCount(), 16);
			for (int i = 0; i < c; i++) {
				NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
				FluidStack fluidStack = GTUtility.loadFluid(nbtTagCompound);
				f_mark[i] = fluidStack;

				if (nbtTagCompound.hasKey("informationAmount")) {
					int informationAmount = nbtTagCompound.getInteger("informationAmount");
					f_mark[i] = GTUtility.copyAmount(informationAmount, fluidStack);
				}
			}
		}

		if (aNBT.hasKey("storedItems")) {
			NBTTagList nbtTagList = aNBT.getTagList("storedItems", 10);
			int c = Math.min(nbtTagList.tagCount(), 16);
			for (int i = 0; i < c; i++) {
				NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
				ItemStack fluidStack = GTUtility.loadItem(nbtTagCompound);
				i_mark[i] = fluidStack;

				if (nbtTagCompound.hasKey("informationAmount")) {
					int informationAmount = nbtTagCompound.getInteger("informationAmount");
					i_mark[i] = GTUtility.copyAmount(informationAmount, fluidStack);
				}
			}
		}

		if (aNBT.hasKey("sizesF")) {
			int size[] = aNBT.getIntArray("sizesF");
			for (int i = 0; i < 16; i++) {
				if (f_mark[i] != null) {
					f_display[i] = f_mark[i].copy();
					f_display[i].amount = size[i];

				}

			}
		}

		if (aNBT.hasKey("sizes")) {
			int size[] = aNBT.getIntArray("sizes");
			for (int i = 0; i < 16; i++) {
				if (i_mark[i] != null) {
					i_display[i] = i_mark[i].copy();
					i_display[i].stackSize = size[i];

				}

			}
		}
		program=aNBT.getBoolean("program");
		interval = aNBT.getInteger("interval");
		autoPullItemList = aNBT.getBoolean("autoPull");
	}

	public IItemHandlerModifiable inventoryHandlerMark = new ItemStackHandler(i_mark);;
	public IItemHandlerModifiable inventoryHandlerDisplay = new ItemStackHandler(i_display);

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		
		return getProxy().getNode();
	}

	@Override
	public void securityBreak() {
	
		
	}

	@Override
	public DimensionalCoord getLocation() {
	
		return new DimensionalCoord((TileEntity)this.getBaseMetaTileEntity());
	};    
	@Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
		return new ITexture[] { aBaseTexture,
	            TextureFactory.of(MyMod.iohub ,MyMod.iohub.magicNO_overlay_dual_active) };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture,
            TextureFactory.of( MyMod.iohub,MyMod.iohub.magicNO_overlay_dual) };
    }

}
