package dev.cylox.client;

import dev.cylox.client.module.Category;
import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.setting.NumberSetting;
import dev.cylox.client.setting.ModeSetting;
import org.junit.Test;
import static org.junit.Assert.*;

public class UiCoreTest {
    @Test
    public void builtInRegistryContainsWorkingHudAndMovementModules() {
        ModuleRegistry registry = ModuleRegistry.createBuiltIns();
        assertEquals(4, registry.getModules(Category.HUD).size());
        assertEquals(2, registry.getModules(Category.MOVEMENT).size());
        assertEquals(1, registry.getModules(Category.MISC).size());
        assertEquals(7, registry.getModules().size());
        assertNotNull(registry.getModule("Watermark"));
        assertNotNull(registry.getModule("FPS"));
        assertNotNull(registry.getModule("Keystrokes"));
        assertNotNull(registry.getModule("Crosshair"));
        assertNotNull(registry.getModule("ToggleSprint"));
        assertNotNull(registry.getModule("ToggleSneak"));
        assertNotNull(registry.getModule("ProtocolBridge"));
        assertTrue(registry.getModule("ProtocolBridge").isEnabled());
        assertEquals("Auto", ((ModeSetting) registry.getModule("ProtocolBridge").getSettings().get(0)).getValue());
        assertFalse(registry.getModule("ToggleSprint").isEnabled());
        assertFalse(registry.getModule("ToggleSneak").isEnabled());
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
