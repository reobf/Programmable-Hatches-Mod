package reobf.proghatches.main.mixin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import reobf.proghatches.gt.metatileentity.DualInputHatch;

public class MixinCallback {

    public static boolean encodingSpecialBehaviour = true;

    public static MethodHandle hanlde;
    static {

        try {
            hanlde = MethodHandles.lookup()
                .unreflect(GT_MetaTileEntity_MultiBlockBase.class.getMethod("getRecipeMap"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void handleAddedToMachineList(IGregTechTileEntity aTileEntity, Object o) {

        try {
            if (aTileEntity == null) return;
            IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
            if (aMetaTileEntity != null && aMetaTileEntity instanceof DualInputHatch) {

                // ( (GT_MetaTileEntity_MultiBlockBase)o).mInputBusses.add((GT_MetaTileEntity_Hatch_InputBus)
                // aMetaTileEntity);
                // ( (GT_MetaTileEntity_MultiBlockBase)o).mInputHatches.add(((DualInputHatch)
                // aMetaTileEntity).delegateInputHatch);

                ((DualInputHatch) aMetaTileEntity)
                    .setFilter(MixinCallback.hanlde.invoke((GT_MetaTileEntity_MultiBlockBase) (Object) o));
                // must cast to GT_MetaTileEntity_MultiBlockBase or else polymorphic invoke signature will be
                // MixinGTRecipeFilter
            }

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

}
