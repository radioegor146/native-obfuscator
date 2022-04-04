package by.radioegor146.instructions;

import by.radioegor146.CachedMethodInfo;
import by.radioegor146.MethodContext;
import by.radioegor146.MethodProcessor;
import by.radioegor146.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class InvokeDynamicHandler extends GenericInstructionHandler<InvokeDynamicInsnNode> {

    @Override
    protected void process(MethodContext context, InvokeDynamicInsnNode node) {
        String indyMethodName = String.format("invokedynamic$%s$%d",
                context.method.name,
                context.getInvokeDynamics().size());
        context.getInvokeDynamics().put(indyMethodName, node);

        Type returnType = Type.getReturnType(node.desc);
        Type[] args = Type.getArgumentTypes(node.desc);
        instructionName = "INVOKESTATIC_" + returnType.getSort();

        StringBuilder argsBuilder = new StringBuilder();
        List<Integer> argOffsets = new ArrayList<>();

        int stackOffset = -1;
        for (Type argType : args) {
            argOffsets.add(stackOffset);
            stackOffset -= argType.getSize();
        }

        for (int i = 0; i < argOffsets.size(); i++) {
            argsBuilder.append(", ").append(context.getSnippets().getSnippet("INVOKE_ARG_" + args[i].getSort(),
                    Util.createMap("index", argOffsets.get(i))));
        }

        context.output.append(context.getSnippets().getSnippet("INVOKE_POPCNT",
                Util.createMap("count", -stackOffset - 1))).append(" ");


        props.put("class_ptr", context.getCachedClasses().getPointer(context.clazz.name));

        int classId = context.getCachedClasses().getId(context.clazz.name);
        context.output.append(String.format("if (!cclasses[%d] || env->IsSameObject(cclasses[%d], NULL)) { cclasses_mtx[%d].lock(); if (!cclasses[%d] || env->IsSameObject(cclasses[%d], NULL)) { if (jclass clazz = %s) { cclasses[%d] = (jclass) env->NewWeakGlobalRef(clazz); env->DeleteLocalRef(clazz); } } cclasses_mtx[%d].unlock(); %s } ",
                classId,
                classId,
                classId,
                classId,
                classId,
                MethodProcessor.getClassGetter(context, context.clazz.name),
                classId,
                classId,
                trimmedTryCatchBlock));
        
        CachedMethodInfo methodInfo = new CachedMethodInfo(context.clazz.name, indyMethodName, node.desc, true);
        int methodId = context.getCachedMethods().getId(methodInfo);
        props.put("methodid", context.getCachedMethods().getPointer(methodInfo));

        context.output.append(
                String.format("if (!cmethods[%d]) { cmethods[%d] = env->GetStaticMethodID(%s, %s, %s); %s  } ",
                        methodId,
                        methodId,
                        context.getCachedClasses().getPointer(context.clazz.name),
                        context.getStringPool().get(indyMethodName),
                        context.getStringPool().get(node.desc),
                        trimmedTryCatchBlock));

        props.put("args", argsBuilder.toString());
    }

    public static void processIndy(ClassNode classNode, String methodName, InvokeDynamicInsnNode indy) {
        MethodNode indyWrapper = new MethodNode(Opcodes.ASM7,
                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC,
                methodName, indy.desc, null, new String[0]);

        int localVarsPosition = 0;
        for (Type arg : Type.getArgumentTypes(indy.desc)) {
            indyWrapper.instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
            localVarsPosition += arg.getSize();
        }

        indyWrapper.instructions.add(new InvokeDynamicInsnNode(indy.name, indy.desc, indy.bsm, indy.bsmArgs));
        indyWrapper.instructions.add(new InsnNode(Type.getReturnType(indy.desc).getOpcode(Opcodes.IRETURN)));
        classNode.methods.add(indyWrapper);
    }

}
