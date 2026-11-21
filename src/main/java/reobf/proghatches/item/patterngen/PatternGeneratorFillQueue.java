package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;

import reobf.proghatches.main.MyMod;

/**
 * Server-tick direct-fill queue.
 * <p>
 * The queue holds only recipe ids and target coordinates, never live GT objects. Each id is
 * removed and persisted before its pattern is placed, so a crash or restart can lose at most
 * one pattern but can never duplicate one. A queue pauses on logout, a missing item or a missing
 * map. A full target forgets its coordinates but keeps the pending ids and re-arms, so the player
 * can right-click another container. Missing/invalid targets and permission failures return the
 * remaining recipes through the normal inventory overflow path.
 */
public final class PatternGeneratorFillQueue {

    public static final int ARM_OK = 0;
    public static final int ARM_ALREADY_PENDING = -6;

    private static final int BATCH_SIZE = 4;
    private static final Map<UUID, ActiveFill> ACTIVE = new HashMap<>();
    private static final Map<UUID, String> STATUS = new HashMap<>();

    private PatternGeneratorFillQueue() {}

    private static final class ActiveFill {

        private final UUID player;
        private final int dimension;
        private final int x;
        private final int y;
        private final int z;

        private ActiveFill(UUID player, int dimension, int x, int y, int z) {
            this.player = player;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * Queues the current selection (or every filtered recipe) and arms the item for a target
     * right-click. Returns 0 on success, a {@link PatternGeneratorRecipes} error code, or
     * {@link #ARM_ALREADY_PENDING}.
     */
    public static int arm(EntityPlayerMP player, ItemStack tool, boolean selectedOnly) {
        if (player == null || tool == null) return PatternGeneratorRecipes.ERR_NO_RECIPES;
        PatternGeneratorSettings settings = PatternGeneratorSettings.load(tool);
        if (ACTIVE.containsKey(player.getUniqueID()) || settings.hasPending()) {
            notify(player, "proghatches.pattern_generator.target.exists");
            return ARM_ALREADY_PENDING;
        }

        PatternRecipeCollection collection = PatternGeneratorRecipes.collect(settings);
        if (collection.error != 0) {
            String key = PatternGeneratorRecipes.statusKey(collection.error);
            if (key != null) notify(player, key);
            return collection.error;
        }
        if (collection.hasFilterProblem()) {
            notify(player, "proghatches.pattern_generator.target.filter_invalid");
            return PatternGeneratorRecipes.ERR_INVALID_FILTER;
        }

        List<PatternRecipeEntry> entries = new ArrayList<>();
        if (selectedOnly) {
            if (settings.selectedRecipes.isEmpty()) {
                notify(player, "proghatches.pattern_generator.status.none_selected");
                return PatternGeneratorRecipes.ERR_NOTHING_SELECTED;
            }
            java.util.LinkedHashSet<String> effective = PatternRecipeSelection
                .prune(settings.selectedRecipes, collection.filteredIds());
            if (effective.isEmpty()) {
                notify(player, "proghatches.pattern_generator.status.none_selected");
                return PatternGeneratorRecipes.ERR_NOTHING_SELECTED;
            }
            for (PatternRecipeEntry entry : collection.filtered) {
                if (effective.contains(entry.id)) entries.add(entry);
            }
        } else {
            entries.addAll(collection.filtered);
        }
        if (entries.isEmpty()) {
            notify(player, "proghatches.pattern_generator.target.no_recipes");
            return PatternGeneratorRecipes.ERR_NO_RECIPES;
        }

        settings.clearPending();
        for (PatternRecipeEntry entry : entries) settings.pendingRecipes.add(entry.id);
        settings.pendingTotal = entries.size();
        settings.fillArmed = true;
        save(player, tool, settings);
        notify(player, "proghatches.pattern_generator.target.armed", entries.size());
        setStatus(player, "proghatches.pattern_generator.target.armed", entries.size());
        return ARM_OK;
    }

    /** Starts the queue against a clicked container after the item was armed. */
    public static boolean start(EntityPlayerMP player, ItemStack tool, PatternGeneratorSettings settings,
        int dimension, int x, int y, int z) {
        if (player == null || tool == null || settings == null || settings.pendingRecipes.isEmpty()) return false;
        UUID id = player.getUniqueID();
        if (ACTIVE.containsKey(id)) return false;
        settings.fillArmed = false;
        settings.setPendingTarget(dimension, x, y, z);
        save(player, tool, settings);
        ACTIVE.put(id, new ActiveFill(id, dimension, x, y, z));
        notify(
            player,
            "proghatches.pattern_generator.target.started_chat",
            settings.pendingRecipes.size());
        setStatus(player, "proghatches.pattern_generator.target.started", settings.pendingRecipes.size());
        return true;
    }

    /** Resumes a queue that was paused by logout or a restart, if it still has a target. */
    public static boolean resume(EntityPlayerMP player, ItemStack tool) {
        if (player == null || tool == null) return false;
        PatternGeneratorSettings settings = PatternGeneratorSettings.load(tool);
        if (!settings.hasPending() || ACTIVE.containsKey(player.getUniqueID())) return false;
        if (!settings.hasPendingTarget()) {
            settings.fillArmed = true;
            save(player, tool, settings);
            setStatus(player, "proghatches.pattern_generator.target.armed", settings.pendingRecipes.size());
            return true;
        }
        ACTIVE.put(
            player.getUniqueID(),
            new ActiveFill(
                player.getUniqueID(),
                settings.pendingDim,
                settings.pendingX,
                settings.pendingY,
                settings.pendingZ));
        return true;
    }

    public static boolean isActive(EntityPlayerMP player) {
        return player != null && ACTIVE.containsKey(player.getUniqueID());
    }

    public static String consumeStatus(EntityPlayerMP player) {
        return player == null ? null : STATUS.remove(player.getUniqueID());
    }

    public static void tick() {
        if (ACTIVE.isEmpty()) return;
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return;
        for (ActiveFill fill : new ArrayList<>(ACTIVE.values())) {
            EntityPlayerMP player = findPlayer(server, fill.player);
            if (player == null) {
                // Logout: stop ticking, keep the persisted queue for an explicit resume.
                ACTIVE.remove(fill.player);
                continue;
            }
            ItemStack tool = findTool(player);
            if (tool == null) {
                ACTIVE.remove(fill.player);
                setStatus(player, "proghatches.pattern_generator.target.item_missing");
                continue;
            }
            try {
                process(player, tool, fill);
            } catch (Throwable t) {
                if (MyMod.LOG != null) MyMod.LOG.error("Pattern generator direct fill failed", t);
                ACTIVE.remove(fill.player);
                setStatus(player, "proghatches.pattern_generator.target.invalid");
            }
        }
    }

    private static void process(EntityPlayerMP player, ItemStack tool, ActiveFill fill) {
        PatternGeneratorSettings settings = PatternGeneratorSettings.load(tool);
        if (!settings.hasPending()) {
            int total = settings.pendingTotal;
            ACTIVE.remove(fill.player);
            settings.clearPending();
            save(player, tool, settings);
            setStatus(player, "proghatches.pattern_generator.target.complete", total);
            return;
        }
        WorldServer world = null;
        try {
            world = MinecraftServer.getServer()
                .worldServerForDimension(fill.dimension);
        } catch (Throwable ignored) {}
        if (world == null) {
            pause(player, "proghatches.pattern_generator.target.invalid");
            return;
        }
        PatternGeneratorTarget target = PatternGeneratorTarget
            .resolve(world, fill.x, fill.y, fill.z, 0.5f, 0.5f, 0.5f);
        if (target == null || target.inventory == null) {
            stopAndReturn(player, tool, settings, null, "proghatches.pattern_generator.target.invalid");
            return;
        }
        if (!target.mayEdit(player)) {
            stopAndReturn(player, tool, settings, null, "proghatches.pattern_generator.target.permission");
            return;
        }

        PatternRecipeCollection collection = PatternGeneratorRecipes.collect(settings);
        if (collection.error != 0) {
            pause(player, "proghatches.pattern_generator.target.map_unavailable");
            return;
        }
        if (collection.hasFilterProblem()) {
            pause(player, "proghatches.pattern_generator.filter.invalid.status");
            return;
        }
        Map<String, PatternRecipeEntry> byId = new LinkedHashMap<>();
        for (PatternRecipeEntry entry : collection.all) byId.put(entry.id, entry);

        int placed = 0;
        while (placed < BATCH_SIZE && !settings.pendingRecipes.isEmpty()) {
            String id = settings.pendingRecipes.get(0);
            PatternRecipeEntry entry = byId.get(id);
            if (entry == null) {
                // The map/machine changed after the queue was armed. Stop instead of
                // silently dropping recipes that are only temporarily unavailable.
                pause(player, "proghatches.pattern_generator.target.map_unavailable");
                return;
            }
            ItemStack pattern;
            try {
                pattern = PatternGeneratorRecipes.encode(entry, player, settings);
            } catch (Throwable ignored) {
                pattern = null;
            }
            if (pattern == null) {
                settings.pendingRecipes.remove(0);
                save(player, tool, settings);
                continue;
            }
            if (PatternGeneratorDelivery.containsEquivalentPattern(target.inventory, pattern)) {
                settings.pendingRecipes.remove(0);
                save(player, tool, settings);
                continue;
            }
            int slot = PatternGeneratorDelivery.findSlot(target.inventory, pattern, target.patternCapacity());
            if (slot < 0) {
                // Full is temporary. Keep the pending ids, forget this container and re-arm
                // so the player can right-click another pattern container.
                rearmAfterFull(player, tool, settings);
                return;
            }
            // Persist the removal before the actual insertion. A crash can lose this one
            // pattern, but the queue can never insert it a second time.
            settings.pendingRecipes.remove(0);
            save(player, tool, settings);
            if (!PatternGeneratorDelivery.insertAt(target.inventory, slot, pattern)) {
                settings.pendingRecipes.add(0, id);
                save(player, tool, settings);
                stopAndReturn(player, tool, settings, collection, "proghatches.pattern_generator.target.invalid");
                return;
            }
            PatternGeneratorDelivery.markTargetDirty(target.tile, target.inventory);
            placed++;
        }

        if (settings.pendingRecipes.isEmpty()) {
            int total = settings.pendingTotal;
            ACTIVE.remove(fill.player);
            settings.clearPending();
            save(player, tool, settings);
            notify(player, "proghatches.pattern_generator.target.complete_chat", total);
            setStatus(player, "proghatches.pattern_generator.target.complete", total);
        }
    }

    /** Clears the active/persisted queue. Cancelling never generates patterns into the player's
     * inventory; it only discards the remaining task. */
    public static boolean cancel(EntityPlayerMP player, ItemStack tool) {
        if (player == null || tool == null) return false;
        ACTIVE.remove(player.getUniqueID());
        PatternGeneratorSettings settings = PatternGeneratorSettings.load(tool);
        boolean hadPending = settings.hasPending() || settings.fillArmed || settings.hasPendingTarget();
        settings.clearPending();
        save(player, tool, settings);
        setStatus(
            player,
            hadPending ? "proghatches.pattern_generator.target.canceled"
                : "proghatches.pattern_generator.target.none");
        return hadPending;
    }

    private static void stopAndReturn(EntityPlayerMP player, ItemStack tool, PatternGeneratorSettings settings,
        PatternRecipeCollection collection, String reasonKey) {
        ACTIVE.remove(player.getUniqueID());
        if (collection == null || collection.error != 0 || collection.hasFilterProblem()) {
            collection = PatternGeneratorRecipes.collect(settings);
            if (collection.error != 0 || collection.hasFilterProblem()) {
                pause(player, "proghatches.pattern_generator.target.map_unavailable");
                return;
            }
        }
        int returned = returnPending(player, settings, collection);
        settings.clearPending();
        save(player, tool, settings);
        String reason = StatCollector.translateToLocal(reasonKey);
        notify(player, "proghatches.pattern_generator.target.stopped_chat", reason, returned);
        setStatus(player, "proghatches.pattern_generator.target.stopped", reason, returned);
    }

    private static int returnPending(EntityPlayerMP player, PatternGeneratorSettings settings,
        PatternRecipeCollection collection) {
        if (collection == null || collection.error != 0 || collection.hasFilterProblem()) return 0;
        Map<String, PatternRecipeEntry> byId = new LinkedHashMap<>();
        for (PatternRecipeEntry entry : collection.all) byId.put(entry.id, entry);
        List<PatternRecipeEntry> leftovers = new ArrayList<>();
        for (String id : settings.pendingRecipes) {
            PatternRecipeEntry entry = byId.get(id);
            if (entry != null) leftovers.add(entry);
        }
        return PatternGeneratorRecipes.generateEntries(player, leftovers, settings);
    }

    private static void rearmAfterFull(EntityPlayerMP player, ItemStack tool, PatternGeneratorSettings settings) {
        ACTIVE.remove(player.getUniqueID());
        settings.clearPendingTarget();
        settings.fillArmed = true;
        save(player, tool, settings);
        int remaining = settings.pendingRecipes.size();
        setStatus(player, "proghatches.pattern_generator.target.full_rearmed", remaining);
        notify(player, "proghatches.pattern_generator.target.full_rearmed_chat", remaining);
    }

    private static void pause(EntityPlayerMP player, String reasonKey) {
        if (player != null) ACTIVE.remove(player.getUniqueID());
        String reason = StatCollector.translateToLocal(reasonKey);
        setStatus(player, "proghatches.pattern_generator.target.paused", reason);
        notify(player, "proghatches.pattern_generator.target.paused_chat", reason);
    }

    private static void save(EntityPlayerMP player, ItemStack tool, PatternGeneratorSettings settings) {
        settings.save(tool);
        if (player == null) return;
        try {
            player.inventory.markDirty();
            if (player.inventoryContainer != null) player.inventoryContainer.detectAndSendChanges();
        } catch (Throwable ignored) {}
    }

    private static EntityPlayerMP findPlayer(MinecraftServer server, UUID id) {
        if (server == null || id == null) return null;
        try {
            for (Object value : server.getConfigurationManager().playerEntityList) {
                if (value instanceof EntityPlayerMP player && id.equals(player.getUniqueID())) return player;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static ItemStack findTool(EntityPlayerMP player) {
        if (player == null || player.inventory == null) return null;
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() == MyMod.pattern_generator
            && PatternGeneratorSettings.load(held)
                .hasPending()) {
            return held;
        }
        if (player.inventory.mainInventory == null) return null;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() == MyMod.pattern_generator
                && PatternGeneratorSettings.load(stack)
                    .hasPending()) {
                return stack;
            }
        }
        return null;
    }

    private static void notify(EntityPlayerMP player, String key, Object... args) {
        if (player == null) return;
        try {
            player.addChatMessage(new ChatComponentTranslation(key, args));
        } catch (Throwable ignored) {}
    }

    private static void setStatus(EntityPlayerMP player, String key, Object... args) {
        if (player == null) return;
        String text;
        try {
            text = StatCollector.translateToLocalFormatted(key, args);
        } catch (Throwable ignored) {
            text = key;
        }
        STATUS.put(player.getUniqueID(), text);
    }
}
