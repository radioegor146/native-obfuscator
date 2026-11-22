package by.radioegor146.bytecode;

import by.radioegor146.Platform;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class LdcPreprocessor implements Preprocessor {
    @Override
    public boolean process(ClassNode classNode, MethodNode methodNode, Platform platform) {
        boolean changed = false;
        AbstractInsnNode insnNode = methodNode.instructions.getFirst();
        while (insnNode != null) {
            if (insnNode instanceof LdcInsnNode) {
                LdcInsnNode ldcInsnNode = (LdcInsnNode) insnNode;

                if (ldcInsnNode.cst instanceof Handle) {
                    methodNode.instructions.insertBefore(ldcInsnNode,
                            MethodHandleUtils.generateMethodHandleLdcInsn((Handle) ldcInsnNode.cst));
                    AbstractInsnNode nextInsnNode = insnNode.getNext();
                    methodNode.instructions.remove(insnNode);
                    changed = true;
                    insnNode = nextInsnNode;
                    continue;
                }

                if (ldcInsnNode.cst instanceof Type) {
                    Type type = (Type) ldcInsnNode.cst;

                    if (type.getSort() == Type.METHOD) {
                        methodNode.instructions.insertBefore(ldcInsnNode,
                                MethodHandleUtils.generateMethodTypeLdcInsn(type));
                        AbstractInsnNode nextInsnNode = insnNode.getNext();
                        methodNode.instructions.remove(insnNode);
                        changed = true;
                        insnNode = nextInsnNode;
                    }
                }
            }

            insnNode = insnNode.getNext();
        }
        return changed;
    }
}
