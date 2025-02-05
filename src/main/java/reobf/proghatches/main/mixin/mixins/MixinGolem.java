package reobf.proghatches.main.mixin.mixins;

import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import reobf.proghatches.thaum.AIFix;
import thaumcraft.common.entities.golems.EntityGolemBase;

@Mixin(value = EntityGolemBase.class, remap = false)
public abstract class MixinGolem extends EntityGolem {

    public MixinGolem(World p_i1686_1_) {
        super(p_i1686_1_);

    }

    @Inject(method = "setupGolem", at = @At("RETURN"), remap = false, require = 1)
    public void setupGolem(CallbackInfoReturnable c) {

        if (getCore() == 120) {

            tasks.addTask(2, new AIFix(this));
        }
    }

    @Shadow
    public abstract byte getCore();
}
