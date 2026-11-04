package dev.cylox.client.minecraft189;

import dev.cylox.client.runtime.CyloxRuntime;
import dev.cylox.client.ui.ClickGui;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

/** Polls and routes input while the in-game Click GUI is open. */
public final class CyloxKeyHook {
    private static final int TOGGLE_KEY = Keyboard.KEY_RSHIFT;
    private static boolean wasToggleDown;
    private static boolean restoreGrabOnClose;

    private CyloxKeyHook() {}

    /** Injected at the start of the vanilla client tick. */
    public static void onClientTick(Object ignored) {
        ClickGui gui = CyloxRuntime.getClickGui();
        boolean toggleDown = Keyboard.isKeyDown(TOGGLE_KEY);
        if (toggleDown && !wasToggleDown) {
            boolean opening = !gui.isOpen();
            gui.setOpen(opening);
            if (opening) {
                restoreGrabOnClose = Mouse.isGrabbed();
                Mouse.setGrabbed(false);
            } else {
                restoreMouse();
            }
        }
        wasToggleDown = toggleDown;
        if (!gui.isOpen()) return;

        int height = Display.getHeight();
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int mouseX = Mouse.getEventX();
            int mouseY = height - Mouse.getEventY() - 1;
            if (button >= 0) {
                if (Mouse.getEventButtonState()) gui.mouseClicked(mouseX, mouseY, button);
                else gui.mouseReleased(mouseX, mouseY, button);
            }
        }
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                int keyCode = Keyboard.getEventKey();
                if (keyCode != TOGGLE_KEY) {
                    gui.keyTyped(Keyboard.getEventCharacter(), keyCode);
                }
            }
        }
        if (Mouse.isButtonDown(0)) {
            gui.mouseDragged(Mouse.getX(), height - Mouse.getY() - 1, 0);
        }
        if (!gui.isOpen()) restoreMouse();
    }

    private static void restoreMouse() {
        if (restoreGrabOnClose) Mouse.setGrabbed(true);
        restoreGrabOnClose = false;
    }
}
