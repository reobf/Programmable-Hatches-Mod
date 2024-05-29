package reobf.proghatches.main;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IDefaultGuiHandler extends IGuiHandler {
@Override
default Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
	
	throw new RuntimeException();
}
@Override
default Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		
	throw new RuntimeException();
}
}
