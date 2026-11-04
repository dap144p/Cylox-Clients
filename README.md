# Cylox Client — Minecraft Java 1.8.9

Cylox Client is an independent Minecraft Java Edition client project targeting **1.8.9**, inspired by integrated PvP clients such as CMCLIENT. The goal is to build Cylox-owned client systems, modules, rendering optimizations, HUD, menus, and configuration rather than distribute a loose collection of user-installed mods.

**Developer / Owner:** dapdev  
**Minecraft:** Java Edition 1.8.9  
**Client name:** Cylox Client  
**Current project version:** 0.2.3-alpha  
**Architecture:** Cylox client core + vanilla 1.8.9 base; no required Forge/Fabric runtime

## Download

- [Latest Cylox Client releases](https://github.com/dap144p/Cylox-Clients/releases)
- Package: **Cylox-Client-Vanilla-1.8.9-<version>.zip**
- Core library: **Cylox-Client-Core-<version>.jar**

The installer JAR is intentionally deferred. The ZIP contains one version folder for direct placement in the Minecraft `versions/` directory.

## Install on MJ Launcher / MojoLauncher

1. Download the latest package ZIP from [GitHub Releases](https://github.com/dap144p/Cylox-Clients/releases).
2. Extract the ZIP.
3. Copy the single **Cylox-1.8.9** folder into the launcher's Minecraft `versions/` directory.
4. Select **Cylox-1.8.9** and launch.
5. On first launch only, allow the launcher to fetch any missing Minecraft 1.8.9 files and the Cylox core library. The launch profile explicitly reuses the **1.8.9 game JAR** through `"jar": "1.8.9"`.

A compatible launcher stores downloaded game files in its normal game-directory cache. On later launches, it should reuse files that are already present and valid instead of downloading them again. A new download is expected only when files are missing, damaged, the cache was cleared, or a different game directory is selected. The launcher may separately manage its Java runtime.

Expected structure:

```text
.minecraft/
└── versions/
    └── Cylox-1.8.9/
        └── Cylox-1.8.9.json
```

The version metadata uses `inheritsFrom: "1.8.9"` and `jar: "1.8.9"`. Compatible launchers merge the official 1.8.9 metadata and reuse its client JAR, libraries, and assets from the launcher's cache. The Cylox profile adds only the Cylox Core library; LaunchWrapper and ASM are inherited from Vanilla 1.8.9 rather than declared a second time. Downloads are handled by the launcher and cached in the selected game directory; the ZIP does not redistribute Minecraft's original client JAR.

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

The package does not include or redistribute Minecraft's original client JAR. The launcher downloads the official 1.8.9 game files from Mojang's metadata/download service. Use a legitimate Minecraft Java Edition installation and your own account.

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

Automatic parent-version resolution depends on the launcher correctly supporting legacy 1.8.9 version metadata and `inheritsFrom`. MJ Launcher / MojoLauncher behavior must still be verified on the target Android device and launcher build. CI success confirms the build and tests, not successful runtime execution on every launcher.
