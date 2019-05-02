package by.radioegor146.instructions;

import by.radioegor146.CachedMethodInfo;
import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.ArrayList;
import java.util.List;

public class InvokeDynamicHandler extends GenericInstructionHandler<InvokeDynamicInsnNode> {

    @Override
    protected void process(MethodContext context, InvokeDynamicInsnNode node) {
        String indyMethodName = "invokedynamic$" + context.method.name + "$" + context.obfuscator.getInvokeDynamics().size();
        context.obfuscator.getInvokeDynamics().put(indyMethodName, node);
        Type returnType = Type.getReturnType(node.desc);
        Type[] argTypes = Type.getArgumentTypes(node.desc);
        instructionName = "INVOKESTATIC_" + returnType.getSort();
        StringBuilder argsBuilder = new StringBuilder();
        List<Integer> argOffsets = new ArrayList<>();
        List<Integer> argSorts = new ArrayList<>();
        int stackOffset = -1;
        for (Type argType : argTypes) {
            int currentOffset = stackOffset;
            stackOffset -= argType.getSize();
            argOffsets.add(currentOffset);
            argSorts.add(argType.getSort());
        }
        for (int i = 0; i < argOffsets.size(); i++) {
            argsBuilder.append(", ").append(context.obfuscator.getSnippets().getSnippet("INVOKE_ARG_" + argSorts.get(i),
                    Util.createMap("index",
                    String.valueOf(argOffsets.get(i)))));
        }
        context.output.append(context.obfuscator.getSnippets().getSnippet("INVOKE_POPCNT",
                Util.createMap("count", String.valueOf(-stackOffset - 1)))).append(" ");
        props.put("class_ptr", context.obfuscator.getCachedClasses().getPointer(context.clazz.name));
        int methodId = context.obfuscator.getCachedMethods().getId(new CachedMethodInfo(
                context.clazz.name,
                indyMethodName,
                node.desc,
                true
        ));
        context.output.append("if (!cmethods[")
                .append(methodId)
                .append("].load()) { cmethods[")
                .append(methodId)
                .append("].store(env->GetStaticMethodID(")
                .append(context.obfuscator.getCachedClasses().getPointer(context.clazz.name))
                .append(", ")
                .append(context.obfuscator.getStringPool().get(indyMethodName))
                .append(", ")
                .append(context.obfuscator.getStringPool().get(node.desc))
                .append(")); ")
                .append(trimmedTryCatchBlock)
                .append("  } ");
        props.put("methodid", context.obfuscator.getCachedMethods().getPointer(new CachedMethodInfo(
                context.clazz.name, indyMethodName, node.desc, true
        )));
        props.put("args", argsBuilder.toString());
    }
}
