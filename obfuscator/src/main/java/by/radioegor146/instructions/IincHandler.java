package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.IincInsnNode;

public class IincHandler extends GenericInstructionHandler<IincInsnNode> {

    @Override
    protected void process(MethodContext context, IincInsnNode node) {
        props.put("incr", String.valueOf(node.incr));
        props.put("var", String.valueOf(node.var));
    }

    @Override
    public String insnToString(MethodContext context, IincInsnNode node) {
        return String.format("IINC %d %d", node.var, node.incr);
    }

    @Override
    public int getNewStackPointer(IincInsnNode node, int currentStackPointer) {
        return currentStackPointer;
    }
}
