package reobf.proghatches.gt.metatileentity.util;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;

public class ArrayListInv implements IInventory {

    public ArrayListInv(List<ItemStack> patternCache) {

        list = patternCache;
        if (list == null) list = ImmutableList.of();
    }

    List<ItemStack> list;

    @Override
    public int getSizeInventory() {

        return list.size();
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {

        return list.get(slotIn);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getInventoryName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasCustomInventoryName() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void markDirty() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void openInventory() {
        // TODO Auto-generated method stub

    }

    @Override
    public void closeInventory() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        // TODO Auto-generated method stub
        return false;
    }

}
