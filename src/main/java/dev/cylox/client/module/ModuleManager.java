package dev.cylox.client.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private static final List<ClientModule> MODULES = new ArrayList<ClientModule>();
    private static boolean initialized;

    private ModuleManager() { }

    public static void init() {
        if (initialized) return;
        MODULES.add(new ClientModule("Performance", "Client performance tools", false));
        MODULES.add(new ClientModule("HUD Editor", "Custom HUD layout", false));
        MODULES.add(new ClientModule("Render Control", "Visual and render options", false));
        MODULES.add(new ClientModule("AFK Tools", "Session utility controls", false));
        MODULES.add(new ClientModule("Alt Manager", "Account profile interface", false));
        MODULES.add(new ClientModule("AutoProtocol", "ViaForge integration - planned", false));
        initialized = true;
    }

    public static List<ClientModule> getModules() {
        return Collections.unmodifiableList(MODULES);
    }
}
