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
        int dimensions = node.dims;
        props.put("desc", node.desc);
        if (elementType.getSort() == Type.OBJECT) {
            context.needsArrayClassLoader = true;
            props.put("classloader", "classloader");
        } else {
            props.put("classloader", "nullptr");
        }
        props.put("dims", String.format("{ %s }", IntStream.range(context.stackPointer - dimensions, context.stackPointer)
                .mapToObj(i -> String.format("cstack%d.i", i)).collect(Collectors.joining(", "))));
        props.put("returnstackindex", String.valueOf(context.stackPointer - dimensions));
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
