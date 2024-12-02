package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.vfyjxf.nee.block.tile.TilePatternInterface;

import appeng.helpers.DualityInterface;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.fmp.LayerCraftingMachine;
import reobf.proghatches.fmp.LayerCraftingMachine.StateHolder;

@Mixin(value={DualityInterface.class,TilePatternInterface.class},remap=false)
public class MixinDirectionCapture {

	
	
	@ModifyVariable( require = 1,method="pushPattern",at =  @At(
			value="INVOKE",target="Lappeng/api/implementations/tiles/ICraftingMachine;acceptsPlans()Z"
		,shift=Shift.BEFORE	) )
	public ForgeDirection cap(ForgeDirection i){
		//System.out.println(i);
		LayerCraftingMachine.StateHolder.state=i.getOpposite();
		return i;
	}
	
	
	
	
	
	
	
	
	
}
