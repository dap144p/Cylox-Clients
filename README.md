# Cylox Client — Background Edition

**Developer:** dapdev  
**Version:** 1.0.6  
**Minecraft:** Java 1.8.9 (base protocol 47)  
**Runtime:** MJ Launcher-provided Java and renderer; Minecraft 1.8.9 client, libraries and assets resolved from version metadata  
**Loader:** LaunchWrapper tweaker only; no Forge or Fabric

Cylox is a minimal 1.8.9 client adapter. Its gameplay-side purpose is to reduce rendering work while the game window is inactive. Its protocol compatibility features are embedded in the same Cylox JAR.

## Background behavior

- Detects whether the LWJGL display is active and visible.
- Skips the vanilla `EntityRenderer` frame when the display is inactive/hidden.
- Caps the main display loop at **5 FPS** in the background.
- Returns to normal rendering and the launcher's selected frame limit when the display becomes active again.
- Does not pause the Minecraft network stack deliberately and does not automate player actions.

This is not an Android background service. The launcher must keep the game process alive for the connection to remain open. Android may suspend or kill background applications, and the JAR cannot override that operating-system or launcher policy. A real MJ Launcher/device test is required before treating background connectivity as guaranteed.

## Manual Via protocol selector

- ViaVersion, ViaBackwards and ViaRewind are bundled inside **the single Cylox client JAR**.
- Open Multiplayer and tap **Select: [version]** immediately to the right of the standard **Cancel** button. Cylox leaves the vanilla Cancel button and the other Multiplayer controls at their original coordinates.
- A menu opens above the selector with supported target versions. Choose the exact protocol version required by the destination server; use the page controls for additional versions.
- The selected target is saved in `cylox/protocol.properties` inside the game directory.
- Default: native Minecraft **1.8.9** (protocol 47).
- Cylox attaches the Via encoder/decoder during vanilla Netty channel initialization, before the first handshake packet is sent. The log reports the target protocol that was attached.
- Protocol selection is **manual**. Cylox does not ping the server to guess its version and does not switch protocol automatically.
- Set the Via selector to the destination server's protocol before connecting. A wrong selection can cause the server to reject the handshake or can lead to compatibility issues.
- Via translates packets; it cannot guarantee that every server plugin, custom registry, or version-specific feature will behave identically.

## Scope

Included: manual protocol selection menu, embedded Via protocol bridge, background render suppression and a small 1.8.9 LaunchWrapper adapter.

Not included: anti-AFK, auto-clicking, auto-movement, PvP modules, HUD, Click GUI, cosmetics, texture packs, Minecraft assets, Java runtime, renderer binaries, or base Minecraft libraries.

## Download

- [Latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Versioned releases](https://github.com/dap144p/Cylox-Clients/releases)
- [Repository](https://github.com/dap144p/Cylox-Clients)

The complete installation ZIP contains one standalone Cylox version profile and one Cylox JAR. The profile includes official 1.8.9 library and asset metadata, so a compatible launcher can resolve missing Minecraft dependencies without requiring a separately installed vanilla profile. There are no separate Via JARs.

## Install — MJ Launcher / version-folder method

1. Download the complete **Background Client ZIP** from Releases.
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

- `build/libs/Cylox-Client-1.8.9-1.0.6.jar` — includes Cylox and all three Via libraries.
- `build/distributions/Cylox-Background-Client-1.8.9-1.0.6.zip` — standalone profile plus the single Cylox JAR at its declared library path.

The CI job verifies the injection points against Mojang's official 1.8.9 client JAR. The client JAR is kept below a 20 MB ceiling; Minecraft itself, Java, renderer and launcher libraries are not bundled.

## License

GPL-3.0-or-later. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
