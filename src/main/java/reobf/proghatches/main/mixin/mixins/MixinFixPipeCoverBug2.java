package reobf.proghatches.main.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

@Mixin(value=BaseMetaPipeEntity.class,remap=false,priority=1000)
public class MixinFixPipeCoverBug2 {
	
	
	@ModifyArg(remap=false,method="onRightclick", require = 1,at =  @At(value="INVOKE",target = 
	"onPlayerAttach"
,remap=false) )
	public ForgeDirection onRightclick( ForgeDirection sd) {
		
		try{
		return realSide;
		}finally{
			
		//realSide=null;//set to null?
		}
	}
	
	ForgeDirection realSide;
	

		   
		   
	   
}
