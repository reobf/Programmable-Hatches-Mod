package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.FluidDisplayWidget;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextEditorWidget;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.recipe.RecipeMap;
import reobf.proghatches.item.patterngen.PatternGeneratorSettings.FilterMode;
import reobf.proghatches.main.MyMod;

/**
 * Server-authoritative ModularUI2 screen for the pattern generator.
 * <p>
 * Visible tabs are Main, Filters, Preview, Target and Presets. The status line is shared by
 * every tab.
 */
public final class PatternGeneratorPanel {

    private static final int WIDTH = 340;
    private static final int HEIGHT = 240;
    private static final int CONTENT_Y = 26;
    private static final int CONTENT_HEIGHT = 196;
    private static final int PAGE_SIZE = 7;
    private static final int MAX_INPUT_ICONS = 6;
    private static final int MAX_OUTPUT_ICONS = 2;
    private static final int ORE_PAGE_SIZE = 6;
    private static final int PRESET_PAGE_SIZE = 6;
    private static final int MAX_FILTER_TEXT = 2048;
    private static final int MAX_AUTHOR = 64;
    private static List<String> ORE_PREFIXES;

    private final GuiData data;
    private final PanelSyncManager sync;
    private final ItemStack tool;
    private final PatternGeneratorSettings config;
    private final Runnable persist;
    private final PagedWidget.Controller tabs = new PagedWidget.Controller();

    private final String[] status = { "" };
    private final String[] filterStatus = { "" };
    private final String[] presetStatus = { "" };
    private final String[] presetName = { "" };
    private final String[] targetInfo = { "" };
    private final int[] matchCount = { 0 };
    private final int[] selectedCount = { 0 };
    private final int[] page = { 0 };
    private final int[] pendingRemaining = { 0 };
    private final int[] pendingTotal = { 0 };
    private final int[] presetPage = { 0 };
    private final boolean[] armed = { false };
    private final boolean[] recompute = { true };
    private boolean problemActive;
    private boolean publishingMap;
    private boolean machineErrorActive;
    private boolean closeGuiPending;
    private com.cleanroommc.modularui.widgets.textfield.TextFieldWidget presetNameField;
    private ItemStackHandler machineHandler;
    private ItemSlot machineSlot;
    private boolean applyingPreset;

    private StringSyncValue mapValue;
    private StringSyncValue statusValue;
    private IntSyncValue matchValue;
    private IntSyncValue selectedValue;
    private IntSyncValue pageValue;
    private IntSyncValue circuitValue;
    private IntSyncValue minVoltageValue;
    private IntSyncValue maxVoltageValue;
    private GenericSyncValue<NBTTagCompound, ?> pageDataValue;
    private BooleanSyncValue conflictValue;
    private BooleanSyncValue byproductValue;
    private BooleanSyncValue armedValue;
    private StringSyncValue authorValue;
    private StringSyncValue targetValue;
    private IntSyncValue pendingValue;
    private IntSyncValue pendingTotalValue;
    private com.cleanroommc.modularui.value.StringValue.Dynamic presetNameValue;
    private GenericSyncValue<NBTTagCompound, ?> presetLoadValue;
    private GenericSyncValue<NBTTagCompound, ?> selectionValue;
    private NBTTagCompound selectionData = new NBTTagCompound();
    private NBTTagCompound pageData = new NBTTagCompound();
    private NBTTagCompound presetData = new NBTTagCompound();
    private NBTTagCompound presetLoadData = new NBTTagCompound();
    private PreviewPage pageModel = new PreviewPage();
    private PresetPage presetModel = new PresetPage();

    private final List<FilterSection> filterSections = new ArrayList<>();
    private final List<String> pendingOres = new ArrayList<>();
    private FilterSection pendingOreSection;
    private int orePage;

    private final List<String> pendingMaps = new ArrayList<>();
    private final List<String> pendingMapNames = new ArrayList<>();
    private int mapPage;

    private final List<Integer> pendingVoltages = new ArrayList<>();
    private boolean voltagePickerMax;
    private int voltagePage;

    public PatternGeneratorPanel(GuiData data, PanelSyncManager sync, ItemStack tool,
        PatternGeneratorSettings config, Runnable persist) {
        this.data = data;
        this.sync = sync;
        this.tool = tool;
        this.config = config;
        this.persist = persist;
    }

    public ModularPanel build() {
        ModularPanel panel = new ModularPanel("pattern_generator").size(WIDTH, HEIGHT)
            .background(GTGuiTextures.BACKGROUND_STANDARD);
        createSyncValues();

        ParentWidget<?> mainPage = new ParentWidget<>().size(WIDTH, CONTENT_HEIGHT);
        ParentWidget<?> filtersPage = new ParentWidget<>().size(WIDTH, CONTENT_HEIGHT);
        ParentWidget<?> previewPage = new ParentWidget<>().size(WIDTH, CONTENT_HEIGHT);
        ParentWidget<?> targetPage = new ParentWidget<>().size(WIDTH, CONTENT_HEIGHT);
        ParentWidget<?> presetsPage = new ParentWidget<>().size(WIDTH, CONTENT_HEIGHT);

        buildMainPage(mainPage);
        buildFiltersPage(filtersPage);
        buildPreviewPage(previewPage);
        buildTargetPage(targetPage);
        buildPresetsPage(presetsPage);

        panel.child(
            new PagedWidget<>().controller(this.tabs)
                .pos(0, CONTENT_Y)
                .size(WIDTH, CONTENT_HEIGHT)
                .addPage(mainPage)
                .addPage(filtersPage)
                .addPage(previewPage)
                .addPage(targetPage)
                .addPage(presetsPage));

        String[] tabKeys = { "proghatches.pattern_generator.tab.main", "proghatches.pattern_generator.tab.filters",
            "proghatches.pattern_generator.tab.preview", "proghatches.pattern_generator.tab.target",
            "proghatches.pattern_generator.tab.presets" };
        for (int i = 0; i < tabKeys.length; i++) {
            final int index = i;
            panel.child(
                new ButtonWidget<>().size(62, 16)
                    .pos(4 + i * 66, 4)
                    .background(GTGuiTextures.BUTTON_STANDARD)
                    .overlay(IKey.dynamic(() -> tabLabel(index, tabKeys[index])))
                    .onMousePressed(mouseButton -> {
                        if (this.sync.isClient()) {
                            if (index != 0) clearMapPicker();
                            if (index != 1) {
                                clearOrePicker();
                                clearVoltagePicker();
                            }
                            this.tabs.setPage(index);
                        }
                        return true;
                    }));
        }

        panel.child(
            new TextWidget<>(IKey.dynamic(() -> this.status[0])).pos(5, 226)
                .size(330, 12)
                .color(0xFF202020)
                .alignment(Alignment.CenterLeft));

        this.sync.onServerTick(this::serverTick);
        this.sync.onClientTick(this::clientTick);
        return panel;
    }

    private void clientTick() {
        if (!this.closeGuiPending) return;
        this.closeGuiPending = false;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer != null) minecraft.thePlayer.closeScreen();
    }

    private void createSyncValues() {
        this.mapValue = new StringSyncValue(() -> this.config.recipeMapId, value -> {
            this.config.recipeMapId = value == null ? "" : value;
            if (!this.sync.isClient() && !this.publishingMap) {
                this.persist.run();
                markRecompute();
            }
        }).allowC2S();
        this.statusValue = new StringSyncValue(() -> this.status[0], value -> {
            this.status[0] = value == null ? "" : value;
        }).allowC2S();
        this.matchValue = new IntSyncValue(() -> this.matchCount[0], value -> this.matchCount[0] = Math.max(0, value));
        this.selectedValue = new IntSyncValue(
            () -> this.selectedCount[0],
            value -> this.selectedCount[0] = Math.max(0, value));
        this.pageValue = new IntSyncValue(() -> this.page[0], value -> {
            this.page[0] = Math.max(0, value);
            markRecompute();
        }).allowC2S();
        this.conflictValue = new BooleanSyncValue(() -> this.config.onlyConflict, value -> {
            this.config.onlyConflict = value;
            if (!this.sync.isClient()) {
                this.persist.run();
                markRecompute();
            }
        }).allowC2S();
        this.byproductValue = new BooleanSyncValue(() -> this.config.firstOutputOnly, value -> {
            this.config.firstOutputOnly = value;
            if (!this.sync.isClient()) this.persist.run();
        }).allowC2S();
        this.armedValue = new BooleanSyncValue(() -> this.armed[0], value -> this.armed[0] = value);
        this.authorValue = new StringSyncValue(() -> this.config.author, value -> {
            String safe = value == null ? "" : value;
            if (safe.length() > MAX_AUTHOR) safe = safe.substring(0, MAX_AUTHOR);
            this.config.author = safe;
            if (!this.sync.isClient()) this.persist.run();
        }).allowC2S();
        this.targetValue = new StringSyncValue(
            () -> this.targetInfo[0],
            value -> this.targetInfo[0] = value == null ? "" : value);
        this.pendingValue = new IntSyncValue(
            () -> this.pendingRemaining[0],
            value -> this.pendingRemaining[0] = Math.max(0, value));
        this.pendingTotalValue = new IntSyncValue(
            () -> this.pendingTotal[0],
            value -> this.pendingTotal[0] = Math.max(0, value));
        this.circuitValue = new IntSyncValue(() -> this.config.circuit, value -> {
            this.config.circuit = Math.max(-1, Math.min(32, value));
            if (!this.sync.isClient()) {
                this.persist.run();
                markRecompute();
            }
        }).allowC2S();
        this.minVoltageValue = new IntSyncValue(() -> this.config.minVoltage, value -> {
            this.config.minVoltage = Math.max(-1, value);
            if (!this.sync.isClient()) {
                this.persist.run();
                markRecompute();
            }
        }).allowC2S();
        this.maxVoltageValue = new IntSyncValue(() -> this.config.maxVoltage, value -> {
            this.config.maxVoltage = Math.max(-1, value);
            if (!this.sync.isClient()) {
                this.persist.run();
                markRecompute();
            }
        }).allowC2S();
        this.presetNameValue = new com.cleanroommc.modularui.value.StringValue.Dynamic(
            () -> this.presetName[0],
            value -> this.presetName[0] = value == null ? "" : value);
        this.pageDataValue = new GenericSyncValue<>(
            NBTTagCompound.class,
            () -> this.pageData,
            value -> {
                this.pageData = value == null ? new NBTTagCompound() : (NBTTagCompound) value.copy();
                this.pageModel = PreviewPage.fromNbt(this.pageData);
            },
            ByteBufAdapters.NBT,
            nbt -> (NBTTagCompound) nbt.copy());
        this.presetLoadValue = new GenericSyncValue<>(
            NBTTagCompound.class,
            () -> this.presetLoadData,
            value -> {
                this.presetLoadData = value == null ? new NBTTagCompound() : (NBTTagCompound) value.copy();
                if (!this.sync.isClient()) applyClientPreset(this.presetLoadData);
            },
            ByteBufAdapters.NBT,
            nbt -> (NBTTagCompound) nbt.copy()).allowC2S();
        this.selectionValue = new GenericSyncValue<>(
            NBTTagCompound.class,
            () -> this.selectionData,
            value -> {
                this.selectionData = value == null ? new NBTTagCompound() : (NBTTagCompound) value.copy();
                this.config.selectedRecipes.clear();
                NBTTagList ids = this.selectionData.getTagList("ids", 8);
                for (int i = 0; i < ids.tagCount(); i++) {
                    String id = ids.getStringTagAt(i);
                    if (id != null && !id.isEmpty()) this.config.selectedRecipes.add(id);
                }
            },
            ByteBufAdapters.NBT,
            nbt -> (NBTTagCompound) nbt.copy());

        this.sync.syncValue("pattern_generator_map", this.mapValue);
        this.sync.syncValue("pattern_generator_status", this.statusValue);
        this.sync.syncValue("pattern_generator_matches", this.matchValue);
        this.sync.syncValue("pattern_generator_selected", this.selectedValue);
        this.sync.syncValue("pattern_generator_page", this.pageValue);
        this.sync.syncValue("pattern_generator_circuit", this.circuitValue);
        this.sync.syncValue("pattern_generator_min_voltage", this.minVoltageValue);
        this.sync.syncValue("pattern_generator_max_voltage", this.maxVoltageValue);
        this.sync.syncValue("pattern_generator_only_conflict", this.conflictValue);
        this.sync.syncValue("pattern_generator_first_output_only", this.byproductValue);
        this.sync.syncValue("pattern_generator_armed", this.armedValue);
        this.sync.syncValue("pattern_generator_author", this.authorValue);
        this.sync.syncValue("pattern_generator_target_info", this.targetValue);
        this.sync.syncValue("pattern_generator_pending", this.pendingValue);
        this.sync.syncValue("pattern_generator_pending_total", this.pendingTotalValue);
        this.sync.syncValue("pattern_generator_page_data", this.pageDataValue);
        this.sync.syncValue("pattern_generator_preset_load", this.presetLoadValue);
        this.sync.syncValue("pattern_generator_selection", this.selectionValue);
    }

    private void buildTargetPage(ParentWidget<?> page) {
        page.child(
            IKey.lang("proghatches.pattern_generator.target.title")
                .asWidget()
                .pos(5, 4));
        page.child(
            new ButtonWidget<>().size(105, 18)
                .pos(4, 18)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.target.fill_selected"))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.target.fill_selected.tooltip")))
                .syncHandler(armHandler("pattern_generator_arm_selected", true)));
        page.child(
            new ButtonWidget<>().size(105, 18)
                .pos(113, 18)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.target.fill_matching"))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.target.fill_matching.tooltip")))
                .syncHandler(armHandler("pattern_generator_arm_matching", false)));

        page.child(
            new TextWidget<>(IKey.dynamic(this::targetHint)).pos(5, 42)
                .size(330, 10));
        page.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> fmt(
                        "proghatches.pattern_generator.target.pending",
                        this.pendingValue.getIntValue(),
                        this.pendingTotalValue.getIntValue()))).pos(5, 54)
                .size(330, 10));
        page.child(
            new TextWidget<>(IKey.dynamic(() -> this.targetInfo[0])).pos(5, 66)
                .size(330, 10));

        page.child(
            new ButtonWidget<>().size(70, 18)
                .pos(4, 82)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.target.resume"))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.target.resume.tooltip")))
                .syncHandler(resumeHandler()));
        page.child(
            new ButtonWidget<>().size(70, 18)
                .pos(78, 82)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.target.cancel"))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.target.cancel.tooltip")))
                .syncHandler(cancelHandler()));

        page.child(
            new ButtonWidget<>().size(160, 14)
                .pos(4, 108)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(
                    IKey.dynamic(
                        () -> (PatternGeneratorPanel.this.config.firstOutputOnly ? "§a" : "§8")
                            + fmt("proghatches.pattern_generator.target.byproducts")))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.target.byproducts.tooltip")))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) {
                        this.byproductValue.setBoolValue(!this.byproductValue.getBoolValue(), true, true);
                    }
                    return true;
                }));

        page.child(
            IKey.lang("proghatches.pattern_generator.target.author")
                .asWidget()
                .pos(5, 130));
        page.child(
            new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget().value(this.authorValue)
                .setMaxLength(MAX_AUTHOR)
                .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
                .setTextColor(0xFFFFFFFF)
                .pos(5, 140)
                .size(180, 18)
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.target.author.tooltip"))));

        page.child(
            new TextWidget<>(IKey.lang("proghatches.pattern_generator.target.hint")).pos(5, 166)
                .size(330, 10)
                .scale(0.8f));
    }

    private void closeGuiNextTick() {
        this.closeGuiPending = true;
    }

    private InteractionSyncHandler armHandler(String key, boolean selectedOnly) {
        InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(click -> {
            if (this.sync.isClient()) {
                closeGuiNextTick();
                return;
            }
            if (!(this.data.getPlayer() instanceof EntityPlayerMP player)) return;
            int result = PatternGeneratorFillQueue.arm(player, currentTool(), selectedOnly);
            if (result == PatternGeneratorFillQueue.ARM_OK) {
                updateTargetState();
            } else if (result == PatternGeneratorFillQueue.ARM_ALREADY_PENDING) {
                setStatus(fmt("proghatches.pattern_generator.target.exists"));
            } else if (result == PatternGeneratorRecipes.ERR_INVALID_FILTER) {
                setStatus(fmt("proghatches.pattern_generator.target.filter_invalid"));
            } else {
                String statusKey = PatternGeneratorRecipes.statusKey(result);
                setStatus(statusKey == null ? fmt("proghatches.pattern_generator.target.no_recipes") : fmt(statusKey));
            }
        });
        this.sync.syncValue(key, handler);
        return handler;
    }

    private InteractionSyncHandler resumeHandler() {
        InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(click -> {
            if (this.sync.isClient()) return;
            if (!(this.data.getPlayer() instanceof EntityPlayerMP player)) return;
            if (!PatternGeneratorFillQueue.resume(player, currentTool())) {
                setStatus(fmt("proghatches.pattern_generator.target.none"));
            }
            updateTargetState();
        });
        this.sync.syncValue("pattern_generator_resume", handler);
        return handler;
    }

    private InteractionSyncHandler cancelHandler() {
        InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(click -> {
            if (this.sync.isClient()) return;
            if (!(this.data.getPlayer() instanceof EntityPlayerMP player)) return;
            PatternGeneratorFillQueue.cancel(player, currentTool());
            updateTargetState();
        });
        this.sync.syncValue("pattern_generator_cancel", handler);
        return handler;
    }

    private void buildPresetsPage(ParentWidget<?> page) {
        page.child(
            IKey.lang("proghatches.pattern_generator.preset.name")
                .asWidget()
                .pos(5, 4));
        this.presetNameField = new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget()
            .value(this.presetNameValue)
            .setMaxLength(48)
            .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
            .setTextColor(0xFFFFFFFF)
            .pos(5, 14)
            .size(180, 18);
        page.child(this.presetNameField);
        page.child(
            new ButtonWidget<>().size(50, 18)
                .pos(189, 14)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.preset.save"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) savePresetClient();
                    return true;
                }));
        page.child(
            new ButtonWidget<>().size(42, 18)
                .pos(243, 14)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.preset.refresh"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) {
                        refreshPresets();
                        setPresetStatus("");
                    }
                    return true;
                }));

        for (int row = 0; row < PRESET_PAGE_SIZE; row++) {
            final int index = row;
            ParentWidget<?> rowBox = new ParentWidget<>().pos(0, 38 + row * 18)
                .size(WIDTH, 18)
                .setEnabledIf(widget -> presetRow(index) != null);
            rowBox.child(
                new ButtonWidget<>().size(150, 16)
                    .pos(4, 0)
                    .background(GTGuiTextures.BUTTON_STANDARD)
                    .overlay(IKey.dynamic(() -> presetRowLabel(index)))
                    .tooltipDynamic(tooltip -> {
                        PresetRow preset = presetRow(index);
                        if (preset != null) {
                            tooltip.addLine(preset.summary);
                            tooltip.addLine(
                                fmt("proghatches.pattern_generator.preset.author_line", preset.author));
                        }
                    })
                    .tooltipAutoUpdate(true)
                    .onMousePressed(mouseButton -> {
                        if (this.sync.isClient()) {
                            PresetRow preset = presetRow(index);
                            if (preset != null) loadPreset(preset.name);
                        }
                        return true;
                    }));
            rowBox.child(
                new ButtonWidget<>().size(18, 16)
                    .pos(158, 0)
                    .background(GTGuiTextures.BUTTON_STANDARD)
                    .overlay(IKey.str("R"))
                    .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.preset.rename.tooltip")))
                    .onMousePressed(mouseButton -> {
                        if (this.sync.isClient()) {
                            PresetRow preset = presetRow(index);
                            if (preset != null) renamePreset(preset.name);
                        }
                        return true;
                    }));
            rowBox.child(
                new ButtonWidget<>().size(18, 16)
                    .pos(178, 0)
                    .background(GTGuiTextures.BUTTON_STANDARD)
                    .overlay(IKey.str("X"))
                    .onMousePressed(mouseButton -> {
                        if (this.sync.isClient()) {
                            PresetRow preset = presetRow(index);
                            if (preset != null) deletePreset(preset.name);
                        }
                        return true;
                    }));
            rowBox.child(
                new TextWidget<>(IKey.dynamic(() -> presetRowSummary(index))).pos(200, 4)
                    .size(136, 10)
                    .scale(0.8f)
                    .maxWidth(136));
            page.child(rowBox);
        }

        page.child(
            new TextWidget<>(IKey.dynamic(this::presetEmptyHint)).pos(5, 76)
                .size(330, 12)
                .alignment(Alignment.Center));
        page.child(
            new ButtonWidget<>().size(16, 16)
                .pos(4, 148)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str("<"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient() && this.presetPage[0] > 0) {
                        this.presetPage[0]--;
                        refreshPresets();
                    }
                    return true;
                }));
        page.child(
            new TextWidget<>(IKey.dynamic(this::presetPageLabel)).pos(24, 151)
                .size(50, 10)
                .scale(0.8f)
                .alignment(Alignment.Center));
        page.child(
            new ButtonWidget<>().size(16, 16)
                .pos(78, 148)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str(">"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient() && this.presetPage[0] + 1 < Math.max(1, this.presetModel.pages)) {
                        this.presetPage[0]++;
                        refreshPresets();
                    }
                    return true;
                }));
        page.child(
            new TextWidget<>(IKey.dynamic(() -> this.presetStatus[0])).pos(5, 168)
                .size(330, 10)
                .color(0xFFAA0000)
                .scale(0.8f));
        page.child(
            new TextWidget<>(IKey.lang("proghatches.pattern_generator.preset.hint")).pos(5, 180)
                .size(330, 10)
                .scale(0.8f));
        if (this.sync.isClient()) refreshPresets();
    }

    private void savePresetClient() {
        String name = currentPresetName();
        if (name.isEmpty()) {
            setPresetStatus(fmt("proghatches.pattern_generator.preset.name_required"));
            return;
        }
        if (!PatternMachineResolver.isMachineItem(this.config.machine)) {
            setPresetStatus(fmt("proghatches.pattern_generator.preset.no_machine"));
            return;
        }
        // Selection arrives through the dedicated selection sync, so the client always has the
        // server-authoritative id set when saving.
        PatternGeneratorSettings settings = new PatternGeneratorSettings();
        settings.copyConfigFrom(this.config);

        PatternGeneratorPresets.Preset preset = PatternGeneratorPresets.Preset.fromSettings(
            name,
            settings,
            PatternMachineResolver.machineMode(this.config.machine));
        if (PatternGeneratorPresets.save(preset)) {
            publishPresetName(preset.name);
            setPresetStatus(fmt("proghatches.pattern_generator.preset.saved", preset.name));
            refreshPresets();
        } else {
            setPresetStatus(fmt("proghatches.pattern_generator.preset.save_failed", name));
        }
    }

    private String currentPresetName() {
        String name = this.presetNameField != null ? this.presetNameField.getText() : this.presetName[0];
        return name == null ? "" : name.trim();
    }

    private void loadPreset(String name) {
        if (!this.sync.isClient()) return;
        PatternGeneratorPresets.Preset preset = PatternGeneratorPresets.load(name);
        if (preset == null) {
            setPresetStatus(fmt("proghatches.pattern_generator.preset.missing", name));
            refreshPresets();
            return;
        }
        publishPresetName(preset.name);
        this.presetLoadData = preset.toTag();
        this.presetLoadValue.setValue(this.presetLoadData, true, true);
    }

    private void applyClientPreset(NBTTagCompound tag) {
        if (this.sync.isClient() || tag == null || tag.hasNoTags()) return;
        PatternGeneratorPresets.Preset preset = PatternGeneratorPresets.Preset.fromTag(tag);
        if (preset == null) {
            setStatus(fmt("proghatches.pattern_generator.preset.invalid", "?"));
            return;
        }
        if (!(this.data.getPlayer() instanceof EntityPlayerMP player)) return;
        PatternGeneratorSettings current = PatternGeneratorSettings.load(currentTool());
        if (PatternGeneratorFillQueue.isActive(player) || current.hasPending()) {
            setStatus(fmt("proghatches.pattern_generator.preset.pending"));
            return;
        }
        preset.applyTo(current);
        current.save(currentTool());
        this.config.copyConfigFrom(current);
        this.persist.run();
        if (this.machineHandler != null) {
            this.applyingPreset = true;
            try {
                this.machineHandler.setStackInSlot(
                    0,
                    current.machine == null ? null : current.machine.copy());
            } finally {
                this.applyingPreset = false;
            }
            if (this.machineSlot != null) {
                try {
                    this.machineSlot.getSyncHandler()
                        .forceSyncItem();
                } catch (Throwable ignored) {}
            }
        }
        publishConfigValues();
        markRecompute();
        setStatus(fmt("proghatches.pattern_generator.preset.loaded", preset.name));
    }

    private void renamePreset(String oldName) {
        String newName = currentPresetName();
        if (newName.isEmpty()) {
            setPresetStatus(fmt("proghatches.pattern_generator.preset.name_required"));
            return;
        }
        if (PatternGeneratorPresets.rename(oldName, newName)) {
            String safe = PatternGeneratorPresets.safeName(newName);
            publishPresetName(safe);
            setPresetStatus(fmt("proghatches.pattern_generator.preset.renamed", safe));
            refreshPresets();
        } else {
            setPresetStatus(fmt("proghatches.pattern_generator.preset.rename_failed", oldName, newName));
            refreshPresets();
        }
    }

    private void deletePreset(String name) {
        if (PatternGeneratorPresets.delete(name)) {
            setPresetStatus(fmt("proghatches.pattern_generator.preset.deleted", name));
        } else {
            setPresetStatus(fmt("proghatches.pattern_generator.preset.missing", name));
        }
        refreshPresets();
    }

    private void refreshPresets() {
        if (!this.sync.isClient()) return;
        publishPresets();
    }

    private void publishPresets() {
        java.util.List<PatternGeneratorPresets.Preset> presets = PatternGeneratorPresets.list();
        int pages = Math.max(1, (presets.size() + PRESET_PAGE_SIZE - 1) / PRESET_PAGE_SIZE);
        if (this.presetPage[0] >= pages) this.presetPage[0] = pages - 1;
        if (this.presetPage[0] < 0) this.presetPage[0] = 0;
        PresetPage model = new PresetPage();
        model.page = this.presetPage[0];
        model.pages = pages;
        model.total = presets.size();
        int start = model.page * PRESET_PAGE_SIZE;
        int end = Math.min(start + PRESET_PAGE_SIZE, presets.size());
        for (int i = start; i < end; i++) {
            PatternGeneratorPresets.Preset preset = presets.get(i);
            PresetRow row = new PresetRow();
            row.name = preset.name;
            row.machine = preset.settings.machine == null ? ""
                : PatternIngredient.displayName(preset.settings.machine);
            row.map = PatternMachineResolver.mapDisplayName(preset.settings.recipeMapId);
            row.circuit = PatternGeneratorRecipes.circuitLabel(preset.settings.circuit);
            row.onlyConflict = preset.settings.onlyConflict;
            row.firstOutputOnly = preset.settings.firstOutputOnly;
            row.author = preset.settings.author == null || preset.settings.author.trim()
                .isEmpty() ? fmt("proghatches.pattern_generator.preset.player_author") : preset.settings.author;
            row.recipes = preset.settings.selectedRecipes.size();
            row.machineMode = preset.machineMode;
            row.updatedAt = preset.updatedAt;
            row.summary = fmt(
                "proghatches.pattern_generator.preset.summary",
                row.machine,
                row.map,
                row.circuit);
            model.rows.add(row);
        }
        this.presetModel = model;
        this.presetData = model.toNbt();
    }

    private void publishPresetName(String name) {
        String safe = name == null ? "" : name;
        this.presetName[0] = safe;
        if (this.presetNameField != null) this.presetNameField.setText(safe);
    }

    private void setPresetStatus(String text) {
        this.presetStatus[0] = text == null ? "" : text;
    }

    private PresetRow presetRow(int index) {
        if (index < 0 || index >= this.presetModel.rows.size()) return null;
        return this.presetModel.rows.get(index);
    }

    private String presetRowLabel(int index) {
        PresetRow preset = presetRow(index);
        return preset == null ? "" : shorten(preset.name, 28);
    }

    private String presetRowSummary(int index) {
        PresetRow preset = presetRow(index);
        if (preset == null) return "";
        return shorten(preset.summary, 26);
    }

    private String presetEmptyHint() {
        if (!this.sync.isClient()) return "";
        return this.presetModel.total == 0 ? fmt("proghatches.pattern_generator.preset.empty") : "";
    }

    private String presetPageLabel() {
        return (this.presetModel.page + 1) + "/" + Math.max(1, this.presetModel.pages);
    }

    private String targetHint() {
        if (this.armedValue.getBoolValue()) return fmt("proghatches.pattern_generator.target.armed_hint");
        if (this.pendingRemaining[0] > 0) return fmt("proghatches.pattern_generator.target.pending_hint");
        return fmt("proghatches.pattern_generator.target.not_armed");
    }

    private void updateTargetState() {
        if (this.sync.isClient()) return;
        PatternGeneratorSettings current = PatternGeneratorSettings.load(currentTool());
        int remaining = current.pendingRecipes.size();
        this.pendingRemaining[0] = remaining;
        this.pendingTotal[0] = current.pendingTotal;
        this.armed[0] = current.fillArmed;
        this.targetInfo[0] = targetLabel(current);
        this.pendingValue.setIntValue(remaining, true, true);
        this.pendingTotalValue.setIntValue(current.pendingTotal, true, true);
        this.armedValue.setBoolValue(current.fillArmed, true, true);
        this.targetValue.setStringValue(this.targetInfo[0], true, true);
    }

    private String targetLabel(PatternGeneratorSettings settings) {
        if (settings == null || !settings.hasPendingTarget()) {
            return fmt("proghatches.pattern_generator.target.no_target");
        }
        return fmt(
            "proghatches.pattern_generator.target.coords",
            settings.pendingX,
            settings.pendingY,
            settings.pendingZ,
            settings.pendingDim);
    }

    private ItemStack currentTool() {
        if (this.data.getPlayer() instanceof EntityPlayerMP player) {
            ItemStack held = player.getHeldItem();
            if (held != null && held.getItem() == MyMod.pattern_generator) return held;
        }
        return this.tool;
    }

    private void publishConfigValues() {
        publishMapValue(this.config.recipeMapId);
        this.circuitValue.setIntValue(this.config.circuit, true, true);
        this.minVoltageValue.setIntValue(this.config.minVoltage, true, true);
        this.maxVoltageValue.setIntValue(this.config.maxVoltage, true, true);
        this.conflictValue.setBoolValue(this.config.onlyConflict, true, true);
        this.byproductValue.setBoolValue(this.config.firstOutputOnly, true, true);
        this.authorValue.setStringValue(this.config.author, true, true);
        for (FilterSection section : this.filterSections) section.syncFromConfig();
    }

    private void publishMapValue(String id) {
        this.publishingMap = true;
        try {
            this.mapValue.setStringValue(id == null ? "" : id, true, true);
        } finally {
            this.publishingMap = false;
        }
    }

    private void buildMainPage(ParentWidget<?> page) {
        page.child(
            IKey.lang("proghatches.pattern_generator.machine")
                .asWidget()
                .pos(5, 4));

        this.machineHandler = new ItemStackHandler(new ItemStack[] { this.config.machine }) {

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                super.setStackInSlot(slot, stack);
                PatternGeneratorPanel.this.clearMapPicker();
                if (PatternGeneratorPanel.this.applyingPreset) return;
                boolean server = PatternGeneratorPanel.this.data.getPlayer() instanceof EntityPlayerMP;
                if (!server) {
                    PatternGeneratorPanel.this.config.machine = stack == null ? null : stack.copy();
                    if (PatternGeneratorPanel.this.config.machine != null) {
                        PatternGeneratorPanel.this.config.machine.stackSize = 1;
                    }
                    return;
                }
                PatternGeneratorPanel.this.config.setMachine(stack);
                PatternGeneratorPanel.this.persist.run();
                PatternGeneratorPanel.this.publishMapValue(PatternGeneratorPanel.this.config.recipeMapId);
                PatternGeneratorPanel.this.setStatus("");
                PatternGeneratorPanel.this.machineErrorActive = false;
                PatternGeneratorPanel.this.markRecompute();
            }
        };

        this.machineSlot = new PhantomItemSlot() {

            @Override
            public void onInit() {
                super.onInit();
                try {
                    if (getSyncHandler() != null && getSyncHandler()
                        .getSyncManager() != null
                        && !getSyncHandler().getSyncManager()
                            .isClient()) {
                        // Change detection does not send an unchanged initial slot.
                        getSyncHandler().forceSyncItem();
                    }
                } catch (Throwable ignored) {}
            }

            @Override
            protected void drawSlotAmountText(int amount, String text) {
                // Ghost slot; the count badge is noise.
            }
        }.slot(new ModularSlot(this.machineHandler, 0).filter(PatternMachineResolver::isMachineItem))
            .background(GTGuiTextures.SLOT_ITEM_STANDARD)
            .pos(5, 16)
            .size(18, 18)
            .tooltipBuilder(
                t -> t.addLine(IKey.lang("proghatches.pattern_generator.machine.tooltip"))
                    .addLine(IKey.lang("proghatches.pattern_generator.machine.tooltip.nei")));
        page.child(this.machineSlot);

        page.child(
            IKey.lang("proghatches.pattern_generator.map")
                .asWidget()
                .pos(29, 4));

        page.child(
            new ButtonWidget<>().size(190, 18)
                .pos(29, 16)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.dynamic(() -> mapLabel(this.mapValue.getStringValue())))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.map.tooltip")))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) showMapPicker();
                    return true;
                }));

        page.child(
            IKey.lang("proghatches.pattern_generator.circuit")
                .asWidget()
                .pos(5, 40));
        page.child(
            new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget().value(this.circuitValue)
                .formatAsInteger(true)
                .numbersInt(-1, 32)
                .setMaxLength(3)
                .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
                .setTextColor(0xFFFFFFFF)
                .pos(5, 52)
                .size(60, 18));
        page.child(
            IKey.lang("proghatches.pattern_generator.circuit.hint")
                .asWidget()
                .pos(69, 57));

        page.child(
            new TextWidget<>(IKey.dynamic(() -> "§8" + machineSummary())).pos(5, 78)
                .size(330, 10));
        page.child(
            new TextWidget<>(IKey.dynamic(() -> "§8" + mapSummary())).pos(5, 90)
                .size(330, 10));
        page.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> "§8" + fmt(
                        "proghatches.pattern_generator.summary.circuit",
                        PatternGeneratorRecipes.circuitLabel(this.config.circuit)))).pos(5, 102)
                .size(330, 10));

        page.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> fmt("proghatches.pattern_generator.count.matches", this.matchValue.getIntValue())))
                        .pos(5, 122)
                        .size(150, 10));
        page.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> fmt("proghatches.pattern_generator.count.selected", this.selectedValue.getIntValue())))
                        .pos(160, 122)
                        .size(150, 10));

        InteractionSyncHandler previewHandler = new InteractionSyncHandler().setOnMousePressed(click -> {
            if (!this.sync.isClient()) return;
            this.pageValue.setIntValue(0, true, true);
            this.tabs.setPage(2);
        });
        this.sync.syncValue("pattern_generator_open_preview", previewHandler);
        page.child(
            new ButtonWidget<>().syncHandler(previewHandler)
                .size(70, 18)
                .pos(5, 146)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.preview")));

        page.child(
            generateButton("pattern_generator_generate_selected_main", true, 79, 146, 100));

        page.child(
            generateButton("pattern_generator_generate_all_main", false, 183, 146, 110));

        InteractionSyncHandler resetHandler = new InteractionSyncHandler().setOnMousePressed(click -> {
            if (this.sync.isClient()) return;
            resetAll();
        });
        this.sync.syncValue("pattern_generator_reset_all", resetHandler);
        page.child(
            new ButtonWidget<>().syncHandler(resetHandler)
                .size(80, 18)
                .pos(5, 170)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.reset"))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.reset.tooltip"))));

        // Keep the popup last: its child backdrop must draw after every Main-page sibling.
        buildMapPicker(page);
    }

    private void buildFiltersPage(ParentWidget<?> page) {
        FilterSection inputInclude = addSection(
            page,
            "input_include",
            "proghatches.pattern_generator.filter.input_include",
            () -> this.config.inputInclude,
            value -> this.config.inputInclude = value,
            () -> this.config.inputIncludeMode,
            mode -> this.config.inputIncludeMode = mode,
            4,
            2);
        FilterSection inputExclude = addSection(
            page,
            "input_exclude",
            "proghatches.pattern_generator.filter.input_exclude",
            () -> this.config.inputExclude,
            value -> this.config.inputExclude = value,
            () -> this.config.inputExcludeMode,
            mode -> this.config.inputExcludeMode = mode,
            4,
            80);
        FilterSection outputInclude = addSection(
            page,
            "output_include",
            "proghatches.pattern_generator.filter.output_include",
            () -> this.config.outputInclude,
            value -> this.config.outputInclude = value,
            () -> this.config.outputIncludeMode,
            mode -> this.config.outputIncludeMode = mode,
            172,
            2);
        FilterSection outputExclude = addSection(
            page,
            "output_exclude",
            "proghatches.pattern_generator.filter.output_exclude",
            () -> this.config.outputExclude,
            value -> this.config.outputExclude = value,
            () -> this.config.outputExcludeMode,
            mode -> this.config.outputExcludeMode = mode,
            172,
            80);

        this.filterSections.add(inputInclude);
        this.filterSections.add(inputExclude);
        this.filterSections.add(outputInclude);
        this.filterSections.add(outputExclude);

        page.child(
            new ButtonWidget<>().size(70, 14)
                .pos(4, 160)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.dynamic(() -> voltageButtonLabel(false)))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.filter.voltage.tooltip")))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) showVoltagePicker(false);
                    return true;
                }));
        page.child(
            new ButtonWidget<>().size(70, 14)
                .pos(78, 160)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.dynamic(() -> voltageButtonLabel(true)))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.filter.voltage.tooltip")))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) showVoltagePicker(true);
                    return true;
                }));
        page.child(
            new TextWidget<>(IKey.dynamic(() -> fmt("proghatches.pattern_generator.filter.rules.total", totalRules())))
                .pos(4, 176)
                .size(160, 10));
        page.child(
            new TextWidget<>(IKey.dynamic(() -> this.filterStatus[0])).pos(4, 186)
                .size(332, 10)
                .color(0xFFFF5555));

        buildOrePicker(page);
        buildVoltagePicker(page);
    }

    private FilterSection addSection(ParentWidget<?> page, String id, String labelKey, java.util.function.Supplier<String> textGetter,
        java.util.function.Consumer<String> textSetter, java.util.function.Supplier<FilterMode> modeGetter,
        java.util.function.Consumer<FilterMode> modeSetter, int x, int y) {
        FilterSection section = new FilterSection(
            id,
            labelKey,
            textGetter,
            textSetter,
            modeGetter,
            modeSetter);
        this.sync.syncValue("pattern_generator_filter_" + id + "_text", section.textValue);
        this.sync.syncValue("pattern_generator_filter_" + id + "_mode", section.modeValue);

        page.child(
            IKey.lang(labelKey)
                .asWidget()
                .pos(x, y));
        page.child(
            new ButtonWidget<>().size(50, 14)
                .pos(x + 82, y - 1)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.dynamic(section::modeLabel))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) {
                        section.modeValue.setBoolValue(!section.modeValue.getBoolValue(), true, true);
                    }
                    return true;
                })
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.filter.mode.tooltip"))));
        page.child(
            new ButtonWidget<>().size(26, 14)
                .pos(x + 134, y - 1)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.filter.clear"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) {
                        section.textValue.setStringValue("", true, true);
                        section.field.pull();
                    }
                    return true;
                }));

        section.field = new SyncedTextEditor(section.textValue);
        section.field.size(160, 40)
            .pos(x, y + 15)
            .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
            .setTextColor(0xFFFFFFFF);
        section.field.tooltip(
            t -> t.addLine(IKey.lang("proghatches.pattern_generator.filter.drag.tooltip"))
                .addLine(IKey.lang("proghatches.pattern_generator.filter.rules.tooltip")));
        page.child(section.field);
        page.child(
            new FilterDragTarget(section).pos(x, y + 15)
                .size(160, 40));

        page.child(
            new TextWidget<>(IKey.dynamic(section::rulesLabel)).pos(x, y + 57)
                .size(160, 10));
        page.child(
            new TextWidget<>(IKey.dynamic(section::errorLabel)).pos(x, y + 68)
                .size(160, 10)
                .color(0xFFFF5555));
        return section;
    }

    private void buildMapPicker(ParentWidget<?> page) {
        ParentWidget<?> picker = new ParentWidget<>().pos(52, 30)
            .size(236, 146)
            .setEnabledIf(widget -> !this.pendingMaps.isEmpty());
        picker.child(
            new Widget<>().pos(0, 0)
                .size(236, 146)
                .background(new Rectangle().color(0xFFC6C6C6), GTGuiTextures.BACKGROUND_POPUP_STANDARD));
        picker.child(
            new TextWidget<>(IKey.lang("proghatches.pattern_generator.map.picker.title")).pos(4, 3)
                .size(204, 10));
        picker.child(
            new ButtonWidget<>().size(20, 14)
                .pos(212, 2)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str("X"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) clearMapPicker();
                    return true;
                }));
        for (int i = 0; i < ORE_PAGE_SIZE; i++) {
            final int row = i;
            picker.child(
                new ButtonWidget<>().size(228, 16)
                    .pos(4, 18 + i * 18)
                    .background(GTGuiTextures.BUTTON_STANDARD)
                    .overlay(IKey.dynamic(() -> mapChoiceLabel(row)))
                    .onMousePressed(mouseButton -> {
                        if (this.sync.isClient()) selectMap(row);
                        return true;
                    })
                    .setEnabledIf(widget -> !mapChoiceId(row).isEmpty()));
        }
        picker.child(
            new ButtonWidget<>().size(16, 16)
                .pos(78, 128)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str("<"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient() && this.mapPage > 0) this.mapPage--;
                    return true;
                })
                .setEnabledIf(widget -> this.mapPage > 0));
        picker.child(
            new TextWidget<>(IKey.dynamic(this::mapPageLabel)).pos(98, 131)
                .size(40, 10)
                .scale(0.8f)
                .alignment(Alignment.Center));
        picker.child(
            new ButtonWidget<>().size(16, 16)
                .pos(142, 128)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str(">"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient() && this.mapPage + 1 < mapPageCount()) this.mapPage++;
                    return true;
                })
                .setEnabledIf(widget -> this.mapPage + 1 < mapPageCount()));
        page.child(picker);
    }

    private void showMapPicker() {
        this.pendingMaps.clear();
        this.pendingMapNames.clear();
        this.mapPage = 0;
        List<RecipeMap<?>> maps = PatternMachineResolver.resolveMaps(this.config.machine);
        for (RecipeMap<?> map : maps) {
            if (map == null || map.unlocalizedName == null || map.unlocalizedName.isEmpty()) continue;
            this.pendingMaps.add(map.unlocalizedName);
            this.pendingMapNames.add(PatternMachineResolver.mapDisplayName(map.unlocalizedName));
        }
        if (this.pendingMaps.isEmpty()) {
            String key = PatternMachineResolver.isMachineItem(this.config.machine)
                ? "proghatches.pattern_generator.status.no_map"
                : "proghatches.pattern_generator.status.no_machine";
            setStatus(fmt(key));
        }
    }

    private void clearMapPicker() {
        this.pendingMaps.clear();
        this.pendingMapNames.clear();
        this.mapPage = 0;
    }

    private String mapChoiceId(int row) {
        int index = this.mapPage * ORE_PAGE_SIZE + row;
        return index < 0 || index >= this.pendingMaps.size() ? "" : this.pendingMaps.get(index);
    }

    private String mapChoiceLabel(int row) {
        int index = this.mapPage * ORE_PAGE_SIZE + row;
        if (index < 0 || index >= this.pendingMapNames.size()) return "";
        return shorten(this.pendingMapNames.get(index), 36);
    }

    private void selectMap(int row) {
        String id = mapChoiceId(row);
        if (id.isEmpty()) return;
        this.mapValue.setStringValue(id, true, true);
        clearMapPicker();
    }

    private int mapPageCount() {
        return Math.max(1, (this.pendingMaps.size() + ORE_PAGE_SIZE - 1) / ORE_PAGE_SIZE);
    }

    private String mapPageLabel() {
        return (this.mapPage + 1) + "/" + mapPageCount();
    }

    private void buildVoltagePicker(ParentWidget<?> page) {
        ParentWidget<?> picker = new ParentWidget<>().pos(52, 36)
            .size(236, 146)
            .setEnabledIf(widget -> !this.pendingVoltages.isEmpty());
        picker.child(
            new Widget<>().pos(0, 0)
                .size(236, 146)
                .background(new Rectangle().color(0xFFC6C6C6), GTGuiTextures.BACKGROUND_POPUP_STANDARD));
        picker.child(
            new TextWidget<>(IKey.dynamic(this::voltagePickerTitle)).pos(4, 3)
                .size(204, 10));
        picker.child(
            new ButtonWidget<>().size(20, 14)
                .pos(212, 2)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str("X"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) clearVoltagePicker();
                    return true;
                }));
        for (int i = 0; i < ORE_PAGE_SIZE; i++) {
            final int row = i;
            picker.child(
                new ButtonWidget<>().size(228, 16)
                    .pos(4, 18 + i * 18)
                    .background(GTGuiTextures.BUTTON_STANDARD)
                    .overlay(IKey.dynamic(() -> voltageChoiceLabel(row)))
                    .onMousePressed(mouseButton -> {
                        if (this.sync.isClient()) selectVoltage(row);
                        return true;
                    })
                    .setEnabledIf(widget -> voltageChoiceValue(row) != Integer.MIN_VALUE));
        }
        picker.child(
            new ButtonWidget<>().size(16, 16)
                .pos(78, 128)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str("<"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient() && this.voltagePage > 0) this.voltagePage--;
                    return true;
                })
                .setEnabledIf(widget -> this.voltagePage > 0));
        picker.child(
            new TextWidget<>(IKey.dynamic(this::voltagePageLabel)).pos(98, 131)
                .size(40, 10)
                .scale(0.8f)
                .alignment(Alignment.Center));
        picker.child(
            new ButtonWidget<>().size(16, 16)
                .pos(142, 128)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str(">"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient() && this.voltagePage + 1 < voltagePageCount()) this.voltagePage++;
                    return true;
                })
                .setEnabledIf(widget -> this.voltagePage + 1 < voltagePageCount()));
        page.child(picker);
    }

    private void showVoltagePicker(boolean max) {
        clearOrePicker();
        this.pendingVoltages.clear();
        this.voltagePickerMax = max;
        this.voltagePage = 0;
        // -1 is "Any" (no bound); 0 is the explicit "None (0 EU/t)" entry below ULV.
        this.pendingVoltages.add(-1);
        this.pendingVoltages.add(0);
        java.util.LinkedHashSet<Integer> seen = new java.util.LinkedHashSet<>();
        seen.add(0);
        for (long value : GTValues.V) {
            int clamped = (int) Math.min(Integer.MAX_VALUE, Math.abs(value));
            if (clamped <= 0 || !seen.add(clamped)) continue;
            this.pendingVoltages.add(clamped);
        }
    }

    private void clearVoltagePicker() {
        this.pendingVoltages.clear();
        this.voltagePage = 0;
    }

    private String voltagePickerTitle() {
        return fmt(
            this.voltagePickerMax ? "proghatches.pattern_generator.filter.voltage.title.max"
                : "proghatches.pattern_generator.filter.voltage.title.min");
    }

    private int voltageChoiceValue(int row) {
        int index = this.voltagePage * ORE_PAGE_SIZE + row;
        return index < 0 || index >= this.pendingVoltages.size() ? Integer.MIN_VALUE : this.pendingVoltages.get(index);
    }

    private String voltageChoiceLabel(int row) {
        int value = voltageChoiceValue(row);
        if (value == Integer.MIN_VALUE) return "";
        return voltageText(value, true);
    }

    private void selectVoltage(int row) {
        int value = voltageChoiceValue(row);
        if (value == Integer.MIN_VALUE) return;
        if (this.voltagePickerMax) {
            this.maxVoltageValue.setIntValue(value, true, true);
        } else {
            this.minVoltageValue.setIntValue(value, true, true);
        }
        clearVoltagePicker();
    }

    private int voltagePageCount() {
        return Math.max(1, (this.pendingVoltages.size() + ORE_PAGE_SIZE - 1) / ORE_PAGE_SIZE);
    }

    private String voltagePageLabel() {
        return (this.voltagePage + 1) + "/" + voltagePageCount();
    }

    private String voltageButtonLabel(boolean max) {
        int value = max ? this.config.maxVoltage : this.config.minVoltage;
        return fmt(
            max ? "proghatches.pattern_generator.filter.voltage.max"
                : "proghatches.pattern_generator.filter.voltage.min",
            voltageText(value, false));
    }

    private static String voltageText(int value, boolean withUnit) {
        if (value < 0) return fmt("proghatches.pattern_generator.filter.voltage.any");
        String number = groupedNumber(value);
        if (value == 0) {
            return withUnit ? fmt("proghatches.pattern_generator.filter.voltage.none", number)
                : fmt("proghatches.pattern_generator.filter.voltage.none_short");
        }
        String tier = voltageTierName(value);
        if (tier.isEmpty()) {
            return withUnit ? fmt("proghatches.pattern_generator.filter.voltage.value_number", number) : number;
        }
        return withUnit ? fmt("proghatches.pattern_generator.filter.voltage.value", tier, number) : tier;
    }

    private static String groupedNumber(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private static String voltageTierName(int value) {
        for (int i = 0; i < GTValues.V.length; i++) {
            int tier = (int) Math.min(Integer.MAX_VALUE, Math.abs(GTValues.V[i]));
            if (tier == value && i < GTValues.VN.length) return GTValues.VN[i];
        }
        return "";
    }

    private void buildOrePicker(ParentWidget<?> page) {
        ParentWidget<?> picker = new ParentWidget<>().pos(52, 36)
            .size(236, 146)
            .setEnabledIf(widget -> !this.pendingOres.isEmpty());
        // Sibling backgrounds are drawn before sibling content, so the fill must be a child
        // backdrop to cover the filter fields drawn before this popup.
        picker.child(
            new Widget<>().pos(0, 0)
                .size(236, 146)
                .background(new Rectangle().color(0xFFC6C6C6), GTGuiTextures.BACKGROUND_POPUP_STANDARD));
        picker.child(
            new TextWidget<>(IKey.lang("proghatches.pattern_generator.filter.choose_oredict")).pos(4, 3)
                .size(204, 10));
        picker.child(
            new ButtonWidget<>().size(20, 14)
                .pos(212, 2)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str("X"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) clearOrePicker();
                    return true;
                }));
        for (int i = 0; i < ORE_PAGE_SIZE; i++) {
            final int row = i;
            picker.child(
                new ButtonWidget<>().size(228, 16)
                    .pos(4, 18 + i * 18)
                    .background(GTGuiTextures.BUTTON_STANDARD)
                    .overlay(IKey.dynamic(() -> oreChoiceLabel(row)))
                    .tooltipDynamic(tooltip -> {
                        String full = oreChoice(row);
                        if (!full.isEmpty()) tooltip.addLine(full);
                    })
                    .tooltipAutoUpdate(true)
                    .onMousePressed(mouseButton -> {
                        if (this.sync.isClient()) chooseOre(row);
                        return true;
                    })
                    .setEnabledIf(widget -> !oreChoice(row).isEmpty()));
        }
        picker.child(
            new ButtonWidget<>().size(16, 16)
                .pos(78, 128)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str("<"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient() && this.orePage > 0) this.orePage--;
                    return true;
                })
                .setEnabledIf(widget -> this.orePage > 0));
        picker.child(
            new TextWidget<>(IKey.dynamic(this::orePageLabel)).pos(98, 131)
                .size(40, 10)
                .scale(0.8f)
                .alignment(Alignment.Center));
        picker.child(
            new ButtonWidget<>().size(16, 16)
                .pos(142, 128)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str(">"))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient() && this.orePage + 1 < orePageCount()) this.orePage++;
                    return true;
                })
                .setEnabledIf(widget -> this.orePage + 1 < orePageCount()));
        page.child(picker);
    }

    private void buildPreviewPage(ParentWidget<?> page) {
        page.child(
            new TextWidget<>(IKey.dynamic(this::previewMapHeader)).pos(4, 4)
                .size(140, 10));
        page.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> fmt(
                        "proghatches.pattern_generator.preview.page_summary",
                        this.matchValue.getIntValue(),
                        this.selectedValue.getIntValue()))).pos(148, 4)
                .size(94, 10)
                .alignment(Alignment.CenterRight));
        page.child(
            new ButtonWidget<>().size(16, 16)
                .pos(246, 1)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str("<"))
                .syncHandler(pageButton("prev", -1)));
        page.child(
            new TextWidget<>(IKey.dynamic(this::pageLabel)).pos(264, 4)
                .size(48, 10)
                .scale(0.8f)
                .alignment(Alignment.Center));
        page.child(
            new ButtonWidget<>().size(16, 16)
                .pos(314, 1)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.str(">"))
                .syncHandler(pageButton("next", 1)));

        page.child(
            new ButtonWidget<>().size(92, 14)
                .pos(4, 17)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(
                    IKey.dynamic(
                        () -> (this.config.onlyConflict ? "§a" : "§8")
                            + fmt("proghatches.pattern_generator.preview.only_conflict")))
                .tooltip(
                    t -> t.addLine(IKey.lang("proghatches.pattern_generator.preview.only_conflict.tooltip")))
                .onMousePressed(mouseButton -> {
                    if (this.sync.isClient()) {
                        this.conflictValue.setBoolValue(!this.conflictValue.getBoolValue(), true, true);
                    }
                    return true;
                }));
        page.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> this.config.firstOutputOnly
                        ? "§c" + fmt("proghatches.pattern_generator.preview.byproducts")
                        : "")).pos(102, 20)
                            .size(110, 10)
                            .scale(0.8f));

        for (int row = 0; row < PAGE_SIZE; row++) {
            final int index = row;
            int y = 32 + row * 19;
            ParentWidget<?> rowBox = new ParentWidget<>().pos(0, y)
                .size(WIDTH, 19)
                .setEnabledIf(widget -> row(index) != null);
            InteractionSyncHandler rowHandler = new InteractionSyncHandler().setOnMousePressed(click -> {
                if (this.sync.isClient()) return;
                toggleRow(index);
            });
            this.sync.syncValue("pattern_generator_row_" + row, rowHandler);
            rowBox.child(
                new ButtonWidget<>().syncHandler(rowHandler)
                    .size(16, 16)
                    .pos(2, 1)
                    .background(GTGuiTextures.BUTTON_STANDARD)
                    .overlay(IKey.dynamic(() -> selectionLabel(index))));

            for (int slot = 0; slot < MAX_INPUT_ICONS; slot++) {
                final int icon = slot;
                int x = 20 + slot * 18;
                PreviewItemIcon itemIcon = new PreviewItemIcon(index, false, icon);
                itemIcon.item(rowItem(index, false, icon));
                itemIcon.displayAmount(false);
                itemIcon.pos(x, 0);
                itemIcon.size(18, 18);
                itemIcon.setEnabledIf(
                    widget -> {
                        PatternIngredient ingredient = ingredient(index, false, icon);
                        return ingredient != null && !ingredient.isFluid();
                    });
                itemIcon.tooltipDynamic(tooltip -> addIngredientTooltip(tooltip, index, false, icon));
                itemIcon.tooltipAutoUpdate(true);
                rowBox.child(itemIcon);

                PreviewFluidIcon fluidIcon = new PreviewFluidIcon(index, false);
                fluidIcon.value(rowFluid(index, false, icon));
                fluidIcon.displayAmount(false);
                fluidIcon.pos(x, 0);
                fluidIcon.size(18, 18);
                fluidIcon.setEnabledIf(
                    widget -> {
                        PatternIngredient ingredient = ingredient(index, false, icon);
                        return ingredient != null && ingredient.isFluid();
                    });
                fluidIcon.tooltipDynamic(tooltip -> addIngredientTooltip(tooltip, index, false, icon));
                fluidIcon.tooltipAutoUpdate(true);
                rowBox.child(fluidIcon);
                rowBox.child(amountOverlay(index, false, icon).pos(x, 0)
                    .size(18, 18));
            }
            rowBox.child(
                new TextWidget<>(IKey.dynamic(() -> plusLabel(index, false))).pos(128, 4)
                    .size(16, 10));
            rowBox.child(
                new TextWidget<>(IKey.str("->")).pos(146, 4)
                    .size(10, 10));
            for (int slot = 0; slot < MAX_OUTPUT_ICONS; slot++) {
                final int icon = slot;
                int x = 158 + slot * 18;
                PreviewItemIcon itemIcon = new PreviewItemIcon(index, true, icon);
                itemIcon.item(rowItem(index, true, icon));
                itemIcon.displayAmount(false);
                itemIcon.pos(x, 0);
                itemIcon.size(18, 18);
                itemIcon.setEnabledIf(
                    widget -> {
                        PatternIngredient ingredient = ingredient(index, true, icon);
                        return ingredient != null && !ingredient.isFluid();
                    });
                itemIcon.tooltipDynamic(tooltip -> addIngredientTooltip(tooltip, index, true, icon));
                itemIcon.tooltipAutoUpdate(true);
                rowBox.child(itemIcon);

                PreviewFluidIcon fluidIcon = new PreviewFluidIcon(index, true);
                fluidIcon.value(rowFluid(index, true, icon));
                fluidIcon.displayAmount(false);
                fluidIcon.pos(x, 0);
                fluidIcon.size(18, 18);
                fluidIcon.setEnabledIf(
                    widget -> {
                        PatternIngredient ingredient = ingredient(index, true, icon);
                        return ingredient != null && ingredient.isFluid();
                    });
                fluidIcon.tooltipDynamic(tooltip -> addIngredientTooltip(tooltip, index, true, icon));
                fluidIcon.tooltipAutoUpdate(true);
                rowBox.child(fluidIcon);
                rowBox.child(amountOverlay(index, true, icon).pos(x, 0)
                    .size(18, 18));
            }
            rowBox.child(
                new TextWidget<>(IKey.dynamic(() -> plusLabel(index, true))).pos(194, 4)
                    .size(16, 10));
            rowBox.child(
                new TextWidget<>(IKey.dynamic(() -> rowCircuit(index))).pos(212, 4)
                    .size(120, 10)
                    .scale(0.8f)
                    .maxWidth(120)
                    .tooltipDynamic(tooltip -> {
                        String full = rowCircuitTooltip(index);
                        if (!full.isEmpty()) tooltip.addLine(full);
                    })
                    .tooltipAutoUpdate(true));
            page.child(rowBox);
        }

        page.child(
            new TextWidget<>(IKey.dynamic(this::emptyHint)).pos(5, 80)
                .size(330, 12)
                .alignment(Alignment.Center));
        page.child(
            new ButtonWidget<>().size(50, 18)
                .pos(4, 174)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.preview.select_all"))
                .syncHandler(selectionAction("pattern_generator_select_all", true)));
        page.child(
            new ButtonWidget<>().size(46, 18)
                .pos(58, 174)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.preview.select_none"))
                .syncHandler(selectionAction("pattern_generator_select_none", false)));
        page.child(
            new ButtonWidget<>().size(100, 18)
                .pos(108, 174)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.preview.generate_selected"))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.preview.generate.tooltip")))
                .syncHandler(generateHandler("pattern_generator_generate_selected_preview", true)));
        page.child(
            new ButtonWidget<>().size(120, 18)
                .pos(212, 174)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(IKey.lang("proghatches.pattern_generator.preview.generate_all"))
                .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.preview.generate.tooltip")))
                .syncHandler(generateHandler("pattern_generator_generate_all_preview", false)));
    }

    private ButtonWidget<?> generateButton(String key, boolean selectedOnly, int x, int y, int width) {
        return new ButtonWidget<>().size(width, 18)
            .pos(x, y)
            .background(GTGuiTextures.BUTTON_STANDARD)
            .overlay(
                IKey.lang(
                    selectedOnly ? "proghatches.pattern_generator.preview.generate_selected"
                        : "proghatches.pattern_generator.preview.generate_all"))
            .tooltip(t -> t.addLine(IKey.lang("proghatches.pattern_generator.preview.generate.tooltip")))
            .syncHandler(generateHandler(key, selectedOnly));
    }

    private InteractionSyncHandler generateHandler(String key, boolean selectedOnly) {
        InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(click -> {
            if (this.sync.isClient()) return;
            if (!(this.data.getPlayer() instanceof EntityPlayerMP player)) return;
            PatternGeneratorRecipes.GenerationResult result = PatternGeneratorRecipes
                .generate(player, this.tool, this.config, selectedOnly);
            applyGenerationStatus(result, selectedOnly);
            recomputeNow(false);
        });
        this.sync.syncValue(key, handler);
        return handler;
    }

    private void resetAll() {
        if (this.sync.isClient()) return;
        if (!(this.data.getPlayer() instanceof EntityPlayerMP player)) return;
        ItemStack tool = currentTool();
        PatternGeneratorFillQueue.cancel(player, tool);
        PatternGeneratorSettings current = PatternGeneratorSettings.load(tool);
        current.machine = null;
        current.recipeMapId = "";
        current.circuit = -1;
        current.inputInclude = "";
        current.inputExclude = "";
        current.outputInclude = "";
        current.outputExclude = "";
        current.inputIncludeMode = PatternGeneratorSettings.FilterMode.NAME;
        current.inputExcludeMode = PatternGeneratorSettings.FilterMode.NAME;
        current.outputIncludeMode = PatternGeneratorSettings.FilterMode.NAME;
        current.outputExcludeMode = PatternGeneratorSettings.FilterMode.NAME;
        current.onlyConflict = false;
        current.firstOutputOnly = false;
        current.minVoltage = -1;
        current.maxVoltage = -1;
        current.author = "";
        current.selectedRecipes.clear();
        current.clearPending();
        current.save(tool);
        this.config.copyConfigFrom(current);
        this.config.clearPending();
        this.persist.run();
        if (this.machineHandler != null) {
            this.applyingPreset = true;
            try {
                this.machineHandler.setStackInSlot(0, null);
            } finally {
                this.applyingPreset = false;
            }
            if (this.machineSlot != null) {
                try {
                    this.machineSlot.getSyncHandler()
                        .forceSyncItem();
                } catch (Throwable ignored) {}
            }
        }
        clearMapPicker();
        publishConfigValues();
        publishSelection();
        updateTargetState();
        this.page[0] = 0;
        recomputeNow(true);
        setStatus(fmt("proghatches.pattern_generator.reset.done"));
    }

    private InteractionSyncHandler selectionAction(String key, boolean selectAll) {
        InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(click -> {
            if (this.sync.isClient()) return;
            if (selectAll) {
                PatternRecipeCollection current = PatternGeneratorRecipes.collect(this.config);
                if (current.error == 0 && !current.hasFilterProblem()) {
                    for (PatternRecipeEntry entry : current.filtered) this.config.selectedRecipes.add(entry.id);
                }
            } else {
                this.config.selectedRecipes.clear();
            }
            this.persist.run();
            recomputeNow(false);
        });
        this.sync.syncValue(key, handler);
        return handler;
    }

    private InteractionSyncHandler pageButton(String key, int direction) {
        InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(click -> {
            if (!this.sync.isClient()) return;
            int target = this.page[0] + direction;
            if (target < 0 || target >= Math.max(1, this.pageModel.pages)) return;
            this.pageValue.setIntValue(target, true, true);
        });
        this.sync.syncValue("pattern_generator_page_" + key, handler);
        return handler;
    }

    private void toggleRow(int index) {
        PreviewRow row = row(index);
        if (row == null) return;
        if (!this.config.selectedRecipes.remove(row.id)) this.config.selectedRecipes.add(row.id);
        this.persist.run();
        recomputeNow(false);
    }

    private void serverTick() {
        if (this.sync.isClient()) return;
        if (this.data.getPlayer() instanceof EntityPlayerMP player) {
            String queueStatus = PatternGeneratorFillQueue.consumeStatus(player);
            if (queueStatus != null) setStatus(queueStatus);
        }
        updateTargetState();
        if (!this.recompute[0]) return;
        this.recompute[0] = false;
        recomputeNow(false);
    }

    private void markRecompute() {
        if (!this.sync.isClient()) this.recompute[0] = true;
    }

    private void recomputeNow(boolean resetPage) {
        if (this.sync.isClient()) return;
        PatternRecipeCollection result = PatternGeneratorRecipes.collect(this.config);
        if (resetPage) this.page[0] = 0;

        if (result.error != 0) {
            this.matchCount[0] = 0;
            this.selectedCount[0] = 0;
            this.page[0] = 0;
            this.pageModel = new PreviewPage();
            this.pageData = new NBTTagCompound();
            setStatus(fmt(PatternGeneratorRecipes.statusKey(result.error)));
            this.machineErrorActive = result.error == PatternGeneratorRecipes.ERR_NO_MACHINE
                || result.error == PatternGeneratorRecipes.ERR_NO_MAP;
            this.problemActive = false;
            publishCounts();
            publishPage();
            return;
        }

        if (!result.mapId.isEmpty() && !result.mapId.equals(this.config.recipeMapId)) {
            this.config.recipeMapId = result.mapId;
            this.persist.run();
        }
        this.publishMapValue(this.config.recipeMapId);
        if (this.machineErrorActive) {
            this.machineErrorActive = false;
            if (!result.hasFilterProblem()) setStatus("");
        }

        int matches = result.filtered.size();
        int selected = PatternRecipeSelection.countMatching(this.config.selectedRecipes, result.filteredIds());
        int pages = Math.max(1, (matches + PAGE_SIZE - 1) / PAGE_SIZE);
        if (this.page[0] < 0) this.page[0] = 0;
        if (this.page[0] >= pages) this.page[0] = pages - 1;

        this.matchCount[0] = matches;
        this.selectedCount[0] = selected;
        this.pageModel = buildPageModel(result, pages);
        this.pageData = this.pageModel.toNbt();

        if (result.hasFilterProblem()) {
            if (!this.problemActive) {
                PatternRecipeCollection.FilterProblem problem = result.firstFilterProblem();
                setStatus(
                    fmt(
                        "proghatches.pattern_generator.filter.invalid.status",
                        fmt(problem.section.langKey),
                        shorten(problem.pattern, 48)));
            }
            this.problemActive = true;
        } else if (this.problemActive) {
            this.problemActive = false;
            setStatus("");
        }

        publishCounts();
        publishPage();
        publishSelection();
    }

    private PreviewPage buildPageModel(PatternRecipeCollection result, int pages) {
        PreviewPage model = new PreviewPage();
        model.page = this.page[0];
        model.pages = pages;
        model.total = result.filtered.size();
        model.mapId = result.mapId;
        model.mapName = result.mapName;
        Map<String, List<PatternIngredient>> prioritizedInputs = prioritizedConflictInputs(result);
        int start = this.page[0] * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, result.filtered.size());
        for (int i = start; i < end; i++) {
            PatternRecipeEntry entry = result.filtered.get(i);
            model.rows.add(
                PreviewRow.of(
                    entry,
                    isSelected(entry.id),
                    prioritizedInputs.get(entry.id)));
        }
        return model;
    }

    private boolean isSelected(String id) {
        return this.config.selectedRecipes.contains(id);
    }

    /**
     * In only-conflict mode, the recipes for one shared output often differ by a single input.
     * Put the rarest inputs first within each conflict group so those differences occupy the
     * visible six input slots; stable sorting keeps the original order for equally rare inputs.
     */
    private Map<String, List<PatternIngredient>> prioritizedConflictInputs(PatternRecipeCollection result) {
        if (!this.config.onlyConflict) return Collections.emptyMap();
        Map<String, Map<String, Integer>> frequencies = new HashMap<>();
        for (PatternRecipeEntry entry : result.filtered) {
            String group = entry.conflictKey == null ? "" : entry.conflictKey;
            Map<String, Integer> counts = frequencies.computeIfAbsent(group, key -> new HashMap<>());
            for (PatternIngredient input : entry.inputs) {
                String key = input.typeKey();
                if (!key.isEmpty()) counts.merge(key, 1, Integer::sum);
            }
        }
        Map<String, List<PatternIngredient>> prioritized = new HashMap<>();
        for (PatternRecipeEntry entry : result.filtered) {
            Map<String, Integer> counts = frequencies.get(entry.conflictKey == null ? "" : entry.conflictKey);
            List<PatternIngredient> inputs = new ArrayList<>(entry.inputs);
            if (counts != null) {
                inputs.sort(
                    (a, b) -> Integer.compare(
                        counts.getOrDefault(a.typeKey(), 0),
                        counts.getOrDefault(b.typeKey(), 0)));
            }
            prioritized.put(entry.id, inputs);
        }
        return prioritized;
    }

    private void publishCounts() {
        this.matchValue.setIntValue(this.matchCount[0], true, true);
        this.selectedValue.setIntValue(this.selectedCount[0], true, true);
    }

    private void publishSelection() {
        if (this.sync.isClient()) return;
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (String id : this.config.selectedRecipes) {
            if (id != null && !id.isEmpty()) list.appendTag(new NBTTagString(id));
        }
        tag.setTag("ids", list);
        if (tag.equals(this.selectionData)) return;
        this.selectionData = tag;
        this.selectionValue.setValue(tag, false, true);
    }

    private void publishPage() {
        this.pageValue.setIntValue(this.page[0], false, true);
        this.pageDataValue.setValue(this.pageData, false, true);
    }

    private void setStatus(String text) {
        this.status[0] = text == null ? "" : text;
        this.statusValue.setStringValue(this.status[0], true, true);
    }

    private void applyGenerationStatus(PatternGeneratorRecipes.GenerationResult result, boolean selectedOnly) {
        if (result.error != 0) {
            if (result.error == PatternGeneratorRecipes.ERR_INVALID_FILTER && result.filterProblem != null) {
                setStatus(
                    fmt(
                        "proghatches.pattern_generator.filter.invalid.status",
                        fmt(result.filterProblem.section.langKey),
                        shorten(result.filterProblem.pattern, 48)));
            } else {
                setStatus(fmt(PatternGeneratorRecipes.statusKey(result.error)));
            }
            return;
        }
        if (result.generated > 0) {
            setStatus(
                fmt(
                    selectedOnly ? "proghatches.pattern_generator.status.generated_selected"
                        : "proghatches.pattern_generator.status.generated",
                    result.generated));
        } else if (result.skipped > 0) {
            setStatus(fmt("proghatches.pattern_generator.status.no_patterns"));
        } else {
            setStatus(fmt("proghatches.pattern_generator.status.no_recipes"));
        }
    }

    private PreviewRow row(int index) {
        if (index < 0 || index >= this.pageModel.rows.size()) return null;
        return this.pageModel.rows.get(index);
    }

    private PatternIngredient ingredient(int row, boolean output, int index) {
        PreviewRow previewRow = row(row);
        if (previewRow == null) return null;
        List<PatternIngredient> list = output ? previewRow.outputs : previewRow.inputs;
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }

    private IValue<ItemStack> rowItem(int row, boolean output, int index) {
        return new IValue<>() {

            @Override
            public ItemStack getValue() {
                PatternIngredient ingredient = ingredient(row, output, index);
                return ingredient == null ? null : ingredient.item;
            }

            @Override
            public void setValue(ItemStack value) {}

            @Override
            public Class<ItemStack> getValueType() {
                return ItemStack.class;
            }
        };
    }

    private IValue<FluidStack> rowFluid(int row, boolean output, int index) {
        return new IValue<>() {

            @Override
            public FluidStack getValue() {
                PatternIngredient ingredient = ingredient(row, output, index);
                return ingredient == null ? null : ingredient.fluid;
            }

            @Override
            public void setValue(FluidStack value) {}

            @Override
            public Class<FluidStack> getValueType() {
                return FluidStack.class;
            }
        };
    }

    private void addIngredientTooltip(com.cleanroommc.modularui.screen.RichTooltip tooltip, int row, boolean output,
        int index) {
        PatternIngredient ingredient = ingredient(row, output, index);
        if (ingredient == null) return;
        if (ingredient.item != null) {
            tooltip.addFromItem(ingredient.item);
            if (ingredient.amount > 1) {
                tooltip.addLine(fmt("proghatches.pattern_generator.preview.amount.item", ingredient.amount));
            }
        } else if (ingredient.fluid != null) {
            tooltip.addFromFluid(ingredient.fluid);
            tooltip.addLine(fmt("proghatches.pattern_generator.preview.amount.fluid", ingredient.amount));
        }
    }

    private String selectionLabel(int index) {
        PreviewRow row = row(index);
        if (row == null) return "";
        return row.selected ? "§a[x]" : "§8[ ]";
    }

    private String plusLabel(int index, boolean output) {
        PreviewRow row = row(index);
        if (row == null) return "";
        int total = output ? row.outputTotal : row.inputTotal;
        int extra = total - (output ? MAX_OUTPUT_ICONS : MAX_INPUT_ICONS);
        return extra > 0 ? "+" + extra : "";
    }

    private HoverThroughText amountOverlay(int row, boolean output, int index) {
        return new HoverThroughText(IKey.dynamic(() -> iconAmount(row, output, index))).scale(0.6f)
            .color(0xFFFFFFFF)
            .shadow(true)
            .alignment(Alignment.BottomRight)
            .setEnabledIf(widget -> {
                PatternIngredient ingredient = ingredient(row, output, index);
                return ingredient != null && ingredient.amount > 1;
            });
    }

    private String iconAmount(int row, boolean output, int index) {
        PatternIngredient ingredient = ingredient(row, output, index);
        return ingredient == null || ingredient.amount <= 1 ? "" : compactAmount(ingredient.amount);
    }

    private static String compactAmount(long amount) {
        if (amount < 1_000L) return Long.toString(amount);
        if (amount < 1_000_000L) return scaled(amount / 1_000.0) + "k";
        if (amount < 1_000_000_000L) return scaled(amount / 1_000_000.0) + "M";
        return scaled(amount / 1_000_000_000.0) + "G";
    }

    private static String scaled(double value) {
        if (value >= 100 || Math.abs(value - Math.rint(value)) < 0.05) return Long.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String rowCircuit(int index) {
        PreviewRow row = row(index);
        if (row == null) return "";
        if (row.circuit > 0) return fmt("proghatches.pattern_generator.circuit.value", row.circuit);
        if (row.ncLabel != null && !row.ncLabel.isEmpty()) {
            return fmt("proghatches.pattern_generator.preview.nc", shorten(row.ncLabel, 8));
        }
        return "";
    }

    private String rowCircuitTooltip(int index) {
        PreviewRow row = row(index);
        if (row == null) return "";
        if (row.circuit > 0) return fmt("proghatches.pattern_generator.circuit.value", row.circuit);
        if (row.ncLabel != null && !row.ncLabel.isEmpty()) {
            return fmt("proghatches.pattern_generator.preview.nc", row.ncLabel);
        }
        return "";
    }

    private String previewMapHeader() {
        if (this.pageModel.mapId == null && this.pageModel.mapName == null) return "";
        String name;
        if (this.pageModel.mapId != null && !this.pageModel.mapId.isEmpty()) {
            name = PatternMachineResolver.mapDisplayName(this.pageModel.mapId);
        } else {
            name = this.pageModel.mapName == null ? "" : this.pageModel.mapName;
        }
        if (name.isEmpty()) return "";
        return fmt("proghatches.pattern_generator.summary.map", name);
    }

    private String emptyHint() {
        if (!this.sync.isClient()) return "";
        return this.matchCount[0] == 0 ? fmt("proghatches.pattern_generator.preview.empty") : "";
    }

    private String pageLabel() {
        return (this.pageModel.page + 1) + "/" + Math.max(1, this.pageModel.pages);
    }

    private String tabLabel(int index, String key) {
        String prefix;
        try {
            prefix = this.tabs.getActivePageIndex() == index ? "§e" : "§8";
        } catch (Throwable ignored) {
            prefix = "§8";
        }
        return prefix + fmt(key);
    }

    private String machineSummary() {
        if (this.config.machine == null || this.config.machine.getItem() == null) {
            return fmt("proghatches.pattern_generator.summary.none");
        }
        return fmt("proghatches.pattern_generator.summary.machine", PatternIngredient.displayName(this.config.machine));
    }

    private String mapSummary() {
        return fmt("proghatches.pattern_generator.summary.map", mapLabel(this.mapValue.getStringValue()));
    }

    private String totalRules() {
        int total = 0;
        for (FilterSection section : this.filterSections) total += section.ruleCount();
        return Integer.toString(total);
    }

    private int orePageCount() {
        return Math.max(1, (this.pendingOres.size() + ORE_PAGE_SIZE - 1) / ORE_PAGE_SIZE);
    }

    private String orePageLabel() {
        return (this.orePage + 1) + "/" + orePageCount();
    }

    private String oreChoice(int row) {
        int index = this.orePage * ORE_PAGE_SIZE + row;
        if (index < 0 || index >= this.pendingOres.size()) return "";
        return this.pendingOres.get(index);
    }

    private String oreChoiceLabel(int row) {
        String value = oreChoice(row);
        return value.isEmpty() ? "" : shorten(value, 36);
    }

    private void chooseOre(int row) {
        String value = oreChoice(row);
        if (this.pendingOreSection == null || value.isEmpty()) return;
        this.pendingOreSection.append(PatternFilterRules.quoteLiteral(value));
        clearOrePicker();
    }

    private void clearOrePicker() {
        this.pendingOres.clear();
        this.pendingOreSection = null;
        this.orePage = 0;
        this.filterStatus[0] = "";
    }

    private void showOrePicker(FilterSection section, List<String> choices) {
        clearVoltagePicker();
        this.pendingOreSection = section;
        this.pendingOres.clear();
        this.pendingOres.addAll(choices);
        this.orePage = 0;
    }

    private static List<String> oreNames(ItemStack stack) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        try {
            ItemStack lookup = stack.copy();
            if (lookup.getItemDamage() == OreDictionary.WILDCARD_VALUE) lookup.setItemDamage(0);
            for (int id : OreDictionary.getOreIDs(lookup)) {
                String name = OreDictionary.getOreName(id);
                if (name != null && !name.isEmpty() && !"Unknown".equals(name)) names.add(name);
            }
        } catch (Throwable ignored) {}
        List<String> result = new ArrayList<>(names);
        Collections.sort(result);
        return result;
    }

    /**
     * Adds deduplicated prefix choices before the full ore names. A prefix such as {@code ingot}
     * matches every ore name that starts with it, while the full entries stay specific.
     */
    private static List<String> oreChoices(List<String> names) {
        LinkedHashSet<String> choices = new LinkedHashSet<>();
        for (String name : names) {
            String prefix = orePrefix(name);
            if (prefix != null && !prefix.isEmpty()) choices.add(prefix);
        }
        choices.addAll(names);
        return new ArrayList<>(choices);
    }

    private static String orePrefix(String name) {
        for (String prefix : orePrefixes()) {
            if (!name.startsWith(prefix)) continue;
            int end = prefix.length();
            if (end == name.length() || Character.isUpperCase(name.charAt(end))) return prefix;
        }
        int split = 1;
        while (split < name.length() && !Character.isUpperCase(name.charAt(split))) split++;
        return split >= 2 ? name.substring(0, split) : null;
    }

    private static List<String> orePrefixes() {
        if (ORE_PREFIXES == null) {
            List<String> names = new ArrayList<>();
            for (OrePrefixes prefix : OrePrefixes.VALUES) {
                if (prefix == null) continue;
                String name = prefix.name();
                if (name.length() < 2 || name.startsWith("_") || "NULL".equals(name)) continue;
                names.add(name);
            }
            names.sort((a, b) -> {
                int length = Integer.compare(b.length(), a.length());
                return length != 0 ? length : a.compareTo(b);
            });
            ORE_PREFIXES = names;
        }
        return ORE_PREFIXES;
    }

    private static String mapLabel(String id) {
        if (id == null || id.isEmpty()) {
            return StatCollector.translateToLocal("proghatches.pattern_generator.map.auto");
        }
        return PatternMachineResolver.mapDisplayName(id);
    }

    private static String fmt(String key, Object... args) {
        return StatCollector.translateToLocalFormatted(key, args);
    }

    private static String shorten(String value, int length) {
        if (value == null) return "";
        return value.length() <= length ? value : value.substring(0, length) + "...";
    }

    private final class FilterDragTarget extends Widget<FilterDragTarget>
        implements RecipeViewerGhostIngredientSlot<ItemStack> {

        private final FilterSection section;

        private FilterDragTarget(FilterSection section) {
            this.section = section;
        }

        @Override
        public boolean handleDragAndDrop(ItemStack stack, int mouseButton) {
            if (!PatternGeneratorPanel.this.sync.isClient() || stack == null || stack.getItem() == null) return false;
            PatternGeneratorPanel.this.filterStatus[0] = "";
            FluidStack fluid = null;
            try {
                fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
            } catch (Throwable ignored) {}
            if (fluid != null && fluid.getFluid() != null) {
                String name = fluid.getLocalizedName();
                if (name == null || name.isEmpty()) name = fluid.getFluid()
                    .getName();
                if (name != null && !name.isEmpty()) {
                    this.section.append(PatternFilterRules.quoteLiteral(name.replace('\n', ' ')));
                }
                return consumeDrag(stack);
            }
            if (this.section.modeValue.getBoolValue()) {
                List<String> names = oreNames(stack);
                if (names.isEmpty()) {
                    PatternGeneratorPanel.this.filterStatus[0] = fmt(
                        "proghatches.pattern_generator.filter.no_oredict");
                    return consumeDrag(stack);
                }
                List<String> choices = oreChoices(names);
                if (choices.size() == 1) {
                    this.section.append(PatternFilterRules.quoteLiteral(choices.get(0)));
                    return consumeDrag(stack);
                }
                showOrePicker(this.section, choices);
                return consumeDrag(stack);
            }
            String name = PatternIngredient.displayName(stack);
            if (name == null || name.isEmpty()) return consumeDrag(stack);
            this.section.append(PatternFilterRules.quoteLiteral(name.replace('\n', ' ')));
            return consumeDrag(stack);
        }

        /**
         * NEI only clears its dragged stack when the size reaches zero. Without this the ghost
         * item stays on the cursor and the following picker clicks are swallowed by the drag.
         */
        private boolean consumeDrag(ItemStack stack) {
            stack.stackSize = 0;
            return true;
        }

        @Override
        public boolean canClickThrough() {
            return true;
        }

        @Override
        public boolean canHover() {
            return false;
        }

        @Override
        public boolean canHoverThrough() {
            return true;
        }
    }

    private final class FilterSection {

        private final String id;
        private final String labelKey;
        private final java.util.function.Supplier<String> textGetter;
        private final java.util.function.Supplier<FilterMode> modeGetter;
        private final StringSyncValue textValue;
        private final BooleanSyncValue modeValue;
        private PatternFilterRules.Compiled compiled;
        private SyncedTextEditor field;

        private FilterSection(String id, String labelKey, java.util.function.Supplier<String> textGetter,
            java.util.function.Consumer<String> textSetter, java.util.function.Supplier<FilterMode> modeGetter,
            java.util.function.Consumer<FilterMode> modeSetter) {
            this.id = id;
            this.labelKey = labelKey;
            this.textGetter = textGetter;
            this.modeGetter = modeGetter;
            this.compiled = PatternFilterRules.compile(textGetter.get());
            this.textValue = new StringSyncValue(textGetter, value -> {
                String safe = value == null ? "" : value;
                if (safe.length() > MAX_FILTER_TEXT) safe = safe.substring(0, MAX_FILTER_TEXT);
                textSetter.accept(safe);
                this.compiled = PatternFilterRules.compile(safe);
                if (!PatternGeneratorPanel.this.sync.isClient()) {
                    PatternGeneratorPanel.this.persist.run();
                    markRecompute();
                }
            }).allowC2S();
            this.modeValue = new BooleanSyncValue(
                () -> modeGetter.get() == FilterMode.OREDICT,
                value -> {
                    modeSetter.accept(value ? FilterMode.OREDICT : FilterMode.NAME);
                    if (!PatternGeneratorPanel.this.sync.isClient()) {
                        PatternGeneratorPanel.this.persist.run();
                        markRecompute();
                    }
                }).allowC2S();
        }

        private int ruleCount() {
            return this.compiled.ruleCount();
        }

        private String rulesLabel() {
            return fmt("proghatches.pattern_generator.filter.rules", this.compiled.ruleCount());
        }

        private String errorLabel() {
            return this.compiled.hasInvalid()
                ? fmt("proghatches.pattern_generator.filter.invalid", shorten(this.compiled.invalidPattern(), 24))
                : "";
        }

        private String modeLabel() {
            return this.modeValue.getBoolValue() ? fmt("proghatches.pattern_generator.filter.mode.oredict")
                : fmt("proghatches.pattern_generator.filter.mode.name");
        }

        private void syncFromConfig() {
            String text = this.textGetter.get();
            this.compiled = PatternFilterRules.compile(text);
            this.textValue.setStringValue(text == null ? "" : text, true, true);
            this.modeValue.setBoolValue(this.modeGetter.get() == FilterMode.OREDICT, true, true);
            if (this.field != null) this.field.pull();
        }

        private void append(String literal) {
            String current = this.textValue.getStringValue();
            String next = current == null || current.trim()
                .isEmpty() ? literal : current + "\n" + literal;
            this.textValue.setStringValue(next, true, true);
            if (this.field != null) this.field.pull();
        }
    }

    private Interactable.Result handlePreviewKey(int rowIndex, boolean output, int keyCode, ItemStack query) {
        if (!this.sync.isClient() || query == null || query.getItem() == null) return Interactable.Result.IGNORE;
        boolean usage = !output;
        if (usage ? !PatternGeneratorNei.isUsageKey(keyCode) : !PatternGeneratorNei.isRecipeKey(keyCode)) {
            return Interactable.Result.IGNORE;
        }
        PatternRecipeEntry entry = findEntry(rowIndex);
        if (entry == null) return Interactable.Result.IGNORE;
        boolean opened = usage ? PatternGeneratorNei.openUsage(entry, query)
            : PatternGeneratorNei.openRecipe(entry, query);
        return opened ? Interactable.Result.STOP : Interactable.Result.IGNORE;
    }

    private PatternRecipeEntry findEntry(int rowIndex) {
        PreviewRow preview = row(rowIndex);
        if (preview == null) return null;
        PatternRecipeCollection collection = PatternGeneratorRecipes.collect(this.config);
        if (collection.error != 0) return null;
        for (PatternRecipeEntry entry : collection.all) {
            if (entry.id.equals(preview.id)) return entry;
        }
        return null;
    }

    private final class PreviewItemIcon extends ItemDisplayWidget implements Interactable {

        private final int rowIndex;
        private final boolean output;
        private final int icon;

        private PreviewItemIcon(int rowIndex, boolean output, int icon) {
            this.rowIndex = rowIndex;
            this.output = output;
            this.icon = icon;
        }

        @Override
        public Result onKeyPressed(char character, int keyCode) {
            PatternIngredient ingredient = ingredient(this.rowIndex, this.output, this.icon);
            if (ingredient == null) return Result.IGNORE;
            return handlePreviewKey(this.rowIndex, this.output, keyCode, ingredient.item);
        }
    }

    private final class PreviewFluidIcon extends FluidDisplayWidget implements Interactable {

        private final int rowIndex;
        private final boolean output;

        private PreviewFluidIcon(int rowIndex, boolean output) {
            this.rowIndex = rowIndex;
            this.output = output;
        }

        @Override
        public Result onKeyPressed(char character, int keyCode) {
            return handlePreviewKey(this.rowIndex, this.output, keyCode, getStackForRecipeViewer());
        }
    }

    /**
     * Draw-only text. It must not become the top hovered widget over an item icon, or NEI's
     * recipe/usage lookup sees a widget without a stack under the mouse.
     */
    private static final class HoverThroughText extends TextWidget<HoverThroughText> {

        private HoverThroughText(IKey key) {
            super(key);
        }

        @Override
        public boolean canHover() {
            return false;
        }
    }

    private static final class SyncedTextEditor extends TextEditorWidget {

        private final IStringValue<?> value;
        private boolean syncing;

        private SyncedTextEditor(IStringValue<?> value) {
            this.value = value;
            // BaseTextFieldWidget's renderer assumes at least one line before the first pull.
            this.handler.getText()
                .add("");
        }

        @Override
        public void onInit() {
            super.onInit();
            pull();
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            if (!isFocused()) pull();
        }

        @Override
        protected void onTextChanged() {
            super.onTextChanged();
            if (!this.syncing && this.value != null) this.value.setStringValue(this.handler.getTextAsString());
        }

        private void pull() {
            if (this.value == null) return;
            String current = this.value.getStringValue();
            if (current == null) current = "";
            if (!this.handler.getText()
                .isEmpty() && current.equals(this.handler.getTextAsString())) return;
            this.syncing = true;
            try {
                this.handler.getText()
                    .clear();
                for (String line : current.split("\\n", -1)) this.handler.getText()
                    .add(line);
                this.handler.clampCursors();
            } finally {
                this.syncing = false;
            }
        }
    }

    private static final class PresetPage {

        private int page;
        private int pages = 1;
        private int total;
        private final List<PresetRow> rows = new ArrayList<>();

        static PresetPage fromNbt(NBTTagCompound tag) {
            PresetPage page = new PresetPage();
            if (tag == null) return page;
            page.page = Math.max(0, tag.getInteger("page"));
            page.pages = Math.max(1, tag.getInteger("pages"));
            page.total = Math.max(0, tag.getInteger("total"));
            NBTTagList rows = tag.getTagList("rows", 10);
            for (int i = 0; i < rows.tagCount(); i++) {
                PresetRow row = PresetRow.fromNbt(rows.getCompoundTagAt(i));
                if (row != null) page.rows.add(row);
            }
            return page;
        }

        NBTTagCompound toNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("page", this.page);
            tag.setInteger("pages", this.pages);
            tag.setInteger("total", this.total);
            NBTTagList list = new NBTTagList();
            for (PresetRow row : this.rows) list.appendTag(row.toNbt());
            tag.setTag("rows", list);
            return tag;
        }
    }

    private static final class PresetRow {

        private String name = "";
        private String machine = "";
        private String map = "";
        private String circuit = "";
        private String summary = "";
        private String author = "";
        private int recipes;
        private int machineMode = -1;
        private long updatedAt;
        private boolean onlyConflict;
        private boolean firstOutputOnly;

        static PresetRow fromNbt(NBTTagCompound tag) {
            if (tag == null) return null;
            PresetRow row = new PresetRow();
            row.name = tag.getString("name");
            row.machine = tag.getString("machine");
            row.map = tag.getString("map");
            row.circuit = tag.getString("circuit");
            row.summary = tag.getString("summary");
            row.author = tag.getString("author");
            row.recipes = Math.max(0, tag.getInteger("recipes"));
            row.machineMode = tag.getInteger("machineMode");
            row.updatedAt = tag.getLong("updatedAt");
            row.onlyConflict = tag.getBoolean("onlyConflict");
            row.firstOutputOnly = tag.getBoolean("firstOutputOnly");
            return row;
        }

        NBTTagCompound toNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("name", this.name);
            tag.setString("machine", this.machine);
            tag.setString("map", this.map);
            tag.setString("circuit", this.circuit);
            tag.setString("summary", this.summary);
            tag.setString("author", this.author);
            tag.setInteger("recipes", this.recipes);
            tag.setInteger("machineMode", this.machineMode);
            tag.setLong("updatedAt", this.updatedAt);
            tag.setBoolean("onlyConflict", this.onlyConflict);
            tag.setBoolean("firstOutputOnly", this.firstOutputOnly);
            return tag;
        }
    }

    private static final class PreviewPage {

        private int page;
        private int pages = 1;
        private int total;
        private String mapId = "";
        private String mapName = "";
        private final List<PreviewRow> rows = new ArrayList<>();

        static PreviewPage fromNbt(NBTTagCompound tag) {
            PreviewPage page = new PreviewPage();
            if (tag == null) return page;
            page.page = Math.max(0, tag.getInteger("page"));
            page.pages = Math.max(1, tag.getInteger("pages"));
            page.total = Math.max(0, tag.getInteger("total"));
            page.mapId = tag.getString("mapId");
            page.mapName = tag.getString("mapName");
            NBTTagList rows = tag.getTagList("rows", 10);
            for (int i = 0; i < rows.tagCount(); i++) {
                PreviewRow row = PreviewRow.fromNbt(rows.getCompoundTagAt(i));
                if (row != null) page.rows.add(row);
            }
            return page;
        }

        NBTTagCompound toNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("page", this.page);
            tag.setInteger("pages", this.pages);
            tag.setInteger("total", this.total);
            tag.setString("mapId", this.mapId);
            tag.setString("mapName", this.mapName);
            NBTTagList list = new NBTTagList();
            for (PreviewRow row : this.rows) list.appendTag(row.toNbt());
            tag.setTag("rows", list);
            return tag;
        }
    }

    private static final class PreviewRow {

        private String id = "";
        private int circuit = -1;
        private String ncLabel = "";
        private boolean selected;
        private int inputTotal;
        private int outputTotal;
        private final List<PatternIngredient> inputs = new ArrayList<>();
        private final List<PatternIngredient> outputs = new ArrayList<>();

        static PreviewRow of(PatternRecipeEntry entry, boolean selected) {
            return of(entry, selected, null);
        }

        static PreviewRow of(PatternRecipeEntry entry, boolean selected, List<PatternIngredient> previewInputs) {
            PreviewRow row = new PreviewRow();
            row.id = entry.id;
            row.circuit = entry.circuit;
            row.ncLabel = entry.ncLabel == null ? "" : entry.ncLabel;
            row.selected = selected;
            row.inputTotal = entry.inputs.size();
            row.outputTotal = entry.outputs.size();
            List<PatternIngredient> inputs = previewInputs == null ? entry.inputs : previewInputs;
            for (int i = 0; i < Math.min(MAX_INPUT_ICONS, inputs.size()); i++) row.inputs.add(inputs.get(i));
            for (int i = 0; i < Math.min(MAX_OUTPUT_ICONS, entry.outputs.size()); i++) row.outputs.add(entry.outputs.get(i));
            return row;
        }

        static PreviewRow fromNbt(NBTTagCompound tag) {
            if (tag == null) return null;
            PreviewRow row = new PreviewRow();
            row.id = tag.getString("id");
            row.circuit = tag.getInteger("circuit");
            row.ncLabel = tag.getString("nc");
            row.selected = tag.getBoolean("sel");
            row.inputTotal = tag.getInteger("inTotal");
            row.outputTotal = tag.getInteger("outTotal");
            readIngredients(tag.getTagList("in", 10), row.inputs);
            readIngredients(tag.getTagList("out", 10), row.outputs);
            return row;
        }

        private static void readIngredients(NBTTagList list, List<PatternIngredient> target) {
            for (int i = 0; i < list.tagCount(); i++) {
                PatternIngredient ingredient = PatternIngredient.fromTag(list.getCompoundTagAt(i));
                if (ingredient != null) target.add(ingredient);
            }
        }

        NBTTagCompound toNbt() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("id", this.id);
            tag.setInteger("circuit", this.circuit);
            tag.setString("nc", this.ncLabel);
            tag.setBoolean("sel", this.selected);
            tag.setInteger("inTotal", this.inputTotal);
            tag.setInteger("outTotal", this.outputTotal);
            tag.setTag("in", ingredientList(this.inputs));
            tag.setTag("out", ingredientList(this.outputs));
            return tag;
        }

        private static NBTTagList ingredientList(List<PatternIngredient> ingredients) {
            NBTTagList list = new NBTTagList();
            for (PatternIngredient ingredient : ingredients) list.appendTag(ingredient.toTag());
            return list;
        }
    }
}
