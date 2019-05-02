package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

public class MultiANewArrayHandler extends GenericInstructionHandler<MultiANewArrayInsnNode> {

    @Override
    protected void process(MethodContext context, MultiANewArrayInsnNode node) {
        props.put("count", String.valueOf(node.dims));
        props.put("desc", node.desc);
    }
}
