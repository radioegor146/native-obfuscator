package by.radioegor146.special;

import by.radioegor146.MethodContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class InitSpecialMethodProcessor implements SpecialMethodProcessor {

    @Override
    public String preProcess(MethodContext context) {
        String name = "native_special_init" + context.methodIndex;
        context.proxyMethod = new MethodNode(Opcodes.ASM7,
                Opcodes.ACC_NATIVE | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                name, context.method.desc, context.method.signature, new String[0]);
        context.clazz.methods.add(context.proxyMethod);
        return name;
    }

    @Override
    public void postProcess(MethodContext context) {
        InsnList list = new InsnList();
        for (int i = 0; i <= context.invokeSpecialId; i++) {
            list.add(context.method.instructions.get(i));
        }
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        int localVarsPosition = 1;
        for (Type arg : context.argTypes) {
            list.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
            localVarsPosition += arg.getSize();
        }
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, context.clazz.name,
                "native_special_init" + context.methodIndex, context.method.desc));
        list.add(new InsnNode(Opcodes.RETURN));
        context.method.instructions = list;
    }
}
