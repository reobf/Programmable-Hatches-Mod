package reobf.proghatches.gt.metatileentity;

import com.gtnewhorizon.gtnhlib.item.ItemTransfer;
import java.util.Optional;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;

import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.common.GTClient;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.ProghatchesUtil;

public class FilterOutputBus extends MTEHatchOutputBus {

    public FilterOutputBus(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
        boolean keepone) {
        super(mName, mTier, mDescriptionArray, mTextures);
        this.keepone = keepone;
    }

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {

        super.addUIWidgets(builder, buildContext);
        ProghatchesUtil.attachZeroSizedStackRemover(builder, buildContext);

    }

    public FilterOutputBus(int aID, String aName, String aNameRegional, int tier, boolean keepone) {
        super(
            aID,
            aName,
            aNameRegional,
            tier,
            reobf.proghatches.main.Config
                .get("FOB", ImmutableMap.of("keepone", keepone, "slots", Math.min(16, (1 + tier) * (tier + 1))))

        );
        this.keepone = keepone;
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
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
        if (aBaseMetaTileEntity.isClientSide() && ((GTClient) GTMod.proxy).changeDetected() == 4) {
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
                ItemTransfer transfer = new ItemTransfer();
                transfer.push(aBaseMetaTileEntity, aBaseMetaTileEntity.getFrontFacing(), tTileEntity);
                transfer.setStacksToTransfer(mInventory.length);
                transfer.setMaxItemsPerTransfer(64);
                transfer.transfer();
                if (keepone) {
                    for (int i = 0; i < mInventory.length; i++) if (mInventory[i] != null) mInventory[i].stackSize++;

                }
                protectFlag = false;
                // for (int i = 0; i < mInventory.length; i++)
                // if (mInventory[i] != null && mInventory[i].stackSize <= 0)
                // mInventory[i] = null;
            }
        }

    }

}
