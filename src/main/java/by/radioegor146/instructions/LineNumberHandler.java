package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.LineNumberNode;

public class LineNumberHandler implements InstructionTypeHandler<LineNumberNode> {
    @Override
    public void accept(MethodContext context, LineNumberNode node) {
        context.line = node.line;
        context.output.append(String.format("    // L %d:\n", context.line));
    }

}
