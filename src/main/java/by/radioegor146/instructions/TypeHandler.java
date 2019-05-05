package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.MethodProcessor;
import org.objectweb.asm.tree.TypeInsnNode;

public class TypeHandler extends GenericInstructionHandler<TypeInsnNode> {

    @Override
    protected void process(MethodContext context, TypeInsnNode node) {
        props.put("desc", node.desc);

        int classId = context.getCachedClasses().getId(node.desc);
        context.output.append(String.format("if (!cclasses[%d]) { cclasses_mtx[%d].lock(); if (!cclasses[%d]) { if (jclass clazz = %s) { cclasses[%d] = (jclass) env->NewGlobalRef(clazz); env->DeleteLocalRef(clazz); } } cclasses_mtx[%d].unlock(); %s } ",
                classId,
                classId,
                classId,
                MethodProcessor.getClassGetter(context, node.desc),
                classId,
                classId,
                trimmedTryCatchBlock));

        props.put("desc_ptr", context.getCachedClasses().getPointer(node.desc));
    }
}
