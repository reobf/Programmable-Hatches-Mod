package reobf.proghatches.main;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableSet;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.helpers.DualityInterface;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;

public class FakeHost extends TileEntity implements IInterfaceHost, IUpgradeableHost, IPriorityHost, ICustomNameObject {

    public FakeHost(TileEntity coverHost, IInterfaceHost realCover) {
        super();
        if (coverHost != null) {
            this.xCoord = coverHost.xCoord;
            this.yCoord = coverHost.yCoord;
            this.zCoord = coverHost.zCoord;
            this.setWorldObj(coverHost.getWorldObj());
        }
        cover = realCover;
    }

    IInterfaceHost cover;

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        cover.provideCrafting(craftingTracker);

    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        // TODO Auto-generated method stub
        return cover.pushPattern(patternDetails, table);
    }

    @Override
    public boolean isBusy() {
        // TODO Auto-generated method stub
        return cover.isBusy();
    }

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        // TODO Auto-generated method stub
        return cover.getInstalledUpgrades(u);
    }

    @Override
    public TileEntity getTile() {
        // TODO Auto-generated method stub
        return cover.getTile();
    }

    @Override
    public IConfigManager getConfigManager() {
        // TODO Auto-generated method stub
        return cover.getConfigManager();
    }

    @Override
    public IInventory getInventoryByName(String name) {
        // TODO Auto-generated method stub
        return cover.getInventoryByName(name);
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        // TODO Auto-generated method stub
        return cover.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        // TODO Auto-generated method stub
        return cover.injectCraftedItems(link, items, mode);
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        cover.jobStateChange(link);
    }

    @Override
    public IGridNode getActionableNode() {
        // TODO Auto-generated method stub
        return cover.getActionableNode();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        // TODO Auto-generated method stub
        return cover.getGridNode(dir);
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        // TODO Auto-generated method stub
        return cover.getCableConnectionType(dir);
    }

    @Override
    public void securityBreak() {
        cover.securityBreak();

    }

    @Override
    public DimensionalCoord getLocation() {
        // TODO Auto-generated method stub
        return cover.getLocation();
    }

    @Override
    public int rows() {
        // TODO Auto-generated method stub
        return cover.rows();
    }

    @Override
    public int rowSize() {
        // TODO Auto-generated method stub
        return cover.rowSize();
    }

    @Override
    public IInventory getPatterns() {
        // TODO Auto-generated method stub
        return cover.getPatterns();
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return cover.getName();
    }

    @Override
    public boolean shouldDisplay() {
        // TODO Auto-generated method stub
        return cover.shouldDisplay();
    }

    @Override
    public DualityInterface getInterfaceDuality() {
        // TODO Auto-generated method stub
        return cover.getInterfaceDuality();
    }

    @Override
    public EnumSet<ForgeDirection> getTargets() {
        // TODO Auto-generated method stub
        return cover.getTargets();
    }

    @Override
    public TileEntity getTileEntity() {
        // TODO Auto-generated method stub
        return cover.getTileEntity();
    }

    @Override
    public void saveChanges() {
        cover.saveChanges();

    }

    @Override
    public int getPriority() {

        return ((IPriorityHost) cover).getPriority();
    }

    @Override
    public void setPriority(int newValue) {
        ((IPriorityHost) cover).setPriority(newValue);

    }

    @Override
    public String getCustomName() {
        // TODO Auto-generated method stub
        return ((ICustomNameObject) cover).getCustomName();
    }

    @Override
    public boolean hasCustomName() {
        // TODO Auto-generated method stub
        return ((ICustomNameObject) cover).hasCustomName();
    }

    @Override
    public void setCustomName(String name) {
        ((ICustomNameObject) cover).setCustomName(name);

    }

}
