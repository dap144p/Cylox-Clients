# Cylox Client — Standalone Vanilla 1.8.9

**Developer:** dapdev  
**Version:** 0.7.0  
**Base:** Minecraft Java 1.8.9 (protocol 47)  
**Runtime:** Vanilla + LaunchWrapper 1.12; no Forge/Fabric requirement.

## What is playable in v0.7.0

Cylox launches the normal Minecraft Java 1.8.9 game and adds its own lightweight in-game Click GUI and client HUD. The game itself remains vanilla; Cylox does not replace Minecraft's world, renderer, or networking stack.

Working client features:
- Three-column Click GUI with category navigation, module search, module toggles, settings controls, and keybind editor.
- Right Shift opens/closes the Click GUI; Escape closes it.
- Watermark HUD (CYLOX / 1.8.9).
- Live FPS counter updated from render frames.
- Keystrokes HUD for W/A/S/D, Space, and left/right mouse buttons.
- Optional custom center crosshair with adjustable size and gap.
- Lightweight LWJGL/OpenGL drawing; no compile-time Minecraft or mod-loader dependency in the active adapter.
- HUD features are local/client-side and can be toggled in the HUD category.

The Click GUI opens on the HUD category. Other module categories are intentionally empty in this build; Cylox no longer lists combat/movement entries that were only non-functional UI previews.

## Protocol compatibility

| Server version family | Protocol | v0.7.0 status |
|---|---:|---|
| Minecraft 1.8.x | 47 | Native vanilla compatibility |
| Minecraft 1.9–1.12.2 | 107–340 | Version detection only; not playable through Cylox yet |
| Minecraft 1.13–1.13.2 | 393–404 | Version detection only; not playable through Cylox yet |

Cylox can send a standard status ping and read the server-advertised version/protocol. This does not translate login or gameplay packets. Connecting 1.8.9 to a newer-protocol server requires a complete bidirectional translator, which is not included yet.

## Download

- [Cylox Client latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Latest source ZIP](https://github.com/dap144p/Cylox-Clients/archive/refs/heads/main.zip)
- [GitHub repository](https://github.com/dap144p/Cylox-Clients)

## Install — version folder method

1. Install vanilla Minecraft Java 1.8.9 once in your launcher.
2. Extract the Cylox ZIP into the Minecraft game directory (the directory that contains versions/ and libraries/). Merge folders when prompted.
3. Refresh the launcher's version list and select Cylox-1.8.9.
4. Launch. The profile inherits the launcher's vanilla 1.8.9 client and adds Cylox through LaunchWrapper.

Do not extract the ZIP inside versions/; it already contains the correct versions/ and libraries/ paths.

The ZIP does not redistribute Minecraft, assets, natives, or the base game's libraries. Use a launcher that supports inherited 1.8.9 versions, LaunchWrapper, and standard library artifacts. MJ Launcher device/runtime smoke testing is still required.

## Build

Requirements: JDK 8+ and Gradle 8.10.2.

    gradle clean test buildClientPackage

Output:

    build/distributions/Cylox-Client-Vanilla-1.8.9-0.7.0.zip

Verify injection targets against the official client JAR:

    gradle verifyVanilla189Hooks -Pvanilla189Jar=/path/to/1.8.9.jar

The build does not require a decompiled or MCP Minecraft JAR.

## Attribution

Cylox Client — **dapdev**.
