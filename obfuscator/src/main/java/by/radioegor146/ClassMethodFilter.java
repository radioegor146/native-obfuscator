package by.radioegor146;

import by.radioegor146.nativeobfuscator.Native;
import by.radioegor146.nativeobfuscator.NotNative;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class ClassMethodFilter {

    private static final String NATIVE_ANNOTATION_DESC = Type.getDescriptor(Native.class);
    private static final String NOT_NATIVE_ANNOTATION_DESC = Type.getDescriptor(NotNative.class);

    private final List<String> blackList;
    private final List<String> whiteList;
    private final boolean useAnnotations;

    public ClassMethodFilter(List<String> blackList, List<String> whiteList, boolean useAnnotations) {
        this.blackList = blackList;
        this.whiteList = whiteList;
        this.useAnnotations = useAnnotations;
    }

    public boolean shouldProcess(ClassNode classNode) {
        if (this.blackList != null && this.blackList.contains(classNode.name)) {
            return false;
        }
        if (this.whiteList != null && !this.whiteList.contains(classNode.name)) {
            return false;
        }
        if (!useAnnotations) {
            return true;
        }
        if (classNode.invisibleAnnotations != null && 
            classNode.invisibleAnnotations.stream().anyMatch(annotationNode ->
                annotationNode.desc.equals(NATIVE_ANNOTATION_DESC))) {
            return true;
        }
        return classNode.methods.stream().anyMatch(methodNode -> this.shouldProcess(classNode, methodNode));
    }

    public boolean shouldProcess(ClassNode classNode, MethodNode methodNode) {
        if (blackList != null && blackList.contains(MethodProcessor.nameFromNode(methodNode, classNode))) {
            return false;
        }
        if (whiteList != null && !whiteList.contains(MethodProcessor.nameFromNode(methodNode, classNode))) {
            return false;
        }
        if (!useAnnotations) {
            return true;
        }
        if (methodNode.invisibleAnnotations != null && 
            methodNode.invisibleAnnotations.stream().anyMatch(annotationNode ->
                annotationNode.desc.equals(NATIVE_ANNOTATION_DESC))) {
            return true;
        }
        return methodNode.invisibleAnnotations == null || methodNode.invisibleAnnotations
                .stream().noneMatch(annotationNode -> annotationNode.desc.equals(
                        NOT_NATIVE_ANNOTATION_DESC));
    }

    public static void cleanAnnotations(ClassNode classNode) {
        if (classNode.invisibleAnnotations != null) {
            classNode.invisibleAnnotations.removeIf(annotationNode -> annotationNode.desc.equals(NATIVE_ANNOTATION_DESC));
        }
        classNode.methods.stream()
                .filter(methodNode -> methodNode.invisibleAnnotations != null)
                .forEach(methodNode -> methodNode.invisibleAnnotations.removeIf(annotationNode ->
                    annotationNode.desc.equals(NATIVE_ANNOTATION_DESC) || annotationNode.desc.equals(NOT_NATIVE_ANNOTATION_DESC)));
    }
}
