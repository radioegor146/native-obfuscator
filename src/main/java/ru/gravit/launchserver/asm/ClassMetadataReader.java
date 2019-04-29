package ru.gravit.launchserver.asm;

import java.io.ByteArrayOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;

public class ClassMetadataReader {
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

    private final List<JarFile> classPath;

    public ClassMetadataReader(List<JarFile> classPath) {
        this.classPath = classPath;
    }

    public List<JarFile> getCp() {
        return classPath;
    }

    public ClassMetadataReader() {
        this.classPath = new ArrayList<>();
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
        for (JarFile file : classPath) {
            if (file.getEntry(className + ".class") == null)
                continue;
            try (InputStream in = file.getInputStream(file.getEntry(className + ".class"))) {
                return read(in);
            }
        }
        throw new ClassNotFoundException(className);
    }

    public String getSuperClass(String type) {
        if (type.equals("java/lang/Object")) return null;
        try {
            return getSuperClassASM(type);
        } catch (IOException | ClassNotFoundException e) {
            return "java/lang/Object";
        }
    }

    protected String getSuperClassASM(String type) throws IOException, ClassNotFoundException {
        CheckSuperClassVisitor cv = new CheckSuperClassVisitor();
        acceptVisitor(type, cv);
        return cv.superClassName;
    }

    public ArrayList<String> getSuperClasses(String type) {
        ArrayList<String> superclasses = new ArrayList<>(1);
        superclasses.add(type);
        while ((type = getSuperClass(type)) != null)
            superclasses.add(type);
        Collections.reverse(superclasses);
        return superclasses;
    }

    public void close() {
        classPath.forEach((file) -> {
            try {
                file.close();
            } catch (IOException ex) {
                
            }
        });
    }
}