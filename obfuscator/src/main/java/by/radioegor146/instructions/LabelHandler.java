package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.LabelNode;

public class LabelHandler implements InstructionTypeHandler<LabelNode> {

    @Override
    public void accept(MethodContext context, LabelNode node) {
        context.enterLabel(node);
        context.output.append("    ").append(context.getLabelPool().getName(node.getLabel()))
                .append(":;\n");
    }

    @Override
    public String insnToString(MethodContext context, LabelNode node) {
        return "LABEL " + context.getLabelPool().getName(node.getLabel());
    }

    @Override
    public int getNewStackPointer(LabelNode node, int currentStackPointer) {
        return currentStackPointer;
    }
}
