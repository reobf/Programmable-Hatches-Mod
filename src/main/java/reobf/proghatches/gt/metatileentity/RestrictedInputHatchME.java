package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.util.Arrays;

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
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.item.AEFluidStack;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolderSuper;
import reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;

public class RestrictedInputHatchME extends MTEHatchInputME implements IDataCopyablePlaceHolderSuper {

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

    public void updateInformationSlot(int index) {
        if (getBaseMetaTileEntity().isAllowedToWork() == false) {
            storedInformationFluids[index] = null;

        }
        if (index < 0 || index >= SLOT_COUNT) {
            return;
        }

        FluidStack fluidStack = storedFluids[index];
        if (fluidStack == null) {
            storedInformationFluids[index] = null;
            return;
        }

        AENetworkProxy proxy = getProxy();
        if (proxy == null || !proxy.isActive()) {
            storedInformationFluids[index] = null;
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
            storedInformationFluids[index] = resultFluid;
        } catch (final GridAccessException ignored) {}
    }

    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        try {
            return super.endRecipeProcessing(controller);
        } finally {
            for (int index = 0; index < SLOT_COUNT; index++) {
                updateInformationSlot(index);
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
                        updateInformationSlot(index);
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
    @Override
    public NBTTagCompound super_getCopiedData(EntityPlayer player) {

        return super.getCopiedData(player);
    }

    @Override
    public String super_getCopiedDataIdentifier(EntityPlayer player) {

        return super.getCopiedDataIdentifier(player);
    }

    @Override
    public boolean super_pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {

        return super.pasteCopiedData(player, nbt);
    }

    @Override
    public boolean impl_pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt.hasKey("multiples")) multiples = nbt.getInteger("multiples");
        if (nbt.hasKey("restrict")) restrict = nbt.getInteger("restrict");
        if (nbt.hasKey("restrict_lowbound")) restrict_lowbound = nbt.getInteger("restrict_lowbound");
        return true;
    }

    @Override
    public NBTTagCompound impl_getCopiedData(EntityPlayer player, NBTTagCompound ret) {
        ret.setInteger("multiples", multiples);
        ret.setInteger("restrict", restrict);
        ret.setInteger("restrict_lowbound", restrict_lowbound);
        return ret;
    }

    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        return IDataCopyablePlaceHolderSuper.super.getCopiedData(player);
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        return IDataCopyablePlaceHolderSuper.super.pasteCopiedData(player, nbt);
    }

    @Override
    public String getCopiedDataIdentifier(EntityPlayer player) {
        return IDataCopyablePlaceHolderSuper.super.getCopiedDataIdentifier(player);
    }
}
