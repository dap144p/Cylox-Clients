# Cylox Client v0.1.0-alpha — Installation & Usage

**Developer / Owner:** dapdev

## 1. What is this?
Cylox Client is a **client-side mod** for Minecraft Java Edition 1.8.9 using Minecraft Forge. It changes the main menu to a Cylox-themed interface and provides an early module/settings screen.

It does not replace Minecraft, does not install a server plugin, and does not make a 1.8.9 client natively run newer Minecraft versions.

## 2. Download
- **Latest JAR (direct download):** https://github.com/dap144p/Cylox-Clients/releases/download/latest/Cylox-Client-latest-Forge-1.8.9.jar
- Latest release page: https://github.com/dap144p/Cylox-Clients/releases/tag/latest
- Repository/source code: https://github.com/dap144p/Cylox-Clients
- Build history and versioned workflow artifacts: https://github.com/dap144p/Cylox-Clients/actions
- Forge 1.8.9 files: https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html

The direct JAR link always targets the rolling `latest` release. A successful GitHub Actions build updates that release automatically.

## 3. Requirements
- Minecraft Java Edition **1.8.9**.
- Forge **11.15.1.2318** for Minecraft 1.8.9.
- Java 8 runtime is recommended for this legacy Minecraft/Forge setup.
- A launcher that supports Forge 1.8.9 and custom mods.

This build does not bundle Minecraft, Forge, Java, ViaForge, or any other third-party mod.

## 4. Where to install the JAR
Install the downloaded JAR into the **mods folder of the Minecraft 1.8.9 Forge instance** you want to use.

Typical locations:
- Windows: `%AppData%\\.minecraft\\mods\\`
- Linux: `~/.minecraft/mods/`
- macOS: `~/Library/Application Support/minecraft/mods/`
- Android launchers: open the selected 1.8.9 Forge instance's game directory, then its `mods` folder. The exact path depends on the launcher and instance configuration.

If the `mods` folder does not exist, create it. Do not put the JAR in the resourcepacks, shaderpacks, versions, or config folder.

## 5. How to open Cylox
1. Select the Minecraft 1.8.9 Forge profile/instance.
2. Start the game.
3. At the main menu, the Cylox main menu should appear.
4. Select **Singleplayer** or **Multiplayer** to open those screens.
5. Select **Modules** to view the current module list.
6. Select **Settings** to open the current settings placeholder.
7. Select **Vanilla Menu** to return to the normal Minecraft main menu.

## 6. What works in v0.1.0-alpha
- Forge 1.8.9 mod entry point and client-only metadata.
- Cylox-themed main menu with black base and green, blue, and orange accents.
- Navigation to Singleplayer, Multiplayer, Modules, Settings, and Vanilla Menu.
- Basic module list and ON/OFF state toggles in the current GUI.

## 7. What is not implemented yet
The following are planned, not functional features in this alpha:
- AutoProtocol detection and protocol translation.
- ViaForge bridge/integration.
- Performance optimizations.
- Render Control effects.
- HUD editor and custom HUD elements.
- AFK utilities.
- Alt/account manager.
- Persistent module settings across restarts.

The current module toggles are UI scaffolding only. Turning a module ON does not activate its advertised feature yet. AutoProtocol requires a separately compatible ViaForge integration; this JAR does not include ViaForge.

## 8. Troubleshooting
- **Cylox menu does not appear:** confirm the game is actually running Minecraft 1.8.9 with Forge 11.15.1.2318 and that the JAR is in that instance's `mods` folder.
- **Game crashes on startup:** temporarily remove other recently added mods and inspect the Forge crash report/log. Check that the selected instance is 1.8.9, not another version.
- **No JAR at the latest link:** check the [latest release page](https://github.com/dap144p/Cylox-Clients/releases/tag/latest) or [Actions build history](https://github.com/dap144p/Cylox-Clients/actions). The release is updated only after a successful build.
- **Android launcher:** use the instance-specific game directory. Do not assume the desktop `.minecraft/mods` path exists on Android.

## 9. Project links
- Source repository: https://github.com/dap144p/Cylox-Clients
- Latest JAR: https://github.com/dap144p/Cylox-Clients/releases/download/latest/Cylox-Client-latest-Forge-1.8.9.jar
- Latest release page: https://github.com/dap144p/Cylox-Clients/releases/tag/latest
- GitHub Actions builds: https://github.com/dap144p/Cylox-Clients/actions
- Forge 1.8.9: https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html
- Minecraft Forge documentation: https://docs.minecraftforge.net/

## 10. Disclaimer
This is an early alpha build. A successful compile confirms that the source compiled, but it does not prove compatibility with every modpack, launcher, server, or device. Back up the instance before testing. Use server features in accordance with each server's rules.
