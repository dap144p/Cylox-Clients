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

/** Locates vanilla 1.8.9 hooks by bytecode markers without MCP class names. */
public final class CyloxClientTransformer implements IClassTransformer {
    private static volatile int patchedTickClasses;
    private static volatile int patchedRenderClasses;
    private static volatile int patchedProtocolConnectClasses;
    private static volatile int patchedCompressionClasses;
    private static final String HOOK = "dev/cylox/client/minecraft189/CyloxKeyHook";
    private static final String OVERLAY = "dev/cylox/client/minecraft189/CyloxOverlayRenderer";
    private static final String VIA = "dev/cylox/client/minecraft189/CyloxProtocolBridge";

    public static void resetVerificationCounters() {
        patchedTickClasses = 0;
        patchedRenderClasses = 0;
        patchedProtocolConnectClasses = 0;
        patchedCompressionClasses = 0;
    }
    public static int getPatchedTickClassCount() { return patchedTickClasses; }
    public static int getPatchedRenderClassCount() { return patchedRenderClasses; }
    public static int getPatchedProtocolConnectClassCount() { return patchedProtocolConnectClasses; }
    public static int getPatchedCompressionClassCount() { return patchedCompressionClasses; }

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
            boolean protocolConnectPatched = false;
            boolean compressionPatched = false;

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
                    insertBeforeReturns(method, OVERLAY, "render", "(Ljava/lang/Object;)V", 0);
                    renderPatched = true;
                    changed = true;
                }

                boolean socketInitializer = method.desc.contains("Lio/netty/channel/socket/SocketChannel;")
                    || method.desc.contains("Lio/netty/channel/Channel;");
                if (!protocolConnectPatched && socketInitializer
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
            if (protocolConnectPatched) {
                patchedProtocolConnectClasses++;
                System.out.println("[Cylox] Protocol connect hook injected: " + node.name);
            }
            if (compressionPatched) {
                patchedCompressionClasses++;
                System.out.println("[Cylox] Via compression reorder hook injected: " + node.name);
            }
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[Cylox] Transformer skipped " + name + ": " + error.getClass().getSimpleName()
                + ": " + error.getMessage());
            return basicClass;
        }
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
