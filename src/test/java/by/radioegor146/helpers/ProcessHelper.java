package by.radioegor146.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProcessHelper {

    public static class ProcessResult {

        public int exitCode;
        public long execTime;
        public boolean success;
        public String stdout;
        public String stderr;
        public String commandLine;

        public ProcessResult(int exitCode, long execTime, boolean success, String stdout, String stderr, String commandLine) {
            this.exitCode = exitCode;
            this.execTime = execTime;
            this.stdout = stdout;
            this.stderr = stderr;
            this.success = success;
            this.commandLine = commandLine;
        }

        public void check(String processName) {
            if (exitCode == 0 && success) {
                return;
            }
            System.err.println(processName + " has failed. Success: " + success);
            System.err.println("Exit code: " + exitCode);
            System.err.println("Command line: \n" + commandLine);
            System.err.println("stdout: \n" + stdout);
            System.err.println("stderr: \n" + stderr);
            throw new RuntimeException(processName + " has failed");
        }
    }

    public static ProcessResult run(Path directory, long timeLimit, List<String> command) throws IOException {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).start();
        long startTime = System.currentTimeMillis();
        boolean success = true;
        try {
            if (!process.waitFor(timeLimit, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                success = false;
            }
            process.waitFor();
        } catch (InterruptedException e) {
            success = false;
        }
        long execTime = System.currentTimeMillis() - startTime;
        return new ProcessResult(process.exitValue(), execTime, success,
                new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(Collectors.joining("\n")),
                new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n")),
                String.join(" ", command)
        );
    }
}
