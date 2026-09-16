package by.radioegor146.source;

import by.radioegor146.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BuildFilesBuilder {

    private final String projectName;
    private final List<String> classFiles;
    private final List<String> mainFiles;
    private final List<String> flags;

    public BuildFilesBuilder(String projectName) {
        this.projectName = projectName;
        classFiles = new ArrayList<>();
        mainFiles = new ArrayList<>();
        flags = new ArrayList<>();
    }

    public void addClassFile(String classFile) {
        classFiles.add(classFile);
    }

    public void addMainFile(String mainFile) {
        mainFiles.add(mainFile);
    }

    public void addFlag(String flag) {
        flags.add(flag);
    }

    public List<OutputFile> build(String nativeDir, String jarFileName) {
        List<OutputFile> result = new ArrayList<>();
        
        String cmakeTemplate = Util.readResource("sources/CMakeLists.txt");
        String cmakeContent = Util.dynamicFormat(cmakeTemplate, Util.createMap(
                "classfiles", String.join(" ", classFiles),
                "mainfiles", String.join(" ", mainFiles),
                "projectname", projectName,
                "definitions", flags.stream().map(flag -> String.format("-D%s=1", flag)).collect(Collectors.joining(" "))
        ));
        result.add(new OutputFile("cpp/CMakeLists.txt", cmakeContent));
        
        String zigTemplate = Util.readResource("sources/build.zig");
        String zigContent = Util.dynamicFormat(zigTemplate, Util.createMap(
                "nativedir", nativeDir,
                "jarfilename", jarFileName,
                "classfiles", classFiles.stream().map(file -> String.format("\"%s\",", file)).collect(Collectors.joining(" ")),
                "mainfiles", mainFiles.stream().map(file -> String.format("\"%s\",", file)).collect(Collectors.joining(" ")),
                "projectname", projectName,
                "definitions", flags.stream().map(flag -> String.format("-D%s=1", flag)).collect(Collectors.joining(" "))
        ));
        result.add(new OutputFile("build.zig", zigContent));
        
        return result;
    }

    public class OutputFile {
        public final String path;
        public final String content;

        public OutputFile(String path, String content) {
            this.path = path;
            this.content = content;
        }
    }
}
