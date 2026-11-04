package dev.cylox.client.minecraft189;

import dev.cylox.client.ui.UiRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

/** Minecraft 1.8.9 rendering adapter backed by the game's own GUI and font classes. */
public final class MinecraftUiRenderer implements UiRenderer {
    private final FontRenderer font;

    public MinecraftUiRenderer(FontRenderer font) {
        if (font == null) {
            throw new IllegalArgumentException("FontRenderer is required");
        }
        this.font = font;
    }

    @Override
    public void fill(float x, float y, float width, float height, int argb) {
        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);
        if (right > left && bottom > top) {
            Gui.drawRect(left, top, right, bottom, argb);
        }
    }

    @Override
    public void outline(float x, float y, float width, float height, int argb) {
        if (width <= 0.0F || height <= 0.0F) {
            return;
        }
        fill(x, y, width, 1.0F, argb);
        fill(x, y + height - 1.0F, width, 1.0F, argb);
        fill(x, y, 1.0F, height, argb);
        fill(x + width - 1.0F, y, 1.0F, height, argb);
    }

    @Override
    public void text(String value, float x, float y, int argb, float scale) {
        if (value == null || value.isEmpty() || scale <= 0.0F) {
            return;
        }
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            font.drawString(value, 0, 0, argb, false);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    @Override
    public float textWidth(String value, float scale) {
        if (value == null || scale <= 0.0F) {
            return 0.0F;
        }
        return font.getStringWidth(value) * scale;
    }
}
