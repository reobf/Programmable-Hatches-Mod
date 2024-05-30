package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
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

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.ToolDictNames;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.util.GT_TooltipDataCache.TooltipData;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;
import gregtech.common.tileentities.machines.IDualInputInventory;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

public class BufferedDualInputHatch extends DualInputHatch implements IRecipeProcessingAwareDualHatch {
	public Deque<Long> scheduled=new LinkedList<>();//no randomaccess, LinkedList will work fine
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
					final List<ItemStack> tCircuits = BufferedDualInputHatch.this.getConfigurationCircuits();
					final int index = GT_Utility.findMatchingStackInList(tCircuits, cursorStack);
					if (index < 0) {
						int curIndex = GT_Utility.findMatchingStackInList(tCircuits, inventory.getStackInSlot(slot))
								+ 1;
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
				inventory.setStackInSlot(slot, GT_Utility.copyAmount(0, newCircuit));

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
				.setBackground(getGUITextureSet().getItemSlot(), GT_UITextures.OVERLAY_SLOT_INT_CIRCUIT);
	}

	public NBTTagCompound writeToNBT(ItemStack is, NBTTagCompound tag) {
		is.writeToNBT(tag);
		tag.setInteger("ICount", is.stackSize);

		return tag;
	}

	/**
	 * @param tag
	 * @return
	 */
	public ItemStack loadItemStackFromNBT(NBTTagCompound tag) {

		ItemStack is = ItemStack.loadItemStackFromNBT(tag);
		is.stackSize = tag.getInteger("ICount");
		return is;
	}

	public int fluidLimit() {

		return (int) ((int) (64000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1)));
	}

	public int itemLimit() {

		return (int) (64 * Math.pow(2, Math.max(mTier - 3, 0)));
	}

	private static int fluidLimit(int mTier,boolean mMultiFluid) {

		return (int) ((int) (64000 * Math.pow(2, mTier) / (mMultiFluid ? 4 : 1)));
	}

	private static int itemLimit(int mTier) {

		return (int) (64 * Math.pow(2, Math.max(mTier - 3, 0)));
	}
	public BufferedDualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid,
			int bufferNum, String... optional) {
		this(id, name, nameRegional, tier, getSlots(tier) + 1, mMultiFluid, bufferNum, optional);

	}

	public BufferedDualInputHatch(int id, String name, String nameRegional, int tier, int slot, boolean mMultiFluid,
			int bufferNum, String... optional) {
		super(id, name, nameRegional, tier, slot, mMultiFluid,

				(optional.length > 0 ? optional
						: reobf.proghatches.main.Config
								.get("BDH",
										ImmutableMap
												.of("bufferNum", bufferNum, "cap",
														format.format(fluidLimit(tier,mMultiFluid )),
														"mMultiFluid", mMultiFluid, "slots",
														itemLimit(tier), "stacksize",
														(int) (64 * Math.pow(2, Math.max(tier - 3, 0)))))

				))/* ) */;
		this.bufferNum = bufferNum;
		initBackend();

	}

	public void initTierBasedField() {

		if (mMultiFluid) {
			mStoredFluid = new ListeningFluidTank[] {

					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this),
					new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this)

			};
		} else {

			mStoredFluid = new ListeningFluidTank[] { new ListeningFluidTank((int) (4000 * Math.pow(2, mTier)), this) };

		}

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
	public ArrayList<DualInvBuffer> inv0 = new ArrayList<DualInvBuffer>();

	public class DualInvBuffer implements IDualInputInventory {
		public long tickFirstClassify=-1;
		public FluidTank[] mStoredFluidInternal;
		public ItemStack[] mStoredItemInternal;
		public FluidTank[] mStoredFluidInternalSingle;
		public ItemStack[] mStoredItemInternalSingle;
		public boolean recipeLocked;
		public int i;
		public int f;
		public boolean lock;

		// public boolean lock;
		public boolean full() {

			for (int index=0;index<mStoredItemInternalSingle.length;index++) {
				ItemStack i =mStoredItemInternal[index]; 
				ItemStack si =mStoredItemInternalSingle[index]; 
				if(i!=null){
				if (si!=null&&Integer.MAX_VALUE - i.stackSize <si.stackSize) {
					return true;//over flow! count as full
				}
				
				if (i.stackSize >= itemLimit()) {
					return true;
				}
				}
			}
			
			
			for (int index=0;index<mStoredFluidInternalSingle.length;index++) {
				FluidTank i =mStoredFluidInternal[index]; 
				FluidTank si =mStoredFluidInternalSingle[index]; 
				if (si!=null&&Integer.MAX_VALUE - i.getFluidAmount() <si.getFluidAmount()) {
					return true;//over flow! count as full
				}
				if (i.getFluidAmount() >= fluidLimit()) {
					return true;
				}
			
			}
			return false;

		}

		public void updateSlots() {
			for (int i = 0; i < this.i; i++)
				if (mStoredItemInternal[i] != null && mStoredItemInternal[i].stackSize <= 0) {
					mStoredItemInternal[i] = null;
				}
			for (int i = 0; i < this.f; i++)
				if (Optional.ofNullable(mStoredFluidInternal[i].getFluid()).filter(s -> s.amount == 0).isPresent()) {
					mStoredFluidInternal[i].setFluid(null);
				}

		}

		public NBTTagCompound toTag() {

			NBTTagCompound tag = new NBTTagCompound();

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
					tag.setTag("mStoredItemInternal" + i, writeToNBT(mStoredItemInternal[i], new NBTTagCompound()));
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
			return tag;
		}

		public void fromTag(NBTTagCompound tag) {

			if (mStoredFluidInternal != null) {
				for (int i = 0; i < mStoredFluidInternal.length; i++) {
					if (tag.hasKey("mStoredFluidInternal" + i)) {
						mStoredFluidInternal[i].readFromNBT(tag.getCompoundTag("mStoredFluidInternal" + i));
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
						mStoredItemInternal[i] = loadItemStackFromNBT(tag.getCompoundTag("mStoredItemInternal" + i));
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
			
			if(tag.getInteger("i")>0)i = tag.getInteger("i");
			if(tag.getInteger("f")>0)f = tag.getInteger("f");
			recipeLocked = tag.getBoolean("recipeLocked");
			lock = tag.getBoolean("lock");
		}

		int v = 4;

		public void init(int item, int fluid) {
			i = item;
			f = fluid;
			mStoredFluidInternal = initFluidTack(new FluidTank[fluid]);
			mStoredFluidInternalSingle = initFluidTack(new FluidTank[fluid]);
			mStoredItemInternal = new ItemStack[item + v];
			mStoredItemInternalSingle = new ItemStack[item];
		}

		private FluidTank[] initFluidTack(FluidTank[] t) {
			for (int i = 0; i < t.length; i++) {
				t[i] = new FluidTank(Integer.MAX_VALUE);
			}
			return t;
		}
		public boolean isAccessibleForMulti() {
			
			return !isEmpty()&&
				tickFirstClassify+5<currentTick();
			//wait for possible future input, to take better adventage of parallels
		}
		public long currentTick(){return ((CoverableTileEntity)getBaseMetaTileEntity()).mTickTimer;}
		public boolean isEmpty() {

			for (FluidTank f : mStoredFluidInternal) {
				if (f.getFluidAmount() > 0) {
					return false;
				}
			}
			for (ItemStack i : mStoredItemInternal) {

				if (i != null && i.stackSize > 0) {
					return false;
				}
			}
			return true;
		}

		public boolean clearRecipeIfNeeded() {
			if (lock) {
				return !recipeLocked;
			}
			if (isEmpty()) {
				for (FluidTank ft : mStoredFluidInternalSingle) {
					ft.setFluid(null);
				}
				for (int ii = 0; ii < i; ii++) {
					mStoredItemInternalSingle[ii] = null;

				}

				recipeLocked = false;
				return true;
			}
			return false;
		}

		private boolean fluidEquals(FluidTank a, FluidTank b) {
			// if(a==b)return false;
			// if(a==null||b==null)return false;
			if (a.getFluidAmount() != b.getFluidAmount())
				return false;
			if (a.getFluid() == null && a.getFluid() == null)
				return true;
			if (a.getFluid() != null && (!a.getFluid().equals(b.getFluid())))
				return false;

			return true;
		}

		/**
		 * classify() with less check, for better performance
		 */
		public void firstClassify(ListeningFluidTank[] fin, ItemStack[] iin) {
			tickFirstClassify=currentTick();
			for (int ix = 0; ix < f; ix++) {
				mStoredFluidInternal[ix]
						.setFluid(Optional.ofNullable(fin[ix].getFluid()).map(FluidStack::copy).orElse(null));
				fin[ix].setFluidDirect(null);

			}
			for (int ix = 0; ix < i; ix++) {
				mStoredItemInternal[ix] = Optional.ofNullable(iin[ix]).map(ItemStack::copy).orElse(null);
				iin[ix] = null;
			}
			Long tick=tickFirstClassify+5+1;
			if(!tick.equals(scheduled.peekFirst()))
			scheduled.push(tick);
			//justHadNewItems = true;
			onClassify();
			programLocal();
		}

		private void programLocal() {
			if (!program)
				return;
			ArrayList<ItemStack> isa = new ArrayList<>();
			for (int i = 0; i < mStoredItemInternal.length; i++) {
				ItemStack is = mStoredItemInternal[i];
				if (is == null)
					continue;
				if (is.getItem() != MyMod.progcircuit)
					continue;
				mStoredItemInternal[i] = null;
				// inv0.mStoredItemInternal[inv0.mStoredItemInternal.length-1]=

				isa.add(GT_Utility.copyAmount(0, ItemProgrammingCircuit.getCircuit(is).orElse(null)));
			}

			int nums = Math.min(v, isa.size());
			if (nums == 0)
				return;

			for (int i = 0; i < v; i++) {
				if (i < nums) {
					mStoredItemInternal[this.i + i] = isa.get(i);
				} else {
					mStoredItemInternal[this.i + i] = null;
				}

			}

		}

		public void classify(ListeningFluidTank[] fin, ItemStack[] iin) {
			tickFirstClassify=-1;//make it instantly accessible
			boolean hasJob = false;
			for (int ix = 0; ix < f; ix++) {
				if (fin[ix].getFluidAmount() > 0) {
					hasJob = true;
				}
				if (fluidEquals(mStoredFluidInternalSingle[ix], fin[ix])) {
				} else {
					return;
				}
				;
			}
			for (int ix = 0; ix < i; ix++) {
				if (iin[ix] != null && iin[ix].stackSize > 0) {
					hasJob = true;
				}
				if (ItemStack.areItemStacksEqual(mStoredItemInternalSingle[ix], iin[ix])) {
				} else {
					return;
				}
				;
			}
			if (!hasJob) {
				return;
			}

			for (int ix = 0; ix < f; ix++) {
				mStoredFluidInternal[ix].fill(mStoredFluidInternalSingle[ix].getFluid(), true);
				fin[ix].setFluidDirect(null);

			}
			for (int ix = 0; ix < i; ix++) {
				if (mStoredItemInternalSingle[ix] != null)
					if (mStoredItemInternal[ix] == null)
						mStoredItemInternal[ix] = mStoredItemInternalSingle[ix].copy();
					else
						mStoredItemInternal[ix].stackSize += mStoredItemInternalSingle[ix].stackSize;
				iin[ix] = null;
			}
			justHadNewItems = true;
			onClassify();
			if (program)
				programLocal();

		}

		public boolean recordRecipeOrClassify(ListeningFluidTank[] fin, ItemStack[] iin) {
			boolean readyToRecord = clearRecipeIfNeeded();
			// clearRecipeIfNeeded();
			if (recipeLocked == false && readyToRecord == true) {
				boolean actuallyFound = false;
				for (int ix = 0; ix < f; ix++) {
					if (fin[ix].getFluidAmount() > 0) {
						actuallyFound = true;
						mStoredFluidInternalSingle[ix].setFluid(fin[ix].getFluid());
					}
				}
				for (int ix = 0; ix < i; ix++) {
					if (iin[ix] != null) {
						actuallyFound = true;
						mStoredItemInternalSingle[ix] = iin[ix].copy();
					}
				}
				recipeLocked = actuallyFound;
				if (actuallyFound)
					firstClassify(fin, iin);
				return actuallyFound;
			}
			return false;
		}

		@Override
		public ItemStack[] getItemInputs() {
			ItemStack[] condensed = filterStack.apply(mStoredItemInternal);
			ItemStack additional = getStackInSlot(getCircuitSlot());
			if (additional == null)
				return condensed;

			int before_size = condensed.length;
			ItemStack[] bruh = new ItemStack[before_size + 1];
			bruh[before_size] = additional;
			System.arraycopy(condensed, 0, bruh, 0, before_size);
			return bruh;
		}

		@Override
		public FluidStack[] getFluidInputs() {

			return asFluidStack.apply(mStoredFluidInternal);
		}

	}

	int bufferNum;

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new BufferedDualInputHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
	}

	public void initBackend() {
		for (int i = 0; i < bufferNum; i++)
			inv0.add(new DualInvBuffer());
		inv0.forEach(s -> s.init(this.mInventory.length - 1, this.mStoredFluid.length));

	}

	@SuppressWarnings("rawtypes")
	public static class CallerCheck {

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

	private boolean updateEveryTick;

	public boolean updateEveryTick() {
		return updateEveryTick;
	}

	// public boolean prevdirty;
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.onPostTick(aBaseMetaTileEntity, aTick);
		if (aBaseMetaTileEntity.getWorld().isRemote)
			return;
		Optional.ofNullable(scheduled.peekLast()).filter(s->s==aTick).ifPresent(s->{
			scheduled.removeLast();
			justHadNewItems=true;
			//inv0.forEach(st->System.out.println(st.isAccessibleForMulti()));
		});
		
		dirty = dirty || updateEveryTick();
		if (dirty) {
			updateSlots();
		}
		dirty = dirty || getBaseMetaTileEntity().hasInventoryBeenModified();
		// System.out.println(dirty);
		// dirty=dirty||(!highEfficiencyMode());
		boolean on = (this.getBaseMetaTileEntity().isAllowedToWork());
		for (DualInvBuffer inv0 : this.sortByEmpty()) {
			if (on && dirty) {
				if (inv0.full() == false) {
					if (!inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory)) {
						inv0.classify(this.mStoredFluid, mInventory);
					}
				}
			}

			inv0.clearRecipeIfNeeded();
		}
		// prevdirty=dirty;
		dirty = false;
	}

	@Override
	public ItemStack getStackInSlot(int aIndex) {
		// if(aIndex>=mInventory.length)return
		// inv0.mStoredItemInternal[aIndex-mInventory.length];
		return super.getStackInSlot(aIndex);
	}

	/**
	 * non-empty one fist, then use empty one
	 */
	public ArrayList<DualInvBuffer> sortByEmpty() {
		ArrayList<DualInvBuffer> non_empty = new ArrayList<>();
		FirstObjectHolder<DualInvBuffer> empty = new FirstObjectHolder<>();
		inv0.forEach(s -> {
			(s.isEmpty()
					&& (!s.recipeLocked/* non-locked is considered not 'empty' */) ? empty : non_empty).add(s);
		});

		empty.opt().ifPresent(non_empty::add);
		// only one empty is needed, because only one buffer at maximum will be
		// filled one time

		return non_empty;
	}

	public void classify() {
		if (isRemote())
			return;
		for (DualInvBuffer inv0 : this.sortByEmpty()) {
			if (inv0.full() == false)
				inv0.classify(this.mStoredFluid, mInventory);
		}

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

	public void add1by1Slot(ModularWindow.Builder builder, int index, IDrawable... background) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.get(index).mStoredItemInternal,
				1000, 1);
		if (background.length == 0) {
			background = new IDrawable[] { getGUITextureSet().getItemSlot() };
		}
		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 1).widgetCreator(get()).startFromSlot(1000)
				.endAtSlot(1000).background(background).build().setPos(3, 3));
	}

	public void add2by2Slots(ModularWindow.Builder builder, int index, IDrawable... background) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.get(index).mStoredItemInternal,
				1000, 4);
		if (background.length == 0) {
			background = new IDrawable[] { getGUITextureSet().getItemSlot() };
		}
		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 2).widgetCreator(get()).startFromSlot(1000)
				.endAtSlot(1003).background(background).build().setPos(3, 3));
	}

	public void add3by3Slots(ModularWindow.Builder builder, int index, IDrawable... background) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.get(index).mStoredItemInternal,
				1000, 9);
		if (background.length == 0) {
			background = new IDrawable[] { getGUITextureSet().getItemSlot() };
		}
		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 3).widgetCreator(get()).startFromSlot(1000)
				.endAtSlot(1008).background(background).build().setPos(3, 3));
	}

	public void add4by4Slots(ModularWindow.Builder builder, int index, IDrawable... background) {
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.get(index).mStoredItemInternal,
				1000, 16);
		if (background.length == 0) {
			background = new IDrawable[] { getGUITextureSet().getItemSlot() };
		}
		builder.widget(SlotGroup.ofItemHandler(inventoryHandler, 4).widgetCreator(get()).startFromSlot(1000)
				.endAtSlot(1015).background(background).build().setPos(3, 3)

		);
	}

	private Function<BaseSlot, SlotWidget> get() {

		return s -> new SlotWidget(s) {

			//ItemStack is;

			@Override
			public void detectAndSendChanges(boolean init) {

				// getContext().syncSlotContent(this.getMcSlot());
				super.detectAndSendChanges(init);

				/*
				 * ItemStack iss=this.getMcSlot().getStack();
				 * if(!ItemStack.areItemStacksEqual(is,iss))init=true;
				 * init=true;
				 * is=Optional.ofNullable(iss).map(ItemStack::copy).orElse(null)
				 * ; if (init || this.getMcSlot().isNeedsSyncing()) {
				 * getContext().syncSlotContent(this.getMcSlot()); if
				 * (this.getMcSlot().isNeedsSyncing()) { markForUpdate(); }
				 * this.getMcSlot().resetNeedsSyncing(); }
				 */
			}

		};

	}

	private Widget createButtonBuffer(int id,int xoffset,int yoffset) {
		// for(int i=0;i<bufferNum;i++)
		return new ButtonWidget().setOnClick((clickData, widget) -> {
			if (clickData.mouseButton == 0) {
				if (!widget.isClient())
					widget.getContext().openSyncedWindow(BUFFER_0 + id);
			}
		}).setPlayClickSound(true).setBackground(GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_PLUS_LARGE)
				.addTooltips(ImmutableList
						.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.buffer", "" + id)))
				.setSize(16, 16).setPos(xoffset + 16 * (id % 3), yoffset + 16 * (id / 3));

		/*
		 * return new ButtonWidget().setOnClick((clickData, widget) -> { if
		 * (clickData.mouseButton == 0) { widget.getContext()
		 * .openSyncedWindow(BUFFER_0); } }) .setPlayClickSound(true)
		 * .setBackground(GT_UITextures.BUTTON_STANDARD,
		 * GT_UITextures.OVERLAY_BUTTON_PLUS_LARGE)
		 * .addTooltips(ImmutableList.of("Place manual items")) .setSize(18, 18)
		 * .setPos(7 + offset*18, 62-18*2);
		 */

		/*
		 * return new CycleButtonWidget().setToggle(getter, setter)
		 * .setStaticTexture(picture)
		 * .setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE)
		 * .setTooltipShowUpDelay(TOOLTIP_DELAY) .setPos(7 + offset*18, 62-18*2)
		 * .setSize(18, 18) .setGTTooltip(tooltipDataSupplier);
		 */
	}

	static private final int BUFFER_0 = 1001;

	protected ModularWindow createWindow(final EntityPlayer player, int index) {
		DualInvBuffer inv0 = this.inv0.get(index);
		final int WIDTH = 18 * 6 + 6;
		final int HEIGHT = 18 * 4 + 6;
		final int PARENT_WIDTH = getGUIWidth();
		final int PARENT_HEIGHT = getGUIHeight();
		ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
		builder.setBackground(GT_UITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
		builder.setGuiTint(getGUIColorization());
		builder.setDraggable(true);
		// make sure the manual window is within the parent window
		// otherwise picking up manual items would toss them
		// See GuiContainer.java flag1

		builder.setPos((size, window) -> Alignment.Center.getAlignedPos(size, new Size(PARENT_WIDTH, PARENT_HEIGHT))
				.add(Alignment.TopRight.getAlignedPos(new Size(PARENT_WIDTH, PARENT_HEIGHT), new Size(WIDTH, HEIGHT))));
		switch (slotTierOverride(mTier)) {
		case 0:
			add1by1Slot(builder, index);
			break;
		case 1:
			add2by2Slots(builder, index);
			break;
		case 2:
			add3by3Slots(builder, index);
			break;
		default:
			add4by4Slots(builder, index);
			break;
		}

		Pos2d[] p = new Pos2d[] { new Pos2d(3 + 18 * 1, 7 - 4), new Pos2d(3 + 18 * 2, 7 - 4),
				new Pos2d(3 + 18 * 3, 7 - 4), new Pos2d(3 + 18 * 4, 7 - 4) };
		Pos2d position = p[Math.min(3, slotTierOverride(this.mTier))];

		Scrollable sc = new Scrollable().setVerticalScroll();

		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv0.mStoredItemInternal, 0,
				inv0.mStoredItemInternal.length).phantom();
		for (int i = 0; i < inv0.v; i++)

			sc.widget((i == 0 ? circuitSlot(inventoryHandler, inv0.i + i)
					: new SlotWidget(new BaseSlot(inventoryHandler, inv0.i + i) {

						public int getSlotStackLimit() {
							return 0;
						};

					}

					) {

						@Override
						public List<String> getExtraTooltip() {
							return Arrays
									.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"));
						}
					}.disableShiftInsert().setHandlePhantomActionClient(true).setGTTooltip(() -> new TooltipData(
							Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
									LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")),
							Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
									LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"))))).setPos(0,
											18 * i));

		builder.widget(sc.setSize(18, 18 * 2).setPos(3 + 18 * 5, 3));

		{
			Pos2d position0 = new Pos2d(0, 0);

			final Scrollable scrollable = new Scrollable().setVerticalScroll();
			for (int i = 0; i < inv0.mStoredFluidInternal.length; i++) {
				position0 = new Pos2d((i % fluidSlotsPerRow()) * 18, (i / fluidSlotsPerRow()) * 18);
				scrollable.widget(new FluidSlotWidget(new LimitedFluidTank(inv0.mStoredFluidInternal[i]))
						.setBackground(ModularUITextures.FLUID_SLOT).setPos(position0));

			}

			builder.widget(
					scrollable.setSize(18 * fluidSlotsPerRow(), 18 * Math.min(4, inv0.mStoredFluidInternal.length)

					).setPos(position));
		}

		/*
		 * for (int i = 0; i < inv0.mStoredFluidInternal.length; i++) {
		 * builder.widget( new FluidSlotWidget(new
		 * LimitedFluidTank(inv0.mStoredFluidInternal[i])).setBackground(
		 * ModularUITextures.FLUID_SLOT) .setPos(position)); position=new
		 * Pos2d(position.getX(),position.getY()).add(0, 18); }
		 */

		builder.widget(TextWidget.dynamicString(() -> inv0.recipeLocked ? "§4Lock" : "§aIdle").setSynced(true)
				.setPos(3 + 18 * 5, 3 + 18 * 2));

		builder.widget(new CycleButtonWidget().setToggle(() -> !inv0.lock, (s) -> {
			inv0.lock = !s;
		}).setStaticTexture(GT_UITextures.OVERLAY_BUTTON_RECIPE_LOCKED_DISABLED)
				.setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * 5, 3 + 18 * 3).setSize(18, 18)
				.setGTTooltip(() -> mTooltipCache.getData("programmable_hatches.gt.lockbuffer"))

		);
		/*
		 * builder.widget(new FakeSyncWidget.BooleanSyncer(()->
		 * inv0.recipeLocked, s->inv0.recipeLocked=s ));
		 */
		builder.widget(new FakeSyncWidget.StringSyncer(() -> inv0.toTag().toString(), s -> inv0.fromTag(cv(s))));

		return builder.build();
	}

	private NBTTagCompound cv(String s) {
		try {
			return (NBTTagCompound) JsonToNBT.func_150315_a(s);
		} catch (NBTException e) {
			return new NBTTagCompound();
		}
	}

	ButtonWidget createPowerSwitchButton(IWidgetBuilder<?> builder) {
		IGregTechTileEntity thiz = this.getBaseMetaTileEntity();
		Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {

			if (thiz.isAllowedToWork()) {
				thiz.disableWorking();
			} else {
				thiz.enableWorking();
				// BufferedDualInputHatch bff =(BufferedDualInputHatch)
				// (thiz).getMetaTileEntity();
				BufferedDualInputHatch.this.dirty = true;
			}
		}).setPlayClickSoundResource(() -> thiz.isAllowedToWork() ? SoundResource.GUI_BUTTON_UP.resourceLocation
				: SoundResource.GUI_BUTTON_DOWN.resourceLocation).setBackground(() -> {
					if (thiz.isAllowedToWork()) {
						return new IDrawable[] { GT_UITextures.BUTTON_STANDARD_PRESSED,
								GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_ON };
					} else {
						return new IDrawable[] { GT_UITextures.BUTTON_STANDARD,
								GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF };
					}
				}).attachSyncer(new FakeSyncWidget.BooleanSyncer(thiz::isAllowedToWork, val -> {
					if (val)
						thiz.enableWorking();
					else
						thiz.disableWorking();
				}), builder).addTooltip(LangManager.translateToLocal("GT5U.gui.button.power_switch"))
				.setTooltipShowUpDelay(TOOLTIP_DELAY).setPos(new Pos2d(getGUIWidth() - 18 - 3, 5)).setSize(16, 16);
		return (ButtonWidget) button;
	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		Scrollable sc = new Scrollable().setVerticalScroll();
		for (int i = 0; i < bufferNum; i++) {
			final int ii = i;
			buildContext.addSyncedWindow(BUFFER_0 + i, (s) -> createWindow(s, ii));
			sc.widget(createButtonBuffer(i,0,0));
		}
		builder.widget(sc.setSize(16*3,16*2).setPos(3,3));
		
		builder.widget(createPowerSwitchButton(builder));
		builder.widget(new SyncedWidget() {

			@SuppressWarnings("unchecked")
			public void detectAndSendChanges(boolean init) {
				// player operation is more complicated, always set to true when
				// GUI open
				BufferedDualInputHatch.this.dirty = true;

				// flush changes to client
				// sometimes vanilla detection will fail so sync it manually
				// System.out.println(last-getBaseMetaTileEntity().getTimer());
				if (last >= getBaseMetaTileEntity().getTimer())
					getWindow().getContext().getContainer().inventorySlots.forEach(s -> ((Slot) s).onSlotChanged());

			};

			@Override
			public void readOnClient(int id, PacketBuffer buf) throws IOException {
			}

			@Override
			public void readOnServer(int id, PacketBuffer buf) throws IOException {
			}
		});
		ProghatchesUtil.attachZeroSizedStackRemover(builder, buildContext);
		super.addUIWidgets(builder, buildContext);
		// builder.widget(widget);

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
		dirty = aNBT.getBoolean("dirty");
		for (int i = 0; i < bufferNum; i++) {
			final int ii = i;
			inv0.get(i).fromTag((NBTTagCompound) aNBT.getTag("BUFFER_" + ii));
		}
		justHadNewItems = aNBT.getBoolean("justHadNewItems");
		updateEveryTick = aNBT.getBoolean("updateEveryTick");
		super.loadNBTData(aNBT);
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		aNBT.setBoolean("dirty", dirty);
		for (int i = 0; i < bufferNum; i++)

			aNBT.setTag("BUFFER_" + i, inv0.get(i).toTag());

		aNBT.setBoolean("justHadNewItems", justHadNewItems);
		aNBT.setBoolean("updateEveryTick", updateEveryTick);
		super.saveNBTData(aNBT);
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
		 * GT_Utility .copyAmount(0,ItemProgrammingCircuit.getCircuit(is)
		 * .orElse(null)) ; }
		 */

	}

	public class LimitedFluidTank implements IFluidTank {

		IFluidTank inner;

		public LimitedFluidTank(IFluidTank i) {
			inner = i;
		}

		@Override
		public FluidStack getFluid() {

			return inner.getFluid();
		}

		@Override
		public int getFluidAmount() {

			return inner.getFluidAmount();
		}

		@Override
		public int getCapacity() {

			return fluidLimit();
		}

		@Override
		public FluidTankInfo getInfo() {

			return inner.getInfo();
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {

			return inner.fill(resource, doFill);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {

			return inner.drain(maxDrain, doDrain);
		}

	}

	boolean justHadNewItems;

	@Override
	public boolean justUpdated() {
		boolean ret = justHadNewItems;
		justHadNewItems = false;
		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Optional</* ? extends */IDualInputInventory> getFirstNonEmptyInventory() {
		return (Optional) inv0.stream().filter(DualInvBuffer::isAccessibleForMulti).findAny();
	}

	private Predicate<DualInvBuffer> not(Predicate<DualInvBuffer> s) {
		return s.negate();
	}

	@Override
	public Iterator<? extends IDualInputInventory> inventories() {

		return inv0.stream().filter(DualInvBuffer::isAccessibleForMulti).iterator();
	}

	@Override
	public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
			int z) {

		class IndexedObject<T> {
			private T holded;
			private int index;

			IndexedObject(int i, T obj) {
				this.holded = obj;
				this.index = i;
			}
		}
		class RecipeTracker {
			boolean broken;
			int times;
			boolean first = true;
			boolean onceCompared;

			public void track(@Nonnull ItemStack recipe, @Nullable ItemStack storage) {
				if (recipe.getItem() instanceof ItemProgrammingCircuit) {
					onceCompared = true;
					return;
				}
				if (recipe.getItem() != (storage == null ? null : storage.getItem())) {
					broken = true;
					onceCompared = true;
					return;
				}
				int a = recipe.stackSize;
				int b = Optional.ofNullable(storage).map(s -> s.stackSize).orElse(0);
				track(a, b, false);
			}

			public void track(@Nonnull FluidTank recipe, @Nonnull FluidTank storage) {
				if (recipe.getFluid().getFluid() != Optional.of(storage).map(FluidTank::getFluid)
						.map(FluidStack::getFluid).orElse(null)) {
					broken = true;
					onceCompared = true;
					return;
				}

				int a = recipe.getFluidAmount();
				int b = storage.getFluidAmount();
				track(a, b, false);
			}

			public void track(int a, int b, boolean ignoreEmpty) {
				int t = 0;
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
		tag.setInteger("inv_size", inv0.size());
		IntStream.range(0, inv0.size()).forEach(s -> {
			DualInvBuffer inv = inv0.get(s);
			NBTTagCompound sub = new NBTTagCompound();
			tag.setTag("No" + s, sub);
			sub.setBoolean("full", inv.full());
			sub.setBoolean("noClear", inv.lock);
			sub.setBoolean("locked", inv.recipeLocked);
			sub.setBoolean("empty", inv.isEmpty());
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

			sub.setInteger("possibleCopies", (rt.broken || (!rt.onceCompared && !inv.isEmpty())) ? -1 : rt.times);
		});

		super.getWailaNBTData(player, tile, tag, world, x, y, z);
	}

	@Override
	public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {

		super.getWailaBody(itemStack, currenttip, accessor, config);
		NBTTagCompound tag = accessor.getNBTData();
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
			int copies = sub.getInteger("possibleCopies");
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
			currenttip.add("#" + s + " " + info + " " + cpinfo);
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
						GT_ModHandler.damageOrDechargeItem(tCurrentItem, 1, 1000, aPlayer);
						GT_Utility.sendSoundToPlayers(tile.getWorld(), SoundResource.IC2_TOOLS_WRENCH, 1.0F, -1,
								tile.getXCoord(), tile.getYCoord(), tile.getZCoord());
						updateEveryTick = !updateEveryTick;

						GT_Utility.sendChatToPlayer(aPlayer, "updateEveryTick:" + updateEveryTick);
						/*
						 * GT_Utility .sendChatToPlayer(aPlayer,
						 * LangManager.translateToLocal(
						 * "programmable_hatches.gt.updateEveryTick") );
						 */
						aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.updateEveryTick"));

						markDirty();
						return true;
					}

				}
			}
		}

		return super.onRightclick(aBaseMetaTileEntity, aPlayer);
	}

	@Override
	public CheckRecipeResult endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
		dirty = true;
		return super.endRecipeProcessing(controller);
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
				final ItemStack tItem = inv.mStoredItemInternal[i];
				if ((tItem != null) && (tItem.stackSize > 0)) {
					final EntityItem tItemEntity = new EntityItem(aWorld, aX + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
							aY + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F, aZ + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
							new ItemStack(tItem.getItem(), tItem.stackSize, tItem.getItemDamage()));
					if (tItem.hasTagCompound()) {
						tItemEntity.getEntityItem().setTagCompound((NBTTagCompound) tItem.getTagCompound().copy());
					}
					tItemEntity.motionX = (XSTR_INSTANCE.nextGaussian() * 0.05D);
					tItemEntity.motionY = (XSTR_INSTANCE.nextGaussian() * 0.25D);
					tItemEntity.motionZ = (XSTR_INSTANCE.nextGaussian() * 0.05D);
					aWorld.spawnEntityInWorld(tItemEntity);
					tItem.stackSize = 0;
					inv.mStoredItemInternal[i] = null;
				}
			}
		super.onBlockDestroyed();
	}
}
