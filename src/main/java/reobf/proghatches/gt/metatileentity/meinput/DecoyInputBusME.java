package reobf.proghatches.gt.metatileentity.meinput;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeMultimap;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cache.GridStorageCache;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.item.AEItemStack;
import appeng.util.item.MeaningfulItemIterator;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolderSuper;
import reobf.proghatches.gt.metatileentity.util.IMEHatchOverrided;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class DecoyInputBusME extends MTEHatchInputBusME implements IMEHatchOverrided, IDataCopyablePlaceHolderSuper {

	
	

	
	
	
    public DecoyInputBusME(int aID, /* boolean autoPullAvailable, */ String aName, String aNameRegional) {
        super(aID, /* autoPullAvailable */true, aName, aNameRegional);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
        desc = reobf.proghatches.main.Config.get("DIBME", ImmutableMap.of());
    }

    String[] desc;

    @Override
    public String[] getDescription() {

        return desc;
    }

    public DecoyInputBusME(String aName, /* boolean autoPullAvailable, */ int aTier, String[] aDescription,
        ITexture[][][] aTextures) {
        super(aName, true, aTier, aDescription, aTextures);

    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new DecoyInputBusME(mName, mTier, mDescriptionArray, mTextures);
    }

    private static Field f;

    static {
        try {
            f = GridStorageCache.class.getDeclaredField("activeCellProviders");
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError(e);

        }
        f.setAccessible(true);
    }

    private static Set<ICellProvider> get(GridStorageCache thiz) {

        try {
            return (Set<ICellProvider>) f.get(thiz);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
    @Override
    public void overridedBehoviour(int minPull) {
        AENetworkProxy proxy = getProxy();

        try {
            GridStorageCache st = (GridStorageCache) proxy.getStorage();
            ArrayList<Object> order = new ArrayList();
            TreeMultimap<Integer, IMEInventoryHandler<IAEItemStack>> orderMap = TreeMultimap
                .create((a, b) -> -a + b, (a, b) -> {
                    if (a == b) return 0;
                    int diff = a.hashCode() - b.hashCode();
                    if (diff != 0) return diff;
                    if (order.indexOf(a) == -1) order.add(a);
                    if (order.indexOf(b) == -1) order.add(b);
                    return order.indexOf(a) - order.indexOf(b);
                });
            for (final ICellProvider cc : get(st)) {

                List<IMEInventoryHandler> list = cc.getCellArray(StorageChannel.ITEMS);

                for (IMEInventoryHandler l : list) {
                    // if(!(l instanceof MEInventoryHandler))continue;
                    /*
                     * if(l instanceof MEInventoryHandler){
                     * MEInventoryHandler hh=(MEInventoryHandler) l;
                     * if((hh.getInternal() instanceof MEPassThrough)) continue;
                     * }
                     */
                	if(!(l instanceof CraftingGridCache))
                    orderMap.put(cc.getPriority(), l);
                }

            }
            
            boolean keepFirstEmpty = false;
            if (reserveFirst && orderMap.isEmpty() == false) {
                if (orderMap.get(
                    orderMap.keySet()
                        .first())
                    .stream()
                    .filter(
                        s -> s
                            .getAvailableItems(
                                StorageChannel.ITEMS.createList(),
                                appeng.util.IterationCounter.fetchNewId())
                            .getFirstItem() != null)
                    .findAny()
                    .isPresent() == false) {
                    keepFirstEmpty = true;
                }
            }

            ArrayList<IAEItemStack> added = new ArrayList<>();
            IItemList<IAEItemStack> all = new IItemList<IAEItemStack>() {

                private final NavigableMap<IAEItemStack, IAEItemStack> records = new ConcurrentSkipListMap<>();

                @Override
                public void add(final IAEItemStack option) {
                    if (option == null) {
                        return;
                    }
                    if (option.getItem() instanceof ItemFluidDrop) {
                        return;
                    }
                    final IAEItemStack st = this.records.get(option);

                    if (st != null) {
                        //if (st.getStackSize() == 0) {
                            //added.add(option.copy());
                            added.stream().filter(s->s.equals(option)).findAny().ifPresent(s->{
                            	s.add(option.copy());
                            	
                            });;
                        //}
                        st.add(option);
                        return;
                    }
                    added.add(option.copy());
                    final IAEItemStack opt = option.copy();

                    this.putItemRecord(opt);
                }

                @Override
                public IAEItemStack findPrecise(final IAEItemStack itemStack) {
                    if (itemStack == null) {
                        return null;
                    }

                    return this.records.get(itemStack);
                }

                @Override
                public Collection<IAEItemStack> findFuzzy(final IAEItemStack filter, final FuzzyMode fuzzy) {
                    throw new Error();
                }

                @Override
                public boolean isEmpty() {
                    return !this.iterator()
                        .hasNext();
                }

                @Override
                public void addStorage(final IAEItemStack option) {
                    if (option == null) {
                        return;
                    }
                    if (option.getItem() instanceof ItemFluidDrop) {
                        return;
                    }
                    final IAEItemStack st = this.records.get(option);

                    if (st != null) {
                        /*if (st.getStackSize() == 0) {
                            added.add(option.copy());
                        }*/
                    	 added.stream().filter(s->s.equals(option)).findAny().ifPresent(s->{
                         	s.add(option.copy());
                         	
                         });;
                        st.incStackSize(option.getStackSize());
                        return;
                    }
                    added.add(option.copy());
                    final IAEItemStack opt = option.copy();

                    this.putItemRecord(opt);
                }

                /*
                 * public void clean() { Iterator<StackType> i = iterator();
                 * while (i.hasNext()) { StackType AEI = i.next(); if (
                 * !AEI.isMeaningful() ) i.remove(); } }
                 */

                @Override
                public void addCrafting(final IAEItemStack option) {
                    return;/*
                            * if (option == null) {
                            * return;
                            * }
                            * final IAEItemStack st = this.records.get(option);
                            * if (st != null) {
                            * st.setCraftable(true);
                            * return;
                            * }
                            * final IAEItemStack opt = option.copy();
                            * opt.setStackSize(0);
                            * opt.setCraftable(true);
                            * this.putItemRecord(opt);
                            */
                }

                @Override
                public void addRequestable(final IAEItemStack option) {
                    return;/*
                            * if (option == null) {
                            * return;
                            * }
                            * final IAEItemStack st = this.records.get(option);
                            * if (st != null) {
                            * st.setCountRequestable(st.getCountRequestable() + option.getCountRequestable());
                            * st.setCountRequestableCrafts(
                            * st.getCountRequestableCrafts() + option.getCountRequestableCrafts());
                            * return;
                            * }
                            * final IAEItemStack opt = option.copy();
                            * opt.setStackSize(0);
                            * opt.setCraftable(false);
                            * opt.setCountRequestable(option.getCountRequestable());
                            * opt.setCountRequestableCrafts(option.getCountRequestableCrafts());
                            * this.putItemRecord(opt);
                            */
                }

                @Override
                public IAEItemStack getFirstItem() {
                    for (final IAEItemStack stackType : this) {
                        return stackType;
                    }

                    return null;
                }

                @Override
                public int size() {
                    return this.records.size();
                }

                @Override
                public Iterator<IAEItemStack> iterator() {
                    return new MeaningfulItemIterator<>(
                        this.records.values()
                            .iterator());
                }

                @Override
                public void resetStatus() {
                    for (final IAEItemStack i : this) {
                        i.reset();
                    }
                }

                public void clear() {
                    this.records.clear();
                }

                private IAEItemStack putItemRecord(final IAEItemStack itemStack) {
                    return this.records.put(itemStack, itemStack);
                }
                private Collection<IAEItemStack> findFuzzyDamage(final AEItemStack filter, final FuzzyMode fuzzy,
                    final boolean ignoreMeta) {
                    throw new Error();
                }

				@Override
				public byte getStackType() {
					// TODO Auto-generated method stub
					return LIST_ITEM;
				}
            };

            for (Entry<Integer, IMEInventoryHandler<IAEItemStack>> ent : orderMap.entries()) {
                ent.getValue()
                    .getAvailableItems(all, appeng.util.IterationCounter.fetchNewId());
                if (added.size() > 16) {
                    break;
                }
            }
            int index = 0;
            if (keepFirstEmpty) {
            	MEHatchRefactor.setConfigItem(this, 0, null,null);
               // this.mInventory[0] = null;
                index++;

            }
            Iterator<IAEItemStack> iterator = added.iterator();
            while (iterator.hasNext() && index < 16) {
                IAEItemStack currItem = iterator.next();
                // if(all.findPrecise(currItem)!=null){continue;}
                if (currItem.getStackSize() >= minPull) {
                    ItemStack itemstack = GTUtility.copyAmount(1, currItem.getItemStack());
                    MEHatchRefactor.setConfigItem(this, index, itemstack,currItem.getItemStack());
                    //this.mInventory[index] = itemstack;
                    index++;
                }
            }
            for (int i = index; i < 16; i++) {
            	MEHatchRefactor.setConfigItem(this, i, null,null);
            	//mInventory[i] = null;
            }

        } catch (final GridAccessException ignored) {}

        /*
         * try { GridStorageCache st = (GridStorageCache) proxy.getStorage();
         * IItemList<IAEItemStack> all=StorageChannel.ITEMS.createList();
         * for (final ICellProvider cc : get(st)) { for (final
         * IMEInventoryHandler<IAEItemStack> h :
         * cc.getCellArray(StorageChannel.ITEMS)) { if(!(h instanceof
         * IMEInventoryHandler))continue; if(h instanceof MEInventoryHandler){
         * MEInventoryHandler hh=(MEInventoryHandler) h; if((hh.getInternal()
         * instanceof MEPassThrough)) continue; } h.getAvailableItems(all); } }
         * int index = 0; Iterator<IAEItemStack> it = all.iterator();
         * while (it.hasNext() && index < 16) { IAEItemStack currItem =
         * it.next(); if (currItem.getStackSize() >= minPull) { ItemStack
         * itemstack = GTUtility.copyAmount(1, currItem.getItemStack());
         * this.mInventory[index] = itemstack; index++; } } if(index==16)return;
         * if(reserveFirst&&index==0){ mInventory[0] = null; index++;
         * } IMEMonitor<IAEItemStack> sg = proxy.getStorage()
         * .getItemInventory(); Iterator<IAEItemStack> iterator =
         * sg.getStorageList() .iterator();
         * while (iterator.hasNext() && index < 16) { IAEItemStack currItem =
         * iterator.next(); if(all.findPrecise(currItem)!=null){continue;} if
         * (currItem.getStackSize() >= minPull) { ItemStack itemstack =
         * GTUtility.copyAmount(1, currItem.getItemStack());
         * this.mInventory[index] = itemstack; index++; } } for (int i = index;
         * i < 16; i++) { mInventory[i] = null; }
         * } catch (final GridAccessException ignored) {}
         */

    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {

        builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
            if (clickData.mouseButton == 0) {
                if (!widget.isClient()) {

                    for (int index = 0; index < 16; index++) {
                    	MEHatchRefactor.updateInformationSlot(this, index);
                    	//updateInformationSlot(index, mInventory[index]);
                    }
                }
            }
        })
            .setBackground(() -> {
                {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD };
                }
            })
            .addTooltips(
                Arrays.asList(
                    StatCollector.translateToLocal("proghatches.restricted.refresh.0"),
                    StatCollector.translateToLocal("proghatches.restricted.refresh.1")))
            .setSize(16, 16)
            .setPos(80, 10 + 18));

        // .widget(new FakeSyncWidget.BooleanSyncer(() -> autoPullFluidList,
        // this::setAutoPullFluidList));

        super.addUIWidgets(builder, buildContext);

    }

    boolean reserveFirst;
    // private BaseActionSource requestSource;

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack is) {
        if (aPlayer.isSneaking()) {
            reserveFirst = !reserveFirst;
            aPlayer.addChatMessage(
                new ChatComponentTranslation(
                    "proghatches.decoy.reservefirst." + (reserveFirst ? "enabled" : "disabled")));

            return;
        }
        super.onScrewdriverRightClick(side, aPlayer, aX, aY, aZ, is);
    }

    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        // TODO Auto-generated method stub
        return super.endRecipeProcessing(controller);
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {

        /*
         * try {
         * System.out.println(overridedExtract(
         * ((IStorageGrid) this.getProxy().getNode().getGrid().getCache(IStorageGrid.class)).getItemInventory(),
         * AEItemStack.create(new ItemStack(net.minecraft.init.Items.apple,10)),Actionable.MODULATE,
         * new MachineSource((IActionHost)this.getBaseMetaTileEntity())
         * ));
         * } catch (Exception e) {
         * e.printStackTrace();
         * }
         */

        return super.onRightclick(aBaseMetaTileEntity, aPlayer);
    }

    /**
     * @param thiz
     * @param request
     * @param mode
     * @param src
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IAEStack overridedExtract(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src) {

        if (mode == Actionable.SIMULATE) return thiz.extractItems(request, mode, src);

        long requested = request.getStackSize();
        long num = request.getStackSize();
        try {
            AENetworkProxy proxy = getProxy();
            GridStorageCache st;
            ArrayList<IMEInventoryHandler> ordered = new ArrayList();

            st = (GridStorageCache) proxy.getStorage();
            for (final ICellProvider cc : get(st)) {
                ordered.addAll(cc.getCellArray(StorageChannel.ITEMS));
            }
            ordered.sort((IMEInventoryHandler a, IMEInventoryHandler b) -> a.getPriority() - b.getPriority());

            boolean somethingChanged = true;
            done: while (true) {
                if (somethingChanged) {
                    somethingChanged = false;
                } else {
                    break done;
                }
                if (num <= 0) {
                    break done;
                }
                ArrayList<IMEInventoryHandler> most = new ArrayList<>();
                long most_ = -1;
                long sec_ = 0;
                for (IMEInventoryHandler o : ordered) {
                    IAEStack ex = o.extractItems(
                        request.copy()
                            .setStackSize(Long.MAX_VALUE),
                        Actionable.SIMULATE,
                        src);
                    if (ex != null) {
                        if (most_ == -1) {
                            most_ = ex.getStackSize();
                            most.add(o);
                        } else if (most_ == ex.getStackSize()) {
                            most.add(o);
                        } else if (most_ < ex.getStackSize()) {
                            sec_ = most_;
                            most = new ArrayList<>();
                            most.add(o);
                            most_ = ex.getStackSize();
                        } else if (sec_ == 0) sec_ = ex.getStackSize();
                    }
                }
                if (most_ == -1) {

                    break done;
                }

                most.sort((a, b) -> a.getPriority() - b.getPriority());
                {
                    long eachmax = most_ - sec_;
                    int nums = most.size();

                    if (num >= eachmax * nums) {

                        for (IMEInventoryHandler s : most) {
                            IAEStack ret = s.extractItems(
                                request.copy()
                                    .setStackSize(eachmax),
                                Actionable.MODULATE,
                                src);
                            if (ret != null) {
                                somethingChanged = true;
                                num -= ret.getStackSize();
                            }
                        }
                    } else if (num > nums) {
                        for (IMEInventoryHandler s : most) {
                            IAEStack ret = s.extractItems(
                                request.copy()
                                    .setStackSize(num / nums),
                                Actionable.MODULATE,
                                src);
                            if (ret != null) {
                                somethingChanged = true;
                                num -= ret.getStackSize();
                            }
                        }
                    } else {
                        for (IMEInventoryHandler s : most) {
                            IAEStack ret = s.extractItems(
                                request.copy()
                                    .setStackSize(1),
                                Actionable.MODULATE,
                                src);
                            if (ret != null) {
                                num -= ret.getStackSize();
                                if (num <= 0) {
                                    somethingChanged = true;
                                    break done;
                                }
                            }
                        }

                    }

                }

            }

            if (num != 0) try {
                MyMod.LOG.fatal("[Decoy Bus]:Fail to extract!");
                MyMod.LOG.fatal("Expected:" + request.toString());
                MyMod.LOG.fatal("Left:" + num);
                MyMod.LOG.fatal("Auto-pull");
                for (ItemStack item : mInventory) {
                    MyMod.LOG.fatal(Objects.toString(item));
                }
                MyMod.LOG.fatal("shadowInventory");
                try {
                    Field f = MTEHatchInputBusME.class.getDeclaredField("shadowInventory");
                    f.setAccessible(true);
                    ItemStack[] get = (ItemStack[]) f.get(this);
                    for (ItemStack item : get) {
                        MyMod.LOG.fatal(Objects.toString(item));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MyMod.LOG.fatal("savedStackSizes");
                try {
                    Field f = MTEHatchInputBusME.class.getDeclaredField("savedStackSizes");
                    f.setAccessible(true);
                    int[] get = (int[]) f.get(this);
                    for (int item : get) {
                        MyMod.LOG.fatal(Objects.toString(item));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MyMod.LOG.fatal("But not found from:");
                for (IMEInventoryHandler xo : ordered) {
                    MyMod.LOG.fatal(xo + ":");
                    xo.getAvailableItems(StorageChannel.ITEMS.createList(), appeng.util.IterationCounter.fetchNewId())
                        .forEach((s) -> { MyMod.LOG.fatal(s); });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (GridAccessException e) {
            MyMod.LOG.fatal("[Decoy Bus]:GridAccessException");
            e.printStackTrace();

        }

        if (requested == num) return null;
        return request.copy()
            .setStackSize(requested - num);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setBoolean("reserveFirst", reserveFirst);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        reserveFirst = aNBT.getBoolean("reserveFirst");
        super.loadNBTData(aNBT);
    }

    @Override
    public boolean impl_pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt.hasKey("reserveFirst")) reserveFirst = nbt.getBoolean("reserveFirst");
        return true;
    }

    @Override
    public NBTTagCompound impl_getCopiedData(EntityPlayer player, NBTTagCompound tag) {
        tag.setBoolean("reserveFirst", reserveFirst);
        return tag;
    }

    /*
     * @Override
     * public NBTTagCompound getCopiedData(EntityPlayer player) {
     * NBTTagCompound ret=super_getCopiedData(player,()->MethodHandles.lookup());
     * ret.setBoolean("reserveFirst", reserveFirst);
     * return ret;
     * }
     * @Override
     * public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
     * boolean suc=super_pasteCopiedData(player, nbt,()->MethodHandles.lookup());
     * if(suc){
     * if(nbt.hasKey("reserveFirst"))reserveFirst=nbt.getBoolean("reserveFirst");
     * }
     * return suc;
     * }
     * @Override
     * public String getCopiedDataIdentifier(EntityPlayer player) {
     * return super_getCopiedDataIdentifier(player,()->MethodHandles.lookup());
     * }
     */
    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        return IDataCopyablePlaceHolderSuper.super.getCopiedData(player);
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        return IDataCopyablePlaceHolderSuper.super.pasteCopiedData(player, nbt);
    }

    @Override
    public String getCopiedDataIdentifier(EntityPlayer player) {
        return IDataCopyablePlaceHolderSuper.super.getCopiedDataIdentifier(player);
    }

    @Override
    public NBTTagCompound super_getCopiedData(EntityPlayer player) {

        return super.getCopiedData(player);
    }

    @Override
    public String super_getCopiedDataIdentifier(EntityPlayer player) {

        return super.getCopiedDataIdentifier(player);
    }

    @Override
    public boolean super_pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {

        return super.pasteCopiedData(player, nbt);
    }
    
    
@SuppressWarnings("unchecked")
@Override
public IAEStack qureyStorage(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src) {
	
	return  thiz.extractItems(request, mode, src);
}

public ItemStack getFirstValidStack(boolean slotsMustMatch) {
	

		return super.getFirstValidStack(false);
	
}



@Override
public void startRecipeProcessing() {
	if(autoPullItemList)
	refreshItemList();
	super.startRecipeProcessing();
}

@Override
public void setConfigFluid(MTEHatchInputME thiz, int index, FluidStack fs, FluidStack o) {
	
	
}

public  void setConfigItem(MTEHatchInputBusME thiz, int index, ItemStack bruh,
		ItemStack itemStack2) {
	try {
			thiz.setSlotConfig(index, bruh);
		
			if(itemStack2!=null){
				Slot ww= this.slots[index];
				ww.extracted=itemStack2;
				ww.extractedAmount=itemStack2.stackSize;
		
			}} catch (Exception e) {
			throw new AssertionError(e);
		}
	
}

@Override
public int minAutoPull() {
	
	return minAutoPullStackSize;
}



}
