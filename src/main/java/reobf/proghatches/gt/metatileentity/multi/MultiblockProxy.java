package reobf.proghatches.gt.metatileentity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_VACUUM_FREEZER;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_VACUUM_FREEZER_ACTIVE;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.util.GTUtility.moveMultipleItemStacks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.gtnewhorizon.structurelib.alignment.enumerable.Flip;
import com.gtnewhorizon.structurelib.alignment.enumerable.Rotation;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.exceptions.FailedConnection;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.GridConnection;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.TAE;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IGTHatchAdder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.common.tileentities.machines.MTEHatchOutputBusME;
import gregtech.common.tileentities.machines.MTEHatchOutputME;
import gtPlusPlus.core.block.ModBlocks;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.PatternHousing;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class MultiblockProxy extends MTEEnhancedMultiBlockBase<MultiblockProxy>
    implements IGridProxyable, ICraftingProvider, ISurvivalConstructable/* , IPowerChannelState */ {

    private static IStructureDefinition<MultiblockProxy> STRUCTURE_DEFINITION0;

    @Override
    public boolean getDefaultHasMaintenanceChecks() {

        return false;
    }

    @Override
    public boolean addInputHatchToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        // TODO Auto-generated method stub
        return super.addInputHatchToMachineList(aTileEntity, aBaseCasingIndex);
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        // TODO Auto-generated method stub
        super.getWailaBody(itemStack, currentTip, accessor, config);
    }

    static IStructureDefinition<MultiblockProxy> STRUCTURE_DEFINITION() {

        IHatchElement<? super MultiblockProxy> Housing = new IHatchElement<MultiblockProxy>() {

            @Override
            public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {

                return ImmutableList.of(PatternHousing.class);
            }

            @Override
            public IGTHatchAdder<? super MultiblockProxy> adder() {

                return (a, b, c) -> {
                    if ((b.getMetaTileEntity()) instanceof PatternHousing == false) {
                        return false;
                    }
                    PatternHousing hs = ((PatternHousing) (b.getMetaTileEntity()));
                    hs.updateTexture(c);

                    hs.link(a);

                    try {
                        new GridConnection(
                            a.getProxy()
                                .getNode(),
                            hs.getProxy()
                                .getNode(),
                            ForgeDirection.UNKNOWN);
                    } catch (FailedConnection e) {}

                    return true;
                };
            }

            @Override
            public String name() {

                return "Pattern Housing";
            }

            @Override
            public long count(MultiblockProxy t) {
                // TODO Auto-generated method stub
                return t.linkage.size();
            }

        };
        return STRUCTURE_DEFINITION0 != null ? STRUCTURE_DEFINITION0
            : (STRUCTURE_DEFINITION0 = StructureDefinition.<MultiblockProxy>builder()
                .addShape("main", transpose(new String[][] { { "~", "b" } }))
                .addShape("piece", transpose(new String[][] { { "b", "b" } }))
                .addElement(
                    'b', // ,ofBlock(getCasingBlock(),
                         // getCasingMeta())
                    buildHatchAdder(MultiblockProxy.class)// .atLeast()
                        .atLeast(Housing/*
                                         * InputBus, OutputBus,
                                         * Maintenance, Energy,
                                         * Muffler
                                         */)
                        .casingIndex(getCasingTextureIndex())
                        .dot(1)
                        .buildAndChain(
                            onElementPass(x -> {}/* ++x.mCasing */, ofBlock(getCasingBlock(), getCasingMeta()))))

                .build());
    }

    public MultiblockProxy(String aName) {
        super(aName);

    }

    public static Block getCasingBlock() {
        return ModBlocks.blockCasings2Misc;
    }

    public static int getCasingMeta() {
        return 1;
    }

    public static byte getCasingTextureIndex() {
        return (byte) TAE.GTPP_INDEX(1 + 16);
    }

    public MultiblockProxy(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
        try {
            Field f = MTEEnhancedMultiBlockBase.class.getDeclaredField("mExtendedFacing");
            f.setAccessible(true);
            f.set(this, ExtendedFacing.of(ForgeDirection.WEST, Rotation.NORMAL, Flip.NONE));
        } catch (Exception e) {}

        // super.setExtendedFacing(ExtendedFacing.of(ForgeDirection.WEST, Rotation.NORMAL, Flip.NONE));

    }

    @Override
    public boolean isNewExtendedFacingValid(ForgeDirection direction, Rotation rotation, Flip flip) {

        return true;
    }

    @Override
    public void setExtendedFacing(ExtendedFacing newExtendedFacing) {
        super.setExtendedFacing(newExtendedFacing);
        updateValidGridProxySides(true);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        // TODO Auto-generated method stub
        return new MultiblockProxy(mName);
    }

    protected static final int CASING_INDEX = 49 + 11 - 12;

    public ITexture getTexture() {

        return TextureFactory.of(getCasingBlock(), getCasingMeta());

    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {

        if (side == getExtendedFacing().getRelativeRightInWorld()) {
            return new ITexture[] { getTexture(), TextureFactory.of(Textures.BlockIcons.OVERLAY_PIPE_IN),
                TextureFactory.of(TextureFactory.of(MyMod.iohub, 0X7F))

            };
        }
        if (side == getExtendedFacing().getRelativeRightInWorld()
            .getOpposite()) {
            return new ITexture[] { getTexture(), TextureFactory.of(Textures.BlockIcons.ITEM_OUT_SIGN),
                TextureFactory.of(TextureFactory.of(MyMod.iohub, 0X7F))

            };
        }
        if (side != facing) {
            return new ITexture[] { getTexture() };
        }

        return active ? getTexturesActive(getTexture()) : getTexturesInactive(getTexture());
    }

    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.builder()
            .addIcon(OVERLAY_FRONT_VACUUM_FREEZER_ACTIVE)
            .extFacing()
            .build()
            // TextureFactory.builder()
            // .setFromBlock(MyMod.iohub,
            // BlockIOHub.magicNO_provider_active_overlay).glow().build()
        };
    }

    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.builder()
            .addIcon(OVERLAY_FRONT_VACUUM_FREEZER)
            .extFacing()
            .build()

        };
    }

    @Override
    public IStructureDefinition<MultiblockProxy> getStructureDefinition() {

        return STRUCTURE_DEFINITION();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {

        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        Config.get(tt, "M_ID");
        return tt;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {

        return true;
    }

    boolean hasin;

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {

        patterns = new ICraftingPatternDetails[0];
        linkage.clear();
        if (!checkPiece("main", 0, 0, 0)) return false;

        if (!checkPiece("piece", 0, 1, 0)) return false;
        int i = 2;
        while (checkPiece("piece", 0, i++, 0)) {

        }
        flushPatterns();

        return true;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        // TODO Auto-generated method stub
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

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece("main", stackSize, hintsOnly, 0, 0, 0);
        for (int i = 1; i < stackSize.stackSize + 1; i++) {
            buildPiece("piece", stackSize, hintsOnly, 0, i, 0);
        }
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {

        int built = survivialBuildPiece("main", stackSize, 0, 0, 0, elementBudget, env, false, true);
        if (built >= 0) return built;
        for (int i = 1; i < stackSize.stackSize + 1; i++) {
            built = survivialBuildPiece("piece", stackSize, 0, i, 0, elementBudget, env, false, true);
            if (built >= 0) return built;
        }
        return built;
    }

    AENetworkProxy gridProxy;
    // private boolean linked;
    // private int x;
    // private int y;
    // private int z;
    private LinkedList<int[]> pos = new LinkedList<>();

    @Override
    public AENetworkProxy getProxy() {

        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", ItemList.Hatch_CraftingInput_Bus_ME.get(1), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides(false);
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    private void updateValidGridProxySides(boolean f) {

        this.getProxy()
            .setValidSides(EnumSet.of(getExtendedFacing().getRelativeRightInWorld()));

        if (f) try {
            if (hasin) {
                this.getProxy()
                    .setFlags();
            } else {
                this.getProxy()
                    .setFlags(GridFlags.REQUIRE_CHANNEL);
            }

            this.getProxy()
                .getGrid()
                .postEvent(
                    new MENetworkChannelChanged(
                        this.getProxy()
                            .getNode()));
            /*
             * PathGridCache path = this.getProxy().getGrid().getCache(IPathingGrid.class);
             * Method m = path.getClass().getDeclaredMethod("updateNodReq", MENetworkChannelChanged.class);
             * m.setAccessible(true);
             * m.invoke(path, new MENetworkChannelChanged(this.getProxy().getNode()));
             */
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    @Override
    public void onFacingChange() {

        super.onFacingChange();
        updateValidGridProxySides(true);
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {

        return getProxy().getNode();
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        ArrayList<MTEMultiBlockBase> gets = getTileAsMB();
        for (MTEMultiBlockBase get : gets) {
            // MTEMultiBlockBase get = getTileAsMB().get();
            boolean ok = get.mDualInputHatches.stream()
                .filter(s -> s instanceof DualInputHatch)
                .map(s -> {

                    return pushPattern0((DualInputHatch) s, patternDetails, table);
                })
                .filter(s -> s)
                .findFirst()
                .isPresent();
            if (ok) return ok;

        }
        return false;
    }

    private void clearInv(DualInputHatch master) {

        for (FluidTank f : master.mStoredFluid) {
            f.setFluid(null);
        }
        for (int i = 0; i < master.mInventory.length; i++) {

            if (master.isValidSlot(i)) {
                master.mInventory[i] = null;
            }
        }

    }

    private boolean isInputEmpty(DualInputHatch master) {

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

    private boolean pushPattern0(DualInputHatch master, ICraftingPatternDetails patternDetails,
        InventoryCrafting table) {

        if (master instanceof PatternDualInputHatch) {
            PatternDualInputHatch dih = ((PatternDualInputHatch) master);
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
                if (is == null) continue;
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
                BufferedDualInputHatch m = (BufferedDualInputHatch) master;

                DualInvBuffer theBuffer = ((BufferedDualInputHatch) master).classifyForce();
                if (theBuffer != null) {
                    ((BufferedDualInputHatch) master).recordRecipe(theBuffer);
                    theBuffer.onChange();
                }
                // ((BufferedDualInputHatch) master).classifyForce();
            }
            return true;// hoo ray
        }

        return false;
    }

    @Override
    public boolean isBusy() {

        return false;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {

        for (ICraftingPatternDetails p : patterns) craftingTracker.addCraftingOption(this, p);
    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity());
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {

        if (aNBT.hasKey("x") == false) return;
        super.loadNBTData(aNBT);
        pos.clear();
        int[] i = aNBT.getIntArray("coord");
        Lists.partition(IntArrayList.of(i), 3)
            .forEach(s -> pos.add(new int[] { s.get(0), s.get(1), s.get(2) }));

        // linked = aNBT.getBoolean("linked");

        getProxy().readFromNBT(aNBT);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {

        getProxy().writeToNBT(aNBT);
        super.saveNBTData(aNBT);
        aNBT.setIntArray(
            "coord",
            pos.stream()
                .flatMapToInt(s -> Arrays.stream(s))
                .toArray());

        // aNBT.setIntArray("coord", new int[] { x, y, z });
        // aNBT.setBoolean("linked", linked);
    }

    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.getWorld().isRemote) return;
        this.markDirty();

        if (aPlayer.isSneaking() && aPlayer.getHeldItem() == null) {
            // linked = false;
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.detach"));

            return;
        }
        try {
            String s = aPlayer.getHeldItem()
                .getTagCompound()
                .getString("dataLines0");
            s = s.replaceAll("§b|§r|§m", "");
            s = s.replaceAll("(-){2,}", "");
            s = s.replace(" ", "");
            s = s.replace("X", "");
            s = s.replace("Y", "");
            s = s.replace("Z", "");
            s = s.replace("D", "");

            String[] splits = s.split(":");
            int x = Integer.valueOf(splits[1].replace(",", ""));
            int y = Integer.valueOf(splits[2].replace(",", ""));
            int z = Integer.valueOf(splits[3].replace(",", ""));
            int d = Integer.valueOf(splits[4].replace(",", ""));
            World w = this.getBaseMetaTileEntity()
                .getWorld();
            if (d == w.provider.dimensionId) {

                for (int i = 0; i < pos.size(); i++) {
                    if (pos.get(i)[0] == x && pos.get(i)[1] == y && pos.get(i)[2] == z) return;
                }
                pos.add(new int[] { x, y, z });

                if (this.getBaseMetaTileEntity()
                    .getWorld()
                    .getChunkProvider()
                    .chunkExists(x >> 4, z >> 4) == false) {
                    aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.deferred"));
                    // this.linked = true;
                    return;
                }
                if (getTileAsMBLast().isPresent() == false) {
                    pos.removeLast();
                    // this.linked = false;
                    return;
                }
                aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.success"));
                // this.linked = true;

                return;

            } else {
                pos.removeLast();
                // this.linked = false;
                aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.dim"));
                aPlayer.addChatComponentMessage(null);
            } ;

        } catch (Exception w) {// w.printStackTrace();
            // this.linked = false;
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.fail"));

        }
        super.onLeftclick(aBaseMetaTileEntity, aPlayer);

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ArrayList<MTEMultiBlockBase> getTileAsMB() {
        return (ArrayList) pos.stream()
            .map(s -> getTile(s[0], s[1], s[2]))
            .filter(s -> s.isPresent())
            .map(s -> {

                return s.get() instanceof BaseMetaTileEntity ? ((BaseMetaTileEntity) s.get()).getMetaTileEntity()
                    : null;
            })
            .filter(s -> s instanceof MTEMultiBlockBase)
            .collect(Collectors.toCollection(ArrayList::new));

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Optional<MTEMultiBlockBase> getTileAsMBLast() {
        int[] poss = pos.getLast();
        return (Optional) getTile(poss[0], poss[1], poss[2]).map(s -> {

            return s instanceof BaseMetaTileEntity ? ((BaseMetaTileEntity) s).getMetaTileEntity() : null;
        })
            .filter(s -> s instanceof MTEMultiBlockBase)

        ;
    }

    public Optional<TileEntity> getTile(int x, int y, int z) {
        try {
            if (this.getBaseMetaTileEntity()
                .getWorld()
                .getChunkProvider()
                .chunkExists(x >> 4, z >> 4) == false) {
                return Optional.empty();
            }
            return Optional.ofNullable(
                this.getBaseMetaTileEntity()
                    .getWorld()
                    .getTileEntity(x, y, z));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }

    MachineSource requestSource;

    protected BaseActionSource getRequest() {

        if (requestSource == null) requestSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
        return requestSource;
    }

    IItemList<IAEItemStack> cacheitem = StorageChannel.ITEMS.<IAEItemStack>createList();
    IItemList<IAEFluidStack> cachefluid = StorageChannel.FLUIDS.<IAEFluidStack>createList();
    static Field f1, f2;
    static {
        try {
            f1 = MTEHatchOutputBusME.class.getDeclaredField("itemCache");
            f1.setAccessible(true);
            f2 = MTEHatchOutputME.class.getDeclaredField("fluidCache");
            f2.setAccessible(true);
        } catch (Exception e) {
            new AssertionError(e);
        }
    }
    boolean instant;
    IFluidHandler virtualTargetF = new IFluidHandler() {

        @Override
        public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
            int a = resource.amount;
            if (doFill) {
                instant = true;
                cachefluid.add(AEFluidStack.create(resource));
            }
            return a;
        }

        @Override
        public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {

            return null;
        }

        @Override
        public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {

            return null;
        }

        @Override
        public boolean canFill(ForgeDirection from, Fluid fluid) {

            return true;
        }

        @Override
        public boolean canDrain(ForgeDirection from, Fluid fluid) {

            return false;
        }

        @Override
        public FluidTankInfo[] getTankInfo(ForgeDirection from) {

            return new FluidTankInfo[0];
        }
    };
    IInventory virtualTarget = new IInventory() {

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            cacheitem.add(AEItemStack.create(stack));
        }

        @Override
        public void openInventory() {}

        @Override
        public void markDirty() {}

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return false;
        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {

            return true;
        }

        @Override
        public boolean hasCustomInventoryName() {

            return false;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int index) {

            return null;
        }

        @Override
        public ItemStack getStackInSlot(int slotIn) {

            return null;
        }

        @Override
        public int getSizeInventory() {

            return 64;
        }

        @Override
        public int getInventoryStackLimit() {

            return Integer.MAX_VALUE;
        }

        @Override
        public String getInventoryName() {

            return "";
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {

            return null;
        }

        @Override
        public void closeInventory() {

        }
    };

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        // TODO Auto-generated method stub
        super.onPostTick(aBaseMetaTileEntity, aTick);
        instant = false;
        /*
         * try{
         * System.out.println(getProxy().getNode().meetsChannelRequirements());
         * System.out.println(((GridNode)getProxy().getNode()).usedChannels());
         * }catch(Exception r){r.printStackTrace();}
         */
        if (aBaseMetaTileEntity.getWorld().isRemote) return;
        if (update() || aTick % 20 == 1) {

            // aBaseMetaTileEntity.setActive(isActive());

            for (MTEMultiBlockBase get : getTileAsMB()) {
                // MTEMultiBlockBase get = getTileAsMB().get();
                for (MTEHatchOutputBus bus : get.mOutputBusses) {
                    if (bus.isValid() == false) continue;
                    IGregTechTileEntity base = bus.getBaseMetaTileEntity();
                    int amount = moveMultipleItemStacks(
                        base,
                        virtualTarget,
                        base.getFrontFacing(),
                        base.getBackFacing(),
                        null,
                        false,
                        (byte) 64,
                        (byte) 1,
                        (byte) 64,
                        (byte) 1,
                        mInventory.length);
                    ItemStack[] mInventory = bus.mInventory;
                    for (int i = 0; i < mInventory.length; i++)
                        if (mInventory[i] != null && mInventory[i].stackSize <= 0) mInventory[i] = null;
                    if (amount > 0) {
                        instant = true;
                    }
                    if (bus instanceof MTEHatchOutputBusME) {
                        MTEHatchOutputBusME me = (MTEHatchOutputBusME) bus;
                        try {
                            IItemList<IAEItemStack> cache = (IItemList<IAEItemStack>) f1.get(me);

                            for (IAEItemStack s : cache) {
                                if (s.getStackSize() == 0) continue;
                                this.cacheitem.add(s);
                                s.setStackSize(0);
                            }

                        }

                        catch (Exception e) {}
                    }

                }
                // end of bus
                for (MTEHatchOutput bus : get.mOutputHatches) {

                    if (bus.isValid() == false) continue;
                    IGregTechTileEntity base = bus.getBaseMetaTileEntity();
                    ForgeDirection d = base.getFrontFacing();

                    if (bus.mFluid != null)
                        GTUtility.moveFluid(base, virtualTargetF, d, Math.max(1, bus.mFluid.amount), null);

                    if (bus instanceof MTEHatchOutputME) {
                        MTEHatchOutputME me = (MTEHatchOutputME) bus;
                        try {
                            IItemList<IAEFluidStack> cache = (IItemList<IAEFluidStack>) f2.get(me);

                            for (IAEFluidStack s : cache) {
                                if (s.getStackSize() == 0) continue;
                                this.cachefluid.add(s);
                                s.setStackSize(0);
                            }

                        }

                        catch (Exception e) {}

                    }
                }
                // end of hatch

            }
        }
        if (instant || aTick % 45 == 41) {
            instant = false;
            AENetworkProxy proxy = getProxy();
            if (proxy == null) {
                return;
            }
            try {
                IMEMonitor<IAEItemStack> sg = proxy.getStorage()
                    .getItemInventory();
                for (IAEItemStack s : cacheitem) {
                    if (s.getStackSize() == 0) continue;
                    IAEItemStack rest = Platform.poweredInsert(proxy.getEnergy(), sg, s, getRequest());
                    if (rest != null && rest.getStackSize() > 0) {
                        s.setStackSize(rest.getStackSize());
                        break;
                    }
                    s.setStackSize(0);
                }
                IMEMonitor<IAEFluidStack> sf = proxy.getStorage()
                    .getFluidInventory();
                for (IAEFluidStack s : cachefluid) {
                    if (s.getStackSize() == 0) continue;
                    IAEFluidStack rest = Platform.poweredInsert(proxy.getEnergy(), sf, s, getRequest());
                    if (rest != null && rest.getStackSize() > 0) {
                        s.setStackSize(rest.getStackSize());
                        break;
                    }
                    s.setStackSize(0);
                }
            } catch (final GridAccessException ignored) {}

        }

        if (update() || aTick % 50 == 1) {
            TileEntity in = getBaseMetaTileEntity().getTileEntityAtSide(getExtendedFacing().getRelativeRightInWorld());
            hasin = false;
            if (in instanceof BaseMetaTileEntity
                && ((BaseMetaTileEntity) in).getMetaTileEntity() instanceof MultiblockProxy) {
                MultiblockProxy mtein = ((MultiblockProxy) ((BaseMetaTileEntity) in).getMetaTileEntity());
                if (mtein != null) {
                    if (mtein.getExtendedFacing()
                        .getRelativeLeftInWorld() == getExtendedFacing().getRelativeRightInWorld()) {
                        mtein = null;
                    }
                }

                if (mtein != null) {
                    hasin = true;
                    if (conn == null) {
                        try {
                            if (this.getProxy()
                                .getNode() != null
                                && mtein.getProxy()
                                    .getNode() != null)
                                conn = new GridConnection(
                                    this.getProxy()
                                        .getNode(),
                                    mtein.getProxy()
                                        .getNode(),
                                    ForgeDirection.UNKNOWN) {

                                    @Override
                                    public void destroy() {
                                        super.destroy();
                                        conn = null;
                                    }
                                };
                        } catch (FailedConnection e) {
                            // e.printStackTrace();
                            // nah, that's normal
                        }
                    }
                }

            }
            if (conn != null && !hasin) conn.destroy();
        }
        if (!Boolean.valueOf(hasin)
            .equals(prevhasin)) {
            prevhasin = hasin;
            updateValidGridProxySides(true);
        }
    }

    Boolean prevhasin;
    GridConnection conn;

    @Override
    public void onMachineBlockUpdate() {
        super.onMachineBlockUpdate();
        synchronized (lock) {
            update = true;
        }
    }

    private boolean update;
    static Object lock = new Object();

    public boolean update() {
        synchronized (lock) {

            if (update) {
                update = false;
                return true;
            }
            return false;
        }
    }

    public ArrayList<PatternHousing> linkage = new ArrayList<>();

    public void updatePatternHousing(PatternHousing patternHousing) {
        linkage.removeIf(s -> {
            if (s.isValid() == false) return true;

            return false;
        });
        flushPatterns();

    }

    public void getWailaNBTData(net.minecraft.entity.player.EntityPlayerMP player, TileEntity tile, NBTTagCompound tag,
        World world, int x, int y, int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);

    };

    ICraftingPatternDetails[] patterns = new ICraftingPatternDetails[0];

    private void flushPatterns() {

        patterns = linkage.stream()
            .flatMap(s -> Arrays.stream(s.mInventory))
            .filter(s -> s != null && s.getItem() instanceof ICraftingPatternItem)
            .map(s -> ((ICraftingPatternItem) s.getItem()).getPatternForItem(s, getBaseMetaTileEntity().getWorld()))
            .filter(Objects::nonNull)
            .toArray(ICraftingPatternDetails[]::new);
        try {
            getProxy().getGrid()
                .postEvent(new MENetworkCraftingPatternChange(this, getProxy().getNode()));
        } catch (GridAccessException e) {}

    }

    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {

        super.drawTexts(screenElements, inventorySlot);
        screenElements.setSpace(0);
        screenElements.setPos(0, 0);
        // make it look same on 2.7.2-
        // 2.7.2- set it to a non zero value
        screenElements.widget(TextWidget.dynamicString(() ->

        pos.size() + ""

        )
            .setDefaultColor(COLOR_TEXT_WHITE.get())
            .setEnabled(widget -> { return (getBaseMetaTileEntity().isAllowedToWork()); }));
    }

    /*
     * @Override
     * public boolean isPowered() {
     * return getProxy().isPowered();
     * }
     * @Override
     * public boolean isActive() {
     * return getProxy().isActive();
     * }
     */

}
