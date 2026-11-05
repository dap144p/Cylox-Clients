package dev.cylox.client.minecraft189;

import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.runtime.CyloxRuntime;
import dev.cylox.client.ui.ClickGui;
import java.util.function.IntPredicate;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

/** Polls game input and routes the Click GUI and module keybinds. */
public final class CyloxKeyHook {
    private static final int TOGGLE_KEY = Keyboard.KEY_RSHIFT;

    private static boolean wasToggleDown;
    private static boolean restoreGrabOnClose;

    private CyloxKeyHook() {}

    /** Injected at the start of the vanilla client tick. */
    public static void onClientTick(Object ignored) {
        CyloxRuntime.getHud().setInput(
            Keyboard.isKeyDown(Keyboard.KEY_W),
            Keyboard.isKeyDown(Keyboard.KEY_A),
            Keyboard.isKeyDown(Keyboard.KEY_S),
            Keyboard.isKeyDown(Keyboard.KEY_D),
            Keyboard.isKeyDown(Keyboard.KEY_SPACE),
            Mouse.isButtonDown(0),
            Mouse.isButtonDown(1)
        );

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
                CyloxRuntime.saveSettings();
            }
        }
        wasToggleDown = toggleDown;

        IntPredicate keyState = keyCode -> Keyboard.isKeyDown(keyCode);
        boolean keybindChanged = CyloxRuntime.getKeybindController().tick(
            CyloxRuntime.getModules(), keyState, TOGGLE_KEY, !gui.isOpen());
        if (keybindChanged) CyloxRuntime.saveSettings();
        if (!gui.isOpen()) return;

        boolean settingsChanged = false;
        int height = Display.getHeight();
        while (Mouse.next()) {
            int button = Mouse.getEventButton();
            int mouseX = Mouse.getEventX();
            int mouseY = height - Mouse.getEventY() - 1;
            if (button >= 0) {
                if (Mouse.getEventButtonState()) gui.mouseClicked(mouseX, mouseY, button);
                else gui.mouseReleased(mouseX, mouseY, button);
                settingsChanged = true;
            }
        }
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                int keyCode = Keyboard.getEventKey();
                if (keyCode != TOGGLE_KEY) {
                    gui.keyTyped(Keyboard.getEventCharacter(), keyCode);
                    settingsChanged = true;
                }
            }
        }
        if (Mouse.isButtonDown(0)) {
            gui.mouseDragged(Mouse.getX(), height - Mouse.getY() - 1, 0);
        }
        if (settingsChanged) CyloxRuntime.saveSettings();
        if (!gui.isOpen()) restoreMouse();
    }

    private static void restoreMouse() {
        if (restoreGrabOnClose) Mouse.setGrabbed(true);
        restoreGrabOnClose = false;
    }
}
