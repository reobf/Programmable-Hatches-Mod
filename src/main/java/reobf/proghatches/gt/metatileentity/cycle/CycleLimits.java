package reobf.proghatches.gt.metatileentity.cycle;

public final class CycleLimits {

    public static final CycleLimits DEFAULT = new CycleLimits(6, 16, 400, 2_000_000L);

    public final int maxPatterns, maxItems, maxLpSolves;
    public final long maxEnumNodes, maxWork;

    public CycleLimits(int maxPatterns, int maxItems, int maxLpSolves, long maxEnumNodes) {
        this(maxPatterns, maxItems, maxLpSolves, maxEnumNodes, Long.MAX_VALUE);
    }

    public CycleLimits(int maxPatterns, int maxItems, int maxLpSolves, long maxEnumNodes, long maxWork) {
        if (maxPatterns < 1 || maxItems < 1 || maxLpSolves < 1 || maxEnumNodes < 1 || maxWork < 1)
            throw new IllegalArgumentException("limits must be >= 1");
        this.maxPatterns = maxPatterns;
        this.maxItems = maxItems;
        this.maxLpSolves = maxLpSolves;
        this.maxEnumNodes = maxEnumNodes;
        this.maxWork = maxWork;
    }
}
