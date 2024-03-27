package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;


import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import reobf.proghatches.lang.LangManager;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

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
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.helpers.ICustomNameObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.extensions.ArrayExt;

public class PatternDualInputHatch extends BufferedDualInputHatch
    implements ICraftingProvider, IGridProxyable, ICustomNameObject, IInterfaceViewable, IPowerChannelState {

    public PatternDualInputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
        boolean mMultiFluid, int bufferNum, boolean fluid) {
        super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {

        return new ITexture[] { aBaseTexture, TextureFactory.of(BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER) };

    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(BlockIcons.OVERLAY_ME_CRAFTING_INPUT_BUFFER) };

    }

    public PatternDualInputHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, int bufferNum,
        boolean fluid, String... optional) {
        super(
            id,
            name,
            nameRegional,
            tier,
            mMultiFluid,
            bufferNum,
            (optional.length > 0 ? optional
                : reobf.proghatches.main.Config.get("PDIH", ImmutableMap.of(
                		"bufferNum",bufferNum,
                		"cap",format.format((int) (4000 * Math.pow(4, tier) / (mMultiFluid ? 4 : 1))),
                		"mMultiFluid",mMultiFluid,
                		"slots", Math.min(16, (1 + tier) * (tier + 1)),
                		"stacksize",(int) (64 * Math.pow(2, Math.max(tier - 3, 0)))
                		))
                	
                	/*defaultObj(

                    ArrayExt.of(
                        "Item/Fluid Input for Multiblocks",
                        "Contents are always separated with other bus/hatch",
                        "Programming Cover function integrated",
                        "Blocking mode is always on",
                        "If all pattern inputs cannot be push in one single try, it won't be pushed at all.",
                        "Buffer: " + bufferNum,
                        "For each buffer:",
                        "Capacity: " + format.format((int) (4000 * Math.pow(4, tier)) / (mMultiFluid ? 4 : 1))
                            + "L"
                            + (mMultiFluid ? " x4 types of fluid" : ""),
                        Math.min(16, (1 + tier) * (tier + 1)) + "Slots",
                        "Slot maximum stacksize:" + (int) (64 * Math.pow(2, Math.max(tier - 3, 0))),
                        LangManager.translateToLocal("programmable_hatches.addedby")),
                    ArrayExt.of("多方块机器的物品/流体输入", "总是与其它输入仓/输入总线隔离"

                        ,
                        "自带编程覆盖板功能",
                        "阻挡模式不可关闭",
                        "样板所有原料无法单次全部输入时,将拒绝此样板的输入",
                        "缓冲数量: " + bufferNum,
                        "缓冲容量: " + format.format((int) (4000 * Math.pow(4, tier) / (mMultiFluid ? 4 : 1)))
                            + "L"
                            + (mMultiFluid ? " x4种流体" : ""),
                        Math.min(16, (1 + tier) * (tier + 1)) + "格",
                        "每格堆叠限制:" + (int) (64 * Math.pow(2, Math.max(tier - 3, 0))),
                        LangManager.translateToLocal("programmable_hatches.addedby")

                    ))*/
                
            		
            		
            		));
        this.supportFluids = fluid;
    }

    ItemStack[] pattern = new ItemStack[36];
   
    
    ButtonWidget createRefundButton(IWidgetBuilder<?> builder) {
    	
    Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {
    	
   
        	PatternDualInputHatch.this.dirty=true;
        	try {
				PatternDualInputHatch.this.refundAll();
			} catch (Exception e) {
				
				//e.printStackTrace();
			}
    })
    		.setPlayClickSound(true)
             .setBackground(GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_EXPORT)
       
     
        .addTooltips(ImmutableList.of("Return all internally stored items back to AE"))
       
        .setPos(new Pos2d(getGUIWidth()-18-3,5+16+2))
        .setSize(16, 16);
    return (ButtonWidget) button;
} 
       MachineSource requestSource;
    private BaseActionSource getRequest() {
 
	if (requestSource == null) requestSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
    return requestSource;
}
    private void refundAll() throws Exception {
    	markDirty();
    	BaseActionSource src = getRequest();
             IMEMonitor<IAEItemStack> sg = getProxy().getStorage()
                 .getItemInventory();
          abstract class Inv{abstract ItemStack[] geti();abstract FluidTank[] getf();  }
          Consumer<Inv> consumer= inv->{
        	  try{
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
             for (FluidTank fluidStack : inv.getf()) {
                 if (fluidStack == null || fluidStack.getFluidAmount() == 0) continue;
                 IAEFluidStack rest = Platform.poweredInsert(
                		 getProxy().getEnergy(),
                     fsg,
                     AEApi.instance()
                         .storage()
                         .createFluidStack(fluidStack.getFluid()),
                     src);
                 fluidStack.setFluid(Optional.ofNullable(rest).map(IAEFluidStack::getFluidStack).orElse(null));
             };}
             catch(Exception e){throw new RuntimeException(e);}
        	  };
        	  
        	  inv0.stream().map(s->new Inv() {
				@Override
				ItemStack[] geti() {
					return s.mStoredItemInternal;
				}
				
				@Override
				FluidTank[] getf() {
					return s.mStoredFluidInternal;
				}
			}).forEach(consumer);
        	  ;
        	  consumer.accept(new Inv() {
				
				@Override
				ItemStack[] geti() {
					
					return mInventory;
				}
				
				@Override
				FluidTank[] getf() {
					
					return mStoredFluid;
				}
			});
        	  
         }
	

	@Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        buildContext.addSyncedWindow(88, this::createPatternWindow);
       
        builder.widget(createRefundButton(builder));
        builder.widget(
            new ButtonWidget().setOnClick(
                (clickData, widget) -> {
                    if (widget.getContext()
                        .isClient() == false)
                        widget.getContext()
                            .openSyncedWindow(88);
                })
                .setPlayClickSound(true)
                .setBackground(GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_PLUS_LARGE)
                .addTooltips(
                    ImmutableList.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.pattern")))
                .setSize(16, 16)
              //  .setPos(10 + 16 * 9, 3 + 16 * 2)
                .setPos(new Pos2d(getGUIWidth()-18-3,5+16+2+16+2))
        );

        super.addUIWidgets(builder, buildContext);
    }

    protected ModularWindow createPatternWindow(final EntityPlayer player) {
        final int WIDTH = 18 * 4 + 6;
        final int HEIGHT = 18 * 9 + 6;
        final int PARENT_WIDTH = getGUIWidth();
        final int PARENT_HEIGHT = getGUIHeight();

        ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
        builder.setBackground(GT_UITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
        builder.setGuiTint(getGUIColorization());
        builder.setDraggable(true);
        builder.setPos(
            (a, b) -> new Pos2d(
                PARENT_WIDTH + b.getPos()
                    .getX(),
                PARENT_HEIGHT*0 + b.getPos()
                    .getY() ));
        for (int i = 0; i < 36; i++) {
            BaseSlot bs;
            builder.widget(new SlotWidget(bs = new BaseSlot(new MappingItemHandler(pattern, 0, 36), i)

            ) {

                @Override
                protected ItemStack getItemStackForRendering(Slot slotIn) {
                    ItemStack stack = slotIn.getStack();
                    if (stack == null || !(stack.getItem() instanceof ItemEncodedPattern)) {
                        return stack;
                    }
                    ItemStack output = ((ItemEncodedPattern) stack.getItem()).getOutput(stack);
                    return output != null ? output : stack;

                }
            }.setFilter(itemStack -> itemStack.getItem() instanceof ICraftingPatternItem)
                .setChangeListener(() -> { onPatternChange(bs.getSlotIndex(), bs.getStack()); })
                .setPos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
                .setBackground(getGUITextureSet().getItemSlot(), GT_UITextures.OVERLAY_SLOT_PATTERN_ME));

        }

        return builder.build();
    }

    boolean needPatternSync;

    private void onPatternChange(int index, ItemStack newItem) {
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

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new PatternDualInputHatch(
            mName,
            mTier,
            mDescriptionArray,
            mTextures,
            mMultiFluid,
            bufferNum,
            this.supportFluids);

    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        NBTTagCompound tag = aNBT.getCompoundTag("patternSlots");
        if (tag != null) for (int i = 0; i < pattern.length; i++) {
            pattern[i] = Optional.ofNullable(tag.getCompoundTag("i" + i))
                .map(ItemStack::loadItemStackFromNBT)
                .orElse(null);
        }
        Optional.ofNullable(customName)
            .ifPresent(s -> aNBT.setString("customName", s));
        getProxy().readFromNBT(aNBT);
        super.loadNBTData(aNBT);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        NBTTagCompound tag = new NBTTagCompound();// aNBT.getCompoundTag("patternSlots");

        for (int i = 0; i < pattern.length; i++) {
            final int ii = i;
            Optional.ofNullable(pattern[i])
                .map(s -> s.writeToNBT(new NBTTagCompound()))
                .ifPresent(s -> tag.setTag("i" + ii, s));
        }
        aNBT.setTag("patternSlots", tag);
        Optional.ofNullable(aNBT.getTag("customName"))
            .map(NBTBase::toString)
            .ifPresent(s -> customName = s);
        getProxy().writeToNBT(aNBT);
        super.saveNBTData(aNBT);
    }

    boolean supportFluids = true;

    private void clearInv() {

        for (int i = 0; i < 16; i++) mInventory[i] = null;
        for (int i = 0; i < 4; i++) mStoredFluid[i].setFluid(null);;

    }

    private boolean postMEPatternChange() {
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
            if (needPatternSync && aTimer>lastSync+100) {
                needPatternSync = !postMEPatternChange();
                lastSync=aTimer;
            }
            if (aTimer % 20 == 0) {
                getBaseMetaTileEntity().setActive(isActive());
            }
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (!isActive()) return false;  
        if (!isEmpty()) return false;
        if (!supportFluids) {
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
                if (fluids > 4) {
                    clearInv();
                    return false;
                }
                mStoredFluid[i].setFluid(ItemFluidPacket.getFluidStack(itemStack));
            } else {
                items++;
                if (items > 16) {
                    clearInv();
                    return false;
                }
                mInventory[i] = itemStack;

            }
        }
        dirty=true;
        classify();

        justHadNewItems = true;
        return true;
    }

    private boolean isEmpty() {
        for (ItemStack is : mInventory) {
            if (is != null && is.stackSize > 0) return true;
        }
        for (FluidTank is : mStoredFluid) {
            if (is.getFluidAmount() > 0) return true;
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
                    GregTech_API.sBlockMachines,
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
    public int rows() {

        return 9;
    }

    @Override
    public int rowSize() {

        return 4;
    }

    @Override
    public IInventory getPatterns() {

        return null;
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
            name.append(getInventoryName());
        }

        /*
         * if (mInventory[SLOT_CIRCUIT] != null) {
         * name.append(" - ");
         * name.append(mInventory[SLOT_CIRCUIT].getItemDamage());
         * }
         * if (mInventory[SLOT_MANUAL_START] != null) {
         * name.append(" - ");
         * name.append(mInventory[SLOT_MANUAL_START].getDisplayName());
         * }
         */// TODO
        return name.toString();
    }

    @Override
    public TileEntity getTileEntity() {
        return (TileEntity) getBaseMetaTileEntity();
    }

    @Override
    public boolean shouldDisplay() {

        return false;
    }

    String customName;

    @Override
    public String getCustomName() {

        return customName;
    }

    @Override
    public boolean hasCustomName() {

        return customName != null;
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

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if (!isActive()) return;

        for (ItemStack slot : pattern) {
            if (slot == null) continue;
            ICraftingPatternDetails details = null;
            try {
                details = ((ICraftingPatternItem) slot.getItem()).getPatternForItem(
                    slot,
                    this.getBaseMetaTileEntity()
                        .getWorld());

            } catch (Exception e) {

            }
            if (details == null) {
                GT_Mod.GT_FML_LOGGER.warn(
                    "Found an invalid pattern at " + getBaseMetaTileEntity().getCoords()
                        + " in dim "
                        + getBaseMetaTileEntity().getWorld().provider.dimensionId);
                continue;
            }
            craftingTracker.addCraftingOption(this, details);
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

        getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

    }

    @Override
    public void onFacingChange() {
        updateValidGridProxySides();
    }

    @Override
    public boolean isPowered() {
        return getProxy() != null && getProxy().isPowered();
    }

}
