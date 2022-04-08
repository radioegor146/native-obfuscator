package by.radioegor146.bytecode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.function.Supplier;

public class PreprocessorUtils {
    private static final String MAGIC_CONST = String.valueOf(Math.random());

    public static final Supplier<AbstractInsnNode> LOOKUP_LOCAL = () -> new MethodInsnNode(Opcodes.INVOKESTATIC,
            "native/magic/1/lookup/obfuscator" + MAGIC_CONST, "a",
            "()Ljava/lang/invoke/MethodHandles$Lookup;");
    public static final Supplier<AbstractInsnNode> CLASSLOADER_LOCAL = () -> new MethodInsnNode(Opcodes.INVOKESTATIC,
            "native/magic/1/classloader/obfuscator" + MAGIC_CONST, "a",
            "()Ljava/lang/ClassLoader;");
    public static final Supplier<AbstractInsnNode> CLASS_LOCAL = () -> new MethodInsnNode(Opcodes.INVOKESTATIC,
            "native/magic/1/class/obfuscator" + MAGIC_CONST, "a",
            "()Ljava/lang/Class;");

    private static boolean areMethodNodesEqual(MethodInsnNode methodInsnNode, MethodInsnNode realMethodInsnNode) {
        if (methodInsnNode.getType() != realMethodInsnNode.getType()) {
            return false;
        }
        if (!methodInsnNode.owner.equals(realMethodInsnNode.owner)) {
            return false;
        }
        if (!methodInsnNode.name.equals(realMethodInsnNode.name)) {
            return false;
        }
        return methodInsnNode.desc.equals(realMethodInsnNode.desc);
    }

    private static boolean compareSuppliers(AbstractInsnNode abstractInsnNode, Supplier<AbstractInsnNode> supplier) {
        if (!(abstractInsnNode instanceof MethodInsnNode)) {
            return false;
        }
        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
        MethodInsnNode realMethodInsnNode = (MethodInsnNode) supplier.get();
        return areMethodNodesEqual(methodInsnNode, realMethodInsnNode);
    }

    public static boolean isLookupLocal(AbstractInsnNode abstractInsnNode) {
        return compareSuppliers(abstractInsnNode, LOOKUP_LOCAL);
    }

    public static boolean isClassLoaderLocal(AbstractInsnNode abstractInsnNode) {
        return compareSuppliers(abstractInsnNode, CLASSLOADER_LOCAL);
    }

    public static boolean isClassLocal(AbstractInsnNode abstractInsnNode) {
        return compareSuppliers(abstractInsnNode, CLASS_LOCAL);
    }

    private PreprocessorUtils() {
    }
}
