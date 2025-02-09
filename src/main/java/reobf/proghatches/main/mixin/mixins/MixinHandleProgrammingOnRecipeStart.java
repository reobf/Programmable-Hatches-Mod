package reobf.proghatches.main.mixin.mixins;

import java.util.Arrays;
import java.util.Objects;

import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import reobf.proghatches.gt.cover.IProgrammer;

@Mixin(value = MTEMultiBlockBase.class, remap = false)
public abstract class MixinHandleProgrammingOnRecipeStart {

    // spotless:off
    @ModifyVariable(
    method = "startRecipeProcessing",ordinal=0/*set it to 0 to enable explicit mode or mixin will raise warnings*/,
    at = @At(opcode = Opcodes.ASTORE, value = "STORE"/*"reobf.proghatches.main.mixin.StoreInjectionPoint"*/), require = 1)
    public MTEHatchInputBus startRecipeProcessing(MTEHatchInputBus a) {
    	//spotless:on
        try {
            MTEHatchInputBus bus = (MTEHatchInputBus) a;
            Arrays.stream(ForgeDirection.VALID_DIRECTIONS)
                .map(
                    s -> bus.getBaseMetaTileEntity()
                    .getCoverInfoAtSide(s).getCoverBehavior())
                .filter(Objects::nonNull)
                .filter(s -> s instanceof IProgrammer)
                .forEach(s -> ((IProgrammer) s).impl(bus.getBaseMetaTileEntity()));;

        } catch (Exception e) {
            // huh?
            e.printStackTrace();
        }
        return a;

    }
    // spotless:off
    /*
     * opcode = Opcodes.ASTORE seems to be useless?
     * [14:57:37] [Client thread/WARN] [mixin]: @At("STORE" implicit MTEHatchInputBus) has invalid IMPLICIT
     * discriminator for opcode 8 in
     * gregtech/api/metatileentity/implementations/MTEMultiBlockBase::startRecipeProcessing()V: Found 0 candidate
     * variables but exactly 1 is required.
     * [14:57:37] [Client thread/WARN] [mixin]: @At("STORE" implicit MTEHatchInputBus) has invalid IMPLICIT
     * discriminator for opcode 51 in
     * gregtech/api/metatileentity/implementations/MTEMultiBlockBase::startRecipeProcessing()V: Found 0 candidate
     * variables but exactly 1 is required.
     * [14:57:37] [Client thread/WARN] [mixin]: @At("STORE" implicit MTEHatchInputBus) has invalid IMPLICIT
     * discriminator for opcode 59 in
     * gregtech/api/metatileentity/implementations/MTEMultiBlockBase::startRecipeProcessing()V: Found 0 candidate
     * variables but exactly 1 is required.
     */
}
