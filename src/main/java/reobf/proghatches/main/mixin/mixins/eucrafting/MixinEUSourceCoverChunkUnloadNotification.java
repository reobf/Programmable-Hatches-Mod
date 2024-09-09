package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.SoftOverride;

import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.common.covers.CoverInfo;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.eucrafting.AECover.Data;
import reobf.proghatches.main.MyMod;

@Mixin(value = CoverableTileEntity.class, remap = true)
public abstract class MixinEUSourceCoverChunkUnloadNotification extends BaseTileEntity {

	@Override
	public void onChunkUnload() {
		unloadCover();
		super.onChunkUnload();
	}

	@Shadow
	public abstract CoverInfo getCoverInfoAtSide(ForgeDirection side);

	protected void unloadCover() {
		try{
		for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			final CoverInfo coverInfo = getCoverInfoAtSide(side);
			if (coverInfo.isValid()) {

				Object be = coverInfo.getCoverBehavior();
				
				
				if (be != null && be instanceof AECover) {
					((AECover) be).chunkUnload((Data) coverInfo.getCoverData());

				}

			}
		}
		}catch(Exception e){MyMod.LOG.error("caught error in mixin",e);}
	}

}
