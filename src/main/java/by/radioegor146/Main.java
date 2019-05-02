package by.radioegor146;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String VERSION = "1.7b";

    public static void main(String[] args) throws IOException {
        System.out.println("native-obfuscator v" + VERSION);
        if (args.length < 2) {
            System.err.println("java -jar native-obfuscator.jar <jar file> <output directory> [libraries dir]");
            return;
        }

        List<Path> libs = new ArrayList<>();
        if (args.length > 2) {
            Files.walk(Paths.get(args[2]), FileVisitOption.FOLLOW_LINKS)
                    .filter(f -> f.toString().endsWith(".jar") || f.toString().endsWith(".zip"))
                    .forEach(libs::add);
        }

        new NativeObfuscator().process(Paths.get(args[0]), Paths.get(args[1]), libs);
    }
}
