package by.radioegor146.instructions;

import by.radioegor146.CachedFieldInfo;
import by.radioegor146.MethodContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;

public class FieldInstructionHandler extends GenericInstructionHandler<FieldInsnNode> {

    @Override
    protected void process(MethodContext context, FieldInsnNode node) {
        boolean isStatic = node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.PUTSTATIC;
        CachedFieldInfo info = new CachedFieldInfo(node.owner, node.name, node.desc, isStatic);

        instructionName += "_" + Type.getType(node.desc).getSort();
        if (isStatic) {
            props.put("class_ptr", context.getCachedClasses().getPointer(node.owner));
        }

        int fieldId = context.getCachedFields().getId(info);
        props.put("fieldid", context.getCachedFields().getPointer(info));

        context.output
                .append(String.format("if (!cfields[%d].load()) { cfields[%d].store(env->Get",
                        fieldId, fieldId))
                .append(isStatic ? "Static" : "")
                .append(String.format("FieldID(%s, %s, %s));",
                        context.getCachedClasses().getPointer(node.owner),
                        context.getStringPool().get(node.name),
                        context.getStringPool().get(node.desc)))
                .append(trimmedTryCatchBlock)
                .append("  } ");
    }
}
