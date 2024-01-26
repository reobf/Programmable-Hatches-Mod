package reobf.proghatches.gt.cover;

import static gregtech.api.enums.Mods.GregTech;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import gregtech.api.gui.modularui.GT_CoverUIBuildContext;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.covers.IControlsWorkCover;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IMachineProgress;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.api.util.ISerializableObject;
import gregtech.common.covers.redstone.GT_Cover_AdvancedRedstoneReceiverBase.GateMode;
import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.gt.cover.SmartArmCover.Data;
import reobf.proghatches.util.ProghatchesUtil;

@SuppressWarnings("hiding")
public class WirelessControlCover extends GT_CoverBehaviorBase<WirelessControlCover.Data>
    implements IControlsWorkCover {

    @Override
    public ModularWindow createWindow(GT_CoverUIBuildContext buildContext) {
        return new WirelessCCUIFactory(buildContext).createWindow();
    }

    public WirelessControlCover() {
        super(Data.class);

    }

    @Override
    public boolean useModularUI() {

        return true;
    }

    @Override
    public boolean hasCoverGUI() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    protected int getTickRateImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity) {

        return 1;
    }

    @Override
    public Data doCoverThingsImpl(ForgeDirection side, byte aInputRedstone, int aCoverID, Data d,
        ICoverable aTileEntity, long aTimer) {
        aTileEntity.markDirty();
        aInputRedstone = getRedstone(d, aTileEntity);
        if (d.invert) aInputRedstone = (byte) (15 - aInputRedstone);

        if (!makeSureOnlyOne(side, aTileEntity)) return d;
        if (aTileEntity instanceof IMachineProgress) {
            IMachineProgress machine = (IMachineProgress) aTileEntity;
            if (d.safe == false && d.crashed == false) {
                if ((aInputRedstone > 0)) {
                    if (!machine.isAllowedToWork()) machine.enableWorking();
                } else if (machine.isAllowedToWork()) machine.disableWorking();
                machine.setWorkDataValue(aInputRedstone);
            } else if (d.crashed) {
                machine.disableWorking();
            } else {
                if (machine.wasShutdown()) {
                    machine.disableWorking();
                    /*
                     * if (!mPlayerNotified) {
                     * EntityPlayer player = lastPlayer == null ? null : lastPlayer.get();
                     * if (player != null) {
                     * lastPlayer = null;
                     * mPlayerNotified = true;
                     * GT_Utility.sendChatToPlayer(
                     * player,
                     * aTileEntity.getInventoryName() + "at "
                     * + String.format(
                     * "(%d,%d,%d)",
                     * aTileEntity.getXCoord(),
                     * aTileEntity.getYCoord(),
                     * aTileEntity.getZCoord())
                     * + " shut down.");
                     * }
                     * }
                     */
                    d.crashed = true;
                    return d;
                } else {
                    d.safe = false;
                    doCoverThingsImpl(side, aInputRedstone, aCoverID, d, aTileEntity, aTimer);
                    d.safe = true;
                    return d;
                }
            }
        }
        return d;
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
            GateMode.AND);

    }

    public class Data implements ISerializableObject {

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
        public ISerializableObject copy() {

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
        public ISerializableObject readFromPacket(ByteArrayDataInput aBuf, EntityPlayerMP aPlayer) {
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
             * boolean e=aBuf.readBoolean();
             * boolean a=aBuf.readBoolean();
             * boolean b=aBuf.readBoolean();
             * boolean c=aBuf.readBoolean();
             * Data d =new Data(aBuf.readInt(), aBuf.readBoolean(),);
             * d.invert=a;
             * d.safe=b;
             * d.crashed=c;
             * d.privateFreq=e;
             * d.gateMode=aBuf.readInt();
             */
            return d;
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
    public Data createDataObject(int aLegacyData) {

        throw new RuntimeException("not legacy");
    }

    @Override
    public Data createDataObject() {

        return new Data(0, false, null);
    }

    private boolean makeSureOnlyOne(ForgeDirection side, ICoverable aTileEntity) {
        return IControlsWorkCover.makeSureOnlyOne(side, aTileEntity);
    }

    @Override
    public boolean letsEnergyInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsEnergyOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsFluidInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, Fluid aFluid,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsFluidOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, Fluid aFluid,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsItemsInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, int aSlot,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsItemsOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, int aSlot,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean onCoverRemovalImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity,
        boolean aForced) {
        if ((aTileEntity instanceof IMachineProgress)) {
            ((IMachineProgress) aTileEntity).enableWorking();
            ((IMachineProgress) aTileEntity).setWorkDataValue((byte) 0);
        }
        return true;
    }

    private class WirelessCCUIFactory extends UIFactory {

        private static final int startX = 10;
        private static final int startY = 25;
        private static final int spaceX = 18;
        private static final int spaceY = 18;

        private int maxSlot;

        protected WirelessCCUIFactory(GT_CoverUIBuildContext buildContext) {
            super(buildContext);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected void addUIWidgets(ModularWindow.Builder builder) {

            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().invert ? 1 : 0)
                    .setSetter(s -> getCoverData().invert = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 1) return GT_UITextures.OVERLAY_BUTTON_REDSTONE_OFF;
                        return GT_UITextures.OVERLAY_BUTTON_REDSTONE_ON;
                    })

                    .addTooltip(0, StatCollector.translateToLocal("programmable_hatches.cover.wireless.invert.false"))
                    .addTooltip(1, StatCollector.translateToLocal("programmable_hatches.cover.wireless.invert.true"))
                    .setPos(startX, startY)

            );
            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().safe ? 1 : 0)
                    .setSetter(s -> getCoverData().safe = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 0) return GT_UITextures.OVERLAY_BUTTON_CROSS;
                        return GT_UITextures.OVERLAY_BUTTON_CHECKMARK;
                    })

                    .addTooltip(0, StatCollector.translateToLocal("programmable_hatches.cover.wireless.safe.false"))
                    .addTooltip(1, StatCollector.translateToLocal("programmable_hatches.cover.wireless.safe.true"))
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

                    .addTooltip(0, StatCollector.translateToLocal("programmable_hatches.cover.wireless.crashed.false"))
                    .addTooltip(1, StatCollector.translateToLocal("programmable_hatches.cover.wireless.crashed.true"))
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

                    .addTooltip(
                        0,
                        StatCollector.translateToLocal("programmable_hatches.cover.wireless.uuidsource.false"))
                    .addTooltip(
                        1,
                        StatCollector.translateToLocal("programmable_hatches.cover.wireless.uuidsource.true"))
                    .setPos(startX + spaceX * 3, startY)

            );
            builder.widget(
                ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                    .setGetter(() -> getCoverData().privateFreq ? 1 : 0)
                    .setSetter(s -> getCoverData().privateFreq = s == 1)
                    .setLength(2)
                    .setTextureGetter(i -> {
                        if (i == 1) return GT_UITextures.OVERLAY_BUTTON_RECIPE_LOCKED;
                        return GT_UITextures.OVERLAY_BUTTON_RECIPE_UNLOCKED;
                    })

                    .addTooltip(0, StatCollector.translateToLocal("programmable_hatches.cover.wireless.private.false"))
                    .addTooltip(1, StatCollector.translateToLocal("programmable_hatches.cover.wireless.private.true"))
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
                                return GT_UITextures.OVERLAY_BUTTON_GATE_AND;
                            case 1:
                                return GT_UITextures.OVERLAY_BUTTON_GATE_NAND;
                            case 2:
                                return GT_UITextures.OVERLAY_BUTTON_GATE_OR;
                            case 3:
                                return GT_UITextures.OVERLAY_BUTTON_GATE_NOR;
                            case 4:
                                return GT_UITextures.OVERLAY_BUTTON_ANALOG;// NO
                            default:
                                return GT_UITextures.OVERLAY_BUTTON_GATE_AND;
                        }
                    })

                    .addTooltip(0, StatCollector.translateToLocal("programmable_hatches.cover.wireless.gatemode.0"))
                    .addTooltip(1, StatCollector.translateToLocal("programmable_hatches.cover.wireless.gatemode.1"))
                    .addTooltip(2, StatCollector.translateToLocal("programmable_hatches.cover.wireless.gatemode.2"))
                    .addTooltip(3, StatCollector.translateToLocal("programmable_hatches.cover.wireless.gatemode.3"))
                    // .addTooltip(4, StatCollector.translateToLocal("programmable_hatches.cover.wireless.gatemode.4"))
                    // .addTooltip(0, StatCollector.translateToLocal("programmable_hatches.cover.wireless.gatemode.0"))

                    // .addTooltip(1,StatCollector.translateToLocal("programmable_hatches.cover.wireless.private.true"))
                    .setPos(startX + spaceX * 5, startY)

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

                    .setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
                    .setPos(startX, startY + spaceY * 2)
                    .setSize(spaceX * 2 + 5, 12)

            )

            ;

        }
    }

    public static final UITexture OFF = UITexture
        .fullImage(GregTech.ID, "blocks/iconsets/OVERLAY_FRONT_IMPLOSION_COMPRESSOR.png");
    public static final UITexture ON = UITexture
        .fullImage(GregTech.ID, "blocks/iconsets/OVERLAY_FRONT_IMPLOSION_COMPRESSOR_ACTIVE.png");
    public static final UITexture MACHINE = UITexture.fullImage("proghatches", "uuid_machine.png");
    public static final UITexture COVER = UITexture.fullImage("proghatches", "uuid_cover.png");

}
