package dev.cylox.client.minecraft189;

import dev.cylox.client.background.BackgroundPolicy;
import org.lwjgl.opengl.Display;

/** LWJGL 2 lifecycle and keybind bridge for the Minecraft 1.8.9 client. */
public final class CyloxBackgroundHooks {
    private static volatile boolean toggleLeftClick;
    private static volatile boolean renderException;
    private static volatile boolean pauseException;
    private static boolean f8KeyDown;
    private static boolean f9KeyDown;
    private static boolean f10KeyDown;

    private CyloxBackgroundHooks() { }

    /** Processes only the key-down edge, so OS key repeat cannot toggle twice. */
    public static void onKeyboardEvent(int keyCode, boolean pressed) {
        synchronized (CyloxBackgroundHooks.class) {
            if (keyCode == 66) {
                if (pressed && !f8KeyDown) toggleLeftClick = !toggleLeftClick;
                f8KeyDown = pressed;
            } else if (keyCode == 67) {
                if (pressed && !f9KeyDown) renderException = !renderException;
                f9KeyDown = pressed;
            } else if (keyCode == 68) {
                if (pressed && !f10KeyDown) pauseException = !pauseException;
                f10KeyDown = pressed;
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

    /** Allows the vanilla lost-focus pause check to run before Background skips rendering. */
    public static boolean displayActiveForPause(boolean displayActive) {
        return BackgroundPolicy.displayActiveForPause(displayActive, isForeground(), pauseException);
    }

    /** Called at the background render guard. F9 enables only a short camera frustum. */
    public static boolean shouldRender() {
        return BackgroundPolicy.shouldRender(isForeground(), renderException);
    }

    /** Caps the vanilla camera far plane to a six-block view while F9 is active in Background. */
    public static float backgroundFarPlane(float requestedFarPlane) {
        return BackgroundPolicy.backgroundFarPlane(requestedFarPlane, isForeground(), renderException);
    }

    /** Rewrites Display.sync's target while the game window is inactive. */
    public static int frameLimit(int requestedFrameLimit) {
        return BackgroundPolicy.frameLimit(isForeground(), requestedFrameLimit);
    }

    /** Draws the current independent keybind states after Minecraft's in-game HUD. */
    public static void drawToggleStatus(Object hudOwner, int screenWidth, int screenHeight) {
        if (hudOwner == null || screenWidth <= 0 || screenHeight <= 0) return;
        int right = screenWidth - 4;
        int onColor = 0xFF36D7E8;
        int offColor = 0xFF8DA5B5;
        CyloxUiHooks.drawTextRight(hudOwner, "F8: " + (toggleLeftClick ? "ON" : "OFF"),
            right, screenHeight - 31, toggleLeftClick ? onColor : offColor);
        CyloxUiHooks.drawTextRight(hudOwner, "F9: " + (renderException ? "ON" : "OFF"),
            right, screenHeight - 21, renderException ? onColor : offColor);
        CyloxUiHooks.drawTextRight(hudOwner, "F10: " + (pauseException ? "ON" : "OFF"),
            right, screenHeight - 11, pauseException ? onColor : offColor);
    }

    private static boolean isForeground() {
        try {
            boolean foreground = Display.isActive() && Display.isVisible();
            if (!foreground) {
                synchronized (CyloxBackgroundHooks.class) {
                    f8KeyDown = false;
                    f9KeyDown = false;
                    f10KeyDown = false;
                }
            }
            return foreground;
        } catch (Throwable unavailable) {
            return true;
        }
    }
}