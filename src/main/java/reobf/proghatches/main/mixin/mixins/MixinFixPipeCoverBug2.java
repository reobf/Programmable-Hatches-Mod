package reobf.proghatches.main.mixin.mixins;

import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import gregtech.api.metatileentity.BaseMetaPipeEntity;

@Mixin(value = BaseMetaPipeEntity.class, remap = false, priority = 1000)
public class MixinFixPipeCoverBug2 {
/*
    @ModifyArg(
        remap = false,
        method = "onRightclick",
        require = 1,
        at = @At(value = "INVOKE", target = "onPlayerAttach", remap = false))
    public ForgeDirection onRightclick(ForgeDirection sd) {

        try {
            return realSide;
        } finally {

            // realSide=null;//set to null?
        }
    }

    ForgeDirection realSide;
*/
}
