package dev.cylox.client.minecraft189;

import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Adds only the background render/frame hooks and the optional Via pipeline hooks. */
public final class CyloxClientTransformer implements net.minecraft.launchwrapper.IClassTransformer {
    private static volatile int patchedRenderClasses;
    private static volatile int patchedFrameLimitClasses;
    private static volatile int patchedProtocolConnectClasses;
    private static volatile int patchedCompressionClasses;

    private static final String BACKGROUND = "dev/cylox/client/minecraft189/CyloxBackgroundHooks";
    private static final String VIA = "dev/cylox/client/minecraft189/CyloxProtocolBridge";

    public static void resetVerificationCounters() {
        patchedRenderClasses = 0;
        patchedFrameLimitClasses = 0;
        patchedProtocolConnectClasses = 0;
        patchedCompressionClasses = 0;
    }

    public static int getPatchedRenderClassCount() { return patchedRenderClasses; }
    public static int getPatchedFrameLimitClassCount() { return patchedFrameLimitClasses; }
    public static int getPatchedProtocolConnectClassCount() { return patchedProtocolConnectClasses; }
    public static int getPatchedCompressionClassCount() { return patchedCompressionClasses; }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            boolean changed = false;
            boolean renderPatched = false;
            boolean frameLimitPatched = false;
            boolean protocolConnectPatched = false;
            boolean compressionPatched = false;

            for (MethodNode method : node.methods) {
                if ((method.access & Opcodes.ACC_STATIC) == 0) {
                    if (!renderPatched && "(FJ)V".equals(method.desc)
                        && hasMarkers(method, "mouse", "level", "gui")) {
                        insertRenderGuard(method);
                        renderPatched = true;
                        changed = true;
                    }

                    boolean socketInitializer = method.desc.contains("Lio/netty/channel/socket/SocketChannel;")
                        || method.desc.contains("Lio/netty/channel/Channel;");
                    if (!protocolConnectPatched && "ek$5".equals(node.name) && socketInitializer
                        && hasMarkers(method, "timeout", "splitter", "decoder", "prepender", "encoder", "packet_handler")) {
                        insertBeforeReturns(method, VIA, "install", "(Ljava/lang/Object;)V", 1);
                        protocolConnectPatched = true;
                        changed = true;
                    }

                    if (!compressionPatched && hasMarkers(method, "decompress", "compress")) {
                        insertBeforeReturns(method, VIA, "onCompressionEnabled", "(Ljava/lang/Object;)V", 0);
                        compressionPatched = true;
                        changed = true;
                    }
                }

                if (!frameLimitPatched) {
                    for (AbstractInsnNode instruction = method.instructions.getFirst();
                         instruction != null; instruction = instruction.getNext()) {
                        if (instruction instanceof MethodInsnNode) {
                            MethodInsnNode call = (MethodInsnNode) instruction;
                            if ("org/lwjgl/opengl/Display".equals(call.owner)
                                && "sync".equals(call.name) && "(I)V".equals(call.desc)) {
                                method.instructions.insertBefore(instruction,
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, BACKGROUND, "frameLimit", "(I)I", false));
                                frameLimitPatched = true;
                                changed = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!changed) return basicClass;
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            if (renderPatched) patchedRenderClasses++;
            if (frameLimitPatched) patchedFrameLimitClasses++;
            if (protocolConnectPatched) patchedProtocolConnectClasses++;
            if (compressionPatched) patchedCompressionClasses++;
            if (renderPatched) System.out.println("[Cylox] Background render suppression injected: " + node.name);
            if (frameLimitPatched) System.out.println("[Cylox] Background frame limit injected: " + node.name);
            if (protocolConnectPatched) System.out.println("[Cylox] Via connect hook injected: " + node.name);
            if (compressionPatched) System.out.println("[Cylox] Via compression hook injected: " + node.name);
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[Cylox] Transformer skipped " + name + ": "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
            return basicClass;
        }
    }

    private static void insertRenderGuard(MethodNode method) {
        LabelNode foreground = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BACKGROUND, "shouldRender", "()Z", false));
        guard.add(new JumpInsnNode(Opcodes.IFNE, foreground));
        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(foreground);
        // The new target has the same locals as method entry and an empty operand stack.
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(guard);
    }

    private static void insertBeforeReturns(MethodNode method, String owner, String name,
                                            String descriptor, int localIndex) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.RETURN) {
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, localIndex));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, descriptor, false));
                method.instructions.insertBefore(instruction, hook);
            }
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
