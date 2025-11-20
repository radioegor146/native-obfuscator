package by.radioegor146;

import by.radioegor146.bytecode.PreprocessorRunner;
import by.radioegor146.source.BuildFilesBuilder;
import by.radioegor146.source.ClassSourceBuilder;
import by.radioegor146.source.MainSourceBuilder;
import by.radioegor146.source.StringPool;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gravit.launchserver.asm.ClassMetadataReader;
import ru.gravit.launchserver.asm.SafeClassWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NativeObfuscator {

    private static final Logger logger = LoggerFactory.getLogger(NativeObfuscator.class);

    private final Snippets snippets;
    private final StringPool stringPool;
    private final MethodProcessor methodProcessor;

    private final NodeCache<String> cachedStrings;
    private final NodeCache<String> cachedClasses;
    private final NodeCache<CachedMethodInfo> cachedMethods;
    private final NodeCache<CachedFieldInfo> cachedFields;

    public static class InvokeDynamicInfo {
        private final String methodName;
        private final int index;

        public InvokeDynamicInfo(String methodName, int index) {
            this.methodName = methodName;
            this.index = index;
        }

        public String getMethodName() {
            return methodName;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InvokeDynamicInfo that = (InvokeDynamicInfo) o;
            return index == that.index && Objects.equals(methodName, that.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(methodName, index);
        }
    }

    private HiddenMethodsPool hiddenMethodsPool;

    private int currentClassId;
    private String nativeDir;

    public NativeObfuscator() {
        stringPool = new StringPool();
        snippets = new Snippets(stringPool);
        cachedStrings = new NodeCache<>("(cstrings[%d])");
        cachedClasses = new NodeCache<>("(cclasses[%d])");
        cachedMethods = new NodeCache<>("(cmethods[%d])");
        cachedFields = new NodeCache<>("(cfields[%d])");
        methodProcessor = new MethodProcessor(this);
    }

    public void process(Path inputJarPath, Path outputDir, List<Path> inputLibs,
                        List<String> blackList, List<String> whiteList, String plainLibName,
                        String customLibraryDirectory,
                        Platform platform, boolean useAnnotations, boolean generateDebugJar) throws IOException {
        if (Files.exists(outputDir) && Files.isSameFile(inputJarPath.toRealPath().getParent(), outputDir.toRealPath())) {
            throw new RuntimeException("Input jar can't be in the same directory as output directory");
        }

        List<Path> libs = new ArrayList<>(inputLibs);
        libs.add(inputJarPath);
        ClassMethodFilter classMethodFilter = new ClassMethodFilter(ClassMethodList.parse(blackList), ClassMethodList.parse(whiteList), useAnnotations);
        ClassMetadataReader metadataReader = new ClassMetadataReader(libs.stream().map(x -> {
            try {
                return new JarFile(x.toFile());
            } catch (IOException ex) {
                return null;
            }
        }).collect(Collectors.toList()));

        Path cppDir = outputDir.resolve("cpp");
        Path cppOutput = cppDir.resolve("output");
        Path jniDir = outputDir.resolve("jni-headers");
        Files.createDirectories(cppOutput);

        Util.copyResource("sources/native_jvm.cpp", cppDir);
        Util.copyResource("sources/native_jvm.hpp", cppDir);
        Util.copyResource("sources/native_jvm_output.hpp", cppDir);
        Util.copyResource("sources/string_pool.hpp", cppDir);
        
        Path linuxDir = jniDir.resolve("linux");
        Files.createDirectories(linuxDir);
        Path win32Dir = jniDir.resolve("win32");
        Files.createDirectories(win32Dir);
        Path darwinDir = jniDir.resolve("darwin");
        Files.createDirectories(darwinDir);
        
        Util.copyResource("sources/jni-headers/jni.h", jniDir);
        Util.copyResource("sources/jni-headers/linux/jni_md.h", linuxDir);
        Util.copyResource("sources/jni-headers/win32/jni_md.h", win32Dir);
        Util.copyResource("sources/jni-headers/darwin/jni_md.h", darwinDir);

        String projectName = "native_library";

        BuildFilesBuilder buildBuilder = new BuildFilesBuilder(projectName);
        buildBuilder.addMainFile("native_jvm.hpp");
        buildBuilder.addMainFile("native_jvm.cpp");
        buildBuilder.addMainFile("native_jvm_output.hpp");
        buildBuilder.addMainFile("native_jvm_output.cpp");
        buildBuilder.addMainFile("string_pool.hpp");
        buildBuilder.addMainFile("string_pool.cpp");

        if (platform == Platform.HOTSPOT) {
            buildBuilder.addFlag("USE_HOTSPOT");
        }

        MainSourceBuilder mainSourceBuilder = new MainSourceBuilder();

        File jarFile = inputJarPath.toAbsolutePath().toFile();
        try (JarFile jar = new JarFile(jarFile);
             ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputDir.resolve(jarFile.getName())));
             ZipOutputStream debug = generateDebugJar ? new ZipOutputStream(
                     Files.newOutputStream(outputDir.resolve("debug.jar"))) : null) {

            logger.info("Processing {}...", jarFile);

            if (customLibraryDirectory != null) {
                nativeDir = customLibraryDirectory;

                if (jar.stream().anyMatch(x -> x.getName().equals(nativeDir) ||
                                               x.getName().startsWith(nativeDir + "/"))) {
                    logger.warn("Directory '{}' already exists in input jar file", nativeDir);
                }
            } else {
                int nativeDirId = IntStream.iterate(0, i -> i + 1)
                        .filter(i -> jar.stream().noneMatch(x -> x.getName().equals("native" + i) ||
                                                                 x.getName().startsWith("native" + i + "/")))
                        .findFirst().orElseThrow(RuntimeException::new);
                nativeDir = "native" + nativeDirId;
            }

            hiddenMethodsPool = new HiddenMethodsPool(nativeDir + "/hidden");

            Integer[] classIndexReference = new Integer[]{0};

            jar.stream().forEach(entry -> {
                if (entry.getName().equals(JarFile.MANIFEST_NAME)) return;

                try {
                    if (!entry.getName().endsWith(".class")) {
                        Util.writeEntry(jar, out, entry);
                        if (debug != null) {
                            Util.writeEntry(jar, debug, entry);
                        }
                        return;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream in = jar.getInputStream(entry)) {
                        Util.transfer(in, baos);
                    }
                    byte[] src = baos.toByteArray();

                    if (Util.byteArrayToInt(Arrays.copyOfRange(src, 0, 4)) != 0xCAFEBABE) {
                        Util.writeEntry(out, entry.getName(), src);
                        if (debug != null) {
                            Util.writeEntry(debug, entry.getName(), src);
                        }
                        return;
                    }

                    StringBuilder nativeMethods = new StringBuilder();
                    List<HiddenCppMethod> hiddenMethods = new ArrayList<>();

                    ClassReader classReader = new ClassReader(src);
                    ClassNode rawClassNode = new ClassNode(Opcodes.ASM9);
                    classReader.accept(rawClassNode, 0);

                    if (!classMethodFilter.shouldProcess(rawClassNode) ||
                        rawClassNode.methods.stream().noneMatch(method -> MethodProcessor.shouldProcess(method) &&
                                                                          classMethodFilter.shouldProcess(rawClassNode, method))) {
                        logger.info("Skipping {}", rawClassNode.name);
                        if (useAnnotations) {
                            ClassMethodFilter.cleanAnnotations(rawClassNode);
                            ClassWriter clearedClassWriter = new SafeClassWriter(metadataReader, 0);
                            rawClassNode.accept(clearedClassWriter);
                            Util.writeEntry(out, entry.getName(), clearedClassWriter.toByteArray());
                            if (debug != null) {
                                Util.writeEntry(debug, entry.getName(), clearedClassWriter.toByteArray());
                            }
                            return;
                        }
                        Util.writeEntry(out, entry.getName(), src);
                        if (debug != null) {
                            Util.writeEntry(debug, entry.getName(), src);
                        }
                        return;
                    }

                    logger.info("Preprocessing {}", rawClassNode.name);

                    rawClassNode.methods.stream()
                            .filter(MethodProcessor::shouldProcess)
                            .filter(methodNode -> classMethodFilter.shouldProcess(rawClassNode, methodNode))
                            .forEach(methodNode -> PreprocessorRunner.preprocess(rawClassNode, methodNode, platform));

                    ClassWriter preprocessorClassWriter = new SafeClassWriter(metadataReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    rawClassNode.accept(preprocessorClassWriter);
                    if (debug != null) {
                        Util.writeEntry(debug, entry.getName(), preprocessorClassWriter.toByteArray());
                    }
                    classReader = new ClassReader(preprocessorClassWriter.toByteArray());
                    ClassNode classNode = new ClassNode(Opcodes.ASM9);
                    classReader.accept(classNode, 0);

                    logger.info("Processing {}", classNode.name);

                    if (classNode.methods.stream().noneMatch(x -> x.name.equals("<clinit>"))) {
                        classNode.methods.add(new MethodNode(Opcodes.ASM9, Opcodes.ACC_STATIC,
                                "<clinit>", "()V", null, new String[0]));
                    }

                    cachedStrings.clear();
                    cachedClasses.clear();
                    cachedMethods.clear();
                    cachedFields.clear();

                    try (ClassSourceBuilder cppBuilder =
                                 new ClassSourceBuilder(cppOutput, classNode.name, classIndexReference[0]++, stringPool)) {
                        StringBuilder instructions = new StringBuilder();

                        for (int i = 0; i < classNode.methods.size(); i++) {
                            MethodNode method = classNode.methods.get(i);

                            if (!MethodProcessor.shouldProcess(method)) {
                                continue;
                            }

                            if (!classMethodFilter.shouldProcess(classNode, method)) {
                                continue;
                            }

                            MethodContext context = new MethodContext(this, method, i, classNode, currentClassId);
                            methodProcessor.processMethod(context);
                            instructions.append(context.output.toString().replace("\n", "\n    "));

                            nativeMethods.append(context.nativeMethods);

                            if (context.proxyMethod != null) {
                                hiddenMethods.add(new HiddenCppMethod(context.proxyMethod, context.cppNativeMethodName));
                            }

                            if ((classNode.access & Opcodes.ACC_INTERFACE) > 0) {
                                method.access &= ~Opcodes.ACC_NATIVE;
                            }
                        }

                        if (useAnnotations) {
                            ClassMethodFilter.cleanAnnotations(classNode);
                        }

                        classNode.version = 52;
                        ClassWriter classWriter = new SafeClassWriter(metadataReader,
                                ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                        classNode.accept(classWriter);
                        Util.writeEntry(out, entry.getName(), classWriter.toByteArray());

                        cppBuilder.addHeader(cachedStrings.size(), cachedClasses.size(), cachedMethods.size(), cachedFields.size());
                        cppBuilder.addInstructions(instructions.toString());
                        cppBuilder.registerMethods(cachedStrings, cachedClasses, nativeMethods.toString(), hiddenMethods);

                        buildBuilder.addClassFile("output/" + cppBuilder.getHppFilename());
                        buildBuilder.addClassFile("output/" + cppBuilder.getCppFilename());

                        mainSourceBuilder.addHeader(cppBuilder.getHppFilename());
                        mainSourceBuilder.registerClassMethods(currentClassId, cppBuilder.getFilename());
                    }

                    currentClassId++;
                } catch (IOException ex) {
                    logger.error("Error while processing {}", entry.getName(), ex);
                }
            });

            if (platform == Platform.ANDROID) {
                for (ClassNode hiddenClass : hiddenMethodsPool.getClasses()) {
                    ClassWriter classWriter = new SafeClassWriter(metadataReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    hiddenClass.accept(classWriter);
                    Util.writeEntry(out, hiddenClass.name + ".class", classWriter.toByteArray());
                }
            } else {
                for (ClassNode hiddenClass : hiddenMethodsPool.getClasses()) {
                    String hiddenClassFileName = "data_" + Util.escapeCppNameString(hiddenClass.name.replace('/', '_'));

                    buildBuilder.addClassFile("output/" + hiddenClassFileName + ".hpp");
                    buildBuilder.addClassFile("output/" + hiddenClassFileName + ".cpp");

                    mainSourceBuilder.addHeader(hiddenClassFileName + ".hpp");
                    mainSourceBuilder.registerDefine(stringPool.get(hiddenClass.name), hiddenClassFileName);

                    ClassWriter classWriter = new SafeClassWriter(metadataReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    hiddenClass.accept(classWriter);
                    byte[] rawData = classWriter.toByteArray();
                    List<Byte> data = new ArrayList<>(rawData.length);
                    for (byte b : rawData) {
                        data.add(b);
                    }

                    if (debug != null) {
                        Util.writeEntry(debug, hiddenClass.name + ".class", rawData);
                    }

                    try (BufferedWriter hppWriter = Files.newBufferedWriter(cppOutput.resolve(hiddenClassFileName + ".hpp"))) {
                        hppWriter.append("#include \"../native_jvm.hpp\"\n\n");
                        hppWriter.append("#ifndef ").append(hiddenClassFileName.toUpperCase()).append("_HPP_GUARD\n\n");
                        hppWriter.append("#define ").append(hiddenClassFileName.toUpperCase()).append("_HPP_GUARD\n\n");
                        hppWriter.append("namespace native_jvm::data::__ngen_").append(hiddenClassFileName).append(" {\n");
                        hppWriter.append("    const jbyte* get_class_data();\n");
                        hppWriter.append("    const jsize get_class_data_length();\n");
                        hppWriter.append("}\n\n");
                        hppWriter.append("#endif\n");
                    }

                    try (BufferedWriter cppWriter = Files.newBufferedWriter(cppOutput.resolve(hiddenClassFileName + ".cpp"))) {
                        cppWriter.append("#include \"").append(hiddenClassFileName).append(".hpp\"\n\n");
                        cppWriter.append("namespace native_jvm::data::__ngen_").append(hiddenClassFileName).append(" {\n");
                        cppWriter.append("    static const jbyte class_data[").append(String.valueOf(data.size())).append("] = { ");
                        cppWriter.append(data.stream().map(String::valueOf).collect(Collectors.joining(", ")));
                        cppWriter.append("};\n");
                        cppWriter.append("    static const jsize class_data_length = ").append(String.valueOf(data.size())).append(";\n\n");
                        cppWriter.append("    const jbyte* get_class_data() { return class_data; }\n");
                        cppWriter.append("    const jsize get_class_data_length() { return class_data_length; }\n");
                        cppWriter.append("}\n");
                    }
                }
            }

            String loaderClassName = nativeDir + "/Loader";

            ClassNode loaderClass;

            if (plainLibName == null) {
                ClassReader loaderClassReader = new ClassReader(Objects.requireNonNull(NativeObfuscator.class
                        .getResourceAsStream("compiletime/LoaderUnpack.class")));
                loaderClass = new ClassNode(Opcodes.ASM9);
                loaderClassReader.accept(loaderClass, 0);
                loaderClass.sourceFile = "synthetic";
                System.out.println("/" + nativeDir + "/");
            } else {
                ClassReader loaderClassReader = new ClassReader(Objects.requireNonNull(NativeObfuscator.class
                        .getResourceAsStream("compiletime/LoaderPlain.class")));
                loaderClass = new ClassNode(Opcodes.ASM9);
                loaderClassReader.accept(loaderClass, 0);
                loaderClass.sourceFile = "synthetic";
                loaderClass.methods.forEach(method -> {
                    for (int i = 0; i < method.instructions.size(); i++) {
                        AbstractInsnNode insnNode = method.instructions.get(i);
                        if (insnNode instanceof LdcInsnNode && ((LdcInsnNode) insnNode).cst instanceof String &&
                            ((LdcInsnNode) insnNode).cst.equals("%LIB_NAME%")) {
                            ((LdcInsnNode) insnNode).cst = plainLibName;
                        }
                    }
                });
            }

            ClassNode resultLoaderClass = new ClassNode(Opcodes.ASM9);
            String originalLoaderClassName = loaderClass.name;
            loaderClass.accept(new ClassRemapper(resultLoaderClass, new Remapper() {
                @Override
                public String map(String internalName) {
                    return internalName.equals(originalLoaderClassName) ? loaderClassName : internalName;
                }
            }));

            ClassWriter classWriter = new SafeClassWriter(metadataReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            resultLoaderClass.accept(classWriter);
            Util.writeEntry(out, loaderClassName + ".class", classWriter.toByteArray());

            logger.info("Jar file ready!");
            Manifest mf = jar.getManifest();
            if (mf != null) {
                out.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
                mf.write(out);
            }
            out.closeEntry();
            metadataReader.close();
        }

        Files.write(cppDir.resolve("string_pool.cpp"), stringPool.build().getBytes(StandardCharsets.UTF_8));

        Files.write(cppDir.resolve("native_jvm_output.cpp"), mainSourceBuilder.build(nativeDir, currentClassId)
                .getBytes(StandardCharsets.UTF_8));

        for (BuildFilesBuilder.OutputFile file : buildBuilder.build()) {
            Files.write(outputDir.resolve(file.path), file.content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public Snippets getSnippets() {
        return snippets;
    }

    public StringPool getStringPool() {
        return stringPool;
    }

    public NodeCache<String> getCachedStrings() {
        return cachedStrings;
    }

    public NodeCache<String> getCachedClasses() {
        return cachedClasses;
    }

    public NodeCache<CachedMethodInfo> getCachedMethods() {
        return cachedMethods;
    }

    public NodeCache<CachedFieldInfo> getCachedFields() {
        return cachedFields;
    }

    public String getNativeDir() {
        return nativeDir;
    }

    public HiddenMethodsPool getHiddenMethodsPool() {
        return hiddenMethodsPool;
    }
}
