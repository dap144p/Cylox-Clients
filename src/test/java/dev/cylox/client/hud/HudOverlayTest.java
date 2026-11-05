package dev.cylox.client.hud;

import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.ui.UiRenderer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class HudOverlayTest {
    @Test
    public void drawsWatermarkFpsAndLiveKeystrokes() {
        ModuleRegistry registry = ModuleRegistry.createBuiltIns();
        HudOverlay hud = new HudOverlay(registry);
        hud.setInput(true, false, false, true, true, true, false);
        RecordingRenderer renderer = new RecordingRenderer();

        hud.draw(renderer, 854, 480);

        assertTrue(renderer.texts.contains("CYLOX"));
        assertTrue(renderer.texts.contains("FPS: 0"));
        assertTrue(renderer.texts.contains("W"));
        assertTrue(renderer.texts.contains("D"));
        assertTrue(renderer.texts.contains("SPACE"));
        assertTrue(renderer.texts.contains("LMB"));
        assertTrue(renderer.fills >= 10);
    }

    @Test
    public void customCrosshairIsOptionalAndConfigurable() {
        ModuleRegistry registry = ModuleRegistry.createBuiltIns();
        HudOverlay hud = new HudOverlay(registry);
        RecordingRenderer renderer = new RecordingRenderer();

        hud.draw(renderer, 854, 480);
        int defaultFills = renderer.fills;
        registry.getModule("Crosshair").setEnabled(true);
        hud.draw(renderer, 854, 480);

        assertEquals(defaultFills + 4, renderer.fills);
    }

    private static final class RecordingRenderer implements UiRenderer {
        private final List<String> texts = new ArrayList<String>();
        private int fills;

        @Override public void fill(float x, float y, float width, float height, int argb) { fills++; }
        @Override public void outline(float x, float y, float width, float height, int argb) { }
        @Override public void text(String value, float x, float y, int argb, float scale) { texts.add(value); }
        @Override public float textWidth(String value, float scale) { return value.length() * 6.0F * scale; }
    }
}
