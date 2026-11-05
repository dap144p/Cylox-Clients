# Cylox Client — Standalone Vanilla 1.8.9

**Developer:** dapdev  
**Version:** 0.5.0  
**Base:** Minecraft Java 1.8.9 (protocol 47)  
**Runtime:** Vanilla + LaunchWrapper 1.12; no Forge/Fabric requirement.

## Current build

- Custom three-column Click GUI with categories, module search, module toggles, settings, and keybind editor.
- Right Shift opens/closes the overlay; Escape closes it.
- Lightweight LWJGL/OpenGL overlay; no compile-time Minecraft or mod-loader dependency in the adapter.
- Protocol core foundation: protocol catalogue, VarInt codec, handshake packet encoder, compatibility planner, and translator extension point.
- The runtime currently supports native protocol 47 (the Minecraft 1.8.x family).
- Protocols 1.9 through 1.13.2 are catalogued for future adapters. They are not yet connectable until a complete bidirectional packet translator is implemented and tested.
- Included sample gameplay modules remain interface previews; their toggles currently change local UI state only.
- The install ZIP contains the Cylox client library and version profile. It does not redistribute Minecraft, assets, natives, or game libraries.

## Protocol compatibility status

| Server version family | Protocol | v0.5.0 status |
|---|---:|---|
| Minecraft 1.8.x | 47 | Native protocol compatibility |
| Minecraft 1.9–1.12.2 | 107–340 | Catalogued; translator not implemented |
| Minecraft 1.13–1.13.2 | 393–404 | Catalogued; translator not implemented |

Changing only the handshake protocol number is not a protocol translator. Newer versions change packet IDs, packet layouts, registries, chunks, items, and other gameplay data. Cylox reports these targets as unavailable rather than pretending that a connection will work. A target becomes supported only after its translator handles both directions and passes protocol/runtime tests.

## Download

- [Cylox Client latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Latest source ZIP](https://github.com/dap144p/Cylox-Clients/archive/refs/heads/main.zip)
- [GitHub repository](https://github.com/dap144p/Cylox-Clients)

## Install — version folder method

1. Install vanilla Minecraft Java 1.8.9 once in your launcher.
2. Extract the Cylox ZIP into the root of the Minecraft game directory (the folder that already contains versions and libraries). Merge folders when prompted.
3. Select Cylox-1.8.9 in the launcher's version list.
4. Launch. The profile reuses the installed vanilla 1.8.9 client JAR and downloads only declared missing libraries.

Do not extract the ZIP inside versions; it already contains the correct versions/ and libraries/ paths.

## Build

Requirements: JDK 8+ and Gradle 8.10.2.

    gradle clean test buildClientPackage

Output:

    build/distributions/Cylox-Client-Vanilla-1.8.9-0.5.0.zip

Verify injection targets against the official client JAR:

    gradle verifyVanilla189Hooks -Pvanilla189Jar=/path/to/1.8.9.jar

The build does not require a decompiled or MCP Minecraft JAR.

## Attribution

Cylox Client — **dapdev**.
