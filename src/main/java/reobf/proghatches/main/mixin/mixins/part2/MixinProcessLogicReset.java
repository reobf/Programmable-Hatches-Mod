package reobf.proghatches.main.mixin.mixins.part2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;

import gregtech.api.logic.ProcessingLogic;
import gregtech.api.util.GTRecipe;
import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.main.asm.repack.objectwebasm.Opcodes;

@Mixin(value = ProcessingLogic.class, remap = false)
public class MixinProcessLogicReset {
	 /* @Inject(
		        at = { @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;") },
		        method = { "process" })
		    public void process(CallbackInfoReturnable x) {
		  
		  
	  }
	  */
	@Shadow
	 protected IDualInputInventoryWithPattern activeDualInv;
	@Shadow
	protected Map<IDualInputInventoryWithPattern, Set<GTRecipe>> dualInvWithPatternToRecipeCache;

	  @Inject(
		        method = "process",
		        at = @At(
		            value = "FIELD",
		            target = "Lgregtech/api/logic/ProcessingLogic;activeDualInv:Lgregtech/common/tileentities/machines/IDualInputInventoryWithPattern;",
		            opcode = Opcodes.PUTFIELD,
		            shift=Shift.BEFORE
		        )
		    )
		    private void process(CallbackInfoReturnable ci) {
		       if(activeDualInv instanceof BufferedDualInputHatch.PatternDualInv){
		    	   
		    	   dualInvWithPatternToRecipeCache.remove(activeDualInv);
		    	   ((BufferedDualInputHatch.PatternDualInv)(activeDualInv)).reset++;
		       }
		    }
}
