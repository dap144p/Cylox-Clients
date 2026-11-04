package dev.cylox.client.gui;

import dev.cylox.client.module.ClientModule;
import dev.cylox.client.module.ModuleManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
import java.util.List;

public class GuiCyloxModules extends GuiScreen {
    private final GuiScreen parent;
    private List<ClientModule> modules;
    public GuiCyloxModules(GuiScreen parent) { this.parent = parent; }

    @Override
    public void initGui() {
        modules = ModuleManager.getModules();
        buttonList.clear();
        int w = Math.min(260, width - 30);
        int x = width / 2 - w / 2;
        int y = 45;
        for (int i = 0; i < modules.size(); i++) {
            ClientModule module = modules.get(i);
            buttonList.add(new GuiButton(i, x, y + i * 24, w, 20, module.getName() + "  [" + (module.isEnabled() ? "ON" : "OFF") + "]"));
        }
        buttonList.add(new GuiButton(100, width / 2 - 50, height - 28, 100, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 100) { mc.displayGuiScreen(parent); return; }
        if (button.id >= 0 && button.id < modules.size()) {
            modules.get(button.id).toggle();
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, height, CyloxPalette.BLACK);
        drawRect(0, 0, width, 3, CyloxPalette.BLUE);
        drawCenteredString(fontRendererObj, "CLIENT MODULES", width / 2, 18, CyloxPalette.TEXT);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
