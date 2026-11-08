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
import org.objectweb.asm.tree.TypeInsnNode;

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
    private static volatile int patchedFrameProfileClasses;
    private static volatile int patchedTerrainProfileClasses;
    private static volatile int patchedTerrainBreakdownClasses;
    private static volatile int patchedChunkUploadProfileClasses;
    private static volatile int patchedViaRewindEquipmentClasses;
    private static volatile int patchedVisibilityCacheClasses;
    private static volatile int patchedVisibilityGraphCacheClasses;
    private static volatile int patchedVisibilityInvalidationClasses;
    private static volatile int patchedVisibilityRangeInvalidationClasses;
    private static volatile int patchedNearBuildRadiusClasses;
    private static volatile int patchedPredictiveChunkPrebuildClasses;
    private static volatile int patchedEntitySleepingCacheClasses;
    private static volatile int patchedPlayerEquipmentGuardClasses;
    private static volatile int patchedNameTagProfileClasses;
    private static volatile int patchedFontWidthCacheClasses;
    private static volatile int patchedEntityProfileClasses;
    private static volatile int patchedBlockLayerProfileClasses;
    private static volatile int patchedParticleProfileClasses;
    private static volatile int patchedScoreboardTeamCreateGuardClasses;
    private static volatile int patchedScoreboardTeamRemoveGuardClasses;
    private static volatile int patchedScoreboardMemberRemoveGuardClasses;

    private static final String BACKGROUND = "dev/cylox/client/minecraft189/CyloxBackgroundHooks";
    private static final String VIA = "dev/cylox/client/minecraft189/CyloxProtocolBridge";
    private static final String UI = "dev/cylox/client/minecraft189/CyloxUiHooks";
    private static final String VIA_REWIND = "dev/cylox/client/minecraft189/CyloxViaRewindHooks";
    private static final String RENDER_HOOKS = "dev/cylox/client/minecraft189/CyloxVisibilityCacheHooks";
    private static final String TEXT_WIDTH_CACHE = "dev/cylox/client/minecraft189/CyloxTextWidthCache";
    private static final String CHUNK_BUILD_HOOKS = "dev/cylox/client/chunk/CyloxPredictiveChunkBuildHooks";
    private static final String VIA_REWIND_EQUIPMENT_REWRITER = "com/viaversion/viarewind/protocol/v1_9to1_8/rewriter/EntityPacketRewriter1_9";
    // In the pinned ViaRewind 4.2.0 Java 8 artifact, the tenth anonymous
    // PacketHandlers class is the SET_EQUIPPED_ITEM registration.
    private static final String VIA_REWIND_EQUIPMENT_HANDLER_CLASS = VIA_REWIND_EQUIPMENT_REWRITER + "$10";

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
        patchedFrameProfileClasses = 0;
        patchedTerrainProfileClasses = 0;
        patchedTerrainBreakdownClasses = 0;
        patchedChunkUploadProfileClasses = 0;
        patchedViaRewindEquipmentClasses = 0;
        patchedVisibilityCacheClasses = 0;
        patchedVisibilityGraphCacheClasses = 0;
        patchedVisibilityInvalidationClasses = 0;
        patchedVisibilityRangeInvalidationClasses = 0;
        patchedNearBuildRadiusClasses = 0;
        patchedPredictiveChunkPrebuildClasses = 0;
        patchedEntitySleepingCacheClasses = 0;
        patchedPlayerEquipmentGuardClasses = 0;
        patchedNameTagProfileClasses = 0;
        patchedFontWidthCacheClasses = 0;
        patchedEntityProfileClasses = 0;
        patchedBlockLayerProfileClasses = 0;
        patchedParticleProfileClasses = 0;
        patchedScoreboardTeamCreateGuardClasses = 0;
        patchedScoreboardTeamRemoveGuardClasses = 0;
        patchedScoreboardMemberRemoveGuardClasses = 0;
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
    public static int getPatchedFrameProfileClassCount() { return patchedFrameProfileClasses; }
    public static int getPatchedTerrainProfileClassCount() { return patchedTerrainProfileClasses; }
    public static int getPatchedTerrainBreakdownClassCount() { return patchedTerrainBreakdownClasses; }
    public static int getPatchedChunkUploadProfileClassCount() { return patchedChunkUploadProfileClasses; }
    public static int getPatchedViaRewindEquipmentClassCount() { return patchedViaRewindEquipmentClasses; }
    public static int getPatchedVisibilityCacheClassCount() { return patchedVisibilityCacheClasses; }
    public static int getPatchedVisibilityGraphCacheClassCount() { return patchedVisibilityGraphCacheClasses; }
    public static int getPatchedVisibilityInvalidationClassCount() { return patchedVisibilityInvalidationClasses; }
    public static int getPatchedVisibilityRangeInvalidationClassCount() { return patchedVisibilityRangeInvalidationClasses; }
    public static int getPatchedNearBuildRadiusClassCount() { return patchedNearBuildRadiusClasses; }
    public static int getPatchedPredictiveChunkPrebuildClassCount() { return patchedPredictiveChunkPrebuildClasses; }
    public static int getPatchedEntitySleepingCacheClassCount() { return patchedEntitySleepingCacheClasses; }
    public static int getPatchedPlayerEquipmentGuardClassCount() { return patchedPlayerEquipmentGuardClasses; }
    public static int getPatchedNameTagProfileClassCount() { return patchedNameTagProfileClasses; }
    public static int getPatchedFontWidthCacheClassCount() { return patchedFontWidthCacheClasses; }
    public static int getPatchedEntityProfileClassCount() { return patchedEntityProfileClasses; }
    public static int getPatchedBlockLayerProfileClassCount() { return patchedBlockLayerProfileClasses; }
    public static int getPatchedParticleProfileClassCount() { return patchedParticleProfileClasses; }
    public static int getPatchedScoreboardTeamCreateGuardClassCount() { return patchedScoreboardTeamCreateGuardClasses; }
    public static int getPatchedScoreboardTeamRemoveGuardClassCount() { return patchedScoreboardTeamRemoveGuardClasses; }
    public static int getPatchedScoreboardMemberRemoveGuardClassCount() { return patchedScoreboardMemberRemoveGuardClasses; }

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
            boolean frameProfilePatched = false;
            boolean terrainProfilePatched = false;
            boolean terrainBreakdownPatched = false;
            boolean chunkUploadProfilePatched = false;
            boolean visibilityCachePatched = false;
            boolean visibilityGraphCachePatched = false;
            boolean visibilityInvalidationPatched = false;
            boolean visibilityRangeInvalidationPatched = false;
            boolean nearBuildRadiusPatched = false;
            boolean predictiveChunkPrebuildPatched = false;
            boolean entitySleepingCachePatched = false;
            boolean playerEquipmentGuardPatched = false;
            boolean nameTagProfilePatched = false;
            boolean fontWidthCachePatched = false;
            boolean entityProfilePatched = false;
            boolean blockLayerProfilePatched = false;
            boolean particleProfilePatched = false;
            boolean scoreboardTeamCreateGuardPatched = false;
            boolean scoreboardTeamRemoveGuardPatched = false;
            boolean scoreboardMemberRemoveGuardPatched = false;
            boolean terrainClass = hasClassMarkers(node, "renderlistcamera", "culling", "build near");
            boolean scoreboardClass = hasClassMarkers(node,
                "A team with the name '", "' already exists!",
                "Player is either on another team or not on any team. Cannot remove from team '");
            boolean particleManagerClass = hasClassString(node, "textures/particle/particles.png");
            boolean fontRendererClass = hasClassString(node, "textures/font/ascii.png")
                || hasFontRendererWidthPattern(node);
            boolean nameTagRenderClass = hasClassString(node, "deadmau5");
            String fontUnicodeField = fontRendererClass ? findUnicodeField(node) : null;
            int visibilityGraphCandidates = 0;
            int blockLayerCandidates = 0;
            if (terrainClass) {
                for (MethodNode candidate : node.methods) {
                    if (isVisibilityGraphMethod(candidate)) visibilityGraphCandidates++;
                    if (isBlockLayerRenderMethod(candidate)) blockLayerCandidates++;
                }
            }
            boolean buttonWidgetClass = hasClassString(node, "textures/gui/widgets.png");
            boolean guiScreenBaseClass = isMinecraftGuiClass(name, transformedName, "GuiScreen")
                || (hasMethodDescriptor(node, "(IIF)V") && hasMethodDescriptor(node, "(I)V"));
            boolean mainMenuClass = isMinecraftGuiClass(name, transformedName, "GuiMainMenu")
                || hasClassMarkers(node, "menu.singleplayer", "menu.multiplayer", "menu.options", "menu.quit");

            for (MethodNode method : node.methods) {
                if ((method.access & Opcodes.ACC_STATIC) == 0) {
                    if (!playerEquipmentGuardPatched && isDirectPlayerEquipmentSetter(node, method)) {
                        insertPlayerEquipmentBoundsGuard(method);
                        playerEquipmentGuardPatched = true;
                        changed = true;
                        System.out.println("[Cylox] Player equipment-slot bounds guard injected: " + node.name + ".");
                    }

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
                        frameProfilePatched = true;
                        changed = true;
                    }

                    if (terrainClass && !terrainProfilePatched
                        && hasMarkers(method, "renderlistcamera", "culling", "build near")) {
                        insertTimedSection(method, 1);
                        terrainProfilePatched = true;
                        changed = true;
                    }

                    if (terrainClass && !terrainBreakdownPatched
                        && hasMarkers(method, "renderlistcamera", "culling", "build near")) {
                        terrainBreakdownPatched = insertTerrainBreakdown(method);
                        if (terrainBreakdownPatched) changed = true;
                    }

                    if (terrainClass && !nearBuildRadiusPatched
                        && hasMarkers(method, "renderlistcamera", "culling", "build near")) {
                        nearBuildRadiusPatched = patchNearBuildSyncRadius(node, method);
                        if (!nearBuildRadiusPatched) {
                            throw new IllegalStateException("Could not safely locate the near-chunk synchronous build predicate");
                        }
                        changed = true;
                    }

                    if (terrainClass && !predictiveChunkPrebuildPatched
                        && hasMarkers(method, "renderlistcamera", "culling", "build near")) {
                        predictiveChunkPrebuildPatched = insertPredictiveChunkPrebuild(method);
                        if (!predictiveChunkPrebuildPatched) {
                            throw new IllegalStateException("Could not safely locate setupTerrain player coordinates for predictive chunk pre-build ordering");
                        }
                        changed = true;
                    }

                    if (terrainClass && visibilityGraphCandidates == 1 && !visibilityCachePatched
                        && isVisibilityGraphMethod(method)) {
                        visibilityGraphCachePatched = injectVisibilityGraphCache(method);
                        if (!visibilityGraphCachePatched) {
                            throw new IllegalStateException("Could not safely locate the VisGraph snapshot injection points");
                        }
                        injectVisibilityCache(method);
                        visibilityCachePatched = true;
                        changed = true;
                    }

                    if (terrainClass && isTerrainCacheRangeInvalidationMethod(node, method)) {
                        insertVisibilityCacheRangeInvalidation(method);
                        visibilityRangeInvalidationPatched = true;
                        changed = true;
                    }

                    if (terrainClass && isTerrainWorldReplacementMethod(node, method)) {
                        insertVisibilityCacheInvalidation(method);
                        visibilityInvalidationPatched = true;
                        changed = true;
                    }

                    if (terrainClass && !entityProfilePatched
                        && hasMarkers(method, "entities", "blockentities")) {
                        entityProfilePatched = insertEntityRenderBreakdown(method);
                        if (entityProfilePatched) changed = true;
                    }

                    if (terrainClass && !entitySleepingCachePatched
                        && hasMarkers(method, "entities", "blockentities")) {
                        entitySleepingCachePatched = insertViewEntitySleepingCache(method);
                        if (entitySleepingCachePatched) changed = true;
                    }

                    if (terrainClass && blockLayerCandidates == 1 && !blockLayerProfilePatched
                        && isBlockLayerRenderMethod(method)) {
                        insertTimedSection(method, 4);
                        blockLayerProfilePatched = true;
                        changed = true;
                    }

                    if (terrainClass && !chunkUploadProfilePatched
                        && "(J)V".equals(method.desc) && hasMethodCallDescriptor(method, "(J)Z")) {
                        insertTimedSection(method, 2);
                        chunkUploadProfilePatched = true;
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

            if (fontRendererClass && fontUnicodeField != null) {
                for (MethodNode method : node.methods) {
                    if ((method.access & Opcodes.ACC_STATIC) == 0 && isStringWidthMethod(node, method)) {
                        injectTextWidthCache(node, method, fontUnicodeField);
                        fontWidthCachePatched = true;
                        changed = true;
                    }
                }
            }

            if (scoreboardClass) {
                scoreboardTeamCreateGuardPatched = patchScoreboardTeamCreateGuard(node);
                scoreboardTeamRemoveGuardPatched = patchScoreboardTeamRemoveGuard(node);
                scoreboardMemberRemoveGuardPatched = patchScoreboardMemberRemoveGuard(node);
                if (scoreboardTeamCreateGuardPatched || scoreboardTeamRemoveGuardPatched
                    || scoreboardMemberRemoveGuardPatched) changed = true;
            }

            if (nameTagRenderClass) {
                for (MethodNode method : node.methods) {
                    if ((method.access & Opcodes.ACC_STATIC) == 0 && isNameTagRenderMethod(method)) {
                        insertTimedSection(method, 12);
                        nameTagProfilePatched = true;
                        changed = true;
                    }
                }
            }

            if (particleManagerClass) {
                for (MethodNode method : node.methods) {
                    if ((method.access & Opcodes.ACC_STATIC) == 0 && isParticleRenderMethod(method)) {
                        insertTimedSection(method, 5);
                        particleProfilePatched = true;
                        changed = true;
                    }
                }
            }

            boolean viaRewindEquipmentPatched = false;
            // In the pinned 4.2.0 Java 8 artifact SET_EQUIPPED_ITEM is
            // the tenth anonymous PacketHandlers class; other packet handlers
            // have similar read/cancel/write bytecode and must remain untouched.
            if (VIA_REWIND_EQUIPMENT_HANDLER_CLASS.equals(node.name)) {
                viaRewindEquipmentPatched = injectViaRewindEquipmentGuard(node);
                if (viaRewindEquipmentPatched) changed = true;
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
            if (frameProfilePatched) patchedFrameProfileClasses++;
            if (terrainProfilePatched) patchedTerrainProfileClasses++;
            if (terrainBreakdownPatched) patchedTerrainBreakdownClasses++;
            if (chunkUploadProfilePatched) patchedChunkUploadProfileClasses++;
            if (viaRewindEquipmentPatched) patchedViaRewindEquipmentClasses++;
            if (visibilityCachePatched) patchedVisibilityCacheClasses++;
            if (visibilityGraphCachePatched) patchedVisibilityGraphCacheClasses++;
            if (visibilityInvalidationPatched) patchedVisibilityInvalidationClasses++;
            if (visibilityRangeInvalidationPatched) patchedVisibilityRangeInvalidationClasses++;
            if (nearBuildRadiusPatched) patchedNearBuildRadiusClasses++;
            if (predictiveChunkPrebuildPatched) patchedPredictiveChunkPrebuildClasses++;
            if (entitySleepingCachePatched) patchedEntitySleepingCacheClasses++;
            if (playerEquipmentGuardPatched) patchedPlayerEquipmentGuardClasses++;
            if (nameTagProfilePatched) patchedNameTagProfileClasses++;
            if (fontWidthCachePatched) patchedFontWidthCacheClasses++;
            if (entityProfilePatched) patchedEntityProfileClasses++;
            if (blockLayerProfilePatched) patchedBlockLayerProfileClasses++;
            if (particleProfilePatched) patchedParticleProfileClasses++;
            if (scoreboardTeamCreateGuardPatched) patchedScoreboardTeamCreateGuardClasses++;
            if (scoreboardTeamRemoveGuardPatched) patchedScoreboardTeamRemoveGuardClasses++;
            if (scoreboardMemberRemoveGuardPatched) patchedScoreboardMemberRemoveGuardClasses++;
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
            if (frameProfilePatched) System.out.println("[Cylox] Opt-in frame profiler injected: " + node.name);
            if (terrainProfilePatched) System.out.println("[Cylox] Opt-in terrain profiler injected: " + node.name);
            if (chunkUploadProfilePatched) System.out.println("[Cylox] Opt-in chunk-upload profiler injected: " + node.name);
            if (viaRewindEquipmentPatched) System.out.println("[Cylox] ViaRewind local-player equipment guard injected: " + node.name);
            if (visibilityCachePatched) System.out.println("[Cylox] Terrain visibility cache injected: " + node.name);
            if (visibilityGraphCachePatched) System.out.println("[Cylox] Terrain visibility graph snapshot cache injected: " + node.name);
            if (visibilityInvalidationPatched) System.out.println("[Cylox] Terrain visibility cache world-reset invalidation injected: " + node.name);
            if (visibilityRangeInvalidationPatched) System.out.println("[Cylox] Terrain visibility cache section-range invalidation injected: " + node.name);
            if (nearBuildRadiusPatched) System.out.println("[Cylox] Near synchronous chunk-build radius reduced to the current section: " + node.name);
            if (predictiveChunkPrebuildPatched) System.out.println("[Cylox] Predictive ahead-of-motion chunk pre-build ordering injected: " + node.name);
            if (entitySleepingCachePatched) System.out.println("[Cylox] Reused render-view sleeping state across entity loops: " + node.name);
            if (nameTagProfilePatched) System.out.println("[Cylox] Opt-in floating nameplate profiler injected: " + node.name);
            if (fontWidthCachePatched) System.out.println("[Cylox] Bounded FontRenderer string-width cache injected: " + node.name);
            if (entityProfilePatched) System.out.println("[Cylox] Opt-in entity renderer profiler injected: " + node.name);
            if (blockLayerProfilePatched) System.out.println("[Cylox] Opt-in block-layer profiler injected: " + node.name);
            if (particleProfilePatched) System.out.println("[Cylox] Opt-in particle profiler injected: " + node.name);
            if (scoreboardTeamCreateGuardPatched) System.out.println("[Cylox] Idempotent scoreboard team-create guard injected: " + node.name);
            if (scoreboardTeamRemoveGuardPatched) System.out.println("[Cylox] Null-safe scoreboard team-remove guard injected: " + node.name);
            if (scoreboardMemberRemoveGuardPatched) System.out.println("[Cylox] Membership-safe scoreboard team-player removal guard injected: " + node.name);
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[Cylox] Transformer skipped " + name + ": "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
            return basicClass;
        }
    }



    /**
     * Adds the upstream ViaRewind local-player equipment cancellation to the
     * anonymous SET_EQUIPPED_ITEM PacketHandlers class in the Java 8 artifact.
     */
    private static boolean injectViaRewindEquipmentGuard(ClassNode node) {
        MethodNode target = null;
        int matches = 0;
        for (MethodNode method : node.methods) {
            if (!"(Lcom/viaversion/viaversion/api/protocol/packet/PacketWrapper;)V".equals(method.desc)) {
                continue;
            }
            boolean readsPacket = false;
            boolean cancelsPacket = false;
            boolean writesPacket = false;
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!"com/viaversion/viaversion/api/protocol/packet/PacketWrapper".equals(call.owner)) continue;
                if ("read".equals(call.name)
                    && "(Lcom/viaversion/viaversion/api/type/Type;)Ljava/lang/Object;".equals(call.desc)) {
                    readsPacket = true;
                } else if ("cancel".equals(call.name) && "()V".equals(call.desc)) {
                    cancelsPacket = true;
                } else if ("write".equals(call.name)
                    && "(Lcom/viaversion/viaversion/api/type/Type;Ljava/lang/Object;)V".equals(call.desc)) {
                    writesPacket = true;
                }
            }
            if (readsPacket && cancelsPacket && writesPacket) {
                target = method;
                matches++;
            }
        }
        // Fail closed: patch only the unique SET_EQUIPPED_ITEM lambda. A changed
        // upstream class shape must fail CI verification instead of patching a
        // different packet handler.
        if (matches != 1 || target == null) {
            for (MethodNode method : node.methods) {
                if (method.desc.indexOf("PacketWrapper") < 0) continue;
                StringBuilder diagnostic = new StringBuilder("[Cylox] ViaRewind candidate ")
                    .append(method.name).append(method.desc).append(":");
                for (AbstractInsnNode instruction = method.instructions.getFirst();
                     instruction != null; instruction = instruction.getNext()) {
                    if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode) instruction;
                        if ("read".equals(call.name) || "cancel".equals(call.name)
                            || "setCancelled".equals(call.name) || "write".equals(call.name)) {
                            diagnostic.append(" ").append(call.owner).append(".")
                                .append(call.name).append(call.desc);
                        }
                    }
                }
                System.out.println(diagnostic.toString());
            }
            return false;
        }

        int wrapperSlot = (target.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        LabelNode continueVanilla = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(Opcodes.ALOAD, wrapperSlot));
        guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, VIA_REWIND,
            "cancelOwnPlayerEquipment", "(Ljava/lang/Object;)Z", false));
        guard.add(new JumpInsnNode(Opcodes.IFEQ, continueVanilla));
        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(continueVanilla);
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        target.instructions.insert(guard);
        return true;
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


    /**
     * Makes duplicate vanilla team-create operations idempotent. A duplicate
     * create packet reuses the current team so the remainder of handleTeams
     * can still apply its display fields and member list.
     */
    private static boolean patchScoreboardTeamCreateGuard(ClassNode owner) {
        MethodNode target = null;
        String getterName = null;
        String teamInternalName = null;
        int matches = 0;

        for (MethodNode method : owner.methods) {
            Type[] arguments = Type.getArgumentTypes(method.desc);
            Type returnType = Type.getReturnType(method.desc);
            if (arguments.length != 1 || !"Ljava/lang/String;".equals(arguments[0].getDescriptor())
                || returnType.getSort() != Type.OBJECT
                || !hasMarkers(method, "A team with the name '", "' already exists!")) {
                continue;
            }

            String candidateGetter = findUniqueMethodCallName(method, owner.name, method.desc);
            if (candidateGetter == null) continue;
            matches++;
            target = method;
            getterName = candidateGetter;
            teamInternalName = returnType.getInternalName();
        }

        if (matches != 1 || target == null) return false;

        LabelNode existingTeam = new LabelNode();
        LabelNode createNewTeam = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
        guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
        guard.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner.name,
            getterName, target.desc, false));
        guard.add(new InsnNode(Opcodes.DUP));
        guard.add(new JumpInsnNode(Opcodes.IFNONNULL, existingTeam));
        guard.add(new InsnNode(Opcodes.POP));
        guard.add(new JumpInsnNode(Opcodes.GOTO, createNewTeam));
        guard.add(existingTeam);
        guard.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] {teamInternalName}));
        guard.add(new InsnNode(Opcodes.ARETURN));
        guard.add(createNewTeam);
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        target.instructions.insert(guard);
        return true;
    }

    /**
     * A stale remove-team packet must not pass null into Scoreboard.removeTeam.
     */
    private static boolean patchScoreboardTeamRemoveGuard(ClassNode owner) {
        String teamDescriptor = findScoreboardTeamDescriptor(owner);
        if (teamDescriptor == null) return false;
        String teamInternalName = Type.getType(teamDescriptor).getInternalName();
        String targetDescriptor = "(" + teamDescriptor + ")V";
        MethodNode target = null;
        int matches = 0;

        for (MethodNode method : owner.methods) {
            if (!targetDescriptor.equals(method.desc)
                || !hasMethodCallDescriptor(method, "()Ljava/lang/String;")
                || !hasMethodCallDescriptor(method, "()Ljava/util/Collection;")
                || !hasMethodCall(method, "java/util/Map", "(Ljava/lang/Object;)Ljava/lang/Object;")) {
                continue;
            }
            target = method;
            matches++;
        }

        if (matches != 1 || target == null) return false;

        LabelNode hasTeam = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
        guard.add(new JumpInsnNode(Opcodes.IFNONNULL, hasTeam));
        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(hasTeam);
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        target.instructions.insert(guard);
        return true;
    }

    /**
     * Team-member removals are idempotent: ignore a missing team or a player
     * that is no longer assigned to the packet's team.
     */
    private static boolean patchScoreboardMemberRemoveGuard(ClassNode owner) {
        String teamDescriptor = findScoreboardTeamDescriptor(owner);
        if (teamDescriptor == null) return false;
        String teamInternalName = Type.getType(teamDescriptor).getInternalName();
        String targetDescriptor = "(Ljava/lang/String;" + teamDescriptor + ")V";
        String getterDescriptor = "(Ljava/lang/String;)" + teamDescriptor;
        MethodNode target = null;
        String getterName = null;
        int matches = 0;

        for (MethodNode method : owner.methods) {
            if (!targetDescriptor.equals(method.desc)
                || !hasMarkers(method,
                    "Player is either on another team or not on any team. Cannot remove from team '")) {
                continue;
            }
            String candidateGetter = findUniqueMethodCallName(method, owner.name, getterDescriptor);
            if (candidateGetter == null) continue;
            target = method;
            getterName = candidateGetter;
            matches++;
        }

        if (matches != 1 || target == null) return false;

        LabelNode skipRemoval = new LabelNode();
        LabelNode removeMember = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(Opcodes.ALOAD, 2));
        guard.add(new JumpInsnNode(Opcodes.IFNULL, skipRemoval));
        guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
        guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
        guard.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner.name,
            getterName, getterDescriptor, false));
        guard.add(new VarInsnNode(Opcodes.ALOAD, 2));
        guard.add(new JumpInsnNode(Opcodes.IF_ACMPEQ, removeMember));
        guard.add(skipRemoval);
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(removeMember);
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        target.instructions.insert(guard);
        return true;
    }

    private static String findScoreboardTeamDescriptor(ClassNode owner) {
        String descriptor = null;
        int matches = 0;
        for (MethodNode method : owner.methods) {
            Type[] arguments = Type.getArgumentTypes(method.desc);
            Type returnType = Type.getReturnType(method.desc);
            if (arguments.length == 1 && "Ljava/lang/String;".equals(arguments[0].getDescriptor())
                && returnType.getSort() == Type.OBJECT
                && hasMarkers(method, "A team with the name '", "' already exists!")) {
                descriptor = returnType.getDescriptor();
                matches++;
            }
        }
        return matches == 1 ? descriptor : null;
    }

    private static String findUniqueMethodCallName(MethodNode method, String callOwner, String callDescriptor) {
        String name = null;
        int matches = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() == Opcodes.INVOKEVIRTUAL
                && callOwner.equals(call.owner) && callDescriptor.equals(call.desc)) {
                name = call.name;
                matches++;
            }
        }
        return matches == 1 ? name : null;
    }

    private static boolean hasMethodCall(MethodNode method, String callOwner, String callDescriptor) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (callOwner.equals(call.owner) && callDescriptor.equals(call.desc)) return true;
        }
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
        java.util.List<AbstractInsnNode> originalReturns = new java.util.ArrayList<AbstractInsnNode>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.RETURN) originalReturns.add(instruction);
        }

        LabelNode foreground = new LabelNode();
        InsnNode backgroundReturn = new InsnNode(Opcodes.RETURN);
        InsnList guard = new InsnList();
        guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BACKGROUND, "shouldRender", "()Z", false));
        guard.add(new JumpInsnNode(Opcodes.IFNE, foreground));
        guard.add(backgroundReturn);
        guard.add(foreground);
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
            "dev/cylox/client/minecraft189/CyloxPerformanceMonitor", "beginFrame", "()V", false));
        method.instructions.insert(guard);

        for (AbstractInsnNode instruction : originalReturns) {
            method.instructions.insertBefore(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC,
                "dev/cylox/client/minecraft189/CyloxPerformanceMonitor", "endFrame", "()V", false));
        }
    }

    /**
     * Reorders only the existing dirty RenderChunk set at the end of
     * RenderGlobal.setupTerrain. updateChunks() later submits this LinkedHashSet
     * to the vanilla worker queue in iteration order; no task is duplicated.
     */
    private static boolean insertPredictiveChunkPrebuild(MethodNode method) {
        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (arguments.length != 5 || arguments[0].getSort() != Type.OBJECT
            || arguments[1].getSort() != Type.DOUBLE || arguments[2].getSort() != Type.OBJECT
            || arguments[3].getSort() != Type.INT || arguments[4].getSort() != Type.BOOLEAN
            || Type.getReturnType(method.desc).getSort() != Type.VOID) return false;

        String entityOwner = arguments[0].getInternalName();
        java.util.List<org.objectweb.asm.tree.FieldInsnNode> positionFields =
            new java.util.ArrayList<org.objectweb.asm.tree.FieldInsnNode>();
        Set<String> seenFields = new HashSet<String>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof org.objectweb.asm.tree.FieldInsnNode)) continue;
            org.objectweb.asm.tree.FieldInsnNode field =
                (org.objectweb.asm.tree.FieldInsnNode) instruction;
            if (field.getOpcode() == Opcodes.GETFIELD && entityOwner.equals(field.owner)
                && "D".equals(field.desc) && seenFields.add(field.name)) {
                positionFields.add(field);
            }
        }

        // setupTerrain reads Entity position X, Y and Z in this order before
        // building its camera/frustum positions. Fail closed on another layout.
        if (positionFields.size() < 3
            || positionFields.get(0).name.equals(positionFields.get(2).name)) return false;

        java.util.List<AbstractInsnNode> returns = new java.util.ArrayList<AbstractInsnNode>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.RETURN) returns.add(instruction);
        }
        if (returns.isEmpty()) return false;

        String xField = positionFields.get(0).name;
        String zField = positionFields.get(2).name;
        for (AbstractInsnNode returnInstruction : returns) {
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                entityOwner, xField, "D"));
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD,
                entityOwner, zField, "D"));
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CHUNK_BUILD_HOOKS,
                "onTerrainPrepared", "(Ljava/lang/Object;Ljava/lang/Object;DD)V", false));
            method.instructions.insertBefore(returnInstruction, hook);
        }
        return true;
    }

    private static boolean isVisibilityGraphMethod(MethodNode method) {
        if ((method.access & Opcodes.ACC_STATIC) != 0
            || !"Ljava/util/Set;".equals(Type.getReturnType(method.desc).getDescriptor())) return false;
        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (arguments.length != 1 || arguments[0].getSort() != Type.OBJECT) return false;
        boolean createsGraph = false;
        boolean iterates = false;
        boolean returnsSetFromCall = false;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof org.objectweb.asm.tree.TypeInsnNode
                && instruction.getOpcode() == Opcodes.NEW) createsGraph = true;
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("java/util/Iterator".equals(call.owner)
                    && "hasNext".equals(call.name) && "()Z".equals(call.desc)) iterates = true;
                if ("Ljava/util/Set;".equals(Type.getReturnType(call.desc).getDescriptor())) returnsSetFromCall = true;
            }
        }
        return createsGraph && iterates && returnsSetFromCall;
    }

    private static boolean isBlockLayerRenderMethod(MethodNode method) {
        if ((method.access & Opcodes.ACC_STATIC) != 0
            || Type.getReturnType(method.desc).getSort() != Type.INT) return false;
        Type[] arguments = Type.getArgumentTypes(method.desc);
        return arguments.length == 4 && arguments[0].getSort() == Type.OBJECT
            && arguments[1].getSort() == Type.DOUBLE && arguments[2].getSort() == Type.INT
            && arguments[3].getSort() == Type.OBJECT;
    }

    private static boolean isParticleRenderMethod(MethodNode method) {
        if ((method.access & Opcodes.ACC_STATIC) != 0 || Type.getReturnType(method.desc).getSort() != Type.VOID) return false;
        Type[] arguments = Type.getArgumentTypes(method.desc);
        return arguments.length == 2 && arguments[0].getSort() == Type.OBJECT
            && arguments[1].getSort() == Type.FLOAT;
    }

    private static boolean patchNearBuildSyncRadius(ClassNode owner, MethodNode terrainMethod) {
        MethodNode predicate = null;
        for (AbstractInsnNode instruction = terrainMethod.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (!owner.name.equals(call.owner)
                || Type.getReturnType(call.desc).getSort() != Type.BOOLEAN) continue;
            Type[] arguments = Type.getArgumentTypes(call.desc);
            if (arguments.length != 2 || arguments[0].getSort() != Type.OBJECT
                || arguments[1].getSort() != Type.OBJECT) continue;
            for (MethodNode candidate : owner.methods) {
                if (candidate.name.equals(call.name) && candidate.desc.equals(call.desc)) {
                    if (predicate != null) return false;
                    predicate = candidate;
                }
            }
        }
        if (predicate == null) return false;

        int integerDistanceOperations = 0;
        String distanceMathOwner = null;
        java.util.List<AbstractInsnNode> radiusConstants = new java.util.ArrayList<AbstractInsnNode>();
        for (AbstractInsnNode instruction = predicate.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == Opcodes.INVOKESTATIC && "(I)I".equals(call.desc)) {
                    if (distanceMathOwner == null) distanceMathOwner = call.owner;
                    if (!distanceMathOwner.equals(call.owner)) return false;
                    integerDistanceOperations++;
                }
            }
            if (Integer.valueOf(16).equals(readIntegerConstant(instruction))) {
                radiusConstants.add(instruction);
            }
        }

        // Vanilla 1.8.9 tests absolute X/Y/Z section-origin distances against 16.
        // Fail closed if the verified three-axis bytecode pattern ever changes.
        if (integerDistanceOperations != 3 || radiusConstants.size() != 3) return false;
        for (AbstractInsnNode instruction : radiusConstants) {
            replaceIntegerConstant(instruction, 8);
        }
        return true;
    }

    private static Integer readIntegerConstant(AbstractInsnNode instruction) {
        if (instruction instanceof org.objectweb.asm.tree.IntInsnNode) {
            org.objectweb.asm.tree.IntInsnNode value = (org.objectweb.asm.tree.IntInsnNode) instruction;
            if (value.getOpcode() == Opcodes.BIPUSH || value.getOpcode() == Opcodes.SIPUSH) {
                return Integer.valueOf(value.operand);
            }
        }
        if (instruction instanceof LdcInsnNode) {
            Object value = ((LdcInsnNode) instruction).cst;
            if (value instanceof Integer) return (Integer) value;
        }
        return null;
    }

    private static void replaceIntegerConstant(AbstractInsnNode instruction, int value) {
        if (instruction instanceof org.objectweb.asm.tree.IntInsnNode) {
            ((org.objectweb.asm.tree.IntInsnNode) instruction).operand = value;
        } else if (instruction instanceof LdcInsnNode) {
            ((LdcInsnNode) instruction).cst = Integer.valueOf(value);
        } else {
            throw new IllegalArgumentException("Unsupported integer constant instruction: "
                + instruction.getClass().getName());
        }
    }

    private static boolean isTerrainCacheRangeInvalidationMethod(ClassNode owner, MethodNode method) {
        if ("<init>".equals(method.name) || "<clinit>".equals(method.name)
            || (method.access & Opcodes.ACC_STATIC) != 0
            || !"(IIIIII)V".equals(method.desc)) return false;

        // Only hook the backing markBlocksForUpdate method that delegates to
        // ViewFrustum. Its public range wrapper calls back into RenderGlobal and
        // must not clear the cache a second time.
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("(IIIIII)V".equals(call.desc) && !owner.name.equals(call.owner)) return true;
            }
        }
        return false;
    }

    private static boolean isTerrainWorldReplacementMethod(ClassNode owner, MethodNode method) {
        // A constructor may store its world argument and call an instance refresh method,
        // which resembles the world-replacement pattern. Never inject before super() or this().
        if ("<init>".equals(method.name) || "<clinit>".equals(method.name)) return false;
        if ((method.access & Opcodes.ACC_STATIC) != 0
            || Type.getReturnType(method.desc).getSort() != Type.VOID) return false;

        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (arguments.length != 1 || arguments[0].getSort() != Type.OBJECT) return false;
        boolean writesWorldField = false;
        boolean callsOwnNoArgMethod = false;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode) {
                org.objectweb.asm.tree.FieldInsnNode field = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                if (field.getOpcode() == Opcodes.PUTFIELD && owner.name.equals(field.owner)
                    && arguments[0].getDescriptor().equals(field.desc)) writesWorldField = true;
            } else if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (owner.name.equals(call.owner) && "()V".equals(call.desc)) callsOwnNoArgMethod = true;
            }
        }
        return writesWorldField && callsOwnNoArgMethod;
    }

    private static boolean isDirectPlayerEquipmentSetter(ClassNode owner, MethodNode method) {
        if ((method.access & Opcodes.ACC_STATIC) != 0
            || Type.getReturnType(method.desc).getSort() != Type.VOID) return false;
        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (arguments.length != 2 || arguments[0].getSort() != Type.INT
            || arguments[1].getSort() != Type.OBJECT) return false;

        int arrayStores = 0;
        boolean readsSlot = false;
        org.objectweb.asm.tree.FieldInsnNode inventoryField = null;
        org.objectweb.asm.tree.FieldInsnNode armorArrayField = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.AASTORE) arrayStores++;
            if (instruction instanceof VarInsnNode && instruction.getOpcode() == Opcodes.ILOAD
                && ((VarInsnNode) instruction).var == 1) readsSlot = true;
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode
                && instruction.getOpcode() == Opcodes.GETFIELD) {
                org.objectweb.asm.tree.FieldInsnNode field = (org.objectweb.asm.tree.FieldInsnNode) instruction;
                if (owner.name.equals(field.owner) && field.desc.startsWith("L")) inventoryField = field;
                if (field.desc.startsWith("[L")) armorArrayField = field;
            }
        }
        return arrayStores == 1 && readsSlot && inventoryField != null && armorArrayField != null
            && Type.getType(inventoryField.desc).getInternalName().equals(armorArrayField.owner)
            && ("[" + arguments[1].getDescriptor()).equals(armorArrayField.desc);
    }

    private static void insertPlayerEquipmentBoundsGuard(MethodNode method) {
        LabelNode nonNegative = new LabelNode();
        LabelNode validSlot = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(Opcodes.ILOAD, 1));
        guard.add(new JumpInsnNode(Opcodes.IFGE, nonNegative));
        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(nonNegative);
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        guard.add(new VarInsnNode(Opcodes.ILOAD, 1));
        guard.add(new InsnNode(Opcodes.ICONST_4));
        guard.add(new JumpInsnNode(Opcodes.IF_ICMPLT, validSlot));
        guard.add(new InsnNode(Opcodes.RETURN));
        guard.add(validSlot);
        guard.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(guard);
    }

    /**
     * Reuses a pristine VisGraph opaque-block snapshot for camera positions in
     * the same 16x16x16 section. A fresh graph is reconstructed on every hit
     * because vanilla's flood-fill mutates the graph's BitSet.
     */
    private static boolean injectVisibilityGraphCache(MethodNode method) {
        Type[] methodArguments = Type.getArgumentTypes(method.desc);
        if (methodArguments.length != 1 || methodArguments[0].getSort() != Type.OBJECT) {
            System.err.println("[Cylox] VisGraph cache argument mismatch: " + method.desc);
            return false;
        }
        String blockPositionDescriptor = methodArguments[0].getDescriptor();

        MethodInsnNode visibilityCall = null;
        String graphOwner = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            Type[] arguments = Type.getArgumentTypes(call.desc);
            if (arguments.length == 1 && arguments[0].getDescriptor().equals(blockPositionDescriptor)
                && "Ljava/util/Set;".equals(Type.getReturnType(call.desc).getDescriptor())) {
                visibilityCall = call;
                graphOwner = call.owner;
                break;
            }
        }
        if (visibilityCall == null || graphOwner == null) {
            System.err.println("[Cylox] VisGraph cache could not locate the Set-returning graph call in " + method.desc);
            return false;
        }

        int graphLocal = -1;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof TypeInsnNode) || instruction.getOpcode() != Opcodes.NEW
                || !graphOwner.equals(((TypeInsnNode) instruction).desc)) continue;
            AbstractInsnNode candidate = nextRealInstruction(instruction);
            while (candidate != null) {
                if (candidate instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) candidate;
                    if (graphOwner.equals(call.owner) && "<init>".equals(call.name)
                        && "()V".equals(call.desc)) {
                        AbstractInsnNode store = nextRealInstruction(candidate);
                        if (store instanceof VarInsnNode && store.getOpcode() == Opcodes.ASTORE) {
                            graphLocal = ((VarInsnNode) store).var;
                        }
                        break;
                    }
                }
                candidate = nextRealInstruction(candidate);
            }
            if (graphLocal >= 0) break;
        }
        if (graphLocal < 0) {
            System.err.println("[Cylox] VisGraph cache could not locate the " + graphOwner + " local in " + method.desc);
            return false;
        }

        int sectionLocal = -1;
        AbstractInsnNode worldLoad = null;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            Type[] arguments = Type.getArgumentTypes(call.desc);
            Type result = Type.getReturnType(call.desc);
            if (arguments.length != 1 || !arguments[0].getDescriptor().equals(blockPositionDescriptor)
                || result.getSort() != Type.OBJECT) continue;
            AbstractInsnNode positionLoad = previousRealInstruction(instruction);
            if (!(positionLoad instanceof VarInsnNode) || positionLoad.getOpcode() != Opcodes.ALOAD) continue;
            AbstractInsnNode worldField = previousRealInstruction(positionLoad);
            if (!(worldField instanceof org.objectweb.asm.tree.FieldInsnNode)
                || worldField.getOpcode() != Opcodes.GETFIELD) continue;
            AbstractInsnNode ownerLoad = previousRealInstruction(worldField);
            if (!(ownerLoad instanceof VarInsnNode) || ownerLoad.getOpcode() != Opcodes.ALOAD
                || ((VarInsnNode) ownerLoad).var != 0) continue;
            sectionLocal = ((VarInsnNode) positionLoad).var;
            worldLoad = ownerLoad;
            break;
        }
        if (sectionLocal < 0 || worldLoad == null) {
            System.err.println("[Cylox] VisGraph cache could not locate section local before the chunk lookup in " + method.desc);
            return false;
        }

        AbstractInsnNode positionLoad = previousRealInstruction(visibilityCall);
        AbstractInsnNode graphLoad = previousRealInstruction(positionLoad);
        if (!(positionLoad instanceof VarInsnNode) || positionLoad.getOpcode() != Opcodes.ALOAD
            || ((VarInsnNode) positionLoad).var != 1
            || !(graphLoad instanceof VarInsnNode) || graphLoad.getOpcode() != Opcodes.ALOAD
            || ((VarInsnNode) graphLoad).var != graphLocal) {
            System.err.println("[Cylox] VisGraph cache could not locate receiver/position loads before flood-fill; found "
                + instructionDescription(graphLoad) + " and " + instructionDescription(positionLoad)
                + ", expected ALOAD " + graphLocal + " then ALOAD 1");
            return false;
        }

        InsnList lookup = new InsnList();
        LabelNode cacheMiss = new LabelNode();
        lookup.add(new VarInsnNode(Opcodes.ALOAD, 0));
        lookup.add(new VarInsnNode(Opcodes.ALOAD, sectionLocal));
        lookup.add(new VarInsnNode(Opcodes.ALOAD, graphLocal));
        lookup.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_HOOKS,
            "restoreCachedVisibilityGraph",
            "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z", false));
        lookup.add(new JumpInsnNode(Opcodes.IFEQ, cacheMiss));
        lookup.add(new VarInsnNode(Opcodes.ALOAD, graphLocal));
        lookup.add(new VarInsnNode(Opcodes.ALOAD, 1));
        lookup.add(new MethodInsnNode(visibilityCall.getOpcode(), graphOwner,
            visibilityCall.name, visibilityCall.desc, visibilityCall.itf));
        lookup.add(new InsnNode(Opcodes.ARETURN));
        lookup.add(cacheMiss);
        lookup.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insertBefore(worldLoad, lookup);

        InsnList snapshot = new InsnList();
        snapshot.add(new VarInsnNode(Opcodes.ALOAD, 0));
        snapshot.add(new VarInsnNode(Opcodes.ALOAD, sectionLocal));
        snapshot.add(new VarInsnNode(Opcodes.ALOAD, graphLocal));
        snapshot.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_HOOKS,
            "cacheVisibilityGraph", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V", false));
        method.instructions.insertBefore(graphLoad, snapshot);
        return true;
    }
    private static String instructionDescription(AbstractInsnNode instruction) {
        if (instruction == null) return "null";
        if (instruction instanceof VarInsnNode) {
            VarInsnNode variable = (VarInsnNode) instruction;
            return "var-opcode-" + instruction.getOpcode() + "-local-" + variable.var;
        }
        if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode) {
            org.objectweb.asm.tree.FieldInsnNode field = (org.objectweb.asm.tree.FieldInsnNode) instruction;
            return "field-opcode-" + instruction.getOpcode() + "-" + field.owner + "." + field.name + field.desc;
        }
        if (instruction instanceof MethodInsnNode) {
            MethodInsnNode call = (MethodInsnNode) instruction;
            return "method-" + call.owner + "." + call.name + call.desc;
        }
        return "opcode-" + instruction.getOpcode() + "-" + instruction.getClass().getSimpleName();
    }

    private static AbstractInsnNode nextRealInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode current = instruction == null ? null : instruction.getNext();
        while (current != null && current.getOpcode() < 0) current = current.getNext();
        return current;
    }

    private static AbstractInsnNode previousRealInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode current = instruction == null ? null : instruction.getPrevious();
        while (current != null && current.getOpcode() < 0) current = current.getPrevious();
        return current;
    }

    /**
     * RenderGlobal checks the render-view player's sleeping state inside both
     * entity loops. That state is constant during one renderEntities call, so
     * memoize the boolean and skip repeated getter/virtual-call work without
     * changing entity visibility or draw decisions.
     */
    /**
     * RenderGlobal queries the same render-view player's sleeping state from two
     * separate entity loops. Cache that state once from the render-view argument
     * for this invocation and replace only the two matching vanilla getter chains.
     * No entity visibility, frustum or draw decisions are altered.
     */
    private static boolean insertViewEntitySleepingCache(MethodNode method) {
        if (Type.getReturnType(method.desc).getSort() != Type.VOID) return false;
        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (arguments.length != 3 || arguments[0].getSort() != Type.OBJECT
            || arguments[1].getSort() != Type.OBJECT || arguments[2].getSort() != Type.FLOAT) return false;

        java.util.Map<String, java.util.List<MethodInsnNode>> groups =
            new java.util.HashMap<String, java.util.List<MethodInsnNode>>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode) || instruction.getOpcode() != Opcodes.INVOKEVIRTUAL
                || ! "()Z".equals(((MethodInsnNode) instruction).desc)) continue;
            AbstractInsnNode castInstruction = previousRealInstruction(instruction);
            AbstractInsnNode getterInstruction = previousRealInstruction(castInstruction);
            if (!(castInstruction instanceof TypeInsnNode) || castInstruction.getOpcode() != Opcodes.CHECKCAST
                || !(getterInstruction instanceof MethodInsnNode)) continue;
            MethodInsnNode getter = (MethodInsnNode) getterInstruction;
            if (Type.getArgumentTypes(getter.desc).length != 0
                || Type.getReturnType(getter.desc).getSort() != Type.OBJECT) continue;
            String key = getter.owner + "." + getter.name + getter.desc
                + "|" + ((TypeInsnNode) castInstruction).desc
                + "|" + ((MethodInsnNode) instruction).owner + "." + ((MethodInsnNode) instruction).name
                + ((MethodInsnNode) instruction).desc;
            java.util.List<MethodInsnNode> calls = groups.get(key);
            if (calls == null) {
                calls = new java.util.ArrayList<MethodInsnNode>();
                groups.put(key, calls);
            }
            calls.add((MethodInsnNode) instruction);
        }

        java.util.List<MethodInsnNode> targets = null;
        for (java.util.List<MethodInsnNode> calls : groups.values()) {
            if (calls.size() == 2) {
                if (targets != null) return false;
                targets = calls;
            } else if (calls.size() > 2) {
                return false;
            }
        }
        if (targets == null) return false;

        MethodInsnNode sampleCall = targets.get(0);
        AbstractInsnNode sampleCast = previousRealInstruction(sampleCall);
        AbstractInsnNode sampleGetterNode = previousRealInstruction(sampleCast);
        if (!(sampleCast instanceof TypeInsnNode) || !(sampleGetterNode instanceof MethodInsnNode)) return false;
        TypeInsnNode cast = (TypeInsnNode) sampleCast;
        MethodInsnNode getter = (MethodInsnNode) sampleGetterNode;
        AbstractInsnNode sampleFieldNode = previousRealInstruction(sampleGetterNode);
        AbstractInsnNode sampleThisNode = previousRealInstruction(sampleFieldNode);
        if (!(sampleFieldNode instanceof org.objectweb.asm.tree.FieldInsnNode)
            || sampleFieldNode.getOpcode() != Opcodes.GETFIELD
            || !(sampleThisNode instanceof VarInsnNode) || sampleThisNode.getOpcode() != Opcodes.ALOAD
            || ((VarInsnNode) sampleThisNode).var != 0
            || getter.getOpcode() != Opcodes.INVOKEVIRTUAL
            || Type.getArgumentTypes(getter.desc).length != 0
            || Type.getReturnType(getter.desc).getSort() != Type.OBJECT) return false;

        int sleepingLocal = method.maxLocals++;
        LabelNode notLiving = new LabelNode();
        LabelNode storeSleeping = new LabelNode();
        InsnList initialize = new InsnList();
        initialize.add(new VarInsnNode(Opcodes.ALOAD, 1));
        initialize.add(new TypeInsnNode(Opcodes.INSTANCEOF, cast.desc));
        initialize.add(new InsnNode(Opcodes.DUP));
        initialize.add(new JumpInsnNode(Opcodes.IFEQ, notLiving));
        initialize.add(new InsnNode(Opcodes.POP));
        initialize.add(new VarInsnNode(Opcodes.ALOAD, 1));
        initialize.add(new TypeInsnNode(Opcodes.CHECKCAST, cast.desc));
        initialize.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, sampleCall.owner,
            sampleCall.name, sampleCall.desc, sampleCall.itf));
        initialize.add(new JumpInsnNode(Opcodes.GOTO, storeSleeping));
        initialize.add(notLiving);
        initialize.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER}));
        initialize.add(new InsnNode(Opcodes.POP));
        initialize.add(new InsnNode(Opcodes.ICONST_0));
        initialize.add(storeSleeping);
        initialize.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER}));
        initialize.add(new VarInsnNode(Opcodes.ISTORE, sleepingLocal));

        AbstractInsnNode firstReal = nextRealInstruction(method.instructions.getFirst());
        if (firstReal == null) return false;
        method.instructions.insertBefore(firstReal, initialize);

        for (MethodInsnNode call : targets) {
            AbstractInsnNode castNode = previousRealInstruction(call);
            AbstractInsnNode getterNode = previousRealInstruction(castNode);
            AbstractInsnNode fieldNode = previousRealInstruction(getterNode);
            AbstractInsnNode thisNode = previousRealInstruction(fieldNode);
            if (!(castNode instanceof TypeInsnNode) || castNode.getOpcode() != Opcodes.CHECKCAST
                || !(getterNode instanceof MethodInsnNode)
                || !(fieldNode instanceof org.objectweb.asm.tree.FieldInsnNode)
                || fieldNode.getOpcode() != Opcodes.GETFIELD
                || !(thisNode instanceof VarInsnNode) || thisNode.getOpcode() != Opcodes.ALOAD
                || ((VarInsnNode) thisNode).var != 0) {
                throw new IllegalStateException("Render-view sleeping getter chain changed in " + method.desc);
            }
            InsnList cachedValue = new InsnList();
            cachedValue.add(new VarInsnNode(Opcodes.ILOAD, sleepingLocal));
            method.instructions.insertBefore(thisNode, cachedValue);
            method.instructions.remove(thisNode);
            method.instructions.remove(fieldNode);
            method.instructions.remove(getterNode);
            method.instructions.remove(castNode);
            method.instructions.remove(call);
        }
        return true;
    }

    private static boolean insertEntityRenderBreakdown(MethodNode method) {
        MethodInsnNode entityStart = findProfilerStringCall(method, "entities");
        MethodInsnNode tileEntityStart = findProfilerStringCall(method, "blockentities");
        if (entityStart == null || tileEntityStart == null) return false;

        java.util.List<AbstractInsnNode> returns = new java.util.ArrayList<AbstractInsnNode>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.RETURN) returns.add(instruction);
        }

        insertTerrainSectionBeginAfter(method, entityStart, 3);
        insertTerrainSectionEndBefore(method, tileEntityStart, 3);
        insertTerrainSectionBeginAfter(method, tileEntityStart, 11);
        for (AbstractInsnNode instruction : returns) {
            InsnList end = new InsnList();
            end.add(new LdcInsnNode(Integer.valueOf(11)));
            end.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "dev/cylox/client/minecraft189/CyloxPerformanceMonitor", "endSection", "(I)V", false));
            method.instructions.insertBefore(instruction, end);
        }
        return true;
    }

    private static boolean isNameTagRenderMethod(MethodNode method) {
        if (Type.getReturnType(method.desc).getSort() != Type.VOID) return false;
        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (arguments.length != 6 || arguments[0].getSort() != Type.OBJECT
            || !"Ljava/lang/String;".equals(arguments[1].getDescriptor())
            || arguments[2].getSort() != Type.DOUBLE || arguments[3].getSort() != Type.DOUBLE
            || arguments[4].getSort() != Type.DOUBLE || arguments[5].getSort() != Type.INT
            || !hasMarkers(method, "deadmau5")) return false;
        int stringWidthCalls = 0;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode
                && "(Ljava/lang/String;)I".equals(((MethodInsnNode) instruction).desc)) {
                stringWidthCalls++;
            }
        }
        return stringWidthCalls >= 3;
    }

    private static boolean hasFontRendererWidthPattern(ClassNode owner) {
        if (findUnicodeField(owner) == null) return false;
        int widthMethods = 0;
        for (MethodNode method : owner.methods) {
            if (isStringWidthMethod(owner, method)) widthMethods++;
        }
        return widthMethods == 1;
    }

    private static boolean isStringWidthMethod(ClassNode owner, MethodNode method) {
        if (!"(Ljava/lang/String;)I".equals(method.desc)
            || (method.access & Opcodes.ACC_STATIC) != 0) return false;
        boolean length = false;
        boolean charAt = false;
        boolean charWidth = false;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if ("java/lang/String".equals(call.owner) && "length".equals(call.name)
                && "()I".equals(call.desc)) length = true;
            if ("java/lang/String".equals(call.owner) && "charAt".equals(call.name)
                && "(I)C".equals(call.desc)) charAt = true;
            if (owner.name.equals(call.owner) && "(C)I".equals(call.desc)) charWidth = true;
        }
        return length && charAt && charWidth;
    }

    private static String findUnicodeField(ClassNode owner) {
        for (MethodNode method : owner.methods) {
            if (!"(C)I".equals(method.desc)) continue;
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode) {
                    org.objectweb.asm.tree.FieldInsnNode field =
                        (org.objectweb.asm.tree.FieldInsnNode) instruction;
                    if (field.getOpcode() == Opcodes.GETFIELD && owner.name.equals(field.owner)
                        && "Z".equals(field.desc)) return field.name;
                }
            }
        }
        return null;
    }

    private static void injectTextWidthCache(ClassNode owner, MethodNode method, String unicodeField) {
        java.util.List<AbstractInsnNode> originalReturns = new java.util.ArrayList<AbstractInsnNode>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction.getOpcode() == Opcodes.IRETURN) originalReturns.add(instruction);
        }
        if (originalReturns.isEmpty()) throw new IllegalStateException("FontRenderer width method has no IRETURN");

        int resultLocal = method.maxLocals++;
        LabelNode cached = new LabelNode();
        LabelNode original = new LabelNode();
        InsnList lookup = new InsnList();
        lookup.add(new VarInsnNode(Opcodes.ALOAD, 0));
        lookup.add(new VarInsnNode(Opcodes.ALOAD, 1));
        lookup.add(new VarInsnNode(Opcodes.ALOAD, 0));
        lookup.add(new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner.name, unicodeField, "Z"));
        lookup.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TEXT_WIDTH_CACHE,
            "getCachedWidth", "(Ljava/lang/Object;Ljava/lang/String;Z)I", false));
        lookup.add(new InsnNode(Opcodes.DUP));
        lookup.add(new JumpInsnNode(Opcodes.IFGE, cached));
        lookup.add(new InsnNode(Opcodes.POP));
        lookup.add(new JumpInsnNode(Opcodes.GOTO, original));
        lookup.add(cached);
        lookup.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER}));
        lookup.add(new InsnNode(Opcodes.IRETURN));
        lookup.add(original);
        lookup.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(lookup);

        for (AbstractInsnNode instruction : originalReturns) {
            InsnList store = new InsnList();
            store.add(new VarInsnNode(Opcodes.ISTORE, resultLocal));
            store.add(new VarInsnNode(Opcodes.ALOAD, 0));
            store.add(new VarInsnNode(Opcodes.ALOAD, 1));
            store.add(new VarInsnNode(Opcodes.ALOAD, 0));
            store.add(new org.objectweb.asm.tree.FieldInsnNode(Opcodes.GETFIELD, owner.name, unicodeField, "Z"));
            store.add(new VarInsnNode(Opcodes.ILOAD, resultLocal));
            store.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TEXT_WIDTH_CACHE,
                "cacheWidth", "(Ljava/lang/Object;Ljava/lang/String;ZI)V", false));
            store.add(new VarInsnNode(Opcodes.ILOAD, resultLocal));
            method.instructions.insertBefore(instruction, store);
        }
    }

    private static void injectVisibilityCache(MethodNode method) {
        LabelNode cached = new LabelNode();
        LabelNode original = new LabelNode();
        InsnList lookup = new InsnList();
        lookup.add(new VarInsnNode(Opcodes.ALOAD, 0));
        lookup.add(new VarInsnNode(Opcodes.ALOAD, 1));
        lookup.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_HOOKS,
            "getCachedVisibleFacings", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false));
        lookup.add(new InsnNode(Opcodes.DUP));
        lookup.add(new JumpInsnNode(Opcodes.IFNONNULL, cached));
        lookup.add(new InsnNode(Opcodes.POP));
        lookup.add(new JumpInsnNode(Opcodes.GOTO, original));
        lookup.add(cached);
        lookup.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Object"}));
        lookup.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/util/Set"));
        lookup.add(new InsnNode(Opcodes.ARETURN));
        lookup.add(original);
        lookup.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(lookup);

        int cachedResultLocal = method.maxLocals++;
        for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null; ) {
            AbstractInsnNode next = instruction.getNext();
            if (instruction.getOpcode() == Opcodes.ARETURN) {
                InsnList store = new InsnList();
                store.add(new VarInsnNode(Opcodes.ASTORE, cachedResultLocal));
                store.add(new VarInsnNode(Opcodes.ALOAD, 0));
                store.add(new VarInsnNode(Opcodes.ALOAD, 1));
                store.add(new VarInsnNode(Opcodes.ALOAD, cachedResultLocal));
                store.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_HOOKS,
                    "cacheVisibleFacings", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V", false));
                store.add(new VarInsnNode(Opcodes.ALOAD, cachedResultLocal));
                method.instructions.insertBefore(instruction, store);
            }
            instruction = next;
        }
    }

    private static void insertVisibilityCacheInvalidation(MethodNode method) {
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_HOOKS,
            "invalidateVisibilityCache", "(Ljava/lang/Object;)V", false));
        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CHUNK_BUILD_HOOKS,
            "clear", "(Ljava/lang/Object;)V", false));
        method.instructions.insert(hook);
    }

    private static void insertVisibilityCacheRangeInvalidation(MethodNode method) {
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
        for (int local = 1; local <= 6; local++) {
            hook.add(new VarInsnNode(Opcodes.ILOAD, local));
        }
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RENDER_HOOKS,
            "invalidateVisibilityRange", "(Ljava/lang/Object;IIIIII)V", false));
        method.instructions.insert(hook);
    }

    /**
     * Breaks RenderGlobal.setupTerrain into its existing vanilla profiler phases.
     * Hooks are placed only at section transitions already present in 1.8.9;
     * no traversal, chunk scheduling, or render decisions are changed.
     */
    private static boolean insertTerrainBreakdown(MethodNode method) {
        MethodInsnNode cameraStart = findProfilerStringCall(method, "camera");
        MethodInsnNode renderListStart = findProfilerStringCall(method, "renderlistcamera");
        MethodInsnNode cullPrepStart = findProfilerStringCall(method, "cull");
        MethodInsnNode cullingStart = findProfilerStringCall(method, "culling");
        MethodInsnNode buildNearStart = findProfilerStringCall(method, "build near");
        if (cameraStart == null || renderListStart == null || cullPrepStart == null
            || cullingStart == null || buildNearStart == null) return false;

        MethodInsnNode buildNearEnd = findFollowingProfilerEnd(method, buildNearStart, buildNearStart.owner, false);
        MethodInsnNode cullingEnd = findFollowingProfilerEnd(method, cullingStart, cullingStart.owner, true);
        if (buildNearEnd == null || cullingEnd == null || buildNearEnd == cullingEnd) return false;

        insertTerrainSectionBeginAfter(method, cameraStart, 6);
        insertTerrainSectionEndBefore(method, renderListStart, 6);
        insertTerrainSectionBeginAfter(method, renderListStart, 7);
        insertTerrainSectionEndBefore(method, cullPrepStart, 7);
        insertTerrainSectionBeginAfter(method, cullPrepStart, 8);
        insertTerrainSectionEndBefore(method, cullingStart, 8);
        insertTerrainSectionBeginAfter(method, cullingStart, 9);
        insertTerrainSectionBeginAfter(method, buildNearStart, 10);
        insertTerrainSectionEndBefore(method, buildNearEnd, 10);
        insertTerrainSectionEndBefore(method, cullingEnd, 9);
        return true;
    }

    private static MethodInsnNode findProfilerStringCall(MethodNode method, String label) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)
                || !"(Ljava/lang/String;)V".equals(((MethodInsnNode) instruction).desc)) continue;
            AbstractInsnNode previous = instruction.getPrevious();
            while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
            if (previous instanceof LdcInsnNode && label.equals(((LdcInsnNode) previous).cst)) {
                return (MethodInsnNode) instruction;
            }
        }
        return null;
    }

    private static MethodInsnNode findFollowingProfilerEnd(MethodNode method, MethodInsnNode start,
                                                            String profilerOwner, boolean last) {
        MethodInsnNode found = null;
        boolean afterStart = false;
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction == start) {
                afterStart = true;
            } else if (afterStart && instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (profilerOwner.equals(call.owner) && "()V".equals(call.desc)) {
                    if (!last) return call;
                    found = call;
                }
            }
        }
        return found;
    }

    private static void insertTerrainSectionBeginAfter(MethodNode method, MethodInsnNode call, int section) {
        InsnList hook = new InsnList();
        hook.add(new LdcInsnNode(Integer.valueOf(section)));
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
            "dev/cylox/client/minecraft189/CyloxPerformanceMonitor", "beginSection", "(I)V", false));
        method.instructions.insert(call, hook);
    }

    private static void insertTerrainSectionEndBefore(MethodNode method, MethodInsnNode call, int section) {
        InsnList hook = new InsnList();
        hook.add(new LdcInsnNode(Integer.valueOf(section)));
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
            "dev/cylox/client/minecraft189/CyloxPerformanceMonitor", "endSection", "(I)V", false));
        method.instructions.insertBefore(call, hook);
    }

    private static void insertTimedSection(MethodNode method, int section) {
        java.util.List<AbstractInsnNode> originalReturns = new java.util.ArrayList<AbstractInsnNode>();
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            int opcode = instruction.getOpcode();
            if (opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN || opcode == Opcodes.FRETURN
                || opcode == Opcodes.DRETURN || opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
                originalReturns.add(instruction);
            }
        }

        InsnList begin = new InsnList();
        begin.add(new LdcInsnNode(Integer.valueOf(section)));
        begin.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
            "dev/cylox/client/minecraft189/CyloxPerformanceMonitor", "beginSection", "(I)V", false));
        method.instructions.insert(begin);

        Type returnType = Type.getReturnType(method.desc);
        int resultLocal = -1;
        if (returnType.getSort() != Type.VOID) {
            resultLocal = method.maxLocals;
            method.maxLocals += returnType.getSize();
        }

        for (AbstractInsnNode instruction : originalReturns) {
            InsnList end = new InsnList();
            if (resultLocal >= 0) end.add(new VarInsnNode(returnType.getOpcode(Opcodes.ISTORE), resultLocal));
            end.add(new LdcInsnNode(Integer.valueOf(section)));
            end.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "dev/cylox/client/minecraft189/CyloxPerformanceMonitor", "endSection", "(I)V", false));
            if (resultLocal >= 0) end.add(new VarInsnNode(returnType.getOpcode(Opcodes.ILOAD), resultLocal));
            method.instructions.insertBefore(instruction, end);
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

    private static boolean hasMethodCallDescriptor(MethodNode method, String descriptor) {
        for (AbstractInsnNode instruction = method.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode
                && descriptor.equals(((MethodInsnNode) instruction).desc)) return true;
        }
        return false;
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
