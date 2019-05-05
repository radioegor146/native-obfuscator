package by.radioegor146.instructions;

import by.radioegor146.CachedMethodInfo;
import by.radioegor146.MethodContext;
import by.radioegor146.MethodProcessor;
import by.radioegor146.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;

public class MethodHandler extends GenericInstructionHandler<MethodInsnNode> {

    @Override
    protected void process(MethodContext context, MethodInsnNode node) {
        Type returnType = Type.getReturnType(node.desc);
        Type[] args = Type.getArgumentTypes(node.desc);
        instructionName += "_" + returnType.getSort();

        StringBuilder argsBuilder = new StringBuilder();
        List<Integer> argOffsets = new ArrayList<>();

        int stackOffset = -1;
        for (Type argType : args) {
            argOffsets.add(stackOffset);
            stackOffset -= argType.getSize();
        }

        boolean isStatic = node.getOpcode() == Opcodes.INVOKESTATIC;
        int objectOffset = isStatic ? 0 : 1;

        for (int i = 0; i < argOffsets.size(); i++) {
            argsBuilder.append(", ").append(context.getSnippets().getSnippet("INVOKE_ARG_" + args[i].getSort(),
                    Util.createMap("index", argOffsets.get(i) - objectOffset)));
        }

        if (!isStatic || args.length > 0) {
            int count = -(stackOffset + 1) + objectOffset;
            context.output.append(context.getSnippets().getSnippet("INVOKE_POPCNT",
                    Util.createMap("count", count))).append(" ");
        }

        if (isStatic || node.getOpcode() == Opcodes.INVOKESPECIAL) {
            props.put("class_ptr", context.getCachedClasses().getPointer(node.owner));
        }

        int classId = context.getCachedClasses().getId(node.owner);

        context.output.append(String.format("if (!cclasses[%d]) { cclasses_mtx[%d].lock(); if (!cclasses[%d]) { if (jclass clazz = %s) { cclasses[%d] = (jclass) env->NewGlobalRef(clazz); env->DeleteLocalRef(clazz); } } cclasses_mtx[%d].unlock(); %s } ",
                classId,
                classId,
                classId,
                MethodProcessor.getClassGetter(context, node.owner),
                classId,
                classId,
                trimmedTryCatchBlock));

        CachedMethodInfo methodInfo = new CachedMethodInfo(node.owner, node.name, node.desc, isStatic);
        int methodId = context.getCachedMethods().getId(methodInfo);
        props.put("methodid", context.getCachedMethods().getPointer(methodInfo));

        context.output.append(
                String.format("if (!cmethods[%d]) { cmethods[%d] = env->Get%sMethodID(%s, %s, %s); %s  } ",
                        methodId,
                        methodId,
                        isStatic ? "Static" : "",
                        context.getCachedClasses().getPointer(node.owner),
                        context.getStringPool().get(node.name),
                        context.getStringPool().get(node.desc),
                        trimmedTryCatchBlock));

        props.put("args", argsBuilder.toString());
    }
}
