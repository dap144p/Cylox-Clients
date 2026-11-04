package dev.cylox.client.setting;

/** Base type for a setting displayed by the Cylox Click GUI. */
public abstract class Setting {
    private final String name;

    protected Setting(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Setting name cannot be empty");
        }
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public abstract String getDisplayValue();
}
