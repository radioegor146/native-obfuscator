package by.radioegor146.special;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ClInitSpecialMethodProcessor implements SpecialMethodProcessor {

    @Override
    public String preProcess(MethodContext context) {
        String name = "native_special_clinit" + context.methodIndex;
        context.proxyMethod = new MethodNode(Opcodes.ASM7,
                Opcodes.ACC_NATIVE | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                name, context.method.desc, context.method.signature, new String[0]);
        context.clazz.methods.add(context.proxyMethod);
        return name;
    }

    @Override
    public void postProcess(MethodContext context) {
        InsnList instructions = context.method.instructions;
        instructions.clear();
        instructions.add(new LdcInsnNode(context.classIndex));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, context.obfuscator.getNativeDir() + "/Loader",
                "registerNativesForClass", "(I)V"));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, context.clazz.name,
                "native_special_clinit" + context.methodIndex, context.method.desc));

        if (Util.getFlag(context.clazz.access, Opcodes.ACC_INTERFACE)) {
            if (context.nativeMethod == null) {
                throw new RuntimeException("Native method not created?!");
            }
            context.proxyMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    context.obfuscator.getStaticClassProvider().getCurrentClassName(),
                    context.nativeMethod.name, context.nativeMethod.desc));
            context.proxyMethod.instructions.add(new InsnNode(Opcodes.RETURN));
        }
        instructions.add(new InsnNode(Opcodes.RETURN));
    }
}
