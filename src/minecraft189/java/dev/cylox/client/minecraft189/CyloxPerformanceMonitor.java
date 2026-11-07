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

    private static final ThreadLocal<long[]> START_TIMES = new ThreadLocal<long[]>() {
        @Override
        protected long[] initialValue() {
            return new long[3];
        }
    };

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
        reportIfDue();
    }

    public static void beginSection(int section) {
        if (!ENABLED || section < TERRAIN_SETUP || section > CHUNK_UPLOAD) return;
        START_TIMES.get()[section] = System.nanoTime();
    }

    public static void endSection(int section) {
        if (!ENABLED || section < TERRAIN_SETUP || section > CHUNK_UPLOAD) return;
        long[] starts = START_TIMES.get();
        long start = starts[section];
        starts[section] = 0L;
        if (start == 0L) return;
        long elapsed = Math.max(0L, System.nanoTime() - start);
        if (section == TERRAIN_SETUP) {
            terrainCount++;
            terrainTotalNanos += elapsed;
            if (elapsed > terrainMaxNanos) terrainMaxNanos = elapsed;
        } else {
            uploadCount++;
            uploadTotalNanos += elapsed;
            if (elapsed > uploadMaxNanos) uploadMaxNanos = elapsed;
        }
    }

    private static void reportIfDue() {
        long now = System.nanoTime();
        if (now - windowStartedAt < REPORT_INTERVAL_NANOS) return;

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
    }

    private static double averageMillis(long totalNanos, long count) {
        return count == 0L ? 0.0D : totalNanos / (double) count / 1_000_000.0D;
    }

    private static String oneDecimal(double value) {
        return Double.toString(Math.round(value * 10.0D) / 10.0D);
    }
}
