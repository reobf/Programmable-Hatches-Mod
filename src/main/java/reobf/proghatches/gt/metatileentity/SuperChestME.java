package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Unique;

import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.config.Actionable;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.client.texture.ExtraBlockTextures;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import appeng.util.prioitylist.PrecisePriorityList;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.util.BaseSlotPatched;
import reobf.proghatches.gt.metatileentity.util.IStoageCellUpdate;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.IIconTexture;
import reobf.proghatches.util.ProghatchesUtil;

public class SuperChestME extends MTEHatch
    implements ICellContainer, IGridProxyable, IPriorityHost, IStoageCellUpdate, IPowerChannelState {

    public SuperChestME(String aName, int aTier, int aInvSlotCount, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aInvSlotCount, aDescription, aTextures);

    }

    public SuperChestME(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            aInvSlotCount,
            reobf.proghatches.main.Config.get("SCME", ImmutableMap.of("items", commonSizeCompute(aTier))

            ),
            new ITexture[0]);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
    }

    int capOverride;

    @Override
    public int getInventoryStackLimit() {

        return cap();
    }

    public int cap() {
        if (capOverride >= 64) {

            return

            Math.max(64, Math.min(capOverride, commonSizeCompute(mTier)));
        }
        return commonSizeCompute(mTier);
    }

    protected static int commonSizeCompute(int tier) {
        switch (tier) {
            case 1:
                return 4000000;
            case 2:
                return 8000000;
            case 3:
                return 16000000;
            case 4:
                return 32000000;
            case 5:
                return 64000000;
            case 6:
                return 128000000;
            case 7:
                return 256000000;
            case 8:
                return 512000000;
            case 9:
                return 1024000000;
            case 10:
                return 2147483640;
            default:
                return 0;
        }
    }
    /*
     * @MENetworkEventSubscribe public void channel(final
     * MENetworkChannelsChanged c) { post(); }
     */

    static Field m;
    static {
        try {
            m = MEMonitorHandler.class.getDeclaredField("hasChanged");
        } catch (Exception e) {
            e.printStackTrace();
        }
        m.setAccessible(true);
    }

    public static void forceUpdate(MEMonitorHandler thiz) {

        try {
            m.setBoolean(thiz, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AEItemStack last;

    
    private void postChange(StorageChannel c,IAEItemStack is){
    	
    	
    	/*Iterator<Entry<IMEMonitorHandlerReceiver, Object>> ll = getListener.get();
    	
    	while(ll.hasNext()){
    		Entry<IMEMonitorHandlerReceiver, Object> get = ll.next();
    		System.out.println(get.getKey());
    		if(!get.getKey().isValid(get.getValue())){
    			ll.remove();
    			continue;
    		}
    		get.getKey().postChange(monitor, ImmutableList.of(is), new MachineSource(this));
    		
    	}
    	*/
    	
    	
    	try {
			this.getProxy()
			.getStorage()
			.postAlterationOfStoredItems(
			    StorageChannel.ITEMS,
			    ImmutableList.of(
			        is),
			    new MachineSource(this));
		} catch (GridAccessException e) {
		}
    }
    
    private void post() {

        try {

            if (last != null) {
			    if (mInventory[0] != null) {
			        if (last.equals(mInventory[0])) {
			            if (last.getStackSize() == mInventory[0].stackSize) {
			                return;
			            } else {

			            	postChange(
			                        StorageChannel.ITEMS,
			                        last.copy().setStackSize(mInventory[0].stackSize - last.getStackSize()));
			                        
			                last = AEItemStack.create(mInventory[0]);
			                return;
			            }
			        }

			    } ;

			    postChange(
			            StorageChannel.ITEMS,
			            last.copy().setStackSize( - last.getStackSize()));
			            
			}
			last = AEItemStack.create(mInventory[0]);
			if (last != null) {
				postChange(
			            StorageChannel.ITEMS,
			            last.copy().setStackSize(last.getStackSize()));
			            
			}
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }/*
      * @MENetworkEventSubscribe public void power(final
      * MENetworkPowerStatusChange c) { post(); }
      */

    @Override
    public IGridNode getActionableNode() {

        return getProxy().getNode();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {

        return getProxy().getNode();
    }

    @Override
    public void securityBreak() {

    }

    static interface SilentCloseable extends Closeable {

        @Override
        void close();
    }

    boolean freezeFlag;//

    @Override
    public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
        if (freezeFlag) return ImmutableList.of();
        if (channel == StorageChannel.ITEMS) return ImmutableList.of(handler);
        else return ImmutableList.of();

    }

    @Override
    public int getPriority() {

        return piority;
    }

    private ItemStack visualStack() {
        return new ItemStack(GregTechAPI.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID());
    }

    AENetworkProxy gridProxy;

    private void updateValidGridProxySides() {
        /*
         * if (disabled) {
         * getProxy().setValidSides(EnumSet.noneOf(ForgeDirection.class));
         * return; }
         */
        getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(getBaseMetaTileEntity().getFrontFacing())));

    }

    @Override
    public AENetworkProxy getProxy() {

        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", visualStack(), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    @Override
    public void saveChanges(IMEInventory cellInventory) {

        markDirty();
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new SuperChestME(mName, mTier, mInventory.length, mDescriptionArray, mTextures);
    }

    @Override
    public void blinkCell(int slot) {

        post();
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {

        return new ITexture[] { aBaseTexture, IIconTexture.genFuckingBridge(ExtraBlockTextures.MEChest.getIcon(), 0xD7BBEC),
        		IIconTexture.genFuckingBridge(ExtraBlockTextures.BlockMEChestItems_Light.getIcon(), 0xffffff)

        };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, IIconTexture.genFuckingBridge(ExtraBlockTextures.MEChest.getIcon(), 0xD7BBEC)
            /*
             * , new IIconTexture
             * (ExtraBlockTextures.BlockMEChestItems_Light.getIcon(),
             * 0xffffff)
             */

        };
    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity());
    }

    public Consumer<IItemList> updateFilter;
    ItemStack[] cachedFilter = new ItemStack[1];

    public void updateFilter(ItemStack fs) {
        cachedFilter[0] = fs;
        ItemList fl = new ItemList();
        fl.add(AEItemStack.create(fs));
        updateFilter.accept(fl);
        post();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    IMEInventoryHandler<AEItemStack> handler = new MEInventoryHandler(new UnlimitedWrapper(), StorageChannel.ITEMS) {

        public boolean getSticky() {
            return sticky && !suppressSticky;
        };

        public int getPriority() {
            return piority;
        };

        {
            updateFilter = s -> this.setPartitionList(new PrecisePriorityList(s));
        }
    };

    /*@SuppressWarnings({ "rawtypes", "unchecked" })
    MEMonitorHandler monitor = new MEMonitorHandler(handler){
    	{getListener=()->this.getListeners();}
    };
    Supplier<Iterator<Entry<IMEMonitorHandlerReceiver, Object>>> getListener;*/
    
    /*
     * IMEMonitor handler0= new MEMonitorHandler(handler );
     */
    boolean sticky;
    int piority;

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
        onColorChangeServer(aBaseMetaTileEntity.getColorization());
        post();
    }

    public class UnlimitedWrapper implements IMEInventory<IAEItemStack> {

        public UnlimitedWrapper() {

        }

        @Override
        public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src) {
            if (type != Actionable.SIMULATE) post();
            try {
                long l = input.getStackSize();
                long compl = 0;
                if (l > Integer.MAX_VALUE) {
                    compl = l - Integer.MAX_VALUE;
                }
                ItemStack in = input.getItemStack();
                ItemStack thiz = mInventory[0];
                if (thiz != null && !Platform.isSameItem(in, thiz)) return input;
                if (thiz == null) {
                    thiz = in.copy();
                    thiz.stackSize = 0;
                }
                int space = Math.max(0, cap() - thiz.stackSize);
                int transfer = Math.min(space, in.stackSize);
                if (type == Actionable.SIMULATE) {
                    in.stackSize -= transfer;
                    if (in.stackSize <= 0 && compl == 0) in = null;
                    AEItemStack ret = AEItemStack.create(in);
                    if (ret != null) ret.incStackSize(compl);
                    return ret;
                }
                if (type == Actionable.MODULATE) {
                    thiz.stackSize += transfer;
                    mInventory[0] = thiz;
                    in.stackSize -= transfer;
                    if (in.stackSize <= 0 && compl == 0) in = null;
                    AEItemStack ret = AEItemStack.create(in);
                    if (ret != null) ret.incStackSize(compl);
                    return ret;

                }

                return null;
            } finally {
                last = AEItemStack.create(mInventory[0]);
                if (voidOverflow
                    && (mInventory[0] != null && ItemStack.areItemStackTagsEqual(mInventory[0], input.getItemStack())
                        && mInventory[0].getItem() == input.getItem()
                        && mInventory[0].getItemDamage() == input.getItemDamage())) {
                    return null;
                }
            }
        }

        @Override
        public IAEItemStack extractItems(IAEItemStack input, Actionable type, BaseActionSource src) {
            try {

                if (type != Actionable.SIMULATE) post();

                ItemStack in = input.getItemStack();
                ItemStack thiz = mInventory[0];
                if (thiz != null && !Platform.isSameItem(in, thiz)) return null;
                if (thiz == null) {
                    return null;
                } // thiz=in.copy(); }
                int transfer = Math.min(in.stackSize, thiz.stackSize);
                if (transfer == 0) return null;
                if (type == Actionable.SIMULATE) {
                    in.stackSize = transfer;
                    return AEItemStack.create(in);

                }
                if (type == Actionable.MODULATE) {
                    thiz.stackSize -= transfer;
                    if (thiz.stackSize <= 0) thiz = null;
                    mInventory[0] = thiz;
                    in.stackSize = transfer;
                    return AEItemStack.create(in);
                }

                return null;
            } finally {

                last = AEItemStack.create(mInventory[0]);
            }
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
            if (mInventory[0] != null) out.addStorage(AEItemStack.create(mInventory[0]));
            return out;
        }

        @Override
        public StorageChannel getChannel() {

            return StorageChannel.ITEMS;
        }
    }

    @Override
    public boolean isValidSlot(int aIndex) {

        return true;
    }

    public int getMaxItemCount() {

        return super.getMaxItemCount();
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {

        return true;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {

        return true;
    }

    @Override
    public boolean isItemValidForSlot(int aIndex, ItemStack aStack) {
        if (aIndex == 0) return true;
        if (mInventory[0] == null || mInventory[0].stackSize == 0) {
            return true;
        }

        return Platform.isSameItem(mInventory[0], aStack);
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {

        return true;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {

        if (mInventory[0] != null && aStack != null && (!Platform.isSameItem(mInventory[0], aStack))) {
            return false;
        }
        return aIndex != 0;
    }

    protected void fillStacksIntoFirstSlots() {
        final int L = mInventory.length;
        HashMap<GTUtility.ItemId, Integer> slots = new HashMap<>(L);
        HashMap<GTUtility.ItemId, ItemStack> stacks = new HashMap<>(L);
        List<GTUtility.ItemId> order = new ArrayList<>(L);
        List<Integer> validSlots = new ArrayList<>(L);
        for (int i = 1; i < L; i++) {
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

    boolean autoUnlock;
    boolean suppressSticky;

    boolean wasActive;
    int rep;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {

        boolean active = this.getProxy()
            .isActive();
        if (!aBaseMetaTileEntity.getWorld().isRemote) {
            if (facingJustChanged) {
                facingJustChanged = false;
                try {
                    this.getProxy()
                        .getGrid()
                        .postEvent(new MENetworkCellArrayUpdate());
                } catch (GridAccessException e) {}
                post();

            }
            if (aTick % 40 == 1) {
                post();
            }

            if (rep > 0) {
                rep--;
                update = true;
            }
            if (this.getBaseMetaTileEntity()
                .hasInventoryBeenModified()) {
                update = true;
                rep = 1;
            } ;

            if (update) {
                update = false;
                updateStatus();
            }
            if (wasActive != active) {
                wasActive = active;

                try {
                    this.getProxy()
                        .getGrid()
                        .postEvent(new MENetworkCellArrayUpdate());
                } catch (GridAccessException e) {

                }
                post();
            }
            if (voidFull) {
                voidOverflow = false;
                mInventory[0] = null;
            }
            if (mInventory[0] != null && voidOverflow) {

                if (mInventory[0] != null) mInventory[0].stackSize = Math.min(cap(), mInventory[0].stackSize);
            }

            if (!aBaseMetaTileEntity.getWorld().isRemote && (aTick & 16) != 0) {
                this.getBaseMetaTileEntity()
                    .setActive(
                        this.getProxy()
                            .isPowered() && active);

            }

            if ((!suppressSticky) && ((mInventory[0] == null) && autoUnlock)) {

                suppressSticky = true;
                post();

            }
            if (suppressSticky && ((mInventory[0] != null) || (!autoUnlock))) {

                suppressSticky = false;
                post();

            }

        }
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (aBaseMetaTileEntity.getWorld().isRemote) {
            return;
        }

        boolean needToSort = false;
        for (int i = 1; i < mInventory.length; i++) {
            ItemStack is = mInventory[i];
            if (is == null) continue;
            if (mInventory[0] != null) {
                if (mInventory[0].getItem() != is.getItem() || mInventory[0].getItemDamage() != is.getItemDamage()
                    || (!ItemStack.areItemStackTagsEqual(mInventory[0], is))) {
                    // System.out.println("xx");
                    continue;
                }
            }
            markDirty();
            if (mInventory[0] == null) {
                mInventory[0] = is.copy();
                mInventory[i] = null;
            } else if (cap() - is.stackSize >= mInventory[0].stackSize) {
                mInventory[0].stackSize += is.stackSize;
                mInventory[i] = null;
            } else {
                int to = Math.min(cap() - mInventory[0].stackSize, is.stackSize);
                mInventory[0].stackSize += to;
                mInventory[i].stackSize -= to;
                needToSort = true;
            }
        }
        if (needToSort) fillStacksIntoFirstSlots();

    }

    // use in try-finally
    public SilentCloseable freeze() {
        freezeFlag = true;
        post();
        try {
            this.getProxy()
                .getGrid()
                .postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException e) {}
        return new SilentCloseable() {

            @Override
            public void close() {
                freezeFlag = false;
                try {
                    getProxy().getGrid()
                        .postEvent(new MENetworkCellArrayUpdate());
                } catch (GridAccessException e) {}
            }
        };

    }

    ButtonWidget createRefundButton(IWidgetBuilder<?> builder) {

        Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {

            refund();
        })
            .setPlayClickSound(true)
            .setBackground(GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_EXPORT)

            .addTooltips(ImmutableList.of("Return all internally stored items back to AE"))

            .setPos(new Pos2d(getGUIWidth() - 18 - 3, 5 + 16 + 2))
            .setSize(16, 16);
        return (ButtonWidget) button;
    }

    public void refund() {
        try (SilentCloseable __ = freeze()) {
            if (mInventory[0] != null) {
                IAEItemStack left = getProxy().getStorage()
                    .getItemInventory()
                    .injectItems(AEItemStack.create(mInventory[0]), Actionable.MODULATE, new MachineSource(this));
                mInventory[0] = left == null ? null : left.getItemStack();
            }
        } catch (GridAccessException e) {} finally {}
        markDirty();
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (ProghatchesUtil.handleUse(aPlayer, (MetaTileEntity) aBaseMetaTileEntity.getMetaTileEntity())) {
            return true;
        }

        GTUIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
        return true;
    }

    // @Override
    public boolean useModularUI() {
        return true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {

        return true;
    }

    ItemStackHandler uihandler = new ItemStackHandler(mInventory) {

        public boolean isItemValid(int slot, ItemStack stack) {
            return isItemValidForSlot(slot, stack);
        };

    };

    @Override
    public IItemHandlerModifiable getInventoryHandler() {

        return uihandler;
    }

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        builder.widget(
            new SlotWidget(new BaseSlotPatched(this.getInventoryHandler(), 0))

                .setPos(3, 3));
        builder.widget(
            new DrawableWidget().setDrawable(ModularUITextures.ARROW_LEFT)

                .setPos(3 + 18, 3)
                .setSize(18, 18));
        builder.widget(new SlotWidget(new BaseSlotPatched(this.getInventoryHandler(), 1)).setPos(3 + 18 * 2, 3));
        builder.widget(new SlotWidget(new BaseSlotPatched(this.getInventoryHandler(), 2)).setPos(3 + 18 * 3, 3));
        builder.widget(createRefundButton(builder));
        Widget w;
        builder.widget(
            w = new DrawableWidget().setDrawable(ModularUITextures.ICON_INFO)

                .setPos(3 + 18 * 4 + 1, 3 + 1)
                .setSize(16, 16)
        // .addTooltip("xxxxxxx")
        );

        IntStream.range(0, Integer.valueOf(StatCollector.translateToLocal("programmable_hatches.gt.mechest.tooltip")))
            .forEach(s -> w.addTooltip(LangManager.translateToLocal("programmable_hatches.gt.mechest.tooltip." + +s)));

        builder.widget(createButton(() -> sticky, val -> {
            sticky = val;
            post();
            // updateSlots();
        },
            new ItemDrawable(new ItemStack(Items.slime_ball)),
            ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.sticky")

            )

            ,
            0).setPos(3, 3 + 18 * 2));

        builder.widget(createButton(() -> autoUnlock, val -> {

            cachedFilter[0] = null;
            updateFilter(cachedFilter[0]);
            autoUnlock = val;
            post();
            // updateSlots();
        },
            GTUITextures.OVERLAY_BUTTON_RECIPE_UNLOCKED,

            // new ItemDrawable(new ItemStack(Items.slime_ball)),
            ImmutableList.of(StatCollector.translateToLocal("programmable_hatches.gt.sticky.autounlock")

            )

            ,
            0).setPos(3 + 18, 3 + 18 * 2));
        builder.widget(
            SlotWidget.phantom(

                new ItemStackHandler(cachedFilter) {

                    public void setStackInSlot(int slot, ItemStack stack) {
                        super.setStackInSlot(slot, stack);
                        updateFilter(cachedFilter[0]);
                        autoUnlock = false;
                        post();
                    };
                },
                0

            )
                .setPos(3 + 18 * 2, 3 + 18 * 2)
                .addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.phantom.filter"))

        );

        builder.widget(
            new TextFieldWidget().setPattern(BaseTextFieldWidget.WHOLE_NUMS)
                .setGetter(() -> piority + "")
                .setSetter(s -> {
                    try {
                        piority = Integer.parseInt(s);
                    } catch (Exception e) {
                        piority = 0;
                    } ;
                    post();
                })
                .setSynced(true, true)

                .setFocusOnGuiOpen(true)
                .setTextColor(Color.WHITE.dark(1))

                .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
                .addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.piority"))
                .setPos(3 + 2, 18 * 3 + 3 + 1)
                .setSize(16 * 8, 16))
            .widget(
                new TextFieldWidget().setPattern(BaseTextFieldWidget.WHOLE_NUMS)
                    .setGetter(() -> capOverride + "")
                    .setSetter(s -> {
                        try {
                            capOverride = Integer.parseInt(s);
                        } catch (Exception e) {
                            capOverride = 0;
                        } ;
                        if (capOverride < 64) capOverride = 0;
                        post();
                    })
                    .setSynced(true, true)

                    .setTextColor(Color.WHITE.dark(1))

                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
                    .addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.capOverride.64"))
                    .setPos(3 + 18 * 5 + 1, 3 + 1)
                    .setSize(16 * 4, 16))
            .widget(new CycleButtonWidget().setToggle(() -> voidFull, val -> {
                voidFull = val;

                if (!voidFull) {
                    GTUtility
                        .sendChatToPlayer(buildContext.getPlayer(), GTUtility.trans("269", "Void Full Mode Disabled"));
                } else {
                    GTUtility
                        .sendChatToPlayer(buildContext.getPlayer(), GTUtility.trans("270", "Void Full Mode Enabled"));
                }
            })
                .setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
                .setStaticTexture(GTUITextures.OVERLAY_BUTTON_TANK_VOID_ALL)
                .setGTTooltip(() -> mTooltipCache.getData("GT5U.machines.digitaltank.voidfull.tooltip"))
                .setTooltipShowUpDelay(TOOLTIP_DELAY)
                .setPos(3 + 18 * 3, 3 + 18 * 2)
                .setSize(18, 18))

            .widget(new CycleButtonWidget().setToggle(() -> voidOverflow, val -> {
                voidOverflow = val;

                if (!voidOverflow) {
                    GTUtility.sendChatToPlayer(
                        buildContext.getPlayer(),
                        GTUtility.trans("267", "Overflow Voiding Mode Disabled"));
                } else {
                    GTUtility.sendChatToPlayer(
                        buildContext.getPlayer(),
                        GTUtility.trans("268", "Overflow Voiding Mode Enabled"));
                }
            })
                .setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
                .setStaticTexture(GTUITextures.OVERLAY_BUTTON_TANK_VOID_EXCESS)
                .setGTTooltip(() -> mTooltipCache.getData("GT5U.machines.digitaltank.voidoverflow.tooltip"))
                .setTooltipShowUpDelay(TOOLTIP_DELAY)
                .setPos(3 + 18 * 4, 3 + 18 * 2)
                .setSize(18, 18));

    }

    private Widget createButton(Supplier<Boolean> getter, Consumer<Boolean> setter, IDrawable picture,
        List<String> tooltip, int offset) {
        return new CycleButtonWidget()

            .setToggle(getter, setter)
            .setTextureGetter(__ -> picture)
            .setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE)
            .setTooltipShowUpDelay(TOOLTIP_DELAY)
            .setPos(7 + offset * 18, 62)
            .setSize(18, 18)
            .addTooltips(tooltip);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {

        if (aNBT.hasKey("proxy")) getProxy().readFromNBT(aNBT);
        super.loadNBTData(aNBT);
        piority = aNBT.getInteger("piority");
        sticky = aNBT.getBoolean("sticky");
        autoUnlock = aNBT.getBoolean("autoUnlock");
        suppressSticky = aNBT.getBoolean("suppressSticky");
        NBTTagCompound tag = (NBTTagCompound) aNBT.getTag("cahcedFilter");
        if (tag != null) {
            cachedFilter[0] = ItemStack.loadItemStackFromNBT(tag);
            updateFilter(cachedFilter[0]);
        }
        voidFull = aNBT.getBoolean("voidFull");
        voidOverflow = aNBT.getBoolean("voidOverflow");
        last = AEItemStack.create(mInventory[0]);
        capOverride = aNBT.getInteger("capOverride");
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        getProxy().writeToNBT(aNBT);
        super.saveNBTData(aNBT);
        NBTTagList greggy = aNBT.getTagList("Inventory", 10);
        for (int i = 0; i < mInventory.length; i++) {

            if (mInventory[i] != null) {
                NBTTagCompound t;
                t = ((NBTTagCompound) greggy.getCompoundTagAt(i));
                if (t != null) t.setInteger("Count", mInventory[i].stackSize);
            }

        }
        aNBT.setInteger("piority", piority);
        aNBT.setBoolean("sticky", sticky);
        aNBT.setBoolean("autoUnlock", autoUnlock);
        aNBT.setBoolean("suppressSticky", suppressSticky);
        if (cachedFilter[0] != null) {
            NBTTagCompound tag = new NBTTagCompound();
            cachedFilter[0].writeToNBT(tag);
            aNBT.setTag("cahcedFilter", tag);
        }
        aNBT.setBoolean("voidFull", voidFull);
        aNBT.setBoolean("voidOverflow", voidOverflow);
        if (capOverride != 0) aNBT.setInteger("capOverride", capOverride);
    }

    boolean facingJustChanged;

    @Override
    public void onFacingChange() {
        updateValidGridProxySides();
        facingJustChanged = true;

    }

    @Override
    public void setItemNBT(NBTTagCompound aNBT) {

        final NBTTagList tItemList = new NBTTagList();
        for (int i = 0; i < getRealInventory().length; i++) {
            final ItemStack tStack = getRealInventory()[i];
            if (tStack != null) {
                final NBTTagCompound tTag = new NBTTagCompound();
                tTag.setInteger("IntSlot", i);
                tStack.writeToNBT(tTag);
                tTag.setInteger("Count", tStack.stackSize);
                tItemList.appendTag(tTag);
            }
        }
        aNBT.setTag("Inventory", tItemList);

        aNBT.setInteger("piority", piority);
        aNBT.setBoolean("sticky", sticky);
        aNBT.setBoolean("autoUnlock", autoUnlock);
        aNBT.setBoolean("suppressSticky", suppressSticky);
        if (cachedFilter[0] != null) {
            NBTTagCompound tag = new NBTTagCompound();
            cachedFilter[0].writeToNBT(tag);
            aNBT.setTag("cahcedFilter", tag);
        }
        aNBT.setBoolean("voidFull", voidFull);
        aNBT.setBoolean("voidOverflow", voidOverflow);
        if (capOverride != 0) aNBT.setInteger("capOverride", capOverride);
        /*
         * if(cachedFilter[0]!=null){ NBTTagCompound tag=new NBTTagCompound();
         * cachedFilter[0].writeToNBT(tag); aNBT.setTag("cahcedFilter", tag); }
         * if(piority!=0)aNBT.setInteger("piority", piority);
         * if(sticky)aNBT.setBoolean("sticky", sticky);
         * if(voidFull)aNBT.setBoolean("voidFull", voidFull);
         * if(voidOverflow)aNBT.setBoolean("voidOverflow", voidOverflow);
         * if(capOverride!=0)aNBT.setInteger("capOverride", capOverride);
         */
    }

    @Override
    public boolean shouldDropItemAt(int index) {

        return false;
    }

    public static String name(int t) {

        return StatCollector.translateToLocalFormatted("mesuperchest.name." + (t >= 6), suffix[t - 1]);
    }

    public static String[] suffix = { "I", "II", "III", "IV", "V", "I", "II", "III", "IV", "V" };

    @Override
    public void onColorChangeServer(byte aColor) {

        super.onColorChangeServer(aColor);
        AEColor c;
        if (aColor == -1) {
            c = (AEColor.Transparent);
        } else c = (AEColor.values()[15 - aColor]);

        try {
            getProxy().setColor(c);
            getGridNode(null).updateState();
        } catch (Exception e) {}

    }

    @Override
    public void setPriority(int newValue) {
        this.piority = newValue;

    }

    /*
     * @MENetworkEventSubscribe public void powerRender(final
     * MENetworkPowerStatusChange c) { this.updateStatus(); }
     * @MENetworkEventSubscribe public void chanRender(final
     * MENetworkChannelsChanged changedChannels) { this.updateStatus(); }
     * @MENetworkEventSubscribe public void updateChannels(final
     * MENetworkChannelsChanged changedChannels) { this.updateStatus(); }
     */
    boolean update;

    public void updateStatus() {

        post();

    }

    @Override
    public void setInventorySlotContents(int aIndex, ItemStack aStack) {
        super.setInventorySlotContents(aIndex, aStack);
        post();
    }

    @Override
    public ItemStack decrStackSize(int aIndex, int aAmount) {
        try {
            return super.decrStackSize(aIndex, aAmount);
        } finally {
            post();
        }
    }

    boolean voidFull;
    boolean voidOverflow;

    @MENetworkEventSubscribe
    @Unique
    public void powerRender(final MENetworkPowerStatusChange w) {

        cellUpdate();

    }

    @MENetworkEventSubscribe
    @Unique
    public void updateChannels(final MENetworkChannelsChanged w) {
        cellUpdate();

    }

    @Override
    public void cellUpdate() {
        try {
            this.getProxy()
                .getGrid()
                .postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException e) {}

    }

    @Override
    public boolean isPowered() {

        return getProxy().isPowered();
    }

    @Override
    public boolean isActive() {

        return getProxy().isActive();
    }
}
