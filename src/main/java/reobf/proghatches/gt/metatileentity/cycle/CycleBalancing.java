package reobf.proghatches.gt.metatileentity.cycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CycleBalancing<K> {

    private final long[] runs;
    private final int[] patternIds;
    private final long totalRuns;
    private final List<CycleAmount<K>> netInputs;
    private final List<CycleAmount<K>> netOutputs;
    private final List<CycleAmount<K>> surplus;
    private final List<CycleAmount<K>> seeds;
    private final List<CycleAmount<K>> seedNeed;
    private final List<K> products;
    private final boolean publishable;

    CycleBalancing(long[] runs, int[] patternIds, List<CycleAmount<K>> netInputs, List<CycleAmount<K>> netOutputs,
        List<CycleAmount<K>> surplus, List<CycleAmount<K>> seeds, List<CycleAmount<K>> seedNeed, List<K> products,
        boolean publishable) {
        this.runs = runs.clone();
        this.patternIds = patternIds.clone();
        long sum = 0;
        for (long r : runs) sum = Math.addExact(sum, r);
        this.totalRuns = sum;
        this.netInputs = copy(netInputs);
        this.netOutputs = copy(netOutputs);
        this.surplus = copy(surplus);
        this.seeds = copy(seeds);
        this.seedNeed = copy(seedNeed);
        this.products = copy(products);
        this.publishable = publishable;
    }

    static <T> List<T> copy(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public long[] getRuns() {
        return runs.clone();
    }

    public int[] getPatternIds() {
        return patternIds.clone();
    }

    public long getTotalRuns() {
        return totalRuns;
    }

    public List<CycleAmount<K>> getNetInputs() {
        return netInputs;
    }

    public List<CycleAmount<K>> getNetOutputs() {
        return netOutputs;
    }

    public List<CycleAmount<K>> getSurplus() {
        return surplus;
    }

    public List<CycleAmount<K>> getSeeds() {
        return seeds;
    }

    public List<CycleAmount<K>> getSeedNeed() {
        return seedNeed;
    }

    public List<K> getProducts() {
        return products;
    }

    public boolean isPublishable() {
        return publishable;
    }

    @Override
    public String toString() {
        return "runs=" + Arrays.toString(runs) + " products=" + products + " in=" + netInputs + " out=" + netOutputs
            + " surplus=" + surplus + " seeds=" + seeds + " need=" + seedNeed + (publishable ? "" : " (unpublished)");
    }
}
