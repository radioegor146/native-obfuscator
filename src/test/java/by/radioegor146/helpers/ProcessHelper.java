package by.radioegor146.helpers;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ProcessHelper {

    public static class ProcessResult {
        public int exitCode;
        public long execTime;
        public boolean timeout;
        public String stdout;
        public String stderr;
        public String commandLine;

        public ProcessResult() {
            stdout = stderr = "";
        }

        public void check(String processName) {
            if(!timeout && exitCode == 0) {
                return;
            }

            if(timeout) {
                System.err.println(processName + " has timed out");
            } else {
                System.err.println(processName + " has failed");
                System.err.println("Exit code: " + exitCode);
            }

            System.err.println("Command line: \n" + commandLine);
            System.err.println("stdout: \n" + stdout);
            System.err.println("stderr: \n" + stderr);
            throw new RuntimeException(processName + " " + (timeout ? "has timed out" : "has failed"));
        }
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    private static void readStream(InputStream is, Consumer<String> consumer) {
        executor.submit(() -> {
            try(InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr)) {

                int count;
                char[] buf = new char[1 << 10];
                while((count = reader.read(buf)) != -1) {
                    consumer.accept(String.copyValueOf(buf, 0, count));
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static ProcessResult run(Path directory, long timeLimit, List<String> command) throws IOException {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).start();
        long startTime = System.currentTimeMillis();

        ProcessResult result = new ProcessResult();
        result.commandLine = String.join(" ", command);

        readStream(process.getInputStream(), c -> result.stdout += c);
        readStream(process.getErrorStream(), c -> result.stderr += c);

        try {
            if (!process.waitFor(timeLimit, TimeUnit.MILLISECONDS)) {
                result.timeout = true;
                process.destroyForcibly();
            }
            process.waitFor();
        } catch (InterruptedException ignored) {}

        result.execTime = System.currentTimeMillis() - startTime;
        result.exitCode = process.exitValue();

        return result;
    }
}
