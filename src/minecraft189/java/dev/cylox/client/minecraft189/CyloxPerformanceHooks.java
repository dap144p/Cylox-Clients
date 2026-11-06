package dev.cylox.client.minecraft189;

import java.lang.reflect.Field;

/**
 * Applies conservative Minecraft 1.8.9 graphics defaults once after options.txt loads.
 * World rendering, entities, HUD, input, ticks and network processing remain enabled.
 */
public final class CyloxPerformanceHooks {
    private CyloxPerformanceHooks() { }

    public static void applyDefaults(Object settings, String renderDistanceField,
                                     String fancyGraphicsField, String ambientOcclusionField,
                                     String cloudsField, String particlesField,
                                     String entityShadowsField) {
        if (settings == null) return;
        int applied = 0;
        try {
            Field renderDistance = findField(settings.getClass(), renderDistanceField);
            if (renderDistance != null && renderDistance.getType() == Integer.TYPE) {
                int current = renderDistance.getInt(settings);
                renderDistance.setInt(settings, Math.max(2, Math.min(4, current)));
                applied++;
            }
        } catch (Throwable error) {
            warn("render distance", error);
        }
        applied += setBoolean(settings, fancyGraphicsField, false, "fancy graphics");
        applied += setInt(settings, ambientOcclusionField, 0, "smooth lighting");
        applied += setInt(settings, cloudsField, 0, "clouds");
        applied += setInt(settings, particlesField, 2, "particles");
        applied += setBoolean(settings, entityShadowsField, false, "entity shadows");
        if (applied == 6) {
            System.out.println("[Cylox] Lightweight graphics profile active: render distance max 4 chunks, fast graphics, smooth lighting off, clouds off, minimal particles and entity shadows off.");
        } else {
            System.err.println("[Cylox] Lightweight graphics profile partially applied (" + applied + "/6 settings).");
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) { }
        }
        throw new NoSuchFieldException(name);
    }

    private static int setInt(Object target, String name, int value, String label) {
        try {
            Field field = findField(target.getClass(), name);
            if (field.getType() != Integer.TYPE) throw new IllegalArgumentException("not an int field");
            field.setInt(target, value);
            return 1;
        } catch (Throwable error) {
            warn(label, error);
            return 0;
        }
    }

    private static int setBoolean(Object target, String name, boolean value, String label) {
        try {
            Field field = findField(target.getClass(), name);
            if (field.getType() != Boolean.TYPE) throw new IllegalArgumentException("not a boolean field");
            field.setBoolean(target, value);
            return 1;
        } catch (Throwable error) {
            warn(label, error);
            return 0;
        }
    }

    private static void warn(String setting, Throwable error) {
        System.err.println("[Cylox] Could not apply " + setting + " preset: "
            + error.getClass().getSimpleName() + ": " + error.getMessage());
    }
}
