CYLOX CLIENT 1.8.9 — INSTALLATION
Developer: dapdev
Version: 1.3.3

INSTALL
1. Download the complete Cylox Client ZIP from GitHub Releases.
2. Open the game directory configured in MJ Launcher (the directory containing versions and libraries).
3. Extract the ZIP into that directory and merge the folders.
4. Refresh the launcher's version list.
5. Select Cylox-1.8.9 and launch it. Its standalone profile declares the vanilla 1.8.9 client JAR, game libraries and asset index as dependencies, allowing MJ Launcher to download missing files. A separate vanilla 1.8.9 profile should not be required.
6. Keep the launcher's normal Java and renderer settings.
7. Open Multiplayer and tap "Select: [version]" beside the standard "Cancel" button. The modern menu groups versions as 1.8x, 1.9x, 1.10x and onward; scroll with the mouse wheel or arrow controls.
8. Choose the exact target protocol from the menu that opens above the selector before connecting. The selection is saved in cylox/protocol.properties.

RENDERER CORE
- Near-player dirty chunks join the same time-budgeted asynchronous update queue used by vanilla for distant chunks, avoiding synchronous compilation on the render thread.
- Reuses Minecraft 1.8.9's existing two chunk workers and render-buffer pool without creating additional threads.
- Vanilla's time-budgeted update loop keeps remaining dirty chunks queued when the worker queue is full or the frame budget expires.
- Normal chunk meshes, world visibility, entities, tile entities, particles, HUD, controls, ticks and networking remain enabled. This feature does not change Video Settings or options.txt.
- This is a targeted Sodium-inspired scheduler, not a full Sodium port. Test frame time and stability on the target device.

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
