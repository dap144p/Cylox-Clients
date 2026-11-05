package dev.cylox.client.minecraft189;

import dev.cylox.client.runtime.CyloxRuntime;
import dev.cylox.client.ui.ClickGui;
import dev.cylox.client.ui.UiRenderer;
import java.util.Locale;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

/** Lightweight LWJGL overlay renderer. It has no compile-time Minecraft/Forge/Fabric dependency. */
public final class CyloxOverlayRenderer implements UiRenderer {
    private static final CyloxOverlayRenderer INSTANCE = new CyloxOverlayRenderer();
    private static final int[][] FONT = new int[128][];

    static {
        put('A', 14,17,17,31,17,17,17); put('B',30,17,17,30,17,17,30);
        put('C',14,17,16,16,16,17,14); put('D',30,17,17,17,17,17,30);
        put('E',31,16,16,30,16,16,31); put('F',31,16,16,30,16,16,16);
        put('G',14,17,16,23,17,17,15); put('H',17,17,17,31,17,17,17);
        put('I',14,4,4,4,4,4,14); put('J',7,2,2,2,18,18,12);
        put('K',17,18,20,24,20,18,17); put('L',16,16,16,16,16,16,31);
        put('M',17,27,21,21,17,17,17); put('N',17,25,25,21,19,19,17);
        put('O',14,17,17,17,17,17,14); put('P',30,17,17,30,16,16,16);
        put('Q',14,17,17,17,21,18,13); put('R',30,17,17,30,20,18,17);
        put('S',15,16,16,14,1,1,30); put('T',31,4,4,4,4,4,4);
        put('U',17,17,17,17,17,17,14); put('V',17,17,17,17,17,10,4);
        put('W',17,17,17,21,21,21,10); put('X',17,17,10,4,10,17,17);
        put('Y',17,17,10,4,4,4,4); put('Z',31,1,2,4,8,16,31);
        put('0',14,17,19,21,25,17,14); put('1',4,12,4,4,4,4,14);
        put('2',14,17,1,2,4,8,31); put('3',30,1,1,14,1,1,30);
        put('4',2,6,10,18,31,2,2); put('5',31,16,16,30,1,1,30);
        put('6',14,16,16,30,17,17,14); put('7',31,1,2,4,8,8,8);
        put('8',14,17,17,14,17,17,14); put('9',14,17,17,15,1,1,14);
        put('.',0,0,0,0,0,6,6); put(',',0,0,0,0,6,6,4);
        put(':',0,6,6,0,6,6,0); put(';',0,6,6,0,6,6,4);
        put('/',1,2,2,4,8,8,16); put('-',0,0,0,31,0,0,0);
        put('_',0,0,0,0,0,0,31); put('>',16,8,4,2,4,8,16);
        put('<',1,2,4,8,4,2,1); put('(',2,4,8,8,8,4,2);
        put(')',8,4,2,2,2,4,8); put('?',14,17,1,2,4,0,4);
        put('!',4,4,4,4,4,0,4); put('+',0,4,4,31,4,4,0);
        put('=',0,31,0,31,0,0,0); put('[',14,8,8,8,8,8,14);
        put(']',14,2,2,2,2,2,14); put(' ',0,0,0,0,0,0,0);
        put('\'',4,4,2,0,0,0,0); put('"',10,10,0,0,0,0,0);
        put('%',17,2,4,8,17,0,0); put('#',10,31,10,10,31,10,0);
        put('*',0,17,10,31,10,17,0); put('|',4,4,4,4,4,4,4);
    }

    private static void put(char c, int... rows) { FONT[c] = rows; }
    private CyloxOverlayRenderer() {}

    /** Called from the end of EntityRenderer.updateCameraAndRender. */
    public static void render(Object ignored) {
        CyloxRuntime.getHud().onFrame();
        ClickGui gui = CyloxRuntime.getClickGui();
        int width = Display.getWidth();
        int height = Display.getHeight();
        if (width <= 0 || height <= 0) return;

        int previousMatrix = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, width, height, 0.0D, -100.0D, 100.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        try {
            if (gui.isOpen()) {
                int mouseX = Mouse.getX();
                int mouseY = height - Mouse.getY() - 1;
                gui.draw(INSTANCE, width, height, mouseX, mouseY);
            } else {
                CyloxRuntime.getHud().draw(INSTANCE, width, height);
            }
        } catch (Throwable error) {
            System.err.println("[Cylox] Overlay render failed: " + error.getClass().getSimpleName());
            gui.setOpen(false);
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();
            GL11.glMatrixMode(previousMatrix);
            GL11.glPopAttrib();
        }
    }

    @Override
    public void fill(float x, float y, float width, float height, int argb) {
        if (width <= 0.0F || height <= 0.0F) return;
        color(argb);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
    }

    @Override
    public void outline(float x, float y, float width, float height, int argb) {
        if (width <= 0.0F || height <= 0.0F) return;
        color(argb);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
    }

    @Override
    public void text(String value, float x, float y, int argb, float scale) {
        if (value == null || value.isEmpty() || scale <= 0.0F) return;
        String text = value.toUpperCase(Locale.ROOT);
        color(argb);
        GL11.glBegin(GL11.GL_QUADS);
        float cursor = x;
        float pixel = Math.max(0.55F, scale);
        for (int index = 0; index < text.length(); index++) {
            char c = text.charAt(index);
            if (c >= 128 || c == '\u203A') c = '>';
            int[] rows = c < FONT.length ? FONT[c] : null;
            if (rows == null) rows = FONT['?'];
            for (int row = 0; row < 7; row++) {
                int bits = rows[row];
                for (int col = 0; col < 5; col++) {
                    if ((bits & (1 << (4 - col))) == 0) continue;
                    float left = cursor + col * pixel;
                    float top = y + row * pixel;
                    GL11.glVertex2f(left, top);
                    GL11.glVertex2f(left + pixel, top);
                    GL11.glVertex2f(left + pixel, top + pixel);
                    GL11.glVertex2f(left, top + pixel);
                }
            }
            cursor += 6.0F * pixel;
        }
        GL11.glEnd();
    }

    @Override
    public float textWidth(String value, float scale) {
        if (value == null || scale <= 0.0F) return 0.0F;
        return value.length() * 6.0F * Math.max(0.55F, scale);
    }

    private static void color(int argb) {
        float alpha = ((argb >>> 24) & 255) / 255.0F;
        float red = ((argb >>> 16) & 255) / 255.0F;
        float green = ((argb >>> 8) & 255) / 255.0F;
        float blue = (argb & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
}
