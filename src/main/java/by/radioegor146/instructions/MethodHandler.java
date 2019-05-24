package by.radioegor146.instructions;

import by.radioegor146.CachedMethodInfo;
import by.radioegor146.MethodContext;
import by.radioegor146.MethodProcessor;
import by.radioegor146.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MethodHandler extends GenericInstructionHandler<MethodInsnNode> {

    @Override
    protected void process(MethodContext context, MethodInsnNode node) {
        if (node.owner.equals("java/lang/invoke/MethodHandle") && (node.name.equals("invokeExact") || node.name.equals("invoke")) && node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
            String newMethodName = String.format("methodhandle$%s$%s", node.name, String.valueOf(node.desc.hashCode()).replace("-", ""));
            List<Type> args = new ArrayList<>();
            args.add(Type.getType("Ljava/lang/invoke/MethodHandle;"));
            args.addAll(Arrays.asList(Type.getArgumentTypes(node.desc)));
            String newDesc = Type.getMethodDescriptor(Type.getReturnType(node.desc), args.toArray(new Type[0]));
            context.getMethodHandleInvokes().put(newDesc, node);
            node = (MethodInsnNode) node.clone(null);
            node.name = newMethodName;
            node.owner = context.clazz.name;
            node.desc = newDesc;
            node.setOpcode(Opcodes.INVOKESTATIC);
        }

        Type returnType = Type.getReturnType(node.desc);
        Type[] args = Type.getArgumentTypes(node.desc);
        instructionName += "_" + returnType.getSort();

        StringBuilder argsBuilder = new StringBuilder();
        List<Integer> argOffsets = new ArrayList<>();

        int stackOffset = -1;
        for (Type argType : args) {
            argOffsets.add(stackOffset);
            stackOffset -= argType.getSize();
        }

        boolean isStatic = node.getOpcode() == Opcodes.INVOKESTATIC;
        int objectOffset = isStatic ? 0 : 1;

        for (int i = 0; i < argOffsets.size(); i++) {
            argsBuilder.append(", ").append(context.getSnippets().getSnippet("INVOKE_ARG_" + args[i].getSort(),
                    Util.createMap("index", argOffsets.get(i) - objectOffset)));
        }

        if (!isStatic || args.length > 0) {
            int count = -(stackOffset + 1) + objectOffset;
            context.output.append(context.getSnippets().getSnippet("INVOKE_POPCNT",
                    Util.createMap("count", count))).append(" ");
        }

        if (isStatic || node.getOpcode() == Opcodes.INVOKESPECIAL) {
            props.put("class_ptr", context.getCachedClasses().getPointer(node.owner));
        }

        int classId = context.getCachedClasses().getId(node.owner);

        context.output.append(String.format("if (!cclasses[%d]) { cclasses_mtx[%d].lock(); if (!cclasses[%d]) { if (jclass clazz = %s) { cclasses[%d] = (jclass) env->NewGlobalRef(clazz); env->DeleteLocalRef(clazz); } } cclasses_mtx[%d].unlock(); %s } ",
                classId,
                classId,
                classId,
                MethodProcessor.getClassGetter(context, node.owner),
                classId,
                classId,
                trimmedTryCatchBlock));

        CachedMethodInfo methodInfo = new CachedMethodInfo(node.owner, node.name, node.desc, isStatic);
        int methodId = context.getCachedMethods().getId(methodInfo);
        props.put("methodid", context.getCachedMethods().getPointer(methodInfo));

        context.output.append(
                String.format("if (!cmethods[%d]) { cmethods[%d] = env->Get%sMethodID(%s, %s, %s); %s  } ",
                        methodId,
                        methodId,
                        isStatic ? "Static" : "",
                        context.getCachedClasses().getPointer(node.owner),
                        context.getStringPool().get(node.name),
                        context.getStringPool().get(node.desc),
                        trimmedTryCatchBlock));

        props.put("args", argsBuilder.toString());
    }
    
    public static void processMethodHandleInvoke(ClassNode classNode, String newMethodDesc, MethodInsnNode invoke) {
        String newMethodName = String.format("methodhandle$%s$%s", invoke.name, String.valueOf(invoke.desc.hashCode()).replace("-", ""));
        MethodNode invokeWrapper = new MethodNode(Opcodes.ASM7,
                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC,
                newMethodName, newMethodDesc, null, new String[0]);

        int localVarsPosition = 0;
        for (Type arg : Type.getArgumentTypes(newMethodDesc)) {
            invokeWrapper.instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
            localVarsPosition += arg.getSize();
        }

        invokeWrapper.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, invoke.owner, invoke.name, invoke.desc));
        invokeWrapper.instructions.add(new InsnNode(Type.getReturnType(newMethodDesc).getOpcode(Opcodes.IRETURN)));
        classNode.methods.add(invokeWrapper);
    }
}
