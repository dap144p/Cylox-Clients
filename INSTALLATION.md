CYLOX BACKGROUND CLIENT 1.8.9 — INSTALLATION
Developer: dapdev
Version: 1.0.0

INSTALL
1. Install Minecraft Java 1.8.9 once in MJ Launcher.
2. Download the complete Background Client ZIP from GitHub Releases.
3. Open the game directory configured in MJ Launcher (the directory containing versions and libraries).
4. Extract the ZIP into that directory and merge the folders.
5. Refresh the launcher's version list.
6. Select Cylox-1.8.9. Keep the launcher's normal Java and renderer settings.

BACKGROUND
- Automatically skips vanilla world/UI rendering while the LWJGL display is inactive or hidden.
- The main display loop is capped at 5 FPS while inactive.
- Rendering and the launcher's normal frame limit resume when the display becomes active.
- There is no anti-AFK, auto-clicker or movement automation.
- The game process must remain alive. Android or the launcher may pause/terminate it; the client JAR cannot override those policies.

PROTOCOL
- Native Minecraft 1.8.x uses protocol 47.
- Auto status-ping translation is provided for the tested 1.9–1.13.2 protocol catalogue.
- Unknown protocol IDs fall back to native 1.8.x and may be rejected by newer servers.
- Keep the three Via libraries included in the ZIP. Do not install duplicate versions.

IMPORTANT
- Do not extract the ZIP inside versions/.
- The package does not include Minecraft itself, game assets, Java runtime, renderer binaries or native libraries.
- The small Cylox JAR is only a LaunchWrapper adapter.
- Real MJ Launcher/Android background testing is still required.
- GPL-3.0-or-later; see the licenses folder in the ZIP.
