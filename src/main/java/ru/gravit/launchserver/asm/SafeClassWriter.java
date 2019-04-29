package ru.gravit.launchserver.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.ArrayList;

public class SafeClassWriter extends ClassWriter {

    private final ClassMetadataReader classMetadataReader;

    public SafeClassWriter(ClassMetadataReader classMetadataReader, int flags) {
        super(flags);
        this.classMetadataReader = classMetadataReader;
    }

    public SafeClassWriter(ClassReader classReader, ClassMetadataReader classMetadataReader, int flags) {
        super(classReader, flags);
        this.classMetadataReader = classMetadataReader;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        ArrayList<String> superClasses1 = classMetadataReader.getSuperClasses(type1);
        ArrayList<String> superClasses2 = classMetadataReader.getSuperClasses(type2);
        int size = Math.min(superClasses1.size(), superClasses2.size());
        int i = 0;
        while (i < size && superClasses1.get(i).equals(superClasses2.get(i)))
            i++;
        if (i == 0)
            return "java/lang/Object";
        else
            return superClasses1.get(i - 1);
    }

}