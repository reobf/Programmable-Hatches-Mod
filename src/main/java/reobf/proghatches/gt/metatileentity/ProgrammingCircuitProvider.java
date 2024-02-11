package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.ITEM_IN_SIGN;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_PIPE_IN;
import static reobf.proghatches.main.Config.defaultObj;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEItemStack;
import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.gui.modularui.GT_UIInfos;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.extensions.ArrayExt;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class ProgrammingCircuitProvider extends GT_MetaTileEntity_Hatch
    implements IAddUIWidgets, IPowerChannelState, ICraftingProvider, IGridProxyable {

    public ProgrammingCircuitProvider(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            aInvSlotCount,
            defaultObj(
                ArrayExt.of(
                    "Connect to AE network to provide Programming Circuit crafting",
                    "Each item costs 10AE",
                    "Put programmed circuit into inventory to specify the type of the programming circuit",
                    "Lens and moulds are acceptable as well",
                    "No, you cannot mark target item via NEI bookmark, you have to use REAL item"

                ),
                ArrayExt.of(
                    "连入AE网络以提供编程器芯片合成",
                    "消耗10AE以生成一个物品",
                    "放入编程电路以指定生成的编程器芯片类型",
                    "你也可以放入编程电路以外的物品",
                    "并不能从nei书签标记物品,你需要把目标物品留在提供器内")));
        Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
    }

    private void updateValidGridProxySides() {

        getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {

        return true;
    }

    @Override
    public void onFacingChange() {
        updateValidGridProxySides();
    }

    public ProgrammingCircuitProvider(String aName, int aTier, int aInvSlotCount, String[] aDescription,
        ITexture[][][] aTextures) {
        super(aName, aTier, aInvSlotCount, aDescription, aTextures);

    }

    // item returned in ae tick will not be recognized, delay to the next onPostTick() call
    ArrayList<ItemStack> toReturn = new ArrayList<>();

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        // this.getProxy().getEnergy().extractAEPower(amt, mode, usePowerMultiplier)
        try {
            if (ItemProgrammingCircuit.getCircuit(patternDetails.getOutputs()[0].getItemStack())
                .map(ItemStack::getItem)
                .orElse(null) == MyMod.progcircuit) {
                this.getBaseMetaTileEntity()
                    .doExplosion(2);
                return false;
            }

        } catch (Exception e) {}

        try {
            this.getProxy()
                .getEnergy()
                .extractAEPower(10, Actionable.MODULATE, PowerMultiplier.ONE);
        } catch (GridAccessException e) {

        }
        ItemStack circuitItem = (patternDetails.getOutput(
            table,
            this.getBaseMetaTileEntity()
                .getWorld()));
        toReturn.add((circuitItem));

        return true;
    }

    @Override
    public boolean isBusy() {

        return false;
    }

    int patternCheckTimer = (int) (Math.random() * 20);

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.getWorld().isRemote == false) lab: if (patternCheckTimer++ > 20) {// check every 1s
            patternCheckTimer = 0;
            if (snapshot != null) {
                for (int i = 0; i < snapshot.length; i++) {
                    if (ItemStack.areItemStacksEqual(snapshot[i], mInventory[i]) == false) {

                        doSnapshot();
                        postEvent();
                        break lab;
                    } ;

                }
            }

        }

        toReturn.forEach(
            s -> getStorageGrid().getItemInventory()
                .injectItems(
                    AEItemStack.create(s),
                    Actionable.MODULATE,
                    new MachineSource((IActionHost) getBaseMetaTileEntity())));
        toReturn.clear();

        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return getProxy().getNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {

        return AECableType.DENSE;
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {

        return true;
    }

    /*
     * @Override
     * public IInventory getPatterns() {
     * // TODO Auto-generated method stub
     * return null;
     * }
     */

    /*
     * @Override
     * public String getCustomName() {
     * // TODO Auto-generated method stub
     * return null;
     * }
     * @Override
     * public boolean hasCustomName() {
     * // TODO Auto-generated method stub
     * return false;
     * }
     * @Override
     * public void setCustomName(String name) {
     * // TODO Auto-generated method stub
     * }
     */
    AENetworkProxy gridProxy;

    @Override
    public AENetworkProxy getProxy() {

        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", ItemList.Hatch_CraftingInput_Bus_ME.get(1), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(
            getBaseMetaTileEntity().getWorld(),
            getBaseMetaTileEntity().getXCoord(),
            getBaseMetaTileEntity().getYCoord(),
            getBaseMetaTileEntity().getZCoord());
    }

    @Override
    public void gridChanged() {

    }

    private IStorageGrid getStorageGrid() {
        try {
            return this.getProxy()
                .getGrid()
                .getCache(IStorageGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }

    private final static Item encodedPattern = AEApi.instance()
        .definitions()
        .items()
        .encodedPattern()
        .maybeItem()
        .orNull();

    private boolean postEvent() {
        try {
            this.getProxy()
                .getGrid()
                .postEvent(new MENetworkCraftingPatternChange(this, getGridNode(ForgeDirection.UNKNOWN)));
            return true;
        } catch (GridAccessException ignored) {
            return false;
        }
    }

    // no need to save to NBT
    ItemStack[] snapshot;

    private void doSnapshot() {
        snapshot = mInventory.clone();
        for (int i = 0; i < snapshot.length; i++) {
            snapshot[i] = Optional.ofNullable(snapshot[i])
                .map(ItemStack::copy)
                .orElse(null);
        }
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        // IStorageGrid storage = getStorageGrid();
        // if (storage == null) return;
        /*
         * { ItemStack pattern = getPattern(new ItemStack[0],new ItemStack[]{new ItemStack(Blocks.bookshelf)});
         * ICraftingPatternItem patter = (ICraftingPatternItem) pattern.getItem();
         * craftingTracker.addCraftingOption(this, patter.getPatternForItem(pattern,
         * this.getBaseMetaTileEntity().getWorld()));
         * }
         * ItemStack pattern = getPattern(new ItemStack[]{new ItemStack(Blocks.anvil)},new ItemStack[]{new
         * ItemStack(Blocks.brick_stairs)});
         * ICraftingPatternItem patter = (ICraftingPatternItem) pattern.getItem();
         * craftingTracker.addCraftingOption(this, patter.getPatternForItem(pattern,
         * this.getBaseMetaTileEntity().getWorld()));
         */

        doSnapshot();
        if (this.mInventory[0] != null) {

            craftingTracker
                .addCraftingOption(this, new CircuitProviderPatternDetial(ItemProgrammingCircuit.wrap(mInventory[0])));

        }

    }

    /**
     * for ae2 crafting visualizer
     */
    public static class FakePattern extends Item implements ICraftingPatternItem {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
            p_77624_3_.add("Technical item, not for use.");
            super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
        }

        @Override
        public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
            try {
                ItemStack iss = ItemStack.loadItemStackFromNBT(is.getTagCompound());
                return new CircuitProviderPatternDetial(iss);
            } catch (Exception ew) {
                ew.printStackTrace();
                return new CircuitProviderPatternDetial(new ItemStack(Items.baked_potato));
            }
        }

    }

    public static class CircuitProviderPatternDetial implements ICraftingPatternDetails {

        private ItemStack out;

        public CircuitProviderPatternDetial(ItemStack o) {
            this.out = o;
        }

        @Override
        public ItemStack getPattern() {
            return Optional.of(new ItemStack(MyMod.fakepattern))
                .map(s -> {
                    s.stackTagCompound = out.writeToNBT(new NBTTagCompound());
                    return s;
                })
                .get();

        }

        @Override
        public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
            // glad I don't have to implement this
            throw new IllegalStateException("Only crafting recipes supported.");
        }

        @Override
        public boolean isCraftable() {

            return false;
        }

        /**
         * if return zero-sized input, ae2fc coremodhooks will crash
         * so use one zero-stacksized item to workaround
         */
        @Override
        public IAEItemStack[] getInputs() {

            return new IAEItemStack[] { AEApi.instance()
                .storage()
                .createItemStack(new ItemStack(Items.apple, 0)) };
        }

        @Override
        public IAEItemStack[] getCondensedInputs() {
            return getInputs();
        }

        @Override
        public IAEItemStack[] getCondensedOutputs() {

            return new IAEItemStack[] { AEApi.instance()
                .storage()
                .createItemStack(out) };
        }

        @Override
        public IAEItemStack[] getOutputs() {

            return getCondensedOutputs();
        }

        @Override
        public boolean canSubstitute() {

            return false;
        }

        @Override
        public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
            return out;
        }

        @Override
        public int getPriority() {

            return Integer.MAX_VALUE;
        }

        @Override
        public void setPriority(int priority) {

        }

    }

    @Override
    public boolean isPowered() {
        return getProxy() != null && getProxy().isPowered();
    }

    @Override
    public boolean isActive() {
        return getProxy() != null && getProxy().isActive();
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new ProgrammingCircuitProvider(mName, mTier, 1, mDescriptionArray, mTextures);
    }
@Override
public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
		int colorIndex, boolean aActive, boolean redstoneLevel) {

	return super.getTexture(aBaseMetaTileEntity, side, aFacing, colorIndex, aActive, redstoneLevel);



}
    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return GT_Mod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN), TextureFactory.of(ITEM_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN) };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return GT_Mod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN), TextureFactory.of(ITEM_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN) };
    }

    @Override
    public boolean useModularUI() {

        return true;
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        GT_UIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
        return true;
    }

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(this.mInventory, 0, 1);
        if (inventoryHandler == null) return;

        builder.widget(
            SlotGroup.ofItemHandler(inventoryHandler, 1)
                .startFromSlot(0)
                .endAtSlot(0)
                .background(new IDrawable[] { getGUITextureSet().getItemSlot() })
                .build()
                .setPos(3, 3));
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {

        super.saveNBTData(aNBT);

        int[] count = new int[1];
        toReturn.forEach(s -> aNBT.setTag("toReturn" + (count[0]++), s.writeToNBT(new NBTTagCompound())));
        getProxy().writeToNBT(aNBT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadNBTData(NBTTagCompound aNBT) {

        super.loadNBTData(aNBT);
        toReturn.clear();
        int[] count = new int[1];
        NBTTagCompound c;
        while ((c = (NBTTagCompound) aNBT.getTag("toReturn" + (count[0]++))) != null) {
            toReturn.add(ItemStack.loadItemStackFromNBT(c));
        }
        getProxy().readFromNBT(aNBT);;
    }

}
