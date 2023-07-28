package by.radioegor146.special;

import by.radioegor146.MethodContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClInitSpecialMethodProcessor implements SpecialMethodProcessor {

    @Override
    public String preProcess(MethodContext context) {
        String name = String.format("special_clinit_%d_%d", context.classIndex, context.methodIndex);

        context.proxyMethod = context.obfuscator.getHiddenMethodsPool().getMethod(name, "(Ljava/lang/Class;)V", methodNode -> {
            methodNode.signature = context.method.signature;
            methodNode.access = Opcodes.ACC_NATIVE | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE;
            methodNode.visibleAnnotations = new ArrayList<>();
            methodNode.visibleAnnotations.add(new AnnotationNode("Ljava/lang/invoke/LambdaForm$Hidden;"));
            methodNode.visibleAnnotations.add(new AnnotationNode("Ljdk/internal/vm/annotation/LambdaForm$Hidden;"));
        });
        return name;
    }

    @Override
    public void postProcess(MethodContext context) {
        InsnList instructions = context.method.instructions;
        instructions.clear();
        instructions.add(new LdcInsnNode(context.classIndex));
        instructions.add(new LdcInsnNode(Type.getObjectType(context.clazz.name)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, context.obfuscator.getNativeDir() + "/Loader",
                "registerNativesForClass", "(ILjava/lang/Class;)V", false));
        instructions.add(new LdcInsnNode(Type.getObjectType(context.clazz.name)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                context.proxyMethod.getClassNode().name,
                context.proxyMethod.getMethodNode().name,
                context.proxyMethod.getMethodNode().desc, false));
        instructions.add(new InsnNode(Opcodes.RETURN));
    }
}
