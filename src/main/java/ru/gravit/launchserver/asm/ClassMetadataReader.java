package ru.gravit.launchserver.asm;

import java.io.ByteArrayOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Позволяет искать методы внутри незагруженных классов и общие суперклассы для
 * чего угодно. Работает через поиск class-файлов в classpath.
 */
public class ClassMetadataReader implements Closeable {
    private class CheckSuperClassVisitor extends ClassVisitor {

        String superClassName;

        public CheckSuperClassVisitor() {
            super(Opcodes.ASM7);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
                          String[] interfaces) {
            superClassName = superName;
        }
    }

    private final List<JarFile> cp;

    public ClassMetadataReader(List<JarFile> cp) {
        this.cp = cp;
    }

    public List<JarFile> getCp() {
        return cp;
    }

    public ClassMetadataReader() {
        this.cp = new ArrayList<>();
    }

    public void acceptVisitor(byte[] classData, ClassVisitor visitor) {
        new ClassReader(classData).accept(visitor, 0);
    }

    public void acceptVisitor(String className, ClassVisitor visitor) throws IOException, ClassNotFoundException {
        acceptVisitor(getClassData(className), visitor);
    }
    
    private static byte[] read(InputStream input) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            for (int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
                output.write(buffer, 0, length);
            }
            return output.toByteArray();
        }
    }

    public byte[] getClassData(String className) throws IOException, ClassNotFoundException {
        for (JarFile f : cp) {
            if (f.getEntry(className + ".class") != null) {
                byte[] bytes = null;
                try (InputStream in = f.getInputStream(f.getEntry(className + ".class"))) {
                    bytes = read(in);
                }
                return bytes;
            }
        }
        throw new ClassNotFoundException(className);
    }

    public String getSuperClass(String type) {
        if (type.equals("java/lang/Object")) return null;
        try {
            return getSuperClassASM(type);
        } catch (Exception e) {
            return "java/lang/Object";
        }
    }

    protected String getSuperClassASM(String type) throws IOException, ClassNotFoundException {
        CheckSuperClassVisitor cv = new CheckSuperClassVisitor();
        acceptVisitor(type, cv);
        return cv.superClassName;
    }

    /**
     * Возвращает суперклассы в порядке возрастающей конкретности (начиная с
     * java/lang/Object и заканчивая данным типом)
     */
    public ArrayList<String> getSuperClasses(String type) {
        ArrayList<String> superclasses = new ArrayList<>(1);
        superclasses.add(type);
        while ((type = getSuperClass(type)) != null)
            superclasses.add(type);
        Collections.reverse(superclasses);
        return superclasses;
    }

    public static void close(AutoCloseable closable) {
        try {
            closable.close();
        } catch (Exception ignored) {
        }
    }
    
    @Override
    public void close() {
        cp.stream().forEach(ClassMetadataReader::close);
        cp.clear();
    }

}