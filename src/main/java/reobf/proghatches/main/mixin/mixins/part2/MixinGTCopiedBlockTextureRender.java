package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Constant;
import appeng.crafting.v2.resolvers.ExtractItemResolver;
import gregtech.common.render.GTCopiedBlockTextureRender;
import reobf.proghatches.util.CTexture;

@Mixin(value =GTCopiedBlockTextureRender.class, remap = false)
public class MixinGTCopiedBlockTextureRender implements CTexture.Color{
	
	@Unique
	private Integer color_opt;
	
	
	
	
	@ModifyConstant(
	        method = "/^.*$/", 
	        constant = @Constant(intValue = 0xffffff) 
	    )
	    private int modifyConstantHandler(int original) {
		if(color_opt!=null){
			
			return color_opt;
			
		}
	        return original; 
	    }




	@Override
	public void set(int x) {
		color_opt=x;
		
	}
}
