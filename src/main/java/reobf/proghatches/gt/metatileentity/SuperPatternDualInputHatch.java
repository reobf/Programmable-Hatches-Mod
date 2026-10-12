package reobf.proghatches.gt.metatileentity;

import static reobf.proghatches.gt.metatileentity.util.PatternSlotMask.CAPACITY;
import static reobf.proghatches.gt.metatileentity.util.PatternSlotMask.PAGE_SIZE;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;


import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.NumberFormatMUI;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.gui.modularui.util.PatternSlot;
import reobf.proghatches.gt.metatileentity.util.PatternSlotMask;
import reobf.proghatches.main.Config;
import reobf.proghatches.util.ProghatchesUtil;

/** MK.II input/buffer capacity with up to ten independently unlocked pages of encoded patterns. */
@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class SuperPatternDualInputHatch extends PatternDualInputHatch {

    private static final NumberFormatMUI MULTIPLIER_FORMAT = new NumberFormatMUI();

    private final PatternSlotMask patternSlots = new PatternSlotMask();
    private final NBTTagCompound[] patternTags = new NBTTagCompound[CAPACITY];

    {
        pattern = new ItemStack[CAPACITY];
        multiplier = new int[CAPACITY];
        Arrays.fill(multiplier, 1);
        patternItemCache = new ItemStack[CAPACITY];
        patternDetailCache = new ICraftingPatternDetails[CAPACITY];
    }

    public SuperPatternDualInputHatch(int id, String name, String regionalName) {
        super(id, name, regionalName, 10, true, 24, true, 2,
            Config.get("SPDIH", ImmutableMap.of(
                "bufferNum", 24, "fluidSlots", 32, "mMultiFluid", true, "slots", 32)));
    }

    private SuperPatternDualInputHatch(String name, byte tier, String[] description, ITexture[][][] textures,
        boolean multiFluid, int buffers) {
        super(name, tier, 33, description, textures, multiFluid, buffers);
    }

    @Override
    public int page() {
        return 2;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity tile) {
        return new SuperPatternDualInputHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
    }

    @Override
    public int rows() {
        return patternSlots.pages() * 4;
    }

    private void invalidatePattern(int slot) {
        patternItemCache[slot] = null;
        patternDetailCache[slot] = null;
    }

    private void snapshotTag(int slot) {
        NBTTagCompound tag = pattern[slot] == null ? null : pattern[slot].getTagCompound();
        patternTags[slot] = tag == null ? null : (NBTTagCompound) tag.copy();
    }

    private void checkEditedPatterns() {
        boolean changed = false;
        for (int slot = patternSlots.nextOccupied(0); slot >= 0; slot = patternSlots.nextOccupied(slot + 1)) {
            if (!Objects.equals(pattern[slot].getTagCompound(), patternTags[slot])) {
                snapshotTag(slot);
                invalidatePattern(slot);
                changed = true;
            }
        }
        if (changed) patternsChanged();
    }

    private void patternsChanged() {
        needPatternSync = true;
        if (getBaseMetaTileEntity() != null) markDirty();
    }

    private void patternChanged(int slot) {
        patternSlots.update(slot, pattern[slot] != null && pattern[slot].stackSize > 0);
        snapshotTag(slot);
        invalidatePattern(slot);
        patternsChanged();
    }

    private void setPattern(int slot, ItemStack stack) {
        if (slot < 0 || slot >= patternSlots.unlockedSlots()) {
            throw new IndexOutOfBoundsException("Locked pattern slot: " + slot);
        }
        pattern[slot] = stack == null || stack.stackSize <= 0 ? null : stack;
        patternChanged(slot);
    }

    private final IInventory patterns = new InventoryBasic("super_pattern_input", false, 0) {
        @Override
        public int getSizeInventory() {
            return patternSlots.unlockedSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSizeInventory() ? pattern[slot] : null;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            setPattern(slot, stack);
        }

        @Override
        public ItemStack decrStackSize(int slot, int count) {
            ItemStack stack = getStackInSlot(slot);
            if (stack == null || count <= 0) return null;
            if (count >= stack.stackSize) {
                setPattern(slot, null);
                return stack;
            }
            ItemStack taken = stack.splitStack(count);
            patternChanged(slot);
            return taken;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            return null;
        }

        @Override
        public int getInventoryStackLimit() {
            return 1;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return slot >= 0 && slot < getSizeInventory()
                && stack != null && stack.getItem() instanceof ICraftingPatternItem;
        }

        @Override
        public void markDirty() {
            // Some terminal actions modify an existing ItemStack's NBT in place.
            checkEditedPatterns();
        }
    };

    @Override
    public IInventory getPatterns() {
        return patterns;
    }

    @Override
    public void loadNBTData(NBTTagCompound tag) {
        super.loadNBTData(tag);
        multiplier = Arrays.copyOf(multiplier, CAPACITY);
        for (int i = 0; i < CAPACITY; i++) multiplier[i] = Math.max(1, multiplier[i]);
        patternSlots.restore(tag.getInteger("patternPages"),
            slot -> pattern[slot] != null && pattern[slot].stackSize > 0);
        for (int slot = 0; slot < CAPACITY; slot++) snapshotTag(slot);
        Arrays.fill(patternItemCache, null);
        Arrays.fill(patternDetailCache, null);
        needPatternSync = true;
    }

    @Override
    public void saveNBTData(NBTTagCompound tag) {
        super.saveNBTData(tag);
        tag.setInteger("patternPages", patternSlots.pages());
    }

    private void applyMultipliers(IntUnaryOperator operation) {
        for (int slot = patternSlots.nextOccupied(0); slot >= 0; slot = patternSlots.nextOccupied(slot + 1)) {
            multiplier[slot] = Math.max(1, operation.applyAsInt(multiplier[slot]));
            invalidatePattern(slot);
        }
        patternsChanged();
    }

    private static int multiply(int value, int factor) {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, (long) value * factor));
    }

    /**
     * Each viewer owns a 36-slot window. The server maps it to stable physical indices; the client
     * keeps only the current snapshot. Page changes are acknowledged before interaction resumes.
     */
    private final class PatternPage extends SyncHandler<PatternPage> {
        private final boolean clientSide;
        private int page;
        private int pages = 1;
        private boolean pending;
        private int revision;
        private final ItemStack[] clientItems = new ItemStack[PAGE_SIZE];
        private final int[] clientMultipliers = new int[PAGE_SIZE];
        private final List<ModularSlot> slots = new ArrayList<>();
        private final List<IntSyncValue> multipliers = new ArrayList<>();

        private PatternPage(PanelSyncManager sync) {
            clientSide = sync.isClient();
            Arrays.fill(clientMultipliers, 1);
            allowC2S();
        }

        private int index(int slot) {
            return page * PAGE_SIZE + slot;
        }

        private boolean client() {
            return clientSide;
        }

        private int multiplierAt(int slot) {
            return client() ? clientMultipliers[slot] : multiplier[index(slot)];
        }

        private final ItemStackHandler inventory = new ItemStackHandler(new AbstractList<ItemStack>() {
            @Override
            public int size() {
                return PAGE_SIZE;
            }

            @Override
            public ItemStack get(int slot) {
                return client() ? clientItems[slot] : pattern[index(slot)];
            }

            @Override
            public ItemStack set(int slot, ItemStack stack) {
                ItemStack old = get(slot);
                if (client()) clientItems[slot] = stack;
                else pattern[index(slot)] = stack;
                return old;
            }
        }) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                if (!client()) patternChanged(index(slot));
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack != null && stack.getItem() instanceof ICraftingPatternItem;
            }
        };

        private boolean canChangePage(int direction) {
            return !pending && page + direction >= 0 && page + direction < pages;
        }

        private boolean requestPage(int direction) {
            if (!canChangePage(direction)) return true;
            pending = true;
            revision++;
            syncToServer(0, buffer -> {
                buffer.writeInt(page + direction);
                buffer.writeInt(revision);
            });
            return true;
        }

        private void sendPage() {
            pages = patternSlots.pages();
            syncToClient(0, buffer -> {
                buffer.writeInt(revision);
                buffer.writeInt(page);
                buffer.writeInt(pages);
                for (int slot = 0; slot < PAGE_SIZE; slot++) {
                    NetworkUtils.writeItemStack(buffer, pattern[index(slot)]);
                    buffer.writeInt(multiplier[index(slot)]);
                }
            });
        }

        @Override
        public void detectAndSendChanges(boolean init) {
            if (init || pages != patternSlots.pages()) sendPage();
        }

        @Override
        public void readOnClient(int id, PacketBuffer buffer) {
            // A growth snapshot sent before the request must not unlock the waiting client.
            int receivedRevision = buffer.readInt();
            if (receivedRevision < revision) return;
            revision = receivedRevision;
            page = buffer.readInt();
            pages = buffer.readInt();
            for (int slot = 0; slot < PAGE_SIZE; slot++) {
                clientItems[slot] = NetworkUtils.readItemStack(buffer);
                clientMultipliers[slot] = buffer.readInt();
                multipliers.get(slot).setIntValue(clientMultipliers[slot], false, false);
            }
            pending = false;
        }

        @Override
        public void readOnServer(int id, PacketBuffer buffer) {
            int requested = buffer.readInt();
            int requestRevision = buffer.readInt();
            if (requestRevision <= revision) return;
            revision = requestRevision;
            if (requested >= 0 && requested < patternSlots.pages()) page = requested;
            sendPage();
            // Force even equal/null slots through: a page may have the same pattern but a new index.
            for (ModularSlot slot : slots) slot.getSyncHandler().forceSyncItem();
            for (IntSyncValue value : multipliers) value.notifyUpdate();
        }

        private IntSyncValue multiplierValue(int slot) {
            IntSyncValue value = new IntSyncValue(
                () -> multiplierAt(slot),
                amount -> {
                    if (client()) clientMultipliers[slot] = Math.max(1, amount);
                    else {
                        multiplier[index(slot)] = Math.max(1, amount);
                        invalidatePattern(index(slot));
                        patternsChanged();
                    }
                }) {
                @Override
                public void write(PacketBuffer buffer) {
                    buffer.writeInt(page);
                    super.write(buffer);
                }

                @Override
                public void read(PacketBuffer buffer) {
                    int fromPage = buffer.readInt();
                    int amount = buffer.readVarIntFromBuffer();
                    // A text field may commit after focus is lost to a page button.
                    if (fromPage == page) setIntValue(amount, true, false);
                }
            }.allowC2S();
            multipliers.add(value);
            return value;
        }
    }

    private PatternSlot editablePatternSlot(PatternPage view, ModularSlot slot) {
        PatternSlot widget = new PatternSlot() {
            @Override
            public Result onMousePressed(int mouseButton) {
                return view.pending ? Result.SUCCESS : super.onMousePressed(mouseButton);
            }

            @Override
            public boolean onMouseRelease(int mouseButton) {
                return view.pending || super.onMouseRelease(mouseButton);
            }

            @Override
            public void onMouseDrag(int mouseButton, long timeSinceClick) {
                if (!view.pending) super.onMouseDrag(mouseButton, timeSinceClick);
            }

            @Override
            public boolean handleAsVanillaSlot() {
                return !view.pending && super.handleAsVanillaSlot();
            }
        };
        widget.slot(slot);
        return widget;
    }

    private ButtonWidget<?> actionButton(PanelSyncManager sync, String key, Runnable action) {
        InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(data -> {
            if (!sync.isClient()) action.run();
        });
        sync.syncValue(key, handler);
        ButtonWidget<?> button = new ButtonWidget<>().syncHandler(handler).size(34, 18)
            .background(GTGuiTextures.BUTTON_STANDARD);
        return button.onMousePressed(mouseButton -> {
            // Commit N before the action packet; normal focus loss happens after the click handler.
            button.getContext().removeFocus();
            return false;
        });
    }

    private PageButton tabButton(int index, PagedWidget.Controller tabs) {
        return new PageButton(index, tabs) {
            @Override
            public Result onMousePressed(int mouseButton) {
                getContext().removeFocus();
                return super.onMousePressed(mouseButton);
            }
        }.tab(GuiTextures.TAB_RIGHT, 0);
    }

    private ButtonWidget<?> pageButton(PatternPage view, int direction) {
        ButtonWidget<?> button = new ButtonWidget<>().size(18, 18)
            .background(GTGuiTextures.BUTTON_STANDARD)
            .overlay(IKey.str(direction < 0 ? "<" : ">")
                .color(() -> view.canChangePage(direction) ? 0xFF404040 : 0xFF999999))
            .tooltip(t -> t.addLine(IKey.lang(direction < 0
                ? "proghatches.super_pattern.previous" : "proghatches.super_pattern.next")));
        return button.onMousePressed(mouseButton -> {
            // Save a focused multiplier against the old page before asking the server to switch.
            button.getContext().removeFocus();
            return view.requestPage(direction);
        }).onUpdateListener(widget -> widget.playClickSound(view.canChangePage(direction)));
    }

    @Override
    protected ModularPanel createPatternWindow2(PanelSyncManager sync, IPanelHandler panelHandler) {
        final int width = 78;
        final int contentHeight = 168;
        PatternPage view = new PatternPage(sync);
        sync.syncValue("pattern_page", view);
        ModularPanel panel = new ModularPanel("super_pattern_window") {
            @Override
            public void onOpen(ModularScreen screen) {
                Area main = screen.getMainPanel().getArea();
                left(main.x() + main.w()).top(main.y());
                super.onOpen(screen);
            }
        };
        panel.size(width, contentHeight).onUpdateListener(widget -> {
            int height = contentHeight + (view.pages >= 2 ? 20 : 0);
            if (widget.getArea().h() != height) {
                widget.height(height);
                widget.scheduleResize();
            }
        });

        PagedWidget.Controller tabs = new PagedWidget.Controller();
        ParentWidget<?> patternsTab = new ParentWidget<>().coverChildren();
        ParentWidget<?> individualTab = new ParentWidget<>().coverChildren();
        ParentWidget<?> batchTab = new ParentWidget<>().coverChildren();

        ProghatchesUtil.sortShiftTargetsOfPopup(sync);
        sync.registerSlotGroup("super_pattern_inv", 4, ProghatchesUtil.POPUP_SLOT_SHIFT_PRIORITY);
        for (int i = 0; i < PAGE_SIZE; i++) {
            final int localSlot = i;
            final int x = i % 4 * 18 + 3;
            final int y = i / 4 * 18 + 3;
            ModularSlot editable = new ModularSlot(view.inventory, i) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    // Avoid SlotItemHandler's temporary clear/restore during a validation probe.
                    return panelHandler.isPanelOpen()
                        && stack != null && stack.getItem() instanceof ICraftingPatternItem;
                }

                @Override
                public boolean canTakeStack(EntityPlayer player) {
                    return panelHandler.isPanelOpen() && super.canTakeStack(player);
                }
            }.slotGroup("super_pattern_inv");
            view.slots.add(editable);
            // Keep the grid visible while a page request is pending, without emitting vanilla slot clicks.
            patternsTab.child(editablePatternSlot(view, editable).pos(x, y)
                .background(GTGuiTextures.SLOT_ITEM_STANDARD));
            patternsTab.child(new NonInteractiveText(IKey.dynamic(() -> {
                int amount = view.multiplierAt(localSlot);
                if (amount == 1) return "";
                String label = MULTIPLIER_FORMAT.formatWithSuffix(amount);
                return view.inventory.getStackInSlot(localSlot) == null ? "§7" + label : label;
            })).pos(x, y).size(18, 18));

            ModularSlot display = new ModularSlot(view.inventory, i).accessibility(false, false);
            view.slots.add(display);
            individualTab.child(new PatternSlot().slot(display).pos(x, y)
                .background(GTGuiTextures.SLOT_ITEM_STANDARD));
            individualTab.child(new TextFieldWidget().value(view.multiplierValue(i))
                .formatAsInteger(true).numbersInt(1, Integer.MAX_VALUE).setMaxLength(10)
                .setTextColor(com.cleanroommc.modularui.utils.Color.RED.main)
                .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.multiplier",
                    () -> new Object[] {view.index(localSlot) + 1, view.multiplierAt(localSlot)})))
                .setEnabledIf(widget -> !view.pending).pos(x, y).size(18, 18));
        }

        batchTab.child(actionButton(sync, "super_x2", () -> applyMultipliers(v -> multiply(v, 2)))
            .overlay(IKey.str("x2")).pos(3, 3)
            .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.multiply", 2))));
        batchTab.child(actionButton(sync, "super_set1", () -> applyMultipliers(v -> 1))
            .overlay(IKey.str("=1")).pos(41, 3)
            .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.set", 1))));
        batchTab.child(actionButton(sync, "super_xn", () -> applyMultipliers(v -> multiply(v, n)))
            .overlay(IKey.str("xN")).pos(3, 25)
            .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.multiply", () -> new Object[] {n}))));
        batchTab.child(actionButton(sync, "super_setn", () -> applyMultipliers(v -> n))
            .overlay(IKey.str("=N")).pos(41, 25)
            .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.set", () -> new Object[] {n}))));
        batchTab.child(IKey.str("N").asWidget().pos(3, 53));
        batchTab.child(new TextFieldWidget().value(new IntSyncValue(() -> n, v -> n = Math.max(1, v)).allowC2S())
            .formatAsInteger(true).numbersInt(1, Integer.MAX_VALUE).setMaxLength(10)
            .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.factor")))
            .size(60, 18).pos(15, 49).background(GTGuiTextures.BACKGROUND_TEXT_FIELD));
        batchTab.child(createRefundButton2(sync).pos(3, 81));
        batchTab.child(IKey.lang("proghatches.super_pattern.refund").asWidget().pos(21, 85));

        Item glove = GameRegistry.findItem("miscutils", "GTPP.bauble.fireprotection.0.name");
        IDrawable dragIcon = glove == null ? GTGuiTextures.OVERLAY_BUTTON_PLUS_LARGE.asIcon().size(18, 18)
            : new ItemDrawable(new ItemStack(glove)).asIcon().size(18, 18);
        panel.child(new Widget<>().pos(width - 3, -1).size(32, 112).excludeAreaInRecipeViewer());
        panel.child(new Column().coverChildren().pos(width - 3, -1)
            .child(new DragTab().background(GuiTextures.TAB_RIGHT.get(-1, false),
                dragIcon).size(32, 28)
                .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.drag"))))
            .child(tabButton(0, tabs)
                .overlay(new ItemDrawable(AEApi.instance().definitions().items().encodedPattern().maybeStack(1).get())
                    .asIcon().size(18, 18))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.patterns"))
                    .addLine(IKey.lang("proghatches.super_pattern.capacity",
                        () -> new Object[] {view.pages * PAGE_SIZE, CAPACITY}))
                    .addLine(IKey.lang("proghatches.super_pattern.unlock"))))
            .child(tabButton(1, tabs)
                .overlay(GTGuiTextures.OVERLAY_BUTTON_BATCH_MODE_OFF.asIcon().size(18, 18))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.individual"))))
            .child(tabButton(2, tabs)
                .overlay(GTGuiTextures.OVERLAY_BUTTON_BATCH_MODE_ON.asIcon().size(18, 18))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.batch")))));
        panel.child(new PagedWidget<>().controller(tabs).pos(0, 0).size(width, contentHeight)
            .addPage(patternsTab).addPage(individualTab).addPage(batchTab));

        ParentWidget<?> navigation = new ParentWidget<>().size(width, 20).pos(0, contentHeight)
            .setEnabledIf(widget -> view.pages >= 2);
        navigation.child(pageButton(view, -1).pos(3, 0));
        navigation.child(IKey.dynamic(() -> view.pending ? "..." : (view.page + 1) + "/" + view.pages).asWidget()
            .textAlign(Alignment.Center).pos(21, 0).size(36, 18)
            .tooltip(t -> t.addLine(IKey.lang("proghatches.super_pattern.page",
                () -> new Object[] {view.page + 1, view.pages}))
                .addLine(IKey.lang("proghatches.super_pattern.range",
                    () -> new Object[] {view.index(0) + 1, view.index(PAGE_SIZE - 1) + 1}))
                .addLine(IKey.lang("proghatches.super_pattern.capacity",
                    () -> new Object[] {view.pages * PAGE_SIZE, CAPACITY}))
                .addLine(IKey.lang("proghatches.super_pattern.unlock"))));
        navigation.child(pageButton(view, 1).pos(57, 0));
        panel.child(navigation);
        return panel;
    }
}
