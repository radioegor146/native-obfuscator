package by.radioegor146.bytecode;

import by.radioegor146.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface Preprocessor {

    void process(ClassNode classNode, MethodNode methodNode, Platform platform);
}
