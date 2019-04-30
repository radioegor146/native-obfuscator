/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package by.radioegor146;

import by.radioegor146.helpers.ProcessHelper;
import by.radioegor146.helpers.ProcessHelper.ProcessResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.function.Executable;

/**
 *
 * @author radioegor146
 */
public class ClassicTest implements Executable {

    private final Path testDirectory;
    private Path tempDirectory;
    
    ClassicTest(File directory) {
        testDirectory = directory.toPath();
    }
    
    private void clean() {
        if (true)
            return;
        try {
            Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // ignored
        }
    }
    
    @Override
    public void execute() throws Throwable {
        try {
            System.err.println("Running test #" + testDirectory.toFile().getName());
            Path sourceDirectory = testDirectory.resolve("source");
            if (!sourceDirectory.toFile().exists()) 
                throw new IOException("Source directory not found");

            System.out.println("Preparing...");
            tempDirectory = Files.createTempDirectory("native-obfuscator-test");
            Path tempSourceDirectory = tempDirectory.resolve("source");
            tempSourceDirectory.toFile().mkdirs();
            Path tempClassFilesDirectory = tempDirectory.resolve("classes");
            tempClassFilesDirectory.toFile().mkdirs();
            Path tempOutputDirectory = tempDirectory.resolve("output");
            tempOutputDirectory.toFile().mkdirs();
            
            for (File sourceFile : sourceDirectory.toFile().listFiles(x -> x.getName().endsWith(".java")))
                Files.copy(sourceFile.toPath(), tempSourceDirectory.resolve(sourceFile.getName()));
            
            System.out.println("Compiling...");
            List<String> javacParameters = new ArrayList<>();
            javacParameters.add("javac");
            javacParameters.add("-d");
            javacParameters.add(tempClassFilesDirectory.toAbsolutePath().toString());
            for (File sourceFile : sourceDirectory.toFile().listFiles(x -> x.getName().endsWith(".java")))
                javacParameters.add(sourceFile.getAbsolutePath());
            ProcessHelper.run(tempDirectory, 10000, javacParameters.toArray(new String[0]))
                    .check("Compilation");
            ProcessHelper.run(tempDirectory, 10000, "jar", "cvfe", tempDirectory.resolve("test.jar").toAbsolutePath().toString(), "Test", "-C", tempClassFilesDirectory.toAbsolutePath().toString() + "/", ".")
                    .check("Jar command");
            
            System.out.println("Processing...");
            new NativeObfuscator().process(tempDirectory.resolve("test.jar"), tempOutputDirectory, new ArrayList<>());
            
            System.out.println("Ideal...");
            ProcessResult idealRunResult = ProcessHelper.run(tempDirectory, 30000, "java", "-jar", tempDirectory.resolve("test.jar").toAbsolutePath().toString());
            idealRunResult.check("Ideal run");
            System.out.println("Compiling CPP code...");
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                String arch = "x64";
                if (System.getProperty("sun.arch.data.model").equals("32"))
                    arch = "x86";
                ProcessHelper.run(tempOutputDirectory.resolve("cpp"), 40000, "cmake", "-DCMAKE_GENERATOR_PLATFORM=" + arch, ".").check("CMake prepare");
            } else {
                ProcessHelper.run(tempOutputDirectory.resolve("cpp"), 40000, "cmake", ".").check("CMake prepare");
            }
            ProcessHelper.run(tempOutputDirectory.resolve("cpp"), 40000, "cmake", "--build", ".", "--config", "Release").check("CMake build");
            for (File libFile : tempOutputDirectory.resolve("cpp").resolve("build").resolve("lib").toFile().listFiles(x -> !x.isDirectory()))
                Files.copy(libFile.toPath(), tempOutputDirectory.resolve(libFile.getName()));
            
            System.out.println("Running test...");
            ProcessResult testRunResult = ProcessHelper.run(tempOutputDirectory, 30000, "java", "-Djava.library.path=.", "-jar",
                    tempOutputDirectory.resolve("test.jar").toAbsolutePath().toString());
            testRunResult.check("Test run");
            
            if (!testRunResult.stdout.equals(idealRunResult.stdout)) {
                System.err.println("Ideal:");
                System.err.println(idealRunResult.stdout);
                System.err.println("Test:");
                System.err.println(testRunResult.stdout);
                throw new RuntimeException("Ideal != Test");
            }
            
            System.out.println("OK");
        } catch (IOException | RuntimeException e) {
            clean();
            throw e;
        }
        clean();
    }
    
}
