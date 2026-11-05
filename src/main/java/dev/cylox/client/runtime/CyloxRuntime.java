package dev.cylox.client.runtime;

import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.protocol.ProtocolCompatibilityEngine;
import dev.cylox.client.protocol.ProtocolVersion;
import dev.cylox.client.ui.ClickGui;

/**
 * Shared Cylox client services used by the Minecraft 1.8.9 runtime adapter.
 * Sample modules remain UI previews until real gameplay implementations are added.
 */
public final class CyloxRuntime {
    private static final ModuleRegistry MODULES = ModuleRegistry.createUiPreview();
    private static final ClickGui CLICK_GUI = new ClickGui(MODULES);
    private static final ProtocolCompatibilityEngine PROTOCOL =
        new ProtocolCompatibilityEngine(ProtocolVersion.V1_8_X);

    private CyloxRuntime() { }

    public static ModuleRegistry getModules() { return MODULES; }
    public static ClickGui getClickGui() { return CLICK_GUI; }
    public static ProtocolCompatibilityEngine getProtocolEngine() { return PROTOCOL; }
}
