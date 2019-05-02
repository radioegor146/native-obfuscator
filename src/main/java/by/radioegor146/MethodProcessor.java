package by.radioegor146;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MethodProcessor {

    private static final Map<Integer, String> INSTRUCTIONS = new HashMap<>();

    static {
        try {
            for (Field f : Opcodes.class.getFields()) {
                INSTRUCTIONS.put((int) f.get(null), f.getName());
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final String[] CPP_TYPES = {
            "void", // 0
            "jboolean", // 1
            "jchar", // 2
            "jbyte", // 3
            "jshort", // 4
            "jint", // 5
            "jfloat", // 6
            "jlong", // 7
            "jdouble", // 8
            "jarray", // 9
            "jobject", // 10
            "jobject" // 11
    };

    private static final String[] JAVA_DESCRIPTORS = {
            "V", // 0
            "Z", // 1
            "C", // 2
            "B", // 3
            "S", // 4
            "I", // 5
            "F", // 6
            "J", // 7
            "D", // 8
            "Ljava/lang/Object;", // 9
            "Ljava/lang/Object;", // 10
            "Ljava/lang/Object;" // 11
    };

    private static final int[] TYPE_TO_STACK = {
            1, 1, 1, 1, 1, 1, 1, 2, 2, 0, 0, 0
    };

    private static final int[] STACK_TO_STACK = {
            1, 1, 1, 2, 2, 0, 0, 0, 0
    };

    private NativeObfuscator obfuscator;

    private int methodIndex;
    private int classIndex;
    private MethodNode method;
    private ClassNode clazz;

    private StringBuilder output;
    private StringBuilder nativeMethodsSb;

    private MethodNode proxyMethod;

    private InstructionHandlerContainer<?>[] handlers;

    public MethodProcessor(NativeObfuscator obfuscator,
                           ClassNode clazz, MethodNode method,
                           int classIndex, int methodIndex) {
        this.obfuscator = obfuscator;

        this.clazz = clazz;
        this.method = method;
        this.classIndex = classIndex;
        this.methodIndex = methodIndex;

        output = new StringBuilder();
        nativeMethodsSb = new StringBuilder();

        handlers = new InstructionHandlerContainer[16];
        handlers[0] = new InstructionHandlerContainer<>(new InsnHandler(), InsnNode.class);
        handlers[1] = new InstructionHandlerContainer<>(new IntInstructionHandler(), IntInsnNode.class);
        handlers[2] = new InstructionHandlerContainer<>(new VarHandler(), VarInsnNode.class);
        handlers[3] = new InstructionHandlerContainer<>(new TypeHandler(), TypeInsnNode.class);
        handlers[4] = new InstructionHandlerContainer<>(new FieldInstructionHandler(), FieldInsnNode.class);
        handlers[5] = new InstructionHandlerContainer<>(new MethodHandler(), MethodInsnNode.class);
        handlers[6] = new InstructionHandlerContainer<>(new InvokeDynamicHandler(), InvokeDynamicInsnNode.class);
        handlers[7] = new InstructionHandlerContainer<>(new JumpHandler(), JumpInsnNode.class);
        handlers[8] = new InstructionHandlerContainer<>(new LabelHandler(), LabelNode.class);
        handlers[9] = new InstructionHandlerContainer<>(new LdcHandler(), LdcInsnNode.class);
        handlers[10] = new InstructionHandlerContainer<>(new IincInstructionHandler(), IincInsnNode.class);
        handlers[11] = new InstructionHandlerContainer<>(new TableSwitchHandler(), TableSwitchInsnNode.class);
        handlers[12] = new InstructionHandlerContainer<>(new LookupSwitchHandler(), LookupSwitchInsnNode.class);
        handlers[13] = new InstructionHandlerContainer<>(new MultiANewArrayHandler(), MultiANewArrayInsnNode.class);
        handlers[14] = new InstructionHandlerContainer<>(new FrameHandler(), FrameNode.class);
        handlers[15] = new InstructionHandlerContainer<>(new LineNumberHandler(), LineNumberNode.class);
    }

    private SpecialMethodProcessor getSpecialMethodProcessor(String name) {
        switch (name) {
            case "<init>": return new InitSpecialMethodProcessor();
            case "<clinit>": return new ClInitSpecialMethodProcessor();
            default: return new DefaultSpecialMethodProcessor();
        }
    }

    public String getNativeMethods() {
        return nativeMethodsSb.toString();
    }

    public String getOutput() {
        return output.toString();
    }

    private boolean getFlag(int value, int flag) {
        return (value & flag) > 0;
    }

    public void processMethod() {
        if ((getFlag(method.access, Opcodes.ACC_ABSTRACT) || getFlag(method.access, Opcodes.ACC_NATIVE) ||
                method.name.equals("<init>"))) {
            return;
        }

        InstructionContext context = new InstructionContext();
        SpecialMethodProcessor specialMethodProcessor = getSpecialMethodProcessor(method.name);

        output.append("// ").append(method.name).append(method.desc).append("\n");

        String methodName = specialMethodProcessor.preProcess(context);
        methodName = "__ngen_" + methodName.replace("/", "_");
        methodName = Util.escapeCppNameString(methodName);

        boolean isStatic = getFlag(method.access, Opcodes.ACC_STATIC);
        context.ret = Type.getReturnType(method.desc);
        Type[] args = Type.getArgumentTypes(method.desc);

        context.argTypes = new ArrayList<>(Arrays.asList(args));
        if(!isStatic) {
            context.argTypes.add(0, Type.getType(JAVA_DESCRIPTORS[Type.OBJECT]));
        }

        if(getFlag(clazz.access, Opcodes.ACC_INTERFACE)) {
            String targetDesc = String.format("(%s)%s",
                    context.argTypes.stream().map(Type::getDescriptor).collect(Collectors.joining()),
                    context.ret.getDescriptor());

            String outerJavaMethodName = String.format("iface_static_%d_%d", classIndex, methodIndex);
            context.nativeMethod = new MethodNode(Opcodes.ASM7,
                    Opcodes.ACC_NATIVE | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                    outerJavaMethodName, targetDesc, null, new String[0]);

            String methodSource = String.format("            { (char *)%s, (char *)%s, (void *)&%s },\n",
                    obfuscator.getStringPool().get(outerJavaMethodName),
                    obfuscator.getStringPool().get(targetDesc), methodName);

            obfuscator.getStaticClassProvider().addMethod(context.nativeMethod, methodSource);
        } else {
            nativeMethodsSb.append(String.format("            { (char *)%s, (char *)%s, (void *)&%s },\n",
                    obfuscator.getStringPool().get(proxyMethod.name),
                    obfuscator.getStringPool().get(method.desc), methodName));
        }

        output.append(String.format("%s JNICALL %s(JNIEnv *env, ", CPP_TYPES[context.ret.getSort()], methodName));
        output.append(isStatic ? "jclass clazz" : "jobject obj");

        ArrayList<String> argNames = new ArrayList<>();
        if(!isStatic) argNames.add("obj");

        for (int i = 0; i < args.length; i++) {
            argNames.add("arg" + i);
            output.append(String.format(", %s arg%d", CPP_TYPES[args[i].getSort()], i));
        }

        output.append(") {").append("\n");

        if(method.maxStack > 0) {
            output.append(String.format("    utils::jvm_stack<%d> cstack;\n", method.maxStack));
        }

        if(method.maxLocals > 0) {
            output.append(String.format("    utils::local_vars<%d> clocals;\n", method.maxLocals));
        }

        output.append("    std::unordered_set<jobject> refs;\n");
        output.append("\n");

        int localIndex = 0;
        for(int i = 0; i < context.argTypes.size(); ++i) {
            Type current = context.argTypes.get(i);
            output.append("    ").append(obfuscator.getSnippets().getSnippet(
                    "LOCAL_LOAD_ARG_" + current.getSort(), Util.createMap(
                            "index", localIndex,
                            "arg", argNames.get(i)
                    ))).append("\n");
            localIndex += current.getSize();
        }
        output.append("\n");

        context.argTypes.forEach(t -> context.locals.add(TYPE_TO_STACK[t.getSort()]));

        for(int instruction = 0; instruction < method.instructions.size(); ++instruction) {
            AbstractInsnNode node = method.instructions.get(instruction);

            if(method.name.equals("<init>") && context.invokeSpecialId == -1) {
                if(node.getOpcode() == Opcodes.INVOKESPECIAL) {
                    context.invokeSpecialId = instruction;
                }
                continue;
            }

            handlers[node.getType()].accept(context, node);
        }

        output.append(String.format("    return (%s) 0;\n", CPP_TYPES[context.ret.getSort()]));
        output.append("}\n\n");

        method.localVariables.clear();
        method.tryCatchBlocks.clear();

        specialMethodProcessor.postProcess(context);
    }

    private class InstructionContext {
        private Type ret;
        private ArrayList<Type> argTypes;

        private int line = -1;
        private int invokeSpecialId = -1;
        private List<Integer> stack = new ArrayList<>();
        private List<Integer> locals = new ArrayList<>();
        private List<TryCatchBlockNode> tryCatches = new ArrayList<>();

        private MethodNode nativeMethod;
    }

    private class DefaultSpecialMethodProcessor implements SpecialMethodProcessor {

        @Override
        public String preProcess(InstructionContext context) {
            proxyMethod = method;
            method.access |= Opcodes.ACC_NATIVE;
            return "native_" + method.name + methodIndex;
        }

        @Override
        public void postProcess(InstructionContext context) {
            method.instructions.clear();
            if ((clazz.access & Opcodes.ACC_INTERFACE) > 0) {
                InsnList list = new InsnList();
                for (int i = 0; i <= context.invokeSpecialId; i++) {
                    list.add(method.instructions.get(i));
                }
                int localVarsPosition = 0;
                for (Type arg : context.argTypes) {
                    list.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
                    localVarsPosition += arg.getSize();
                }
                if (context.nativeMethod == null) {
                    throw new RuntimeException("Native method not created?!");
                }
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        obfuscator.getStaticClassProvider().getCurrentClassName(), context.nativeMethod.name,
                        context.nativeMethod.desc));
                list.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN)));
                method.instructions = list;
            }
        }
    }

    private class ClInitSpecialMethodProcessor implements SpecialMethodProcessor {

        @Override
        public String preProcess(InstructionContext context) {
            String name = "native_special_clinit" + methodIndex;
            proxyMethod = new MethodNode(Opcodes.ASM7,
                    Opcodes.ACC_NATIVE | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                    name, method.desc, method.signature, new String[0]);
            clazz.methods.add(proxyMethod);
            return name;
        }

        @Override
        public void postProcess(InstructionContext context) {
            method.instructions.clear();
            method.instructions.add(new LdcInsnNode(classIndex));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, obfuscator.getNativeDir() + "/Loader",
                    "registerNativesForClass", "(I)V"));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, clazz.name,
                    "native_special_clinit" + methodIndex, method.desc));
            if ((clazz.access & Opcodes.ACC_INTERFACE) > 0) {
                if (context.nativeMethod == null) {
                    throw new RuntimeException("Native method not created?!");
                }
                proxyMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        obfuscator.getStaticClassProvider().getCurrentClassName(), context.nativeMethod.name,
                        context.nativeMethod.desc));
                proxyMethod.instructions.add(new InsnNode(Opcodes.RETURN));
            }
            method.instructions.add(new InsnNode(Opcodes.RETURN));
        }
    }

    private class InitSpecialMethodProcessor implements SpecialMethodProcessor {

        @Override
        public String preProcess(InstructionContext context) {
            String name = "native_special_init" + methodIndex;
            proxyMethod = new MethodNode(Opcodes.ASM7,
                    Opcodes.ACC_NATIVE | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                    name, method.desc, method.signature, new String[0]);
            clazz.methods.add(proxyMethod);
            return name;
        }

        @Override
        public void postProcess(InstructionContext context) {
            InsnList list = new InsnList();
            for (int i = 0; i <= context.invokeSpecialId; i++) {
                list.add(method.instructions.get(i));
            }
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            int localVarsPosition = 1;
            for (Type arg : context.argTypes) {
                list.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
                localVarsPosition += arg.getSize();
            }
            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, clazz.name, "native_special_init" + methodIndex,
                    method.desc));
            list.add(new InsnNode(Opcodes.RETURN));
            method.instructions = list;
        }
    }

    private interface SpecialMethodProcessor {
        String preProcess(InstructionContext context);
        void postProcess(InstructionContext context);
    }

    private static class InstructionHandlerContainer<T extends AbstractInsnNode> {

        private InstructionTypeHandler<T> handler;
        private Class<T> clazz;

        public InstructionHandlerContainer(InstructionTypeHandler<T> handler, Class<T> clazz) {
            this.handler = handler;
            this.clazz = clazz;
        }

        public void accept(InstructionContext context, AbstractInsnNode node) {
            handler.accept(context, clazz.cast(node));
        }
    }

    private interface InstructionTypeHandler<T extends AbstractInsnNode> {
        void accept(InstructionContext context, T node);
    }

    private class LineNumberHandler implements InstructionTypeHandler<LineNumberNode> {
        @Override
        public void accept(InstructionContext context, LineNumberNode node) {
            context.line = node.line;
            output.append(String.format("    // Line %d:\n", context.line));
        }

    }

    private class LabelHandler implements InstructionTypeHandler<LabelNode> {

        @Override
        public void accept(InstructionContext context, LabelNode node) {
            output.append(String.format("%s: ;\n", node.getLabel()));
            Util.reverse(method.tryCatchBlocks.stream().filter(x -> x.start.equals(node)))
                    .forEachOrdered(context.tryCatches::add);
            method.tryCatchBlocks.stream().filter(x -> x.end.equals(node))
                    .forEachOrdered(context.tryCatches::remove);

        }
    }

    private class FrameHandler implements InstructionTypeHandler<FrameNode> {

        @Override
        public void accept(InstructionContext context, FrameNode node) {
            switch (node.type) {
                case Opcodes.F_APPEND:
                    node.local.forEach((local) -> {
                        if (local instanceof String) {
                            context.locals.add(TYPE_TO_STACK[Type.OBJECT]);
                        } else if (local instanceof LabelNode) {
                            context.locals.add(TYPE_TO_STACK[Type.OBJECT]);
                        } else {
                            context.locals.add(STACK_TO_STACK[(int) local]);
                        }
                    });
                    break;
                case Opcodes.F_CHOP:
                    node.local.forEach((_item) -> {
                        context.locals.remove(context.locals.size() - 1);
                    });
                    context.stack.clear();
                    break;
                case Opcodes.F_NEW:
                case Opcodes.F_FULL:
                    context.locals.clear();
                    context.stack.clear();
                    node.local.forEach((local) -> {
                        if (local instanceof String) {
                            context.locals.add(TYPE_TO_STACK[Type.OBJECT]);
                        } else if (local instanceof LabelNode) {
                            context.locals.add(TYPE_TO_STACK[Type.OBJECT]);
                        } else {
                            context.locals.add(STACK_TO_STACK[(int) local]);
                        }
                    });
                    node.stack.forEach((stack) -> {
                        if (stack instanceof String) {
                            context.stack.add(TYPE_TO_STACK[Type.OBJECT]);
                        } else if (stack instanceof LabelNode) {
                            context.stack.add(TYPE_TO_STACK[Type.OBJECT]);
                        } else {
                            context.stack.add(STACK_TO_STACK[(int) stack]);
                        }
                    });
                    break;
                case Opcodes.F_SAME:
                    break;
                case Opcodes.F_SAME1:
                    if (node.stack.get(0) instanceof String) {
                        context.stack.add(TYPE_TO_STACK[Type.OBJECT]);
                    } else if (node.stack.get(0) instanceof LabelNode) {
                        context.stack.add(TYPE_TO_STACK[Type.OBJECT]);
                    } else {
                        context.stack.add(STACK_TO_STACK[(int) node.stack.get(0)]);
                    }
                    break;
            }
            if (context.stack.stream().anyMatch(x -> x == 0)) {
                int currentSp = 0;
                output.append("    ");
                for (int type : context.stack) {
                    if (type == 0) {
                        output.append("refs.erase(cstack.refs[").append(currentSp).append("]); ");
                    }
                    currentSp += Math.max(1, type);
                }
                output.append("\n");
            }
            if (context.locals.stream().anyMatch(x -> x == 0)) {
                int currentLp = 0;
                output.append("    ");
                for (int type : context.locals) {
                    if (type == 0) {
                        output.append("refs.erase(clocals.refs[").append(currentLp).append("]); ");
                    }
                    currentLp += Math.max(1, type);
                }
                output.append("\n");
            }
            output.append("    utils::clear_refs(env, refs);\n");
        }
    }

    private abstract class GenericInstructionHandler<T extends AbstractInsnNode> implements InstructionTypeHandler<T> {


        protected Map<String, String> props;
        protected String instructionName;
        protected String trimmedTryCatchBlock;

        @Override
        public void accept(InstructionContext context, T node) {
            props = new HashMap<>();
            StringBuilder tryCatch = new StringBuilder("\n");
            if (context.tryCatches.size() > 0) {
                tryCatch.append(String.format("    %s\n", obfuscator.getSnippets().getSnippet("TRYCATCH_START")));
                for (int i = context.tryCatches.size() - 1; i >= 0; i--) {
                    TryCatchBlockNode tryCatchBlock = context.tryCatches.get(i);
                    if (tryCatchBlock.type == null) {
                        tryCatch.append("    ").append(obfuscator.getSnippets().getSnippet("TRYCATCH_ANY_L", Util.createMap(
                                "rettype", CPP_TYPES[context.ret.getSort()],
                                "handler_block", tryCatchBlock.handler.getLabel().toString()
                        ))).append("\n");
                        break;
                    } else {
                        tryCatch.append("    ").append(obfuscator.getSnippets().getSnippet("TRYCATCH_CHECK", Util.createMap(
                                "rettype", CPP_TYPES[context.ret.getSort()],
                                "exception_class_ptr", obfuscator.getCachedClasses().getPointer(tryCatchBlock.type),
                                "handler_block", tryCatchBlock.handler.getLabel().toString()
                        ))).append("\n");
                    }
                }
                tryCatch.append("    ").append(obfuscator.getSnippets().getSnippet("TRYCATCH_END", Util.createMap("rettype",
                        CPP_TYPES[context.ret.getSort()])));
            } else {
                tryCatch.append("    ").append(obfuscator.getSnippets().getSnippet("TRYCATCH_EMPTY", Util.createMap("rettype",
                        CPP_TYPES[context.ret.getSort()])));
            }
            output.append("    ");
            instructionName = INSTRUCTIONS.getOrDefault(node.getOpcode(), "NOTFOUND");
            props.put("line", String.valueOf(context.line));
            props.put("trycatchhandler", tryCatch.toString());
            props.put("rettype", CPP_TYPES[context.ret.getSort()]);
            trimmedTryCatchBlock = tryCatch.toString().trim().replace("\n", " ");

            process(context, node);

            output.append(obfuscator.getSnippets().getSnippet(instructionName, props));
            output.append("\n");
        }

        protected abstract void process(InstructionContext context, T node);
    }

    private class IincInstructionHandler extends GenericInstructionHandler<IincInsnNode> {

        @Override
        protected void process(InstructionContext context, IincInsnNode node) {
            props.put("incr", String.valueOf(node.incr));
            props.put("var", String.valueOf(node.var));
        }
    }

    private class IntInstructionHandler extends GenericInstructionHandler<IntInsnNode> {

        @Override
        protected void process(InstructionContext context, IntInsnNode node) {
            props.put("operand", String.valueOf(node.operand));
            if (node.getOpcode() == Opcodes.NEWARRAY) {
                instructionName += "_" + node.operand;
            }
        }
    }

    private class FieldInstructionHandler extends GenericInstructionHandler<FieldInsnNode> {

        @Override
        protected void process(InstructionContext context, FieldInsnNode node) {
            instructionName += "_" + Type.getType(node.desc).getSort();
            if (node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.PUTSTATIC) {
                props.put("class_ptr", obfuscator.getCachedClasses().getPointer(node.owner));
            }
            int fieldId = obfuscator.getCachedFields().getId(new CachedFieldInfo(
                    node.owner, node.name, node.desc,
                    node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.PUTSTATIC));

            output.append("if (!cfields[")
                    .append(fieldId)
                    .append("].load()) { cfields[")
                    .append(fieldId)
                    .append("].store(env->Get")
                    .append((node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.PUTSTATIC) ?
                            "Static" : "")
                    .append("FieldID(")
                    .append(obfuscator.getCachedClasses().getPointer(node.owner))
                    .append(", ")
                    .append(obfuscator.getStringPool().get(node.name))
                    .append(", ")
                    .append(obfuscator.getStringPool().get(node.desc))
                    .append(")); ")
                    .append(trimmedTryCatchBlock)
                    .append("  } ");
            props.put("fieldid", obfuscator.getCachedFields().getPointer(new CachedFieldInfo(
                    node.owner, node.name, node.desc,
                    node.getOpcode() == Opcodes.GETSTATIC || node.getOpcode() == Opcodes.PUTSTATIC
            )));
        }
    }

    private class InvokeDynamicHandler extends GenericInstructionHandler<InvokeDynamicInsnNode> {

        @Override
        protected void process(InstructionContext context, InvokeDynamicInsnNode node) {
            String indyMethodName = "invokedynamic$" + method.name + "$" + obfuscator.getInvokeDynamics().size();
            obfuscator.getInvokeDynamics().put(indyMethodName, node);
            Type returnType = Type.getReturnType(node.desc);
            Type[] argTypes = Type.getArgumentTypes(node.desc);
            instructionName = "INVOKESTATIC_" + returnType.getSort();
            StringBuilder argsBuilder = new StringBuilder();
            List<Integer> argOffsets = new ArrayList<>();
            List<Integer> argSorts = new ArrayList<>();
            int stackOffset = -1;
            for (Type argType : argTypes) {
                int currentOffset = stackOffset;
                stackOffset -= argType.getSize();
                argOffsets.add(currentOffset);
                argSorts.add(argType.getSort());
            }
            for (int i = 0; i < argOffsets.size(); i++) {
                argsBuilder.append(", ").append(obfuscator.getSnippets().getSnippet("INVOKE_ARG_" + argSorts.get(i),
                        Util.createMap("index",
                        String.valueOf(argOffsets.get(i)))));
            }
            output.append(obfuscator.getSnippets().getSnippet("INVOKE_POPCNT",
                    Util.createMap("count", String.valueOf(-stackOffset - 1)))).append(" ");
            props.put("class_ptr", obfuscator.getCachedClasses().getPointer(clazz.name));
            int methodId = obfuscator.getCachedMethods().getId(new CachedMethodInfo(
                    clazz.name,
                    indyMethodName,
                    node.desc,
                    true
            ));
            output.append("if (!cmethods[")
                    .append(methodId)
                    .append("].load()) { cmethods[")
                    .append(methodId)
                    .append("].store(env->GetStaticMethodID(")
                    .append(obfuscator.getCachedClasses().getPointer(clazz.name))
                    .append(", ")
                    .append(obfuscator.getStringPool().get(indyMethodName))
                    .append(", ")
                    .append(obfuscator.getStringPool().get(node.desc))
                    .append(")); ")
                    .append(trimmedTryCatchBlock)
                    .append("  } ");
            props.put("methodid", obfuscator.getCachedMethods().getPointer(new CachedMethodInfo(
                    clazz.name, indyMethodName, node.desc, true
            )));
            props.put("args", argsBuilder.toString());
        }
    }

    private class JumpHandler extends GenericInstructionHandler<JumpInsnNode> {

        @Override
        protected void process(InstructionContext context, JumpInsnNode node) {
            props.put("label", String.valueOf(node.label.getLabel()));
        }
    }

    private class LdcHandler extends GenericInstructionHandler<LdcInsnNode> {

        @Override
        protected void process(InstructionContext context, LdcInsnNode node) {
            Object cst = node.cst;
            if (cst instanceof java.lang.String) {
                instructionName += "_STRING";
                props.put("cst", String.valueOf(node.cst));
            } else if (cst instanceof java.lang.Integer) {
                instructionName += "_INT";
                props.put("cst", String.valueOf(node.cst));
            } else if (cst instanceof java.lang.Long) {
                instructionName += "_LONG";
                long cstVal = (long) cst;
                if (cstVal == Long.MIN_VALUE) {
                    props.put("cst", "(jlong) 9223372036854775808ULL");
                } else {
                    props.put("cst", node.cst + "LL");
                }
            } else if (cst instanceof java.lang.Float) {
                instructionName += "_FLOAT";
                props.put("cst", String.valueOf(node.cst));
                float cstVal = (float) cst;
                if (cst.toString().equals("NaN")) {
                    props.put("cst", "NAN");
                } else if (cstVal == Float.POSITIVE_INFINITY) {
                    props.put("cst", "HUGE_VALF");
                } else if (cstVal == Float.NEGATIVE_INFINITY) {
                    props.put("cst", "-HUGE_VALF");
                }
            } else if (cst instanceof java.lang.Double) {
                instructionName += "_DOUBLE";
                props.put("cst", String.valueOf(node.cst));
                double cstVal = (double) cst;
                if (cst.toString().equals("NaN")) {
                    props.put("cst", "NAN");
                } else if (cstVal == Double.POSITIVE_INFINITY) {
                    props.put("cst", "HUGE_VAL");
                } else if (cstVal == Double.NEGATIVE_INFINITY) {
                    props.put("cst", "-HUGE_VAL");
                }
            } else if (cst instanceof org.objectweb.asm.Type) {
                instructionName += "_CLASS";
                props.put("cst_ptr", obfuscator.getCachedClasses().getPointer(node.cst.toString()));
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private class LookupSwitchHandler extends GenericInstructionHandler<LookupSwitchInsnNode> {

        @Override
        protected void process(InstructionContext context, LookupSwitchInsnNode node) {
            output.append(obfuscator.getSnippets().getSnippet("LOOKUPSWITCH_START")).append("\n");
            for (int switchIndex = 0; switchIndex < node.labels.size(); switchIndex++) {
                output.append("    ").append("    ").append(obfuscator.getSnippets().getSnippet("LOOKUPSWITCH_PART",
                        Util.createMap(
                        "key", String.valueOf(node.keys.get(switchIndex)),
                        "label", String.valueOf(node.labels.get(switchIndex).getLabel())
                ))).append("\n");
            }
            output.append("    ").append("    ").append(obfuscator.getSnippets().getSnippet("LOOKUPSWITCH_DEFAULT",
                    Util.createMap(
                    "label", String.valueOf(node.dflt.getLabel())
            ))).append("\n");
            instructionName = "LOOKUPSWITCH_END";
        }
    }

    private class MethodHandler extends GenericInstructionHandler<MethodInsnNode> {

        @Override
        protected void process(InstructionContext context, MethodInsnNode node) {
            Type returnType = Type.getReturnType(node.desc);
            Type[] argTypes = Type.getArgumentTypes(node.desc);
            instructionName += "_" + returnType.getSort();
            StringBuilder argsBuilder = new StringBuilder();
            List<Integer> argOffsets = new ArrayList<>();
            List<Integer> argSorts = new ArrayList<>();
            int stackOffset = -1;
            for (Type argType : argTypes) {
                int currentOffset = stackOffset;
                stackOffset -= argType.getSize();
                argOffsets.add(currentOffset);
                argSorts.add(argType.getSort());
            }
            if (node.getOpcode() == Opcodes.INVOKEINTERFACE || node.getOpcode() == Opcodes.INVOKESPECIAL || node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                for (int i = 0; i < argOffsets.size(); i++) {
                    argsBuilder.append(", ").append(obfuscator.getSnippets().getSnippet("INVOKE_ARG_" + argSorts.get(i),
                            Util.createMap(
                            "index", String.valueOf(argOffsets.get(i) - 1))));
                }
                if (stackOffset != 0) {
                    output.append(obfuscator.getSnippets().getSnippet("INVOKE_POPCNT", Util.createMap("count",
                            String.valueOf(-stackOffset)))).append(" ");
                }
                if (node.getOpcode() == Opcodes.INVOKESPECIAL) {
                    props.put("class_ptr", obfuscator.getCachedClasses().getPointer(node.owner));
                }
                int methodId = obfuscator.getCachedMethods().getId(new CachedMethodInfo(
                        node.owner, node.name, node.desc, false
                ));
                output.append("if (!cmethods[")
                        .append(methodId)
                        .append("].load()) { cmethods[")
                        .append(methodId)
                        .append("].store(env->GetMethodID(")
                        .append(obfuscator.getCachedClasses().getPointer(node.owner))
                        .append(", ")
                        .append(obfuscator.getStringPool().get(node.name))
                        .append(", ")
                        .append(obfuscator.getStringPool().get(node.desc))
                        .append(")); ")
                        .append(trimmedTryCatchBlock)
                        .append("  } ");
                props.put("methodid", obfuscator.getCachedMethods().getPointer(new CachedMethodInfo(
                        node.owner, node.name, node.desc, false
                )));
                props.put("object_offset", "-1");
                props.put("args", argsBuilder.toString());
            } else {
                for (int i = 0; i < argOffsets.size(); i++) {
                    argsBuilder.append(", ").append(obfuscator.getSnippets().getSnippet("INVOKE_ARG_" + argSorts.get(i),
                            Util.createMap(
                            "index", String.valueOf(argOffsets.get(i)))));
                }
                if (-stackOffset - 1 != 0) {
                    output.append(obfuscator.getSnippets().getSnippet("INVOKE_POPCNT", Util.createMap("count",
                            String.valueOf(-stackOffset - 1)))).append(" ");
                }
                props.put("class_ptr", obfuscator.getCachedClasses().getPointer(node.owner));
                int methodId = obfuscator.getCachedMethods().getId(new CachedMethodInfo(
                        node.owner,
                        node.name,
                        node.desc,
                        true
                ));
                output.append("if (!cmethods[")
                        .append(methodId)
                        .append("].load()) { cmethods[")
                        .append(methodId)
                        .append("].store(env->GetStaticMethodID(")
                        .append(obfuscator.getCachedClasses().getPointer(node.owner))
                        .append(", ")
                        .append(obfuscator.getStringPool().get(node.name))
                        .append(", ")
                        .append(obfuscator.getStringPool().get(node.desc))
                        .append(")); ")
                        .append(trimmedTryCatchBlock)
                        .append("  } ");
                props.put("methodid", obfuscator.getCachedMethods().getPointer(new CachedMethodInfo(
                        node.owner, node.name, node.desc, true
                )));
                props.put("args", argsBuilder.toString());
            }
        }
    }

    private class MultiANewArrayHandler extends GenericInstructionHandler<MultiANewArrayInsnNode> {

        @Override
        protected void process(InstructionContext context, MultiANewArrayInsnNode node) {
            props.put("count", String.valueOf(node.dims));
            props.put("desc", node.desc);
        }
    }

    public class TableSwitchHandler extends GenericInstructionHandler<TableSwitchInsnNode> {

        @Override
        protected void process(InstructionContext context, TableSwitchInsnNode node) {
            output.append(obfuscator.getSnippets().getSnippet("TABLESWITCH_START", Util.createMap())).append("\n");
            for (int switchIndex = 0; switchIndex < node.labels.size(); switchIndex++) {
                output.append("    ").append("    ").append(obfuscator.getSnippets().getSnippet("TABLESWITCH_PART",
                        Util.createMap(
                        "index", String.valueOf(node.min + switchIndex),
                        "label", String.valueOf(node.labels.get(switchIndex).getLabel())
                ))).append("\n");
            }
            output.append("    ").append("    ").append(obfuscator.getSnippets().getSnippet("TABLESWITCH_DEFAULT",
                    Util.createMap(
                    "label", String.valueOf(node.dflt.getLabel())
            ))).append("\n");
            output.append("    ");
            instructionName = "TABLESWITCH_END";
        }
    }

    public class TypeHandler extends GenericInstructionHandler<TypeInsnNode> {

        @Override
        protected void process(InstructionContext context, TypeInsnNode node) {
            props.put("desc", node.desc);
            props.put("desc_ptr", obfuscator.getCachedClasses().getPointer(node.desc));
        }
    }

    public class VarHandler extends GenericInstructionHandler<VarInsnNode> {

        @Override
        protected void process(InstructionContext context, VarInsnNode node) {
            props.put("var", String.valueOf(node.var));
        }
    }

    public class InsnHandler extends GenericInstructionHandler<InsnNode> {

        @Override
        protected void process(InstructionContext context, InsnNode node) {}
    }
}
