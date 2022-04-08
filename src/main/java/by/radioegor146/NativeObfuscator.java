package by.radioegor146;

import by.radioegor146.bytecode.Preprocessor;
import by.radioegor146.instructions.MethodHandler;
import by.radioegor146.source.CMakeFilesBuilder;
import by.radioegor146.source.ClassSourceBuilder;
import by.radioegor146.source.MainSourceBuilder;
import by.radioegor146.source.StringPool;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gravit.launchserver.asm.ClassMetadataReader;
import ru.gravit.launchserver.asm.SafeClassWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private InterfaceStaticClassProvider staticClassProvider;
    private final MethodProcessor methodProcessor;

    private final NodeCache<String> cachedStrings;
    private final NodeCache<String> cachedClasses;
    private final NodeCache<CachedMethodInfo> cachedMethods;
    private final NodeCache<CachedFieldInfo> cachedFields;

    public List<String> blackList = Collections.emptyList();
    public List<String> whiteList = null;
    private StringBuilder nativeMethods;

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

    private final Map<InvokeDynamicInfo, InvokeDynamicInsnNode> invokeDynamics;
    private final Map<String, MethodInsnNode> methodHandleInvokes;

    private int currentClassId;
    private String nativeDir;

    public NativeObfuscator() {
        stringPool = new StringPool();
        snippets = new Snippets(stringPool);
        cachedStrings = new NodeCache<>("(cstrings[%d])");
        cachedClasses = new NodeCache<>("(cclasses[%d])");
        cachedMethods = new NodeCache<>("(cmethods[%d])");
        cachedFields = new NodeCache<>("(cfields[%d])");
        invokeDynamics = new HashMap<>();
        methodHandleInvokes = new HashMap<>();
        methodProcessor = new MethodProcessor(this);
    }

    public void process(Path inputJarPath, Path outputDir, List<Path> inputLibs,
                        List<String> blackList, List<String> whiteList, String plainLibName, Platform platform) throws IOException {
        List<Path> libs = new ArrayList<>(inputLibs);
        libs.add(inputJarPath);
        this.blackList = blackList;
        this.whiteList = whiteList;
        ClassMetadataReader metadataReader = new ClassMetadataReader(libs.stream().map(x -> {
            try {
                return new JarFile(x.toFile());
            } catch (IOException ex) {
                return null;
            }
        }).collect(Collectors.toList()));

        Path cppDir = outputDir.resolve("cpp");
        Path cppOutput = cppDir.resolve("output");
        Files.createDirectories(cppOutput);

        Util.copyResource("sources/native_jvm.cpp", cppDir);
        Util.copyResource("sources/native_jvm.hpp", cppDir);
        Util.copyResource("sources/native_jvm_output.hpp", cppDir);
        Util.copyResource("sources/string_pool.hpp", cppDir);

        String projectName = "native_library";

        CMakeFilesBuilder cMakeBuilder = new CMakeFilesBuilder(projectName);
        cMakeBuilder.addMainFile("native_jvm.hpp");
        cMakeBuilder.addMainFile("native_jvm.cpp");
        cMakeBuilder.addMainFile("native_jvm_output.hpp");
        cMakeBuilder.addMainFile("native_jvm_output.cpp");
        cMakeBuilder.addMainFile("string_pool.hpp");
        cMakeBuilder.addMainFile("string_pool.cpp");

        MainSourceBuilder mainSourceBuilder = new MainSourceBuilder();

        File jarFile = inputJarPath.toAbsolutePath().toFile();
        try (JarFile jar = new JarFile(jarFile);
             ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputDir.resolve(jarFile.getName())));
             ZipOutputStream outDebug = new ZipOutputStream(Files.newOutputStream(outputDir.resolve("debug-" + jarFile.getName())))) {

            logger.info("Processing {}...", jarFile);

            int nativeDirId = IntStream.iterate(0, i -> i + 1)
                    .filter(i -> jar.stream().noneMatch(x -> x.getName().startsWith("native" + i)))
                    .findFirst().orElseThrow(RuntimeException::new);
            nativeDir = "native" + nativeDirId;

            staticClassProvider = new InterfaceStaticClassProvider(nativeDir);

            jar.stream().forEach(entry -> {
                if(entry.getName().equals(JarFile.MANIFEST_NAME)) return;

                try {
                    if (!entry.getName().endsWith(".class")) {
                        Util.writeEntry(jar, out, entry);
                        return;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream in = jar.getInputStream(entry)) {
                        Util.transfer(in, baos);
                    }
                    byte[] src = baos.toByteArray();

                    if (Util.byteArrayToInt(Arrays.copyOfRange(src, 0, 4)) != 0xCAFEBABE) {
                        Util.writeEntry(out, entry.getName(), src);
                        return;
                    }

                    nativeMethods = new StringBuilder();

                    ClassReader classReader = new ClassReader(src);
                    ClassNode rawClassNode = new ClassNode(Opcodes.ASM7);
                    classReader.accept(rawClassNode, 0);

                    if (rawClassNode.methods.stream().noneMatch(MethodProcessor::shouldProcess) ||
                            blackList.contains(rawClassNode.name) || (whiteList != null && !whiteList.contains(rawClassNode.name))) {
                        logger.info("Skipping {}", rawClassNode.name);
                        Util.writeEntry(out, entry.getName(), src);
                        Util.writeEntry(outDebug, entry.getName(), src);
                        return;
                    }

                    logger.info("Preprocessing {}", rawClassNode.name);

                    rawClassNode.methods.stream().filter(MethodProcessor::shouldProcess)
                            .forEach(methodNode -> methodNode.instructions.insertBefore(methodNode.instructions.get(0),
                                    new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Thread",
                                            "dumpStack", "()V")));

                    {
                        ClassWriter preprocessorClassWriter = new SafeClassWriter(metadataReader, Opcodes.ASM7);
                        rawClassNode.accept(preprocessorClassWriter);
                        Util.writeEntry(outDebug, entry.getName(), preprocessorClassWriter.toByteArray());
                    }

                    rawClassNode.methods.stream().filter(MethodProcessor::shouldProcess)
                            .forEach(methodNode -> Preprocessor.preprocess(rawClassNode, methodNode, platform));

                    ClassWriter preprocessorClassWriter = new SafeClassWriter(metadataReader, Opcodes.ASM7 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    rawClassNode.accept(preprocessorClassWriter);
                    classReader = new ClassReader(preprocessorClassWriter.toByteArray());
                    ClassNode classNode = new ClassNode(Opcodes.ASM7);
                    classReader.accept(classNode, 0);

                    logger.info("Processing {}", classNode.name);

                    if (classNode.methods.stream().noneMatch(x -> x.name.equals("<clinit>"))) {
                        classNode.methods.add(new MethodNode(Opcodes.ASM7, Opcodes.ACC_STATIC,
                                "<clinit>", "()V", null, new String[0]));
                    }

                    staticClassProvider.newClass();

                    invokeDynamics.clear();
                    methodHandleInvokes.clear();

                    cachedStrings.clear();
                    cachedClasses.clear();
                    cachedMethods.clear();
                    cachedFields.clear();

                    try (ClassSourceBuilder cppBuilder = new ClassSourceBuilder(cppOutput, classNode.name, stringPool)) {
                        StringBuilder instructions = new StringBuilder();

                        for (int i = 0; i < classNode.methods.size(); i++) {
                            MethodNode method = classNode.methods.get(i);
                            if (blackList.contains(String.format("%s#%s!%s", classNode.name, method.name, method.signature))) {
                                continue;
                            }
                            if (whiteList != null && !whiteList.contains(String.format("%s#%s!%s",
                                    classNode.name, method.name, method.signature))) {
                                continue;
                            }

                            MethodContext context = new MethodContext(this, method, i, classNode, currentClassId);
                            methodProcessor.processMethod(context);
                            instructions.append(context.output.toString().replace("\n", "\n    "));

                            nativeMethods.append(context.nativeMethods);

                            if ((classNode.access & Opcodes.ACC_INTERFACE) > 0) {
                                method.access &= ~Opcodes.ACC_NATIVE;
                            }
                        }

                        // invokeDynamics.forEach((key, value) -> InvokeDynamicHandler.processIndy(classNode, key, value));
                        methodHandleInvokes.forEach((key, value) -> MethodHandler.processMethodHandleInvoke(classNode, key, value));
                        
                        classNode.version = 52;
                        ClassWriter classWriter = new SafeClassWriter(metadataReader,
                                Opcodes.ASM7 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                        classNode.accept(classWriter);
                        Util.writeEntry(out, entry.getName(), classWriter.toByteArray());

                        cppBuilder.addHeader(cachedStrings.size(), cachedClasses.size(), cachedMethods.size(), cachedFields.size());
                        cppBuilder.addInstructions(instructions.toString());
                        cppBuilder.registerMethods(cachedStrings, cachedClasses, nativeMethods.toString(), staticClassProvider);

                        cMakeBuilder.addClassFile("output/" + cppBuilder.getHppFilename());
                        cMakeBuilder.addClassFile("output/" + cppBuilder.getCppFilename());

                        mainSourceBuilder.addHeader(cppBuilder.getHppFilename());
                        mainSourceBuilder.registerClassMethods(currentClassId, cppBuilder.getFilename());
                    }

                    currentClassId++;
                } catch (IOException ex) {
                    logger.error("Error while processing {}", entry.getName(), ex);
                }
            });

            for (ClassNode ifaceStaticClass : staticClassProvider.getReadyClasses()) {
                ClassWriter classWriter = new SafeClassWriter(metadataReader, Opcodes.ASM7 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                ifaceStaticClass.accept(classWriter);
                Util.writeEntry(out, ifaceStaticClass.name + ".class", classWriter.toByteArray());
            }

            String loaderClassName = nativeDir + "/Loader";

            ClassNode loaderClass;

            if (plainLibName == null) {
                ClassReader loaderClassReader = new ClassReader(Objects.requireNonNull(NativeObfuscator.class
                        .getResourceAsStream("compiletime/LoaderUnpack.class")));
                loaderClass = new ClassNode(Opcodes.ASM7);
                loaderClassReader.accept(loaderClass, 0);
                loaderClass.sourceFile = "synthetic";
                System.out.println("/" + nativeDir + "/");
            } else {
                ClassReader loaderClassReader = new ClassReader(Objects.requireNonNull(NativeObfuscator.class
                        .getResourceAsStream("compiletime/LoaderPlain.class")));
                loaderClass = new ClassNode(Opcodes.ASM7);
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

            ClassNode resultLoaderClass = new ClassNode(Opcodes.ASM7);
            String originalLoaderClassName = loaderClass.name;
            loaderClass.accept(new ClassRemapper(resultLoaderClass, new Remapper() {
                @Override
                public String map(String internalName) {
                    return internalName.equals(originalLoaderClassName) ? loaderClassName : internalName;
                }
            }));

            ClassWriter classWriter = new SafeClassWriter(metadataReader, Opcodes.ASM7 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            resultLoaderClass.accept(classWriter);
            Util.writeEntry(out, loaderClassName + ".class", classWriter.toByteArray());

            logger.info("Jar file ready!");
            Manifest mf = jar.getManifest();
            if (mf != null) {
                out.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
                mf.write(out);
                outDebug.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
                mf.write(outDebug);
            }
            out.closeEntry();
            metadataReader.close();
        }

        Files.write(cppDir.resolve("string_pool.cpp"), stringPool.build().getBytes(StandardCharsets.UTF_8));

        Files.write(cppDir.resolve("native_jvm_output.cpp"), mainSourceBuilder.build(nativeDir, currentClassId)
                .getBytes(StandardCharsets.UTF_8));

        Files.write(cppDir.resolve("CMakeLists.txt"), cMakeBuilder.build().getBytes(StandardCharsets.UTF_8));
    }

	public Snippets getSnippets() {
        return snippets;
    }

    public StringPool getStringPool() {
        return stringPool;
    }

    public InterfaceStaticClassProvider getStaticClassProvider() {
        return staticClassProvider;
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

    public Map<InvokeDynamicInfo, InvokeDynamicInsnNode> getInvokeDynamics() {
        return invokeDynamics;
    }
    
    public Map<String, MethodInsnNode> getMethodHandleInvokes() {
        return methodHandleInvokes;
    }
}
