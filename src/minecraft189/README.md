# Minecraft 1.8.9 runtime adapter

This source set connects the UI core to a vanilla 1.8.9 client without Forge or Fabric.

## Added runtime path

- CyloxClickGuiScreen wraps the UI in the vanilla GuiScreen lifecycle.
- MinecraftUiRenderer uses the game's Gui, GlStateManager, and FontRenderer.
- CyloxTweaker implements LaunchWrapper ITweaker and targets the vanilla main class.
- CyloxClientTransformer injects a small hook into the Minecraft client tick.
- CyloxKeyHook checks Right Shift on the client thread and opens/closes the Click GUI.
- The GUI does not pause single-player gameplay.

## Launcher profile requirements

The profile must start through LaunchWrapper, not directly through the vanilla main class:

- Main class: net.minecraft.launchwrapper.Launch
- Tweak argument: --tweakClass dev.cylox.client.minecraft189.CyloxTweaker
- LaunchWrapper 1.12 and its ASM dependencies must be present on the classpath.
- Preserve the normal Minecraft 1.8.9 game arguments and libraries supplied by the launcher.

The LaunchWrapper library is a small extra dependency; the Minecraft client JAR and normal game libraries remain launcher-managed.

## Build

The adapter source currently compiles against an MCP-named/deobfuscated Minecraft 1.8.9 client JAR:

    gradle minecraft189AdapterJar -Pmc189Jar=/absolute/path/to/mcp-1.8.9-client.jar

Required compile-time libraries are LaunchWrapper 1.12, ASM 5.0.3, and LWJGL 2.9.1. They are not bundled by the adapter task.

## Runtime limitation — not installable yet

The vanilla 1.8.9 client JAR is obfuscated. This adapter source uses MCP names and still needs a verified reobfuscation step before the JAR can run against the normal launcher-managed client. The project does not yet publish an installable Cylox version JAR or installer.

The transformer uses the client tick's profiler sections as a method signature rather than relying on an assumed obfuscated method name. The exact transformed bytecode still needs a real 1.8.9 runtime test. If the markers do not match, the transformer leaves Minecraft unchanged and logs that the hotkey was not injected.

The sample modules remain UI-only previews and do not implement gameplay behavior.
