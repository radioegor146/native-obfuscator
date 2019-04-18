package by.radioegor146;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import ru.gravit.launchserver.asm.ClassMetadataReader;
import ru.gravit.launchserver.asm.SafeClassWriter;

/**
 *
 * @author radioegor146
 */

public class NativeObfuscator {

    private static final Pattern PATTERN = Pattern.compile("([^a-zA-Z_0-9])");
    private static final Map<Integer, String> INSTRUCTIONS = new HashMap<>();
    private static final Properties CPP_SNIPPETS = new Properties();
    private static StringBuilder nativeMethodsSb = new StringBuilder();
    private static Map<String, InvokeDynamicInsnNode> invokeDynamics = new HashMap<>();
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

    private static String escapeString(String value) {
        return bytesToCString(value.getBytes(StandardCharsets.UTF_8));
    }
    
    private static String bytesToCString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        Map<Byte, String> specificChanges = new HashMap<>();
        specificChanges.put((byte) '\\', "\\\\");
        specificChanges.put((byte) '\b', "\\b");
        specificChanges.put((byte) '\n', "\\n");
        specificChanges.put((byte) '\t', "\\t");
        specificChanges.put((byte) '\r', "\\r");
        specificChanges.put((byte) '\f', "\\f");
        specificChanges.put((byte) '"', "\\\"");
        for (byte b : data) {
            if (specificChanges.containsKey(b)) {
                sb.append(specificChanges.get(b));
                continue;
            }
            if (b >= 32 && b < 127)
                sb.append((char) b);
            else
                sb.append("\\x").append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private static String getCppString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder("new char[" + (bytes.length + 1) + "] { ");
        for (int i = 0; i < bytes.length; i++)
            result.append(bytes[i]).append(", ");
        return result.append("0 }").toString();
//        if (bytes.length > 128) {
//            StringBuilder result = new StringBuilder("((const char *)(std::initializer_list<char>({ ");
//            for (int i = 0; i < bytes.length; i++)
//                result.append(bytes[i]).append(i == bytes.length - 1 ? "" : ", ");
//            return result.append(", 0 }).begin()))").toString();
//        } else 
//            return "\"" + bytesToCString(bytes) + "\"";
    }
    
    private static String escapeCppNameString(String value) {
        Matcher m = PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer(value.length());
        while (m.find()) 
            m.appendReplacement(sb, String.valueOf((int) m.group(1).charAt(0)));
        m.appendTail(sb);
        String output = sb.toString();
        if (output.length() > 0 && (output.charAt(0) >= '0' && output.charAt(0) <= '9'))
            output = "_" + output;
        return output;
    }
    
    private static Map<String, String> createMap(Object... parts) {
        HashMap<String, String> tokens = new HashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            tokens.put(parts[i].toString(), parts[i + 1].toString());
        }
        return tokens;
    }
    
    private static String dynamicFormat(String string, Map<String, String> tokens) {
        String patternString = "\\$(" + String.join("|", tokens.keySet().stream().map(x -> unicodify(x)).collect(Collectors.toList())) + ")";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(string);

        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(tokens.get(matcher.group(1))));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
    
    private static String dynamicRawFormat(String string, Map<String, String> tokens) {
        if (tokens.isEmpty())
            return string;
        String patternString = "(" + String.join("|", tokens.keySet().stream().map(x -> unicodify(x)).collect(Collectors.toList())) + ")";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(string);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(tokens.get(matcher.group(1))));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
    
    private static HashMap<String, Integer> stringPool = new HashMap<>();
    private static int currentLength = 0;
    
    private static String getStringPooledString(String value) {
        if (!stringPool.containsKey(value)) {
            stringPool.put(value, currentLength);
            currentLength += value.getBytes(StandardCharsets.UTF_8).length + 1;
        }
        return "((char *)(string_pool + " + stringPool.get(value) + "LL))";
    }
    
    private static String unicodify(String string) {
        StringBuilder result = new StringBuilder();
        for (char c : string.toCharArray()) {
            result.append("\\u").append(String.format("%04x", (int)c));
        }
        return result.toString();
    }
    
    private static String dynamicStringPoolFormat(String key, Map<String, String> tokens) {
        String value = CPP_SNIPPETS.getProperty(key);
        if (value == null)
            throw new RuntimeException(key + " not found");
        String[] stringVars = CPP_SNIPPETS.getProperty(key + "_S_VARS") == null || CPP_SNIPPETS.getProperty(key + "_S_VARS").equals("") ? new String[0] : CPP_SNIPPETS.getProperty(key + "_S_VARS").split(",");
        HashMap<String, String> vars = new HashMap<>();
        for (String var : stringVars) {
            if (var.startsWith("#")) {
                vars.put(var, CPP_SNIPPETS.getProperty(key + "_S_CONST_" + var.substring(1)));
            } else if (var.startsWith("$")) {
                vars.put(var, tokens.get(var.substring(1)));
            } else {
                throw new RuntimeException("Unknown format modifier: " + var);
            }
        }
        vars.entrySet().stream().filter((var) -> (var.getValue() == null)).forEachOrdered((var) -> {
            throw new RuntimeException(key + " - " + var.getKey() + " is null");
        });
        HashMap<String, String> replaceTokens = new HashMap<>();
        vars.entrySet().forEach((var) -> {
            replaceTokens.put(var.getKey(), getStringPooledString(var.getValue()));
        });
        tokens.entrySet().forEach((var) -> {
            if (!replaceTokens.containsKey("$" + var.getKey()))
                replaceTokens.put("$" + var.getKey(), var.getValue());
        });
        return dynamicRawFormat(value, replaceTokens);
    }
    
    private static String visitMethod(ClassNode classNode, MethodNode methodNode, int index) {
        if (((methodNode.access & Opcodes.ACC_ABSTRACT) > 0) || ((methodNode.access & Opcodes.ACC_NATIVE) > 0))
            return "";
        if (methodNode.name.equals("<init>"))
            return "";
        StringBuilder outputSb = new StringBuilder("// ");
        outputSb.append(methodNode.name).append(methodNode.desc).append("\n");
        String methodName = "";
        String javaMethodName = methodNode.name;
        switch (methodNode.name) {
            case "<init>":
                classNode.methods.add(new MethodNode(Opcodes.ASM7, Opcodes.ACC_NATIVE | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "native_special_init" + index, methodNode.desc, methodNode.signature, new String[0]));
                javaMethodName = "native_special_init" + index;
                methodName += "native_special_init";
                break;
            case "<clinit>":
                classNode.methods.add(new MethodNode(Opcodes.ASM7, Opcodes.ACC_NATIVE | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC, "native_special_clinit" + index, methodNode.desc, methodNode.signature, new String[0]));
                javaMethodName = "native_special_clinit" + index;
                methodName += "native_special_clinit";
                break;
            default:
                methodNode.access |= Opcodes.ACC_NATIVE;
                methodName += "native_" + methodNode.name;
                break;
        }
        methodName += index;
        methodName = "__ngen_" + methodName.replace("/", "_");
        methodName = escapeCppNameString(methodName);
        nativeMethodsSb
                .append("            { (char *)")
                .append(getStringPooledString(javaMethodName))
                .append(", (char *)")
                .append(getStringPooledString(methodNode.desc))
                .append(", (void *)&")
                .append(methodName)
                .append(" },\n");
        int returnTypeSort = Type.getReturnType(methodNode.desc).getSort();
        outputSb
                .append(CPP_TYPES[returnTypeSort])
                .append(" ")
                .append("JNICALL")
                .append(" ")
                .append(methodName)
                .append("(")
                .append("JNIEnv *env")
                .append(", ")
                .append(((methodNode.access & Opcodes.ACC_STATIC) > 0) ? "jclass clazz" : "jobject obj");
        Type[] args = Type.getArgumentTypes(methodNode.desc);
        if (args.length > 0)
            outputSb.append(", ");
        for (int i = 0; i < args.length; i++)
            outputSb.append(CPP_TYPES[args[i].getSort()]).append(" ").append("arg").append(i).append(i == args.length - 1 ? "" : ", ");
        outputSb.append(") {").append("\n");
        outputSb.append("    ").append("utils::jvm_stack<").append(Math.max(1, methodNode.maxStack)).append("> cstack;").append("\n");
        outputSb.append("    ").append("utils::local_vars<").append(Math.max(1, methodNode.maxLocals)).append("> clocals;").append("\n").append("\n");
        int localIndex = 0;
        if (((methodNode.access & Opcodes.ACC_STATIC) == 0)) {
            outputSb.append("    ").append(dynamicStringPoolFormat("LOCAL_LOAD_ARG_" + 9,
                    createMap(
                            "index", localIndex, 
                            "arg", "obj"
                    ))).append("\n");
            localIndex++;
        }   
        for (int i = 0; i < args.length; i++) {
            outputSb.append("    ").append(dynamicStringPoolFormat("LOCAL_LOAD_ARG_" + args[i].getSort(),
                    createMap(
                            "index", localIndex, 
                            "arg", "arg" + i
                    ))).append("\n");
            localIndex += args[i].getSize();
        }
        outputSb.append("\n");
        Set<TryCatchBlockNode> currentTryCatches = new HashSet<>();
        int currentLine = -1;
        int invokeSpecialId = -1;
        for (int insnIndex = 0; insnIndex < methodNode.instructions.size(); insnIndex++) {
            if (methodNode.name.equals("<init>") && invokeSpecialId < 0) {
                if (methodNode.instructions.get(insnIndex).getOpcode() == Opcodes.INVOKESPECIAL) {
                    invokeSpecialId = insnIndex;
                }
                continue;
            }
            AbstractInsnNode insnNode = methodNode.instructions.get(insnIndex);
            switch (insnNode.getType()) {
                case AbstractInsnNode.LABEL:
                    outputSb.append(((LabelNode)insnNode).getLabel()).append(": ;").append("\n");
                    methodNode.tryCatchBlocks.stream().filter((node) -> (node.start.equals(insnNode))).forEachOrdered(currentTryCatches::add);
                    methodNode.tryCatchBlocks.stream().filter((node) -> (node.end.equals(insnNode))).forEachOrdered(currentTryCatches::remove);
                    break;
                case AbstractInsnNode.LINE:
                    outputSb.append("    ").append("// Line ").append(((LineNumberNode)insnNode).line).append(":").append("\n");
                    currentLine = ((LineNumberNode)insnNode).line;
                    break;
                case AbstractInsnNode.FRAME:
                    break;
                default:
                    StringBuilder tryCatch = new StringBuilder("\n");
                    tryCatch.append("    ").append(dynamicStringPoolFormat("TRYCATCH_START", 
                            createMap(
                                    "rettype", CPP_TYPES[returnTypeSort], 
                                    "handle", String.valueOf(currentTryCatches.size() > 0)
                            ))).append("\n");
                    for (TryCatchBlockNode tryCatchBlock : currentTryCatches) {
                        if (tryCatchBlock.type == null) {
                            tryCatch.append("    ").append(dynamicStringPoolFormat("TRYCATCH_ANY_L", createMap(
                                    "rettype", CPP_TYPES[returnTypeSort],
                                    "handler_block", tryCatchBlock.handler.getLabel().toString()
                            ))).append("\n");
                            break;
                        } else {
                            tryCatch.append("    ").append(dynamicStringPoolFormat("TRYCATCH_CHECK", createMap(
                                    "rettype", CPP_TYPES[returnTypeSort],
                                    "exception_class", tryCatchBlock.type,
                                    "handler_block", tryCatchBlock.handler.getLabel().toString()
                            ))).append("\n");
                            break;
                        }
                    }
                    tryCatch.append("    ").append(dynamicStringPoolFormat("TRYCATCH_END", createMap("rettype", CPP_TYPES[returnTypeSort])));
                    outputSb.append("    ");
                    String insnName = INSTRUCTIONS.getOrDefault(insnNode.getOpcode(), "NOTFOUND");
                    HashMap<String, String> props = new HashMap<>();
                    props.put("line", String.valueOf(currentLine));
                    props.put("trycatchhandler", tryCatch.toString());
                    props.put("rettype", CPP_TYPES[returnTypeSort]);
                    if (insnNode instanceof FieldInsnNode) {
                        insnName += "_" + Type.getType(((FieldInsnNode) insnNode).desc).getSort();
                        props.put("desc", ((FieldInsnNode) insnNode).desc);
                        props.put("name", ((FieldInsnNode) insnNode).name);
                        props.put("class", ((FieldInsnNode) insnNode).owner);
                    }
                    if (insnNode instanceof IincInsnNode) {
                        props.put("incr", String.valueOf(((IincInsnNode) insnNode).incr));
                        props.put("var", String.valueOf(((IincInsnNode) insnNode).var));
                    }
                    if (insnNode instanceof IntInsnNode) {
                        props.put("operand", String.valueOf(((IntInsnNode) insnNode).operand));
                        if (insnNode.getOpcode() == Opcodes.NEWARRAY) {
                            insnName += "_" + ((IntInsnNode) insnNode).operand;
                        }
                    }
                    if (insnNode instanceof InvokeDynamicInsnNode) {
                        String indyMethodName = "invokedynamic$" + methodNode.name + "$" + invokeDynamics.size();
                        invokeDynamics.put(indyMethodName, (InvokeDynamicInsnNode) insnNode);
                        Type returnType = Type.getReturnType(((InvokeDynamicInsnNode) insnNode).desc);
                        Type[] argTypes = Type.getArgumentTypes(((InvokeDynamicInsnNode) insnNode).desc);
                        insnName = "INVOKESTATIC_" + returnType.getSort();
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
                        for (int i = 0; i < argOffsets.size(); i++)
                            argsBuilder.append(", ").append(dynamicStringPoolFormat("INVOKE_ARG_" + argSorts.get(i), createMap("index", String.valueOf(argOffsets.get(i)))));
                        outputSb.append(dynamicStringPoolFormat("INVOKE_POPCNT", createMap("count", String.valueOf(-stackOffset - 1)))).append(" ");
                        props.put("class", classNode.name);
                        props.put("name", indyMethodName);
                        props.put("desc", ((InvokeDynamicInsnNode) insnNode).desc);
                        props.put("args", argsBuilder.toString());
                    }
                    if (insnNode instanceof JumpInsnNode) {
                        props.put("label", String.valueOf(((JumpInsnNode) insnNode).label.getLabel()));
                    }
                    if (insnNode instanceof LdcInsnNode) {
                        Object cst = ((LdcInsnNode) insnNode).cst;
                        props.put("cst", String.valueOf(((LdcInsnNode) insnNode).cst));
                        if (cst instanceof java.lang.String) {
                            insnName += "_STRING";
                            props.put("cst", String.valueOf(((LdcInsnNode) insnNode).cst));
                        } else if (cst instanceof java.lang.Integer) {
                            insnName += "_INT";
                        } else if (cst instanceof java.lang.Long) {
                            insnName += "_LONG";
                        } else if (cst instanceof java.lang.Float) {
                            insnName += "_FLOAT";
                            float cstVal = (float) cst;
                            if (cst.toString().equals("NaN"))
                                props.put("cst", "NAN");
                            else if (cstVal == Float.POSITIVE_INFINITY)
                                props.put("cst", "HUGE_VALF");
                            else if (cstVal == Float.NEGATIVE_INFINITY)
                                props.put("cst", "-HUGE_VALF");
                        } else if (cst instanceof java.lang.Double) {
                            insnName += "_DOUBLE";
                            double cstVal = (double) cst;
                            if (cst.toString().equals("NaN"))
                                props.put("cst", "NAN");
                            else if (cstVal == Double.POSITIVE_INFINITY)
                                props.put("cst", "HUGE_VAL");
                            else if (cstVal == Double.NEGATIVE_INFINITY)
                                props.put("cst", "-HUGE_VAL");
                        } else if (cst instanceof org.objectweb.asm.Type) {
                            insnName += "_CLASS";
                            props.put("cst", String.valueOf(((LdcInsnNode) insnNode).cst));
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }
                    if (insnNode instanceof LookupSwitchInsnNode) {
                        outputSb.append(dynamicStringPoolFormat("LOOKUPSWITCH_START", createMap())).append("\n");
                        for (int switchIndex = 0; switchIndex < ((LookupSwitchInsnNode) insnNode).labels.size(); switchIndex++)
                            outputSb.append("    ").append("    ").append(dynamicStringPoolFormat("LOOKUPSWITCH_PART", createMap(
                                    "key", String.valueOf(((LookupSwitchInsnNode) insnNode).keys.get(switchIndex)),
                                    "label", String.valueOf(((LookupSwitchInsnNode) insnNode).labels.get(switchIndex).getLabel())
                            ))).append("\n");
                        outputSb.append("    ").append("    ").append(dynamicStringPoolFormat("LOOKUPSWITCH_DEFAULT", createMap(
                                "label", String.valueOf(((LookupSwitchInsnNode) insnNode).dflt.getLabel())
                        ))).append("\n");
                        outputSb.append("    ").append(dynamicStringPoolFormat("LOOKUPSWITCH_END", createMap())).append("\n");
                        continue;
                    }
                    if (insnNode instanceof MethodInsnNode) {
                        Type returnType = Type.getReturnType(((MethodInsnNode) insnNode).desc);
                        Type[] argTypes = Type.getArgumentTypes(((MethodInsnNode) insnNode).desc);
                        insnName += "_" + returnType.getSort();
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
                        if (insnNode.getOpcode() == Opcodes.INVOKEINTERFACE || insnNode.getOpcode() == Opcodes.INVOKESPECIAL || insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            for (int i = 0; i < argOffsets.size(); i++)
                                argsBuilder.append(", ").append(dynamicStringPoolFormat("INVOKE_ARG_" + argSorts.get(i), createMap("index", String.valueOf(argOffsets.get(i) - 1))));
                            outputSb.append(dynamicStringPoolFormat("INVOKE_POPCNT", createMap("count", String.valueOf(-stackOffset)))).append(" ");
                            props.put("class", ((MethodInsnNode) insnNode).owner);
                            props.put("object_offset", "-1");
                            props.put("name", ((MethodInsnNode) insnNode).name);
                            props.put("desc", ((MethodInsnNode) insnNode).desc);
                            props.put("args", argsBuilder.toString());
                        } else {
                            for (int i = 0; i < argOffsets.size(); i++)
                                argsBuilder.append(", ").append(dynamicStringPoolFormat("INVOKE_ARG_" + argSorts.get(i), createMap("index", String.valueOf(argOffsets.get(i)))));
                            outputSb.append(dynamicStringPoolFormat("INVOKE_POPCNT", createMap("count", String.valueOf(-stackOffset - 1)))).append(" ");
                            props.put("class", ((MethodInsnNode) insnNode).owner);
                            props.put("name", ((MethodInsnNode) insnNode).name);
                            props.put("desc", ((MethodInsnNode) insnNode).desc);
                            props.put("args", argsBuilder.toString());
                        }
                    }
                    if (insnNode instanceof MultiANewArrayInsnNode) {
                        props.put("count", String.valueOf(((MultiANewArrayInsnNode) insnNode).dims));
                        props.put("desc", ((MultiANewArrayInsnNode) insnNode).desc);
                    }
                    if (insnNode instanceof TableSwitchInsnNode) {
                        outputSb.append(dynamicStringPoolFormat("TABLESWITCH_START", createMap())).append("\n");
                        for (int switchIndex = 0; switchIndex < ((TableSwitchInsnNode) insnNode).labels.size(); switchIndex++)
                            outputSb.append("    ").append("    ").append(dynamicStringPoolFormat("TABLESWITCH_PART", createMap(
                                    "index", String.valueOf(((TableSwitchInsnNode) insnNode).min + switchIndex),
                                    "label", String.valueOf(((TableSwitchInsnNode) insnNode).labels.get(switchIndex).getLabel())
                            ))).append("\n");
                        outputSb.append("    ").append("    ").append(dynamicStringPoolFormat("TABLESWITCH_DEFAULT", createMap(
                                "label", String.valueOf(((TableSwitchInsnNode) insnNode).dflt.getLabel())
                        ))).append("\n");
                        outputSb.append("    ").append(dynamicStringPoolFormat("TABLESWITCH_END", createMap())).append("\n");
                        continue;
                    }
                    if (insnNode instanceof TypeInsnNode) {
                        props.put("desc", ((TypeInsnNode) insnNode).desc);
                    }
                    if (insnNode instanceof VarInsnNode) {
                        props.put("var", String.valueOf(((VarInsnNode) insnNode).var));
                    }
//                    Map<String, String> cstrProps = new HashMap<>();
//                    props.entrySet().forEach((entry) -> { 
//                        cstrProps.put(entry.getKey(), getCppString(entry.getValue()));
//                    });
//                    cstrProps.entrySet().forEach((entry) -> {
//                        props.put("_cstr_" + entry.getKey(), entry.getValue());
//                    });
                    String cppCode = CPP_SNIPPETS.getProperty(insnName);
                    if (cppCode == null) {
                        outputSb.append("// ").append("insn not found: ").append(insnName).append(" ").append(insnNode).append("\n");
                    } else {
                        cppCode = dynamicStringPoolFormat(insnName, props);
                        outputSb.append(cppCode);
                    }
                    outputSb.append("\n");
                    break;
            }
        }
        outputSb.append("    return (").append(CPP_TYPES[returnTypeSort]).append(") 0;\n");
        outputSb.append("}\n\n");
        
        methodNode.localVariables.clear();
        methodNode.tryCatchBlocks.clear();
        
        switch (methodNode.name) {
            case "<init>":
                InsnList list = new InsnList();
                for (int i = 0; i <= invokeSpecialId; i++) 
                    list.add(methodNode.instructions.get(i));
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                int localVarsPosition = 1;
                for (Type arg : args) {
                    list.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
                    localVarsPosition += arg.getSize();
                }
                list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classNode.name, "native_special_init" + index, methodNode.desc));
                list.add(new InsnNode(Opcodes.RETURN));
                methodNode.instructions = list;
                break;
            case "<clinit>":
                methodNode.instructions.clear();
                methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, "native_special_clinit" + index, methodNode.desc));
                methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
                break;
            default:
                methodNode.instructions.clear();
                break;
        }
        
        return outputSb.toString();
    }
    
    private static void processIndy(ClassNode classNode, String methodName, InvokeDynamicInsnNode indy) {
        MethodNode indyWrapper = new MethodNode(Opcodes.ASM7, Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC, methodName, indy.desc, null, new String[0]);
        int localVarsPosition = 0;
        for (Type arg : Type.getArgumentTypes(indy.desc)) {
            indyWrapper.instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), localVarsPosition));
            localVarsPosition += arg.getSize();
        }
        indyWrapper.instructions.add(new InvokeDynamicInsnNode(indy.name, indy.desc, indy.bsm, indy.bsmArgs));
        indyWrapper.instructions.add(new InsnNode(Opcodes.ARETURN));
        classNode.methods.add(indyWrapper);
    }

    private static String writeStreamToString(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transfer(stream,baos);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
    
    private static void writeStreamToFile(InputStream stream, Path path) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            while ((bytesRead = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
    
    private static final String VERSION = "1.3b";
    
    static String stripExtension(String str) {
        if (str == null) 
            return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) 
            return str;
        return str.substring(0, pos);
    }
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.IllegalAccessException
     */
    public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException {    
        System.out.println("native-obfuscator v" + VERSION);
        if (args.length < 2) {
            System.err.println("java -jar native-obfuscator.jar <jar file> <output directory>");
            return;
        }
        for (Field f : Opcodes.class.getFields())
            INSTRUCTIONS.put((int) f.get(null), f.getName());
        CPP_SNIPPETS.load(NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/cppsnippets.properties"));
        final File jar = Paths.get(args[0]).normalize().toAbsolutePath().toFile();
        final Path outputDir = Paths.get(args[1]).normalize().toAbsolutePath();
        Files.createDirectories(outputDir);
        Files.createDirectories(outputDir.resolve("cpp"));
        Files.createDirectories(outputDir.resolve("cpp").resolve("output"));
        try (InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/native_jvm.cpp")) {
            writeStreamToFile(in, outputDir.resolve("cpp").resolve("native_jvm.cpp"));
        }
        try (InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/native_jvm.hpp")) {
            writeStreamToFile(in, outputDir.resolve("cpp").resolve("native_jvm.hpp"));
        }
        try (InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/native_jvm_output.hpp")) {
            writeStreamToFile(in, outputDir.resolve("cpp").resolve("native_jvm_output.hpp"));
        }
        try (InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/string_pool.hpp")) {
            writeStreamToFile(in, outputDir.resolve("cpp").resolve("string_pool.hpp"));
        }
        StringBuilder outputHeaderSb = new StringBuilder();
        StringBuilder outputHeaderIncludesSb = new StringBuilder();
        List<String> cmakeClassFiles = new ArrayList<>();
        List<String> cmakeMainFiles = new ArrayList<>();
        cmakeMainFiles.add("native_jvm.hpp");
        cmakeMainFiles.add("native_jvm.cpp");
        cmakeMainFiles.add("native_jvm_output.hpp");
        cmakeMainFiles.add("native_jvm_output.cpp");
        cmakeMainFiles.add("string_pool.hpp");
        cmakeMainFiles.add("string_pool.cpp");
        try (final JarFile f = new JarFile(jar); final ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputDir.resolve(jar.getName()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            System.out.println("Processing " + jar + "...");
            List<JarFile> libs = new ArrayList<>();
            libs.add(f);
            f.stream().forEach(e -> {
                try {
                    if (!e.getName().endsWith(".class")) {
                        writeEntry(f, out, e);
                        return;
                    }
                    // Ignore entries with invalid magic
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream in = f.getInputStream(e)) {
                        transfer(in,baos);
                    }
                    byte[] src = baos.toByteArray();
                    if (byteArrayToInt(Arrays.copyOfRange(src, 0, 4)) != 0xCAFEBABE) {
                        writeEntry(f, out, e.getName(), src);
                        return;
                    }

                    nativeMethodsSb = new StringBuilder();
                    invokeDynamics = new HashMap<>();
                    ClassReader classReader = new ClassReader(src);
                    ClassNode classNode = new ClassNode(Opcodes.ASM7);
                    classReader.accept(classNode, 0);
                    if ((classNode.access & Opcodes.ACC_INTERFACE) > 0 || classNode.methods.stream().filter(x -> (x.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0 && !x.name.equals("<init>")).count() == 0) {
                        System.out.println("Skipping " + classNode.name);
                        writeEntry(f, out, e.getName(), src);
                        return;
                    }
                    System.out.println("Processing " + classNode.name);
                    try (BufferedWriter outputCppFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDir.resolve("cpp").resolve("output").resolve(escapeCppNameString(classNode.name.replace('/', '_')).concat(".cpp")).toFile()), StandardCharsets.UTF_8));
                            BufferedWriter outputHppFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDir.resolve("cpp").resolve("output").resolve(escapeCppNameString(classNode.name.replace('/', '_')).concat(".hpp")).toFile()), StandardCharsets.UTF_8))) {
                        outputCppFile.append("#include \"../native_jvm.hpp\"\n");
                        outputHppFile.append("#include \"../native_jvm.hpp\"\n");
                        outputCppFile.append("#include \"../string_pool.hpp\"\n");
                        outputCppFile.append("#include \"").append(escapeCppNameString(classNode.name.replace('/', '_')).concat(".hpp")).append("\"\n");
                        cmakeClassFiles.add("output/" + escapeCppNameString(classNode.name.replace('/', '_')) + ".hpp");
                        cmakeClassFiles.add("output/" + escapeCppNameString(classNode.name.replace('/', '_')) + ".cpp");
                        outputHeaderIncludesSb.append("#include \"output/").append(escapeCppNameString(classNode.name.replace('/', '_')).concat(".hpp")).append("\"\n");
                        outputCppFile.append("\n");
                        outputCppFile.append("// ").append(classNode.name).append("\n");
                        outputCppFile.append("namespace native_jvm::classes::__ngen_")
                            .append(escapeCppNameString(classNode.name.replace("/", "_")))
                            .append(" {\n\n");
                        outputCppFile.append("    char *string_pool;\n\n");
                        outputHppFile.append("\n");
                        outputHppFile.append("#ifndef ").append(escapeCppNameString(classNode.name.replace('/', '_')).concat("_hpp").toUpperCase()).append("_GUARD\n");
                        outputHppFile.append("\n");
                        outputHppFile.append("#define ").append(escapeCppNameString(classNode.name.replace('/', '_')).concat("_hpp").toUpperCase()).append("_GUARD\n");
                        outputHppFile.append("\n");
                        outputHppFile.append("// ").append(classNode.name).append("\n");
                        outputHppFile.append("namespace native_jvm::classes::__ngen_")
                            .append(escapeCppNameString(classNode.name.replace("/", "_")))
                            .append(" {\n\n");
                        outputCppFile.append("    ");
                        for (int i = 0; i < classNode.methods.size(); i++)
                            outputCppFile.append(visitMethod(classNode, classNode.methods.get(i), i).replace("\n", "\n    "));
                        invokeDynamics.forEach((key, value) -> processIndy(classNode, key, value));
                        ClassWriter classWriter = new SafeClassWriter(new ClassMetadataReader(libs), Opcodes.ASM7 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                        classNode.accept(classWriter);
                        out.putNextEntry(new ZipEntry(e.getName()));
                        out.write(classWriter.toByteArray());
                        outputCppFile.append("\n");
                        outputCppFile.append("    void __ngen_register_methods(JNIEnv *env) {\n");
                        outputHppFile.append("    void __ngen_register_methods(JNIEnv *env);\n");
                        outputCppFile.append("        string_pool = string_pool::get_pool();\n\n");
                        outputCppFile.append("        JNINativeMethod __ngen_methods[] = {\n");
                        outputCppFile.append(nativeMethodsSb);
                        outputCppFile.append("        };\n\n");
                        outputCppFile.append("        jclass clazz = utils::find_class_wo_static(env, ").append(getStringPooledString(classNode.name.replace("/", "."))).append(");\n");
                        outputCppFile.append("        if (clazz) env->RegisterNatives(clazz, __ngen_methods, sizeof(__ngen_methods) / sizeof(__ngen_methods[0]));\n");
                        outputCppFile.append("        if (env->ExceptionCheck()) { env->ExceptionClear(); fprintf(stderr, \"Exception occured while registering native_jvm for %s\\n\", ").append(getStringPooledString(classNode.name.replace("/", "."))).append("); fflush(stderr); }\n");
                        outputCppFile.append("    }\n");
                        outputCppFile.append("}");
                        outputHppFile.append("}\n\n#endif");
                        outputHeaderSb.append("        native_jvm::classes::__ngen_").append(escapeCppNameString(classNode.name.replace("/", "_"))).append("::__ngen_register_methods(env);\n");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace(System.err);
                }
            });
            System.out.println("Jar file ready!");
            Manifest mf = f.getManifest();
            String mainClass = (String) mf.getMainAttributes().get(Name.MAIN_CLASS);
            if (mainClass != null) {
                System.out.println("Creating bootstrapped jar...");
                try (ZipOutputStream bsJar = new ZipOutputStream(Files.newOutputStream(outputDir.resolve(stripExtension(jar.getName()) + "-bootstrap.jar"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                    Manifest newMf = new Manifest();
                    newMf.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0");
                    newMf.getMainAttributes().put(Name.MAIN_CLASS, "NativeBootstrap");
                    newMf.getMainAttributes().put(Name.CLASS_PATH, jar.getName());
                    bsJar.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
                    newMf.write(bsJar);
                    ClassNode bsc = new ClassNode(Opcodes.ASM7);
                    bsc.name = "NativeBootstrap";
                    bsc.version = 52;
                    bsc.superName = "java/lang/Object";
                    bsc.access = Opcodes.ACC_PUBLIC;
                    MethodNode mainMethod = new MethodNode(Opcodes.ASM7, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, new String[0]);
                    mainMethod.instructions.add(new LdcInsnNode("native_jvm"));
                    mainMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "loadLibrary", "(Ljava/lang/String;)V"));
                    mainMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    mainMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, mainClass.replace(".", "/"), "main", "([Ljava/lang/String;)V"));
                    mainMethod.instructions.add(new InsnNode(Opcodes.RETURN));
                    bsc.methods.add(mainMethod);
                    ClassWriter classWriter = new SafeClassWriter(new ClassMetadataReader(libs), Opcodes.ASM7 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    bsc.accept(classWriter);
                    bsJar.putNextEntry(new JarEntry("NativeBootstrap.class"));
                    bsJar.write(classWriter.toByteArray());
                }
                System.out.println("Created!");
            } else {
                System.out.println("Main-Class not found - no bootstrapped jar");
            }
        }

        TreeMap<Integer, String> stringPoolSorted = new TreeMap<>();
        stringPool.entrySet().forEach((string) -> { 
            stringPoolSorted.put(string.getValue(), string.getKey());
        });
        List<Byte> stringPoolResult = new ArrayList<>();
        stringPoolSorted.entrySet().forEach((string) -> {
            for (byte b : string.getValue().getBytes(StandardCharsets.UTF_8))
                stringPoolResult.add(b);
            stringPoolResult.add((byte)0);
        });
        try (InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/string_pool.cpp")) {
            StringBuilder spValue = new StringBuilder("{ ");
            for (int i = 0; i < stringPoolResult.size(); i++)
                spValue.append(stringPoolResult.get(i)).append(i == stringPoolResult.size() - 1 ? "" : ", ");
            spValue.append(" }");
            Files.write(
                outputDir.resolve("cpp").resolve("string_pool.cpp"),
                dynamicFormat(
                    writeStreamToString(in),
                    createMap(
                        "size", stringPoolResult.size() + "LL",
                        "value", spValue.toString()
                    )).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            );
        }
        
        try (InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/native_jvm_output.cpp")) {
            Files.write(
                outputDir.resolve("cpp").resolve("native_jvm_output.cpp"),
                dynamicFormat(
                    writeStreamToString(in),
                    createMap(
                        "register_code", outputHeaderSb,
                        "includes", outputHeaderIncludesSb
                    )).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            );
        }

        try (InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/CMakeLists.txt")) {
            Files.write(
                outputDir.resolve("cpp").resolve("CMakeLists.txt"),
                dynamicFormat(
                    writeStreamToString(in),
                    createMap(
                        "classfiles", String.join(" ", cmakeClassFiles),
                        "mainfiles", String.join(" ", cmakeMainFiles)
                    )).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            );
        }
    }

    private static void writeEntry(JarFile f, ZipOutputStream out, JarEntry e) throws IOException {
        out.putNextEntry(new JarEntry(e.getName()));
        try (InputStream in = f.getInputStream(e)) {
            transfer(in, out);
        }
    }

    private static void writeEntry(JarFile f, ZipOutputStream out, String entryName, byte[] data) throws IOException {
        out.putNextEntry(new JarEntry(entryName));
        out.write(data, 0, data.length);
    }

    private static void transfer(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        for (int r = in.read(buffer, 0, 4096); r != -1; r = in.read(buffer, 0, 4096)) {
            out.write(buffer, 0, r);
        }
    }

    private static int byteArrayToInt(byte[] b) {
        if (b.length == 4) {
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8
                | (b[3] & 0xff);
        } else if (b.length == 2) {
            return (b[0] & 0xff) << 8 | (b[1] & 0xff);
        }

        return 0;
    }
}
