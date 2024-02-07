package by.radioegor146;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class TestsGenerator {

    private final static List<String> ALLOWED_TESTS = null; /*
    private final static List<String> ALLOWED_TESTS = Arrays.asList(
            "StringTokenizer"
    ); // */

    private static boolean testAllowed(Path testPath) {
        //noinspection ConstantValue
        if (ALLOWED_TESTS == null) {
            return true;
        }

        String testPathString = testPath.toString();

        for (String item : ALLOWED_TESTS) {
            if (testPathString.contains(item)) {
                return true;
            }
        }

        return false;
    }

    @TestFactory
    public Stream<DynamicTest> generateTests() throws URISyntaxException, IOException {
        URL tests = TestsGenerator.class.getClassLoader().getResource("tests");
        Objects.requireNonNull(tests, "No tests dir in resources");

        boolean useKrakatau;

        try {
            Process process = new ProcessBuilder().command("krak2", "-V").start();
            process.waitFor();
            if (process.exitValue() != 0) {
                throw new RuntimeException("krak2 -V failed");
            }
            useKrakatau = true;
        } catch (Exception e) {
            System.err.println("No Krakatau2 found (krak2), so tests with it will fail");
            useKrakatau = false;
        }

        boolean finalUseKrakatau = useKrakatau;

        Path testDir = Paths.get(tests.toURI());
        return Files.walk(testDir, FileVisitOption.FOLLOW_LINKS).filter(Files::isDirectory)
                .filter(TestsGenerator::hasJavaFiles).filter(TestsGenerator::testAllowed)
                .map(p -> DynamicTest.dynamicTest(testDir.relativize(p).toString(),
                        new ClassicTest(p, testDir.relativize(p).toString(), finalUseKrakatau)));
    }

    private static boolean hasJavaFiles(Path path) {
        try {
            return Files.list(path).anyMatch(f -> Files.isRegularFile(f) && f.toString().endsWith(".java"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
