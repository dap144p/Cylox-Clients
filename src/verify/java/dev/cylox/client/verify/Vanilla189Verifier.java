package dev.cylox.client.verify;

import dev.cylox.client.minecraft189.CyloxClientTransformer;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Confirms that the real obfuscated 1.8.9 client contains both safe hook targets. */
public final class Vanilla189Verifier {
    private Vanilla189Verifier() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Pass the vanilla 1.8.9 client JAR path");
        CyloxClientTransformer.resetVerificationCounters();
        CyloxClientTransformer transformer = new CyloxClientTransformer();
        JarFile jar = new JarFile(args[0]);
        int scanned = 0;
        try {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
                byte[] bytes = readAll(jar.getInputStream(entry));
                String name = entry.getName().substring(0, entry.getName().length() - 6);
                transformer.transform(name, name, bytes);
                scanned++;
            }
        } finally {
            jar.close();
        }
        int tick = CyloxClientTransformer.getPatchedTickClassCount();
        int render = CyloxClientTransformer.getPatchedRenderClassCount();
        System.out.println("[Cylox] Scanned " + scanned + " vanilla classes; tick hooks=" + tick + ", render hooks=" + render);
        if (tick != 1 || render != 1) {
            throw new IllegalStateException("Expected exactly one Minecraft tick hook and one render hook.");
        }
    }

    private static byte[] readAll(InputStream input) throws Exception {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toByteArray();
        } finally {
            input.close();
        }
    }
}
