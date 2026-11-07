CYLOX CLIENT 1.8.9 — INSTALLATION
Developer: dapdev
Version: 1.3.9

INSTALL
1. Download the complete Cylox Client ZIP from GitHub Releases.
2. Open the game directory configured in MJ Launcher (the directory containing versions and libraries).
3. Extract the ZIP into that directory and merge the folders.
4. Refresh the launcher's version list.
5. Select Cylox-1.8.9 and launch it. Its standalone profile declares the vanilla 1.8.9 client JAR, game libraries and asset index as dependencies, allowing MJ Launcher to download missing files. A separate vanilla 1.8.9 profile should not be required.
6. Keep the launcher's normal Java and renderer settings.
7. Open Multiplayer and tap "Select: [version]" beside the standard "Cancel" button. The modern menu groups versions as 1.8x, 1.9x, 1.10x and onward; scroll with the mouse wheel or arrow controls.
8. Choose the exact target protocol from the menu that opens above the selector before connecting. The selection is saved in cylox/protocol.properties.

RENDERER AND NETWORK CORE
- Via decode handler now forwards unexpected packet/translation exceptions to Minecraft's NetworkManager; only Via's normal CancelCodecException cancellation is swallowed. This makes failed packet translations visible to the normal connection error path rather than silently ignoring them.
- Keeps Minecraft 1.8.9's original chunk scheduler, WorldRenderer, culling, render layers and pooled RegionRenderCacheBuilder buffers.
- Large chunk buffers continue to grow on demand. Cylox does not reserve multi-megabyte buffers for every layer at startup.
- ViaVersion 5.12.3-SNAPSHOT and ViaBackwards 5.12.3-SNAPSHOT are bundled from upstream Java 8 compatibility builds #55 and #45. ViaRewind 4.2.0 is bundled from Java 8 compatibility build #39.
- These replace the older 5.12.1 ViaVersion/ViaBackwards libraries used by v1.3.7. The selector, target protocol and outgoing handshake logic are unchanged.
- The selected Via target is applied at startup or when the selector changes. New network channels reuse that configured target; they do not reload the global Via manager again.
- Via encoder/decoder temporary buffers start at the incoming packet size (minimum 256 bytes) to avoid avoidable initial growth; translated packets can still expand if required.
- The downloaded libraries are the upstream Java 8 compatibility builds, not the normal Java 17+ artifacts. Upstream still treats Java 8 builds as an unsupported compatibility fallback, so the Java 8 warning may remain in the log.
- CI checks the downloaded archive contents and confirms ordinary bundled class files are Java 8 bytecode (major version 52 or earlier).
- This dependency update is intended to improve legacy protocol handling, but it is not yet proof that the exact packet exceptions seen on the test server are fixed. Retest on MJ Launcher before treating the Via issue as resolved.
- Test actual world loading, frame pacing, memory use and server connection stability on MJ Launcher.

CYLOX UI
- The main menu uses a translucent Cylox dark theme instead of the vanilla panorama.
- Button labels are width-fitted, FontRenderer/button lookups are cached, and selector rows adapt to the available screen height.
- Buttons, menus and Protocol Selector use semi-transparent backgrounds so the game world remains visible behind them; text stays opaque for readability.
- Original buttons and menu actions are preserved.

PROTOCOL
- ViaVersion, ViaBackwards and ViaRewind are embedded inside the single Cylox client JAR.
- No separate Via JARs need to be downloaded or installed.
- Default target is native 1.8.9 (protocol 47).
- The menu lists version families with an actual Via translation route from 1.8.9, including newer major.minor release naming such as 26.3x when present. All listed families use the same general-purpose Via pipeline.
- Cylox does not ping servers to select a protocol automatically.
- Choose a protocol family equal to or newer than the minimum server version. For a server requiring 1.13+, choose 1.13x or newer.
- LaunchWrapper parent-delegates the full `io.netty.` package to prevent duplicate Netty class identities. Cylox verifies the channel type before installing Via's encoder/decoder. Logs show the selected route and actual outgoing handshake ID for any selected family.

BACKGROUND
- Automatically skips vanilla world/UI rendering while the LWJGL display is inactive or hidden.
- World rendering stops while inactive; the main loop remains at 20 Hz so client ticks and packet processing continue.
- Rendering and the launcher's normal frame limit resume when the display becomes active.
- There is no anti-AFK, auto-clicker or movement automation.
- The game process must remain alive. Android or the launcher may pause/terminate it; the client JAR cannot override those policies.

IMPORTANT
- Do not extract the ZIP inside versions/.
- The package does not include Minecraft itself, game assets, Java runtime, renderer binaries, native libraries or base Minecraft libraries.
- The single Cylox JAR contains the Cylox adapter and the bundled Via protocol stack.
- Real MJ Launcher/Android testing is still required.
- GPL-3.0-or-later; see the licenses folder in the ZIP.
