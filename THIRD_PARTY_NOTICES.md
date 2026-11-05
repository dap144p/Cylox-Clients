# Third-party notices

Cylox embeds its protocol compatibility layer using the Java-8-compatible ViaMCP 1.8.x integration.

- ViaMCP source: https://github.com/ViaVersionMCP/ViaMCP
- Pinned upstream source commit: `d22b90b20560eca4bdc553d3a0f50a60f5773aec`
- License: GNU General Public License v3.0 (GPL-3.0-or-later).
- The platform startup, protocol pipeline and network hooks are used by the manual 1.8.9-to-newer-server bridge.

The following libraries are bundled inside `Cylox-Client-1.8.9.jar`; they are not shipped as separate installation files:

- ViaVersion 5.12.1
- ViaBackwards 5.12.1
- ViaRewind 4.2.0

Their source and license information is available at:

- https://github.com/ViaVersion/ViaVersion/tree/5.12.1
- https://github.com/ViaVersion/ViaBackwards/tree/5.12.1
- https://github.com/ViaVersion/ViaRewind/tree/4.2.0
- https://github.com/ViaVersionMCP/ViaMCP/tree/d22b90b20560eca4bdc553d3a0f50a60f5773aec

Minecraft, Java, LWJGL, native renderer binaries, game assets and base game libraries are not included.
