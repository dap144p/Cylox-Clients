package dev.cylox.client.hud;

import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.setting.BooleanSetting;
import dev.cylox.client.setting.NumberSetting;
import dev.cylox.client.setting.Setting;
import dev.cylox.client.ui.UiRenderer;

/** Small, game-independent HUD. Input and frame timing are supplied by the LWJGL adapter. */
public final class HudOverlay {
    private final ModuleRegistry modules;
    private long fpsWindowStart = System.nanoTime();
    private int framesInWindow;
    private int fps;
    private boolean keyW;
    private boolean keyA;
    private boolean keyS;
    private boolean keyD;
    private boolean keySpace;
    private boolean mouseLeft;
    private boolean mouseRight;

    public HudOverlay(ModuleRegistry modules) {
        if (modules == null) throw new IllegalArgumentException("ModuleRegistry is required");
        this.modules = modules;
    }

    public void setInput(boolean w, boolean a, boolean s, boolean d, boolean space,
                         boolean left, boolean right) {
        keyW = w;
        keyA = a;
        keyS = s;
        keyD = d;
        keySpace = space;
        mouseLeft = left;
        mouseRight = right;
    }

    /** Called once for every game render frame. */
    public void onFrame() {
        long now = System.nanoTime();
        framesInWindow++;
        long elapsed = now - fpsWindowStart;
        if (elapsed >= 1000000000L) {
            fps = Math.round(framesInWindow * 1000000000.0F / elapsed);
            framesInWindow = 0;
            fpsWindowStart = now;
        }
    }

    public int getFps() {
        return fps;
    }

    public void draw(UiRenderer renderer, int screenWidth, int screenHeight) {
        if (renderer == null || screenWidth <= 0 || screenHeight <= 0) return;

        if (enabled("Watermark")) {
            renderer.fill(8.0F, 8.0F, 116.0F, 20.0F, 0xB9161D29);
            renderer.fill(8.0F, 8.0F, 3.0F, 20.0F, 0xFF3288FF);
            renderer.text("CYLOX", 17.0F, 12.0F, 0xFFFFFFFF, 0.92F);
            renderer.text("1.8.9", 71.0F, 13.0F, 0xFF9FB0C7, 0.68F);
        }

        if (enabled("FPS")) {
            String label = "FPS: " + fps;
            boolean background = booleanSetting("FPS", "Show Background", true);
            if (background) {
                renderer.fill(8.0F, 32.0F, renderer.textWidth(label, 0.78F) + 10.0F, 17.0F, 0xB9161D29);
            }
            renderer.text(label, 13.0F, 37.0F, 0xFFFFFFFF, 0.78F);
        }

        if (enabled("Keystrokes")) {
            drawKeystrokes(renderer, screenHeight);
        }

        if (enabled("Crosshair")) {
            drawCrosshair(renderer, screenWidth, screenHeight);
        }
    }

    private void drawKeystrokes(UiRenderer renderer, int screenHeight) {
        float x = 12.0F;
        float y = Math.max(12.0F, screenHeight - 92.0F);
        drawKey(renderer, "W", x + 24.0F, y, keyW);
        drawKey(renderer, "A", x, y + 24.0F, keyA);
        drawKey(renderer, "S", x + 24.0F, y + 24.0F, keyS);
        drawKey(renderer, "D", x + 48.0F, y + 24.0F, keyD);
        drawKey(renderer, "SPACE", x, y + 48.0F, keySpace, 72.0F);
        if (booleanSetting("Keystrokes", "Show Mouse Buttons", true)) {
            drawKey(renderer, "LMB", x + 78.0F, y, mouseLeft, 34.0F);
            drawKey(renderer, "RMB", x + 78.0F, y + 24.0F, mouseRight, 34.0F);
        }
    }

    private void drawKey(UiRenderer renderer, String label, float x, float y, boolean pressed) {
        drawKey(renderer, label, x, y, pressed, 21.0F);
    }

    private void drawKey(UiRenderer renderer, String label, float x, float y, boolean pressed, float width) {
        renderer.fill(x, y, width, 21.0F, pressed ? 0xE93388FF : 0xB9161D29);
        renderer.outline(x, y, width, 21.0F, pressed ? 0xFF71B1FF : 0xFF39465A);
        float textWidth = renderer.textWidth(label, 0.62F);
        renderer.text(label, x + Math.max(3.0F, (width - textWidth) / 2.0F), y + 7.0F,
            0xFFFFFFFF, 0.62F);
    }

    private void drawCrosshair(UiRenderer renderer, int screenWidth, int screenHeight) {
        float size = (float) numberSetting("Crosshair", "Size", 8.0D);
        float gap = (float) numberSetting("Crosshair", "Gap", 3.0D);
        float centerX = screenWidth / 2.0F;
        float centerY = screenHeight / 2.0F;
        renderer.fill(centerX - 1.0F, centerY - gap - size, 2.0F, size, 0xEFFFFFFF);
        renderer.fill(centerX - 1.0F, centerY + gap, 2.0F, size, 0xEFFFFFFF);
        renderer.fill(centerX - gap - size, centerY - 1.0F, size, 2.0F, 0xEFFFFFFF);
        renderer.fill(centerX + gap, centerY - 1.0F, size, 2.0F, 0xEFFFFFFF);
    }

    private boolean enabled(String name) {
        CyloxModule module = modules.getModule(name);
        return module != null && module.isEnabled();
    }

    private boolean booleanSetting(String moduleName, String settingName, boolean fallback) {
        CyloxModule module = modules.getModule(moduleName);
        if (module == null) return fallback;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting && setting.getName().equalsIgnoreCase(settingName)) {
                return ((BooleanSetting) setting).getValue();
            }
        }
        return fallback;
    }

    private double numberSetting(String moduleName, String settingName, double fallback) {
        CyloxModule module = modules.getModule(moduleName);
        if (module == null) return fallback;
        for (Setting setting : module.getSettings()) {
            if (setting instanceof NumberSetting && setting.getName().equalsIgnoreCase(settingName)) {
                return ((NumberSetting) setting).getValue();
            }
        }
        return fallback;
    }
}
