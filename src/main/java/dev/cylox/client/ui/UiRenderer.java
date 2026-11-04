package dev.cylox.client.ui;

/** Rendering bridge implemented by the Minecraft 1.8.9 game-side adapter. */
public interface UiRenderer {
    void fill(float x, float y, float width, float height, int argb);
    void outline(float x, float y, float width, float height, int argb);
    void text(String value, float x, float y, int argb, float scale);
    float textWidth(String value, float scale);
}
