package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.MethodProcessor;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.HashMap;
import java.util.Map;

public abstract class GenericInstructionHandler<T extends AbstractInsnNode> implements InstructionTypeHandler<T> {

    protected final Map<String, String> props = new HashMap<>();
    protected String instructionName;
    protected String trimmedTryCatchBlock;

    @Override
    public void accept(MethodContext context, T node) {
        props.clear();
        instructionName = MethodProcessor.INSTRUCTIONS.getOrDefault(node.getOpcode(), "NOTFOUND");
        trimmedTryCatchBlock = context.getInlineExceptionCheck();

        context.output.append("    ");
        process(context, node);

        if (instructionName != null) {
            context.getSnippets().append(context.output, instructionName, key -> resolveToken(context, key));
        }
        context.output.append('\n');
    }

    private String resolveToken(MethodContext context, String key) {
        switch (key) {
            case "line":
                return Integer.toString(context.line);
            case "trycatchhandler":
                return context.getExceptionCheck();
            case "rettype":
                return MethodProcessor.CPP_TYPES[context.ret.getSort()];
            case "stackindex0":
                return Integer.toString(context.stackPointer);
            case "stackindex1":
                return Integer.toString(context.stackPointer + 1);
            case "stackindexm1":
                return Integer.toString(context.stackPointer - 1);
            case "stackindexm2":
                return Integer.toString(context.stackPointer - 2);
            case "stackindexm3":
                return Integer.toString(context.stackPointer - 3);
            case "stackindexm4":
                return Integer.toString(context.stackPointer - 4);
            default:
                return props.get(key);
        }
    }

    protected abstract void process(MethodContext context, T node);
}
