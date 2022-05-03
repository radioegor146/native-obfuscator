package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.Opcodes;
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

    @Override
    public int getNewStackPointer(VarInsnNode node, int currentStackPointer) {
        switch (node.getOpcode()) {
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
            case Opcodes.ALOAD:
                return currentStackPointer + 1;
            case Opcodes.LLOAD:
            case Opcodes.DLOAD:
                return currentStackPointer + 2;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
            case Opcodes.ASTORE:
                return currentStackPointer - 1;
            case Opcodes.LSTORE:
            case Opcodes.DSTORE:
                return currentStackPointer - 2;
        }
        throw new RuntimeException();
    }
}
