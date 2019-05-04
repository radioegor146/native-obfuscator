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
import java.util.Objects;
import java.util.stream.Stream;

public class TestsGenerator {

    @TestFactory
    public Stream<DynamicTest> generateTests() throws URISyntaxException, IOException {
        URL tests = TestsGenerator.class.getClassLoader().getResource("tests");
        Objects.requireNonNull(tests, "No tests dir in resources");

        Path testDir = Paths.get(tests.toURI());
        return Files.walk(testDir, FileVisitOption.FOLLOW_LINKS)
                .filter(Files::isDirectory)
                .filter(TestsGenerator::noChildDirectories)
//                .filter(p -> p.toString().contains("util"))
                .map(p -> DynamicTest.dynamicTest(p.toString(), new ClassicTest(p)));
    }

    private static boolean noChildDirectories(Path path) {
        try {
            return Files.list(path).noneMatch(Files::isDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
