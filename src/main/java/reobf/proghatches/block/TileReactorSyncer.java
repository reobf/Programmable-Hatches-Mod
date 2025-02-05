package reobf.proghatches.block;

import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.forge.PlayerMainInvWrapper;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import gregtech.api.gui.modularui.GTUITextures;
import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import ic2.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import reobf.proghatches.block.TileIOHub.UIFactory;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;

public class TileReactorSyncer extends TileEntity implements ITileWithModularUI {

    private boolean isDead;

    @Override
    public void validate() {
        super.validate();
        MyMod.callbacks.put(this, this::pretick);
    }

    boolean skipCycleZero;

    public void pretick() {
        if ((!isDead) && (!isInvalid())) {
            TileEntityNuclearReactorElectric reactor = findTarget();

            if (reactor != null) tick = reactor.updateTicker % 20;
            else tick = -1;

            int new_power = tick != -1 ? values[tick] : 0;
            if (cycles == 0 && skipCycleZero) {
                new_power = 0;
            }
            if (tick == 0) cycles++;
            if (power != new_power) {

                worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType(), 0);
                // schedule it, update it in World#tick, just in case something magic happens...
            }

            power = new_power;

        }
    }

    public int power() {
        if (power < 0) {
            return 0;
        }
        return power;
    }

    public int tick;
    public int power = -1;// ic2 reactor reset its tick value everytime world loads, so it's no point saving the value

    public TileEntityNuclearReactorElectric findTarget() {

        ForgeDirection dir = ForgeDirection.values()[this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord)];
        TileEntity te = this.worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);

        if (te instanceof TileEntityNuclearReactorElectric) return (TileEntityNuclearReactorElectric) te;
        if (te instanceof TileEntityReactorChamberElectric) {
            return ((TileEntityReactorChamberElectric) te).getReactor();
        }
        if (te instanceof TileEntityReactorAccessHatch) {
            Object possible = ((TileEntityReactorAccessHatch) te).getReactor();
            return possible instanceof TileEntityNuclearReactorElectric ? (TileEntityNuclearReactorElectric) possible
                : null;
        }
        return null;
    }

    int cycles;

    @Override
    public void invalidate() {
        cycles = 0;
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        cycles = 0;
        super.onChunkUnload();
        isDead = true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        isDead = false;
    }

    public static SlotGroup playerHotBarGroup(EntityPlayer player, IDrawable background) {
        PlayerMainInvWrapper wrapper = new PlayerMainInvWrapper(player.inventory);
        SlotGroup slotGroup = new SlotGroup();

        /*
         * for (int row = 0; row < 3; row++) {
         * for (int col = 0; col < 9; col++) {
         * SlotWidget slot = new SlotWidget(new BaseSlot(wrapper, col + (row + 1) * 9))
         * .setPos(new Pos2d(col * 18, row * 18));
         * slotGroup.addSlot(slot);
         * if (background != null) {
         * slot.setBackground(background);
         * }
         * }
         * }
         */

        for (int i = 0; i < 9; i++) {
            SlotWidget slot = new SlotWidget(new BaseSlot(wrapper, i)).setPos(new Pos2d(i * 18, 0));
            slotGroup.addSlot(slot);
            if (background != null) {
                slot.setBackground(background);
            }
        }
        return slotGroup;
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        return new UIFactory(buildContext).createWindow();

    }

    protected class UIFactory {

        private final UIBuildContext uiBuildContext;

        public UIFactory(UIBuildContext buildContext) {
            this.uiBuildContext = buildContext;
        }

        public ModularWindow createWindow() {
            ModularWindow.Builder builder = ModularWindow.builder(getGUIWidth(), getGUIHeight());
            builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
            // builder.setGuiTint(getUIBuildContext().getGuiColorization());
            if (doesBindPlayerInventory()) {
                builder.widget(
                    playerHotBarGroup(uiBuildContext.getPlayer(), null).setPos(new Pos2d(3, getGUIHeight() - 18 - 3)));
                // builder.bindPlayerInventory(null)

            }

            addTitleToUI(builder);
            addUIWidgets(builder);

            return builder.build();
        }

        /**
         * Override this to add widgets for your UI.
         */

        // IItemHandlerModifiable fakeInv=new ItemHandlerModifiable();

        /**
         * @param builderx
         */
        /**
         * @param builderx
         */
        protected void addUIWidgets(ModularWindow.Builder builderx) {

            builderx.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> skipCycleZero ? 1 : 0)
                    .setSetter(s -> skipCycleZero = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 1) return GTUITextures.OVERLAY_BUTTON_EXPORT;
                        return GTUITextures.OVERLAY_BUTTON_IMPORT;
                    })

                    .addTooltip(1, LangManager.translateToLocal("tile.reactor_syncer.skip.true"))
                    .addTooltip(0, LangManager.translateToLocal("tile.reactor_syncer.skip.false"))
                    .addTooltip(LangManager.translateToLocal("tile.reactor_syncer.skip.0"))
                    .addTooltip(LangManager.translateToLocal("tile.reactor_syncer.skip.1"))
                    .addTooltip(LangManager.translateToLocal("tile.reactor_syncer.skip.2"))
                    .setPos(3, getGUIHeight() - 60)

            );

            Scrollable builder = new Scrollable().setHorizontalScroll();
            for (int i = 0; i < 20; i++) {

                final int fi = i;
                builder.widget(
                    new TextFieldWidget().setGetterInt(() -> values[fi])
                        .setSetterInt(s -> {
                            values[fi] = Math.max(Math.min(s, 15), 0);
                            markDirty();
                        }

                        )

                        .setSynced(true, true)
                        .setPos(20 * fi, 0)
                        .setSize(20, 20)
                        .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2)));

                builder.widget(new ButtonWidget().setOnClick((a, b) -> {
                    values[fi] = 15;
                    markDirty();
                })
                    .setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_ARROW_GREEN_UP)
                    .setPos(20 * fi, 40)
                    .setSize(20, 20));
                builder.widget(new ButtonWidget().setOnClick((a, b) -> {
                    values[fi] = 0;
                    markDirty();
                })
                    .setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_ARROW_GREEN_DOWN)
                    .setPos(20 * fi, 60)
                    .setSize(20, 20));
                builder.widget(
                    new DrawableWidget().setDrawable(GTUITextures.PICTURE_RADIATION_WARNING)
                        .setPos(0, 20)
                        .setSize(20, 20));

                builder.widget(new TextWidget(fi + "").setPos(20 * fi + 3, 60 + 20));

            }

            builder.setPos(3 + 20 + 20, 3)
                .setSize(getGUIWidth() - 6 - 40, 80 + 10);
            builderx.widget(builder);
            builderx.widget(
                TextWidget.localised("tile.reactor_syncer.info.rs")
                    .setPos(3, 3 + 10));
            builderx.widget(
                TextWidget.localised("tile.reactor_syncer.info.update")
                    .setPos(3, 3 + 10 + 20));

            builderx.widget(
                TextWidget.localised("tile.reactor_syncer.info.max")
                    .setPos(3, 3 + 10 + 40));
            builderx.widget(
                TextWidget.localised("tile.reactor_syncer.info.min")
                    .setPos(3, 3 + 10 + 60));

        }

        public UIBuildContext getUIBuildContext() {
            return uiBuildContext;
        }

        /*
         * public boolean isCoverValid() { return !getUIBuildContext().getTile()
         * .isDead() && getUIBuildContext().getTile()
         * .getCoverBehaviorAtSideNew(getUIBuildContext().getCoverSide()) !=
         * GregTechAPI.sNoBehavior; }
         */

        protected void addTitleToUI(ModularWindow.Builder builder) {
            /*
             * ItemStack coverItem =
             * GTUtility.intToStack(getUIBuildContext().getCoverID()); if
             * (coverItem != null) { builder.widget( new
             * ItemDrawable(coverItem).asWidget() .setPos(5, 5) .setSize(16,
             * 16)) .widget( new
             * TextWidget(coverItem.getDisplayName()).setDefaultColor(
             * COLOR_TITLE.get()) .setPos(25, 9)); }
             */
        }

        protected int getGUIWidth() {
            return 176;
        }

        protected int getGUIHeight() {
            return 107 + 18 * 3 + 18;
        }

        protected boolean doesBindPlayerInventory() {
            return true;
        }

        protected int getTextColorOrDefault(String textType, int defaultColor) {
            return defaultColor;
        }

        protected final Supplier<Integer> COLOR_TITLE = () -> getTextColorOrDefault("title", 0x222222);
        protected final Supplier<Integer> COLOR_TEXT_GRAY = () -> getTextColorOrDefault("text_gray", 0x555555);
        protected final Supplier<Integer> COLOR_TEXT_WARN = () -> getTextColorOrDefault("text_warn", 0xff0000);
    }

    int[] values = new int[20];

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        for (int i = 0; i < 20; i++) {
            compound.setInteger("##" + i, values[i]);
        }
        compound.setBoolean("skipCycleZero", skipCycleZero);
        super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        for (int i = 0; i < 20; i++) {
            values[i] = compound.getInteger("##" + i);
        }
        skipCycleZero = compound.getBoolean("skipCycleZero");
        super.readFromNBT(compound);
    }
}
