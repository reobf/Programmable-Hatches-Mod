package reobf.proghatches.main;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import reobf.proghatches.client.CircuitSpecialRenderer;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.
    @Override
    public void preInit(FMLPreInitializationEvent event) {

        super.preInit(event);
        MinecraftForgeClient.registerItemRenderer(MyMod.progcircuit, new CircuitSpecialRenderer());
    }
}
