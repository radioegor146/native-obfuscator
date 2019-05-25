package by.radioegor146.instructions;

import by.radioegor146.MethodContext;
import by.radioegor146.MethodProcessor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LdcInsnNode;

public class LdcHandler extends GenericInstructionHandler<LdcInsnNode> {

    @Override
    protected void process(MethodContext context, LdcInsnNode node) {
        Object cst = node.cst;
        if (cst instanceof String) {
            instructionName += "_STRING";
            props.put("cst_ptr", context.getCachedStrings().getPointer(node.cst.toString()));
        } else if (cst instanceof Integer) {
            instructionName += "_INT";
            int cstVal = (int) cst;
            if (cstVal == Integer.MIN_VALUE) {
                props.put("cst", "(jint) 2147483648U");
            } else {
                props.put("cst", node.cst.toString());
            }
        } else if (cst instanceof Long) {
            instructionName += "_LONG";
            long cstVal = (long) cst;
            if (cstVal == Long.MIN_VALUE) {
                props.put("cst", "(jlong) 9223372036854775808ULL");
            } else {
                props.put("cst", node.cst.toString() + "LL");
            }
        } else if (cst instanceof Float) {
            instructionName += "_FLOAT";
            props.put("cst", node.cst.toString() + "f");
            float cstVal = (float) cst;
            if (cst.toString().equals("NaN")) {
                props.put("cst", "NAN");
            } else if (cstVal == Float.POSITIVE_INFINITY) {
                props.put("cst", "HUGE_VALF");
            } else if (cstVal == Float.NEGATIVE_INFINITY) {
                props.put("cst", "-HUGE_VALF");
            }
        } else if (cst instanceof Double) {
            instructionName += "_DOUBLE";
            props.put("cst", node.cst.toString());
            double cstVal = (double) cst;
            if (cst.toString().equals("NaN")) {
                props.put("cst", "NAN");
            } else if (cstVal == Double.POSITIVE_INFINITY) {
                props.put("cst", "HUGE_VAL");
            } else if (cstVal == Double.NEGATIVE_INFINITY) {
                props.put("cst", "-HUGE_VAL");
            }
        } else if (cst instanceof Type) {
            instructionName += "_CLASS";

            int classId = context.getCachedClasses().getId(node.cst.toString());
            context.output.append(String.format("if (!cclasses[%d]) { cclasses_mtx[%d].lock(); if (!cclasses[%d]) { if (jclass clazz = %s) { cclasses[%d] = (jclass) env->NewGlobalRef(clazz); env->DeleteLocalRef(clazz); } } cclasses_mtx[%d].unlock(); %s } ",
                    classId,
                    classId,
                    classId,
                    MethodProcessor.getClassGetter(context, node.cst.toString()),
                    classId,
                    classId,
                    trimmedTryCatchBlock));
            
            props.put("cst_ptr", context.getCachedClasses().getPointer(node.cst.toString()));
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
