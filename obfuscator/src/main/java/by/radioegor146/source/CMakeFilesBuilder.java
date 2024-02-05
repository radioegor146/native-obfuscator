package by.radioegor146.source;

import by.radioegor146.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CMakeFilesBuilder {

    private final String projectName;
    private final List<String> classFiles;
    private final List<String> mainFiles;
    private final List<String> flags;

    public CMakeFilesBuilder(String projectName) {
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

    public String build() {
        String template = Util.readResource("sources/CMakeLists.txt");
        return Util.dynamicFormat(template, Util.createMap(
                "classfiles", String.join(" ", classFiles),
                "mainfiles", String.join(" ", mainFiles),
                "projectname", projectName,
                "definitions", flags.stream().map(flag -> String.format("-D%s=1", flag))
                        .collect(Collectors.joining(" "))
        ));
    }
}
