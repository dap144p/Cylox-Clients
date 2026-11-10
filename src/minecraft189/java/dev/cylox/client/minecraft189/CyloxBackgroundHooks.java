package dev.cylox.client.minecraft189;

import dev.cylox.client.background.BackgroundPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

/** LWJGL 2 lifecycle and input hooks for the Minecraft 1.8.9 client. */
public final class CyloxBackgroundHooks {
    private static volatile boolean toggleLeftClick;
    private static boolean f8KeyDown;
    private static volatile MovementBindings containerMovementBindings;
    private static boolean containerMovementWarningLogged;

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

    /** Polls only the four configured movement bindings while a container GUI is open. */
    public static void updateContainerMovement(Object minecraft, Object currentScreen) {
        if (minecraft == null || !isContainerScreen(currentScreen)) return;

        try {
            MovementBindings bindings = getContainerMovementBindings(minecraft);
            for (int i = 0; i < bindings.keys.length; i++) {
                int keyCode = bindings.keyCodes[i].getInt(bindings.keys[i]);
                boolean pressed = keyCode > 0 && Keyboard.isKeyDown(keyCode);
                bindings.pressedFields[i].setBoolean(bindings.keys[i], pressed);
            }
        } catch (Throwable failure) {
            if (!containerMovementWarningLogged) {
                containerMovementWarningLogged = true;
                System.err.println("[Cylox] Container movement input unavailable: "
                    + failure.getClass().getSimpleName());
            }
        }
    }

    private static boolean isContainerScreen(Object screen) {
        if (screen == null) return false;
        for (Class<?> type = screen.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (Modifier.isAbstract(method.getModifiers())
                    && method.getReturnType() == Void.TYPE
                    && parameters.length == 3
                    && parameters[0] == Float.TYPE
                    && parameters[1] == Integer.TYPE
                    && parameters[2] == Integer.TYPE) {
                    return true;
                }
            }
        }
        return false;
    }

    private static MovementBindings getContainerMovementBindings(Object minecraft) throws Exception {
        MovementBindings cached = containerMovementBindings;
        if (cached != null && cached.minecraft == minecraft
            && cached.settingsField.get(minecraft) == cached.settings) {
            return cached;
        }

        synchronized (CyloxBackgroundHooks.class) {
            cached = containerMovementBindings;
            if (cached != null && cached.minecraft == minecraft
                && cached.settingsField.get(minecraft) == cached.settings) {
                return cached;
            }

            for (Field settingsField : allFields(minecraft.getClass())) {
                if (Modifier.isStatic(settingsField.getModifiers())
                    || settingsField.getType().isPrimitive()
                    || settingsField.getType().isArray()) continue;
                settingsField.setAccessible(true);
                Object settings = settingsField.get(minecraft);
                if (settings == null) continue;

                MovementBindings found = findMovementBindings(minecraft, settingsField, settings);
                if (found != null) {
                    containerMovementBindings = found;
                    return found;
                }
            }
        }
        throw new IllegalStateException("Could not locate configured WASD key bindings");
    }

    private static MovementBindings findMovementBindings(
        Object minecraft, Field settingsField, Object settings) throws Exception {
        String[] descriptions = {"key.forward", "key.back", "key.left", "key.right"};
        Object[] keys = new Object[descriptions.length];
        Field[] keyCodes = new Field[descriptions.length];
        Field[] pressedFields = new Field[descriptions.length];

        for (Field field : allFields(settings.getClass())) {
            if (Modifier.isStatic(field.getModifiers())
                || field.getType().isPrimitive()
                || field.getType().isArray()) continue;
            field.setAccessible(true);
            Object binding = field.get(settings);
            if (binding == null) continue;

            String description = findKeyDescription(binding);
            int index = indexOf(descriptions, description);
            if (index < 0 || keys[index] != null) continue;

            Field keyCode = findField(binding.getClass(),
                new String[] {"keyCode", "field_74512_d", "g"});
            Field pressed = findField(binding.getClass(),
                new String[] {"pressed", "field_74513_e", "h"});
            if (keyCode == null || pressed == null) continue;

            keys[index] = binding;
            keyCodes[index] = keyCode;
            pressedFields[index] = pressed;
        }

        for (Object key : keys) if (key == null) return null;
        return new MovementBindings(minecraft, settingsField, settings, keys, keyCodes, pressedFields);
    }

    private static String findKeyDescription(Object binding) throws IllegalAccessException {
        for (Field field : allFields(binding.getClass())) {
            if (field.getType() != String.class || Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            Object value = field.get(binding);
            if ("key.forward".equals(value) || "key.back".equals(value)
                || "key.left".equals(value) || "key.right".equals(value)) {
                return (String) value;
            }
        }
        return null;
    }

    private static Field findField(Class<?> type, String[] names) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                for (String name : names) {
                    if (name.equals(field.getName())) {
                        field.setAccessible(true);
                        return field;
                    }
                }
            }
        }
        return null;
    }

    private static Field[] allFields(Class<?> type) {
        java.util.List<Field> fields = new java.util.ArrayList<Field>();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) fields.add(field);
        }
        return fields.toArray(new Field[fields.size()]);
    }

    private static int indexOf(String[] values, String target) {
        if (target == null) return -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(target)) return i;
        }
        return -1;
    }

    private static final class MovementBindings {
        private final Object minecraft;
        private final Field settingsField;
        private final Object settings;
        private final Object[] keys;
        private final Field[] keyCodes;
        private final Field[] pressedFields;

        private MovementBindings(Object minecraft, Field settingsField, Object settings,
                                 Object[] keys, Field[] keyCodes, Field[] pressedFields) {
            this.minecraft = minecraft;
            this.settingsField = settingsField;
            this.settings = settings;
            this.keys = keys;
            this.keyCodes = keyCodes;
            this.pressedFields = pressedFields;
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
