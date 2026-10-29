package reobf.proghatches.main;

import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

import appeng.items.misc.ItemEncodedPattern;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import reobf.proghatches.ae.TileMolecularAssemblerInterface;
import reobf.proghatches.ae.render.TESRMAInterface;
import reobf.proghatches.client.ChancePatternRenderer;
import reobf.proghatches.client.CircuitSpecialRenderer;
import reobf.proghatches.client.ClientSubItemDamages;

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
        // answers the server's sub item questions on the client thread
        FMLCommonHandler.instance()
            .bus()
            .register(ClientSubItemDamages.INSTANCE);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        ChancePatternRenderer renderer = new ChancePatternRenderer();
        for (Object item : Item.itemRegistry)
            if (item instanceof ItemEncodedPattern p) MinecraftForgeClient.registerItemRenderer(p, renderer);
    }

    @Override
    public void answerSubItemRequest(int[] ids) {
        ClientSubItemDamages.INSTANCE.ask(ids);
    }

}
