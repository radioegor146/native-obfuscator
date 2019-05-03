package by.radioegor146.instructions;

import by.radioegor146.CachedMethodInfo;
import by.radioegor146.MethodContext;
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
        Type[] argTypes = Type.getArgumentTypes(node.desc);
        instructionName += "_" + returnType.getSort();
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
        if (node.getOpcode() == Opcodes.INVOKEINTERFACE || node.getOpcode() == Opcodes.INVOKESPECIAL || node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
            for (int i = 0; i < argOffsets.size(); i++) {
                argsBuilder.append(", ").append(context.obfuscator.getSnippets().getSnippet("INVOKE_ARG_" + argSorts.get(i),
                        Util.createMap("index", String.valueOf(argOffsets.get(i) - 1))));
            }
            if (stackOffset != 0) {
                context.output.append(context.obfuscator.getSnippets().getSnippet("INVOKE_POPCNT",
                        Util.createMap("count", String.valueOf(-stackOffset)))).append(" ");
            }
            if (node.getOpcode() == Opcodes.INVOKESPECIAL) {
                props.put("class_ptr", context.obfuscator.getCachedClasses().getPointer(node.owner));
            }
            int methodId = context.obfuscator.getCachedMethods().getId(new CachedMethodInfo(
                    node.owner, node.name, node.desc, false
            ));
            context.output.append("if (!cmethods[")
                    .append(methodId)
                    .append("]) { cmethods[")
                    .append(methodId)
                    .append("] = env->GetMethodID(")
                    .append(context.obfuscator.getCachedClasses().getPointer(node.owner))
                    .append(", ")
                    .append(context.obfuscator.getStringPool().get(node.name))
                    .append(", ")
                    .append(context.obfuscator.getStringPool().get(node.desc))
                    .append("); ")
                    .append(trimmedTryCatchBlock)
                    .append("  } ");
            props.put("methodid", context.obfuscator.getCachedMethods().getPointer(new CachedMethodInfo(
                    node.owner, node.name, node.desc, false
            )));
            props.put("object_offset", "-1");
            props.put("args", argsBuilder.toString());
        } else {
            for (int i = 0; i < argOffsets.size(); i++) {
                argsBuilder.append(", ").append(context.getSnippets().getSnippet("INVOKE_ARG_" + argSorts.get(i),
                        Util.createMap("index", String.valueOf(argOffsets.get(i)))));
            }
            if (-stackOffset - 1 != 0) {
                context.output.append(context.getSnippets().getSnippet("INVOKE_POPCNT", Util.createMap("count",
                        String.valueOf(-stackOffset - 1)))).append(" ");
            }
            props.put("class_ptr", context.obfuscator.getCachedClasses().getPointer(node.owner));
            int methodId = context.obfuscator.getCachedMethods().getId(new CachedMethodInfo(
                    node.owner, node.name, node.desc, true
            ));
            context.output.append("if (!cmethods[")
                    .append(methodId)
                    .append("]) { cmethods[")
                    .append(methodId)
                    .append("] = env->GetStaticMethodID(")
                    .append(context.getCachedClasses().getPointer(node.owner))
                    .append(", ")
                    .append(context.getStringPool().get(node.name))
                    .append(", ")
                    .append(context.getStringPool().get(node.desc))
                    .append("); ")
                    .append(trimmedTryCatchBlock)
                    .append("  } ");
            props.put("methodid", context.getCachedMethods().getPointer(new CachedMethodInfo(
                    node.owner, node.name, node.desc, true
            )));
            props.put("args", argsBuilder.toString());
        }
    }
}
