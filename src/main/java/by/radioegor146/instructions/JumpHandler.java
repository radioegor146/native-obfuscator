package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.JumpInsnNode;

public class JumpHandler extends GenericInstructionHandler<JumpInsnNode> {

    @Override
    protected void process(MethodContext context, JumpInsnNode node) {
        props.put("label", String.valueOf(context.getLabelPool().getName(node.label.getLabel())));
    }
}
