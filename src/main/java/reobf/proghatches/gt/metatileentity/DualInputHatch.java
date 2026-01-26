package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Mods.GregTech;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.common.modularui2.util.CommonGuiComponents.*;
import static tectech.Reference.MODID;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.IPacketWriter;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable.Result;

import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.DynamicValue;
import com.cleanroommc.modularui.value.EnumValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;

import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.IOnSlotChanged;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.SizedDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.fluid.FluidStackTank;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.IInterfaceHost;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import bartworks.common.tileentities.tiered.MTERadioHatch;
import bartworks.util.MathUtils;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.SoundResource;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IAddGregtechLogo;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.modularui2.GTGuis;
import gregtech.api.modularui2.MetaTileEntityGuiHandler;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTTooltipDataCache;
import gregtech.api.util.GTTooltipDataCache.TooltipData;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GTUtility.ItemId;
import gregtech.api.util.item.GhostCircuitItemStackHandler;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.covers.modes.RedstoneMode;
import gregtech.common.modularui2.sync.GhostCircuitSyncHandler;
import gregtech.common.modularui2.widget.GhostCircuitSlotWidget;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputHatchWithPattern;
import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.gt.metatileentity.util.BaseSlotPatched;
import reobf.proghatches.gt.metatileentity.util.IOnFillCallback;
import reobf.proghatches.gt.metatileentity.util.IPHDual;
import reobf.proghatches.gt.metatileentity.util.IProgrammingCoverBlacklisted;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;
import reobf.proghatches.gt.metatileentity.util.ISkipStackSizeCheck;
import reobf.proghatches.gt.metatileentity.util.InventoryItemHandler;
import reobf.proghatches.gt.metatileentity.util.ListeningFluidTank;
import reobf.proghatches.gt.metatileentity.util.mui2.ItemSlotDummy;
import reobf.proghatches.gt.metatileentity.util.mui2.SyncHandlerAEFluid;
import reobf.proghatches.gt.metatileentity.util.mui2.SyncHandlerAEItem;
import reobf.proghatches.gt.metatileentity.util.polyfill.INeoDualInputInventory;
import reobf.proghatches.item.ItemBadge;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.net.UpgradesMessage;
import reobf.proghatches.util.ProghatchesUtil;

@DualInputHatch.MUI2Compat
public class DualInputHatch extends MTEHatchInputBus implements IConfigurationCircuitSupport, IAddGregtechLogo,
		IAddUIWidgets, IDualInputHatchWithPattern, IProgrammingCoverBlacklisted, IRecipeProcessingAwareDualHatch,
		ISkipStackSizeCheck, IOnFillCallback, IPHDual/* ,IMultiCircuitSupport */ {

	static int[] AZERO = { 0 };
	static java.text.DecimalFormat format = new java.text.DecimalFormat("#,###");
	public boolean mMultiFluid;

	public DualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, String... optional) {
		this(id, name, nameRegional, tier, ProghatchesUtil.getSlots(tier) + 1, mMultiFluid, optional);

	}


	public DualInputHatch(int id, String name, String nameRegional, int tier, int slot, boolean mMultiFluid,
			String... optional) {

		super(id, name, nameRegional, tier, slot,
				(optional.length > 0 ? optional : reobf.proghatches.main.Config.get("DH", ImmutableMap.of(

						"cap", format.format(getInventoryFluidLimit(tier, mMultiFluid)), "mMultiFluid", mMultiFluid,
						"slots", Math.min(16, (1 + tier) * (tier + 1)), "fluidSlots", fluidSlots(tier), "stackLimit",
						getInventoryStackLimit(tier)))

				)

		);
		this.disableSort = true;
		Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id));
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();

	}

	public int fluidSlots() {
		return Math.max(4, mTier - 1);

	}

	public static int fluidSlots(int mTier) {
		return Math.max(4, mTier - 1);

	}

	public void initTierBasedField() {

		if (mMultiFluid) {

			mStoredFluid = Stream.generate(() -> (new ListeningFluidTank(getInventoryFluidLimit(), this)))
					.limit(fluidSlots()).toArray(ListeningFluidTank[]::new);

		} else {

			mStoredFluid = new ListeningFluidTank[] { new ListeningFluidTank(getInventoryFluidLimit(), this) };

		}

	}

	public void reinitTierBasedField() {

		this.markDirty();

		if (mMultiFluid) {
			ListeningFluidTank[] old = mStoredFluid;
			mStoredFluid = Stream.generate(() -> (new ListeningFluidTank(getInventoryFluidLimit(), this)))
					.limit(fluidSlots()).toArray(ListeningFluidTank[]::new);
			for (int i = 0; i < mStoredFluid.length; i++) {
				mStoredFluid[i] = old[i];
			}

		} else {
			ListeningFluidTank[] old = mStoredFluid;
			mStoredFluid = new ListeningFluidTank[] { new ListeningFluidTank(getInventoryFluidLimit(), this) };
			for (int i = 0; i < mStoredFluid.length; i++) {
				mStoredFluid[i] = old[i];
			}
		}

	}


	public DualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
			boolean mMultiFluid) {
		super(mName, mTier, mDescriptionArray, mTextures);
		this.disableSort = true;
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();
		shared.reinit();
	}

	public DualInputHatch(String aName, int aTier, int aSlots, String[] aDescription, ITexture[][][] aTextures,
			boolean mMultiFluid) {
		super(aName, aTier, aSlots, aDescription, aTextures);
		this.disableSort = true;
		this.mMultiFluid = mMultiFluid;
		initTierBasedField();
		shared.reinit();

	}

	public static NBTTagCompound writeToNBT(ItemStack is, NBTTagCompound tag) {
		is.writeToNBT(tag);
		tag.setInteger("ICount", is.stackSize);

		return tag;
	}

	public static ItemStack loadItemStackFromNBT(NBTTagCompound tag) {

		ItemStack is = ItemStack.loadItemStackFromNBT(tag);
		is.stackSize = tag.getInteger("ICount");
		return is;
	}

	public ListeningFluidTank[] mStoredFluid = new ListeningFluidTank[0];

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		super.saveNBTData(aNBT);

		aNBT.setTag("shared", shared.ser());
		aNBT.setInteger("fluidLimit", fluidLimit);
		aNBT.setBoolean("program", program);
		aNBT.setBoolean("mMultiFluid", mMultiFluid);
		if (mStoredFluid != null) {
			for (int i = 0; i < mStoredFluid.length; i++) {
				if (mStoredFluid[i] != null)
					aNBT.setTag("mFluid" + i, mStoredFluid[i].writeToNBT(new NBTTagCompound()));
			}
		}
		try {
			NBTTagList greggy = aNBT.getTagList("Inventory", 10);
			for (int i = 0; i < greggy.tagCount(); i++) {
				NBTTagCompound t = ((NBTTagCompound) greggy.getCompoundTagAt(i));
				int index = t.getInteger("IntSlot");
				t.setInteger("Count", mInventory[index].stackSize);
			}
		} catch (Exception e) {
			// burh
			e.printStackTrace();
		}



	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		if (aNBT.hasKey("x") == false)
			return;
		super.loadNBTData(aNBT);
		shared.deser(aNBT.getCompoundTag("shared"));
		fluidLimit = aNBT.getInteger("fluidLimit");
		program = aNBT.getBoolean("program");
		mMultiFluid = aNBT.getBoolean("mMultiFluid");
		if (mStoredFluid != null) {
			for (int i = 0; i < mStoredFluid.length; i++) {
				if (aNBT.hasKey("mFluid" + i)) {
					mStoredFluid[i].readFromNBT(aNBT.getCompoundTag("mFluid" + i));
				}
			}
		}



	}

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		DualInputHatch neo =new DualInputHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid);

		return neo;
	}



	ItemStackHandler bridge;

	@Override
	public ItemStackHandler getInventoryHandler() {

		if (bridge == null)
			bridge = new InventoryItemHandler(mInventory, this).id(1);

		return bridge;
	}

	public int moveButtons() {
		return 1;

	}

	private static final String SORTING_MODE_TOOLTIP = "GT5U.machines.sorting_mode.tooltip";
	private static final String ONE_STACK_LIMIT_TOOLTIP = "GT5U.machines.one_stack_limit.tooltip";
	private static final int BUTTON_SIZE = 18;


	boolean createInsertion() {
		return true;
	}

	public static class MarkerWidget extends Widget {

		public DualInputHatch thiz;

		public MarkerWidget(DualInputHatch dualInputHatch) {
			thiz = dualInputHatch;
		}
	}

	private static final SizedDrawable t0 = new SizedDrawable(AdaptableUITexture
			.of("appliedenergistics2", "guis/states", 16, 16, 0).getSubArea(3 / 16f, 9 / 16f, 4 / 16f, 10 / 16f), 16,
			16, 1, 1);
	private static final SizedDrawable t1 = new SizedDrawable(
			AdaptableUITexture.of("proghatches", "gui/states", 16, 16, 0), 16, 16, 1, 1);
	private static final SizedDrawable t2 = new SizedDrawable(AdaptableUITexture
			.of("appliedenergistics2", "guis/states", 16, 16, 0).getSubArea(4 / 16f, 9 / 16f, 5 / 16f, 10 / 16f), 16,
			16, 1, 1);
	private static  com.cleanroommc.modularui.drawable.UITexture t00,t01,t02;
	static{
		t00=com.cleanroommc.modularui.drawable.UITexture.builder()
				.location("appliedenergistics2", "guis/states")
				.uv(3 / 16f, 9 / 16f, 4 / 16f, 10 / 16f)
				.build();

		t01=com.cleanroommc.modularui.drawable.UITexture.builder()
				.location("proghatches", "gui/states")

				.build();
		t02=com.cleanroommc.modularui.drawable.UITexture.builder()
				.location("appliedenergistics2", "guis/states")
				.uv(4 / 16f, 9 / 16f, 5 / 16f, 10 / 16f)
				.build();

	}
	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		mui1ct.addUIWidgets(builder,buildContext);
	}



	boolean showFluidLimit() {

		return true;
	}

	@Override
	public boolean justUpdated() {

		return false;
	}

	@SuppressWarnings("rawtypes")
	public final static Iterator emptyItr = new Iterator() {

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			throw new NoSuchElementException();
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<? extends IDualInputInventoryWithPattern> inventories() {
		if (!this.isValid())
			return emptyItr;
		if (theInv.isEmpty())
			return emptyItr;
		return Arrays.asList(theInv).iterator();
	}

	public List<IDualInputInventory> inventoriesReal() {

		return Arrays.asList(theInv);
	}

	@Override
	public Optional<IDualInputInventory> getFirstNonEmptyInventory() {
		if (!this.isValid())
			return Optional.empty();
		boolean empty = true;

		for (FluidStack f : theInv.getFluidInputs()) {
			if (f != null && f.amount > 0)
				empty = false;
			break;
		}
		for (ItemStack f : theInv.getItemInputs()) {
			if (f != null && f.stackSize > 0)
				empty = false;
			break;
		}

		if (empty)
			return Optional.empty();
		return Optional.of(theInv);
	}

	@Override
	public boolean supportsFluids() {

		return true;
	}

	private ItemStack[] dualItem() {
		return filterStack.apply(mInventory, shared.getItems());
	}

	private FluidStack[] dualFluid() {
		return asFluidStack.apply(mStoredFluid, shared.getFluid());
	}

	final public DualInv theInv = new DualInv();

	public class DualInv implements INeoDualInputInventory {

		public boolean isEmpty() {

			for (FluidTank f : mStoredFluid) {
				if (f.getFluidAmount() > 0) {
					return false;
				}
			}
			for (ItemStack i : mInventory/* getItemInputs() */) {

				if (i != null && i.stackSize > 0) {
					return false;
				}
			}
			return true;
		}

		@Override
		public ItemStack[] getItemInputs() {
			ItemStack[] is = dualItem();
			// if(!trunOffEnsure){is=ensureIntMax(is);}
			return is;
		}

		@Override
		public FluidStack[] getFluidInputs() {
			FluidStack[] is = dualFluid();
			// if(!trunOffEnsure){is=ensureIntMax(is);}
			return is;
		}
	}

	static TreeSet recycle = null;

	static TreeSet retrieve() {

		TreeSet t = recycle;
		recycle = null;
		return t;
	}

	static void dump(TreeSet t) {
		t.clear();
		recycle = t;

	}

	static public ItemStack[] ensureIntMax(ItemStack[] in) {
		class Source implements Comparable<Source> {

			int num;
			int index;

			public Source(int n, int i) {
				num = n;
				index = i;
			}

			@Override
			public int compareTo(Source o) {
				return Integer.compare(o.num, num);// inversed
			}
		}
		int removed = 0;

		HashMap<ItemId, TreeSet<Source>> count = new HashMap<>(in.length);
		for (int i = 0; i < in.length; i++) {
			TreeSet<Source> set = (recycle == null) ? new TreeSet<>() : retrieve();

			set.add(new Source(in[i].stackSize, i));
			count.merge(ItemId.createNoCopy(in[i]), set, (a, b) -> {
				a.addAll(b);
				dump(b);
				return a;
			});
			Iterator<TreeSet<Source>> itr = count.values().iterator();
			TreeSet<Source> thiz;
			for (; itr.hasNext();) {
				thiz = itr.next();
				long howmany = 0;
				Iterator<Source> s = thiz.iterator();
				end: while (s.hasNext()) {
					howmany += s.next().num;
					if (howmany > Integer.MAX_VALUE) {
						s.remove();
						removed++;
						while (s.hasNext()) {
							s.next();
							s.remove();
							removed++;
							break end;
						}
					}
				}

			}
		}

		if (removed == 0)
			return in;

		ItemStack[] ret = new ItemStack[in.length - removed];
		int[] cnt = new int[1];
		count.values().stream().flatMap(s -> s.stream()).forEach(s -> {
			ret[cnt[0]] = in[s.index];
			cnt[0]++;
		});

		return ret;
	}

	// boolean trunOffEnsure=true;
	static public FluidStack[] ensureIntMax(FluidStack[] in) {
		class Source implements Comparable<Source> {

			int num;
			int index;

			public Source(int n, int i) {
				num = n;
				index = i;
			}

			@Override
			public int compareTo(Source o) {
				return Integer.compare(o.num, num);// inversed
			}
		}
		int removed = 0;

		IdentityHashMap<Fluid, TreeSet<Source>> count = new IdentityHashMap<>(in.length);
		// do not use hashcode() for better performance
		for (int i = 0; i < in.length; i++) {
			TreeSet<Source> set = (recycle == null) ? new TreeSet<>() : retrieve();

			set.add(new Source(in[i].amount, i));
			count.merge(in[i].getFluid(), set, (a, b) -> {
				a.addAll(b);
				dump(b);
				return a;
			});
			Iterator<TreeSet<Source>> itr = count.values().iterator();
			TreeSet<Source> thiz = null;
			for (; itr.hasNext();) {
				thiz = itr.next();
				long howmany = 0;
				Iterator<Source> s = thiz.iterator();
				end: while (s.hasNext()) {
					howmany += s.next().num;
					if (howmany > Integer.MAX_VALUE) {
						s.remove();
						removed++;
						while (s.hasNext()) {
							s.next();
							s.remove();
							removed++;
							break end;
						}
					}
				}

			}
		}

		if (removed == 0)
			return in;

		FluidStack[] ret = new FluidStack[in.length - removed];
		int[] cnt = new int[1];
		count.values().stream().flatMap(s -> s.stream()).forEach(s -> {
			ret[cnt[0]] = in[s.index];
			cnt[0]++;
		});

		return ret;
	}

	public interface VargsFunction<T, R> {

		R apply(T... t);
	}

	static VargsFunction<Object[], FluidStack[]> asFluidStack = (s) -> Arrays.stream(s).flatMap(Arrays::stream)
			.map(f -> {
				if (f instanceof FluidTank) {
					return ((FluidTank) f).getFluid();
				} else if (f instanceof FluidStack) {
					return (FluidStack) f;
				} else if (f == null) {
					/* ignore */return null;
				} else {
					throw new RuntimeException("only FluidStack or FluidTank are accepted");
				}
			}).filter(a -> a != null && a.amount > 0).toArray(FluidStack[]::new);
	public static VargsFunction<ItemStack[], ItemStack[]> filterStack = (s) -> Arrays.stream(s).flatMap(Arrays::stream)
			.filter(a -> a != null).toArray(ItemStack[]::new);

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		FluidTankInfo[] FTI = new FluidTankInfo[mStoredFluid.length];
		for (int i = 0; i < mStoredFluid.length; i++) {
			FTI[i] = new FluidTankInfo(mStoredFluid[i]);
		}
		return FTI;
	}

	///////////

	public FluidStack[] getStoredFluid() {
		return asFluidStack.apply(mStoredFluid);
	}

	/*
	 * @Override public ITexture[] getTexturesActive(ITexture aBaseTexture) {
	 * return new ITexture[] { aBaseTexture,
	 * TextureFactory.of(OVERLAY_INPUT_HATCH_2x2) }; }
	 *
	 * @Override public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
	 * return new ITexture[] { aBaseTexture,
	 * TextureFactory.of(OVERLAY_INPUT_HATCH_2x2) }; }
	 */
	public int getMaxType() {
		return mStoredFluid.length;
	}

	@Override
	public FluidStack getFluid() {
		for (FluidTank tFluid : mStoredFluid) {
			if (tFluid != null && tFluid.getFluidAmount() > 0)
				return tFluid.getFluid();
		}
		return null;
	}

	public FluidStack getFluid(int aSlot) {
		if (aSlot < 0 || aSlot >= getMaxType())
			return null;
		return mStoredFluid[aSlot].getFluid();
	}

	@Override
	public int getFluidAmount() {
		if (getFluid() != null) {
			return getFluid().amount;
		}
		return 0;
	}

	@Override
	public int getCapacity() {
		if (mStoredFluid.length == 0)
			return 0;
		return mStoredFluid[0].getCapacity();
	}

	public int getFirstEmptySlot() {
		for (int i = 0; i < mStoredFluid.length; i++) {
			if (mStoredFluid[i].getFluidAmount() == 0)
				return i;
		}
		return -1;
	}

	public boolean hasFluid(FluidStack aFluid) {
		if (aFluid == null)
			return false;
		for (FluidTank tFluid : mStoredFluid) {
			if (aFluid.isFluidEqual(tFluid.getFluid()))
				return true;
		}
		return false;
	}

	public int getFluidSlot(FluidStack tFluid) {
		if (tFluid == null)
			return -1;
		for (int i = 0; i < mStoredFluid.length; i++) {
			if (tFluid.equals(mStoredFluid[i].getFluid()))
				return i;
		}
		return -1;
	}

	public int getFluidAmount(FluidStack tFluid) {
		int tSlot = getFluidSlot(tFluid);
		if (tSlot != -1) {
			return mStoredFluid[tSlot].getFluid().amount;
		}
		return 0;
	}

	public void setFluid(FluidStack aFluid, int aSlot) {
		if (aSlot < 0 || aSlot >= getMaxType())
			return;
		mStoredFluid[aSlot].setFluid(aFluid);

	}

	public void addFluid(FluidStack aFluid, int aSlot) {
		if (aSlot < 0 || aSlot >= getMaxType())
			return;

		if (aFluid.equals(mStoredFluid[aSlot].getFluid()))
			mStoredFluid[aSlot].fill(aFluid, true);
		else if (mStoredFluid[aSlot].getFluid() == null)
			mStoredFluid[aSlot].setFluid(aFluid.copy());
	}

	@Override
	public boolean canTankBeFilled() {
		return true;
	}

	@Override
	public boolean canTankBeEmptied() {
		return true;
	};

	@Override
	public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		if (aBaseMetaTileEntity.isServerSide()) {
			mFluid = getFluid();
		}
		super.onPreTick(aBaseMetaTileEntity, aTick);
	}

	public void program() {
		if (shared.isDummy()) {
			// for (int slot :
			// this.getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal()))
			// {
			for (int slot : (Iterable<Integer>) () -> IntStream.range(0, ProghatchesUtil.getSlots(mTier)).iterator()) {
				ItemStack is = getStackInSlot(slot);
				if (is == null)
					continue;
				if (is.getItem() != MyMod.progcircuit)
					continue;
				if (decrStackSize(slot, 64).stackSize == 0) {
					continue;
				}
				markDirty();
				super.setInventorySlotContents(getCircuitSlot(), ItemProgrammingCircuit.getCircuit(is).orElse(null));
			}
			return;
		}

		DualInputHatch meta = this;
		ArrayList<ItemStack> isa = new ArrayList<>();
		int[] slots = (this).getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal());
		for (int slot : (Iterable<Integer>) () -> IntStream.range(0, ProghatchesUtil.getSlots(mTier)).iterator()) {
			ItemStack is = this.mInventory[slot];
			if (is == null)
				continue;
			if (is.getItem() != MyMod.progcircuit)
				continue;
			if (decrStackSize(slot, 64).stackSize == 0) {
				continue;
			}
			isa.add(GTUtility.copyAmount(0, ItemProgrammingCircuit.getCircuit(is).orElse(null)));

		}
		if (isa.isEmpty() == false) {

			shared.clearCircuit();

			for (int i = 0; i < shared.sizeCircuit(); i++) {
				if (i < isa.size()) {
					shared.setCircuit(i, isa.get(i));
				} else {
					shared.setCircuit(i, null);
				}

			}

		}
		/*
		 * IGregTechTileEntity tile = this.getBaseMetaTileEntity();
		 * DualInputHatch meta = this; ArrayList<ItemStack> isa = new
		 * ArrayList<>(); int[] slots =
		 * (this).getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal());
		 * for (int slot : slots) { ItemStack is = ((ISidedInventory)
		 * tile).getStackInSlot(slot); if (is == null) continue; if
		 * (is.getItem() != MyMod.progcircuit) continue; if (((ISidedInventory)
		 * tile).decrStackSize(slot, 64).stackSize == 0) { continue; }
		 * isa.add(GTUtility.copyAmount(0,
		 * ItemProgrammingCircuit.getCircuit(is).orElse(null))); }
		 * if(isa.isEmpty()==false){ if(meta instanceof IMultiCircuitSupport){
		 * int[] aslots=((IMultiCircuitSupport) meta).getCircuitSlots(); for
		 * (int i = 0; i < aslots.length; i++) { if (i < isa.size()) {
		 * ((IInventory) tile).setInventorySlotContents( ((IMultiCircuitSupport)
		 * meta).getCircuitSlots()[i] ,isa.get(i)); } else { ((IInventory)
		 * tile).setInventorySlotContents( ((IMultiCircuitSupport)
		 * meta).getCircuitSlots()[i] ,null); } } }else{ throw new
		 * AssertionError("impossible"); } }
		 */

	}

	@Override
	public int getCircuitSlot() {
		return ProghatchesUtil.getSlots(slotTierOverride(mTier));
	}

	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
		/*
		 * this.mInventory[this.getCircuitSlot() ]=new ItemStack(Items.apple);
		 */
		super.onFirstTick(aBaseMetaTileEntity);
	}
	public void programLoose(){program();}
	boolean program = true;// default: ON

	@SuppressWarnings("unchecked")
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.onPostTick(aBaseMetaTileEntity, aTick);
		if (aBaseMetaTileEntity.getWorld().isRemote)
			return;

		if (program)
			programLoose();

		/*
		 * IGrid a = getNetwork(); if(a!=null){ IStorageGrid
		 * g=a.getCache(IStorageGrid.class);
		 * g.getItemInventory().getAvailableItems(new
		 * appeng.util.item.ItemList()).forEach(s->{ System.out.println( s); });
		 * }
		 */

	}

	@Override
	public void setInventorySlotContents(int aIndex, ItemStack aStack) {
		super.setInventorySlotContents(aIndex, aStack);
		/*
		 * if (program) program();
		 */
	}

	public void onFill() {
	}

	public int fluidLimit = 1;

	@Override
	public int fill(FluidStack aFluid, boolean doFill) {
		if (aFluid == null || aFluid.getFluid().getID() <= 0 || aFluid.amount <= 0 || !canTankBeFilled()
				|| !isFluidInputAllowed(aFluid))
			return 0;

		if (fluidLimit == 0) {
			int oldamount = aFluid.amount;
			aFluid = aFluid.copy();
			for (ListeningFluidTank tk : this.mStoredFluid) {
				if (tk.getFluidAmount() == 0)
					tk.setFluidDirect(null);
				if ((aFluid.amount -= tk.fill(aFluid, doFill)) <= 0) {
					break;
				}
				;

			}
			return oldamount - aFluid.amount;
		}

		if (fluidLimit == 1) {

			if (!hasFluid(aFluid) && getFirstEmptySlot() != -1) {
				int tFilled = Math.min(aFluid.amount, getCapacity());
				if (doFill) {
					FluidStack tFluid = aFluid.copy();
					tFluid.amount = tFilled;
					addFluid(tFluid, getFirstEmptySlot());
					getBaseMetaTileEntity().markDirty();
				}
				return tFilled;
			}
			if (hasFluid(aFluid)) {
				int tLeft = getCapacity() - getFluidAmount(aFluid);
				int tFilled = Math.min(tLeft, aFluid.amount);
				if (doFill) {
					FluidStack tFluid = aFluid.copy();
					tFluid.amount = tFilled;
					addFluid(tFluid, getFluidSlot(tFluid));
					getBaseMetaTileEntity().markDirty();
				}
				return tFilled;
			}
		}
		if (fluidLimit == 2) {

			int oldamount = aFluid.amount;
			aFluid = aFluid.copy();
			LinkedList<ListeningFluidTank> tks = new LinkedList<>();
			for (ListeningFluidTank tk : this.mStoredFluid) {
				if (tk.getFluidAmount() == 0) {
					tk.setFluidDirect(null);
					tks.add(tk);
					if ((aFluid.amount -= tk.fillDirect(aFluid, doFill)) <= 0) {
						break;
					}
					;
				}
			}
			for (ListeningFluidTank tk : this.mStoredFluid) {
				tks.add(tk);
				if ((aFluid.amount -= tk.fillDirect(aFluid, doFill)) <= 0) {
					break;
				}
				;
			}
			tks.forEach(s -> s.onChange());

			return oldamount - aFluid.amount;
		}
		return 0;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (getFluid() == null || !canTankBeEmptied())
			return null;
		if (getFluid().amount <= 0 /* && isFluidChangingAllowed() */) {
			setFluid(null, getFluidSlot(getFluid()));
			getBaseMetaTileEntity().markDirty();
			return null;
		}
		FluidStack tRemove = getFluid().copy();
		tRemove.amount = Math.min(maxDrain, tRemove.amount);
		FluidStack f = getFluid();
		int slot = getFluidSlot(f);
		if (doDrain) {
			f.amount -= tRemove.amount;
			getBaseMetaTileEntity().markDirty();
		}
		if (f == null || f.amount <= 0 /* && isFluidChangingAllowed() */) {
			setFluid(null, slot);
			getBaseMetaTileEntity().markDirty();
		}
		return tRemove;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack aFluid, boolean doDrain) {
		if (aFluid == null || !hasFluid(aFluid))
			return null;
		FluidStack tStored = mStoredFluid[getFluidSlot(aFluid)].getFluid();
		if (tStored.amount <= 0 /* && isFluidChangingAllowed() */) {
			setFluid(null, getFluidSlot(tStored));
			getBaseMetaTileEntity().markDirty();
			return drain(from, aFluid, doDrain);
		}
		FluidStack tRemove = tStored.copy();
		tRemove.amount = Math.min(aFluid.amount, tRemove.amount);
		if (doDrain) {
			tStored.amount -= tRemove.amount;
			getBaseMetaTileEntity().markDirty();
		}
		if (tStored.amount <= 0/* && isFluidChangingAllowed() */) {
			setFluid(null, getFluidSlot(tStored));
			getBaseMetaTileEntity().markDirty();
		}
		return tRemove;
	}

	@Override
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {
		if (mMultiFluid)
			return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_INPUT_HATCH_2x2) };
		return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN) };

	}

	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		if (mMultiFluid)
			return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_INPUT_HATCH_2x2) };
		return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN) };

	}

	@Override
	public boolean isFluidInputAllowed(FluidStack aFluid) {

		if (disableFilter)
			return true;
		return versionInsensitiveContainsInput(aFluid);
	}

	/*
	 * @Override public void onScrewdriverRightClick(ForgeDirection side,
	 * EntityPlayer aPlayer, float aX, float aY, float aZ) { boolean
	 * prev=disableFilter; super.onScrewdriverRightClick(side, aPlayer, aX, aY,
	 * aZ); if(prev==true&&disableFilter==false){ GTUtility
	 * .sendChatToPlayer(aPlayer, defaultName(
	 * "Filter mode of this hatch might not work well", "过滤模式可能无法正常生效") ); } }
	 */

	// private static HashMap<Class<?>, MethodHandle> cache0 = new HashMap<>();
	// private static HashMap<Class<?>, MethodHandle> cache1 = new HashMap<>();

	/**
	 * 2.4.0 compat no longer support 2.4.0 just leave it unchanged
	 */
	public boolean versionInsensitiveContainsInput(FluidStack aFluid) {
		if (mRecipeMap == null)
			return true;
		return this.mRecipeMap.containsInput(aFluid);
		/*
		 * MethodHandle mh = cache0.get(aFluid.getClass()); if (mh != null) {
		 * try { Object map; return ((map =
		 * cache1.get(aFluid.getClass()).invoke(this)) == null) || //
		 * this.mRecipeMap==null // || (boolean) mh.invoke(map, aFluid); //
		 * this.mRecipeMap.containsInput(0) } catch (Throwable e) { throw new
		 * RuntimeException("failed to access mRecipeMap", e); } } try {
		 * Class<?> recipeMapClass =
		 * this.getClass().getField("mRecipeMap").getType(); MethodHandle mhr =
		 * MethodHandles.lookup().findVirtual(recipeMapClass, "containsInput",
		 * MethodType.methodType(boolean.class, aFluid.getClass()));
		 * cache0.put(aFluid.getClass(), mhr); cache1.put(aFluid.getClass(),
		 * MethodHandles.lookup().findGetter(this.getClass(), "mRecipeMap",
		 * recipeMapClass)); return versionInsensitiveContainsInput(aFluid); }
		 * catch (Throwable e) { throw new RuntimeException(
		 * "cannot get mRecipeMap", e); }
		 */
	}

	public void setFilter(RecipeMap<?> recipemap) {

		this.mRecipeMap = recipemap;

	}

	@Override
	public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		if (!(aPlayer instanceof EntityPlayerMP))
			return;

		ItemStack dataStick = aPlayer.inventory.getCurrentItem();
		if (!ItemList.Tool_DataStick.isStackEqual(dataStick, false, true))
			return;

		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("type", "ProgHatchesDualInput");
		tag.setInteger("x", aBaseMetaTileEntity.getXCoord());
		tag.setInteger("y", aBaseMetaTileEntity.getYCoord());
		tag.setInteger("z", aBaseMetaTileEntity.getZCoord());

		dataStick.stackTagCompound = tag;
		dataStick.setStackDisplayName("ProgHatches Dual Input Hatch Link Data Stick (" + aBaseMetaTileEntity.getXCoord()
				+ ", " + aBaseMetaTileEntity.getYCoord() + ", " + aBaseMetaTileEntity.getZCoord() + ")");
		aPlayer.addChatMessage(new ChatComponentText("Saved Link Data to Data Stick"));
	}

	public int slotTierOverride(int mTier) {
		return mTier;
	}



	public int fluidSlotsPerRow() {
		return 1;
	}

	boolean recipe;

	@Override
	public final void startRecipeProcessing() {

		if (recipe) {
			return;
		}
		recipe = true;
		startRecipeProcessingImpl();
	}

	public void startRecipeProcessingImpl() {
		if (program)
			program();
		shared.startRecipeProcessing();
	}

	@Override
	public void updateSlots() {

		if (this.getBaseMetaTileEntity().isServerSide()) {
			for (int i = 0; i < getMaxType(); i++) {
				if (mStoredFluid[i].getFluid() != null && mStoredFluid[i].getFluidAmount() <= 0) {
					mStoredFluid[i].setFluid(null);

				}
			}
		}
		for (int i = 0; i < mInventory.length - 1; i++)
			if (mInventory[i] != null && mInventory[i].stackSize <= 0)
				mInventory[i] = null;
		if (!disableSort)
			fillStacksIntoFirstSlots();

	}

	protected void fillStacksIntoFirstSlots() {
		final int L = mInventory.length - 1;
		HashMap<GTUtility.ItemId, Integer> slots = new HashMap<>(L);
		HashMap<GTUtility.ItemId, ItemStack> stacks = new HashMap<>(L);
		List<GTUtility.ItemId> order = new ArrayList<>(L);
		List<Integer> validSlots = new ArrayList<>(L);
		for (int i = 0; i < L; i++) {
			if (!isValidSlot(i))
				continue;
			validSlots.add(i);
			ItemStack s = mInventory[i];
			if (s == null)
				continue;
			GTUtility.ItemId sID = GTUtility.ItemId.createNoCopy(s);
			slots.merge(sID, s.stackSize, Integer::sum);
			if (!stacks.containsKey(sID))
				stacks.put(sID, s);
			order.add(sID);
			mInventory[i] = null;
		}
		int slotindex = 0;
		for (GTUtility.ItemId sID : order) {
			int toSet = slots.get(sID);
			if (toSet == 0)
				continue;
			int slot = validSlots.get(slotindex);
			slotindex++;
			mInventory[slot] = stacks.get(sID).copy();
			toSet = Math.min(toSet, getInventoryStackLimit());
			mInventory[slot].stackSize = toSet;
			slots.merge(sID, toSet, (a, b) -> a - b);
		}
	}

	/*
	 * private void fillStacksIntoFirstSlotsExtraCircuit() { final int L =
	 * mInventory.length - 4; HashMap<GTUtility.ItemId, Integer> slots = new
	 * HashMap<>(L); HashMap<GTUtility.ItemId, ItemStack> stacks = new
	 * HashMap<>(L); List<GTUtility.ItemId> order = new ArrayList<>(L);
	 * List<Integer> validSlots = new ArrayList<>(L); for (int i = 0; i < L;
	 * i++) { if (!isValidSlot(i)) continue; validSlots.add(i); ItemStack s =
	 * mInventory[i]; if (s == null) continue; GTUtility.ItemId sID =
	 * GTUtility.ItemId.createNoCopy(s); slots.merge(sID, s.stackSize,
	 * Integer::sum); if (!stacks.containsKey(sID)) stacks.put(sID, s);
	 * order.add(sID); mInventory[i] = null; } int slotindex = 0; for
	 * (GTUtility.ItemId sID : order) { int toSet = slots.get(sID); if (toSet ==
	 * 0) continue; int slot = validSlots.get(slotindex); slotindex++;
	 * mInventory[slot] = stacks.get(sID) .copy(); toSet = Math.min(toSet,
	 * mInventory[slot].getMaxStackSize()); mInventory[slot].stackSize = toSet;
	 * slots.merge(sID, toSet, (a, b) -> a - b); } }
	 */
	CheckRecipeResult lastresult;

	@Override
	public final CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {

		if (recipe) {
			recipe = false;
			lastresult = endRecipeProcessingImpl(controller);
			if (lastresult.wasSuccessful() == false)
				controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
			return lastresult;
		}

		return lastresult;
	}

	public CheckRecipeResult endRecipeProcessingImpl(MTEMultiBlockBase controller) {
		this.markDirty();
		updateSlots();
		boolean success = shared.endRecipeProcessing(controller);
		if (!success)
			return CheckRecipeResultRegistry.CRASH;
		return CheckRecipeResultRegistry.SUCCESSFUL;
	}

	@Override
	public int getInventoryStackLimit() {

		return 64 + 64 * Math.max(0, mTier - 3) * Math.max(0, mTier - 3);
	}

	public static int getInventoryStackLimit(int mTier) {

		return 64 + 64 * Math.max(0, mTier - 3) * Math.max(0, mTier - 3);
	}

	public int fluidBuff() {

		return 1 << shared.fluidCapUpgrades;

	}

	public int getInventoryFluidLimit() {

		long val = fluidBuff() * (int) (4000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1));

		return (int) Math.min(val, Integer.MAX_VALUE);
	}

	public static int getInventoryFluidLimit(int mTier, boolean mMultiFluid) {

		return (int) (4000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1));
	}


	public int page(){return 1;}
	// insertion
	public static final int INSERTION = 2001;



	public final static int SHARED_ITEM = 654321;



	@Override
	public boolean allowSelectCircuit() {

		return shared.isDummy();
	}



	@Override
	public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {
		// TODO Auto-generated method stub
		return super.canInsertItem(aIndex, aStack, ordinalSide);
	}

	@Override
	public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {
		// TODO Auto-generated method stub
		return super.canExtractItem(aIndex, aStack, ordinalSide);
	}

	@Override
	public boolean isValidSlot(int aIndex) {
		return aIndex < getCircuitSlot();
	}

	@Override
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
		if (aIndex >= getCircuitSlot())
			return false;
		return side == getBaseMetaTileEntity().getFrontFacing();
	}

	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
		return side == getBaseMetaTileEntity().getFrontFacing() && aIndex < getCircuitSlot()
				&& (mRecipeMap == null || disableFilter || mRecipeMap.containsInput(aStack))
				&& (disableLimited || limitedAllowPutStack(aIndex, aStack));
	}

	public OptioanlSharedContents shared = new OptioanlSharedContents();

	public class OptioanlSharedContents {

		public int fluidCapUpgrades;
		ArrayList<ItemStack> shadowItems = new ArrayList<>();
		ArrayList<FluidStack> shadowFluid = new ArrayList<>();
		ArrayList<ItemStack> cachedItems = new ArrayList<>();
		ArrayList<FluidStack> cachedFluid = new ArrayList<>();
		ArrayList<ItemStack> markedItems = new ArrayList<>();
		ArrayList<FluidStack> markedFluid = new ArrayList<>();

		public void reinit() {

			while (circuitInv.size() < circuitUpgrades)
				circuitInv.add(null);
			while (markedItems.size() < itemMEUpgrades)
				markedItems.add(null);
			while (markedFluid.size() < fluidMEUpgrades)
				markedFluid.add(null);

		}

		public FluidStackTank createTankForFluidStack(int slotIndex, int capacity) {
			return new FluidStackTank(() -> markedFluid.get(slotIndex), (stack) -> {

				markedFluid.set(slotIndex, stack);
			}, capacity);
		}

		public boolean endRecipeProcessing(MTEMultiBlockBase controller) {
			if (isDummy())
				return true;
			boolean storageMissing = false;
			Net net = getNetwork();
			IStorageGrid cahce = null;
			if (net == null)
				storageMissing = true;
			else {
				cahce = net.g.getCache(IStorageGrid.class);
				if (cahce == null)
					storageMissing = true;
			}

			for (int i = 0; i < markedItems.size(); i++) {
				ItemStack sis = shadowItems.get(i);
				ItemStack cis = cachedItems.get(i);
				if (sis != null) {
					int consumed = sis.stackSize - Optional.ofNullable(cis).map(s -> s.stackSize).orElse(0);
					if (consumed > 0 && storageMissing)
						return false;
					ItemStack extract = sis.copy();
					extract.stackSize = consumed;

					IAEItemStack whatweget = cahce.getItemInventory().extractItems(AEItemStack.create(extract),
							Actionable.MODULATE, new MachineSource(net.h));
					int numweget = 0;
					if (whatweget != null)
						numweget = (int) whatweget.getStackSize();
					if (numweget != consumed) {
						return false;
					}
				}
			}
			for (int i = 0; i < markedFluid.size(); i++) {
				FluidStack sis = shadowFluid.get(i);
				FluidStack cis = cachedFluid.get(i);
				if (sis != null) {
					int consumed = sis.amount - Optional.ofNullable(cis).map(s -> s.amount).orElse(0);
					if (consumed > 0 && storageMissing)
						return false;
					FluidStack extract = sis.copy();
					extract.amount = consumed;

					IAEFluidStack whatweget = cahce.getFluidInventory().extractItems(AEFluidStack.create(extract),
							Actionable.MODULATE, new MachineSource(net.h));
					int numweget = 0;
					if (whatweget != null)
						numweget = (int) whatweget.getStackSize();
					if (numweget != consumed) {
						return false;
					}
				}
			}

			return true;
		}

		public void startRecipeProcessing() {
			if (isDummy())
				return;

			IStorageGrid cahce = (IStorageGrid) Optional.ofNullable(getNetwork()).map(s -> s.g)
					.map(s -> s.getCache(IStorageGrid.class)).orElse(null);

			shadowItems.clear();
			shadowFluid.clear();
			cachedItems.clear();
			cachedFluid.clear();
			HashSet<Object> dup = new HashSet<>();
			for (int i = 0; i < markedItems.size(); i++) {
				ItemStack is = markedItems.get(i);
				if (is == null || cahce == null) {
					shadowItems.add(null);
					cachedItems.add(null);
				} else {
					AEItemStack toextract = AEItemStack.create(is);
					toextract.setStackSize(1);
					if (!dup.add(toextract)) {
						shadowItems.add(null);
						cachedItems.add(null);
						markedItems.set(i, null);
						continue;
					}
					;
					ItemStack ris = (Optional
							.ofNullable(cahce.getItemInventory().getStorageList().findPrecise(toextract))
							.map(IAEItemStack::getItemStack).orElse(null));
					shadowItems.add(ris);
					cachedItems.add(Optional.ofNullable(ris).map(s -> s.copy()).orElse(null));
				}
			}
			dup.clear();
			for (int i = 0; i < markedFluid.size(); i++) {
				FluidStack is = markedFluid.get(i);
				if (is == null || cahce == null) {
					shadowFluid.add(null);
					cachedFluid.add(null);
				} else {
					AEFluidStack toextract = AEFluidStack.create(is);
					toextract.setStackSize(1);
					if (!dup.add(toextract)) {
						shadowFluid.add(null);
						cachedFluid.add(null);
						markedFluid.set(i, null);
						continue;
					}
					;
					FluidStack ris = (Optional
							.ofNullable(cahce.getFluidInventory().getStorageList().findPrecise(toextract))
							.map(IAEFluidStack::getFluidStack).orElse(null));
					shadowFluid.add(ris);
					cachedFluid.add(Optional.ofNullable(ris).map(s -> s.copy()).orElse(null));
				}
			}

		}

		public void onDestroy() {
			IGregTechTileEntity te = getBaseMetaTileEntity();
			if (circuitUpgrades > 0)
				te.getWorld().spawnEntityInWorld(new EntityItem(te.getWorld(), te.getXCoord(), te.getYCoord(),
						te.getZCoord(), new ItemStack(MyMod.upgrades, circuitUpgrades, 0)));
			if (itemMEUpgrades > 0)
				te.getWorld().spawnEntityInWorld(new EntityItem(te.getWorld(), te.getXCoord(), te.getYCoord(),
						te.getZCoord(), new ItemStack(MyMod.upgrades, itemMEUpgrades, 1)));
			if (fluidMEUpgrades > 0)
				te.getWorld().spawnEntityInWorld(new EntityItem(te.getWorld(), te.getXCoord(), te.getYCoord(),
						te.getZCoord(), new ItemStack(MyMod.upgrades, fluidMEUpgrades, 2)));
		}

		public NBTTagCompound serList(ArrayList<ItemStack> ls) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("len", ls.size());
			for (int i = 0; i < ls.size(); i++) {
				if (ls.get(i) != null)
					tag.setTag("i" + i, ls.get(i).writeToNBT(new NBTTagCompound()));
			}
			return tag;
		}

		public NBTTagCompound serListF(ArrayList<FluidStack> ls) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("len", ls.size());
			for (int i = 0; i < ls.size(); i++) {
				if (ls.get(i) != null)
					tag.setTag("i" + i, ls.get(i).writeToNBT(new NBTTagCompound()));
			}
			return tag;
		}

		public ArrayList<ItemStack> deserList(NBTTagCompound tag) {
			ArrayList<ItemStack> ls = new ArrayList<ItemStack>();
			int len = tag.getInteger("len");
			for (int i = 0; i < len; i++) {
				ls.add(ItemStack.loadItemStackFromNBT(tag.getCompoundTag("i" + i)));
			}
			return ls;
		}

		public ArrayList<FluidStack> deserListF(NBTTagCompound tag) {
			ArrayList<FluidStack> ls = new ArrayList<FluidStack>();
			int len = tag.getInteger("len");
			for (int i = 0; i < len; i++) {
				ls.add(FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("i" + i)));
			}
			return ls;
		}

		boolean broken;

		public ItemStack[] getDisplayItems() {
			ArrayList<ItemStack> all = new ArrayList<>();
			all.addAll(circuitInv);
			all.add(mInventory[getCircuitSlot()]);
			all.addAll(markedItems);
			all.removeIf(Objects::isNull);
			return all.toArray(new ItemStack[0]);
		}

		public FluidStack[] getDisplayFluid() {

			ArrayList<FluidStack> all = new ArrayList<>();
			all.addAll(markedFluid);
			all.removeIf(Objects::isNull);
			return all.toArray(new FluidStack[0]);
		}

		private void broken() {
			MyMod.LOG.fatal("FAILED TO UPDATE ME INPUTS!");
			MyMod.LOG.fatal("basemeta:" + getBaseMetaTileEntity());
			Thread.dumpStack();
			if (getBaseMetaTileEntity() != null) {
				MyMod.LOG.fatal("same:" + (getBaseMetaTileEntity().getMetaTileEntity() == DualInputHatch.this)
						+ ("valid:" + DualInputHatch.this.isValid()) + ("x:" + getBaseMetaTileEntity().getXCoord())
						+ ("y:" + getBaseMetaTileEntity().getYCoord()) + ("z:" + getBaseMetaTileEntity().getZCoord())
						+ ("w:" + getBaseMetaTileEntity().getWorld().provider.dimensionId));

			}

			MyMod.LOG.fatal(shadowFluid.toString());
			MyMod.LOG.fatal(shadowItems.toString());
			MyMod.LOG.fatal(cachedFluid.toString());
			MyMod.LOG.fatal(cachedItems.toString());

		}

		public ItemStack[] getItems() {

			if (off) {
				return new ItemStack[0];
			}
			ArrayList<ItemStack> all = new ArrayList<>();
			all.addAll(circuitInv);
			all.add(mInventory[getCircuitSlot()]);
			if (recipe == false) {
				if (!broken)
					broken();
				// Thread.dumpStack();
				broken = true;
				all.removeIf(Objects::isNull);
				return all.toArray(new ItemStack[0]);
			}
			all.addAll(cachedItems);
			all.removeIf(Objects::isNull);
			return all.toArray(new ItemStack[0]);
		}

		public FluidStack[] getFluid() {
			if (off) {
				return new FluidStack[0];
			}
			if (recipe == false) {
				if (!broken)
					broken();
				// Thread.dumpStack();
				broken = true;
				return new FluidStack[0];
			}
			ArrayList<FluidStack> all = new ArrayList<>();
			all.addAll(cachedFluid);
			all.removeIf(Objects::isNull);
			return all.toArray(new FluidStack[0]);
		}

		public boolean isDummy() {
			return circuitUpgrades + itemMEUpgrades + fluidMEUpgrades == 0;
		}

		public int circuitUpgrades;
		public int itemMEUpgrades;
		public int fluidMEUpgrades;
		public int infbufUpgrades;
		public ArrayList<ItemStack> circuitInv = new ArrayList<>();

		public void clearCircuit() {
			mInventory[getCircuitSlot()] = null;
			circuitInv.clear();
			for (int i = 0; i < circuitUpgrades; i++) {
				circuitInv.add(null);
			}
		}

		public void setCircuit(int index, ItemStack is) {
			if (index == 0) {
				mInventory[getCircuitSlot()] = is;
				return;
			}
			circuitInv.set(index - 1, is);
		}

		public int sizeCircuit() {
			return circuitUpgrades + 1;
		}

		public NBTTagCompound ser() {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("fluidCapUpgrades", fluidCapUpgrades);
			tag.setInteger("circuitUpgrades", circuitUpgrades);
			tag.setInteger("itemMEUpgrades", itemMEUpgrades);
			tag.setInteger("fluidMEUpgrades", fluidMEUpgrades);
			tag.setInteger("infbufUpgrades", infbufUpgrades);
			tag.setTag("circuitInv", serList(circuitInv));
			tag.setTag("markedItems", serList(markedItems));
			tag.setTag("markedFluid", serListF(markedFluid));
			return tag;
		}

		public void deser(NBTTagCompound tag) {
			fluidCapUpgrades = tag.getInteger("fluidCapUpgrades");
			circuitUpgrades = tag.getInteger("circuitUpgrades");
			itemMEUpgrades = tag.getInteger("itemMEUpgrades");
			fluidMEUpgrades = tag.getInteger("fluidMEUpgrades");
			infbufUpgrades = tag.getInteger("infbufUpgrades");
			circuitInv = deserList(tag.getCompoundTag("circuitInv"));
			markedItems = deserList(tag.getCompoundTag("markedItems"));
			markedFluid = deserListF(tag.getCompoundTag("markedFluid"));
			while (circuitInv.size() > circuitUpgrades)
				circuitInv.remove(circuitInv.size() - 1);
			while (circuitInv.size() < circuitUpgrades)
				circuitInv.add(null);
			while (markedItems.size() > itemMEUpgrades)
				markedItems.remove(markedItems.size() - 1);
			while (markedItems.size() < itemMEUpgrades)
				markedItems.add(null);
			while (markedFluid.size() > fluidMEUpgrades)
				markedFluid.remove(markedFluid.size() - 1);
			while (markedFluid.size() < fluidMEUpgrades)
				markedFluid.add(null);

		}

		public void install(ItemStack heldItem) {
			int damage = heldItem.getItemDamage();
			if (damage == 0) {
				if (circuitUpgrades < 3) {

					circuitUpgrades++;
					heldItem.stackSize--;
					successInstall();

				}
			}
			if (damage == 1) {
				if (itemMEUpgrades + fluidMEUpgrades < 4) {

					itemMEUpgrades++;
					heldItem.stackSize--;
					successInstall();

				}
			}
			if (damage == 2) {
				if (itemMEUpgrades + fluidMEUpgrades < 4) {

					fluidMEUpgrades++;
					heldItem.stackSize--;
					successInstall();

				}
			}
			if (damage == 3) {
				if (infbufUpgrades < 1) {

					infbufUpgrades++;
					heldItem.stackSize--;
					successInstall();

				}
			}

		}

		@SuppressWarnings("unchecked")
		public void successInstall() {
			reinit();
			GTUtility.sendSoundToPlayers(getBaseMetaTileEntity().getWorld(), SoundResource.IC2_TOOLS_WRENCH, 1.0F, -1,
					getBaseMetaTileEntity().getXCoord(), getBaseMetaTileEntity().getYCoord(),
					getBaseMetaTileEntity().getZCoord());
			// close all GUIs of this hatch, because they have to be
			// re-generated with new context
			try {
				if (!getBaseMetaTileEntity().getWorld().isRemote)
					getBaseMetaTileEntity().getWorld().playerEntities.forEach(s -> {
						EntityPlayer player = (EntityPlayer) s;
						if (player.openContainer instanceof ModularUIContainer) {
							ModularUIContainer m = (ModularUIContainer) player.openContainer;
							for (Widget w : m.getContext().getMainWindow().getChildren()) {
								if (w instanceof MarkerWidget && player instanceof EntityPlayerMP) {
									if (((MarkerWidget) w).thiz == DualInputHatch.this) {
										((EntityPlayerMP) player).closeContainer();
										break;
									}

								}

							}

						}

					});
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
			float aX, float aY, float aZ) {
		if (aPlayer.getHeldItem() != null && aPlayer.getHeldItem().getItem() == MyMod.upgrades) {
			shared.install(aPlayer.getHeldItem());
			return true;

		}
		return super.onRightclick(aBaseMetaTileEntity, aPlayer, side, aX, aY, aZ);
	}

	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		if (!aBaseMetaTileEntity.isClientSide())
			if (!shared.isDummy())
				MyMod.net.sendTo(new UpgradesMessage(aBaseMetaTileEntity.getXCoord(), aBaseMetaTileEntity.getYCoord(),
						aBaseMetaTileEntity.getZCoord(), this), (EntityPlayerMP) aPlayer);
		return super.onRightclick(aBaseMetaTileEntity, aPlayer);
	}

	@Override
	public void onBlockDestroyed() {
		shared.onDestroy();

	}

	public static class Net {

		public IGrid g;
		public IActionHost h;

		public Net(IGrid g, IActionHost h) {
			this.g = g;
			this.h = h;
		}
	}

	public Net getNetwork() {
		// DO NOT do the same way that StorageBus does,to skip permission check
		IGregTechTileEntity self = getBaseMetaTileEntity();
		final TileEntity te = self.getWorld().getTileEntity(self.getXCoord() + self.getFrontFacing().offsetX,
				self.getYCoord() + self.getFrontFacing().offsetY, self.getZCoord() + self.getFrontFacing().offsetZ);

		Net g = null;
		if (te instanceof IPartHost) {

			try {
				final Object part = ((IPartHost) te).getPart(self.getFrontFacing().getOpposite());
				if (part instanceof IInterfaceHost) {

					g = new Net(((IInterfaceHost) part).getGridNode(ForgeDirection.UP).getGrid(), (IInterfaceHost) part

					);

				}

			} catch (Exception e) {

			}
		}

		if (g == null) {
			if (te instanceof IInterfaceHost) {

				IGridNode n = ((IInterfaceHost) te).getGridNode(ForgeDirection.UP);
				if (n != null)
					g = new Net(n.getGrid(), ((IInterfaceHost) te));

			}

		}

		if (g == null) {
			Object optCover = AECover.getCoverData(
					this.getBaseMetaTileEntity().getCoverAtSide(this.getBaseMetaTileEntity().getFrontFacing()));
			if (optCover instanceof AECover.Data) {

				IInterfaceHost iface = ((AECover.Data) optCover).getInterfaceOrNull();
				if (iface != null) {
					g = new Net(iface.getGridNode(ForgeDirection.UP).getGrid(), iface);

				}
			}

		}

		return g;

	}

	@Override
	public FluidStack drain(ForgeDirection side, int maxDrain, boolean doDrain) {

		return drain(maxDrain, doDrain);
	}

	@Override
	public boolean canDrain(ForgeDirection side, Fluid aFluid) {

		return true;
	}

	@Override
	public ItemStack[] getSharedItems() {

		return new ItemStack[0];
	}

	public boolean hasBuffer() {
		return false;
	}/*
		 * public void setProcessingLogics(List<ProcessingLogic>
		 * processingLogics) { this.processingLogics = processingLogics; }public
		 * List<ProcessingLogic> getProcessingLogics() { return
		 * processingLogics; }
		 */

	@Override
	public void setProcessingLogic(ProcessingLogic pl) {
		if (!hasBuffer())
			return;
		if (!processingLogics.contains(pl)) {
			processingLogics.add(Objects.requireNonNull(pl));
		}

	}/*
		 * public void resetMulti() { for ( IDualInputInventory o :
		 * inventoriesReal()) { resetMulti(o); } } private void
		 * resetMulti(IDualInputInventory dual) { for (ProcessingLogic pl :
		 * processingLogics) { pl.clearCraftingPatternRecipeCache(dual); } }
		 */

	@Override
	public ItemStack getMachineCraftingIcon() {
		// TODO Auto-generated method stub
		return super.getMachineCraftingIcon();
	}

	public List<ProcessingLogic> processingLogics = new ArrayList<>();

	boolean off;

	@Override
	public void trunOffME() {
		off = true;
	}

	@Override
	public void trunONME() {

		off = false;
	}

	/*
	 * @Override protected boolean forceUseMui2() {
	 *
	 * return mui2.contains(this.getClass()); }
	 */
	@Override
	protected boolean useMui2() {

		return mui2.contains(this.getClass());
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface MUI2Compat {

	}

	public static HashSet<Class> mui2 = new HashSet();
	{
		
		
		Class c=this.getClass();
		while(c.isLocalClass()||c.isMemberClass()||c.isAnonymousClass()) {
			
			c=c.getSuperclass();
		}
		if (c.getAnnotation(MUI2Compat.class) != null) {
			mui2.add(c);
			
			
		}else {
			
			MyMod.LOG.info(c+" "+(c==this.getClass()?"":"("+this.getClass()+")")+" has no MUI2 support.");
			
			
		}
		;
	}

	@Override
	public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {

		ModularPanel builder = mui2ct.create(data, syncManager, uiSettings);
		 mui2ct.buildUI(builder,data, syncManager,  uiSettings);return builder;
	}




	public class MUI1Container {
		public DualInputHatch this$(){
			return DualInputHatch.this;
		}	public SlotWidget catalystSlot(IItemHandlerModifiable inventory, int slot) {
			return (SlotWidget) new SlotWidget(new BaseSlot(inventory, slot) {

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
					.setHandlePhantomActionClient(
							true)
					.setGTTooltip(() -> new TooltipData(
							Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
									LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")),
							Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
									LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"))));
		}

		public Widget circuitSlot(IItemHandlerModifiable inventory, int slot) {

			List<String> tooltip = Arrays.asList(
					EnumChatFormatting.DARK_GRAY + (LangManager.translateToLocal("GT5U.machines.select_circuit.tooltip.1")),

					EnumChatFormatting.DARK_GRAY
							+ (LangManager.translateToLocal("GT5U.machines.select_circuit.tooltip.3")));

			return new SlotWidget(new BaseSlot(inventory, slot, true)) {

				@Override
				protected void phantomClick(ClickData clickData, ItemStack cursorStack) {
					final ItemStack newCircuit;
					if (clickData.shift) {
						if (clickData.mouseButton == 0) {
							// if (NetworkUtils.isClient() && !dialogOpened.get()) {
							// openSelectCircuitDialog(getContext(), dialogOpened);
							// }
							return;
						} else {
							newCircuit = null;
						}
					} else {
						final List<ItemStack> tCircuits = GTUtility.getAllIntegratedCircuits();
						final int index = GTUtility.findMatchingStackInList(tCircuits, cursorStack);
						if (index < 0) {
							int curIndex = GTUtility.findMatchingStackInList(tCircuits, inventory.getStackInSlot(slot)) + 1;
							if (clickData.mouseButton == 0) {
								curIndex += 1;
							} else {
								curIndex -= 1;
							}
							curIndex = Math.floorMod(curIndex, tCircuits.size() + 1) - 1;
							newCircuit = curIndex < 0 ? null : tCircuits.get(curIndex);
						} else {
							// set to whatever it is
							newCircuit = tCircuits.get(index);
						}
					}
					inventory.setStackInSlot(slot, GTUtility.copyAmount(0, newCircuit));

				}

				@Override
				protected void phantomScroll(int direction) {
					phantomClick(new ClickData(direction > 0 ? 1 : 0, false, false, false));
				}

				@Override
				public List<String> getExtraTooltip() {
					return tooltip;
				}

			}.setOverwriteItemStackTooltip(list -> {
				list.removeIf(line -> line.contains(LangManager.translateToLocal("gt.integrated_circuit.tooltip.0"))
						|| line.contains(LangManager.translateToLocal("gt.integrated_circuit.tooltip.1")));
				return list;
			}).disableShiftInsert().setHandlePhantomActionClient(true).setGTTooltip(() -> new TooltipData(tooltip, tooltip))
					.setBackground(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_INT_CIRCUIT);
		}

		/*
			 * private ModularPanel createSharedItemWindow2(PanelSyncManager
			 * syncManager) {
			 *
			 *
			 * ModularPanel builder =new ModularPanel("ins_window"); builder.size(36
			 * + 18 * 3, 36 + 18 * 4); final ItemStackHandler inventoryHandler = new
			 * ItemStackHandler(mInventory.length - (1));
			 *
			 *
			 *
			 *
			 * //
			 * builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
			 * // builder.setGuiTint(getGUIColorization());
			 *
			 *
			 * builder.widget(circuitSlot(this.getInventoryHandler(),
			 * getCircuitSlot()).setPos(8 - 1, 8 - 1)); for (int i = 0; i <
			 * shared.circuitUpgrades; i++) builder.widget(catalystSlot(new
			 * ItemStackHandler(shared.circuitInv), i).setPos(8 - 1, 8 - 1 + 18 + 18
			 * * i));
			 *
			 * int posoffset = 0; for (int i = 0; i < shared.itemMEUpgrades; i++) {
			 * final int fi = i; builder.widget( SlotWidget.phantom(new
			 * ItemStackHandler(shared.markedItems), i) .addTooltips( (this
			 * instanceof BufferedDualInputHatch) ? ImmutableList.of(
			 * StatCollector.translateToLocal(
			 * "programmable_hatches.gt.item.pull.me.0"),
			 * StatCollector.translateToLocal(
			 * "programmable_hatches.gt.item.pull.me.1"),
			 * StatCollector.translateToLocal(
			 * "programmable_hatches.gt.item.pull.me.2")) : ImmutableList.of(
			 * StatCollector.translateToLocal(
			 * "programmable_hatches.gt.item.pull.me.0"),
			 * StatCollector.translateToLocal(
			 * "programmable_hatches.gt.item.pull.me.1"))
			 *
			 * ) .setPos(8 - 1 + 18 + 4, 8 - 1 + 18 * posoffset)); builder.widget(
			 * new DrawableWidget().setDrawable(ModularUITextures.ARROW_RIGHT)
			 *
			 * .setPos(8 - 1 + 18 * 2 + 4, 8 - 1 + 18 * posoffset) .setSize(18,
			 * 18)); builder.widget(new SlotWidget(new BaseSlot(new
			 * ItemStackHandler(1), 0)) {
			 *
			 * int cd = 0;
			 *
			 * public void detectAndSendChanges(boolean init) {
			 *
			 * if (cd-- < 0) { cd = 10; ItemStack is = null; Net net = getNetwork();
			 * if (net != null) { IStorageGrid cahce =
			 * net.g.getCache(IStorageGrid.class); if (cahce != null) { IAEItemStack
			 * aeis = cahce.getItemInventory() .getStorageList()
			 * .findPrecise(AEItemStack.create(shared.markedItems.get(fi))); if
			 * (aeis != null) is = aeis.getItemStack(); } } ((ItemStackHandler)
			 * ((BaseSlot) this.getMcSlot()).getItemHandler()).setStackInSlot(
			 * this.getMcSlot() .getSlotIndex(),
			 *
			 * is); }
			 *
			 * super.detectAndSendChanges(init); };
			 *
			 * }.disableInteraction() .setPos(8 - 1 + 18 * 3 + 4, 8 - 1 + 18 *
			 * posoffset)); posoffset++; }
			 *
			 * for (int i = 0; i < shared.fluidMEUpgrades; i++) { final int fi = i;
			 * builder.widget(new FluidSlotWidget(shared.createTankForFluidStack(i,
			 * 1)) {
			 *
			 * { setPhantom(true); }
			 *
			 * @Override protected void tryClickPhantom(ClickData clickData,
			 * ItemStack cursorStack) { if (clickData.mouseButton != 0) return;
			 *
			 * FluidStack heldFluid = getFluidForPhantomItem(cursorStack); if
			 * (cursorStack == null) { shared.markedFluid.set(fi, null); } else {
			 *
			 * shared.markedFluid.set(fi, heldFluid); } if
			 * (getBaseMetaTileEntity().isServerSide()) {
			 *
			 * detectAndSendChanges(false); } }
			 *
			 * @Override protected void tryScrollPhantom(int direction) {}
			 *
			 * }
			 *
			 * .addTooltips( ImmutableList.of( StatCollector.translateToLocal(
			 * "programmable_hatches.gt.item.pull.me.0"),
			 * StatCollector.translateToLocal(
			 * "programmable_hatches.gt.item.pull.me.1"))) .setPos(8 - 1 + 18 + 4, 8
			 * - 1 + 18 * posoffset)); builder.widget( new
			 * DrawableWidget().setDrawable(ModularUITextures.ARROW_RIGHT)
			 *
			 * .setPos(8 - 1 + 18 * 2 + 4, 8 - 1 + 18 * posoffset) .setSize(18,
			 * 18)); FluidTank ft; builder.widget(new FluidSlotWidget(ft = new
			 * FluidTank(Integer.MAX_VALUE)) {
			 *
			 * int cd = 0; { setPhantom(true); }
			 *
			 * @Override public void buildTooltip(List<Text> tooltip) { FluidStack
			 * fluid = getContent(); if (fluid != null) { addFluidNameInfo(tooltip,
			 * fluid); tooltip.add(Text.localised("modularui.fluid.phantom.amount",
			 * fluid.amount)); addAdditionalFluidInfo(tooltip, fluid); if
			 * (!Interactable.hasShiftDown()) { tooltip.add(Text.EMPTY);
			 * tooltip.add(Text.localised("modularui.tooltip.shift")); } } else {
			 * tooltip.add( Text.localised("modularui.fluid.empty")
			 * .format(EnumChatFormatting.WHITE)); } }
			 *
			 * @Override protected void tryClickPhantom(ClickData clickData,
			 * ItemStack cursorStack) {}
			 *
			 * @Override protected void tryScrollPhantom(int direction) {}
			 *
			 * public void detectAndSendChanges(boolean init) {
			 *
			 * if (cd-- < 0) { cd = 10; FluidStack is = null; Net net =
			 * getNetwork(); if (net != null) { IStorageGrid cahce =
			 * net.g.getCache(IStorageGrid.class); if (cahce != null) {
			 * IAEFluidStack aeis = cahce.getFluidInventory() .getStorageList()
			 * .findPrecise(AEFluidStack.create(shared.markedFluid.get(fi))); if
			 * (aeis != null) is = aeis.getFluidStack(); } } ft.setFluid(is); //
			 * todo // this.setfl
			 *
			 * }
			 *
			 * super.detectAndSendChanges(init); };
			 *
			 * }
			 *
			 * .setPos(8 - 1 + 18 * 3 + 4, 8 - 1 + 18 * posoffset)); posoffset++; }
			 *
			 * builder.widget(
			 * TextWidget.localised("proghatch.dualhatch.optinv.broken")
			 * .setEnabled((a) -> shared.broken)); builder.widget( new
			 * FakeSyncWidget.BooleanSyncer(() -> shared.broken, s -> shared.broken
			 * = s)
			 *
			 * .setSynced(true, false));
			 *
			 * return builder.build(); }
			 */	private CycleButtonWidget createButton(Supplier<Integer> getter, IntConsumer setter,
				Function<Integer, IDrawable> picture, List<String> tooltip, int offset, int len) {
			return (CycleButtonWidget) new CycleButtonWidget().setLength(len).setGetter(getter)
					.setSetter(s -> setter.accept(s))

					.setTextureGetter(picture).setBackground(GTUITextures.BUTTON_STANDARD)
					.setTooltipShowUpDelay(TOOLTIP_DELAY).setPos(7 + offset * 18, 62).setSize(18, 18).addTooltips(tooltip);
		}

		private Widget createButton(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
				Supplier<GTTooltipDataCache.TooltipData> tooltipDataSupplier, int offset) {
			return new CycleButtonWidget().setToggle(getter, setter).setStaticTexture(picture)
					.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
					.setPos(7 + offset * 18, 62).setSize(18, 18).setGTTooltip(tooltipDataSupplier);
		}

		/*private Widget createButton(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
				List<String> tooltip, int offset) {
			return new CycleButtonWidget().setToggle(getter, setter).setStaticTexture(picture)
					.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
					.setPos(7 + offset * 18, 62).setSize(18, 18).addTooltips(tooltip);
		}*/

		private Widget createButtonForbidden(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
				Supplier<GTTooltipDataCache.TooltipData> tooltipDataSupplier, int offset) {
			return new CycleButtonWidget() {

				public com.gtnewhorizons.modularui.api.widget.Interactable.ClickResult onClick(int buttonId,
						boolean doubleClick) {
					return ClickResult.SUCCESS;
				};
			}.setTextureGetter(s -> IDrawable.EMPTY).setToggle(getter, setter)


					.setBackground(GTUITextures.BUTTON_STANDARD_PRESSED, picture, GTUITextures.OVERLAY_BUTTON_FORBIDDEN)

					.setTooltipShowUpDelay(TOOLTIP_DELAY).setPos(7 + offset * 18, 62).setSize(18, 18)
					.setGTTooltip(tooltipDataSupplier);


		}	protected Widget createButtonSharedItem() {
			return new ButtonWidget() {

				{
					this.setTicker(s -> {

						setBackground(GTUITextures.BUTTON_STANDARD,

								mInventory[getCircuitSlot()] == null ? GTUITextures.OVERLAY_SLOT_CIRCUIT :

										new ItemDrawable(mInventory[getCircuitSlot()]
						// new ItemStack(Blocks.hopper)

						));

					});
				}
			}.setOnClick((clickData, widget) -> {
				if (clickData.mouseButton == 0) {
					if (!widget.isClient())
						widget.getContext().openSyncedWindow(SHARED_ITEM);
				}
			}).setPlayClickSound(true)
					.setBackground(GTUITextures.BUTTON_STANDARD, new ItemDrawable(new ItemStack(Blocks.hopper)))

					.setEnabled(s -> {
						return !s.getContext().isWindowOpen(SHARED_ITEM);
					})

					.addTooltips(ImmutableList.of(LangManager.translateToLocal("programmable_hatches.gt.shared")))
					.setPos(/*
							 * extraCircuit? new Pos2d(
							 * getCircuitSlotX()-18,getCircuitSlotY() ):
							 */
							new Pos2d(getCircuitSlotX() - 1, getCircuitSlotY()/*-18*/ - 1)

					).setSize(18, 18);

		}
		private void addUIWidgets0(ModularWindow.Builder builder, UIBuildContext buildContext) {
			// buildContext.addCloseListener(() -> uiButtonCount = 0);
			// addSortStacksButton(builder);
			// addOneStackLimitButton(builder);
			switch (slotTierOverride(mTier)) {
			case 0:
				add1by1Slot(builder);
				break;
			case 1:
				add2by2Slots(builder);
				break;
			case 2:
				add3by3Slots(builder);
				break;
			default:
				add4by4Slots(builder);
			}
		} public void add1by1Slot(ModularWindow.Builder builder, IDrawable... background) {
			final ItemStackHandler inventoryHandler = getInventoryHandler();
			if (inventoryHandler == null)
				return;

			if (background.length == 0) {
				background = new IDrawable[] { getGUITextureSet().getItemSlot() };
			}
			builder.widget(
					SlotGroup.ofItemHandler(inventoryHandler, 1).slotCreator(BaseSlotPatched.newInst(inventoryHandler))
							.startFromSlot(0).endAtSlot(0).background(background).build().setPos(79, 34));
		}

		public void add2by2Slots(ModularWindow.Builder builder, IDrawable... background) {
			final ItemStackHandler inventoryHandler = getInventoryHandler();
			if (inventoryHandler == null)
				return;

			if (background.length == 0) {
				background = new IDrawable[] { getGUITextureSet().getItemSlot() };
			}
			builder.widget(
					SlotGroup.ofItemHandler(inventoryHandler, 2).slotCreator(BaseSlotPatched.newInst(inventoryHandler))
							.startFromSlot(0).endAtSlot(3).background(background).build().setPos(70, 25));
		}

		public void add3by3Slots(ModularWindow.Builder builder, IDrawable... background) {
			final ItemStackHandler inventoryHandler = getInventoryHandler();
			if (inventoryHandler == null)
				return;

			if (background.length == 0) {
				background = new IDrawable[] { getGUITextureSet().getItemSlot() };
			}
			builder.widget(
					SlotGroup.ofItemHandler(inventoryHandler, 3).slotCreator(BaseSlotPatched.newInst(inventoryHandler))
							.startFromSlot(0).endAtSlot(8).background(background).build().setPos(61, 16));
		}

		public void add4by4Slots(ModularWindow.Builder builder, IDrawable... background) {
			final ItemStackHandler inventoryHandler = getInventoryHandler();
			if (inventoryHandler == null)
				return;

			if (background.length == 0) {
				background = new IDrawable[] { getGUITextureSet().getItemSlot() };
			}
			final Scrollable scrollable = new Scrollable().setVerticalScroll();
			scrollable.setSize(18*4, 18*4);
			scrollable.widget(
					SlotGroup.ofItemHandler(inventoryHandler, 4).slotCreator(BaseSlotPatched.newInst(inventoryHandler))
							.startFromSlot(0).endAtSlot(page()*16-1).background(background).build());
			builder.widget(scrollable.setPos(52, 7));



		}
		private Widget createToggleButton(Supplier<Boolean> getter, Consumer<Boolean> setter, UITexture picture,
				Supplier<GTTooltipDataCache.TooltipData> tooltipDataSupplier, int uiButtonCount) {
			return new CycleButtonWidget().setToggle(getter, setter).setStaticTexture(picture)
					.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
					.setPos(7 + (uiButtonCount * BUTTON_SIZE), 62).setSize(BUTTON_SIZE, BUTTON_SIZE)
					.setGTTooltip(tooltipDataSupplier);
		}

		private void addSortStacksButton(ModularWindow.Builder builder) {
			builder.widget(createToggleButton(() -> !disableSort, val -> disableSort = !val,
					GTUITextures.OVERLAY_BUTTON_SORTING_MODE, () -> mTooltipCache.getData(SORTING_MODE_TOOLTIP), 0));
		}

		private void addOneStackLimitButton(ModularWindow.Builder builder) {
			builder.widget(createToggleButton(() -> !disableLimited, val -> {
				disableLimited = !val;
				updateSlots();
			}, GTUITextures.OVERLAY_BUTTON_ONE_STACK_LIMIT, () -> mTooltipCache.getData(ONE_STACK_LIMIT_TOOLTIP), 1));
		}

		protected Widget createButtonInsertion() {
			return new ButtonWidget().setOnClick((clickData, widget) -> {
				if (clickData.mouseButton == 0) {
					if (!widget.isClient())
						widget.getContext().openSyncedWindow(INSERTION);
				}
			}).setPlayClickSound(true)
					.setBackground(GTUITextures.BUTTON_STANDARD, new ItemDrawable(new ItemStack(Blocks.hopper)))
					.setEnabled(s -> {
						return !s.getContext().isWindowOpen(INSERTION);

					}).addTooltips(ImmutableList.of(LangManager.translateToLocal("programmable_hatches.gt.insertion")))
					.setPos(/*
							 * extraCircuit? new Pos2d(
							 * getCircuitSlotX()-18,getCircuitSlotY() ):
							 */
							new Pos2d(getCircuitSlotX() - 1, getCircuitSlotY() - 18 * 2 - 1
							// getGUIWidth() - 18 - 3, 30
							)

					).setSize(18, 18);

		}
		private ModularWindow createSharedItemWindow(UIBuildContext buildContext) {

			ModularWindow.Builder builder = ModularWindow.builder(36 + 18 * 3, 36 + 18 * 4);
			builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
			builder.setGuiTint(getGUIColorization());
			builder.setDraggable(true);

			builder.widget(circuitSlot(this$().getInventoryHandler(), getCircuitSlot()).setPos(8 - 1, 8 - 1));
			for (int i = 0; i < shared.circuitUpgrades; i++)
				builder.widget(catalystSlot(new ItemStackHandler(shared.circuitInv), i).setPos(8 - 1, 8 - 1 + 18 + 18 * i));

			int posoffset = 0;
			for (int i = 0; i < shared.itemMEUpgrades; i++) {
				final int fi = i;
				builder.widget(SlotWidget.phantom(new ItemStackHandler(shared.markedItems), i)
						.addTooltips((this$() instanceof BufferedDualInputHatch)
								? ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.0"),
										StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.1"),
										StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.2"))
								: ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.0"),
										StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.1"))

						).setPos(8 - 1 + 18 + 4, 8 - 1 + 18 * posoffset));
				builder.widget(new DrawableWidget().setDrawable(ModularUITextures.ARROW_RIGHT)

						.setPos(8 - 1 + 18 * 2 + 4, 8 - 1 + 18 * posoffset).setSize(18, 18));
				builder.widget(new SlotWidget(new BaseSlot(new ItemStackHandler(1), 0)) {

					int cd = 0;

					public void detectAndSendChanges(boolean init) {

						if (cd-- < 0) {
							cd = 10;
							ItemStack is = null;
							Net net = getNetwork();
							if (net != null) {
								IStorageGrid cahce = net.g.getCache(IStorageGrid.class);
								if (cahce != null) {
									IAEItemStack aeis = cahce.getItemInventory().getStorageList()
											.findPrecise(AEItemStack.create(shared.markedItems.get(fi)));
									if (aeis != null)
										is = aeis.getItemStack();
								}
							}
							((ItemStackHandler) ((BaseSlot) this.getMcSlot()).getItemHandler()).setStackInSlot(
									this.getMcSlot().getSlotIndex(),

									is);
						}

						super.detectAndSendChanges(init);
					};

				}.disableInteraction().setPos(8 - 1 + 18 * 3 + 4, 8 - 1 + 18 * posoffset));
				posoffset++;
			}

			for (int i = 0; i < shared.fluidMEUpgrades; i++) {
				final int fi = i;
				builder.widget(new FluidSlotWidget(shared.createTankForFluidStack(i, 1)) {

					{
						setPhantom(true);
					}

					@Override
					protected void tryClickPhantom(ClickData clickData, ItemStack cursorStack) {
						if (clickData.mouseButton != 0)
							return;

						FluidStack heldFluid = getFluidForPhantomItem(cursorStack);
						if (cursorStack == null) {
							shared.markedFluid.set(fi, null);
						} else {

							shared.markedFluid.set(fi, heldFluid);
						}
						if (getBaseMetaTileEntity().isServerSide()) {

							detectAndSendChanges(false);
						}
					}

					@Override
					protected void tryScrollPhantom(int direction) {
					}

				}

						.addTooltips(
								ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.0"),
										StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.1")))
						.setPos(8 - 1 + 18 + 4, 8 - 1 + 18 * posoffset));
				builder.widget(new DrawableWidget().setDrawable(ModularUITextures.ARROW_RIGHT)

						.setPos(8 - 1 + 18 * 2 + 4, 8 - 1 + 18 * posoffset).setSize(18, 18));
				FluidTank ft;
				builder.widget(new FluidSlotWidget(ft = new FluidTank(Integer.MAX_VALUE)) {

					int cd = 0;
					{
						setPhantom(true);
					}

					@Override
					public void buildTooltip(List<Text> tooltip) {
						FluidStack fluid = getContent();
						if (fluid != null) {
							addFluidNameInfo(tooltip, fluid);
							tooltip.add(Text.localised("modularui.fluid.phantom.amount", fluid.amount));
							addAdditionalFluidInfo(tooltip, fluid);
							if (!Interactable.hasShiftDown()) {
								tooltip.add(Text.EMPTY);
								tooltip.add(Text.localised("modularui.tooltip.shift"));
							}
						} else {
							tooltip.add(Text.localised("modularui.fluid.empty").format(EnumChatFormatting.WHITE));
						}
					}

					@Override
					protected void tryClickPhantom(ClickData clickData, ItemStack cursorStack) {
					}

					@Override
					protected void tryScrollPhantom(int direction) {
					}

					public void detectAndSendChanges(boolean init) {

						if (cd-- < 0) {
							cd = 10;
							FluidStack is = null;
							Net net = getNetwork();
							if (net != null) {
								IStorageGrid cahce = net.g.getCache(IStorageGrid.class);
								if (cahce != null) {
									IAEFluidStack aeis = cahce.getFluidInventory().getStorageList()
											.findPrecise(AEFluidStack.create(shared.markedFluid.get(fi)));
									if (aeis != null)
										is = aeis.getFluidStack();
								}
							}
							ft.setFluid(is);
							// todo
							// this.setfl

						}

						super.detectAndSendChanges(init);
					};

				}

						.setPos(8 - 1 + 18 * 3 + 4, 8 - 1 + 18 * posoffset));
				posoffset++;
			}

			builder.widget(TextWidget.localised("proghatch.dualhatch.optinv.broken").setEnabled((a) -> shared.broken));
			builder.widget(new FakeSyncWidget.BooleanSyncer(() -> shared.broken, s -> shared.broken = s)

					.setSynced(true, false));

			return builder.build();
		} protected ModularWindow createInsertionWindow(UIBuildContext buildContext) {
			int len = (int) Math.round(Math.sqrt(mInventory.length - 1));
			final int WIDTH = 18 * len + 6;
			final int HEIGHT = 18 * (len + 1) + 6;
			ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
			builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
			builder.setGuiTint(getGUIColorization());
			builder.setDraggable(true);

			final IItemHandlerModifiable inventoryHandler = new ItemStackHandler(mInventory.length - (1));
			IDrawable[] background = new IDrawable[] { getGUITextureSet().getItemSlot() };

			builder.widget(SlotGroup.ofItemHandler(inventoryHandler, len).widgetCreator(SlotWidget::new).startFromSlot(0)
					.endAtSlot(/* mInventory.length-2 */9999).background(background).build().setPos(3, 3)

			);

			builder.widget(new SyncedWidget() {

				ArrayList<Integer> toclear = new ArrayList<>(1);

				@Override
				public void detectAndSendChanges(boolean init) {
					toclear.clear();
					for (int i = 0; i < inventoryHandler.getSlots(); i++) {
						int fi = i;

						Optional.ofNullable(inventoryHandler.getStackInSlot(i)).filter(s -> s.stackSize > 0)
								.ifPresent(s -> {
									markDirty();
									if (mInventory[fi] != null) {
										int oldsize = s.stackSize;
										s.stackSize = mInventory[fi].stackSize;
										boolean eq = ItemStack.areItemStacksEqual(s, mInventory[fi]);
										s.stackSize = oldsize;
										if (eq) {
											int canInject = Math.min(oldsize,
													getInventoryStackLimit() - mInventory[fi].stackSize);
											s.stackSize -= canInject;
											mInventory[fi].stackSize += canInject;

										}

									} else {
										// guaranteed!
										mInventory[fi] = s.copy();
										s.stackSize = 0;

									}
									if (s.stackSize == 0)
										toclear.add(fi);

								});

					}
					toclear.forEach(s -> {
						markDirty();
						ItemStack is = inventoryHandler.getStackInSlot(s);
						if (is != null && is.stackSize <= 0) {
							inventoryHandler.setStackInSlot(s, null);
						}
					});

				}

				@Override
				public void onDestroy() {

					inventoryHandler.getStacks().forEach(s -> {
						if (s != null) {
							if (buildContext.getPlayer().worldObj.isRemote)
								return;
							EntityItem entityitem = buildContext.getPlayer().dropPlayerItemWithRandomChoice(s.copy(),
									false);
							if (entityitem != null) {
								entityitem.delayBeforeCanPickup = 0;
								entityitem.func_145797_a(buildContext.getPlayer().getCommandSenderName());
							}
						}

					});
					;

				}

				@Override
				public void readOnClient(int id, PacketBuffer buf) throws IOException {

				}

				@Override
				public void readOnServer(int id, PacketBuffer buf) throws IOException {

				}
			});

			ArrayList<String> tt = new ArrayList<>(10);
			int i = 0;
			while (true) {
				String k = "programmable_hatches.gt.insertion.tooltip";
				if (LangManager.translateToLocal(k).equals(Integer.valueOf(i).toString())) {
					break;
				}
				String key = k + "." + i;
				String trans = LangManager.translateToLocalFormatted(key, getInventoryStackLimit());

				tt.add(trans);
				i++;

			}
			builder.widget(new DrawableWidget().setDrawable(new ItemDrawable(new ItemStack(Blocks.hopper))).addTooltips(tt)
					.setPos(WIDTH / 2 - 16 / 2, HEIGHT - 16 - 3).setSize(16, 16));

			return builder.build();
		}
		public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
			// ProghatchesUtil.removeMultiCache(builder, this);
			builder.widget(new MarkerWidget(this$()));
			buildContext.addSyncedWindow(INSERTION, (s) -> createInsertionWindow(buildContext));
			buildContext.addSyncedWindow(SHARED_ITEM, (s) -> createSharedItemWindow(buildContext));
			if (createInsertion())
				builder.widget(createButtonInsertion());
			if (moveButtons() == 1) {
				// super.addUIWidgets(builder, buildContext);
				addSortStacksButton(builder);
				addOneStackLimitButton(builder);
			}
			addUIWidgets0(builder, buildContext);
			if (!allowSelectCircuit())
				builder.widget(createButtonSharedItem());

			builder.widget(new SlotWidget(new BaseSlot(getInventoryHandler(), getCircuitSlot())).setEnabledForce(false));

			builder.widget(createButton(() -> !disableFilter, val -> {
				disableFilter = !val;
				updateSlots();
			}, GTUITextures.OVERLAY_BUTTON_INVERT_FILTER, () -> mTooltipCache.getData("programmable_hatches.gt.filtermode"),
					0).setPos(7, 62 - moveButtons() * 18));

			builder.widget(createButton(() -> program, val -> {
				program = val;
				updateSlots();
			}, GTUITextures.OVERLAY_SLOT_CIRCUIT, () -> mTooltipCache.getData("programmable_hatches.gt.program"), 0)
					.setPos(7, 62 - 18 - moveButtons() * 18));

			builder.widget(createButtonForbidden(() -> true, val -> {
				;
				updateSlots();
			}, GTUITextures.OVERLAY_BUTTON_INPUT_SEPARATION_ON_DISABLED,
					() -> mTooltipCache.getData("programmable_hatches.gt.separate"), 1).setPos(7 + 1 * 18,
							62 - moveButtons() * 18));
			if (mMultiFluid == true && showFluidLimit())
				builder.widget(createButton(() -> fluidLimit, val -> {
					fluidLimit = val;
					// updateSlots();
				}, s -> {
					if (s == 0)
						return t0;
					if (s == 1)
						return t1;
					return t2;

				}, ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo")
				/*
				 * tatCollector.translateToLocal(
				 * "programmable_hatches.gt.fluidlimit.0"),
				 * StatCollector.translateToLocal(
				 * "programmable_hatches.gt.fluidlimit.1"),
				 * StatCollector.translateToLocal(
				 * "programmable_hatches.gt.fluidlimit.2")
				 */ )

						, 0, 3).addTooltip(0, StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo.0"))
								.addTooltip(1, StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo.1"))

								.addTooltip(2, StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo.2"))

								.setPos(7 + 1 * 18, 62 - 18 - moveButtons() * 18));

			Pos2d[] p = new Pos2d[] { new Pos2d(79 + 18 * 1, 34), new Pos2d(70 + 18 * 2, 25), new Pos2d(61 + 18 * 3, 16),
					new Pos2d(52 + 18 * 4, 7) };
			Pos2d position = p[Math.min(3, slotTierOverride(this$().mTier))];

			if (mStoredFluid.length > 1) {
				position = new Pos2d(position.getX(), p[3].getY());

			}

			{
				Pos2d position0 = new Pos2d(0, 0);
				// int countInRow = 0;
				final Scrollable scrollable = new Scrollable().setVerticalScroll();
				for (int i = 0; i < mStoredFluid.length; i++) {
					position0 = new Pos2d((i % fluidSlotsPerRow()) * 18, (i / fluidSlotsPerRow()) * 18);
					scrollable.widget(new FluidSlotWidget(mStoredFluid[i]).setBackground(ModularUITextures.FLUID_SLOT)
							.setPos(position0));

				}

				builder.widget(scrollable.setSize(18 * fluidSlotsPerRow(), 18 * 4).setPos(position));
			} /*
				 * for (int i = 0; i < mStoredFluid.length; i++) { builder.widget(
				 * new
				 * FluidSlotWidget(mStoredFluid[i]).setBackground(ModularUITextures.
				 * FLUID_SLOT) .setPos(position)); position=new
				 * Pos2d(position.getX(),position.getY()).add(0, 18); }
				 */
			///
			/*
			 * if(extraCircuit) { for(int i=1;i<4;i++) builder.widget( new
			 * SlotWidget(new BaseSlot(inventoryHandler, getCircuitSlot()+i) {
			 * public int getSlotStackLimit() { return 0; }; } ) {
			 *
			 * @Override public List<String> getExtraTooltip() { return Arrays
			 * .asList(LangManager.translateToLocal(
			 * "programmable_hatches.gt.marking.slot.1")); }
			 * }.disableShiftInsert().setHandlePhantomActionClient(true).
			 * setGTTooltip(() -> new TooltipData(
			 * Arrays.asList(LangManager.translateToLocal(
			 * "programmable_hatches.gt.marking.slot.0"),
			 * LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"
			 * )), Arrays.asList(LangManager.translateToLocal(
			 * "programmable_hatches.gt.marking.slot.0"),
			 * LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"
			 * )))).setPos( getCircuitSlotX()-1,getCircuitSlotY()-18 * i-1) ); }
			 */

		}








	}

	private MUI1Container mui1ct= initMUI1();
	public MUI1Container initMUI1(){
		return  new MUI1Container();
	}




	private MUI2Container mui2ct= initMUI2();

	public MUI2Container initMUI2(){
		return  new MUI2Container();
	}
	public class MUI2Container {
		  public  Grid gridTemplate4by4X(IntFunction<IWidget> widgetCreator) {



			  return new Grid().coverChildren()
		            .pos(52, 7)
		            .mapTo(4, 16*page(), widgetCreator);
		    }
		public com.cleanroommc.modularui.widgets.CycleButtonWidget createButton2(PanelSyncManager syncManager,String key,IntSupplier getter, IntConsumer setter, com.cleanroommc.modularui.drawable.UITexture back,
				String tool, int offset,int count) {

			IntSyncValue g=new IntSyncValue(getter, setter);
			//syncManager.syncValue(key, g);
			return new  com.cleanroommc.modularui.widgets.CycleButtonWidget().stateCount(count)
					.value( (IIntValue<?>) g)
					//.setToggle(getter, setter)

					//.background(back)
					.stateBackground(1, GTGuiTextures.BUTTON_STANDARD_PRESSED)
					.stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
				      .stateOverlay(0, back)
                      .stateOverlay(1, back)
					.pos(7 + offset * 18, 62).size(18, 18).tooltip(s->{s.add(tool);});
		}


		public ModularPanel create(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings){
			ModularPanel builder = GTGuis.mteTemplatePanelBuilder(DualInputHatch.this, data, syncManager,uiSettings)
					.doesAddGregTechLogo(false).doesAddGhostCircuitSlot(allowSelectCircuit()).build();
			return builder;
		}
		public void buildUI(ModularPanel builder,PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {


			ItemStackHandler inventoryHandler = getInventoryHandler();
			com.cleanroommc.modularui.widgets.slot.SlotGroup sg = new com.cleanroommc.modularui.widgets.slot.SlotGroup(
					"item_inv", 1, true);

			syncManager.registerSlotGroup(sg);
			//ModularPanel builder = GTGuis.mteTemplatePanelBuilder(DualInputHatch.this, data, syncManager,uiSettings)
			//		.doesAddGregTechLogo(false).doesAddGhostCircuitSlot(allowSelectCircuit()).build();
			ProghatchesUtil.attachZeroSizedStackRemover2(syncManager, builder);
			IPanelHandler shared_panel = syncManager.panel("shared_panel",
					(manager, handler) -> createSharedItemWindow2(manager), true);

			builder.child(new ItemSlot().disabled().slot(ModularSlot(inventoryHandler, getCircuitSlot())));// sync
																											// the
																											// circuit
																											// to
																											// client

			if (!allowSelectCircuit()) {
				builder.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>().onUpdateListener(s -> {
					s.background(GTGuiTextures.BUTTON_STANDARD,

							mInventory[getCircuitSlot()] == null ? GTGuiTextures.OVERLAY_SLOT_INT_CIRCUIT :

									new com.cleanroommc.modularui.drawable.ItemDrawable(mInventory[getCircuitSlot()]

									));
				}, true).onMousePressed(mouseButton -> {
					shared_panel.openPanel();
					return shared_panel.isPanelOpen();
				}).setEnabledIf((s) -> !shared_panel.isPanelOpen())
						.background(GTGuiTextures.BUTTON_STANDARD, GTGuiTextures.OVERLAY_SLOT_INT_CIRCUIT)
						.disableHoverBackground()
						.tooltip(tooltip -> tooltip
								.add(LangManager.translateToLocal("programmable_hatches.gt.insertion")))
						.pos(getCircuitSlotX() - 1, getCircuitSlotY()/*-18*/ - 1).size(18, 18));

			}
			/*syncManager.setContainerCustomizer(new ContainerCustomizer() {

				public @Nullable ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
					ModularSlot slot = this.getContainer().getModularSlot(index);
					if (!slot.isPhantom()) {
						ItemStack stack = slot.getStack();
						if (stack != null) {
							stack = stack.copy();
							int base = 0;
							if (stack.stackSize > stack.getMaxStackSize()) {
								base = stack.stackSize - stack.getMaxStackSize();
								stack.stackSize = stack.getMaxStackSize();
							}
							ItemStack remainder = transferItem(slot, stack.copy());
							if (base == 0 && (remainder == null || remainder.stackSize < 1))
								stack = null;
							else
								stack.stackSize = base + remainder.stackSize;
							slot.putStack(stack);
							return null;
						}
					}
					return null;
				}
			});*/

			Supplier<com.cleanroommc.modularui.api.widget.IWidget> genSlots;
			Supplier<com.cleanroommc.modularui.widgets.layout.Grid> genSlotsFluid;

			Pos2d[] fluidslot_pos_table = new Pos2d[] { new Pos2d(52 + 18 * 1, 7), new Pos2d(52 + 18 * 2, 7),
					new Pos2d(52 + 18 * 3, 7), new Pos2d(52 + 18 * 4, 7) };
			int fluidslot_pos_index = -1;
			switch (slotTierOverride(mTier)) {
			case 0:
				genSlots = () -> gridTemplate1by1(
						index -> new ItemSlot().slot((ModularSlot(inventoryHandler, index)).slotGroup(sg))).pos(0,0);
				fluidslot_pos_index = 0;
				break;
			case 1:
				genSlots = () -> gridTemplate2by2(
						index -> new ItemSlot().slot((ModularSlot(inventoryHandler, index)).slotGroup(sg))).pos(0,0);
				fluidslot_pos_index = 1;
				break;
			case 2:
				genSlots = () -> gridTemplate3by3(
						index -> new ItemSlot().slot((ModularSlot(inventoryHandler, index)).slotGroup(sg))).pos(0,0);
				fluidslot_pos_index = 2;
				break;
			default:
				genSlots = () -> gridTemplate4by4X(
						index -> new ItemSlot().slot((ModularSlot(inventoryHandler, index)).slotGroup(sg))).pos(0,0);
				fluidslot_pos_index = 3;
			}
			genSlotsFluid = () -> new Grid().coverChildren().pos(0, 0).mapTo(1*fluidSlotsPerRow(), mStoredFluid.length,
					index -> new FluidSlot().syncHandler(new FluidSlotSyncHandler(mStoredFluid[index])));

			ScrollWidget<?> list = new ScrollWidget<>(new VerticalScrollData()).size(18*fluidSlotsPerRow());
			list.getScrollArea().getScrollY().setScrollSize(18 * mStoredFluid.length/fluidSlotsPerRow());
			list.size(18*fluidSlotsPerRow(), 18 * 4);
			list.child(genSlotsFluid.get());
			list.pos(fluidslot_pos_table[fluidslot_pos_index].x, fluidslot_pos_table[fluidslot_pos_index].y);
			builder.child(list);


			ScrollWidget<?> listX = new ScrollWidget<>(new VerticalScrollData()).size(18);
			listX.getScrollArea().getScrollY().setScrollSize(18 * 4*page());
			listX.size(18*(fluidslot_pos_index+1), 18 * 4);
			listX.child(genSlots.get());
			listX.pos(52, 7);
			builder.child(listX);

			//builder.child(genSlots.get());






			builder.bindPlayerInventory();

			IPanelHandler popupPanel = syncManager.panel("popup",
					(manager, handler) -> createInsertionWindow2(manager), true);

			if (createInsertion())
				builder.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>().onMousePressed(mouseButton -> {
					popupPanel.openPanel();
					return popupPanel.isPanelOpen();
				}).background(GTGuiTextures.BUTTON_STANDARD,
						new com.cleanroommc.modularui.drawable.ItemDrawable(new ItemStack(Blocks.hopper)))
						.disableHoverBackground()
						.tooltip(tooltip -> tooltip
								.add(LangManager.translateToLocal("programmable_hatches.gt.insertion")))
						.pos(getCircuitSlotX() - 1, getCircuitSlotY() - 18 * 2 - 1).size(18, 18));
			// shared_panel.openPanel();
			// shared_panel.closePanel();


			buttons(builder, syncManager);
			//return builder;

		}
		private int b2i(boolean b){return b?1:0;}
		private boolean i2b(int b){return b==1;}
	    public  final com.cleanroommc.modularui.drawable.UITexture OVERLAY_BUTTON_INVERT_FILTER = com.cleanroommc.modularui.drawable.UITexture
	            .fullImage(GregTech.ID, "gui/overlay_button/invert_filter");
		public void buttons(ModularPanel builder,PanelSyncManager sync){


			builder.child(createButton2(sync,"v0",() -> b2i(!disableFilter), val -> {
				disableFilter = !i2b(val);
				updateSlots();
			}, OVERLAY_BUTTON_INVERT_FILTER/*.OVERLAY_BUTTON_INVERT_FILTER*/,  StatCollector.translateToLocal("programmable_hatches.gt.filtermode"),
					0,2).pos(7, 62 - moveButtons() * 18));

			builder.child(createButton2(sync,"v1",() -> b2i(program), val -> {
				program = i2b(val);
				updateSlots();
			}, GTGuiTextures.OVERLAY_SLOT_INT_CIRCUIT/*.OVERLAY_BUTTON_INVERT_FILTER*/,  StatCollector.translateToLocal("programmable_hatches.gt.program"),
					0,2).pos(7, 62 - 18 - moveButtons() * 18));




			if (mMultiFluid == true && showFluidLimit())
			{



			com.cleanroommc.modularui.widgets.CycleButtonWidget[] b=new com.cleanroommc.modularui.widgets.CycleButtonWidget[1];
			int[] set=new int[1];
			builder.child(b[0]=createButton2(sync,"v2",() -> fluidLimit, val -> {
				fluidLimit = val;
				set[0]=val;
				b[0].markTooltipDirty();
			},GTGuiTextures.OVERLAY_SLOT_INT_CIRCUIT,"", 0, 3)

					.stateOverlay(0, t00)
					.stateOverlay(1, t01)
					.stateOverlay(2, t02)

					.stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
					.stateBackground(1, GTGuiTextures.BUTTON_STANDARD)
					.stateBackground(2, GTGuiTextures.BUTTON_STANDARD)
					.tooltipDynamic(s->{
						int i=set[0];

						s.addLine(StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo"));

						s.addLine(StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo."+i));
					}).pos(7 + 1 * 18, 62 - 18 - moveButtons() * 18)

					);
			}

				/*
				builder.child(createButton(() -> fluidLimit, val -> {
					fluidLimit = val;

				}, s -> {
					if (s == 0)
						return t0;
					if (s == 1)
						return t1;
					return t2;

				}, ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo")
			 )

						, 0, 3).addTooltip(0, StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo.0"))
								.addTooltip(1, StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo.1"))

								.addTooltip(2, StatCollector.translateToLocal("programmable_hatches.gt.fluidlimit.neo.2"))

								.setPos(7 + 1 * 18, 62 - 18 - moveButtons() * 18));*/
		}
		public ModularSlot ModularSlot(IItemHandler  inventoryHandler, int index) {

			com.cleanroommc.modularui.widgets.slot.ModularSlot slot = new ModularSlot(inventoryHandler, index) {

				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					boolean b = super.isItemValid(stack);
					if (b) {
						ItemStack is = getStack();
						if (is != null && is.stackSize > is.getMaxStackSize()) {
							return false;
						}
					}
					return b;
				}

				@Override
				public boolean canTakeStack(EntityPlayer playerIn) {
					return true;
				}

				@Override
				protected boolean canTake() {

					return true;
				}

			};
			slot.changeListener(new IOnSlotChanged() {

				@Override
				public void onChange(ItemStack newItem, boolean onlyAmountChanged, boolean client, boolean init) {

					slot.putStack(newItem);

				}
			});

			return slot.ignoreMaxStackSize(true);
		}

		GhostCircuitSlotWidget circuitSlot2(IMetaTileEntity mte,PanelSyncManager syncManager) {

			return (GhostCircuitSlotWidget) new GhostCircuitSlotWidget(mte,syncManager ) {
				@Override
				public @NotNull Result onMousePressed(int mouseButton) {
					MouseData mouseData = MouseData.create(mouseButton);
					getSyncHandler().syncToServer(GhostCircuitSyncHandler.SYNC_CLICK, mouseData::writeToPacket);

					return Result.SUCCESS;
				}
			}.slot(new ModularSlot(new GhostCircuitItemStackHandler(mte),0)).pos(0, 0);
		}

		ItemSlot catalystSlot2(ArrayList<ItemStack> inv) {

			ItemSlot is = new ItemSlot() {
				public Result onMousePressed(int mouseButton) {
					//if (getSlot().getHasStack())
					//	getSlot().putStack(null);
					return super.onMousePressed(mouseButton);
				};
			}.slot(new ModularSlot(new ItemStackHandler(inv) {
			}, 0) {
				@Override
				public int getSlotStackLimit() {
					return 0;
				}
			}).pos(0, 0);
			// is.getSyncHandler().isPhantom();

			return is;

		}

		ItemSlot markSlot(ArrayList<ItemStack> inv, int index) {

			ItemSlot is = new PhantomItemSlot(){
				public Result onMousePressed(int mouseButton) {
					//if (getSlot().getHasStack())
					//	getSlot().putStack(null);
					return super.onMousePressed(mouseButton);
				};
			}.slot(new ModularSlot(new ItemStackHandler(inv), index) {
				@Override
				public void putStack(ItemStack stack) {
					if (stack != null)
						stack.stackSize = 0;
					super.putStack(stack);
				}

				@Override
				public int getSlotStackLimit() {
					return 0;
				}
			}).pos(0, 0);

			is.addTooltipStringLines((DualInputHatch.this instanceof BufferedDualInputHatch)
					? ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.0"),
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.1"),
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.2"))
					: ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.0"),
							StatCollector.translateToLocal("programmable_hatches.gt.item.pull.me.1"))

			);
			return is;
		}

		FluidSlot markSlot(List<FluidStack> inv,int index) {

			FluidSlot is = new FluidSlot(){


				 @Override
				    public @NotNull Result onMousePressed(int mouseButton) {

				        if (!((FluidSlotSyncHandler)this.getSyncHandler()).canFillSlot() && !((FluidSlotSyncHandler)this.getSyncHandler()).canDrainSlot()) {
				            return Result.ACCEPT;
				        }
				        ItemStack cursorStack =Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
				        if (((FluidSlotSyncHandler)this.getSyncHandler()).isPhantom() || cursorStack != null) {
				            MouseData mouseData = MouseData.create(mouseButton);
				            ((FluidSlotSyncHandler)this.getSyncHandler()).syncToServer(1, mouseData::writeToPacket);
				        }
				        return Result.SUCCESS;
				    }

				/*@Override
				public Result onMousePressed(int mouseButton) {
					return Result.SUCCESS;
				}*/

				@Override
				 public Result onMouseTapped(int mouseButton) {
					//setFluid(null, 0);

				return Result.IGNORE;}





			};
			is.syncHandler(new FluidSlotSyncHandler(new FluidStackTank(()->inv.get(index), s->inv.set(index, s), 1))
			.phantom(true));
			return is;
		}

		protected ModularPanel createSharedItemWindow2(PanelSyncManager syncManager) {

			ModularPanel builder = new ModularPanel("shared_window");
			builder.size(36 + 18 * 3, 36 + 18 * 4);
			builder.child(circuitSlot2(DualInputHatch.this,syncManager).pos(8 - 1, 8 - 1));
			for (int i = 0; i < shared.circuitUpgrades; i++)
				builder.child(catalystSlot2((shared.circuitInv)).pos(8 - 1, 8 - 1 + 18 + 18 * i));
			int posoffset = 0;
			for (int i = 0; i < shared.itemMEUpgrades; i++) {
				final int fi = i;
				builder.child(markSlot(shared.markedItems, i).pos(8 - 1 + 18 + 4, 8 - 1 + 18 * posoffset));
				builder.child(
						new com.cleanroommc.modularui.api.drawable.IDrawable.DrawableWidget(GuiTextures.MOVE_RIGHT)
								.pos(8 - 1 + 18 * 2 + 4, 8 - 1 + 18 * posoffset).size(18, 18));

				SyncHandlerAEItem hd = new SyncHandlerAEItem() {

					@Override
					public AEItemStack getOnServer() {

						Net net = getNetwork();
						if (net != null) {
							IStorageGrid cahce = net.g.getCache(IStorageGrid.class);
							if (cahce != null) {
								IAEItemStack aeis = cahce.getItemInventory().getStorageList()
										.findPrecise(AEItemStack.create(shared.markedItems.get(fi)));
								return (AEItemStack) aeis;
							}
						}
						return null;
					}

				};
				syncManager.syncValue("shared_item:" + i, hd);

				builder.child(new ItemSlotDummy() {
					{
						hd.cb(s -> {
							this.getSlot().putStack(s.getOnClientI());
						});
					}
				}.slot(new ModularSlot(new ItemStackHandler(1), 0)
						.accessibility(false, false)
						).pos(8 - 1 + 18 * 3 + 4, 8 - 1 + 18 * posoffset));

				posoffset++;
			}
			for (int i = 0; i < shared.fluidMEUpgrades; i++) {
				final int fi = i;
				builder.child(markSlot(shared.markedFluid, i).pos(8 - 1 + 18 + 4, 8 - 1 + 18 * posoffset));
				builder.child(
						new com.cleanroommc.modularui.api.drawable.IDrawable.DrawableWidget(GuiTextures.MOVE_RIGHT)
								.pos(8 - 1 + 18 * 2 + 4, 8 - 1 + 18 * posoffset).size(18, 18));
				SyncHandlerAEFluid hd = new SyncHandlerAEFluid() {
					@Override
					public AEFluidStack getOnServer() {

						Net net = getNetwork();
						if (net != null) {
							IStorageGrid cahce = net.g.getCache(IStorageGrid.class);
							if (cahce != null) {
								IAEFluidStack aeis = cahce.getFluidInventory().getStorageList()
										.findPrecise(AEFluidStack.create(shared.markedFluid.get(fi)));
								return (AEFluidStack) aeis;
							}
						}
						return null;
					}

				};
				syncManager.syncValue("shared_fluid:" + i, hd);

				builder.child(new FluidSlot()
				.syncHandler(
						new FluidSlotSyncHandler(new FluidTank(1))
						{
							{hd.cb(s->{
								this.setValue(s.getOnClientI());
								//((FluidTank)this.getFluidTank()).setFluid(s.getOnClientI());;
								});
							}
						public void detectAndSendChanges(boolean init) {

						};

						}
						.canDrainSlot(false).canFillSlot(false)
						)
						.pos(8 - 1 + 18 * 3 + 4, 8 - 1 + 18 * posoffset));
				posoffset++;
			}


			return builder;

		}

		protected ModularPanel createInsertionWindow2(PanelSyncManager syncManager) {
			int len = (int) Math.round(Math.sqrt(mInventory.length - 1));
			final int WIDTH = 18 * len + 6;
			final int HEIGHT = 18 * (len + 1) + 6;
			AtomicReference<Runnable> onclose = new AtomicReference();
			AtomicReference<Runnable> oncloseclient = new AtomicReference();
			ModularPanel builder = new ModularPanel("ins_window") {
				@Override
				public void onClose() {
					oncloseclient.get().run();
					super.onClose();
				}
			};
			builder.size(WIDTH, HEIGHT);
			com.cleanroommc.modularui.widgets.slot.SlotGroup sg=new com.cleanroommc.modularui.widgets.slot.SlotGroup("temp", 1);
			syncManager.registerSlotGroup(sg);
			final ItemStackHandler inventoryHandler = new ItemStackHandler(mInventory.length - (1));

			List<ModularSlot> slots=new ArrayList<>();
			builder.child(new Grid().coverChildren().pos(3, 3).mapTo(len, len * len,
					index ->{
						ModularSlot ms;
						ItemSlot is= new ItemSlot().slot(ms=ModularSlot(inventoryHandler, index)
							.slotGroup(sg)
							);
						slots.add(ms);

					//	syncManager.itemSlot("tmp_sync_", index, ms);
					return is;
					}
					));

			SyncHandler sync;
			syncManager.syncValue("fakesync_updater", 0, sync = new SyncHandler() {

				@Override
				public void detectAndSendChanges(boolean init) {

					ArrayList<Integer> toclear = new ArrayList<>(1);
					toclear.clear();
					for (int i = 0; i < inventoryHandler.getSlots(); i++) {
						int fi = i;

						Optional.ofNullable(inventoryHandler.getStackInSlot(i)).filter(s -> s.stackSize > 0)
								.ifPresent(s -> {s=s.copy();
									markDirty();
									if (mInventory[fi] != null) {
										int oldsize = s.stackSize;
										s.stackSize = mInventory[fi].stackSize;
										boolean eq = ItemStack.areItemStacksEqual(s, mInventory[fi]);
										s.stackSize = oldsize;
										if (eq) {
											int canInject = Math.min(oldsize,
													getInventoryStackLimit() - mInventory[fi].stackSize);
											s.stackSize -= canInject;
											mInventory[fi].stackSize += canInject;

										}

									} else {
										// guaranteed!
										mInventory[fi] = s.copy();
										s.stackSize = 0;

									}
									//if (s.stackSize == 0)
									//	toclear.add(fi);
									if(s.stackSize==0)s=null;
									inventoryHandler.setStackInSlot(fi, s);
									ItemStack ss = s;
									slots.get(fi).getSyncHandler().syncToClient(ItemSlotSH.SYNC_ITEM, buffer -> {
						                buffer.writeBoolean(false);
						                NetworkUtils.writeItemStack(buffer, ss);
						                buffer.writeBoolean(true);
						                buffer.writeBoolean(false);
						            });
								});

					}
					/*toclear.forEach(s -> {
						markDirty();
						ItemStack is = inventoryHandler.getStackInSlot(s);
						if (is != null && is.stackSize <= 0) {
							inventoryHandler.getStacks().set(s, null);

							//.setStackInSlot(s, null);
						}
					});*/

				}

				@Override
				public void readOnServer(int id, PacketBuffer buf) throws IOException {
					if (id == 100) {
						onclose.get().run();
					}
				}

				@Override
				public void readOnClient(int id, PacketBuffer buf) throws IOException {

				}

			});
			oncloseclient.set(() -> {
				sync.syncToServer(100);
			});
			onclose.set(() -> {
				inventoryHandler.getStacks().forEach(s -> {
					if (s != null) {
						if (syncManager.getPlayer().worldObj.isRemote)
							return;
						EntityItem entityitem = syncManager.getPlayer().dropPlayerItemWithRandomChoice(s.copy(), false);
						if (entityitem != null) {
							entityitem.delayBeforeCanPickup = 0;
							entityitem.func_145797_a(syncManager.getPlayer().getCommandSenderName());
						}

					}

				});
				;
				for (int i = 0; i < inventoryHandler.getSlots(); i++)
					inventoryHandler.setStackInSlot(i, null);

			});
			ArrayList<String> tt = new ArrayList<>(10);
			int i = 0;
			while (true) {
				String k = "programmable_hatches.gt.insertion.tooltip";
				if (LangManager.translateToLocal(k).equals(Integer.valueOf(i).toString())) {
					break;
				}
				String key = k + "." + i;
				String trans = LangManager.translateToLocalFormatted(key, getInventoryStackLimit());

				tt.add(trans);
				i++;

			}
			builder.child(new com.cleanroommc.modularui.api.drawable.IDrawable.DrawableWidget(
					new com.cleanroommc.modularui.drawable.ItemDrawable(new ItemStack(Blocks.hopper)))
							.addTooltipStringLines(tt).pos(WIDTH / 2 - 16 / 2, HEIGHT - 16 - 3).size(16, 16));
			return builder;
		}

	}

	private boolean hasBadge(EntityPlayer p){
		for(ItemStack item:p.inventory.mainInventory){
			if(item!=null&&item.getItem() instanceof ItemBadge){return true;}

		}
		return false;}
	@SuppressWarnings("deprecation")
	@Override
    public void openGui(EntityPlayer player) {
        if (((GTGuis.GLOBAL_SWITCH_MUI2||hasBadge(player)) && useMui2())) {
            if (!NetworkUtils.isClient(player)) {
                MetaTileEntityGuiHandler.open(player, (IMetaTileEntity) this);
            }
        } else {
            GTUIInfos.openGTTileEntityUI(getBaseMetaTileEntity(), player);
        }

    }

}
