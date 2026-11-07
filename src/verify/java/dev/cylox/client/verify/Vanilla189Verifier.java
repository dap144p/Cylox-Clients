package dev.cylox.client.verify;

import dev.cylox.client.minecraft189.CyloxClientTransformer;
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
        verifyViaRewindEquipmentHook(new File(args[1]));
        System.out.println("[Cylox] Verified " + scanned + " Minecraft 1.8.9 classes.");
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
