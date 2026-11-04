package dev.cylox.client.minecraft189;

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

/**
 * Injects the key polling hook into Minecraft 1.8.9's client tick.
 * The method is identified by its stable profiler section markers instead of
 * an obfuscated method name, so this source can be checked against the actual
 * 1.8.9 bytecode during the remapping/build stage.
 */
public final class CyloxClientTransformer implements IClassTransformer {
    private static final String TARGET_CLASS = "net.minecraft.client.Minecraft";
    private static final String HOOK_CLASS = "dev/cylox/client/minecraft189/CyloxKeyHook";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET_CLASS.equals(transformedName)) {
            return basicClass;
        }

        ClassReader reader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            if (!"()V".equals(method.desc) || !isClientTick(method)) {
                continue;
            }

            InsnList hook = new InsnList();
            hook.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                HOOK_CLASS,
                "onClientTick",
                "()V",
                false
            ));
            method.instructions.insert(hook);

            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            System.out.println("[Cylox] Right Shift hook injected into Minecraft client tick.");
            return writer.toByteArray();
        }

        System.err.println("[Cylox] Could not locate the 1.8.9 client tick method; Click GUI hotkey is disabled.");
        return basicClass;
    }

    private boolean isClientTick(MethodNode method) {
        boolean guiSection = false;
        boolean gameModeSection = false;

        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null;
             instruction = instruction.getNext()) {
            if (instruction instanceof LdcInsnNode) {
                Object constant = ((LdcInsnNode) instruction).cst;
                if ("gui".equals(constant)) {
                    guiSection = true;
                } else if ("gameMode".equals(constant)) {
                    gameModeSection = true;
                }
            }
            if (guiSection && gameModeSection) {
                return true;
            }
        }
        return false;
    }
}
