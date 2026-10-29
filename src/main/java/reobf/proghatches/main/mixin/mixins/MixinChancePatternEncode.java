package reobf.proghatches.main.mixin.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.parts.IPatternTerminal;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.helpers.PatternEncodingHelper;
import reobf.proghatches.gt.metatileentity.chance.ChanceMark;

@Mixin(value = PatternEncodingHelper.class, remap = false)
public class MixinChancePatternEncode {

    @Inject(method = "encode", at = @At("RETURN"), require = 1)
    private static void ph$mark(IPatternTerminal terminal, IEnergySource powerSource,
        IMEMonitor<IAEItemStack> itemMonitor, BaseActionSource actionSource, String auther, World world,
        CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) ItemStack output) {
        if (!cir.getReturnValueZ() || output == null || !output.hasTagCompound()) return;
        if (actionSource instanceof PlayerSource ps && ChanceMark.holdingToolkit(ps.player))
            output.getTagCompound()
                .setBoolean(ChanceMark.KEY, true);
    }
}
