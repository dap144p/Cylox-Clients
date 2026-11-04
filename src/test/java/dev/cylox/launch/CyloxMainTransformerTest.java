package dev.cylox.launch;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class CyloxMainTransformerTest {
    private static final String TARGET = "net.minecraft.client.main.Main";

    @Test
    public void injectsRuntimeCallIntoVanillaMain() {
        byte[] original = createMainClass();
        byte[] transformed = new CyloxMainTransformer().transform(TARGET, TARGET, original);
        assertNotNull(transformed);
        assertTrue(containsCyloxBootstrap(transformed));
    }

    @Test
    public void ignoresUnrelatedClasses() {
        byte[] original = createMainClass();
        byte[] transformed = new CyloxMainTransformer().transform(
                "example.Other",
                "example.Other",
                original
        );
        assertFalse(containsCyloxBootstrap(transformed));
    }

    private static byte[] createMainClass() {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                TARGET.replace('.', '/'), null, "java/lang/Object", null);
        MethodVisitor main = writer.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
        );
        main.visitCode();
        main.visitInsn(Opcodes.RETURN);
        main.visitMaxs(0, 0);
        main.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static boolean containsCyloxBootstrap(byte[] classBytes) {
        final boolean[] found = new boolean[] { false };
        ClassReader reader = new ClassReader(classBytes);
        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String methodName,
                                                String methodDescriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKESTATIC
                                && "dev/cylox/client/CyloxRuntime".equals(owner)
                                && "bootstrap".equals(methodName)
                                && "([Ljava/lang/String;)V".equals(methodDescriptor)) {
                            found[0] = true;
                        }
                    }
                };
            }
        }, 0);
        return found[0];
    }
}
