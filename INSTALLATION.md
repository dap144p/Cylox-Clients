# Cylox Client — Installation & Usage

**Developer:** dapdev  
**Client:** Cylox Client 0.2.2-alpha  
**Minecraft:** Java Edition 1.8.9  
**Runtime:** Cylox client core on the vanilla 1.8.9 base; Forge/Fabric are not required

## Install using the one-folder package

1. Download **Cylox-Client-Vanilla-1.8.9-<version>.zip** from [GitHub Releases](https://github.com/dap144p/Cylox-Clients/releases).
2. Extract the ZIP.
3. Copy the single **Cylox-1.8.9** folder into the launcher's Minecraft `versions/` directory.
4. Select **Cylox-1.8.9** in the version selector and launch.
5. On first launch, allow the launcher to download the inherited Minecraft 1.8.9 base and Cylox dependencies. You should not need to install Vanilla 1.8.9 manually if the launcher supports inherited versions.

Expected structure:

```text
versions/Cylox-1.8.9/Cylox-1.8.9.json
```

Do not rename the folder or JSON file.

## Automatic dependency handling

The version JSON declares `inheritsFrom: "1.8.9"`. A compatible launcher merges the official Minecraft 1.8.9 metadata and downloads the original game JAR, libraries, and assets into its normal game directory/cache when missing. The JSON also declares LaunchWrapper, ASM, and the Cylox Client Core artifact, which the launcher resolves into its normal library cache.

The Cylox ZIP contains only the one version folder. It does not include Mojang's original client JAR and does not require manually copying a separate `libraries/` folder.

## Current development status

This is an early client foundation, not a finished CMCLIENT-equivalent feature set. The current build provides the launch profile and Cylox bootstrap/core foundation. Custom PvP HUD, module system, rendering optimizations, menu, and additional client features remain under development.

The JAR installer is deferred. The current distribution is a ZIP with one folder for `versions/`.

## Troubleshooting

- **Cylox-1.8.9 is not listed:** verify the JSON is inside the launcher's actual game directory under `versions/Cylox-1.8.9/`.
- **Missing parent version / missing inherited client JAR:** the launcher may not support automatic resolution of inherited legacy versions. Check that it supports Minecraft 1.8.9 version inheritance and retry its download/repair function.
- **Missing Cylox library:** check the launcher network/download log and confirm it supports artifact downloads declared in version metadata.
- **Launch fails on Android:** save the complete launcher log and note the MJ Launcher / MojoLauncher version. CI success does not guarantee runtime compatibility on every Android build.
