package dev.cylox.client.chunk;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Reorders already-dirty vanilla render chunks so worker builds facing the
 * player's recent movement direction are submitted before unrelated chunks.
 *
 * This does not create compile tasks, load extra chunks, or change visibility.
 * RenderGlobal.updateChunks() consumes its LinkedHashSet in iteration order.
 */
public final class CyloxPredictiveChunkBuildHooks {
    private static final Map<Object, MotionState> MOTION =
        new WeakHashMap<Object, MotionState>();
    private static volatile ChunkQueueAccessor cachedAccessor;

    private CyloxPredictiveChunkBuildHooks() { }

    public static void onTerrainPrepared(Object renderGlobal, Object viewEntity,
                                          double playerX, double playerZ) {
        if (renderGlobal == null || viewEntity == null
            || !isFinite(playerX) || !isFinite(playerZ)) return;

        double directionX;
        double directionZ;
        long now = System.nanoTime();

        synchronized (MOTION) {
            MotionState state = MOTION.get(renderGlobal);
            if (state == null || state.viewEntity.get() != viewEntity) {
                state = new MotionState(viewEntity, playerX, playerZ);
                MOTION.put(renderGlobal, state);
                return;
            }

            double dx = playerX - state.lastX;
            double dz = playerZ - state.lastZ;
            state.lastX = playerX;
            state.lastZ = playerZ;
            double movementSquared = dx * dx + dz * dz;

            // A large position jump is a teleport/respawn, not a useful prediction.
            if (movementSquared > 64.0D) {
                state.directionX = 0.0D;
                state.directionZ = 0.0D;
                state.lastMovementNanos = 0L;
            } else if (movementSquared >= 0.0004D) {
                double length = Math.sqrt(movementSquared);
                double nextX = dx / length;
                double nextZ = dz / length;
                double dot = state.directionX * nextX + state.directionZ * nextZ;

                if (state.lastMovementNanos == 0L || dot < 0.35D) {
                    state.directionX = nextX;
                    state.directionZ = nextZ;
                } else {
                    double smoothX = state.directionX * 0.65D + nextX * 0.35D;
                    double smoothZ = state.directionZ * 0.65D + nextZ * 0.35D;
                    double smoothLength = Math.sqrt(smoothX * smoothX + smoothZ * smoothZ);
                    if (smoothLength > 0.0001D) {
                        state.directionX = smoothX / smoothLength;
                        state.directionZ = smoothZ / smoothLength;
                    }
                }
                state.lastMovementNanos = now;
            }

            if (state.lastMovementNanos == 0L
                || now - state.lastMovementNanos > 500000000L) {
                state.directionX = 0.0D;
                state.directionZ = 0.0D;
            }

            directionX = state.directionX;
            directionZ = state.directionZ;
            if (now - state.lastPriorityNanos < 100000000L) return;
            state.lastPriorityNanos = now;
        }

        if (directionX * directionX + directionZ * directionZ < 0.5D) return;

        try {
            ChunkQueueAccessor accessor = getAccessor(renderGlobal);
            if (accessor == null) return;
            Object value = accessor.chunksToUpdate.get(renderGlobal);
            if (!(value instanceof LinkedHashSet)) return;

            @SuppressWarnings("unchecked")
            Set<Object> chunks = (Set<Object>) value;
            prioritizeAhead(chunks, playerX, playerZ, directionX, directionZ,
                accessor.positionAccess);
        } catch (Exception ignored) {
            // Optional scheduling optimization: always fall back to vanilla order.
        }
    }

    /** Clears movement prediction when RenderGlobal is attached to another world. */
    public static void clear(Object renderGlobal) {
        if (renderGlobal == null) return;
        synchronized (MOTION) {
            MOTION.remove(renderGlobal);
        }
    }

    private static ChunkQueueAccessor getAccessor(Object renderGlobal) throws Exception {
        Class<?> owner = renderGlobal.getClass();
        ChunkQueueAccessor current = cachedAccessor;
        if (current != null && current.renderGlobalClass == owner) return current;

        for (Class<?> type = owner; type != null; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (!Set.class.isAssignableFrom(field.getType())
                    || Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                Object value = field.get(renderGlobal);
                if (!(value instanceof LinkedHashSet)) continue;

                Set<?> chunks = (Set<?>) value;
                if (chunks.isEmpty()) continue;
                Object sample = chunks.iterator().next();
                if (sample == null) continue;

                PositionAccess positionAccess = findPositionAccess(sample.getClass());
                if (positionAccess == null) continue;

                ChunkQueueAccessor found = new ChunkQueueAccessor(owner, field, positionAccess);
                cachedAccessor = found;
                return found;
            }
        }
        return null;
    }

    private static PositionAccess findPositionAccess(Class<?> renderChunkClass) {
        for (Class<?> type = renderChunkClass; type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getParameterTypes().length != 0
                    || Modifier.isStatic(method.getModifiers())
                    || method.getReturnType().isPrimitive()) continue;

                Method packedPosition = findPackedPositionMethod(method.getReturnType());
                if (packedPosition == null) continue;
                try {
                    method.setAccessible(true);
                    return new PositionAccess(method, packedPosition);
                } catch (RuntimeException inaccessible) {
                    return null;
                }
            }
        }
        return null;
    }

    private static Method findPackedPositionMethod(Class<?> blockPosClass) {
        int intFieldCount = 0;
        for (Class<?> type = blockPosClass; type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() == Integer.TYPE
                    && !Modifier.isStatic(field.getModifiers())) intFieldCount++;
            }
        }
        if (intFieldCount < 3) return null;

        for (Class<?> type = blockPosClass; type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getParameterTypes().length == 0
                    && method.getReturnType() == Long.TYPE
                    && !Modifier.isStatic(method.getModifiers())) {
                    try {
                        method.setAccessible(true);
                        return method;
                    } catch (RuntimeException inaccessible) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private static void prioritizeAhead(Set<Object> chunks, double playerX, double playerZ,
                                        double directionX, double directionZ,
                                        PositionAccess positionAccess) throws Exception {
        if (chunks == null || chunks.size() < 2 || positionAccess == null) return;

        List<Object> remaining = new ArrayList<Object>(chunks.size());
        List<ChunkOrder> ahead = new ArrayList<ChunkOrder>();
        int index = 0;
        for (Object chunk : chunks) {
            if (chunk == null) {
                remaining.add(chunk);
                index++;
                continue;
            }

            try {
                Object blockPos = positionAccess.positionMethod.invoke(chunk);
                long packed = ((Long) positionAccess.packedPositionMethod.invoke(blockPos)).longValue();
                int chunkX = (int) (packed >> 38);
                int chunkZ = (int) (packed & 0x3FFFFFFL);
                if ((chunkZ & 0x2000000) != 0) chunkZ |= 0xFC000000;

                double dx = (double) chunkX + 8.0D - playerX;
                double dz = (double) chunkZ + 8.0D - playerZ;
                double forward = dx * directionX + dz * directionZ;
                double lateral = Math.abs(dx * directionZ - dz * directionX);

                // Only prioritize the forward corridor; keep all other chunks
                // in their original vanilla insertion order.
                if (forward > 0.0D && lateral <= forward + 16.0D) {
                    ahead.add(new ChunkOrder(chunk, dx * dx + dz * dz, index));
                } else {
                    remaining.add(chunk);
                }
            } catch (Exception invalidChunkPosition) {
                remaining.add(chunk);
            }
            index++;
        }

        if (ahead.isEmpty()) return;
        Collections.sort(ahead, new Comparator<ChunkOrder>() {
            @Override
            public int compare(ChunkOrder left, ChunkOrder right) {
                int distance = Double.compare(left.distanceSquared, right.distanceSquared);
                return distance != 0 ? distance : left.originalIndex - right.originalIndex;
            }
        });

        chunks.clear();
        for (ChunkOrder item : ahead) chunks.add(item.chunk);
        chunks.addAll(remaining);
    }

    // Package-private to let the unit tests exercise the exact stable-ordering rule.
    static void prioritizeAheadForTest(Set<Object> chunks, double playerX, double playerZ,
                                       double directionX, double directionZ) throws Exception {
        if (chunks == null || chunks.isEmpty()) return;
        Object sample = chunks.iterator().next();
        PositionAccess access = findPositionAccess(sample.getClass());
        prioritizeAhead(chunks, playerX, playerZ, directionX, directionZ, access);
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static final class MotionState {
        final WeakReference<Object> viewEntity;
        double lastX;
        double lastZ;
        double directionX;
        double directionZ;
        long lastMovementNanos;
        long lastPriorityNanos;

        MotionState(Object entity, double x, double z) {
            viewEntity = new WeakReference<Object>(entity);
            lastX = x;
            lastZ = z;
        }
    }

    private static final class PositionAccess {
        final Method positionMethod;
        final Method packedPositionMethod;

        PositionAccess(Method positionMethod, Method packedPositionMethod) {
            this.positionMethod = positionMethod;
            this.packedPositionMethod = packedPositionMethod;
        }
    }

    private static final class ChunkQueueAccessor {
        final Class<?> renderGlobalClass;
        final Field chunksToUpdate;
        final PositionAccess positionAccess;

        ChunkQueueAccessor(Class<?> renderGlobalClass, Field chunksToUpdate,
                           PositionAccess positionAccess) {
            this.renderGlobalClass = renderGlobalClass;
            this.chunksToUpdate = chunksToUpdate;
            this.positionAccess = positionAccess;
        }
    }

    private static final class ChunkOrder {
        final Object chunk;
        final double distanceSquared;
        final int originalIndex;

        ChunkOrder(Object chunk, double distanceSquared, int originalIndex) {
            this.chunk = chunk;
            this.distanceSquared = distanceSquared;
            this.originalIndex = originalIndex;
        }
    }
}
