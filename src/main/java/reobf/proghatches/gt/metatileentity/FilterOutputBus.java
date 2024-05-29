package reobf.proghatches.gt.metatileentity;

import static gregtech.api.util.GT_Utility.moveMultipleItemStacks;

import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import gregtech.api.GregTech_API;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.common.GT_Client;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.ProghatchesUtil;

public class FilterOutputBus extends GT_MetaTileEntity_Hatch_OutputBus {

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
		super(aID, aName, aNameRegional, tier, reobf.proghatches.main.Config.get("FOB",
				ImmutableMap.of("keepone", keepone, "slots", Math.min(16, (1 + tier) * (tier + 1))))
		

		);
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
				Optional.ofNullable(this.mInventory[aIndex]).ifPresent(s -> s.stackSize = 0);
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
					for (int i = 0; i < mInventory.length; i++)
						if (mInventory[i] != null)
							mInventory[i].stackSize--;

				}
				moveMultipleItemStacks(aBaseMetaTileEntity, tTileEntity, aBaseMetaTileEntity.getFrontFacing(),
						aBaseMetaTileEntity.getBackFacing(), null, false, (byte) 64, (byte) 1, (byte) 64, (byte) 1,
						mInventory.length);
				if (keepone) {
					for (int i = 0; i < mInventory.length; i++)
						if (mInventory[i] != null)
							mInventory[i].stackSize++;

				}
				protectFlag = false;
				// for (int i = 0; i < mInventory.length; i++)
				// if (mInventory[i] != null && mInventory[i].stackSize <= 0)
				// mInventory[i] = null;
			}
		}

	}

}
