# Cylox Client — Background Edition

**Developer:** dapdev  
**Version:** 1.0.0  
**Minecraft:** Java 1.8.9 (base protocol 47)  
**Runtime:** launcher-provided Java, LWJGL/renderer and Minecraft assets  
**Loader:** LaunchWrapper tweaker only; no Forge or Fabric

Cylox is a minimal 1.8.9 client adapter whose only gameplay-side purpose is to reduce rendering work while the game window is inactive.

## Background behavior

- Detects whether the LWJGL display is active and visible.
- Skips the vanilla `EntityRenderer` frame when the display is inactive/hidden.
- Caps the main display loop at **5 FPS** in the background.
- Returns to normal rendering and the launcher's selected frame limit when the display becomes active again.
- Does not pause the Minecraft network stack deliberately and does not automate player actions.

This is not an Android background service. The launcher must keep the game process alive for the connection to remain open. Android may suspend or kill background applications, and the JAR cannot override that operating-system or launcher policy. A real MJ Launcher/device test is required before treating background connectivity as guaranteed.

## Scope

Included: automatic background render suppression and a small 1.8.9 LaunchWrapper adapter.

Not included: anti-AFK, auto-clicking, auto-movement, PvP modules, HUD, Click GUI, cosmetics, texture packs, Minecraft assets, Java runtime, renderer binaries, or base Minecraft libraries.

## Newer server versions

The optional protocol bridge performs a status ping and uses ViaVersion's client-side pipeline for the tested protocol catalogue **1.9 through 1.13.2**. Native 1.8.x servers use protocol 47 directly. Unknown protocols fall back to native 1.8.x; they are not claimed to be compatible. Custom server plugins and registries can still cause incompatibilities.

The three Via libraries are installed separately from the Cylox client JAR. The adapter JAR itself does not embed them.

## Download

- [Latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Versioned releases](https://github.com/dap144p/Cylox-Clients/releases)
- [Repository](https://github.com/dap144p/Cylox-Clients)

Use the ZIP for a version-folder launcher installation. The JAR asset is the Cylox adapter only.

## Install — MJ Launcher / version-folder method

1. Install Minecraft Java **1.8.9** once in MJ Launcher.
2. Download the complete **Background Client ZIP** from Releases.
3. Open the same Minecraft game directory used by MJ Launcher (the directory containing `versions/` and `libraries/`).
4. Extract the ZIP into that directory and merge the folders.
5. Refresh the version list and select **Cylox-1.8.9**.
6. Keep the launcher's normal Java runtime and renderer selection.

Do not extract the ZIP inside `versions/`; it already contains the correct folder paths.

## Build

Requirements: JDK 8+ and Gradle 8.10.2.

```bash
gradle clean test buildClientPackage verifyVanilla189Hooks -Pvanilla189Jar=/path/to/official-1.8.9.jar
```

Outputs:

- `build/libs/Cylox-Client-1.8.9-1.0.0.jar`
- `build/distributions/Cylox-Background-Client-1.8.9-1.0.0.zip`

The CI job verifies the bytecode injection points against Mojang's official 1.8.9 client JAR and enforces a 5 MB maximum for the Cylox adapter JAR. The total install ZIP is larger because it includes the external protocol-translation libraries.

## License

GPL-3.0-or-later. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
