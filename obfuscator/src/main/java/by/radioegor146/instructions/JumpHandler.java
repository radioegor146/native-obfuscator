package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.JumpInsnNode;

public class JumpHandler extends GenericInstructionHandler<JumpInsnNode> {

    @Override
    protected void process(MethodContext context, JumpInsnNode node) {
        props.put("label", String.valueOf(context.getLabelPool().getName(node.label.getLabel())));
    }

    @Override
    public String insnToString(MethodContext methodContext, JumpInsnNode node) {
        return String.format("%s %s", Util.getOpcodeString(node.getOpcode()), methodContext.getLabelPool().getName(node.label.getLabel()));
    }

    @Override
    public int getNewStackPointer(JumpInsnNode node, int currentStackPointer) {
        switch (node.getOpcode()) {
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                return currentStackPointer - 1;
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
                return currentStackPointer - 2;
            case Opcodes.GOTO:
                return currentStackPointer;
        }
        throw new RuntimeException();
    }
}
