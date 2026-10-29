package reobf.proghatches.gt.metatileentity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import org.apache.commons.lang3.tuple.Pair;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.LongSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.google.common.collect.ImmutableMap;

import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.storage.IStorageInterceptor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.me.GridAccessException;
import appeng.me.cache.NetworkMonitor;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.Platform;
import appeng.util.inv.MEInventoryCrafting;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import codechicken.nei.ItemStackMap;
import codechicken.nei.ItemStackSet;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.enums.ItemList;
import gregtech.common.gui.modularui.util.PatternSlot;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import reobf.proghatches.ae.TileChanceController;
import reobf.proghatches.gt.metatileentity.chance.ChanceMark;
import reobf.proghatches.gt.metatileentity.cycle.CycleAmount;
import reobf.proghatches.gt.metatileentity.cycle.CycleAnalysis;
import reobf.proghatches.gt.metatileentity.cycle.CycleAnalyzer;
import reobf.proghatches.gt.metatileentity.cycle.CycleBalancing;
import reobf.proghatches.gt.metatileentity.cycle.CycleItemSetting;
import reobf.proghatches.gt.metatileentity.cycle.CycleLimits;
import reobf.proghatches.gt.metatileentity.cycle.CyclePattern;
import reobf.proghatches.gt.metatileentity.cycle.CycleWarning;
import reobf.proghatches.gt.metatileentity.util.PushStaging;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class CyclicPatternDualInputHatch extends PatternDualInputHatch {

    public CyclicPatternDualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid,
        int bufferNum, boolean sf, int page) {
        super(
            id,
            name,
            nameRegional,
            tier,
            mMultiFluid,
            bufferNum,
            sf,
            page,
            reobf.proghatches.main.Config.get(
                "CPDIH",
                ImmutableMap.of(
                    "bufferNum",
                    bufferNum,
                    "fluidSlots",
                    page * 16,
                    "mMultiFluid",
                    mMultiFluid,
                    "slots",
                    page * 16)));
    }

    public CyclicPatternDualInputHatch(String mName, byte mTier, int slots, String[] desc, ITexture[][][] tex,
        boolean mMultiFluid, int bufferNum) {
        super(mName, mTier, slots, desc, tex, mMultiFluid, bufferNum);
    }

    public class CInst extends CyclicPatternDualInputHatch {

        public CInst(String mName, byte mTier, int slots, String[] d, ITexture[][][] t, boolean mf, int bn) {
            super(mName, mTier, slots, d, t, mf, bn);
        }

        @Override
        public boolean supportsFluids() {
            return CyclicPatternDualInputHatch.this.supportsFluids();
        }

        @Override
        public int page() {
            return CyclicPatternDualInputHatch.this.page();
        }
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity te) {
        return new CInst(mName, mTier, 16 * page() + 1, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
    }

    static final int GROUPS = 4, SLOTS = 36, COLS = 4, ROWS = SLOTS / COLS, MAX_ROWS = 8, DEBOUNCE = 10;
    static final CycleLimits LIMITS = new CycleLimits(SLOTS, 16, 400, 2_000_000L, 2_000_000_000L);

    final List<LinkedHashMap<SKey, CycSetting>> settings = new ArrayList<>();
    final LinkedHashMap<SKey, SeedState> seeds = new LinkedHashMap<>();
    final ArrayDeque<Dose> doses = new ArrayDeque<>();
    int cycMultiplier = 1;
    boolean autoSeed = true;
    int autoSeedMult = 1, autoSeedChanceMult = 4;
    final int[] groupEnd = { 9, 18, 27 };

    @SuppressWarnings("unchecked")
    CycleAnalysis<SKey>[] analysis = new CycleAnalysis[GROUPS];
    final List<List<CycleWarning<SKey>>> hatchWarnings = new ArrayList<>();
    final ItemStack[] analysedPattern = new ItemStack[36];
    @SuppressWarnings("unchecked")
    List<IAEStack<?>>[] memberRuns = new List[36];
    boolean analysisDirty = true;
    long nextAnalysisTick;
    ICraftingPatternDetails[] published = {};
    ICraftingPatternDetails[] offered = {};
    final HashMap<ICraftingPatternDetails, BalRef> lookup = new HashMap<>();
    final SeedInterceptor interceptor = new SeedInterceptor();
    boolean removed;
    boolean interceptorCheck = true;
    boolean hasOwed;
    boolean selfIo;
    MachineSource seedSource;
    long uiCacheTick = -1;

    {
        for (int g = 0; g < GROUPS; g++) {
            settings.add(new LinkedHashMap<>());
            hatchWarnings.add(new ArrayList<>());
        }
    }

    static final class SKey {

        final IAEStack<?> st;

        private SKey(IAEStack<?> st) {
            this.st = st;
        }

        static SKey of(IAEStack<?> s) {
            IAEStack<?> c = s.copy();
            c.setStackSize(1);
            return new SKey(c);
        }

        static SKey probe(IAEStack<?> s) {
            return new SKey(s);
        }

        IAEStack<?> toAE(long n) {
            IAEStack<?> c = st.copy();
            c.setStackSize(n);
            return c;
        }

        boolean isFluid() {
            return st.isFluid();
        }

        String name() {
            return st.isItem() ? ((IAEItemStack) st).getItemStack()
                .getDisplayName()
                : ((IAEFluidStack) st).getFluidStack()
                    .getLocalizedName();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SKey k && st.equals(k.st);
        }

        @Override
        public int hashCode() {
            return st.hashCode();
        }

        @Override
        public String toString() {
            return String.valueOf(st);
        }
    }

    static final class SeedState {

        long stock;
        long owed;
    }

    static final class CycSetting {

        long target;
        boolean output = true;
        int tol = 5;

        CycleItemSetting toCore() {
            return new CycleItemSetting(target, output, tol);
        }
    }

    static final class Dose {

        final List<IAEStack<?>> run;
        long runs;
        final int lane;
        /** The marked pattern the run belongs to, so the hatch that takes it books the copies; null when unmarked. */
        final ItemStack pattern;
        ICraftingPatternDetails details;

        Dose(List<IAEStack<?>> run, long runs, int lane, ItemStack pattern) {
            this.run = run;
            this.runs = runs;
            this.lane = lane;
            this.pattern = pattern;
        }
    }

    static final class BalRef {

        final int group;
        final CycleBalancing<SKey> bal;
        final List<IAEStack<?>>[] memberRun;
        final int[] memberLane;
        final ItemStack[] memberPattern;

        BalRef(int group, CycleBalancing<SKey> bal, List<IAEStack<?>>[] memberRun, int[] memberLane,
            ItemStack[] memberPattern) {
            this.group = group;
            this.bal = bal;
            this.memberRun = memberRun;
            this.memberLane = memberLane;
            this.memberPattern = memberPattern;
        }
    }

    ICraftingPatternDetails doseDetails(Dose d) {
        if (d.pattern == null) return null;
        if (d.details == null) try {
            d.details = ((ICraftingPatternItem) d.pattern.getItem()).getPatternForItem(
                d.pattern,
                getBaseMetaTileEntity().getWorld());
        } catch (Exception ignored) {}
        return d.details;
    }

    static boolean isCircuit(IAEStack<?> s) {
        return s != null && s.isItem() && ((IAEItemStack) s).getItem() instanceof ItemProgrammingCircuit;
    }

    static List<PushStaging.Part> toParts(List<IAEStack<?>> run) {
        List<PushStaging.Part> parts = new ArrayList<>(run.size());
        for (IAEStack<?> s : run) {
            if (s.isFluid()) parts.add(PushStaging.Part.fluid(((IAEFluidStack) s).getFluidStack(), s.getStackSize()));
            else parts.add(PushStaging.Part.item(((IAEItemStack) s).getItemStack(), s.getStackSize()));
        }
        return parts;
    }

    private static List<CycleAmount<SKey>> toAmounts(IAEStack<?>[] arr) {
        List<CycleAmount<SKey>> ret = new ArrayList<>();
        if (arr == null) return ret;
        for (IAEStack<?> s : arr) {
            if (s == null || isCircuit(s) || s.getStackSize() <= 0) continue;
            ret.add(new CycleAmount<>(SKey.of(s), s.getStackSize()));
        }
        return ret;
    }

    static final int LANES = 8;
    static final int LANE_UNBOUND = 0, LANE_OK = 1, LANE_UNLOADED = 2, LANE_INVALID = 3;
    final int[] patternLane = new int[36];
    final int[][] laneOffset = new int[LANES + 1][];
    final ItemStack[] laneIconSeen = new ItemStack[LANES + 1];
    int laneSignature = -1;

    BufferedDualInputHatch laneHatch(int lane) {
        int[] o = lane > 0 && lane <= LANES ? laneOffset[lane] : null;
        if (o == null) return null;
        IGregTechTileEntity me = getBaseMetaTileEntity();
        World w = me.getWorld();
        int x = me.getXCoord() + o[0], y = me.getYCoord() + o[1], z = me.getZCoord() + o[2];
        if (!w.blockExists(x, y, z)) return null;
        return validTarget(w.getTileEntity(x, y, z));
    }

    BufferedDualInputHatch validTarget(TileEntity te) {
        if (te instanceof IGregTechTileEntity g && g.getMetaTileEntity() instanceof BufferedDualInputHatch b
            && !(b instanceof CyclicPatternDualInputHatch)
            && b != this) return b;
        return null;
    }

    int laneState(int lane) {
        int[] o = lane > 0 && lane <= LANES ? laneOffset[lane] : null;
        if (o == null) return LANE_UNBOUND;
        IGregTechTileEntity me = getBaseMetaTileEntity();
        World w = me.getWorld();
        int x = me.getXCoord() + o[0], y = me.getYCoord() + o[1], z = me.getZCoord() + o[2];
        if (!w.blockExists(x, y, z)) return LANE_UNLOADED;
        return validTarget(w.getTileEntity(x, y, z)) != null ? LANE_OK : LANE_INVALID;
    }

    boolean laneUsable(int lane) {
        if (lane == 0) return true;
        int s = laneState(lane);
        return s == LANE_OK || s == LANE_UNLOADED;
    }

    void setPatternLane(int slot, int lane) {
        if (slot < 0 || slot >= patternLane.length) return;
        int v = Math.max(0, Math.min(LANES, lane));
        if (patternLane[slot] == v) return;
        patternLane[slot] = v;
        analysisDirty = true;
        uiCacheTick = -1;
        markDirty();
    }

    static boolean isDataStick(ItemStack st) {
        return st != null && ItemList.Tool_DataStick.isStackEqual(st, false, true);
    }

    static boolean isLinkStick(ItemStack st) {
        return isDataStick(st) && st.hasTagCompound()
            && "ProgHatchesDualInput".equals(
                st.getTagCompound()
                    .getString("type"));
    }

    String bindLane(int lane, EntityPlayer p) {
        if (lane < 1 || lane > LANES || p == null) return null;
        ItemStack cur = p.inventory.getItemStack(), held = p.inventory.getCurrentItem();
        ItemStack st = isLinkStick(cur) ? cur : isLinkStick(held) ? held : isDataStick(cur) ? cur : held;
        if (!isDataStick(st)) return "nostick";
        NBTTagCompound t = st.getTagCompound();
        String type = t == null ? "" : t.getString("type");
        if ("CraftingInputBuffer".equals(type)) return "crib";
        if (!"ProgHatchesDualInput".equals(type)) return "empty";
        int x = t.getInteger("x"), y = t.getInteger("y"), z = t.getInteger("z");
        IGregTechTileEntity me = getBaseMetaTileEntity();
        World w = me.getWorld();
        if (t.hasKey("dim") && t.getInteger("dim") != w.provider.dimensionId) return "dim";
        if (!w.blockExists(x, y, z)) return "unloaded";
        if (validTarget(w.getTileEntity(x, y, z)) == null) return "invalid";
        int[] o = { x - me.getXCoord(), y - me.getYCoord(), z - me.getZCoord() };
        if (Arrays.equals(o, laneOffset[lane])) return null;
        if (laneHasDoses(lane)) return "busy";
        laneOffset[lane] = o;
        laneIconSeen[lane] = null;
        analysisDirty = true;
        uiCacheTick = -1;
        markDirty();
        return null;
    }

    String unbindLane(int lane) {
        if (lane < 1 || lane > LANES || laneOffset[lane] == null) return null;
        if (laneHasDoses(lane)) return "busy";
        laneOffset[lane] = null;
        laneIconSeen[lane] = null;
        analysisDirty = true;
        uiCacheTick = -1;
        markDirty();
        return null;
    }

    private int computeLaneSignature() {
        int sig = 1;
        for (int l = 1; l <= LANES; l++) {
            int s = laneState(l);
            BufferedDualInputHatch t = s == LANE_OK ? laneHatch(l) : null;
            sig = sig * 31 + s;
            sig = sig * 31 + (t == null ? 0
                : t.getBaseMetaTileEntity()
                    .getMetaTileID());
        }
        return sig;
    }

    static List<IAEStack<?>> runFor(BufferedDualInputHatch t, List<IAEStack<?>> run) {
        if (t.program) return run;
        List<IAEStack<?>> r = new ArrayList<>(run.size());
        for (IAEStack<?> s : run) if (!isCircuit(s)) r.add(s);
        return r;
    }

    boolean laneHasDoses(int lane) {
        for (Dose d : doses) if (d.lane == lane) return true;
        return false;
    }

    final long[] laneBadSince = new long[LANES + 1];

    /** Doses of a lane whose target stays unbound or invalid for 10 seconds go back to ME. */
    private void refundStrandedDoses(long now) {
        for (int l = 1; l <= LANES; l++) {
            if (!laneHasDoses(l)) {
                laneBadSince[l] = 0;
                continue;
            }
            int s = laneState(l);
            if (s == LANE_OK || s == LANE_UNLOADED) {
                laneBadSince[l] = 0;
                continue;
            }
            if (laneBadSince[l] == 0) laneBadSince[l] = now;
            else if (now - laneBadSince[l] >= 200) {
                List<Dose> stranded = new ArrayList<>();
                for (Iterator<Dose> it = doses.iterator(); it.hasNext();) {
                    Dose d = it.next();
                    if (d.lane == l) {
                        stranded.add(d);
                        it.remove();
                    }
                }
                for (Dose d : stranded) refundDose(d);
                MyMod.LOG.warn("cyclic lane " + l + " has no target, refunded " + stranded.size() + " queued doses");
                laneBadSince[l] = 0;
                markDirty();
            }
        }
    }

    private NBTTagList writeLaneOffsets() {
        NBTTagList list = new NBTTagList();
        for (int l = 1; l <= LANES; l++) {
            if (laneOffset[l] == null) continue;
            NBTTagCompound c = new NBTTagCompound();
            c.setInteger("l", l);
            c.setInteger("dx", laneOffset[l][0]);
            c.setInteger("dy", laneOffset[l][1]);
            c.setInteger("dz", laneOffset[l][2]);
            list.appendTag(c);
        }
        return list;
    }

    private void readLaneOffsets(NBTTagList list) {
        Arrays.fill(laneOffset, null);
        Arrays.fill(laneIconSeen, null);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound c = list.getCompoundTagAt(i);
            int l = c.getInteger("l");
            if (l < 1 || l > LANES) continue;
            laneOffset[l] = new int[] { c.getInteger("dx"), c.getInteger("dy"), c.getInteger("dz") };
        }
    }

    int groupStart(int g) {
        return g == 0 ? 0 : groupEnd[g - 1];
    }

    int groupStop(int g) {
        return g == GROUPS - 1 ? SLOTS : groupEnd[g];
    }

    static int order(int slot) {
        return slot % COLS * ROWS + slot / COLS;
    }

    static int slotAt(int order) {
        return order % ROWS * COLS + order / ROWS;
    }

    int groupOf(int slot) {
        int g = 0, o = order(slot);
        while (g < GROUPS - 1 && o >= groupEnd[g]) g++;
        return g;
    }

    static int[] sanitizeGroups(int[] src) {
        int[] e = Arrays.copyOf(src, GROUPS - 1);
        for (int i = 0; i < e.length; i++) e[i] = Math.max(0, Math.min(SLOTS, e[i]));
        Arrays.sort(e);
        return e;
    }

    static int packGroups(int[] e) {
        return e[0] | e[1] << 6 | e[2] << 12;
    }

    static int[] unpackGroups(int v) {
        return sanitizeGroups(new int[] { v & 63, v >> 6 & 63, v >> 12 & 63 });
    }

    void setGroups(int[] e) {
        if (Arrays.equals(e, groupEnd)) return;
        System.arraycopy(e, 0, groupEnd, 0, groupEnd.length);
        analysisDirty = true;
        uiCacheTick = -1;
        markDirty();
    }

    private void readPatternLanes(int[] src) {
        for (int i = 0; i < patternLane.length; i++)
            patternLane[i] = i < src.length ? Math.max(0, Math.min(LANES, src[i])) : 0;
    }

    /** One analysis of all groups, advanced a time slice per tick; the patterns stay withdrawn while it spans ticks. */
    static final class AnalysisRun {

        final List<List<CyclePattern<SKey>>> pats = new ArrayList<>();
        int group;
        boolean second, redo, withdrawn;
        CycleAnalyzer.Job<SKey> job;
    }

    static final long SLICE_NANOS = 4_000_000L, MIN_SLICE_NANOS = 500_000L, STEP_WORK = 50_000L;
    static long sliceTick = Long.MIN_VALUE, sliceLeft;
    AnalysisRun analysisRun;

    /** What is left of this server tick's analysis time, shared by all cyclic hatches. */
    private static long analysisSlice() {
        MinecraftServer srv = MinecraftServer.getServer();
        long tick = srv == null ? 0 : srv.getTickCounter();
        if (tick != sliceTick) {
            sliceTick = tick;
            double tt = RecipeFilterCRIB.getWorldTickTimeAll();
            sliceLeft = tt < 0.05 ? SLICE_NANOS
                : tt < 0.1 ? (long) (SLICE_NANOS - (tt - 0.05) / 0.05 * (SLICE_NANOS - MIN_SLICE_NANOS))
                    : MIN_SLICE_NANOS;
        }
        return Math.max(0, sliceLeft);
    }

    private void driveAnalysis() {
        AnalysisRun r = analysisRun;
        long budget = analysisSlice(), t0 = System.nanoTime(), used = 0;
        while (analysisRun == r && used < budget) {
            advanceAnalysis(r);
            used = System.nanoTime() - t0;
        }
        sliceLeft -= used;
        if (analysisRun == r && !r.withdrawn) withdraw(r);
    }

    private void withdraw(AnalysisRun r) {
        r.withdrawn = true;
        uiCacheTick = -1;
        if (published.length == 0) return;
        lookup.clear();
        published = new ICraftingPatternDetails[0];
        rebuildOffered();
        needPatternSync = true;
    }

    private boolean analysing() {
        return analysisRun != null && analysisRun.withdrawn;
    }

    private void advanceAnalysis(AnalysisRun r) {
        LinkedHashMap<SKey, CycSetting> gs = settings.get(r.group);
        if (r.job == null) r.job = new CycleAnalyzer.Job<>(r.pats.get(r.group), coreSettings(gs), cycMultiplier, LIMITS);
        CycleAnalysis<SKey> a;
        try {
            if (!r.job.step(STEP_WORK)) return;
            a = r.job.result();
        } catch (RuntimeException e) {
            MyMod.LOG.error("cyclic pattern analysis failed", e);
            a = CycleAnalyzer.analyze(new ArrayList<>(), coreSettings(gs), cycMultiplier, LIMITS);
        }
        r.job = null;
        if (autoSeed && raiseTargets(a, gs)) {
            if (!r.second) {
                r.second = true;
                return;
            }
            r.redo = true;
        }
        for (SKey k : a.getCyclic()) if (!gs.containsKey(k)) gs.put(k, new CycSetting());
        analysis[r.group] = a;
        List<SKey> cyc = a.getCyclic();
        if (cyc.size() > MAX_ROWS) hatchWarnings.get(r.group)
            .add(new CycleWarning<>(CycleWarning.Code.ROWS_HIDDEN, null, -1, cyc.size(), MAX_ROWS));
        r.second = false;
        if (++r.group < GROUPS) return;
        analysisRun = null;
        publish();
        if (r.redo) analysisDirty = true;
        needPatternSync = true;
        uiCacheTick = -1;
        markDirty();
    }

    void startAnalysis() {
        World w = getBaseMetaTileEntity().getWorld();
        Arrays.fill(memberRuns, null);
        AnalysisRun ar = new AnalysisRun();
        ar.withdrawn = analysisRun != null && analysisRun.withdrawn;
        for (int g = 0; g < GROUPS; g++) {
            List<CycleWarning<SKey>> hw = hatchWarnings.get(g);
            hw.clear();
            List<CyclePattern<SKey>> pats = new ArrayList<>();
            Set<SKey> consumed = new HashSet<>(), produced = new HashSet<>();
            for (int o = groupStart(g); o < groupStop(g); o++) {
                int s = slotAt(o);
                ItemStack st = pattern[s];
                analysedPattern[s] = st;
                if (st == null) continue;
                ICraftingPatternDetails d = null;
                try {
                    if (st.getItem() instanceof ICraftingPatternItem pi) d = pi.getPatternForItem(st, w);
                } catch (Exception ignored) {}
                if (d == null || d.isCraftable()) {
                    hw.add(new CycleWarning<>(CycleWarning.Code.INVALID_PATTERN, null, s, 0, 0));
                    continue;
                }
                if (d.canSubstitute()) {
                    hw.add(new CycleWarning<>(CycleWarning.Code.SUBSTITUTION, null, s, 0, 0));
                    continue;
                }
                List<CycleAmount<SKey>> in;
                List<CycleAmount<SKey>> out;
                List<IAEStack<?>> run = new ArrayList<>();
                try {
                    in = toAmounts(d.getCondensedAEInputs());
                    out = toAmounts(d.getCondensedAEOutputs());
                    IAEStack<?>[] raw = d.getAEInputs();
                    if (raw == null) raw = d.getCondensedAEInputs();
                    if (raw != null) for (IAEStack<?> x : raw) {
                        if (x != null && x.getStackSize() > 0) run.add(x.copy());
                    }
                } catch (RuntimeException e) {
                    hw.add(new CycleWarning<>(CycleWarning.Code.INVALID_PATTERN, null, s, 0, 0));
                    continue;
                }
                if (out.isEmpty() || run.isEmpty()) {
                    hw.add(new CycleWarning<>(CycleWarning.Code.INVALID_PATTERN, null, s, 0, 0));
                    continue;
                }
                BufferedDualInputHatch stager = patternLane[s] == 0 ? this : laneHatch(patternLane[s]);
                if (stager != null && stager.stageParts(toParts(run)) == null) {
                    hw.add(new CycleWarning<>(CycleWarning.Code.TOO_LARGE_TO_STAGE, null, s, 0, 0));
                    continue;
                }
                try {
                    pats.add(new CyclePattern<>(s, in, out));
                } catch (IllegalArgumentException e) {
                    hw.add(new CycleWarning<>(CycleWarning.Code.INVALID_PATTERN, null, s, 0, 0));
                    continue;
                }
                memberRuns[s] = run;
                for (CycleAmount<SKey> c : in) consumed.add(c.key);
                for (CycleAmount<SKey> c : out) produced.add(c.key);
            }
            LinkedHashMap<SKey, CycSetting> gs = settings.get(g);
            for (SKey k : consumed) if (produced.contains(k) && !gs.containsKey(k)) gs.put(k, new CycSetting());
            ar.pats.add(pats);
        }
        analysisRun = ar;
        analysisDirty = false;
        uiCacheTick = -1;
    }

    private static Map<SKey, CycleItemSetting> coreSettings(Map<SKey, CycSetting> gs) {
        Map<SKey, CycleItemSetting> cs = new HashMap<>();
        for (Map.Entry<SKey, CycSetting> e : gs.entrySet()) cs.put(e.getKey(), e.getValue().toCore());
        return cs;
    }

    /** Raises each seed target to one craft's need times the auto seed multiplier of the balancing's kind. */
    private boolean raiseTargets(CycleAnalysis<SKey> a, Map<SKey, CycSetting> gs) {
        boolean changed = false;
        outer: for (CycleBalancing<SKey> b : a.getBalancings()) {
            if (b.getNetInputs()
                .isEmpty()) continue;
            for (CycleAmount<SKey> c : b.getSeedNeed()) if (c.amount == Long.MAX_VALUE) continue outer;
            long mult = Math.max(1, runsChance(b) ? autoSeedChanceMult : autoSeedMult);
            for (CycleAmount<SKey> c : b.getSeedNeed()) {
                CycSetting s = gs.get(c.key);
                long want;
                try {
                    want = Math.multiplyExact(c.amount, mult);
                } catch (ArithmeticException e) {
                    continue;
                }
                if (s == null || s.target >= want) continue;
                s.target = want;
                changed = true;
            }
        }
        return changed;
    }

    private boolean runsChance(CycleBalancing<SKey> b) {
        int[] ids = b.getPatternIds();
        long[] runs = b.getRuns();
        for (int j = 0; j < ids.length; j++) if (runs[j] > 0 && ids[j] >= 0
            && ids[j] < analysedPattern.length
            && ChanceMark.isMarked(analysedPattern[ids[j]])) return true;
        return false;
    }

    void publish() {
        lookup.clear();
        List<ICraftingPatternDetails> list = new ArrayList<>();
        World w = getBaseMetaTileEntity().getWorld();
        for (int g = 0; g < GROUPS; g++) {
            if (analysis[g] == null) continue;
            for (CycleBalancing<SKey> bal : analysis[g].getPublishable()) {
                ItemStack ps = buildCyclicPatternStack(g, bal);
                if (ps == null) continue;
                ICraftingPatternDetails det = null;
                try {
                    det = ((ICraftingPatternItem) ps.getItem()).getPatternForItem(ps, w);
                } catch (Exception ignored) {}
                if (det == null || lookup.containsKey(det)) continue;
                int[] ids = bal.getPatternIds();
                long[] runs = bal.getRuns();
                @SuppressWarnings("unchecked")
                List<IAEStack<?>>[] mr = new List[ids.length];
                int[] lanes = new int[ids.length];
                ItemStack[] mp = new ItemStack[ids.length];
                boolean ok = true;
                for (int j = 0; j < ids.length; j++) {
                    mr[j] = ids[j] >= 0 && ids[j] < memberRuns.length ? memberRuns[ids[j]] : null;
                    if (mr[j] == null) ok = false;
                    else {
                        lanes[j] = patternLane[ids[j]];
                        if (ChanceMark.isMarked(analysedPattern[ids[j]])) mp[j] = analysedPattern[ids[j]];
                    }
                    if (mr[j] != null && runs[j] > 0 && !laneUsable(lanes[j])) {
                        ok = false;
                        CycleWarning<SKey> lw = new CycleWarning<>(CycleWarning.Code.LANE_UNBOUND, null, ids[j], lanes[j], 0);
                        if (!hatchWarnings.get(g)
                            .contains(lw))
                            hatchWarnings.get(g)
                                .add(lw);
                    }
                }
                if (!ok) continue;
                lookup.put(det, new BalRef(g, bal, mr, lanes, mp));
                list.add(det);
            }
        }
        published = list.toArray(new ICraftingPatternDetails[0]);
        rebuildOffered();
    }

    void rebuildOffered() {
        ICraftingPatternDetails[] o = new ICraftingPatternDetails[published.length];
        for (int i = 0; i < published.length; i++)
            o[i] = cycMultiplier > 1 ? new DA(published[i], cycMultiplier) : published[i];
        offered = o;
    }

    private static String join(long[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(a[i]);
        }
        return sb.toString();
    }

    private static String join(int[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(a[i]);
        }
        return sb.toString();
    }

    ItemStack buildCyclicPatternStack(int g, CycleBalancing<SKey> b) {
        List<IAEStack<?>> inputs = new ArrayList<>();
        for (CycleAmount<SKey> c : b.getNetInputs()) inputs.add(c.key.toAE(c.amount));
        List<IAEStack<?>> outputs = new ArrayList<>();
        for (CycleAmount<SKey> c : b.getNetOutputs()) outputs.add(c.key.toAE(c.amount));
        if (inputs.isEmpty() || outputs.isEmpty()) return null;
        ItemStack patternStack = new ItemStack(ItemAndBlockHolder.PATTERN);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList tag2;
        tag.setTag("Inputs", tag2 = FluidPatternDetails.writeStackArray(inputs.toArray(new IAEStack<?>[0])));
        tag.setTag("in", tag2.copy());
        tag.setTag("Outputs", tag2 = FluidPatternDetails.writeStackArray(outputs.toArray(new IAEStack<?>[0])));
        tag.setTag("out", tag2.copy());
        tag.setInteger("combine", 0);
        tag.setBoolean("beSubstitute", false);
        NBTTagCompound c = new NBTTagCompound();
        c.setInteger("g", g);
        c.setString("x", join(b.getRuns()));
        c.setString("s", join(b.getPatternIds()));
        tag.setTag("phCyclic", c);
        patternStack.setTagCompound(tag);
        return patternStack;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper h) {
        if (!isActive()) return;
        for (ICraftingPatternDetails d : offered) h.addCraftingOption(this, d);
    }

    @Override
    public boolean allowsPatternOptimization() {
        return false;
    }

    @Override
    public void optimize(ItemStackMap<Pair<Object, Integer>> m) {}

    @Override
    public void blacklist(ItemStackSet black) {
        for (ICraftingPatternDetails d : published) black.add(d.getPattern());
        for (ICraftingPatternDetails d : offered) black.add(d.getPattern());
    }

    void setCycMultiplier(long v) {
        int nv = (int) Math.max(1, Math.min(Integer.MAX_VALUE, v));
        if (nv == cycMultiplier) return;
        cycMultiplier = nv;
        analysisDirty = true;
        markDirty();
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting table) {
        if (!isActive() && !skipActiveCheck) return false;
        if (!getBaseMetaTileEntity().isServerSide()) return false;
        BalRef ref = lookup.get(RecipeFilterCRIB.basePattern(details));
        if (ref == null) return false;
        if (!doses.isEmpty() || !isInputEmpty()) return false;
        long k = copiesFromTable(table, ref.bal.getNetInputs());
        if (k <= 0) return false;
        List<CycleAmount<SKey>> seedList = ref.bal.getSeeds();
        long[] need = new long[seedList.size()];
        SeedState[] states = new SeedState[seedList.size()];
        List<Dose> add = new ArrayList<>();
        try {
            for (int i = 0; i < need.length; i++) {
                CycleAmount<SKey> sd = seedList.get(i);
                need[i] = Math.multiplyExact(sd.amount, k);
                SeedState st = seeds.get(sd.key);
                if (st == null || st.stock < need[i]) return false;
                Math.addExact(st.owed, need[i]);
                states[i] = st;
            }
            long[] runs = ref.bal.getRuns();
            for (int j = 0; j < runs.length; j++) {
                if (runs[j] <= 0 || ref.memberLane[j] == 0) continue;
                BufferedDualInputHatch t = laneHatch(ref.memberLane[j]);
                if (t == null || t.stageParts(toParts(runFor(t, ref.memberRun[j]))) == null) return false;
            }
            for (int j = 0; j < runs.length; j++) {
                if (runs[j] > 0) add.add(
                    new Dose(ref.memberRun[j], Math.multiplyExact(runs[j], k), ref.memberLane[j], ref.memberPattern[j]));
            }
        } catch (ArithmeticException e) {
            return false;
        }
        for (int i = 0; i < need.length; i++) {
            states[i].stock -= need[i];
            states[i].owed += need[i];
        }
        doses.addAll(add);
        recountOwed();
        markDirty();
        registerInterceptor();
        drainDoses();
        return true;
    }

    @Override
    public int[] pushPatternMulti(ICraftingPatternDetails d, InventoryCrafting t, int maxTodo) {
        return new int[] { 0 };
    }

    @Override
    public boolean isBusy() {
        return !isInputEmpty() || !doses.isEmpty();
    }

    long copiesFromTable(InventoryCrafting table, List<CycleAmount<SKey>> base) {
        if (base.isEmpty()) return 0;
        try {
            Map<SKey, Long> got = new HashMap<>();
            for (int slot = 0; slot < table.getSizeInventory(); slot++) {
                ItemStack view = table.getStackInSlot(slot);
                if (view == null) continue;
                IAEStack<?> ae = table instanceof MEInventoryCrafting me ? me.getAEStackInSlot(slot) : null;
                long amt = PushStaging.realSize(table, slot, view);
                SKey key;
                if (ae instanceof IAEFluidStack) {
                    key = SKey.of(ae);
                } else if (view.getItem() instanceof ItemFluidPacket) {
                    FluidStack fs = ItemFluidPacket.getFluidStack(view);
                    if (fs == null) return 0;
                    key = SKey.of(AEFluidStack.create(fs));
                } else if (ae instanceof IAEItemStack) {
                    key = SKey.of(ae);
                } else {
                    AEItemStack is = AEItemStack.create(view);
                    if (is == null) return 0;
                    key = SKey.of(is);
                }
                if (isCircuit(key.st)) continue;
                got.merge(key, amt, Math::addExact);
            }
            if (got.size() != base.size()) return 0;
            CycleAmount<SKey> b0 = base.get(0);
            Long g0 = got.get(b0.key);
            if (g0 == null) return 0;
            long k = g0 / b0.amount;
            if (k < 1) return 0;
            for (CycleAmount<SKey> b : base) {
                Long g = got.get(b.key);
                if (g == null || g != Math.multiplyExact(b.amount, k)) return 0;
            }
            return k;
        } catch (ArithmeticException e) {
            return 0;
        }
    }

    /** Hands queued doses to their lane's hatch; a lane that cannot take more keeps its doses in order. */
    void drainDoses() {
        if (doses.isEmpty()) return;
        Set<Integer> blocked = new HashSet<>();
        boolean changed = false;
        for (Iterator<Dose> it = doses.iterator(); it.hasNext();) {
            Dose d = it.next();
            if (blocked.contains(d.lane)) continue;
            BufferedDualInputHatch t = d.lane == 0 ? this : laneHatch(d.lane);
            long taken = 0;
            if (t != null) {
                List<IAEStack<?>> run = runFor(t, d.run);
                taken = run.isEmpty() ? d.runs : t.takeRuns(toParts(run), d.runs, doseDetails(d));
            }
            if (taken < 0) {
                MyMod.LOG.warn("cyclic dose does not fit the input slots of lane " + d.lane + ", refunded");
                it.remove();
                refundDose(d);
                changed = true;
                continue;
            }
            if (taken >= d.runs) {
                it.remove();
                changed = true;
                continue;
            }
            if (taken > 0) {
                d.runs -= taken;
                changed = true;
            }
            blocked.add(d.lane);
        }
        if (changed) markDirty();
    }

    @Override
    void refundAll() throws Exception {
        if (!doses.isEmpty()) {
            List<Dose> all = new ArrayList<>(doses);
            doses.clear();
            for (Dose d : all) refundDose(d);
            markDirty();
        }
        super.refundAll();
    }

    private static long doseAmount(IAEStack<?> s, long runs) {
        try {
            return Math.multiplyExact(s.getStackSize(), runs);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    private void addStock(SKey key, long n) {
        if (n <= 0) return;
        SeedState st = seeds.computeIfAbsent(key, x -> new SeedState());
        if (st.stock > Long.MAX_VALUE - n) {
            MyMod.LOG.warn("cyclic seed stock overflow, lost: " + key + " x" + (n - (Long.MAX_VALUE - st.stock)));
            st.stock = Long.MAX_VALUE;
        } else st.stock += n;
    }

    static Pair<SKey, Long> seedOf(ItemStack st) {
        if (st == null || st.stackSize <= 0) return null;
        if (st.getItem() instanceof ItemFluidPacket) {
            if (ItemFluidPacket.isDisplay(st)) return null;
            IAEFluidStack f = ItemFluidPacket.getFluidAEStack(st);
            if (f == null || f.getStackSize() <= 0) return null;
            try {
                return Pair.of(SKey.of(f), Math.multiplyExact(f.getStackSize(), (long) st.stackSize));
            } catch (ArithmeticException e) {
                return null;
            }
        }
        FluidStack drop = ItemFluidDrop.getFluidStack(st);
        if (drop != null) {
            IAEFluidStack f = AEFluidStack.create(drop);
            return f == null ? null : Pair.of(SKey.of(f), (long) st.stackSize);
        }
        IAEItemStack i = AEItemStack.create(st);
        return i == null ? null : Pair.of(SKey.of(i), (long) st.stackSize);
    }

    private boolean rowIsFluid(int g, int r) {
        if (isServer()) {
            SKey k = rowKey(g, r);
            return k != null && k.isFluid();
        }
        return cIcon[g][r] != null && GTUtility.getFluidFromDisplayStack(cIcon[g][r]) != null;
    }

    private SKey seedSlotKey(int g, int r) {
        if (isServer()) return rowKey(g, r);
        ItemStack ic = cIcon[g][r];
        if (ic == null || GTUtility.getFluidFromDisplayStack(ic) != null) return null;
        IAEItemStack a = AEItemStack.create(ic);
        return a == null ? null : SKey.of(a);
    }

    void acceptSeed(ItemStack st) {
        if (!isServer()) return;
        Pair<SKey, Long> s = seedOf(st);
        if (s == null) return;
        stockChanged(s.getKey(), s.getValue());
    }

    private void stockChanged(SKey key, long n) {
        addStock(key, n);
        uiCacheTick = -1;
        markDirty();
    }

    private boolean fluidMatchesRow(int g, int r, FluidStack fs) {
        SKey k = rowKey(g, r);
        IAEFluidStack a = fs == null ? null : AEFluidStack.create(fs);
        return k != null && k.isFluid() && a != null && k.equals(SKey.of(a));
    }

    private boolean clickFluidItem(int g, int r, boolean all, PanelSyncManager sm) {
        ItemStack held = sm.getCursorItem();
        if (held == null || !(held.getItem() instanceof ItemFluidPacket) && ItemFluidDrop.getFluidStack(held) == null)
            return false;
        ItemStack part = held.copy();
        part.stackSize = all ? held.stackSize : 1;
        Pair<SKey, Long> s = seedOf(part);
        SKey k = rowKey(g, r);
        if (s == null || k == null || !k.equals(s.getKey())) return true;
        stockChanged(k, s.getValue());
        held.stackSize -= part.stackSize;
        sm.setCursorItem(held.stackSize > 0 ? held : null);
        return true;
    }

    final class SeedTank implements IFluidTank {

        private final int g, r;

        SeedTank(int g, int r) {
            this.g = g;
            this.r = r;
        }

        @Override
        public FluidStack getFluid() {
            return null;
        }

        @Override
        public int getFluidAmount() {
            return 0;
        }

        @Override
        public int getCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(null, Integer.MAX_VALUE);
        }

        @Override
        public int fill(FluidStack fs, boolean doFill) {
            if (fs == null || fs.amount <= 0) return 0;
            if (doFill && isServer()) {
                SKey k = rowKey(g, r);
                if (k == null || !k.isFluid() || ((IAEFluidStack) k.st).getFluid() != fs.getFluid()) {
                    IAEFluidStack a = AEFluidStack.create(fs);
                    k = a == null ? null : SKey.of(a);
                }
                if (k != null) stockChanged(k, fs.amount);
            }
            return fs.amount;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }
    }

    final class SeedInput implements IItemHandlerModifiable {

        @Override
        public int getSlots() {
            return GROUPS * MAX_ROWS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return null;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (stack != null) acceptSeed(stack);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack == null || !isItemValid(slot, stack)) return stack;
            if (!simulate) acceptSeed(stack);
            return null;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return null;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            SKey k = seedSlotKey(slot / MAX_ROWS, slot % MAX_ROWS);
            Pair<SKey, Long> s = seedOf(stack);
            return k != null && !k.isFluid() && s != null && k.equals(s.getKey());
        }
    }

    final SeedInput seedInput = new SeedInput();

    private void insertOrKeep(SKey key, long amount) {
        if (amount <= 0) return;
        long rest = amount;
        selfIo = true;
        try {
            IStorageGrid sg = getProxy().getStorage();
            IAEStack<?> r;
            if (key.isFluid()) r = Platform.poweredInsert(
                getProxy().getEnergy(),
                sg.getFluidInventory(),
                (IAEFluidStack) key.toAE(amount),
                seedSource());
            else r = Platform.poweredInsert(
                getProxy().getEnergy(),
                sg.getItemInventory(),
                (IAEItemStack) key.toAE(amount),
                seedSource());
            rest = r == null ? 0 : Math.max(0, r.getStackSize());
        } catch (GridAccessException ignored) {} finally {
            selfIo = false;
        }
        addStock(key, rest);
    }

    void refundDose(Dose d) {
        for (IAEStack<?> s : d.run) {
            if (s == null || isCircuit(s)) continue;
            long amount = doseAmount(s, d.runs);
            SKey key = SKey.of(s);
            SeedState st = seeds.get(key);
            if (st != null && st.owed > 0) {
                long back = Math.min(st.owed, amount);
                st.owed -= back;
                addStock(key, back);
                amount -= back;
            }
            insertOrKeep(key, amount);
        }
        recountOwed();
        uiCacheTick = -1;
        markDirty();
    }

    final class SeedInterceptor implements IStorageInterceptor {

        @Override
        public boolean canAccept(IAEStack<?> s) {
            if (removed || !hasOwed || s == null) return false;
            SeedState st = seeds.get(SKey.probe(s));
            return st != null && st.owed > 0;
        }

        @Override
        public IAEStack<?> injectItems(IAEStack<?> input, Actionable type, BaseActionSource src) {
            if (input == null) return null;
            if (removed || selfIo || (src != null && src == seedSource)) return input;
            SeedState st = seeds.get(SKey.probe(input));
            if (st == null || st.owed <= 0) return input;
            input = feedOrders(input, type, src);
            if (input == null) return null;
            long take = Math.min(Math.min(st.owed, input.getStackSize()), Long.MAX_VALUE - st.stock);
            if (take <= 0) return input;
            if (type == Actionable.MODULATE) {
                st.owed -= take;
                st.stock += take;
                recountOwed();
                markDirty();
            }
            long rest = input.getStackSize() - take;
            if (rest <= 0) return null;
            IAEStack<?> r = input.copy();
            r.setStackSize(rest);
            return r;
        }

        @Override
        public boolean shouldRemoveInterceptor(IAEStack<?> s) {
            return removed || !hasOwed;
        }
    }

    /** Hands what the chance controllers' orders wait for to their CPUs, ahead of the seeds owed. */
    IAEStack<?> feedOrders(IAEStack<?> input, Actionable type, BaseActionSource src) {
        List<TileChanceController> controllers = new ArrayList<>();
        ICraftingGrid cg;
        try {
            IGrid grid = getProxy().getGrid();
            for (IGridNode n : grid.getMachines(TileChanceController.class))
                if (n.getMachine() instanceof TileChanceController t) controllers.add(t);
            if (controllers.isEmpty()) return input;
            cg = grid.getCache(ICraftingGrid.class);
        } catch (GridAccessException e) {
            return input;
        }
        for (ICraftingCPU cpu : cg.getCpus()) {
            if (!(cpu instanceof CraftingCPUCluster cl) || !cl.isBusy() || !cl.canAccept(input)) continue;
            ICraftingLink link = cl.getLastCraftingLink();
            boolean ours = false;
            for (TileChanceController t : controllers) if (t.owns(link)) {
                ours = true;
                break;
            }
            if (!ours) continue;
            input = cl.injectItems(input, type, src);
            if (input == null || input.getStackSize() <= 0) return null;
        }
        return input;
    }

    void recountOwed() {
        boolean any = false;
        for (SeedState s : seeds.values()) if (s.owed > 0) {
            any = true;
            break;
        }
        hasOwed = any;
    }

    final List<WeakReference<NetworkMonitor<?>>> monitors = new ArrayList<>();

    private NetworkMonitor<?>[] currentMonitors() {
        NetworkMonitor<?>[] ret = new NetworkMonitor<?>[2];
        try {
            IStorageGrid sg = getProxy().getStorage();
            if (sg.getItemInventory() instanceof NetworkMonitor<?> nm) ret[0] = nm;
            if (sg.getFluidInventory() instanceof NetworkMonitor<?> nm2) ret[1] = nm2;
        } catch (GridAccessException ignored) {}
        return ret;
    }

    private boolean isStored(NetworkMonitor<?> m) {
        for (WeakReference<NetworkMonitor<?>> r : monitors) if (r.get() == m) return true;
        return false;
    }

    /** Drops the interceptor from monitors of a previous grid; adds it to the current ones while seeds are owed. */
    void registerInterceptor() {
        if (removed) return;
        NetworkMonitor<?>[] cur = currentMonitors();
        Iterator<WeakReference<NetworkMonitor<?>>> it = monitors.iterator();
        while (it.hasNext()) {
            NetworkMonitor<?> m = it.next()
                .get();
            if (m == null) it.remove();
            else if (m != cur[0] && m != cur[1]) {
                m.removeStorageInterceptor(interceptor);
                it.remove();
            }
        }
        if (!hasOwed) return;
        for (NetworkMonitor<?> m : cur) {
            if (m == null) continue;
            m.addStorageInterceptor(interceptor);
            if (!isStored(m)) monitors.add(new WeakReference<>(m));
        }
    }

    void unregisterInterceptor() {
        for (WeakReference<NetworkMonitor<?>> r : monitors) {
            NetworkMonitor<?> m = r.get();
            if (m != null) m.removeStorageInterceptor(interceptor);
        }
        monitors.clear();
        for (NetworkMonitor<?> m : currentMonitors()) if (m != null) m.removeStorageInterceptor(interceptor);
    }

    MachineSource seedSource() {
        if (seedSource == null) seedSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
        return seedSource;
    }

    long effectiveTarget(SKey k) {
        long sum = 0;
        for (int g = 0; g < GROUPS; g++) {
            if (analysis[g] == null || !analysis[g].getCyclic()
                .contains(k)) continue;
            CycSetting s = settings.get(g)
                .get(k);
            if (s == null) continue;
            long t = s.target;
            sum = sum > Long.MAX_VALUE - t ? Long.MAX_VALUE : sum + t;
        }
        return sum;
    }

    Set<SKey> cyclicKeys() {
        Set<SKey> ret = new LinkedHashSet<>();
        for (int g = 0; g < GROUPS; g++) if (analysis[g] != null) ret.addAll(analysis[g].getCyclic());
        return ret;
    }

    void syncSeeds() {
        if (!getBaseMetaTileEntity().isServerSide() || !getProxy().isActive()) return;
        Set<SKey> keys = new LinkedHashSet<>(seeds.keySet());
        keys.addAll(cyclicKeys());
        for (SKey k : keys) {
            SeedState st = seeds.get(k);
            long stock = st == null ? 0 : st.stock, owed = st == null ? 0 : st.owed;
            long have = stock > Long.MAX_VALUE - owed ? Long.MAX_VALUE : stock + owed;
            long want = effectiveTarget(k);
            if (have < want) pullSeed(k, want - have);
            else if (have > want && stock > 0) {
                long n = Math.min(stock, have - want);
                st.stock -= n;
                insertOrKeep(k, n);
            }
        }
        uiCacheTick = -1;
        markDirty();
    }

    private void pullSeed(SKey k, long n) {
        selfIo = true;
        try {
            IStorageGrid sg = getProxy().getStorage();
            IAEStack<?> got;
            if (k.isFluid()) got = Platform.poweredExtraction(
                getProxy().getEnergy(),
                sg.getFluidInventory(),
                (IAEFluidStack) k.toAE(n),
                seedSource());
            else got = Platform.poweredExtraction(
                getProxy().getEnergy(),
                sg.getItemInventory(),
                (IAEItemStack) k.toAE(n),
                seedSource());
            if (got != null && got.getStackSize() > 0) addStock(k, got.getStackSize());
        } catch (GridAccessException ignored) {} finally {
            selfIo = false;
        }
    }

    void returnSeeds() {
        if (!getBaseMetaTileEntity().isServerSide()) return;
        for (Map.Entry<SKey, SeedState> e : seeds.entrySet()) {
            SKey k = e.getKey();
            SeedState st = e.getValue();
            if (st.stock <= 0) continue;
            selfIo = true;
            try {
                IStorageGrid sg = getProxy().getStorage();
                IAEStack<?> rest;
                if (k.isFluid()) rest = Platform.poweredInsert(
                    getProxy().getEnergy(),
                    sg.getFluidInventory(),
                    (IAEFluidStack) k.toAE(st.stock),
                    seedSource());
                else rest = Platform.poweredInsert(
                    getProxy().getEnergy(),
                    sg.getItemInventory(),
                    (IAEItemStack) k.toAE(st.stock),
                    seedSource());
                st.stock = rest == null ? 0 : Math.max(0, rest.getStackSize());
            } catch (GridAccessException ignored) {} finally {
                selfIo = false;
            }
        }
        uiCacheTick = -1;
        markDirty();
    }

    void forgetOwed() {
        for (SeedState s : seeds.values()) s.owed = 0;
        recountOwed();
        uiCacheTick = -1;
        markDirty();
    }

    @Override
    public void onBlockDestroyed() {
        removed = true;
        unregisterInterceptor();
        for (Dose d : doses) {
            for (IAEStack<?> s : d.run) {
                if (s == null || isCircuit(s)) continue;
                addStock(SKey.of(s), doseAmount(s, d.runs));
            }
        }
        doses.clear();
        try {
            returnSeeds();
        } catch (Exception e) {
            MyMod.LOG.warn("cyclic seed return failed", e);
        }
        IGregTechTileEntity te = getBaseMetaTileEntity();
        World w = te.getWorld();
        for (Map.Entry<SKey, SeedState> e : seeds.entrySet()) {
            SKey k = e.getKey();
            SeedState st = e.getValue();
            if (st.stock <= 0) continue;
            if (k.isFluid()) {
                ItemStack pkt = ItemFluidPacket.newStack((IAEFluidStack) k.toAE(st.stock));
                if (pkt != null) w.spawnEntityInWorld(
                    new EntityItem(w, te.getXCoord() + 0.5, te.getYCoord() + 0.5, te.getZCoord() + 0.5, pkt));
                else MyMod.LOG.warn("cyclic seed lost: " + k + " x" + st.stock);
                st.stock = 0;
                continue;
            }
            ItemStack template = ((IAEItemStack) k.st).getItemStack();
            int max = Math.max(1, template.getMaxStackSize());
            int entities = 0;
            while (st.stock > 0 && entities < 256) {
                int n = (int) Math.min(st.stock, max);
                ItemStack drop = template.copy();
                drop.stackSize = n;
                w.spawnEntityInWorld(
                    new EntityItem(w, te.getXCoord() + 0.5, te.getYCoord() + 0.5, te.getZCoord() + 0.5, drop));
                st.stock -= n;
                entities++;
            }
            if (st.stock > 0) {
                MyMod.LOG.warn("cyclic seed lost: " + k + " x" + st.stock);
                st.stock = 0;
            }
        }
        super.onBlockDestroyed();
    }

    @Override
    public void onRemoval() {
        removed = true;
        unregisterInterceptor();
        super.onRemoval();
    }

    @Override
    public void onUnload() {
        removed = true;
        unregisterInterceptor();
        super.onUnload();
    }

    @Override
    public void onFirstTick(IGregTechTileEntity te) {
        super.onFirstTick(te);
        analysisDirty = true;
        interceptorCheck = true;
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        interceptorCheck = true;
    }

    @Override
    public void onPostTick(IGregTechTileEntity te, long t) {
        if (te.isServerSide()) {
            for (int i = 0; i < 36; i++) if (pattern[i] != analysedPattern[i]) {
                analysisDirty = true;
                break;
            }
            if (t % 100 == 0) {
                int sig = computeLaneSignature();
                if (sig != laneSignature) {
                    laneSignature = sig;
                    analysisDirty = true;
                }
            }
            if (t % 20 == 0) refundStrandedDoses(t);
            if (analysisDirty && t >= nextAnalysisTick) {
                startAnalysis();
                nextAnalysisTick = t + DEBOUNCE;
            }
            if (analysisRun != null) driveAnalysis();
            drainDoses();
            if (t % 20 == 0 || interceptorCheck) {
                if (hasOwed || !monitors.isEmpty()) registerInterceptor();
                interceptorCheck = false;
            }
        }
        super.onPostTick(te, t);
    }

    private static NBTTagList writeSettings(List<LinkedHashMap<SKey, CycSetting>> settings) {
        NBTTagList list = new NBTTagList();
        for (int g = 0; g < settings.size(); g++) {
            for (Map.Entry<SKey, CycSetting> e : settings.get(g)
                .entrySet()) {
                NBTTagCompound c = new NBTTagCompound();
                c.setInteger("g", g);
                c.setTag(
                    "k",
                    e.getKey()
                        .toAE(1)
                        .toNBTGeneric());
                c.setLong("target", e.getValue().target);
                c.setBoolean("out", e.getValue().output);
                c.setInteger("tol", e.getValue().tol);
                list.appendTag(c);
            }
        }
        return list;
    }

    private void readSettings(NBTTagList list) {
        for (LinkedHashMap<SKey, CycSetting> m : settings) m.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound c = list.getCompoundTagAt(i);
            int g = c.getInteger("g");
            if (g < 0 || g >= GROUPS) continue;
            IAEStack<?> k = IAEStack.fromNBTGeneric(c.getCompoundTag("k"));
            if (k == null) continue;
            CycSetting s = new CycSetting();
            s.target = Math.max(0, c.getLong("target"));
            s.output = c.getBoolean("out");
            s.tol = Math.max(0, Math.min(100, c.getInteger("tol")));
            settings.get(g)
                .put(SKey.of(k), s);
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        NBTTagList sl = new NBTTagList();
        for (Map.Entry<SKey, SeedState> e : seeds.entrySet()) {
            NBTTagCompound c = new NBTTagCompound();
            c.setTag(
                "k",
                e.getKey()
                    .toAE(1)
                    .toNBTGeneric());
            c.setLong("stock", e.getValue().stock);
            c.setLong("owed", e.getValue().owed);
            sl.appendTag(c);
        }
        aNBT.setTag("cycSeeds", sl);
        aNBT.setTag("cycSettings", writeSettings(settings));
        aNBT.setInteger("cycMultiplier", cycMultiplier);
        aNBT.setBoolean("cycAutoSeed", autoSeed);
        aNBT.setInteger("cycAutoSeedMult", autoSeedMult);
        aNBT.setInteger("cycAutoSeedChanceMult", autoSeedChanceMult);
        aNBT.setIntArray("cycLanes", patternLane.clone());
        aNBT.setTag("cycLaneOffsets", writeLaneOffsets());
        aNBT.setIntArray("cycGroups", groupEnd.clone());
        NBTTagList dl = new NBTTagList();
        for (Dose d : doses) {
            NBTTagCompound c = new NBTTagCompound();
            c.setLong("runs", d.runs);
            c.setInteger("lane", d.lane);
            NBTTagList in = new NBTTagList();
            for (IAEStack<?> s : d.run) in.appendTag(s.toNBTGeneric());
            c.setTag("in", in);
            if (d.pattern != null) c.setTag("pattern", d.pattern.writeToNBT(new NBTTagCompound()));
            dl.appendTag(c);
        }
        aNBT.setTag("cycDoses", dl);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x")) {
            seeds.clear();
            NBTTagList sl = aNBT.getTagList("cycSeeds", 10);
            for (int i = 0; i < sl.tagCount(); i++) {
                NBTTagCompound c = sl.getCompoundTagAt(i);
                IAEStack<?> k = IAEStack.fromNBTGeneric(c.getCompoundTag("k"));
                if (k == null) continue;
                SeedState st = new SeedState();
                st.stock = Math.max(0, c.getLong("stock"));
                st.owed = Math.max(0, c.getLong("owed"));
                seeds.put(SKey.of(k), st);
            }
            readSettings(aNBT.getTagList("cycSettings", 10));
            cycMultiplier = Math.max(1, aNBT.getInteger("cycMultiplier"));
            autoSeed = !aNBT.hasKey("cycAutoSeed") || aNBT.getBoolean("cycAutoSeed");
            if (aNBT.hasKey("cycAutoSeedMult")) autoSeedMult = Math.max(1, aNBT.getInteger("cycAutoSeedMult"));
            if (aNBT.hasKey("cycAutoSeedChanceMult"))
                autoSeedChanceMult = Math.max(1, aNBT.getInteger("cycAutoSeedChanceMult"));
            readPatternLanes(aNBT.getIntArray("cycLanes"));
            readLaneOffsets(aNBT.getTagList("cycLaneOffsets", 10));
            int[] ge = aNBT.getIntArray("cycGroups");
            if (ge.length == GROUPS - 1) System.arraycopy(sanitizeGroups(ge), 0, groupEnd, 0, ge.length);
            doses.clear();
            NBTTagList dl = aNBT.getTagList("cycDoses", 10);
            for (int i = 0; i < dl.tagCount(); i++) {
                NBTTagCompound c = dl.getCompoundTagAt(i);
                long runs = c.getLong("runs");
                if (runs <= 0) continue;
                NBTTagList in = c.getTagList("in", 10);
                List<IAEStack<?>> run = new ArrayList<>();
                for (int j = 0; j < in.tagCount(); j++) {
                    IAEStack<?> s = IAEStack.fromNBTGeneric(in.getCompoundTagAt(j));
                    if (s != null) run.add(s);
                }
                if (run.isEmpty()) continue;
                ItemStack pattern = c.hasKey("pattern") ? ItemStack.loadItemStackFromNBT(c.getCompoundTag("pattern"))
                    : null;
                doses.add(new Dose(run, runs, Math.max(0, Math.min(LANES, c.getInteger("lane"))), pattern));
            }
            recountOwed();
            analysisDirty = true;
            interceptorCheck = true;
        }
        super.loadNBTData(aNBT);
    }

    /**
     * The settings carry the AE NBT of arbitrary items, so they travel as one compressed Base64 string: the Matter
     * Manipulator round-trips copied data through JSON, which re-types numbers and breaks numeric arrays and lists.
     */
    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = super.getCopiedData(player);
        NBTTagCompound c = new NBTTagCompound();
        c.setTag("settings", writeSettings(settings));
        c.setInteger("multiplier", cycMultiplier);
        c.setBoolean("autoSeed", autoSeed);
        c.setInteger("autoSeedMult", autoSeedMult);
        c.setInteger("autoSeedChanceMult", autoSeedChanceMult);
        c.setIntArray("lanes", patternLane.clone());
        c.setTag("laneOffsets", writeLaneOffsets());
        c.setIntArray("groups", groupEnd.clone());
        try {
            ret.setString(
                "cyc",
                Base64.getEncoder()
                    .encodeToString(CompressedStreamTools.compress(c)));
        } catch (IOException e) {
            MyMod.LOG.warn("could not copy cyclic pattern settings", e);
        }
        return ret;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (!super.pasteCopiedData(player, nbt)) return false;
        if (nbt.hasKey("cyc")) {
            try {
                NBTTagCompound c = CompressedStreamTools.func_152457_a(
                    Base64.getDecoder()
                        .decode(nbt.getString("cyc")),
                    new NBTSizeTracker(Long.MAX_VALUE));
                readSettings(c.getTagList("settings", 10));
                if (c.hasKey("multiplier")) setCycMultiplier(c.getInteger("multiplier"));
                if (c.hasKey("autoSeed")) autoSeed = c.getBoolean("autoSeed");
                if (c.hasKey("autoSeedMult")) autoSeedMult = Math.max(1, c.getInteger("autoSeedMult"));
                if (c.hasKey("autoSeedChanceMult"))
                    autoSeedChanceMult = Math.max(1, c.getInteger("autoSeedChanceMult"));
                int[] ge = c.getIntArray("groups");
                if (ge.length == GROUPS - 1) setGroups(sanitizeGroups(ge));
                if (doses.isEmpty()) {
                    if (c.hasKey("lanes")) readPatternLanes(c.getIntArray("lanes"));
                    if (c.hasKey("laneOffsets")) readLaneOffsets(c.getTagList("laneOffsets", 10));
                }
            } catch (IOException | IllegalArgumentException e) {
                MyMod.LOG.warn("could not paste cyclic pattern settings", e);
            }
        }
        analysisDirty = true;
        markDirty();
        return true;
    }

    // ===================== UI =====================

    final long[][] cTarget = new long[GROUPS][MAX_ROWS];
    final boolean[][] cOut = new boolean[GROUPS][MAX_ROWS];
    final int[][] cTol = new int[GROUPS][MAX_ROWS];

    {
        for (int g = 0; g < GROUPS; g++) {
            Arrays.fill(cOut[g], true);
            Arrays.fill(cTol[g], 5);
        }
    }
    final int[] cRows = new int[GROUPS];
    final String[][] cRowLine = new String[GROUPS][MAX_ROWS];
    final String[] cReport = new String[GROUPS];
    final String[] cSummary = new String[GROUPS];
    final ItemStack[][] cIcon = new ItemStack[GROUPS][MAX_ROWS];
    final int[] cLevel = new int[GROUPS];
    private final String[][] sRowLine = new String[GROUPS][MAX_ROWS];
    private final String[] sReport = new String[GROUPS];
    private final String[] sSummary = new String[GROUPS];
    private final ItemStack[][] sIcon = new ItemStack[GROUPS][MAX_ROWS];
    private final int[] sLevel = new int[GROUPS];

    static final int STRIPE_DARK = 0x50000000, STRIPE_LIGHT = 0x60FFFFFF;
    static final int[] LEVEL_COLORS = { 0xFF303030, 0xFF005400, 0xFF6A4200, 0xFF940000 };
    static final int PAGE_W = 18 * 4 + 6, PAGE_H = 18 * 9 + 6, GROUP_PAGE_Y = 21;

    private boolean isServer() {
        return getBaseMetaTileEntity() != null && getBaseMetaTileEntity().isServerSide();
    }

    private SKey rowKey(int g, int r) {
        if (analysis[g] == null) return null;
        List<SKey> cyc = analysis[g].getCyclic();
        return r < cyc.size() ? cyc.get(r) : null;
    }

    private CycSetting getSetting(int g, int r) {
        SKey k = rowKey(g, r);
        CycSetting s = k == null ? null : settings.get(g)
            .get(k);
        return s == null ? new CycSetting() : s;
    }

    private CycSetting editSetting(int g, int r) {
        SKey k = rowKey(g, r);
        if (k == null) return null;
        return settings.get(g)
            .computeIfAbsent(k, x -> new CycSetting());
    }

    void setTarget(int g, int r, long v) {
        CycSetting s = editSetting(g, r);
        if (s == null) return;
        s.target = Math.max(0, v);
        analysisDirty = true;
        markDirty();
    }

    void setOutput(int g, int r, boolean v) {
        CycSetting s = editSetting(g, r);
        if (s == null) return;
        s.output = v;
        analysisDirty = true;
        markDirty();
    }

    void setTol(int g, int r, int v) {
        CycSetting s = editSetting(g, r);
        if (s == null) return;
        s.tol = Math.max(0, Math.min(100, v));
        analysisDirty = true;
        markDirty();
    }

    int rowCount(int g) {
        return analysis[g] == null ? 0
            : Math.min(MAX_ROWS, analysis[g].getCyclic()
                .size());
    }

    private static String enc(String color, String key, Object... args) {
        StringBuilder sb = new StringBuilder(color).append('\u0001')
            .append(key);
        for (Object a : args) sb.append('\u0001')
            .append(a);
        return sb.toString();
    }

    static String decode(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] lines = s.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) sb.append('\n');
            String[] parts = lines[i].split("\u0001", -1);
            if (parts.length < 2) {
                sb.append(parts[0]);
                continue;
            }
            sb.append(parts[0])
                .append(
                    LangManager
                        .translateToLocalFormatted(parts[1], (Object[]) Arrays.copyOfRange(parts, 2, parts.length)));
        }
        return sb.toString();
    }

    private static String fmt(List<CycleAmount<SKey>> list, long mult) {
        StringBuilder sb = new StringBuilder();
        try {
            for (CycleAmount<SKey> c : list) {
                if (sb.length() > 0) sb.append(" + ");
                sb.append(Math.multiplyExact(c.amount, mult))
                    .append(' ')
                    .append(c.key.name());
            }
        } catch (ArithmeticException e) {
            return "overflow";
        }
        return sb.toString();
    }

    private static String color(CycleWarning.Severity s) {
        return s == CycleWarning.Severity.RED ? "§c" : "§e";
    }

    private static String warnLine(CycleWarning<SKey> w) {
        String key = "proghatches.cyclic.warn." + w.code.name()
            .toLowerCase(Locale.ROOT);
        String c = color(w.severity);
        String name = w.key == null ? "" : w.key.name();
        switch (w.code) {
            case INVALID_PATTERN:
            case SUBSTITUTION:
            case TOO_LARGE_TO_STAGE:
                return enc(c, key, order(w.patternId) + 1);
            case LANE_UNBOUND:
                return enc(c, key, order(w.patternId) + 1, w.a);
            case TOO_MANY_PATTERNS:
            case TOO_MANY_ITEMS:
            case ROWS_HIDDEN:
                return enc(c, key, w.a, w.b);
            case SEARCH_LIMIT:
                return enc(c, key, name, w.a);
            case NO_BALANCING:
            case NOT_MINIMAL:
            case NO_NET_INPUT:
            case NEED_OVERFLOW:
            case OVERFLOW:
                return enc(c, key, name);
            case SEED_OVER_TARGET:
            case STOCK_LOW:
                return enc(c, key, name, w.a, w.b);
            default:
                return enc(c, key);
        }
    }

    private String rowLine(int g, int r) {
        SKey k = rowKey(g, r);
        if (k == null) return "";
        SeedState st = seeds.get(k);
        long need = -1;
        for (CycleBalancing<SKey> b : analysis[g].getBalancings()) {
            for (CycleAmount<SKey> c : b.getSeedNeed()) if (c.key.equals(k)) need = Math.max(need, c.amount);
        }
        return enc(
            "",
            "proghatches.cyclic.row",
            k.name(),
            st == null ? 0 : st.stock,
            st == null ? 0 : st.owed,
            need < 0 ? "-" : String.valueOf(need));
    }

    private boolean groupEmpty(int g) {
        for (int o = groupStart(g); o < groupStop(g); o++) if (pattern[slotAt(o)] != null) return false;
        return true;
    }

    private String report(int g) {
        if (!groupEmpty(g) && analysing()) {
            List<String> lines = new ArrayList<>();
            lines.add(enc("§e", "proghatches.cyclic.busy"));
            for (CycleWarning<SKey> w : hatchWarnings.get(g)) lines.add(warnLine(w));
            return String.join("\n", lines);
        }
        if (groupEmpty(g) || analysis[g] == null) return enc("", "proghatches.cyclic.none");
        List<String> lines = new ArrayList<>();
        CycleAnalysis<SKey> a = analysis[g];
        for (CycleBalancing<SKey> b : a.getBalancings()) {
            lines.add(
                enc(
                    b.isPublishable() ? "§a" : "",
                    b.isPublishable() ? "proghatches.cyclic.balancing" : "proghatches.cyclic.balancing.unpublished",
                    fmt(b.getNetInputs(), cycMultiplier),
                    fmt(b.getNetOutputs(), cycMultiplier),
                    join(b.getRuns()),
                    fmt(b.getSeedNeed(), 1)));
            if (!b.getSurplus()
                .isEmpty())
                lines.add(enc("", "proghatches.cyclic.surplus", fmt(b.getSurplus(), cycMultiplier)));
        }
        for (CycleWarning<SKey> w : warnings(g)) lines.add(warnLine(w));
        return String.join("\n", lines);
    }

    private List<CycleWarning<SKey>> warnings(int g) {
        List<CycleWarning<SKey>> ret = new ArrayList<>(hatchWarnings.get(g));
        CycleAnalysis<SKey> a = analysis[g];
        if (a == null) return ret;
        ret.addAll(a.getWarnings());
        Set<SKey> low = new HashSet<>();
        for (CycleBalancing<SKey> b : a.getPublishable()) {
            for (CycleAmount<SKey> c : b.getSeedNeed()) {
                SeedState st = seeds.get(c.key);
                long stock = st == null ? 0 : st.stock;
                if (stock < c.amount && low.add(c.key))
                    ret.add(new CycleWarning<>(CycleWarning.Code.STOCK_LOW, c.key, -1, stock, c.amount));
            }
        }
        return ret;
    }

    private String summary(int g) {
        int pub = 0;
        for (BalRef r : lookup.values()) if (r.group == g) pub++;
        String first = enc("", "proghatches.cyclic.summary.published", pub);
        if (!groupEmpty(g) && analysing()) {
            sLevel[g] = 2;
            return first + "\n" + enc("§e", "proghatches.cyclic.status.busy");
        }
        if (groupEmpty(g) || analysis[g] == null) {
            sLevel[g] = 0;
            return first + "\n" + enc("", "proghatches.cyclic.status.empty");
        }
        int red = 0, yellow = 0;
        for (CycleWarning<SKey> w : warnings(g)) {
            if (w.severity == CycleWarning.Severity.RED) red++;
            else yellow++;
        }
        sLevel[g] = red > 0 ? 3 : yellow > 0 ? 2 : 1;
        if (red > 0) return first + "\n" + enc("§c", "proghatches.cyclic.status.red", red);
        if (yellow > 0) return first + "\n" + enc("§e", "proghatches.cyclic.status.yellow", yellow);
        return first + "\n" + enc("§a", "proghatches.cyclic.status.ok");
    }

    private ItemStack rowIcon(int g, int r) {
        SKey k = rowKey(g, r);
        if (k == null) return null;
        if (k.isFluid()) return GTUtility.getFluidDisplayStack(((IAEFluidStack) k.st).getFluidStack(), false);
        ItemStack s = ((IAEItemStack) k.st).getItemStack();
        s.stackSize = 1;
        return s;
    }

    private void refreshUiCache() {
        long now = getBaseMetaTileEntity().getWorld()
            .getTotalWorldTime();
        if (now == uiCacheTick) return;
        uiCacheTick = now;
        for (int g = 0; g < GROUPS; g++) {
            sReport[g] = report(g);
            sSummary[g] = summary(g);
            for (int r = 0; r < MAX_ROWS; r++) {
                sRowLine[g][r] = rowLine(g, r);
                sIcon[g][r] = rowIcon(g, r);
            }
        }
    }

    private ItemStack serverRowIcon(int g, int r) {
        refreshUiCache();
        return sIcon[g][r];
    }

    private String serverSummary(int g) {
        refreshUiCache();
        return sSummary[g];
    }

    private int serverLevel(int g) {
        refreshUiCache();
        return sLevel[g];
    }

    private String serverRowLine(int g, int r) {
        refreshUiCache();
        return sRowLine[g][r];
    }

    private String serverReport(int g) {
        refreshUiCache();
        return sReport[g];
    }

    @Override
    protected boolean showMultiplierPages() {
        return false;
    }

    @Override
    protected IDrawable[] patternSlotBackground(int slot) {
        return new IDrawable[] { GTGuiTextures.SLOT_ITEM_STANDARD, slotStripes(slot, STRIPE_DARK) };
    }

    private void drawSlotStripes(int slot, float x, float y, float w, float h, int color) {
        int g = groupOf(slot);
        drawStripes(g, x, y, w, h, g < 2 ? 4.5f : 4.2426405f, 1.5f, color);
    }

    private IDrawable slotStripes(int slot, int color) {
        return (context, x, y, w, h, theme) -> drawSlotStripes(slot, x, y, w, h, color);
    }

    static int stripeColorOn(int rgb) {
        return Color.getRed(rgb) * 299 + Color.getGreen(rgb) * 587 + Color.getBlue(rgb) * 114 > 128_000 ? STRIPE_DARK
            : STRIPE_LIGHT;
    }

    static IDrawable groupStripes(int g, float period, float thick, int color) {
        return (context, x, y, w, h, theme) -> drawStripes(g, x + 1, y + 1, w - 2, h - 2, period, thick, color);
    }

    static void drawStripes(int g, float x, float y, float w, float h, float period, float thick, int color) {
        float d = 0.70710677f, nx = g == 0 ? 0 : g == 1 ? 1 : d, ny = g == 0 ? 1 : g == 1 ? 0 : g == 2 ? d : -d;
        float[] sx = { x, x, x + w, x + w }, sy = { y, y + h, y + h, y };
        float lo = Float.MAX_VALUE, hi = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            lo = Math.min(lo, nx * sx[i] + ny * sy[i]);
            hi = Math.max(hi, nx * sx[i] + ny * sy[i]);
        }
        float mid = (lo + hi) / 2, half = (hi - lo) / 2 - thick / 4;
        float phase = 2 * (int) (half / period) + 1 > 2 * (int) (half / period + 0.5f) ? 0 : 0.5f;
        int k0 = (int) Math.floor((lo - mid) / period - phase), k1 = (int) Math.ceil((hi - mid) / period - phase);
        int r = Color.getRed(color), gr = Color.getGreen(color), b = Color.getBlue(color), a = Color.getAlpha(color);
        float[] px = new float[16], py = new float[16], qx = new float[16], qy = new float[16];
        com.cleanroommc.modularui.utils.Platform.setupDrawColor();
        com.cleanroommc.modularui.utils.Platform.startDrawing(
            com.cleanroommc.modularui.utils.Platform.DrawMode.TRIANGLES,
            com.cleanroommc.modularui.utils.Platform.VertexFormat.POS_COLOR,
            buf -> {
                for (int k = k0; k <= k1; k++) {
                    float c = mid + (k + phase) * period;
                    int n = clipHalfPlane(sx, sy, 4, nx, ny, c - thick / 2, qx, qy);
                    n = clipHalfPlane(qx, qy, n, -nx, -ny, -c - thick / 2, px, py);
                    for (int i = 1; i + 1 < n; i++) {
                        buf.pos(px[0], py[0], 0)
                            .color(r, gr, b, a)
                            .endVertex();
                        buf.pos(px[i], py[i], 0)
                            .color(r, gr, b, a)
                            .endVertex();
                        buf.pos(px[i + 1], py[i + 1], 0)
                            .color(r, gr, b, a)
                            .endVertex();
                    }
                }
            });
        com.cleanroommc.modularui.utils.Platform.endDrawColor();
    }

    private static int clipHalfPlane(float[] xs, float[] ys, int n, float nx, float ny, float c, float[] ox,
        float[] oy) {
        int m = 0;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            float di = nx * xs[i] + ny * ys[i] - c, dj = nx * xs[j] + ny * ys[j] - c;
            if (di >= 0) {
                ox[m] = xs[i];
                oy[m++] = ys[i];
            }
            if (di > 0 && dj < 0 || di < 0 && dj > 0) {
                float t = di / (di - dj);
                ox[m] = xs[i] + t * (xs[j] - xs[i]);
                oy[m++] = ys[i] + t * (ys[j] - ys[i]);
            }
        }
        return m;
    }

    private String groupTitle(int g) {
        return groupTitle(g, groupStart(g), groupStop(g));
    }

    private static String groupTitle(int g, int start, int stop) {
        return start == stop ? LangManager.translateToLocalFormatted("proghatches.cyclic.group.empty", g + 1)
            : LangManager.translateToLocalFormatted("proghatches.cyclic.group", g + 1, start + 1, stop);
    }

    private String[] summaryLines(int g) {
        String[] p = decode(cSummary[g]).split("\n", -1);
        return p.length >= 2 ? p : new String[] { p[0], "" };
    }

    private String groupHeader(int g) {
        return LangManager.translateToLocalFormatted("proghatches.cyclic.group.short", g + 1) + " "
            + summaryLines(g)[1].replaceAll("§.", "");
    }

    private int levelColor(int g) {
        return LEVEL_COLORS[Math.max(0, Math.min(LEVEL_COLORS.length - 1, cLevel[g]))];
    }

    private void groupTooltip(int g, RichTooltip t) {
        t.addLine(IKey.str(groupTitle(g)));
        for (String s : summaryLines(g)) if (!s.isEmpty()) t.addLine(IKey.str(s));
        String rep = decode(cReport[g]);
        if (!rep.isEmpty()) addLines(t, rep);
    }

    private static void addLines(RichTooltip t, String s) {
        for (String line : s.split("\n|\\\\n")) t.addLine(IKey.str(line));
    }

    private static void addLangLines(RichTooltip t, String key) {
        addLines(t, LangManager.translateToLocal(key));
    }

    @Override
    protected int addExtraPatternWindowPages(PanelSyncManager sm, PagedWidget<?> paged, ParentWidget<?> tabs,
        PagedWidget.Controller controller, int firstIndex) {
        for (int g = 0; g < GROUPS; g++) {
            final int gg = g;
            sm.syncValue(
                "cyc_sum_" + g,
                new StringSyncValue(() -> isServer() ? serverSummary(gg) : cSummary[gg], v -> cSummary[gg] = v));
            sm.syncValue(
                "cyc_lvl_" + g,
                new IntSyncValue(() -> isServer() ? serverLevel(gg) : cLevel[gg], v -> cLevel[gg] = v));
            sm.syncValue(
                "cyc_report_" + g,
                new StringSyncValue(() -> isServer() ? serverReport(gg) : cReport[gg], v -> cReport[gg] = v));
            sm.syncValue(
                "cyc_rows_" + g,
                new IntSyncValue(() -> isServer() ? rowCount(gg) : cRows[gg], v -> cRows[gg] = v));
        }
        tabs.child(
            new PageButton(firstIndex, controller).tab(GuiTextures.TAB_RIGHT, 0)
                .overlay(
                    GTGuiTextures.OVERLAY_BUTTON_CYCLIC.asIcon()
                        .size(16, 16))
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.tab.balancing")));
        paged.addPage(createBalancingPage(sm));
        tabs.child(
            new PageButton(firstIndex + 1, controller).tab(GuiTextures.TAB_RIGHT, 0)
                .overlay(
                    new ItemDrawable(ItemList.Conveyor_Module_LV.get(1)).asIcon()
                        .size(16, 16))
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.tab.lanes")));
        paged.addPage(createLanePage(sm));
        tabs.child(
            new PageButton(firstIndex + 2, controller).tab(GuiTextures.TAB_RIGHT, 0)
                .overlay(
                    new ItemDrawable(ItemList.Tool_DataStick.get(1)).asIcon()
                        .size(16, 16))
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.tab.bind")));
        paged.addPage(createBindPage(sm));
        tabs.child(
            new PageButton(firstIndex + 3, controller).tab(GuiTextures.TAB_RIGHT, 0)
                .overlay(
                    new ItemDrawable(GTOreDictUnificator.get(OrePrefixes.gearGt, Materials.Iron, 1)).asIcon()
                        .size(18, 18))
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.tab")));
        paged.addPage(createGeneralPage(sm));
        return 4;
    }

    String cLanes = "";
    final String[] cLaneInfo = new String[LANES + 1];
    final ItemStack[] cLaneIcon = new ItemStack[LANES + 1];
    final String[] cLaneWarn = new String[LANES + 1];
    static final int[] LANE_COLORS = { 0xFF000000, 0xFF0000AA, 0xFF00AA00, 0xFF00AAAA, 0xFFAA0000, 0xFFAA00AA,
        0xFFFFAA00, 0xFF5555FF, 0xFF55FF55 };
    static final int[] LANE_STATE_COLORS = { 0xFF303030, 0xFF005400, 0xFF6A4200, 0xFF940000 };
    static final String[] LANE_TIP_COLORS = { "", "§a", "§6", "§c" };
    static final String[] LANE_STATE_KEYS = { "unbound", "ok", "unloaded", "invalid" };
    static final int LANE_WARN_TICKS = 100;

    private String laneString() {
        StringBuilder sb = new StringBuilder(patternLane.length);
        for (int v : patternLane) sb.append((char) ('0' + v));
        return sb.toString();
    }

    private int clientLane(int slot) {
        return slot < cLanes.length() ? Math.max(0, Math.min(LANES, cLanes.charAt(slot) - '0')) : 0;
    }

    private String laneInfo(int lane) {
        int s = laneState(lane);
        if (s == LANE_UNBOUND) return "0";
        IGregTechTileEntity me = getBaseMetaTileEntity();
        int[] o = laneOffset[lane];
        return s + "," + (me.getXCoord() + o[0]) + "," + (me.getYCoord() + o[1]) + "," + (me.getZCoord() + o[2]);
    }

    private ItemStack laneIcon(int lane) {
        int s = laneState(lane);
        if (s == LANE_OK) {
            BufferedDualInputHatch t = laneHatch(lane);
            ItemStack ic = t.getMachineCraftingIcon();
            laneIconSeen[lane] = ic != null ? ic : t.getStackForm(1);
        } else if (s != LANE_UNLOADED) laneIconSeen[lane] = null;
        return laneIconSeen[lane];
    }

    private int clientLaneState(int lane) {
        String s = cLaneInfo[lane];
        if (s == null || s.isEmpty()) return LANE_UNBOUND;
        int v = s.charAt(0) - '0';
        return v >= 0 && v < LANE_STATE_KEYS.length ? v : LANE_UNBOUND;
    }

    private String laneShort(int lane) {
        return LangManager.translateToLocal("proghatches.cyclic.lane.short." + LANE_STATE_KEYS[clientLaneState(lane)]);
    }

    private String laneTargetName(int lane) {
        if (clientLaneState(lane) == LANE_UNBOUND) return null;
        String[] p = cLaneInfo[lane].split(",", -1);
        if (p.length < 4) return null;
        ItemStack ic = cLaneIcon[lane];
        return ic == null ? LangManager.translateToLocalFormatted("proghatches.cyclic.lane.pos", p[1], p[2], p[3])
            : LangManager.translateToLocalFormatted("proghatches.cyclic.lane.target", ic.getDisplayName(), p[1], p[2], p[3]);
    }

    private String laneTargetText(int lane) {
        if (lane == 0) return LangManager.translateToLocal("proghatches.cyclic.lane.self");
        int s = clientLaneState(lane);
        String name = s == LANE_OK ? laneTargetName(lane) : null;
        if (name != null) return name;
        return (s == LANE_UNLOADED ? "§6" : "§c") + laneShort(lane);
    }

    private void laneRowTooltip(int lane, RichTooltip t) {
        t.addLine(IKey.str(LangManager.translateToLocalFormatted("proghatches.cyclic.lane.title", lane)));
        int s = clientLaneState(lane);
        t.addLine(
            IKey.str(
                LANE_TIP_COLORS[s]
                    + LangManager.translateToLocal("proghatches.cyclic.lane.state." + LANE_STATE_KEYS[s])));
        String name = laneTargetName(lane);
        if (name != null) t.addLine(IKey.str(name));
        String w = cLaneWarn[lane];
        if (w != null && !w.isEmpty())
            t.addLine(IKey.str("§c" + LangManager.translateToLocal("proghatches.cyclic.lane.warn." + w)));
    }

    private void selfRowTooltip(RichTooltip t) {
        t.addLine(IKey.str(LangManager.translateToLocalFormatted("proghatches.cyclic.lane.title", 0)));
        t.addLine(IKey.str(LangManager.translateToLocal("proghatches.cyclic.lane.self.bind")));
    }

    private boolean showPattern() {
        return !isServer() && Interactable.hasShiftDown();
    }

    private void laneSlotTooltip(int slot, RichTooltip t) {
        if (showPattern()) {
            if (pattern[slot] != null) t.addFromItem(pattern[slot]);
            return;
        }
        int lane = clientLane(slot);
        t.addLine(IKey.str(LangManager.translateToLocalFormatted("proghatches.cyclic.lane.line", lane, laneTargetText(lane))));
        addLangLines(t, "proghatches.cyclic.lane.click");
    }

    void stepPatternLane(int slot, int dir) {
        if (slot < 0 || slot >= patternLane.length) return;
        setPatternLane(slot, Math.floorMod(patternLane[slot] + dir, LANES + 1));
    }

    static final UITexture SELF_ARROW = GTGuiTextures.PICTURE_ARROW_WHITE_DOWN.withColorOverride(0xFFFFFFFF);
    static final UITexture SELF_ARROW_SHADOW = GTGuiTextures.PICTURE_ARROW_WHITE_DOWN.withColorOverride(0xFF3F3F3F);
    static final IDrawable SELF_ICON = (context, x, y, w, h, theme) -> {
        int ah = Math.min(h, w * 16 / 10), aw = ah * 10 / 16;
        int ax = x + (w - aw) / 2, ay = y + (h - ah) / 2;
        SELF_ARROW_SHADOW.draw(context, ax + 1, ay + 1, aw, ah, theme);
        SELF_ARROW.draw(context, ax, ay, aw, ah, theme);
    };

    static IDrawable laneRing(int lane) {
        return (context, x, y, w, h, theme) -> {
            int c = LANE_COLORS[lane];
            GuiDraw.drawRect(x + 1, y + 1, w - 2, 1, c);
            GuiDraw.drawRect(x + 1, y + h - 2, w - 2, 1, c);
            GuiDraw.drawRect(x + 1, y + 2, 1, h - 4, c);
            GuiDraw.drawRect(x + w - 2, y + 2, 1, h - 4, c);
        };
    }

    private IDrawable laneSlotBackground(int slot) {
        return (context, x, y, w, h, theme) -> {
            int lane = clientLane(slot);
            if (showPattern() || lane == 0 && pattern[slot] == null) {
                drawSlotStripes(slot, x, y, w, h, STRIPE_DARK);
                return;
            }
            int c = LANE_COLORS[lane];
            GuiDraw.drawRect(x, y, w, h, 0xB0000000 | (c & 0xFFFFFF));
            GuiDraw.drawRect(x + 1, y + 1, w - 2, h - 2, c);
            drawSlotStripes(slot, x, y, w, h, stripeColorOn(c));
        };
    }

    private final class LaneSlot extends PatternSlot {

        private final int index;

        LaneSlot(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack getItemStackForRendering(ItemStack stack, boolean dragging) {
            if (showPattern()) return super.getItemStackForRendering(stack, dragging);
            if (stack == null || dragging) return stack;
            int lane = clientLane(index);
            if (lane == 0) return null;
            ItemStack icon = clientLaneState(lane) == LANE_UNBOUND ? null : cLaneIcon[lane];
            return icon != null ? icon : stack;
        }

        @Override
        protected void drawSlotAmountText(int amount, String format) {
            if (showPattern()) super.drawSlotAmountText(amount, format);
        }

        @Override
        public IDrawable getCurrentBackground(WidgetThemeEntry<?> widgetTheme) {
            return isSynced() && getSlot().getStack() != null ? getBackground()
                : super.getCurrentBackground(widgetTheme);
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            super.draw(context, widgetTheme);
            if (isSynced() && !showPattern() && getSlot().getStack() != null && clientLane(index) == 0)
                SELF_ICON.draw(context, 1, 1, 16, 16, widgetTheme.getTheme());
        }
    }

    private ParentWidget<?> createLanePage(PanelSyncManager sm) {
        ParentWidget<?> page = new ParentWidget<>().size(PAGE_W, PAGE_H);
        sm.syncValue("cyc_lanes", new StringSyncValue(() -> isServer() ? laneString() : cLanes, v -> cLanes = v));
        ItemStackHandler shown = new ItemStackHandler(pattern);
        for (int i = 0; i < 36; i++) {
            final int ii = i;
            int x = (i % 4) * 18 + 3, y = (i / 4) * 18 + 3;
            page.child(
                new LaneSlot(i).slot(new ModularSlot(shown, i).accessibility(false, false))
                    .background(GTGuiTextures.SLOT_ITEM_STANDARD, laneSlotBackground(i))
                    .pos(x, y));
            InteractionSyncHandler ih = new InteractionSyncHandler().setOnMousePressed(d -> {
                if (sm.isClient()) return;
                if (d.mouseButton == 2) setPatternLane(ii, 0);
                else if (d.mouseButton == 0 || d.mouseButton == 1) stepPatternLane(ii, d.mouseButton == 0 ? 1 : -1);
            })
                .setOnMouseScroll(d -> {
                    if (sm.isClient() || d.mouseButton == 0) return;
                    stepPatternLane(ii, d.mouseButton > 0 ? 1 : -1);
                });
            sm.syncValue("cyc_lane_" + i, ih);
            page.child(
                new ButtonWidget<>().syncHandler(ih)
                    .background(IDrawable.EMPTY)
                    .hoverBackground(new Rectangle().color(0x40FFFFFF))
                    .tooltipDynamic(t -> laneSlotTooltip(ii, t))
                    .tooltipAutoUpdate(true)
                    .pos(x, y)
                    .size(18, 18));
        }
        return page;
    }

    private ParentWidget<?> createBindPage(PanelSyncManager sm) {
        ParentWidget<?> page = new ParentWidget<>().size(PAGE_W, PAGE_H);
        page.child(
            IKey.str("0")
                .asWidget()
                .pos(3, 3 + 5)
                .size(7, 9));
        page.child(
            IKey.dynamic(() -> LangManager.translateToLocal("proghatches.cyclic.lane.self.short"))
                .asWidget()
                .color(LANE_STATE_COLORS[LANE_OK])
                .tooltipBuilder(this::selfRowTooltip)
                .pos(11, 3 + 5)
                .size(PAGE_W - 33, 9));
        page.child(
            IDrawable.of(
                GTGuiTextures.SLOT_ITEM_STANDARD,
                laneRing(0),
                SELF_ICON.asIcon()
                    .size(14, 13))
                .asWidget()
                .tooltipBuilder(this::selfRowTooltip)
                .pos(PAGE_W - 21, 3)
                .size(18, 18));
        String[] warn = new String[LANES + 1];
        long[] warnUntil = new long[LANES + 1];
        Rectangle hover = new Rectangle().color(0x40FFFFFF);
        IDrawable stick = new ItemDrawable(ItemList.Tool_DataStick.get(1)).asIcon()
            .size(14, 14);
        for (int l = 1; l <= LANES; l++) {
            final int ll = l;
            int y = 3 + l * 18;
            sm.syncValue("cyc_lane_warn_" + l, new StringSyncValue(() -> {
                if (!isServer()) return cLaneWarn[ll] == null ? "" : cLaneWarn[ll];
                long now = getBaseMetaTileEntity().getWorld()
                    .getTotalWorldTime();
                return warn[ll] != null && now < warnUntil[ll] ? warn[ll] : "";
            }, v -> cLaneWarn[ll] = v));
            sm.syncValue(
                "cyc_lane_info_" + l,
                new StringSyncValue(() -> isServer() ? laneInfo(ll) : cLaneInfo[ll], v -> cLaneInfo[ll] = v));
            sm.syncValue(
                "cyc_lane_icon_" + l,
                GenericSyncValue.builder(ItemStack.class)
                    .getter(() -> isServer() ? laneIcon(ll) : cLaneIcon[ll])
                    .setter(v -> cLaneIcon[ll] = v)
                    .adapter(ByteBufAdapters.ITEM_STACK)
                    .nullable()
                    .build());
            page.child(
                IKey.str(String.valueOf(l))
                    .asWidget()
                    .pos(3, y + 5)
                    .size(7, 9));
            page.child(
                IKey.dynamic(() -> laneShort(ll))
                    .asWidget()
                    .color(() -> LANE_STATE_COLORS[clientLaneState(ll)])
                    .tooltipDynamic(t -> laneRowTooltip(ll, t))
                    .tooltipAutoUpdate(true)
                    .pos(11, y + 5)
                    .size(PAGE_W - 33, 9));
            InteractionSyncHandler bh = new InteractionSyncHandler().setOnMousePressed(d -> {
                if (sm.isClient()) return;
                String w = d.shift ? unbindLane(ll) : bindLane(ll, sm.getPlayer());
                warn[ll] = w;
                warnUntil[ll] = getBaseMetaTileEntity().getWorld()
                    .getTotalWorldTime() + LANE_WARN_TICKS;
            });
            sm.syncValue("cyc_bind_" + l, bh);
            ItemDrawable icon = new ItemDrawable();
            IDrawable machine = icon.asIcon()
                .size(14, 14);
            IDrawable unbound = IDrawable.of(GTGuiTextures.BUTTON_STANDARD, laneRing(l));
            IDrawable bound = IDrawable.of(GTGuiTextures.SLOT_ITEM_STANDARD, laneRing(l));
            IDrawable unboundHover = IDrawable.of(unbound, hover), boundHover = IDrawable.of(bound, hover);
            page.child(
                new ButtonWidget<>().syncHandler(bh)
                    .background(new DynamicDrawable(() -> clientLaneState(ll) == LANE_UNBOUND ? unbound : bound))
                    .hoverBackground(
                        new DynamicDrawable(() -> clientLaneState(ll) == LANE_UNBOUND ? unboundHover : boundHover))
                    .overlay(new DynamicDrawable(() -> {
                        if (clientLaneState(ll) == LANE_UNBOUND) return stick;
                        if (cLaneIcon[ll] == null) return null;
                        icon.setItem(cLaneIcon[ll]);
                        return machine;
                    }))
                    .tooltipDynamic(t -> laneRowTooltip(ll, t))
                    .tooltipAutoUpdate(true)
                    .pos(PAGE_W - 21, y)
                    .size(18, 18));
        }
        return page;
    }

    private ParentWidget<?> createBalancingPage(PanelSyncManager sm) {
        ParentWidget<?> page = new ParentWidget<>().size(PAGE_W, PAGE_H);
        PagedWidget.Controller ctrl = new PagedWidget.Controller();
        PagedWidget<?> groups = new PagedWidget<>().controller(ctrl)
            .pos(0, GROUP_PAGE_Y)
            .size(PAGE_W, PAGE_H - GROUP_PAGE_Y);
        for (int g = 0; g < GROUPS; g++) {
            final int gg = g;
            page.child(new PageButton(g, ctrl) {

                @Override
                public Result onMousePressed(int mouseButton) {
                    getContext().removeFocus();
                    return super.onMousePressed(mouseButton);
                }
            }.background(true, GTGuiTextures.BUTTON_STANDARD_PRESSED, groupStripes(g, 4, 1.5f, 0xA0000000))
                .background(false, GTGuiTextures.BUTTON_STANDARD, groupStripes(g, 4, 1.5f, STRIPE_DARK))
                .overlay(
                    IKey.str(String.valueOf(g + 1))
                        .color(Color.WHITE.main)
                        .shadow(true))
                .tooltipDynamic(t -> groupTooltip(gg, t))
                .tooltipAutoUpdate(true)
                .pos(3 + g * 18, 3)
                .size(18, 16));
            groups.addPage(createGroupPage(sm, g));
        }
        page.child(groups);
        return page;
    }

    private ParentWidget<?> createGroupPage(PanelSyncManager sm, int g) {
        int h = PAGE_H - GROUP_PAGE_Y;
        ParentWidget<?> page = new ParentWidget<>().size(PAGE_W, h);
        page.child(
            IKey.dynamic(() -> groupHeader(g))
                .asWidget()
                .color(() -> levelColor(g))
                .alignment(Alignment.Center)
                .background(groupStripes(g, 4, 1.5f, 0x20000000))
                .tooltipDynamic(t -> groupTooltip(g, t))
                .tooltipAutoUpdate(true)
                .pos(3, 0)
                .size(PAGE_W - 6, 11));
        ListWidget<IWidget, ?> list = new ListWidget<>();
        list.crossAxisAlignment(Alignment.CrossAxis.START)
            .pos(3, 13)
            .size(PAGE_W - 6, h - 16);
        for (int r = 0; r < MAX_ROWS; r++) list.child(createRow(sm, g, r));
        page.child(list);
        return page;
    }

    private IWidget createRow(PanelSyncManager sm, int g, int r) {
        sm.syncValue(
            "cyc_row_" + g + "_" + r,
            new StringSyncValue(() -> isServer() ? serverRowLine(g, r) : cRowLine[g][r], v -> cRowLine[g][r] = v));
        sm.syncValue(
            "cyc_icon_" + g + "_" + r,
            GenericSyncValue.builder(ItemStack.class)
                .getter(() -> isServer() ? serverRowIcon(g, r) : cIcon[g][r])
                .setter(v -> cIcon[g][r] = v)
                .adapter(ByteBufAdapters.ITEM_STACK)
                .nullable()
                .build());
        ParentWidget<?> row = new ParentWidget<>().size(68, 37);
        row.setEnabledIf(w -> r < cRows[g]);
        row.child(
            new ItemDisplayWidget().item(new ObjectValue.Dynamic<>(ItemStack.class, () -> cIcon[g][r], v -> {}))
                .background(GTGuiTextures.SLOT_ITEM_STANDARD)
                .tooltipDynamic(t -> {
                    String s = decode(cRowLine[g][r]);
                    if (!s.isEmpty()) addLines(t, s);
                })
                .tooltipAutoUpdate(true)
                .pos(0, 0)
                .size(18, 18));
        row.child(
            new TextFieldWidget()
                .value(new LongSyncValue(() -> isServer() ? getSetting(g, r).target : cTarget[g][r], v -> {
                    if (isServer()) setTarget(g, r, v);
                    else cTarget[g][r] = v;
                }).allowC2S())
                .formatAsInteger(true)
                .numbersLong(0, 9_007_199_254_740_991L)
                .setTextColor(Color.WHITE.main)
                .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.target"))
                .pos(19, 1)
                .size(30, 16));
        row.child(
            new ItemSlot().slot(new ModularSlot(seedInput, g * MAX_ROWS + r).accessibility(true, false))
                .background(GTGuiTextures.SLOT_ITEM_STANDARD)
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.seedslot"))
                .setEnabledIf(w -> !rowIsFluid(g, r))
                .pos(50, 0)
                .size(18, 18));
        row.child(new FluidSlot() {

            @Override
            protected void addToolTip(RichTooltip tooltip) {
                addLangLines(tooltip, "proghatches.cyclic.seedslot.fluid");
            }
        }.syncHandler(new FluidSlotSyncHandler(new SeedTank(g, r)) {

            @Override
            protected void tryClickContainer(MouseData mouseData) {
                if (!clickFluidItem(g, r, mouseData.mouseButton == 0, getSyncManager()))
                    super.tryClickContainer(mouseData);
            }
        }.canDrainSlot(false)
            .filter(fs -> fluidMatchesRow(g, r, fs)))
            .background(GTGuiTextures.SLOT_FLUID_STANDARD)
            .setEnabledIf(w -> rowIsFluid(g, r))
            .pos(50, 0)
            .size(18, 18));
        row.child(
            new ToggleButton().value(new BooleanSyncValue(() -> isServer() ? getSetting(g, r).output : cOut[g][r], v -> {
                if (isServer()) setOutput(g, r, v);
                else cOut[g][r] = v;
            }).allowC2S())
                .overlay(true, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
                .overlay(false, GTGuiTextures.OVERLAY_BUTTON_CROSS)
                .tooltipBuilder(true, t -> addLangLines(t, "proghatches.cyclic.output"))
                .tooltipBuilder(false, t -> addLangLines(t, "proghatches.cyclic.output.off"))
                .pos(1, 19)
                .size(16, 16));
        row.child(
            new TextFieldWidget().value(new IntSyncValue(() -> isServer() ? getSetting(g, r).tol : cTol[g][r], v -> {
                if (isServer()) setTol(g, r, v);
                else cTol[g][r] = v;
            }).allowC2S())
                .formatAsInteger(true)
                .numbersInt(0, 100)
                .setTextColor(Color.WHITE.main)
                .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.tolerance"))
                .pos(19, 19)
                .size(26, 16));
        row.child(
            IKey.str("%")
                .asWidget()
                .pos(46, 23)
                .size(6, 9));
        return row;
    }

    private final class GroupTrack extends com.cleanroommc.modularui.widget.Widget<GroupTrack> implements Interactable {

        private final IntSyncValue sync;
        private final int[] drag = new int[GROUPS - 1];
        private int handle = -1;

        GroupTrack(IntSyncValue sync) {
            this.sync = sync;
            listenGuiAction((IGuiAction.MouseReleased) button -> {
                if (handle < 0) return false;
                handle = -1;
                sync.setIntValue(packGroups(sanitizeGroups(drag)), true, true);
                return true;
            });
            tooltipDynamic(this::buildTooltip);
            tooltipAutoUpdate(true);
        }

        private int[] shown() {
            return handle < 0 ? groupEnd : sanitizeGroups(drag);
        }

        private int unitAt(int mx) {
            return Math.max(0, Math.min(SLOTS, Math.round((mx - 1) / 2f)));
        }

        private void buildTooltip(RichTooltip t) {
            addLangLines(t, "proghatches.cyclic.groups");
            int[] e = shown();
            for (int g = 0; g < GROUPS; g++)
                t.addLine(IKey.str(groupTitle(g, g == 0 ? 0 : e[g - 1], g == GROUPS - 1 ? SLOTS : e[g])));
        }

        @Override
        public Result onMousePressed(int mouseButton) {
            if (mouseButton != 0) return Result.IGNORE;
            System.arraycopy(groupEnd, 0, drag, 0, drag.length);
            int u = unitAt(getContext().getMouseX());
            handle = 0;
            for (int i = 1; i < drag.length; i++) if (Math.abs(drag[i] - u) < Math.abs(drag[handle] - u)) handle = i;
            drag[handle] = u;
            return Result.SUCCESS;
        }

        @Override
        public void onMouseDrag(int mouseButton, long timeSinceClick) {
            if (handle >= 0) drag[handle] = unitAt(getContext().getMouseX());
        }

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            int[] e = shown();
            int h = getArea().height;
            GTGuiTextures.SLOT_ITEM_STANDARD.draw(context, 0, 2, getArea().width, h - 4, widgetTheme.getTheme());
            for (int g = 0; g < GROUPS; g++) {
                int a = g == 0 ? 0 : e[g - 1], b = g == GROUPS - 1 ? SLOTS : e[g], w = (b - a) * 2;
                if (w <= 0) continue;
                drawStripes(g, 1 + a * 2, 3, w, h - 6, 3, 1, STRIPE_DARK);
                String n = String.valueOf(b - a);
                int tw = Minecraft.getMinecraft().fontRenderer.getStringWidth(n);
                if (tw + 1 <= w) GuiDraw.drawText(n, 1 + a * 2 + (w - tw + 1) / 2, 3, 1, Color.WHITE.main, true);
            }
            for (int k = 1; k < COLS; k++) GuiDraw.drawRect(1 + k * ROWS * 2, h - 2, 1, 2, 0xFF555555);
            for (int i = 0; i < e.length; i++) {
                int x = 1 + e[i] * 2;
                GTGuiTextures.BUTTON_STANDARD.draw(context, x - 2, 0, 4, h, widgetTheme.getTheme());
                if (handle >= 0 && e[i] == drag[handle]) GuiDraw.drawRect(x - 1, 1, 2, h - 2, 0x80FFFFFF);
            }
        }
    }

    private ParentWidget<?> createGeneralPage(PanelSyncManager sm) {
        ParentWidget<?> page = new ParentWidget<>().size(PAGE_W, PAGE_H);
        for (int g = 0; g < GROUPS; g++) {
            final int gg = g;
            page.child(
                new com.cleanroommc.modularui.widget.Widget<>()
                    .background(GTGuiTextures.SLOT_ITEM_STANDARD, groupStripes(g, 3, 1, STRIPE_DARK))
                    .pos(3, 4 + g * 11)
                    .size(9, 9));
            page.child(
                IKey.dynamic(() -> groupHeader(gg))
                    .asWidget()
                    .color(() -> levelColor(gg))
                    .tooltipDynamic(t -> groupTooltip(gg, t))
                    .tooltipAutoUpdate(true)
                    .pos(13, 4 + g * 11)
                    .size(PAGE_W - 16, 10));
        }
        IntSyncValue groups = new IntSyncValue(() -> packGroups(groupEnd), v -> {
            if (isServer()) setGroups(unpackGroups(v));
            else System.arraycopy(unpackGroups(v), 0, groupEnd, 0, groupEnd.length);
        }).allowC2S();
        sm.syncValue("cyc_groups", groups);
        page.child(
            new GroupTrack(groups).pos(2, 50)
                .size(PAGE_W - 4, 14));

        InteractionSyncHandler sh = new InteractionSyncHandler().setOnMousePressed(d -> {
            if (!sm.isClient()) syncSeeds();
        });
        sm.syncValue("cyc_sync", sh);
        page.child(
            new ButtonWidget<>().syncHandler(sh)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(GTGuiTextures.OVERLAY_BUTTON_AUTOPULL_ME)
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.sync"))
                .pos(3, 66)
                .size(16, 16));
        page.child(
            IKey.lang("proghatches.cyclic.sync.short")
                .asWidget()
                .pos(21, 70)
                .size(PAGE_W - 24, 9));

        page.child(
            new ToggleButton().value(new BooleanSyncValue(() -> autoSeed, v -> {
                autoSeed = v;
                if (isServer()) {
                    analysisDirty = true;
                    markDirty();
                }
            }).allowC2S())
                .overlay(true, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
                .overlay(false, GTGuiTextures.OVERLAY_BUTTON_CROSS)
                .tooltipBuilder(true, t -> addLangLines(t, "proghatches.cyclic.autoseed.on"))
                .tooltipBuilder(false, t -> addLangLines(t, "proghatches.cyclic.autoseed.off"))
                .pos(3, 84)
                .size(16, 16));
        page.child(
            new TextFieldWidget().value(new IntSyncValue(() -> autoSeedMult, v -> {
                autoSeedMult = Math.max(1, v);
                if (isServer()) {
                    analysisDirty = true;
                    markDirty();
                }
            }).allowC2S())
                .formatAsInteger(true)
                .numbersInt(1, Integer.MAX_VALUE)
                .setTextColor(Color.WHITE.main)
                .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.autoseed.mult"))
                .pos(21, 84)
                .size(26, 16));
        page.child(
            new TextFieldWidget().value(new IntSyncValue(() -> autoSeedChanceMult, v -> {
                autoSeedChanceMult = Math.max(1, v);
                if (isServer()) {
                    analysisDirty = true;
                    markDirty();
                }
            }).allowC2S())
                .formatAsInteger(true)
                .numbersInt(1, Integer.MAX_VALUE)
                .setTextColor(Color.WHITE.main)
                .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.autoseed.chancemult"))
                .pos(49, 84)
                .size(26, 16));

        InteractionSyncHandler rh = new InteractionSyncHandler().setOnMousePressed(d -> {
            if (sm.isClient()) return;
            if (d.shift) forgetOwed();
            else returnSeeds();
        });
        sm.syncValue("cyc_return", rh);
        page.child(
            new ButtonWidget<>().syncHandler(rh)
                .background(GTGuiTextures.BUTTON_STANDARD)
                .overlay(
                    new ItemDrawable(new ItemStack(Items.wheat_seeds)).asIcon()
                        .size(14, 14))
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.return"))
                .pos(3, 102)
                .size(16, 16));
        page.child(
            IKey.lang("proghatches.cyclic.return.short")
                .asWidget()
                .pos(21, 106)
                .size(PAGE_W - 24, 9));

        page.child(createRefundButton2(sm).pos(3, 120));
        page.child(
            IKey.lang("proghatches.pattern_window.refund")
                .asWidget()
                .pos(21, 124)
                .size(PAGE_W - 24, 9));

        page.child(
            IKey.lang("proghatches.cyclic.multiplier.short")
                .asWidget()
                .pos(3, 140)
                .size(PAGE_W - 6, 9));
        page.child(
            new TextFieldWidget().value(new IntSyncValue(() -> cycMultiplier, v -> {
                if (isServer()) setCycMultiplier(v);
                else cycMultiplier = v;
            }).allowC2S())
                .formatAsInteger(true)
                .numbersInt(1, Integer.MAX_VALUE)
                .setTextColor(Color.WHITE.main)
                .background(GTGuiTextures.BACKGROUND_TEXT_FIELD)
                .tooltipBuilder(t -> addLangLines(t, "proghatches.cyclic.multiplier"))
                .pos(3, 150)
                .size(PAGE_W - 6, 16));
        return page;
    }
}
