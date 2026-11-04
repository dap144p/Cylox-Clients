package dev.cylox.client.minecraft189;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

/** Edge-triggered Right Shift shortcut, called on the Minecraft client thread. */
public final class CyloxKeyHook {
    private static final int TOGGLE_KEY = Keyboard.KEY_RSHIFT;
    private static boolean wasDown;

    private CyloxKeyHook() {
    }

    public static void onClientTick() {
        boolean isDown = Keyboard.isKeyDown(TOGGLE_KEY);
        if (isDown && !wasDown) {
            Minecraft minecraft = Minecraft.getMinecraft();
            if (minecraft != null) {
                if (minecraft.currentScreen instanceof CyloxClickGuiScreen) {
                    minecraft.displayGuiScreen(null);
                } else if (minecraft.currentScreen == null) {
                    CyloxClickGuiScreen.open(minecraft);
                }
            }
        }
        wasDown = isDown;
    }
}
