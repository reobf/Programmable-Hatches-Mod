package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.ITEM_IN_SIGN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.util.DimensionalCoord;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.RecursiveLinkExcpetion;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class RemoteInputBus extends MTEHatchInputBus implements IRecipeProcessingAwareHatch, IDataCopyablePlaceHolder {
    /**
     * GT 290's hatch base classes override getDescription() with their own hardcoded
     * "input bus / output hatch / ..." text, which shadowed every PH machine's own tooltip
     * (the Config.get(...) template passed to the constructor). Hand it back.
     */
    @Override
    public String[] getDescription() {
        return mDescriptionArray;
    }


    static public ArrayList<String> blacklist = new ArrayList<>();
    static {

        blacklist.add(RemoteInputBus.class.getTypeName());
        blacklist.add("thaumic.tinkerer.common.block.tile.transvector.TileTransvectorInterface");
        blacklist.add("remoteio.common.tile.TileRemoteInterface");
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return GTMod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(ITEM_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(ITEM_IN_SIGN) };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return GTMod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(ITEM_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(ITEM_IN_SIGN) };
    }

    public RemoteInputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);

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

        } catch (Exception w) {// w.printStackTrace();
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

    public RemoteInputBus(int id, String name, String nameRegional, int tier) {
        super(id, name, nameRegional, tier, 0, reobf.proghatches.main.Config.get("RIB", ImmutableMap.of()));
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id));

    }

    

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new RemoteInputBus(mName, mTier, mDescriptionArray, mTextures);
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

    ArrayList<ItemStack> arr;

    public List<ItemStack> filterTakable(TileEntity e) {
        // if (processingRecipe == false) return new ArrayList<ItemStack>();
        if (e == null || (e instanceof IInventory == false)) return new ArrayList<ItemStack>();
        IInventory inv = (IInventory) e;
        if (arr != null) return arr;
        arr = new ArrayList<ItemStack>();
        // boolean b=e instanceof ISidedInventory;

        int size = inv.getSizeInventory();
        if (e instanceof ISidedInventory) {

            ISidedInventory side = (ISidedInventory) e;
            HashSet<Integer> slots = new HashSet<Integer>();

            ForgeDirection dir = this.getBaseMetaTileEntity()
                .getFrontFacing();
            // for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS){
            for (int i : side.getAccessibleSlotsFromSide(dir.ordinal())) {
                if (side.getStackInSlot(i) != null && side.canExtractItem(i, side.getStackInSlot(i), dir.ordinal()))
                    slots.add(i);
            } ;
            // }

            // slots.stream().map(inv::getStackInSlot).forEach(arr::add);
            ;
            for (int i = 0; i < size; i++) {
                if (slots.contains(i)) {
                    ItemStack item = inv.getStackInSlot(i);
                    arr.add(item);
                } else {
                    arr.add(null);
                }

            }

        } else {

            for (int i = 0; i < size; i++) {
                ItemStack item = inv.getStackInSlot(i);
                arr.add(item);
            }

        }

        return arr;
    }

    int x, y, z;
    boolean linked;

    // boolean justQueried;
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

    Throwable t = new Throwable();

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
        if (2 > 1) {
            return false;
        }
        if (count++ < 40) return false;
        count = 0;
        return checkDepth();
    }

    @Override
    public int getSizeInventory() {
        try (AutoCloseable o = mark()) {

            if (!processingRecipe) return 1;
            // justQueried=true;
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            if (!linked) return 1;
            /*
             * if (checkDepthLoose()) {
             * getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);
             * return 0;
             * }
             */

            return opt.filter(s -> s instanceof IInventory)
                .map(s -> ((IInventory) s).getSizeInventory())
                .orElse(0) + 2;
        } catch (RecursiveLinkExcpetion e) {
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int ordinalSide) {

        return new int[0];
    }

    @Override
    public void setInventorySlotContents(int aIndex, ItemStack aStack) {
        if (blocked) return;
        markDirty();
        if (aIndex == getCircuitSlot()) {
            mInventory[0] = GTUtility.copyAmount(0, aStack);
            return;
        }
        /*
         * List<ItemStack> arr = getTile().map(this::filterTakable).orElseGet(ArrayList::new);
         * if (aIndex >= 0 && aIndex < arr.size())
         * arr.set(aIndex , aStack);
         */
    }

    @Override
    @Nullable
    public ItemStack getStackInSlot(int aIndex) {
        if (blocked) return null;
        try (AutoCloseable o = mark()) {
            // justQueried=true;
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
            if (aIndex == getCircuitSlot()) {
                return mInventory[0];
            }
            /*
             * int i = getCircuitSlot();
             * if (i == aIndex)
             * return mInventory[i];
             */
            if (!processingRecipe) return null;
            if (!linked) return null;

            // Optional<TileEntity> opt = getTile();
            List<ItemStack> arr = opt.map(this::filterTakable)
                .orElseGet(ArrayList::new);
            if (aIndex == arr.size() + 1) {
                return mInventory[0];
            }
            if (aIndex == arr.size()) {

                TileEntity gt = opt.orElse(null);
                if (gt != null && gt instanceof IGregTechTileEntity) {
                    IMetaTileEntity meta = ((IGregTechTileEntity) gt).getMetaTileEntity();
                    if (meta != null && (meta instanceof IConfigurationCircuitSupport)) {
                        IConfigurationCircuitSupport c = (IConfigurationCircuitSupport) meta;
                        return meta.getStackInSlot(c.getCircuitSlot());
                    }
                }

                return null;
            }
            if (aIndex < 0 || aIndex >= arr.size()) {
                return null;
            }

            return arr.get(aIndex);
            // }catch(Exception e){e.printStackTrace();return null;}
        } catch (RecursiveLinkExcpetion e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getCircuitSlot() {

        return Integer.MAX_VALUE;
    }

    public void updateSlots() {}// no we don't

    public void removePhantom() {
        try {
            getTile().filter(s -> s instanceof IInventory)
                .ifPresent(s -> {
                    s.markDirty();
                    int index = -1;
                    TileEntity gt = s;
                    if (gt != null && gt instanceof IGregTechTileEntity) {
                        IMetaTileEntity meta = ((IGregTechTileEntity) gt).getMetaTileEntity();
                        if (meta != null && (meta instanceof IConfigurationCircuitSupport)) {
                            index = ((IConfigurationCircuitSupport) meta).getCircuitSlot();
                        }
                    }

                    IInventory a = ((IInventory) s);
                    int size = a.getSizeInventory();
                    for (int i = 0; i < size; i++) {

                        if (a.getStackInSlot(i) != null && a.getStackInSlot(i).stackSize == 0 && i != index)//
                            a.decrStackSize(i, 0);// remove 0-sized phantom item
                    }

                });

        } catch (RuntimeException e) {
            e.printStackTrace();
            // ??????????

        }

    }

    static HashSet<DimensionalCoord> using = new HashSet<>();
    boolean blocked;

    @Override
    public void onPostTick(gregtech.api.interfaces.tileentity.IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);
        // Contents are proxied from the linked remote inventory (only visible while processingRecipe), so
        // local change detection never fires for them; nudge watching controllers periodically now that
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

    protected boolean processingRecipe = false;

    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        if (!blocked) {
            using.remove(new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity()));
        }
        processingRecipe = false;
        blocked = false;
        arr = null;
        removePhantom();
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public IItemHandlerModifiable getInventoryHandler() {

        // The circuit "slot" is addressed as getCircuitSlot()==Integer.MAX_VALUE by GT's ghost-circuit
        // plumbing (GhostCircuitItemStackHandler does inventory.get/setStackInSlot(getCircuitSlot()));
        // every index maps onto mInventory[0] here, and the validateSlotIndex override below only
        // admits that sentinel index.
        return new ItemStackHandler(1) {

            public void setSize(int size) {}

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
                mInventory[0] = GTUtility.copyAmount(0, stack);
            }

            @Override
            public int getSlots() {
                return 1;
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return mInventory[0];
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

                return stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return null;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            protected int getStackLimit(int slot, ItemStack stack) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return true;
            }

            @Override
            public NBTTagCompound serializeNBT() {
                NBTTagCompound nbt = new NBTTagCompound();
                if (mInventory[0] != null) mInventory[0].writeToNBT(nbt);

                return nbt;
            }

            @Override
            public void deserializeNBT(NBTTagCompound nbt) {
                mInventory[0] = ItemStack.loadItemStackFromNBT(nbt);

                this.onLoad();
            }

            protected void validateSlotIndex(int slot) {
                if (slot != getCircuitSlot()) {
                    throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.stacks.size() + ")");
                }
            }

        };
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
    // The base MTEHatchInputBus GUI switched to MUI2, so the MUI1 preview UI above stopped being
    // used (the stock bus GUI opened instead). Full custom panel: status line + read-only preview
    // of the remote inventory (16 item slots + the target's circuit slot) + info tooltips.
    @Override
    public com.cleanroommc.modularui.screen.ModularPanel buildUI(com.cleanroommc.modularui.factory.PosGuiData data,
        com.cleanroommc.modularui.value.sync.PanelSyncManager syncManager,
        com.cleanroommc.modularui.screen.UISettings uiSettings) {
        com.cleanroommc.modularui.screen.ModularPanel builder = gregtech.api.modularui2.GTGuis
            .mteTemplatePanelBuilder(this, data, syncManager, uiSettings)
            .doesAddGregTechLogo(false)
            .doesAddGhostCircuitSlot(false)
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


        // read-only preview of the remote inventory, server-refreshed every 100 ticks
        // (slot 16 mirrors the remote machine's configuration-circuit slot)
        final com.cleanroommc.modularui.utils.item.ItemStackHandler display =
            new com.cleanroommc.modularui.utils.item.ItemStackHandler(17);
        syncManager.syncValue("remote_refresh", new com.cleanroommc.modularui.value.sync.SyncHandler() {

            int count;

            @Override
            public void detectAndSendChanges(boolean init) {
                if (!init && count-- > 0) return;
                count = 100;
                Optional<TileEntity> opt = getTile();
                if (opt.isPresent()) {
                    arr = null;
                    List<ItemStack> list = opt.map(e -> filterTakable(e)).get();
                    arr = null;
                    for (int i = 0; i < display.getSlots() - 1; i++) {
                        display.setStackInSlot(i, list.size() > i ? list.get(i) : null);
                    }
                    TileEntity gt = opt.orElse(null);
                    display.setStackInSlot(16, null);
                    if (gt instanceof IGregTechTileEntity) {
                        IMetaTileEntity meta = ((IGregTechTileEntity) gt).getMetaTileEntity();
                        if (meta instanceof IConfigurationCircuitSupport) {
                            IConfigurationCircuitSupport c = (IConfigurationCircuitSupport) meta;
                            display.setStackInSlot(16, meta.getStackInSlot(c.getCircuitSlot()));
                        }
                    }
                } else {
                    for (int i = 0; i < display.getSlots(); i++) display.setStackInSlot(i, null);
                }
                // push the whole preview to the client explicitly (same idea as GT's stocking bus,
                // which syncs its Slot[] data separately instead of relying on implicit slot sync)
                syncToClient(1, buf -> {
                    for (int i = 0; i < display.getSlots(); i++)
                        com.cleanroommc.modularui.network.NetworkUtils.writeItemStack(buf, display.getStackInSlot(i));
                });
            }

            @Override
            public void readOnClient(int id, net.minecraft.network.PacketBuffer buf) {
                if (id == 1) for (int i = 0; i < display.getSlots(); i++)
                    display.setStackInSlot(i, com.cleanroommc.modularui.network.NetworkUtils.readItemStack(buf));
            }

            @Override
            public void readOnServer(int id, net.minecraft.network.PacketBuffer buf) {}
        });
        for (int i = 0; i < 17; i++) {
            com.cleanroommc.modularui.widgets.slot.ItemSlot slot = new com.cleanroommc.modularui.widgets.slot.ItemSlot()
                .slot(new com.cleanroommc.modularui.widgets.slot.ModularSlot(display, i).accessibility(false, false));
            if (i == 16) {
                slot.background(
                    gregtech.api.modularui2.GTGuiTextures.SLOT_ITEM_STANDARD,
                    gregtech.api.modularui2.GTGuiTextures.OVERLAY_SLOT_INT_CIRCUIT);
            }
            builder.child(slot.pos(3 + (i % 8) * 18, 3 + 16 + (i / 8) * 18));
        }

        // The bus's OWN configuration circuit (stored in mInventory[0], overlaid on the remote
        // inventory for recipe checks). MUI1's window template added this automatically; MUI2's
        // ghost-circuit widget works now that getInventoryHandler() tolerates the MAX_VALUE index.
        builder.child(gregtech.api.modularui2.common.CommonWidgets.createCircuitSlot(syncManager, this)
            // custom position: right-aligned in the third preview row (the default 153,60 from
            // getCircuitSlotX/Y belongs to the stock bus layout and lands outside our grid)
            .pos(3 + 18 * 7, 3 + 16 + 18 * 2));

        // info tooltips (same lang keys as the MUI1 icon)
        // MUI1's info icon, referenced straight from ModularUI(1)'s assets
        builder.child(new com.cleanroommc.modularui.widget.Widget<>()
            .background(com.cleanroommc.modularui.drawable.UITexture
                .fullImage("modularui", "gui/widgets/information"))
            .pos(3 + 18 * 8 + 4, 3 + 16)
            .size(16, 16)
            .tooltipBuilder(t -> {
                int n = Integer.valueOf(StatCollector.translateToLocal("programmable_hatches.gt.remotebus.tooltip"));
                for (int i = 0; i < n; i++) t.addLine(
                    com.cleanroommc.modularui.api.drawable.IKey
                        .str(LangManager.translateToLocal("programmable_hatches.gt.remotebus.tooltip." + i)));
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
        if (!(opt.get() instanceof IInventory)) return "programmable_hatches.remote.dummytarget";
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
            // ClientHighlight is a separate class so client-only types are never touched during
            // THIS class's verification (the dedicated server crashed with ClassNotFoundException:
            // WorldClient at registration otherwise); it only classloads on actual button press.
            reobf.proghatches.util.ClientHighlight
                .highlightAndClose(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
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
