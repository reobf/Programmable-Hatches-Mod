package reobf.proghatches.gt.metatileentity.util;

import java.util.BitSet;
import java.util.function.IntPredicate;

/** Stable pattern indices with a persistent, monotonically growing number of accessible pages. */
public final class PatternSlotMask {

    public static final int PAGE_SIZE = 36;
    public static final int MAX_PAGES = 10;
    public static final int CAPACITY = PAGE_SIZE * MAX_PAGES;

    private final BitSet occupied = new BitSet(CAPACITY);
    private int occupiedCount;
    private int pages = 1;

    public int pages() {
        return pages;
    }

    public int unlockedSlots() {
        return pages * PAGE_SIZE;
    }

    public int nextOccupied(int from) {
        return occupied.nextSetBit(from);
    }

    public void update(int slot, boolean present) {
        if (slot < 0 || slot >= unlockedSlots()) throw new IndexOutOfBoundsException("Locked pattern slot: " + slot);
        if (occupied.get(slot) != present) {
            occupied.set(slot, present);
            occupiedCount += present ? 1 : -1;
        }
        growIfFull();
    }

    /** Rebuild derived state once on load, retaining even items beyond a missing/stale page count. */
    public void restore(int savedPages, IntPredicate present) {
        pages = Math.max(1, Math.min(MAX_PAGES, savedPages));
        occupied.clear();
        occupiedCount = 0;
        for (int slot = 0; slot < CAPACITY; slot++) {
            if (present.test(slot)) {
                occupied.set(slot);
                occupiedCount++;
                pages = Math.max(pages, slot / PAGE_SIZE + 1);
            }
        }
        growIfFull();
    }

    private void growIfFull() {
        if (pages < MAX_PAGES && occupiedCount == unlockedSlots()) pages++;
    }
}
