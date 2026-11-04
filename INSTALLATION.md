# Cylox Client — Installation & Usage

**Developer:** dapdev  
**Client:** Cylox Client 0.2.1-alpha  
**Minecraft:** Java Edition 1.8.9  
**Runtime:** Cylox client core on the vanilla 1.8.9 base; Forge/Fabric are not required

## Install using the one-folder package

1. Open MJ Launcher / MojoLauncher and confirm the Minecraft game directory used by the instance.
2. Make sure Minecraft Java 1.8.9 is installed in that directory. Launch vanilla 1.8.9 once if its base files have not been downloaded.
3. Download **Cylox-Client-Vanilla-1.8.9-<version>.zip** from [GitHub Releases](https://github.com/dap144p/Cylox-Clients/releases).
4. Extract the ZIP and copy the single **Cylox-1.8.9** folder into the launcher's Minecraft `versions/` directory.
5. Select **Cylox-1.8.9** in the version selector.
6. Launch and allow the launcher to resolve dependencies on first start.

The installed structure must be:

```text
versions/Cylox-1.8.9/Cylox-1.8.9.json
```

Do not rename the folder or JSON file.

## Dependency handling

The version JSON declares LaunchWrapper, ASM, and the Cylox Client Core artifact. A compatible launcher downloads these into its normal library cache. The ZIP itself contains only the one version folder; no separate top-level `libraries/` folder needs to be copied manually.

The original Minecraft 1.8.9 client JAR is not included. It comes from the user's own vanilla installation.

## Current development status

This is an early client foundation, not a finished CMCLIENT-equivalent feature set. The current build provides the launch profile and Cylox bootstrap/core foundation. Custom PvP HUD, module system, rendering optimizations, menu, and additional client features remain under development.

The JAR installer is deferred. The current distribution is a ZIP with one folder for `versions/`.

## Troubleshooting

- **Cylox-1.8.9 is not listed:** verify the JSON is inside the launcher's actual game directory under `versions/Cylox-1.8.9/`.
- **Missing parent version:** install or launch vanilla Minecraft 1.8.9 once.
- **Missing library:** check the launcher network/download log and confirm it supports legacy version metadata with artifact downloads.
- **Launch fails on Android:** save the complete launcher log and note the MJ Launcher / MojoLauncher version. CI success does not guarantee runtime compatibility on every Android build.
