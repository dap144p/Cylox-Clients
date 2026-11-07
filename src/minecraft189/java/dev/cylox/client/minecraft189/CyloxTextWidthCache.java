package dev.cylox.client.minecraft189;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bounded cache for repeated FontRenderer string-width calculations.
 * The cache is scoped to the active FontRenderer and Unicode-font mode, so a
 * renderer replacement or font-mode change cannot reuse stale measurements.
 */
public final class CyloxTextWidthCache {
    private static final int MAX_ENTRIES = 512;

    private static final LinkedHashMap<String, Integer> WIDTHS =
        new LinkedHashMap<String, Integer>(MAX_ENTRIES, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > MAX_ENTRIES;
            }
        };

    private static Object activeRenderer;
    private static boolean activeUnicode;
    private static boolean initialized;
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
        Integer width = WIDTHS.get(text);
        if (width == null) {
            misses++;
            return -1;
        }
        hits++;
        return width.intValue();
    }

    public static synchronized void cacheWidth(Object renderer, String text,
                                                boolean unicode, int width) {
        if (renderer == null || text == null || width < 0) return;
        activate(renderer, unicode);
        WIDTHS.put(text, Integer.valueOf(width));
    }

    private static void activate(Object renderer, boolean unicode) {
        if (!initialized || activeRenderer != renderer || activeUnicode != unicode) {
            WIDTHS.clear();
            activeRenderer = renderer;
            activeUnicode = unicode;
            initialized = true;
            invalidations++;
        }
    }

    public static synchronized long getHits() { return hits; }
    public static synchronized long getMisses() { return misses; }
    public static synchronized long getInvalidations() { return invalidations; }
    public static synchronized int getSize() { return WIDTHS.size(); }
}
