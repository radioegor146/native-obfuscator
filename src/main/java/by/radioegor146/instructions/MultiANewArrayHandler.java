package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

public class MultiANewArrayHandler extends GenericInstructionHandler<MultiANewArrayInsnNode> {

    @Override
    protected void process(MethodContext context, MultiANewArrayInsnNode node) {
        Type elementType = Type.getType(node.desc).getElementType();
        props.put("required_count", String.valueOf(node.dims));
        props.put("count", String.valueOf(Type.getType(node.desc).getDimensions()));
        props.put("desc", elementType.getInternalName());
        if (elementType.getSort() != Type.OBJECT) {
            props.put("sort", String.valueOf(elementType.getSort()));
            instructionName = "MULTIANEWARRAY_VALUE";
        }
    }

    @Override
    public String insnToString(MethodContext context, MultiANewArrayInsnNode node) {
        return String.format("MULTIANEWARRAY %d %s", node.dims, node.desc);
    }

    @Override
    public int getNewStackPointer(MultiANewArrayInsnNode node, int currentStackPointer) {
        return currentStackPointer - node.dims;
    }
}
