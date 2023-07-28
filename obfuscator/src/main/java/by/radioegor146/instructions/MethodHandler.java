package by.radioegor146.instructions;

import by.radioegor146.*;
import by.radioegor146.bytecode.PreprocessorUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MethodHandler extends GenericInstructionHandler<MethodInsnNode> {

    private static Type simplifyType(Type type) {
        switch (type.getSort()) {
            case Type.OBJECT:
            case Type.ARRAY:
                return Type.getObjectType("java/lang/Object");
            case Type.METHOD:
                throw new RuntimeException();
        }
        return type;
    }

    private static String simplifyDesc(String desc) {
        return Type.getMethodType(simplifyType(Type.getReturnType(desc)), Arrays.stream(Type.getArgumentTypes(desc))
                .map(MethodHandler::simplifyType).toArray(Type[]::new)).getDescriptor();
    }

    @Override
    protected void process(MethodContext context, MethodInsnNode node) {
        if (PreprocessorUtils.isLookupLocal(node)) {
            context.output.append("if (lookup == nullptr) { lookup = utils::get_lookup(env, clazz); ")
                    .append(trimmedTryCatchBlock).append(" } cstack").append(context.stackPointer).append(".l = lookup;");
            instructionName = null;
            return;
        }
        if (PreprocessorUtils.isClassLoaderLocal(node)) {
            context.output.append("cstack").append(context.stackPointer).append(".l = classloader;");
            instructionName = null;
            return;
        }
        if (PreprocessorUtils.isClassLocal(node)) {
            context.output.append("cstack").append(context.stackPointer).append(".l = clazz;");
            instructionName = null;
            return;
        }
        if (PreprocessorUtils.isLinkCallSiteMethod(node)) {
            Type returnType = Type.getReturnType(node.desc);
            Type[] args = Type.getArgumentTypes(node.desc);
            instructionName += "_" + returnType.getSort();

            StringBuilder argsBuilder = new StringBuilder();
            List<Integer> argOffsets = new ArrayList<>();

            int stackOffset = context.stackPointer;
            for (Type argType : args) {
                stackOffset -= argType.getSize();
            }
            int argumentOffset = stackOffset;
            for (Type argType : args) {
                argOffsets.add(argumentOffset);
                argumentOffset += argType.getSize();
            }

            for (int i = 0; i < argOffsets.size(); i++) {
                argsBuilder.append(", ").append(context.getSnippets().getSnippet("INVOKE_ARG_" + args[i].getSort(),
                        Util.createMap("index", argOffsets.get(i))));
            }

            context.output.append("cstack").append(stackOffset).append(".l = utils::link_call_site(env")
                    .append(argsBuilder).append("); ");
            context.output.append(trimmedTryCatchBlock);
            instructionName = null;
            return;
        }
        if (PreprocessorUtils.isInvokeReverse(node)) {
            // stack - args, mh
            String methodDesc = simplifyDesc(node.desc);
            Type[] methodArguments = Type.getArgumentTypes(methodDesc);
            methodArguments[methodArguments.length - 1] = Type.getObjectType("java/lang/invoke/MethodHandle");
            methodDesc = Type.getMethodDescriptor(Type.getReturnType(methodDesc), methodArguments);
            String mhDesc = simplifyDesc(Type.getMethodType(Type.getReturnType(node.desc),
                    Util.reverse(Util.reverse(Arrays.stream(Type.getArgumentTypes(node.desc)))
                            .skip(1)).toArray(Type[]::new)).getDescriptor());

            HiddenMethodsPool.HiddenMethod hiddenMethod = context.obfuscator.getHiddenMethodsPool()
                    .getMethod("invokereverse", methodDesc, method -> {
                        method.visibleAnnotations = new ArrayList<>();
                        method.visibleAnnotations.add(new AnnotationNode("Ljava/lang/invoke/LambdaForm$Hidden;"));
                        method.visibleAnnotations.add(new AnnotationNode("Ljdk/internal/vm/annotation/Hidden;"));
                        int methodHandleIndex = 0;
                        for (Type argument : Type.getArgumentTypes(mhDesc)) {
                            methodHandleIndex += argument.getSize();
                        }
                        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, methodHandleIndex));
                        int index = 0;
                        for (Type argument : Type.getArgumentTypes(mhDesc)) {
                            method.instructions.add(new VarInsnNode(argument.getOpcode(Opcodes.ILOAD), index));
                            index += argument.getSize();
                        }
                        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                "java/lang/invoke/MethodHandle", "invoke", mhDesc));
                        method.instructions.add(new InsnNode(Type.getReturnType(mhDesc).getOpcode(Opcodes.IRETURN)));
                    });

            node = (MethodInsnNode) node.clone(null);
            node.name = hiddenMethod.getMethodNode().name;
            node.owner = hiddenMethod.getClassNode().name;
            node.desc = hiddenMethod.getMethodNode().desc;
            node.setOpcode(Opcodes.INVOKESTATIC);
        }
        if (node.owner.equals("java/lang/invoke/MethodHandle") &&
                (node.name.equals("invokeExact") || node.name.equals("invoke")) &&
                node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
            // stack - mh, args
            String methodDesc = simplifyDesc(Type.getMethodType(Type.getReturnType(node.desc),
                    Stream.concat(Arrays.stream(new Type[]{
                            Type.getObjectType("java/lang/invoke/MethodHandle")
                    }), Arrays.stream(Type.getArgumentTypes(node.desc))).toArray(Type[]::new)).getDescriptor());
            Type[] methodArguments = Type.getArgumentTypes(methodDesc);
            methodArguments[0] = Type.getObjectType("java/lang/invoke/MethodHandle");
            methodDesc = Type.getMethodDescriptor(Type.getReturnType(methodDesc), methodArguments);
            String mhDesc = simplifyDesc(node.desc);

            HiddenMethodsPool.HiddenMethod hiddenMethod = context.obfuscator.getHiddenMethodsPool()
                    .getMethod("mhinvoke", methodDesc, method -> {
                        method.visibleAnnotations = new ArrayList<>();
                        method.visibleAnnotations.add(new AnnotationNode("Ljava/lang/invoke/LambdaForm$Hidden;"));
                        method.visibleAnnotations.add(new AnnotationNode("Ljdk/internal/vm/annotation/Hidden;"));
                        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        int index = 1;
                        for (Type argument : Type.getArgumentTypes(mhDesc)) {
                            method.instructions.add(new VarInsnNode(argument.getOpcode(Opcodes.ILOAD), index));
                            index += argument.getSize();
                        }
                        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                "java/lang/invoke/MethodHandle", "invoke", mhDesc));
                        method.instructions.add(new InsnNode(Type.getReturnType(mhDesc).getOpcode(Opcodes.IRETURN)));
                    });

            node = (MethodInsnNode) node.clone(null);
            node.name = hiddenMethod.getMethodNode().name;
            node.owner = hiddenMethod.getClassNode().name;
            node.desc = hiddenMethod.getMethodNode().desc;
            node.setOpcode(Opcodes.INVOKESTATIC);
        }

        Type returnType = Type.getReturnType(node.desc);
        Type[] args = Type.getArgumentTypes(node.desc);
        instructionName += "_" + returnType.getSort();

        StringBuilder argsBuilder = new StringBuilder();
        List<Integer> argOffsets = new ArrayList<>();

        int stackOffset = context.stackPointer;
        for (Type argType : args) {
            stackOffset -= argType.getSize();
        }
        int argumentOffset = stackOffset;
        for (Type argType : args) {
            argOffsets.add(argumentOffset);
            argumentOffset += argType.getSize();
        }

        boolean isStatic = node.getOpcode() == Opcodes.INVOKESTATIC;
        int objectOffset = isStatic ? 0 : 1;

        for (int i = 0; i < argOffsets.size(); i++) {
            argsBuilder.append(", ").append(context.getSnippets().getSnippet("INVOKE_ARG_" + args[i].getSort(),
                    Util.createMap("index", argOffsets.get(i))));
        }

        props.put("objectstackindex", String.valueOf(stackOffset - objectOffset));
        props.put("returnstackindex", String.valueOf(stackOffset - objectOffset));

        if (isStatic || node.getOpcode() == Opcodes.INVOKESPECIAL) {
            props.put("class_ptr", context.getCachedClasses().getPointer(node.owner));
        }

        int classId = context.getCachedClasses().getId(node.owner);

        context.output.append(String.format("if (!cclasses[%d] || env->IsSameObject(cclasses[%d], NULL)) { cclasses_mtx[%d].lock(); if (!cclasses[%d] || env->IsSameObject(cclasses[%d], NULL)) { if (jclass clazz = %s) { cclasses[%d] = (jclass) env->NewWeakGlobalRef(clazz); env->DeleteLocalRef(clazz); } } cclasses_mtx[%d].unlock(); %s } ",
                classId,
                classId,
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

    @Override
    public String insnToString(MethodContext context, MethodInsnNode node) {
        return String.format("%s %s.%s%s", Util.getOpcodeString(node.getOpcode()), node.owner, node.name, node.desc);
    }

    @Override
    public int getNewStackPointer(MethodInsnNode node, int currentStackPointer) {
        if (node.getOpcode() != Opcodes.INVOKESTATIC) {
            currentStackPointer -= 1;
        }
        return currentStackPointer - Arrays.stream(Type.getArgumentTypes(node.desc)).mapToInt(Type::getSize).sum()
                + Type.getReturnType(node.desc).getSize();
    }
}
