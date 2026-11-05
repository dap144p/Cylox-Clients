package dev.cylox.client.module;

import dev.cylox.client.setting.BooleanSetting;
import dev.cylox.client.setting.NumberSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Registry of working client-side HUD modules. No placeholder combat actions are exposed. */
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

        return new ModuleRegistry()
            .register(watermark)
            .register(fps)
            .register(keystrokes)
            .register(crosshair);
    }

    /** Kept for source compatibility with early preview builds; now returns real HUD modules. */
    @Deprecated
    public static ModuleRegistry createUiPreview() {
        return createBuiltIns();
    }
}
