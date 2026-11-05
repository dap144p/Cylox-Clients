package dev.cylox.client.background;

/** Pure policy for the background renderer and frame-loop limiter. */
public final class BackgroundPolicy {
    public static final int BACKGROUND_FPS = 5;

    private BackgroundPolicy() { }

    public static boolean shouldRender(boolean foreground) {
        return foreground;
    }

    public static int frameLimit(boolean foreground, int requestedFrameLimit) {
        return foreground ? requestedFrameLimit : BACKGROUND_FPS;
    }
}
