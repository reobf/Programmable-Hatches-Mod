package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Result of resolving and filtering one recipe map. */
public final class PatternRecipeCollection {

    public static final class FilterProblem {

        public final FilterSection section;
        public final String pattern;

        FilterProblem(FilterSection section, String pattern) {
            this.section = section;
            this.pattern = pattern;
        }
    }

    public enum FilterSection {

        INPUT_INCLUDE("proghatches.pattern_generator.filter.input_include"),
        INPUT_EXCLUDE("proghatches.pattern_generator.filter.input_exclude"),
        OUTPUT_INCLUDE("proghatches.pattern_generator.filter.output_include"),
        OUTPUT_EXCLUDE("proghatches.pattern_generator.filter.output_exclude");

        public final String langKey;

        FilterSection(String langKey) {
            this.langKey = langKey;
        }
    }

    public final int error;
    public final String mapId;
    public final String mapName;
    public final List<PatternRecipeEntry> all;
    public final List<PatternRecipeEntry> filtered;
    public final List<FilterProblem> filterProblems;

    PatternRecipeCollection(int error, String mapId, String mapName, List<PatternRecipeEntry> all,
        List<PatternRecipeEntry> filtered, List<FilterProblem> filterProblems) {
        this.error = error;
        this.mapId = mapId == null ? "" : mapId;
        this.mapName = mapName == null ? "" : mapName;
        this.all = all == null ? Collections.emptyList() : all;
        this.filtered = filtered == null ? Collections.emptyList() : filtered;
        this.filterProblems = filterProblems == null ? Collections.emptyList() : filterProblems;
    }

    public static PatternRecipeCollection error(int error) {
        return new PatternRecipeCollection(error, "", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public boolean hasFilterProblem() {
        return !this.filterProblems.isEmpty();
    }

    public FilterProblem firstFilterProblem() {
        return this.filterProblems.isEmpty() ? null : this.filterProblems.get(0);
    }

    public List<String> allIds() {
        List<String> ids = new ArrayList<>(this.all.size());
        for (PatternRecipeEntry entry : this.all) ids.add(entry.id);
        return ids;
    }

    public List<String> filteredIds() {
        List<String> ids = new ArrayList<>(this.filtered.size());
        for (PatternRecipeEntry entry : this.filtered) ids.add(entry.id);
        return ids;
    }
}
