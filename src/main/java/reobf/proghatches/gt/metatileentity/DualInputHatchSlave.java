package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_CRAFTING_INPUT_SLAVE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableMap;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.helpers.ICustomNameObject;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputHatchWithPattern;
import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.IPHDual;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;
import reobf.proghatches.main.registration.Registration;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;

import gregtech.api.modularui2.GTGuiTextures;
import gregtech.api.modularui2.GTGuis;
import reobf.proghatches.lang.LangManager;


@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class DualInputHatchSlave<T extends MetaTileEntity & IDualInputHatchWithPattern & IMetaTileEntity> extends MTEHatchInputBus
    implements IDualInputHatchWithPattern, IRecipeProcessingAwareDualHatch, IDataCopyablePlaceHolder {
    /**
     * GT 290's hatch base classes override getDescription() with their own hardcoded
     * "input bus / output hatch / ..." text, which shadowed every PH machine's own tooltip
     * (the Config.get(...) template passed to the constructor). Hand it back.
     */
    @Override
    public String[] getDescription() {
        return mDescriptionArray;
    }


    private T master; // use getMaster() to access
    private int masterX, masterY, masterZ;
    private boolean masterSet = false; // indicate if values of masterX,
                                       // masterY, masterZ are valid

    public DualInputHatchSlave(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 6, 0, reobf.proghatches.main.Config.get("DHS", ImmutableMap.of())

        );
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
        disableSort = true;
    }

    public DualInputHatchSlave(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
        disableSort = true;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new DualInputHatchSlave<>(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return getTexturesInactive(aBaseTexture);
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_ME_CRAFTING_INPUT_SLAVE) };
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);
        if (aTimer % 100 == 0 && masterSet && getMaster() == null) {
            trySetMasterFromCoord(masterX, masterY, masterZ);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        super.loadNBTData(aNBT);

        if (aNBT.hasKey("reverseMode")) reverseMode = aNBT.getInteger("reverseMode");
        if (aNBT.hasKey("master")) {
            NBTTagCompound masterNBT = aNBT.getCompoundTag("master");
            masterX = masterNBT.getInteger("x");
            masterY = masterNBT.getInteger("y");
            masterZ = masterNBT.getInteger("z");
            masterSet = true;
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("reverseMode", reverseMode);
        if (masterSet) {
            NBTTagCompound masterNBT = new NBTTagCompound();
            masterNBT.setInteger("x", masterX);
            masterNBT.setInteger("y", masterY);
            masterNBT.setInteger("z", masterZ);
            aNBT.setTag("master", masterNBT);
        }
    }

    @Override
    public boolean isGivingInformation() {
        return true;
    }

    @Override
    public String[] getInfoData() {
        ArrayList<String> ret = new ArrayList<String>();
        if (getMaster() != null) {
            ret.add(
                "This bus is linked to the Crafting Input Buffer at " + masterX
                    + ", "
                    + masterY
                    + ", "
                    + masterZ
                    + ".");
            ret.addAll(Arrays.asList(getMaster().getInfoData()));
        } else ret.add("This bus is not linked to any Buffered Dual Inputhatch.");
        return ret.toArray(new String[0]);
    }

    public T getMaster() {
        if (master == null) return null;
        if (((IMetaTileEntity) master).getBaseMetaTileEntity() == null) { // master
                                                                          // disappeared
            master = null;
        }
        return master;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    /**
     * Buffer order this mirror asks its host for. 0 = most copies first, 1 = fewest copies first,
     * 2 = follow the host's own button (the default, i.e. the behaviour before issue #332).
     * <p>
     * One host can feed several mirrors, each wired into a different multiblock, and those machines
     * want different orders - so the choice belongs to the mirror, not to the host. It has no effect
     * when the host is not a buffered hatch, because a hatch with a single inventory has nothing to
     * order (see DualInputHatch#inventories(boolean)).
     */
    public int reverseMode = 2;

    /** True when this mirror has a host whose buffer order it can actually choose. */
    public boolean canChooseOrder() {
        return getMaster() instanceof BufferedDualInputHatch;
    }

    @Override
    public Iterator<? extends IDualInputInventoryWithPattern> inventories() {
        if (!this.isValid()) return DualInputHatch.emptyItr;
        T m = getMaster();
        if (m == null) return Collections.emptyIterator();
        if (reverseMode != 2 && m instanceof BufferedDualInputHatch) {
            return ((BufferedDualInputHatch) m).inventories(reverseMode == 1);
        }
        return m.inventories();
    }

    @Override
    public Optional<IDualInputInventory> getFirstNonEmptyInventory() {
        if (!this.isValid()) return Optional.empty();
        return getMaster() != null ? getMaster().getFirstNonEmptyInventory() : Optional.empty();
    }

    @Override
    public boolean supportsFluids() {
        return getMaster() != null && getMaster().supportsFluids();
    }

    /** Local mirror of registered watchers (MTEHatch's own list is private) for forwarding to the master. */
    private final java.util.List<gregtech.common.tileentities.machines.IHatchWatcher> forwardedWatchers = new java.util.ArrayList<>();

    @Override
    public void addWatcher(gregtech.common.tileentities.machines.IHatchWatcher watcher) {
        super.addWatcher(watcher);
        forwardedWatchers.add(watcher);
        T m = getMaster();
        if (m != null) m.addWatcher(watcher);
    }

    @Override
    public void removeWatcher(gregtech.common.tileentities.machines.IHatchWatcher watcher) {
        super.removeWatcher(watcher);
        forwardedWatchers.remove(watcher);
        T m = getMaster();
        if (m != null) m.removeWatcher(watcher);
    }

    @SuppressWarnings("unchecked")
    public IDualInputHatch trySetMasterFromCoord(int x, int y, int z) {
        TileEntity tileEntity = getBaseMetaTileEntity().getWorld()
            .getTileEntity(x, y, z);
        if (tileEntity == null) return null;
        if (!(tileEntity instanceof IGregTechTileEntity)) return null;
        IMetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
        if (!(metaTileEntity instanceof IDualInputHatch)) return null;

        if (!(metaTileEntity instanceof IPHDual)) return null;

        masterX = x;
        masterY = y;
        masterZ = z;
        masterSet = true;
        boolean changed = master != metaTileEntity;
        master = (T) metaTileEntity;
        if (changed) {
            for (gregtech.common.tileentities.machines.IHatchWatcher w : forwardedWatchers) master.addWatcher(w);
        }
        return master;
    }

    private boolean tryLinkDataStick(EntityPlayer aPlayer) {
        ItemStack dataStick = aPlayer.inventory.getCurrentItem();

        if (!ItemList.Tool_DataStick.isStackEqual(dataStick, false, true)) {
            return false;
        }
        if (dataStick.hasTagCompound() && dataStick.stackTagCompound.getString("type")
            .equals("CraftingInputBuffer")) {
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.slave.compat"));
            return false;
        }
        if (!dataStick.hasTagCompound() || !dataStick.stackTagCompound.getString("type")
            .equals("ProgHatchesDualInput")) {
            return false;
        }

        NBTTagCompound nbt = dataStick.stackTagCompound;
        int x = nbt.getInteger("x");
        int y = nbt.getInteger("y");
        int z = nbt.getInteger("z");
        if (trySetMasterFromCoord(x, y, z) != null) {
            aPlayer.addChatMessage(new ChatComponentText("Link successful"));
            return true;
        }
        aPlayer.addChatMessage(new ChatComponentText("Link failed"));
        return true;
    }

    /**
     * Right-click opens THIS mirror's own GUI (where its buffer-order option lives); the host's GUI
     * moved to left-click, see {@link #onLeftclick}. Before issue #332 a mirror had no settings of its
     * own, so right-click just forwarded to the host.
     */
    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (!(aPlayer instanceof EntityPlayerMP)) {
            return false;
        }
        if (tryLinkDataStick(aPlayer)) {
            return true;
        }
        // MUI2: open our own panel on the server only, the same way the ME mapping mirror does.
        if (aBaseMetaTileEntity.isClientSide()) return true;
        openGui(aPlayer);
        return true;
    }

    /** Left-click opens the linked host's GUI. Sneak still breaks the block normally. */
    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.isServerSide() && !aPlayer.isSneaking()) {
            T m = getMaster();
            if (m != null) {
                m.onRightclick(m.getBaseMetaTileEntity(), aPlayer);
                return;
            }
        }
        super.onLeftclick(aBaseMetaTileEntity, aPlayer);
    }

    /**
     * Our panel is MUI2 only (buildUI below), so openGui must take the MUI2 branch. If this returned
     * false GT would fall back to GTUIInfos.openGTTileEntityUI, i.e. the MUI1 path, and nothing would
     * be shown at all.
     */
    @Override
    protected boolean useMui2() {
        return true;
    }

    /**
     * This mirror's own GUI.
     * <p>
     * Deliberately NOT super.buildUI(): every dual-input device only borrows MTEHatchInputBus as a
     * shell, it is not really a bus, so the inherited bus panel would show item slots that stand for
     * nothing. A blank MTE template panel is built instead, the same way the ME mapping mirror does it.
     */
    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {
        ModularPanel panel = GTGuis.mteTemplatePanelBuilder(this, data, syncManager, uiSettings)
            .doesAddGregTechLogo(false)
            // No ghost circuit slot: MTEHatchInputBus.getCircuitSlot() is getSlots(mTier), which is 49
            // at this mirror's tier 6, while the mirror is built with ZERO inventory slots (it only
            // forwards the host's). Letting the template add it threw
            // "Slot 49 not in valid range - [0,0)" on every right-click.
            .doesAddGhostCircuitSlot(false)
            .build();
        panel.child(
            new CycleButtonWidget().stateCount(3)
                .value(new IntSyncValue(() -> reverseMode, v -> reverseMode = v).allowC2S())
                .stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
                .stateBackground(1, GTGuiTextures.BUTTON_STANDARD_PRESSED)
                .stateBackground(2, GTGuiTextures.BUTTON_STANDARD)
                .stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_SORTING_MODE)
                .stateOverlay(1, GTGuiTextures.OVERLAY_BUTTON_SORTING_MODE)
                .stateOverlay(2, GTGuiTextures.OVERLAY_BUTTON_SORTING_MODE)
                .tooltipDynamic(t -> {
                    t.addLine(LangManager.translateToLocal("programmable_hatches.gt.mirrororder"));
                    t.addLine(LangManager.translateToLocal("programmable_hatches.gt.mirrororder." + reverseMode));
                    if (!canChooseOrder())
                        t.addLine(LangManager.translateToLocal("programmable_hatches.gt.mirrororder.na"));
                })
                .pos(7, 7)
                .size(18, 18));
        return panel;
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();
        currenttip.add((tag.getBoolean("linked") ? "Linked" : "Not linked"));

        if (tag.hasKey("masterX")) {
            currenttip.add(
                "Bound to " + tag
                    .getInteger("masterX") + ", " + tag.getInteger("masterY") + ", " + tag.getInteger("masterZ"));
        }

        if (tag.hasKey("masterName")) {
            currenttip.add(EnumChatFormatting.GOLD + tag.getString("masterName") + EnumChatFormatting.RESET);
        }

        super.getWailaBody(itemStack, currenttip, accessor, config);
    }

    public String getNameOf(T tg) {

        if (tg instanceof ICustomNameObject) {
            ICustomNameObject iv = (ICustomNameObject) tg;
            if (iv.hasCustomName()) return iv.getCustomName();

        }

        StringBuilder name = new StringBuilder();
        if (tg instanceof ICraftingMedium && ((ICraftingMedium) tg).getCrafterIcon() != null) {
            name.append(
                ((ICraftingMedium) tg).getCrafterIcon()
                    .getDisplayName());
        } else {
            name.append(tg.getLocalName());
        }

        return name.toString();
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {

        tag.setBoolean("linked", getMaster() != null);
        if (masterSet) {
            tag.setInteger("masterX", masterX);
            tag.setInteger("masterY", masterY);
            tag.setInteger("masterZ", masterZ);
        }
        if (getMaster() != null) tag.setString("masterName", getNameOf(getMaster()));
        /*
         * if (getMaster() != null) tag.setString("masterName",
         * getMaster().getnam);
         */

        super.getWailaNBTData(player, tile, tag, world, x, y, z);
    }

    @Override
    public void startRecipeProcessing() {

        if (getMaster() != null) if (getMaster() instanceof IRecipeProcessingAwareDualHatch)
            ((IRecipeProcessingAwareDualHatch) getMaster()).startRecipeProcessing();

    }

    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        if (getMaster() != null) if (getMaster() instanceof IRecipeProcessingAwareDualHatch)
            return ((IRecipeProcessingAwareDualHatch) getMaster()).endRecipeProcessing(controller);
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public List<ItemStack> getItemsForHoloGlasses() {
        return getMaster() != null ? getMaster().getItemsForHoloGlasses() : null;
    }

    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = new NBTTagCompound();
        writeType(ret, player);
        ret.setInteger("masterX", masterX);
        ret.setInteger("masterY", masterY);
        ret.setInteger("masterZ", masterZ);
        ret.setBoolean("masterSet", masterSet);
        ret.setInteger("reverseMode", reverseMode);

        return ret;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
        if (nbt.hasKey("masterX")) masterX = nbt.getInteger("masterX");
        if (nbt.hasKey("masterY")) masterY = nbt.getInteger("masterY");
        if (nbt.hasKey("masterZ")) masterZ = nbt.getInteger("masterZ");
        if (nbt.hasKey("masterSet")) masterSet = nbt.getBoolean("masterSet");
        if (nbt.hasKey("reverseMode")) reverseMode = Math.max(0, Math.min(2, nbt.getInteger("reverseMode")));
        master = null;
        return true;
    }

    @Override
    public ItemStack[] getSharedItems() {
        return getMaster() != null ? getMaster().getSharedItems() : new ItemStack[0];
    }

    
    /*public void setProcessingLogics(List<ProcessingLogic> processingLogics) {
    
    	// if (getMaster() != null) getMaster().setProcessingLogics(processingLogics);
    }public List<ProcessingLogic> getProcessingLogics() {
    	// if (getMaster() != null) getMaster().getProcessingLogics();
    }*/
    @Override
    public void trunOffME() {
        T master = getMaster();
        if (master instanceof IRecipeProcessingAwareDualHatch) ((IRecipeProcessingAwareDualHatch) master).trunOffME();
    }

    @Override
    public void trunONME() {
        T master = getMaster();
        if (master instanceof IRecipeProcessingAwareDualHatch) ((IRecipeProcessingAwareDualHatch) master).trunONME();
    }



	/*@Override
	public void setProcessingLogic(ProcessingLogic arg0) {
		// TODO Auto-generated method stub
		
	}*/
}
