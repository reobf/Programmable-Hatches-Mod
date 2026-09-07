package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import gregtech.api.GregTechAPI;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.util.GTTooltipDataCache;
import gregtech.api.util.GTTooltipDataCache.TooltipData;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.IMultiCircuitSupport;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.ProghatchesUtil;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class MultiCircuitInputBus extends MTEHatchInputBus implements IMultiCircuitSupport, IDataCopyablePlaceHolder {
    /**
     * GT 290's hatch base classes override getDescription() with their own hardcoded
     * "input bus / output hatch / ..." text, which shadowed every PH machine's own tooltip
     * (the Config.get(...) template passed to the constructor). Hand it back.
     */
    @Override
    public String[] getDescription() {
        return mDescriptionArray;
    }


    @Override
    public IItemHandlerModifiable getInventoryHandler() {
        // TODO Auto-generated method stub
        return super.getInventoryHandler();
    }

    int uiButtonCount;

    private Widget createToggleButton(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
        Supplier<GTTooltipDataCache.TooltipData> tooltipDataSupplier) {
        return new CycleButtonWidget().setToggle(getter, setter)
            .setStaticTexture(picture)
            .setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
            .setTooltipShowUpDelay(TOOLTIP_DELAY)
            .setPos(7 + (uiButtonCount++ * BUTTON_SIZE), 62)
            .setSize(BUTTON_SIZE, BUTTON_SIZE)
            .setGTTooltip(tooltipDataSupplier);
    }

    public void addSortStacksButton(ModularWindow.Builder builder) {
        builder.widget(
            createToggleButton(
                () -> !disableSort,
                val -> disableSort = !val,
                GTUITextures.OVERLAY_BUTTON_SORTING_MODE,
                () -> mTooltipCache.getData(SORTING_MODE_TOOLTIP)));
    }

    public void addOneStackLimitButton(ModularWindow.Builder builder) {
        builder.widget(createToggleButton(() -> !disableLimited, val -> {
            disableLimited = !val;
            updateSlots();
        }, GTUITextures.OVERLAY_BUTTON_ONE_STACK_LIMIT, () -> mTooltipCache.getData(ONE_STACK_LIMIT_TOOLTIP)));
    }

    private static final String SORTING_MODE_TOOLTIP = "GT5U.machines.sorting_mode.tooltip";
    private static final String ONE_STACK_LIMIT_TOOLTIP = "GT5U.machines.one_stack_limit.tooltip";
    private static final int BUTTON_SIZE = 18;

    /**
     * Item slots are always allocated for the largest variant so that changing a variant's tier can
     * never truncate an already-saved inventory or move the circuit slots. See usableItemSlots().
     */
    public static final int ITEM_SLOT_CAPACITY = 16;

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        buildContext.addCloseListener(() -> uiButtonCount = 0);
        addSortStacksButton(builder);
        addOneStackLimitButton(builder);
        super.addUIWidgets(builder, buildContext);

        // NOTE: this switch used to fall through, so every tier drew all four grids on top of each
        // other. It only went unnoticed while the bus was registered at tier 4..7 (always `default`).
        switch (mTier) {
            case 0:
                getBaseMetaTileEntity().add1by1Slot(builder);
                break;
            case 1:
                getBaseMetaTileEntity().add2by2Slots(builder);
                break;
            case 2:
                getBaseMetaTileEntity().add3by3Slots(builder);
                break;
            default:
                getBaseMetaTileEntity().add4by4Slots(builder);
        }

        ProghatchesUtil.attachZeroSizedStackRemover(builder, buildContext);
        for (int i = 1; i < 4; i++) builder.widget(new SlotWidget(new BaseSlot(inventoryHandler, getCircuitSlot() + i) {

            public int getSlotStackLimit() {
                return 0;
            };

        }

        ) {

            @Override
            public List<String> getExtraTooltip() {
                return Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"));
            }
        }.disableShiftInsert()
            .setHandlePhantomActionClient(true)
            .setGTTooltip(
                () -> new TooltipData(
                    Arrays.asList(
                        LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
                        LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")),
                    Arrays.asList(
                        LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
                        LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"))))
            .setPos(getCircuitSlotX() - 1, getCircuitSlotY() - 18 * i - 1)

        );
    }

    public MultiCircuitInputBus(int id, String name, String nameRegional, int tier, String... optional) {

        super(
            id,
            name,
            nameRegional,
            tier,
            ITEM_SLOT_CAPACITY + 4,
            (optional.length > 0 ? optional
                : reobf.proghatches.main.Config.get(
                    "MCIB",
                    ImmutableMap.of(

                        "slots",
                        Math.min(16, (1 + tier) * (tier + 1))

                    ))

            )

        );

        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id));

    }

    public MultiCircuitInputBus(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures) {
        super(mName, mTier, ITEM_SLOT_CAPACITY + 4, mDescriptionArray, mTextures);
    }

    /**
     * GT 290 changed {@code MTEHatch.getSlots(tier)} from the old 16-capped formula to a plain
     * (tier+1)^2, but this bus is still built with {@code ProghatchesUtil.getSlots(tier) + 4}
     * slots — so the inherited {@code getCircuitSlot()} started pointing past the end of the
     * inventory (25..64 for a 20 slot inventory). That made every circuit slot index invalid:
     * the MUI2 ghost circuit handler threw while opening the GUI, and getCircuitSlots() /
     * isValidSlot() / allowPullStack() all worked on non-existent slots. Pin it to the real
     * layout instead: ProghatchesUtil.getSlots(tier) item slots, then the four circuit slots.
     */
    @Override
    public int getCircuitSlot() {
        return ITEM_SLOT_CAPACITY;
    }

    /**
     * How many of the allocated item slots this tier actually exposes: 1 / 4 / 9 / 16 for ULV..HV.
     * <p>
     * The backing array is always {@link #ITEM_SLOT_CAPACITY} + 4 entries regardless of tier, so the
     * circuit slots keep the same indices on every variant. That matters for compatibility: shrinking
     * the array on the lower tiers would drop the saved stacks past the new end AND shift the four
     * circuit marks to different indices, silently wiping every existing bus's configuration. Slots
     * at or past this limit stay readable and extractable (see allowPullStack / isValidSlot) so
     * anything already stored in a bus that just became a lower tier can still be drained out, and
     * fillStacksIntoFirstSlots() leaves them alone instead of compacting them away.
     */
    private int usableItemSlots() {
        return ProghatchesUtil.getSlots(mTier);
    }

    /** Side length of the square item slot grid actually drawn for this tier. */
    private int itemGridDimension() {
        return Math.max(1, (int) Math.ceil(Math.sqrt(usableItemSlots())));
    }

    @Override
    public com.cleanroommc.modularui.screen.ModularPanel buildUI(
        com.cleanroommc.modularui.factory.PosGuiData data,
        com.cleanroommc.modularui.value.sync.PanelSyncManager syncManager,
        com.cleanroommc.modularui.screen.UISettings uiSettings) {
        com.cleanroommc.modularui.screen.ModularPanel panel = new Gui(this).build(data, syncManager, uiSettings);
        // the MUI1 GUI attached this; carry it over so a 0-sized stack can never be left on the
        // cursor / in the player inventory (negative stack size dupe guard)
        ProghatchesUtil.attachZeroSizedStackRemover2(syncManager, panel);
        return panel;
    }

    /**
     * The stock bus GUI sizes its slot grid from the tier ((tier+1)^2 slots), which no longer
     * matches this bus, and it only draws a single circuit slot. This one uses the real slot
     * count and adds the three extra marking slots next to the stock circuit slot.
     */
    private static class Gui extends gregtech.common.gui.modularui.hatch.MTEHatchInputBusGui {

        private final MultiCircuitInputBus bus;

        Gui(MultiCircuitInputBus bus) {
            super(bus);
            this.bus = bus;
        }

        @Override
        protected int getDimension() {
            return bus.itemGridDimension();
        }

        /**
         * The GregTech logo shares the bottom-right corner row with the circuit slot. Dropping it
         * frees the 17px the extra circuit column needs, and keeps the corner from being crowded.
         */
        @Override
        protected boolean doesAddGregTechLogo() {
            return false;
        }

        /**
         * The three extra circuit slots used to be appended to the bottom-right corner <em>row</em>,
         * which grows leftwards (reverseLayout) and therefore ran straight over the right half of the
         * item grid. Stack them vertically above the stock circuit slot instead - the same layout the
         * MUI1 GUI used ({@code getCircuitSlotY() - 18 * i}) - so nothing overlaps: the column is
         * 18px wide at the right edge, while the centered 4x4 grid ends well left of it.
         */
        @Override
        protected com.cleanroommc.modularui.widget.ParentWidget<?> createContentSection(
            com.cleanroommc.modularui.screen.ModularPanel panel,
            com.cleanroommc.modularui.value.sync.PanelSyncManager syncManager) {
            com.cleanroommc.modularui.widget.ParentWidget<?> content = super.createContentSection(panel, syncManager);

            com.cleanroommc.modularui.widgets.layout.Flow column = com.cleanroommc.modularui.widgets.layout.Flow
                .column()
                .coverChildren()
                .right(0)
                // directly on top of the stock circuit slot, which sits in the bottom-right corner row
                .bottom(SLOT_SIZE);

            // topmost is the highest index, so slot+1 ends up adjacent to the stock circuit slot
            for (int i = 3; i >= 1; i--) {
                column.child(markingSlot(bus.getCircuitSlot() + i));
            }
            return content.child(column);
        }

        private com.cleanroommc.modularui.api.widget.IWidget markingSlot(final int slot) {
            return new com.cleanroommc.modularui.widgets.slot.PhantomItemSlot()
                .syncHandler(
                    // ClearableMarkSlotSH: marks are stored with stackSize 0, and MUI2's default
                    // left-click path is then a no-op, so the mark could not be removed by clicking it
                    new reobf.proghatches.gt.metatileentity.util.ClearableMarkSlotSH(
                        new com.cleanroommc.modularui.widgets.slot.ModularSlot(bus.inventoryHandler, slot) {

                            @Override
                            public int getSlotStackLimit() {
                                // marking slot: the circuit is only a marker, never stored
                                return 0;
                            }
                        }))
                .background(
                    gregtech.api.modularui2.GTGuiTextures.SLOT_ITEM_STANDARD,
                    gregtech.api.modularui2.GTGuiTextures.OVERLAY_SLOT_INT_CIRCUIT)
                .tooltipBuilder(t -> {
                    t.addLine(
                        com.cleanroommc.modularui.api.drawable.IKey.lang("programmable_hatches.gt.marking.slot.0"));
                    t.addLine(
                        com.cleanroommc.modularui.api.drawable.IKey.lang("programmable_hatches.gt.marking.slot.1"));
                })
                .tooltipShowUpTimer(TOOLTIP_DELAY);
        }
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return aIndex < usableItemSlots();
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        // deliberately getCircuitSlot() and not usableItemSlots(): a bus that used to be a higher
        // tier may still hold stacks past its current grid, and those have to remain extractable
        if (aIndex >= getCircuitSlot()) return false;
        return side == getBaseMetaTileEntity().getFrontFacing();
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return side == getBaseMetaTileEntity().getFrontFacing() && aIndex < usableItemSlots()
            && (mRecipeMap == null || disableFilter || mRecipeMap.containsInput(aStack))
            && (disableLimited || limitedAllowPutStack(aIndex, aStack));
    }

    int[] cSlotCache;

    @Override
    public int[] getCircuitSlots() {
        if (cSlotCache != null) return cSlotCache;
        return cSlotCache = new int[] { getCircuitSlot(), getCircuitSlot() + 1, getCircuitSlot() + 2,
            getCircuitSlot() + 3 };
    }

    // Matter Manipulator copy/paste. The whole user configuration of this bus is the four circuit
    // marks in getCircuitSlots() (zero-sized ghost items); MM's generic ghost-circuit handling only
    // reaches the first of them, because GhostCircuitItemStackHandler is built from the single
    // getCircuitSlot(). Deliberately NOT copied: the real item stock in slots 0..ITEM_SLOT_CAPACITY-1,
    // mRecipeMap (pushed by the multiblock, not user-set), and disableSort/disableLimited/disableFilter,
    // which MM already carries generically for every MTEHatchInputBus. No tier clamping is needed: every
    // variant allocates ITEM_SLOT_CAPACITY + 4 slots, so the circuit slots are 16..19 on all of them.
    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = new NBTTagCompound();
        writeType(ret, player);
        int[] slots = getCircuitSlots();
        for (int i = 0; i < slots.length; i++) {
            // saveItem(null) is an empty tag that loads back as null, so an EMPTY mark is copied too:
            // pasting a config with no circuits onto a configured bus clears the ones it had
            ret.setTag("circuit" + i, GTUtility.saveItem(getStackInSlot(slots[i])));
        }
        return ret;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
        int[] slots = getCircuitSlots();
        for (int i = 0; i < slots.length; i++) {
            String key = "circuit" + i;
            if (!nbt.hasKey(key)) continue;
            // marks live at stackSize 0 (GTUtility.getIntegratedCircuit / ClearableMarkSlotSH / the
            // ProgrammingCover all store them that way), so force the amount back to 0 here;
            // copyAmount returns null for an empty or unloadable tag, which clears the slot
            ItemStack mark = GTUtility.copyAmount(0, GTUtility.loadItem(nbt, key));
            // same write path as the ProgrammingCover: it runs onContentsChanged()/markDirty(), the
            // hook the GUI's phantom marking slots trigger too
            setInventorySlotContents(slots[i], mark);
        }
        return true;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MultiCircuitInputBus(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public void updateSlots() {
        for (int i = 0; i < mInventory.length - 4; i++)
            if (mInventory[i] != null && mInventory[i].stackSize <= 0) mInventory[i] = null;
        if (!disableSort) fillStacksIntoFirstSlots();
    }

    protected void fillStacksIntoFirstSlots() {
        final int L = mInventory.length - 4;
        HashMap<GTUtility.ItemId, Integer> slots = new HashMap<>(L);
        HashMap<GTUtility.ItemId, ItemStack> stacks = new HashMap<>(L);
        List<GTUtility.ItemId> order = new ArrayList<>(L);
        List<Integer> validSlots = new ArrayList<>(L);
        for (int i = 0; i < L; i++) {
            if (!isValidSlot(i)) continue;
            validSlots.add(i);
            ItemStack s = mInventory[i];
            if (s == null) continue;
            GTUtility.ItemId sID = GTUtility.ItemId.createNoCopy(s);
            slots.merge(sID, s.stackSize, Integer::sum);
            if (!stacks.containsKey(sID)) stacks.put(sID, s);
            order.add(sID);
            mInventory[i] = null;
        }
        int slotindex = 0;
        for (GTUtility.ItemId sID : order) {
            int toSet = slots.get(sID);
            if (toSet == 0) continue;
            int slot = validSlots.get(slotindex);
            slotindex++;
            mInventory[slot] = stacks.get(sID)
                .copy();
            toSet = Math.min(toSet, mInventory[slot].getMaxStackSize());
            mInventory[slot].stackSize = toSet;
            slots.merge(sID, toSet, (a, b) -> a - b);
        }
    }
}
