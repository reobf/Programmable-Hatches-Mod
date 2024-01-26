package reobf.proghatches.main.mixin.mixins;

import java.util.Arrays;
import java.util.Objects;

import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import reobf.proghatches.gt.cover.ProgrammingCover;

@SuppressWarnings("unused")
@Mixin(value = GT_MetaTileEntity_MultiBlockBase.class, remap = false)
public abstract class MixinHandleProgrammingOnRecipeStart {

    // GT_MetaTileEntity_Hatch_InputBus

    @SuppressWarnings("rawtypes")
    @ModifyVariable(method = "startRecipeProcessing", at = @At(opcode = Opcodes.ASTORE, value = "STORE"), require = 1)
    public GT_MetaTileEntity_Hatch_InputBus startRecipeProcessing(GT_MetaTileEntity_Hatch_InputBus a) {

        try {
            GT_MetaTileEntity_Hatch_InputBus bus = (GT_MetaTileEntity_Hatch_InputBus) a;
            Arrays.stream(ForgeDirection.VALID_DIRECTIONS)
                .map(
                    s -> bus.getBaseMetaTileEntity()
                        .getCoverBehaviorAtSideNew(s))
                .filter(Objects::nonNull)
                .filter(s -> s instanceof ProgrammingCover)
                .forEach(s -> ((ProgrammingCover) s).impl(bus.getBaseMetaTileEntity()));;

        } catch (Exception e) {
            // huh?
            e.printStackTrace();
        }
        return a;

        // MixinCallback.handleAddedToMachineList(aTileEntity,this);
    }

}
