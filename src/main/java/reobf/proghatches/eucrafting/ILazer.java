package reobf.proghatches.eucrafting;

import java.util.Collection;

import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.interfaces.tileentity.IColoredTileEntity;
import reobf.proghatches.eucrafting.PartLazerP2P.RestrictedTarget;
import tectech.mechanics.pipe.IConnectsToEnergyTunnel;

public interface ILazer extends IConnectsToEnergyTunnel, IColoredTileEntity {

    // public boolean isHost();
    public ILazer getLazerP2PIn(ForgeDirection dir);

    public ForgeDirection getLazerDir();

    // public List<ILazer> getLazerP2POuts();
    // public default TileEntity findConnected(){return null;};
    public Collection<? extends RestrictedTarget> collectAllEndpoints();

    public Object getForward();
}
