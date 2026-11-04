# Cylox Client — Forge 1.8.9

Cylox Client is a client-side Minecraft Forge 1.8.9 project with a compact Bedrock-inspired interface and PvP/AFK client direction. Visual identity: black base with green, blue, and orange accents.

**Developer / Owner:** dapdev  
**Minecraft:** Java Edition 1.8.9  
**Loader:** Minecraft Forge 11.15.1.2318  
**Current client version:** 0.1.0-alpha

## Download

- [**Cylox Android / MJ Launcher Installer (.jar)**](https://github.com/dap144p/Cylox-Clients/releases/download/latest/Cylox-Client-Installer.jar)
- [**Cylox Client mod (.jar)**](https://github.com/dap144p/Cylox-Clients/releases/download/latest/Cylox-Client-Forge-1.8.9-0.1.0-alpha.jar)
- [Latest releases](https://github.com/dap144p/Cylox-Clients/releases)

The installer lists published versioned releases from GitHub. A new version appears in the selector after its release is published; the installer itself does not need to be rebuilt for each client version.

## Install on MJ Launcher / MojoLauncher

1. Download `Cylox-Client-Installer.jar`.
2. Open MJ Launcher and choose **Execute A .jar**.
3. Select the installer JAR.
4. Choose a published Cylox version and the Minecraft game directory.
5. Press **Install Selected Version** and wait for the installer log to finish.
6. Select the installed Forge 1.8.9 profile in the launcher.

The installer downloads the selected Cylox mod and the official Forge 1.8.9 installer. The installer uses a Java Swing interface and requires the launcher's JAR execution environment to support Swing/AWT and the Forge installer runtime. Compatibility must be verified on the target launcher/device; a successful CI build does not prove that every Android launcher runtime can execute it.

## Manual installation

For another launcher, install Minecraft 1.8.9 + Forge 11.15.1.2318, then put the Cylox Client mod JAR in that game's `mods` directory.

## v0.1 foundation

- Cylox-styled main menu.
- Singleplayer, Multiplayer, Modules, Settings, and Vanilla Menu navigation.
- Basic module registry and ON/OFF interface.
- Forge 1.8.9 client build pipeline.

## Planned modules

Performance, Render Control, HUD Editor, AFK Tools, Alt Manager, and AutoProtocol. These are planned features; current module toggles are UI scaffolding and do not implement those systems yet.

## Build

The client uses legacy ForgeGradle 2.1 and is built with JDK 8 / Gradle 2.14.1. The installer is compiled separately as a Java 8 executable JAR. GitHub Actions builds both artifacts, publishes a versioned release, and updates the rolling `latest` release.

## Status and compatibility

- Target: Minecraft Java 1.8.9 + Forge 11.15.1.2318.
- Client-side only; no Cylox server plugin required.
- Early alpha; test alongside other mods before using an important instance.
