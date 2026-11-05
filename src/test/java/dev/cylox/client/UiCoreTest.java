package dev.cylox.client;

import dev.cylox.client.module.Category;
import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.setting.NumberSetting;
import org.junit.Test;
import static org.junit.Assert.*;

public class UiCoreTest {
    @Test
    public void builtInRegistryContainsWorkingHudAndMovementModules() {
        ModuleRegistry registry = ModuleRegistry.createBuiltIns();
        assertEquals(4, registry.getModules(Category.HUD).size());
        assertEquals(1, registry.getModules(Category.MOVEMENT).size());
        assertEquals(5, registry.getModules().size());
        assertNotNull(registry.getModule("Watermark"));
        assertNotNull(registry.getModule("FPS"));
        assertNotNull(registry.getModule("Keystrokes"));
        assertNotNull(registry.getModule("Crosshair"));
        assertNotNull(registry.getModule("ToggleSprint"));
        assertFalse(registry.getModule("ToggleSprint").isEnabled());
        assertNull(registry.getModule("KillAura"));
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
