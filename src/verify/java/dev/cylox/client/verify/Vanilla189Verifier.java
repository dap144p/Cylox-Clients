package dev.cylox.client.verify;

import dev.cylox.client.minecraft189.CyloxClientTransformer;
import dev.cylox.client.minecraft189.CyloxRenderHooks;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Verifies all critical injection points against Mojang's official obfuscated 1.8.9 JAR. */
public final class Vanilla189Verifier {
    private Vanilla189Verifier() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Pass the official Minecraft 1.8.9 JAR path");
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
        require("terrain render-graph scratch reuse", CyloxClientTransformer.getPatchedRenderGraphClassCount());
        verifyScratchCollectionReuse();
        System.out.println("[Cylox] Verified " + scanned + " Minecraft 1.8.9 classes.");
    }

    private static void verifyScratchCollectionReuse() {
        Object owner = new Object();
        ArrayList<Object> firstList = CyloxRenderHooks.reuseRenderInfos(owner);
        firstList.add(owner);
        ArrayList<Object> secondList = CyloxRenderHooks.reuseRenderInfos(owner);
        if (firstList != secondList || !secondList.isEmpty()) {
            throw new IllegalStateException("Render-info scratch list was not reused and cleared");
        }

        LinkedList<Object> firstQueue = CyloxRenderHooks.reuseTraversalQueue();
        firstQueue.add(owner);
        if (firstQueue.poll() != owner || !firstQueue.isEmpty()) {
            throw new IllegalStateException("Terrain traversal queue does not preserve FIFO behavior");
        }
        LinkedList<Object> secondQueue = CyloxRenderHooks.reuseTraversalQueue();
        if (firstQueue != secondQueue || !secondQueue.isEmpty()) {
            throw new IllegalStateException("Terrain traversal queue was not reused and cleared");
        }
        System.out.println("[Cylox] Verified reusable render-info list and FIFO terrain queue.");
    }

    private static void require(String hook, int count) {
        if (count < 1) throw new IllegalStateException("Could not locate " + hook + " hook in vanilla 1.8.9");
        System.out.println("[Cylox] Verified " + hook + " in " + count + " class(es).");
    }
}
