# Cylox Client — Installation & Usage

**Developer:** dapdev  
**Client:** Cylox Client 0.1.0-alpha  
**Minecraft:** Java Edition 1.8.9  
**Forge:** 11.15.1.2318

## MJ Launcher / MojoLauncher installer

1. Download [Cylox-Client-Installer.jar](https://github.com/dap144p/Cylox-Clients/releases/download/latest/Cylox-Client-Installer.jar).
2. Open MJ Launcher and select **Execute A .jar**.
3. Choose the downloaded installer.
4. Select the Cylox version from the release list.
5. Set the Minecraft game directory. It must be the actual game directory used by this launcher.
6. Press **Install Selected Version** and wait for the completion message.
7. Select the Forge 1.8.9 profile in MJ Launcher.

The installer fetches versioned client JARs from the public GitHub Releases API and downloads the official Forge 1.8.9 installer. The first published version is 0.1.0-alpha. New versions appear in the selector after a versioned GitHub Release is published.

### Runtime compatibility note

The installer is a Java 8 JAR with a Swing/AWT interface. MJ Launcher's **Execute A .jar** feature is designed to run compatible Java installers, but actual support for this custom installer, Swing dialogs, the embedded Forge installer invocation, and Android filesystem permissions must be tested on the target MJ Launcher build. If the installer UI does not open or Forge installation fails, use the manual method below and report the installer log.

## Manual installation (other launchers)

1. Install Minecraft Java 1.8.9.
2. Install Forge 11.15.1.2318 for that game directory.
3. Launch the Forge profile once, then close the game.
4. Put the downloaded Cylox Client mod JAR in the same game directory's `mods` folder.
5. Select the Forge 1.8.9 profile and launch.

Download the [latest Cylox Client mod JAR](https://github.com/dap144p/Cylox-Clients/releases/download/latest/Cylox-Client-Forge-1.8.9-0.1.0-alpha.jar).

## Installer behavior

- Lists published versioned releases from GitHub.
- Downloads the selected client mod JAR.
- Attempts to install official Forge 1.8.9 into the selected game directory.
- Shows progress and errors in the installer log.
- Does not delete existing mods or game files.

## Current client limitations

This is an early alpha foundation. Main menu navigation and module toggles exist, but module toggles are currently UI placeholders. Performance, Render Control, HUD Editor, AFK Tools, Alt Manager, and AutoProtocol are planned and are not yet implemented.
