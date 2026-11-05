# Cylox Client — Standalone Vanilla 1.8.9

**Developer:** dapdev  
**Version:** 0.8.0  
**Base:** Minecraft Java 1.8.9 (protocol 47)  
**Runtime:** Vanilla + LaunchWrapper 1.12; no Forge/Fabric requirement.

## Working client features

Cylox launches the normal Minecraft Java 1.8.9 game and adds a lightweight in-game Click GUI and client HUD. The Minecraft world, renderer and native networking remain based on vanilla 1.8.9.

- Three-column Click GUI with category navigation, module search, toggles, setting controls and keybind editor.
- Right Shift opens/closes the Click GUI; Escape closes it.
- Watermark HUD (CYLOX / 1.8.9).
- Live FPS counter.
- Keystrokes HUD for W/A/S/D, Space and left/right mouse buttons.
- Optional custom center crosshair with adjustable size and gap.
- Module keybinds now trigger on a key press edge; holding a key does not repeatedly toggle the module.
- Module enabled states, keybinds and setting values persist in the game directory at `cylox/settings.properties`.
- Lightweight LWJGL/OpenGL drawing; no compile-time Minecraft or mod-loader dependency in the active adapter.

The Click GUI opens on the HUD category. The only built-in modules currently exposed are the working HUD modules; combat and movement entries are not listed as functional features until actual game-side implementations are ready.

## Protocol compatibility

| Server version family | Protocol | v0.8.0 status |
|---|---:|---|
| Minecraft 1.8.x | 47 | Native vanilla compatibility |
| Minecraft 1.9–1.12.2 | 107–340 | Not yet supported by the built-in client connection |
| Minecraft 1.13–1.13.2 | 393–404 | Not yet supported by the built-in client connection |

The protocol catalogue and server status ping can identify advertised protocol IDs, but that is not packet translation. A complete translator must rewrite login, play and stateful packets in both directions.

An external translator was evaluated, but its official Java 8-compatible proxy distribution explicitly rejects 32-bit Java runtimes. It is therefore not bundled or advertised as compatible with the 32-bit Android target. Native 1.8.x connectivity remains the supported mode in this build.

## Download

- [Cylox Client latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Latest source ZIP](https://github.com/dap144p/Cylox-Clients/archive/refs/heads/main.zip)
- [GitHub repository](https://github.com/dap144p/Cylox-Clients)

## Install — version folder method

1. Install vanilla Minecraft Java 1.8.9 once in your launcher.
2. Extract the Cylox ZIP into the Minecraft game directory (the directory that contains `versions/` and `libraries/`). Merge folders when prompted.
3. Refresh the launcher's version list and select `Cylox-1.8.9`.
4. Launch. The profile inherits the launcher's vanilla 1.8.9 client and adds Cylox through LaunchWrapper.

Do not extract the ZIP inside `versions/`; it already contains the correct paths.

## In-game controls and settings

- Right Shift: open or close the Cylox Click GUI.
- Escape: close the Click GUI.
- Select a module in the HUD category to edit its settings or assign a keybind.
- Press the assigned key while the Click GUI is closed to toggle the module.
- Settings are saved automatically in `<game directory>/cylox/settings.properties`.

## Build

Requirements: JDK 8+ and Gradle 8.10.2.

    gradle clean test buildClientPackage

Output:

    build/distributions/Cylox-Client-Vanilla-1.8.9-0.8.0.zip

Verify injection targets against the official client JAR:

    gradle verifyVanilla189Hooks -Pvanilla189Jar=/path/to/1.8.9.jar

The build does not require a decompiled or MCP Minecraft JAR.

## Attribution

Cylox Client — **dapdev**.
