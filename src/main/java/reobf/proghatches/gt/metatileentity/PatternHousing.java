package reobf.proghatches.gt.metatileentity;

import java.util.ArrayList;
import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.StringUtils;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;

import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IAddGregtechLogo;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.render.TextureFactory;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.gt.metatileentity.multi.MultiblockProxy;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class PatternHousing extends MTEHatch implements IAddGregtechLogo {

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }

    public void setInventorySlotContents(int aIndex, ItemStack aStack) {
        super.setInventorySlotContents(aIndex, aStack);;
        onUpdate();
    };

    public ItemStack decrStackSize(int index, int amount) {
        ItemStack ret = super.decrStackSize(index, amount);

        onUpdate();
        return ret;

    };

    pattern pattern = new pattern();

    public class pattern implements IGridProxyable, IInterfaceViewable {

        @Override
        public IGridNode getGridNode(ForgeDirection dir) {

            return getProxy().getNode();
        }

        @Override
        public AECableType getCableConnectionType(ForgeDirection dir) {

            return AECableType.NONE;
        }

        @Override
        public void securityBreak() {}

        @Override
        public AENetworkProxy getProxy() {

            return gridProxy;
        }

        @Override
        public DimensionalCoord getLocation() {

            return new DimensionalCoord(getBaseMetaTileEntity().getTileEntityOffset(0, 0, 0));
        }

        @Override
        public void gridChanged() {

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

            return PatternHousing.this;
        }

        @Override
        public String getName() {

            return "ph";
        }

        @Override
        public TileEntity getTileEntity() {

            return (TileEntity) getBaseMetaTileEntity();
        }

        @Override
        public boolean shouldDisplay() {

            return true;
        }
    };

    AENetworkProxy gridProxy;

    @Override
    public AENetworkProxy getProxy() {

        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(pattern, "proxy", ItemList.Hatch_CraftingInput_Bus_ME.get(1), false);
            // gridProxy.setFlags(/*GridFlags.REQUIRE_CHANNEL*/);
            gridProxy.setValidSides(EnumSet.noneOf(ForgeDirection.class));
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {

        super.loadNBTData(aNBT);
        getProxy().readFromNBT(aNBT);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {

        getProxy().writeToNBT(aNBT);
        super.saveNBTData(aNBT);

    }

    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PosGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(mainPanel);
    }

    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager) {

        final com.cleanroommc.modularui.widgets.slot.SlotGroup SLOT_GROUP = new com.cleanroommc.modularui.widgets.slot.SlotGroup(
            "decayables",
            5);
        syncManager.registerSlotGroup(SLOT_GROUP);

        final ModularPanel panel = ModularPanel.defaultPanel("decayablesChest");
        panel.bindPlayerInventory(0);
        panel.child(
            (IWidget) new TextWidget(IKey.lang("tile.blockDecayablesChest.name")).top(5)
                .left(5));
        panel.child(
            SlotGroupWidget.builder()
                .matrix(
                    StringUtils.repeat('I', 9),
                    StringUtils.repeat('I', 9),
                    StringUtils.repeat('I', 9),
                    StringUtils.repeat('I', 9))
                .key(
                    'I',
                    index -> new ItemSlot().slot(
                        SyncHandlers.itemSlot(this.inventoryHandler, index)
                            .slotGroup(SLOT_GROUP)

                    ))
                .build()
                .flex(
                    flex -> flex.anchor(Alignment.TopCenter)
                        .marginTop(15)
                        .leftRelAnchor(0.5f, 0.5f)));
        return panel;
    }

    private ArrayList<MultiblockProxy> linkage = new ArrayList<>();

    public void link(MultiblockProxy m) {
        linkage.add(m);
        m.linkage.add(this);
    }

    public void onUpdate() {

        linkage.removeIf(s -> {
            if (s.isValid() == false) return true;
            s.updatePatternHousing(this);

            return false;
        });

    }

    public PatternHousing(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 36, "");
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
    }

    public PatternHousing(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 36, aDescription, aTextures);
    }
    /*
     * @Override
     * public synchronized String[] getDescription() {
     * return DESC;
     * }
     */

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

    @Override
    public boolean onWrenchRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer entityPlayer,
        float aX, float aY, float aZ, ItemStack aTool) {

        if (getBaseMetaTileEntity().isValidFacing(wrenchingSide)) {
            getBaseMetaTileEntity().setFrontFacing(wrenchingSide);
            return true;
        }
        return false;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {

        return true;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new PatternHousing(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {

        return new ITexture[] { aBaseTexture,
            TextureFactory.of(TextureFactory.of(MyMod.iohub, BlockIOHub.magicNO_drive)) };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {

        return new ITexture[] { aBaseTexture,

            TextureFactory.of(TextureFactory.of(MyMod.iohub, BlockIOHub.magicNO_drive))
            // TextureFactory.of(BlockIcons.OVERLAY_ME_HATCH)

        };
    }

    boolean wasActive;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {

        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        // TODO Auto-generated method stub
        return true;
    }

    public void addGregTechLogo(ModularWindow.Builder builder) {}

    @Override
    public void addUIWidgets(com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder builder,
        UIBuildContext buildContext) {
        builder.widget(
            SlotGroup.ofItemHandler(inventoryHandler, 9)
                .startFromSlot(0)
                .endAtSlot(36)
                .phantom(false)
                .background(getGUITextureSet().getItemSlot(), GTUITextures.OVERLAY_SLOT_PATTERN_ME)
                .widgetCreator(slot -> new SlotWidget(slot) {

                    @Override
                    protected ItemStack getItemStackForRendering(Slot slotIn) {
                        ItemStack stack = slot.getStack();
                        if (stack == null || !(stack.getItem() instanceof ItemEncodedPattern)) {
                            return stack;
                        }
                        ItemEncodedPattern patternItem = (ItemEncodedPattern) stack.getItem();
                        ItemStack output = patternItem.getOutput(stack);
                        return output != null ? output : stack;
                    }
                }.setFilter(itemStack -> itemStack.getItem() instanceof ICraftingPatternItem)
                    .setChangeListener(() -> onUpdate()))
                .build()
                .setPos(7, 9));
        // super.addUIWidgets(builder, buildContext);
    }

    /*
     * @Override
     * protected boolean forceUseMui2() {
     * return true;
     * }
     */
    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.getWorld().isRemote == false) openGui(aPlayer);
        return true;
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
