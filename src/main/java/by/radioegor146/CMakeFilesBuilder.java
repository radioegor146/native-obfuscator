package by.radioegor146;

import java.util.ArrayList;
import java.util.List;

class CMakeFilesBuilder {

    private String projectName;
    private List<String> classFiles;
    private List<String> mainFiles;

    public CMakeFilesBuilder(String projectName) {
        this.projectName = projectName;
        classFiles = new ArrayList<>();
        mainFiles = new ArrayList<>();
    }

    public void addClassFile(String classFile) {
        classFiles.add(classFile);
    }

    public void addMainFile(String mainFile) {
        mainFiles.add(mainFile);
    }

    public String build() {
        String template = Util.readResource("sources/CMakeLists.txt");
        return Util.dynamicFormat(template, Util.createMap(
                "classfiles", String.join(" ", classFiles),
                "mainfiles", String.join(" ", mainFiles),
                "projectname", projectName
        ));
    }
}
