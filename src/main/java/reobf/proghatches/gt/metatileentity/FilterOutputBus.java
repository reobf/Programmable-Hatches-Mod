package reobf.proghatches.gt.metatileentity;

import static gregtech.api.util.GT_Utility.moveMultipleItemStacks;
import static reobf.proghatches.main.Config.defaultObj;

import java.util.Optional;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import gregtech.api.GregTech_API;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.api.util.extensions.ArrayExt;
import gregtech.common.GT_Client;
import reobf.proghatches.main.registration.Registration;

public class FilterOutputBus extends GT_MetaTileEntity_Hatch_OutputBus {

    public FilterOutputBus(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
        boolean keepone) {
        super(mName, mTier, mDescriptionArray, mTextures);
        this.keepone = keepone;
    }

    public FilterOutputBus(int aID, String aName, String aNameRegional, int tier, boolean keepone) {
        super(
            aID,
            aName,
            aNameRegional,
            tier,
            defaultObj(

                ArrayExt.of(
                    "Item Output for Multiblocks",
                    keepone ? "Preserve the last stack of item when moving stacks out."
                        : "Remain a phantom item instead of clearing it when moving stacks out.",
                    "Use void protection to restrict recipe indirectly.",
                    Math.min(16, (1 + tier) * (tier + 1)) + "Slots"

                ),
                ArrayExt.of(
                    "多方块机器的物品输出",
                    keepone ? "自动输出时每格总是会保留一个物品" : "自动输出时留下一个虚拟物品",
                    "配合溢出保护功能间接限制配方",
                    Math.min(16, (1 + tier) * (tier + 1)) + "格"

                )));
        this.keepone = keepone;
        Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
    }

    private boolean keepone;

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new FilterOutputBus(mName, mTier, mDescriptionArray, mTextures, keepone);
    }

    @Override
    public void setInventorySlotContents(int aIndex, ItemStack aStack) {
        if (protectFlag) {

            if (aStack == null) {
                Optional.ofNullable(this.mInventory[aIndex])
                    .ifPresent(s -> s.stackSize = 0);
                return;
            }

        }

        super.setInventorySlotContents(aIndex, aStack);
    }

    private boolean protectFlag;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isClientSide() && GT_Client.changeDetected == 4) {
            aBaseMetaTileEntity.issueTextureUpdate();
        }
        if (aBaseMetaTileEntity.isServerSide() && aBaseMetaTileEntity.isAllowedToWork() && (aTick & 0x7) == 0) {
            final IInventory tTileEntity = aBaseMetaTileEntity
                .getIInventoryAtSide(aBaseMetaTileEntity.getFrontFacing());
            if (tTileEntity != null) {
                protectFlag = true;
                if (keepone) {
                    for (int i = 0; i < mInventory.length; i++) if (mInventory[i] != null) mInventory[i].stackSize--;

                }
                moveMultipleItemStacks(
                    aBaseMetaTileEntity,
                    tTileEntity,
                    aBaseMetaTileEntity.getFrontFacing(),
                    aBaseMetaTileEntity.getBackFacing(),
                    null,
                    false,
                    (byte) 64,
                    (byte) 1,
                    (byte) 64,
                    (byte) 1,
                    mInventory.length);
                if (keepone) {
                    for (int i = 0; i < mInventory.length; i++) if (mInventory[i] != null) mInventory[i].stackSize++;

                }
                protectFlag = false;
                // for (int i = 0; i < mInventory.length; i++)
                // if (mInventory[i] != null && mInventory[i].stackSize <= 0) mInventory[i] = null;
            }
        }

    }

}
