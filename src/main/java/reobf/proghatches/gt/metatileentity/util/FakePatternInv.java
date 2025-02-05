package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider.CircuitProviderPatternDetial;
import reobf.proghatches.item.ItemProgrammingCircuit;

public class FakePatternInv implements IInventory {

    ItemStack[] inv;

    public FakePatternInv(ItemStack[] mInventory) {
        inv = mInventory;
    }

    public FakePatternInv(ItemStack[] mInventory, int mul) {
        inv = mInventory;
        this.mul = mul;
    }

    int mul = 1;

    @Override
    public int getSizeInventory() {

        return inv.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {

        return new CircuitProviderPatternDetial(ItemProgrammingCircuit.wrap(inv[slotIn], mul, false)).getPattern();

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

        return "";
    }

    @Override
    public boolean hasCustomInventoryName() {

        return false;
    }

    @Override
    public int getInventoryStackLimit() {

        return 1;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {

        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {

        return true;
    }

}
