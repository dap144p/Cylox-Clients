# Cylox Client — Forge 1.8.9

Cylox Client is a client-side Minecraft Forge 1.8.9 project focused on a compact Bedrock-inspired interface with PvP, AFK, and alt-client workflows. Its visual identity uses black as the base with green, blue, and orange accents.

## v0.1 foundation
- Replaces the standard main menu with a Cylox-styled menu.
- Adds Singleplayer, Multiplayer, Modules, Settings, and Vanilla Menu navigation.
- Adds a basic module registry and ON/OFF interface.
- Establishes the Forge 1.8.9 build pipeline.

## Planned modules
Performance, Render Control, HUD Editor, AFK Tools, Alt Manager, and AutoProtocol. AutoProtocol is currently a planned integration point; protocol translation will require a compatible ViaForge build and a separate integration/testing pass. The module toggle in this alpha is only a UI scaffold and does not yet implement those systems.

## Build
This is a legacy ForgeGradle 2.1 project. Build with JDK 8 and Gradle 2.14.1:

```bash
gradle setupDecompWorkspace build --no-daemon
```

GitHub Actions runs the same build on pushes to `main`/`master` and manual dispatch. The JAR is uploaded as the `Cylox-Client-Forge-1.8.9` workflow artifact.

## Compatibility and status
- Target: Minecraft 1.8.9 + Forge 11.15.1.2318.
- Client-side mod; no Cylox server plugin required.
- This is an early foundation, not a finished PvP/AFK client.
- Test alongside other mods before relying on it in a live instance.
