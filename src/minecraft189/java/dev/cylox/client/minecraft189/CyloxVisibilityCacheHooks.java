package dev.cylox.client.minecraft189;

import java.util.HashSet;
import java.util.Set;

/**
 * Conservative cache for RenderGlobal's current camera-block visibility graph.
 *
 * The cached Set is never handed directly to vanilla: setupTerrain removes a
 * facing from the returned Set in one branch, so each hit receives a small
 * defensive copy. Block-range notifications and world replacement invalidate
 * the single-entry cache. All operations are synchronized because world
 * notifications can be delivered from different client task paths.
 */
public final class CyloxRenderHooks {
    private static Object cachedRenderGlobal;
    private static Object cachedBlockPosition;
    private static Set<?> cachedVisibleFacings;
    private static long cacheHits;
    private static long cacheMisses;
    private static long cacheInvalidations;

    private CyloxRenderHooks() { }

    public static synchronized Object getCachedVisibleFacings(Object renderGlobal, Object blockPosition) {
        if (renderGlobal == cachedRenderGlobal && blockPosition != null
            && cachedBlockPosition != null && cachedBlockPosition.equals(blockPosition)
            && cachedVisibleFacings != null) {
            cacheHits++;
            return new HashSet<Object>(cachedVisibleFacings);
        }
        cacheMisses++;
        return null;
    }

    public static synchronized void cacheVisibleFacings(Object renderGlobal, Object blockPosition, Object visibleFacings) {
        if (renderGlobal == null || blockPosition == null || !(visibleFacings instanceof Set)) return;
        cachedRenderGlobal = renderGlobal;
        cachedBlockPosition = blockPosition;
        cachedVisibleFacings = new HashSet<Object>((Set<?>) visibleFacings);
    }

    public static synchronized void invalidateVisibilityCache(Object renderGlobal) {
        if (cachedRenderGlobal != null && (renderGlobal == null || renderGlobal == cachedRenderGlobal)) {
            cachedRenderGlobal = null;
            cachedBlockPosition = null;
            cachedVisibleFacings = null;
            cacheInvalidations++;
        }
    }

    public static synchronized long getVisibilityCacheHits() { return cacheHits; }
    public static synchronized long getVisibilityCacheMisses() { return cacheMisses; }
    public static synchronized long getVisibilityCacheInvalidations() { return cacheInvalidations; }
}
