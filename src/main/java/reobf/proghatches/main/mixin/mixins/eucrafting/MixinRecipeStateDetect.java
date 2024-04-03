package reobf.proghatches.main.mixin.mixins.eucrafting;

import static gregtech.api.util.GT_Utility.filterValidMTEs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Input;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.gt.cover.ProgrammingCover;
import reobf.proghatches.gt.cover.RecipeCheckResultCover;
import reobf.proghatches.gt.cover.RecipeCheckResultCover.Data;

@Mixin(value = GT_MetaTileEntity_MultiBlockBase.class, remap = false)
public class MixinRecipeStateDetect {
	@Shadow
	public ArrayList<GT_MetaTileEntity_Hatch_InputBus> mInputBusses = new ArrayList<>();
	@Shadow
	public ArrayList<GT_MetaTileEntity_Hatch_Input> mInputHatches = new ArrayList<>();
	@Shadow
	public ArrayList<IDualInputHatch> mDualInputHatches = new ArrayList<>();
	@Shadow
	CheckRecipeResult checkRecipeResult;

	@Inject(method = "endRecipeProcessing", at = @At(value = "RETURN"), require = 1)
	public void endRecipeProcessing(CallbackInfo a) {
		for (GT_MetaTileEntity_Hatch_InputBus hatch : filterValidMTEs(mInputBusses)) {
			updateCover(hatch);
		}
		for (GT_MetaTileEntity_Hatch_Input hatch : filterValidMTEs(mInputHatches)) {
			updateCover(hatch);
		}
		for (IDualInputHatch hatch : mDualInputHatches) {
			if (hatch != null && ((MetaTileEntity) hatch).isValid()) {

				updateCover((MetaTileEntity) hatch);
			}
			;

		}

	}

	public void updateCover(MetaTileEntity bus) {

		Arrays.stream(ForgeDirection.VALID_DIRECTIONS)
				.filter(s -> Optional.ofNullable(bus.getBaseMetaTileEntity().getCoverBehaviorAtSideNew(s))
						.map(sx -> sx instanceof RecipeCheckResultCover).orElse(false))
				.forEach(s -> RecipeCheckResultCover.update(checkRecipeResult,
						(Data) bus.getBaseMetaTileEntity().getCoverInfoAtSide(s).getCoverData())

		);

		;

		

	}

}
