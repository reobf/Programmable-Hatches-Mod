package reobf.proghatches.main.mixin.mixins.eucrafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.common.covers.CoverInfo;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.eucrafting.AECover.Data;

@Mixin(value = CoverableTileEntity.class, remap = false)
public abstract class MixinEUSourceCoverChunkUnloadNotification extends BaseTileEntity {

	@Override
	public void onChunkUnload() {
		unloadCover();
		super.onChunkUnload();
	}

	@Shadow
	public abstract CoverInfo getCoverInfoAtSide(ForgeDirection side);

	protected void unloadCover() {
		for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			final CoverInfo coverInfo = getCoverInfoAtSide(side);
			if (coverInfo.isValid()) {

				GT_CoverBehaviorBase<?> be = coverInfo.getCoverBehavior();
				if (be != null && be instanceof AECover) {
					((AECover) be).chunkUnload((Data) coverInfo.getCoverData());

				}

			}
		}
	}

}
