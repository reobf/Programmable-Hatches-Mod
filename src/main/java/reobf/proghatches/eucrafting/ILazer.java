package reobf.proghatches.eucrafting;

import java.util.List;

import com.github.technus.tectech.mechanics.pipe.IConnectsToEnergyTunnel;

import gregtech.api.interfaces.tileentity.IColoredTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface ILazer extends IConnectsToEnergyTunnel,IColoredTileEntity{
public  boolean isHost();
public ILazer getLazerP2PIn(ForgeDirection dir);
public ForgeDirection getLazerDir();
public List<ILazer> getLazerP2POuts();
public default TileEntity findConnected(){return null;};
}
