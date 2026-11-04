package dev.cylox.client.runtime;

import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.ui.ClickGui;

/**
 * Shared client state used by the optional Minecraft 1.8.9 screen adapter.
 * Sample modules remain UI previews until real gameplay implementations are added.
 */
public final class CyloxRuntime {
    private static final ModuleRegistry MODULES = ModuleRegistry.createUiPreview();
    private static final ClickGui CLICK_GUI = new ClickGui(MODULES);

    private CyloxRuntime() {
    }

    public static ModuleRegistry getModules() {
        return MODULES;
    }

    public static ClickGui getClickGui() {
        return CLICK_GUI;
    }
}
