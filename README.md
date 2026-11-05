# Cylox Client — Standalone Vanilla 1.8.9

**Developer:** dapdev  
**Version:** 0.9.0  
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
- Edge-triggered module keybinds; holding a key does not repeatedly toggle a module.
- Persistent module states, keybinds and settings in `cylox/settings.properties`.
- **ToggleSprint:** holds Minecraft's native sprint key while enabled; the physical sprint key continues to work when the module is off.
- Lightweight LWJGL/OpenGL drawing; no Forge or Fabric runtime dependency.

ToggleSprint is a client-side input convenience. It uses Minecraft's own sprint binding and normal movement rules; it does not change player coordinates, velocity or network packets.

## Protocol compatibility

| Server version family | Protocol | v0.9.0 status |
|---|---:|---|
| Minecraft 1.8.x | 47 | Native vanilla compatibility |
| Minecraft 1.9–1.12.2 | 107–340 | Not yet supported by the built-in client connection |
| Minecraft 1.13–1.13.2 | 393–404 | Not yet supported by the built-in client connection |

The protocol catalogue and server status ping can identify advertised protocol IDs, but that is not packet translation. A complete translator must rewrite login, play and stateful packets in both directions.

An external translator was evaluated, but its official Java 8-compatible proxy distribution rejects 32-bit Java runtimes. It is not bundled or advertised as compatible with the 32-bit Android target. Native 1.8.x connectivity remains the supported mode in this build.

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
- Open the **Movement** category and enable **ToggleSprint** to keep Minecraft's native sprint binding held while a player is in-game.
- Disable ToggleSprint to restore normal physical-key behavior.
- Select a module to edit its settings or assign a keybind.
- Press the assigned key while the Click GUI is closed to toggle that module.
- Settings are saved automatically in `<game directory>/cylox/settings.properties`.

## Build

Requirements: JDK 8+ and Gradle 8.10.2.

    gradle clean test buildClientPackage

Output:

    build/distributions/Cylox-Client-Vanilla-1.8.9-0.9.0.zip

Verify injection targets against the official client JAR:

    gradle verifyVanilla189Hooks -Pvanilla189Jar=/path/to/1.8.9.jar

The build does not require a decompiled or MCP Minecraft JAR.

## Attribution

Cylox Client — **dapdev**.
