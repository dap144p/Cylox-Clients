package dev.cylox.client.module;

import dev.cylox.client.setting.BooleanSetting;
import dev.cylox.client.setting.NumberSetting;
import dev.cylox.client.setting.ModeSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Registry of implemented client-side modules. Unimplemented combat actions are not exposed. */
public final class ModuleRegistry {
    private final List<CyloxModule> modules = new ArrayList<CyloxModule>();

    public ModuleRegistry register(CyloxModule module) {
        if (module == null) throw new IllegalArgumentException("Module cannot be null");
        for (CyloxModule existing : modules) {
            if (existing.getName().equalsIgnoreCase(module.getName())) {
                throw new IllegalArgumentException("Duplicate module: " + module.getName());
            }
        }
        modules.add(module);
        return this;
    }

    public List<CyloxModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<CyloxModule> getModules(Category category) {
        List<CyloxModule> result = new ArrayList<CyloxModule>();
        for (CyloxModule module : modules) {
            if (module.getCategory() == category) result.add(module);
        }
        return Collections.unmodifiableList(result);
    }

    public CyloxModule getModule(String name) {
        if (name == null) return null;
        for (CyloxModule module : modules) {
            if (module.getName().equalsIgnoreCase(name)) return module;
        }
        return null;
    }

    public static ModuleRegistry createBuiltIns() {
        CyloxModule watermark = new CyloxModule("Watermark", "Cylox client name and Minecraft version", Category.HUD);
        watermark.setEnabled(true);

        CyloxModule fps = new CyloxModule("FPS", "Live frames-per-second counter", Category.HUD)
            .addSetting(new BooleanSetting("Show Background", true));
        fps.setEnabled(true);

        CyloxModule keystrokes = new CyloxModule("Keystrokes", "Live movement keys and mouse buttons", Category.HUD)
            .addSetting(new BooleanSetting("Show Mouse Buttons", true));
        keystrokes.setEnabled(true);

        CyloxModule crosshair = new CyloxModule("Crosshair", "Client-side custom center crosshair", Category.HUD)
            .addSetting(new NumberSetting("Size", 8.0D, 4.0D, 14.0D, 1.0D))
            .addSetting(new NumberSetting("Gap", 3.0D, 1.0D, 8.0D, 1.0D));

        CyloxModule toggleSprint = new CyloxModule("ToggleSprint",
            "Holds Minecraft's native sprint key while enabled", Category.MOVEMENT);
        CyloxModule toggleSneak = new CyloxModule("ToggleSneak",
            "Holds Minecraft's native sneak key while enabled", Category.MOVEMENT);
        CyloxModule protocolBridge = new CyloxModule("ProtocolBridge",
            "Automatically translates supported 1.9–1.13.2 servers for this 1.8.9 client", Category.MISC)
            .addSetting(new ModeSetting("Target Version", "Auto", "Native"));
        protocolBridge.setEnabled(true);

        return new ModuleRegistry()
            .register(watermark)
            .register(fps)
            .register(keystrokes)
            .register(crosshair)
            .register(toggleSprint)
            .register(toggleSneak)
            .register(protocolBridge);
    }

    /** Kept for source compatibility with early preview builds; now returns real modules. */
    @Deprecated
    public static ModuleRegistry createUiPreview() {
        return createBuiltIns();
    }
}
