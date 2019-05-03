package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import org.objectweb.asm.tree.AbstractInsnNode;

public class InstructionHandlerContainer<T extends AbstractInsnNode> {

    private final InstructionTypeHandler<T> handler;
    private final Class<T> clazz;

    public InstructionHandlerContainer(InstructionTypeHandler<T> handler, Class<T> clazz) {
        this.handler = handler;
        this.clazz = clazz;
    }

    public void accept(MethodContext context, AbstractInsnNode node) {
        handler.accept(context, clazz.cast(node));
    }
}
