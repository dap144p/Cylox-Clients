package dev.cylox.client.minecraft189;

import dev.cylox.client.runtime.CyloxRuntime;
import dev.cylox.client.ui.ClickGui;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/**
 * Native Minecraft 1.8.9 screen wrapper for the shared Cylox Click GUI.
 * A launcher hook or client entry point must call open() to display this screen.
 */
public final class CyloxClickGuiScreen extends GuiScreen {
    private final ClickGui clickGui;

    public CyloxClickGuiScreen() {
        this.clickGui = CyloxRuntime.getClickGui();
        this.clickGui.setOpen(true);
    }

    public static void open() {
        open(Minecraft.getMinecraft());
    }

    public static void open(Minecraft minecraft) {
        if (minecraft == null) {
            return;
        }
        CyloxRuntime.getClickGui().setOpen(true);
        minecraft.displayGuiScreen(new CyloxClickGuiScreen());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (!clickGui.isOpen()) {
            mc.displayGuiScreen(null);
            return;
        }
        clickGui.draw(new MinecraftUiRenderer(fontRendererObj), width, height, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        clickGui.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        clickGui.mouseDragged(mouseX, mouseY, clickedMouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        clickGui.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        clickGui.keyTyped(typedChar, keyCode);
        if (!clickGui.isOpen()) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void onGuiClosed() {
        clickGui.setOpen(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
