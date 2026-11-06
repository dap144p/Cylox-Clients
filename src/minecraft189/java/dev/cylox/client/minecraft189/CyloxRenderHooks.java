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
