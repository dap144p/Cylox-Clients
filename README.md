# Cylox Client — 1.8.9

Custom vanilla-based Minecraft 1.8.9 client project. The project is intended to use its own client core and does **not** depend on Forge or Fabric.

## Current snapshot: UI Core 0.1.0

This snapshot establishes the first in-game UI layer:

- Hybrid Click GUI layout: category sidebar, module list, and selected-module settings panel.
- Search field, module toggles, keybind editing, and draggable window.
- Boolean, number, and mode setting controls.
- HUD category and lightweight renderer/input interfaces.
- Global GUI settings for window scale, panel opacity, and compact rows.
- Java 8 source compatibility.

The sample modules are **UI previews only**. Their toggles change local UI state; they do not implement gameplay features yet.

## Download

- [Download source ZIP](https://github.com/dap144p/Cylox-Clients/archive/refs/heads/main.zip)
- [Open repository](https://github.com/dap144p/Cylox-Clients)

There is no installable Minecraft client JAR in this snapshot. The UI core still needs to be connected to the 1.8.9 runtime/render hooks before it can be installed as a client version. A source ZIP is provided here so the current work can be inspected and built.

## Build

Requirements: JDK 8 or newer and Gradle 7.x.

```bash
gradle clean build
```

The library output is created at:

```text
build/libs/Cylox-UI-Core-0.1.0.jar
```

This JAR is a UI library, **not** a standalone Minecraft client or launcher installer.

## Integration contract

The game-side adapter should implement `dev.cylox.client.ui.UiRenderer` and forward Minecraft/LWJGL input to `ClickGui`:

1. Call `ClickGui.draw(...)` during the 2D GUI render pass.
2. Forward mouse click, drag, and release events.
3. Forward typed characters and key codes to `ClickGui.keyTyped(...)`.
4. Open/close the screen through the custom Cylox runtime hook.

No Minecraft, Forge, Fabric, or OptiFine classes are referenced by this UI-core module.

## Attribution

Cylox Client — Developer: **dapdev**.
