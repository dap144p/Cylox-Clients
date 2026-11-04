package dev.cylox.client.minecraft189;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Locates vanilla 1.8.9 methods by profiler markers, without MCP class names. */
public final class CyloxClientTransformer implements IClassTransformer {
    private static volatile int patchedTickClasses;
    private static volatile int patchedRenderClasses;
    private static final String HOOK = "dev/cylox/client/minecraft189/CyloxKeyHook";
    private static final String OVERLAY = "dev/cylox/client/minecraft189/CyloxOverlayRenderer";

    public static void resetVerificationCounters() {
        patchedTickClasses = 0;
        patchedRenderClasses = 0;
    }
    public static int getPatchedTickClassCount() { return patchedTickClasses; }
    public static int getPatchedRenderClassCount() { return patchedRenderClasses; }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return basicClass;
        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            boolean changed = false;
            boolean tickPatched = false;
            boolean renderPatched = false;

            for (MethodNode method : node.methods) {
                if ((method.access & Opcodes.ACC_STATIC) != 0) continue;

                if (!tickPatched && "()V".equals(method.desc) && hasMarkers(method, "gui", "gameMode")) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "onClientTick",
                        "(Ljava/lang/Object;)V", false));
                    method.instructions.insert(hook);
                    tickPatched = true;
                    changed = true;
                }

                if (!renderPatched && "(FJ)V".equals(method.desc)
                    && hasMarkers(method, "mouse", "level", "gui")) {
                    for (AbstractInsnNode instruction = method.instructions.getFirst();
                         instruction != null; instruction = instruction.getNext()) {
                        if (instruction.getOpcode() == Opcodes.RETURN) {
                            InsnList hook = new InsnList();
                            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, OVERLAY, "render",
                                "(Ljava/lang/Object;)V", false));
                            method.instructions.insertBefore(instruction, hook);
                        }
                    }
                    renderPatched = true;
                    changed = true;
                }
            }

            if (!changed) return basicClass;
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            if (tickPatched) {
                patchedTickClasses++;
                System.out.println("[Cylox] Tick hook injected: " + node.name);
            }
            if (renderPatched) {
                patchedRenderClasses++;
                System.out.println("[Cylox] Render overlay injected: " + node.name);
            }
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[Cylox] Transformer skipped " + name + ": " + error.getClass().getSimpleName());
            return basicClass;
        }
    }

    private static boolean hasMarkers(MethodNode method, String... markers) {
        Set<String> found = new HashSet<String>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof LdcInsnNode) {
                Object value = ((LdcInsnNode) instruction).cst;
                if (value instanceof String) found.add((String) value);
            }
        }
        for (String marker : markers) if (!found.contains(marker)) return false;
        return true;
    }
}
