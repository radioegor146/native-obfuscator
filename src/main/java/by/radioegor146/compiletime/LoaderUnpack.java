package by.radioegor146.compiletime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LoaderUnpack {
    public static native void registerNativesForClass(int index, Class<?> clazz);

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        String platform = System.getProperty("os.arch").toLowerCase();

        String platformTypeName;
        switch (platform) {
            case "x86_64":
            case "amd64":
                platformTypeName = "x64";
                break;
            case "aarch64":
                platformTypeName = "arm64";
                break;
            case "arm":
                platformTypeName = "arm32";
                break;
            case "x86":
                platformTypeName = "x86";
                break;
            default:
                platformTypeName = "raw" + platform;
                break;
        }

        String osTypeName;
        if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            osTypeName = "linux.so";
        } else if (osName.contains("win")) {
            osTypeName = "windows.dll";
        } else {
            osTypeName = "raw" + osName;
        }

        String libFileName = String.format("/%s/%s-%s", LoaderUnpack.class.getName().split("\\.")[0], platformTypeName, osTypeName);

        File libFile;
        try {
            libFile = File.createTempFile("lib", null);
            libFile.deleteOnExit();
            if (!libFile.exists()) {
                throw new IOException();
            }
        } catch (IOException iOException) {
            throw new UnsatisfiedLinkError("Failed to create temp file");
        }
        byte[] arrayOfByte = new byte[2048];
        try {
            InputStream inputStream = LoaderUnpack.class.getResourceAsStream(libFileName);
            if (inputStream == null) {
                throw new UnsatisfiedLinkError(String.format("Failed to open lib file: %s", libFileName));
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(libFile);
                try {
                    int size;
                    while ((size = inputStream.read(arrayOfByte)) != -1) {
                        fileOutputStream.write(arrayOfByte, 0, size);
                    }
                    fileOutputStream.close();
                } catch (Throwable throwable) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                    throw throwable;
                }
                inputStream.close();
            } catch (Throwable throwable) {
                try {
                    inputStream.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (IOException exception) {
            throw new UnsatisfiedLinkError(String.format("Failed to copy file: %s", exception.getMessage()));
        }
        System.load(libFile.getAbsolutePath());
    }
}
