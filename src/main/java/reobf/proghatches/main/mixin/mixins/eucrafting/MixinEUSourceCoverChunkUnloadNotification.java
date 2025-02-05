package reobf.proghatches.main.mixin.mixins.eucrafting;

import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.util.CoverBehaviorBase;
import gregtech.common.covers.CoverInfo;
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
        try {
            for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                final CoverInfo coverInfo = getCoverInfoAtSide(side);
                if (coverInfo.isValid()) {

                    CoverBehaviorBase be = coverInfo.getCoverBehavior();

                    if (be != null && be instanceof AECover) {
                        ((AECover) be).chunkUnload((Data) coverInfo.getCoverData());

                    }

                }
            }
        } catch (Exception e) {
            MyMod.LOG.error("caught error in mixin", e);
        }
    }

}
