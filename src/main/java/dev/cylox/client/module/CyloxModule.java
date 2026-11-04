package dev.cylox.client.module;

import dev.cylox.client.setting.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CyloxModule {
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting> settings = new ArrayList<Setting>();
    private boolean enabled;
    private int keyCode;

    public CyloxModule(String name, String description, Category category) {
        if (name == null || name.trim().isEmpty() || category == null) {
            throw new IllegalArgumentException("Module name and category are required");
        }
        this.name = name;
        this.description = description == null ? "" : description;
        this.category = category;
    }

    public CyloxModule addSetting(Setting setting) {
        if (setting != null) settings.add(setting);
        return this;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void toggle() { enabled = !enabled; }
    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = Math.max(0, keyCode); }
    public List<Setting> getSettings() { return Collections.unmodifiableList(settings); }
}
