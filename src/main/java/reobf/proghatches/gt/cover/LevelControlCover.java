package reobf.proghatches.gt.cover;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.widget.Widget.ClickData;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import gregtech.api.covers.CoverContext;
import gregtech.api.gui.modularui.CoverUIBuildContext;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IMachineProgress;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.Cover;
import gregtech.common.gui.mui1.cover.CoverUIFactory;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.eucrafting.AECover.Data;
import reobf.proghatches.eucrafting.CoverBehaviorBase;
import reobf.proghatches.eucrafting.ISer;
import reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget;

public class LevelControlCover extends CoverBehaviorBase<LevelControlCover.Data> {

    public LevelControlCover(CoverContext context, gregtech.api.interfaces.ITexture t) {
        super(context, Data.class, t);

    }

    @Override
    public boolean hasCoverGUI() {
        // TODO Auto-generated method stub
        return true;
    }

    // @Override
    public boolean useModularUI() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public ModularWindow createWindow(CoverUIBuildContext buildContext) {

        return new CoverUIFactory<LevelControlCover>(buildContext) {

            protected LevelControlCover adaptCover(Cover cover) {
                return (LevelControlCover) cover;
            };

            private Data getCoverData() {

                return getCover().coverData;
            }

            private ItemStack tryConvertToFluid(ItemStack is) {

                FluidStack fs = GTUtility.getFluidForFilledItem(is, true);
                if (fs != null) {
                    return GTUtility.getFluidDisplayStack(fs, false);
                }

                return null;
            }

            public void addUIWidgets(ModularWindow.Builder builder) {

                builder.widget(TextWidget.dynamicString(() -> {

                    ICoverable te = getUIBuildContext().getTile();
                    if (te instanceof IMachineProgress) {
                        return "Working: " + ((IMachineProgress) te).isAllowedToWork() + "";

                    }
                    return "Machine not valid.";

                })
                    .setPos(3, 60));

                ItemStackHandler iss = new ItemStackHandler(getCoverData().filter);
                builder.widget(new SlotWidget(new BaseSlot(iss, 0, true)) {

                    @Override
                    public void phantomClick(ClickData clickData, ItemStack cursorStack) {
                        if (cursorStack == null) {
                            getCoverData().filter[0] = null;

                        } else {
                            ItemStack fis = getCoverData().mode == 0 ? null : tryConvertToFluid(cursorStack);

                            getCoverData().filter[0] = fis == null ? cursorStack : fis;
                            getCoverData().filter[0] = getCoverData().filter[0].copy();
                            getCoverData().filter[0].stackSize = 1;
                        }

                    }

                }.setPos(3, 3 + 20)
                    .addTooltip(StatCollector.translateToLocal("proghatches.amountmaintainer.phantomslot")));

                builder.widget(
                    new CycleButtonWidget().setGetter(() -> getCoverData().invert ? 1 : 0)
                        .setSetter(s -> getCoverData().invert = s == 1)
                        .setLength(2)
                        .setTextureGetter(s -> {
                            if (s == 0) return GTUITextures.OVERLAY_BUTTON_CROSS;
                            if (s == 1) return GTUITextures.OVERLAY_BUTTON_CHECKMARK;
                            return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ALL;
                        })
                        .addTooltip(0, StatCollector.translateToLocal("proghatches.levelcontrolcover.invert.0"))
                        .addTooltip(1, StatCollector.translateToLocal("proghatches.levelcontrolcover.invert.1"))
                        .setBackground(() -> {
                            {
                                return new IDrawable[] { GTUITextures.BUTTON_STANDARD, };
                            }
                        })

                        .setSize(18, 18)
                        .setPos(3 + 20, 3 + 40));
                builder.widget(
                    new CycleButtonWidget().setGetter(() -> getCoverData().mode)
                        .setSetter(s -> getCoverData().mode = s)
                        .setLength(2)
                        .setTextureGetter(s -> {
                            if (s == 0) return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ITEM;
                            if (s == 1) return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_FLUID;
                            return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ALL;
                        })
                        .addTooltip(
                            0,
                            StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.0"))
                        .addTooltip(
                            1,
                            StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.1"))
                        .setBackground(() -> {
                            {
                                return new IDrawable[] { GTUITextures.BUTTON_STANDARD, };
                            }
                        })

                        .setSize(18, 18)
                        .setPos(3 + 20, 3 + 20));
                builder.widget(
                    new NumericWidget().setSetter(val -> getCoverData().amount = (long) val)
                        .setGetter(() -> getCoverData().amount)
                        .setBounds(0, 9_007_199_254_740_991D)
                        .setScrollValues(1, 4, 64)
                        .setTextAlignment(Alignment.Center)
                        .setTextColor(Color.WHITE.normal)
                        .setSize(60, 18)
                        .setPos(60 + 18, 3 + 20)
                        .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD)
                        .addTooltips(Arrays.asList(

                        )));
            }

        }.createWindow();
    }

    public static class Data implements ISer {

        public AEFluidStack maybeFluid() {
            if (filter[0] == null) return null;
            FluidStack fs = GTUtility.getFluidFromDisplayStack(filter[0]);
            if (fs != null) {
                AEFluidStack is = AEFluidStack.create(fs);
                is.setStackSize(Long.MAX_VALUE);
                return is;
            }
            return null;
        }

        public AEItemStack maybeItem() {
            if (filter[0] == null) return null;

            FluidStack fs = GTUtility.getFluidFromDisplayStack(filter[0]);
            if (fs == null) {
                AEItemStack is = AEItemStack.create(filter[0]);
                is.setStackSize(Long.MAX_VALUE);
                return is;
            }
            return null;
        }

        public int mode;
        ItemStack filter[] = new ItemStack[1];
        long amount = 1;
        boolean invert;

        public Data(ItemStack filter2, long amount2, boolean invert2) {
            this.filter[0] = filter2;
            amount = amount2;
            invert = invert2;
        }

        public Data() {}

        @Override
        public ISer copy() {

            return new Data(filter[0], amount, invert);
        }

        @Override
        public NBTBase saveDataToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            if (filter[0] != null) {

                filter[0].writeToNBT(tag);
                FluidStack fs = GTUtility.getFluidFromDisplayStack(filter[0]);
                if (fs != null) {
                    String name = FluidRegistry.getFluidName(fs);
                    tag.setString("fluid_ID_string", name);
                }

            }
            tag.setLong("a", amount);
            tag.setInteger("m", mode);
            tag.setBoolean("i", invert);
            return tag;
        }

        @Override
        public void writeToByteBuf(ByteBuf aBuf) {

            try {
                byte[] b = CompressedStreamTools.compress((NBTTagCompound) saveDataToNBT());
                aBuf.writeInt(b.length);
                aBuf.writeBytes(b);
            } catch (IOException e) {
                aBuf.writeByte(0);
                e.printStackTrace();
            }

        }

        @Override
        public void loadDataFromNBT(NBTBase aNBT) {

            NBTTagCompound tag = (NBTTagCompound) aNBT;
            amount = tag.getLong("a");
            invert = tag.getBoolean("i");
            filter[0] = null;
            filter[0] = ItemStack.loadItemStackFromNBT(tag);
            mode = tag.getInteger("m");
            exit: {
                FluidStack fs = GTUtility.getFluidFromDisplayStack(filter[0]);
                if (fs == null) {
                    break exit;
                }
                String name = tag.getString("fluid_ID_string");
                if (name.isEmpty()) {
                    break exit;
                }
                Fluid f = FluidRegistry.getFluid(name);
                if (f == null) {
                    break exit;
                }
                if (f == fs.getFluid()) {
                    break exit;
                }
                fs = new FluidStack(f, fs.amount);
            }

        }

        @Override
        public void readFromPacket(ByteArrayDataInput aBuf) {
            Data d = new Data();
            try {
                byte b[] = new byte[aBuf.readInt()];
                aBuf.readFully(b);

                d.loadDataFromNBT(CompressedStreamTools.func_152457_a(b, new NBTSizeTracker(Long.MAX_VALUE)));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public Data initializeDataSer() {

        return new Data();
    }

    @Override
    public int getDefaultTickRate() {

        return 10;
    }

    @Override
    public void doCoverThings(byte x, long aTimer) {
        ICoverable aTileEntity = this.coveredTile.get();
        @NotNull
        Data aCoverVariable = coverData;
        long amount = 0;
        if (aTileEntity instanceof IGregTechTileEntity/* &&aTileEntity instanceof IActionHost */) {
            IMetaTileEntity mte = ((IGregTechTileEntity) aTileEntity).getMetaTileEntity();
            AENetworkProxy grid = null;

            if (aTileEntity instanceof IGridProxyable) {
                grid = ((IGridProxyable) aTileEntity).getProxy();
            }
            if (grid == null) {
                for (ForgeDirection fd : ForgeDirection.VALID_DIRECTIONS) {
                    ISer dat = coverData;// getCoverData(aTileEntity.getCoverAtSide(fd));
                    if (dat instanceof AECover.Data) {
                        AECover.Data ae = (reobf.proghatches.eucrafting.AECover.Data) dat;
                        grid = ae.getProxy();
                        if (grid != null) break;
                    }

                }

            }

            if (grid != null) try {

                IAEFluidStack avf = grid.getStorage()
                    .getFluidInventory()
                    .getAvailableItem(aCoverVariable.maybeFluid());
                amount = avf == null ? 0 : avf.getStackSize();

                if (amount == 0) {
                    IAEItemStack av = grid.getStorage()
                        .getItemInventory()
                        .getAvailableItem(aCoverVariable.maybeItem());
                    amount = av == null ? 0 : av.getStackSize();
                }

            } catch (GridAccessException e) {}

            // new MachineSource((IActionHost) aTileEntity);

        }

        boolean ok = (aCoverVariable.invert ? amount <= aCoverVariable.amount : amount >= aCoverVariable.amount);

        if (aTileEntity instanceof IMachineProgress) {

            boolean allowedToWork = ok;
            if (allowedToWork) {
                ((IMachineProgress) aTileEntity).enableWorking();
            } else {
                ((IMachineProgress) aTileEntity).disableWorking();
            }

        }
        return;// aCoverVariable;
    }

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
    public boolean letsRedstoneGoIn() {
        return true;
    }

    @Override
    public boolean letsRedstoneGoOut() {
        return true;
    }

}
