package dev.cylox.client.minecraft189;

/**
 * Fixed-size text-width cache for repeated FontRenderer measurements.
 *
 * The cache is scoped to the active FontRenderer and Unicode-font mode.
 * Two-way set associativity keeps lookup work constant and avoids the
 * per-entry nodes and boxed values used by a LinkedHashMap.
 */
public final class CyloxTextWidthCache {
    private static final int SET_COUNT = 256;
    private static final int WAYS_PER_SET = 2;
    private static final int MAX_ENTRIES = SET_COUNT * WAYS_PER_SET;

    private static final String[] TEXTS = new String[MAX_ENTRIES];
    private static final int[] HASHES = new int[MAX_ENTRIES];
    private static final int[] WIDTHS = new int[MAX_ENTRIES];
    private static final long[] ACCESS_TIMES = new long[MAX_ENTRIES];

    private static Object activeRenderer;
    private static boolean activeUnicode;
    private static boolean initialized;
    private static int size;
    private static long accessClock;
    private static long hits;
    private static long misses;
    private static long invalidations;

    private CyloxTextWidthCache() { }

    /** Returns -1 on a miss so vanilla remains the source of truth. */
    public static synchronized int getCachedWidth(Object renderer, String text, boolean unicode) {
        if (renderer == null || text == null) {
            misses++;
            return -1;
        }
        activate(renderer, unicode);

        int hash = text.hashCode();
        int first = setStart(hash);
        for (int way = 0; way < WAYS_PER_SET; way++) {
            int slot = first + way;
            String cached = TEXTS[slot];
            if (cached != null && HASHES[slot] == hash && cached.equals(text)) {
                ACCESS_TIMES[slot] = ++accessClock;
                hits++;
                return WIDTHS[slot];
            }
        }
        misses++;
        return -1;
    }

    public static synchronized void cacheWidth(Object renderer, String text,
                                                boolean unicode, int width) {
        if (renderer == null || text == null || width < 0) return;
        activate(renderer, unicode);

        int hash = text.hashCode();
        int first = setStart(hash);
        int target = -1;
        for (int way = 0; way < WAYS_PER_SET; way++) {
            int slot = first + way;
            String cached = TEXTS[slot];
            if (cached != null && HASHES[slot] == hash && cached.equals(text)) {
                target = slot;
                break;
            }
            if (cached == null && target < 0) target = slot;
        }

        if (target < 0) {
            int second = first + 1;
            target = ACCESS_TIMES[first] <= ACCESS_TIMES[second] ? first : second;
        } else if (TEXTS[target] == null) {
            size++;
        }

        TEXTS[target] = text;
        HASHES[target] = hash;
        WIDTHS[target] = width;
        ACCESS_TIMES[target] = ++accessClock;
    }

    private static int setStart(int hash) {
        int spread = hash ^ (hash >>> 16);
        return (spread & (SET_COUNT - 1)) * WAYS_PER_SET;
    }

    private static void activate(Object renderer, boolean unicode) {
        if (!initialized || activeRenderer != renderer || activeUnicode != unicode) {
            java.util.Arrays.fill(TEXTS, null);
            java.util.Arrays.fill(HASHES, 0);
            java.util.Arrays.fill(WIDTHS, 0);
            java.util.Arrays.fill(ACCESS_TIMES, 0L);
            size = 0;
            accessClock = 0L;
            activeRenderer = renderer;
            activeUnicode = unicode;
            initialized = true;
            invalidations++;
        }
    }

    public static synchronized long getHits() { return hits; }
    public static synchronized long getMisses() { return misses; }
    public static synchronized long getInvalidations() { return invalidations; }
    public static synchronized int getSize() { return size; }
}
