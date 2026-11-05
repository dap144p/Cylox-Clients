package dev.cylox.client.input;

import dev.cylox.client.module.CyloxModule;
import dev.cylox.client.module.ModuleRegistry;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

/** Edge-triggered module keybinds. Each physical key press toggles a module only once. */
public final class ModuleKeybindController {
    private final boolean[] previousState = new boolean[256];

    public boolean tick(ModuleRegistry registry, IntPredicate keyDown, int ignoredKey, boolean allowToggles) {
        if (registry == null || keyDown == null) {
            throw new IllegalArgumentException("Registry and key state reader are required");
        }

        Set<Integer> checkedKeys = new HashSet<Integer>();
        boolean changed = false;
        for (CyloxModule module : registry.getModules()) {
            int keyCode = module.getKeyCode();
            if (keyCode <= 0 || keyCode >= previousState.length || keyCode == ignoredKey) continue;
            if (!checkedKeys.add(keyCode)) continue;

            boolean down = keyDown.test(keyCode);
            boolean justPressed = down && !previousState[keyCode];
            previousState[keyCode] = down;
            if (allowToggles && justPressed) {
                for (CyloxModule target : registry.getModules()) {
                    if (target.getKeyCode() == keyCode) target.toggle();
                }
                changed = true;
            }
        }
        return changed;
    }
}
