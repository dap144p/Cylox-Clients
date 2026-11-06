# Cylox Client

**Developer:** dapdev  
**Version:** 1.3.2  
**Minecraft:** Java 1.8.9 (base protocol 47)  
**Runtime:** MJ Launcher-provided Java and renderer; Minecraft 1.8.9 client, libraries and assets resolved from version metadata  
**Loader:** LaunchWrapper tweaker only; no Forge or Fabric

Cylox is a minimal 1.8.9 client. Its gameplay-side purpose is to reduce rendering work while the game window is inactive. Its protocol compatibility features are embedded in the same Cylox JAR.

## Background behavior

- Detects whether the LWJGL display is active and visible.
- Skips the vanilla `EntityRenderer` frame completely when the display is inactive/hidden, so world rendering drops to **0 FPS**.
- Keeps the whole Minecraft main loop at **20 Hz** in the background. This is deliberately not reduced to 2 FPS: `Display.sync()` throttles the game loop itself, so a 2 FPS cap could starve client ticks and delay queued packet processing.
- Returns to normal rendering and the launcher's selected frame limit when the display becomes active again.
- Does not pause the Minecraft network stack deliberately and does not automate player actions.

This is not an Android background service. The launcher must keep the game process alive for the connection to remain open. Android may suspend or kill background applications, and the JAR cannot override that operating-system or launcher policy. A real MJ Launcher/device test is required before treating background connectivity as guaranteed.

## Lightweight in-game graphics profile

- Applies once after Minecraft loads `options.txt`; Cylox does not overwrite the file with its own defaults.
- Caps render distance at **4 chunks** (lower user values are preserved), disables Fancy Graphics, smooth lighting/AO, clouds and entity shadows, and sets particles to **Minimal**.
- Keeps the world, blocks, entities, player model, HUD, input, game ticks and network processing active. This is a normal Minecraft world with lower-cost visual settings, not a 1D/monochrome render or entity-removal mode.
- Video settings can still be changed during a session. The lightweight preset is applied again at the next launch.

## Cylox adaptive lightweight UI

- Uses one consistent dark navy, cyan and orange visual system across the main menu, standard menu backgrounds, vanilla buttons and the protocol selector.
- Keeps backgrounds and controls semi-transparent so gameplay can remain visible behind supported screens; button hover and focus states become more prominent without hiding the world.
- Adapts protocol-selector row count to the available screen height and keeps the full protocol list scrollable.
- Clips long button labels to their available width and preserves original vanilla button objects, IDs and click actions.
- Caches reflective button and FontRenderer accessors, and builds the supported protocol-family list once instead of repeating reflection and route discovery every frame.
- Uses immediate-mode rectangles and Minecraft's existing font renderer; no added UI framework, texture packs, blur, shaders or animations.

## Manual Via protocol selector

- ViaVersion, ViaBackwards and ViaRewind are bundled inside **the single Cylox client JAR**.
- Open Multiplayer and tap **Select: [version]** beside the standard **Cancel** button. The modern selector groups releases as `1.8x`, `1.9x`, `1.10x` and onward; scroll with the mouse wheel or arrow controls.
- The menu shows every version family with a real translation path from native 1.8.9: 1.8x through 1.21x, plus newer major.minor families such as 26.3x when present. Every listed family uses the same Via translation pipeline.
- The selected target is saved in `cylox/protocol.properties` inside the game directory.
- Default: native Minecraft **1.8.9** (protocol 47).
- LaunchWrapper delegates the entire `io.netty.` package through the parent classloader so Minecraft's `NioSocketChannel` and Cylox's `Channel` share one class identity. Cylox verifies the channel type, then attaches Via's encoder/decoder before the initial handshake and logs the actual outgoing protocol ID.
- Protocol selection is **manual**. Cylox does not ping the server to guess its version and does not switch protocol automatically.
- Set the Via selector to the destination server's protocol before connecting. A wrong selection can cause the server to reject the handshake or can lead to compatibility issues.
- Via translates packets; it cannot guarantee that every server plugin, custom registry, or version-specific feature will behave identically.

## Scope

Included: Cylox main-menu theme, themed vanilla buttons and menu backgrounds, manual version-family selector, embedded Via protocol translation, outgoing-handshake verification, background render suppression and a small 1.8.9 LaunchWrapper adapter.

Not included: anti-AFK, auto-clicking, auto-movement, PvP modules, custom HUD, Click GUI, cosmetics, texture packs, Minecraft assets, Java runtime, renderer binaries, or base Minecraft libraries.

## Download

- [Latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Versioned releases](https://github.com/dap144p/Cylox-Clients/releases)
- [Repository](https://github.com/dap144p/Cylox-Clients)

The complete installation ZIP contains one standalone Cylox version profile and one Cylox JAR. The profile includes official 1.8.9 library and asset metadata, so a compatible launcher can resolve missing Minecraft dependencies without requiring a separately installed vanilla profile. There are no separate Via JARs.

## Install — MJ Launcher / version-folder method

1. Download the complete **Cylox Client ZIP** from Releases.
2. Open the Minecraft game directory used by MJ Launcher (the directory containing `versions/` and `libraries/`).
3. Extract the ZIP into that directory and merge the folders.
4. Refresh the version list and select **Cylox-1.8.9**.
5. Launch it. The standalone profile declares the vanilla 1.8.9 client JAR, libraries and asset index as dependencies; MJ Launcher should download missing files. You do not need to install a separate vanilla 1.8.9 profile first.
6. Keep the launcher's normal Java runtime and renderer settings.
7. Open Multiplayer, tap **Select: [version]** beside **Cancel**, then choose the target version from the menu before connecting.

Do not extract the ZIP inside `versions/`; it already contains the correct folder paths.

## Build

Requirements: JDK 17 and Gradle 8.10.2.

```bash
gradle clean test buildClientPackage verifyVanilla189Hooks -Pvanilla189Jar=/path/to/official-1.8.9.jar
```

Outputs:

- `build/libs/Cylox-Client-1.8.9-1.3.0.jar` — includes Cylox and all three Via libraries.
- `build/distributions/Cylox-Client-1.8.9-1.3.2.zip` — standalone profile plus the single Cylox JAR at its declared library path.

The CI job verifies the injection points against Mojang's official 1.8.9 client JAR. The client JAR is kept below a 20 MB ceiling; Minecraft itself, Java, renderer and launcher libraries are not bundled.

## License

GPL-3.0-or-later. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
