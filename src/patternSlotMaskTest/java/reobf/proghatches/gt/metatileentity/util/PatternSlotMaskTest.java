package reobf.proghatches.gt.metatileentity.util;

import java.util.Random;

/** Dependency-free regression checks; run with the testPatternSlotMask Gradle task. */
public final class PatternSlotMaskTest {

    public static void main(String[] args) {
        growsOnlyWhenFullAndNeverShrinks();
        survivesReloadAndRepairsPageCounts();
        skipsEmptySlotsWithoutRenumbering();
        rejectsLockedSlots();
        randomEditsMatchIndependentInventoryModel();
        System.out.println("PatternSlotMask: all 5 regression checks passed");
    }

    private static void growsOnlyWhenFullAndNeverShrinks() {
        PatternSlotMask mask = new PatternSlotMask();
        equal(36, mask.unlockedSlots());
        for (int slot = 0; slot < 360; slot++) {
            mask.update(slot, true);
            equal(Math.min(10, (slot + 1) / 36 + 1), mask.pages());
        }
        for (int slot = 0; slot < 360; slot++) mask.update(slot, false);
        equal(10, mask.pages());
        equal(-1, mask.nextOccupied(0));

        mask = new PatternSlotMask();
        for (int slot = 0; slot < 36; slot++) mask.update(slot, true);
        mask.update(0, false);
        for (int slot = 36; slot < 72; slot++) mask.update(slot, true);
        equal(2, mask.pages()); // The last page is full, but the earlier hole is still free.
        mask.update(0, true);
        equal(3, mask.pages());
        mask.update(0, true); // Replacing an occupied pattern must not double-count it.
        equal(3, mask.pages());
    }

    private static void survivesReloadAndRepairsPageCounts() {
        PatternSlotMask mask = new PatternSlotMask();
        mask.restore(10, slot -> false);
        equal(10, mask.pages());
        mask.restore(0, slot -> slot == 359);
        equal(10, mask.pages());
        equal(359, mask.nextOccupied(0));
        mask.restore(1, slot -> slot < 36);
        equal(2, mask.pages());
        mask.restore(Integer.MAX_VALUE, slot -> false);
        equal(10, mask.pages());
        mask.restore(-1, slot -> false);
        equal(1, mask.pages());
        equal(-1, mask.nextOccupied(0));
    }

    private static void skipsEmptySlotsWithoutRenumbering() {
        PatternSlotMask mask = new PatternSlotMask();
        mask.restore(10, slot -> slot == 0 || slot == 63 || slot == 64 || slot == 359);
        int[] expected = { 0, 63, 64, 359 };
        int visited = 0;
        for (int slot = mask.nextOccupied(0); slot >= 0; slot = mask.nextOccupied(slot + 1)) {
            equal(expected[visited++], slot);
        }
        equal(4, visited);
        mask.update(0, false);
        mask.update(63, false);
        mask.update(64, false);
        equal(359, mask.nextOccupied(0));
        equal(-1, mask.nextOccupied(360));
        equal(10, mask.pages());
    }

    private static void rejectsLockedSlots() {
        PatternSlotMask mask = new PatternSlotMask();
        expectBounds(() -> mask.update(-1, true));
        expectBounds(() -> mask.update(36, true));
        mask.restore(10, slot -> false);
        expectBounds(() -> mask.update(360, true));
    }

    private static void randomEditsMatchIndependentInventoryModel() {
        Random random = new Random(339);
        boolean[] occupied = new boolean[360];
        PatternSlotMask mask = new PatternSlotMask();
        int pages = 1;
        for (int step = 0; step < 20000; step++) {
            int slot = random.nextInt(pages * 36);
            occupied[slot] = random.nextInt(20) != 0;
            mask.update(slot, occupied[slot]);
            int count = 0;
            for (boolean present : occupied) if (present) count++;
            if (count == pages * 36 && pages < 10) pages++;
            equal(pages, mask.pages());
            int expected = 0;
            for (int actual = mask.nextOccupied(0); actual >= 0; actual = mask.nextOccupied(actual + 1)) {
                while (!occupied[expected]) expected++;
                equal(expected++, actual);
                count--;
            }
            equal(0, count);
            if (step % 137 == 0) {
                mask = new PatternSlotMask();
                mask.restore(pages, index -> occupied[index]);
                equal(pages, mask.pages());
            }
        }
    }

    private static void expectBounds(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected a locked-slot rejection");
        } catch (IndexOutOfBoundsException expected) {}
    }

    private static void equal(int expected, int actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
}
