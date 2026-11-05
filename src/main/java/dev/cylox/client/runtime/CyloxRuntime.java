package dev.cylox.client.runtime;

import dev.cylox.client.hud.HudOverlay;
import dev.cylox.client.input.ModuleKeybindController;
import dev.cylox.client.module.ModuleRegistry;
import dev.cylox.client.protocol.ProtocolCompatibilityEngine;
import dev.cylox.client.protocol.ProtocolVersion;
import dev.cylox.client.protocol.ServerStatusClient;
import dev.cylox.client.ui.ClickGui;
import java.io.File;
import java.io.IOException;

/** Shared Cylox services used by the Minecraft 1.8.9 runtime adapter. */
public final class CyloxRuntime {
    private static final ModuleRegistry MODULES = ModuleRegistry.createBuiltIns();
    private static final ClickGui CLICK_GUI = new ClickGui(MODULES);
    private static final HudOverlay HUD = new HudOverlay(MODULES);
    private static final ModuleKeybindController KEYBINDS = new ModuleKeybindController();
    private static final ProtocolCompatibilityEngine PROTOCOL =
        new ProtocolCompatibilityEngine(ProtocolVersion.V1_8_X);
    private static final ServerStatusClient SERVER_STATUS = new ServerStatusClient();
    private static CyloxSettingsStore settingsStore;
    private static boolean initialized;

    private CyloxRuntime() { }

    /** Called once by the LaunchWrapper tweaker after the game directory is known. */
    public static synchronized void initialize(File gameDirectory) {
        if (initialized) return;
        File root = gameDirectory == null ? new File(".") : gameDirectory;
        settingsStore = new CyloxSettingsStore(new File(new File(root, "cylox"), "settings.properties"));
        try {
            settingsStore.load(MODULES);
        } catch (IOException error) {
            System.err.println("[Cylox] Could not load settings: " + error.getMessage());
        }
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(CyloxRuntime::saveSettings, "Cylox-settings-save"));
        } catch (IllegalStateException ignored) {
            // The VM is already shutting down; the explicit save path remains available.
        }
        initialized = true;
    }

    public static synchronized void saveSettings() {
        if (settingsStore == null) return;
        try {
            settingsStore.save(MODULES);
        } catch (IOException error) {
            System.err.println("[Cylox] Could not save settings: " + error.getMessage());
        }
    }

    public static ModuleRegistry getModules() { return MODULES; }
    public static ClickGui getClickGui() { return CLICK_GUI; }
    public static HudOverlay getHud() { return HUD; }
    public static ModuleKeybindController getKeybindController() { return KEYBINDS; }
    public static ProtocolCompatibilityEngine getProtocolEngine() { return PROTOCOL; }
    public static ServerStatusClient getServerStatusClient() { return SERVER_STATUS; }
}
