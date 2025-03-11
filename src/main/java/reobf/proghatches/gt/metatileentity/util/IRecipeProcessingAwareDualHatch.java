package reobf.proghatches.gt.metatileentity.util;

import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.IDualInputHatch;

// spotless:off
public interface IRecipeProcessingAwareDualHatch extends
    /*
     * future GT Multiblock might check IDualInputHatch for 'instanceof IRecipeProcessingAwareHatch', so do not
     * implement in case it's called twice
     */
    /* IRecipeProcessingAwareHatch, */
    IDualInputHatch {

    void startRecipeProcessing();

    CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller);
    public void trunOffME();
    public void trunONME();
}
