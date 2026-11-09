package dev.cylox.client.background;

/** Background rendering and tick-safe main-loop policy. */
public final class BackgroundPolicy {
    // This throttles Minecraft's whole main loop, not only rendering.
    // Keep 20 Hz so client ticks and queued packet tasks are not starved.
    public static final int BACKGROUND_LOOP_FPS = 20;
    private static final float BACKGROUND_VIEW_FAR_PLANE = 6.0F / 1.41421356237F;

    private BackgroundPolicy() { }

    public static boolean shouldRender(boolean foreground) {
        return foreground;
    }

    public static boolean shouldRender(boolean foreground, boolean renderException) {
        return foreground || renderException;
    }

    public static int frameLimit(boolean foreground, int requestedFrameLimit) {
        return foreground ? requestedFrameLimit : BACKGROUND_LOOP_FPS;
    }

    public static float backgroundFarPlane(float requestedFarPlane, boolean foreground, boolean renderException) {
        return !foreground && renderException
            ? Math.min(requestedFarPlane, BACKGROUND_VIEW_FAR_PLANE)
            : requestedFarPlane;
    }

    public static boolean displayActiveForPause(boolean displayActive, boolean foreground, boolean pauseException) {
        return displayActive || (!foreground && pauseException);
    }

    public static boolean forceLeftClick(boolean vanillaLeftClick, boolean noOpenScreen, boolean toggleEnabled) {
        return vanillaLeftClick || (noOpenScreen && toggleEnabled);
    }

    public static int leftClickCounter(int currentCounter, boolean noOpenScreen, boolean toggleEnabled) {
        return noOpenScreen && toggleEnabled ? 0 : currentCounter;
    }
}