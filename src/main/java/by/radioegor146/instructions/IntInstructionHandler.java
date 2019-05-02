package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.IntInsnNode;

public class IntInstructionHandler extends GenericInstructionHandler<IntInsnNode> {

    @Override
    protected void process(MethodContext context, IntInsnNode node) {
        props.put("operand", String.valueOf(node.operand));
        if (node.getOpcode() == Opcodes.NEWARRAY) {
            instructionName += "_" + node.operand;
        }
    }
}
