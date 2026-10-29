package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.util.ParallelHelper;

@Mixin(value = ParallelHelper.class, remap = false)
public interface ParallelHelperAccessor {

    @Accessor("chanceMultiplier")
    double ph$chanceMultiplier();
}
