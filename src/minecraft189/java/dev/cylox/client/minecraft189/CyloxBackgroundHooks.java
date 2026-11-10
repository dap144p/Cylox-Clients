package dev.cylox.client.minecraft189;

import dev.cylox.client.background.BackgroundPolicy;
import org.lwjgl.opengl.Display;

/** LWJGL 2 lifecycle and F8 keybind bridge for the Minecraft 1.8.9 client. */
public final class CyloxBackgroundHooks {
    private static volatile boolean toggleLeftClick;
    private static boolean f8KeyDown;

    private CyloxBackgroundHooks() { }

    /** Processes only the F8 key-down edge, so OS key repeat cannot toggle twice. */
    public static void onKeyboardEvent(int keyCode, boolean pressed) {
        synchronized (CyloxBackgroundHooks.class) {
            if (keyCode == 66) {
                if (pressed && !f8KeyDown) toggleLeftClick = !toggleLeftClick;
                f8KeyDown = pressed;
            }
        }
    }

    /** Called before Minecraft's normal block-damage controller path. */
    public static boolean forceLeftClick(boolean vanillaLeftClick, Object currentScreen) {
        return BackgroundPolicy.forceLeftClick(vanillaLeftClick, currentScreen == null, toggleLeftClick);
    }

    /** Clears vanilla's regain-focus attack delay only while the F8 toggle is active. */
    public static int leftClickCounter(int currentCounter, Object currentScreen) {
        return BackgroundPolicy.leftClickCounter(currentCounter, currentScreen == null, toggleLeftClick);
    }

    /** Called at the background render guard. */
    public static boolean shouldRender() {
        return BackgroundPolicy.shouldRender(isForeground());
    }

    /** Rewrites Display.sync's target while the game window is inactive. */
    public static int frameLimit(int requestedFrameLimit) {
        return BackgroundPolicy.frameLimit(isForeground(), requestedFrameLimit);
    }

    /** Draws the current F8 state after Minecraft's in-game HUD. */
    public static void drawToggleStatus(Object hudOwner, int screenWidth, int screenHeight) {
        if (hudOwner == null || screenWidth <= 0 || screenHeight <= 0) return;
        int right = screenWidth - 4;
        int color = toggleLeftClick ? 0xFF36D7E8 : 0xFF8DA5B5;
        CyloxUiHooks.drawTextRight(hudOwner, "F8: " + (toggleLeftClick ? "ON" : "OFF"),
            right, screenHeight - 11, color);
    }

    private static boolean isForeground() {
        try {
            boolean foreground = Display.isActive() && Display.isVisible();
            if (!foreground) {
                synchronized (CyloxBackgroundHooks.class) {
                    f8KeyDown = false;
                }
            }
            return foreground;
        } catch (Throwable unavailable) {
            return true;
        }
    }
}
