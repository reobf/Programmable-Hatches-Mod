package reobf.proghatches.item.patterngen;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/** Pure helpers for pruning and counting persisted recipe selections. */
public final class PatternRecipeSelection {

    private PatternRecipeSelection() {}

    /**
     * Keeps only ids that still exist in the current map and preserves selection order.
     * Stale ids are dropped silently.
     */
    public static LinkedHashSet<String> prune(Collection<String> selected, Collection<String> valid) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (selected == null || valid == null) return result;
        Set<String> validSet = valid instanceof Set<?> set ? castSet(set) : new HashSet<>(valid);
        for (String id : selected) {
            if (id != null && validSet.contains(id)) result.add(id);
        }
        return result;
    }

    public static int countMatching(Collection<String> selected, Collection<String> ids) {
        if (selected == null || selected.isEmpty() || ids == null || ids.isEmpty()) return 0;
        Set<String> selectedSet = selected instanceof Set<?> set ? castSet(set) : new HashSet<>(selected);
        int count = 0;
        for (String id : ids) {
            if (id != null && selectedSet.contains(id)) count++;
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> castSet(Set<?> set) {
        return (Set<String>) set;
    }
}
