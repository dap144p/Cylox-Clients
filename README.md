# Cylox Client — Minecraft Java 1.8.9

Cylox Client is an independent Minecraft Java Edition client project targeting **1.8.9**, inspired by integrated PvP clients such as CMCLIENT. The goal is to build Cylox-owned client systems, modules, rendering optimizations, HUD, menus, and configuration rather than distribute a loose collection of user-installed mods.

**Developer / Owner:** dapdev  
**Minecraft:** Java Edition 1.8.9  
**Client name:** Cylox Client  
**Current project version:** 0.2.1-alpha  
**Architecture:** Cylox client core + vanilla 1.8.9 base; no required Forge/Fabric runtime

## Download

- [Latest Cylox Client releases](https://github.com/dap144p/Cylox-Clients/releases)
- Package: **Cylox-Client-Vanilla-1.8.9-<version>.zip**
- Core library: **Cylox-Client-Core-<version>.jar**

The installer JAR is intentionally deferred. The ZIP contains one version folder for direct placement in the Minecraft `versions/` directory. The launcher resolves declared runtime libraries from the version metadata.

## Install on MJ Launcher / MojoLauncher

1. Make sure Minecraft Java 1.8.9 is installed in the launcher's game directory. Launch vanilla 1.8.9 once if the base files are not present.
2. Download the latest package ZIP from [GitHub Releases](https://github.com/dap144p/Cylox-Clients/releases).
3. Extract the ZIP.
4. Copy the single **Cylox-1.8.9** folder into the launcher's Minecraft `versions/` directory.
5. Select **Cylox-1.8.9** in the version list and launch.
6. Allow the launcher to download declared dependencies on first launch.

Expected structure:

```text
.minecraft/
└── versions/
    └── Cylox-1.8.9/
        └── Cylox-1.8.9.json
```

The Cylox core and required libraries are declared in the version metadata and are resolved by compatible launchers into their normal library cache. The ZIP does not require manually copying a separate top-level `libraries/` directory.

The exact game-directory path depends on the launcher and instance configuration. Use the game directory selected by the launcher; do not assume Android's app-data path is the active game directory.

## Development status

This is an early development build. The current implementation establishes the launch profile, Cylox LaunchWrapper tweaker, core library, and bytecode bootstrap/test foundation. It is **not yet a feature-complete PvP client**.

Planned client-owned systems include:
- Custom PvP HUD and HUD configuration.
- Toggleable client modules and persistent settings.
- Rendering and performance work for entities, particles, chunks, and HUD.
- Custom main menu and client settings.
- Additional PvP utilities and cosmetic systems.

These are development targets, not claims that they are already implemented. The visual direction is a dedicated Cylox client UI; the previously discussed Bedrock-style UI direction is not part of the plan.

## Copyright and base game files

The package does not include or redistribute Minecraft's original client JAR. The launcher uses the user's own official Minecraft 1.8.9 installation. Use a legitimate Minecraft Java Edition installation and your own account.

## Build

Requirements: JDK 8 and Gradle 8.10.2 or compatible.

```bash
gradle clean test buildClientPackage
```

Build outputs:
- `build/libs/Cylox-Client-Core-<version>.jar`
- `build/distributions/Cylox-Client-Vanilla-1.8.9-<version>.zip`

GitHub Actions runs tests, packages the version folder, and publishes versioned and rolling latest releases.

## Compatibility notes

The version metadata uses the legacy 1.8.9 launcher format. MJ Launcher / MojoLauncher compatibility must still be verified on the target Android device and launcher build. CI success confirms the build and tests, not successful runtime execution on every launcher.
