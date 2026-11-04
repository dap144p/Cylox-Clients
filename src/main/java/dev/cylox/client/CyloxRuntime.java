package dev.cylox.client;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runtime bootstrap for Cylox Client.
 * This early vanilla migration release validates that the custom runtime loads.
 */
public final class CyloxRuntime {
    private static final String VERSION = "0.2.3-alpha";
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private CyloxRuntime() {
    }

    public static void bootstrap(String[] gameArguments) {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        System.setProperty("cylox.client.loaded", "true");
        System.setProperty("cylox.client.name", "Cylox Client");
        System.setProperty("cylox.client.version", VERSION);
        System.setProperty("cylox.client.minecraft", "1.8.9");
        System.setProperty("cylox.client.runtime", "vanilla");

        int argumentCount = gameArguments == null ? 0 : gameArguments.length;
        System.out.println("[Cylox Client] " + VERSION
                + " | Minecraft Java 1.8.9"
                + " | Vanilla runtime"
                + " | arguments=" + argumentCount);
    }
}
