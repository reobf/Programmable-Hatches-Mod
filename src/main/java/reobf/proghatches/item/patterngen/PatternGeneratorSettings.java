package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

/**
 * Persistent settings for the pattern generator item.
 * <p>
 * The item is the only source of truth: both GUI sides read the same NBT shape, and all
 * server-side edits write it back immediately. Recipe contents are never stored here, only
 * the selected machine, map, filters, selected recipe ids and the optional direct-fill queue.
 */
public final class PatternGeneratorSettings {

    private static final String ROOT = "PHPatternGenerator";
    public static final int CURRENT_SCHEMA = 3;
    private static final int LEGACY_SCHEMA_1 = 1;
    private static final int LEGACY_SCHEMA_2 = 2;

    private static final int MAX_MAP_ID = 128;
    private static final int MAX_REGEX = 2048;
    private static final int MAX_SELECTED = 2048;
    private static final int MAX_RECIPE_ID = 64;
    private static final int MAX_AUTHOR = 64;
    private static final int NO_TARGET = Integer.MIN_VALUE;

    public enum FilterMode {

        NAME("name"),
        OREDICT("oredict");

        private final String tag;

        FilterMode(String tag) {
            this.tag = tag;
        }

        public String tag() {
            return this.tag;
        }

        public static FilterMode fromTag(String value) {
            if (value != null) {
                for (FilterMode mode : values()) {
                    if (mode.tag.equals(value)) return mode;
                }
            }
            return NAME;
        }
    }

    public ItemStack machine;
    public String recipeMapId = "";
    public int circuit = -1;
    public String inputInclude = "";
    public String inputExclude = "";
    public String outputInclude = "";
    public String outputExclude = "";
    public FilterMode inputIncludeMode = FilterMode.NAME;
    public FilterMode inputExcludeMode = FilterMode.NAME;
    public FilterMode outputIncludeMode = FilterMode.NAME;
    public FilterMode outputExcludeMode = FilterMode.NAME;
    public boolean onlyConflict;
    public boolean firstOutputOnly;
    public int minVoltage = -1;
    public int maxVoltage = -1;
    public String author = "";
    public final Set<String> selectedRecipes = new LinkedHashSet<>();

    /** Armed direct fill: the next right-click on a pattern container starts the queue. */
    public boolean fillArmed;
    /** Ordered recipe ids still waiting for direct fill. */
    public final List<String> pendingRecipes = new ArrayList<>();
    /** Number of recipes in the current fill, including already delivered ones. */
    public int pendingTotal;
    public int pendingDim = NO_TARGET;
    public int pendingX;
    public int pendingY;
    public int pendingZ;

    public static PatternGeneratorSettings load(ItemStack tool) {
        if (tool == null || tool.getTagCompound() == null) return new PatternGeneratorSettings();
        NBTTagCompound root = tool.getTagCompound();
        if (!root.hasKey(ROOT)) return new PatternGeneratorSettings();
        return fromTag(root.getCompoundTag(ROOT));
    }

    public void save(ItemStack tool) {
        if (tool == null) return;
        NBTTagCompound root = tool.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            tool.setTagCompound(root);
        }
        root.setTag(ROOT, toTag());
    }

    public void setMachine(ItemStack stack) {
        this.machine = stack == null ? null : stack.copy();
        if (this.machine != null) this.machine.stackSize = 1;
        this.recipeMapId = "";
        clearPending();
    }

    public List<String> selectedRecipeList() {
        return new ArrayList<>(this.selectedRecipes);
    }

    public boolean hasPending() {
        return !this.pendingRecipes.isEmpty();
    }

    public boolean hasPendingTarget() {
        return this.pendingDim != NO_TARGET;
    }

    public void clearPending() {
        this.fillArmed = false;
        this.pendingRecipes.clear();
        this.pendingTotal = 0;
        this.pendingDim = NO_TARGET;
        this.pendingX = 0;
        this.pendingY = 0;
        this.pendingZ = 0;
    }

    public void setPendingTarget(int dimension, int x, int y, int z) {
        this.pendingDim = dimension;
        this.pendingX = x;
        this.pendingY = y;
        this.pendingZ = z;
    }

    /** Keeps the pending ids but forgets the container, so the next right-click picks a new one. */
    public void clearPendingTarget() {
        this.pendingDim = NO_TARGET;
        this.pendingX = 0;
        this.pendingY = 0;
        this.pendingZ = 0;
    }

    /** Copies every configuration field, but never the transient fill queue. */
    public void copyConfigFrom(PatternGeneratorSettings source) {
        if (source == null) return;
        this.machine = source.machine == null ? null : source.machine.copy();
        if (this.machine != null) this.machine.stackSize = 1;
        this.recipeMapId = source.recipeMapId;
        this.circuit = source.circuit;
        this.inputInclude = source.inputInclude;
        this.inputExclude = source.inputExclude;
        this.outputInclude = source.outputInclude;
        this.outputExclude = source.outputExclude;
        this.inputIncludeMode = source.inputIncludeMode;
        this.inputExcludeMode = source.inputExcludeMode;
        this.outputIncludeMode = source.outputIncludeMode;
        this.outputExcludeMode = source.outputExcludeMode;
        this.onlyConflict = source.onlyConflict;
        this.firstOutputOnly = source.firstOutputOnly;
        this.minVoltage = source.minVoltage;
        this.maxVoltage = source.maxVoltage;
        this.author = source.author;
        this.selectedRecipes.clear();
        this.selectedRecipes.addAll(source.selectedRecipes);
    }

    static PatternGeneratorSettings fromTag(NBTTagCompound tag) {
        PatternGeneratorSettings settings = new PatternGeneratorSettings();
        if (tag == null) return settings;
        int schema = tag.getInteger("schema");
        if (schema == LEGACY_SCHEMA_1) {
            settings.readMachine(tag);
            settings.recipeMapId = safeString(tag.getString("recipeMap"), MAX_MAP_ID);
            settings.circuit = clampCircuit(tag.getInteger("circuit"));
            settings.inputInclude = migrateLegacyFilter(tag.getString("inputFilter"));
            settings.outputInclude = migrateLegacyFilter(tag.getString("outputFilter"));
            return settings;
        }
        if (schema != LEGACY_SCHEMA_2 && schema != CURRENT_SCHEMA) return settings;

        settings.readMachine(tag);
        settings.recipeMapId = safeString(tag.getString("recipeMap"), MAX_MAP_ID);
        settings.circuit = clampCircuit(tag.getInteger("circuit"));
        settings.inputInclude = safeRegex(tag.getString("inputInclude"));
        settings.inputExclude = safeRegex(tag.getString("inputExclude"));
        settings.outputInclude = safeRegex(tag.getString("outputInclude"));
        settings.outputExclude = safeRegex(tag.getString("outputExclude"));
        settings.inputIncludeMode = FilterMode.fromTag(tag.getString("inputIncludeMode"));
        settings.inputExcludeMode = FilterMode.fromTag(tag.getString("inputExcludeMode"));
        settings.outputIncludeMode = FilterMode.fromTag(tag.getString("outputIncludeMode"));
        settings.outputExcludeMode = FilterMode.fromTag(tag.getString("outputExcludeMode"));
        settings.onlyConflict = tag.getBoolean("onlyConflict");
        if (tag.hasKey("minVoltage")) settings.minVoltage = clampVoltage(tag.getInteger("minVoltage"));
        if (tag.hasKey("maxVoltage")) settings.maxVoltage = clampVoltage(tag.getInteger("maxVoltage"));
        settings.readSelected(tag);
        if (schema >= CURRENT_SCHEMA) settings.readPhaseC(tag);
        return settings;
    }

    NBTTagCompound toTag() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("schema", CURRENT_SCHEMA);
        if (this.machine != null) {
            NBTTagCompound machineTag = new NBTTagCompound();
            this.machine.writeToNBT(machineTag);
            tag.setTag("machine", machineTag);
        }
        tag.setString("recipeMap", safeString(this.recipeMapId, MAX_MAP_ID));
        tag.setInteger("circuit", clampCircuit(this.circuit));
        tag.setString("inputInclude", safeRegex(this.inputInclude));
        tag.setString("inputExclude", safeRegex(this.inputExclude));
        tag.setString("outputInclude", safeRegex(this.outputInclude));
        tag.setString("outputExclude", safeRegex(this.outputExclude));
        tag.setString("inputIncludeMode", this.inputIncludeMode.tag());
        tag.setString("inputExcludeMode", this.inputExcludeMode.tag());
        tag.setString("outputIncludeMode", this.outputIncludeMode.tag());
        tag.setString("outputExcludeMode", this.outputExcludeMode.tag());
        tag.setBoolean("onlyConflict", this.onlyConflict);
        tag.setBoolean("firstOutputOnly", this.firstOutputOnly);
        tag.setInteger("minVoltage", clampVoltage(this.minVoltage));
        tag.setInteger("maxVoltage", clampVoltage(this.maxVoltage));
        tag.setString("author", safeString(this.author, MAX_AUTHOR));
        NBTTagList selected = new NBTTagList();
        int count = 0;
        for (String id : this.selectedRecipes) {
            if (id == null || id.isEmpty() || count >= MAX_SELECTED) continue;
            selected.appendTag(new NBTTagString(safeString(id, MAX_RECIPE_ID)));
            count++;
        }
        tag.setTag("selectedRecipes", selected);

        tag.setBoolean("fillArmed", this.fillArmed);
        NBTTagList pending = new NBTTagList();
        count = 0;
        for (String id : this.pendingRecipes) {
            if (id == null || id.isEmpty() || count >= MAX_SELECTED) continue;
            pending.appendTag(new NBTTagString(safeString(id, MAX_RECIPE_ID)));
            count++;
        }
        tag.setTag("pendingRecipes", pending);
        tag.setInteger("pendingTotal", Math.max(0, Math.min(MAX_SELECTED, this.pendingTotal)));
        if (hasPendingTarget()) {
            tag.setInteger("pendingDim", this.pendingDim);
            tag.setInteger("pendingX", this.pendingX);
            tag.setInteger("pendingY", this.pendingY);
            tag.setInteger("pendingZ", this.pendingZ);
        }
        return tag;
    }

    private void readMachine(NBTTagCompound tag) {
        if (!tag.hasKey("machine")) {
            this.machine = null;
            return;
        }
        this.machine = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("machine"));
        if (this.machine != null) this.machine.stackSize = 1;
    }

    private void readSelected(NBTTagCompound tag) {
        if (!tag.hasKey("selectedRecipes")) return;
        NBTTagList list = tag.getTagList("selectedRecipes", 8);
        int limit = Math.min(list.tagCount(), MAX_SELECTED);
        for (int i = 0; i < limit; i++) {
            String id = safeString(list.getStringTagAt(i), MAX_RECIPE_ID);
            if (!id.isEmpty()) this.selectedRecipes.add(id);
        }
    }

    private void readPhaseC(NBTTagCompound tag) {
        this.firstOutputOnly = tag.getBoolean("firstOutputOnly");
        this.author = safeString(tag.getString("author"), MAX_AUTHOR);
        this.fillArmed = tag.getBoolean("fillArmed");
        if (tag.hasKey("pendingRecipes")) {
            NBTTagList list = tag.getTagList("pendingRecipes", 8);
            int limit = Math.min(list.tagCount(), MAX_SELECTED);
            for (int i = 0; i < limit; i++) {
                String id = safeString(list.getStringTagAt(i), MAX_RECIPE_ID);
                if (!id.isEmpty()) this.pendingRecipes.add(id);
            }
        }
        this.pendingTotal = Math.max(0, Math.min(MAX_SELECTED, tag.getInteger("pendingTotal")));
        if (tag.hasKey("pendingDim")) {
            this.pendingDim = tag.getInteger("pendingDim");
            this.pendingX = tag.getInteger("pendingX");
            this.pendingY = tag.getInteger("pendingY");
            this.pendingZ = tag.getInteger("pendingZ");
        }
    }

    private static String migrateLegacyFilter(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? "" : safeRegex(PatternFilterRules.quoteLiteral(trimmed));
    }

    private static int clampCircuit(int value) {
        return Math.max(-1, Math.min(32, value));
    }

    private static int clampVoltage(int value) {
        return Math.max(-1, value);
    }

    private static String safeRegex(String value) {
        return safeString(value, MAX_REGEX);
    }

    private static String safeString(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
