package reobf.proghatches.ae;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.parts.PartBasicState;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.eucrafting.EUUtil;
import reobf.proghatches.eucrafting.IGuiProvidingPart;
import reobf.proghatches.fmp.ICraftingMachinePart;

public class PartStockingExportBus extends PartBasicState implements

    IGridProxyable, IActionHost, IGuiProvidingPart, IGridTickable, ICraftingMachinePart {

    ItemStack[] inv = new ItemStack[1];

    @Override
    public void writeToNBT(NBTTagCompound data) {
        if (inv[0] != null) {
            data.setTag("theslot", inv[0].writeToNBT(new NBTTagCompound()));
        }
        super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        inv[0] = ItemStack.loadItemStackFromNBT(data.getCompoundTag("theslot"));
        super.readFromNBT(data);
    }

    private InventoryAdaptor adaptor;
    private int adaptorHash;

    public PartStockingExportBus(ItemStack is) {
        super(is);

    }

    protected int getGUIWidth() {
        return 176;
    }

    protected int getGUIHeight() {
        return 107 + 18 * 3 + 18;
    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(getGUIWidth(), getGUIHeight());
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
        builder.bindPlayerInventory(buildContext.getPlayer());

        builder.widget(new SlotWidget(new ItemStackHandler(inv) {

            @Override
            public int getSlotLimit(int slot) {

                return 16;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (stack.getItem() instanceof IUpgradeModule) {
                    return ((IUpgradeModule) stack.getItem()).getType(stack) == Upgrades.SPEED
                        || ((IUpgradeModule) stack.getItem()).getType(stack) == Upgrades.SUPERSPEED;
                }
                return false;
            }

        }, 0).setPos(3, 3));

        return builder.build();
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.ExportBus.getMin(), TickRates.ExportBus.getMax(), false, false);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartExportSides.getIcon());

        rh.setBounds(4, 4, 12, 12, 12, 14);
        rh.renderInventoryBox(renderer);

        rh.setBounds(5, 5, 14, 11, 11, 15);
        rh.renderInventoryBox(renderer);

        rh.setBounds(6, 6, 15, 10, 10, 16);
        rh.renderInventoryBox(renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
        final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
        rh.setTexture(
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartExportSides.getIcon(),
            CableBusTextures.PartExportSides.getIcon());

        rh.setBounds(4, 4, 12, 12, 12, 14);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(5, 5, 14, 11, 11, 15);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(6, 6, 15, 10, 10, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorBack.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartMonitorSidesStatus.getIcon(),
            CableBusTextures.PartMonitorSidesStatus.getIcon());

        rh.setBounds(6, 6, 11, 10, 10, 12);
        rh.renderBlock(x, y, z, renderer);

        this.renderLights(x, y, z, rh, renderer);
    }

    @Override
    public int cableConnectionRenderTo() {
        return 5;
    }

    protected boolean canDoBusWork() {
        final TileEntity self = this.getHost()
            .getTile();
        final World world = self.getWorldObj();
        final int xCoordinate = self.xCoord + this.getSide().offsetX;
        final int zCoordinate = self.zCoord + this.getSide().offsetZ;

        return world != null && world.getChunkProvider()
            .chunkExists(xCoordinate >> 4, zCoordinate >> 4);
    }

    private TileEntity getTileEntity(final TileEntity self, final int x, final int y, final int z) {
        final World w = self.getWorldObj();

        if (w.getChunkProvider()
            .chunkExists(x >> 4, z >> 4)) {
            return w.getTileEntity(x, y, z);
        }

        return null;
    }

    protected InventoryAdaptor getHandler() {
        final TileEntity self = this.getHost()
            .getTile();
        final TileEntity target = this.getTileEntity(
            self,
            self.xCoord + this.getSide().offsetX,
            self.yCoord + this.getSide().offsetY,
            self.zCoord + this.getSide().offsetZ);

        final int newAdaptorHash = Platform.generateTileHash(target);

        if (this.adaptorHash == newAdaptorHash && newAdaptorHash != 0) {
            return this.adaptor;
        }

        this.adaptorHash = newAdaptorHash;
        this.adaptor = InventoryAdaptor.getAdaptor(
            target,
            this.getSide()
                .getOpposite());

        return this.adaptor;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (!this.getProxy()
            .isActive() || !this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }
        end: try {
            final InventoryAdaptor destination = this.getHandler();

            final IMEMonitor<IAEItemStack> inv = this.getProxy()
                .getStorage()
                .getItemInventory();
            final IEnergyGrid energy = this.getProxy()
                .getEnergy();
            didSomething = false;
            if (destination == null) break end;

            IMEMonitor<IAEItemStack> iinv = getProxy().getStorage()
                .getItemInventory();
            itemToSend = calculateItemsToSend();
            for (IAEItemStack iae : iinv.getStorageList()) {
                // IAEItemStack iae=iinv.getStorageList().getFirstItem();
                if (iae == null) break;
                if(iae.getItem( )instanceof ItemFluidDrop){continue;}
                final IAEItemStack itemsToAdd = inv.extractItems(
                    iae.copy()
                        .setStackSize(itemToSend),
                    Actionable.SIMULATE,
                    this.mySrc);
                if (itemsToAdd == null) break;
                itemsToAdd.setCraftable(false);
                this.pushItemIntoTarget(destination, energy, inv, itemsToAdd);

                if (itemToSend <= 0) break;

            }

        } catch (GridAccessException e) {} catch (Exception e) {
            e.printStackTrace();

        }

        return didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    MachineSource mySrc = new MachineSource(this);
    private long itemToSend;
    private boolean didSomething;

    private void pushItemIntoTarget(final InventoryAdaptor d, final IEnergyGrid energy,
        final IMEInventory<IAEItemStack> inv, IAEItemStack ais) {
        final ItemStack is = ais.getItemStack();
        is.stackSize = (int) this.itemToSend;
        if (this.itemToSend > Integer.MAX_VALUE) {
            this.itemToSend = Integer.MAX_VALUE;
        }
        final ItemStack o = d.simulateAdd(is);
        final long canFit = o == null ? this.itemToSend : this.itemToSend - o.stackSize;

        if (canFit > 0) {
            ais = ais.copy();
            ais.setStackSize(canFit);
            final IAEItemStack itemsToAdd = Platform.poweredExtraction(energy, inv, ais, this.mySrc);

            if (itemsToAdd != null) {
                this.itemToSend -= itemsToAdd.getStackSize();

                final ItemStack failed = d.addItems(itemsToAdd.getItemStack());
                if (failed != null) {
                    ais.setStackSize(failed.stackSize);
                    inv.injectItems(ais, Actionable.MODULATE, this.mySrc);
                } else {
                    this.didSomething = true;
                }
            }
        }
    }

    protected long calculateItemsToSend() {

        long items = 1;
        if (inv[0] != null && inv[0].getItem() instanceof IUpgradeModule) {
            if (((IUpgradeModule) inv[0].getItem()).getType(inv[0]) == Upgrades.SPEED) {

                items = items << inv[0].stackSize;

            }
            if (((IUpgradeModule) inv[0].getItem()).getType(inv[0]) == Upgrades.SUPERSPEED) {

                items = items << (inv[0].stackSize * 2);

            }
        }

        return items;

        /*
         * return switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
         * default -> 1;
         * case 1 -> 8;
         * case 2 -> 32;
         * case 3 -> 64;
         * case 4 -> 96;
         * };
         */
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPartActivate(EntityPlayer player, Vec3 pos) {

        if (player.isSneaking()) return false;
        TileEntity t = this.getTile();

        EUUtil.open(player, player.getEntityWorld(), t.xCoord, t.yCoord, t.zCoord, getSide());

        return true;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection) {

        return false;
    }

    @Override
    public boolean acceptsPlans(ForgeDirection ejectionDirection) {
        System.out.println(ejectionDirection);
        return false;
    }

}
