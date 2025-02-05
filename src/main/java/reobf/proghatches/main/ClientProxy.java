package reobf.proghatches.main;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import reobf.proghatches.ae.TileMolecularAssemblerInterface;
import reobf.proghatches.ae.render.TESRMAInterface;
import reobf.proghatches.client.CircuitSpecialRenderer;

public class ClientProxy extends CommonProxy {

    @SuppressWarnings("unchecked")
    @Override
    public void preInit(FMLPreInitializationEvent event) {

        super.preInit(event);
        MinecraftForgeClient.registerItemRenderer(MyMod.progcircuit, new CircuitSpecialRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer/*
                                                     * (
                                                     * TileEntityRendererDispatcher.instance.mapSpecialRenderers.put
                                                     */(TileMolecularAssemblerInterface.class, new TESRMAInterface());
    }

}
