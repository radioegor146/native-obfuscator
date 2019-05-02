package by.radioegor146;

import by.radioegor146.instructions.*;
import by.radioegor146.special.ClInitSpecialMethodProcessor;
import by.radioegor146.special.DefaultSpecialMethodProcessor;
import by.radioegor146.special.InitSpecialMethodProcessor;
import by.radioegor146.special.SpecialMethodProcessor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MethodProcessor {

    public static final Map<Integer, String> INSTRUCTIONS = new HashMap<>();

    static {
        try {
            for (Field f : Opcodes.class.getFields()) {
                INSTRUCTIONS.put((int) f.get(null), f.getName());
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static final String[] CPP_TYPES = {
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

    public static final int[] TYPE_TO_STACK = {
            1, 1, 1, 1, 1, 1, 1, 2, 2, 0, 0, 0
    };

    public static final int[] STACK_TO_STACK = {
            1, 1, 1, 2, 2, 0, 0, 0, 0
    };

    private NativeObfuscator obfuscator;

    private InstructionHandlerContainer<?>[] handlers;

    public MethodProcessor(NativeObfuscator obfuscator) {
        this.obfuscator = obfuscator;

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

    public void processMethod(MethodContext context) {
        MethodNode method = context.method;
        StringBuilder output = context.output;

        if ((Util.getFlag(method.access, Opcodes.ACC_ABSTRACT) || Util.getFlag(method.access, Opcodes.ACC_NATIVE) ||
                method.name.equals("<init>"))) {
            return;
        }

        SpecialMethodProcessor specialMethodProcessor = getSpecialMethodProcessor(method.name);

        output.append("// ").append(method.name).append(method.desc).append("\n");

        String methodName = specialMethodProcessor.preProcess(context);
        methodName = "__ngen_" + methodName.replace("/", "_");
        methodName = Util.escapeCppNameString(methodName);

        boolean isInterface = Util.getFlag(context.clazz.access, Opcodes.ACC_INTERFACE);
        boolean isStatic = Util.getFlag(method.access, Opcodes.ACC_STATIC);
        context.ret = Type.getReturnType(method.desc);
        Type[] args = Type.getArgumentTypes(method.desc);

        context.argTypes = new ArrayList<>(Arrays.asList(args));
        if(!isStatic) {
            context.argTypes.add(0, Type.getType(Object.class));
        }

        if(Util.getFlag(context.clazz.access, Opcodes.ACC_INTERFACE)) {
            String targetDesc = String.format("(%s)%s",
                    context.argTypes.stream().map(Type::getDescriptor).collect(Collectors.joining()),
                    context.ret.getDescriptor());

            String outerJavaMethodName = String.format("iface_static_%d_%d", context.classIndex, context.methodIndex);
            context.nativeMethod = new MethodNode(Opcodes.ASM7,
                    Opcodes.ACC_NATIVE | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                    outerJavaMethodName, targetDesc, null, new String[0]);

            String methodSource = String.format("            { (char *)%s, (char *)%s, (void *)&%s },\n",
                    obfuscator.getStringPool().get(outerJavaMethodName),
                    obfuscator.getStringPool().get(targetDesc), methodName);

            obfuscator.getStaticClassProvider().addMethod(context.nativeMethod, methodSource);
        } else {
            context.nativeMethods.append(String.format("            { (char *)%s, (char *)%s, (void *)&%s },\n",
                    obfuscator.getStringPool().get(context.proxyMethod.name),
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

}
