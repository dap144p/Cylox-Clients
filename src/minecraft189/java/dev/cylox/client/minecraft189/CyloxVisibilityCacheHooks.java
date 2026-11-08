package dev.cylox.client.minecraft189;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.Iterator;
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
    private static final Map<Class<?>, Method> POSITION_PACKERS =
        new HashMap<Class<?>, Method>();

    private static Object activeRenderGlobal;
    private static long cacheHits;
    private static long cacheMisses;
    private static long cacheInvalidations;
    private static long graphCacheHits;
    private static long graphCacheMisses;
    private static long graphCacheFailures;
    private static long rangeInvalidations;
    private static long rangeRemovedEntries;
    private static long rangeFailures;

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
    public static synchronized boolean restoreCachedVisibilityGraph(Object renderGlobal,
            Object sectionPosition, Object visibilityGraph) {
        if (renderGlobal == null || sectionPosition == null || visibilityGraph == null) {
            graphCacheMisses++;
            return false;
        }
        activateRenderGlobal(renderGlobal);
        CacheKey key = new CacheKey(renderGlobal, sectionPosition);
        GraphSnapshot snapshot = VISIBILITY_GRAPH_CACHE.get(key);
        if (snapshot == null) {
            graphCacheMisses++;
            return false;
        }
        try {
            Field bitSetField = snapshot.layout.opaqueBlocks;
            Field remainingField = snapshot.layout.remainingCount;
            if (!bitSetField.getDeclaringClass().isInstance(visibilityGraph)) {
                throw new IllegalStateException("Cached VisGraph layout does not match the active graph");
            }
            BitSet target = (BitSet) bitSetField.get(visibilityGraph);
            target.clear();
            target.or(snapshot.opaqueBlocks);
            remainingField.setInt(visibilityGraph, snapshot.remainingCount);
            graphCacheHits++;
            return true;
        } catch (Throwable error) {
            VISIBILITY_GRAPH_CACHE.remove(key);
            graphCacheFailures++;
            return false;
        }
    }

    /**
     * Compatibility helper retained for verifier/diagnostic use. Runtime cache
     * hits restore the vanilla-allocated graph in place to avoid a second object.
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

    /**
     * Invalidates only cached visibility data for 16x16x16 sections intersecting
     * the changed inclusive block range. Cache keys are immutable BlockPos values;
     * their packed coordinates are decoded through the stable no-argument long
     * accessor rather than relying on obfuscated field names.
     */
    public static synchronized void invalidateVisibilityRange(Object renderGlobal,
            int x1, int y1, int z1, int x2, int y2, int z2) {
        if (activeRenderGlobal == null || renderGlobal != activeRenderGlobal) return;

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        int removed = 0;
        try {
            Iterator<Map.Entry<CacheKey, Set<?>>> visibility = VISIBILITY_CACHE.entrySet().iterator();
            while (visibility.hasNext()) {
                CacheKey key = visibility.next().getKey();
                if (intersectsChangedSection(key.blockPosition, minX, minY, minZ, maxX, maxY, maxZ)) {
                    visibility.remove();
                    removed++;
                }
            }

            Iterator<Map.Entry<CacheKey, GraphSnapshot>> graphs = VISIBILITY_GRAPH_CACHE.entrySet().iterator();
            while (graphs.hasNext()) {
                CacheKey key = graphs.next().getKey();
                if (intersectsChangedSection(key.blockPosition, minX, minY, minZ, maxX, maxY, maxZ)) {
                    graphs.remove();
                    removed++;
                }
            }
        } catch (Throwable error) {
            // If packed coordinates cannot be decoded, clear both caches rather
            // than risk reusing a snapshot after a block update.
            if (!VISIBILITY_CACHE.isEmpty() || !VISIBILITY_GRAPH_CACHE.isEmpty()) {
                removed += VISIBILITY_CACHE.size() + VISIBILITY_GRAPH_CACHE.size();
                VISIBILITY_CACHE.clear();
                VISIBILITY_GRAPH_CACHE.clear();
            }
            rangeFailures++;
        }

        if (removed > 0) {
            cacheInvalidations++;
            rangeInvalidations++;
            rangeRemovedEntries += removed;
        }
    }

    private static boolean intersectsChangedSection(Object blockPosition,
            int minX, int minY, int minZ, int maxX, int maxY, int maxZ) throws Exception {
        BlockCoordinates coordinates = decodeBlockPosition(blockPosition);
        int sectionMinX = Math.floorDiv(coordinates.x, 16) << 4;
        int sectionMinY = Math.floorDiv(coordinates.y, 16) << 4;
        int sectionMinZ = Math.floorDiv(coordinates.z, 16) << 4;
        return sectionMinX <= maxX && sectionMinX + 15 >= minX
            && sectionMinY <= maxY && sectionMinY + 15 >= minY
            && sectionMinZ <= maxZ && sectionMinZ + 15 >= minZ;
    }

    private static final class BlockCoordinates {
        final int x;
        final int y;
        final int z;

        BlockCoordinates(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static BlockCoordinates decodeBlockPosition(Object position) throws Exception {
        if (position == null) throw new IllegalArgumentException("Block position is null");
        Method packer;
        synchronized (POSITION_PACKERS) {
            packer = POSITION_PACKERS.get(position.getClass());
            if (packer == null) {
                Method candidate = null;
                for (Class<?> type = position.getClass(); type != null; type = type.getSuperclass()) {
                    for (Method method : type.getDeclaredMethods()) {
                        if (Modifier.isStatic(method.getModifiers()) || method.getParameterTypes().length != 0
                            || method.getReturnType() != Long.TYPE) continue;
                        if (candidate != null) throw new IllegalStateException("Ambiguous packed BlockPos accessor");
                        candidate = method;
                    }
                }
                if (candidate == null) throw new NoSuchMethodException("No packed BlockPos accessor");
                candidate.setAccessible(true);
                POSITION_PACKERS.put(position.getClass(), candidate);
                packer = candidate;
            }
        }
        long packed = ((Long) packer.invoke(position)).longValue();
        // Minecraft 1.8.9 BlockPos packs signed 26-bit X, 12-bit Y and 26-bit Z.
        int x = (int) (packed >> 38);
        int y = (int) (packed << 26 >> 52);
        int z = (int) (packed << 38 >> 38);
        return new BlockCoordinates(x, y, z);
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
    public static synchronized long getVisibilityRangeInvalidations() { return rangeInvalidations; }
    public static synchronized long getVisibilityRangeRemovedEntries() { return rangeRemovedEntries; }
    public static synchronized long getVisibilityRangeFailures() { return rangeFailures; }
    public static synchronized int getVisibilityGraphCacheSize() { return VISIBILITY_GRAPH_CACHE.size(); }
}
