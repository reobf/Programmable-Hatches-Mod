package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.RecipeMapWorkable;
import gregtech.api.recipe.RecipeMap;
import gregtech.common.blocks.ItemMachines;

/** Resolves a GT machine item to the recipe maps its prototype can execute. */
public final class PatternMachineResolver {

    private PatternMachineResolver() {}

    public static boolean isMachineItem(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        try {
            return ItemMachines.getMetaTileEntity(stack) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Returns the machine's available maps in a stable order. Deduplicated by map id.
     * Machines from GT and addons registered through GT's MTE registry work here by default;
     * TST is one such addon.
     */
    public static List<RecipeMap<?>> resolveMaps(ItemStack machine) {
        List<RecipeMap<?>> result = new ArrayList<>();
        if (machine == null || machine.getItem() == null) return result;

        IMetaTileEntity mte;
        try {
            mte = ItemMachines.getMetaTileEntity(machine);
        } catch (Throwable ignored) {
            return result;
        }
        if (!(mte instanceof RecipeMapWorkable workable)) return result;

        Map<String, RecipeMap<?>> unique = new LinkedHashMap<>();
        try {
            Collection<RecipeMap<?>> available = workable.getAvailableRecipeMaps();
            if (available != null) {
                for (RecipeMap<?> map : available) add(unique, map);
            }
        } catch (Throwable ignored) {
            // Fall through to getRecipeMap below.
        }
        if (unique.isEmpty()) {
            try {
                add(unique, workable.getRecipeMap());
            } catch (Throwable ignored) {}
        }
        result.addAll(unique.values());
        return result;
    }

    public static RecipeMap<?> selectMap(List<RecipeMap<?>> maps, String id) {
        if (maps == null || maps.isEmpty()) return null;
        if (id != null && !id.isEmpty()) {
            for (RecipeMap<?> map : maps) {
                if (map != null && id.equals(map.unlocalizedName)) return map;
            }
        }
        return maps.get(0);
    }

    public static String cycleMapId(List<RecipeMap<?>> maps, String current) {
        if (maps == null || maps.isEmpty()) return "";
        int index = 0;
        if (current != null) {
            for (int i = 0; i < maps.size(); i++) {
                RecipeMap<?> map = maps.get(i);
                if (map != null && current.equals(map.unlocalizedName)) {
                    index = (i + 1) % maps.size();
                    break;
                }
            }
        }
        RecipeMap<?> next = maps.get(index);
        return next == null ? "" : next.unlocalizedName;
    }

    public static String mapDisplayName(String id) {
        if (id == null || id.isEmpty()) return "";
        String translated = StatCollector.translateToLocal(id);
        return translated == null || translated.isEmpty() ? id : translated;
    }

    private static void add(Map<String, RecipeMap<?>> unique, RecipeMap<?> map) {
        if (map == null || map.unlocalizedName == null) return;
        unique.putIfAbsent(map.unlocalizedName, map);
    }
}
