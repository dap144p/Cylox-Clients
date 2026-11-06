package dev.cylox.client.minecraft189;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.lwjgl.opengl.GL11;

/** Lightweight immediate-mode UI skin: no textures, shaders, animations or added UI libraries. */
public final class CyloxUiHooks {
    private static final int WHITE = 0xFFEAF3F8;
    private static final int MUTED = 0xFF8DA5B5;
    private static final int CYAN = 0xFF36D7E8;
    private static final int ORANGE = 0xFFFFA45B;
    private static final ConcurrentMap<Class<?>, ButtonAccess> BUTTON_ACCESS =
        new ConcurrentHashMap<Class<?>, ButtonAccess>();
    private static final ConcurrentMap<Class<?>, FontOwnerAccess> FONT_OWNER_ACCESS =
        new ConcurrentHashMap<Class<?>, FontOwnerAccess>();
    private static final ConcurrentMap<Class<?>, FontMethods> FONT_METHODS =
        new ConcurrentHashMap<Class<?>, FontMethods>();

    /** Cached reflective fields; button rendering is called for every visible button every frame. */
    private static final class ButtonAccess {
        final Field x, y, width, height, visible, enabled, hovered, label;
        ButtonAccess(Class<?> type) {
            x = findField(type, "xPosition", "field_146128_h", "h");
            y = findField(type, "yPosition", "field_146129_i", "i");
            width = findField(type, "width", "field_146120_f", "f");
            height = findField(type, "height", "field_146121_g", "g");
            visible = findField(type, "visible", "field_146125_m", "m");
            enabled = findField(type, "enabled", "field_146124_l", "l");
            hovered = findField(type, "hovered", "field_146123_n", "n");
            Field text = findField(type, "displayString", "field_146126_j", "j");
            label = text != null ? text : findFirstField(type, String.class);
        }
    }

    /** Resolves a screen/Minecraft FontRenderer field once per owner class, not once per frame. */
    private static final class FontOwnerAccess {
        final Field renderer;
        FontOwnerAccess(Class<?> type) {
            Field found = findField(type, "fontRendererObj", "field_71466_p",
                "field_146289_q", "p", "q");
            if (found == null || !isFontRendererClass(found.getType())) {
                found = null;
                for (Class<?> current = type; current != null && found == null;
                     current = current.getSuperclass()) {
                    for (Field candidate : current.getDeclaredFields()) {
                        if (Modifier.isStatic(candidate.getModifiers())
                            || !isFontRendererClass(candidate.getType())) continue;
                        try { candidate.setAccessible(true); found = candidate; break; }
                        catch (Throwable ignored) { }
                    }
                }
            }
            renderer = found;
        }
    }

    /** Cached FontRenderer method handles shared by all screens and buttons. */
    private static final class FontMethods {
        final Method width, draw, trim;
        FontMethods(Class<?> type) {
            width = findFontWidthMethod(type);
            draw = findFontDrawMethod(type);
            trim = findFontTrimMethod(type);
        }
    }

    private CyloxUiHooks() { }

    private static ButtonAccess buttonAccess(Object button) {
        Class<?> type = button.getClass();
        ButtonAccess access = BUTTON_ACCESS.get(type);
        if (access == null) {
            ButtonAccess created = new ButtonAccess(type);
            ButtonAccess prior = BUTTON_ACCESS.putIfAbsent(type, created);
            access = prior == null ? created : prior;
        }
        return access;
    }

    private static FontOwnerAccess fontOwnerAccess(Object owner) {
        Class<?> type = owner.getClass();
        FontOwnerAccess access = FONT_OWNER_ACCESS.get(type);
        if (access == null) {
            FontOwnerAccess created = new FontOwnerAccess(type);
            FontOwnerAccess prior = FONT_OWNER_ACCESS.putIfAbsent(type, created);
            access = prior == null ? created : prior;
        }
        return access;
    }

    private static FontMethods fontMethods(Object font) {
        Class<?> type = font.getClass();
        FontMethods methods = FONT_METHODS.get(type);
        if (methods == null) {
            FontMethods created = new FontMethods(type);
            FontMethods prior = FONT_METHODS.putIfAbsent(type, created);
            methods = prior == null ? created : prior;
        }
        return methods;
    }

    private static int readInt(Field field, Object target, int fallback) {
        try { return field == null ? fallback : field.getInt(target); }
        catch (Throwable ignored) { return fallback; }
    }

    private static boolean readBoolean(Field field, Object target, boolean fallback) {
        try { return field == null ? fallback : field.getBoolean(target); }
        catch (Throwable ignored) { return fallback; }
    }

    private static String readString(Field field, Object target) {
        try {
            Object value = field == null ? null : field.get(target);
            return value instanceof String ? (String) value : "";
        } catch (Throwable ignored) { return ""; }
    }


    /** Replaces vanilla GuiButton drawing while preserving the original button and its actions. */
    public static boolean drawButton(Object button, Object minecraft, int mouseX, int mouseY) {
        if (button == null) return false;
        try {
            ButtonAccess access = buttonAccess(button);
            int x = readInt(access.x, button, 0);
            int y = readInt(access.y, button, 0);
            int w = readInt(access.width, button, 0);
            int h = readInt(access.height, button, 0);
            if (w <= 0 || h <= 0) return false;
            if (!readBoolean(access.visible, button, true)) {
                if (access.hovered != null) access.hovered.setBoolean(button, false);
                return true;
            }
            boolean enabled = readBoolean(access.enabled, button, true);
            boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + h;
            if (access.hovered != null) access.hovered.setBoolean(button, hovered);

            int fill = !enabled ? 0x80101A25 : hovered ? 0xB81A3545 : 0x98122333;
            int edge = !enabled ? 0x70273541 : hovered ? 0xD036D7E8 : 0xA024465A;
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            rect(x, y, x + w, y + h, fill);
            rect(x, y, x + w, y + 1, edge);
            rect(x, y + h - 1, x + w, y + h, edge);
            rect(x, y, x + 1, y + h, edge);
            rect(x + w - 1, y, x + w, y + h, edge);
            rect(x + 2, y + 2, x + 4, y + h - 2, hovered ? 0xE0FFA45B : 0xC036D7E8);
            GL11.glPopAttrib();

            String label = readString(access.label, button);
            if (label.length() > 0) {
                Object renderer = font(minecraft);
                label = fitText(renderer, label, Math.max(1, w - 8));
                centered(renderer, label, x + w / 2, y + (h - 8) / 2, enabled ? WHITE : MUTED);
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
        rect(left, top, right, bottom, 0x780C1B2A);
        rect(left, top, right, top + 2, 0xC036D7E8);
        rect(left, top, left + 2, bottom, 0xA024465A);
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
        rect(0, 0, w, h, 0x7008121E);
        rect(0, 0, w, 2, 0xA0173448);
        rect(0, 0, 3, h, 0xA01C6272);
        rect(0, h - 2, w, h, 0x90152C3B);
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

    private static void centered(Object renderer, String text, int center, int y, int color) {
        if (renderer == null || text == null || text.length() == 0) return;
        try {
            FontMethods methods = fontMethods(renderer);
            int width = text.length() * 6;
            if (methods.width != null) {
                Object measured = methods.width.invoke(renderer, text);
                if (measured instanceof Integer) width = ((Integer) measured).intValue();
            }
            drawWithFont(renderer, text, center - width / 2.0F, (float) y, color);
        } catch (Throwable ignored) { }
    }

    private static Object font(Object owner) {
        if (owner == null) return null;
        if (isFontRendererClass(owner.getClass())) return owner;
        try {
            Field field = fontOwnerAccess(owner).renderer;
            return field == null ? null : field.get(owner);
        } catch (Throwable ignored) { return null; }
    }

    private static boolean isFontRendererClass(Class<?> candidate) {
        if (candidate == null) return false;
        boolean width = false;
        boolean draw = false;
        for (Class<?> type = candidate; type != null; type = type.getSuperclass()) {
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

    /** Draws a left-aligned Minecraft string using the cached FontRenderer mapping. */
    public static void drawText(Object owner, String text, float x, float y, int color) {
        if (text == null || text.length() == 0) return;
        Object renderer = font(owner);
        if (renderer != null) drawWithFont(renderer, text, x, y, color);
    }

    private static void drawWithFont(Object renderer, String text, float x, float y, int color) {
        if (renderer == null || text == null || text.length() == 0) return;
        try {
            Method draw = fontMethods(renderer).draw;
            if (draw == null) return;
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            try {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                draw.invoke(renderer, text, x, y, color);
            } finally {
                GL11.glPopAttrib();
            }
        } catch (Throwable ignored) { }
    }

    private static String fitText(Object renderer, String text, int maxWidth) {
        if (renderer == null || text == null || text.length() == 0 || maxWidth < 1) return text;
        try {
            Method trim = fontMethods(renderer).trim;
            if (trim == null) return text;
            Object result = trim.invoke(renderer, text, maxWidth);
            return result instanceof String ? (String) result : text;
        } catch (Throwable ignored) { return text; }
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

    private static Method findFontWidthMethod(Class<?> fontType) {
        return findMethod(fontType, new String[] {"getStringWidth", "func_78256_a", "a"},
            new Class<?>[] {String.class}, Integer.TYPE);
    }

    private static Method findFontDrawMethod(Class<?> fontType) {
        return findMethod(fontType, new String[] {"drawStringWithShadow", "func_175063_a", "drawString", "a"},
            new Class<?>[] {String.class, Float.TYPE, Float.TYPE, Integer.TYPE}, null);
    }

    private static Method findFontTrimMethod(Class<?> fontType) {
        return findMethod(fontType, new String[] {"trimStringToWidth", "func_78269_a", "a"},
            new Class<?>[] {String.class, Integer.TYPE}, String.class);
    }

    private static Method findMethod(Class<?> type, String[] names, Class<?>[] parameters,
                                     Class<?> returnType) {
        for (String name : names) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                try {
                    Method method = current.getDeclaredMethod(name, parameters);
                    if (returnType != null && method.getReturnType() != returnType) continue;
                    method.setAccessible(true);
                    return method;
                } catch (Throwable ignored) { }
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
    private static Field findField(Class<?> type, String... names) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (String name : names) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (Throwable ignored) { }
            }
        }
        return null;
    }

    private static Field findFirstField(Class<?> type, Class<?> fieldType) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getType() == fieldType && !Modifier.isStatic(field.getModifiers())) {
                    try { field.setAccessible(true); return field; }
                    catch (Throwable ignored) { }
                }
            }
        }
        return null;
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
