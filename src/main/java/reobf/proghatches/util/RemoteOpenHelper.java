package reobf.proghatches.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Remote GUI opening, ported from GTNH's RemoteIO (remoteio.common.CommonProxy#activateBlock +
 * ServerProxyPlayer + ContainerHandler). Two tricks combined:
 * <ul>
 * <li>the block is activated with a shim EntityPlayerMP that shares the real player's connection,
 * inventory and window state but reports a constant distance of 6 blocks, so open-time range checks
 * pass;</li>
 * <li>the opened container is whitelisted for {@link PlayerOpenContainerEvent}; vanilla re-checks
 * {@code Container.canInteractWith} every tick and would close a far-away chest immediately, but the
 * event's ALLOW result overrides that check, keeping the GUI open at any distance.</li>
 * </ul>
 * Register {@link #INSTANCE} on the Forge event bus once during init.
 */
public class RemoteOpenHelper {

    public static final RemoteOpenHelper INSTANCE = new RemoteOpenHelper();

    private final Map<String, Container> containerWhitelist = new HashMap<>();

    @SubscribeEvent
    public void onContainerOpen(PlayerOpenContainerEvent event) {
        if (event.entityPlayer.openContainer != null
            && event.entityPlayer.openContainer != event.entityPlayer.inventoryContainer) {
            Container whitelisted = containerWhitelist.get(
                event.entityPlayer.getCommandSenderName());
            if (whitelisted != null && whitelisted == event.entityPlayer.openContainer) {
                event.setResult(Event.Result.ALLOW);
            }
        }
    }

    /** Forwards a right-click to the block at (x,y,z), keeping whatever GUI it opens interactable. */
    public static void activateBlock(World world, int x, int y, int z, EntityPlayerMP entityPlayerMP) {
        Container container = entityPlayerMP.openContainer;
        ProxyPlayer proxyPlayer = new ProxyPlayer(entityPlayerMP);

        proxyPlayer.playerNetServerHandler = entityPlayerMP.playerNetServerHandler;
        proxyPlayer.inventory = entityPlayerMP.inventory;
        proxyPlayer.currentWindowId = entityPlayerMP.currentWindowId;
        proxyPlayer.inventoryContainer = entityPlayerMP.inventoryContainer;
        proxyPlayer.openContainer = entityPlayerMP.openContainer;
        proxyPlayer.worldObj = world;
        proxyPlayer.setPositionAndRotation(x + 0.5, y + 0.5, z + 0.5, 0, 0);

        Block block = world.getBlock(x, y, z);
        if (block != null) {
            block.onBlockActivated(world, x, y, z, proxyPlayer, ForgeDirection.UP.ordinal(), 0.5F, 1.0F, 0.5F);
        }

        // the proxy hijacked theItemInWorldManager.thisPlayerMP in its constructor; restore it
        entityPlayerMP.theItemInWorldManager.thisPlayerMP = entityPlayerMP;
        if (container != proxyPlayer.openContainer) {
            entityPlayerMP.openContainer = proxyPlayer.openContainer;
        }

        INSTANCE.containerWhitelist.put(entityPlayerMP.getCommandSenderName(), entityPlayerMP.openContainer);
    }

    /** EntityPlayerMP shim that always appears to be within interaction range. */
    public static class ProxyPlayer extends EntityPlayerMP {

        public ProxyPlayer(EntityPlayerMP parentPlayer) {
            super(
                parentPlayer.mcServer,
                (WorldServer) parentPlayer.worldObj,
                parentPlayer.getGameProfile(),
                parentPlayer.theItemInWorldManager);
        }

        @Override
        public float getDistanceToEntity(Entity e) {
            return 6;
        }

        @Override
        public double getDistanceSq(double px, double py, double pz) {
            return 6;
        }

        @Override
        public double getDistance(double px, double py, double pz) {
            return 6;
        }

        @Override
        public double getDistanceSqToEntity(Entity e) {
            return 6;
        }
    }
}
