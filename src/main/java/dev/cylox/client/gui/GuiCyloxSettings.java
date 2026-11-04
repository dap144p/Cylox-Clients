package dev.cylox.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

public class GuiCyloxSettings extends GuiScreen {
    private final GuiScreen parent;
    public GuiCyloxSettings(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 - 10, 200, 20, "Client settings - coming soon"));
        buttonList.add(new GuiButton(1, width / 2 - 50, height / 2 + 20, 100, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) mc.displayGuiScreen(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, height, CyloxPalette.BLACK);
        drawRect(0, 0, width, 3, CyloxPalette.ORANGE);
        drawCenteredString(fontRendererObj, "CYLOX SETTINGS", width / 2, height / 2 - 55, CyloxPalette.TEXT);
        drawCenteredString(fontRendererObj, "Central configuration panel", width / 2, height / 2 - 38, CyloxPalette.MUTED);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
