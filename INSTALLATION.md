CYLOX CLIENT 1.8.9 — INSTALLATION
Developer: dapdev
Version: 0.10.0

INSTALL
1. Install vanilla Minecraft Java 1.8.9 once in your launcher.
2. Open the Minecraft game directory configured in that launcher (the directory containing versions and libraries).
3. Extract this ZIP into that game directory. Merge the versions and libraries folders.
4. Refresh the launcher's version list.
5. Select Cylox-1.8.9 and start it.

IN-GAME
- Right Shift opens/closes the Cylox Click GUI.
- Escape closes the Click GUI.
- The HUD category contains Watermark, FPS, Keystrokes, and Crosshair.
- The Movement category contains ToggleSprint and ToggleSneak.
- Enable ToggleSprint to hold Minecraft's native sprint binding while in-game.
- Enable ToggleSneak to hold Minecraft's native sneak binding while in-game.
- Disable either module to restore normal physical-key behavior.
- Select a module to edit settings or assign its keybind.
- Press an assigned key while the Click GUI is closed to toggle that module.
- Module states, keybinds and setting values are saved to cylox/settings.properties inside the game directory.

IMPORTANT
- Do not extract this ZIP inside the versions folder.
- This package does not include or redistribute Minecraft, assets, natives, or base game libraries.
- The Cylox profile inherits the launcher's vanilla 1.8.9 installation.
- Native multiplayer compatibility is Minecraft 1.8.x (protocol 47).
- Server status detection does not translate newer login or gameplay packets.
- Protocol 1.9+ servers are not supported by the built-in connection in this version.
- ViaProxy is a separate proxy process and is not bundled or tested as an MJ Launcher-integrated bridge.
- The launcher must support inherited 1.8.9 versions, LaunchWrapper, and standard library artifacts.
- Android/MJ Launcher device smoke testing is still required; CI verification does not confirm behavior on a physical phone.

If the profile is not visible, restart the launcher or refresh its version list.
