# Minecraft 1.8.9 screen adapter

This source set connects the existing UI core to the native 1.8.9 GUI lifecycle:

- CyloxClickGuiScreen is a vanilla GuiScreen wrapper.
- MinecraftUiRenderer draws with Minecraft's own Gui, GlStateManager, and FontRenderer.
- Mouse, drag, release, and key input are forwarded to ClickGui.
- doesGuiPauseGame() returns false, so opening the Click GUI does not pause a single-player world.

## Compile

The adapter must compile against an MCP-named/deobfuscated Minecraft 1.8.9 client JAR:

    gradle minecraft189AdapterJar -Pmc189Jar=/absolute/path/to/mcp-1.8.9-client.jar

The mapped JAR is a compile-time input only. It is not copied into the resulting adapter JAR.

## Important runtime status

This is not yet a launcher-installable client. The standard Minecraft 1.8.9 JAR is obfuscated, so the adapter still needs a verified 1.8.9 remapping/launch hook before it can be loaded by a normal vanilla profile. No Forge or Fabric classes are used. A hotkey/tweaker has not been registered yet; CyloxClickGuiScreen.open() is the game-side entry point for the next runtime-hook stage.

The sample modules remain UI-only previews and do not implement gameplay behavior.
