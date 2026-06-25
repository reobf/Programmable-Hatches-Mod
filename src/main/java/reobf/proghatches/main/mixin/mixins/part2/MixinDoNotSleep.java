package reobf.proghatches.main.mixin.mixins.part2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.vfyjxf.nee.block.tile.TilePatternInterface;

import appeng.helpers.DualityInterface;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.CommonBaseMetaTileEntity;
import reobf.proghatches.gt.metatileentity.util.IDoNotSleep;

@Mixin(value = { CommonBaseMetaTileEntity.class, TilePatternInterface.class }, remap = false)
public abstract class MixinDoNotSleep implements IGregTechTileEntity{

	
	@Inject(method="tryDisableTicking", at = { @At("HEAD") },cancellable = true)
    public void tryDisableTicking0(CallbackInfo v) {
		if(this.getMetaTileEntity() instanceof IDoNotSleep y&&y.really())
v.cancel();
    }
	
	
	
}
