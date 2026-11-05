package dev.cylox.client.verify;

import dev.cylox.client.minecraft189.CyloxClientTransformer;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/** Verifies vanilla tick/render hooks and the obfuscated 1.8.9 ToggleSprint mappings. */
public final class Vanilla189Verifier {
    private Vanilla189Verifier() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Pass the vanilla 1.8.9 client JAR path");
        CyloxClientTransformer.resetVerificationCounters();
        CyloxClientTransformer transformer = new CyloxClientTransformer();
        Map<String, byte[]> movementClasses = new HashMap<String, byte[]>();
        JarFile jar = new JarFile(args[0]);
        int scanned = 0;
        try {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
                byte[] bytes = readAll(jar.getInputStream(entry));
                String name = entry.getName().substring(0, entry.getName().length() - 6);
                if ("ave".equals(name) || "avh".equals(name) || "avb".equals(name)) {
                    movementClasses.put(name, bytes);
                }
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

        verifyToggleSprintMappings(movementClasses);
    }

    private static void verifyToggleSprintMappings(Map<String, byte[]> classes) throws Exception {
        ClassNode minecraft = readClass(classes, "ave");
        ClassNode gameSettings = readClass(classes, "avh");
        ClassNode keyBinding = readClass(classes, "avb");

        requireField(minecraft, "t", "Lavh;", false); // Minecraft.gameSettings
        requireField(minecraft, "h", "Lbew;", false); // Minecraft.thePlayer
        requireField(gameSettings, "ae", "Lavb;", false); // GameSettings.keyBindSprint
        requireMethod(keyBinding, "i", "()I", false); // KeyBinding.getKeyCode
        requireMethod(keyBinding, "a", "(IZ)V", true); // KeyBinding.setKeyBindState

        System.out.println("[Cylox] Verified vanilla ToggleSprint mappings: ave -> avh/avb");
    }

    private static ClassNode readClass(Map<String, byte[]> classes, String name) {
        byte[] bytes = classes.get(name);
        if (bytes == null) throw new IllegalStateException("Missing vanilla mapping class: " + name);
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static void requireField(ClassNode node, String name, String descriptor, boolean isStatic) {
        for (FieldNode field : node.fields) {
            if (name.equals(field.name) && descriptor.equals(field.desc)
                && (((field.access & Opcodes.ACC_STATIC) != 0) == isStatic)) {
                return;
            }
        }
        throw new IllegalStateException("Missing mapped field " + node.name + "." + name + " " + descriptor);
    }

    private static void requireMethod(ClassNode node, String name, String descriptor, boolean isStatic) {
        for (MethodNode method : node.methods) {
            if (name.equals(method.name) && descriptor.equals(method.desc)
                && (((method.access & Opcodes.ACC_STATIC) != 0) == isStatic)) {
                return;
            }
        }
        throw new IllegalStateException("Missing mapped method " + node.name + "." + name + descriptor);
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
