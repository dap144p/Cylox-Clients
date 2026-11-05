package dev.cylox.client.verify;

import dev.cylox.client.minecraft189.CyloxClientTransformer;
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
                CyloxClientTransformer transformer = new CyloxClientTransformer();
                transformer.transform(internalName, internalName, bytes);
                scanned++;
            }
        }

        require("render suppression", CyloxClientTransformer.getPatchedRenderClassCount());
        require("background frame limiter", CyloxClientTransformer.getPatchedFrameLimitClassCount());
        require("Via connect hook", CyloxClientTransformer.getPatchedProtocolConnectClassCount());
        require("Via compression hook", CyloxClientTransformer.getPatchedCompressionClassCount());
        System.out.println("[Cylox] Verified " + scanned + " Minecraft 1.8.9 classes.");
    }

    private static void require(String hook, int count) {
        if (count < 1) throw new IllegalStateException("Could not locate " + hook + " hook in vanilla 1.8.9");
        System.out.println("[Cylox] Verified " + hook + " in " + count + " class(es).");
    }
}
