package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.MethodProcessor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.function.Consumer;

public class FrameHandler implements InstructionTypeHandler<FrameNode> {

    @Override
    public void accept(MethodContext context, FrameNode node) {
        Consumer<Object> appendLocal = local -> {
            if (local instanceof String) {
                context.locals.add(MethodProcessor.TYPE_TO_STACK[Type.OBJECT]);
            } else if (local instanceof LabelNode) {
                context.locals.add(MethodProcessor.TYPE_TO_STACK[Type.OBJECT]);
            } else {
                context.locals.add(MethodProcessor.STACK_TO_STACK[(int) local]);
            }
        };

        Consumer<Object> appendStack = stack -> {
            if (stack instanceof String) {
                context.stack.add(MethodProcessor.TYPE_TO_STACK[Type.OBJECT]);
            } else if (stack instanceof LabelNode) {
                context.stack.add(MethodProcessor.TYPE_TO_STACK[Type.OBJECT]);
            } else {
                context.stack.add(MethodProcessor.STACK_TO_STACK[(int) stack]);
            }
        };


        switch (node.type) {
            case Opcodes.F_APPEND:
                node.local.forEach(appendLocal);
                context.stack.clear();
                break;

            case Opcodes.F_CHOP:
                node.local.forEach(item -> context.locals.remove(context.locals.size() - 1));
                context.stack.clear();
                break;

            case Opcodes.F_NEW:
            case Opcodes.F_FULL:
                context.locals.clear();
                context.stack.clear();
                node.local.forEach(appendLocal);
                node.stack.forEach(appendStack);
                break;

            case Opcodes.F_SAME:
                context.stack.clear();
                break;

            case Opcodes.F_SAME1:
                context.stack.clear();
                appendStack.accept(node.stack.get(0));
                break;
        }

        if (context.stack.stream().anyMatch(x -> x == 0)) {
            int currentSp = 0;
            context.output.append("    ");
            for (int type : context.stack) {
                if (type == 0) {
                    context.output.append("refs.erase(cstack.refs[").append(currentSp).append("]); ");
                }
                currentSp += Math.max(1, type);
            }
            context.output.append("\n");
        }

        if (context.locals.stream().anyMatch(x -> x == 0)) {
            int currentLp = 0;
            context.output.append("    ");
            for (int type : context.locals) {
                if (type == 0) {
                    context.output.append("refs.erase(clocals.refs[").append(currentLp).append("]); ");
                }
                currentLp += Math.max(1, type);
            }
            context.output.append("\n");
        }
        context.output.append("    utils::clear_refs(env, refs);\n");
    }
}
