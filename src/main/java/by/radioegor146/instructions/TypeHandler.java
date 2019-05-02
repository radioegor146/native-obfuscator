package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.TypeInsnNode;

public class TypeHandler extends GenericInstructionHandler<TypeInsnNode> {

    @Override
    protected void process(MethodContext context, TypeInsnNode node) {
        props.put("desc", node.desc);
        props.put("desc_ptr", context.getCachedClasses().getPointer(node.desc));
    }
}
