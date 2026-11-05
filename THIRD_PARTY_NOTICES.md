# Third-party notices

Cylox Client v0.11.0 contains a client-side protocol bridge adapted from **ViaMCP**'s Minecraft 1.8.x implementation.

- ViaMCP source: https://github.com/ViaVersionMCP/ViaMCP
- Pinned upstream source commit: `d22b90b20560eca4bdc553d3a0f50a60f5773aec`
- License: GNU General Public License v3.0 (GPL-3.0-or-later).
- Modified for Cylox: the platform startup and Netty pipeline are retained with an automatic status-ping protocol selector and bytecode injection hooks for the vanilla 1.8.9 client.

The release also includes Java-8-compatible ViaVersion 5.12.1, ViaBackwards 5.12.1, and ViaRewind 4.2.0 libraries from the pinned ViaMCP repository snapshot. Their corresponding sources and licenses are available through:
- ViaVersion: https://github.com/ViaVersion/ViaVersion/tree/5.12.1
- ViaBackwards: https://github.com/ViaVersion/ViaBackwards/tree/5.12.1
- ViaRewind: https://github.com/ViaVersion/ViaRewind/tree/4.2.0
- ViaMCP Java-8-compatible library artifacts and integration source: https://github.com/ViaVersionMCP/ViaMCP/tree/d22b90b20560eca4bdc553d3a0f50a60f5773aec

The bundled Minecraft game, assets, natives, and base libraries are not included. Cylox's GPL-3.0 license file is provided in this repository and release package.
