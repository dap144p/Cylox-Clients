package dev.cylox.client;

import dev.cylox.client.module.Category;
import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.setting.NumberSetting;
import org.junit.Test;
import static org.junit.Assert.*;

public class UiCoreTest {
    @Test
    public void previewRegistryContainsEveryCategory() {
        ModuleRegistry registry = ModuleRegistry.createUiPreview();
        for (Category category : Category.values()) {
            assertNotNull(registry.getModules(category));
        }
        assertTrue(registry.getModules().size() >= Category.values().length);
    }

    @Test
    public void moduleToggleChangesUiState() {
        CyloxModule module = new CyloxModule("Test", "", Category.HUD);
        assertFalse(module.isEnabled());
        module.toggle();
        assertTrue(module.isEnabled());
    }

    @Test
    public void numberSettingClampsToRange() {
        NumberSetting setting = new NumberSetting("Scale", 1.0D, 0.5D, 2.0D, 0.1D);
        setting.setValue(9.0D);
        assertEquals(2.0D, setting.getValue(), 0.0001D);
    }
}
