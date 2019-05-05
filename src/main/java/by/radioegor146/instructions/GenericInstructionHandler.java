package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.MethodProcessor;
import by.radioegor146.Util;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.HashMap;
import java.util.Map;

public abstract class GenericInstructionHandler<T extends AbstractInsnNode> implements InstructionTypeHandler<T> {

    protected Map<String, String> props;
    protected String instructionName;
    protected String trimmedTryCatchBlock;

    @Override
    public void accept(MethodContext context, T node) {
        props = new HashMap<>();
        StringBuilder tryCatch = new StringBuilder("\n");
        if (context.tryCatches.size() > 0) {
            tryCatch.append(String.format("    %s\n", context.getSnippets().getSnippet("TRYCATCH_START")));
            for (int i = context.tryCatches.size() - 1; i >= 0; i--) {
                TryCatchBlockNode tryCatchBlock = context.tryCatches.get(i);
                if (tryCatchBlock.type == null) {
                    tryCatch.append("    ").append(context.getSnippets().getSnippet("TRYCATCH_ANY_L", Util.createMap(
                            "rettype", MethodProcessor.CPP_TYPES[context.ret.getSort()],
                            "handler_block", tryCatchBlock.handler.getLabel().toString()
                    ))).append("\n");
                    break;
                } else {
                    tryCatch.append("    ").append(context.getSnippets().getSnippet("TRYCATCH_CHECK", Util.createMap(
                            "rettype", MethodProcessor.CPP_TYPES[context.ret.getSort()],
                            "exception_class_ptr", context.getCachedClasses().getPointer(tryCatchBlock.type),
                            "handler_block", tryCatchBlock.handler.getLabel().toString()
                    ))).append("\n");
                }
            }
            tryCatch.append("    ").append(context.getSnippets().getSnippet("TRYCATCH_END",
                    Util.createMap("rettype", MethodProcessor.CPP_TYPES[context.ret.getSort()])));
        } else {
            tryCatch.append("    ").append(context.getSnippets().getSnippet("TRYCATCH_EMPTY",
                    Util.createMap("rettype", MethodProcessor.CPP_TYPES[context.ret.getSort()])));
        }
        context.output.append("    ");
        instructionName = MethodProcessor.INSTRUCTIONS.getOrDefault(node.getOpcode(), "NOTFOUND");
        props.put("line", String.valueOf(context.line));
        props.put("trycatchhandler", tryCatch.toString());
        props.put("rettype", MethodProcessor.CPP_TYPES[context.ret.getSort()]);
        trimmedTryCatchBlock = tryCatch.toString().trim().replace("\n", " ");

        process(context, node);

        context.output.append(context.obfuscator.getSnippets().getSnippet(instructionName, props));
        context.output.append("\n");
    }

    protected abstract void process(MethodContext context, T node);
}
