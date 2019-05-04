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
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.function.Executable;

public class ClassicTest implements Executable {

    private Path testData;
    private Path temp;

    ClassicTest(Path path) {
        testData = path;
    }

    private void clean() {
        try {
            Files.walk(temp)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {}
    }

    @Override
    public void execute() throws Throwable {
        try {
            System.err.println("Running test " + testData.toFile().getName());
            System.out.println("Preparing...");

            temp = Files.createTempDirectory(String.format("native-obfuscator-test-%s-", testData.toFile().getName()));

            Path tempSource = temp.resolve("source");
            Path tempClasses = temp.resolve("classes");
            Path tempOutput = temp.resolve("output");
            Path tempCpp = tempOutput.resolve("cpp");
            Files.createDirectories(tempSource);
            Files.createDirectories(tempClasses);
            Files.createDirectories(tempOutput);
            Files.createDirectories(tempCpp);

            Path idealJar = temp.resolve("test.jar");
            Path resultJar = tempOutput.resolve("test.jar");

            List<Path> javaFiles = new ArrayList<>();
            Files.find(testData, 1, (path, attr) -> attr.isRegularFile() && path.toString().endsWith(".java"))
                    .forEach(javaFiles::add);

            String mainClassName = javaFiles.stream()
                    .filter(uncheckedPredicate(p -> Files.lines(p).anyMatch(l -> l.matches(
                            ".*public(\\s+static)?\\s+void\\s+main.*"))))
                    .map(p -> p.getFileName().toString())
                    .map(f -> f.substring(0, f.lastIndexOf('.')))
                    .findAny().orElseThrow(() -> new RuntimeException("Can't find class with main"));

            javaFiles.forEach(unchecked(p -> Files.copy(p, tempSource.resolve(p.getFileName()))));

            System.out.println("Compiling...");

            List<String> javacParameters = new ArrayList<>(Arrays.asList("javac", "-d", tempClasses.toString()));
            javaFiles.stream().map(Path::toString).forEach(javacParameters::add);

            ProcessHelper.run(temp, 10000, javacParameters)
                    .check("Compilation");

            List<String> jarParameters = Arrays.asList(
                    "jar", "cvfe", idealJar.toString(), mainClassName,
                    "-C", tempClasses.toString() + File.separator, ".");
            ProcessHelper.run(temp, 10000,
                    jarParameters)
                    .check("Jar command");

            System.out.println("Processing...");

            new NativeObfuscator().process(idealJar, tempOutput, new ArrayList<>(), Collections.emptyList());

            System.out.println("Ideal...");

            ProcessResult idealRunResult = ProcessHelper.run(temp, 30000,
                    Arrays.asList("java", "-Dseed=1337", "-jar", idealJar.toString()));
            System.out.println(String.format("Took %dms", idealRunResult.execTime));
            idealRunResult.check("Ideal run");

            System.out.println("Compiling CPP code...");
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                String arch = "x64";
                if (System.getProperty("sun.arch.data.model").equals("32")) {
                    arch = "x86";
                }
                ProcessHelper.run(tempCpp, 40000,
                        Arrays.asList("cmake", "-DCMAKE_GENERATOR_PLATFORM=" + arch, "."))
                        .check("CMake prepare");
            } else {
                ProcessHelper.run(tempCpp, 40000,
                        Arrays.asList("cmake", "."))
                        .check("CMake prepare");
            }

            ProcessResult compileRunresult = ProcessHelper.run(tempCpp, 40000,
                    Arrays.asList("cmake", "--build", ".", "--config", "Release"));
            System.out.println(String.format("Took %dms", compileRunresult.execTime));
            compileRunresult.check("CMake build");


            Files.find(tempCpp.resolve("build").resolve("lib"), 1, (path, args) -> Files.isRegularFile(path))
                    .forEach(unchecked(p -> Files.copy(p, tempOutput.resolve(p.getFileName()))));

            System.out.println("Running test...");

            ProcessResult testRunResult = ProcessHelper.run(tempOutput, 30000,
                    Arrays.asList("java", "-Djava.library.path=.", "-Dseed=1337", "-jar", resultJar.toString()));
            System.out.println(String.format("Took %dms", testRunResult.execTime));
            testRunResult.check("Test run");

            if (!testRunResult.stdout.equals(idealRunResult.stdout)) {
                // Some tests are random based
                Pattern testResult = Pattern.compile("^Passed = \\d+, failed = (\\d+)$", Pattern.MULTILINE);
                Matcher matcher = testResult.matcher(testRunResult.stdout);
                if(matcher.find()) {
                    if(!matcher.group(1).equals("0")) {
                        fail(testRunResult, idealRunResult);
                    }
                } else {
                    fail(testRunResult, idealRunResult);
                }
            }

            System.out.println("OK");
        } catch (IOException | RuntimeException e) {
            e.printStackTrace(System.err);
            throw e;
        } finally {
            clean();
        }
    }

    private void fail(ProcessResult testRun, ProcessResult ideaRun) {
        System.err.println("Ideal:");
        System.err.println(ideaRun.stdout);
        System.err.println("Test:");
        System.err.println(testRun.stdout);
        throw new RuntimeException("Ideal != Test");
    }

    private <T> Predicate<T> uncheckedPredicate(UncheckedPredicate<T> consumer) {
        return value -> {
            try {
                return consumer.test(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private <T> Consumer<T> unchecked(UncheckedConsumer<T> consumer) {
        return value -> {
            try {
                consumer.accept(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private interface UncheckedConsumer<T> {
        void accept(T value) throws Exception;
    }

    private interface UncheckedPredicate<T> {
        boolean test(T value) throws Exception;
    }

}
