package dev.cylox.client.minecraft189;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.lwjgl.opengl.GL11;

/** Lightweight immediate-mode UI skin: no textures, shaders, animations or added UI libraries. */
public final class CyloxUiHooks {
    private static final int WHITE = 0xFFEAF3F8;
    private static final int MUTED = 0xFF8DA5B5;
    private static final int CYAN = 0xFF36D7E8;
    private static final int ORANGE = 0xFFFFA45B;
    private CyloxUiHooks() { }

    /** Replaces vanilla GuiButton drawing while preserving the original button and its actions. */
    public static boolean drawButton(Object button, Object minecraft, int mouseX, int mouseY) {
        if (button == null) return false;
        try {
            int x = intField(button, 0, "xPosition", "field_146128_h", "h");
            int y = intField(button, 0, "yPosition", "field_146129_i", "i");
            int w = intField(button, 0, "width", "field_146120_f", "f");
            int h = intField(button, 0, "height", "field_146121_g", "g");
            if (w <= 0 || h <= 0) return false;
            if (!boolField(button, true, "visible", "field_146125_m", "m")) {
                setBool(button, false, "hovered", "field_146123_n", "n");
                return true;
            }
            boolean enabled = boolField(button, true, "enabled", "field_146124_l", "l");
            boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
            setBool(button, hovered, "hovered", "field_146123_n", "n");

            int fill = !enabled ? 0xFF101A25 : hovered ? 0xFF1A3545 : 0xFF122333;
            int edge = !enabled ? 0xFF273541 : hovered ? CYAN : 0xFF24465A;
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            rect(x, y, x + w, y + h, fill);
            rect(x, y, x + w, y + 1, edge);
            rect(x, y + h - 1, x + w, y + h, edge);
            rect(x, y, x + 1, y + h, edge);
            rect(x + w - 1, y, x + w, y + h, edge);
            rect(x + 2, y + 2, x + 4, y + h - 2, hovered ? ORANGE : CYAN);
            GL11.glPopAttrib();

            String label = stringField(button, "displayString", "field_146126_j", "j");
            if (label.length() > 0) {
                centered(font(minecraft), label, x + w / 2, y + (h - 8) / 2, enabled ? WHITE : MUTED);
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Replaces the panorama/title renderer with a static Cylox main menu. */
    public static void drawMainMenu(Object screen, int mouseX, int mouseY) {
        int width = intField(screen, 0, "width", "field_146294_l", "l");
        int height = intField(screen, 0, "height", "field_146295_m", "m");
        if (width <= 0 || height <= 0) return;
        drawScreenBackground(screen);

        List<?> buttons = listField(screen, "buttonList", "field_146292_n", "n");
        Object minecraft = objectField(screen, "mc", "field_146297_k", "k");
        int left = width / 2 - 110, right = width / 2 + 110;
        int top = height / 2 - 45, bottom = height / 2 + 65;
        if (buttons != null) {
            for (Object b : buttons) {
                int x = intField(b, -1, "xPosition", "field_146128_h", "h");
                int y = intField(b, -1, "yPosition", "field_146129_i", "i");
                int w = intField(b, 0, "width", "field_146120_f", "f");
                int h = intField(b, 0, "height", "field_146121_g", "g");
                if (x >= 0 && y >= 0 && w > 0 && h > 0) {
                    left = Math.min(left, x - 12); right = Math.max(right, x + w + 12);
                    top = Math.min(top, y - 12); bottom = Math.max(bottom, y + h + 12);
                }
            }
        }
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        rect(left, top, right, bottom, 0xD80C1B2A);
        rect(left, top, right, top + 2, CYAN);
        rect(left, top, left + 2, bottom, 0xFF24465A);
        GL11.glPopAttrib();

        Object font = font(screen);
        centered(font, "CYLOX", width / 2, Math.max(18, top - 54), CYAN);
        centered(font, "LIGHTWEIGHT CLIENT  /  MINECRAFT 1.8.9",
            width / 2, Math.max(31, top - 39), MUTED);
        if (buttons != null) for (Object b : buttons) drawButton(b, minecraft, mouseX, mouseY);
        centered(font, "VANILLA 1.8.9  -  DAPDEV", width / 2, height - 18, MUTED);
        centered(font, "CYLOX", 30, 12, ORANGE);
    }

    /** Replaces the tiled options texture with a static dark canvas on standard GUI screens. */
    public static void drawScreenBackground(Object screen) {
        int w = intField(screen, 0, "width", "field_146294_l", "l");
        int h = intField(screen, 0, "height", "field_146295_m", "m");
        if (w <= 0 || h <= 0) return;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        rect(0, 0, w, h, 0xFF08121E);
        rect(0, 0, w, 2, 0xFF173448);
        rect(0, 0, 3, h, 0xFF1C6272);
        rect(0, h - 2, w, h, 0xFF152C3B);
        rect(12, 12, 13, h - 12, 0x332FC9DA);
        GL11.glPopAttrib();
    }

    private static void rect(int l, int t, int r, int b, int color) {
        if (r <= l || b <= t) return;
        GL11.glColor4f(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F,
            (color & 255) / 255F, ((color >>> 24) & 255) / 255F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(l, b); GL11.glVertex2f(r, b);
        GL11.glVertex2f(r, t); GL11.glVertex2f(l, t);
        GL11.glEnd();
    }

    private static void centered(Object font, String text, int center, int y, int color) {
        if (font == null || text == null || text.length() == 0) return;
        try {
            int width = text.length() * 6;
            Method widthMethod = findFontWidthMethod(font);
            if (widthMethod != null) {
                Object measured = widthMethod.invoke(font, text);
                if (measured instanceof Integer) width = ((Integer) measured).intValue();
            }
            Method drawMethod = findFontDrawMethod(font);
            if (drawMethod == null) return;

            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            try {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                drawMethod.invoke(font, text, center - width / 2.0F, (float) y, color);
            } finally {
                GL11.glPopAttrib();
            }
        } catch (Throwable ignored) { }
    }

    private static Object font(Object owner) {
        if (owner == null) return null;
        Object direct = objectField(owner, "fontRendererObj", "field_71466_p",
            "field_146289_q", "p", "q");
        if (isFontRenderer(direct)) return direct;

        // Mappings differ between launcher-provided, SRG and obfuscated 1.8.9 classes.
        // Fall back to locating the unique field that has the FontRenderer method shape.
        for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                try {
                    field.setAccessible(true);
                    Object candidate = field.get(owner);
                    if (isFontRenderer(candidate)) return candidate;
                } catch (Throwable ignored) { }
            }
        }
        return null;
    }

    private static boolean isFontRenderer(Object candidate) {
        if (candidate == null) return false;
        boolean width = false;
        boolean draw = false;
        for (Class<?> type = candidate.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                Class<?>[] p = method.getParameterTypes();
                if (p.length == 1 && p[0] == String.class
                    && method.getReturnType() == Integer.TYPE) width = true;
                if (p.length == 4 && p[0] == String.class && p[1] == Float.TYPE
                    && p[2] == Float.TYPE && p[3] == Integer.TYPE) draw = true;
            }
        }
        return width && draw;
    }

    private static Method findFontWidthMethod(Object font) {
        String[] names = {"getStringWidth", "func_78256_a", "a"};
        for (String name : names) {
            for (Class<?> type = font.getClass(); type != null; type = type.getSuperclass()) {
                for (Method method : type.getDeclaredMethods()) {
                    Class<?>[] p = method.getParameterTypes();
                    if (name.equals(method.getName()) && p.length == 1
                        && p[0] == String.class && method.getReturnType() == Integer.TYPE) {
                        try { method.setAccessible(true); return method; }
                        catch (Throwable ignored) { }
                    }
                }
            }
        }
        for (Class<?> type = font.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                Class<?>[] p = method.getParameterTypes();
                if (p.length == 1 && p[0] == String.class
                    && method.getReturnType() == Integer.TYPE) {
                    try { method.setAccessible(true); return method; }
                    catch (Throwable ignored) { }
                }
            }
        }
        return null;
    }

    private static Method findFontDrawMethod(Object font) {
        String[] names = {"drawStringWithShadow", "func_175063_a", "drawString", "a"};
        for (String name : names) {
            for (Class<?> type = font.getClass(); type != null; type = type.getSuperclass()) {
                for (Method method : type.getDeclaredMethods()) {
                    Class<?>[] p = method.getParameterTypes();
                    if (name.equals(method.getName()) && p.length == 4
                        && p[0] == String.class && p[1] == Float.TYPE
                        && p[2] == Float.TYPE && p[3] == Integer.TYPE) {
                        try { method.setAccessible(true); return method; }
                        catch (Throwable ignored) { }
                    }
                }
            }
        }
        for (Class<?> type = font.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                Class<?>[] p = method.getParameterTypes();
                if (p.length == 4 && p[0] == String.class && p[1] == Float.TYPE
                    && p[2] == Float.TYPE && p[3] == Integer.TYPE) {
                    try { method.setAccessible(true); return method; }
                    catch (Throwable ignored) { }
                }
            }
        }
        return null;
    }

    private static List<?> listField(Object o, String... n) {
        Object v = objectField(o, n); return v instanceof List<?> ? (List<?>) v : null;
    }
    private static String stringField(Object o, String... n) {
        Object value = objectField(o, n);
        if (value instanceof String && ((String) value).length() > 0) return (String) value;
        if (o != null) {
            for (Class<?> type = o.getClass(); type != null; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (field.getType() != String.class) continue;
                    try {
                        field.setAccessible(true);
                        Object candidate = field.get(o);
                        if (candidate instanceof String && ((String) candidate).length() > 0) {
                            return (String) candidate;
                        }
                    } catch (Throwable ignored) { }
                }
            }
        }
        return "";
    }
    private static int intField(Object o, int d, String... n) {
        Field f = findField(o, n); try { return f == null ? d : f.getInt(o); }
        catch (Throwable ignored) { return d; }
    }
    private static boolean boolField(Object o, boolean d, String... n) {
        Field f = findField(o, n); try { return f == null ? d : f.getBoolean(o); }
        catch (Throwable ignored) { return d; }
    }
    private static void setBool(Object o, boolean v, String... n) {
        Field f = findField(o, n); try { if (f != null) f.setBoolean(o, v); }
        catch (Throwable ignored) { }
    }
    private static Object objectField(Object o, String... n) {
        Field f = findField(o, n); try { return f == null ? null : f.get(o); }
        catch (Throwable ignored) { return null; }
    }
    private static Field findField(Object o, String... names) {
        if (o == null) return null;
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            for (String n : names) try {
                Field f = c.getDeclaredField(n); f.setAccessible(true); return f;
            } catch (Throwable ignored) { }
        }
        return null;
    }
}
