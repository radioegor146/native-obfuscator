package by.radioegor146;

import by.radioegor146.helpers.ProcessHelper;
import by.radioegor146.helpers.ProcessHelper.ProcessResult;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassicTest implements Executable {

    private final Path testData;
    private Path temp;
    private final String testName;
    private final boolean useKrakatau;

    ClassicTest(Path path, String testName, boolean useKrakatau) {
        testData = path;
        this.testName = testName;
        this.useKrakatau = useKrakatau;
    }

    private void clean() {
        try {
            Files.walk(temp)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    @SuppressWarnings("RedundantStringFormatCall")
    @Override
    public void execute() throws Throwable {
        try {
            System.err.println("Running test \"" + testName + "\"");
            System.out.println("Preparing...");

            temp = Files.createTempDirectory(String.format("native-obfuscator-test-%s-", testData.toFile().getName()));

            Path tempSource = temp.resolve("source");
            Path tempKrakatauSource = temp.resolve("source-krakatau");
            Path tempClasses = temp.resolve("classes");
            Files.createDirectories(tempSource);
            Files.createDirectories(tempKrakatauSource);
            Files.createDirectories(tempClasses);

            Path idealJar = temp.resolve("test.jar");

            List<Path> javaFiles = new ArrayList<>();
            List<Path> krakatauFiles = new ArrayList<>();
            List<Path> resourceFiles = new ArrayList<>();
            Files.find(testData, 10, (path, attr) -> true)
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                    .forEach(javaFiles::add);
            Files.find(testData, 10, (path, attr) -> true)
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".j"))
                    .forEach(krakatauFiles::add);
            Files.find(testData, 10, (path, attr) -> attr.isDirectory() || (!path.toString().endsWith(".java") && !path.toString().endsWith(".j")))
                    .filter(Files::isRegularFile)
                    .forEach(resourceFiles::add);

            Optional<String> mainClassOptional = javaFiles.stream()
                    .filter(uncheckedPredicate(p -> Files.lines(p).collect(Collectors.joining("\n"))
                            .matches("(?s).*public(\\s+static)?\\s+void\\s+main.*")))
                    .map(p -> testData.relativize(p).toString().replace('\\', '/'))
                    .map(f -> f.substring(0, f.lastIndexOf('.')))
                    .findAny();

            if (!mainClassOptional.isPresent()) {
                System.out.println("Can't find main class");
                throw new RuntimeException("Main class not found");
            }

            javaFiles.forEach(uncheckedConsumer(p -> {
                Path target = tempSource.resolve(testData.relativize(p));
                Files.createDirectories(target.getParent());
                Files.copy(p, target);
            }));
            krakatauFiles.forEach(uncheckedConsumer(p -> {
                Path target = tempKrakatauSource.resolve(testData.relativize(p));
                Files.createDirectories(target.getParent());
                Files.copy(p, target);
            }));

            System.out.println("Compiling...");

            List<String> javacParameters = new ArrayList<>(Arrays.asList("javac", "-d", tempClasses.toString()));
            javaFiles.stream().map(Path::toString).forEach(javacParameters::add);

            ProcessHelper.run(temp, 10_000, javacParameters)
                    .check("Compilation");

            if (useKrakatau) {
                System.out.println("Compiling Krakatau...");
                krakatauFiles.stream().forEach(uncheckedConsumer(path -> {
                    List<String> krakatauParameters = new ArrayList<>(Arrays.asList("krak2", "asm", "--out",
                            tempClasses.resolve(testData.relativize(path)).toString().replaceAll("\\.j$", ".class"),
                            path.toString()));
                    ProcessHelper.run(temp, 1_000, krakatauParameters)
                            .check("Krakatau compilation");
                }));
            }

            System.out.println("Copying resources");
            resourceFiles.forEach(uncheckedConsumer(p -> {
                Path target = tempClasses.resolve(testData.relativize(p));
                Files.createDirectories(target.getParent());
                Files.copy(p, target);
            }));

            List<String> jarParameters = new ArrayList<>(Arrays.asList(
                    "jar", "cvfe", idealJar.toString(), mainClassOptional.get(),
                    "-C", tempClasses + File.separator, "."));
            ProcessHelper.run(temp, 10_000,
                            jarParameters)
                    .check("Jar command");

            System.out.println("Ideal...");

            ProcessResult idealRunResult = ProcessHelper.run(temp, 60_000,
                    Arrays.asList("java", "-Dseed=1337", "-jar", idealJar.toString()));
            System.out.println(String.format("Took %dms", idealRunResult.execTime));
            idealRunResult.check("Ideal run");

            for (Platform platform : Platform.values()) {
                System.out.println(String.format("Processing platform %s...", platform.toString()));

                Path tempOutput = temp.resolve(String.format("output_%s", platform));
                Path tempCpp = tempOutput.resolve("cpp");
                Files.createDirectories(tempOutput);
                Files.createDirectories(tempCpp);
                Path resultJar = tempOutput.resolve("test.jar");

                new NativeObfuscator().process(idealJar, tempOutput, Collections.emptyList(), Collections.emptyList(),
                        null, "native_library", null, platform, false, true);

                System.out.println("Compiling CPP code...");
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    String arch = "x64";
                    if (System.getProperty("sun.arch.data.model").equals("32")) {
                        arch = "x86";
                    }
                    ProcessHelper.run(tempCpp, 120_000,
                                    Arrays.asList("cmake", "-DCMAKE_GENERATOR_PLATFORM=" + arch, "."))
                            .check("CMake prepare");
                } else {
                    ProcessHelper.run(tempCpp, 120_000,
                                    Arrays.asList("cmake", "."))
                            .check("CMake prepare");
                }

                ProcessResult compileRunresult = ProcessHelper.run(tempCpp, 160_000,
                        Arrays.asList("cmake", "--build", ".", "--config", "Release"));
                System.out.println(String.format("Took %dms", compileRunresult.execTime));
                compileRunresult.check("CMake build");


                Files.find(tempCpp.resolve("build").resolve("lib"), 1, (path, args) -> Files.isRegularFile(path))
                        .forEach(uncheckedConsumer(p -> Files.copy(p, tempOutput.resolve(p.getFileName()))));

                System.out.println("Running test...");

                long timeout = 200_000;
                ProcessResult testRunResult = ProcessHelper.run(tempOutput, timeout,
                        Arrays.asList("java",
                                "-Djava.library.path=.",
                                "-Dseed=1337",
                                "-Dplatform=" + platform.name(),
                                "-Dtest.src=" + temp.toString(),
                                "-jar", resultJar.toString()));
                System.out.println(String.format("Took %dms", testRunResult.execTime));
                testRunResult.check("Test run");

                if (!testRunResult.stdout.equals(idealRunResult.stdout)) {
                    fail(testRunResult, idealRunResult);
                }

                System.out.println("OK");
            }

            clean();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }

    private void fail(ProcessResult testRun, ProcessResult ideaRun) {
        System.err.println("Ideal:");
        System.err.println(ideaRun.stdout);
        System.err.println("Test:");
        System.err.println(testRun.stdout);
        throw new RuntimeException("Ideal != Test");
    }

    private <T> Predicate<T> uncheckedPredicate(UncheckedPredicate<T> predicate) {
        return value -> {
            try {
                return predicate.test(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private <T> Consumer<T> uncheckedConsumer(UncheckedConsumer<T> consumer) {
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
