package reobf.proghatches.gt.metatileentity.meinput;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.util.Arrays;

import javax.management.RuntimeErrorException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import gregtech.common.tileentities.machines.MTEHatchInputME;

import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.IDoNotSleep;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregtech.api.modularui2.GTGuiTextures;
import reobf.proghatches.gt.metatileentity.util.IMEHatchOverrided;
import reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;

public class RestrictedInputHatchME extends MTEHatchInputME implements IDataCopyablePlaceHolder,IMEHatchOverrided,IDoNotSleep {

    public RestrictedInputHatchME(int aID, boolean autoPullAvailable, String aName, String aNameRegional) {
        super(aID, autoPullAvailable, aName, aNameRegional);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
        desc = reobf.proghatches.main.Config.get("RIHME", ImmutableMap.of());
    }

    public RestrictedInputHatchME(String aName, boolean autoPullAvailable, int aTier, String[] aDescription,
        ITexture[][][] aTextures) {
        super(aName, autoPullAvailable, aTier, aDescription, aTextures);

    }

    String[] desc;

    @Override
    public String[] getDescription() {

        return desc;
    }

    private static final int SLOT_COUNT = 16;

    /*public void updateInformationSlot(int index) {
        if (getBaseMetaTileEntity().isAllowedToWork() == false) {
        	MEHatchRefactor.setPulledFluid(this, index, null);
            //storedInformationFluids[index] = null;

        }
        if (index < 0 || index >= SLOT_COUNT) {
            return;
        }

        FluidStack fluidStack = MEHatchRefactor.getConfigFluid(this, index);//storedFluids[index];
        if (fluidStack == null) {
        	MEHatchRefactor.setPulledFluid(this, index, null);
           // storedInformationFluids[index] = null;
            return;
        }

        AENetworkProxy proxy = getProxy();
        if (proxy == null || !proxy.isActive()) {
        	MEHatchRefactor.setPulledFluid(this, index, null);
            //storedInformationFluids[index] = null;
            return;
        }

        try {
            IMEMonitor<IAEFluidStack> sg = proxy.getStorage()
                .getFluidInventory();
            IAEFluidStack request = AEFluidStack.create(fluidStack);
            request.setStackSize(Integer.MAX_VALUE);
            IAEFluidStack result = sg.extractItems(request, Actionable.SIMULATE, getRequestSource());
            FluidStack resultFluid = (result != null) ? result.getFluidStack() : null;
            if (resultFluid != null && resultFluid.amount > restrict) {
                resultFluid.amount = restrict;
            }
            if (resultFluid != null && resultFluid.amount < restrict_lowbound) {
                resultFluid = null;
            }
            if (resultFluid != null && restrict_lowbound > 0 && multiples == 1) {
                resultFluid.amount = (resultFluid.amount / restrict_lowbound) * restrict_lowbound;
            }
            if (resultFluid != null && restrict_lowbound > 0 && multiples == 2) {
                // s.stackSize=(s.stackSize/restrict_lowbound)*restrict_lowbound;

                resultFluid.amount = 1 << (31 - Integer.numberOfLeadingZeros(resultFluid.amount / restrict_lowbound));
                resultFluid.amount *= restrict_lowbound;
            }
            MEHatchRefactor.setPulledFluid(this, index, resultFluid);
            //storedInformationFluids[index] = resultFluid;
        } catch (final GridAccessException ignored) {}
    }
*/
    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        try {
            return super.endRecipeProcessing(controller);
        } finally {
            for (int index = 0; index < SLOT_COUNT; index++) {
                try {
					updateInformationSlot(index);
				} catch (Exception e) {
					if(!(e instanceof GridAccessException)){throw new RuntimeException(e);}
					//e.printStackTrace();
				}
            }
        }

    }

    BaseActionSource requestSource;

    private BaseActionSource getRequestSource() {
        if (requestSource == null) requestSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
        return requestSource;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new RestrictedInputHatchME(mName, false, mTier, mDescriptionArray, mTextures);

    }

    private static final int CONFIG_WINDOW_ID = 123456;

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {

        buildContext.addSyncedWindow(CONFIG_WINDOW_ID, this::createStackSizeConfigurationWindow);
        builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
            if (!widget.isClient()) {
                widget.getContext()
                    .openSyncedWindow(CONFIG_WINDOW_ID);
            }
        })
            .setPlayClickSound(true)
            .setBackground(() -> {
                if (autoPullFluidList) {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
                        GTUITextures.OVERLAY_BUTTON_AUTOPULL_ME };
                } else {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
                        GTUITextures.OVERLAY_BUTTON_AUTOPULL_ME_DISABLED };
                }
            })
            .addTooltips(Arrays.asList(StatCollector.translateToLocal("proghatches.restricted.configure")))
            .setSize(16, 16)
            .setPos(80, 10));
        // .widget(new FakeSyncWidget.BooleanSyncer(() -> autoPullFluidList, this::setAutoPullFluidList));
        builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
            if (clickData.mouseButton == 0) {
                if (!widget.isClient()) {

                    for (int index = 0; index < SLOT_COUNT; index++) {
                    	 try {
         					updateInformationSlot(index);
         				} catch (Exception e) {
         					if(!(e instanceof GridAccessException)){throw new RuntimeException(e);}
         					//e.printStackTrace();
         				}
                    }
                }
            }
        })
            .setBackground(() -> {
                {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD };
                }
            })
            .addTooltips(
                Arrays.asList(
                    StatCollector.translateToLocal("proghatches.restricted.refresh.0"),
                    StatCollector.translateToLocal("proghatches.restricted.refresh.1")))
            .setSize(16, 16)
            .setPos(80, 10 + 18));

        super.addUIWidgets(builder, buildContext);

    }

    int multiples;

    Widget createMultiplesModeButton(IWidgetBuilder<?> builder, int HEIGHT) {

        Widget button = new CycleButtonWidget().setLength(3)
            .addTooltip(0, LangManager.translateToLocal("proghatches.restricted.multiples.exact"))
            .addTooltip(1, LangManager.translateToLocal("proghatches.restricted.multiples"))
            .addTooltip(2, LangManager.translateToLocal("proghatches.restricted.multiples.alt"))
            .setGetter(() -> multiples)
            .setSetter(s -> multiples = s)

            .setBackground(() -> {
                if (multiples == 1) {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED, mode0 };
                }

            else if (multiples == 2) {
                return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED, mode1 };
            }

            else {
                return new IDrawable[] { GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF };
            }
            })

            .setTooltipShowUpDelay(TOOLTIP_DELAY)

            .setPos(new Pos2d(3, HEIGHT - 3 - 16))
            .setSize(16, 16);
        return button;
    }

    protected ModularWindow createStackSizeConfigurationWindow(final EntityPlayer player) {
        final int WIDTH = 78;
        final int HEIGHT = 80 + 18 + 18;
        final int PARENT_WIDTH = getGUIWidth();
        final int PARENT_HEIGHT = getGUIHeight();
        ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
        builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
        builder.setGuiTint(getGUIColorization());
        builder.setDraggable(true);
        builder.setPos(
            (size, window) -> Alignment.Center.getAlignedPos(size, new Size(PARENT_WIDTH, PARENT_HEIGHT))
                .add(
                    Alignment.TopRight.getAlignedPos(new Size(PARENT_WIDTH, PARENT_HEIGHT), new Size(WIDTH, HEIGHT))
                        .add(WIDTH - 3, 0)));
        builder.widget(
            TextWidget.localised("proghatches.restricted.bound.down")
                .setPos(3, 2)
                .setSize(74, 14 + 18))
            .widget(
                new NumericWidget().setSetter(val -> restrict_lowbound = (int) val)
                    .setGetter(() -> restrict_lowbound)
                    .setBounds(1, Integer.MAX_VALUE)
                    .setScrollValues(1, 4, 64)
                    .setTextAlignment(Alignment.Center)
                    .setTextColor(Color.WHITE.normal)
                    .setSize(70, 18)
                    .setPos(3, 18 + 18)
                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD));
        builder.widget(
            TextWidget.localised("proghatches.restricted.bound.up")
                .setPos(3, 42 + 18)
                .setSize(74, 14))
            .widget(
                new NumericWidget().setSetter(val -> restrict = (int) val)
                    .setGetter(() -> restrict)
                    .setBounds(1, Integer.MAX_VALUE)
                    .setScrollValues(1, 4, 64)
                    .setTextAlignment(Alignment.Center)
                    .setTextColor(Color.WHITE.normal)
                    .setSize(70, 18)
                    .setPos(3, 58 + 18)
                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD));
        builder.widget(createMultiplesModeButton(builder, HEIGHT));
        return builder.build();
    }

    // ===== MUI2 =====
    // GT's MTEHatchInputME already builds its GUI with MUI2, so the legacy MUI1 addUIWidgets()/
    // createStackSizeConfigurationWindow() above are dead code now (this hatch's extra config had
    // become unreachable in-game). Reuse GT's panel and append a config button + refresh button.
    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {
        ModularPanel panel = super.buildUI(data, syncManager, uiSettings);

        com.cleanroommc.modularui.api.IPanelHandler configPanel = syncManager
            .syncedPanel("restrict_config", true, (m, h) -> createConfigWindow2(m));
        panel.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>()
            .onMousePressed(mb -> {
                configPanel.openPanel();
                return configPanel.isPanelOpen();
            })
            .background(GTGuiTextures.BUTTON_STANDARD, GTGuiTextures.OVERLAY_BUTTON_AUTOPULL_ME)
            .tooltip(t -> t.addLine(com.cleanroommc.modularui.api.drawable.IKey
                .str(StatCollector.translateToLocal("proghatches.restricted.configure"))))
            .size(16, 16)
            .pos(80, 10));

        // refresh re-reads every stocked slot from the network; runs server-side, so route the click
        // through an InteractionSyncHandler. updateInformationSlot may raise GridAccessException.
        com.cleanroommc.modularui.value.sync.InteractionSyncHandler refresh =
            new com.cleanroommc.modularui.value.sync.InteractionSyncHandler().setOnMousePressed(d -> {
                for (int index = 0; index < SLOT_COUNT; index++) {
                    try {
                        updateInformationSlot(index);
                    } catch (Exception e) {
                        if (!(e instanceof GridAccessException)) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        syncManager.syncValue("restrict_refresh", refresh);
        panel.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>()
            .syncHandler(refresh)
            .background(GTGuiTextures.BUTTON_STANDARD)
            .tooltip(t -> {
                t.addLine(com.cleanroommc.modularui.api.drawable.IKey
                    .str(StatCollector.translateToLocal("proghatches.restricted.refresh.0")));
                t.addLine(com.cleanroommc.modularui.api.drawable.IKey
                    .str(StatCollector.translateToLocal("proghatches.restricted.refresh.1")));
            })
            .size(16, 16)
            .pos(80, 10 + 18));

        return panel;
    }

    protected ModularPanel createConfigWindow2(PanelSyncManager syncManager) {
        final int WIDTH = 78;
        final int HEIGHT = 80 + 18 + 18;
        ModularPanel builder = new ModularPanel("restrict_config");
        builder.size(WIDTH, HEIGHT);

        builder.child(com.cleanroommc.modularui.api.drawable.IKey
            .str(StatCollector.translateToLocal("proghatches.restricted.bound.down"))
            .asWidget()
            .pos(3, 2)
            .size(74, 14 + 18));
        com.cleanroommc.modularui.value.sync.IntSyncValue lowVal = new com.cleanroommc.modularui.value.sync.IntSyncValue(
            () -> restrict_lowbound,
            s -> restrict_lowbound = s).allowC2S();
        builder.child(new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget()
            .value(lowVal)
            .formatAsInteger(true)
            .numbersInt(1, Integer.MAX_VALUE)
            .setTextAlignment(com.cleanroommc.modularui.utils.Alignment.Center)
            .setTextColor(com.cleanroommc.modularui.utils.Color.WHITE.main)
            .size(70, 18)
            .pos(3, 18 + 18)
            .background(GTGuiTextures.BACKGROUND_TEXT_FIELD));

        builder.child(com.cleanroommc.modularui.api.drawable.IKey
            .str(StatCollector.translateToLocal("proghatches.restricted.bound.up"))
            .asWidget()
            .pos(3, 42 + 18)
            .size(74, 14));
        com.cleanroommc.modularui.value.sync.IntSyncValue upVal = new com.cleanroommc.modularui.value.sync.IntSyncValue(
            () -> restrict,
            s -> restrict = s).allowC2S();
        builder.child(new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget()
            .value(upVal)
            .formatAsInteger(true)
            .numbersInt(1, Integer.MAX_VALUE)
            .setTextAlignment(com.cleanroommc.modularui.utils.Alignment.Center)
            .setTextColor(com.cleanroommc.modularui.utils.Color.WHITE.main)
            .size(70, 18)
            .pos(3, 58 + 18)
            .background(GTGuiTextures.BACKGROUND_TEXT_FIELD));

        // multiples mode: 0 = off/exact, 1 = multiples, 2 = alt
        com.cleanroommc.modularui.drawable.UITexture mode0mui2 = com.cleanroommc.modularui.drawable.UITexture
            .fullImage("proghatches", "gui/restrict_mode0");
        com.cleanroommc.modularui.drawable.UITexture mode1mui2 = com.cleanroommc.modularui.drawable.UITexture
            .fullImage("proghatches", "gui/restrict_mode1");
        com.cleanroommc.modularui.value.sync.IntSyncValue mulVal = new com.cleanroommc.modularui.value.sync.IntSyncValue(
            () -> multiples,
            s -> multiples = s).allowC2S();
        builder.child(new com.cleanroommc.modularui.widgets.CycleButtonWidget()
            .stateCount(3)
            .value((com.cleanroommc.modularui.api.value.IIntValue<?>) mulVal)
            .stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
            .stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_POWER_SWITCH_OFF)
            .stateBackground(1, GTGuiTextures.BUTTON_STANDARD_PRESSED)
            .stateOverlay(1, mode0mui2)
            .stateBackground(2, GTGuiTextures.BUTTON_STANDARD_PRESSED)
            .stateOverlay(2, mode1mui2)
            .addTooltip(0, LangManager.translateToLocal("proghatches.restricted.multiples.exact"))
            .addTooltip(1, LangManager.translateToLocal("proghatches.restricted.multiples"))
            .addTooltip(2, LangManager.translateToLocal("proghatches.restricted.multiples.alt"))
            .pos(3, HEIGHT - 3 - 16)
            .size(16, 16));

        return builder;
    }

    int restrict = Integer.MAX_VALUE;
    int restrict_lowbound = 1;

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("restrict", restrict);
        aNBT.setInteger("restrict_l", restrict_lowbound);
        aNBT.setInteger("multiples", multiples);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        super.loadNBTData(aNBT);
        restrict = aNBT.getInteger("restrict");
        restrict_lowbound = aNBT.getInteger("restrict_l");
        multiples = aNBT.getInteger("multiples");
    }

    static AdaptableUITexture mode0 = AdaptableUITexture.of("proghatches", "gui/restrict_mode0", 18, 18, 1);
    static AdaptableUITexture mode1 = AdaptableUITexture.of("proghatches", "gui/restrict_mode1", 18, 18, 1);

    /*
     * @Override
     * public NBTTagCompound getCopiedData(EntityPlayer player) {
     * NBTTagCompound ret=super_getCopiedData(player,()->MethodHandles.lookup());
     * ret.setInteger("multiples", multiples);
     * ret.setInteger("restrict", restrict);
     * ret.setInteger("restrict_lowbound", restrict_lowbound);
     * return ret;
     * }
     * @Override
     * public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
     * boolean suc=super_pasteCopiedData(player, nbt,()->MethodHandles.lookup());
     * if(suc){
     * if(nbt.hasKey("multiples"))multiples=nbt.getInteger("multiples");
     * if(nbt.hasKey("restrict"))restrict=nbt.getInteger("restrict");
     * if(nbt.hasKey("restrict_lowbound"))restrict_lowbound=nbt.getInteger("restrict_lowbound");
     * }
     * return suc;
     * }
     * @Override
     * public String getCopiedDataIdentifier(EntityPlayer player) {
     * return super_getCopiedDataIdentifier(player,()->MethodHandles.lookup());
     * }
     */
    // Flattened + inlined from the former IDataCopyablePlaceHolderSuper compat layer:
    // our extra fields layered on top of the GT super's copied data, tagged with this type.
    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = super.getCopiedData(player);
        ret.setInteger("multiples", multiples);
        ret.setInteger("restrict", restrict);
        ret.setInteger("restrict_lowbound", restrict_lowbound);
        ret.setString("type", getCopiedDataIdentifier(player));
        return ret;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
        nbt = (NBTTagCompound) nbt.copy();
        if (nbt.hasKey("multiples")) multiples = nbt.getInteger("multiples");
        if (nbt.hasKey("restrict")) restrict = nbt.getInteger("restrict");
        if (nbt.hasKey("restrict_lowbound")) restrict_lowbound = nbt.getInteger("restrict_lowbound");
        nbt.setString("type", super.getCopiedDataIdentifier(player));
        return super.pasteCopiedData(player, nbt);
    }
    
    @Override
    public IAEStack qureyStorage(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src){
		
    	IAEStack result = thiz.extractItems(request, mode, src);
    	  FluidStack resultFluid = (result != null) ? ((IAEFluidStack) result).getFluidStack() : null;
          if (resultFluid != null && resultFluid.amount > restrict) {
              resultFluid.amount = restrict;
          }
          if (resultFluid != null && resultFluid.amount < restrict_lowbound) {
              resultFluid = null;
          }
          if (resultFluid != null && restrict_lowbound > 0 && multiples == 1) {
              resultFluid.amount = (resultFluid.amount / restrict_lowbound) * restrict_lowbound;
          }
          if (resultFluid != null && restrict_lowbound > 0 && multiples == 2) {
              // s.stackSize=(s.stackSize/restrict_lowbound)*restrict_lowbound;

              resultFluid.amount = 1 << (31 - Integer.numberOfLeadingZeros(resultFluid.amount / restrict_lowbound));
              resultFluid.amount *= restrict_lowbound;
          }

    	
    	return AEFluidStack.create(resultFluid);
    	
    }
    @Override
    public boolean override() {
    	return false;
    }@Override
    public void setConfigFluid(MTEHatchInputME thiz, int index, FluidStack bruh, FluidStack itemStack2) {
    	try {
			thiz.setSlotConfig(index, bruh);
		
			if(itemStack2!=null){
				Slot ww= this.slots[index];
				ww.extracted=itemStack2;
				ww.extractedAmount=itemStack2.amount;
		
			}} catch (Exception e) {
			throw new AssertionError(e);
		}
    	
    }

    public  void setConfigItem(MTEHatchInputBusME thiz, int index, ItemStack bruh,
    		ItemStack itemStack2) {
    	
    	
    }@Override
    public int minAutoPull() {
    	
    	return minAutoPullAmount;
    }
}