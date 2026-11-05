package dev.cylox.client.setting;

import java.util.Arrays;

public final class ModeSetting extends Setting {
    private final String[] modes;
    private int selectedIndex;

    public ModeSetting(String name, String... modes) {
        super(name);
        if (modes == null || modes.length == 0) {
            throw new IllegalArgumentException("At least one mode is required");
        }
        this.modes = Arrays.copyOf(modes, modes.length);
        this.selectedIndex = 0;
    }

    public String getValue() {
        return modes[selectedIndex];
    }

    public String[] getModes() {
        return Arrays.copyOf(modes, modes.length);
    }

    public boolean setValue(String value) {
        if (value == null) return false;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(value)) {
                selectedIndex = i;
                return true;
            }
        }
        return false;
    }

    public void cycle() {
        selectedIndex = (selectedIndex + 1) % modes.length;
    }

    @Override
    public String getDisplayValue() {
        return getValue();
    }
}
