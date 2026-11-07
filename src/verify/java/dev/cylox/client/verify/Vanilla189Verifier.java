package dev.cylox.client.verify;

import dev.cylox.client.minecraft189.CyloxClientTransformer;
import dev.cylox.client.minecraft189.CyloxVisibilityCacheHooks;
import dev.cylox.client.minecraft189.CyloxGraphicsPreset;
import java.util.HashSet;
import java.util.Set;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Verifies all critical injection points against Mojang's official obfuscated 1.8.9 JAR. */
public final class Vanilla189Verifier {
    private Vanilla189Verifier() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Pass the official Minecraft 1.8.9 JAR and pinned ViaRewind JAR paths");
        File vanillaJar = new File(args[0]);
        if (!vanillaJar.isFile()) throw new IllegalArgumentException("Minecraft JAR does not exist: " + vanillaJar);

        CyloxClientTransformer.resetVerificationCounters();
        int scanned = 0;
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
                new CyloxClientTransformer().transform(internalName, internalName, bytes);
                scanned++;
            }
        }

        require("render suppression", CyloxClientTransformer.getPatchedRenderClassCount());
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
        require("terrain visibility cache invalidation", CyloxClientTransformer.getPatchedVisibilityInvalidationClassCount());
        require("opt-in entity renderer profiler", CyloxClientTransformer.getPatchedEntityProfileClassCount());
        require("opt-in block-layer profiler", CyloxClientTransformer.getPatchedBlockLayerProfileClassCount());
        require("opt-in particle profiler", CyloxClientTransformer.getPatchedParticleProfileClassCount());
        verifyViaRewindEquipmentHook(new File(args[1]));
        verifyVisibilityCacheBehavior();
        verifyGraphicsPresetBehavior();
        System.out.println("[Cylox] Verified " + scanned + " Minecraft 1.8.9 classes.");
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

        CyloxVisibilityCacheHooks.invalidateVisibilityCache(renderGlobal);
        if (CyloxVisibilityCacheHooks.getVisibilityCacheSize() != 0) {
            throw new IllegalStateException("Visibility cache did not clear on invalidation");
        }
        System.out.println("[Cylox] Verified bounded 64-entry LRU visibility cache, defensive copies and invalidation.");
    }

    private static void verifyGraphicsPresetBehavior() throws Exception {
        File root = java.nio.file.Files.createTempDirectory("cylox-graphics-preset").toFile();
        File options = new File(root, "options.txt");
        File cyloxDirectory = new File(root, "cylox");
        File backup = new File(cyloxDirectory, "options-before-graphics-preset.txt");
        try {
            java.nio.file.Files.write(options.toPath(), java.util.Arrays.asList(
                "renderDistance:3",
                "fancyGraphics:false",
                "ao:0",
                "particles:2",
                "entityShadows:false",
                "renderClouds:false",
                "mipmapLevels:0",
                "maxFps:30",
                "enableVsync:true",
                "lang:en_US"
            ), java.nio.charset.StandardCharsets.UTF_8);

            CyloxGraphicsPreset.applyBenchmarkPresetOnce(root);
            java.util.List<String> applied = java.nio.file.Files.readAllLines(
                options.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            String[] expected = new String[] {
                "renderDistance:16",
                "fancyGraphics:true",
                "ao:2",
                "particles:0",
                "entityShadows:true",
                "renderClouds:true",
                "mipmapLevels:4",
                "maxFps:260",
                "enableVsync:false",
                "lang:en_US"
            };
            for (String entry : expected) {
                if (!applied.contains(entry)) {
                    throw new IllegalStateException("Graphics preset is missing expected setting: " + entry);
                }
            }
            if (!backup.isFile()) throw new IllegalStateException("Original options.txt backup was not created");
            java.util.List<String> original = java.nio.file.Files.readAllLines(
                backup.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            if (!original.contains("renderDistance:3") || !original.contains("lang:en_US")) {
                throw new IllegalStateException("Original options.txt backup did not preserve existing settings");
            }

            java.nio.file.Files.write(options.toPath(), java.util.Arrays.asList("renderDistance:4"),
                java.nio.charset.StandardCharsets.UTF_8);
            CyloxGraphicsPreset.applyBenchmarkPresetOnce(root);
            java.util.List<String> afterSecondLaunch = java.nio.file.Files.readAllLines(
                options.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            if (!afterSecondLaunch.contains("renderDistance:4")) {
                throw new IllegalStateException("Graphics preset overwrote user settings after its first launch");
            }
            System.out.println("[Cylox] Verified one-time maximum graphics preset, options.txt backup and user-setting preservation.");
        } finally {
            deleteRecursively(root);
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
