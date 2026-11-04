package dev.cylox.client.setting;

import java.util.Locale;

public final class NumberSetting extends Setting {
    private final double minimum;
    private final double maximum;
    private final double step;
    private double value;

    public NumberSetting(String name, double value, double minimum, double maximum, double step) {
        super(name);
        if (minimum > maximum || step <= 0.0D) {
            throw new IllegalArgumentException("Invalid number setting range or step");
        }
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
        setValue(value);
    }

    public double getValue() {
        return value;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setValue(double value) {
        double clamped = Math.max(minimum, Math.min(maximum, value));
        double steps = Math.round((clamped - minimum) / step);
        this.value = Math.max(minimum, Math.min(maximum, minimum + steps * step));
    }

    public void setFromRatio(float ratio) {
        float safeRatio = Math.max(0.0F, Math.min(1.0F, ratio));
        setValue(minimum + (maximum - minimum) * safeRatio);
    }

    public float getRatio() {
        if (maximum == minimum) return 0.0F;
        return (float) ((value - minimum) / (maximum - minimum));
    }

    @Override
    public String getDisplayValue() {
        return String.format(Locale.US, "%.2f", value);
    }
}
