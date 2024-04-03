package reobf.proghatches.main.mixin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import reobf.proghatches.gt.metatileentity.DualInputHatch;

public class MixinCallback {

	public static boolean encodingSpecialBehaviour = true;

	public static void handleAddedToMachineList(IGregTechTileEntity aTileEntity, Object o) {
		GT_MetaTileEntity_MultiBlockBase thiz = (GT_MetaTileEntity_MultiBlockBase) o;
		try {
			if (aTileEntity == null)
				return;
			IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
			if (aMetaTileEntity != null && aMetaTileEntity instanceof DualInputHatch) {

				((DualInputHatch) aMetaTileEntity).setFilter(thiz.getRecipeMap());

			}

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}

}
