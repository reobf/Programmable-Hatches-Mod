package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.objects.XSTR.XSTR_INSTANCE;


import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.Interactable.Result;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.ToolDictNames;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.objects.GTDualInputPattern;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTTooltipDataCache.TooltipData;
import gregtech.api.util.item.GhostCircuitItemStackHandler;
import gregtech.api.util.GTUtility;
import gregtech.common.modularui2.sync.GhostCircuitSyncHandler;
import gregtech.common.modularui2.widget.GhostCircuitSlotWidget;
import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.bufferutil.FluidTankG;
import reobf.proghatches.gt.metatileentity.bufferutil.ItemStackG;
import reobf.proghatches.gt.metatileentity.bufferutil.LongWrapper;
import reobf.proghatches.gt.metatileentity.util.BaseSlotPatched;
import reobf.proghatches.gt.metatileentity.util.FirstObjectHolder;
import reobf.proghatches.gt.metatileentity.util.ICraftingV2;
import reobf.proghatches.gt.metatileentity.util.IInputStateProvider;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;
import reobf.proghatches.gt.metatileentity.util.ListeningFluidTank;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandlerG;
import reobf.proghatches.gt.metatileentity.util.mui2.IMUITexture;
import reobf.proghatches.gt.metatileentity.util.polyfill.INeoDualInputInventory;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class BufferedDualInputHatch extends DualInputHatch
		implements IRecipeProcessingAwareDualHatch, IInputStateProvider, ICraftingV2, IMUITexture,
		reobf.proghatches.util.OmniOcularSuppressor.IDoNotShowInOO

{
	public static abstract class ExConfigEntry{
		public CycleButtonWidget asMUI1(int x,int y) {
			return (CycleButtonWidget) new CycleButtonWidget().setToggle(this::get, this::set).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * x, 3 + 18 * y).setSize(18, 18)
				.addTooltips((List<String>)Stream.of(tips()).map(s->StatCollector.translateToLocal(s)).collect(Collectors.toList()));
		}
		public com.cleanroommc.modularui.widgets.CycleButtonWidget asMUI2(int x,int y) {
			return new com.cleanroommc.modularui.widgets.CycleButtonWidget()
					.stateCount(2)
					.value(accessorI(this::get, this::set))
					
					.stateBackground(1, GTGuiTextures.BUTTON_STANDARD_PRESSED)
					.stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
				      .stateOverlay(1, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
                     .stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
					
					
				
					.pos(3 + 18 * x, 3 + 18 * y).size(18, 18)
					.tooltipBuilder(sx->{Stream.of(tips()).map(s->StatCollector.translateToLocal(s)).forEach(sx::addLine);});

		}		
		
		public abstract boolean shouldApply();
		public abstract boolean get();
		public abstract void set(boolean b);
		public abstract String[] tips();
		public static ExConfigEntry create(Supplier<Boolean> get,Consumer<Boolean> set,String... tips) {
		return create(()->true, get, set, tips);
		}
		public static ExConfigEntry create(Supplier<Boolean> apply,Supplier<Boolean> get,Consumer<Boolean> set,String... tips) {
			
			return new ExConfigEntry() {

				@Override
				public boolean shouldApply() {
				
					return apply.get();
				}

				@Override
				public boolean get() {
					return get.get();
				}

				@Override
				public void set(boolean b) {
					 set.accept(b);
					
				}

				@Override
				public String[] tips() {
					
					return tips;
				}};
		}
		
	}
	public static class ExConfig{
		Table<Integer, Integer, ExConfigEntry> table = HashBasedTable.create();
		public void reg(int x,int y,ExConfigEntry xx) {
			ExConfigEntry old = table.put(x, y, xx);
			if(old !=null) {throw new AssertionError("NO");}
		}
	}
	public ExConfig exconfig=initExConfig();
	public ExConfig initExConfig() {
		ExConfig ret=new ExConfig();
		///////////
		ret.reg(0, 0, ExConfigEntry.create(
				() -> updateEveryTick, 
				(s) -> {updateEveryTick = s;},
				"programmable_hatches.gt.forcecheck"));
		ret.reg(1, 0, ExConfigEntry.create(
				() -> CMMode, 
				(s) -> {CMMode = s;},
				"programmable_hatches.gt.cmmode.0",
				"programmable_hatches.gt.cmmode.1",
				"programmable_hatches.gt.cmmode.2",
				"programmable_hatches.gt.cmmode.3",
				"programmable_hatches.gt.cmmode.4",
				"programmable_hatches.gt.cmmode.5",
				"programmable_hatches.gt.cmmode.6"
				));

		ret.reg(3, 0, ExConfigEntry.create(
				()->(isInfBuffer() || shared.infbufUpgrades > 0),
				() -> autoAppend, 
				(s) -> {autoAppend = s;},
				"programmable_hatches.gt.elasticbuffer.0",
				"programmable_hatches.gt.elasticbuffer.1",
				"programmable_hatches.gt.elasticbuffer.2"
				));
		return ret;
	}
	public Deque<Long> scheduled = new LinkedList<>();// no randomaccess,
														// LinkedList will work
														// fine

	public boolean hasBuffer() {
		return true;
	}

	@Override
	public int getInventoryFluidLimit() {
		/*
		 * long val= fluidBuff()*(int) (4000 * Math.pow(2, mTier) / (mMultiFluid
		 * ? 4 : 1))*(mTier+1) ; return (int) Math.min(val, Integer.MAX_VALUE);
		 */
		// return super.getInventoryFluidLimit();

		return (int) ((int) (32000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1)));
	}

	public long fluidLimit() {

		return (int) ((int) (128000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1)));
	}

	public long itemLimit() {

		return (int) (64 * Math.pow(4, Math.max(mTier - 3, 0)));
	}

	private static long fluidLimit(int mTier, boolean mMultiFluid) {

		return (int) ((int) (128000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1)));
	}

	private static long itemLimit(int mTier) {

		return (int) (64 * Math.pow(4, Math.max(mTier - 3, 0)));
	}

	public BufferedDualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid,
			int bufferNum, String... optional) {
		this(id, name, nameRegional, tier, ProghatchesUtil.getSlots(tier) + 1, mMultiFluid, bufferNum, optional);

	}

	public BufferedDualInputHatch(int id, String name, String nameRegional, int tier, int slot, boolean mMultiFluid,
			int bufferNum, String... optional) {
		super(id, name, nameRegional, tier, slot, mMultiFluid,

				(optional.length > 0 ? optional
						: reobf.proghatches.main.Config.get("BDH", ImmutableMap.<String, Object> builder()
								.put("bufferNum", bufferNum).put("cap", format.format(fluidLimit(tier, mMultiFluid)))
								.put("mMultiFluid", mMultiFluid).put("slots", Math.min(16, (1 + tier) * (tier + 1)))
								.put("stacksize", itemLimit(tier)).put("fluidSlots", fluidSlots(tier))
								// .put("supportFluid", fluid)
								.build())

				));/* ) */

		this.bufferNum = bufferNum;
		initBackend();

	}

	public void initTierBasedField() {

		if (supportsFluids())
			super.initTierBasedField();
		/*
		 * if (mMultiFluid) { mStoredFluid = new ListeningFluidTank[] { new
		 * ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this), new
		 * ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this), new
		 * ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this), new
		 * ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this) }; } else
		 * { mStoredFluid = new ListeningFluidTank[] { new
		 * ListeningFluidTank((int) (4000 * Math.pow(2, mTier)), this) }; }
		 */
	}

	public BufferedDualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
			boolean mMultiFluid, int bufferNum) {
		super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid);
		this.bufferNum = bufferNum;
		initBackend();
		this.disableSort = true;

	}

	public BufferedDualInputHatch(String aName, int aTier, int aSlots, String[] aDescription, ITexture[][][] aTextures,
			boolean mMultiFluid, int bufferNum) {
		super(aName, aTier, aSlots, aDescription, aTextures, mMultiFluid);
		this.bufferNum = bufferNum;
		initBackend();
		this.disableSort = true;

	}

	// public ItemStack[] dualItem(){return
	// filterStack.apply(inv0.mStoredItemInternal);}
	// public FluidStack[] dualFluid(){return
	// asFluidStack.apply(inv0.mStoredFluidInternal);}
	final public ArrayList<DualInvBuffer> inv0 = new ArrayList<DualInvBuffer>();
	//boolean limitToIntMax;

	public long singleSlotLimit() {
		return Integer.MAX_VALUE;//limitToIntMax ? Integer.MAX_VALUE : Long.MAX_VALUE;
	}

	// private long mask=new
	// Random().nextLong()&(~0b1111_1111_1111_1111);//65536 buffers
	// private short count;
	public static boolean emptyopt=true;
	public class DualInvBuffer implements INeoDualInputInventory {

		//public int PID;
		/*
		 * @Override public boolean areYouSerious() { boolean
		 * y=lock&&recipeLocked; return !y; }
		 */
		public long tickFirstClassify = -1;

		public void onChange() {
		}
		//true->might have something but not 100% sure
		//false->100% empty
		public boolean nonempty;
		protected FluidTankG[] mStoredFluidInternal;
		protected ItemStackG[] mStoredItemInternal;
		protected FluidTank[] mStoredFluidInternalSingle;
		protected ItemStack[] mStoredItemInternalSingle;
		public boolean recipeLocked;
		public int i;
		public int f;

		// public int ip=-1;
		// public int fp=-1;

		public boolean lock;

		// public boolean lock;
		public boolean full() {
			if(nonempty==false&&emptyopt)return false;
			for (int index = 0; index < mStoredItemInternalSingle.length; index++) {
				ItemStackG i = mStoredItemInternal[index];
				ItemStack si = mStoredItemInternalSingle[index];
				if (i != null) {
					if (si != null && singleSlotLimit() - i.stackSize() < si.stackSize) {
						return true;// over flow! count as full
					}

					if (i.stackSize() >= itemLimit()) {
						return true;
					}
				}
			}

			for (int index = 0; index < mStoredFluidInternalSingle.length; index++) {
				FluidTankG i = mStoredFluidInternal[index];
				FluidTank si = mStoredFluidInternalSingle[index];
				if (si != null && singleSlotLimit() - i.getFluidAmount() < si.getFluidAmount()) {
					return true;// over flow! count as full
				}
				if (i.getFluidAmount() >= fluidLimit()) {
					return true;
				}

			}
			return false;

		}

		public void updateSlots() {
			nonempty=false;
			for (int i = 0; i < this.i; i++)
				if ((mStoredItemInternal[i] != null && mStoredItemInternal[i].stackSize() <= 0)||mStoredItemInternal[i] == null) {
					mStoredItemInternal[i] = null;
				}else {nonempty=true;}
			for (int i = 0; i < this.f; i++)
				if (Optional.ofNullable(mStoredFluidInternal[i].getFluid()).filter(s -> s.amount == 0).isPresent()) {
					mStoredFluidInternal[i].setFluid(null);
				}else {nonempty=true;}

		}

		public NBTTagCompound toTag() {

			NBTTagCompound tag = new NBTTagCompound();
			//tag.setInteger("PID", PID);
			for (int i = 0; i < mStoredFluidInternal.length; i++) {
				if (mStoredFluidInternal[i] != null)
					tag.setTag("mStoredFluidInternal" + i, mStoredFluidInternal[i].writeToNBT(new NBTTagCompound()));
			}
			for (int i = 0; i < mStoredFluidInternalSingle.length; i++) {
				if (mStoredFluidInternalSingle[i] != null)
					tag.setTag("mStoredFluidInternalSingle" + i,
							mStoredFluidInternalSingle[i].writeToNBT(new NBTTagCompound()));
			}
			for (int i = 0; i < mStoredItemInternal.length; i++) {
				if (mStoredItemInternal[i] != null)
					tag.setTag("mStoredItemInternal" + i, writeToNBTG(mStoredItemInternal[i], new NBTTagCompound()));
			}
			for (int i = 0; i < mStoredItemInternalSingle.length; i++) {
				if (mStoredItemInternalSingle[i] != null)
					tag.setTag("mStoredItemInternalSingle" + i,
							writeToNBT(mStoredItemInternalSingle[i], new NBTTagCompound()));
			}

			tag.setInteger("i", i);
			tag.setInteger("f", f);
			tag.setBoolean("recipeLocked", recipeLocked);
			tag.setBoolean("lock", lock);
			tag.setInteger("unlockDelay", unlockDelay);
			return tag;
		}

		public void fromTag(NBTTagCompound tag) {

			//PID = tag.getInteger("PID");
			if (mStoredFluidInternal != null) {
				for (int i = 0; i < mStoredFluidInternal.length; i++) {
					if (tag.hasKey("mStoredFluidInternal" + i)) {
						mStoredFluidInternal[i].readFromNBT(tag.getCompoundTag("mStoredFluidInternal" + i));
						if(mStoredFluidInternal[i].getFluidAmount()>0)nonempty=true;
					}
				}
			}
			if (mStoredFluidInternalSingle != null) {
				for (int i = 0; i < mStoredFluidInternalSingle.length; i++) {
					if (tag.hasKey("mStoredFluidInternalSingle" + i)) {
						mStoredFluidInternalSingle[i].readFromNBT(tag.getCompoundTag("mStoredFluidInternalSingle" + i));
					}
				}
			}
			if (mStoredItemInternal != null) {
				for (int i = 0; i < mStoredItemInternal.length; i++) {
					if (tag.hasKey("mStoredItemInternal" + i)) {
						mStoredItemInternal[i] = loadItemStackFromNBTG(tag.getCompoundTag("mStoredItemInternal" + i));
						if(mStoredItemInternal[i]!=null&&mStoredItemInternal[i].stackSize()>0)nonempty=true;
					}
				}
			}
			if (mStoredItemInternalSingle != null) {
				for (int i = 0; i < mStoredItemInternalSingle.length; i++) {
					if (tag.hasKey("mStoredItemInternalSingle" + i)) {
						mStoredItemInternalSingle[i] = loadItemStackFromNBT(
								tag.getCompoundTag("mStoredItemInternalSingle" + i));
					}
				}
			}
			if (i == 0)
				if (tag.getInteger("i") > 0)
					i = tag.getInteger("i");
			if (f == 0)
				if (tag.getInteger("f") > 0)
					f = tag.getInteger("f");
			recipeLocked = tag.getBoolean("recipeLocked");
			lock = tag.getBoolean("lock");
			unlockDelay = tag.getInteger("unlockDelay");
			if (recipeLocked) treeInsert(this); // rebuild the (transient) tree on load
		}

		int v = 4;

		int unlockDelay = 0;

		/** Contiguous mirror of the recorded recipe's fluid singles (null = empty slot). */
		FluidStack[] singleBack;

		public void init(int item, int fluid) {
			i = item;
			f = fluid;
			mStoredFluidInternal = initFluidTack(new FluidTankG[fluid]);
			singleBack = new FluidStack[fluid];
			mStoredFluidInternalSingle = new FluidTank[fluid];
			for (int ix = 0; ix < fluid; ix++) {
				mStoredFluidInternalSingle[ix] = new BackedFluidTank(singleBack, ix);
			}
			mStoredItemInternal = new ItemStackG[item + v];
			mStoredItemInternalSingle = new ItemStack[item];
		}

		private FluidTankG[] initFluidTack(FluidTankG[] t) {
			for (int i = 0; i < t.length; i++) {
				t[i] = new FluidTankG();
			}
			return t;
		}

		public boolean isAccessibleForMulti() {

			/*
			 * return !isEmpty()&& tickFirstClassify+2<currentTick();
			 */
			return !isEmpty();
		}

		public long currentTick() {
			CoverableTileEntity obj = ((CoverableTileEntity) getBaseMetaTileEntity());
			return obj != null ? obj.mTickTimer : 0;

		}

		/** True iff no fluid tank holds anything (same predicate isEmpty() uses for the fluid side). */
		private boolean fluidSideEmpty() {
			for (FluidTankG t : mStoredFluidInternal) {
				if (t.isEmpty() == false) return false;
			}
			return true;
		}

		/** True iff no item slot holds anything (same predicate isEmpty() uses for the item side). */
		private boolean itemSideEmpty() {
			for (ItemStackG g : mStoredItemInternal) {
				if (g != null && g.isEmpty() == false) return false;
			}
			return true;
		}

		public boolean isEmpty() {
			if(nonempty==false&&emptyopt)return true;
			for (FluidTankG f : mStoredFluidInternal) {
				if (f.isEmpty()==false) {nonempty=true;
					return false;
				}
			}
			for (ItemStackG i : mStoredItemInternal) {

				if (i != null && i.isEmpty()==false) {nonempty=true;
					return false;
				}
			}nonempty=false;
			return true;
		}

		/*
		 * 0->not ready 1->ready 2->replaced
		 */
		public int clearRecipeIfNeeded() {
			if (lock) {
				unlockDelay = 0;
				return !recipeLocked ? 1 : 0;
			}
			if (isEmpty()) {
				if (!recipeLocked) {
					return 1;
				}

				if (Config.delayUnlock) {
					if (unlockDelay == 0) {
						unlockDelay = 10;
						preventSleep = Math.max(preventSleep, 25);
						return 0;
					}
					if (unlockDelay > 0) {
						unlockDelay--;
						if (unlockDelay != 0)
							return 0;

					}
				}

				treeRemove(this); // prune BEFORE the recorded keys below are wiped
				for (FluidTank ft : mStoredFluidInternalSingle) {
					ft.setFluid(null);
				}
				for (int ii = 0; ii < i; ii++) {
					mStoredItemInternalSingle[ii] = null;

				}
				recipeLocked = false;
				//PID = 0;
				/*
				 * if(detail!=null){ int index=inv0.indexOf(this); DualInvBuffer
				 * neo=new DualInvBuffer(); neo.init(mInventory.length - 1,
				 * mStoredFluid.length); DualInvBuffer old =
				 * inv0.set(index,neo); return 2; }
				 */

				return 1;
			} else {
				unlockDelay = 0;
			}
			return 0;
		}

		private boolean fluidEqualsIngoreAmount(FluidTank a, FluidTank b) {

			if (a.getFluid() == null && a.getFluid() == null)
				return true;
			if (a.getFluid() != null && (!a.getFluid().equals(b.getFluid())))
				return false;

			return true;
		}

		private boolean fluidEqualsIngoreAmount(FluidTankG a, FluidTank b) {

			if (a.getFluid() == null && a.getFluid() == null)
				return true;
			if (a.getFluid() != null && (!a.getFluid().equals(b.getFluid())))
				return false;

			return true;
		}

		public boolean areItemStacksEqualIngoreAmount(ItemStack p_77989_0_, ItemStack p_77989_1_) {
			return p_77989_0_ == null && p_77989_1_ == null ? true
					: (p_77989_0_ != null && p_77989_1_ != null ? isItemStackEqualIngoreAmount(p_77989_0_, p_77989_1_)
							: false);
		}

		/**
		 * compares ItemStack argument to the instance ItemStack; returns true
		 * if both ItemStacks are equal
		 */
		private boolean isItemStackEqualIngoreAmount(ItemStack p_77959_1_, ItemStack thiz) {
			return false ? false
					: (thiz.getItem() != p_77959_1_.getItem() ? false
							: (thiz.getItemDamage() != p_77959_1_.getItemDamage() ? false
									: (thiz.stackTagCompound == null && p_77959_1_.stackTagCompound != null ? false
											: thiz.stackTagCompound == null
													|| thiz.stackTagCompound.equals(p_77959_1_.stackTagCompound))));
		}

		/**
		 * classify() with less check, for better performance
		 */
		public void firstClassify(ListeningFluidTank[] fin, ItemStack[] iin,int hintf,int hinti) {
			tickFirstClassify = currentTick();
			for (int ix = 0; ix < Math.min(f,hintf); ix++) {
				mStoredFluidInternal[ix]
						.setFluid(Optional.ofNullable(fin[ix].getFluid()).map(FluidStack::copy).orElse(null));
				fin[ix].setFluidDirect(null);
				nonempty=true;
			}
			for (int ix = 0; ix < Math.min(i,hinti); ix++) {
				mStoredItemInternal[ix] = ItemStackG
						.neo(Optional.ofNullable(iin[ix]).map(ItemStack::copy).orElse(null));
				iin[ix] = null;
				nonempty=true;
			}
			/*
			 * Long tick=tickFirstClassify+2;
			 * if(!tick.equals(scheduled.peekFirst())) { scheduled.push(tick); }
			 */

			markJustHadNewItems();
			onClassify();
			programLocal();
			onChange();
			justHadNewItems = true;
		}

		private void programLocal() {
			if (!program)
				return;
			ArrayList<ItemStack> isa = new ArrayList<>();
			for (int i = 0; i < mStoredItemInternal.length; i++) {
				ItemStackG is = mStoredItemInternal[i];
				if (is == null)
					continue;
				if (is.getItem() != MyMod.progcircuit)
					continue;
				mStoredItemInternal[i] = null;
				// inv0.mStoredItemInternal[inv0.mStoredItemInternal.length-1]=

				isa.add(GTUtility.copyAmount(0, ItemProgrammingCircuit.getCircuit(is.getStack()).orElse(null)));
			}

			int nums = Math.min(v, isa.size());
			if (nums == 0)
				return;

			for (int i = 0; i < v; i++) {
				if (i < nums) {
					mStoredItemInternal[this.i + i] = ItemStackG.neo(isa.get(i));
				} else {
					mStoredItemInternal[this.i + i] = null;
				}

			}

		}

		/**
		 * Whether this buffer's recorded recipe is currently inserted in the hatch's recipeTree.
		 * Kept in sync in the same call stack at every mutation site (record / clear / fromTag /
		 * compaction move / inv0 removal) — an inconsistent state never escapes the call stack.
		 */
		public boolean inTree;

		public boolean classify(ListeningFluidTank[] fin, ItemStack[] iin, boolean removeInputOnSuccess) {
			int indexItem=-1;
			int indexFluid=-1;
			boolean hasJob = false;
			for (int ix = 0; ix < f; ix++) {
				if (fin[ix].getFluidAmount() > 0) {
					hasJob = true;
				}
				int result;
				if (0!=(result=fluidEquals(mStoredFluidInternalSingle[ix], fin[ix]))) {
					if ((fin[ix].getFluidAmount() > 0 && mStoredFluidInternal[ix].getFluidAmount() > 0)
							&& !fluidEqualsIngoreAmount(mStoredFluidInternal[ix], fin[ix])) {
						return false;
					}
				} else {
					return false;
				}
				//result is not 0 here, 
				//if result==1 this is a non-empty slot (and might be the last non-empty slot), record it for further optimization
				//if result==2 this is an empty slot, so ignore it

				if(result==1)indexFluid=ix;

			}
			for (int ix = 0; ix < i; ix++) {
				if (iin[ix] != null && iin[ix].stackSize > 0) {
					hasJob = true;
				}
				if (ItemStack.areItemStacksEqual(mStoredItemInternalSingle[ix], iin[ix])) {
					if ((iin[ix] != null && mStoredItemInternal[ix] != null)
							&& !areItemStacksEqualIngoreAmount(mStoredItemInternal[ix].getStack(), iin[ix])) {
						return false;
					}
				} else {
					return false;
				}
				//recorded fluid equals current fluid here
				//so they are both empty or non-empty, so check one of them will work
				//if check passes 
				//this is a non-empty slot (and might be the last non-empty slot), record it for further optimization
				if(mStoredItemInternalSingle[ix]!=null)indexItem=ix;

			}
			if (!hasJob) {
				return false;
			}
//System.out.println(indexItem+" "+indexFluid);
			return accumulateMatched(fin, iin, removeInputOnSuccess, indexFluid, indexItem);
		}

		/**
		 * Tree-confirmed merge: the RecipeTree walk (cheap layers + item NBT layers) already proved
		 * the input region equals this buffer's recorded recipe, so skip the slot-by-slot compare
		 * and go straight to the accumulate phase. Fluid tags are intentionally NOT part of the
		 * tree predicate (dead in practice per user decision) — a tag-differing fluid dose merges
		 * and adopts the recorded tag (accumulation fills from the recorded single anyway).
		 */
		public boolean classifyMatched(ListeningFluidTank[] fin, ItemStack[] iin, boolean removeInputOnSuccess) {
			int indexFluid = -1;
			int indexItem = -1;
			for (int ix = 0; ix < f; ix++) if (mStoredFluidInternalSingle[ix].getFluid() != null) indexFluid = ix;
			for (int ix = 0; ix < i; ix++) if (mStoredItemInternalSingle[ix] != null) indexItem = ix;
			if (indexFluid < 0 && indexItem < 0) return false; // no recorded recipe: nothing to merge
			return accumulateMatched(fin, iin, removeInputOnSuccess, indexFluid, indexItem);
		}

		/** The action phase shared by classify() and classifyMatched(): accumulate one dose, consume inputs. */
		private boolean accumulateMatched(ListeningFluidTank[] fin, ItemStack[] iin, boolean removeInputOnSuccess,
				int indexFluid, int indexItem) {
			for (int ix = 0; ix < indexFluid+1; ix++) {
				mStoredFluidInternal[ix].fill(mStoredFluidInternalSingle[ix].getFluid(), true);
				if (removeInputOnSuccess)
					fin[ix].setFluidDirect(null);
				else if (fin[ix].getFluid() != null)
					fin[ix].setFluidDirect(fin[ix].getFluid().copy());
				nonempty=true;
			}
			for (int ix = 0; ix < indexItem+1; ix++) {
				if (mStoredItemInternalSingle[ix] != null)
					if (mStoredItemInternal[ix] == null)
						mStoredItemInternal[ix] = ItemStackG.neo(mStoredItemInternalSingle[ix].copy());
					else
						mStoredItemInternal[ix].stackSizeInc(new LongWrapper( mStoredItemInternalSingle[ix].stackSize));
				if (removeInputOnSuccess)
					iin[ix] = null;
				else if (iin[ix] != null)
					iin[ix] = iin[ix].copy();
				nonempty=true;
			}
			tickFirstClassify = -1;// make it instantly accessible
			markJustHadNewItems();

			/*
			 * Integer check = detailmap.get(Recipe.fromBuffer(this, false));
			 * if(check==null){ currentID++;
			 * detailmap.put(Recipe.fromBuffer(this, true),currentID );
			 * check=currentID; this.PID=check; }
			 */

			onClassify();
			if (program)
				programLocal();
			onChange();
			justHadNewItems = true;
			return true;
		}

		public boolean recordRecipeOrClassify(ListeningFluidTank[] fin, ItemStack[] iin) {
			int readyToRecord = clearRecipeIfNeeded();
			if (readyToRecord == 2) {
				return false;
			}
			int indexi=-1;
			int indexf=-1;
			// clearRecipeIfNeeded();
			if (recipeLocked == false && readyToRecord == 1) {
				boolean actuallyFound = false;
				for (int ix = 0; ix < f; ix++) {
					if (fin[ix].getFluidAmount() > 0) {
						indexf=ix;
						actuallyFound = true;
						mStoredFluidInternalSingle[ix].setFluid(fin[ix].getFluid());
					}
				}
				for (int ix = 0; ix < i; ix++) {
					if (iin[ix] != null) {
						indexi=ix;
						actuallyFound = true;
						mStoredItemInternalSingle[ix] = iin[ix].copy();
					}
				}
				recipeLocked = actuallyFound;
				if (actuallyFound) {
					treeInsert(this);
					firstClassify(fin, iin,indexf+1,indexi+1);
				}
				return actuallyFound;
			}
			return false;
		}

		@Override
		public ItemStack[] getItemInputs() {
			/*
			 * ItemStack[] condensed = filterStack.apply(mStoredItemInternal);
			 * ItemStack additional = getStackInSlot(getCircuitSlot()); if
			 * (additional == null) return condensed; int before_size =
			 * condensed.length; ItemStack[] bruh = new ItemStack[before_size +
			 * 1]; bruh[before_size] = additional; System.arraycopy(condensed,
			 * 0, bruh, 0, before_size); return bruh;
			 */
			ItemStack[] flat = flat(mStoredItemInternal);
			// nonempty=false means "100% empty", so one empty SIDE is not enough: an item-only
			// buffer used to be flagged false by getFluidInputs (and vice versa), making it
			// invisible to the multi's nonempty filter and lying to isEmpty()'s fast path
			// (record-target selection & surplus-buffer removal). Check the other side too —
			// then this doubles as the cheap "just got drained" false-setter.
			if (flat.length == 0) {
				if (fluidSideEmpty()) nonempty = false;
			} else nonempty = true;
			ItemStack[] condensed = filterStack.apply(flat, shared.getItems());

			// if(!trunOffEnsure){condensed=ensureIntMax(condensed);}

			return condensed;

		}

		@Override
		public FluidStack[] getFluidInputs() {
			FluidStack[] flat = flat(mStoredFluidInternal);
			if (flat.length == 0) {
				if (itemSideEmpty()) nonempty = false;
			} else nonempty = true;
			FluidStack[] condensed = asFluidStack.apply(flat(mStoredFluidInternal), shared.getFluid());
			// if(!trunOffEnsure){condensed=ensureIntMax(condensed);}

			return condensed;
		}

		public int space() {
			long ret = Long.MAX_VALUE;
			boolean found = false;
			for (int ix = 0; ix < i; ix++) {

				if (mStoredItemInternalSingle[ix] != null && mStoredItemInternalSingle[ix].stackSize > 0) {
					long now = 0;
					if (mStoredItemInternal[ix] != null)
						now = mStoredItemInternal[ix].stackSize();
					long tmp = (itemLimit() - now) / mStoredItemInternalSingle[ix].stackSize;
					if (tmp <= ret) {
						ret = tmp;
						found = true;
					}
				}
			}
			for (int ix = 0; ix < f; ix++) {

				if (mStoredFluidInternalSingle[ix].getFluidAmount() > 0) {
					long now = mStoredFluidInternal[ix].getFluidAmount();

					long tmp = (fluidLimit() - now) / mStoredFluidInternalSingle[ix].getFluidAmount();
					if (tmp <= ret) {
						ret = tmp;
						found = true;
					}
				}
			}

			if (found)
				return (int) Math.min(ret, Integer.MAX_VALUE);
			return 0;

		}

	}

	int bufferNum;

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new BufferedDualInputHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
	}

	// private long lastMark;
	// avoid setting justHadNewItems to true every tick
	public void markJustHadNewItems() {
		/*
		 * long now=this.getBaseMetaTileEntity().getTimer();
		 * if(now>=lastMark-1){return;} lastMark=now;
		 */
		justHadNewItems = true;

	}

	public void initBackend() {
		for (int i = 0; i < bufferNum; i++)
			inv0.add(new DualInvBuffer());
		inv0.forEach(s -> s.init(this.mInventory.length - 1, this.mStoredFluid.length));

	}
/*
	@SuppressWarnings("rawtypes")
	public static class CallerCheck0 {

		public static Supplier<Boolean> isMEInterface = () -> true;
		static Throwable t = new Throwable();
		static {
			isMEInterface = () -> {
				t.fillInStackTrace();
				// String name=t.getStackTrace()[4].getClassName();

				return t.getStackTrace()[4].getClassName().contains("appeng.util.inv.AdaptorIInventory")
						|| t.getStackTrace()[3].getClassName()
								.contains("com.glodblock.github.inventory.FluidConvertingInventoryAdaptor");
			};

			try {
				// sun.reflect.Reflection.getCallerClass(0);
				Class<?> u = Class.forName("sun.reflect.Reflection");
				Method m = u.getDeclaredMethod("getCallerClass", int.class);
				m.invoke(null, 0);
				MethodHandle mh = MethodHandles.lookup().unreflect(m);

				isMEInterface = () -> {
					try {
						Class c6 = (Class) mh.invoke(6);
						Class c5 = (Class) mh.invoke(5);

						return c6.getName().contains("appeng.util.inv.AdaptorIInventory") || c5.getName()
								.contains("com.glodblock.github.inventory.FluidConvertingInventoryAdaptor")

						;
					} catch (Throwable e) {
						e.printStackTrace();
						return true;
					}

				};

			} catch (Throwable any) {
				any.printStackTrace();
			}
		}
	}
*/
	private boolean updateEveryTick;

	public boolean updateEveryTick() {
		return updateEveryTick;
	}

	private boolean sleep;
	private int sleepTime;
	private boolean isOnLastTick;
	// public boolean prevdirty;
	public int preventSleep;

	@Override
	public void startRecipeProcessingImpl() {

		if (isInputEmpty() == false && getBaseMetaTileEntity().isAllowedToWork()) {
			// one tree walk either proves "no buffer can match" or yields the DEFINITIVE matches:
			// merge via classifyMatched without re-comparing (see RecipeTree javadoc)
			final java.util.ArrayList<DualInvBuffer> candidates = treeLookup();
			for (DualInvBuffer inv0 : this.sortByEmptyItr()) {

				if (inv0.full() == false) {
					boolean done;
					if (candidates != null && candidates.contains(inv0)) {
						done = inv0.classifyMatched(this.mStoredFluid, mInventory, true);
					} else {
						done = (!treeEligible(inv0) && inv0.classify(this.mStoredFluid, mInventory, true))
							|| inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory);
					}
					if (done) break;


				}

				// inv0.clearRecipeIfNeeded();
			}
		}

		super.startRecipeProcessingImpl();
	}

	public static class DeferredEvaluator {

		public DeferredEvaluator(Supplier<Boolean> provider) {
			this.provider = provider;
		}

		Supplier<Boolean> provider;
		Boolean cache;

		public boolean get() {
			if (cache == null) {
				cache = provider.get();
			}
			return cache;
		}
	}

	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.onPostTick(aBaseMetaTileEntity, aTick);
		if (aBaseMetaTileEntity.getWorld().isRemote)
			return;

		/*toDisconnect.forEach(s -> {
			s.wrapped = null;
		});
		toDisconnect.clear();*/
		// System.out.println(scheduled);
		// System.out.println(aTick+" "+scheduled.peekLast());
		Optional.ofNullable(scheduled.peekLast()).filter(s -> s < aTick).ifPresent(s -> {
			scheduled.removeLast();
			justHadNewItems = true;
			// inv0.forEach(st->System.out.println(st.isAccessibleForMulti()));
		});

		dirty = dirty || updateEveryTick();
		if (dirty) {
			updateSlots();
		}
		dirty = dirty || getBaseMetaTileEntity().hasInventoryBeenModified();
		// System.out.println(dirty);
		// dirty=dirty||(!highEfficiencyMode());
		boolean on = (this.getBaseMetaTileEntity().isAllowedToWork());
		if (isOnLastTick != on) {
			dirty = true;
		}
		;
		isOnLastTick = on;

		// System.out.println("sleep);
		// Boolean inputEmpty=null;

		DeferredEvaluator inputEmpty = new DeferredEvaluator(this::isInputEmpty);
		if (dirty) {
			sleep = false;// wake up
			sleepTime = 0;
		} else if (!sleep) {
			/* boolean inputEmpty=isInputEmpty(); */// not dirty but awake,
													// check if need to sleep
			if (inputEmpty.get()) {
				if (preventSleep == 0)
					if (Config.sleep)
						sleep = true;

			} // Zzz
		}
		if (sleep)
			sleepTime++;
		if (preventSleep > 0) {
			preventSleep--;
			sleep = false;
		}
		// System.out.println(sleep);

		// if(inputEmpty==null)inputEmpty=isInputEmpty();
		if (!sleep || updateEveryTick()) {
			final java.util.ArrayList<DualInvBuffer> candidates = (on && !inputEmpty.get()) ? treeLookup() : null;
			for (DualInvBuffer inv0 : this.sortByEmptyItr()) {
				if (on && !inputEmpty.get()) {
					if (inv0.full() == false) {
						boolean done;
						if (candidates != null && candidates.contains(inv0)) {
							done = inv0.classifyMatched(this.mStoredFluid, mInventory, true);
						} else {
							done = (!treeEligible(inv0) && inv0.classify(this.mStoredFluid, mInventory, true))
								|| inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory);
						}
						if (done) break;

					}
				}

				inv0.clearRecipeIfNeeded();
			}
		}
		// prevdirty=dirty;

		if (autoAppend && allFull && !isInputEmpty()) {
			DualInvBuffer append;
			inv0.add(append = new DualInvBuffer());
			append.init(this.mInventory.length - 1, this.mStoredFluid.length);
			allFull = false;
		} else {
			if (inv0.size() > bufferNum) {
				boolean exfull = true;
				for (int i = bufferNum; i < inv0.size(); i++) {
					if (inv0.get(i).isEmpty()) {
						// isEmpty() ignores recipeLocked: the buffer may leave inv0 with a recipe
						// still recorded (delayed unlock) — prune it or the tree would keep serving
						// a buffer that no longer exists
						treeRemove(inv0.get(i));
						inv0.remove(i);
						exfull = false;
						break;
					}

				}
				if (exfull/* &&inv0.size()>bufferNum */)
					for (int i = 0; i < bufferNum; i++) {
						if (inv0.get(i).isEmpty() && (!inv0.get(i).recipeLocked)) {
							DualInvBuffer from = inv0.get(bufferNum);
							DualInvBuffer to = inv0.get(i);
							// recipe unchanged, host object swapped: re-home the leaf entry
							treeRemove(from);
							// to.fromTag(from.toTag());//TODO shallow copy
							// instead
							moveTo(from.mStoredFluidInternal, to.mStoredFluidInternal);
							// CONTENT copy, not object copy: the single tanks are views bound to
							// their owning buffer's singleBack mirror (see BackedFluidTank)
							for (int mi = 0; mi < Math.min(from.mStoredFluidInternalSingle.length,
								to.mStoredFluidInternalSingle.length); mi++) {
								to.mStoredFluidInternalSingle[mi].setFluid(from.mStoredFluidInternalSingle[mi].getFluid());
							}
							moveTo(from.mStoredItemInternal, to.mStoredItemInternal);
							moveTo(from.mStoredItemInternalSingle, to.mStoredItemInternalSingle);
							to.nonempty=true;
							to.f = from.f;
							to.i = from.i;
							// to.fp=from.fp;
							// to.ip=from.ip;
							to.lock = from.lock;
							to.v = from.v;
							to.recipeLocked = from.recipeLocked;
							to.tickFirstClassify = from.tickFirstClassify;
							to.unlockDelay = from.unlockDelay;
							if (to.recipeLocked) treeInsert(to);
							inv0.remove(bufferNum);
							break;
						}
					}

			}

		}

		dirty = false;

		// GT (#7185) removed interval recipe polling: hatches must push. justHadNewItems is set when new
		// items/fluids land in the internal buffers (AE pushes, scheduled arrivals, slot moves), which
		// bypasses hasInventoryBeenModified(), so notify registered controllers here explicitly.
		if (justHadNewItems) {
			notifyWatchers();
			justHadNewItems = false;
		}
	}

	private void moveTo(Object[] a, Object[] b) {
		System.arraycopy(a, 0, b, 0, a.length);
	}

	boolean autoAppend = false;

	@Override
	public ItemStack getStackInSlot(int aIndex) {
		// if(aIndex>=mInventory.length)return
		// inv0.mStoredItemInternal[aIndex-mInventory.length];
		return super.getStackInSlot(aIndex);
	}

	boolean allFull;

	/**
	 * non-empty one fist, then append an empty one at last
	 */
	public ArrayList<DualInvBuffer> sortByEmpty() {
		ArrayList<DualInvBuffer> non_empty = new ArrayList<>();
		FirstObjectHolder<DualInvBuffer> empty = new FirstObjectHolder<>();
		inv0.forEach(s -> {
			(s.isEmpty()
					&& (!s.recipeLocked/*
										 * locked is considered not 'empty'
										 */) ? empty : non_empty).add(s);
		});

		empty.opt().ifPresent(non_empty::add);
		if (!empty.opt().isPresent()) {
			allFull = true;
		}
		// only one empty is needed, because only one buffer at maximum will be
		// filled one time

		return non_empty;
	}
	public Iterable<DualInvBuffer> sortByEmptyItr() {
		if(true)
		return sortByEmpty();
		Iterator<DualInvBuffer> all = inv0.iterator();
		return new Iterable<BufferedDualInputHatch.DualInvBuffer>() {
			
			@Override
			public Iterator<DualInvBuffer> iterator() {
				
				return new Iterator<BufferedDualInputHatch.DualInvBuffer>(){
					
				
				    private DualInvBuffer nextElement;
				    private DualInvBuffer firstEmpty;
				    private boolean foundFirstEmpty;
				    private Boolean hasNextResult; 
				    private boolean hasNextComputed;

				   

				    @Override
				    public boolean hasNext() {
				       
				        if (hasNextComputed) {
				            return hasNextResult;
				        }
				        
				        
				        hasNextComputed = true;
				        
				        if (nextElement != null) {
				            hasNextResult = true;
				            return true;
				        }
				        
				        while (all.hasNext()) {
				            DualInvBuffer item = all.next();
				            if (!item.isEmpty()) {
				                nextElement = item;
				                hasNextResult = true;
				                return true;
				            } else {
				                if (!foundFirstEmpty) {
				                    firstEmpty = item;
				                    foundFirstEmpty = true;
				                }
				            }
				        }
				        
				        if (firstEmpty != null) {
				            nextElement = firstEmpty;
				            firstEmpty = null;
				            hasNextResult = true;
				            return true;
				        }
				        
				        hasNextResult = false;
				        if(!foundFirstEmpty){allFull=true;}
				        return false;
				    }

				    @Override
				    public DualInvBuffer next() {
				        if (!hasNext()) {
				            throw new java.util.NoSuchElementException();
				        }
				        
				        
				        hasNextComputed = false;
				        hasNextResult = null;
				        
				        DualInvBuffer result = nextElement;
				        nextElement = null;
				        return result;
				    }
					
					
				};
			}
		};
		
	}
	
	

	public void classify() {
		if (isRemote())
			return;
		final java.util.ArrayList<DualInvBuffer> candidates = treeLookup();
		for (DualInvBuffer inv0 : this.sortByEmptyItr()) {
			if (inv0.full() == false) {
				boolean done;
				if (candidates != null && candidates.contains(inv0)) {
					done = inv0.classifyMatched(this.mStoredFluid, mInventory, true);
				} else {
					done = !treeEligible(inv0) && inv0.classify(this.mStoredFluid, mInventory, true);
				}
				if (done) break;
			}
		}

	}

	public DualInvBuffer classifyForce() {
		if (isRemote())
			return null;
		final java.util.ArrayList<DualInvBuffer> candidates = treeLookup();
		for (DualInvBuffer inv0 : this.sortByEmptyItr()) {
			if (inv0.full() == false) {
				boolean done;
				if (candidates != null && candidates.contains(inv0)) {
					done = inv0.classifyMatched(this.mStoredFluid, mInventory, true);
				} else {
					done = (!treeEligible(inv0) && inv0.classify(this.mStoredFluid, mInventory, true))
						|| inv0.recordRecipeOrClassify(mStoredFluid, mInventory);
				}
				if (done) return inv0;
			}
		}
		return null;

	}

	boolean dirty;

	@Override
	public void onFill() {
		// Thread.dumpStack();
		classify();
		markDirty();
		dirty = true;
	}

	@Override
	public void setInventorySlotContents(int aIndex, ItemStack aStack) {
		super.setInventorySlotContents(aIndex, aStack);

		classify();
		markDirty();
		dirty = true;
	}

	final int offset = 0;

	
	static private final int BUFFER_0 = 1001;

	

	static int EX_CONFIG = 985211;

	private NBTTagCompound cv(String s) {
		try {
			return (NBTTagCompound) JsonToNBT.func_150315_a(s);
		} catch (NBTException e) {
			return new NBTTagCompound();
		}
	}



	public int moveButtons() {
		return 0;

	}

	public void onClassify() {
		last = getBaseMetaTileEntity().getTimer();
	}

	private long last;

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		if (aNBT.hasKey("x") == false)
			return;
		dirty = aNBT.getBoolean("dirty");
		int iex = aNBT.getInteger("exinvlen");
		boolean warn = false;
		for (int i = 0; i < bufferNum + iex; i++) {
			final int ii = i;
			NBTTagCompound tag = (NBTTagCompound) aNBT.getTag("BUFFER_" + ii);

			if (tag == null) {
				if (warn == false) {
					warn = true;
					MyMod.LOG.error("Tag broken:" + ii);
					MyMod.LOG.error(aNBT.toString());
				}

				continue;
			}

			if (i < bufferNum)
				inv0.get(i).fromTag(tag);
			else {
				DualInvBuffer append;
				inv0.add(append = new DualInvBuffer());
				append.init(this.mInventory.length - 1, this.mStoredFluid.length);
				inv0.get(i).fromTag(tag);
			}
		}autoAppend=aNBT.getBoolean("autoAppend");
		CMMode = aNBT.getBoolean("CMMode");
		merge = aNBT.getBoolean("merge");
		justHadNewItems = aNBT.getBoolean("justHadNewItems");
		updateEveryTick = aNBT.getBoolean("updateEveryTick");
		preventSleep = aNBT.getInteger("preventSleep");
		// legacy keys useNewGTPatternCache / currentID / order / detailmap* / detailmapUsage* are
		// intentionally ignored: the GT-pattern-cache experiment they belonged to was never wired
		// up (its consumers have been commented out for a long time) and the RecipeTree covers
		// recipe identity now. Old saves load fine; the stale tags are simply dropped.
		super.loadNBTData(aNBT);
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		aNBT.setBoolean("dirty", dirty);
		for (int i = 0; i < inv0.size(); i++)

			aNBT.setTag("BUFFER_" + i, inv0.get(i).toTag());
		aNBT.setInteger("exinvlen", inv0.size() - bufferNum);
		aNBT.setBoolean("CMMode", CMMode);
		aNBT.setBoolean("merge", merge);
		aNBT.setBoolean("justHadNewItems", justHadNewItems);
		aNBT.setBoolean("updateEveryTick", updateEveryTick);
		aNBT.setBoolean("autoAppend", autoAppend);
		aNBT.setInteger("preventSleep", preventSleep);

		super.saveNBTData(aNBT);
	}

	/*
	 * Matter Manipulator support: this class only adds its three ExConfig toggles on top of what
	 * DualInputHatch already copies. Deliberately NOT copied: the BUFFER_i inventories and exinvlen
	 * (real buffered stock, plus the extra buffers autoAppend grows at runtime), dirty /
	 * justHadNewItems / preventSleep / last (per-tick runtime state), and merge (still persisted, but
	 * its only toggle is commented out in onRightclick's saw branch, so it is not user-settable).
	 * No defensive nbt.copy() is needed here: unlike DecoyInputBusME this chain never rewrites
	 * "type" - the identifier resolves virtually, so super already stamps and validates this
	 * subclass's own class name.
	 */
	@Override
	public NBTTagCompound getCopiedData(EntityPlayer player) {
		NBTTagCompound ret = super.getCopiedData(player);
		ret.setBoolean("updateEveryTick", updateEveryTick);
		ret.setBoolean("CMMode", CMMode);
		ret.setBoolean("autoAppend", autoAppend);
		return ret;
	}

	@Override
	public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
		if (nbt == null || !getCopiedDataIdentifier(player)
			.equals(nbt.getString("type"))) return false;
		if (nbt.hasKey("updateEveryTick")) updateEveryTick = nbt.getBoolean("updateEveryTick");
		if (nbt.hasKey("CMMode")) CMMode = nbt.getBoolean("CMMode");
		// the same capability guard the elastic-buffer button uses (ExConfigEntry.shouldApply in
		// initExConfig): without the inf-buffer upgrade that button is not shown, so a tag taken
		// from an upgraded hatch must not switch elastic buffering on here
		if (nbt.hasKey("autoAppend") && (isInfBuffer() || shared.infbufUpgrades > 0))
			autoAppend = nbt.getBoolean("autoAppend");
		return super.pasteCopiedData(player, nbt);
	}

	private int count;
	public void programLoose() {
		if(((count++)%20)==1)
		program();
		}
	public void program() {

		for (DualInvBuffer inv0 : this.inv0) {
			inv0.programLocal();
		}
		/*
		 * for(int i=0;i<inv0.mStoredItemInternal.length-1;i++){ ItemStack
		 * is=inv0.mStoredItemInternal[i]; if(is==null)continue;
		 * if(is.getItem()!=MyMod.progcircuit)continue;
		 * inv0.mStoredItemInternal[i]=null;
		 * inv0.mStoredItemInternal[inv0.mStoredItemInternal.length-1]=
		 * GTUtility .copyAmount(0,ItemProgrammingCircuit.getCircuit(is)
		 * .orElse(null)) ; }
		 */

	}

	public class LimitedFluidTank implements IFluidTank {

		FluidTankG inner;
		private DualInvBuffer b;
		public LimitedFluidTank bind(DualInvBuffer b) {this.b=b;;return this;}
		public LimitedFluidTank(FluidTankG mStoredFluidInternal) {
			inner = mStoredFluidInternal;
		}

		@Override
		public FluidStack getFluid() {
			//b.nonempty=true;
			return inner.getFluid();
		}

		@Override
		public int getFluidAmount() {
			int ret = (int) Math.min(inner.getFluidAmount(), Integer.MAX_VALUE);

			if (ret > 64) {

				// System.out.println(ret);
			}
			return ret;
		}

		@Override
		public int getCapacity() {

			return (int) Math.min(fluidLimit(), Integer.MAX_VALUE);
		}

		@Override
		public FluidTankInfo getInfo() {

			return inner.getInfo();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			b.nonempty=true;
			return inner.fill(resource, doFill);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {

			return inner.drain(maxDrain, doDrain);
		}

	}

	boolean justHadNewItems;

	class PiorityBuffer implements Comparable<PiorityBuffer> {

		PiorityBuffer(DualInvBuffer buff) {
			this.buff = buff;
			this.piority = getPossibleCopies(buff);
		}

		DualInvBuffer buff;
		long piority;

		@Override
		public String toString() {
			return "" + piority;
		}

		@Override
		public int compareTo(PiorityBuffer o) {

			return -Long.compare(piority, o.piority);
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Optional</* ? extends */IDualInputInventory> getFirstNonEmptyInventory() {
		if (!this.isValid())
			return Optional.empty();
		markDirty();
		dirty = true;

		if (Config.experimentalOptimize) {

			return (Optional) inv0.stream().filter(s->s.nonempty||!emptyopt).filter((DualInvBuffer::isAccessibleForMulti)).map(s -> new PiorityBuffer(s))
					.sorted().map(s -> {
						return s.buff;
					}).findFirst();
		} else {

			return (Optional) inv0.stream().filter(s->s.nonempty||!emptyopt).filter((DualInvBuffer::isAccessibleForMulti)).findFirst();

		}

	}

	private Predicate<DualInvBuffer> not(Predicate<DualInvBuffer> s) {
		return s.negate();
	}

	boolean merge;

	@SuppressWarnings("unchecked")
	public List<IDualInputInventory> inventoriesReal() {

		return (List) (inv0);
	}

	@Override
	public Iterator<? extends IDualInputInventoryWithPattern> inventories() {
		if (!this.isValid())
			return emptyItr;
		markDirty();
		dirty = true;

		/*
		 * if (merge) { return mergeSame(); }
		 */

		if (Config.experimentalOptimize) {

			return inv0.stream().filter(s->s.nonempty||!emptyopt).filter(DualInvBuffer::isAccessibleForMulti).map(s -> new PiorityBuffer(s)).sorted()
					.map(s -> {
						return s.buff;
					}).map(this::wrap)

					.iterator();
		}
		return inv0.stream().filter(s->s.nonempty||!emptyopt).filter(DualInvBuffer::isAccessibleForMulti).map(this::wrap).iterator();

	}


	private IDualInputInventoryWithPattern wrap(DualInvBuffer to) {
		/*if (to.PID > 0 && useNewGTPatternCache) {

			return new PatternDualInv(to);
		}*/

		return to;
	}

	static Random ran = new Random();
	final int mask = ran.nextInt();

	//LinkedList<PatternDualInv> toDisconnect = new LinkedList<PatternDualInv>();

	/*public class PatternDualInv implements IDualInputInventoryWithPattern {
		@Override
		public boolean shouldBeCached() {
		
			return useNewGTPatternCache&&ID!=0;
		}
		public BufferedDualInputHatch parent() {
			return BufferedDualInputHatch.this;
		}

		@Nullable
		DualInvBuffer wrapped;
		final int ID;
		
        public int reset;
		public PatternDualInv(DualInvBuffer to) {
			toDisconnect.add(this);
			wrapped = to;
			ID = to.PID;
		}

		public PatternDualInv(int to) {

			ID = to;
		}

		@Override
		public int hashCode() {
			return ID ^ mask;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PatternDualInv) {
				return this.hashCode() == ((PatternDualInv) obj).hashCode()
						&& parent() == ((PatternDualInv) obj).parent();
			}
			return super.equals(obj);
		}

		@Override
		public boolean isEmpty() {

			return wrapped.isEmpty();
		}

		@Override
		public ItemStack[] getItemInputs() {

			return wrapped.getItemInputs();
		}

		@Override
		public FluidStack[] getFluidInputs() {

			return wrapped.getFluidInputs();
		}

		@Override
		public GTDualInputPattern getPatternInputs() {

	
				
				
			
			return wrapped.getPatternInputs();
		}

	}*/

	@Override
	public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
			int z) {
		tag.setInteger("exinvlen", inv0.size() - bufferNum);
		tag.setBoolean("sleep", sleep);
		tag.setInteger("sleepTime", sleepTime);
		tag.setInteger("inv_size", bufferNum);
		IntStream.range(0, bufferNum).forEach(s -> {
			DualInvBuffer inv = inv0.get(s);
			NBTTagCompound sub = new NBTTagCompound();
			tag.setTag("No" + s, sub);
			sub.setBoolean("full", inv.full());
			sub.setBoolean("noClear", inv.lock);
			sub.setBoolean("locked", inv.recipeLocked);
			sub.setBoolean("empty", inv.isEmpty());
			//sub.setInteger("patternID", inv.PID);
			RecipeTracker rt = new RecipeTracker();

			sub.setString("lock_item",
					IntStream.range(0, inv.mStoredItemInternalSingle.length)
							.mapToObj(ss -> new IndexedObject<>(ss, inv.mStoredItemInternalSingle[ss]))
							.filter(ss -> ss.holded != null).map(ss -> {
								rt.track(ss.holded, inv.mStoredItemInternal[ss.index]);
								return "#" + ss.index + ":" + ss.holded.getDisplayName() + "x" + ss.holded.stackSize;
							}).collect(StringBuilder::new, (a, b) -> a.append(((a.length() == 0) ? "" : "\n") + b),
									(a, b) -> a.append(b))
							.toString());
			sub.setString("lock_fluid",
					IntStream.range(0, inv.mStoredFluidInternalSingle.length)
							.mapToObj(ss -> new IndexedObject<>(ss, inv.mStoredFluidInternalSingle[ss]))
							.filter(ss -> ss.holded.getFluidAmount() > 0).map(ss -> {
								rt.track(ss.holded, inv.mStoredFluidInternal[ss.index]);
								return "#" + ss.index + ":" + ss.holded.getFluid().getLocalizedName() + "x"
										+ ss.holded.getFluidAmount();
							}).collect(StringBuilder::new, (a, b) -> a.append(((a.length() == 0) ? "" : "\n") + b),
									(a, b) -> a.append(b))
							.toString());

			sub.setLong("possibleCopies", (rt.broken || (!rt.onceCompared && !inv.isEmpty())) ? -1 : rt.times);
		});

		super.getWailaNBTData(player, tile, tag, world, x, y, z);
	}

	private static class IndexedObject<T> {

		private T holded;
		private int index;

		IndexedObject(int i, T obj) {
			this.holded = obj;
			this.index = i;
		}
	}

	private static class RecipeTracker {

		boolean broken;
		long times;
		boolean first = true;
		boolean onceCompared;

		public void track(@Nonnull ItemStack recipe, @Nullable ItemStackG mStoredItemInternal) {
			if (recipe.getItem() instanceof ItemProgrammingCircuit) {
				onceCompared = true;
				return;
			}
			if (recipe.getItem() != (mStoredItemInternal == null ? null : mStoredItemInternal.getItem())) {
				broken = true;
				onceCompared = true;
				return;
			}
			int a = recipe.stackSize;
			long b = Optional.ofNullable(mStoredItemInternal).map(s -> s.stackSize()).orElse(0l);
			track(a, b, false);
		}

		public void track(@Nonnull FluidTank recipe, @Nonnull FluidTankG storage) {
			if (recipe.getFluid().getFluid() != Optional.of(storage).map(FluidTankG::getFluid).map(FluidStack::getFluid)
					.orElse(null)) {
				broken = true;
				onceCompared = true;
				return;
			}

			int a = recipe.getFluidAmount();
			long b = storage.getFluidAmount();
			track(a, b, false);
		}

		public void track(int a, long b, boolean ignoreEmpty) {
			long t = 0;
			if (a == 0) {
				broken = true;
				return;
				/* Actually impossible */}
			if (b == 0) {
				if (!ignoreEmpty)
					broken = true;
				return;
			}
			if (b % a != 0) {
				broken = true;
				return;
			}
			t = b / a;
			if (t != times) {
				onceCompared = true;
				if (first) {
					first = false;
					times = t;
					return;
				} else {
					broken = true;
					return;
				}
			}

		}
	}

	public long getPossibleCopies(DualInvBuffer toCheck) {
		DualInvBuffer inv = toCheck;
		RecipeTracker rt = new RecipeTracker();

		IntStream.range(0, inv.mStoredItemInternalSingle.length)
				.mapToObj(ss -> new IndexedObject<>(ss, inv.mStoredItemInternalSingle[ss]))
				.filter(ss -> ss.holded != null).forEach(ss -> {
					rt.track(ss.holded, inv.mStoredItemInternal[ss.index]);
				});

		IntStream.range(0, inv.mStoredFluidInternalSingle.length)
				.mapToObj(ss -> new IndexedObject<>(ss, inv.mStoredFluidInternalSingle[ss]))
				.filter(ss -> ss.holded.getFluidAmount() > 0).forEach(ss -> {
					rt.track(ss.holded, inv.mStoredFluidInternal[ss.index]);
				});
		return (rt.broken || (!rt.onceCompared && !inv.isEmpty())) ? -1 : rt.times;

	}

	@Override
	public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {

		super.getWailaBody(itemStack, currenttip, accessor, config);
		NBTTagCompound tag = accessor.getNBTData();
		if (Config.debug || Config.dev)
			currenttip.add(

					"sleep:" + tag.getBoolean("sleep") + " " + tag.getInteger("sleepTime")

			);


		int idle[] = new int[1];
		IntStream.range(0, tag.getInteger("inv_size")).forEach(s -> {
			NBTTagCompound sub = (NBTTagCompound) tag.getTag("No" + s);
			boolean noClear = sub.getBoolean("noClear");
			int st = (sub.getBoolean("full") ? 1 : 0) + (sub.getBoolean("empty") ? 2 : 0)
					+ (sub.getBoolean("locked") ? 4 : 0);
			String info = "";
			switch (st) {
			case 0b000:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.000");
				break;
			case 0b001:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.001");
				break;
			case 0b010:
				idle[0]++;
				if (idle[0] > 5)
					return;
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.010");
				break;
			case 0b011:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.011");
				break;
			case 0b100:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.100");
				break;
			case 0b101:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.101");
				break;
			case 0b110:
				info = noClear ? LangManager.translateToLocal("programmable_hatches.buffer.waila.110.0")
						: LangManager.translateToLocal("programmable_hatches.buffer.waila.110.1");
				break;
			case 0b111:
				info = LangManager.translateToLocal("programmable_hatches.buffer.waila.111");
				break;

			}
			String cpinfo = "";
			long copies = sub.getLong("possibleCopies");
			if (copies == -1 && (sub.getBoolean("locked"))// if not locked, do
															// not warn about
															// the copies
					&& (!sub.getBoolean("empty"))// if empty, actual copies will
													// be zero but will count as
													// broken, so do not warn.
			)
				cpinfo = cpinfo + LangManager.translateToLocal("programmable_hatches.buffer.waila.broken");
			if (copies > 0) {
				cpinfo = cpinfo + LangManager.translateToLocalFormatted("programmable_hatches.buffer.waila.copies",
						copies + "");
				if (!sub.getBoolean("locked")) {
					cpinfo += "???STRANGE SITUATION???";
				}
			}
			String prefix = "";
			if (sub.getInteger("patternID") > 0) {
				prefix = "<" + sub.getInteger("patternID") + ">";
			}
			if (sub.getInteger("patternID") == 0 && !sub.getBoolean("empty")) {
				prefix = "△";
			}

			// ( ?sub.getInteger("patternID"):"△")
			currenttip.add(prefix + "#" + s + " " + info + " " + cpinfo);
			String lock_item = sub.getString("lock_item");
			String lock_fluid = sub.getString("lock_fluid");
			if ((!lock_item.isEmpty()) && (!lock_item.isEmpty())) {
				// currenttip.add();
				currenttip.add(" " + LangManager.translateToLocal("programmable_hatches.buffer.waila.present"));

			}
			if (!lock_item.isEmpty())
				Arrays.stream(lock_item.split("\n")).map(ss -> " " + ss).forEach(currenttip::add);
			if (!lock_fluid.isEmpty())
				Arrays.stream(lock_fluid.split("\n")).map(ss -> " " + ss).forEach(currenttip::add);

		});
		;
		if (idle[0] > 5)
			currenttip.add(LangManager.translateToLocalFormatted("programmable_hatches.buffer.waila.hidden",
					(idle[0] - 5) + ""));
		if (tag.getInteger("exinvlen") > 0)
			currenttip.add("Extra buffer:" + tag.getInteger("exinvlen"));
	}

	private Boolean isRemote;

	public boolean isRemote() {
		if (isRemote == null)
			isRemote = FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;// this.getBaseMetaTileEntity().getWorld().isRemote;
		return isRemote;
	}

	@Override
	public void updateSlots() {
		inv0.forEach(DualInvBuffer::updateSlots);
		super.updateSlots();
	}

	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {

		/*
		 * mergeSame().forEachRemaining(s->{
		 * System.out.println(Arrays.toString(s.getItemInputs())); });
		 */
		BaseMetaTileEntity tile = (BaseMetaTileEntity) this.getBaseMetaTileEntity();
		if (tile.isServerSide()) {
			if (!tile.privateAccess() || aPlayer.getDisplayName().equalsIgnoreCase(tile.getOwnerName())) {
				final ItemStack tCurrentItem = aPlayer.inventory.getCurrentItem();
				if (tCurrentItem != null) {
					boolean suc = false;
					for (int id : OreDictionary.getOreIDs(tCurrentItem)) {
						if (OreDictionary.getOreName(id).equals(ToolDictNames.craftingToolFile.toString())) {
							suc = true;
							break;
						}
						;
					}
					if (suc) {
						GTModHandler.damageOrDechargeItem(tCurrentItem, 1, 1000, aPlayer);
						GTUtility.sendSoundToPlayers(tile.getWorld(), SoundResource.IC2_TOOLS_WRENCH, 1.0F, -1,
								tile.getXCoord(), tile.getYCoord(), tile.getZCoord());
						updateEveryTick = !updateEveryTick;

						GTUtility.sendChatToPlayer(aPlayer, "updateEveryTick:" + updateEveryTick);
						/*
						 * GTUtility .sendChatToPlayer(aPlayer,
						 * LangManager.translateToLocal(
						 * "programmable_hatches.gt.updateEveryTick") );
						 */
						aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.updateEveryTick"));

						markDirty();
						return true;
					}

				}
				if (tCurrentItem != null) {
					boolean suc = false;
					for (int id : OreDictionary.getOreIDs(tCurrentItem)) {
						if (OreDictionary.getOreName(id).equals(ToolDictNames.craftingToolSaw.toString())) {
							suc = true;
							break;
						}
						;
					}
					if (suc) {
						GTModHandler.damageOrDechargeItem(tCurrentItem, 1, 1000, aPlayer);
						GTUtility.sendSoundToPlayers(tile.getWorld(), SoundResource.IC2_TOOLS_CHAINSAW_CHAINSAW_USE_TWO,
								1.0F, -1, tile.getXCoord(), tile.getYCoord(), tile.getZCoord());
						/*
						 * merge = !merge; GTUtility.sendChatToPlayer(aPlayer,
						 * "merge:" + merge); aPlayer.addChatMessage(new
						 * ChatComponentTranslation(
						 * "programmable_hatches.gt.merge"));
						 */
						markDirty();
						return true;
					}

				}
			}
		}

		return super.onRightclick(aBaseMetaTileEntity, aPlayer);
	}

	@Override
	public CheckRecipeResult endRecipeProcessingImpl(MTEMultiBlockBase controller) {
		dirty = true;
		return super.endRecipeProcessingImpl(controller);
	}

	@Override
	public void onBlockDestroyed() {
		IGregTechTileEntity te = this.getBaseMetaTileEntity();
		World aWorld = te.getWorld();
		int aX = te.getXCoord();
		short aY = te.getYCoord();
		int aZ = te.getZCoord();
		for (DualInvBuffer inv : this.inv0)
			for (int i = 0; i < inv.mStoredItemInternal.length; i++) {
				final ItemStackG tItem = inv.mStoredItemInternal[i];
				if ((tItem != null) && (tItem.stackSize() > 0)) {
					final EntityItem tItemEntity = new EntityItem(aWorld, aX + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
							aY + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F, aZ + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
							tItem.getStack());

					tItemEntity.motionX = (XSTR_INSTANCE.nextGaussian() * 0.05D);
					tItemEntity.motionY = (XSTR_INSTANCE.nextGaussian() * 0.25D);
					tItemEntity.motionZ = (XSTR_INSTANCE.nextGaussian() * 0.05D);
					aWorld.spawnEntityInWorld(tItemEntity);
					tItem.stackSize(0);
					inv.mStoredItemInternal[i] = null;
				}
			}
		super.onBlockDestroyed();
	}

	public boolean isInputEmpty() {

		for (FluidTank f : mStoredFluid) {
			if (f.getFluidAmount() > 0) {
				return false;
			}
		}
		for (ItemStack i : mInventory) {

			if (i != null && i.stackSize > 0) {
				return false;
			}
		}
		return true;
	}

	/*
	 * @Override public boolean onRightclick(IGregTechTileEntity
	 * aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side, float aX,
	 * float aY, float aZ) { return super.onRightclick(aBaseMetaTileEntity,
	 * aPlayer, side, aX, aY, aZ); }
	 */
	/*
	 * @SuppressWarnings("unchecked") public Iterator<? extends
	 * IDualInputInventory> mergeSame() { class Wrapper { DualInvBuffer d;
	 * public Wrapper(DualInvBuffer s) { d = s; } boolean fast = true;; private
	 * int sft(int i, int t) { return i ^ t; }
	 * 
	 * @Override public int hashCode() { if (fast) { int c = 0; int hash = 0;
	 * for (int i = 0; i < d.mStoredFluidInternalSingle.length; i++) {
	 * FluidStack f = d.mStoredFluidInternalSingle[i].getFluid(); if (f != null)
	 * { hash = hash ^ sft(f.getFluidID(), c); } c++; } for (int i = 0; i <
	 * d.mStoredItemInternalSingle.length; i++) { ItemStack f =
	 * d.mStoredItemInternalSingle[i]; if (f != null) { { hash = hash ^
	 * sft(Item.getIdFromItem(f.getItem()) | f.getItemDamage() , c); } c++; } }
	 * return hash; } int hash = 0; for (int i = 0; i <
	 * d.mStoredFluidInternalSingle.length; i++) { FluidStack f =
	 * d.mStoredFluidInternalSingle[i].getFluid(); if (f != null) hash ^=
	 * f.hashCode(); int a = hash & 1; hash = hash >>> 1; if (a != 0) hash |=
	 * 0x80000000; } for (int i = 0; i < d.mStoredItemInternalSingle.length;
	 * i++) { ItemStack f = d.mStoredItemInternalSingle[i]; if (f != null) {
	 * hash ^= f.stackSize * 31 + +Item.getIdFromItem(f.getItem()); if
	 * (f.getTagCompound() != null) { hash ^= f.getTagCompound() .hashCode(); }
	 * } int a = hash & 1; hash = hash >>> 1; if (a != 0) hash |= 0x80000000; }
	 * return hash; }
	 * 
	 * @Override public boolean equals(Object obj) { if (obj == this) { return
	 * true; } boolean empty = true; DualInvBuffer a = d; DualInvBuffer b =
	 * ((Wrapper) obj).d; for (int i = 0; i <
	 * a.mStoredFluidInternalSingle.length; i++) { if
	 * (!fluidEquals(a.mStoredFluidInternalSingle[i],
	 * b.mStoredFluidInternalSingle[i])) { return false; } if
	 * (a.mStoredFluidInternalSingle[i].getFluidAmount() > 0) empty = false; }
	 * for (int i = 0; i < a.mStoredItemInternalSingle.length; i++) { if
	 * (!ItemStack.areItemStacksEqual(a.mStoredItemInternalSingle[i],
	 * b.mStoredItemInternalSingle[i])) { return false; } if
	 * (a.mStoredItemInternalSingle[i] != null) empty = false; } if (empty)
	 * return false; return true; } } Multimap<Wrapper, DualInvBuffer> a =
	 * HashMultimap.create(); inv0.stream()
	 * .filter((DualInvBuffer::isAccessibleForMulti)) .forEach(s -> { a.put(new
	 * Wrapper(s), s); }); return (Iterator<? extends IDualInputInventory>)
	 * a.asMap() .values() .stream() .map(s -> { if (s.size() == 1) { return
	 * s.iterator() .next(); } int sharedID = 0; int hashCode = 0; for
	 * (DualInvBuffer ss : s) { hashCode = ss.hashCode(); if (sharedID == 0)
	 * sharedID = ss.PID; else { if (sharedID != ss.PID) { sharedID = 0; break;
	 * } } } int hashCodef = hashCode; int PID = sharedID; return new
	 * INeoDualInputInventory() {
	 * 
	 * @Override public boolean areYouSerious() { return true; }
	 * 
	 * @Override public IDualInputInventory butYouCanCacheThisInstead() { if
	 * (PID == 0) return null; return s.iterator() .next(); }
	 * 
	 * @Override public int hashCode() { return hashCodef; }
	 * 
	 * @Override public boolean equals(Object obj) { if (obj instanceof
	 * DualInvBuffer) { return PID == ((DualInvBuffer) obj).PID && hashCodef ==
	 * obj.hashCode(); } return super.equals(obj); } void init() {
	 * Iterator<DualInvBuffer> itr = s.iterator(); int icount = 0; ItemStack[][]
	 * idata = new ItemStack[s.size()][]; int fcount = 0; FluidStack[][] fdata =
	 * new FluidStack[s.size()][]; for (int i = 0; i < s.size(); i++) {
	 * DualInvBuffer e = itr.next(); idata[i] =
	 * filterStack.apply(e.mStoredItemInternal); icount += idata[i].length;
	 * fdata[i] = asFluidStack.apply(e.mStoredFluidInternal); fcount +=
	 * fdata[i].length; } i = new ItemStack[icount]; f = new FluidStack[fcount];
	 * int ic = 0; for (ItemStack[] ii : idata) { for (ItemStack iii : ii) {
	 * i[ic] = iii; ic++; } } ic = 0; for (FluidStack[] ii : fdata) { for
	 * (FluidStack iii : ii) { f[ic] = iii; ic++; } } i = filterStack.apply(i,
	 * shared.getItems()); if (!shared.isDummy())// dummy->no extra fluid f =
	 * asFluidStack.apply(f, shared.getFluid()); } ItemStack[] i; FluidStack[]
	 * f;
	 * 
	 * @Override public ItemStack[] getItemInputs() { if (i == null) init();
	 * return i; }
	 * 
	 * @Override public FluidStack[] getFluidInputs() { if (f == null) init();
	 * return f; } }; }) .iterator(); }
	 */
	
	
	// ===================== recipe-hash fast path =====================
	// classify() compares the whole input region against every buffer's recorded recipe slot by
	// slot (ItemStack.areItemStacksEqual incl. NBT + fluidEquals incl. amount) every tick while
	// awake. A single int hash of the region, using exactly the fields those predicates compare,
	// lets the per-buffer scan bail out with one int comparison: equal contents imply equal hashes,
	// so a hash mismatch proves classify() would have returned false. A collision merely falls
	// through to the full (authoritative) comparison.

	/**
	 * Cheap slot fingerprints used as the recipe tree's bucket keys: no NBT, no registry lookups —
	 * Item/Fluid are singletons so System.identityHashCode is a sound stand-in. Contents that the
	 * classify() predicates consider equal always produce equal cheap keys, so a matching node (if
	 * any) is always in the input slot's own bucket; NBT is only deep-compared inside a bucket hit.
	 */
	static public int hashFluidSlotCheap(FluidStack fs) {
		if (fs == null || fs.amount == 0) return 0;
		return System.identityHashCode(fs.getFluid()) * 31 + fs.amount;
	}

	static public int hashItemSlotCheap(ItemStack s) {
		if (s == null) return 0;
		int h = System.identityHashCode(s.getItem());
		h = h * 31 + Items.feather.getDamage(s);
		h = h * 31 + s.stackSize;
		return h;
	}

	/**
	 * Recipe-single tank mirrored into one slot of the buffer's contiguous singleBack array; the
	 * tank's own field stays the source of truth, only field assignments are mirrored (all of them
	 * go through the overridden methods here — FluidTank.readFromNBT dispatches virtually to
	 * setFluid). Same idea as BackedListeningFluidTank on the input side.
	 */
	static final class BackedFluidTank extends FluidTank {

		private final FluidStack[] back;
		private final int idx;

		BackedFluidTank(FluidStack[] back, int idx) {
			super(Integer.MAX_VALUE);
			this.back = back;
			this.idx = idx;
		}

		@Override
		public void setFluid(FluidStack f) {
			super.setFluid(f);
			back[idx] = this.fluid;
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			try {
				return super.fill(resource, doFill);
			} finally {
				back[idx] = this.fluid;
			}
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			try {
				return super.drain(maxDrain, doDrain);
			} finally {
				back[idx] = this.fluid;
			}
		}
	}

	/** Mirrors fluidEquals minus the tag comparison — never rejects anything fluidEquals accepts. */
	static boolean fluidSlotMatchesNoTag(FluidStack fa, FluidStack fb) {
		if (fa == null && fb == null) return true;
		if ((fa == null ? 0 : fa.amount) != (fb == null ? 0 : fb.amount)) return false;
		return fa == null || fb == null || fa.getFluid() == fb.getFluid();
	}

	/** The NBT-free half of ItemStack.areItemStacksEqual: null↔null, item, raw damage, stackSize. */
	static boolean itemSlotMatchesCheap(ItemStack a, ItemStack b) {
		if (a == null || b == null) return a == b;
		return a.getItem() == b.getItem() && Items.feather.getDamage(a) == Items.feather.getDamage(b)
			&& a.stackSize == b.stackSize;
	}

	/** The tag half of ItemStack.areItemStacksEqual (null-safe deep equality). */
	static boolean tagMatches(Object a, Object b) {
		return a == null ? b == null : b != null && a.equals(b);
	}

	/**
	 * Sparse prefix tree over all recorded recipes of this hatch. A recipe/input region is
	 * decomposed into its NON-EMPTY (slot, content) pairs only — empty slots never become nodes,
	 * the slot index inside every key encodes the gaps (so item-gap-item and two adjacent items
	 * differ by construction). Three sections in fixed order, each enumerated in DESCENDING slot
	 * order (the user's "match from the last occupied slot backwards"):
	 * <p>
	 * section A: occupied fluid slots, keyed (slot, fluid identity + amount), tags ignored by
	 * design (fluid tags are dead in practice - a tree match is definitive even if they differ);
	 * section B: occupied item slots, keyed (slot, item / raw damage / count) - no NBT;
	 * section C: those same item slots' tags, in section B's order, bucketed by tag.hashCode()
	 * computed lazily only if the walk survives that far. NBT therefore stays last: it is touched
	 * only when every fluid and every item's cheap half already matched.
	 * <p>
	 * Because pair 0 is the HIGHEST occupied slot, a growing input dies at the root in O(1): a
	 * 1~15-item input probes the root bucket with (slot14, ...) while a 1~16 recipe's first key
	 * is (slot15, ...) - no shared prefix is ever walked. Walk length = the input's own pair
	 * count; matching ends exactly when the input's pairs are exhausted (tail leaves), so no
	 * per-node length census is needed. Deterministic, no backtracking.
	 * <p>
	 * A tree match is EXACT equality of the whole region (modulo fluid tags), so the scan merges
	 * via classifyMatched() without re-comparing; a null lookup proves no buffer can match. Node
	 * keys reference the recorded *Single contents (stable between record and clear).
	 * <p>
	 * INVARIANT: buffer.inTree == (its recorded recipe is inserted), maintained in the same call
	 * stack at every mutation site - record, clear, fromTag/load, compaction move, inv0 removal.
	 */
	public static final class RecipeTree {

		static final byte SEC_FLUID = 0, SEC_ITEM = 1, SEC_TAG = 2;

		static final class Node {

			final byte sec;
			final int slot;
			/** FluidStack (A) / ItemStack (B) / NBTTagCompound or null (C). */
			final Object key;
			java.util.HashMap<Integer, java.util.ArrayList<Node>> children;
			/** Recipes whose pair list ends exactly at this node. */
			java.util.ArrayList<DualInvBuffer> tail;

			Node(byte sec, int slot, Object key) {
				this.sec = sec;
				this.slot = slot;
				this.key = key;
			}

			boolean isEmpty() {
				return (children == null || children.isEmpty()) && (tail == null || tail.isEmpty());
			}
		}

		final Node root = new Node((byte) -1, -1, null);

		/**
		 * Decomposes a region (works for both the recorded singles and the live input arrays)
		 * into its non-empty pairs: fluids by descending slot, then items by descending slot,
		 * then those items' tags in the same order. Fills sec/slot/content and the PRE-computable
		 * bucket keys (ck of tag pairs is computed lazily in bucketKey - never here).
		 *
		 * @return pair count
		 */
		private static int enumerate(FluidStack[] fback, ItemStack[] items, int f, int i, int[] ck, byte[] sec,
			int[] slot, Object[] content) {
			int n = 0;
			// fluids scan the contiguous backend mirror (see BackedListeningFluidTank/BackedFluidTank):
			// NullScan hops the null runs; a non-null 0-amount stack just needs the follow-up check
			for (int s = reobf.proghatches.util.NullScan.lastNonNull(fback, Math.min(f, fback.length) - 1); s >= 0;
				s = s == 0 ? -1 : reobf.proghatches.util.NullScan.lastNonNull(fback, s - 1)) {
				FluidStack fs = fback[s];
				if (fs.amount == 0) continue;
				sec[n] = SEC_FLUID;
				slot[n] = s;
				content[n] = fs;
				ck[n] = (SEC_FLUID * 31 + s) * 31 + hashFluidSlotCheap(fs);
				n++;
			}
			int itemsStart = n;
			// Unsafe-accelerated null-run skipping (pairs of reference slots zero-tested per
			// getLong under compressed oops; plain-loop fallback inside)
			for (int s = reobf.proghatches.util.NullScan.lastNonNull(items, Math.min(i, items.length) - 1); s >= 0;
				s = s == 0 ? -1 : reobf.proghatches.util.NullScan.lastNonNull(items, s - 1)) {
				ItemStack st = items[s];
				sec[n] = SEC_ITEM;
				slot[n] = s;
				content[n] = st;
				ck[n] = (SEC_ITEM * 31 + s) * 31 + hashItemSlotCheap(st);
				n++;
			}
			int itemsEnd = n;
			for (int k = itemsStart; k < itemsEnd; k++) {
				ItemStack st = (ItemStack) content[k];
				sec[n] = SEC_TAG;
				slot[n] = slot[k];
				content[n] = st.stackTagCompound;
				n++;
			}
			return n;
		}

		/** Tag-pair bucket keys are derived here, lazily, so an early divergence never hashes NBT. */
		private static int bucketKey(int d, int[] ck, byte[] sec, Object[] content) {
			if (sec[d] == SEC_TAG) return SEC_TAG * 961 + (content[d] == null ? 0 : content[d].hashCode());
			return ck[d];
		}

		private static boolean nodeMatches(Node n, byte sec, int slot, Object content) {
			if (n.sec != sec) return false;
			if (sec == SEC_FLUID) return n.slot == slot && fluidSlotMatchesNoTag((FluidStack) n.key, (FluidStack) content);
			if (sec == SEC_ITEM) return n.slot == slot && itemSlotMatchesCheap((ItemStack) n.key, (ItemStack) content);
			return tagMatches(n.key, content);
		}

		public void insert(DualInvBuffer buf, int f, int i) {
			if (buf.inTree) remove(buf, f, i); // idempotence / self-heal
			int cap = f + i + i;
			int[] ck = new int[cap];
			byte[] sec = new byte[cap];
			int[] slot = new int[cap];
			Object[] content = new Object[cap];
			int n = enumerate(buf.singleBack, buf.mStoredItemInternalSingle, f, i, ck, sec, slot, content);
			if (n == 0) return; // nothing recorded: never inserted (recordRecipeOrClassify can't produce this)
			Node cur = root;
			for (int d = 0; d < n; d++) {
				if (cur.children == null) cur.children = new java.util.HashMap<>();
				int k = bucketKey(d, ck, sec, content);
				java.util.ArrayList<Node> bucket = cur.children.get(k);
				if (bucket == null) cur.children.put(k, bucket = new java.util.ArrayList<>(1));
				Node next = null;
				for (Node node : bucket) {
					if (nodeMatches(node, sec[d], slot[d], content[d])) {
						next = node;
						break;
					}
				}
				if (next == null) bucket.add(next = new Node(sec[d], slot[d], content[d]));
				cur = next;
			}
			if (cur.tail == null) cur.tail = new java.util.ArrayList<>(1);
			if (!cur.tail.contains(buf)) cur.tail.add(buf);
			buf.inTree = true;
		}

		public void remove(DualInvBuffer buf, int f, int i) {
			if (!buf.inTree) return;
			buf.inTree = false;
			int cap = f + i + i;
			int[] ck = new int[cap];
			byte[] sec = new byte[cap];
			int[] slot = new int[cap];
			Object[] content = new Object[cap];
			int n = enumerate(buf.singleBack, buf.mStoredItemInternalSingle, f, i, ck, sec, slot, content);
			Node[] chain = new Node[n + 1];
			int[] cks = new int[n];
			chain[0] = root;
			for (int d = 0; d < n; d++) {
				Node cur = chain[d];
				if (cur.children == null) return; // drift: path missing, nothing to prune
				cks[d] = bucketKey(d, ck, sec, content);
				java.util.ArrayList<Node> bucket = cur.children.get(cks[d]);
				if (bucket == null) return;
				Node next = null;
				for (Node node : bucket) {
					if (nodeMatches(node, sec[d], slot[d], content[d])) {
						next = node;
						break;
					}
				}
				if (next == null) return;
				chain[d + 1] = next;
			}
			if (chain[n].tail != null) chain[n].tail.remove(buf);
			for (int d = n; d >= 1; d--) {
				if (!chain[d].isEmpty()) break; // still shared by other recipes
				java.util.ArrayList<Node> bucket = chain[d - 1].children.get(cks[d - 1]);
				if (bucket != null) {
					bucket.remove(chain[d]);
					if (bucket.isEmpty()) chain[d - 1].children.remove(cks[d - 1]);
				}
			}
		}

		/**
		 * Definitive matches (buffers whose recorded recipe EQUALS the input region, modulo fluid
		 * tags), or null. Fails at the first divergence; pair 0 already compares the highest
		 * occupied slot.
		 */
		public java.util.ArrayList<DualInvBuffer> lookup(FluidStack[] fin, ItemStack[] iin, int f, int i) {
			int cap = f + i + i;
			int[] ck = new int[cap];
			byte[] sec = new byte[cap];
			int[] slot = new int[cap];
			Object[] content = new Object[cap];
			int n = enumerate(fin, iin, f, i, ck, sec, slot, content);
			if (n == 0) return null; // empty input: no recipe is empty
			Node cur = root;
			for (int d = 0; d < n; d++) {
				if (cur.children == null) return null;
				java.util.ArrayList<Node> bucket = cur.children.get(bucketKey(d, ck, sec, content));
				if (bucket == null) return null;
				Node next = null;
				for (Node node : bucket) {
					if (nodeMatches(node, sec[d], slot[d], content[d])) {
						next = node;
						break;
					}
				}
				if (next == null) return null;
				cur = next;
			}
			return cur.tail; // recipes with MORE pairs hang deeper and are correctly not returned
		}
	}
	public final transient RecipeTree recipeTree = new RecipeTree();

	/** Buffers with legacy-NBT slot counts never enter the tree; the scan falls back to plain classify for them. */
	boolean treeEligible(DualInvBuffer b) {
		return b.f == this.mStoredFluid.length && b.i == this.mInventory.length - 1;
	}

	void treeInsert(DualInvBuffer b) {
		if (!treeEligible(b)) return;
		recipeTree.insert(b, b.f, b.i);
	}

	void treeRemove(DualInvBuffer b) {
		recipeTree.remove(b, b.f, b.i);
	}

	java.util.ArrayList<DualInvBuffer> treeLookup() {
		return recipeTree.lookup(this.mStoredFluidBack, this.mInventory, this.mStoredFluid.length, this.mInventory.length - 1);
	}

	/**
	 * 2 both null
	 * 0 not same
	 * 1 same
	 * */
	static public int fluidEquals(FluidTank a, FluidTank b) {
		// if(a==b)return false;
		// if(a==null||b==null)return false;
		if (a.getFluid() == null && b.getFluid() == null)
			return 2;
		if (a.getFluidAmount() != b.getFluidAmount())
			return 0;
		
		if (a.getFluid() != null && (!a.getFluid().equals(b.getFluid())))
			return 0;

		return 1;
	}

	static public boolean fluidEquals(FluidStack a, FluidStack b) {
		// if(a==b)return false;
		// if(a==null||b==null)return false;

		if (a == null && b == null)
			return true;
		if ((a == null) || (b == null))
			return false;
		if (a.amount != b.amount) {
			return false;
		}
		if (a.getFluid() != b.getFluid()) {
			return false;
		}
		return true;
	}

	

	public boolean isInfBuffer() {
		return false;
	}

	public boolean isInputEmpty(BufferedDualInputHatch master) {

		for (FluidTank f : master.mStoredFluid) {
			if (f.getFluidAmount() > 0) {
				return false;
			}
		}
		for (ItemStack i : master.mInventory) {

			if (i != null && i.stackSize > 0) {
				return false;
			}
		}
		return true;
	}

	public void clearInv(BufferedDualInputHatch master) {

		for (FluidTank f : master.mStoredFluid) {
			f.setFluid(null);
		}
		for (int i = 0; i < master.mInventory.length; i++) {

			if (master.isValidSlot(i)) {
				master.mInventory[i] = null;
			}
		}

	}

	boolean CMMode = false;

	@Override
	public boolean pushPatternCM(ICraftingPatternDetails patternDetails, InventoryCrafting table,
			ForgeDirection ejectionDirection) {
		BufferedDualInputHatch master = this;
		if (this instanceof PatternDualInputHatch) {
			PatternDualInputHatch dih = ((PatternDualInputHatch) this);
			try {
				dih.skipActiveCheck = true;
				return dih.pushPattern(patternDetails, table);
			} finally {
				dih.skipActiveCheck = false;
			}
		}
		if (master != null) {
			if (!isInputEmpty(master)) {
				return false;
			}
			int i = 0;
			int f = 0;
			int ilimit = master.getInventoryStackLimit();
			int flimit = master.getInventoryFluidLimit();
			boolean isplit = master.disableLimited;
			boolean fsplit = master.fluidLimit == 0;
			for (int index = 0; index < table.getSizeInventory(); index++) {
				ItemStack is = (table.getStackInSlot(index));
				if (is == null)
					continue;
				is = is.copy();
				if (is.getItem() instanceof ItemFluidPacket) {
					FluidStack fs = ItemFluidPacket.getFluidStack(is);
					if (fs == null) {
						continue;
					}
					while (fs.amount > 0) {
						if (f >= master.mStoredFluid.length) {
							clearInv(master);
							return false;
						}
						int tosplit = Math.min(fs.amount, flimit);
						fs.amount -= tosplit;
						if ((!fsplit) && fs.amount > 0) {
							clearInv(master);
							return false;
						}
						FluidStack splitted = new FluidStack(fs.getFluid(), tosplit);
						master.mStoredFluid[f].setFluidDirect(splitted);
						f++;
					}

				} else {
					while (is.stackSize > 0) {
						if (master.isValidSlot(i) == false) {
							clearInv(master);
							return false;
						}
						ItemStack splitted = is.splitStack(Math.min(is.stackSize, ilimit));
						if ((!isplit) && is.stackSize > 0) {
							clearInv(master);
							return false;
						}
						master.mInventory[i] = splitted;
						i++;
					}
				}

			}
			if (master instanceof BufferedDualInputHatch) {

				DualInvBuffer buff = ((BufferedDualInputHatch) master).classifyForce();
				if (buff != null) {
					buff.onChange();
				}
				((BufferedDualInputHatch) master).justHadNewItems = true;
			}
			if (master.getBaseMetaTileEntity() instanceof gregtech.api.interfaces.tileentity.IHasInventory inv) {
				inv.markInventoryBeenModified();
			}
			return true;// hoo ray
		}

		return false;
	}
  
	@Override
	public boolean acceptsPlansCM() {
 
		return CMMode;
	}

	@Override
	public boolean enableCM() {

		return CMMode;
	}

	/*
	 * @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) {
	 * if(CMMode)return EMPTY_TK; return super.getTankInfo(from); } private
	 * FluidTankInfo[] EMPTY_TK=new FluidTankInfo[0]; private int[]
	 * EMPTY_INT=new int[0];
	 * 
	 * @Override public int[] getAccessibleSlotsFromSide(int ordinalSide) {
	 * if(CMMode)return EMPTY_INT; return
	 * super.getAccessibleSlotsFromSide(ordinalSide); }
	 */

	private Method m;
	private boolean fail;

	public void resetMulti() {
		if (fail) {
			return;
		}
		/*for (ProcessingLogic pl : processingLogics) {
			for (int i : detailmap.values()) {

				try {
			
					pl.removeInventoryRecipeCache(new PatternDualInv(i));
					
			
					
				} catch (Exception e) {
					fail = true;
				}

			}

		}*/
	}

	public static NBTTagCompound writeToNBTG(ItemStackG is, NBTTagCompound tag) {
		is.writeToNBT(tag);
		// tag.setInteger("ICount", is.stackSize);

		return tag;
	}

	public static ItemStackG loadItemStackFromNBTG(NBTTagCompound tag) {

		ItemStackG is = ItemStackG.loadItemStackFromNBT(tag);
		// is.stackSize = tag.getInteger("ICount");
		return is;
	}

	public static ItemStack[] flat(ItemStackG[] mStoredItemInternal2) {
		ItemStack[][] all=new ItemStack[mStoredItemInternal2.length][];
		int size=0;
		for(int i=0;i<all.length;i++){
			if(mStoredItemInternal2[i]!=null){
				all[i]=mStoredItemInternal2[i].flat();
				size=size+all[i].length;
			}
		}
		ItemStack[] ret=new ItemStack[size];
		int g=0;
		for (ItemStack[] all1 : all) {
		    if (all1 != null ) {
		    	
		    	if(all1.length==1){
		    		ret[g]=all1[0];
		    		g++;
		    	}else{
			    	System.arraycopy(all1, 0, ret, g, all1.length);
			        g += all1.length;
		        }
		    }
		}
		
		
		return ret;
		/*return Arrays.asList(mStoredItemInternal2).stream().filter(Objects::nonNull)
				.flatMap(s -> Arrays.stream(s.flat())).toArray(ItemStack[]::new);*/

	}

	public static FluidStack[] flat(FluidTankG[] mStoredItemInternal2) {

		FluidStack[][] all=new FluidStack[mStoredItemInternal2.length][];
		int size=0;
		for(int i=0;i<all.length;i++){
			if(mStoredItemInternal2[i]!=null){
			all[i]=mStoredItemInternal2[i].flat();
			size=size+all[i].length;}
		}
		FluidStack[] ret=new FluidStack[size];
		int g=0;
		for (FluidStack[] all1 : all) {
		    if (all1 != null ) {
		    	
		    	if(all1.length==1){
		    		ret[g]=all1[0];
		    		g++;
		    	}else{
			    	System.arraycopy(all1, 0, ret, g, all1.length);
			        g += all1.length;
		        }
		    }
		}
		
		
		return ret;
		/*return Arrays.asList(mStoredItemInternal2).stream().flatMap(s -> Arrays.stream(s.flat()))
				.toArray(FluidStack[]::new);*/

	}

		ItemSlot circuitSlotInBuffer2(int pos,int indexPhantom, MappingItemHandlerG inventoryHandlerPhantom) {
			

			ItemSlot is = new ItemSlot() {
				public Result onMousePressed(int mouseButton) {
					//if (getSlot().getHasStack())
					//	getSlot().putStack(null);
					return super.onMousePressed(mouseButton);
				};
				
			}
			
			.slot(new ModularSlot(inventoryHandlerPhantom, indexPhantom){
				public int getSlotStackLimit() {return 0;};
				public int getItemStackLimit(ItemStack stack) {return 0;};
			}
					).pos(0, pos*18);
	
			return is;

		

		
	
	}
		com.cleanroommc.modularui.widgets.CycleButtonWidget createPowerSwitchButton2(PanelSyncManager syncManager){
			return new  com.cleanroommc.modularui.widgets.CycleButtonWidget() {
				@Override
				public @NotNull Result onMousePressed(int mouseButton) {
					if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						
						syncManager.findPanelHandlerNullable("EX_Config").openPanel();;
						return Result.ACCEPT;
					}
					return super.onMousePressed(mouseButton);
				}
				
				
			}.stateCount(2)
					
					.value(  new IntSyncValue(()-> getBaseMetaTileEntity().isAllowedToWork()?1:0, s->{
						 if(s==1)
						getBaseMetaTileEntity().enableWorking();
						 else
						getBaseMetaTileEntity().disableWorking();
					}).allowC2S())
					
					.stateBackground(1, GTGuiTextures.BUTTON_STANDARD)
					.stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
				      .stateOverlay(1, GTGuiTextures.OVERLAY_BUTTON_POWER_SWITCH_ON)
                     .stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_POWER_SWITCH_OFF)
					.pos(getGUIWidth() - 18 - 3, 5//.tooltip(s->{s.add(tool);})
					
					).size(16, 16);
		}
		
		@Override
		public void populateUI(ModularPanel builder, PosGuiData data, PanelSyncManager syncManager,
				UISettings uiSettings) {
			super.populateUI(builder, data, syncManager, uiSettings);
			
			syncManager.syncedPanel("EX_Config", true, 
					(manager, handler) -> createWindowEX2(manager));
	
			
			
			builder.child(createPowerSwitchButton2(syncManager));
			
			
			syncManager.syncValue("setDirty", new SyncHandler() {
				@Override
				public void readOnServer(int id, PacketBuffer buf) {
				}

				@Override
				public void readOnClient(int id, PacketBuffer buf) {
				}

				@Override
				public void detectAndSendChanges(boolean init) {
					BufferedDualInputHatch.this.dirty = true;
					BufferedDualInputHatch.this.inv0.forEach(s->s.nonempty=true);
					markDirty();
				}
			});
			addBuffer(builder, data, syncManager, uiSettings);
		}
		public final ModularPanel createWindowEX2(@NotNull PanelSyncManager manager) {
			final int WIDTH = 18 * 6 + 6;
			final int HEIGHT = 18 * 4 + 6;	
			ModularPanel builder = new ModularPanel("EX_Config");
			builder.size(WIDTH, HEIGHT);
			
			/*builder.child(new com.cleanroommc.modularui.widgets.CycleButtonWidget()
					.stateCount(2)
					.value(accessorI(() -> updateEveryTick, (s) -> {
				updateEveryTick = s;
			}))
					
					//.setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
					//.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
					.stateBackground(1, GTGuiTextures.BUTTON_STANDARD_PRESSED)
					.stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
				      .stateOverlay(1, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
                     .stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
					
					
				
					.pos(3 + 18 * 0, 3 + 18 * 0).size(18, 18)
					.tooltipBuilder(s->s.addLine( StatCollector.translateToLocal("programmable_hatches.gt.forcecheck")))

			);*/
			exconfig.table.cellSet().stream().map(s->s.getValue().asMUI2(s.getRowKey(), s.getColumnKey()))
			.forEach(builder::child);
			
			
			
			return builder;
		}
		@SuppressWarnings({ "deprecation"})
		public void addBuffer(ModularPanel builder, PosGuiData data, PanelSyncManager syncManager,
		UISettings uiSettings){
			// buttons are 16x16 laid out in 3 columns; number of rows = ceil(bufferNum / 3)
			final int BTN = 16;
			final int COLS = 3;
			final int VISIBLE_ROWS = 2;
			int rows = (bufferNum + COLS - 1) / COLS; // ceil
			ScrollWidget<?> list = new ScrollWidget<>(new VerticalScrollData());
			// total scrollable content height = all rows; viewport shows VISIBLE_ROWS rows
			list.getScrollArea().getScrollY().setScrollSize(BTN * rows);
			list.size(BTN * COLS, BTN * VISIBLE_ROWS).pos(3, 3);
		for (int i = 0; i < bufferNum; i++) {
			int id=i;
			int xoffset=0; int yoffset=0;
			
			//String sg="slot_group_buffer_"+id;
			//syncManager.getSlotGroup(sg);
			
			final IPanelHandler panel = syncManager.syncedPanel("buffer_panel_sync_"+id, true, 
					(manager, handler) -> createBufferWindow2(manager,id));
	
			com.cleanroommc.modularui.widgets.ButtonWidget button=new com.cleanroommc.modularui.widgets.ButtonWidget<>()
			.onMousePressed(s->{
				panel.openPanel();
				return panel.isPanelOpen();
			})
			.background(GTGuiTextures.BUTTON_STANDARD,OVERLAY_BUTTON_PLUS_LARGE)
			.tooltipBuilder(s->{
				s.addLine(LangManager.translateToLocalFormatted("programmable_hatches.gt.buffer", "" + id));
			})	.size(BTN)
			.pos(xoffset + BTN * (id % COLS), yoffset + BTN * (id / COLS));
			
			
			
			list.addChild(button, i);
			
		    }
			builder.child(list);
			
			
		
			
			
			
			
			
			
			
			
			
			
			
			
			
		}
		public ModularPanel createBufferWindow2(PanelSyncManager syncManager,int ind) {
			final int WIDTH = 18 * 6 + 6;
			final int HEIGHT = 18 * 4 + 6;
			final int PARENT_WIDTH = getGUIWidth();
			final int PARENT_HEIGHT = getGUIHeight();
			ModularPanel builder = new ModularPanel("buffer_panel_"+ind);
			builder.size(WIDTH, HEIGHT);
			//builder.background(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
			//builder.setGuiTint(getGUIColorization());
			//builder.setDraggable(true);
			int fluidslot_pos_index;
			Supplier<com.cleanroommc.modularui.widgets.layout.Grid> genSlots;
			String sg="slot_group_buffer_"+ind;
			syncManager.registerSlotGroup(sg, 1);
			int x=Math.min(3, slotTierOverride(mTier))+1;
			
			final MappingItemHandlerG inventoryHandler = new MappingItemHandlerG(inv0.get(ind).mStoredItemInternal,
					0, x*x*page()).bind(inv0.get(ind)).id(1);
			switch (slotTierOverride(mTier)) {
			case 0:
				genSlots = () -> gridTemplate1by1X(
						index -> new ItemSlot().slot((ModularSlot(inventoryHandler, index)).slotGroup(sg))).pos(0, 0);
				fluidslot_pos_index = 0;
				break;
			case 1:
				genSlots = () -> gridTemplate2by2X(
						index -> new ItemSlot().slot((ModularSlot(inventoryHandler, index)).slotGroup(sg))).pos(0, 0);
				fluidslot_pos_index = 1;
				break;
			case 2:
				genSlots = () -> gridTemplate3by3X(
						index -> new ItemSlot().slot((ModularSlot(inventoryHandler, index)).slotGroup(sg))).pos(0, 0);
				fluidslot_pos_index = 2;
				break;
			default:
				genSlots = () -> gridTemplate4by4X(
						index -> new ItemSlot().slot((ModularSlot(inventoryHandler, index)).slotGroup(sg))).pos(0, 0);
				fluidslot_pos_index = 3;
			}
			
			// The item grid is 4 rows per page(), but this panel is a fixed 4 rows tall, so on the
			// MK.II (page() == 2, 32 slots) the lower half rendered outside the window with no way
			// to reach it. Scroll it, exactly as the main GUI already does for the same grid (see
			// DualInputHatch#populateUI) and as the fluid/circuit lists below already do here.
			ScrollWidget<?> listItem = new ScrollWidget<>(new VerticalScrollData()).size(18);
			listItem.getScrollArea().getScrollY().setScrollSize(18 * 4 * page());
			listItem.size(18 * (fluidslot_pos_index + 1), 18 * 4);
			listItem.child(genSlots.get());
			listItem.pos(3, 3);
			builder.child(listItem);
			ScrollWidget<?> list = new ScrollWidget<>(new VerticalScrollData()).size(18)/*.keepScrollBarInArea(true)*/;
			list.getScrollArea().getScrollY().setScrollSize(18 * inv0.get(ind).mStoredFluidInternal.length/fluidSlotsPerRow());
			list.size(18 * fluidSlotsPerRow(), 18 * Math.min(4, inv0.get(ind).mStoredFluidInternal.length/fluidSlotsPerRow()));
			list.pos(3+18*4-(fluidSlotsPerRow()-1)*18, 3);
			
			
			for (int i = 0; i < inv0.get(ind).mStoredFluidInternal.length; i++) {
				
				list.addChild(new FluidSlot()
						.pos(i % fluidSlotsPerRow() * 18, (i / fluidSlotsPerRow()) * 18)
						.syncHandler(new LimitedFluidTank(inv0.get(ind).mStoredFluidInternal[i]).bind(inv0.get(ind)))
						
						, i);
				
				
			}
			
			
			builder.child(list);
			
			final MappingItemHandlerG inventoryHandlerPhantom = new MappingItemHandlerG(inv0.get(ind).mStoredItemInternal, 0,
					inv0.get(ind).mStoredItemInternal.length).phantom();
			ScrollWidget<?> listPhantom = new ScrollWidget<>(new VerticalScrollData()).size(18)/*.keepScrollBarInArea(true)*/;
			listPhantom.getScrollArea().getScrollY().setScrollSize(18 * inv0.get(ind).v);
			
			
			for (int i = 0; i < inv0.get(ind).v; i++){
				int indexPhantom= inv0.get(ind).i + i;
				listPhantom.addChild(circuitSlotInBuffer2(i,indexPhantom,inventoryHandlerPhantom),i);
			
			}
			
			listPhantom.size(18 , 18 * 2);
			listPhantom.pos(3+18*5, 3);
			builder.child(listPhantom);
			
			
			
			builder.child(new ToggleButton().value(new BooleanSyncValue(
					()->inv0.get(ind).lock
					,
					s->{inv0.get(ind).lock=s;inv0.get(ind).clearRecipeIfNeeded();}
					).allowC2S())
                    .overlay(false, GTGuiTextures.OVERLAY_BUTTON_RECIPE_UNLOCKED)
                    .overlay(true, GTGuiTextures.OVERLAY_BUTTON_RECIPE_LOCKED)
                    .size(16).pos(3+18*5, 3+18*3))
			.tooltip(s->{
				
				s.addLine(StatCollector.translateToLocal("programmable_hatches.gt.lockbuffer"));
				
			})
			
			;
			BooleanSyncValue recipeLocked;
			syncManager.syncValue("sync_recipeLocked_"+ind, recipeLocked=new BooleanSyncValue(()->inv0.get(ind).recipeLocked).allowC2S());
			builder.child(IKey.dynamic(() -> recipeLocked.getBoolValue() ? "§4Lock" : "§aIdle").asWidget()
					.pos(3 + 18 * 5, 3 + 18 * 2).size(16));
			
			
			return builder;
		}




public static IntSyncValue accessorI(Supplier<Boolean> object, Consumer<Boolean> object2) {
	
		return new IntSyncValue(()->object.get()?1:0, s->object2.accept(s==1)).allowC2S();
	}

@Override
public int getGUIWidth() {
	
	return super.getGUIWidth();
}
@Override
public int getGUIHeight() {

	return super.getGUIHeight();
}

// No more slots after HV tier
public int getOffsetX() {
    return 0;
}

public int getOffsetY() {
    return 0;
}

}