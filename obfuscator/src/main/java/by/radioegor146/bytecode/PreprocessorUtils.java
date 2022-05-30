package by.radioegor146.bytecode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
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
    public static final Function<String, AbstractInsnNode> INVOKE_REVERSE =
            desc -> {
                List<Type> argumentTypes = new ArrayList<>(Arrays.asList(Type.getArgumentTypes(desc)));
                argumentTypes.add(Type.getObjectType("java/lang/invoke/MethodHandle"));
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "native/magic/1/invoke/obfuscator" + MAGIC_CONST, "a",
                        Type.getMethodDescriptor(Type.getReturnType(desc), argumentTypes.toArray(new Type[0])));
            };
    public static final Supplier<AbstractInsnNode> LINK_CALL_SITE_METHOD = () -> new MethodInsnNode(Opcodes.INVOKESTATIC,
            "native/magic/1/linkcallsite/obfuscator" + MAGIC_CONST, "a", "(Ljava/lang/Object;Ljava/lang/Object;" +
            "Ljava/lang/Object;Ljava/lang/Object;" +
            "Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");

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

    public static boolean isInvokeReverse(AbstractInsnNode abstractInsnNode) {
        if (!(abstractInsnNode instanceof MethodInsnNode)) {
            return false;
        }
        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
        MethodInsnNode realMethodInsnNode = (MethodInsnNode) INVOKE_REVERSE.apply("()V");
        return methodInsnNode.owner.equals(realMethodInsnNode.owner);
    }

    public static boolean isLinkCallSiteMethod(AbstractInsnNode abstractInsnNode) {
        return compareSuppliers(abstractInsnNode, LINK_CALL_SITE_METHOD);
    }

    private PreprocessorUtils() {
    }
}
