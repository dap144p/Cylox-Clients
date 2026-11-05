package dev.cylox.client.runtime;

import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.setting.NumberSetting;
import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class CyloxSettingsStoreTest {
    @Rule public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void savesAndRestoresModuleStateKeybindsAndSettings() throws Exception {
        File settingsFile = new File(temporaryFolder.newFolder("game"), "cylox/settings.properties");
        ModuleRegistry before = ModuleRegistry.createBuiltIns();
        before.getModule("Watermark").setEnabled(false);
        before.getModule("Crosshair").setEnabled(true);
        before.getModule("Crosshair").setKeyCode(42);
        ((NumberSetting) before.getModule("Crosshair").getSettings().get(0)).setValue(12.0D);

        CyloxSettingsStore store = new CyloxSettingsStore(settingsFile);
        store.save(before);

        ModuleRegistry after = ModuleRegistry.createBuiltIns();
        store.load(after);
        assertFalse(after.getModule("Watermark").isEnabled());
        assertTrue(after.getModule("Crosshair").isEnabled());
        assertEquals(42, after.getModule("Crosshair").getKeyCode());
        assertEquals(12.0D, ((NumberSetting) after.getModule("Crosshair").getSettings().get(0)).getValue(), 0.0001D);
    }

    @Test
    public void malformedValuesKeepSafeDefaults() throws Exception {
        File settingsFile = new File(temporaryFolder.getRoot(), "settings.properties");
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("module.FPS.enabled", "not-a-boolean");
        properties.setProperty("module.Crosshair.keybind", "not-a-key");
        properties.setProperty("module.Crosshair.setting.Size", "not-a-number");
        FileOutputStreamWriter.write(settingsFile, properties);

        ModuleRegistry registry = ModuleRegistry.createBuiltIns();
        new CyloxSettingsStore(settingsFile).load(registry);
        assertTrue(registry.getModule("FPS").isEnabled());
        assertEquals(0, registry.getModule("Crosshair").getKeyCode());
        assertEquals(8.0D, ((NumberSetting) registry.getModule("Crosshair").getSettings().get(0)).getValue(), 0.0001D);
    }

    private static final class FileOutputStreamWriter {
        private static void write(File file, java.util.Properties properties) throws Exception {
            java.io.FileOutputStream output = new java.io.FileOutputStream(file);
            try {
                properties.store(output, "test");
            } finally {
                output.close();
            }
        }
    }
}
