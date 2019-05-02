package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.VarInsnNode;

public class VarHandler extends GenericInstructionHandler<VarInsnNode> {

    @Override
    protected void process(MethodContext context, VarInsnNode node) {
        props.put("var", String.valueOf(node.var));
    }
}
