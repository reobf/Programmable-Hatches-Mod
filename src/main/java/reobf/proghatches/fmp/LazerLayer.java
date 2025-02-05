package reobf.proghatches.fmp;

import java.util.Collection;
import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import appeng.api.util.AEColor;
import appeng.parts.AEBasePart;
import gregtech.api.interfaces.tileentity.IColoredTileEntity;
import reobf.proghatches.eucrafting.ILazer;
import reobf.proghatches.eucrafting.PartLazerP2P.RestrictedTarget;

public class LazerLayer extends LayerBase implements ILazer, IColoredTileEntity {

    @Override
    public boolean canConnect(ForgeDirection side) {
        IPart pt = getPart(side);
        if (pt instanceof ILazer) {
            return ((ILazer) pt).canConnect(side);
        }
        return false;
    }

    @Override
    public byte getColorization() {
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            IPart pt = getPart(side);
            if (pt instanceof ILazer) {
                if (pt instanceof AEBasePart) {
                    AEColor col = ((AEBasePart) pt).getHost()
                        .getColor();
                    if (col == AEColor.Transparent) return -1;

                    return (byte) (15 - col.ordinal());
                }

            }
        }

        return -1;
    }

    @Override
    public byte setColorization(byte aColor) {

        return -1;
    }

    public boolean isHost() {
        return true;
    }

    public ILazer getLazerP2PIn(ForgeDirection dir) {
        IPart pt = getPart(dir);
        if (pt instanceof ILazer) {
            return ((ILazer) pt).getLazerP2PIn(dir);
        }

        return null;
    }

    public ForgeDirection getLazerDir() {
        return null;
    }

    public List<ILazer> getLazerP2POuts() {
        return null;
    }

    @Override
    public Collection<? extends RestrictedTarget> collectAllEndpoints() {
        return null;
    }

    @Override
    public Object getForward() {
        // TODO Auto-generated method stub
        return null;
    }
}
