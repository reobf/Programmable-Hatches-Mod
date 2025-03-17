package reobf.proghatches.gt.metatileentity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.IStructureElementChain;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget.IntegerSyncer;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.block.crafting.BlockAdvancedCraftingUnit;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.core.Api;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.IGTHatchAdder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.api.util.shutdown.SimpleShutDownReason;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.eucrafting.IInstantCompletable;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider.CircuitProviderPatternDetial;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProviderPrefabricated;
import reobf.proghatches.gt.metatileentity.ProviderChainer;
import reobf.proghatches.gt.metatileentity.util.ICircuitProvider;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class LargeProgrammingCircuitProvider extends MTEEnhancedMultiBlockBase<LargeProgrammingCircuitProvider>
    implements ISurvivalConstructable, IGridProxyable, ICraftingProvider, IInstantCompletable, ICircuitProvider,
    IInterfaceViewable, IPowerChannelState, IActionHost, IMultiplePatternPushable {

    public LargeProgrammingCircuitProvider(String aName) {
        super(aName);

    }

    public LargeProgrammingCircuitProvider(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));

    }

    @Override
    public void onFacingChange() {

        updateValidGridProxySides();
        super.onFacingChange();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection forgeDirection) {

        return AECableType.DENSE;
    }

    boolean forceUpdatePattern;

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        forceUpdatePattern = true;
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }

    public List<ICircuitProvider> providers = new ArrayList<>();
    private boolean chip;
    protected static final int CASING_INDEX = 49;
    private static final IStructureDefinition<LargeProgrammingCircuitProvider> STRUCTURE_DEFINITION;
    protected static final String STRUCTURE_PIECE_BASE = "base";
    protected static final String STRUCTURE_PIECE_LAYER = "layer";
    protected static final String STRUCTURE_PIECE_LAYER_HINT = "layerHint";
    protected static final String STRUCTURE_PIECE_TOP_HINT = "topHint";
    static {
        Function<IGTHatchAdder<? super LargeProgrammingCircuitProvider>, IHatchElement<LargeProgrammingCircuitProvider>> provider = s -> new IHatchElement<LargeProgrammingCircuitProvider>() {

            @Override
            public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
                return ImmutableList.of(
                    ProgrammingCircuitProvider.class,
                    ProviderChainer.class,
                    ProgrammingCircuitProviderPrefabricated.class);
            }

            @Override
            public IGTHatchAdder<? super LargeProgrammingCircuitProvider> adder() {

                return s;
            }

            @Override
            public String name() {

                return "providerAdder";
            }

            @Override
            public long count(LargeProgrammingCircuitProvider t) {

                return t.providers.size();
            }
        };
        IStructureElement<LargeProgrammingCircuitProvider> acc = StructureUtility
            .ofBlockAdder(LargeProgrammingCircuitProvider::addAccUnit, 1);
        IStructureElement<LargeProgrammingCircuitProvider> accHint = ofChain(
            ofBlock(
                Api.INSTANCE.definitions()
                    .blocks()
                    .craftingAccelerator()
                    .maybeBlock()
                    .get(),
                0),
            ofBlock(
                Api.INSTANCE.definitions()
                    .blocks()
                    .craftingAccelerator()
                    .maybeBlock()
                    .get(),
                1),
            ofBlock(
                Api.INSTANCE.definitions()
                    .blocks()
                    .craftingAccelerator()
                    .maybeBlock()
                    .get(),
                2),
            ofBlock(
                Api.INSTANCE.definitions()
                    .blocks()
                    .craftingAccelerator()
                    .maybeBlock()
                    .get(),
                3),
            ofBlock(
                Api.INSTANCE.definitions()
                    .blocks()
                    .craftingAccelerator64x()
                    .maybeBlock()
                    .get(),
                0),
            ofBlock(
                Api.INSTANCE.definitions()
                    .blocks()
                    .craftingAccelerator64x()
                    .maybeBlock()
                    .get(),
                1),
            ofBlock(
                Api.INSTANCE.definitions()
                    .blocks()
                    .craftingAccelerator64x()
                    .maybeBlock()
                    .get(),
                2),
            ofBlock(
                Api.INSTANCE.definitions()
                    .blocks()
                    .craftingAccelerator64x()
                    .maybeBlock()
                    .get(),
                3));

        IHatchElement<LargeProgrammingCircuitProvider> providerSide = provider
            .apply(LargeProgrammingCircuitProvider::addProvider);
        IHatchElement<LargeProgrammingCircuitProvider> providerTop = provider
            .apply(LargeProgrammingCircuitProvider::addProviderTop);

        STRUCTURE_DEFINITION = StructureDefinition.<LargeProgrammingCircuitProvider>builder()
            .addShape(STRUCTURE_PIECE_BASE, /* transpose */(new String[][] { { "bbb", "b~b", "bbb" }, }))
            .addShape(STRUCTURE_PIECE_LAYER, /* transpose */(new String[][] { { "lhl", "hxh", "lhl" }, }))
            .addShape(STRUCTURE_PIECE_LAYER_HINT, /* transpose */(new String[][] { { "lhl", "hXh", "lhl" }, }))
            .addShape(STRUCTURE_PIECE_TOP_HINT, /* transpose */(new String[][] { { "lll", "lhl", "lll" }, }))
            .addElement(
                'b',
                ofChain(
                    buildHatchAdder(LargeProgrammingCircuitProvider.class).atLeast(Energy, Maintenance)
                        .casingIndex(CASING_INDEX)
                        .dot(1)

                        .build(),
                    // ofBlock(GregTechAPI.sBlockCasings4, 1),
                    onElementPass(LargeProgrammingCircuitProvider::onCasingFound, ofBlock(GregTechAPI.sBlockCasings4, 1)

                    ))

            )
            .addElement('l', ofBlock(GregTechAPI.sBlockCasings4, 1))
            .addElement('x', (IStructureElementChain<LargeProgrammingCircuitProvider>) () -> {
                return buildHatchAdder(LargeProgrammingCircuitProvider.class).atLeast(providerTop)
                    .casingIndex(CASING_INDEX)
                    .dot(2)
                    .buildAndChain(
                        acc,
                        ofBlock(
                            Api.INSTANCE.definitions()
                                .blocks()
                                .fluix()
                                .maybeBlock()
                                .get(),
                            0),
                        StructureUtility.ofBlockAdder(
                            LargeProgrammingCircuitProvider::onTopCenterFound,
                            GregTechAPI.sBlockCasings4,
                            1))
                    .fallbacks();

            }

            )
            .addElement(
                'X',
                ofChain(
                    accHint,
                    ofBlock(
                        Api.INSTANCE.definitions()
                            .blocks()
                            .fluix()
                            .maybeBlock()
                            .get(),
                        0)))
            .addElement(
                'h',

                buildHatchAdder(LargeProgrammingCircuitProvider.class).atLeast(providerSide)
                    .casingIndex(CASING_INDEX)
                    .dot(2)
                    // .disallowOnly(ForgeDirection.UP,
                    // ForgeDirection.DOWN)
                    .buildAndChain(GregTechAPI.sBlockCasings4, 1)

            )

            .build();
    }
    int mCasing;

    protected void onCasingFound() {

        mCasing++;
    }

    boolean mTopLayerFound;

    protected void onTopLayerFound(boolean aIsCasing) {
        mTopLayerFound = true;
        if (aIsCasing) onCasingFound();
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new LargeProgrammingCircuitProvider(mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {
        if (side != facing) {
            return new ITexture[] { BlockIcons.getCasingTextureForId(CASING_INDEX) };
        }
        return active ? getTexturesActive(BlockIcons.getCasingTextureForId(CASING_INDEX))
            : getTexturesInactive(BlockIcons.getCasingTextureForId(CASING_INDEX));
    }

    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.builder()
            .setFromBlock(MyMod.iohub, BlockIOHub.magicNO_provider_active_overlay)
            .glow()
            .build() };
    }

    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(MyMod.iohub, BlockIOHub.magicNO_provider_overlay) };
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        /*
         * tt.addMachineType("Programming Circuit Provider") .addInfo(
         * "Controller block for the Large Programming Circuit Provider")
         * .addInfo(
         * "The correct height equals the slot number in the NEI recipe")
         * .addSeparator() .beginVariableStructureBlock(3, 3, 2, 12, 3, 3,
         * false) .addController("Front center") .addOtherStructurePart(
         * "Clean Stainless Steel Machine Casing", "7 x h - 5 (minimum)")
         * .addOtherStructurePart("Fluix Block",
         * "Center of each layer except the first and last one")
         * .addOtherStructurePart("Programming Circuit Provider",
         * "Any casing that is adjacent to Fluix Block") .addEnergyHatch(
         * "Any front layer casing", 1) .addMaintenanceHatch(
         * "Any front layer casing", 1) .toolTipFinisher("ProgrammableHatches");
         */
        Config.get(tt, "M_LPCP");

        return tt;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public boolean isRotationChangeAllowed() {
        return false;
    }

    @Override
    public IStructureDefinition<LargeProgrammingCircuitProvider> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    int mHeight;

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        // reset

        mHeight = 1;
        mTopLayerFound = false;
        mCasing = 0;
        totalAcc = 0;
        // check base
        if (!checkPiece(STRUCTURE_PIECE_BASE, 1, 1, 0)) return false;

        // check each layer
        while (mHeight < 12) {
            providerFoundThisLayer = false;
            if (!checkPiece(STRUCTURE_PIECE_LAYER, 1, 1, -mHeight)) {
                return false;
            }

            if (mTopLayerFound) {
                if (providerFoundThisLayer) {
                    return false;
                } // no providers allowed on top layer!
                break;
            }

            mHeight++;
        }

        // validate final invariants... (actual height is mHeight+1)
        // mCasing >= 7 * (mHeight + 1) - 5 && mHeight + 1 >= 3&&
        boolean succ = mTopLayerFound && mMaintenanceHatches.size() == 1;
        // if(succ){forceUpdatePattern=true ;}
        multiply = Math.min(multiply, totalAcc + 1);
        multiply = Math.max(multiply, 1);
        
        if(mEnergyHatches.size()==0)return false;
        
        return succ;
    }

    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    boolean providerFoundThisLayer;
    int totalAcc;

    static boolean addAccUnit(LargeProgrammingCircuitProvider t, Block block, int meta) {
        if (block.getClass() == BlockCraftingUnit.class || block.getClass() == BlockAdvancedCraftingUnit.class) {
            boolean advanced = block.getClass() == BlockAdvancedCraftingUnit.class;
            t.totalAcc += ((advanced ? 64 : 1) * (1 << (meta * 2))) / (advanced ? 1 : 4);
            // System.out.println(t.totalAcc);

            return true;
        }

        return false;
    }

    static boolean onTopCenterFound(LargeProgrammingCircuitProvider t, Block block, int meta) {
        if (block == GregTechAPI.sBlockCasings4 && meta == 1) {
            t.mTopLayerFound = true;
            return true;
        }

        return false;
    }

    static boolean addProvider(LargeProgrammingCircuitProvider t, IGregTechTileEntity aTileEntity, Short text) {
        return addProviderWithUpdater(t, aTileEntity, text, tt -> {
            tt.providerFoundThisLayer = true;
            return !tt.mTopLayerFound;
        });
    }

    static boolean addProviderTop(LargeProgrammingCircuitProvider t, IGregTechTileEntity aTileEntity, Short text) {
        return addProviderWithUpdater(t, aTileEntity, text, tt -> {
            tt.mTopLayerFound = true;
            return !tt.providerFoundThisLayer;
        });
    }

    static private <T extends MTEHatch & ICircuitProvider> boolean addProviderWithUpdater(
        LargeProgrammingCircuitProvider t, IGregTechTileEntity aTileEntity, Short text,
        Predicate<LargeProgrammingCircuitProvider> cb) {

        if (aTileEntity == null) return false;
        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
        if (aMetaTileEntity == null) return false;
        if (aMetaTileEntity instanceof ICircuitProvider) {
            aMetaTileEntity.getBaseMetaTileEntity()
                .setActive(true);
            @SuppressWarnings("unchecked")
            T hatch = (T) aMetaTileEntity;
            Optional.ofNullable(text)
                .ifPresent(hatch::updateTexture);
            hatch.updateCraftingIcon(t.getMachineCraftingIcon());

            if (!cb.test(t)) {
                return false;
            } ;
            if (hatch instanceof ProgrammingCircuitProvider) ((ProgrammingCircuitProvider) hatch).disable();
            t.providers.add(hatch);

            return true;
        }
        return false;

    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_BASE, stackSize, hintsOnly, 1, 1, 0);
        int tTotalHeight = Math.min(12, stackSize.stackSize + 2); // min 2
                                                                  // output
                                                                  // layer, so
                                                                  // at least
                                                                  // 1 + 2
                                                                  // height
        for (int i = 1; i < tTotalHeight - 1; i++) {
            buildPiece(STRUCTURE_PIECE_LAYER_HINT, stackSize, hintsOnly, 1, 1, -i);
        }
        buildPiece(STRUCTURE_PIECE_TOP_HINT, stackSize, hintsOnly, 1, 1, -(tTotalHeight - 1));
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        mHeight = 0;
        int built = survivialBuildPiece(STRUCTURE_PIECE_BASE, stackSize, 1, 1, 0, elementBudget, env, false, true);
        if (built >= 0) return built;
        int tTotalHeight = Math.min(12, stackSize.stackSize + 2); // min 2
                                                                  // output
                                                                  // layer, so
                                                                  // at least
                                                                  // 1 + 2
                                                                  // height
        for (int i = 1; i < tTotalHeight - 1; i++) {
            mHeight = i;
            built = survivialBuildPiece(
                STRUCTURE_PIECE_LAYER_HINT,
                stackSize,
                1,
                1,
                -i,
                elementBudget,
                env,
                false,
                true);
            if (built >= 0) return built;
        }
        mHeight = tTotalHeight - 1;
        return survivialBuildPiece(
            STRUCTURE_PIECE_TOP_HINT,
            stackSize,
            1,
            1,
            -(tTotalHeight - 1),

            elementBudget,
            env,
            false,
            true);
    }

    public long getConsumption() {

        return providers.size() * 256;

    }

    @Nonnull
    @Override
    public CheckRecipeResult checkProcessing() {

        long pw = getMaxInputPower();
        long cs = getConsumption();
        if (pw < cs) {
            return CheckRecipeResultRegistry.insufficientPower(cs);
        }
        mEfficiency = 10000;
        mMaxProgresstime = 100;

        // if(cs>Integer.MAX_VALUE){throw new RuntimeException();}
        mEUt = -((int) cs);
        return SimpleCheckRecipeResult.ofSuccess("proghatches.largepcp.running");
    }

    @Override
    public void clearHatches() {
        this.providers.clear();

        super.clearHatches();
    }

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
    public IGridNode getGridNode(ForgeDirection dir) {
        return getProxy().getNode();
    }

    @Override
    public void securityBreak() {

    }

    private void updateValidGridProxySides() {

        getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

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
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setBoolean("removeStorageCircuit", removeStorageCircuit);
        aNBT.setBoolean("chip", chip);
        aNBT.setInteger("multiply", multiply);
        getProxy().writeToNBT(aNBT);
        int[] count = new int[1];
        // toReturn.forEach(s -> aNBT.setTag("toReturn" + (count[0]++), s.writeToNBT(new NBTTagCompound())));
        aNBT.setTag("ret", this.writeList(this.ret));
        aNBT.setInteger("cacheState", cacheState.ordinal());
        count[0] = 0;
        patternCache.forEach(s -> aNBT.setTag("patternCache" + (count[0]++), s.writeToNBT(new NBTTagCompound())));
        super.saveNBTData(aNBT);
    }

    private NBTTagCompound writeItem(final IAEItemStack finalOutput2) {
        final NBTTagCompound out = new NBTTagCompound();

        if (finalOutput2 != null) {
            finalOutput2.writeToNBT(out);
        }

        return out;
    }

    private appeng.util.item.ItemList readList(final NBTTagList tag) {
        final appeng.util.item.ItemList out = new appeng.util.item.ItemList();

        if (tag == null) {
            return out;
        }

        for (int x = 0; x < tag.tagCount(); x++) {
            final IAEItemStack ais = AEItemStack.loadItemStackFromNBT(tag.getCompoundTagAt(x));
            if (ais != null) {
                out.add(ais);
            }
        }

        return out;
    }

    private NBTTagList writeList(final IItemList<IAEItemStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEItemStack ais : myList) {
            out.appendTag(this.writeItem(ais));
        }

        return out;
    }
@Override
public void setItemNBT(NBTTagCompound nbt) {
	if(chip)nbt.setBoolean("chip", chip);
	
}
    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        removeStorageCircuit = aNBT.getBoolean("removeStorageCircuit");
        chip = aNBT.getBoolean("chip");
        multiply = aNBT.getInteger("multiply");
        if (multiply <= 0) multiply = 1;
        getProxy().readFromNBT(aNBT);

        this.ret = this.readList((NBTTagList) aNBT.getTag("ret"));
        // toReturn.clear();
        patternCache.clear();
        int[] count = new int[1];
        NBTTagCompound c;
        /*
         * while ((c = (NBTTagCompound) aNBT.getTag("toReturn" + (count[0]++))) != null) {
         * toReturn.add(ItemStack.loadItemStackFromNBT(c));
         * }
         */
        count[0] = 0;
        while ((c = (NBTTagCompound) aNBT.getTag("patternCache" + (count[0]++))) != null) {
            patternCache.add(ItemStack.loadItemStackFromNBT(c));
        }

        cacheState = CacheState.values()[aNBT.getInteger("cacheState")];
        super.loadNBTData(aNBT);
    }

    // ArrayList<ItemStack> toReturn = new ArrayList<>();
    private appeng.util.item.ItemList ret = new appeng.util.item.ItemList();

    @SuppressWarnings({ "deprecation", "unchecked" })
    public static void shut(MTEMultiBlockBase thiz, String reason) {
        if (shut == null) {

            try {
                Class.forName("gregtech.api.util.shutdown.ShutDownReason");
                // 2.6.0+
                MyMod.LOG.info("Use ShutDownReason");
                // lazy-load class
                Class<?> c = Class.forName(
                    "reobf.proghatches.gt.metatileentity.multi.LargeProgrammingCircuitProvider$NewShutRunnable");
                shut = (BiConsumer<MTEMultiBlockBase, String>) c.getConstructors()[0].newInstance();
            } catch (Exception e) {
                // 2.5.1
                MyMod.LOG.info("ShutDownReason.class not found, use 0-arg stopMachine.");
                shut = (a, s) -> {
                    if (s == null) {
                        a.criticalStopMachine();
                        return;
                    }
                    a.stopMachine();
                };
            }

        }

        shut.accept(thiz, reason);
    }

    static public class NewShutRunnable implements BiConsumer<MTEMultiBlockBase, String> {

        public NewShutRunnable() {}

        // MTEMultiBlockBase thiz;
        @Override
        public void accept(MTEMultiBlockBase a, String reason) {
            if (reason == null) {
                a.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
                return;
            }
            a.stopMachine(new SimpleShutDownReason(reason/* "proghatch.access_loop" */, true));
        }

    }

    static public BiConsumer<MTEMultiBlockBase, String> shut;
    public int multiply = 1;

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (!getBaseMetaTileEntity().isActive()) return false;
        try {
            if (!(patternDetails instanceof ProgrammingCircuitProvider.CircuitProviderPatternDetial)) {
                return false;
            }
            if (ItemProgrammingCircuit.getCircuit(((CircuitProviderPatternDetial) patternDetails).out)
                .map(ItemStack::getItem)
                .orElse(null) == MyMod.progcircuit) {
                shut(this, null);
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
        // toReturn.add((circuitItem));
        ret.add(AEItemStack.create(circuitItem));
        return true;
    }

    @Override
    public boolean isBusy() {

        return false;
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

    final private int ran = (int) (Math.random() * 20);
    int lasthash;

    private static Method findFuzzyDamage;

    @SuppressWarnings("unchecked")
    private static Collection<IAEItemStack> findFuzzyDamage(IItemList thiz, final AEItemStack filter,
        final FuzzyMode fuzzy, final boolean ignoreMeta) {
        try {
            if (findFuzzyDamage == null) {

                findFuzzyDamage = appeng.util.item.ItemList.class
                    .getDeclaredMethod("findFuzzyDamage", AEItemStack.class, FuzzyMode.class, boolean.class);

                findFuzzyDamage.setAccessible(true);
            }

            return (Collection<IAEItemStack>) findFuzzyDamage.invoke(thiz, filter, fuzzy, ignoreMeta);
        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        returnItems();
        if (aBaseMetaTileEntity.getWorld().isRemote) return;
        
        
       if(mInventory[1]!=null){
    	   
    	   if(chip==false&&mInventory[1].getItem()==MyMod.chip){
    		   chip=true;
    		   mInventory[1].stackSize--;
    		   if( mInventory[1].stackSize==0) mInventory[1]=null;
    	   }
    	   
    	   
       }
        
        if (getBaseMetaTileEntity().isActive()) {
            if (removeStorageCircuit && (aTick % 50 == 0)) {
                try {
                    /*
                     * IItemList<IAEItemStack> list=new appeng.util.item.ItemList();
                     * this.getProxy().getStorage().getItemInventory()
                     * .getAvailableItems(list);
                     */
                    IItemList<IAEItemStack> list = this.getProxy()
                        .getStorage()
                        .getItemInventory()
                        .getStorageList();

                    // findFuzzyDamage(list,AEItemStack.create(new
                    // ItemStack(MyMod.progcircuit)),FuzzyMode.IGNORE_ALL,true)
                    list.forEach(s -> {
                        if (s.getItem() == MyMod.progcircuit) try {
                            this.getProxy()
                                .getStorage()
                                .getItemInventory()
                                .extractItems(
                                    s,
                                    Actionable.MODULATE,
                                    new MachineSource(LargeProgrammingCircuitProvider.this));
                        } catch (Exception e) {}
                    });;

                } catch (Exception e) {}
            }

            int hash = providers.hashCode();
            if (lasthash != hash || cacheState == CacheState.POWEROFF || cacheState == CacheState.CRASH) {
                cacheState = CacheState.UPDATED;
                forceUpdatePattern = true;
            }
            lasthash = hash;
            boolean any = false;
            if (cacheState.shouldCheck() && mMachine) {
                if (aTick % 20 == ran) {

                    for (ICircuitProvider p : providers) {
                        if (p.patternDirty()) {
                            any = true;
                            p.clearDirty();
                        }

                    }

                }
            }
            if (any || forceUpdatePattern) {
                forceUpdatePattern = false;
                cacheState = CacheState.DIRTY;
                postEvent();
            }
        } else {
            if (patternCache.size() > 0) postEvent();
            patternCache.clear();
            cacheState = CacheState.POWEROFF;

        }

    }

    public void returnItems() {
        ret.forEach(
            s -> getStorageGrid().getItemInventory()
                .injectItems(s, Actionable.MODULATE, new MachineSource((IActionHost) getBaseMetaTileEntity())));
        ret.clear();
        /*
         * toReturn.replaceAll(s -> Optional
         * .ofNullable(getStorageGrid().getItemInventory().injectItems(AEItemStack.create(s), Actionable.MODULATE,
         * )))
         * .filter(ss -> ss.getStackSize() <= 0).map(ss -> ss.getItemStack()).orElse(null));
         * toReturn.removeIf(Objects::isNull);
         */

    }

    List<ItemStack> patternCache = new ArrayList<>();

    enum CacheState {

        DIRTY(false),
        FRESHLY_UPDATED(true),
        UPDATED(true),
        CRASH(false),
        POWEROFF(false)

        ;

        boolean shouldCheck;

        CacheState(boolean shouldCheck) {
            this.shouldCheck = shouldCheck;
        }

        public boolean shouldCheck() {
            return shouldCheck;
        }

    }

    private boolean removeStorageCircuit;
    CacheState cacheState = CacheState.POWEROFF;

    private HashSet<Object> reusable = new HashSet<>();

    public void updateCache() {
        patternCache.clear();
        reusable.clear();// just in case
        if (!checkLoop(reusable)) {
            cacheState = CacheState.CRASH;

            shut(this, "proghatch.access_loop");

            reusable.clear();
            return;
        } ;

        reusable.clear();
        providers.forEach(s -> patternCache.addAll(s.getCircuit()));
    }

    private ItemStack mul(ItemStack s) {
        s = s.copy();
        s.stackSize = Math.max(multiply, 1);
        return s;
    }

    @MENetworkEventSubscribe
    public void powerChange(MENetworkPowerStatusChange w) {
        // System.out.println("xxxxxxxxxxx1");
        cacheState = CacheState.DIRTY;
    }

    @MENetworkEventSubscribe
    public void powerChange(MENetworkChannelChanged w) {
        // System.out.println("xxxxxxxxxxx2");
        cacheState = CacheState.DIRTY;
    }

    @MENetworkEventSubscribe
    public void powerChange(MENetworkChannelsChanged w) {
        // System.out.println("xxxxxxxxxxx3");
        cacheState = CacheState.DIRTY;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if (mStartUpCheck > 0) return;
        if (cacheState == CacheState.DIRTY) {
            updateCache();
            cacheState = CacheState.FRESHLY_UPDATED;
        }
        if (cacheState == CacheState.FRESHLY_UPDATED || cacheState == CacheState.UPDATED)
            if (isActive()) patternCache.forEach(
                s -> craftingTracker.addCraftingOption(
                    this,
                    multiply <= 1 ? new CircuitProviderPatternDetial(s) : new CircuitProviderPatternDetial(mul(s))));

    }

    public boolean checkLoop(HashSet<Object> blacklist) {
        for (ICircuitProvider p : providers) {
            if (!p.checkLoop(blacklist)) {
                return false;
            } ;
        }
        return blacklist.add(this);

    };

    @Override
    public void complete() {
        // returnItems();

    }

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

    @Override
    public void clearDirty() {
        if (cacheState == CacheState.FRESHLY_UPDATED) cacheState = CacheState.UPDATED;
    }

    @Override
    public boolean patternDirty() {

        return cacheState == CacheState.FRESHLY_UPDATED;
    }

    @Override
    public Collection<ItemStack> getCircuit() {

        return patternCache;
    }

    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {

        super.drawTexts(screenElements, inventorySlot);
        screenElements.setSpace(0);
        screenElements.setPos(0, 0);
        // make it look same on 2.7.2-
        // 2.7.2- set it to a non zero value
        screenElements.widget(TextWidget.dynamicString(() ->

        StatCollector.translateToLocalFormatted("proghatches.largepcp.eu", this.providers.size(), -this.mEUt)

        )
            .setDefaultColor(COLOR_TEXT_WHITE.get())
            .setEnabled(widget -> { return (getBaseMetaTileEntity().isAllowedToWork()); }));
    }

    ButtonWidget createParallelButton(IWidgetBuilder<?> builder, UIBuildContext buildContext) {

        Widget button = new ButtonWidget().setOnClick(
            (clickData, widget) -> {
                if (!widget.getContext()
                    .isClient())
                    widget.getContext()
                        .openSyncedWindow(987);
            })

            .setBackground(() -> {

                return new IDrawable[] { GTUITextures.BUTTON_STANDARD, GTUITextures.PICTURE_INFORMATION };

            })

            .addTooltip(StatCollector.translateToLocal("proghatches.largepcp.parallel"))

            .setTooltipShowUpDelay(TOOLTIP_DELAY)
            .setPos(getVoidingModeButtonPos())
            .setSize(16, 16);

        return (ButtonWidget) button;
    }

    ButtonWidget createRemoveCircuitButton(IWidgetBuilder<?> builder, UIBuildContext buildContext) {
        builder.widget(
            new FakeSyncWidget.BooleanSyncer(() -> this.removeStorageCircuit, s -> this.removeStorageCircuit = s));
        Widget button = new ButtonWidget()
            .setOnClick((clickData, widget) -> { removeStorageCircuit = !removeStorageCircuit; })

            .setPlayClickSoundResource(
                () -> isAllowedToWork() ? SoundResource.GUI_BUTTON_UP.resourceLocation
                    : SoundResource.GUI_BUTTON_DOWN.resourceLocation)
            .setBackground(() -> {
                if (removeStorageCircuit) {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED, GTUITextures.OVERLAY_BUTTON_CROSS };
                } else {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD, GTUITextures.OVERLAY_BUTTON_CROSS };
                }
            })

            .addTooltip(StatCollector.translateToLocal("proghatches.largepcp.remove_circuit"))

            .setTooltipShowUpDelay(TOOLTIP_DELAY)
            .setPos(getVoidingModeButtonPos().add(18, 0))
            .setSize(16, 16);

        return (ButtonWidget) button;
    }

    @Override
    public void addUIWidgets(com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder builder,
        UIBuildContext buildContext) {
        builder.widget(
            new DrawableWidget().setDrawable(GTUITextures.PICTURE_SCREEN_BLACK)
                .setPos(4, 4)
                .setSize(190, 85));
        final SlotWidget inventorySlot = new SlotWidget(inventoryHandler, 1);
        builder.widget(
            inventorySlot.setPos(173, 167)
                .setBackground(GTUITextures.SLOT_DARK_GRAY));

        final DynamicPositionedColumn screenElements = new DynamicPositionedColumn();
        drawTexts(screenElements, inventorySlot);
        // builder.widget(screenElements);
        builder.widget(
            new Scrollable().setVerticalScroll()
                .widget(screenElements)
                .setPos(10, 7)
                .setSize(182, 79));
        builder.widget(createPowerSwitchButton(builder))
            .widget(createParallelButton(builder, buildContext))
            .widget(createRemoveCircuitButton(builder, buildContext));
        builder.widget(new IntegerSyncer(() -> {

            return totalAcc;
        }, st -> {
            totalAcc = st;

        }).setSynced(true, false));

        buildContext.addSyncedWindow(987, (s) -> createWindow(s));

    }

    private ModularWindow createWindow(EntityPlayer s) {
        final int WIDTH = 18 * 6 + 6;
        final int HEIGHT = 18 + 6;
        final int PARENT_WIDTH = getGUIWidth();
        final int PARENT_HEIGHT = getGUIHeight();
        ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
        builder.setBackground(GTUITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
        builder.setGuiTint(getGUIColorization());
        builder.setDraggable(true);

        builder.setPos((size, window) -> Alignment.Center.getAlignedPos(size, new Size(PARENT_WIDTH, PARENT_HEIGHT)));
        NumericWidget w;
        builder.widget(w = (NumericWidget) new NumericWidget().setSetter(val -> {
            forceUpdatePattern = true;

            multiply = (int) val;
            int max = Math.max(totalAcc + 1, 1);
            int min = 1;
            if (multiply > max) multiply = max;
            if (multiply < min) multiply = min;
        })
            .setGetter(() -> multiply)

            // .setScrollValues(1, 4, 64)
            .setTextAlignment(Alignment.CenterLeft)
            .setTextColor(Color.WHITE.normal)
            .setSize(18 * 6, 18)
            .setPos(3, 3)
            .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD));

        /*
         * builder.widget(new IntegerSyncer(()->{
         * w.setBounds(1,Math.max( totalAcc+1,1));
         * return totalAcc;}, st->{
         * totalAcc=st;
         * w.setBounds(1,Math.max( totalAcc+1,1));
         * }).setSynced(true, false));
         */

        return builder.build();
    }

    @Override
    public void gridChanged() {
        // TODO Auto-generated method stub
        super.gridChanged();
    }

    @Override
    public boolean isPowered() {
        return getProxy() != null && getProxy().isPowered();
    }

    @Override
    public boolean isActive() {
        return getProxy() != null && getProxy().isActive();
    }

    static IInventory EMPTY = new IInventory() {

        @Override
        public int getSizeInventory() {

            return 0;
        }

        @Override
        public ItemStack getStackInSlot(int slotIn) {

            return null;
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {

            return null;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int index) {

            return null;
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {

        }

        @Override
        public String getInventoryName() {

            return "N/A";
        }

        @Override
        public boolean hasCustomInventoryName() {

            return false;
        }

        @Override
        public int getInventoryStackLimit() {

            return 0;
        }

        @Override
        public void markDirty() {

        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {

            return false;
        }

        @Override
        public void openInventory() {

        }

        @Override
        public void closeInventory() {

        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {

            return false;
        }
    };;

    public IInventory getPatterns() {

        return EMPTY;// new ArrayListInv(patternCache);
    }

    public String getName() {

        return "N/A";
    }

    public TileEntity getTileEntity() {

        return (TileEntity) this.getBaseMetaTileEntity();
    }

    public boolean shouldDisplay() {

        return false;
    }

    public int rows() {

        return 0;
    }

    public int rowSize() {

        return 0;
    }

    @Override
    public IGridNode getActionableNode() {

        return getProxy().getNode();
    }

    @Override
    public boolean allowsPatternOptimization() {

        return false;
    }
   // boolean instant=true;
    @Override
    public int[] pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table, int maxTodo) {

        if (maxTodo <= 0) {
            return new int[]{0};
        }
        if (!getBaseMetaTileEntity().isActive()) return new int[]{0};
        try {
            if (!(patternDetails instanceof ProgrammingCircuitProvider.CircuitProviderPatternDetial)) {
                return new int[]{0};
            }
            if (ItemProgrammingCircuit.getCircuit(((CircuitProviderPatternDetial) patternDetails).out)
                .map(ItemStack::getItem)
                .orElse(null) == MyMod.progcircuit) {
                shut(this, null);
                return new int[]{0};
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
        // toReturn.add((circuitItem));
        AEItemStack ais = AEItemStack.create(circuitItem);
        if (ais != null) ais.setStackSize(ais.getStackSize() * maxTodo);
        ret.add(ais);
      /*  if(instant){
        	return new int[]{maxTodo,0};
        }*/
        return new int[]{maxTodo};
    }

	public boolean instant() {
	
		return chip;
	}
 
}
