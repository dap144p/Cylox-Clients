CYLOX CLIENT 1.8.9 — INSTALLATION
Developer: dapdev
Version: 0.11.0

INSTALL
1. Install vanilla Minecraft Java 1.8.9 once in your launcher.
2. Open the Minecraft game directory configured in that launcher (the directory containing versions and libraries).
3. Extract the complete Cylox ZIP into that game directory. Merge both versions and libraries folders.
4. Refresh the launcher's version list.
5. Select Cylox-1.8.9 and start it.

PROTOCOL BRIDGE
- The ProtocolBridge module is enabled by default; Target Version defaults to Auto.
- Auto mode pings the server status before connecting and uses ViaVersion/ViaBackwards to translate catalogued Minecraft 1.9–1.13.2 server protocols for the 1.8.9 client.
- To use only vanilla 1.8.x networking, open the Click GUI with Right Shift, choose Misc → ProtocolBridge, and select Native.
- Failed/unknown status detection falls back to native protocol 47. A newer server may reject that native connection.
- The ZIP includes the ViaVersion 5.12.1, ViaBackwards 5.12.1 and ViaRewind 4.2.0 libraries. Keep all three; do not install duplicate copies.

IN-GAME
- Right Shift opens/closes the Cylox Click GUI; Escape closes it.
- HUD: Watermark, FPS, Keystrokes and Crosshair.
- Movement: ToggleSprint and ToggleSneak. Disable either to restore physical-key behavior.
- Misc: ProtocolBridge, with Auto or Native target mode.
- Module states, keybinds and setting values are saved to cylox/settings.properties inside the game directory.

IMPORTANT
- Do not extract this ZIP inside the versions folder.
- This package does not include or redistribute Minecraft, assets, natives, or base game libraries.
- The Cylox profile inherits the launcher's vanilla 1.8.9 installation and adds LaunchWrapper plus the Cylox adapter.
- The standalone JAR is not the complete MJ Launcher installation; use the ZIP so the translator libraries and version metadata are present.
- Protocol translation depends on upstream mappings and server behavior. Server plugins, custom registries and unsupported protocol extensions may still cause incompatibilities.
- The launcher must support inherited 1.8.9 versions, LaunchWrapper, and standard library artifacts.
- CI checks do not replace an actual multiplayer test on MJ Launcher/Android. Physical device smoke testing is still outstanding.
- This distribution includes GPL-3.0-or-later code; see LICENSE and THIRD_PARTY_NOTICES.md.

If the profile is not visible, restart the launcher or refresh its version list.
