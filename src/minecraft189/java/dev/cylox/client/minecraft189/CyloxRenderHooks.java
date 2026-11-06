package dev.cylox.client.minecraft189;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Mobile-focused chunk scheduling hooks for vanilla Minecraft 1.8.9. */
public final class CyloxRenderHooks {
    private static final ConcurrentMap<String, Method> METHODS = new ConcurrentHashMap<String, Method>();
    private static volatile boolean workerBudgetLogged;
    private static volatile boolean fallbackLogged;
    private static volatile boolean lookupFailureLogged;

    private CyloxRenderHooks() { }

    /** Caps vanilla's available-processor input so the dispatcher creates at most two workers. */
    public static int limitChunkWorkers(int processors) {
        int limited = Math.min(Math.max(1, processors), 4);
        if (!workerBudgetLogged) {
            workerBudgetLogged = true;
            System.out.println("[Cylox] Chunk scheduler budget: up to 2 worker threads.");
        }
        return limited;
    }

    /**
     * Prefer the dispatcher's existing asynchronous rebuild queue. If the bounded queue
     * is full, run the original synchronous method as a correctness-preserving fallback.
     */
    public static boolean scheduleNearChunk(Object dispatcher, Object chunk,
                                            String asyncMethodName, String syncMethodName) {
        if (dispatcher == null || chunk == null) return false;
        Method async = resolve(dispatcher.getClass(), asyncMethodName, chunk.getClass());
        if (async != null) {
            try {
                if (Boolean.TRUE.equals(async.invoke(dispatcher, chunk))) return true;
                logFallback("worker queue is full");
            } catch (Throwable error) {
                logFallback("asynchronous scheduling failed: " + error.getClass().getSimpleName());
            }
        } else {
            logLookupFailure("asynchronous dispatcher method was not found");
        }

        Method sync = resolve(dispatcher.getClass(), syncMethodName, chunk.getClass());
        if (sync != null) {
            try {
                sync.invoke(dispatcher, chunk);
                return true;
            } catch (Throwable error) {
                logLookupFailure("synchronous fallback failed: " + error.getClass().getSimpleName());
            }
        } else {
            logLookupFailure("synchronous dispatcher method was not found");
        }
        return false;
    }

    private static Method resolve(Class<?> owner, String name, Class<?> argumentClass) {
        String key = owner.getName() + "#" + name + "#" + argumentClass.getName();
        Method cached = METHODS.get(key);
        if (cached != null) return cached;

        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!name.equals(method.getName()) || method.getParameterTypes().length != 1
                    || !method.getParameterTypes()[0].isAssignableFrom(argumentClass)
                    || method.getReturnType() != Boolean.TYPE) continue;
                try {
                    method.setAccessible(true);
                    Method previous = METHODS.putIfAbsent(key, method);
                    return previous == null ? method : previous;
                } catch (Throwable error) {
                    logLookupFailure("could not access dispatcher method: " + error.getClass().getSimpleName());
                    return null;
                }
            }
        }
        return null;
    }

    private static void logFallback(String reason) {
        if (!fallbackLogged) {
            fallbackLogged = true;
            System.out.println("[Cylox] Chunk scheduler fallback: " + reason + "; using vanilla synchronous rebuild.");
        }
    }

    private static void logLookupFailure(String reason) {
        if (!lookupFailureLogged) {
            lookupFailureLogged = true;
            System.err.println("[Cylox] Chunk scheduler hook warning: " + reason + ".");
        }
    }
}
