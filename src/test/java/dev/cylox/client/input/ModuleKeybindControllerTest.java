package dev.cylox.client.input;

import dev.cylox.client.module.Category;
import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.module.ModuleRegistry;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModuleKeybindControllerTest {
    @Test
    public void togglesOnPressEdgeAndNotEveryHeldTick() {
        ModuleRegistry registry = new ModuleRegistry();
        CyloxModule module = new CyloxModule("Test HUD", "", Category.HUD);
        module.setKeyCode(44);
        registry.register(module);
        ModuleKeybindController controller = new ModuleKeybindController();
        final boolean[] keys = new boolean[256];

        assertFalse(controller.tick(registry, key -> keys[key], 54, true));
        keys[44] = true;
        assertTrue(controller.tick(registry, key -> keys[key], 54, true));
        assertTrue(module.isEnabled());
        assertFalse(controller.tick(registry, key -> keys[key], 54, true));
        assertTrue(module.isEnabled());

        keys[44] = false;
        assertFalse(controller.tick(registry, key -> keys[key], 54, true));
        keys[44] = true;
        assertTrue(controller.tick(registry, key -> keys[key], 54, true));
        assertFalse(module.isEnabled());
    }

    @Test
    public void doesNotToggleWhileClickGuiIsOpenOrOnIgnoredKey() {
        ModuleRegistry registry = new ModuleRegistry();
        CyloxModule module = new CyloxModule("Test HUD", "", Category.HUD);
        module.setKeyCode(44);
        registry.register(module);
        ModuleKeybindController controller = new ModuleKeybindController();
        final boolean[] keys = new boolean[256];

        keys[44] = true;
        assertFalse(controller.tick(registry, key -> keys[key], 54, false));
        assertFalse(module.isEnabled());
        keys[44] = false;
        controller.tick(registry, key -> keys[key], 54, false);
        keys[54] = true;
        assertFalse(controller.tick(registry, key -> keys[key], 54, true));
        assertFalse(module.isEnabled());
    }
}
