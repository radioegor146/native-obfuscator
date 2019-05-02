package by.radioegor146;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class InterfaceStaticClassProvider {

    private String nativeDir;
    private List<ClassNode> readyClasses;

    private ClassNode currentClass;
    private StringBuilder methods;

    public InterfaceStaticClassProvider(String nativeDir) {
        this.nativeDir = nativeDir;
        readyClasses = new ArrayList<>();
    }

    public void addMethod(MethodNode method, String source) {
        ClassNode classNode = getCurrentClass();

        if (classNode.methods.size() > 16384) {
            throw new RuntimeException("too many static interface methods");
        }

        classNode.methods.add(method);
        methods.append(source);
    }

    public void newClass() {
        currentClass = null;
        methods = null;
    }

    public List<ClassNode> getReadyClasses() {
        return readyClasses;
    }

    public boolean isEmpty() {
        return currentClass == null;
    }

    public String getMethods() {
        return methods.toString();
    }

    public String getCurrentClassName() {
        return getCurrentClass().name;
    }

    private ClassNode getCurrentClass() {
        if(currentClass == null) {
            methods = new StringBuilder();
            currentClass = new ClassNode();
            currentClass.version = 52;
            currentClass.sourceFile = "synthetic";
            currentClass.access = Opcodes.ACC_PUBLIC;
            currentClass.superName = "java/lang/Object";
            currentClass.name = nativeDir + "/interfacestatic/Methods" + readyClasses.size();

            readyClasses.add(currentClass);
        }

        return currentClass;
    }
}
