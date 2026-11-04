package dev.cylox.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import java.io.IOException;

public class GuiCyloxMainMenu extends GuiScreen {
    @Override
    public void initGui() {
        buttonList.clear();
        int w = 150;
        int x = width / 2 - w / 2;
        int y = height / 2 - 8;
        buttonList.add(new GuiButton(0, x, y, w, 20, "Singleplayer"));
        buttonList.add(new GuiButton(1, x, y + 25, w, 20, "Multiplayer"));
        buttonList.add(new GuiButton(2, x, y + 50, w, 20, "Modules"));
        buttonList.add(new GuiButton(3, x, y + 75, w, 20, "Settings"));
        buttonList.add(new GuiButton(4, x, y + 100, w, 20, "Vanilla Menu"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) mc.displayGuiScreen(new GuiSelectWorld(this));
        if (button.id == 1) mc.displayGuiScreen(new GuiMultiplayer(this));
        if (button.id == 2) mc.displayGuiScreen(new GuiCyloxModules(this));
        if (button.id == 3) mc.displayGuiScreen(new GuiCyloxSettings(this));
        if (button.id == 4) mc.displayGuiScreen(new GuiMainMenu());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, height, CyloxPalette.BLACK);
        drawRect(0, 0, width, 3, CyloxPalette.GREEN);
        drawCenteredString(fontRendererObj, "Cylox", width / 2, height / 2 - 92, CyloxPalette.TEXT);
        drawCenteredString(fontRendererObj, "CLIENT  /  FORGE 1.8.9", width / 2, height / 2 - 75, CyloxPalette.BLUE);
        drawCenteredString(fontRendererObj, "PvP  /  AFK  /  ALT", width / 2, height / 2 - 59, CyloxPalette.MUTED);
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawString(fontRendererObj, "CYLOX CLIENT  v0.1.0-alpha", 8, height - 14, CyloxPalette.MUTED);
        drawString(fontRendererObj, "GREEN / BLUE / ORANGE", width - 125, height - 14, CyloxPalette.ORANGE);
    }
}
