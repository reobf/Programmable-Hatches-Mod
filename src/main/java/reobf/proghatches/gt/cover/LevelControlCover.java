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
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
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

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.LongSyncValue;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import gregtech.api.modularui2.CoverGuiData;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.gui.modularui.cover.base.CoverBaseGui;

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

    // ===== MUI2 =====
    // GT now opens covers via getCoverGui(); the MUI1 createWindow above is dead. This rebuilds the
    // same controls (filter phantom slot, item/fluid mode, invert, threshold amount, work status).
    @Override
    protected @NotNull CoverBaseGui<?> getCoverGui() {
        return new CoverBaseGui<LevelControlCover>(this) {
            @Override
            public void addUIWidgets(PanelSyncManager syncManager, Flow column, CoverGuiData data) {
                // filter handler shares coverData.filter[]; forces count 1 and, in fluid mode,
                // converts a filled container to its fluid display stack (mirrors the MUI1 phantomClick)
                com.cleanroommc.modularui.utils.item.ItemStackHandler filterHandler =
                    new com.cleanroommc.modularui.utils.item.ItemStackHandler(cover.coverData.filter) {
                        @Override
                        public void setStackInSlot(int slot, ItemStack stack) {
                            if (stack != null) {
                                ItemStack result = stack;
                                if (cover.coverData.mode != 0) {
                                    FluidStack fs = GTUtility.getFluidForFilledItem(stack, true);
                                    if (fs != null) result = GTUtility.getFluidDisplayStack(fs, false);
                                }
                                result = result == null ? null : result.copy();
                                if (result != null) result.stackSize = 1;
                                stack = result;
                            }
                            super.setStackInSlot(slot, stack);
                        }
                    };

                column.child(makeRowLayout()
                    .child(positionRow(Flow.row()
                        .child(new PhantomItemSlot()
                            .slot(new ModularSlot(filterHandler, 0))
                            .marginRight(4)
                            .tooltipBuilder(t -> t.addLine(IKey
                                .str(StatCollector.translateToLocal("proghatches.amountmaintainer.phantomslot")))))
                        .child(new com.cleanroommc.modularui.widgets.CycleButtonWidget()
                            .stateCount(2)
                            .value((IIntValue<?>) new IntSyncValue(() -> cover.coverData.mode, v -> cover.coverData.mode = v).allowC2S())
                            .stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
                            .stateBackground(1, GTGuiTextures.BUTTON_STANDARD)
                            .stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_VOID_EXCESS_ITEM)
                            .stateOverlay(1, GTGuiTextures.OVERLAY_BUTTON_VOID_EXCESS_FLUID)
                            .addTooltip(0, StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.0"))
                            .addTooltip(1, StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.1"))
                            .marginRight(2)
                            .size(18, 18))
                        .child(new com.cleanroommc.modularui.widgets.CycleButtonWidget()
                            .stateCount(2)
                            .value((IIntValue<?>) new IntSyncValue(() -> cover.coverData.invert ? 1 : 0, v -> cover.coverData.invert = v != 0).allowC2S())
                            .stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
                            .stateBackground(1, GTGuiTextures.BUTTON_STANDARD)
                            .stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_CROSS)
                            .stateOverlay(1, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
                            .addTooltip(0, StatCollector.translateToLocal("proghatches.levelcontrolcover.invert.0"))
                            .addTooltip(1, StatCollector.translateToLocal("proghatches.levelcontrolcover.invert.1"))
                            .size(18, 18))))
                    .child(positionRow(Flow.row()
                        .child(makeNumberField(70)
                            .value(new LongSyncValue(() -> cover.coverData.amount, v -> cover.coverData.amount = v).allowC2S())
                            .numbersLong(0, 9_007_199_254_740_991L))))
                    .child(positionRow(Flow.row()
                        .child(IKey.dynamic(() -> {
                            ICoverable te = cover.getTile();
                            if (te instanceof IMachineProgress) {
                                return "Working: " + ((IMachineProgress) te).isAllowedToWork();
                            }
                            return "Machine not valid.";
                        }).asWidget()))));
            }
        };
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
                    @NotNull Cover cv = aTileEntity.getCoverAtSide(fd);//coverData;// getCoverData(aTileEntity.getCoverAtSide(fd));
                    if (cv instanceof AECover)
                    {AECover.Data dat = ((AECover)cv).coverData;
                    	if(dat instanceof AECover.Data) {
                        AECover.Data ae = (reobf.proghatches.eucrafting.AECover.Data) dat;
                        grid = ae.getProxy();
                        if (grid != null) break;
                    }
                    }
                }

            }

            if (grid != listenedGrid) {
                detachStorageListener();
                attachStorageListener(grid);
                amountDirty = true;
            }
            if (!ItemStack.areItemStacksEqual(listenedFilter, aCoverVariable.filter[0])) {
                listenedFilter = aCoverVariable.filter[0] == null ? null
                    : aCoverVariable.filter[0].copy();
                amountDirty = true;
            }
            if (amountDirty) {
                cachedAmount = lookupAmount(grid, aCoverVariable);
                amountDirty = false;
            }
            amount = cachedAmount;

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

    // ===== issue #328 =====
    // 原来每 getDefaultTickRate()（10 tick）调一次 IMEInventory#getAvailableItem。NetworkMonitor 没有覆写
    // 这个方法，它会退化到默认实现 getAvailableItems(...)，也就是把整个网络的存储从头扫一遍；而且过滤器
    // 装的是物品时，流体侧那一次还是照扫不误，等于每 10tick 两次全盘扫描，覆盖板一多就很吃性能。
    // 现在改成挂 AE 自己的监听器：只有网络里出现和过滤器同类型的变动才置脏，置脏之后从 NetworkMonitor
    // 已经缓存好的列表里做一次 findPrecise 定点查找。稳态下每 tick 的开销是零。
    private long cachedAmount;
    private boolean amountDirty = true;
    private Object listenerToken;
    private AENetworkProxy listenedGrid;
    private ItemStack listenedFilter;

    @SuppressWarnings("rawtypes")
    private final IMEMonitorHandlerReceiver storageListener = new IMEMonitorHandlerReceiver() {

        @Override
        public boolean isValid(Object verificationToken) {
            // token 置 null 之后 AE 会自己把监听器摘掉，不用担心覆盖板被拆了还挂在网络上
            return listenerToken != null && verificationToken == listenerToken;
        }

        @Override
        public void postChange(IBaseMonitor monitor, Iterable change, BaseActionSource actionSource) {
            if (amountDirty || change == null) return;
            for (Object o : change) {
                if (isWatched(o)) {
                    amountDirty = true;
                    return;
                }
            }
        }

        @Override
        public void onListUpdate() {
            amountDirty = true;
        }
    };

    private boolean isWatched(Object o) {
        if (o instanceof IAEFluidStack) {
            AEFluidStack want = coverData.maybeFluid();
            return want != null && ((IAEFluidStack) o).isSameType(want);
        }
        if (o instanceof IAEItemStack) {
            AEItemStack want = coverData.maybeItem();
            return want != null && ((IAEItemStack) o).isSameType(want);
        }
        return false;
    }

    private void attachStorageListener(AENetworkProxy grid) {
        if (grid == null) return;
        try {
            listenerToken = new Object();
            grid.getStorage()
                .getItemInventory()
                .addListener(storageListener, listenerToken);
            grid.getStorage()
                .getFluidInventory()
                .addListener(storageListener, listenerToken);
            listenedGrid = grid;
        } catch (GridAccessException e) {
            listenerToken = null;
            listenedGrid = null;
        }
    }

    private void detachStorageListener() {
        if (listenedGrid != null) try {
            listenedGrid.getStorage()
                .getItemInventory()
                .removeListener(storageListener);
            listenedGrid.getStorage()
                .getFluidInventory()
                .removeListener(storageListener);
        } catch (GridAccessException e) {}
        listenedGrid = null;
        listenerToken = null;
    }

    private long lookupAmount(AENetworkProxy grid, Data aCoverVariable) {
        if (grid == null) return 0;
        try {
            AEFluidStack wantFluid = aCoverVariable.maybeFluid();
            if (wantFluid != null) {
                IAEFluidStack got = grid.getStorage()
                    .getFluidInventory()
                    .getStorageList()
                    .findPrecise(wantFluid);
                return got == null ? 0 : got.getStackSize();
            }
            AEItemStack wantItem = aCoverVariable.maybeItem();
            if (wantItem != null) {
                IAEItemStack got = grid.getStorage()
                    .getItemInventory()
                    .getStorageList()
                    .findPrecise(wantItem);
                return got == null ? 0 : got.getStackSize();
            }
        } catch (GridAccessException e) {
            // 网络暂时够不着，保持脏标记下次再试
            amountDirty = true;
        }
        return 0;
    }

    @Override
    public void onCoverRemoval() {
        detachStorageListener();
        super.onCoverRemoval();
    }

    @Override
    public void onCoverUnload() {
        detachStorageListener();
        super.onCoverUnload();
    }

    @Override
    public void onBaseTEDestroyed() {
        detachStorageListener();
        super.onBaseTEDestroyed();
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