package dev.cylox.client.minecraft189;

import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
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

/** Adds the background hooks, embedded Via pipeline hooks and manual protocol selector UI. */
public final class CyloxClientTransformer implements net.minecraft.launchwrapper.IClassTransformer {
    private static volatile int patchedRenderClasses;
    private static volatile int patchedFrameLimitClasses;
    private static volatile int patchedProtocolPipelineClasses;
    private static volatile int patchedCompressionClasses;
    private static volatile int patchedSelectorButtonClasses;
    private static volatile int patchedSelectorActionClasses;
    private static volatile int patchedSelectorDrawClasses;
    private static volatile int patchedSelectorScrollClasses;
    private static volatile int patchedButtonThemeClasses;
    private static volatile int patchedScreenBackgroundClasses;
    private static volatile int patchedMainMenuClasses;

    private static final String BACKGROUND = "dev/cylox/client/minecraft189/CyloxBackgroundHooks";
    private static final String VIA = "dev/cylox/client/minecraft189/CyloxProtocolBridge";
    private static final String UI = "dev/cylox/client/minecraft189/CyloxUiHooks";

    public static void resetVerificationCounters() {
        patchedRenderClasses = 0;
        patchedFrameLimitClasses = 0;
        patchedProtocolPipelineClasses = 0;
        patchedCompressionClasses = 0;
        patchedSelectorButtonClasses = 0;
        patchedSelectorActionClasses = 0;
        patchedSelectorDrawClasses = 0;
        patchedSelectorScrollClasses = 0;
        patchedButtonThemeClasses = 0;
        patchedScreenBackgroundClasses = 0;
        patchedMainMenuClasses = 0;
    }

    public static int getPatchedRenderClassCount() { return patchedRenderClasses; }
    public static int getPatchedFrameLimitClassCount() { return patchedFrameLimitClasses; }
    public static int getPatchedProtocolPipelineClassCount() { return patchedProtocolPipelineClasses; }
    public static int getPatchedCompressionClassCount() { return patchedCompressionClasses; }
    public static int getPatchedSelectorButtonClassCount() { return patchedSelectorButtonClasses; }
    public static int getPatchedSelectorActionClassCount() { return patchedSelectorActionClasses; }
    public static int getPatchedSelectorDrawClassCount() { return patchedSelectorDrawClasses; }
    public static int getPatchedSelectorScrollClassCount() { return patchedSelectorScrollClasses; }
    public static int getPatchedButtonThemeClassCount() { return patchedButtonThemeClasses; }
    public static int getPatchedScreenBackgroundClassCount() { return patchedScreenBackgroundClasses; }
    public static int getPatchedMainMenuClassCount() { return patchedMainMenuClasses; }

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
            boolean protocolPipelinePatched = false;
            boolean compressionPatched = false;
            boolean selectorButtonPatched = false;
            boolean selectorActionPatched = false;
            boolean selectorDrawPatched = false;
            boolean selectorScrollPatched = false;
            boolean buttonThemePatched = false;
            boolean screenBackgroundPatched = false;
            boolean mainMenuPatched = false;
            boolean buttonWidgetClass = hasClassString(node, "textures/gui/widgets.png");
            boolean guiScreenBaseClass = isMinecraftGuiClass(name, transformedName, "GuiScreen")
                || (hasMethodDescriptor(node, "(IIF)V") && hasMethodDescriptor(node, "(I)V"));
            boolean mainMenuClass = isMinecraftGuiClass(name, transformedName, "GuiMainMenu")
                || hasClassMarkers(node, "menu.singleplayer", "menu.multiplayer", "menu.options", "menu.quit");

            for (MethodNode method : node.methods) {
                if ((method.access & Opcodes.ACC_STATIC) == 0) {
                    if (!buttonThemePatched && buttonWidgetClass && isButtonDrawMethod(method)) {
                        insertButtonTheme(method);
                        buttonThemePatched = true;
                        changed = true;
                    }

                    if (!screenBackgroundPatched && guiScreenBaseClass && isDefaultBackgroundMethod(node, method)) {
                        replaceWithScreenBackground(method);
                        screenBackgroundPatched = true;
                        changed = true;
                    }

                    if (!mainMenuPatched && mainMenuClass && "(IIF)V".equals(method.desc)) {
                        replaceWithMainMenu(method);
                        mainMenuPatched = true;
                        changed = true;
                    }
                    if (!renderPatched && "(FJ)V".equals(method.desc)
                        && hasMarkers(method, "mouse", "level", "gui")) {
                        insertRenderGuard(method);
                        renderPatched = true;
                        changed = true;
                    }

                    int channelArgumentSlot = channelArgumentSlot(method.desc);
                    if (!protocolPipelinePatched && channelArgumentSlot >= 0
                        && hasMarkers(method, "timeout", "splitter", "decoder", "prepender", "encoder", "packet_handler")) {
                        insertBeforeReturns(method, VIA, "install", "(Ljava/lang/Object;)V", channelArgumentSlot);
                        protocolPipelinePatched = true;
                        changed = true;
                        System.out.println("[Cylox] Via initializer hook configured: "
                            + node.name + "." + method.name + method.desc
                            + ", channelLocal=" + channelArgumentSlot + ".");
                    }

                    if (!compressionPatched && hasMarkers(method, "decompress", "compress")) {
                        insertBeforeReturns(method, VIA, "onCompressionEnabled", "(Ljava/lang/Object;)V", 0);
                        compressionPatched = true;
                        changed = true;
                    }

                    if (!selectorButtonPatched && hasMarkers(method,
                        "selectServer.edit", "selectServer.delete", "selectServer.select",
                        "selectServer.direct", "selectServer.add", "selectServer.refresh", "gui.cancel")) {
                        insertBeforeReturns(method, VIA, "addSelectorButton", "(Ljava/lang/Object;)V", 0);
                        selectorButtonPatched = true;
                        changed = true;
                    }

                    if (!selectorActionPatched && hasMarkers(method,
                        "selectServer.deleteQuestion", "selectServer.deleteWarning")) {
                        InsnList hook = new InsnList();
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, VIA,
                            "handleSelectorAction", "(Ljava/lang/Object;Ljava/lang/Object;)V", false));
                        method.instructions.insert(hook);
                        selectorActionPatched = true;
                        changed = true;
                    }
                }

                if (!selectorScrollPatched && (method.access & Opcodes.ACC_STATIC) == 0) {
                    for (AbstractInsnNode instruction = method.instructions.getFirst();
                         instruction != null; instruction = instruction.getNext()) {
                        if (instruction instanceof MethodInsnNode) {
                            MethodInsnNode call = (MethodInsnNode) instruction;
                            if ("org/lwjgl/input/Mouse".equals(call.owner)
                                && "getEventDWheel".equals(call.name) && "()I".equals(call.desc)) {
                                InsnList hook = new InsnList();
                                hook.add(new InsnNode(Opcodes.DUP));
                                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                hook.add(new InsnNode(Opcodes.SWAP));
                                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, VIA,
                                    "handleSelectorMouseWheel", "(Ljava/lang/Object;I)V", false));
                                method.instructions.insert(instruction, hook);
                                selectorScrollPatched = true;
                                changed = true;
                                break;
                            }
                        }
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

            if (selectorButtonPatched) {
                for (MethodNode method : node.methods) {
                    if (!"(IIF)V".equals(method.desc)) continue;
                    for (AbstractInsnNode instruction = method.instructions.getFirst();
                         instruction != null; instruction = instruction.getNext()) {
                        if (instruction instanceof MethodInsnNode) {
                            MethodInsnNode call = (MethodInsnNode) instruction;
                            if (node.superName.equals(call.owner) && "(IIF)V".equals(call.desc)) {
                                InsnList hook = new InsnList();
                                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                hook.add(new VarInsnNode(Opcodes.ILOAD, 1));
                                hook.add(new VarInsnNode(Opcodes.ILOAD, 2));
                                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, VIA,
                                    "drawSelectorPanel", "(Ljava/lang/Object;II)V", false));
                                method.instructions.insertBefore(instruction, hook);
                                selectorDrawPatched = true;
                                changed = true;
                                break;
                            }
                        }
                    }
                    if (selectorDrawPatched) break;
                }
            }

            if (!changed) return basicClass;
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            if (renderPatched) patchedRenderClasses++;
            if (frameLimitPatched) patchedFrameLimitClasses++;
            if (protocolPipelinePatched) patchedProtocolPipelineClasses++;
            if (compressionPatched) patchedCompressionClasses++;
            if (selectorButtonPatched) patchedSelectorButtonClasses++;
            if (selectorActionPatched) patchedSelectorActionClasses++;
            if (selectorDrawPatched) patchedSelectorDrawClasses++;
            if (selectorScrollPatched) patchedSelectorScrollClasses++;
            if (buttonThemePatched) patchedButtonThemeClasses++;
            if (screenBackgroundPatched) patchedScreenBackgroundClasses++;
            if (mainMenuPatched) patchedMainMenuClasses++;
            if (renderPatched) System.out.println("[Cylox] Background render suppression injected: " + node.name);
            if (frameLimitPatched) System.out.println("[Cylox] Background frame limit injected: " + node.name);
            if (protocolPipelinePatched) System.out.println("[Cylox] Via protocol pipeline hook injected: " + node.name);
            if (compressionPatched) System.out.println("[Cylox] Via compression hook injected: " + node.name);
            if (selectorButtonPatched) System.out.println("[Cylox] Manual Via selector button injected: " + node.name);
            if (selectorActionPatched) System.out.println("[Cylox] Manual Via selector action injected: " + node.name);
            if (selectorDrawPatched) System.out.println("[Cylox] Modern protocol selector panel injected: " + node.name);
            if (selectorScrollPatched) System.out.println("[Cylox] Protocol selector mouse-wheel hook injected: " + node.name);
            if (buttonThemePatched) System.out.println("[Cylox] Cylox button theme injected: " + node.name);
            if (screenBackgroundPatched) System.out.println("[Cylox] Cylox menu background injected: " + node.name);
            if (mainMenuPatched) System.out.println("[Cylox] Cylox main menu renderer injected: " + node.name);
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[Cylox] Transformer skipped " + name + ": "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
            return basicClass;
        }
    }


    private static boolean isMinecraftGuiClass(String name, String transformedName, String simpleName) {
        String expected = "net.minecraft.client.gui." + simpleName;
        return matchesClassName(name, expected, simpleName)
            || matchesClassName(transformedName, expected, simpleName);
    }

    private static boolean matchesClassName(String candidate, String expected, String simpleName) {
        if (candidate == null) return false;
        String normalized = candidate.replace('/', '.');
        return normalized.equals(expected) || normalized.equals(simpleName)
            || normalized.endsWith("." + simpleName);
    }

    private static boolean hasMethodDescriptor(ClassNode node, String descriptor) {
        for (MethodNode method : node.methods) if (descriptor.equals(method.desc)) return true;
        return false;
    }

    private static boolean hasClassString(ClassNode node, String expected) {
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (instruction instanceof LdcInsnNode && expected.equals(((LdcInsnNode) instruction).cst)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasClassMarkers(ClassNode node, String... markers) {
        Set<String> found = new HashSet<String>();
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (instruction instanceof LdcInsnNode) {
                    Object value = ((LdcInsnNode) instruction).cst;
                    if (value instanceof String) found.add((String) value);
                }
            }
        }
        for (String marker : markers) if (!found.contains(marker)) return false;
        return true;
    }

    private static boolean isButtonDrawMethod(MethodNode method) {
        if ((method.access & Opcodes.ACC_STATIC) != 0 || !"V".equals(
            Type.getReturnType(method.desc).getDescriptor())) return false;
        Type[] args = Type.getArgumentTypes(method.desc);
        if (args.length != 3 || args[0].getSort() != Type.OBJECT
            || args[1].getSort() != Type.INT || args[2].getSort() != Type.INT) return false;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode
                && "(IIIIII)V".equals(((MethodInsnNode) instruction).desc)) return true;
        }
        return false;
    }

    private static boolean isDefaultBackgroundMethod(ClassNode node, MethodNode method) {
        if ((method.access & Opcodes.ACC_STATIC) != 0 || !"()V".equals(method.desc)) return false;
        int stage = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            int opcode = instruction.getOpcode();
            if (opcode < 0) continue;
            if (stage == 0 && instruction instanceof VarInsnNode
                && opcode == Opcodes.ALOAD && ((VarInsnNode) instruction).var == 0) {
                stage++;
            } else if (stage == 1 && opcode == Opcodes.ICONST_0) {
                stage++;
            } else if (stage == 2 && instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!node.name.equals(call.owner) || !"(I)V".equals(call.desc)) return false;
                stage++;
            } else if (stage == 3 && opcode == Opcodes.RETURN) {
                stage++;
            } else {
                return false;
            }
        }
        return stage == 4;
    }

    private static void insertButtonTheme(MethodNode method) {
        LabelNode vanilla = new LabelNode();
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
        hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
        hook.add(new VarInsnNode(Opcodes.ILOAD, 2));
        hook.add(new VarInsnNode(Opcodes.ILOAD, 3));
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UI,
            "drawButton", "(Ljava/lang/Object;Ljava/lang/Object;II)Z", false));
        hook.add(new JumpInsnNode(Opcodes.IFEQ, vanilla));
        hook.add(new InsnNode(Opcodes.RETURN));
        hook.add(vanilla);
        hook.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(hook);
    }

    private static void replaceWithScreenBackground(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) method.localVariables.clear();
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UI,
            "drawScreenBackground", "(Ljava/lang/Object;)V", false));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        method.maxStack = 1;
        method.maxLocals = 1;
    }

    private static void replaceWithMainMenu(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) method.localVariables.clear();
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, UI,
            "drawMainMenu", "(Ljava/lang/Object;II)V", false));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        method.maxStack = 3;
        method.maxLocals = 4;
    }

    /** Finds the local-variable slot for the Channel argument in an instance method. */
    private static int channelArgumentSlot(String descriptor) {
        Type[] arguments = Type.getArgumentTypes(descriptor);
        int slot = 1; // instance method: slot 0 is this
        for (Type argument : arguments) {
            if (argument.getSort() == Type.OBJECT) {
                String name = argument.getInternalName();
                if ("io/netty/channel/Channel".equals(name)
                    || "io/netty/channel/socket/SocketChannel".equals(name)) {
                    return slot;
                }
            }
            slot += argument.getSize();
        }
        return -1;
    }

    private static void insertRenderGuard(MethodNode method) {
        LabelNode foreground = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BACKGROUND, "shouldRender", "()Z", false));
        guard.add(new JumpInsnNode(Opcodes.IFNE, foreground));
        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(foreground);
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
