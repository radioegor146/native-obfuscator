package by.radioegor146;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class HiddenMethodsPool {

    private final String baseName;

    public HiddenMethodsPool(String baseName) {
        this.baseName = baseName;
    }

    private final HashMap<String, Integer> namePool = new HashMap<>();
    private final HashMap<String, HashMap<String, HiddenMethod>> methods = new HashMap<>();
    private final List<ClassNode> classes = new ArrayList<>();

    public static class HiddenMethod {

        private final ClassNode classNode;
        private final MethodNode methodNode;

        private HiddenMethod(ClassNode classNode, MethodNode methodNode) {
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

    public HiddenMethod getMethod(String name, String desc, Consumer<MethodNode> creator) {
        HiddenMethod existingMethod = methods.computeIfAbsent(name, unused -> new HashMap<>()).get(desc);
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
            classNode.access = Opcodes.ACC_PUBLIC;
            classNode.version = 52;
            classNode.name = baseName + "/Hidden" + classes.size();
            classes.add(classNode);
        }
        classNode.methods.add(newMethod);
        HiddenMethod hiddenMethod = new HiddenMethod(classNode, newMethod);
        methods.computeIfAbsent(name, unused -> new HashMap<>()).put(desc, hiddenMethod);
        return hiddenMethod;
    }

    public List<ClassNode> getClasses() {
        return classes;
    }
}
