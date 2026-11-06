package dev.cylox.client.minecraft189;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Reuses temporary collections allocated by RenderGlobal.setupTerrain.
 *
 * These collections are only used on the Minecraft render thread. The
 * traversal queue preserves the LinkedList return type expected by the
 * original bytecode while routing Queue operations to an ArrayDeque.
 */
public final class CyloxRenderHooks {
    private static final ThreadLocal<ArrayList<Object>> RENDER_INFOS =
        new ThreadLocal<ArrayList<Object>>() {
            @Override
            protected ArrayList<Object> initialValue() {
                return new ArrayList<Object>(256);
            }
        };

    private static final ThreadLocal<ReusableTraversalQueue> TRAVERSAL_QUEUES =
        new ThreadLocal<ReusableTraversalQueue>() {
            @Override
            protected ReusableTraversalQueue initialValue() {
                return new ReusableTraversalQueue();
            }
        };

    private CyloxRenderHooks() { }

    private static volatile long nextBudgetSampleNanos;
    private static volatile long uploadBudgetNanos = 250_000L;

    /**
     * Vanilla 1.8.9 passes the frame-start timestamp to RenderGlobal.updateChunks.
     * By the time uploads are processed that deadline has already expired, so
     * runChunkUploads normally drains only its first queued upload. Extend the
     * deadline by a small mode-aware budget so the queue can make progress.
     *
     * Integrated singleplayer gets a smaller budget, reduced further when its
     * recent server tick cost is high. Reflection failure is deliberately
     * conservative: it falls back to the smallest budget.
     */
    public static long extendChunkDeadline(long originalDeadlineNanos) {
        if (originalDeadlineNanos == 0L) return 0L;
        long budget = currentUploadBudgetNanos();
        long proposed = System.nanoTime() + budget;
        return Math.max(originalDeadlineNanos, proposed);
    }

    private static long currentUploadBudgetNanos() {
        long now = System.nanoTime();
        if (now < nextBudgetSampleNanos) return uploadBudgetNanos;

        synchronized (CyloxRenderHooks.class) {
            now = System.nanoTime();
            if (now < nextBudgetSampleNanos) return uploadBudgetNanos;
            long nextBudget = 250_000L;
            try {
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                Object minecraft = minecraftClass.getMethod("getMinecraft").invoke(null);
                boolean singleplayer = Boolean.TRUE.equals(
                    minecraftClass.getMethod("isSingleplayer").invoke(minecraft));

                if (!singleplayer) {
                    nextBudget = 2_000_000L;
                } else {
                    Object server = minecraftClass.getMethod("getIntegratedServer").invoke(minecraft);
                    if (server != null) {
                        Object value = server.getClass().getField("tickTimeArray").get(server);
                        if (value instanceof long[]) {
                            long[] tickTimes = (long[]) value;
                            long total = 0L;
                            int samples = 0;
                            for (long tickTime : tickTimes) {
                                if (tickTime > 0L) {
                                    total += tickTime;
                                    samples++;
                                }
                            }
                            double averageTickNanos = samples == 0 ? 0.0D
                                : (double) total / (double) samples;
                            nextBudget = averageTickNanos > 75_000_000.0D
                                ? 250_000L : 1_000_000L;
                        }
                    }
                }
            } catch (Throwable unavailable) {
                // Keep the conservative default if runtime mapping/reflection differs.
                nextBudget = 250_000L;
            }
            uploadBudgetNanos = nextBudget;
            nextBudgetSampleNanos = now + 500_000_000L;
            return nextBudget;
        }
    }

    /**
     * Returns the render-thread scratch list used for the visible chunk graph.
     * The owner argument keeps the call site explicit and documents that this
     * list belongs to RenderGlobal.setupTerrain.
     */
    public static ArrayList<Object> reuseRenderInfos(Object owner) {
        ArrayList<Object> result = RENDER_INFOS.get();
        result.clear();
        return result;
    }

    /** Returns a cleared FIFO queue without allocating linked-list nodes. */
    public static LinkedList<Object> reuseTraversalQueue() {
        ReusableTraversalQueue result = TRAVERSAL_QUEUES.get();
        result.clear();
        return result;
    }

    private static final class ReusableTraversalQueue extends LinkedList<Object> {
        private final ArrayDeque<Object> elements = new ArrayDeque<Object>(256);

        @Override
        public boolean add(Object value) {
            elements.addLast(value);
            return true;
        }

        @Override
        public boolean offer(Object value) {
            return elements.offerLast(value);
        }

        @Override
        public Object poll() {
            return elements.pollFirst();
        }

        @Override
        public boolean isEmpty() {
            return elements.isEmpty();
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public void clear() {
            elements.clear();
        }
    }
}
