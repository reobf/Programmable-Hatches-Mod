package reobf.proghatches.gt.metatileentity.cycle;

import java.util.ArrayList;
import java.util.List;

public final class CycleAnalysis<K> {

    private final List<K> items;
    private final List<K> cyclic;
    private final List<K> products;
    private final List<CycleBalancing<K>> balancings;
    private final List<CycleBalancing<K>> publishable;
    private final List<K> unsolved;
    private final List<CycleWarning<K>> warnings;
    private final long nodesVisited;
    private final int patternCount;

    CycleAnalysis(List<K> items, List<K> cyclic, List<K> products, List<CycleBalancing<K>> balancings,
        List<K> unsolved, List<CycleWarning<K>> warnings, long nodesVisited, int patternCount) {
        this.items = CycleBalancing.copy(items);
        this.cyclic = CycleBalancing.copy(cyclic);
        this.products = CycleBalancing.copy(products);
        this.balancings = CycleBalancing.copy(balancings);
        List<CycleBalancing<K>> pub = new ArrayList<>();
        for (CycleBalancing<K> b : balancings) if (b.isPublishable()) pub.add(b);
        this.publishable = CycleBalancing.copy(pub);
        this.unsolved = CycleBalancing.copy(unsolved);
        this.warnings = CycleBalancing.copy(warnings);
        this.nodesVisited = nodesVisited;
        this.patternCount = patternCount;
    }

    public List<K> getItems() {
        return items;
    }

    public List<K> getCyclic() {
        return cyclic;
    }

    public List<K> getProducts() {
        return products;
    }

    public List<CycleBalancing<K>> getBalancings() {
        return balancings;
    }

    public List<CycleBalancing<K>> getPublishable() {
        return publishable;
    }

    public List<K> getUnsolved() {
        return unsolved;
    }

    public List<CycleWarning<K>> getWarnings() {
        return warnings;
    }

    public long getNodesVisited() {
        return nodesVisited;
    }

    public int getPatternCount() {
        return patternCount;
    }
}
