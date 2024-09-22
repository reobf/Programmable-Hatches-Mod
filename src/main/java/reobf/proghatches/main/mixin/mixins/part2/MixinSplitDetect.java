package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.me.GridConnection;
import appeng.me.GridNode;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.block.ChunkTrackingGridCahce;
import reobf.proghatches.block.IChunkTrackingGridCahce;
/**
 * AE GridCache opSplit is not working as expected(?)
 * so check it manually
 * */
@Mixin(value=GridConnection.class,remap=false)
public class MixinSplitDetect {
	@Shadow
	private GridNode sideA;
	@Shadow
	    private GridNode sideB;
	@Inject(method="destroy",at = { @At("RETURN") })public void a(CallbackInfo a)
	{
		
		if(sideA.getGrid()!=sideB.getGrid()){
			ChunkTrackingGridCahce.merge	(
			(ChunkTrackingGridCahce)sideA.getGrid().getCache(IChunkTrackingGridCahce.class),
			(ChunkTrackingGridCahce)sideB.getGrid().getCache(IChunkTrackingGridCahce.class));
			
			
		}else{
			//System.out.println(sideA.getGrid());
			
		}
		
		
	}

}
