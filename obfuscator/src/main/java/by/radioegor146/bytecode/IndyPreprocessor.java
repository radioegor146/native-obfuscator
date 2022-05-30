package by.radioegor146.bytecode;

import by.radioegor146.Platform;
import by.radioegor146.Util;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class IndyPreprocessor implements IPreprocessor {

    private static AbstractInsnNode getTypeLoadInsnNode(Type type) {
        switch (type.getSort()) {
            case Type.ARRAY:
            case Type.OBJECT:
                return new LdcInsnNode(type);
            case Type.BOOLEAN:
                return new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
            case Type.BYTE:
                return new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
            case Type.CHAR:
                return new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
            case Type.DOUBLE:
                return new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
            case Type.FLOAT:
                return new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
            case Type.INT:
                return new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            case Type.LONG:
                return new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
            case Type.SHORT:
                return new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
            default:
                throw new RuntimeException(String.format("Unsupported TypeLoad type: %s", type));
        }
    }

    private InsnList generateMethodTypeLdcInsn(Type type) {
        if (type.getSort() != Type.METHOD) {
            throw new RuntimeException(String.format("Not a MT: %s", type));
        }
        InsnList insntructions = new InsnList();
        insntructions.add(new LdcInsnNode(type.getDescriptor())); // 5
        insntructions.add(PreprocessorUtils.CLASSLOADER_LOCAL.get()); // 6
        insntructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType",
                "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")); // 5
        return insntructions;
    }

    private InsnList generateMethodHandleLdcInsn(Handle handle) {
        InsnList instructions = new InsnList();
        instructions.add(PreprocessorUtils.LOOKUP_LOCAL.get()); // 5
        instructions.add(new LdcInsnNode(Type.getObjectType(handle.getOwner()))); // 6
        switch (handle.getTag()) {
            case Opcodes.H_GETFIELD:
            case Opcodes.H_GETSTATIC:
            case Opcodes.H_PUTFIELD:
            case Opcodes.H_PUTSTATIC:
                instructions.add(new LdcInsnNode(handle.getName())); // 7
                instructions.add(getTypeLoadInsnNode(Type.getType(handle.getDesc()))); // 8
                String methodName = "";
                switch (handle.getTag()) {
                    case Opcodes.H_GETFIELD:
                        methodName = "findGetter";
                        break;
                    case Opcodes.H_GETSTATIC:
                        methodName = "findStaticGetter";
                        break;
                    case Opcodes.H_PUTFIELD:
                        methodName = "findSetter";
                        break;
                    case Opcodes.H_PUTSTATIC:
                        methodName = "findStaticSetter";
                        break;
                }
                instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "java/lang/invoke/MethodHandles$Lookup", methodName,
                        "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;")); // 5
                break;
            case Opcodes.H_INVOKEVIRTUAL:
            case Opcodes.H_INVOKEINTERFACE:
                instructions.add(new LdcInsnNode(handle.getName())); // 7
                instructions.add(new LdcInsnNode(handle.getDesc())); // 8
                instructions.add(PreprocessorUtils.CLASSLOADER_LOCAL.get()); // 9
                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType",
                        "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")); // 8
                instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, // 5
                        "java/lang/invoke/MethodHandles$Lookup", "findVirtual",
                        "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"));
                break;
            case Opcodes.H_INVOKESTATIC:
                instructions.add(new LdcInsnNode(handle.getName())); // 7
                instructions.add(new LdcInsnNode(handle.getDesc())); // 8
                instructions.add(PreprocessorUtils.CLASSLOADER_LOCAL.get()); // 9
                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType",
                        "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")); // 8
                instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, // 5
                        "java/lang/invoke/MethodHandles$Lookup", "findStatic",
                        "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"));
                break;
            case Opcodes.H_INVOKESPECIAL:
                instructions.add(new LdcInsnNode(handle.getName())); // 7
                instructions.add(new LdcInsnNode(handle.getDesc())); // 8
                instructions.add(PreprocessorUtils.CLASSLOADER_LOCAL.get()); // 9
                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType",
                        "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")); // 8
                instructions.add(PreprocessorUtils.CLASS_LOCAL.get()); // 9
                instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, // 5
                        "java/lang/invoke/MethodHandles$Lookup", "findSpecial",
                        "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"));
                break;
            case Opcodes.H_NEWINVOKESPECIAL:
                instructions.add(new LdcInsnNode(handle.getDesc())); // 7
                instructions.add(PreprocessorUtils.CLASSLOADER_LOCAL.get()); // 8
                instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType",
                        "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;")); // 7
                instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, // 5
                        "java/lang/invoke/MethodHandles$Lookup", "findConstructor",
                        "(Ljava/lang/Class;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"));
                break;
        }
        return instructions;
    }

    private void processIndy(ClassNode classNode, MethodNode methodNode,
                             InvokeDynamicInsnNode invokeDynamicInsnNode, Platform platform) {
        LabelNode bootstrapStart = new LabelNode(new Label());
        LabelNode bootstrapEnd = new LabelNode(new Label());
        LabelNode bsmeStart = new LabelNode(new Label());
        LabelNode invokeStart = new LabelNode(new Label());

        InsnList bootstrapInstructions = new InsnList();
        bootstrapInstructions.add(bootstrapStart); // 0
        switch (platform) {
            case STD_JAVA: {
                Type[] bsmArguments = Type.getArgumentTypes(invokeDynamicInsnNode.bsm.getDesc());
                if (bsmArguments.length < 3 || !bsmArguments[0].getDescriptor().equals("Ljava/lang/invoke/MethodHandles$Lookup;") ||
                        !bsmArguments[1].getDescriptor().equals("Ljava/lang/String;") ||
                        !bsmArguments[2].getDescriptor().equals("Ljava/lang/invoke/MethodType;")) {
                    InsnList resultInstructions = new InsnList();
                    resultInstructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/BootstrapMethodError")); // 1
                    resultInstructions.add(new InsnNode(Opcodes.DUP)); // 2
                    resultInstructions.add(new LdcInsnNode("Wrong 3 first arguments in bsm")); // 3
                    resultInstructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/BootstrapMethodError",
                            "<init>", "(Ljava/lang/String;)V")); // 1
                    resultInstructions.add(new InsnNode(Opcodes.ATHROW)); // 0
                    methodNode.instructions.insert(invokeDynamicInsnNode, resultInstructions);
                    methodNode.instructions.remove(invokeDynamicInsnNode);
                    return;
                }


                Type[] arguments = Type.getArgumentTypes(invokeDynamicInsnNode.desc);
                bootstrapInstructions.add(new LdcInsnNode(arguments.length)); // 1
                bootstrapInstructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object")); // 1
                {
                    int index = arguments.length;
                    for (Type argument : Util.reverse(Arrays.stream(arguments)).collect(Collectors.toList())) {
                        index--;
                        if (argument.getSize() == 1) {
                            if (argument.getSort() != Type.ARRAY && argument.getSort() != Type.OBJECT) {
                                bootstrapInstructions.add(new InsnNode(Opcodes.SWAP)); // 2
                                bootstrapInstructions.add(getBoxingInsnNode(argument)); // 2
                                bootstrapInstructions.add(new InsnNode(Opcodes.SWAP)); // 2
                            }
                        } else if (argument.getSize() == 2) {
                            bootstrapInstructions.add(new InsnNode(Opcodes.DUP_X2)); // 3
                            bootstrapInstructions.add(new InsnNode(Opcodes.POP)); // 2
                            bootstrapInstructions.add(getBoxingInsnNode(argument)); // 2
                            bootstrapInstructions.add(new InsnNode(Opcodes.SWAP)); // 2
                        }
                        bootstrapInstructions.add(new InsnNode(Opcodes.DUP)); // 3
                        bootstrapInstructions.add(new InsnNode(Opcodes.DUP2_X1)); // 5
                        bootstrapInstructions.add(new InsnNode(Opcodes.POP2)); // 3
                        bootstrapInstructions.add(new LdcInsnNode(index)); // 4
                        bootstrapInstructions.add(new InsnNode(Opcodes.SWAP)); // 4
                        bootstrapInstructions.add(new InsnNode(Opcodes.AASTORE)); // 1
                    }
                }

                bootstrapInstructions.add(PreprocessorUtils.LOOKUP_LOCAL.get()); // 2
                bootstrapInstructions.add(new LdcInsnNode(invokeDynamicInsnNode.name)); // 3
                bootstrapInstructions.add(generateMethodTypeLdcInsn(Type.getMethodType(invokeDynamicInsnNode.desc)));

                for (Object bsmArgument : invokeDynamicInsnNode.bsmArgs) {
                    if (bsmArgument instanceof String) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 5
                    } else if (bsmArgument instanceof Type) {
                        if (((Type) bsmArgument).getSort() == Type.METHOD) {
                            bootstrapInstructions.add(generateMethodTypeLdcInsn((Type) bsmArgument));
                        } else {
                            bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 5
                        }
                    } else if (bsmArgument instanceof Integer) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 5
                    } else if (bsmArgument instanceof Long) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 6
                    } else if (bsmArgument instanceof Float) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 5
                    } else if (bsmArgument instanceof Double) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 6
                    } else if (bsmArgument instanceof Handle) {
                        bootstrapInstructions.add(generateMethodHandleLdcInsn((Handle) bsmArgument));
                    } else {
                        throw new RuntimeException("Wrong argument type: " + bsmArgument.getClass());
                    }
                }
                bootstrapInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, invokeDynamicInsnNode.bsm.getOwner(),
                        invokeDynamicInsnNode.bsm.getName(), invokeDynamicInsnNode.bsm.getDesc())); // 2
                bootstrapInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/invoke/CallSite")); // 2
                bootstrapInstructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/lang/invoke/CallSite",
                        "getTarget", "()Ljava/lang/invoke/MethodHandle;")); // 2
                bootstrapInstructions.add(new JumpInsnNode(Opcodes.GOTO, invokeStart)); // 2
            }
            break;
            case HOTSPOT: {
                bootstrapInstructions.add(new InsnNode(Opcodes.ICONST_1));
                bootstrapInstructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
                bootstrapInstructions.add(new InsnNode(Opcodes.DUP));
                bootstrapInstructions.add(new LdcInsnNode(Type.getObjectType(classNode.name)));
                bootstrapInstructions.add(new InsnNode(Opcodes.SWAP));
                bootstrapInstructions.add(generateMethodHandleLdcInsn(invokeDynamicInsnNode.bsm));
                bootstrapInstructions.add(new InsnNode(Opcodes.SWAP));
                bootstrapInstructions.add(new LdcInsnNode(invokeDynamicInsnNode.name));
                bootstrapInstructions.add(new InsnNode(Opcodes.SWAP));
                bootstrapInstructions.add(generateMethodTypeLdcInsn(Type.getMethodType(invokeDynamicInsnNode.desc)));
                bootstrapInstructions.add(new InsnNode(Opcodes.SWAP));
                bootstrapInstructions.add(new LdcInsnNode(invokeDynamicInsnNode.bsmArgs.length));
                bootstrapInstructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
                int index = 0;
                for (Object bsmArgument : invokeDynamicInsnNode.bsmArgs) {
                    bootstrapInstructions.add(new InsnNode(Opcodes.DUP));
                    bootstrapInstructions.add(new LdcInsnNode(index));
                    if (bsmArgument instanceof String) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 5
                    } else if (bsmArgument instanceof Type) {
                        if (((Type) bsmArgument).getSort() == Type.METHOD) {
                            bootstrapInstructions.add(generateMethodTypeLdcInsn((Type) bsmArgument));
                        } else {
                            bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 5
                        }
                    } else if (bsmArgument instanceof Integer) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 5
                        bootstrapInstructions.add(getBoxingInsnNode(Type.INT_TYPE));
                    } else if (bsmArgument instanceof Long) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 6
                        bootstrapInstructions.add(getBoxingInsnNode(Type.LONG_TYPE));
                    } else if (bsmArgument instanceof Float) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 5
                        bootstrapInstructions.add(getBoxingInsnNode(Type.FLOAT_TYPE));
                    } else if (bsmArgument instanceof Double) {
                        bootstrapInstructions.add(new LdcInsnNode(bsmArgument)); // 6
                        bootstrapInstructions.add(getBoxingInsnNode(Type.DOUBLE_TYPE));
                    } else if (bsmArgument instanceof Handle) {
                        bootstrapInstructions.add(generateMethodHandleLdcInsn((Handle) bsmArgument));
                    } else {
                        throw new RuntimeException("Wrong argument type: " + bsmArgument.getClass());
                    }
                    bootstrapInstructions.add(new InsnNode(Opcodes.AASTORE));
                    index++;
                }
                bootstrapInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Object"));
                bootstrapInstructions.add(new InsnNode(Opcodes.SWAP));
                bootstrapInstructions.add(PreprocessorUtils.LINK_CALL_SITE_METHOD.get());
                /* bootstrapInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodHandleNatives",
                        "linkCallSite", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;")); */
                bootstrapInstructions.add(new InsnNode(Opcodes.POP));
                bootstrapInstructions.add(new InsnNode(Opcodes.ICONST_0));
                bootstrapInstructions.add(new InsnNode(Opcodes.AALOAD)); // 1
                bootstrapInstructions.add(new InsnNode(Opcodes.DUP)); // 2
                bootstrapInstructions.add(new TypeInsnNode(Opcodes.INSTANCEOF, "java/lang/invoke/CallSite")); // 2
                LabelNode methodHandleReady = new LabelNode(new Label());
                bootstrapInstructions.add(new JumpInsnNode(Opcodes.IFEQ, methodHandleReady)); // 1
                bootstrapInstructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/lang/invoke/CallSite",
                        "getTarget", "()Ljava/lang/invoke/MethodHandle;")); // 1
                bootstrapInstructions.add(methodHandleReady); // 1
                bootstrapInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/invoke/MethodHandle"));
                bootstrapInstructions.add(new JumpInsnNode(Opcodes.GOTO, invokeStart)); // 2
            }
            break;
        }
        bootstrapInstructions.add(bootstrapEnd);

        InsnList invokeInstructions = new InsnList();
        invokeInstructions.add(invokeStart);
        switch (platform) {
            case HOTSPOT: {
                invokeInstructions.add(PreprocessorUtils.INVOKE_REVERSE.apply(invokeDynamicInsnNode.desc));
                Type returnType = Type.getReturnType(invokeDynamicInsnNode.desc);
                if (returnType.getSort() == Type.OBJECT) {
                    invokeInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, returnType.getInternalName())); // 1
                } else if (returnType.getSort() == Type.ARRAY) {
                    invokeInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, returnType.getDescriptor())); // 1
                }
                break;
            }
            case STD_JAVA: {
                invokeInstructions.add(new InsnNode(Opcodes.SWAP)); // 2
                invokeInstructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle",
                        "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;")); // 1
                Type returnType = Type.getReturnType(invokeDynamicInsnNode.desc);
                if (returnType.getSort() == Type.OBJECT) {
                    invokeInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, returnType.getInternalName())); // 1
                } else if (returnType.getSort() == Type.ARRAY) {
                    invokeInstructions.add(new TypeInsnNode(Opcodes.CHECKCAST, returnType.getDescriptor())); // 1
                } else {
                    invokeInstructions.add(getUnboxingTypeInsn(returnType));
                }
                break;
            }
        }

        InsnList bsmeInstructions = new InsnList();
        bsmeInstructions.add(bsmeStart); // 1
        bsmeInstructions.add(new InsnNode(Opcodes.DUP));
        bsmeInstructions.add(new TypeInsnNode(Opcodes.INSTANCEOF, "java/lang/BootstrapMethodError"));
        LabelNode throwLabel = new LabelNode(new Label());
        bsmeInstructions.add(new JumpInsnNode(Opcodes.IFNE, throwLabel));
        bsmeInstructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/BootstrapMethodError")); // 2
        bsmeInstructions.add(new InsnNode(Opcodes.DUP)); // 3
        bsmeInstructions.add(new InsnNode(Opcodes.DUP2_X1)); // 5
        bsmeInstructions.add(new InsnNode(Opcodes.POP2)); // 3
        bsmeInstructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/BootstrapMethodError",
                "<init>", "(Ljava/lang/Throwable;)V")); // 1
        bsmeInstructions.add(throwLabel);
        bsmeInstructions.add(new InsnNode(Opcodes.ATHROW)); // 0

        InsnList resultInstructions = new InsnList();
        resultInstructions.add(bootstrapInstructions);
        resultInstructions.add(bsmeInstructions);
        resultInstructions.add(invokeInstructions);

        methodNode.instructions.insert(invokeDynamicInsnNode, resultInstructions);
        methodNode.instructions.remove(invokeDynamicInsnNode);
        methodNode.tryCatchBlocks.add(0, new TryCatchBlockNode(bootstrapStart, bootstrapEnd, bsmeStart, "java/lang/Throwable"));
    }

    private AbstractInsnNode getBoxingInsnNode(Type argument) {
        switch (argument.getSort()) {
            case Type.BOOLEAN:
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            case Type.BYTE:
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
            case Type.CHAR:
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
            case Type.DOUBLE:
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            case Type.FLOAT:
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
            case Type.INT:
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            case Type.LONG:
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
            case Type.SHORT:
                return new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
            default:
                throw new RuntimeException(String.format("Failed to box %s", argument));
        }
    }

    private InsnList getUnboxingTypeInsn(Type argument) {
        InsnList result = new InsnList();
        switch (argument.getSort()) {
            case Type.BOOLEAN:
                result.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"));
                result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z"));
                break;
            case Type.BYTE:
                result.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Byte"));
                result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B"));
                break;
            case Type.CHAR:
                result.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Character"));
                result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C"));
                break;
            case Type.DOUBLE:
                result.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Double"));
                result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D"));
                break;
            case Type.FLOAT:
                result.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Float"));
                result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F"));
                break;
            case Type.INT:
                result.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Integer"));
                result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I"));
                break;
            case Type.LONG:
                result.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Long"));
                result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J"));
                break;
            case Type.SHORT:
                result.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Short"));
                result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S"));
                break;
            case Type.VOID:
                result.add(new InsnNode(Opcodes.POP));
                break;
            default:
                throw new RuntimeException(String.format("Failed to unbox %s", argument));
        }
        return result;
    }

    @Override
    public void process(ClassNode classNode, MethodNode methodNode, Platform platform) {
        for (int i = 0; i < methodNode.instructions.size(); i++) {
            AbstractInsnNode insnNode = methodNode.instructions.get(i);
            if (insnNode instanceof InvokeDynamicInsnNode) {
                processIndy(classNode, methodNode, (InvokeDynamicInsnNode) insnNode, platform);
            }
        }
    }
}
