package by.radioegor146.special;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class DefaultSpecialMethodProcessor implements SpecialMethodProcessor {

    @Override
    public String preProcess(MethodContext context) {
        context.proxyMethod = context.method;
        context.method.access |= Opcodes.ACC_NATIVE;
        return "native_" + context.method.name + context.methodIndex;
    }

    @Override
    public void postProcess(MethodContext context) {
        context.method.instructions.clear();
        if (Util.getFlag(context.clazz.access, Opcodes.ACC_INTERFACE)) {
            InsnList list = new InsnList();
            int localVarsPosition = 0;
            for (Type arg : context.argTypes) {
                list.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
                localVarsPosition += arg.getSize();
            }
            if (context.nativeMethod == null) {
                throw new RuntimeException("Native method not created?!");
            }
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    context.obfuscator.getStaticClassProvider().getCurrentClassName(),
                    context.nativeMethod.name, context.nativeMethod.desc, false));
            list.add(new InsnNode(Type.getReturnType(context.method.desc).getOpcode(Opcodes.IRETURN)));
            context.method.instructions = list;
        }
    }
}
