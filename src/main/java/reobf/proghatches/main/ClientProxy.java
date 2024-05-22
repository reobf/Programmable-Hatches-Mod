package reobf.proghatches.main;

import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import reobf.proghatches.client.CircuitSpecialRenderer;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {

		super.preInit(event);
		MinecraftForgeClient.registerItemRenderer(MyMod.progcircuit, new CircuitSpecialRenderer());

	}

}
