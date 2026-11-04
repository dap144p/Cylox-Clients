# Cylox Client — Vanilla 1.8.9

Cylox Client is a custom Minecraft Java Edition client project targeting **1.8.9**. The runtime is being migrated away from Forge/Fabric to a vanilla-based bootstrap and client-owned bytecode hooks.

**Developer / Owner:** dapdev  
**Minecraft:** Java Edition 1.8.9  
**Client name:** Cylox Client  
**Current project version:** 0.2.0-alpha  
**Runtime:** Vanilla 1.8.9 + Cylox bootstrap (no Forge/Fabric runtime)

## Download

- [Latest Cylox Client releases](https://github.com/dap144p/Cylox-Clients/releases)
- The vanilla package is published as **Cylox-Client-Vanilla-1.8.9-<version>.zip**.
- The core library is published as **Cylox-Client-Core-<version>.jar**.

The installer JAR is intentionally deferred. The current distribution is a ZIP containing the version metadata and Cylox-owned runtime library.

## Install on MJ Launcher / MojoLauncher

1. Make sure Minecraft Java 1.8.9 is available in the launcher's game directory. The launcher can download the official base files when needed.
2. Download the latest vanilla package ZIP from GitHub Releases.
3. Extract the ZIP into the same Minecraft game directory used by MJ Launcher / MojoLauncher.
4. Keep the included **versions/** and **libraries/** folders in their original locations.
5. Select the **Cylox-1.8.9** version profile.
6. Launch the game. The launcher may download LaunchWrapper and ASM libraries on the first run.

Example package structure:

\`\`\`text
.minecraft/
├── versions/
│   └── Cylox-1.8.9/
│       └── Cylox-1.8.9.json
└── libraries/
    └── dev/cylox/cylox-client-core/<version>/
        └── cylox-client-core-<version>.jar
\`\`\`

The exact Minecraft game-directory path depends on the launcher and instance configuration. Do not extract this package into Android's app data folder unless that is the actual game directory selected by the launcher.

## What this alpha contains

- Vanilla 1.8.9 version profile using **inheritsFrom**.
- Cylox-owned LaunchWrapper tweaker and bytecode bootstrap hook.
- Startup verification that confirms the Cylox runtime is loaded.
- Automated unit test for the main-class transformer.
- No Forge/FML or Fabric runtime dependency.

**Important status:** this is the vanilla runtime foundation, not the finished PvP client UI. The Bedrock-inspired main menu, module interface, HUD, performance controls, and other gameplay features are still under development and are not included as working features in this alpha.

## Copyright and base game files

The package does not include or redistribute Minecraft's original client JAR. The launcher uses the user's own official Minecraft 1.8.9 installation. You must have a legitimate Minecraft Java Edition installation and use the launcher with your own account.

## Build

Requirements: JDK 8 and Gradle 8.10.2 or compatible.

\`\`\`bash
gradle clean test buildClientPackage
\`\`\`

Build outputs:

- **build/libs/Cylox-Client-Core-<version>.jar**
- **build/distributions/Cylox-Client-Vanilla-1.8.9-<version>.zip**

GitHub Actions tests the transformer and publishes versioned and rolling latest releases.

## Compatibility notes

The version metadata follows the legacy 1.8.9 launcher format. MJ Launcher / MojoLauncher compatibility still needs verification on the target Android device and launcher build. A successful GitHub Actions build validates compilation and packaging; it does not by itself prove that every Android launcher can launch the profile.
