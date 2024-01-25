package cn.langya;

import by.radioegor146.NativeObfuscator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainUI extends Application {
    private File jarFile;
    private File outputDirectory;

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("NativeObfuscator");

        TitledPane settingsPane = new TitledPane();
        settingsPane.setText("Setting");

        CheckBox useAnnotationsCheckbox = new CheckBox("Use annotations to ignore/include native obfuscation");
        CheckBox generateDebugJarCheckbox = new CheckBox("Enable build debugging .jar files");

        settingsPane.setContent(new VBox(10, useAnnotationsCheckbox, generateDebugJarCheckbox));
        settingsPane.setExpanded(true);

        TitledPane obfuscationPane = new TitledPane();
        obfuscationPane.setText("Start Obfuscator");

        TextField jarFileTextField = new TextField();
        jarFileTextField.setPromptText("Jar file path");

        TextField outputDirectoryTextField = new TextField();
        outputDirectoryTextField.setPromptText("Output directory");

        Button selectJarFileButton = new Button("Select the JAR file");
        selectJarFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAR Files", "*.jar"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                jarFile = selectedFile;
                jarFileTextField.setText(jarFile.getAbsolutePath());
            }
        });

        Button selectOutputDirectoryButton = new Button("InputDirectory");
        selectOutputDirectoryButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                outputDirectory = selectedDirectory;
                outputDirectoryTextField.setText(outputDirectory.getAbsolutePath());
            }
        });

        Button startObfuscationButton = new Button("Start Obfuscator");

        Label obfuscationTimeLabel = new Label();

        startObfuscationButton.setOnAction(e -> {

            // 获取其他输入参数
            List<Path> libraries = new ArrayList<>();
            List<String> blackList = new ArrayList<>();
            boolean useAnnotations = useAnnotationsCheckbox.isSelected();
            boolean generateDebugJar = generateDebugJarCheckbox.isSelected();

            // 混淆
            try {
                if (jarFile != null && outputDirectory != null) {
                    new NativeObfuscator().process(Paths.get(jarFile.getAbsolutePath()), Paths.get(outputDirectory.getAbsolutePath()),
                            libraries, blackList, null, null, null, useAnnotations, generateDebugJar);

                    obfuscationTimeLabel.setText("Building");

                    String cppDirectoryPath = outputDirectory.getAbsolutePath() + File.separator + "cpp";

                    ProcessBuilder pb1 = new ProcessBuilder("cmd.exe", "/c", "cmake .");
                    pb1.directory(new File(cppDirectoryPath));
                    pb1.inheritIO();
                    Process process1 = pb1.start();
                    process1.waitFor();

                    ProcessBuilder pb2 = new ProcessBuilder("cmd.exe", "/c", "cmake --build . --config Release");
                    pb2.directory(new File(cppDirectoryPath));
                    pb2.inheritIO();
                    Process process2 = pb2.start();
                    process2.waitFor();
                    int exitCode2 = process2.waitFor();
                    if (exitCode2 == 0) {
                        obfuscationTimeLabel.setText("Success");
                    } else {
                        obfuscationTimeLabel.setText("Error " + process2);
                    }
                    process2.destroy();

                    obfuscationTimeLabel.setText("混淆完成!!!!");
                } else {
                    obfuscationTimeLabel.setText("请选择Jar文件和输出目录");
                }
            } catch (Exception ex) {
                obfuscationTimeLabel.setText("混淆出错：" + ex.getMessage());
            }
        });

        obfuscationPane.setContent(new VBox(10, jarFileTextField, selectJarFileButton, outputDirectoryTextField,
                selectOutputDirectoryButton, startObfuscationButton, obfuscationTimeLabel));
        obfuscationPane.setExpanded(true);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(label, settingsPane, obfuscationPane);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setTitle("NellyObfuscator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
