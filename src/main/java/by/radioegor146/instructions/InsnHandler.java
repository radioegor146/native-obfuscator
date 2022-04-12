package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.tree.InsnNode;

public class InsnHandler extends GenericInstructionHandler<InsnNode> {

    @Override
    protected void process(MethodContext context, InsnNode node) {}

    @Override
    public String insnToString(MethodContext context, InsnNode node) {
        return Util.getOpcodeString(node.getOpcode());
    }
}
