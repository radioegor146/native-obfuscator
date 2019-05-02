package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.IincInsnNode;

public class IincInstructionHandler extends GenericInstructionHandler<IincInsnNode> {

    @Override
    protected void process(MethodContext context, IincInsnNode node) {
        props.put("incr", String.valueOf(node.incr));
        props.put("var", String.valueOf(node.var));
    }
}
