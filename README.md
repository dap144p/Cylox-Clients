# Cylox Client — Forge 1.8.9

Cylox Client is a client-side Minecraft Forge 1.8.9 project focused on a compact Bedrock-inspired interface with PvP, AFK, and alt-client workflows. Its visual identity uses black as the base with green, blue, and orange accents.

**Developer / Owner:** dapdev  
**Minecraft:** Java Edition 1.8.9  
**Loader:** Minecraft Forge 11.15.1.2318  
**Current version:** 0.1.0-alpha

## Download

[**⬇ Download latest Cylox Client JAR**](https://github.com/dap144p/Cylox-Clients/releases/download/latest/Cylox-Client-latest-Forge-1.8.9.jar)

This direct-download link is kept stable. Every successful build on `main` updates the `latest` GitHub Release and replaces its JAR with the newest build.

## Quick links
- [Installation & usage guide](INSTALLATION.md)
- [Source repository](https://github.com/dap144p/Cylox-Clients)
- [GitHub Actions build history](https://github.com/dap144p/Cylox-Clients/actions)
- [Latest release page](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Forge 1.8.9 download](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html)

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

GitHub Actions builds the client on relevant pushes to `main`/ `master` and on manual dispatch. Each successful build uploads a versioned workflow artifact and updates the rolling `latest` release JAR.

## Compatibility and status
- Target: Minecraft 1.8.9 + Forge 11.15.1.2318.
- Client-side mod; no Cylox server plugin required.
- This is an early foundation, not a finished PvP/AFK client.
- Test alongside other mods before relying on it in a live instance.
