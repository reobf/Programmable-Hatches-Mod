package reobf.proghatches.main.mixin.mixins;

import static gregtech.api.util.GT_Utility.filterValidMTEs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Input;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.util.GT_Utility;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import reobf.proghatches.gt.cover.ProgrammingCover;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;

@SuppressWarnings("unused")
@Mixin(value = GT_MetaTileEntity_MultiBlockBase.class, remap = false)
public abstract class MixinAwarenessForDualHatch {
	@Shadow
	public ArrayList<IDualInputHatch> mDualInputHatches = new ArrayList<>();
	@Shadow
	public CheckRecipeResult checkRecipeResult;

	@Inject(method = "startRecipeProcessing", at = { @At(value = "RETURN") })
	public void a(CallbackInfo c) {
		for (IDualInputHatch hatch : (mDualInputHatches)) {
			if (hatch == null || !((MetaTileEntity) hatch).isValid())
				continue;
			if (hatch instanceof IRecipeProcessingAwareDualHatch) {
				((IRecipeProcessingAwareDualHatch) hatch).startRecipeProcessing();
			}
		}
	}

	@Inject(method = "endRecipeProcessing", at = { @At(value = "RETURN") })
	public void b(CallbackInfo c) {
		Consumer<CheckRecipeResult> setResultIfFailure = result -> {
			if (!result.wasSuccessful()) {
				this.checkRecipeResult = result;
			}
		};

		for (IDualInputHatch hatch : (mDualInputHatches)) {
			if (hatch == null || !((MetaTileEntity) hatch).isValid())
				continue;
			if (hatch instanceof IRecipeProcessingAwareDualHatch) {
				setResultIfFailure.accept(((IRecipeProcessingAwareDualHatch) hatch)
						.endRecipeProcessing((GT_MetaTileEntity_MultiBlockBase) (Object) this));
			}
		}
	}

}
