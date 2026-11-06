# Third-party notices

Cylox embeds its protocol compatibility layer using the Java-8-compatible ViaMCP 1.8.x integration. The translator libraries are upstream Java 8 compatibility builds because the MJ Launcher runtime used for this client is Java 8.

- ViaMCP source: https://github.com/ViaVersionMCP/ViaMCP
- Pinned upstream source commit: `d22b90b20560eca4bdc553d3a0f50a60f5773aec`
- License: GNU General Public License v3.0 (GPL-3.0-or-later).
- The platform startup, protocol pipeline and network hooks are used by the manual 1.8.9-to-newer-server bridge.

The following libraries are bundled inside `Cylox-Client-1.8.9.jar`; they are not shipped as separate installation files:

- ViaVersion 5.12.3-SNAPSHOT (upstream Java 8 compatibility build #55)
- ViaBackwards 5.12.3-SNAPSHOT (upstream Java 8 compatibility build #45)
- ViaRewind 4.2.0 (upstream Java 8 compatibility build #39)

Their source and license information is available at:

- https://github.com/ViaVersion/ViaVersion
- https://github.com/ViaVersion/ViaBackwards
- https://github.com/ViaVersion/ViaRewind/tree/4.2.0
- https://github.com/ViaVersionMCP/ViaMCP/tree/d22b90b20560eca4bdc553d3a0f50a60f5773aec

The Java 8 compatibility artifacts are pinned to these upstream CI builds:

- https://ci.viaversion.com/job/ViaVersion-Java8/55/
- https://ci.viaversion.com/view/ViaBackwards/job/ViaBackwards-Java8/45/
- https://ci.viaversion.com/view/ViaRewind/job/ViaRewind-Java8/39/

The CI build verifies each downloaded archive contains its required entry classes and that all ordinary (non-multi-release) class files use Java 8-compatible bytecode (class-file major version 52 or earlier).

Minecraft, Java, LWJGL, native renderer binaries, game assets and base game libraries are not included.
