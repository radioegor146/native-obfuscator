package by.radioegor146;

import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class Main {

    private static final String VERSION = "2.2b";

    @CommandLine.Command(name = "native-obfuscator", mixinStandardHelpOptions = true, version = "native-obfuscator " + VERSION,
            description = "Transpiles .jar file into .cpp files and generates output .jar file")
    private static class NativeObfuscatorRunner implements Callable<Integer> {

        @CommandLine.Parameters(index = "0", description = "Jar file to transpile")
        private File jarFile;

        @CommandLine.Parameters(index = "1", description = "Output directory")
        private String outputDirectory;

        @CommandLine.Option(names = {"-l", "--libraries"}, description = "Directory for dependent libraries")
        private File librariesDirectory;

        @CommandLine.Option(names = {"-x", "--exclusions-list"}, description = "File with list of exclusions for transpilation")
        private File exclusionsListFile;

        @CommandLine.Option(names = {"--plain-lib-name"}, description = "Plain library name for LoaderPlain")
        private String libraryName;

        @Override
        public Integer call() throws Exception {
            List<Path> libs = new ArrayList<>();
            if (librariesDirectory != null) {
                Files.walk(librariesDirectory.toPath(), FileVisitOption.FOLLOW_LINKS)
                        .filter(f -> f.toString().endsWith(".jar") || f.toString().endsWith(".zip"))
                        .forEach(libs::add);
            }

            List<String> exclusionsList = new ArrayList<>();
            if (exclusionsListFile != null) {
                exclusionsList = Files.readAllLines(exclusionsListFile.toPath(), StandardCharsets.UTF_8);
            }

            new NativeObfuscator().process(jarFile.toPath(), Paths.get(outputDirectory),
                    libs, exclusionsList, libraryName);

            return 0;
        }
    }

    public static void main(String[] args) throws IOException {
        System.exit(new CommandLine(new NativeObfuscatorRunner()).execute(args));
    }
}
