package dev.cylox.client.verify;

import dev.cylox.client.minecraft189.CyloxClientTransformer;
import dev.cylox.client.minecraft189.CyloxVisibilityCacheHooks;
import dev.cylox.client.minecraft189.CyloxGraphicsPreset;
import dev.cylox.client.minecraft189.CyloxTextWidthCache;
import java.util.HashSet;
import java.util.Set;
import java.util.BitSet;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

/** Verifies all critical injection points against Mojang's official obfuscated 1.8.9 JAR. */
public final class Vanilla189Verifier {
    private Vanilla189Verifier() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Pass the official Minecraft 1.8.9 JAR and pinned ViaRewind JAR paths");
        File vanillaJar = new File(args[0]);
        if (!vanillaJar.isFile()) throw new IllegalArgumentException("Minecraft JAR does not exist: " + vanillaJar);

        CyloxClientTransformer.resetVerificationCounters();
        int scanned = 0;
        boolean minecraftMainBytecodeVerified = false;
        try (JarFile jar = new JarFile(vanillaJar)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
                byte[] bytes;
                try (InputStream input = jar.getInputStream(entry);
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
                    bytes = output.toByteArray();
                }
                String internalName = entry.getName().substring(0, entry.getName().length() - 6);
                byte[] transformed = new CyloxClientTransformer().transform(internalName, internalName, bytes);
                if ("ave".equals(internalName) && transformed != bytes) {
                    verifyMinecraftRunTickBytecode(transformed);
                    minecraftMainBytecodeVerified = true;
                }
                scanned++;
            }
        }

        if (!minecraftMainBytecodeVerified) {
            throw new IllegalStateException("Transformed Minecraft main class ave.s()V was not bytecode-verified");
        }
        System.out.println("[Cylox] ASM BasicVerifier accepted transformed vanilla method ave.s()V.");
        require("render suppression", CyloxClientTransformer.getPatchedRenderClassCount());
        require("F8/F9/F10 keyboard toggle events", CyloxClientTransformer.getPatchedKeyboardToggleClassCount());
        require("F8 held left-click input", CyloxClientTransformer.getPatchedLeftClickToggleClassCount());
        require("F10 lost-focus pause exception", CyloxClientTransformer.getPatchedPauseExceptionClassCount());
        require("F9 six-block background camera frustum", CyloxClientTransformer.getPatchedBackgroundFarPlaneClassCount());
        require("background frame limiter", CyloxClientTransformer.getPatchedFrameLimitClassCount());
        require("Via protocol pipeline", CyloxClientTransformer.getPatchedProtocolPipelineClassCount());
        require("Via compression hook", CyloxClientTransformer.getPatchedCompressionClassCount());
        require("Multiplayer selector button", CyloxClientTransformer.getPatchedSelectorButtonClassCount());
        require("Multiplayer selector action", CyloxClientTransformer.getPatchedSelectorActionClassCount());
        require("Modern Multiplayer selector panel", CyloxClientTransformer.getPatchedSelectorDrawClassCount());
        require("Protocol selector mouse-wheel scroll", CyloxClientTransformer.getPatchedSelectorScrollClassCount());
        require("Cylox themed vanilla buttons", CyloxClientTransformer.getPatchedButtonThemeClassCount());
        require("Cylox menu background", CyloxClientTransformer.getPatchedScreenBackgroundClassCount());
        require("Cylox custom main menu", CyloxClientTransformer.getPatchedMainMenuClassCount());
        require("opt-in frame profiler", CyloxClientTransformer.getPatchedFrameProfileClassCount());
        require("opt-in terrain profiler", CyloxClientTransformer.getPatchedTerrainProfileClassCount());
        require("opt-in terrain phase profiler", CyloxClientTransformer.getPatchedTerrainBreakdownClassCount());
        require("opt-in chunk-upload profiler", CyloxClientTransformer.getPatchedChunkUploadProfileClassCount());
        require("terrain visibility cache", CyloxClientTransformer.getPatchedVisibilityCacheClassCount());
        require("terrain visibility cache world invalidation", CyloxClientTransformer.getPatchedVisibilityInvalidationClassCount());
        require("terrain visibility cache range invalidation", CyloxClientTransformer.getPatchedVisibilityRangeInvalidationClassCount());
        require("near-chunk synchronous build radius limiter", CyloxClientTransformer.getPatchedNearBuildRadiusClassCount());
        require("predictive ahead-of-motion chunk pre-build ordering", CyloxClientTransformer.getPatchedPredictiveChunkPrebuildClassCount());
        require("terrain visibility graph snapshot cache", CyloxClientTransformer.getPatchedVisibilityGraphCacheClassCount());
        require("entity render-view state reuse", CyloxClientTransformer.getPatchedEntitySleepingCacheClassCount());
        require("player equipment slot bounds guard", CyloxClientTransformer.getPatchedPlayerEquipmentGuardClassCount());
        require("opt-in entity renderer profiler", CyloxClientTransformer.getPatchedEntityProfileClassCount());
        require("opt-in floating nameplate profiler", CyloxClientTransformer.getPatchedNameTagProfileClassCount());
        require("FontRenderer text-width cache", CyloxClientTransformer.getPatchedFontWidthCacheClassCount());
        require("FontRenderer glyph geometry batching", CyloxClientTransformer.getPatchedFontGlyphBatchClassCount());
        require("opt-in block-layer profiler", CyloxClientTransformer.getPatchedBlockLayerProfileClassCount());
        require("opt-in particle profiler", CyloxClientTransformer.getPatchedParticleProfileClassCount());
        require("opt-in whole-world renderer profiler", CyloxClientTransformer.getPatchedWorldRenderProfileClassCount());
        require("opt-in world-pass phase profiler", CyloxClientTransformer.getPatchedWorldPassProfileClassCount());
        require("opt-in cloud-check profiler", CyloxClientTransformer.getPatchedCloudsProfileClassCount());
        require("opt-in cloud-geometry profiler", CyloxClientTransformer.getPatchedCloudGeometryProfileClassCount());
        require("opt-in weather Tessellator-submit profiler", CyloxClientTransformer.getPatchedWeatherSubmitProfileClassCount());
        require("Fancy Clouds tessellator batching", CyloxClientTransformer.getPatchedFancyCloudBatchClassCount());
        require("opt-in render-frame phase profiler", CyloxClientTransformer.getPatchedRenderFrameBreakdownClassCount());
        require("opt-in HUD overlay/chat profiler", CyloxClientTransformer.getPatchedGuiOverlayProfileClassCount());
        require("opt-in HUD component profiler", CyloxClientTransformer.getPatchedGuiOverlayBreakdownClassCount());
        verifyViaRewindEquipmentHook(new File(args[1]));
        verifyVisibilityCacheBehavior();
        verifyVisibilityGraphSnapshotBehavior();
        verifyVisibilityRangeInvalidationBehavior();
        verifyTextWidthCacheBehavior();
        verifyGraphicsPresetBehavior();
        verifyDeepProfilerSectionTiming();
        System.out.println("[Cylox] Verified " + scanned + " Minecraft 1.8.9 classes.");
    }

    /** Runs ASM's operand-stack verifier against the exact method implicated by the crash log. */
    private static void verifyMinecraftRunTickBytecode(byte[] transformedClass) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(transformedClass).accept(node, 0);
        MethodNode target = null;
        for (MethodNode method : node.methods) {
            if ("s".equals(method.name) && "()V".equals(method.desc)) {
                if (target != null) {
                    throw new IllegalStateException("Found multiple ave.s()V methods in transformed Minecraft class");
                }
                target = method;
            }
        }
        if (target == null) {
            throw new IllegalStateException("Could not find transformed Minecraft method ave.s()V");
        }
        Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(new BasicVerifier());
        analyzer.analyze(node.name, target);
    }

    private static void verifyVisibilityCacheBehavior() {
        Object renderGlobal = new Object();
        Set<String> expected = new HashSet<String>();
        expected.add("north");
        expected.add("south");

        for (int i = 0; i < 64; i++) {
            CyloxVisibilityCacheHooks.cacheVisibleFacings(renderGlobal, Integer.valueOf(i), expected);
        }
        if (CyloxVisibilityCacheHooks.getVisibilityCacheSize() != 64) {
            throw new IllegalStateException("Visibility cache did not retain exactly 64 entries");
        }

        Object oldest = CyloxVisibilityCacheHooks.getCachedVisibleFacings(renderGlobal, Integer.valueOf(0));
        if (!(oldest instanceof Set)) {
            throw new IllegalStateException("Visibility cache missed the first stored entry");
        }
        CyloxVisibilityCacheHooks.cacheVisibleFacings(renderGlobal, Integer.valueOf(64), expected);
        if (CyloxVisibilityCacheHooks.getVisibilityCacheSize() != 64
            || CyloxVisibilityCacheHooks.getCachedVisibleFacings(renderGlobal, Integer.valueOf(1)) != null) {
            throw new IllegalStateException("Visibility cache did not evict its least-recently-used entry");
        }

        Object firstCopy = CyloxVisibilityCacheHooks.getCachedVisibleFacings(renderGlobal, Integer.valueOf(0));
        if (!(firstCopy instanceof Set) || firstCopy == expected || !expected.equals(firstCopy)) {
            throw new IllegalStateException("Visibility cache did not return a defensive copy");
        }
        ((Set<?>) firstCopy).clear();
        Object secondCopy = CyloxVisibilityCacheHooks.getCachedVisibleFacings(renderGlobal, Integer.valueOf(0));
        if (!(secondCopy instanceof Set) || !expected.equals(secondCopy)) {
            throw new IllegalStateException("Mutating a returned visibility set changed the cached value");
        }

        Object replacementRenderGlobal = new Object();
        if (CyloxVisibilityCacheHooks.getCachedVisibleFacings(replacementRenderGlobal,
                Integer.valueOf(0)) != null
            || CyloxVisibilityCacheHooks.getVisibilityCacheSize() != 0) {
            throw new IllegalStateException("Visibility cache leaked entries across RenderGlobal instances");
        }

        CyloxVisibilityCacheHooks.invalidateVisibilityCache(replacementRenderGlobal);
        if (CyloxVisibilityCacheHooks.getVisibilityCacheSize() != 0) {
            throw new IllegalStateException("Visibility cache did not clear on invalidation");
        }
        System.out.println("[Cylox] Verified bounded 64-entry LRU visibility cache, defensive copies and invalidation.");
    }

    private static final class FakeVisGraph {
        private final BitSet opaqueBlocks = new BitSet(4096);
        private int remainingCount = 4096;

        void markOpaque(int index) {
            opaqueBlocks.set(index);
            remainingCount--;
        }

        boolean isOpaque(int index) {
            return opaqueBlocks.get(index);
        }

        int getRemainingCount() {
            return remainingCount;
        }
    }

    private static final class FakeBlockPos {
        private final int x;
        private final int y;
        private final int z;

        FakeBlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public long toLong() {
            long xMask = (1L << 26) - 1L;
            long yMask = (1L << 12) - 1L;
            long zMask = (1L << 26) - 1L;
            return ((long) x & xMask) << 38
                | ((long) y & yMask) << 26
                | ((long) z & zMask);
        }

        @Override
        public int hashCode() {
            return (x * 31 + y) * 31 + z;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof FakeBlockPos)) return false;
            FakeBlockPos that = (FakeBlockPos) other;
            return x == that.x && y == that.y && z == that.z;
        }
    }

    private static void verifyVisibilityRangeInvalidationBehavior() {
        Object renderGlobal = new Object();
        Set<String> facings = new HashSet<String>();
        facings.add("north");
        FakeBlockPos nearCameraA = new FakeBlockPos(1, 1, 1);
        FakeBlockPos nearCameraB = new FakeBlockPos(15, 15, 15);
        FakeBlockPos farCamera = new FakeBlockPos(33, 1, 1);
        FakeBlockPos negativeCamera = new FakeBlockPos(-17, 1, 1);

        CyloxVisibilityCacheHooks.cacheVisibleFacings(renderGlobal, nearCameraA, facings);
        CyloxVisibilityCacheHooks.cacheVisibleFacings(renderGlobal, nearCameraB, facings);
        CyloxVisibilityCacheHooks.cacheVisibleFacings(renderGlobal, farCamera, facings);
        CyloxVisibilityCacheHooks.cacheVisibleFacings(renderGlobal, negativeCamera, facings);

        CyloxVisibilityCacheHooks.cacheVisibilityGraph(renderGlobal,
            new FakeBlockPos(0, 0, 0), new FakeVisGraph());
        CyloxVisibilityCacheHooks.cacheVisibilityGraph(renderGlobal,
            new FakeBlockPos(32, 0, 0), new FakeVisGraph());
        CyloxVisibilityCacheHooks.cacheVisibilityGraph(renderGlobal,
            new FakeBlockPos(-32, 0, 0), new FakeVisGraph());

        CyloxVisibilityCacheHooks.invalidateVisibilityRange(renderGlobal, 1, 1, 1, 1, 1, 1);

        if (CyloxVisibilityCacheHooks.getCachedVisibleFacings(renderGlobal, nearCameraA) != null
            || CyloxVisibilityCacheHooks.getCachedVisibleFacings(renderGlobal, nearCameraB) != null
            || CyloxVisibilityCacheHooks.getCachedVisibleFacings(renderGlobal, farCamera) == null
            || CyloxVisibilityCacheHooks.getCachedVisibleFacings(renderGlobal, negativeCamera) == null) {
            throw new IllegalStateException("Range invalidation removed the wrong camera-block cache sections");
        }

        if (CyloxVisibilityCacheHooks.getCachedVisibilityGraph(renderGlobal,
                new FakeBlockPos(0, 0, 0)) != null
            || CyloxVisibilityCacheHooks.getCachedVisibilityGraph(renderGlobal,
                new FakeBlockPos(32, 0, 0)) == null
            || CyloxVisibilityCacheHooks.getCachedVisibilityGraph(renderGlobal,
                new FakeBlockPos(-32, 0, 0)) == null) {
            throw new IllegalStateException("Range invalidation removed the wrong VisGraph snapshots");
        }
        if (CyloxVisibilityCacheHooks.getVisibilityCacheSize() != 2
            || CyloxVisibilityCacheHooks.getVisibilityGraphCacheSize() != 2) {
            throw new IllegalStateException("Range invalidation did not retain unaffected cache entries");
        }

        CyloxVisibilityCacheHooks.invalidateVisibilityCache(renderGlobal);
        System.out.println("[Cylox] Verified block-range invalidation preserves unaffected positive and negative VisGraph sections.");
    }

    private static void verifyVisibilityGraphSnapshotBehavior() {
        Object renderGlobal = new Object();
        Object sectionPosition = Integer.valueOf(256);
        FakeVisGraph source = new FakeVisGraph();
        source.markOpaque(5);
        source.markOpaque(4090);

        CyloxVisibilityCacheHooks.cacheVisibilityGraph(renderGlobal, sectionPosition, source);
        FakeVisGraph first = new FakeVisGraph();
        if (!CyloxVisibilityCacheHooks.restoreCachedVisibilityGraph(renderGlobal, sectionPosition, first)
            || first == source || !first.isOpaque(5) || !first.isOpaque(4090)
            || first.getRemainingCount() != 4094) {
            throw new IllegalStateException("Visibility graph snapshot did not restore the vanilla graph in place");
        }

        first.markOpaque(7);
        FakeVisGraph second = new FakeVisGraph();
        if (!CyloxVisibilityCacheHooks.restoreCachedVisibilityGraph(renderGlobal, sectionPosition, second)
            || second.isOpaque(7) || !second.isOpaque(5) || !second.isOpaque(4090)
            || second.getRemainingCount() != 4094) {
            throw new IllegalStateException("Flood-fill mutation changed the pristine VisGraph snapshot");
        }
        if (CyloxVisibilityCacheHooks.getVisibilityGraphCacheSize() != 1) {
            throw new IllegalStateException("Visibility graph snapshot cache size is incorrect");
        }

        if (CyloxVisibilityCacheHooks.restoreCachedVisibilityGraph(renderGlobal,
                Integer.valueOf(512), new FakeVisGraph())) {
            throw new IllegalStateException("Visibility graph restore should miss for an uncached section");
        }
        CyloxVisibilityCacheHooks.invalidateVisibilityCache(renderGlobal);
        if (CyloxVisibilityCacheHooks.getVisibilityGraphCacheSize() != 0) {
            throw new IllegalStateException("Visibility graph snapshots were not cleared on invalidation");
        }
        System.out.println("[Cylox] Verified in-place VisGraph restore, pristine snapshots, isolated flood-fill and invalidation.");
    }

    private static void verifyTextWidthCacheBehavior() {
        Object renderer = new Object();
        String text = "\u00a7aLeaderboard";
        if (CyloxTextWidthCache.getCachedWidth(renderer, text, false) != -1) {
            throw new IllegalStateException("Text-width cache should miss before the first measurement");
        }
        CyloxTextWidthCache.cacheWidth(renderer, text, false, 73);
        if (CyloxTextWidthCache.getCachedWidth(renderer, text, false) != 73) {
            throw new IllegalStateException("Text-width cache did not return the stored width");
        }
        if (CyloxTextWidthCache.getCachedWidth(renderer, text, true) != -1) {
            throw new IllegalStateException("Text-width cache reused a width across Unicode-font modes");
        }
        CyloxTextWidthCache.cacheWidth(renderer, text, true, 81);
        if (CyloxTextWidthCache.getCachedWidth(renderer, text, true) != 81) {
            throw new IllegalStateException("Text-width cache did not retain the Unicode-mode width");
        }
        if (CyloxTextWidthCache.getCachedWidth(new Object(), text, false) != -1) {
            throw new IllegalStateException("Text-width cache reused measurements across FontRenderer instances");
        }

        Object collisionRenderer = new Object();
        CyloxTextWidthCache.cacheWidth(collisionRenderer, "AaAa", false, 11);
        CyloxTextWidthCache.cacheWidth(collisionRenderer, "BBBB", false, 22);
        if (CyloxTextWidthCache.getCachedWidth(collisionRenderer, "AaAa", false) != 11) {
            throw new IllegalStateException("Text-width cache did not refresh the first colliding entry");
        }
        CyloxTextWidthCache.cacheWidth(collisionRenderer, "AaBB", false, 33);
        if (CyloxTextWidthCache.getCachedWidth(collisionRenderer, "AaAa", false) != 11
            || CyloxTextWidthCache.getCachedWidth(collisionRenderer, "BBBB", false) != -1
            || CyloxTextWidthCache.getCachedWidth(collisionRenderer, "AaBB", false) != 33) {
            throw new IllegalStateException("Text-width cache did not evict the least-recently-used entry in a colliding set");
        }

        Object boundedRenderer = new Object();
        for (int i = 0; i < 513; i++) {
            CyloxTextWidthCache.cacheWidth(boundedRenderer, "label-" + i, false, i);
        }
        if (CyloxTextWidthCache.getSize() > 512) {
            throw new IllegalStateException("Text-width cache exceeded its 512-entry bound");
        }
        System.out.println("[Cylox] Verified FontRenderer-scoped text widths, Unicode isolation, two-way collision handling and 512-entry bound.");
    }

    private static void verifyGraphicsPresetBehavior() throws Exception {
        File freshRoot = java.nio.file.Files.createTempDirectory("cylox-graphics-fresh").toFile();
        File freshOptions = new File(freshRoot, "options.txt");
        File freshCylox = new File(freshRoot, "cylox");
        File freshBackup = new File(freshCylox, "options-before-graphics-preset.txt");
        File freshMarker = new File(freshCylox, "graphics-max-preset-1.4.5.applied");

        File migratedRoot = java.nio.file.Files.createTempDirectory("cylox-graphics-migrated").toFile();
        File migratedOptions = new File(migratedRoot, "options.txt");
        File migratedCylox = new File(migratedRoot, "cylox");
        File migratedBackup = new File(migratedCylox, "options-before-graphics-preset.txt");
        File legacyMarker = new File(migratedCylox, "graphics-max-preset-1.4.5.applied");
        File restoredMarker = new File(migratedCylox, "graphics-max-preset-restored-1.5.5.applied");
        File reappliedMarker = new File(migratedCylox, "graphics-max-preset-reapplied-1.5.6.applied");

        File legacyRoot = java.nio.file.Files.createTempDirectory("cylox-graphics-legacy").toFile();
        File legacyOptions = new File(legacyRoot, "options.txt");
        File legacyCylox = new File(legacyRoot, "cylox");
        File legacyPresetMarker = new File(legacyCylox, "graphics-max-preset-1.4.5.applied");

        try {
            if (!freshCylox.mkdirs() && !freshCylox.isDirectory()) {
                throw new IllegalStateException("Could not create fresh graphics test directory");
            }
            java.nio.file.Files.write(freshOptions.toPath(), Arrays.asList(
                "renderDistance:3", "fancyGraphics:false", "ao:0", "particles:2",
                "entityShadows:false", "renderClouds:false", "mipmapLevels:0",
                "maxFps:30", "enableVsync:true", "lang:en_US"
            ), java.nio.charset.StandardCharsets.UTF_8);

            CyloxGraphicsPreset.applyBenchmarkPresetOnce(freshRoot);
            java.util.List<String> applied = java.nio.file.Files.readAllLines(
                freshOptions.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            String[] expectedPreset = new String[] {
                "renderDistance:16", "fancyGraphics:true", "ao:2", "particles:0",
                "entityShadows:true", "renderClouds:true", "mipmapLevels:4",
                "maxFps:260", "enableVsync:false", "lang:en_US"
            };
            for (String entry : expectedPreset) {
                if (!applied.contains(entry)) {
                    throw new IllegalStateException("Fresh install is missing maximum-graphics benchmark setting: " + entry);
                }
            }
            if (!freshMarker.isFile() || !freshBackup.isFile()) {
                throw new IllegalStateException("Fresh benchmark preset marker or original settings backup is missing");
            }
            java.util.List<String> original = java.nio.file.Files.readAllLines(
                freshBackup.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            if (!original.contains("renderDistance:3") || !original.contains("fancyGraphics:false")) {
                throw new IllegalStateException("Original graphics settings were not backed up");
            }

            java.nio.file.Files.write(freshOptions.toPath(), Arrays.asList(
                "renderDistance:4", "maxFps:90", "lang:en_US"
            ), java.nio.charset.StandardCharsets.UTF_8);
            CyloxGraphicsPreset.applyBenchmarkPresetOnce(freshRoot);
            java.util.List<String> afterManualEdit = java.nio.file.Files.readAllLines(
                freshOptions.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            if (!afterManualEdit.contains("renderDistance:4") || !afterManualEdit.contains("maxFps:90")) {
                throw new IllegalStateException("A later manual graphics edit was overwritten");
            }

            if (!migratedCylox.mkdirs() && !migratedCylox.isDirectory()) {
                throw new IllegalStateException("Could not create migrated graphics test directory");
            }
            java.nio.file.Files.write(migratedBackup.toPath(), Arrays.asList(
                "renderDistance:3", "fancyGraphics:false", "ao:0", "particles:2",
                "entityShadows:false", "renderClouds:false", "mipmapLevels:0",
                "maxFps:30", "enableVsync:true", "lang:en_US"
            ), java.nio.charset.StandardCharsets.UTF_8);
            java.nio.file.Files.write(migratedOptions.toPath(), Arrays.asList(
                "renderDistance:3", "fancyGraphics:false", "ao:0", "particles:2",
                "entityShadows:false", "renderClouds:false", "mipmapLevels:0",
                "maxFps:120", "enableVsync:true", "lang:en_US"
            ), java.nio.charset.StandardCharsets.UTF_8);
            if (!legacyMarker.createNewFile() || !restoredMarker.createNewFile()) {
                throw new IllegalStateException("Could not create v1.5.5 migration markers");
            }

            CyloxGraphicsPreset.applyBenchmarkPresetOnce(migratedRoot);
            java.util.List<String> repaired = java.nio.file.Files.readAllLines(
                migratedOptions.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            for (String entry : new String[] {
                "renderDistance:16", "fancyGraphics:true", "ao:2", "particles:0",
                "entityShadows:true", "renderClouds:true", "mipmapLevels:4",
                "maxFps:120", "enableVsync:false"
            }) {
                if (!repaired.contains(entry)) {
                    throw new IllegalStateException("v1.5.5 migration repair did not restore expected setting: " + entry);
                }
            }
            if (!reappliedMarker.isFile()) {
                throw new IllegalStateException("v1.5.5 migration repair marker was not created");
            }
            java.nio.file.Files.write(migratedOptions.toPath(), Arrays.asList("renderDistance:5"), 
                java.nio.charset.StandardCharsets.UTF_8);
            CyloxGraphicsPreset.applyBenchmarkPresetOnce(migratedRoot);
            java.util.List<String> afterRepairEdit = java.nio.file.Files.readAllLines(
                migratedOptions.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            if (!afterRepairEdit.contains("renderDistance:5")) {
                throw new IllegalStateException("A manual graphics edit after migration repair was overwritten");
            }

            if (!legacyCylox.mkdirs() && !legacyCylox.isDirectory()) {
                throw new IllegalStateException("Could not create legacy graphics test directory");
            }
            java.nio.file.Files.write(legacyOptions.toPath(), Arrays.asList(
                "renderDistance:16", "fancyGraphics:true", "lang:en_US"
            ), java.nio.charset.StandardCharsets.UTF_8);
            if (!legacyPresetMarker.createNewFile()) {
                throw new IllegalStateException("Could not create legacy benchmark marker");
            }
            CyloxGraphicsPreset.applyBenchmarkPresetOnce(legacyRoot);
            java.util.List<String> legacy = java.nio.file.Files.readAllLines(
                legacyOptions.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            if (!legacy.contains("renderDistance:16") || !legacy.contains("fancyGraphics:true")) {
                throw new IllegalStateException("An existing benchmarked install was unexpectedly changed");
            }

            System.out.println("[Cylox] Verified one-time maximum-graphics stress preset, settings backup, manual-edit preservation and v1.5.5 migration repair.");
        } finally {
            deleteRecursively(freshRoot);
            deleteRecursively(migratedRoot);
            deleteRecursively(legacyRoot);
        }
    }

    private static void verifyDeepProfilerSectionTiming() throws Exception {
        String previous = System.getProperty("cylox.profile");
        System.setProperty("cylox.profile", "true");
        try {
            Class<?> monitor = Class.forName(
                "dev.cylox.client.minecraft189.CyloxPerformanceMonitor");
            java.lang.reflect.Field countsField = monitor.getDeclaredField("DEEP_SECTION_COUNTS");
            countsField.setAccessible(true);
            long[] before = ((long[]) countsField.get(null)).clone();
            for (int section = 34; section <= 44; section++) {
                dev.cylox.client.minecraft189.CyloxPerformanceMonitor.beginSection(section);
                dev.cylox.client.minecraft189.CyloxPerformanceMonitor.endSection(section);
            }
            long[] after = (long[]) countsField.get(null);
            if (after.length != 11) {
                throw new IllegalStateException("Unexpected deep profiler section count: " + after.length);
            }
            for (int index = 0; index < after.length; index++) {
                if (after[index] != before[index] + 1L) {
                    throw new IllegalStateException("Deep profiler section " + (34 + index)
                        + " was not recorded");
                }
            }
            System.out.println("[Cylox] Verified all deep profiler sections 34-44 record samples.");
        } finally {
            if (previous == null) System.clearProperty("cylox.profile");
            else System.setProperty("cylox.profile", previous);
        }
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) return;
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) deleteRecursively(child);
        }
        if (!file.delete()) {
            System.err.println("[Cylox] Could not remove verifier temporary file: " + file);
        }
    }

    private static void verifyViaRewindEquipmentHook(File viaRewindJar) throws Exception {
        if (!viaRewindJar.isFile()) {
            throw new IllegalArgumentException("ViaRewind JAR does not exist: " + viaRewindJar);
        }
        String className = "com/viaversion/viarewind/protocol/v1_9to1_8/rewriter/EntityPacketRewriter1_9";
        String entryName = className + ".class";
        int patchedHandlers = 0;
        boolean foundRewriter = false;
        try (JarFile jar = new JarFile(viaRewindJar)) {
            JarEntry rewriterEntry = jar.getJarEntry(entryName);
            if (rewriterEntry == null) {
                throw new IllegalStateException("ViaRewind equipment rewriter is missing: " + entryName);
            }
            foundRewriter = true;

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.endsWith(".class")
                    || !name.startsWith(className + "$")) {
                    continue;
                }
                String internalName = name.substring(0, name.length() - 6);
                byte[] original;
                try (InputStream input = jar.getInputStream(entry);
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
                    original = output.toByteArray();
                }
                int before = CyloxClientTransformer.getPatchedViaRewindEquipmentClassCount();
                byte[] transformed = new CyloxClientTransformer().transform(internalName, internalName, original);
                int after = CyloxClientTransformer.getPatchedViaRewindEquipmentClassCount();
                if (transformed != null && !Arrays.equals(original, transformed)) {
                    patchedHandlers += after - before;
                }
            }
        }
        if (!foundRewriter || patchedHandlers != 1) {
            throw new IllegalStateException("Expected exactly one ViaRewind equipment-handler patch, found "
                + patchedHandlers);
        }
        System.out.println("[Cylox] Verified ViaRewind local-player equipment guard in its PacketHandlers inner class.");
    }

    private static void require(String hook, int count) {
        if (count < 1) throw new IllegalStateException("Could not locate " + hook + " hook in vanilla 1.8.9");
        System.out.println("[Cylox] Verified " + hook + " in " + count + " class(es).");
    }
}
