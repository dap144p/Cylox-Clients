# Cylox Client — Vanilla 1.8.9

**Developer:** dapdev  
**Version:** 0.4.0  
**Loader:** Vanilla Minecraft + LaunchWrapper 1.12 (no Forge/Fabric)

## Current build

- Three-column Click GUI with categories, search, module toggles, settings, and keybind editor.
- Right Shift opens/closes the overlay; Escape closes it.
- The game-side overlay uses LWJGL/OpenGL and does not require MCP class names or a modified Minecraft JAR.
- A bytecode verifier scans the official obfuscated 1.8.9 client and confirms the tick/render injection points.
- The install ZIP contains the custom version JSON and the small Cylox library. It does not redistribute Minecraft, assets, natives, or the game's libraries.

**Important:** the included sample modules are interface previews. Their toggles change local UI state only; gameplay modules are not implemented yet. Testing inside MJ Launcher on a physical device is still required.

## Download

- [Cylox Client latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Latest source ZIP](https://github.com/dap144p/Cylox-Clients/archive/refs/heads/main.zip)
- [GitHub repository](https://github.com/dap144p/Cylox-Clients)

## Install — version folder method

1. Install vanilla Minecraft Java **1.8.9** once in your launcher.
2. Extract the Cylox ZIP into the **root of the Minecraft game directory** (the folder that already contains `versions` and `libraries`). Merge folders when prompted.
3. Select **Cylox-1.8.9** in the launcher's version list.
4. Launch. The profile reuses the installed vanilla 1.8.9 client JAR and only downloads missing declared libraries.

Do not extract the ZIP inside `versions`; it already contains the correct `versions/` and `libraries/` paths.

## Build

Requirements: JDK 8+ and Gradle 8.10.2.

    gradle clean buildClientPackage

Output:

    build/distributions/Cylox-Client-Vanilla-1.8.9-0.4.0.zip

Verify injection targets against the official client JAR:

    gradle verifyVanilla189Hooks -Pvanilla189Jar=/path/to/1.8.9.jar

The build does not require a decompiled or MCP Minecraft JAR.

## Attribution

Cylox Client — **dapdev**.
