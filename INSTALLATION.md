CYLOX BACKGROUND CLIENT 1.8.9 — INSTALLATION
Developer: dapdev
Version: 1.0.6

INSTALL
1. Download the complete Background Client ZIP from GitHub Releases.
2. Open the game directory configured in MJ Launcher (the directory containing versions and libraries).
3. Extract the ZIP into that directory and merge the folders.
4. Refresh the launcher's version list.
5. Select Cylox-1.8.9 and launch it. Its standalone profile declares the vanilla 1.8.9 client JAR, game libraries and asset index as dependencies, allowing MJ Launcher to download missing files. A separate vanilla 1.8.9 profile should not be required.
6. Keep the launcher's normal Java and renderer settings.
7. Open Multiplayer and tap "Select: [version]" immediately to the right of the standard "Cancel" button. The vanilla Cancel and other Multiplayer buttons keep their original positions.
8. Choose the exact target protocol from the menu that opens above the selector before connecting. The selection is saved in cylox/protocol.properties.

PROTOCOL
- ViaVersion, ViaBackwards and ViaRewind are embedded inside the single Cylox client JAR.
- No separate Via JARs need to be downloaded or installed.
- Default target is native 1.8.9 (protocol 47).
- The menu only lists target versions with a known Via translation route from 1.8.9.
- Cylox does not ping servers to select a protocol automatically.
- Choose the same protocol version required by the destination server. A mismatch may be rejected or may have compatibility issues.
- Cylox attaches Via's encoder and decoder during vanilla channel initialization, before the initial handshake is sent. Look for `Via pipeline attached before initial handshake`.

BACKGROUND
- Automatically skips vanilla world/UI rendering while the LWJGL display is inactive or hidden.
- The main display loop is capped at 5 FPS while inactive.
- Rendering and the launcher's normal frame limit resume when the display becomes active.
- There is no anti-AFK, auto-clicker or movement automation.
- The game process must remain alive. Android or the launcher may pause/terminate it; the client JAR cannot override those policies.

IMPORTANT
- Do not extract the ZIP inside versions/.
- The package does not include Minecraft itself, game assets, Java runtime, renderer binaries, native libraries or base Minecraft libraries.
- The single Cylox JAR contains the Cylox adapter and the bundled Via protocol stack.
- Real MJ Launcher/Android testing is still required.
- GPL-3.0-or-later; see the licenses folder in the ZIP.
