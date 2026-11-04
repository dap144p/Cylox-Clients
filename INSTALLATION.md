# Cylox Client — Installation & Usage

**Developer:** dapdev  
**Client:** Cylox Client 0.2.0-alpha  
**Minecraft:** Java Edition 1.8.9  
**Runtime:** Vanilla-based; no Forge/Fabric runtime

## Install using the version-folder package

1. Open MJ Launcher / MojoLauncher and confirm the Minecraft game directory used by the instance.
2. Make sure Minecraft Java 1.8.9 is available in that directory. Launching vanilla 1.8.9 once is recommended so the launcher has downloaded its base files.
3. Download **Cylox-Client-Vanilla-1.8.9-<version>.zip** from [GitHub Releases](https://github.com/dap144p/Cylox-Clients/releases).
4. Extract the ZIP into the Minecraft game directory, merging the included **versions/** and **libraries/** folders.
5. Return to the version selector and choose **Cylox-1.8.9**.
6. Launch. On first start, the launcher may download LaunchWrapper and ASM dependencies.

Do not rename the version folder or JSON file. They must remain:

\`\`\`text
versions/Cylox-1.8.9/Cylox-1.8.9.json
\`\`\`

The Cylox runtime library must remain in the included **libraries/dev/cylox/** path.

## What is installed

- A launcher version profile that inherits the official Minecraft 1.8.9 base version.
- The Cylox Client Core library.
- LaunchWrapper and ASM are declared as runtime dependencies and are downloaded by the launcher if absent.

The original Minecraft client JAR is not included in the Cylox package. It is supplied by the user's own vanilla installation.

## Current alpha limitations

This release validates the vanilla bootstrap and class-transformer pipeline. It does not yet contain the custom Bedrock-inspired main menu, PvP modules, performance toggles, HUD editor, or other planned in-game UI/features.

The JAR installer is deferred. This release uses a ZIP that is extracted into the Minecraft game directory.

## Troubleshooting

- **Cylox-1.8.9 is not listed:** check that the version profile JSON exists inside the launcher's actual game directory.
- **Missing parent version:** install or launch vanilla Minecraft 1.8.9 once, then try the Cylox profile again.
- **Missing library:** ensure the ZIP's libraries folder was merged into the game directory and allow the launcher to download required dependencies.
- **Launch fails on Android:** save the launcher log and report the error with the MJ Launcher / MojoLauncher version. CI success does not guarantee runtime compatibility on every Android build.
