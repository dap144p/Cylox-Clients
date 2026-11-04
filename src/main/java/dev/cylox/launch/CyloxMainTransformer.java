package dev.cylox.launch;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Installs the Cylox bootstrap hook into the stable vanilla Main entry class.
 */
public final class CyloxMainTransformer implements IClassTransformer {
    private static final String TARGET_CLASS = "net.minecraft.client.main.Main";
    private static final String RUNTIME_CLASS = "dev/cylox/client/CyloxRuntime";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET_CLASS.equals(transformedName)) {
            return basicClass;
        }

        final AtomicBoolean hookInserted = new AtomicBoolean(false);
        ClassReader reader = new ClassReader(basicClass);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor method = super.visitMethod(access, methodName, descriptor, signature, exceptions);
                if (!"main".equals(methodName)
                        || !"([Ljava/lang/String;)V".equals(descriptor)
                        || (access & Opcodes.ACC_STATIC) == 0) {
                    return method;
                }

                hookInserted.set(true);
                return new MethodVisitor(Opcodes.ASM5, method) {
                    @Override
                    public void visitCode() {
                        super.visitCode();
                        visitVarInsn(Opcodes.ALOAD, 0);
                        visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                RUNTIME_CLASS,
                                "bootstrap",
                                "([Ljava/lang/String;)V",
                                false
                        );
                    }
                };
            }
        };

        reader.accept(visitor, 0);
        if (!hookInserted.get()) {
            System.err.println("[Cylox Client] Vanilla Main entry point was found, but its main method signature did not match.");
            return basicClass;
        }

        System.out.println("[Cylox Client] Vanilla Main bootstrap hook installed.");
        return writer.toByteArray();
    }
}
