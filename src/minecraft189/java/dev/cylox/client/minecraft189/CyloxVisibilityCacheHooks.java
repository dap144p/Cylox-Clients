package dev.cylox.client.minecraft189;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bounded cache for recent RenderGlobal camera-block visibility graphs.
 *
 * Vanilla mutates the Set returned by getVisibleFacings in one visibility
 * branch, so cached values are never returned directly. The cache is scoped
 * to one RenderGlobal instance, bounded to avoid retaining world positions,
 * and cleared when the world/render range changes.
 */
public final class CyloxVisibilityCacheHooks {
    private static final int MAX_CACHE_ENTRIES = 64;

    private static final class CacheKey {
        final Object renderGlobal;
        final Object blockPosition;
        final int hashCode;

        CacheKey(Object renderGlobal, Object blockPosition) {
            this.renderGlobal = renderGlobal;
            this.blockPosition = blockPosition;
            this.hashCode = 31 * System.identityHashCode(renderGlobal) + blockPosition.hashCode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CacheKey)) return false;
            CacheKey that = (CacheKey) other;
            return renderGlobal == that.renderGlobal && blockPosition.equals(that.blockPosition);
        }
    }

    private static final LinkedHashMap<CacheKey, Set<?>> VISIBILITY_CACHE =
        new LinkedHashMap<CacheKey, Set<?>>(MAX_CACHE_ENTRIES, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<CacheKey, Set<?>> eldest) {
                return size() > MAX_CACHE_ENTRIES;
            }
        };

    private static Object activeRenderGlobal;
    private static long cacheHits;
    private static long cacheMisses;
    private static long cacheInvalidations;

    private CyloxVisibilityCacheHooks() { }

    public static synchronized Object getCachedVisibleFacings(Object renderGlobal, Object blockPosition) {
        if (renderGlobal == null || blockPosition == null) {
            cacheMisses++;
            return null;
        }
        activateRenderGlobal(renderGlobal);
        Set<?> cached = VISIBILITY_CACHE.get(new CacheKey(renderGlobal, blockPosition));
        if (cached == null) {
            cacheMisses++;
            return null;
        }
        cacheHits++;
        return new HashSet<Object>(cached);
    }

    public static synchronized void cacheVisibleFacings(Object renderGlobal, Object blockPosition,
                                                        Object visibleFacings) {
        if (renderGlobal == null || blockPosition == null || !(visibleFacings instanceof Set)) return;
        activateRenderGlobal(renderGlobal);
        VISIBILITY_CACHE.put(new CacheKey(renderGlobal, blockPosition),
            new HashSet<Object>((Set<?>) visibleFacings));
    }

    public static synchronized void invalidateVisibilityCache(Object renderGlobal) {
        if (activeRenderGlobal == null
            || (renderGlobal != null && renderGlobal != activeRenderGlobal)) return;
        if (!VISIBILITY_CACHE.isEmpty()) {
            VISIBILITY_CACHE.clear();
            cacheInvalidations++;
        }
        if (renderGlobal == null) activeRenderGlobal = null;
    }

    private static void activateRenderGlobal(Object renderGlobal) {
        if (activeRenderGlobal != renderGlobal) {
            if (!VISIBILITY_CACHE.isEmpty()) {
                VISIBILITY_CACHE.clear();
                cacheInvalidations++;
            }
            activeRenderGlobal = renderGlobal;
        }
    }

    public static synchronized long getVisibilityCacheHits() { return cacheHits; }
    public static synchronized long getVisibilityCacheMisses() { return cacheMisses; }
    public static synchronized long getVisibilityCacheInvalidations() { return cacheInvalidations; }
    public static synchronized int getVisibilityCacheSize() { return VISIBILITY_CACHE.size(); }
}
