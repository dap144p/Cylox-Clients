package dev.cylox.client.minecraft189;

/**
 * Opt-in render timing diagnostics for low-end device profiling.
 *
 * Disabled by default. Enable with the JVM property -Dcylox.profile=true.
 * All measured sections run on the Minecraft render thread; no worker threads,
 * scheduled tasks, per-frame allocations, or gameplay changes are introduced.
 */
public final class CyloxPerformanceMonitor {
    private static final boolean ENABLED = Boolean.getBoolean("cylox.profile");
    private static final long REPORT_INTERVAL_NANOS = 5_000_000_000L;
    private static final int FRAME = 0;
    private static final int TERRAIN_SETUP = 1;
    private static final int CHUNK_UPLOAD = 2;
    private static final int ENTITY_RENDER = 3;
    private static final int BLOCK_LAYER = 4;
    private static final int PARTICLE_RENDER = 5;
    private static final int TERRAIN_CAMERA = 6;
    private static final int TERRAIN_RENDER_LIST = 7;
    private static final int TERRAIN_CULL_PREP = 8;
    private static final int TERRAIN_CULLING = 9;
    private static final int TERRAIN_BUILD_NEAR = 10;
    private static final int TILE_ENTITIES = 11;
    private static final int NAME_TAG = 12;

    private static final ThreadLocal<long[]> START_TIMES = new ThreadLocal<long[]>() {
        @Override
        protected long[] initialValue() {
            return new long[13];
        }
    };
    private static final long[] TERRAIN_DETAIL_COUNTS = new long[5];
    private static final long[] TERRAIN_DETAIL_TOTAL_NANOS = new long[5];
    private static final long[] TERRAIN_DETAIL_MAX_NANOS = new long[5];

    private static long windowStartedAt = System.nanoTime();
    private static long frameCount;
    private static long frameTotalNanos;
    private static long frameMaxNanos;
    private static long terrainCount;
    private static long terrainTotalNanos;
    private static long terrainMaxNanos;
    private static long uploadCount;
    private static long uploadTotalNanos;
    private static long uploadMaxNanos;
    private static long entityCount;
    private static long entityTotalNanos;
    private static long entityMaxNanos;
    private static long blockLayerCount;
    private static long blockLayerTotalNanos;
    private static long blockLayerMaxNanos;
    private static long particleCount;
    private static long particleTotalNanos;
    private static long particleMaxNanos;
    private static long tileEntityCount;
    private static long tileEntityTotalNanos;
    private static long tileEntityMaxNanos;
    private static long nameTagCount;
    private static long nameTagTotalNanos;
    private static long nameTagMaxNanos;
    private static long heapSampleCount;
    private static long heapTotalUsedBytes;
    private static long heapPeakUsedBytes;
    private static long heapPeakCommittedBytes;

    private CyloxPerformanceMonitor() { }

    public static void beginFrame() {
        if (!ENABLED) return;
        START_TIMES.get()[FRAME] = System.nanoTime();
    }

    public static void endFrame() {
        if (!ENABLED) return;
        long[] starts = START_TIMES.get();
        long start = starts[FRAME];
        starts[FRAME] = 0L;
        if (start == 0L) return;
        long elapsed = Math.max(0L, System.nanoTime() - start);
        frameCount++;
        frameTotalNanos += elapsed;
        if (elapsed > frameMaxNanos) frameMaxNanos = elapsed;
        if ((frameCount & 15L) == 0L) sampleHeap();
        reportIfDue();
    }

    public static void beginSection(int section) {
        if (!ENABLED || section < TERRAIN_SETUP || section > NAME_TAG) return;
        START_TIMES.get()[section] = System.nanoTime();
    }

    public static void endSection(int section) {
        if (!ENABLED || section < TERRAIN_SETUP || section > NAME_TAG) return;
        long[] starts = START_TIMES.get();
        long start = starts[section];
        starts[section] = 0L;
        if (start == 0L) return;
        long elapsed = Math.max(0L, System.nanoTime() - start);
        if (section == TERRAIN_SETUP) {
            terrainCount++;
            terrainTotalNanos += elapsed;
            if (elapsed > terrainMaxNanos) terrainMaxNanos = elapsed;
        } else if (section == CHUNK_UPLOAD) {
            uploadCount++;
            uploadTotalNanos += elapsed;
            if (elapsed > uploadMaxNanos) uploadMaxNanos = elapsed;
        } else if (section == ENTITY_RENDER) {
            entityCount++;
            entityTotalNanos += elapsed;
            if (elapsed > entityMaxNanos) entityMaxNanos = elapsed;
        } else if (section == BLOCK_LAYER) {
            blockLayerCount++;
            blockLayerTotalNanos += elapsed;
            if (elapsed > blockLayerMaxNanos) blockLayerMaxNanos = elapsed;
        } else if (section == PARTICLE_RENDER) {
            particleCount++;
            particleTotalNanos += elapsed;
            if (elapsed > particleMaxNanos) particleMaxNanos = elapsed;
        } else if (section == TILE_ENTITIES) {
            tileEntityCount++;
            tileEntityTotalNanos += elapsed;
            if (elapsed > tileEntityMaxNanos) tileEntityMaxNanos = elapsed;
        } else if (section == NAME_TAG) {
            nameTagCount++;
            nameTagTotalNanos += elapsed;
            if (elapsed > nameTagMaxNanos) nameTagMaxNanos = elapsed;
        } else {
            int detail = section - TERRAIN_CAMERA;
            if (detail < 0 || detail >= TERRAIN_DETAIL_COUNTS.length) return;
            TERRAIN_DETAIL_COUNTS[detail]++;
            TERRAIN_DETAIL_TOTAL_NANOS[detail] += elapsed;
            if (elapsed > TERRAIN_DETAIL_MAX_NANOS[detail]) {
                TERRAIN_DETAIL_MAX_NANOS[detail] = elapsed;
            }
        }
    }

    private static void reportIfDue() {
        long now = System.nanoTime();
        if (now - windowStartedAt < REPORT_INTERVAL_NANOS) return;

        sampleHeap();
        Runtime runtime = Runtime.getRuntime();
        long currentUsedHeapBytes = Math.max(0L, runtime.totalMemory() - runtime.freeMemory());
        long currentCommittedHeapBytes = runtime.totalMemory();
        double averageHeapUsedBytes = heapSampleCount == 0L
            ? 0.0D : heapTotalUsedBytes / (double) heapSampleCount;
        double averageFrameMs = averageMillis(frameTotalNanos, frameCount);
        double averageFps = frameTotalNanos <= 0L ? 0.0D
            : frameCount * 1_000_000_000.0D / frameTotalNanos;
        System.out.println("[Cylox Perf] avgFPS=" + oneDecimal(averageFps)
            + ", frameAvgMs=" + oneDecimal(averageFrameMs)
            + ", frameMaxMs=" + oneDecimal(frameMaxNanos / 1_000_000.0D)
            + ", terrainAvgMs=" + oneDecimal(averageMillis(terrainTotalNanos, terrainCount))
            + ", terrainMaxMs=" + oneDecimal(terrainMaxNanos / 1_000_000.0D)
            + ", chunkUploadAvgMs=" + oneDecimal(averageMillis(uploadTotalNanos, uploadCount))
            + ", chunkUploadMaxMs=" + oneDecimal(uploadMaxNanos / 1_000_000.0D)
            + ", entityAvgMs=" + oneDecimal(averageMillis(entityTotalNanos, entityCount))
            + ", entityMaxMs=" + oneDecimal(entityMaxNanos / 1_000_000.0D)
            + ", blockLayerAvgMs=" + oneDecimal(averageMillis(blockLayerTotalNanos, blockLayerCount))
            + ", blockLayerMaxMs=" + oneDecimal(blockLayerMaxNanos / 1_000_000.0D)
            + ", particleAvgMs=" + oneDecimal(averageMillis(particleTotalNanos, particleCount))
            + ", particleMaxMs=" + oneDecimal(particleMaxNanos / 1_000_000.0D)
            + ", tileEntityAvgMs=" + oneDecimal(averageMillis(tileEntityTotalNanos, tileEntityCount))
            + ", tileEntityMaxMs=" + oneDecimal(tileEntityMaxNanos / 1_000_000.0D)
            + ", nameTagAvgMs=" + oneDecimal(averageMillis(nameTagTotalNanos, nameTagCount))
            + ", nameTagMaxMs=" + oneDecimal(nameTagMaxNanos / 1_000_000.0D)
            + ", heapUsedAvgMB=" + oneDecimal(averageHeapUsedBytes / 1048576.0D)
            + ", heapUsedPeakMB=" + oneDecimal(heapPeakUsedBytes / 1048576.0D)
            + ", heapUsedCurrentMB=" + oneDecimal(currentUsedHeapBytes / 1048576.0D)
            + ", heapCommittedCurrentMB=" + oneDecimal(currentCommittedHeapBytes / 1048576.0D)
            + ", heapCommittedPeakMB=" + oneDecimal(heapPeakCommittedBytes / 1048576.0D)
            + ", heapMaxMB=" + oneDecimal(runtime.maxMemory() / 1048576.0D)
            + ", heapSamples=" + heapSampleCount
            + ", terrainCameraAvgMs=" + detailAverage(0)
            + ", terrainCameraMaxMs=" + detailMaximum(0)
            + ", terrainCameraSamples=" + TERRAIN_DETAIL_COUNTS[0]
            + ", terrainRenderListAvgMs=" + detailAverage(1)
            + ", terrainRenderListMaxMs=" + detailMaximum(1)
            + ", terrainRenderListSamples=" + TERRAIN_DETAIL_COUNTS[1]
            + ", terrainCullPrepAvgMs=" + detailAverage(2)
            + ", terrainCullPrepMaxMs=" + detailMaximum(2)
            + ", terrainCullPrepSamples=" + TERRAIN_DETAIL_COUNTS[2]
            + ", terrainCullingAvgMs=" + detailAverage(3)
            + ", terrainCullingMaxMs=" + detailMaximum(3)
            + ", terrainCullingSamples=" + TERRAIN_DETAIL_COUNTS[3]
            + ", terrainBuildNearAvgMs=" + detailAverage(4)
            + ", terrainBuildNearMaxMs=" + detailMaximum(4)
            + ", terrainBuildNearSamples=" + TERRAIN_DETAIL_COUNTS[4]
            + ", visibilityHits=" + CyloxVisibilityCacheHooks.getVisibilityCacheHits()
            + ", visibilityMisses=" + CyloxVisibilityCacheHooks.getVisibilityCacheMisses()
            + ", visibilityInvalidations=" + CyloxVisibilityCacheHooks.getVisibilityCacheInvalidations()
            + ", visibilityRangeInvalidations=" + CyloxVisibilityCacheHooks.getVisibilityRangeInvalidations()
            + ", visibilityRangeRemovedEntries=" + CyloxVisibilityCacheHooks.getVisibilityRangeRemovedEntries()
            + ", visibilityRangeFailures=" + CyloxVisibilityCacheHooks.getVisibilityRangeFailures()
            + ", visibilityCacheEntries=" + CyloxVisibilityCacheHooks.getVisibilityCacheSize()
            + ", visibilityGraphHits=" + CyloxVisibilityCacheHooks.getVisibilityGraphCacheHits()
            + ", visibilityGraphMisses=" + CyloxVisibilityCacheHooks.getVisibilityGraphCacheMisses()
            + ", visibilityGraphFailures=" + CyloxVisibilityCacheHooks.getVisibilityGraphCacheFailures()
            + ", visibilityGraphEntries=" + CyloxVisibilityCacheHooks.getVisibilityGraphCacheSize()
            + ", textWidthHits=" + CyloxTextWidthCache.getHits()
            + ", textWidthMisses=" + CyloxTextWidthCache.getMisses()
            + ", textWidthInvalidations=" + CyloxTextWidthCache.getInvalidations()
            + ", textWidthEntries=" + CyloxTextWidthCache.getSize()
            + ", frames=" + frameCount + ".");

        windowStartedAt = now;
        frameCount = 0L;
        frameTotalNanos = 0L;
        frameMaxNanos = 0L;
        terrainCount = 0L;
        terrainTotalNanos = 0L;
        terrainMaxNanos = 0L;
        uploadCount = 0L;
        uploadTotalNanos = 0L;
        uploadMaxNanos = 0L;
        entityCount = 0L;
        entityTotalNanos = 0L;
        entityMaxNanos = 0L;
        blockLayerCount = 0L;
        blockLayerTotalNanos = 0L;
        blockLayerMaxNanos = 0L;
        particleCount = 0L;
        particleTotalNanos = 0L;
        particleMaxNanos = 0L;
        tileEntityCount = 0L;
        tileEntityTotalNanos = 0L;
        tileEntityMaxNanos = 0L;
        nameTagCount = 0L;
        nameTagTotalNanos = 0L;
        nameTagMaxNanos = 0L;
        heapSampleCount = 0L;
        heapTotalUsedBytes = 0L;
        heapPeakUsedBytes = 0L;
        heapPeakCommittedBytes = 0L;
        for (int i = 0; i < TERRAIN_DETAIL_COUNTS.length; i++) {
            TERRAIN_DETAIL_COUNTS[i] = 0L;
            TERRAIN_DETAIL_TOTAL_NANOS[i] = 0L;
            TERRAIN_DETAIL_MAX_NANOS[i] = 0L;
        }
    }

    private static void sampleHeap() {
        Runtime runtime = Runtime.getRuntime();
        long committedBytes = runtime.totalMemory();
        long usedBytes = Math.max(0L, committedBytes - runtime.freeMemory());
        heapSampleCount++;
        heapTotalUsedBytes += usedBytes;
        if (usedBytes > heapPeakUsedBytes) heapPeakUsedBytes = usedBytes;
        if (committedBytes > heapPeakCommittedBytes) heapPeakCommittedBytes = committedBytes;
    }

    private static String detailAverage(int index) {
        return oneDecimal(averageMillis(TERRAIN_DETAIL_TOTAL_NANOS[index], TERRAIN_DETAIL_COUNTS[index]));
    }

    private static String detailMaximum(int index) {
        return oneDecimal(TERRAIN_DETAIL_MAX_NANOS[index] / 1_000_000.0D);
    }

    private static double averageMillis(long totalNanos, long count) {
        return count == 0L ? 0.0D : totalNanos / (double) count / 1_000_000.0D;
    }

    private static String oneDecimal(double value) {
        return Double.toString(Math.round(value * 10.0D) / 10.0D);
    }
}
