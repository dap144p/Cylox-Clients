# Cylox Client — 1.8.9

Custom vanilla-based Minecraft 1.8.9 client project. The target game does **not** use Forge or Fabric.

## Current snapshot: UI + runtime hook source 0.3.0

- Hybrid Click GUI with categories, module list, settings, search, keybind editor, and GUI preferences.
- Vanilla GuiScreen wrapper and Minecraft GUI/font renderer adapter.
- LaunchWrapper ITweaker entry point.
- ASM transformer that injects the Right Shift key poll into the client tick.
- Right Shift opens/closes the Click GUI.
- Java 8 source compatibility.

**The sample modules remain UI previews only.** Their toggles change local UI state; they do not implement gameplay features.

## Download

- [Download source ZIP](https://github.com/dap144p/Cylox-Clients/archive/refs/heads/main.zip)
- [Open repository](https://github.com/dap144p/Cylox-Clients)

There is still no installable Minecraft client JAR in this snapshot. The adapter is compiled against MCP-named Minecraft classes and requires verified reobfuscation for the normal vanilla 1.8.9 runtime. Runtime integration has not yet been tested inside MJ Launcher.

## Build the UI core

Requirements: JDK 8 or newer and Gradle 7.x.

    gradle clean build

Output:

    build/libs/Cylox-UI-Core-0.3.0.jar

This is the standalone UI-core library, not a Minecraft client.

## Build the 1.8.9 adapter source

Provide an MCP-named/deobfuscated 1.8.9 client JAR at libs/minecraft-1.8.9-mcp.jar, or pass its path:

    gradle minecraft189AdapterJar -Pmc189Jar=/absolute/path/to/mcp-1.8.9-client.jar

The adapter task also needs the small LaunchWrapper/ASM/LWJGL compile dependencies. They are compile-time inputs and are not bundled into the adapter output.

## Runtime details

See [src/minecraft189/README.md](src/minecraft189/README.md) for the LaunchWrapper profile requirements and the remaining reobfuscation/test work.

## Attribution

Cylox Client — Developer: **dapdev**.
