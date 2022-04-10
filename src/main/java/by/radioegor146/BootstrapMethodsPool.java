package by.radioegor146;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class BootstrapMethodsPool {

    private final String baseName;

    public BootstrapMethodsPool(String baseName) {
        this.baseName = baseName;
    }

    private final HashMap<String, Integer> namePool = new HashMap<>();
    private final HashMap<String, HashMap<String, BootstrapMethod>> methods = new HashMap<>();
    private final List<ClassNode> classes = new ArrayList<>();

    public static class BootstrapMethod {

        private final ClassNode classNode;
        private final MethodNode methodNode;

        private BootstrapMethod(ClassNode classNode, MethodNode methodNode) {
            this.classNode = classNode;
            this.methodNode = methodNode;
        }

        public ClassNode getClassNode() {
            return classNode;
        }

        public MethodNode getMethodNode() {
            return methodNode;
        }
    }

    public BootstrapMethod getMethod(String name, String desc, Consumer<MethodNode> creator) {
        BootstrapMethod existingMethod = methods.computeIfAbsent(name, unused -> new HashMap<>()).get(desc);
        if (existingMethod != null) {
            return existingMethod;
        }

        String newName = name + namePool.compute(name, (otherName, value) -> value == null ? 0 : value + 1);
        MethodNode newMethod = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_BRIDGE |
                Opcodes.ACC_SYNTHETIC, newName, desc, null, new String[0]);
        creator.accept(newMethod);
        ClassNode classNode = classes.size() == 0 ? null : classes.get(0).methods.size() > 10000 ? null : classes.get(0);
        if (classNode == null) {
            classNode = new ClassNode(Opcodes.ASM7);
            classNode.version = 52;
            classNode.name = baseName + "/Bootstrap" + classes.size();
            classes.add(classNode);
        }
        classNode.methods.add(newMethod);
        BootstrapMethod bootstrapMethod = new BootstrapMethod(classNode, newMethod);
        methods.computeIfAbsent(name, unused -> new HashMap<>()).put(desc, bootstrapMethod);
        return bootstrapMethod;
    }

    public List<ClassNode> getClasses() {
        return classes;
    }
}
