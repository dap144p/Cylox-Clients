package dev.cylox.client.minecraft189;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bounded caches for RenderGlobal camera-block visibility results and the
 * immutable opaque-block snapshot used to build a VisGraph for one chunk section.
 *
 * VisGraph.func_178609_b mutates its BitSet while flood-filling, so cached
 * snapshots are copied into a fresh VisGraph instance before every use.
 */
public final class CyloxVisibilityCacheHooks {
    private static final int MAX_CACHE_ENTRIES = 64;
    private static final int MAX_GRAPH_CACHE_ENTRIES = 32;

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

    private static final class GraphLayout {
        final Constructor<?> constructor;
        final Field opaqueBlocks;
        final Field remainingCount;

        GraphLayout(Class<?> graphClass) throws Exception {
            Field bitSet = null;
            Field remaining = null;
            for (Field field : graphClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (field.getType() == BitSet.class) {
                    if (bitSet != null) throw new IllegalStateException("Ambiguous VisGraph BitSet field");
                    bitSet = field;
                } else if (field.getType() == Integer.TYPE) {
                    if (remaining != null) throw new IllegalStateException("Ambiguous VisGraph counter field");
                    remaining = field;
                }
            }
            if (bitSet == null || remaining == null) {
                throw new IllegalStateException("Unsupported VisGraph field layout");
            }
            bitSet.setAccessible(true);
            remaining.setAccessible(true);
            Constructor<?> ctor = graphClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            constructor = ctor;
            opaqueBlocks = bitSet;
            remainingCount = remaining;
        }
    }

    private static final class GraphSnapshot {
        final GraphLayout layout;
        final BitSet opaqueBlocks;
        final int remainingCount;

        GraphSnapshot(GraphLayout layout, BitSet opaqueBlocks, int remainingCount) {
            this.layout = layout;
            this.opaqueBlocks = opaqueBlocks;
            this.remainingCount = remainingCount;
        }

        Object createWorkingCopy() throws Exception {
            Object graph = layout.constructor.newInstance();
            BitSet target = (BitSet) layout.opaqueBlocks.get(graph);
            target.or(opaqueBlocks);
            layout.remainingCount.setInt(graph, remainingCount);
            return graph;
        }
    }

    private static final LinkedHashMap<CacheKey, Set<?>> VISIBILITY_CACHE =
        new LinkedHashMap<CacheKey, Set<?>>(MAX_CACHE_ENTRIES, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<CacheKey, Set<?>> eldest) {
                return size() > MAX_CACHE_ENTRIES;
            }
        };

    private static final LinkedHashMap<CacheKey, GraphSnapshot> VISIBILITY_GRAPH_CACHE =
        new LinkedHashMap<CacheKey, GraphSnapshot>(MAX_GRAPH_CACHE_ENTRIES, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<CacheKey, GraphSnapshot> eldest) {
                return size() > MAX_GRAPH_CACHE_ENTRIES;
            }
        };

    private static final Map<Class<?>, GraphLayout> GRAPH_LAYOUTS =
        new HashMap<Class<?>, GraphLayout>();

    private static Object activeRenderGlobal;
    private static long cacheHits;
    private static long cacheMisses;
    private static long cacheInvalidations;
    private static long graphCacheHits;
    private static long graphCacheMisses;
    private static long graphCacheFailures;

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
        return copySet(cached);
    }

    public static synchronized void cacheVisibleFacings(Object renderGlobal, Object blockPosition,
                                                        Object visibleFacings) {
        if (renderGlobal == null || blockPosition == null || !(visibleFacings instanceof Set)) return;
        activateRenderGlobal(renderGlobal);
        VISIBILITY_CACHE.put(new CacheKey(renderGlobal, blockPosition),
            copySet((Set<?>) visibleFacings));
    }

    /**
     * Looks up an opaque-block snapshot for a section origin and returns a fresh
     * VisGraph instance. A null result means the caller must run vanilla's scan.
     */
    public static synchronized Object getCachedVisibilityGraph(Object renderGlobal, Object sectionPosition) {
        if (renderGlobal == null || sectionPosition == null) {
            graphCacheMisses++;
            return null;
        }
        activateRenderGlobal(renderGlobal);
        CacheKey key = new CacheKey(renderGlobal, sectionPosition);
        GraphSnapshot snapshot = VISIBILITY_GRAPH_CACHE.get(key);
        if (snapshot == null) {
            graphCacheMisses++;
            return null;
        }
        try {
            Object copy = snapshot.createWorkingCopy();
            graphCacheHits++;
            return copy;
        } catch (Throwable error) {
            VISIBILITY_GRAPH_CACHE.remove(key);
            graphCacheFailures++;
            return null;
        }
    }

    /**
     * Captures the VisGraph's opaque BitSet before vanilla flood-fill mutates it.
     * Reflection is deliberately layout-based (BitSet + one instance int), not
     * tied to obfuscated field names. Unsupported layouts fail open to vanilla.
     */
    public static synchronized void cacheVisibilityGraph(Object renderGlobal, Object sectionPosition,
                                                          Object visibilityGraph) {
        if (renderGlobal == null || sectionPosition == null || visibilityGraph == null) return;
        activateRenderGlobal(renderGlobal);
        try {
            GraphLayout layout = getGraphLayout(visibilityGraph.getClass());
            BitSet opaque = (BitSet) layout.opaqueBlocks.get(visibilityGraph);
            int remaining = layout.remainingCount.getInt(visibilityGraph);
            VISIBILITY_GRAPH_CACHE.put(new CacheKey(renderGlobal, sectionPosition),
                new GraphSnapshot(layout, (BitSet) opaque.clone(), remaining));
        } catch (Throwable error) {
            graphCacheFailures++;
        }
    }

    public static synchronized void invalidateVisibilityCache(Object renderGlobal) {
        if (activeRenderGlobal == null
            || (renderGlobal != null && renderGlobal != activeRenderGlobal)) return;
        if (!VISIBILITY_CACHE.isEmpty() || !VISIBILITY_GRAPH_CACHE.isEmpty()) {
            VISIBILITY_CACHE.clear();
            VISIBILITY_GRAPH_CACHE.clear();
            cacheInvalidations++;
        }
        if (renderGlobal == null) activeRenderGlobal = null;
    }

    private static void activateRenderGlobal(Object renderGlobal) {
        if (activeRenderGlobal != renderGlobal) {
            if (!VISIBILITY_CACHE.isEmpty() || !VISIBILITY_GRAPH_CACHE.isEmpty()) {
                VISIBILITY_CACHE.clear();
                VISIBILITY_GRAPH_CACHE.clear();
                cacheInvalidations++;
            }
            activeRenderGlobal = renderGlobal;
        }
    }

    private static GraphLayout getGraphLayout(Class<?> graphClass) throws Exception {
        GraphLayout layout = GRAPH_LAYOUTS.get(graphClass);
        if (layout == null) {
            layout = new GraphLayout(graphClass);
            GRAPH_LAYOUTS.put(graphClass, layout);
        }
        return layout;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Set<?> copySet(Set<?> source) {
        // VisGraph returns EnumSet<EnumFacing>; keep its compact bit-vector
        // representation instead of allocating a HashSet on each cache hit.
        if (source instanceof EnumSet) return (Set<?>) ((EnumSet) source).clone();
        return new HashSet<Object>(source);
    }

    public static synchronized long getVisibilityCacheHits() { return cacheHits; }
    public static synchronized long getVisibilityCacheMisses() { return cacheMisses; }
    public static synchronized long getVisibilityCacheInvalidations() { return cacheInvalidations; }
    public static synchronized int getVisibilityCacheSize() { return VISIBILITY_CACHE.size(); }
    public static synchronized long getVisibilityGraphCacheHits() { return graphCacheHits; }
    public static synchronized long getVisibilityGraphCacheMisses() { return graphCacheMisses; }
    public static synchronized long getVisibilityGraphCacheFailures() { return graphCacheFailures; }
    public static synchronized int getVisibilityGraphCacheSize() { return VISIBILITY_GRAPH_CACHE.size(); }
}
