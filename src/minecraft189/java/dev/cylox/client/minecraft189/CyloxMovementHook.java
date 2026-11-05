package dev.cylox.client.minecraft189;

import dev.cylox.client.input.SprintKeyState;
import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.runtime.CyloxRuntime;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Applies native ToggleSprint and ToggleSneak key bindings.
 * It keeps Minecraft's physical-key behavior and does not alter coordinates,
 * velocity, movement packets, or server-side state directly.
 */
public final class CyloxMovementHook {
    private static Access access;
    private static boolean warned;

    private CyloxMovementHook() { }

    /** Called once near the start of the vanilla 1.8.9 client tick. */
    public static void apply(Object minecraft) {
        if (minecraft == null) return;
        try {
            Access resolved = access;
            if (resolved == null || !resolved.minecraftClass.isInstance(minecraft)) {
                resolved = resolve(minecraft.getClass());
                access = resolved;
            }

            Object player = resolved.playerField.get(minecraft);
            Object gameSettings = resolved.gameSettingsField.get(minecraft);
            if (gameSettings == null) return;

            applyBinding(gameSettings, player, resolved.sprintBindingField, "ToggleSprint");
            applyBinding(gameSettings, player, resolved.sneakBindingField, "ToggleSneak");
        } catch (Throwable error) {
            if (!warned) {
                warned = true;
                System.err.println("[Cylox] Movement key hooks unavailable: "
                    + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }
    }

    private static void applyBinding(Object gameSettings, Object player, Field bindingField,
                                     String moduleName) throws Exception {
        Object binding = bindingField.get(gameSettings);
        if (binding == null) return;

        int keyCode = ((Number) access.getKeyCode.invoke(binding)).intValue();
        boolean physicalDown = isPhysicalKeyDown(keyCode);
        CyloxModule module = CyloxRuntime.getModules().getModule(moduleName);
        boolean enabled = module != null && module.isEnabled();
        boolean pressed = SprintKeyState.shouldPress(enabled, player != null, physicalDown);
        access.setKeyBindState.invoke(null, keyCode, pressed);
    }

    private static boolean isPhysicalKeyDown(int keyCode) {
        if (keyCode >= 0) return Keyboard.isKeyDown(keyCode);
        int mouseButton = keyCode + 100;
        if (mouseButton < 0) return false;
        try {
            return Mouse.isButtonDown(mouseButton);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Access resolve(Class<?> minecraftClass) throws Exception {
        ClassLoader loader = minecraftClass.getClassLoader();
        Class<?> keyBinding = loadFirst(loader, "avb", "net.minecraft.client.settings.KeyBinding");
        Class<?> settingsType = loadFirst(loader, "avh", "net.minecraft.client.settings.GameSettings");

        Field gameSettings = findField(minecraftClass, "t", "field_71474_y", "gameSettings");
        Field player = findField(minecraftClass, "h", "field_71439_g", "thePlayer");

        // Verified 1.8.9 notch/SRG/MCP names:
        // ad = field_151444_V = keyBindSprint; ac = field_74311_E = keyBindSneak.
        Field sprintBinding = findField(settingsType, "ad", "field_151444_V", "keyBindSprint");
        Field sneakBinding = findField(settingsType, "ac", "field_74311_E", "keyBindSneak");
        Method getKeyCode = findMethod(keyBinding, new Class<?>[0],
            "i", "func_151463_i", "getKeyCode");
        Method setKeyBindState = findMethod(keyBinding,
            new Class<?>[] { int.class, boolean.class },
            "a", "func_74510_a", "setKeyBindState");

        if (!Modifier.isStatic(setKeyBindState.getModifiers())) {
            throw new NoSuchMethodException("KeyBinding.setKeyBindState is not static");
        }
        gameSettings.setAccessible(true);
        player.setAccessible(true);
        sprintBinding.setAccessible(true);
        sneakBinding.setAccessible(true);
        getKeyCode.setAccessible(true);
        setKeyBindState.setAccessible(true);
        return new Access(minecraftClass, gameSettings, player, sprintBinding, sneakBinding,
            getKeyCode, setKeyBindState);
    }

    private static Class<?> loadFirst(ClassLoader loader, String... names) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (String name : names) {
            try {
                return Class.forName(name, false, loader);
            } catch (ClassNotFoundException error) {
                last = error;
            }
        }
        throw last == null ? new ClassNotFoundException("No class names supplied") : last;
    }

    private static Field findField(Class<?> type, String... names) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (String name : names) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {
                    // Try the next vanilla/SRG/MCP name and superclass.
                }
            }
        }
        throw new NoSuchFieldException(type.getName() + " fields " + join(names));
    }

    private static Method findMethod(Class<?> type, Class<?>[] parameters, String... names)
        throws NoSuchMethodException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (String name : names) {
                try {
                    return current.getDeclaredMethod(name, parameters);
                } catch (NoSuchMethodException ignored) {
                    // Try the next vanilla/SRG/MCP name and superclass.
                }
            }
        }
        throw new NoSuchMethodException(type.getName() + " methods " + join(names));
    }

    private static String join(String[] values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) result.append(", ");
            result.append(value);
        }
        return result.toString();
    }

    private static final class Access {
        private final Class<?> minecraftClass;
        private final Field gameSettingsField;
        private final Field playerField;
        private final Field sprintBindingField;
        private final Field sneakBindingField;
        private final Method getKeyCode;
        private final Method setKeyBindState;

        private Access(Class<?> minecraftClass, Field gameSettingsField, Field playerField,
                       Field sprintBindingField, Field sneakBindingField,
                       Method getKeyCode, Method setKeyBindState) {
            this.minecraftClass = minecraftClass;
            this.gameSettingsField = gameSettingsField;
            this.playerField = playerField;
            this.sprintBindingField = sprintBindingField;
            this.sneakBindingField = sneakBindingField;
            this.getKeyCode = getKeyCode;
            this.setKeyBindState = setKeyBindState;
        }
    }
}
