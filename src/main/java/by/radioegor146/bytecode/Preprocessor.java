package by.radioegor146.bytecode;

import by.radioegor146.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class Preprocessor {

    private final static List<IPreprocessor> PREPROCESSORS = new ArrayList<>();

    static {
        PREPROCESSORS.add(new IndyPreprocessor());
    }

    public static void preprocess(ClassNode classNode, MethodNode methodNode, Platform platform) {
        for (IPreprocessor preprocessor : PREPROCESSORS) {
            preprocessor.process(classNode, methodNode, platform);
        }
    }
}
