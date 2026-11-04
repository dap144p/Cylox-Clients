# Vanilla 1.8.9 runtime adapter

The current runtime adapter is independent of MCP names and does not modify or redistribute the Minecraft JAR.

- LaunchWrapper 1.12 starts the vanilla 1.8.9 main class.
- ASM identifies the client tick and render-frame methods using profiler markers present in the original obfuscated client.
- The tick hook routes keyboard and mouse events while the Click GUI is open.
- The render hook draws the overlay using LWJGL/OpenGL.
- No Forge, Fabric, MCP client JAR, or repackaged Minecraft JAR is required.

The build downloads the official 1.8.9 client JAR only for bytecode verification and does not put that JAR in the release.

Sample modules remain UI previews only; real gameplay modules and an MJ Launcher device smoke test are still outstanding.
