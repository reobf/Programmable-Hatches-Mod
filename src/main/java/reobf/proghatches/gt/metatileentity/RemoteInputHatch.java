package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.FLUID_IN_SIGN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.api.widget.Widget.ClickData;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.util.DimensionalCoord;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMultiInput;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.RecursiveLinkExcpetion;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class RemoteInputHatch extends MTEHatchMultiInput
    implements IRecipeProcessingAwareHatch, IDataCopyablePlaceHolder {

    static public ArrayList<String> blacklist = new ArrayList<>();
    static {

        blacklist.add(RemoteInputHatch.class.getTypeName());
        blacklist.add("thaumic.tinkerer.common.block.tile.transvector.TileTransvectorInterface");
        blacklist.add("remoteio.common.tile.TileRemoteInterface");
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return GTMod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return GTMod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) };
    }

    public RemoteInputHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, 0, aTier, aDescription, aTextures);

    }

    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.getWorld().isRemote) return;
        this.markDirty();
        if (aPlayer.isSneaking() && aPlayer.getHeldItem() == null) {
            linked = false;
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.detach"));

            return;
        }
        try {
            net.minecraft.item.ItemStack held = aPlayer.getHeldItem();
            int[] coords = held == null ? null
                : reobf.proghatches.util.ProghatchesUtil.parseScannerCoords(held.getTagCompound());
            if (coords == null) throw new Exception();
            int x = coords[0];
            int y = coords[1];
            int z = coords[2];
            World w = this.getBaseMetaTileEntity()
                .getWorld();
            int d = coords.length >= 4 ? coords[3] : w.provider.dimensionId;
            if (d == w.provider.dimensionId) {

                this.x = x;
                this.y = y;
                this.z = z;
                if (this.getBaseMetaTileEntity()
                    .getWorld()
                    .getChunkProvider()
                    .chunkExists(x >> 4, z >> 4) == false) {
                    aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.deferred"));
                    this.linked = true;
                    return;
                }
                if (checkBlackList()
                // blacklist.contains(this.getBaseMetaTileEntity().getWorld().getBlock(x,
                // y, z).getUnlocalizedName())
                ) {

                    aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.blacklisted"));
                    this.linked = false;
                    return;
                }
                aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.success"));
                this.linked = true;

                return;

            } else {
                this.linked = false;
                aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.dim"));
                aPlayer.addChatComponentMessage(null);
            } ;

        } catch (Exception w) {
            this.linked = false;
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.fail"));

        }
        super.onLeftclick(aBaseMetaTileEntity, aPlayer);
    }

    @SuppressWarnings("unused")
    private boolean checkBlackList() {
        World ww = this.getBaseMetaTileEntity()
            .getWorld();
        return Optional.ofNullable(ww.getTileEntity(x, y, z))
            .map(TileEntity::getClass)
            .map(Class::toString)
            .map(blacklist::contains)
            .orElse(false) ||
        // Optional.ofNullable(ww.getBlock(x, y,
        // z)).map(Block::getClass).map(Class::toString).map(blacklist::contains).orElse(false)||
            Optional.ofNullable(ww.getTileEntity(x, y, z))
                .filter(sp -> sp instanceof IGregTechTileEntity)
                .map(
                    sp -> ((IGregTechTileEntity) sp).getMetaTileEntity()
                        .getClass()
                        .getTypeName())
                .map(blacklist::contains)
                .orElse(false);

    }

    @SuppressWarnings("unused")
    private boolean checkBlackList(Optional<TileEntity> opt) {// World
                                                              // ww=this.getBaseMetaTileEntity().getWorld();
        return opt.map(TileEntity::getClass)
            .map(Class::toString)
            .map(blacklist::contains)
            .orElse(false) ||
        // Optional.ofNullable(ww.getBlock(x, y,
        // z)).map(Block::getClass).map(Class::toString).map(blacklist::contains).orElse(false)||
            opt.filter(sp -> sp instanceof IGregTechTileEntity)
                .map(
                    sp -> ((IGregTechTileEntity) sp).getMetaTileEntity()
                        .getClass()
                        .getTypeName())
                .map(blacklist::contains)
                .orElse(false);

    }

    public RemoteInputHatch(int id, String name, String nameRegional, int tier) {
        super(id, 0, name, nameRegional, tier

        );
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id));

    }

    String[] desc;

    @Override
    public String[] getDescription() {

        if (desc == null) {
            desc = reobf.proghatches.main.Config.get("RIH", ImmutableMap.of());

        }

        return desc;
    }

    @SuppressWarnings("unchecked")
    

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new RemoteInputHatch(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {

        return false;
    }

    @Override
    public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {

        return false;
    }

    public Optional<TileEntity> getTile() {
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
    public FluidStack getFluid(int aSlot) {
        if (blocked) {
            return null;
        }
        try (AutoCloseable o = mark()) {
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            /*
             * if (checkDepthLoose()) {
             * getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);
             * return null;
             * }
             */

            return getTile().map(this::filterTakable)
                .map(s -> {
                    if (aSlot < 0 || aSlot >= s.size()) return null;
                    return s.get(aSlot);
                })
                .orElse(null);
        } catch (RecursiveLinkExcpetion e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FluidStack getFillableStack() {
        if (blocked) {
            return null;
        }
        try (AutoCloseable o = mark()) {
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            /*
             * if (checkDepthLoose()) {
             * getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);
             * return null;
             * }
             */

            return getTile().map(this::filterTakable)
                .filter(s -> s.size() >= 1)
                .map(s -> s.get(0))
                .orElse(null);
        } catch (RecursiveLinkExcpetion e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @SuppressWarnings("unchecked")
    public List<FluidStack> filterTakable(TileEntity e) {

        if (processingRecipe == false) return new ArrayList<FluidStack>();

        try {
            if (tmp != null) {
                return (List<FluidStack>) (Object) tmp;
            }

            // endRecipeProcessing(null);
            // this means this method is called twice during recipe check?
            // remove consumed fluid
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        processingRecipe = true;

        if (e == null || (e instanceof IFluidHandler == false)) return new ArrayList<FluidStack>();
        IFluidHandler inv = (IFluidHandler) e;

        ArrayList<FluidStack> arr = new ArrayList<FluidStack>();
        // boolean b=e instanceof IFluidHandler;

        if (e instanceof IFluidHandler) {

            IFluidHandler side = (IFluidHandler) e;
            HashMultiset<ShadowFluidStack> slots = HashMultiset.create();
            // for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS){
            FluidTankInfo[] info = side.getTankInfo(
                this.getBaseMetaTileEntity()
                    .getFrontFacing());
            if (info == null) return new ArrayList<>(0);
            for (FluidTankInfo i : info) {

                if (i.fluid != null && side.canDrain(
                    this.getBaseMetaTileEntity()
                        .getFrontFacing(),
                    i.fluid.getFluid()))

                    slots.add(new ShadowFluidStack(i.fluid));
            } ;
            // }
            ArrayList<ShadowFluidStack> arrm = new ArrayList<>(slots);
            tmp = arrm;
            return (List<FluidStack>) (Object) arrm;
        }

        return new ArrayList<>();
    }

    private volatile ArrayList<ShadowFluidStack> tmp = null;

    public class ShadowFluidStack extends FluidStack {

        FluidStack original;

        public ShadowFluidStack(FluidStack stack) {
            super(stack.copy()/* might be a clone itself */, stack.amount);
            original = stack.copy();
        }
    }

    int x, y, z;
    boolean linked;

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {

        super.saveNBTData(aNBT);

        aNBT.setIntArray("coord", new int[] { x, y, z });
        aNBT.setBoolean("linked", linked);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        super.loadNBTData(aNBT);
        int[] i = aNBT.getIntArray("coord");
        x = i[0];
        y = i[1];
        z = i[2];

        linked = aNBT.getBoolean("linked");
    }

    @Override
    public boolean shouldDropItemAt(int index) {

        return false;
    }

    @Override
    public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {}// no we don't

    Throwable t = new Throwable();

    public int getCapacityPerTank(int aTier, int aSlot) {
        return 0;
    }

    public boolean checkDepth() {
        t.fillInStackTrace();

        boolean b = t.getStackTrace().length > 80;
        if (b) {

            MyMod.LOG.fatal("Warning! Potential infinite recursion!");
            MyMod.LOG.fatal("To prevent stack overflow, the block will be removed.");
            MyMod.LOG.fatal(x + "," + y + "," + z + "@dim:" + getBaseMetaTileEntity().getWorld().provider.dimensionId);
            t.printStackTrace();

        }
        return b;
    }

    private int count;

    public boolean checkDepthLoose() {
        if (2 > 1) return false;

        if (count++ < 40) return false;
        count = 0;
        return checkDepth();
    }

    private static HashSet<Object> record = new HashSet<>();

    public AutoCloseable mark() {
        if (!record.add(this)) {
            getBaseMetaTileEntity().getWorld()
                .setBlockToAir(this.x, this.y, this.z);
            throw new RecursiveLinkExcpetion();
        } ;

        return () -> { record.remove(this); };
    }

    @Override
    public int getSizeInventory() {

        return 0;

    }

    @Override
    public void setInventorySlotContents(int aIndex, ItemStack aStack) {

    }

    @Override
    @Nullable
    public ItemStack getStackInSlot(int aIndex) {
        return null;

    }

    public void updateSlots() {}// no we don't

    @Override
    public FluidStack[] getStoredFluid() {
        if (blocked) {
            return new FluidStack[0];
        }
        try (AutoCloseable o = mark()) {
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            /*
             * if (checkDepthLoose()) {
             * getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);
             * return new FluidStack[0];
             * }
             */

            return getTile().map(this::filterTakable)
                .orElse(new ArrayList<>())
                .toArray(new FluidStack[0]);

        } catch (RecursiveLinkExcpetion e) {
            return new FluidStack[0];
        } catch (Exception e) {
            e.printStackTrace();
            return new FluidStack[0];
        }

    }

    protected boolean processingRecipe = false;
    static HashSet<DimensionalCoord> using = new HashSet<>();
    boolean blocked;

    @Override
    public void onPostTick(gregtech.api.interfaces.tileentity.IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);
        // Contents are proxied from the linked remote fluid handler (only visible while processingRecipe),
        // so local change detection never fires for them; nudge watching controllers periodically now that
        // GT's interval recipe polling is gone (no-op when nothing is watching or nothing is linked).
        if (aBaseMetaTileEntity.isServerSide() && aTimer % 32 == 0 && linked) {
            notifyWatchers();
        }
    }

    @Override
    public void startRecipeProcessing() {

        processingRecipe = true;
        if (false == using.add(new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity()))) {
            blocked = true;

        } ;
    }

    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        if (!blocked) {
            using.remove(new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity()));
        }
        processingRecipe = false;
        blocked = false;
        if (tmp == null) return CheckRecipeResultRegistry.SUCCESSFUL;
        TileEntity tile = getTile().orElse(null);
        if (tile != null) tile.markDirty();
        boolean missing = (tile == null);
        IFluidHandler fh = null;
        if (tile instanceof IFluidHandler) fh = (IFluidHandler) tile;
        else missing = true;
        AtomicBoolean fail = new AtomicBoolean(false);
        // even if missing, tmp might be consumed? just check it
        final boolean fmissing = missing;
        final IFluidHandler ffh = fh;

        tmp.forEach(s -> {
            int consume = -s.amount + s.original.amount;
            Fluid fluid = s.original.getFluid();
            if (consume > 0 && fmissing) {
                fail.set(true);
                return;
            }
            if (consume == 0) return;
            if (consume != ffh.drain(
                this.getBaseMetaTileEntity()
                    .getFrontFacing(),
                new FluidStack(fluid, consume),
                true).amount) {
                fail.set(true);
                // return CheckRecipeResultRegistry.CRASH;
            }

        });

        if (fail.get()) {
            controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
            return CheckRecipeResultRegistry.CRASH;
        }

        tmp = null;
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public FluidStack getFluid() {
        FluidStack FS = getFillableStack();

        return FS;
    }

    @Override
    public FluidStack getDrainableStack() {

        return getFillableStack();
    }

    @Override
    public FluidStack drain(ForgeDirection side, FluidStack aFluid, boolean doDrain) {
        if (blocked) {
            return null;
        }
        try (AutoCloseable o = mark()) {
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            List<FluidStack> all = filterTakable(getTile().orElse(null));
            // this is an ME input hatch. allowing draining via logistics would be very wrong (and against
            // canTankBeEmptied()) but we do need to support draining from controller, which uses the UNKNOWN direction.
            if (side != ForgeDirection.UNKNOWN) return null;
            // FluidStack stored = getMatchingFluidStack(aFluid);
            FluidStack stored = all.stream()
                .filter(s -> s.getFluid() == aFluid.getFluid())
                .findAny()
                .orElse(null);

            if (stored == null) return null;
            FluidStack drained = GTUtility.copyAmount(Math.min(stored.amount, aFluid.amount), stored);
            if (doDrain) {
                stored.amount -= drained.amount;
            }
            return drained;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * private FluidStack getMatchingFluidStack(FluidStack aFluid) {
     * if(tmp==null)return null;
     * return tmp.stream().filter(s->s.getFluid()==aFluid.getFluid())
     * .findAny().orElse(null);
     * }
     */
    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = new NBTTagCompound();
        writeType(ret, player);
        ret.setInteger("x", x);
        ret.setInteger("y", y);
        ret.setInteger("z", z);
        ret.setBoolean("linked", linked);
        return ret;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
        if (nbt.hasKey("x")) x = nbt.getInteger("x");
        if (nbt.hasKey("y")) y = nbt.getInteger("y");
        if (nbt.hasKey("z")) z = nbt.getInteger("z");
        if (nbt.hasKey("linked")) linked = nbt.getBoolean("linked");
        return true;
    }

    // ===================== MUI2 =====================
    // MTEHatchMultiInput's stock MUI2 GUI indexes fluidTanks[0..3], but this hatch is constructed
    // with 0 slots (contents live in the remote handler), so opening it crashed with an AIOOBE and
    // the GUI never appeared. Full custom panel instead: status line + read-only remote preview.
    @Override
    public com.cleanroommc.modularui.screen.ModularPanel buildUI(com.cleanroommc.modularui.factory.PosGuiData data,
        com.cleanroommc.modularui.value.sync.PanelSyncManager syncManager,
        com.cleanroommc.modularui.screen.UISettings uiSettings) {
        com.cleanroommc.modularui.screen.ModularPanel builder = gregtech.api.modularui2.GTGuis
            .mteTemplatePanelBuilder(this, data, syncManager, uiSettings)
            .doesAddGregTechLogo(false)
            .build();

        // status decided server-side; only the lang KEY is synced so the client localizes it itself
        com.cleanroommc.modularui.value.sync.StringSyncValue status =
            new com.cleanroommc.modularui.value.sync.StringSyncValue(this::remoteStatusSynced);
        syncManager.syncValue("remote_status", status);

        // target rendered as its pick-block item; hover = server-evaluated WAILA tooltip
        final reobf.proghatches.util.TargetBlockInfoSync targetInfo = new reobf.proghatches.util.TargetBlockInfoSync(
            () -> this.getBaseMetaTileEntity() == null ? null
                : this.getBaseMetaTileEntity()
                    .getWorld(),
            () -> linked ? new int[] { x, y, z } : null);
        syncManager.syncValue("target_item", targetInfo);
        builder.child(
            targetInfo.createWidget()
                .pos(5, 2));

        builder.child(com.cleanroommc.modularui.api.drawable.IKey
            .dynamic(() -> remoteStatusDisplay(status.getStringValue(), targetInfo))
            .asWidget()
            .pos(24, 5)
            .size(getGUIWidth() - 29, 12));

        // Highlight-target button: purely client-side, reuses AE2's interface-terminal highlighter.
        builder.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>().onMousePressed(mouseButton -> {
            highlightTargetClient(status.getStringValue());
            return true;
        })
            .background(
                gregtech.api.modularui2.GTGuiTextures.BUTTON_STANDARD,
                gregtech.api.modularui2.GTGuiTextures.OVERLAY_BUTTON_HIGHLIGHT_BLOCK)
            .tooltip(t -> t.addLine(
                com.cleanroommc.modularui.api.drawable.IKey.lang("programmable_hatches.remote.highlight")))
            .size(16, 16)
            .pos(3 + 18 * 8 + 4, 3 + 16 + 18));

        // Open-target button: forwards a plain right-click to the linked block on the server, so
        // chests / tanks / machines open their own GUI. NOTE: vanilla containers validate player
        // distance in canInteractWith, so a far-away chest may close itself immediately; GT machines
        // go through the mixin-fixed remote-open path instead.
        com.cleanroommc.modularui.value.sync.InteractionSyncHandler openTarget = new com.cleanroommc.modularui.value.sync.InteractionSyncHandler().setOnMousePressed(d -> {
            if (getBaseMetaTileEntity() == null || getBaseMetaTileEntity().isClientSide()) return;
            if (!linked) return;
            World w = this.getBaseMetaTileEntity().getWorld();
            if (!w.getChunkProvider().chunkExists(x >> 4, z >> 4)) return;
            if (checkBlackList()) return;
            if (!(data.getPlayer() instanceof net.minecraft.entity.player.EntityPlayerMP)) return;
            // RemoteIO-style remote activation: distance-spoofing proxy + container whitelist,
            // so vanilla chests/tanks stay open at any distance (see RemoteOpenHelper)
            reobf.proghatches.util.RemoteOpenHelper
                .activateBlock(w, x, y, z, (net.minecraft.entity.player.EntityPlayerMP) data.getPlayer());
        });
        syncManager.syncValue("open_target", openTarget);
        builder.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>().syncHandler(openTarget)
            .background(
                gregtech.api.modularui2.GTGuiTextures.BUTTON_STANDARD,
                gregtech.api.modularui2.GTGuiTextures.OVERLAY_BUTTON_EXPORT)
            .tooltip(t -> t.addLine(
                com.cleanroommc.modularui.api.drawable.IKey.lang("programmable_hatches.remote.open")))
            .size(16, 16)
            .pos(3 + 18 * 8 + 4, 3 + 16 + 18 * 2));


        // 16 read-only preview slots showing the remote tank's extractable fluids, server-refreshed
        // every 100 ticks (same cadence as the old MUI1 SyncedWidget)
        final FluidStack[] display = new FluidStack[16];
        syncManager.syncValue("remote_refresh", new com.cleanroommc.modularui.value.sync.SyncHandler() {

            int count;

            @Override
            public void detectAndSendChanges(boolean init) {
                if (!init && count-- > 0) return;
                count = 100;
                Optional<TileEntity> opt = getTile();
                if (opt.isPresent()) {
                    List<FluidStack> list;
                    try {
                        processingRecipe = true;
                        tmp = null;
                        list = filterTakable(opt.get());
                    } finally {
                        processingRecipe = false;
                        tmp = null;
                    }
                    for (int i = 0; i < display.length; i++) display[i] = list.size() > i ? list.get(i) : null;
                } else {
                    java.util.Arrays.fill(display, null);
                }
                // push the whole preview to the client explicitly (see RemoteInputBus for rationale)
                syncToClient(1, buf -> {
                    for (int i = 0; i < display.length; i++)
                        com.cleanroommc.modularui.network.NetworkUtils.writeFluidStack(buf, display[i]);
                });
            }

            @Override
            public void readOnClient(int id, net.minecraft.network.PacketBuffer buf) {
                if (id == 1) for (int i = 0; i < display.length; i++)
                    display[i] = com.cleanroommc.modularui.network.NetworkUtils.readFluidStack(buf);
            }

            @Override
            public void readOnServer(int id, net.minecraft.network.PacketBuffer buf) {}
        });
        for (int i = 0; i < 16; i++) {
            final int fi = i;
            builder.child(new com.cleanroommc.modularui.widgets.slot.FluidSlot()
                .syncHandler(new com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler(
                    new com.gtnewhorizons.modularui.common.fluid.FluidStackTank(
                        () -> display[fi], f -> display[fi] = f, Integer.MAX_VALUE)).canFillSlot(false)
                            .canDrainSlot(false))
                .pos(3 + (i % 8) * 18, 3 + 16 + (i / 8) * 18));
        }

        // info tooltips (same lang keys as the MUI1 icon)
        // MUI1's info icon, referenced straight from ModularUI(1)'s assets
        builder.child(new com.cleanroommc.modularui.widget.Widget<>()
            .background(com.cleanroommc.modularui.drawable.UITexture
                .fullImage("modularui", "gui/widgets/information"))
            .pos(3 + 18 * 8 + 4, 3 + 16)
            .size(16, 16)
            .tooltipBuilder(t -> {
                int n = Integer.valueOf(StatCollector.translateToLocal("programmable_hatches.gt.remotehatch.tooltip"));
                for (int i = 0; i < n; i++) t.addLine(
                    com.cleanroommc.modularui.api.drawable.IKey
                        .str(LangManager.translateToLocal("programmable_hatches.gt.remotehatch.tooltip." + i)));
            }));
        return builder;
    }

    private String remoteStatusKey() {
        if (!linked) return "programmable_hatches.remote.unlinked";
        if (!this.getBaseMetaTileEntity().getWorld().getChunkProvider().chunkExists(x >> 4, z >> 4))
            return "programmable_hatches.remote.chunk";
        Optional<TileEntity> opt = getTile();
        if (!opt.isPresent()) return "programmable_hatches.remote.nothing";
        checkBlackList();
        if (!(opt.get() instanceof IFluidHandler)) return "programmable_hatches.remote.dummytarget";
        return "programmable_hatches.remote.ok";
    }


    /**
     * Client-only. Highlights the linked target position with AE2's block highlighter (pulsing blue
     * box + chat message) and closes the screen, exactly like the interface terminal's highlight.
     */
    private void highlightTargetClient(String synced) {
        if (synced == null || !synced.startsWith("I")) return;
        try {
            // Format: "I<x>,<y>,<z>|<damage>|<blockRegName>" — coords first, since the registry
            // name is free-form and may itself contain '|' (e.g. BuildCraft|Factory:tankBlock).
            String[] p = synced.substring(1).split("\\|", 3);
            String[] c = p[0].split(",");
            if (c.length < 3) return;
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            appeng.client.render.highlighter.BlockPosHighlighter.highlightBlocks(
                mc.thePlayer,
                java.util.Collections.singletonList(
                    new appeng.api.util.DimensionalCoord(
                        mc.theWorld,
                        Integer.parseInt(c[0]),
                        Integer.parseInt(c[1]),
                        Integer.parseInt(c[2]))),
                appeng.core.localization.PlayerMessages.MachineHighlighted.getUnlocalized(),
                appeng.core.localization.PlayerMessages.MachineInOtherDim.getUnlocalized());
            mc.thePlayer.closeScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Server-side: "K<langKey>" for a plain status, or "I<x>,<y>,<z>|<damage>|<blockRegName>" when a
     * target container is present, so the client can localize the container name with its own lang.
     * The registry name goes LAST because it is free-form and may contain '|' (BuildCraft mod ids).
     */
    private String remoteStatusSynced() {
        String key = remoteStatusKey();
        if (!"programmable_hatches.remote.ok".equals(key)) return "K" + key;
        try {
            World w = this.getBaseMetaTileEntity().getWorld();
            net.minecraft.block.Block b = w.getBlock(x, y, z);
            String reg = net.minecraft.block.Block.blockRegistry.getNameForObject(b);
            int dmg = b.getDamageValue(w, x, y, z);
            return "I" + x + "," + y + "," + z + "|" + dmg + "|" + reg;
        } catch (Exception e) {
            return "K" + key;
        }
    }

    /** Client-side: turn the synced status into display text (container name localized locally). */
    private String remoteStatusDisplay(String v, reobf.proghatches.util.TargetBlockInfoSync targetInfo) {
        if (v == null || v.isEmpty()) return "";
        if (v.startsWith("K")) return LangManager.translateToLocal(v.substring(1));
        String[] p = v.substring(1).split("\\|", 3);
        if (p.length < 3) return "";
        // Prefer the name of the stack the server resolved (Waila stack providers / getPickBlock):
        // GT machines all share one Block and keep their identity in the tile entity, so
        // new ItemStack(block, 1, blockMetadata) would give a generic, often untranslated name.
        String name = targetInfo == null ? null : targetInfo.clientDisplayName();
        if (name == null || name.isEmpty()) {
            try {
                net.minecraft.block.Block b = net.minecraft.block.Block.getBlockFromName(p[2]);
                name = new ItemStack(b, 1, Integer.parseInt(p[1])).getDisplayName();
            } catch (Exception e) {
                name = p[2];
            }
        }
        return LangManager.translateToLocalFormatted("programmable_hatches.remote.hostinfo", name, p[0]);
    }

}
