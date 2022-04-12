package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.tree.VarInsnNode;

public class VarHandler extends GenericInstructionHandler<VarInsnNode> {

    @Override
    protected void process(MethodContext context, VarInsnNode node) {
        props.put("var", String.valueOf(node.var));
    }

    @Override
    public String insnToString(MethodContext context, VarInsnNode node) {
        return String.format("%s %d", Util.getOpcodeString(node.getOpcode()), node.var);
    }
}
