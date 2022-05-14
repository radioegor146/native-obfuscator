package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiANewArrayHandler extends GenericInstructionHandler<MultiANewArrayInsnNode> {

    @Override
    protected void process(MethodContext context, MultiANewArrayInsnNode node) {
        Type elementType = Type.getType(node.desc).getElementType();
        props.put("required_count", String.valueOf(node.dims));
        int dimensions = node.dims;
        props.put("count", String.valueOf(Type.getType(node.desc).getDimensions()));
        props.put("desc", elementType.getInternalName());
        if (elementType.getSort() != Type.OBJECT) {
            props.put("sort", String.valueOf(elementType.getSort()));
            instructionName = "MULTIANEWARRAY_VALUE";
        }
        props.put("dims", String.format("{ %s }", IntStream.range(context.stackPointer - dimensions, context.stackPointer)
                .mapToObj(i -> String.format("cstack%d.i", i)).collect(Collectors.joining(", "))));
        props.put("returnstackindex", String.valueOf(context.stackPointer - dimensions));
        // TODO
    }

    @Override
    public String insnToString(MethodContext context, MultiANewArrayInsnNode node) {
        return String.format("MULTIANEWARRAY %d %s", node.dims, node.desc);
    }

    @Override
    public int getNewStackPointer(MultiANewArrayInsnNode node, int currentStackPointer) {
        return currentStackPointer - node.dims + 1;
    }
}
