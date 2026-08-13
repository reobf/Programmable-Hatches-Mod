package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
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
import reobf.proghatches.gt.metatileentity.util.IMultiCircuitSupport;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.ProghatchesUtil;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class MultiCircuitInputBus extends MTEHatchInputBus implements IMultiCircuitSupport {

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

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        buildContext.addCloseListener(() -> uiButtonCount = 0);
        addSortStacksButton(builder);
        addOneStackLimitButton(builder);
        super.addUIWidgets(builder, buildContext);

        switch (mTier) {
            case 0:
                getBaseMetaTileEntity().add1by1Slot(builder);
            case 1:
                getBaseMetaTileEntity().add2by2Slots(builder);
            case 2:
                getBaseMetaTileEntity().add3by3Slots(builder);
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
            ProghatchesUtil.getSlots(tier) + 4,
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
        super(mName, mTier, ProghatchesUtil.getSlots(mTier) + 4, mDescriptionArray, mTextures);
    }

    /**
     * GT 290 changed {@code MTEHatch.getSlots(tier)} from the old 16-capped formula to a plain
     * (tier+1)^2, but this bus is still built with {@code ProghatchesUtil.getSlots(tier) + 4}
     * slots — so the inherited {@code getCircuitSlot()} started pointing past the end of the
     * inventory (25..64 for a 20 slot inventory). That made every circuit slot index invalid:
     * the MUI2 ghost circuit handler threw while opening the GUI, and getCircuitSlots() /
     * isValidSlot() / allowPullStack() all worked on non-existent slots. Pin it to the real
     * layout instead: 16 item slots, then the four circuit slots.
     */
    @Override
    public int getCircuitSlot() {
        return ProghatchesUtil.getSlots(mTier);
    }

    /** Side length of the square item slot grid, i.e. everything before the circuit slots. */
    private int itemGridDimension() {
        int slots = getCircuitSlot();
        int dim = (int) Math.ceil(Math.sqrt(slots));
        return Math.max(1, dim);
    }

    @Override
    public com.cleanroommc.modularui.screen.ModularPanel buildUI(
        com.cleanroommc.modularui.factory.PosGuiData data,
        com.cleanroommc.modularui.value.sync.PanelSyncManager syncManager,
        com.cleanroommc.modularui.screen.UISettings uiSettings) {
        return new Gui(this).build(data, syncManager, uiSettings);
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

        @Override
        protected com.cleanroommc.modularui.widgets.layout.Flow createBottomRightCornerFlow(
            com.cleanroommc.modularui.screen.ModularPanel panel,
            com.cleanroommc.modularui.value.sync.PanelSyncManager syncManager) {
            com.cleanroommc.modularui.widgets.layout.Flow flow = super.createBottomRightCornerFlow(panel, syncManager);
            // reverse layout: children are laid out right to left, so these end up left of the
            // stock circuit slot
            for (int i = 1; i < 4; i++) {
                final int slot = bus.getCircuitSlot() + i;
                flow.child(
                    new com.cleanroommc.modularui.widgets.slot.PhantomItemSlot()
                        .syncHandler(
                            new com.cleanroommc.modularui.value.sync.PhantomItemSlotSH(
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
                                com.cleanroommc.modularui.api.drawable.IKey
                                    .lang("programmable_hatches.gt.marking.slot.0"));
                            t.addLine(
                                com.cleanroommc.modularui.api.drawable.IKey
                                    .lang("programmable_hatches.gt.marking.slot.1"));
                        })
                        .tooltipShowUpTimer(TOOLTIP_DELAY));
            }
            return flow;
        }
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return aIndex < getCircuitSlot();
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        if (aIndex >= getCircuitSlot()) return false;
        return side == getBaseMetaTileEntity().getFrontFacing();
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return side == getBaseMetaTileEntity().getFrontFacing() && aIndex < getCircuitSlot()
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
