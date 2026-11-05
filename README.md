# Cylox Client — Standalone Vanilla 1.8.9

**Developer:** dapdev  
**Version:** 0.10.0  
**Base:** Minecraft Java 1.8.9 (protocol 47)  
**Runtime:** Vanilla + LaunchWrapper 1.12; no Forge/Fabric requirement.

## Working client features

Cylox launches the normal Minecraft Java 1.8.9 game and adds a lightweight in-game Click GUI and client HUD. The Minecraft world, renderer and native networking remain based on vanilla 1.8.9.

- Three-column Click GUI with category navigation, module search, toggles, setting controls and keybind editor.
- Right Shift opens/closes the Click GUI; Escape closes it.
- Watermark HUD (CYLOX / 1.8.9), live FPS, Keystrokes (W/A/S/D, Space and mouse buttons), and configurable Crosshair.
- Edge-triggered module keybinds; holding a key does not repeatedly toggle a module.
- Persistent module states, keybinds and settings in `cylox/settings.properties`.
- **ToggleSprint:** holds Minecraft's native sprint key while enabled.
- **ToggleSneak:** holds Minecraft's native sneak key while enabled.
- Both movement modules respect the physical key when their toggle is disabled and return to the physical-key state when disabled.
- Lightweight LWJGL/OpenGL drawing; no Forge or Fabric runtime dependency.

Movement modules call Minecraft's own KeyBinding state. They do not modify player coordinates, velocity, movement packets or server state directly. The CI verifier checks the 1.8.9 obfuscated key-binding fields, methods and game tick/render hook targets against the official Mojang client JAR.

## Protocol compatibility

| Server version family | Protocol | v0.10.0 status |
|---|---:|---|
| Minecraft 1.8.x | 47 | Native vanilla compatibility |
| Minecraft 1.9–1.12.2 | 107–340 | Not supported by the built-in client connection |
| Minecraft 1.13–1.13.2 | 393–404 | Not supported by the built-in client connection |

The protocol catalogue and server status ping can identify advertised protocol IDs, but that is not packet translation. A complete translator must rewrite login, play and stateful packets in both directions. This translator is still not integrated.

ViaProxy is an external proxy application, not an in-client translator. It can translate between supported client/server versions when run as a separate process, but requires its own runtime and memory. Cylox does not bundle or launch it, and that workflow has not been verified on MJ Launcher/Android. Native 1.8.x connectivity remains the only built-in supported mode.

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
- Open **Movement** and enable **ToggleSprint** to hold Minecraft's native sprint binding while in-game.
- Enable **ToggleSneak** to hold Minecraft's native sneak binding while in-game.
- Disable either module to restore normal physical-key behavior.
- Select a module to edit its settings or assign a keybind.
- Press the assigned key while the Click GUI is closed to toggle that module.
- Settings are saved automatically in `<game directory>/cylox/settings.properties`.

## Build

Requirements: JDK 8+ and Gradle 8.10.2.

    gradle clean test buildClientPackage

Output:

    build/distributions/Cylox-Client-Vanilla-1.8.9-0.10.0.zip

Verify injection targets and movement key mappings against the official client JAR:

    gradle verifyVanilla189Hooks -Pvanilla189Jar=/path/to/1.8.9.jar

The build does not require a decompiled or MCP Minecraft JAR.

## Attribution

Cylox Client — **dapdev**.
