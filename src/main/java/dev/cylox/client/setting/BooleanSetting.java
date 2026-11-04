package dev.cylox.client.setting;

public final class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void toggle() {
        value = !value;
    }

    @Override
    public String getDisplayValue() {
        return value ? "ON" : "OFF";
    }
}
