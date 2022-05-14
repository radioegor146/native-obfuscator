package by.radioegor146.bytecode;

import by.radioegor146.Platform;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.Supplier;

public interface IPreprocessor {

    void process(ClassNode classNode, MethodNode methodNode, Platform platform);
}
