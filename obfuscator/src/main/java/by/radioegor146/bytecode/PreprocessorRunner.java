package by.radioegor146.bytecode;

import by.radioegor146.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class PreprocessorRunner {

    private final static List<Preprocessor> PREPROCESSORS = new ArrayList<>();

    static {
        PREPROCESSORS.add(new IndyPreprocessor());
        PREPROCESSORS.add(new LdcPreprocessor());
    }

    public static boolean preprocess(ClassNode classNode, MethodNode methodNode, Platform platform) {
        boolean changed = false;
        for (Preprocessor preprocessor : PREPROCESSORS) {
            changed |= preprocessor.process(classNode, methodNode, platform);
        }
        return changed;
    }
}
