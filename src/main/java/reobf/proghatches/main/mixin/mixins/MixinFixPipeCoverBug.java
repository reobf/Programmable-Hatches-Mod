package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

@Mixin(value=BaseMetaPipeEntity.class,remap=false,priority=999)
public class MixinFixPipeCoverBug{
	

	ForgeDirection realSide;
	
	@Inject(remap=false,method="onRightclick",at=@At(value="INVOKE",target = 
				"onPlayerAttach"), require = 1)
		public void onRightclick0(EntityPlayer aPlayer, ForgeDirection side, float aX, float aY, float aZ,CallbackInfoReturnable<Boolean> cc) {
			realSide=side;
			if(((CoverableTileEntity)(Object)this).getCoverIDAtSide(side) == 0)
			realSide=GT_Utility.determineWrenchingSide(side, aX, aY, aZ);
			
			}
		   
		   
		   
		   
	   
}
