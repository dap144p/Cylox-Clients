package dev.cylox.client.module;

import dev.cylox.client.setting.BooleanSetting;
import dev.cylox.client.setting.ModeSetting;
import dev.cylox.client.setting.NumberSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Holds module metadata and state. Sample entries are UI previews, not gameplay implementations. */
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

    public static ModuleRegistry createUiPreview() {
        return new ModuleRegistry()
            .register(new CyloxModule("KillAura", "Combat module preview", Category.COMBAT)
                .addSetting(new NumberSetting("Range", 3.0D, 1.0D, 6.0D, 0.1D))
                .addSetting(new ModeSetting("Target Mode", "Players", "All")))
            .register(new CyloxModule("AutoClicker", "Combat module preview", Category.COMBAT)
                .addSetting(new NumberSetting("CPS", 10.0D, 1.0D, 20.0D, 1.0D))
                .addSetting(new BooleanSetting("Randomize", false)))
            .register(new CyloxModule("Sprint", "Movement module preview", Category.MOVEMENT)
                .addSetting(new ModeSetting("Mode", "Legit", "Omni")))
            .register(new CyloxModule("Speed", "Movement module preview", Category.MOVEMENT)
                .addSetting(new NumberSetting("Multiplier", 1.0D, 1.0D, 2.0D, 0.05D)))
            .register(new CyloxModule("AutoRespawn", "Player module preview", Category.PLAYER)
                .addSetting(new BooleanSetting("Instant", true)))
            .register(new CyloxModule("Fullbright", "Render module preview", Category.RENDER)
                .addSetting(new NumberSetting("Gamma", 1.0D, 1.0D, 10.0D, 0.5D)))
            .register(new CyloxModule("FastPlace", "World module preview", Category.WORLD)
                .addSetting(new NumberSetting("Delay", 0.0D, 0.0D, 4.0D, 1.0D)))
            .register(new CyloxModule("AutoGG", "Misc module preview", Category.MISC)
                .addSetting(new ModeSetting("Message", "GG", "Good game")))
            .register(new CyloxModule("FPS", "HUD module preview", Category.HUD)
                .addSetting(new BooleanSetting("Show Background", false)))
            .register(new CyloxModule("Keystrokes", "HUD module preview", Category.HUD)
                .addSetting(new BooleanSetting("Show Mouse Buttons", true)));
    }
}
