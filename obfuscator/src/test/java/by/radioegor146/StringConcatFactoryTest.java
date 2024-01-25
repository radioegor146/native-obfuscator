package by.radioegor146;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.FileOutputStream;
import java.io.IOException;

public class StringConcatFactoryTest implements Opcodes {
    public static void main(String[] args) throws IOException {
        ClassNode classNode = new ClassNode();
        classNode.version = V17;
        classNode.access = ACC_PUBLIC | ACC_SUPER;
        classNode.superName = "java/lang/Object";
        classNode.name = "TestStringConcatFactory";
        MethodNode method = new MethodNode(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);


        method.instructions.add(new FieldInsnNode(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));

        method.instructions.add(new InsnNode(ICONST_1));
        method.instructions.add(new InsnNode(ICONST_2));
        method.instructions.add(new InsnNode(ICONST_3));

        Handle handle = new Handle(H_INVOKESTATIC,
                "java/lang/invoke/StringConcatFactory",
                "makeConcatWithConstants",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
                false);
        method.instructions.add(new InvokeDynamicInsnNode("makeConcatWithConstants",
                "(III)Ljava/lang/String;",
                handle,
                "\u0001-\u0001-\u0001-\u0002-\u0002-\u0002",
                3.14, 123, true));
        method.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
        method.instructions.add(new InsnNode(RETURN));
        // System.out.println(String);
        classNode.methods.add(method);

        // read
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);

        // save
        FileOutputStream stream = new FileOutputStream("TestStringConcatFactory.class");
        stream.write(writer.toByteArray());
        stream.close();
    }
}
