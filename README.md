# Cylox Client — Standalone Vanilla 1.8.9

**Developer:** dapdev  
**Version:** 0.11.0  
**Base:** Minecraft Java 1.8.9 (protocol 47)  
**Runtime:** Vanilla + LaunchWrapper 1.12; no Forge/Fabric requirement.

## Client features

Cylox launches the normal Minecraft Java 1.8.9 game and adds a lightweight in-game Click GUI, HUD, movement controls and an optional client-side protocol translator.

- Three-column Click GUI with category navigation, module search, toggles, setting controls and keybind editor.
- Right Shift opens/closes the Click GUI; Escape closes it.
- Watermark HUD (CYLOX / 1.8.9), live FPS, Keystrokes (W/A/S/D, Space and mouse buttons), and configurable Crosshair.
- Persistent module states, keybinds and settings in `cylox/settings.properties`.
- **ToggleSprint** and **ToggleSneak** use Minecraft's native key bindings and preserve physical-key behavior when disabled.
- Lightweight LWJGL/OpenGL drawing; no Forge or Fabric runtime dependency.

## Protocol compatibility

Cylox uses ViaVersion's client-side Netty pipeline through the ViaMCP 1.8.x platform implementation.

| Server version family | Protocol | v0.11.0 mode |
|---|---:|---|
| Minecraft 1.8.x | 47 | Native vanilla connection |
| Minecraft 1.9–1.12.2 | 107–340 | Auto translation |
| Minecraft 1.13–1.13.2 | 393–404 | Auto translation |

The **ProtocolBridge** module is enabled by default and starts in **Auto** mode. Before opening a game connection, Cylox performs a Minecraft status ping off the Netty event loop, reads the server's advertised protocol ID, and selects that ViaVersion target. It then creates a translator connection for that socket. Each game connection receives its own Via user/pipeline.

If the status ping fails or the protocol is not in Cylox's tested catalogue, Cylox falls back to native protocol 47. Such a fallback does not make an incompatible newer server playable; the server can still reject the native connection. Use **Native** in the module settings to disable protocol auto-detection completely.

The included Java-8-compatible libraries are ViaVersion 5.12.1, ViaBackwards 5.12.1 and ViaRewind 4.2.0. The version ZIP contains the libraries and the version metadata also declares them for launchers that fetch libraries individually. Do not remove them from the game directory. Do not install duplicate ViaVersion/ViaBackwards/ViaRewind copies alongside this bundle.

Protocol support depends on upstream mappings and server behavior; plugins, custom registries, server-side modifications and unsupported protocol extensions can still prevent joining or cause gameplay limitations. CI verifies compilation, packet-pipeline injection points and vanilla bytecode mappings, but it is not a live multiplayer test.

## Download

- [Cylox Client v0.11.0 release](https://github.com/dap144p/Cylox-Clients/releases/tag/v0.11.0)
- [Latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [GitHub repository](https://github.com/dap144p/Cylox-Clients)

Use the **ZIP** for MJ Launcher and other version-folder launchers. The standalone JAR is the client adapter only; its Via dependencies are supplied separately in the release and in the ZIP.

## Install — version folder method

1. Install vanilla Minecraft Java 1.8.9 once in your launcher.
2. Open the Minecraft game directory configured in that launcher (the directory containing `versions/` and `libraries/`).
3. Extract the Cylox ZIP into that game directory. Merge the `versions` and `libraries` folders when prompted.
4. Refresh the launcher's version list.
5. Select `Cylox-1.8.9` and start it.

Do not extract the ZIP inside `versions/`; it already contains the correct paths.

## In-game settings

- Right Shift: open or close the Cylox Click GUI.
- Escape: close the Click GUI.
- **Movement:** ToggleSprint and ToggleSneak.
- **Misc → ProtocolBridge:** keep the module enabled and set **Target Version** to **Auto** for status-ping detection, or **Native** to force protocol 47.
- HUD: Watermark, FPS, Keystrokes and Crosshair.
- Select a module to edit its settings or assign a keybind. Press the assigned key while the Click GUI is closed to toggle it.
- Settings are saved automatically in `<game directory>/cylox/settings.properties`.

## Build and verification

Requirements: JDK 8+ and Gradle 8.10.2.

    gradle clean test buildClientPackage

Output:

    build/distributions/Cylox-Client-Vanilla-1.8.9-0.11.0.zip

Verify the tick/render/network/compression hooks and movement mappings against Mojang's official 1.8.9 JAR:

    gradle verifyVanilla189Hooks -Pvanilla189Jar=/path/to/1.8.9.jar

The build downloads pinned Java-8-compatible Via libraries from the ViaMCP snapshot declared in `build.gradle`. The build does not require a decompiled or MCP Minecraft JAR.

## Licensing and attribution

The integrated ViaMCP platform and the combined Cylox distribution are released under **GPL-3.0-or-later**. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for exact upstream source revisions and attribution.

Cylox Client — **dapdev**.
