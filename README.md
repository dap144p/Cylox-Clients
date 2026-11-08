# Cylox Client

**Developer:** dapdev  
**Version:** 1.4.6  
**Minecraft:** Java 1.8.9 (base protocol 47)  
**Runtime:** MJ Launcher-provided Java and renderer; Minecraft 1.8.9 client, libraries and assets resolved from version metadata  
**Loader:** LaunchWrapper tweaker only; no Forge or Fabric

Cylox is a minimal 1.8.9 client. Its gameplay-side purpose is to reduce rendering work while the game window is inactive. Its protocol compatibility features are embedded in the same Cylox JAR.

## Background behavior

- Detects whether the LWJGL display is active and visible.
- Skips the vanilla `EntityRenderer` frame completely when the display is inactive/hidden, so world rendering drops to **0 FPS**.
- Keeps the whole Minecraft main loop at **20 Hz** in the background. This is deliberately not reduced to 2 FPS: `Display.sync()` throttles the game loop itself, so a 2 FPS cap could starve client ticks and delay queued packet processing.
- Returns to normal rendering and the launcher's selected frame limit when the display becomes active again.
- Does not pause the Minecraft network stack deliberately and does not automate player actions.

This is not an Android background service. The launcher must keep the game process alive for the connection to remain open. Android may suspend or kill background applications, and the JAR cannot override that operating-system or launcher policy. A real MJ Launcher/device test is required before treating background connectivity as guaranteed.

## Java 8 Via stub packaging (1.4.2)

- Adds the JavaDowngrader 2.0.1 Java 8 API archive to the single Cylox JAR at its original `xyz/wagyourtail/jvmdg` package path.
- The previous Java 8 ViaVersion and ViaBackwards archives carry private relocated copies under their own prefixes. Those copies do not satisfy ViaRewind bytecode references to the unprefixed stub package.
- CI verifies that `J_L_Record.class` is present at the exact root package path and that the added API classes use Java 8-compatible bytecode.
- Keeps Java 8 as the MJ Launcher runtime target; it does not change the selected Via protocol, Netty handshake, packet translation rules, or renderer.
- This addresses the missing-class error in the supplied log. A successful connection and world join still require a device/server smoke test.

## Via decoder error propagation (1.3.9)

- Keeps normal `CancelCodecException` packet cancellation unchanged.
- Forwards every other decoder/translation exception through Netty with `ctx.fireExceptionCaught(cause)` so Minecraft's `NetworkManager` can handle the failed connection instead of silently dropping the error.
- This is an error-handling correction, not a claim that all Via packet translation bugs or scoreboard compatibility issues are fixed; retest the affected server and inspect the full stack trace if it still fails.

## Via compatibility and equipment handling (1.4.0)

- Keeps the established ViaVersion 5.12.3-SNAPSHOT Java 8 compatibility build #55, ViaBackwards 5.12.3-SNAPSHOT Java 8 compatibility build #45, and ViaRewind 4.2.0 Java 8 compatibility build #39.
- Applies the upstream ViaRewind fix from commit `f6424ddb565b9350cc347ce43f67c7e608767499` to the embedded 4.2.0 class at load time. The hook cancels redundant equipment packets for the local player in the 1.9-to-1.8 translation; it leaves other entities and the existing off-hand handling unchanged.
- A 1.8 client already receives its own equipment through window-0 slot updates, and its legacy armor inventory uses different slot indexing. The local-player guard prevents the redundant translated equipment packet from reaching that legacy inventory handler.
- Registers the Cylox transformer before ViaLoadingBase initializes protocol classes, ensuring the ViaRewind equipment rewriter is transformed on first load.
- Keeps the manual selector, target resolver, per-channel Netty pipeline and outgoing handshake logic unchanged.
- Keeps the 1.3.9 decoder behavior: normal `CancelCodecException` cancellation is preserved; other decoder/translation exceptions are forwarded to Minecraft's network manager.
- CI checks the exact pinned ViaRewind Java 8 JAR, verifies the expected rewriter class, and confirms that Cylox injects the local-player guard. Java 8 compatibility artifacts remain upstream compatibility builds, not a guarantee that every server plugin or custom packet is supported.

## Vanilla terrain rendering (1.4.0)

- Removes Cylox 1.3.7's custom reuse of the `RenderGlobal.setupTerrain` visible-chunk list and traversal queue.
- Restores Minecraft 1.8.9's native terrain traversal and culling implementation; no custom list or queue replacement is injected.
- Preserves the native chunk-update scheduling, `WorldRenderer`, render layers, frustum/visibility culling and `RegionRenderCacheBuilder` pool.
- Does not change video settings or claim a measured FPS improvement. The purpose of this rollback is to eliminate the custom terrain traversal as a cause of delayed or incomplete chunk visibility.
- Actual chunk loading, dimension/gamemode transitions, frame time, memory use and Via compatibility still require testing on MJ Launcher and the target device.

## Low-end performance diagnostics (1.4.1)

- Adds opt-in timing for complete rendered frames, vanilla terrain setup/culling, and the vanilla chunk-upload phase.
- Diagnostics are disabled by default and do not alter graphics settings, chunk scheduling, worker count, gameplay ticks, or network behavior.
- To collect a five-second rolling report, add `-Dcylox.profile=true` to the JVM arguments before launching.
- Reports include average FPS, average/maximum frame time, average/maximum terrain-setup time, and average/maximum chunk-upload time.
- Use the report to identify whether a low-end device is limited by frame rendering, terrain visibility traversal, or chunk uploads before introducing a renderer change.
- This diagnostic build does not claim an FPS improvement by itself. Actual optimization requires comparing measurements on the same device, world, render distance, renderer, and scene.

## Low-end render engine — phase 1 (1.4.3)

- Caches the current camera block's `getVisibleFacings` result in `RenderGlobal`. When only yaw/pitch changes while the camera remains in the same block, Cylox can reuse the six-direction visibility result instead of rebuilding the opaque-block graph across the 16×16×16 block section.
- The cache is single-entry, keyed by the `RenderGlobal` instance and immutable `BlockPos`, invalidated by render-range block updates and world replacement, and returns a defensive Set copy because vanilla mutates the result in its spectator/visibility branch.
- Does not cache frustum decisions, replace vanilla BFS traversal, change chunk scheduling, hide chunks, reduce render distance, or alter gameplay/network behavior.
- Extends opt-in profiling to entity rendering, block-layer rendering and particle rendering. The five-second report includes average/maximum time for each area plus visibility-cache hits, misses and invalidations.
- Enable the report with `-Dcylox.profile=true`. The profiler is disabled by default.
- This is a conservative first optimization, not a complete Sodium-equivalent renderer. Further entity, block-layer, particle and render-state optimizations will be selected from the new measurements and tested independently to avoid chunk-visibility regressions.

## Terrain profiling breakdown (1.4.4)

- Splits the existing vanilla `RenderGlobal.setupTerrain` profiler boundaries into camera update, render-list initialization, cull preparation, visibility culling and synchronous nearby-chunk builds.
- Uses the existing vanilla section transitions only. It does not replace BFS traversal, frustum tests, the render queue, chunk update scheduling or chunk upload behavior.
- The `terrainBuildNear*` metrics are nested inside `terrainCulling*`; do not add them together. They show how much of the culling phase is spent rebuilding nearby chunks synchronously.
- The five-second report emits average, maximum and sample count for each phase. This remains opt-in and disabled unless `-Dcylox.profile=true` is enabled.
- This build adds diagnostics only. It does not claim an FPS increase; collect another comparable AlwiNation spawn log before selecting the next optimization.


## Spawn-lobby stress-test preset and visibility cache (1.4.5)

- Extends the conservative single-position visibility cache to a 64-entry least-recently-used cache for recently visited camera blocks. The cache remains scoped to one RenderGlobal instance and is cleared on render-range updates or world replacement.
- Returns defensive Set copies because vanilla can mutate the visibility result. Vanilla BFS traversal, frustum decisions, render queues, chunk scheduling, chunk uploads, and world visibility remain unchanged.
- On the first 1.4.5 launch only, seeds the standard Minecraft Video Settings through `options.txt`: Fancy Graphics, 16-chunk render distance, maximum smooth lighting, All particles, entity shadows, clouds enabled, mipmap level 4, maximum FPS limit, and VSync off.
- Backs up an existing `options.txt` to `cylox/options-before-graphics-preset.txt` before changing it. The preset marker prevents repeated overwrites, so later changes made in Minecraft's own Settings menu are preserved.
- The maximum graphics preset is intentionally a stress test, not a recommended low-end daily profile. Restore the backup or lower settings in Video Settings after profiling.

## Spawn-lobby render optimization — phase 2 (1.4.6)

- Extends terrain visibility caching beyond the camera-block result: Cylox snapshots the opaque-block BitSet and remaining-cell counter from vanilla's VisGraph after the 16×16×16 scan, keyed by RenderGlobal and section origin.
- On a cache hit, it reconstructs a fresh VisGraph from the pristine snapshot before running vanilla's flood-fill. This is required because vanilla mutates the VisGraph BitSet while computing visible directions. The cache is bounded to 32 section snapshots and is cleared with the existing world/render-range invalidation path.
- The existing camera-block visibility Set cache remains bounded to 64 entries and now preserves EnumSet's compact representation when copying cached directions.
- Uses immutable vanilla `BlockPos` objects directly as keys in both bounded visibility caches. The active `RenderGlobal` identity still scopes and resets the maps on world replacement, while avoiding a temporary wrapper-key allocation for each cache lookup or insertion.
- Adds a 512-entry LRU FontRenderer width cache keyed by renderer instance, Unicode-font mode and exact formatted string. Repeated nameplate/armor-stand hologram width measurements can be reused without changing text, color codes, draw order or visibility.
- Splits the opt-in RenderGlobal profile into entity rendering and tile-entity rendering. Adds separate name-tag timing, with name-tag time nested inside entity rendering; do not add the nested timings together.
- New report fields include terrain visibility-graph cache hits/misses/failures, text-width cache hits/misses/invalidations, entity time, tile-entity time and name-tag time.
- Preserves vanilla BFS traversal, frustum decisions, render queues, chunk scheduling, chunk uploads, entity drawing, tile-entity drawing, scoreboard teams, hologram text and Via/network behavior. No entities or holograms are hidden, skipped or distance-limited.
- These are conservative CPU-side reuse optimizations. They do not claim a measured FPS increase; compare logs on the same device, server location, renderer and scene before judging the result.

## Low-stall nearby chunk rebuilds — beta follow-up

- Narrows vanilla's synchronous near-build predicate from 16 to 8 blocks on each axis. Since render-section origins are 16 blocks apart, only the section containing the player can use the blocking `updateChunkNow` path.
- Dirty neighboring sections are not dropped or marked clean. They remain in the existing `chunksToUpdate` set and are dispatched through vanilla `updateChunks` / `updateChunkLater` scheduling.
- This reduces render-thread stalls from rebuilding several adjacent sections synchronously. Updated geometry in neighboring sections may appear later while the existing worker queue processes those rebuilds.
- Leaves VisGraph flood-fill, BFS traversal, frustum checks, visible chunk selection, chunk upload behavior, render distance, and network handling unchanged.
- The bytecode patch applies only when the official 1.8.9 near-build predicate matches the verified three-axis distance-check shape; otherwise verification fails closed.

## Camera-first chunk preparation and predictive pre-build — beta follow-up

- When entering a world or standing still, reads the view entity's horizontal yaw and reorders the existing dirty `RenderChunk` queue by camera sectors: center-front first, front flanks second, sides/rear flanks third, and the rear cone last. Chunks are nearest-first within each sector.
- While the player is moving, retains the predictive behavior: chunks in the recent movement corridor are submitted nearest-first. After 0.5 seconds without movement, priority falls back to the current camera direction.
- Reorders only the existing dirty `RenderChunk` set before vanilla `updateChunks()` submits compile tasks to the existing worker dispatcher. Every queued chunk remains present exactly once; no new compile tasks are created.
- The first visible area can therefore become ready before the sides and rear, while the rest of the queue continues through vanilla's existing worker limits and upload path.
- This does not force all chunks to finish simultaneously, load chunks beyond the selected Render Distance, create a second render distance, remove block faces, alter vanilla visibility/frustum/BFS decisions, or add a second GPU render pass. It only changes the submission order of chunks already in the active render/update queue.
- The goal is to make initial world entry and movement feel less stalled by preparing the direction the camera is facing first. Average FPS improvement is not guaranteed; compare the same MJ Launcher scene and collect before/after profiles on the target Android device.

## Cylox adaptive lightweight UI

- Uses one consistent dark navy, cyan and orange visual system across the main menu, standard menu backgrounds, vanilla buttons and the protocol selector.
- Keeps backgrounds and controls semi-transparent so gameplay can remain visible behind supported screens; button hover and focus states become more prominent without hiding the world.
- Adapts protocol-selector row count to the available screen height and keeps the full protocol list scrollable.
- Clips long button labels to their available width and preserves original vanilla button objects, IDs and click actions.
- Caches reflective button and FontRenderer accessors, and builds the supported protocol-family list once instead of repeating reflection and route discovery every frame.
- Uses immediate-mode rectangles and Minecraft's existing font renderer; no added UI framework, texture packs, blur, shaders or animations.

## Manual Via protocol selector

- ViaVersion, ViaBackwards and ViaRewind are bundled inside **the single Cylox client JAR**.
- Open Multiplayer and tap **Select: [version]** beside the standard **Cancel** button. The modern selector groups releases as `1.8x`, `1.9x`, `1.10x` and onward; scroll with the mouse wheel or arrow controls.
- The menu shows every version family with a real translation path from native 1.8.9: 1.8x through 1.21x, plus newer major.minor families such as 26.3x when present. Every listed family uses the same Via translation pipeline.
- The selected target is saved in `cylox/protocol.properties` inside the game directory.
- Default: native Minecraft **1.8.9** (protocol 47).
- LaunchWrapper delegates the entire `io.netty.` package through the parent classloader so Minecraft's `NioSocketChannel` and Cylox's `Channel` share one class identity. Cylox verifies the channel type, then attaches Via's encoder/decoder before the initial handshake and logs the actual outgoing protocol ID.
- Protocol selection is **manual**. Cylox does not ping the server to guess its version and does not switch protocol automatically.
- Set the Via selector to the destination server's protocol before connecting. A wrong selection can cause the server to reject the handshake or can lead to compatibility issues.
- Via translates packets; it cannot guarantee that every server plugin, custom registry, or version-specific feature will behave identically.

## Scope

Included: Cylox main-menu theme, themed vanilla buttons and menu backgrounds, manual version-family selector, embedded Via protocol translation, outgoing-handshake verification, background render suppression and a 1.8.9 LaunchWrapper adapter that preserves vanilla terrain traversal and chunk scheduling.

Not included: anti-AFK, auto-clicking, auto-movement, PvP modules, custom HUD, Click GUI, cosmetics, texture packs, Minecraft assets, Java runtime, renderer binaries, or base Minecraft libraries.

## Download

- [Latest release](https://github.com/dap144p/Cylox-Clients/releases/tag/latest)
- [Versioned releases](https://github.com/dap144p/Cylox-Clients/releases)
- [Repository](https://github.com/dap144p/Cylox-Clients)

The complete installation ZIP contains one standalone Cylox version profile and one Cylox JAR. The profile includes official 1.8.9 library and asset metadata, so a compatible launcher can resolve missing Minecraft dependencies without requiring a separately installed vanilla profile. There are no separate Via JARs.

## Install — MJ Launcher / version-folder method

1. Download the complete **Cylox Client ZIP** from Releases.
2. Open the Minecraft game directory used by MJ Launcher (the directory containing `versions/` and `libraries/`).
3. Extract the ZIP into that directory and merge the folders.
4. Refresh the version list and select **Cylox-1.8.9**.
5. Launch it. The standalone profile declares the vanilla 1.8.9 client JAR, libraries and asset index as dependencies; MJ Launcher should download missing files. You do not need to install a separate vanilla 1.8.9 profile first.
6. Keep the launcher's normal Java runtime and renderer settings.
7. Open Multiplayer, tap **Select: [version]** beside **Cancel**, then choose the target version from the menu before connecting.

Do not extract the ZIP inside `versions/`; it already contains the correct folder paths.

## Build

Requirements: JDK 17 and Gradle 8.10.2.

```bash
gradle clean test buildClientPackage verifyVanilla189Hooks -Pvanilla189Jar=/path/to/official-1.8.9.jar
```

Outputs:

- `build/libs/Cylox-Client-1.8.9-1.4.6.jar` — includes Cylox and all three Via libraries.
- `build/distributions/Cylox-Client-1.8.9-1.4.6.zip` — standalone profile plus the single Cylox JAR at its declared library path.

The CI job verifies the injection points against Mojang's official 1.8.9 client JAR. The client JAR is kept below a 20 MB ceiling; Minecraft itself, Java, renderer and launcher libraries are not bundled.

## License

GPL-3.0-or-later. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
