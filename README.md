# Cylox Client — 1.8.9

Custom vanilla-based Minecraft 1.8.9 client project. The target runtime does **not** use Forge or Fabric.

## Current snapshot: UI Core 0.2.0

This snapshot adds the first Minecraft 1.8.9 screen adapter source alongside the reusable UI core:

- Hybrid Click GUI: category sidebar, module list, and selected-module settings panel.
- Search field, module toggles, keybind editing, draggable window, and GUI preferences.
- Boolean, number, and mode setting controls.
- Vanilla GuiScreen wrapper and FontRenderer/Gui rendering adapter in the optional minecraft189 source set.
- Mouse, drag, release, and keyboard events forwarded from the Minecraft screen.
- Java 8 source compatibility.

**The sample modules are still UI previews only.** Their toggles change local UI state; they do not implement gameplay features.

## Download

- [Download source ZIP](https://github.com/dap144p/Cylox-Clients/archive/refs/heads/main.zip)
- [Open repository](https://github.com/dap144p/Cylox-Clients)

There is still no launcher-installable Minecraft client JAR in this snapshot. The adapter source is not yet remapped for the obfuscated vanilla runtime, and no LaunchWrapper/tweaker hotkey has been registered.

## Build the UI core

Requirements: JDK 8 or newer and Gradle 7.x.

    gradle clean build

The core library is created at:

    build/libs/Cylox-UI-Core-0.2.0.jar

This is a UI library, not a standalone Minecraft client or launcher installer.

## Compile the optional Minecraft 1.8.9 adapter

The adapter source set requires an MCP-named/deobfuscated Minecraft 1.8.9 client JAR:

    gradle minecraft189AdapterJar -Pmc189Jar=/absolute/path/to/mcp-1.8.9-client.jar

This dependency is compile-time only and is not bundled into the output. Details and the remaining runtime limitation are documented in [src/minecraft189/README.md](src/minecraft189/README.md).

## Attribution

Cylox Client — Developer: **dapdev**.
