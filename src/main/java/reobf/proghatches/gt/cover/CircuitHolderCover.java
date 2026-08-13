package reobf.proghatches.gt.cover;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.shapes.Rectangle;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.ChangeableWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;

import gregtech.api.covers.CoverContext;
import gregtech.api.gui.modularui.CoverUIBuildContext;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.Cover;
import gregtech.common.gui.mui1.cover.CoverUIFactory;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.eucrafting.CoverBehaviorBase;
import reobf.proghatches.eucrafting.ISer;
import reobf.proghatches.gt.cover.SmartArmCover.Data;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.util.ProghatchesUtil;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.ListWidget;
import gregtech.api.modularui2.CoverGuiData;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.gui.modularui.cover.base.CoverBaseGui;

public class CircuitHolderCover extends CoverBehaviorBase<CircuitHolderCover.Data> {

    public CircuitHolderCover(CoverContext context, ITexture t) {
        super(context, CircuitHolderCover.Data.class, t);

    }

    public static class Data implements ISer {

        NBTTagCompound tag;

        public Data(NBTTagCompound tag2) {
            tag = tag2;
        }

        @Override
        public ISer copy() {

            return new Data(tag);
        }

        @Override
        public NBTBase saveDataToNBT() {

            return tag.copy();
        }

        @Override
        public void writeToByteBuf(ByteBuf aBuf) {
            try {
                byte[] b = CompressedStreamTools.compress(tag);
                aBuf.writeInt(b.length);
                aBuf.writeBytes(b);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void loadDataFromNBT(NBTBase aNBT) {
            tag = ((NBTTagCompound) aNBT);

        }

        @Override
        public void readFromPacket(ByteArrayDataInput aBuf) {

            byte[] b = new byte[aBuf.readInt()];
            aBuf.readFully(b);
            NBTTagCompound xtag = new NBTTagCompound();
            try {
                xtag = CompressedStreamTools.func_152457_a(b, new NBTSizeTracker(Long.MAX_VALUE));
            } catch (IOException e) {
                e.printStackTrace();
            }

            tag = xtag;
        }
    }

    // @Override
    public boolean useModularUI() {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * GT 54.31 gates cover-GUI opening on hasCoverGUI() (default false); the legacy useModularUI()
     * above is no longer consulted, which made shift-right-click do nothing. The other covers got
     * this override during the MUI2 adaptation - this one was missed.
     */
    @Override
    public boolean hasCoverGUI() {
        return true;
    }

    @Override
    public ModularWindow createWindow(CoverUIBuildContext buildContext) {

        return new CircuitHolderUIFactory(buildContext).createWindow();
    }

    // ===== MUI2 =====
    // GT now opens covers via getCoverGui(); the MUI1 createWindow/CircuitHolderUIFactory above is dead.
    // The MUI1 ChangeableWidget rebuilt a Scrollable on every change; here we use a fixed scrollable grid
    // sized to the cover's capacity. Each slot shows circuits[i] via a DynamicDrawable and acts on the live
    // coverData.tag on click (left = apply to the machine's circuit slot, right = remove). A separate '+'
    // button appends the held item. All server-side mutations go through InteractionSyncHandler, and the
    // grid reflects changes because coverData.tag is synced by the cover framework.
    @Override
    protected @NotNull CoverBaseGui<?> getCoverGui() {
        return new CoverBaseGui<CircuitHolderCover>(this) {
            @Override
            protected boolean doesBindPlayerInventory() {
                // Cover GUIs hide the player inventory by default. This cover records circuits from the
                // cursor stack, so the player must be able to pick items up inside the GUI -> show it.
                return true;
            }

            @Override
            public void addUIWidgets(PanelSyncManager syncManager, Flow column, CoverGuiData data) {
                ItemStack coverItem = cover.getTile().getCoverItemAtSide(cover.getSide());
                final int limit = damageToLimit(coverItem == null ? 0 : coverItem.getItemDamage());
                final int perrow = 5;
                final int rows = (limit + perrow - 1) / perrow;

                // The client-side Cover instance can be REPLACED when the server issues a cover data
                // update, so anything that keeps reading the captured cover.coverData goes stale and the
                // grid never visually updates. GT's own MUI2 cover GUIs never read cover fields on the
                // client; they sync the data. Do the same: push the circuit list explicitly into a
                // GUI-local cache whenever the server-side tag changes, and render from that cache.
                final ItemStack[] shownCircuits = new ItemStack[limit];
                syncManager.syncValue("holder_circuits", new com.cleanroommc.modularui.value.sync.SyncHandler() {

                    private String lastSer;

                    @Override
                    public void detectAndSendChanges(boolean init) {
                        String ser = String.valueOf(cover.coverData.tag);
                        if (!init && ser.equals(lastSer)) return;
                        lastSer = ser;
                        ItemStack[] cs = ProghatchesUtil.deseri(cover.coverData.tag, "circuit");
                        // keep the server-side cache in step too (harmless, aids debugging)
                        for (int i = 0; i < shownCircuits.length; i++)
                            shownCircuits[i] = i < cs.length ? cs[i] : null;
                        syncToClient(1, buf -> {
                            buf.writeVarIntToBuffer(cs.length);
                            for (ItemStack c : cs)
                                com.cleanroommc.modularui.network.NetworkUtils.writeItemStack(buf, c);
                        });
                    }

                    @Override
                    public void readOnClient(int id, net.minecraft.network.PacketBuffer buf) {
                        if (id != 1) return;
                        int n = buf.readVarIntFromBuffer();
                        java.util.Arrays.fill(shownCircuits, null);
                        for (int i = 0; i < n; i++) {
                            ItemStack is = com.cleanroommc.modularui.network.NetworkUtils.readItemStack(buf);
                            if (i < shownCircuits.length) shownCircuits[i] = is;
                        }
                    }

                    @Override
                    public void readOnServer(int id, net.minecraft.network.PacketBuffer buf) {}
                });

                ListWidget<com.cleanroommc.modularui.api.widget.IWidget, ?> list = new ListWidget<>();
                list.size(16 * perrow + 8, Math.min(Math.max(rows, 1), 6) * 16);
                for (int r = 0; r * perrow < limit; r++) {
                    Flow row = Flow.row().size(16 * perrow, 16);
                    for (int cc = 0; cc < perrow && r * perrow + cc < limit; cc++) {
                        final int idx = r * perrow + cc;
                        row.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>()
                            .background(GTGuiTextures.BUTTON_STANDARD, new DynamicDrawable(() -> {
                                return shownCircuits[idx] != null
                                    ? new com.cleanroommc.modularui.drawable.ItemDrawable(shownCircuits[idx])
                                    : com.cleanroommc.modularui.api.drawable.IDrawable.EMPTY;
                            }))
                            .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouseData -> {
                                ItemStack[] cs = ProghatchesUtil.deseri(cover.coverData.tag, "circuit");
                                if (idx >= cs.length) return;
                                if (mouseData.mouseButton == 1) {
                                    java.util.ArrayList<ItemStack> ar = new java.util.ArrayList<>(cs.length);
                                    for (ItemStack c : cs) ar.add(c);
                                    ar.remove(idx);
                                    cover.coverData.tag = ProghatchesUtil.ser(new NBTTagCompound(), ar.toArray(new ItemStack[0]), "circuit");
                                    cover.getTile().markDirty();
                                    cover.getTile().issueCoverUpdate(cover.getSide());
                                } else {
                                    ICoverable te = cover.getTile();
                                    if (te instanceof BaseMetaTileEntity) {
                                        IMetaTileEntity mte = ((BaseMetaTileEntity) te).getMetaTileEntity();
                                        if (mte instanceof IConfigurationCircuitSupport) {
                                            mte.setInventorySlotContents(((IConfigurationCircuitSupport) mte).getCircuitSlot(), cs[idx]);
                                        }
                                    }
                                }
                            }))
                            .tooltipBuilder(t -> t.addLine(LangManager.translateToLocalFormatted("programmable_hatches.gt.holder.apply")))
                            .size(16, 16));
                    }
                    list.child(row);
                }

                com.cleanroommc.modularui.widgets.ButtonWidget<?> addBtn = new com.cleanroommc.modularui.widgets.ButtonWidget<>()
                    .syncHandler(new InteractionSyncHandler().setOnMousePressed(mouseData -> {
                        ItemStack[] cs = ProghatchesUtil.deseri(cover.coverData.tag, "circuit");
                        if (limit <= cs.length) return;
                        ItemStack held = data.getPlayer().inventory.getItemStack();
                        if (held != null) {
                            java.util.ArrayList<ItemStack> ar = new java.util.ArrayList<>(cs.length + 1);
                            for (ItemStack c : cs) ar.add(c);
                            ar.add(held);
                            cover.coverData.tag = ProghatchesUtil.ser(new NBTTagCompound(), ar.toArray(new ItemStack[0]), "circuit");
                            cover.getTile().markDirty();
                            cover.getTile().issueCoverUpdate(cover.getSide());
                        }
                    }))
                    .background(GTGuiTextures.BUTTON_STANDARD, GTGuiTextures.OVERLAY_BUTTON_PLUS_LARGE)
                    .tooltipBuilder(t -> t.addLine(LangManager.translateToLocalFormatted("programmable_hatches.gt.holder.max", "" + limit)))
                    .size(16, 16);

                column.child(makeRowLayout()
                    .child(positionRow(Flow.row().child(addBtn)))
                    .child(list));
            }
        };
    }

    private class CircuitHolderUIFactory extends CoverUIFactory<CircuitHolderCover> {

        @Override
        public void addTitleToUI(Builder builder) {
            ItemStack coverItem = GTUtility.intToStack(getUIBuildContext().getCoverID());
            if (coverItem != null) {
                builder.widget(
                    new ItemDrawable(coverItem).asWidget()
                        .setPos(5, 5)
                        .setSize(16, 16))
                /*
                 * .widget(
                 * new TextWidget(coverItem.getDisplayName()).setDefaultColor(COLOR_TITLE.get())
                 * .setPos(25, 9))
                 */;
            }
        }

        @Override
        public int getGUIWidth() {
            return 176 / 2;
        }

        public CircuitHolderUIFactory(CoverUIBuildContext buildContext) {
            super(buildContext);

        }

        @Override
        public void addUIWidgets(ModularWindow.Builder builder) {

            builder.setPos((size, window) ->
            // window.
            // Alignment.CenterLeft.getAlignedPos(size, new Size(getGUIWidth(), getGUIHeight()))
            new Pos2d(
                size.width / 2 - window.getSize().width / 2 - getGUIWidth() - 18,
                size.height / 2 - window.getSize().height / 2 + getGUIHeight() / 2

            )

            );
            builder.setDraggable(true);
            builder.widget(
                new DrawableWidget().setDrawable(new Rectangle().setColor(Color.LIGHT_BLUE.bright(2)))
                    .setPos(3, 3 + 20)
                    .setSize(getGUIWidth() - 6, getGUIHeight() - 6 - 20)

            );

            builder.widget((chw = new ChangeableWidget(this::createSc) {

                @Override
                public ItemStack[] getCircuits() {
                    return ProghatchesUtil.deseri(getCoverData().tag, "circuit");
                }
            }).setPos(3, 3));
            InventoryPlayer inv = getUIBuildContext().getPlayer().inventory;
            ItemStack coveris = getUIBuildContext().getTile()
                .getCoverItemAtSide(getUIBuildContext().getCoverSide());

            builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
                if (NetworkUtils.isClient() == false) {
                    ItemStack[] is = ProghatchesUtil.deseri(getCoverData().tag, "circuit");
                    if (damageToLimit(coveris.getItemDamage()) <= is.length) {
                        return;
                    }

                    if (inv.getItemStack() != null) {
                        // System.out.println(inv.getItemStack());

                        ArrayList<ItemStack> ar = new ArrayList<>(is.length + 1);
                        for (ItemStack iis : is) {
                            ar.add(iis);
                        }
                        ar.add(inv.getItemStack());
                        ItemStack[] x = ar.toArray(new ItemStack[is.length + 1]);

                        getCoverData().tag = ProghatchesUtil.ser(new NBTTagCompound(), x, "circuit");
                        getUIBuildContext().getTile()
                            .markDirty();
                    }

                    chw.notifyChangeServer();
                }

            })
                .setPlayClickSound(true)
                .setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_PLUS_LARGE)
                .addTooltips(
                    ImmutableList.of(
                        LangManager.translateToLocalFormatted(
                            "programmable_hatches.gt.holder.max",
                            "" + damageToLimit(coveris.getItemDamage()))))
                .setSize(16, 16)
                .setPos(3 + 32 + 16, 3));

        }

        @Override
        protected CircuitHolderCover adaptCover(Cover cover) {
            if (cover instanceof CircuitHolderCover) {
                return (CircuitHolderCover) cover;
            }
            return null;
        }

        private Data getCoverData() {

            return getCover().coverData;
        }

        ChangeableWidget chw;

        public Scrollable createSc(ItemStack is[]) {

            Scrollable sc = new Scrollable();

            sc.setVerticalScroll();

            // IDragAndDropHandler

            /*
             * sc.widget(
             * new DrawableWidget().setDrawable(new Rectangle().setColor(
             * Color.LIGHT_BLUE.bright(2)
             * )).setPos(0, 0).setSize(getGUIWidth()-6, getGUIHeight()-6-20));
             */
            int perrow = (getGUIWidth() - 6) / 16;
            if (perrow <= 0) perrow = 1;
            sc.setSize(getGUIWidth() - 6, getGUIHeight() - 6 - 20);
            sc.setPos(0, 0 + 20);
            for (int i = 0; i < is.length; i++) {
                final int fi = i;
                ButtonWidget w = new ButtonWidget();
                w.setSize(16, 16);
                w.addTooltips(
                    ImmutableList.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.holder.apply")));
                w.setOnClick((a, b) -> {
                    if (!NetworkUtils.isClient()) {
                        if (a.mouseButton == 1) {
                            ItemStack[] isx = ProghatchesUtil.deseri(getCoverData().tag, "circuit");
                            ArrayList<ItemStack> ar = new ArrayList<>(isx.length);
                            for (ItemStack iis : isx) {
                                ar.add(iis);
                            }
                            ar.remove(fi);
                            ItemStack[] x = ar.toArray(new ItemStack[isx.length - 1]);
                            getCoverData().tag = ProghatchesUtil.ser(new NBTTagCompound(), x, "circuit");
                            getUIBuildContext().getTile()
                                .markDirty();
                            chw.notifyChangeServer();
                            return;

                        }

                        // System.out.println(is[fi]);

                        ICoverable te = getUIBuildContext().getTile();
                        if (te instanceof BaseMetaTileEntity) {
                            IMetaTileEntity mte = ((BaseMetaTileEntity) te).getMetaTileEntity();
                            if (mte instanceof IConfigurationCircuitSupport) {
                                IConfigurationCircuitSupport ci = (IConfigurationCircuitSupport) mte;
                                mte.setInventorySlotContents(ci.getCircuitSlot(), is[fi]);
                            }
                        }
                    }
                });
                w.setBackground(GTUITextures.BUTTON_STANDARD, new ItemDrawable().setItem(is[fi]));
                w.setPos(16 * (i % perrow), 16 * (i / perrow));
                sc.widget(w);

            }

            return sc;
        }

    }

    public void writeTag(PacketBuffer b, NBTTagCompound t) {

        byte bb[];
        try {
            bb = CompressedStreamTools.compress(t);
            b.writeInt(bb.length);
            b.writeBytes(bb);
        } catch (IOException e) {
            e.printStackTrace();
            b.writeInt(0);
        }
    }

    public int damageToLimit(int itemDamage) {
        int offset = itemDamage - 90;// 90:see ItemDedicatedCover
        if (offset == 0) return 9;
        if (offset == 1) return 12;
        if (offset == 2) return 15;
        if (offset == 3) return 32;
        if (offset == 4) return 64;
        return 1;
    }

    public NBTTagCompound readTag(PacketBuffer b) {
        byte bb[] = new byte[b.readInt()];
        b.readBytes(bb);
        try {
            NBTTagCompound tag = CompressedStreamTools.func_152457_a(bb, new NBTSizeTracker(Long.MAX_VALUE));
            return tag;
        } catch (IOException e) {
            e.printStackTrace();
            return new NBTTagCompound();
        }

    }

    public abstract class ChangeableWidget extends Widget implements ISyncedWidget, IWidgetParent {

        // sideonly.client
        NBTTagCompound tag;

        public NBTTagCompound getCircuitsTag() {

            if (NetworkUtils.isClient()) {
                return tag;
            }
            NBTTagCompound t = new NBTTagCompound();
            ProghatchesUtil.ser(t, getCircuits(), "circuit");
            return t;
        }

        public abstract ItemStack[] getCircuits();

        private boolean needsUpdate;

        private final List<Widget> child = new ArrayList<>();

        @Nullable
        private Widget queuedChild = null;

        private final Function<NBTTagCompound, Widget> widgetSupplier;
        private boolean initialised = false;
        private boolean firstTick = true;

        /**
         * Creates a widget which child can be changed dynamically. Call
         * {@link #notifyChangeServer()} to notify the widget for a change.
         *
         * @param widgetSupplier
         *                       widget to supply. Can return null
         */
        public ChangeableWidget(Function<ItemStack[], Scrollable> sp) {
            this.widgetSupplier = s -> {

                ItemStack[] is = ProghatchesUtil.deseri(s, "circuit");
                return sp.apply(is);

            };
        }

        @Override
        public @NotNull Size determineSize(int maxWidth, int maxHeight) {
            if (this.child.isEmpty()) {
                return Size.ZERO;
            }
            return this.child.get(0)
                .getSize();
        }

        /**
         * Notifies the widget that the child probably changed. Only executed on
         * server and synced to client. This method is preferred!
         */
        public void notifyChangeServer() {
            if (!isClient()) {
                notifyChange(true);
            }
        }

        /**
         * Notifies the widget that the child probably changed. Only executed on
         * client and NOT synced to server.
         */
        public void notifyChangeClient() {
            notifyChangeNoSync();
        }

        /**
         * Notifies the widget that the child probably changed. Can execute on
         * both sides and NOT synced.
         */
        public void notifyChangeNoSync() {
            notifyChange(false);
            initQueuedChild();
        }

        private void notifyChange(boolean sync) {
            if (this.widgetSupplier == null || !isInitialised()) {
                return;
            }
            NBTTagCompound tag = getCircuitsTag();
            if (sync && !isClient()) {
                syncToClient(0, s -> writeTag(s, tag));
            }
            removeCurrentChild();
            this.queuedChild = this.widgetSupplier.apply(tag);
            this.initialised = false;
        }

        private void initQueuedChild() {
            if (this.queuedChild != null) {
                final Consumer<Widget> initChildrenWrapper = widget -> {
                    if (widget instanceof IWidgetParent) {
                        ((IWidgetParent) widget).initChildren();
                    }
                };
                initChildrenWrapper.accept(this.queuedChild);
                IWidgetParent.forEachByLayer(this.queuedChild, initChildrenWrapper);
                AtomicInteger syncId = new AtomicInteger(1);
                final Consumer<Widget> initSyncedWidgetWrapper = widget1 -> {
                    if (widget1 instanceof ISyncedWidget) {
                        getWindow().addDynamicSyncedWidget(syncId.getAndIncrement(), (ISyncedWidget) widget1, this);
                    }
                };
                if (queuedChild instanceof IWidgetParent) initSyncedWidgetWrapper.accept(queuedChild);
                IWidgetParent.forEachByLayer(this.queuedChild, initSyncedWidgetWrapper);
                this.queuedChild.initialize(getWindow(), this, getLayer() + 1);
                this.child.add(this.queuedChild);
                this.initialised = true;
                this.queuedChild = null;
                this.firstTick = true;
            }
            checkNeedsRebuild();
        }

        public void removeCurrentChild() {
            if (!this.child.isEmpty()) {
                Widget widget = this.child.get(0);
                widget.setEnabled(false);
                if (widget instanceof IWidgetParent) {
                    widget.onPause();
                    widget.onDestroy();
                }
                IWidgetParent.forEachByLayer(widget, Widget::onPause);
                IWidgetParent.forEachByLayer(widget, Widget::onDestroy);
                this.child.clear();
            }
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            if (init) {
                notifyChangeServer();
            }
            if (this.initialised && !this.child.isEmpty()) {
                final Consumer<Widget> detectAndSendChangesWrapper = widget -> {
                    if (widget instanceof ISyncedWidget) {
                        ((ISyncedWidget) widget).detectAndSendChanges(firstTick);
                    }
                };
                Widget widget = this.child.get(0);
                if (widget instanceof IWidgetParent) detectAndSendChangesWrapper.accept(widget);
                IWidgetParent.forEachByLayer(widget, detectAndSendChangesWrapper);
                firstTick = false;
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer packetBuffer) throws IOException {
            if (id == 0) {
                tag = readTag(packetBuffer);
                notifyChange(false);
                initQueuedChild();
                syncToServer(1, NetworkUtils.EMPTY_PACKET);
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer packetBuffer) throws IOException {
            if (id == 1) {
                initQueuedChild();
            }
        }

        @Override
        public void markForUpdate() {
            needsUpdate = true;
        }

        @Override
        public void unMarkForUpdate() {
            needsUpdate = false;
        }

        @Override
        public boolean isMarkedForUpdate() {
            return needsUpdate;
        }

        @Override
        public List<Widget> getChildren() {
            return this.child;
        }
    }

    @Override
    public Data initializeDataSer() {

        return new Data(new NBTTagCompound());
    }

    @Override
    public boolean allowsCopyPasteTool() {
        // TODO Auto-generated method stub
        return super.allowsCopyPasteTool();
    }

}