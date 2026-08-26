package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.NumberFormatMUI;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.MultiChildWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TabButton;
import com.gtnewhorizons.modularui.common.widget.TabContainer;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;

import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomNameObject;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.PatternMultiplierHelper;
import appeng.util.Platform;
import codechicken.nei.ItemStackMap;
import codechicken.nei.ItemStackSet;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.ExConfigEntry;
import reobf.proghatches.gt.metatileentity.DualInputHatch.Net;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch.DA;
import reobf.proghatches.gt.metatileentity.bufferutil.ItemStackG;
import reobf.proghatches.gt.metatileentity.bufferutil.LongWrapper;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.gt.metatileentity.util.ISpecialOptimize;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable.Result;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.gui.modularui.util.PatternSlot;
import reobf.proghatches.item.ItemFakePattern;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class PatternDualInputHatch extends BufferedDualInputHatch implements ICraftingProvider, IGridProxyable,
    ICustomNameObject, IInterfaceViewable, IPowerChannelState, IActionHost, IMultiplePatternPushable,ISpecialOptimize {

    public PatternDualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
        boolean mMultiFluid, int bufferNum) {
        super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

    }
    public PatternDualInputHatch(String mName, byte mTier,int slots, String[] mDescriptionArray, ITexture[][][] mTextures,
            boolean mMultiFluid, int bufferNum) {
            super(mName, mTier,slots, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

        }
    
   
    @Override
    public int page() {
  
    	return 1;
    }
    @Override
    public int rows() {
        return 4;
    }

    @Override
    public int rowSize() {
        return 9;
    }

    @Override
    public IInventory getPatterns() {
        return patternMapper;
    }

    IInventory patternMapper = new IInventory() {

        @Override
        public int getSizeInventory() {

            return pattern.length;
        }

        @Override
        public ItemStack getStackInSlot(int slotIn) {

            return pattern[slotIn];
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {

            try {
                if (pattern[index] != null) {
                    ItemStack itemstack;

                    if (pattern[index].stackSize <= count) {
                        itemstack = pattern[index];
                        pattern[index] = null;
                        this.markDirty();
                        return itemstack;
                    } else {
                        itemstack = pattern[index].splitStack(count);

                        if (pattern[index].stackSize == 0) {
                            pattern[index] = null;
                        }

                        this.markDirty();
                        return itemstack;
                    }
                } else {
                    return null;
                }
            } finally {

                onPatternChange();
            }
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int index) {

            return null;
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            pattern[index] = stack;
            onPatternChange();
        }

        @Override
        public String getInventoryName() {

            return "";
        }

        @Override
        public boolean hasCustomInventoryName() {

            return false;
        }

        // @Override
        // public int stack

        @Override
        public void markDirty() {

        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {

            return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {

            return true;
        }

        @Override
        public int getInventoryStackLimit() {

            return 1;
        }
    };

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {

        return new ITexture[] { aBaseTexture,
            TextureFactory.of(
                supportsFluids() ? BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER
                    : BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUS) };

    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture,
            TextureFactory.of(
                supportsFluids() ? BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER
                    : BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUS) };

    }

    public PatternDualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, int bufferNum,
        boolean sf,int page, String... optional) {

        super(
            id,
            name,
            nameRegional,
            tier,
            mMultiFluid,
            bufferNum,
            (optional.length > 0 ? optional
                : reobf.proghatches.main.Config.get(
                    "PDIH" + (sf ? "" : "B"),
                    ImmutableMap.of(
                        "bufferNum",
                        bufferNum,
                        "fluidSlots",
                        page*16/* fluidSlots() */, /*
                                               * "cap", format.format((int)
                                               * (4000 * Math.pow(4, tier) /
                                               * (mMultiFluid ? 4 : 1))),
                                               */
                        "mMultiFluid",
                        mMultiFluid,
                        "slots",
                        page*16/*
                                                              * , "stacksize", (int) (64 *
                                                              * Math.pow(2, Math.max(tier - 3, 0)))
                                                              */))

            ));
        if (sf != supportsFluids()) {

            throw new AssertionError();
        }
    }

    public int fluidSlots() {
        return supportsFluids()?16*page():0;

    }

    ItemStack[] pattern = new ItemStack[36];

   

    MachineSource requestSource;

    private BaseActionSource getRequest() {

        if (requestSource == null) requestSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
        return requestSource;
    }

    private void refundAll() throws Exception {
        markDirty();
        dirty = true;
        BaseActionSource src = getRequest();
        IMEMonitor<IAEItemStack> sg = getProxy().getStorage()
            .getItemInventory();
        abstract class Inv {

            abstract ItemStack[] geti();

            abstract FluidStack[] getf();
        }
        Consumer<Inv> consumer = inv -> {
            try {
                for (ItemStack itemStack : inv.geti()) {
                    if (itemStack == null || itemStack.stackSize == 0) continue;
                    IAEItemStack rest = Platform.poweredInsert(
                        getProxy().getEnergy(),
                        sg,
                        AEApi.instance()
                            .storage()
                            .createItemStack(itemStack),
                        src);
                    itemStack.stackSize = rest != null && rest.getStackSize() > 0 ? (int) rest.getStackSize() : 0;
                }
                IMEMonitor<IAEFluidStack> fsg = getProxy().getStorage()
                    .getFluidInventory();
                for (FluidStack fluidStack : inv.getf()) {
                    if (fluidStack == null || fluidStack.amount == 0) continue;
                    IAEFluidStack rest = Platform.poweredInsert(
                        getProxy().getEnergy(),
                        fsg,
                        AEApi.instance()
                            .storage()
                            .createFluidStack(fluidStack),
                        src);
                    fluidStack.amount = 0;
                } ;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        inv0.stream()
            .map(s -> new Inv() {

                @Override
                ItemStack[] geti() {
                    return flat(s.mStoredItemInternal);
                }

                @Override
                FluidStack[] getf() {
                    return flat(s.mStoredFluidInternal);
                }
            })
            .forEach(consumer);;
        consumer.accept(new Inv() {

            @Override
            ItemStack[] geti() {

                return mInventory;
            }

            @Override
            FluidStack[] getf() {

                return Arrays.stream(mStoredFluid)
                    .map(s -> s.getFluid())
                    .toArray(FluidStack[]::new);
            }
        });

    }

   

    static AdaptableUITexture mode0 = AdaptableUITexture.of("proghatches", "gui/restrict_mode0", 18, 18, 1);

    int[] multiplier = new int[36];
    {
        Arrays.fill(multiplier, 1);
    }

    public void refresh() {
        for (int i = 0; i < patternItemCache.length; i++) {
            patternItemCache[i] = null;
        }
        postMEPatternChange();

    }

    
    private static String ps(int amount) {
        return numberFormatx.formatWithSuffix(amount);

    }

    private static final NumberFormatMUI numberFormatx = new NumberFormatMUI();
    boolean needPatternSync;

    private void onPatternChange() {
        if (!getBaseMetaTileEntity().isServerSide()) return;
        // we do not refund 'cause it's impossible to trace the item

        needPatternSync = true;
    }

    @Override
    public void gridChanged() {
        needPatternSync = true;
    }

    @Override
    public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {

        return false;
    }

    @Override
    public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {

        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection side, Fluid aFluid) {

        return false;
    }

    @Override
    public boolean canFill(ForgeDirection side, Fluid aFluid) {

        return false;
    }

    public class Inst extends PatternDualInputHatch {

        public Inst(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures, boolean mMultiFluid,
            int bufferNum) {
            super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
        }
        public Inst(String mName, byte mTier,int slots, String[] mDescriptionArray, ITexture[][][] mTextures, boolean mMultiFluid,
                int bufferNum) {
                super(mName, mTier,slots, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
            }
        @Override
        public boolean supportsFluids() {
            return PatternDualInputHatch.this.supportsFluids();
        }
@Override
public int page() {

	return PatternDualInputHatch.this.page();
}
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new Inst(mName, mTier,16*page()+1, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
    }

    @Override
    public void initTierBasedField() {
        if (supportsFluids()) super.initTierBasedField();
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        additionalConnection = aNBT.getBoolean("additionalConnection");
        NBTTagCompound tag = aNBT.getCompoundTag("patternSlots");
        if (tag != null) for (int i = 0; i < pattern.length; i++) {
            pattern[i] = Optional.ofNullable(tag.getCompoundTag("i" + i))
                .map(ItemStack::loadItemStackFromNBT)
                .orElse(null);
        }
        customName = aNBT.getString("customName");

        getProxy().readFromNBT(aNBT);
        saved = aNBT.getLong("saved");
        super.loadNBTData(aNBT);
        multiplier = aNBT.getIntArray("multiplier");
        if (multiplier.length < 36) multiplier = new int[36];
        for (int i = 0; i < multiplier.length; i++) {
            multiplier[i] = Math.max(multiplier[i], 1);
        }
        restrictToInt=aNBT.getBoolean("restrictToInt" );
        allowopt=aNBT.getBoolean("allowopt");normalopt=aNBT.getBoolean("normalopt");
        updateValidGridProxySides();
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setBoolean("additionalConnection", additionalConnection);
        NBTTagCompound tag = new NBTTagCompound();// aNBT.getCompoundTag("patternSlots");

        for (int i = 0; i < pattern.length; i++) {
            final int ii = i;
            Optional.ofNullable(pattern[i])
                .map(s -> s.writeToNBT(new NBTTagCompound()))
                .ifPresent(s -> tag.setTag("i" + ii, s));
        }
        aNBT.setTag("patternSlots", tag);
        Optional.ofNullable(customName)
            .ifPresent(s -> aNBT.setString("customName", s));
        getProxy().writeToNBT(aNBT);
        aNBT.setLong("saved", saved);
        aNBT.setIntArray("multiplier", multiplier);
        aNBT.setBoolean("restrictToInt", restrictToInt); 
        aNBT.setBoolean("allowopt", allowopt);  aNBT.setBoolean("normalopt", normalopt);
        super.saveNBTData(aNBT);
    }

    private void clearInv() {

        for (int i = 0; i < page()*16; i++) mInventory[i] = null;
        for (int i = 0; i < this.fluidSlots(); i++) mStoredFluid[i].setFluid(null);;

    }

     boolean postMEPatternChange() {
        // don't post until it's active
        if (!getProxy().isActive()) return false;
        try {
            getProxy().getGrid()
                .postEvent(new MENetworkCraftingPatternChange(this, getProxy().getNode()));
        } catch (GridAccessException ignored) {
            return false;
        }
        return true;
    }

    long lastSync;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);

        if (getBaseMetaTileEntity().isServerSide()) {
            if (needPatternSync /*&& aTimer > lastSync + 100*/) {
                needPatternSync = !postMEPatternChange();
                lastSync = aTimer;
            }
            if (aTimer % 20 == 0) {
                getBaseMetaTileEntity().setActive(isActive());
            }
        }
    }

    @Override
    public int getInventoryStackLimit() {

        return Integer.MAX_VALUE;
    }

    public int n = 1;
    public boolean skipActiveCheck;

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (!isActive() && !skipActiveCheck) return false;
        if (!isEmpty()) return false;
        if (!supportsFluids()) {
            for (int i = 0; i < table.getSizeInventory(); ++i) {
                ItemStack itemStack = table.getStackInSlot(i);
                if (itemStack == null) continue;
                if (itemStack.getItem() instanceof ItemFluidPacket) return false;
            }
        }

        int items = 0;
        int fluids = 0;
        int size = table.getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = table.getStackInSlot(i);
            if (itemStack == null) continue;
            if (itemStack.getItem() instanceof ItemFluidPacket) {
                fluids++;
                if (fluids > this.fluidSlots()) {
                    clearInv();
                    return false;
                }

                mStoredFluid[fluids - 1].setFluidDirect(ItemFluidPacket.getFluidStack(itemStack));

            } else {
                items++;
                if (items > page()*16) {
                    clearInv();
                    return false;
                }
                mInventory[items - 1] = itemStack;

            }
        }
        markDirty();
        dirty = true;
        // inv0.recordRecipeOrClassify(this.mStoredFluid, mInventory)
        // classify();

        /*
         * for (DualInvBuffer inv0 : this.sortByEmpty()) { if (inv0.full() ==
         * false) if(inv0.recordRecipeOrClassify(this.mStoredFluid,
         * mInventory)|| inv0.classify(this.mStoredFluid,
         * mInventory,true))break; }
         */

        /*
         * Integer check = detailmap.get(patternDetails); if(check==null){
         * currentID++; detailmap.put(patternDetails,currentID );
         * check=currentID; }
         */

        DualInvBuffer theBuffer = /* ((BufferedDualInputHatch) master). */classifyForce();
        if (theBuffer != null) {
            theBuffer.onChange();
        }
        justHadNewItems = true;
        return true;
    }

    private boolean isEmpty() {
        for (ItemStack is : mInventory) {
            if (is != null && is.stackSize > 0) return false;
        }
        for (FluidTank is : mStoredFluid) {
            if (is.getFluidAmount() > 0) return false;
        }
        return true;
    }

    @Override
    public boolean isBusy() {
        return !isEmpty();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return getProxy().getNode();
    }

    private AENetworkProxy gridProxy;

    @Override
    public AENetworkProxy getProxy() {
        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(
                this,
                "proxy",
                new ItemStack(
                    GregTechAPI.sBlockMachines,
                    1,
                    this.getBaseMetaTileEntity()
                        .getMetaTileID()),
                true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    @Override
    public void securityBreak() {
        // no op
    }

    @Override
    public String getName() {

        if (hasCustomName()) {
            return getCustomName();
        }
        StringBuilder name = new StringBuilder();
        if (getCrafterIcon() != null) {
            name.append(getCrafterIcon().getDisplayName());
        } else {
            name.append(getLocalName());// getinventoryname()
        }

        /*
         * if (mInventory[SLOT_CIRCUIT] != null) { name.append(" - ");
         * name.append(mInventory[SLOT_CIRCUIT].getItemDamage()); }
         */

        for (ItemStack is : this.shared.getDisplayItems()) {
            name.append(" - ");

            if (is.getItem() != GTUtility.getIntegratedCircuit(0)
                .getItem()) {
                name.append(is.getDisplayName());
                if (is.getItemDamage() > 0) {
                    name.append("@" + is.getItemDamage());
                }
            } else {
                name.append(is.getItemDamage());
            }

            // if(is.stackSize>0){name.append("*"+is.stackSize);}
        }

        for (FluidStack is : this.shared.getDisplayFluid()) {
            name.append(" - ");
            name.append(is.getLocalizedName());
            // if(is.amount>0){name.append("*"+is.amount);}
        }

        return name.toString();
    }

    @Override
    public TileEntity getTileEntity() {
        return (TileEntity) getBaseMetaTileEntity();
    }

    @Override
    public boolean shouldDisplay() {

        return true;
    }

    String customName;
    private boolean additionalConnection;

    @Override
    public String getCustomName() {

        return customName;
    }

    @Override
    public boolean hasCustomName() {

        return customName != null && (!customName.equals(""));
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
        float aX, float aY, float aZ) {
        final ItemStack is = aPlayer.inventory.getCurrentItem();
        if (is != null && is.getItem() instanceof ToolQuartzCuttingKnife) {
            if (ForgeEventFactory.onItemUseStart(aPlayer, is, 1) <= 0) return false;
            IGregTechTileEntity te = getBaseMetaTileEntity();
            aPlayer.openGui(
                AppEng.instance(),
                GuiBridge.GUI_RENAMER.ordinal() << 5 | (side.ordinal()),
                te.getWorld(),
                te.getXCoord(),
                te.getYCoord(),
                te.getZCoord());
            return true;
        }
        return super.onRightclick(aBaseMetaTileEntity, aPlayer, side, aX, aY, aZ);
    }

    @Override
    public void setCustomName(String name) {
        customName = name;

    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord(getTileEntity());
    }

    @Override
    public boolean isActive() {
        return getProxy() != null && getProxy().isActive();
    }

    ItemStack[] patternItemCache = new ItemStack[36];
    ICraftingPatternDetails[] patternDetailCache = new ICraftingPatternDetails[36];

    public static class DA implements ICraftingPatternDetails {
   	 private IAEStack[] mul(IAEStack<?>[] in) {
   		 IAEStack[] ret = new IAEStack[in.length];
            for (int k = 0; k < ret.length; k++) {
                ret[k] = in[k];
                if (ret[k] != null) {
                    ret[k] = ret[k].copy()
                        .setStackSize(ret[k].getStackSize() * m);
                }

            }
            return ret;
		}
       public DA(ICraftingPatternDetails p, int m) {
           if (p == null) throw new NullPointerException();
           this.p = p;
           this.m = m;
           if (m < 1) m = 1;
       }
       IAEStack[] aeci,aeco,aei,aeo;
       @Override
       public IAEStack<?>[] getAEInputs() {
       	 if (aei == null) {
                aei = mul(p.getAEInputs());
       	 	}
       	return aei;
       }
      
		@Override
       public IAEStack<?>[] getAEOutputs() {
      	 if (aeo == null) {
            aeo = mul(p.getAEOutputs());
   	 	}
      	 return aeo;
       }
       @Override
       public IAEStack<?>[] getCondensedAEInputs() {
      	 if (aeci == null) {
            aeci = mul(p.getCondensedAEInputs());
   	 	}
      	 return aeci;
      	 }
       @Override
       public IAEStack<?>[] getCondensedAEOutputs() {
         	 if (aeco == null) {
                aeco = mul(p.getCondensedAEOutputs());
       	 	}
          	 return aeco;  }
       
       
       ICraftingPatternDetails p;
       int m;

       @Override
       public ItemStack getPattern() {

           ItemStack is = new ItemStack(MyMod.fakepattern);
           is.setTagCompound(new NBTTagCompound());
           is.getTagCompound()
               .setByte("type", (byte) 3);
           is.getTagCompound()
               .setTag(
                   "p",
                   p.getPattern()
                       .writeToNBT(new NBTTagCompound()));
           is.getTagCompound()
               .setInteger("m", m);
           return is;
       }

       @Override
       public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {

           return p.isValidItemForSlot(slotIndex, itemStack, world);
       }

       @Override
       public boolean isCraftable() {

           return p.isCraftable();
       }

       IAEItemStack[] i;

       public IAEItemStack[] mul(IAEItemStack[] in) {
           IAEItemStack[] ret = new IAEItemStack[in.length];
           for (int k = 0; k < ret.length; k++) {
               ret[k] = in[k];
               if (ret[k] != null) {
                   ret[k] = ret[k].copy()
                       .setStackSize(ret[k].getStackSize() * m);
               }

           }
           return ret;
       }

       @SuppressWarnings("deprecation")
		@Override
       public IAEItemStack[] getInputs() {
           if (i == null) {
               i = mul(p.getInputs());

           }
           return i;
       }

       IAEItemStack[] ci;

       @Override
       public IAEItemStack[] getCondensedInputs() {
           if (ci == null) {
               ci = mul(p.getCondensedInputs());
           }
           return ci;
       }

       IAEItemStack[] co;

       @Override
       public IAEItemStack[] getCondensedOutputs() {
           if (co == null) {
               co = mul(p.getCondensedOutputs());
           }
           return co;
       }

       IAEItemStack[] o;

       @Override
       public IAEItemStack[] getOutputs() {
           if (o == null) {
               o = mul(p.getOutputs());
           }
           return o;
       }

       @Override
       public boolean canSubstitute() {

           return p.canSubstitute();
       }
       public  boolean canBeSubstitute() {  
       	return p.canBeSubstitute();
       };

       ItemStack so;

       @Override
       public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
           if (so == null) {
               so = p.getOutput(craftingInv, world);
               if (so != null) {
                   so = so.copy();
                   so.stackSize *= m;
               }
           }
           return so;
       }

       @Override
       public int getPriority() {

           return p.getPriority();
       }

       @Override
       public void setPriority(int priority) {
           p.setPriority(priority);
       }

       @Override
       public boolean equals(Object obj) {
           if (obj == null) {
               return false;
           }
           if (this.getClass() != obj.getClass()) {
               return false;
           }
           final DA other = (DA) obj;
           if (this.p != null && other.p != null) {
               return this.p.equals(other.p) && this.m == other.m;
           }
           return false;

       }

       @Override
       public int hashCode() {

           return p.hashCode() + 31 * (m - 1);
       }
   }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if (!isActive()) return;

        for (int index = 0; index < pattern.length; index++) {
            ItemStack slot = pattern[index];

            if (slot == null) {
                patternItemCache[index] = null;
                patternDetailCache[index] = null;
                continue;
            }

            if (patternItemCache[index] == pattern[index]) {// just compare
                                                            // object id
                craftingTracker.addCraftingOption(this, patternDetailCache[index]);
                continue;
            }

            ICraftingPatternDetails details = null;
            try {
                details = ((ICraftingPatternItem) slot.getItem()).getPatternForItem(
                    slot,
                    this.getBaseMetaTileEntity()
                        .getWorld());
            } catch (Exception e) {}
            if (details == null) {
                GTMod.GT_FML_LOGGER.warn(
                    "Found an invalid pattern at " + getBaseMetaTileEntity().getCoords()
                        + " in dim "
                        + getBaseMetaTileEntity().getWorld().provider.dimensionId);
                continue;
            }
            patternItemCache[index] = pattern[index];

            patternDetailCache[index] = multiplier[index] == 1 ? details : new DA(details, multiplier[index]);
            craftingTracker.addCraftingOption(this, patternDetailCache[index]);

        }

    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
        return isOutputFacing(forgeDirection) ? AECableType.SMART : AECableType.NONE;
    }

    private void updateValidGridProxySides() {
        if (additionalConnection) {
            getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));
        } else {
            getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
        }
        // getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

    }

    @Override
    public void onFacingChange() {
        updateValidGridProxySides();
    }

    @Override
    public boolean isPowered() {
        return getProxy() != null && getProxy().isPowered();
    }

    @Override
    public ItemStack getCrafterIcon() {
        ItemStack is = this.getMachineCraftingIcon();
        return is == null ? new ItemStack(GregTechAPI.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID()) : is;
    }

    @Override
    public void onBlockDestroyed() {
        try {
            refundAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBlockDestroyed();

        IGregTechTileEntity te = this.getBaseMetaTileEntity();
        World aWorld = te.getWorld();
        int aX = te.getXCoord();
        short aY = te.getYCoord();
        int aZ = te.getZCoord();

        for (int i = 0; i < pattern.length; i++) {
            final ItemStack tItem = pattern[i];
            if ((tItem != null) && (tItem.stackSize > 0)) {
                final EntityItem tItemEntity = new EntityItem(
                    aWorld,
                    aX + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    aY + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    aZ + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    new ItemStack(tItem.getItem(), tItem.stackSize, tItem.getItemDamage()));
                if (tItem.hasTagCompound()) {
                    tItemEntity.getEntityItem()
                        .setTagCompound(
                            (NBTTagCompound) tItem.getTagCompound()
                                .copy());
                }
                tItemEntity.motionX = (XSTR_INSTANCE.nextGaussian() * 0.05D);
                tItemEntity.motionY = (XSTR_INSTANCE.nextGaussian() * 0.25D);
                tItemEntity.motionZ = (XSTR_INSTANCE.nextGaussian() * 0.05D);
                aWorld.spawnEntityInWorld(tItemEntity);
                tItem.stackSize = 0;
                pattern[i] = null;
            }
        }
    }
    public boolean restrictToInt;
	public long singleSlotLimit() {
		return restrictToInt?Integer.MAX_VALUE:Long.MAX_VALUE;//limitToIntMax ? Integer.MAX_VALUE : Long.MAX_VALUE;
	}
    public long fluidLimit() {

        return restrictToInt?Integer.MAX_VALUE:Long.MAX_VALUE;
    }

    public long itemLimit() {

        return restrictToInt?Integer.MAX_VALUE:Long.MAX_VALUE;
    }

    boolean createInsertion() {
        return false;
    }

    boolean showFluidLimit() {

        return false;
    }

    @Override
    public int getInventoryFluidLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack is) {
        if (aPlayer.isSneaking()) {
            IGregTechTileEntity te = getBaseMetaTileEntity();
            aPlayer.openGui(
                AppEng.instance(),
                GuiBridge.GUI_RENAMER.ordinal() << 5 | (side.ordinal()),
                te.getWorld(),
                te.getXCoord(),
                te.getYCoord(),
                te.getZCoord());
            return true;
        }

        additionalConnection = !additionalConnection;
        updateValidGridProxySides();
        aPlayer.addChatComponentMessage(
            new ChatComponentTranslation("GT5U.hatch.additionalConnection." + additionalConnection));
        return true;
    }

    public Net getNetwork() {
        try {
            return new Net(
                this.getGridNode(ForgeDirection.UP)
                    .getGrid(),
                this);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public IGridNode getActionableNode() {

        return this.getGridNode(ForgeDirection.UP);
    }

    public Object getTile() {
        return this.getBaseMetaTileEntity();
    }

    @Override
    public boolean allowsPatternOptimization() {
       
        return allowopt;
    }
boolean allowopt=true;
    @Override
    public int[] pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table, int maxTodo) {
        if (Config.fastPatternDualInput == false) return AZERO;
        if (maxTodo <= 0) return AZERO;
        if (!isActive() && !skipActiveCheck) return AZERO;
        if (!isEmpty()) return AZERO;
        if (!supportsFluids()) {
            for (int i = 0; i < table.getSizeInventory(); ++i) {
                ItemStack itemStack = table.getStackInSlot(i);
                if (itemStack == null) continue;
                if (itemStack.getItem() instanceof ItemFluidPacket) return AZERO;
            }
        }

        int items = 0;
        int fluids = 0;
        int size = table.getSizeInventory();
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = table.getStackInSlot(i);
            if (itemStack == null) continue;
            if (itemStack.getItem() instanceof ItemFluidPacket) {
                fluids++;
                if (fluids > this.fluidSlots()) {
                    clearInv();
                    return AZERO;
                }

                mStoredFluid[fluids - 1].setFluidDirect(ItemFluidPacket.getFluidStack(itemStack));

            } else {
                items++;
                if (items > page()*16) {
                    clearInv();
                    return AZERO;
                }
                mInventory[items - 1] = itemStack;

            }
        }
        markDirty();
        dirty = true;
        // classify();
        int suc = 0;

        // DualInvBuffer theBuffer=classifyForce();

        /*
         * Integer check = detailmap.get(patternDetails); if(check==null){
         * currentID++; detailmap.put(patternDetails,currentID );
         * check=currentID; }
         */

        DualInvBuffer theBuffer = /* ((BufferedDualInputHatch) master). */classifyForce();

        // if(theBuffer!=null){
        suc++;
        maxTodo--;
        // }
        /*
         * for (DualInvBuffer inv0 : this.sortByEmpty()) { if (inv0.full() ==
         * false) if(inv0.recordRecipeOrClassify(this.mStoredFluid,
         * mInventory)|| inv0.classify(this.mStoredFluid, mInventory, true) ){
         * theBuffer=inv0;suc++;maxTodo--; break;} }
         */
        if (theBuffer != null) {// if succeeded, it's safe to simply add to
                                // stacksize to push more patterns
            int todo = Math.min(theBuffer.space()
            /*
             * space() will return correct result here it assumes item/fluid
             * type is correct
             */
                , maxTodo);

            if (todo > 0) {
                for (int ix = 0; ix < theBuffer.i; ix++) {
                    if (theBuffer.mStoredItemInternalSingle[ix] != null) {
                        if (theBuffer.mStoredItemInternal[ix] == null) {
                            theBuffer.mStoredItemInternal[ix] = ItemStackG
                                .neo(theBuffer.mStoredItemInternalSingle[ix].copy());
                            theBuffer.mStoredItemInternal[ix].stackSize(0);// circuit?
                        }
                        theBuffer.mStoredItemInternal[ix]
                            .stackSizeInc(new LongWrapper(theBuffer.mStoredItemInternalSingle[ix].stackSize *1L* todo));
                    }
                }

                for (int ix = 0; ix < theBuffer.f; ix++) {
                    if (theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount() > 0) {
                        if (theBuffer.mStoredFluidInternal[ix].getFluidAmount() <= 0) {
                            FluidStack zerof = theBuffer.mStoredFluidInternalSingle[ix].getFluid()
                                .copy();
                            zerof.amount = 0;
                            theBuffer.mStoredFluidInternal[ix].setFluid(zerof);

                        }
                        theBuffer.mStoredFluidInternal[ix]
                            .amountAcc(theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount() * 1l * todo);
                    }
                }
                theBuffer.nonempty=true;
                suc += todo;
            }
            theBuffer.onChange();
        }

        saved += suc;

        justHadNewItems = true;
        return new int[] { suc };
    }

    long saved;

    @SideOnly(Side.CLIENT)
    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {

        super.getWailaBody(itemStack, currenttip, accessor, config);
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {

            currenttip.add(

                StatCollector.translateToLocalFormatted(
                    "proghatch.saved.statistic",
                    accessor.getNBTData()
                        .getLong("saved")));
        }

    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {

        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.setLong("saved", saved);
    }

   /* @Override
    public boolean isInfBuffer() {

        return true;
    }*/

@Override
public int getCircuitSlot() {
	
	return ProghatchesUtil.getSlots(slotTierOverride(mTier))*page();
}

	@Override
	public void optimize(ItemStackMap<Pair<Object, Integer>> lookupMap) {
		IInventory patternInv = this.getPatterns();

		for (int i = 0; i < patternInv.getSizeInventory(); i++) {
			ItemStack stack = patternInv.getStackInSlot(i);

			if (stack == null) {
				continue;
			}
			if(multiplier[i]>1){
				stack=new PatternDualInputHatch.DA(
						((ICraftingPatternItem)stack.getItem()).getPatternForItem(stack,this.getBaseMetaTileEntity().getWorld())
						, multiplier[i]).getPattern();
			}
			/*if ((stack.getItem() instanceof ItemFakePattern) && (stack.hasTagCompound() == true)
					&& (3 == stack.getTagCompound().getInteger("type"))) {
				PatternDualInputHatch.DA parent = (DA) ((ItemFakePattern) stack.getItem()).getPatternForItem(stack,
						null);
				ICraftingPatternDetails wrapped = parent.p;

				stack = wrapped.getPattern();
			}*/
			
			

			Pair<Object, Integer> pair = lookupMap.get(stack);
			if (pair == null)
				continue;
			Integer bitMultiplier = pair.getValue();
			if (bitMultiplier == 0)
				return;
			boolean isDividing = false;
			if (bitMultiplier < 0) {
				isDividing = true;
				bitMultiplier = -bitMultiplier;
			}
			if(normalopt) {
				 PatternMultiplierHelper.applyModification(stack, bitMultiplier);
				 patternInv.setInventorySlotContents(i, stack);
			}
			else {
			multiplier[i] = isDividing ? multiplier[i] >> bitMultiplier : multiplier[i] << bitMultiplier;
			if (multiplier[i] <= 0) {
				multiplier[i] = 1;
			}
			}
			markDirty();
			onPatternChange(); 
			refresh();
			/*
			 * ItemStack sCopy = sdtack.copy();
			 * pair.getKey().applyModification(sCopy, pair.getValue());
			 * patternInv.setInventorySlotContents(i, sCopy);
			 */

		}

	}
	@Override
	public void blacklist(ItemStackSet black) {
		for(ICraftingPatternDetails a:patternDetailCache){
			if(a!=null)black.add(a.getPattern());
		}
	}

	// ===================== MUI2 =====================
	// MUI2 port of the pattern insertion window. Adds, on top of the parent
	// (BufferedDualInputHatch) UI, a button that opens a popup panel with three pages:
	//   page 1: pattern slots
	//   page 2: individual multiplier op.
	//   page 3: batch multiplier op. (also hosts the refund button)
	// Ported from the legacy MUI1 pattern window (createPatternWindow / addUIWidgets).

	private com.cleanroommc.modularui.widgets.ButtonWidget<?> createRefundButton2(PanelSyncManager syncManager) {
		InteractionSyncHandler refund = new InteractionSyncHandler().setOnMousePressed(data -> {
			PatternDualInputHatch.this.dirty = true;
			try {
				PatternDualInputHatch.this.refundAll();
			} catch (Exception e) {
				// e.printStackTrace();
			}
		});
		syncManager.syncValue("refund_all", refund);
		return (com.cleanroommc.modularui.widgets.ButtonWidget<?>) new com.cleanroommc.modularui.widgets.ButtonWidget<>().syncHandler(refund)
			.background(GTGuiTextures.BUTTON_STANDARD, GTGuiTextures.OVERLAY_BUTTON_EXPORT)
			.tooltip(t -> t.addLine(IKey.str("Return all internally stored items back to AE")))
			.size(16, 16);
	}

	/**
	 * Builds a 16x16 button whose action runs on the server. The click is synced to the
	 * server by the {@link InteractionSyncHandler}, where {@code action} is executed, so
	 * any mutation of server-persisted state (e.g. {@code multiplier[]}) takes effect.
	 */
	private com.cleanroommc.modularui.widgets.ButtonWidget<?> makeBatchButton(PanelSyncManager syncManager, String key, Runnable action) {
		InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(data -> action.run());
		syncManager.syncValue(key, handler);
		return (com.cleanroommc.modularui.widgets.ButtonWidget<?>) new com.cleanroommc.modularui.widgets.ButtonWidget<>().syncHandler(handler)
			.size(16, 16)
			.background(GTGuiTextures.BUTTON_STANDARD);
	}

	@Override
	public void populateUI(ModularPanel builder, PosGuiData data, PanelSyncManager syncManager,
		UISettings uiSettings) {
		super.populateUI(builder, data, syncManager, uiSettings);

		if (disablePatternSlots()) return;

		IPanelHandler patternPanel = syncManager.syncedPanel("pattern_panel", true,
			(manager, handler) -> createPatternWindow2(manager));

		builder.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>().onMousePressed(mouseButton -> {
			patternPanel.openPanel();
			return patternPanel.isPanelOpen();
		})
			.background(GTGuiTextures.BUTTON_STANDARD, GTGuiTextures.OVERLAY_BUTTON_PLUS_LARGE)
			.tooltip(t -> t.addLine(
				IKey.str(LangManager.translateToLocalFormatted("programmable_hatches.gt.pattern"))))
			.size(16, 16)
			.pos(getGUIWidth() - 18 - 3, 5 + 16 + 2 + 16 + 2));
	}

	// ===================== MUI2 drag-handle support =====================
	// A drag handle that protrudes OUTSIDE the panel (like the glove tab here) cannot use MUI2's
	// stock DragHandle / DraggablePanelWrapper: ModularPanel.onMousePressed applies the hovered
	// widget's OWN matrix before onDragStart runs, so DraggablePanelWrapper.onDragStart's
	// context.transformX(0,0) resolves to the HANDLE's origin instead of the panel's. The drag
	// then anchors to the handle and the window jumps by the handle's offset (~WIDTH px) the
	// instant it is grabbed (and the render matrix is offset to match).
	// To avoid all of that, this forwarder does NOT use the movingArea / drawMovingState render
	// path. It repositions the panel directly every frame in onDrag using absolute mouse coords
	// (unaffected by any matrix) and the same resizer math MUI2 uses at drag-end. The panel is
	// never disabled, so it just renders normally at its live-updated position.
	// NOTE: reproduces engine behaviour and can't be compiled/tested here - verify feel in game.

	/** Forwards a drag to the given panel by repositioning it live each frame (no matrix offset). */
	static final class PanelDragForwarder implements com.cleanroommc.modularui.api.widget.IDraggable {

		private final com.cleanroommc.modularui.screen.ModularPanel panel;
		private int grabX, grabY;
		private boolean moving;

		PanelDragForwarder(com.cleanroommc.modularui.screen.ModularPanel panel) {
			this.panel = panel;
		}

		@Override
		public boolean onDragStart(int button) {
			if (button != 0) {
				return false;
			}
			// record the cursor's offset from the panel's top-left (absolute coords).
			Area a = this.panel.getArea();
			this.grabX = this.panel.getContext().getAbsMouseX() - a.x;
			this.grabY = this.panel.getContext().getAbsMouseY() - a.y;
			return true;
		}

		@Override
		public void onDrag(int mouseButton, long timeSinceLastClick) {
			reposition();
		}

		@Override
		public void onDragEnd(boolean successful) {
			// already repositioned every frame during the drag; nothing to finalise.
		}

		private void reposition() {
			Area screen = this.panel.getScreen().getScreenArea();
			Area pa = this.panel.getArea();
			int targetX = this.panel.getContext().getAbsMouseX() - this.grabX;
			int targetY = this.panel.getContext().getAbsMouseY() - this.grabY;
			// convert the absolute target top-left into the 0..1 relative anchor the resizer
			// uses - same normalisation as MUI2's DraggablePanelWrapper#onDragEnd.
			float relX = (targetX - screen.x) / (float) Math.max(1, screen.width - pa.width);
			float relY = (targetY - screen.y) / (float) Math.max(1, screen.height - pa.height);
			this.panel.resizer().resetPosition();
			this.panel.resizer().relativeToScreen();
			this.panel.resizer().topRelAnchor(relY, relY).leftRelAnchor(relX, relX);
			this.panel.scheduleResize();
		}

		@Override
		public void drawMovingState(com.cleanroommc.modularui.screen.viewport.ModularGuiContext context,
			float partialTicks) {
			// panel is not disabled during the drag, so it draws itself at its repositioned
			// location; there is nothing extra to draw here.
		}

		@Override
		public Area getMovingArea() {
			return this.panel.getArea();
		}

		@Override
		public boolean isMoving() {
			return this.moving;
		}

		@Override
		public void setMoving(boolean moving) {
			// intentionally do NOT disable the panel (no setEnabled(false)); we reposition it
			// live and let it render normally, which is what avoids the drag-handle offset.
			this.moving = moving;
		}

		@Override
		public void transform(com.cleanroommc.modularui.api.layout.IViewportStack stack) {
			// no-op: the panel renders at its own (repositioned) area, so no extra transform.
		}
	}

	/** A widget that forwards drags to its ModularPanel via PanelDragForwarder. */
	static final class DragTab extends com.cleanroommc.modularui.widget.Widget<DragTab>
		implements com.cleanroommc.modularui.api.widget.IDraggable, com.cleanroommc.modularui.api.layout.IViewport {

		private com.cleanroommc.modularui.api.widget.IDraggable forwarder;

		@Override
		public void onInit() {
			com.cleanroommc.modularui.api.widget.IWidget p = getParent();
			while (p != null && !(p instanceof com.cleanroommc.modularui.screen.ModularPanel)) {
				p = p.getParent();
			}
			if (p instanceof com.cleanroommc.modularui.screen.ModularPanel panel && panel.isDraggable()) {
				this.forwarder = new PanelDragForwarder(panel);
			}
		}

		@Override
		public boolean onDragStart(int button) {
			return this.forwarder != null && this.forwarder.onDragStart(button);
		}

		@Override
		public void onDragEnd(boolean successful) {
			if (this.forwarder != null) {
				this.forwarder.onDragEnd(successful);
			}
		}

		@Override
		public void onDrag(int mouseButton, long timeSinceLastClick) {
			if (this.forwarder != null) {
				this.forwarder.onDrag(mouseButton, timeSinceLastClick);
			}
		}

		@Override
		public void drawMovingState(com.cleanroommc.modularui.screen.viewport.ModularGuiContext context,
			float partialTicks) {
			if (this.forwarder != null) {
				this.forwarder.drawMovingState(context, partialTicks);
			}
		}

		@Override
		public Area getMovingArea() {
			return this.forwarder != null ? this.forwarder.getMovingArea() : null;
		}

		@Override
		public boolean isMoving() {
			return this.forwarder != null && this.forwarder.isMoving();
		}

		@Override
		public void setMoving(boolean moving) {
			if (this.forwarder != null) {
				this.forwarder.setMoving(moving);
			}
		}

		@Override
		public void transform(com.cleanroommc.modularui.api.layout.IViewportStack stack) {
			super.transform(stack);
		}
	}

	/**
	 * A {@link com.cleanroommc.modularui.widgets.TextWidget} used purely as a visual overlay.
	 * It never takes part in hit-testing (isInside always false), so when it is layered on top
	 * of another widget (e.g. a slot) it does not appear in the hovered-widget list and cannot
	 * swallow that widget's click/drag/release. Without this, clicking a slot under the label
	 * was treated as a click "outside" the slot and threw the held item out. It still renders
	 * normally - drawing is gated by canBeSeen (scissor area), which does not use isInside.
	 */
	static final class NonInteractiveText extends com.cleanroommc.modularui.widgets.TextWidget<NonInteractiveText> {

		NonInteractiveText(IKey key) {
			super(key);
		}

		@Override
		public boolean isInside(com.cleanroommc.modularui.api.layout.IViewportStack stack, int mx, int my, boolean absolute) {
			return false;
		}
	}

	protected ModularPanel createPatternWindow2(PanelSyncManager syncManager) {
		final int WIDTH = 18 * 4 + 6;     // page content width (4 slots wide)
		final int HEIGHT = 18 * 9 + 6;
		// Panel is content-width only; the right-side tabs protrude past its right edge.
		// (Widening the panel area to wrap the tabs was rejected: the extra area is visually
		// transparent but still part of the panel, so clicking that empty corner triggered a
		// "ghost" window drag. So the area stays at content size.)
		// Consequence of a content-width panel: a protruding child sits OUTSIDE the panel
		// area, so NEI fills that strip and the panel's built-in drag does not cover it.
		final int PANEL_W = WIDTH;
		final int TAB_W = 32;             // GuiTextures.TAB_RIGHT width (drag-tab size)
		// open the popup docked to the right edge of the main panel (top-aligned) instead
		// of the default centered position. The main panel's screen area is only known at
		// open time, so the position is set in onOpen (before super lays the panel out).
		ModularPanel builder = new ModularPanel("pattern_window") {
			@Override
			public void onOpen(com.cleanroommc.modularui.screen.ModularScreen screen) {
				Area main = screen.getMainPanel().getArea();
				// left edge flush against the main panel's right edge; tops aligned.
				// left()/top() override the center() set in the ModularPanel constructor.
				this.left(main.x() + main.w()).top(main.y());
				super.onOpen(screen);
			}
		};
		builder.size(PANEL_W, HEIGHT);

		com.cleanroommc.modularui.api.drawable.IDrawable tab1 = new com.cleanroommc.modularui.drawable.ItemDrawable(
			Api.INSTANCE.definitions()
				.items()
				.encodedPattern()
				.maybeStack(1)
				.get()).asIcon()
					.size(18, 18);
		com.cleanroommc.modularui.api.drawable.IDrawable tab2 = GTGuiTextures.OVERLAY_BUTTON_BATCH_MODE_OFF.asIcon()
			.size(18, 18);
		com.cleanroommc.modularui.api.drawable.IDrawable tab3 = GTGuiTextures.OVERLAY_BUTTON_BATCH_MODE_ON.asIcon()
			.size(18, 18);
		// Icon for the drag tab: GT++ (miscutils) heat-protection bauble ("insulated gloves").
		// The registry name really does include the trailing ".name" - that's a GT++ quirk,
		// not the lang-key suffix - so it is kept verbatim. Falls back to a plus overlay if
		// the item isn't found, so it never crashes.
		net.minecraft.item.Item gloveItem = cpw.mods.fml.common.registry.GameRegistry
			.findItem("miscutils", "GTPP.bauble.fireprotection.0.name");
		com.cleanroommc.modularui.api.drawable.IDrawable dragIcon = gloveItem != null
			? new com.cleanroommc.modularui.drawable.ItemDrawable(new ItemStack(gloveItem, 1, 0)).asIcon().size(18, 18)
			: GTGuiTextures.OVERLAY_BUTTON_PLUS_LARGE.asIcon().size(18, 18);

		PagedWidget.Controller tabController = new PagedWidget.Controller();

		ParentWidget<?> page1 = new ParentWidget<>().coverChildren()
			.name("patterns");
		ParentWidget<?> page2 = new ParentWidget<>().coverChildren()
			.name("individual_multiplier");
		ParentWidget<?> page3 = new ParentWidget<>().coverChildren()
			.name("batch_multiplier");

		// ---- page 3: batch multiplier op. ----
		// These buttons mutate multiplier[] (saved to NBT), so the mutation must run
		// server-side. Route each click through an InteractionSyncHandler (the click is
		// synced to the server, where the action runs), mirroring the legacy MUI1
		// com.cleanroommc.modularui.widgets.ButtonWidget.setOnClick behaviour which auto-synced clicks to the server.
		page3.child(makeBatchButton(syncManager, "batch_x2", () -> {
			for (int i = 0; i < 36; i++) {
				multiplier[i] *= 2;
				multiplier[i] = Math.max(multiplier[i], 1);
			}
			refresh();
		}).pos(3, 3)
			.tooltip(t -> t.addLine(IKey.str("x2"))));
		page3.child(IKey.str("x2")
			.asWidget()
			.pos(3 + 3, 3));
		page3.child(makeBatchButton(syncManager, "batch_set1", () -> {
			for (int i = 0; i < 36; i++) multiplier[i] = 1;
			refresh();
		}).pos(3 + 16, 3)
			.tooltip(t -> t.addLine(IKey.str("=1"))));
		page3.child(IKey.str("=1")
			.asWidget()
			.pos(3 + 3 + 16, 3));
		page3.child(makeBatchButton(syncManager, "batch_xn", () -> {
			for (int i = 0; i < 36; i++) {
				multiplier[i] *= n;
				multiplier[i] = Math.max(multiplier[i], 1);
			}
			refresh();
		}).pos(3, 3 + 32)
			.tooltip(t -> t.addLine(IKey.str("xN"))));
		page3.child(IKey.dynamic(() -> "x" + n)
			.asWidget()
			.pos(3 + 3, 3 + 32));
		page3.child(makeBatchButton(syncManager, "batch_setn", () -> {
			for (int i = 0; i < 36; i++) multiplier[i] = n;
			refresh();
		}).pos(3 + 16, 3 + 32)
			.tooltip(t -> t.addLine(IKey.str("=N"))));
		page3.child(IKey.dynamic(() -> "=" + n)
			.asWidget()
			.pos(3 + 3 + 16, 3 + 32));

		com.cleanroommc.modularui.value.sync.IntSyncValue nValue = new com.cleanroommc.modularui.value.sync.IntSyncValue(
			() -> n, s -> {
				n = s;
				refresh();
			}).allowC2S();
		page3.child(new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget().value(nValue)
			.formatAsInteger(true)
			.numbersInt(Integer.MIN_VALUE, Integer.MAX_VALUE)
			.setTextColor(com.cleanroommc.modularui.utils.Color.WHITE.main)
			.tooltip(t -> t.addLine(IKey.str("N=")))
			.size(60, 18)
			.pos(3, 3 + 32 + 18)
			.background(GTGuiTextures.BACKGROUND_TEXT_FIELD));

		// refund button, placed in the empty lower area of the batch op. page
		page3.child(createRefundButton2(syncManager).pos(3, 3 + 32 + 18 + 18 + 4));
		page3.child(IKey.str("Refund")
			.asWidget()
			.pos(3 + 18, 3 + 32 + 18 + 18 + 4 + 4));

		// shared handler: use one handler for pattern + display slots so shift-clicking
		// a pattern doesn't transfer it between pattern slots instead of to player inv.
		// MUI2 ItemStackHandler(ItemStack[]) is backed by Arrays.asList(pattern), so
		// writes propagate directly to the pattern[] array (same as the legacy handler).
		ItemStackHandler shared_handler = new ItemStackHandler(pattern);

		// register the slot group before the slots reference it (rowSize 4 == grid width)
		// rowSize 4 (grid width); shift-click priority -1 so shift-clicking from the player
		// inventory targets other slot groups before these pattern slots (matches the legacy
		// MUI1 setShiftClickPriority(-1) behaviour).
		syncManager.registerSlotGroup("pattern_inv", 4, -1);

		for (int i = 0; i < 36; i++) {
			final int ii = i;

			// ---- page 2: display-only slot + per-slot multiplier field ----
			// GT's PatternSlot renders a pattern's output in place of the pattern and draws the
			// amount exactly once. Substituting by hand via ItemEncodedPattern.getOutput() draws it
			// twice when the output is a fluid: that stack is a GT Fluid Display item carrying the
			// amount both in NBT (mFluidDisplayAmount -> "144L" bottom-left, drawn by
			// FluidDisplayStackRenderer) and in stackSize (-> "144" bottom-right, drawn by MUI2's
			// ItemSlot). PatternSlot takes getItemStackForNEI(0), which zeroes both, then draws the
			// count itself. Item outputs were never affected - they carry no mFluidDisplayAmount.
			// Fixes issue #329.
			page2.child(new PatternSlot().slot(new ModularSlot(shared_handler, i).accessibility(false, false))
				.pos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
				.background(GTGuiTextures.SLOT_ITEM_STANDARD));

			com.cleanroommc.modularui.value.sync.IntSyncValue mulValue = new com.cleanroommc.modularui.value.sync.IntSyncValue(
				() -> multiplier[ii], s -> {
					multiplier[ii] = s;
					refresh();
				}).allowC2S();
			page2.child(new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget().value(mulValue)
				.formatAsInteger(true)
				.numbersInt(Integer.MIN_VALUE, Integer.MAX_VALUE)
				.setMaxLength(999)
				.setTextColor(com.cleanroommc.modularui.utils.Color.RED.main)
				.pos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
				.size(18, 18));

			// ---- page 1: interactive pattern slot + multiplier text overlay ----
			// Same output-rendering fix as page 2, see the comment there (issue #329).
			page1.child(new PatternSlot().slot(new ModularSlot(shared_handler, i).slotGroup("pattern_inv")
				.filter(itemStack -> itemStack.getItem() instanceof ICraftingPatternItem)
				.changeListener((newItem, onlyAmountChanged, client, init) -> onPatternChange()))
				.pos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
				.background(GTGuiTextures.SLOT_ITEM_STANDARD));

			// Multiplier label drawn on top of the slot. It must NOT take part in hit-testing:
			// layered over the slot, a normal TextWidget sits above the slot in the hovered
			// list and swallows the slot's click/release, so a click on the slot is treated as
			// a drop "outside" and throws the held item out. NonInteractiveText returns false
			// from isInside, so it is skipped in hit-testing (clicks/drags reach the slot) while
			// still rendering the number.
			page1.child(new NonInteractiveText(IKey.dynamic(() -> {
				String s = multiplier[ii] == 1 ? "" : (ps(multiplier[ii]) + "");
				if (pattern[ii] == null) return "§7" + s;
				return s;
			}))
				.pos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
				.size(18, 18));
		}

		// Invisible backing behind the (protruding) tab strip, marked as an NEI/recipe-viewer
		// exclusion area. The strip sits outside the panel's own area, so the panel's auto
		// NEI exclusion doesn't cover it; this widget does. It draws nothing and clicks pass
		// through to the tabs rendered on top of it.
		builder.child(new com.cleanroommc.modularui.widget.Widget<>()
			.pos(WIDTH - 3, -1)
			.size(TAB_W, 28 * 4)
			.excludeAreaInRecipeViewer());

		builder.child(new com.cleanroommc.modularui.widgets.layout.Column().coverChildren()
			.pos(WIDTH - 3, -1)
			// First tab slot = GT++ heat-protection glove, used as a drag handle. It is a
			// DragTab (forwards the drag to the panel by repositioning it live each frame, so
			// no jump even though the tab protrudes outside the panel area), NOT a PageButton,
			// so it moves the window and does not switch pages.
			.child(new DragTab()
				.background(GuiTextures.TAB_RIGHT.get(-1, false), dragIcon)
				.size(TAB_W, 28)
				.tooltip(t -> t.addLine(IKey.str("Hold to drag"))))
			.child(new PageButton(0, tabController).tab(GuiTextures.TAB_RIGHT, 0)
				.overlay(tab1)
				.tooltip(t -> t.addLine(IKey.str("Patterns"))))
			.child(new PageButton(1, tabController).tab(GuiTextures.TAB_RIGHT, 0)
				.overlay(tab2)
				.tooltip(t -> t.addLine(IKey.str("Individual Multiplier Op."))))
			.child(new PageButton(2, tabController).tab(GuiTextures.TAB_RIGHT, 0)
				.overlay(tab3)
				.tooltip(t -> t.addLine(IKey.str("Batch Multiplier Op.")))));

		builder.child(new PagedWidget<>().controller(tabController)
			.pos(0, 0)
			.size(WIDTH, HEIGHT)
			.addPage(page1)
			.addPage(page2)
			.addPage(page3));

		return builder;
	}
	// =================== MUI2 end ===================

	boolean normalopt;
@Override
public ExConfig initExConfig() {
	ExConfig x=super.initExConfig();
	x.reg(0, 1, ExConfigEntry.create(
			() -> restrictToInt, 
				(s) -> {restrictToInt = s;},
				"programmable_hatches.gt.restrictToInt.0",
				"programmable_hatches.gt.restrictToInt.1"
				));
	x.reg(1, 1, ExConfigEntry.create(
			() -> allowopt, 
			(s) -> {allowopt = s;},
			"programmable_hatches.gt.allowopt.0",
			"programmable_hatches.gt.allowopt.1"
			));	
	
	x.reg(3, 1, ExConfigEntry.create(
			() -> normalopt, 
			(s) -> {normalopt = s;},
			"programmable_hatches.gt.normalopt.0",
			"programmable_hatches.gt.normalopt.1"
			));
	
	
	return x;
}
	public boolean disablePatternSlots(){return false;}
}