package dev.cylox.client.runtime;

import dev.cylox.client.hud.HudOverlay;
import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.protocol.ProtocolCompatibilityEngine;
import dev.cylox.client.protocol.ProtocolVersion;
import dev.cylox.client.protocol.ServerStatusClient;
import dev.cylox.client.ui.ClickGui;

/** Shared Cylox services used by the Minecraft 1.8.9 runtime adapter. */
public final class CyloxRuntime {
    private static final ModuleRegistry MODULES = ModuleRegistry.createBuiltIns();
    private static final ClickGui CLICK_GUI = new ClickGui(MODULES);
    private static final HudOverlay HUD = new HudOverlay(MODULES);
    private static final ProtocolCompatibilityEngine PROTOCOL =
        new ProtocolCompatibilityEngine(ProtocolVersion.V1_8_X);
    private static final ServerStatusClient SERVER_STATUS = new ServerStatusClient();

    private CyloxRuntime() { }

    public static ModuleRegistry getModules() { return MODULES; }
    public static ClickGui getClickGui() { return CLICK_GUI; }
    public static HudOverlay getHud() { return HUD; }
    public static ProtocolCompatibilityEngine getProtocolEngine() { return PROTOCOL; }
    public static ServerStatusClient getServerStatusClient() { return SERVER_STATUS; }
}
