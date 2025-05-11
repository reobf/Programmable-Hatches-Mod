package reobf.proghatches.gt.cover;

import static gregtech.api.enums.Mods.GregTech;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;

import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import gregtech.api.covers.CoverContext;
import gregtech.api.gui.modularui.CoverUIBuildContext;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IMachineProgress;
import gregtech.common.covers.redstone.CoverAdvancedRedstoneReceiverBase.GateMode;
import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;
import gregtech.common.gui.mui1.cover.CoverUIFactory;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.eucrafting.CoverBehaviorBase;
import reobf.proghatches.eucrafting.ISer;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.util.ProghatchesUtil;

public class WirelessControlCover
    extends CoverBehaviorBase<WirelessControlCover.Data> /* implements IControlsWorkCover */ {

    @Override
    public ModularWindow createWindow(CoverUIBuildContext buildContext) {
        return new WirelessCCUIFactory(buildContext).createWindow();
    }

    public WirelessControlCover(CoverContext context, ITexture t) {
        super(context, WirelessControlCover.Data.class, t);

    }

    // @Override
    public boolean useModularUI() {

        return true;
    }

    @Override
    public boolean hasCoverGUI() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public int getDefaultTickRate() {

        return 1;
    }

    @Override
    public void doCoverThings(byte x, long aTimer) {
        getTile().markDirty();
        byte aInputRedstone = getRedstone(coverData, getTile());
        if (coverData.invert) aInputRedstone = (byte) (15 - aInputRedstone);

        /*
         * if (!makeSureOnlyOne(side, aTileEntity))
         * return d;
         */
        if (getTile() instanceof IMachineProgress) {
            IMachineProgress machine = (IMachineProgress) getTile();
            if (coverData.safe == false && coverData.crashed == false) {
                if ((aInputRedstone > 0)) {
                    if (!machine.isAllowedToWork()) machine.enableWorking();
                } else if (machine.isAllowedToWork()) machine.disableWorking();
                // machine.setWorkDataValue(aInputRedstone);
            } else if (coverData.crashed) {
                machine.disableWorking();
            } else {
                if (machine.wasShutdown()) {
                    machine.disableWorking();
                    /*
                     * if (!mPlayerNotified) { EntityPlayer player = lastPlayer
                     * == null ? null : lastPlayer.get(); if (player != null) {
                     * lastPlayer = null; mPlayerNotified = true;
                     * GTUtility.sendChatToPlayer( player,
                     * aTileEntity.getInventoryName() + "at " + String.format(
                     * "(%d,%d,%d)", aTileEntity.getXCoord(),
                     * aTileEntity.getYCoord(), aTileEntity.getZCoord()) +
                     * " shut down."); } }
                     */
                    coverData.crashed = true;
                    return;
                } else {
                    coverData.safe = false;
                    doCoverThings(x, aTimer);
                    coverData.safe = true;
                    return;
                }
            }
        }
        return;
    }

    private byte getRedstone(Data d, ICoverable aTileEntity) {

        return ProghatchesUtil.getSignalAt(
            d.privateFreq
                ? (d.useMachineOwnerUUID
                    ? (aTileEntity instanceof IGregTechTileEntity ? ((IGregTechTileEntity) aTileEntity).getOwnerUuid()
                        : null)
                    : d.user)
                : null

            ,
            (int) d.freq,
            GateMode.values()[d.gateMode]);

    }

    public class Data implements ISer {

        boolean privateFreq;
        boolean invert;
        boolean safe = true;
        boolean crashed;

        boolean useMachineOwnerUUID;

        public Data() {}

        public Data(int freq, boolean isPrivate, UUID u) {
            this.freq = freq;
            this.useMachineOwnerUUID = isPrivate;
            user = u;
        }

        int freq;
        UUID user;
        public int gateMode;

        @Override
        public ISer copy() {

            Data d = new Data(freq, useMachineOwnerUUID, user);
            d.crashed = crashed;
            d.invert = invert;
            d.safe = safe;
            d.privateFreq = privateFreq;
            d.gateMode = gateMode;
            return d;
        }

        @Override
        public NBTBase saveDataToNBT() {
            NBTTagCompound t = new NBTTagCompound();
            t.setBoolean("privateFreq", privateFreq);
            t.setInteger("gateMode", gateMode);
            t.setBoolean("invert", invert);
            t.setBoolean("safe", safe);
            t.setBoolean("crashed", crashed);
            t.setInteger("freq", freq);
            t.setBoolean("isPrivate", useMachineOwnerUUID);
            t.setBoolean("exist", user != null);
            if (user != null) {
                t.setLong("user0", user.getLeastSignificantBits());
                t.setLong("user1", user.getMostSignificantBits());
            }
            return t;
        }

        @Override
        public void writeToByteBuf(ByteBuf aBuf) {
            aBuf.writeBoolean(privateFreq)
                .writeBoolean(invert)
                .writeBoolean(safe)
                .writeBoolean(crashed)
                .writeInt(freq)
                .writeBoolean(useMachineOwnerUUID)
                .writeInt(gateMode)
                .writeBoolean(user != null);;
            if (user != null) aBuf.writeLong(user.getMostSignificantBits())
                .writeLong(user.getLeastSignificantBits())

            ;
        }

        @Override
        public void readFromPacket(ByteArrayDataInput aBuf) {
            Data d = new Data();
            d.privateFreq = aBuf.readBoolean();
            d.invert = aBuf.readBoolean();
            d.safe = aBuf.readBoolean();
            d.crashed = aBuf.readBoolean();
            d.freq = aBuf.readInt();
            d.useMachineOwnerUUID = aBuf.readBoolean();
            d.gateMode = aBuf.readInt();

            d.user = aBuf.readBoolean() ? new UUID((aBuf.readLong()), (aBuf.readLong())) : null;
            /*
             * boolean e=aBuf.readBoolean(); boolean a=aBuf.readBoolean();
             * boolean b=aBuf.readBoolean(); boolean c=aBuf.readBoolean(); Data
             * d =new Data(aBuf.readInt(), aBuf.readBoolean(),); d.invert=a;
             * d.safe=b; d.crashed=c; d.privateFreq=e;
             * d.gateMode=aBuf.readInt();
             */

        }

        @Override
        public void loadDataFromNBT(NBTBase t) {
            NBTTagCompound c = ((NBTTagCompound) t);
            privateFreq = c.getBoolean("privateFreq");
            invert = c.getBoolean("invert");
            safe = c.getBoolean("safe");
            crashed = c.getBoolean("crashed");
            gateMode = c.getInteger("gateMode");
            freq = ((NBTTagCompound) t).getInteger("freq");
            useMachineOwnerUUID = ((NBTTagCompound) t).getBoolean("isPrivate");
            if (((NBTTagCompound) t).getBoolean("exist"))
                user = new UUID(((NBTTagCompound) t).getLong("user1"), ((NBTTagCompound) t).getLong("user0"));

        }

    }

    @Override
    public Data initializeDataSer() {

        return new Data(0, false, null);
    }

    /*
     * private boolean makeSureOnlyOne(ForgeDirection side, ICoverable aTileEntity) {
     * return IControlsWorkCover.makeSureOnlyOne(side, aTileEntity);
     * }
     */

    @Override
    public boolean letsEnergyIn() {
        return true;
    }

    @Override
    public boolean letsEnergyOut() {
        return true;
    }

    @Override
    public boolean letsFluidIn(Fluid aFluid) {
        return true;
    }

    @Override
    public boolean letsFluidOut(Fluid aFluid) {
        return true;
    }

    @Override
    public boolean letsItemsIn(int aSlot) {
        return true;
    }

    @Override
    public boolean letsItemsOut(int aSlot) {
        return true;
    }

    @Override
    public void onCoverRemoval() {
        if ((getTile() instanceof IMachineProgress)) {
            ((IMachineProgress) getTile()).enableWorking();
            // ((IMachineProgress) aTileEntity).setWorkDataValue((byte) 0);
        }

    }

    private class WirelessCCUIFactory extends CoverUIFactory {

        private static final int startX = 10;
        private static final int startY = 25;
        private static final int spaceX = 18;
        private static final int spaceY = 18;

        protected WirelessCCUIFactory(CoverUIBuildContext buildContext) {
            super(buildContext);
        }

        @Override
        protected void addUIWidgets(ModularWindow.Builder builder) {

            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().invert ? 1 : 0)
                    .setSetter(s -> getCoverData().invert = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 1) return GTUITextures.OVERLAY_BUTTON_REDSTONE_OFF;
                        return GTUITextures.OVERLAY_BUTTON_REDSTONE_ON;
                    })

                    .addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.wireless.invert.false"))
                    .addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.wireless.invert.true"))
                    .setPos(startX, startY)

            );
            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().safe ? 1 : 0)
                    .setSetter(s -> getCoverData().safe = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 0) return GTUITextures.OVERLAY_BUTTON_CROSS;
                        return GTUITextures.OVERLAY_BUTTON_CHECKMARK;
                    })

                    .addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.wireless.safe.false"))
                    .addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.wireless.safe.true"))
                    .setPos(startX + spaceX, startY)

            );
            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().crashed ? 1 : 0)
                    .setSetter(s -> getCoverData().crashed = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 1) return OFF;
                        return ON;
                    })

                    .addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.wireless.crashed.false"))
                    .addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.wireless.crashed.true"))
                    .setPos(startX + spaceX * 2, startY)

            );
            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().useMachineOwnerUUID ? 1 : 0)
                    .setSetter(s -> getCoverData().useMachineOwnerUUID = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 1) return MACHINE;
                        return COVER;
                    })

                    .addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.wireless.uuidsource.false"))
                    .addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.wireless.uuidsource.true"))
                    .setPos(startX + spaceX * 3, startY)

            );
            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().privateFreq ? 1 : 0)
                    .setSetter(s -> getCoverData().privateFreq = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 1) return GTUITextures.OVERLAY_BUTTON_RECIPE_LOCKED;
                        return GTUITextures.OVERLAY_BUTTON_RECIPE_UNLOCKED;
                    })

                    .addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.wireless.private.false"))
                    .addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.wireless.private.true"))
                    .setPos(startX + spaceX * 4, startY)

            );
            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().gateMode)
                    .setSetter(s -> getCoverData().gateMode = s)
                    .setLength(GateMode.values().length - 1)
                    .setTextureGetter(i -> {
                        switch (i) {
                            case 0:
                                return GTUITextures.OVERLAY_BUTTON_GATE_AND;
                            case 1:
                                return GTUITextures.OVERLAY_BUTTON_GATE_NAND;
                            case 2:
                                return GTUITextures.OVERLAY_BUTTON_GATE_OR;
                            case 3:
                                return GTUITextures.OVERLAY_BUTTON_GATE_NOR;
                            case 4:
                                return GTUITextures.OVERLAY_BUTTON_ANALOG;// NO
                            default:
                                return GTUITextures.OVERLAY_BUTTON_GATE_AND;
                        }
                    })

                    .addTooltip(0, LangManager.translateToLocal("programmable_hatches.cover.wireless.gatemode.0"))
                    .addTooltip(1, LangManager.translateToLocal("programmable_hatches.cover.wireless.gatemode.1"))
                    .addTooltip(2, LangManager.translateToLocal("programmable_hatches.cover.wireless.gatemode.2"))
                    .addTooltip(3, LangManager.translateToLocal("programmable_hatches.cover.wireless.gatemode.3"))
                    // .addTooltip(4,
                    // LangManager.translateToLocal("programmable_hatches.cover.wireless.gatemode.4"))
                    // .addTooltip(0,
                    // LangManager.translateToLocal("programmable_hatches.cover.wireless.gatemode.0"))

                    // .addTooltip(1,LangManager.translateToLocal("programmable_hatches.cover.wireless.private.true"))
                    .setPos(startX + spaceX * 5, startY)

            );
            /*
             * aInputRedstone = getRedstone(d, aTileEntity); if (d.invert)
             * aInputRedstone = (byte) (15 - aInputRedstone);
             */

            builder.widget(TextWidget.dynamicString(() -> {
                ICoverable aTileEntity = getUIBuildContext().getTile();
                Data d = getCoverData();
                int aInputRedstone = getRedstone(d, aTileEntity);
                if (d.invert) aInputRedstone = (byte) (15 - aInputRedstone);
                return "redstone:" + aInputRedstone;
            })
                .setPos(startX, startY + spaceY * 3)
                .setSize(spaceX * 2 + 5, 12)

            );

            builder.widget(
                Optional.of(new TextFieldWidget())
                    .filter(s -> {
                        s.setText((getCoverData().freq + ""));
                        return true;
                    })
                    .get()
                    .setValidator(val -> {
                        if (val == null) {
                            val = "";
                        }
                        return val;
                    })
                    .setSynced(false, true)
                    .setGetter(() -> { return getCoverData().freq + ""; })
                    .setSetter(s -> {
                        try {
                            getCoverData().user = getUIBuildContext().getPlayer()
                                .getUniqueID();
                            getCoverData().freq = Integer.valueOf(s);
                        } catch (Exception e) {
                            getCoverData().freq = 0;
                        }
                    })
                    .setPattern(BaseTextFieldWidget.NATURAL_NUMS)
                    .setMaxLength(50)
                    .setScrollBar()
                    .setFocusOnGuiOpen(true)
                    .setTextColor(Color.WHITE.dark(1))

                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
                    .setPos(startX, startY + spaceY * 2)
                    .setSize(spaceX * 2 + 5, 12)

            )

            ;

        }

        private Data getCoverData() {
            // TODO Auto-generated method stub
            return coverData;
        }
    }

    public static final UITexture OFF = UITexture
        .fullImage(GregTech.ID, "blocks/iconsets/OVERLAY_FRONT_IMPLOSION_COMPRESSOR.png");
    public static final UITexture ON = UITexture
        .fullImage(GregTech.ID, "blocks/iconsets/OVERLAY_FRONT_IMPLOSION_COMPRESSOR_ACTIVE.png");
    public static final UITexture MACHINE = UITexture.fullImage("proghatches", "gui/uuid_machine.png");
    public static final UITexture COVER = UITexture.fullImage("proghatches", "gui/uuid_cover.png");

}
