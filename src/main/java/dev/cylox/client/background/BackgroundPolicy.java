package dev.cylox.client.background;

/** Background rendering and tick-safe main-loop policy. */
public final class BackgroundPolicy {
    // This throttles Minecraft's whole main loop, not only rendering.
    // Keep 20 Hz so client ticks and queued packet tasks are not starved.
    public static final int BACKGROUND_LOOP_FPS = 20;

    private BackgroundPolicy() { }

    public static boolean shouldRender(boolean foreground) {
        return foreground;
    }

    public static int frameLimit(boolean foreground, int requestedFrameLimit) {
        return foreground ? requestedFrameLimit : BACKGROUND_LOOP_FPS;
    }
}
