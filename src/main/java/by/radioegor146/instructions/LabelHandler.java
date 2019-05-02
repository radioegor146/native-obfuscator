package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.tree.LabelNode;

public class LabelHandler implements InstructionTypeHandler<LabelNode> {

    @Override
    public void accept(MethodContext context, LabelNode node) {
        context.output.append(String.format("%s: ;\n", node.getLabel()));
        Util.reverse(context.method.tryCatchBlocks.stream().filter(x -> x.start.equals(node)))
                .forEachOrdered(context.tryCatches::add);
        context.method.tryCatchBlocks.stream().filter(x -> x.end.equals(node))
                .forEachOrdered(context.tryCatches::remove);

    }
}
