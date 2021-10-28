package by.radioegor146.source;

import by.radioegor146.InterfaceStaticClassProvider;
import by.radioegor146.NodeCache;
import by.radioegor146.Util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ClassSourceBuilder implements AutoCloseable {

    private final Path cppFile;
    private final Path hppFile;
    private final BufferedWriter cppWriter;
    private final BufferedWriter hppWriter;
    private final String className;
    private final String filename;

    private final StringPool stringPool;

    public ClassSourceBuilder(Path cppOutputDir, String className, StringPool stringPool) throws IOException {
        this.className = className;
        this.stringPool = stringPool;
        filename = Util.escapeCppNameString(className.replace('/', '_'));

        cppFile = cppOutputDir.resolve(filename.concat(".cpp"));
        hppFile = cppOutputDir.resolve(filename.concat(".hpp"));
        cppWriter = Files.newBufferedWriter(cppFile, StandardCharsets.UTF_8);
        hppWriter = Files.newBufferedWriter(hppFile, StandardCharsets.UTF_8);
    }

    public void addHeader(int strings, int classes, int methods, int fields) throws IOException {
        cppWriter.append("#include \"../native_jvm.hpp\"\n");
        cppWriter.append("#include \"../string_pool.hpp\"\n");
        cppWriter.append("#include \"").append(getHppFilename()).append("\"\n");
        cppWriter.append("\n");
        cppWriter.append("// ").append(className).append("\n");
        cppWriter.append("namespace native_jvm::classes::__ngen_").append(filename).append(" {\n\n");
        cppWriter.append("    char *string_pool;\n\n");

        if (strings > 0) {
            cppWriter.append(String.format("    jstring cstrings[%d];\n", strings));
        }
        if (classes > 0) {
            cppWriter.append(String.format("    std::mutex cclasses_mtx[%d];\n", classes));
            cppWriter.append(String.format("    jclass cclasses[%d];\n", classes));
        }
        if (methods > 0) {
            cppWriter.append(String.format("    jmethodID cmethods[%d];\n", methods));
        }
        if (fields > 0) {
            cppWriter.append(String.format("    jfieldID cfields[%d];\n", fields));
        }

        cppWriter.append("\n");
        cppWriter.append("    ");


        hppWriter.append("#include \"../native_jvm.hpp\"\n");
        hppWriter.append("\n");
        hppWriter.append("#ifndef ").append(filename.concat("_hpp").toUpperCase()).append("_GUARD\n");
        hppWriter.append("\n");
        hppWriter.append("#define ").append(filename.concat("_hpp").toUpperCase()).append("_GUARD\n");
        hppWriter.append("\n");
        hppWriter.append("// ").append(className).append("\n");
        hppWriter.append("namespace native_jvm::classes::__ngen_")
                .append(filename)
                .append(" {\n\n");
    }

    public void addInstructions(String instructions) throws IOException {
        cppWriter.append(instructions);
        cppWriter.append("\n");
    }

    public void registerMethods(NodeCache<String> strings, NodeCache<String> classes, String nativeMethods,
                                InterfaceStaticClassProvider staticClassProvider) throws IOException {

        cppWriter.append("    void __ngen_register_methods(JNIEnv *env, jclass clazz) {\n");
        cppWriter.append("        string_pool = string_pool::get_pool();\n\n");

        for (Map.Entry<String, Integer> string : strings.getCache().entrySet()) {
            cppWriter.append("        if (jstring str = env->NewStringUTF(" + stringPool.get(string.getKey()) + ")) { if (jstring int_str = utils::get_interned(env, str)) { ")
                    .append(String.format("cstrings[%d] = ", string.getValue()))
                    .append("(jstring) env->NewGlobalRef(int_str); env->DeleteLocalRef(str); env->DeleteLocalRef(int_str); } }\n");
        }

        if (!classes.isEmpty()) {
            cppWriter.append("\n");
        }

        if (!nativeMethods.isEmpty()) {
            cppWriter.append("        JNINativeMethod __ngen_methods[] = {\n");
            cppWriter.append(nativeMethods);
            cppWriter.append("        };\n\n");
            cppWriter.append("        if (clazz) env->RegisterNatives(clazz, __ngen_methods, sizeof(__ngen_methods) / sizeof(__ngen_methods[0]));\n");
            cppWriter.append("        if (env->ExceptionCheck()) { fprintf(stderr, \"Exception occured while registering native_jvm for %s\\n\", ")
                    .append(stringPool.get(className.replace("/", ".")))
                    .append("); fflush(stderr); env->ExceptionDescribe(); env->ExceptionClear(); }\n");
            cppWriter.append("\n");
        }

        if (!staticClassProvider.isEmpty()) {
            cppWriter.append("        JNINativeMethod __ngen_static_iface_methods[] = {\n");
            cppWriter.append(staticClassProvider.getMethods());
            cppWriter.append("        };\n\n");
            cppWriter.append("        jclass clazz = utils::find_class_wo_static(env, ")
                    .append(stringPool.get(staticClassProvider.getCurrentClassName().replace("/", "."))).append(");\n");
            cppWriter.append("        if (clazz) env->RegisterNatives(clazz, __ngen_static_iface_methods, sizeof(__ngen_static_iface_methods) / sizeof(__ngen_static_iface_methods[0]));\n");
            cppWriter.append("        if (env->ExceptionCheck()) { fprintf(stderr, \"Exception occured while registering native_jvm for %s\\n\", ")
                    .append(stringPool.get(className.replace("/", ".")))
                    .append("); fflush(stderr); env->ExceptionDescribe(); env->ExceptionClear(); }\n");
        }
        cppWriter.append("    }\n");
        cppWriter.append("}");


        hppWriter.append("    void __ngen_register_methods(JNIEnv *env, jclass clazz);\n");
        hppWriter.append("}\n\n#endif");
    }

    @SuppressWarnings("unused")
	private String getGetterForType(String desc) {
        if (desc.startsWith("[")) {
            return "env->FindClass(" + stringPool.get(desc) + ")";
        }
        if (desc.endsWith(";")) {
            desc = desc.substring(1, desc.length() - 1);
        }
        return "utils::find_class_wo_static(env, " + stringPool.get(desc.replace("/", ".")) + ")";
    }

    public String getFilename() {
        return filename;
    }

    public String getHppFilename() {
        return hppFile.getFileName().toString();
    }

    public String getCppFilename() {
        return cppFile.getFileName().toString();
    }

    @Override
    public void close() throws IOException {
        try {
            cppWriter.close();
        } finally {
            hppWriter.close();
        }
    }
}
