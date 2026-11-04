package dev.cylox.client.ui;

import dev.cylox.client.module.Category;
import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.setting.BooleanSetting;
import dev.cylox.client.setting.ModeSetting;
import dev.cylox.client.setting.NumberSetting;
import dev.cylox.client.setting.Setting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Three-column Hybrid Click GUI. Input events are supplied by the host game adapter. */
public final class ClickGui {
    private static final int BG = 0xF0141822;
    private static final int PANEL = 0xE91B2230;
    private static final int PANEL_DARK = 0xEE151B26;
    private static final int BORDER = 0xFF303B4E;
    private static final int BLUE = 0xFF3288FF;
    private static final int TEXT = 0xFFF1F5FC;
    private static final int MUTED = 0xFF8F9BAD;
    private static final int GREEN = 0xFF54D49A;

    private final ModuleRegistry registry;
    private final NumberSetting uiScale = new NumberSetting("Window Scale", 1.0D, 0.75D, 1.0D, 0.05D);
    private final NumberSetting opacity = new NumberSetting("Panel Opacity", 90.0D, 50.0D, 100.0D, 5.0D);
    private final BooleanSetting compactMode = new BooleanSetting("Compact Rows", false);
    private Category selectedCategory = Category.COMBAT;
    private CyloxModule selectedModule;
    private String search = "";
    private boolean searchFocused;
    private boolean waitingForKey;
    private boolean guiSettingsSelected;
    private boolean dragging;
    private boolean open = true;
    private float x = -1.0F;
    private float y = -1.0F;
    private float dragOffsetX;
    private float dragOffsetY;
    private float width;
    private float height;
    private float sidebarWidth;
    private float listWidth;
    private float headerHeight = 38.0F;
    private float rowHeight = 30.0F;
    private float settingTop;
    private final List<CyloxModule> visibleModules = new ArrayList<CyloxModule>();

    public ClickGui(ModuleRegistry registry) {
        if (registry == null) throw new IllegalArgumentException("ModuleRegistry is required");
        this.registry = registry;
    }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public Category getSelectedCategory() { return selectedCategory; }
    public CyloxModule getSelectedModule() { return selectedModule; }
    public String getSearch() { return search; }

    public void draw(UiRenderer renderer, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (!open || renderer == null || screenWidth <= 0 || screenHeight <= 0) return;
        float uiScaleFactor = (float) uiScale.getValue();
        width = Math.min(820.0F, Math.max(300.0F, (screenWidth - 20.0F) * uiScaleFactor));
        height = Math.min(470.0F, Math.max(250.0F, (screenHeight - 20.0F) * uiScaleFactor));
        rowHeight = compactMode.getValue() ? 26.0F : 30.0F;
        if (x < 0.0F || y < 0.0F) {
            x = (screenWidth - width) / 2.0F;
            y = (screenHeight - height) / 2.0F;
        }
        x = clamp(x, 4.0F, Math.max(4.0F, screenWidth - width - 4.0F));
        y = clamp(y, 4.0F, Math.max(4.0F, screenHeight - height - 4.0F));
        sidebarWidth = Math.max(72.0F, Math.min(142.0F, width * 0.19F));
        listWidth = Math.max(104.0F, Math.min(270.0F, width * 0.34F));

        renderer.fill(x, y, width, height, withOpacity(BG));
        renderer.outline(x, y, width, height, BORDER);
        renderer.fill(x, y, width, headerHeight, withOpacity(PANEL_DARK));
        renderer.text("CYLOX", x + 14.0F, y + 12.0F, TEXT, 1.15F);
        renderer.text("CLIENT  /  1.8.9", x + 68.0F, y + 14.0F, MUTED, 0.72F);
        renderer.text(guiSettingsSelected ? "BACK TO GUI" : "GUI SETTINGS", x + width - 126.0F, y + 14.0F, BLUE, 0.72F);
        renderer.fill(x, y + headerHeight, sidebarWidth, height - headerHeight, withOpacity(PANEL_DARK));
        renderer.fill(x + sidebarWidth, y + headerHeight, listWidth, height - headerHeight, withOpacity(PANEL));
        renderer.fill(x + sidebarWidth + listWidth, y + headerHeight, width - sidebarWidth - listWidth, height - headerHeight, withOpacity(PANEL_DARK));
        renderer.fill(x + sidebarWidth, y + headerHeight, 1.0F, height - headerHeight, BORDER);
        renderer.fill(x + sidebarWidth + listWidth, y + headerHeight, 1.0F, height - headerHeight, BORDER);

        drawCategories(renderer);
        drawModuleList(renderer);
        drawSettings(renderer, mouseX, mouseY);
    }

    private void drawCategories(UiRenderer renderer) {
        renderer.text("CATEGORIES", x + 11.0F, y + headerHeight + 13.0F, MUTED, 0.68F);
        float rowY = y + headerHeight + 31.0F;
        for (Category category : Category.values()) {
            boolean selected = category == selectedCategory;
            if (selected) renderer.fill(x + 7.0F, rowY - 5.0F, sidebarWidth - 14.0F, 25.0F, 0x663288FF);
            renderer.fill(x + 7.0F, rowY + 2.0F, 3.0F, 11.0F, selected ? BLUE : 0xFF48566B);
            renderer.text(category.getDisplayName(), x + 17.0F, rowY + 3.0F, selected ? TEXT : MUTED, 0.86F);
            rowY += 31.0F;
        }
    }

    private void drawModuleList(UiRenderer renderer) {
        float panelX = x + sidebarWidth;
        float panelY = y + headerHeight;
        renderer.text("MODULES", panelX + 12.0F, panelY + 12.0F, TEXT, 0.88F);
        float searchY = panelY + 29.0F;
        renderer.fill(panelX + 9.0F, searchY, listWidth - 18.0F, 25.0F, withOpacity(PANEL_DARK));
        renderer.outline(panelX + 9.0F, searchY, listWidth - 18.0F, 25.0F, searchFocused ? BLUE : BORDER);
        String searchText = search.isEmpty() ? "Search modules..." : search;
        renderer.text(searchText, panelX + 16.0F, searchY + 8.0F, search.isEmpty() ? MUTED : TEXT, 0.76F);

        visibleModules.clear();
        for (CyloxModule module : registry.getModules(selectedCategory)) {
            if (module.getName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                visibleModules.add(module);
            }
        }
        float rowY = searchY + 34.0F;
        for (CyloxModule module : visibleModules) {
            if (rowY + rowHeight > y + height - 8.0F) break;
            boolean selected = module == selectedModule;
            if (selected) renderer.fill(panelX + 8.0F, rowY, listWidth - 16.0F, rowHeight - 3.0F, 0x663288FF);
            renderer.text(module.getName(), panelX + 14.0F, rowY + 6.0F, selected ? TEXT : 0xFFD7DFEB, 0.82F);
            float toggleX = panelX + listWidth - 42.0F;
            renderer.fill(toggleX, rowY + 7.0F, 27.0F, 13.0F, module.isEnabled() ? 0xFF245C50 : 0xFF364153);
            renderer.fill(toggleX + (module.isEnabled() ? 15.0F : 2.0F), rowY + 9.0F, 9.0F, 9.0F, module.isEnabled() ? GREEN : 0xFF9AA6B7);
            rowY += rowHeight;
        }
        if (visibleModules.isEmpty()) renderer.text("No modules found", panelX + 13.0F, rowY + 8.0F, MUTED, 0.78F);
    }

    private void drawSettings(UiRenderer renderer, int mouseX, int mouseY) {
        float panelX = x + sidebarWidth + listWidth;
        float panelY = y + headerHeight;
        renderer.text(guiSettingsSelected ? "GUI SETTINGS" : "MODULE SETTINGS", panelX + 13.0F, panelY + 12.0F, MUTED, 0.68F);
        settingTop = panelY + 35.0F;
        if (guiSettingsSelected) {
            drawGuiPreferences(renderer, panelX);
            return;
        }
        if (selectedModule == null || selectedModule.getCategory() != selectedCategory) {
            renderer.text("Select a module", panelX + 13.0F, settingTop + 8.0F, TEXT, 0.94F);
            renderer.text("Its settings will appear here.", panelX + 13.0F, settingTop + 27.0F, MUTED, 0.72F);
            return;
        }
        renderer.text(selectedModule.getName(), panelX + 13.0F, settingTop, TEXT, 1.0F);
        renderer.text(selectedModule.getDescription(), panelX + 13.0F, settingTop + 18.0F, MUTED, 0.68F);
        float bindY = settingTop + 43.0F;
        renderer.text("KEYBIND", panelX + 13.0F, bindY + 4.0F, MUTED, 0.68F);
        float bindW = 66.0F;
        renderer.fill(panelX + width - sidebarWidth - listWidth - bindW - 14.0F, bindY - 2.0F, bindW, 22.0F, waitingForKey ? 0x663288FF : PANEL);
        renderer.outline(panelX + width - sidebarWidth - listWidth - bindW - 14.0F, bindY - 2.0F, bindW, 22.0F, waitingForKey ? BLUE : BORDER);
        renderer.text(waitingForKey ? "Press key" : keyName(selectedModule.getKeyCode()), panelX + width - sidebarWidth - listWidth - bindW - 8.0F, bindY + 5.0F, TEXT, 0.72F);

        float rowY = bindY + 39.0F;
        for (Setting setting : selectedModule.getSettings()) {
            if (rowY + 34.0F > y + height - 8.0F) break;
            renderer.text(setting.getName(), panelX + 13.0F, rowY + 2.0F, 0xFFDCE4F0, 0.78F);
            float controlX = panelX + 13.0F;
            float controlW = Math.max(72.0F, width - sidebarWidth - listWidth - 28.0F);
            if (setting instanceof BooleanSetting) {
                BooleanSetting bool = (BooleanSetting) setting;
                renderer.fill(controlX, rowY + 17.0F, controlW, 19.0F, withOpacity(PANEL));
                renderer.text(bool.getDisplayValue(), controlX + 7.0F, rowY + 23.0F, bool.getValue() ? GREEN : MUTED, 0.72F);
            } else if (setting instanceof NumberSetting) {
                NumberSetting number = (NumberSetting) setting;
                renderer.text(number.getDisplayValue(), controlX + controlW - 42.0F, rowY + 2.0F, BLUE, 0.72F);
                renderer.fill(controlX, rowY + 22.0F, controlW, 5.0F, 0xFF303B4E);
                renderer.fill(controlX, rowY + 22.0F, controlW * number.getRatio(), 5.0F, BLUE);
                renderer.fill(controlX + controlW * number.getRatio() - 3.0F, rowY + 18.0F, 7.0F, 13.0F, TEXT);
            } else if (setting instanceof ModeSetting) {
                ModeSetting mode = (ModeSetting) setting;
                renderer.fill(controlX, rowY + 17.0F, controlW, 19.0F, withOpacity(PANEL));
                renderer.text(mode.getDisplayValue() + "  ›", controlX + 7.0F, rowY + 23.0F, BLUE, 0.72F);
            }
            rowY += 45.0F;
        }
    }

    private void drawGuiPreferences(UiRenderer renderer, float panelX) {
        renderer.text("INTERFACE", panelX + 13.0F, settingTop, TEXT, 0.92F);
        float controlX = panelX + 13.0F;
        float controlW = Math.max(72.0F, width - sidebarWidth - listWidth - 28.0F);
        drawPreferenceSlider(renderer, "Window Scale", uiScale, controlX, settingTop + 22.0F, controlW);
        drawPreferenceSlider(renderer, "Panel Opacity", opacity, controlX, settingTop + 82.0F, controlW);
        renderer.text("Compact Rows", controlX, settingTop + 166.0F, 0xFFDCE4F0, 0.78F);
        renderer.fill(controlX, settingTop + 181.0F, controlW, 21.0F, withOpacity(PANEL));
        renderer.text(compactMode.getDisplayValue(), controlX + 7.0F, settingTop + 188.0F,
            compactMode.getValue() ? GREEN : MUTED, 0.72F);
        renderer.text("ESC closes the Click GUI", controlX, settingTop + 224.0F, MUTED, 0.68F);
    }

    private void drawPreferenceSlider(UiRenderer renderer, String label, NumberSetting setting,
                                      float controlX, float rowY, float controlW) {
        renderer.text(label, controlX, rowY, 0xFFDCE4F0, 0.78F);
        renderer.text(setting.getDisplayValue(), controlX + controlW - 42.0F, rowY, BLUE, 0.72F);
        renderer.fill(controlX, rowY + 20.0F, controlW, 5.0F, 0xFF303B4E);
        renderer.fill(controlX, rowY + 20.0F, controlW * setting.getRatio(), 5.0F, BLUE);
        renderer.fill(controlX + controlW * setting.getRatio() - 3.0F, rowY + 16.0F, 7.0F, 13.0F, TEXT);
    }

    private int withOpacity(int color) {
        int baseAlpha = (color >>> 24) & 0xFF;
        int adjustedAlpha = Math.round(baseAlpha * (float) opacity.getValue() / 100.0F);
        return (color & 0x00FFFFFF) | (adjustedAlpha << 24);
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (!open || button != 0) return;
        float panelX = x + sidebarWidth;
        float panelY = y + headerHeight;
        if (inside(mouseX, mouseY, x + width - 134.0F, y + 5.0F, 128.0F, 28.0F)) {
            guiSettingsSelected = !guiSettingsSelected;
            searchFocused = false;
            waitingForKey = false;
            return;
        }
        if (inside(mouseX, mouseY, x, y, width - 138.0F, headerHeight)) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
            return;
        }
        if (inside(mouseX, mouseY, x + 5.0F, y + headerHeight + 25.0F, sidebarWidth - 10.0F, Category.values().length * 31.0F)) {
            int index = (int) ((mouseY - (y + headerHeight + 26.0F)) / 31.0F);
            if (index >= 0 && index < Category.values().length) {
                selectedCategory = Category.values()[index];
                selectedModule = null;
                guiSettingsSelected = false;
                waitingForKey = false;
            }
            return;
        }
        float searchY = panelY + 29.0F;
        if (inside(mouseX, mouseY, panelX + 9.0F, searchY, listWidth - 18.0F, 25.0F)) {
            searchFocused = true;
            return;
        }
        searchFocused = false;
        float rowY = searchY + 34.0F;
        for (CyloxModule module : visibleModules) {
            if (inside(mouseX, mouseY, panelX + 8.0F, rowY, listWidth - 16.0F, rowHeight - 3.0F)) {
                guiSettingsSelected = false;
                if (mouseX >= panelX + listWidth - 48.0F) module.toggle();
                else selectedModule = module;
                return;
            }
            rowY += rowHeight;
        }
        if (selectedModule != null) handleSettingClick(mouseX, mouseY);
    }

    private void handleSettingClick(int mouseX, int mouseY) {
        float panelX = x + sidebarWidth + listWidth;
        if (guiSettingsSelected) {
            float controlX = panelX + 13.0F;
            float controlW = Math.max(72.0F, width - sidebarWidth - listWidth - 28.0F);
            float scaleY = settingTop + 43.0F;
            float opacityY = settingTop + 103.0F;
            float compactY = settingTop + 160.0F;
            if (inside(mouseX, mouseY, controlX, scaleY, controlW, 16.0F)) {
                uiScale.setFromRatio((mouseX - controlX) / controlW);
            } else if (inside(mouseX, mouseY, controlX, opacityY, controlW, 16.0F)) {
                opacity.setFromRatio((mouseX - controlX) / controlW);
            } else if (inside(mouseX, mouseY, controlX, compactY, controlW, 24.0F)) {
                compactMode.toggle();
            }
            return;
        }
        float panelWidth = width - sidebarWidth - listWidth;
        float bindY = settingTop + 43.0F;
        float bindX = panelX + panelWidth - 80.0F;
        if (inside(mouseX, mouseY, bindX, bindY - 2.0F, 66.0F, 22.0F)) {
            waitingForKey = true;
            return;
        }
        float rowY = bindY + 39.0F;
        float controlW = Math.max(72.0F, panelWidth - 28.0F);
        for (Setting setting : selectedModule.getSettings()) {
            if (setting instanceof BooleanSetting && inside(mouseX, mouseY, panelX + 13.0F, rowY + 14.0F, controlW, 23.0F)) {
                ((BooleanSetting) setting).toggle();
                return;
            }
            if (setting instanceof ModeSetting && inside(mouseX, mouseY, panelX + 13.0F, rowY + 14.0F, controlW, 23.0F)) {
                ((ModeSetting) setting).cycle();
                return;
            }
            if (setting instanceof NumberSetting && inside(mouseX, mouseY, panelX + 13.0F, rowY + 14.0F, controlW, 18.0F)) {
                ((NumberSetting) setting).setFromRatio((mouseX - (panelX + 13.0F)) / controlW);
                return;
            }
            rowY += 45.0F;
        }
    }

    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (button == 0 && dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0) dragging = false;
    }

    /** Key codes use LWJGL 2 values supplied by the game adapter. */
    public void keyTyped(char typedChar, int keyCode) {
        if (!open) return;
        if (waitingForKey && selectedModule != null) {
            if (keyCode == 1) waitingForKey = false;
            else {
                selectedModule.setKeyCode(keyCode);
                waitingForKey = false;
            }
            return;
        }
        if (keyCode == 1) {
            open = false;
            return;
        }
        if (!searchFocused) return;
        if (keyCode == 14) {
            if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
        } else if (!Character.isISOControl(typedChar) && search.length() < 32) {
            search += typedChar;
        }
    }

    private static boolean inside(float px, float py, float x, float y, float w, float h) {
        return px >= x && py >= y && px <= x + w && py <= y + h;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String keyName(int keyCode) {
        return keyCode == 0 ? "NONE" : "KEY " + keyCode;
    }
}
