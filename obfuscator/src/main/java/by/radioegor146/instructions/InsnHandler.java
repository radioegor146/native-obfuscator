package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;

public class InsnHandler extends GenericInstructionHandler<InsnNode> {

    @Override
    protected void process(MethodContext context, InsnNode node) {}

    @Override
    public String insnToString(MethodContext context, InsnNode node) {
        return Util.getOpcodeString(node.getOpcode());
    }

    @Override
    public int getNewStackPointer(InsnNode node, int currentStackPointer) {
        switch (node.getOpcode()) {
            case Opcodes.NOP:
            case Opcodes.ARRAYLENGTH:
            case Opcodes.RETURN:
            case Opcodes.I2S:
            case Opcodes.I2C:
            case Opcodes.I2B:
            case Opcodes.D2L:
            case Opcodes.F2I:
            case Opcodes.L2D:
            case Opcodes.I2F:
            case Opcodes.DNEG:
            case Opcodes.FNEG:
            case Opcodes.LNEG:
            case Opcodes.INEG:
            case Opcodes.SWAP:
            case Opcodes.DALOAD:
            case Opcodes.LALOAD:
                return currentStackPointer;
            case Opcodes.ACONST_NULL:
            case Opcodes.F2D:
            case Opcodes.F2L:
            case Opcodes.I2D:
            case Opcodes.I2L:
            case Opcodes.DUP_X2:
            case Opcodes.DUP_X1:
            case Opcodes.DUP:
            case Opcodes.FCONST_2:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_0:
            case Opcodes.ICONST_5:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_M1:
                return currentStackPointer + 1;
            case Opcodes.LCONST_0:
            case Opcodes.DUP2_X2:
            case Opcodes.DUP2_X1:
            case Opcodes.DUP2:
            case Opcodes.DCONST_1:
            case Opcodes.DCONST_0:
            case Opcodes.LCONST_1:
                return currentStackPointer + 2;
            case Opcodes.IALOAD:
            case Opcodes.MONITOREXIT:
            case Opcodes.MONITORENTER:
            case Opcodes.ARETURN:
            case Opcodes.FRETURN:
            case Opcodes.IRETURN:
            case Opcodes.FCMPG:
            case Opcodes.FCMPL:
            case Opcodes.D2F:
            case Opcodes.D2I:
            case Opcodes.L2F:
            case Opcodes.L2I:
            case Opcodes.IXOR:
            case Opcodes.IOR:
            case Opcodes.IAND:
            case Opcodes.LUSHR:
            case Opcodes.IUSHR:
            case Opcodes.LSHR:
            case Opcodes.ISHR:
            case Opcodes.LSHL:
            case Opcodes.ISHL:
            case Opcodes.FREM:
            case Opcodes.IREM:
            case Opcodes.FDIV:
            case Opcodes.IDIV:
            case Opcodes.FMUL:
            case Opcodes.IMUL:
            case Opcodes.FSUB:
            case Opcodes.ISUB:
            case Opcodes.FADD:
            case Opcodes.IADD:
            case Opcodes.POP:
            case Opcodes.SALOAD:
            case Opcodes.CALOAD:
            case Opcodes.BALOAD:
            case Opcodes.AALOAD:
            case Opcodes.FALOAD:
            case Opcodes.ATHROW:
                return currentStackPointer - 1;
            case Opcodes.IASTORE:
            case Opcodes.DCMPG:
            case Opcodes.DCMPL:
            case Opcodes.LCMP:
            case Opcodes.SASTORE:
            case Opcodes.CASTORE:
            case Opcodes.BASTORE:
            case Opcodes.AASTORE:
            case Opcodes.FASTORE:
                return currentStackPointer - 3;
            case Opcodes.LASTORE:
            case Opcodes.DASTORE:
                return currentStackPointer - 4;
            case Opcodes.POP2:
            case Opcodes.DRETURN:
            case Opcodes.LRETURN:
            case Opcodes.LXOR:
            case Opcodes.LOR:
            case Opcodes.LAND:
            case Opcodes.DREM:
            case Opcodes.LREM:
            case Opcodes.DDIV:
            case Opcodes.LDIV:
            case Opcodes.DMUL:
            case Opcodes.LMUL:
            case Opcodes.DSUB:
            case Opcodes.LSUB:
            case Opcodes.DADD:
            case Opcodes.LADD:
                return currentStackPointer - 2;
        }
        throw new RuntimeException(String.valueOf(node.getOpcode()));
    }
}
