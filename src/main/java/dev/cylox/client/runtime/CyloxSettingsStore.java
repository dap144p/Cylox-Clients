package dev.cylox.client.runtime;

import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.setting.BooleanSetting;
import dev.cylox.client.setting.ModeSetting;
import dev.cylox.client.setting.NumberSetting;
import dev.cylox.client.setting.Setting;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/** Persists module state, keybinds and setting values in the active game directory. */
public final class CyloxSettingsStore {
    private final File file;

    public CyloxSettingsStore(File file) {
        if (file == null) throw new IllegalArgumentException("Settings file is required");
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void load(ModuleRegistry registry) throws IOException {
        if (registry == null) throw new IllegalArgumentException("ModuleRegistry is required");
        if (!file.isFile()) return;

        Properties properties = new Properties();
        FileInputStream input = new FileInputStream(file);
        try {
            properties.load(input);
        } finally {
            input.close();
        }

        for (CyloxModule module : registry.getModules()) {
            String prefix = modulePrefix(module);
            String enabled = properties.getProperty(prefix + ".enabled");
            if ("true".equalsIgnoreCase(enabled) || "false".equalsIgnoreCase(enabled)) {
                module.setEnabled(Boolean.parseBoolean(enabled));
            }

            String keyCode = properties.getProperty(prefix + ".keybind");
            if (keyCode != null) {
                try {
                    int parsed = Integer.parseInt(keyCode.trim());
                    if (parsed >= 0 && parsed < 256) module.setKeyCode(parsed);
                } catch (NumberFormatException ignored) {
                    // Keep the module's default keybind when the file contains an invalid value.
                }
            }

            for (Setting setting : module.getSettings()) {
                String value = properties.getProperty(settingKey(module, setting));
                if (value == null) continue;
                try {
                    if (setting instanceof BooleanSetting) {
                        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                            ((BooleanSetting) setting).setValue(Boolean.parseBoolean(value));
                        }
                    } else if (setting instanceof NumberSetting) {
                        ((NumberSetting) setting).setValue(Double.parseDouble(value));
                    } else if (setting instanceof ModeSetting) {
                        ((ModeSetting) setting).setValue(value);
                    }
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed setting values and preserve the built-in default.
                }
            }
        }
    }

    public void save(ModuleRegistry registry) throws IOException {
        if (registry == null) throw new IllegalArgumentException("ModuleRegistry is required");
        Properties properties = new Properties();

        for (CyloxModule module : registry.getModules()) {
            String prefix = modulePrefix(module);
            properties.setProperty(prefix + ".enabled", Boolean.toString(module.isEnabled()));
            properties.setProperty(prefix + ".keybind", Integer.toString(module.getKeyCode()));
            for (Setting setting : module.getSettings()) {
                String value;
                if (setting instanceof BooleanSetting) {
                    value = Boolean.toString(((BooleanSetting) setting).getValue());
                } else if (setting instanceof NumberSetting) {
                    value = Double.toString(((NumberSetting) setting).getValue());
                } else if (setting instanceof ModeSetting) {
                    value = ((ModeSetting) setting).getValue();
                } else {
                    continue;
                }
                properties.setProperty(settingKey(module, setting), value);
            }
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Could not create Cylox settings directory: " + parent);
        }

        FileOutputStream output = new FileOutputStream(file);
        try {
            properties.store(output, "Cylox Client settings");
        } finally {
            output.close();
        }
    }

    private static String modulePrefix(CyloxModule module) {
        return "module." + module.getName();
    }

    private static String settingKey(CyloxModule module, Setting setting) {
        return modulePrefix(module) + ".setting." + setting.getName();
    }
}
