package dev.cylox.client.minecraft189;

import dev.cylox.client.background.BackgroundPolicy;
import org.lwjgl.opengl.Display;

/** LWJGL 2 lifecycle bridge. Fails open so a detection issue never blanks the game. */
public final class CyloxBackgroundHooks {
    private CyloxBackgroundHooks() { }

    /** Called at the beginning of vanilla EntityRenderer.updateCameraAndRender. */
    public static boolean shouldRender() {
        return BackgroundPolicy.shouldRender(isForeground());
    }

    /** Rewrites Display.sync's target while the game window is inactive. */
    public static int frameLimit(int requestedFrameLimit) {
        return BackgroundPolicy.frameLimit(isForeground(), requestedFrameLimit);
    }

    private static boolean isForeground() {
        try {
            return Display.isActive() && Display.isVisible();
        } catch (Throwable unavailable) {
            return true;
        }
    }
}
