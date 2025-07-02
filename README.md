## native-obfuscator
Java .class to .cpp converter for use with JNI

Currently fully supports only Java 8. Java 9+ and Android support is entirely experimental

Warning: blacklist/whitelist usage is recommended because this tool slows down code significantly (like do not obfuscate full Minecraft .jar)

Also, this tool does not particularly obfuscate your code; it just transpiles it to native. Remember to use protectors like VMProtect, Themida, or obfuscator-llvm (in case of clang usage)

---

### To run this tool, you need to have these tools installed:
1. JDK 8

    - For Windows:
        
        I recommend downloading Oracle JDK 8, though you need to have some login credentials on Oracle.
    - For Linux/MacOS:
    
        Google "your distro install jdk 8", and install the required packages
2. CMake
   
    - For Windows:
     
        Download the latest release from [CMake](https://cmake.org/download/)
    
    - For Linux/MacOS:
    
        Google "your distro install cmake" and install the required package (default - `apt/yum/brew install cmake`)
3. C++/C compiler toolchain

    - For Windows:
    
        Download the freeware version of MSVS from [Microsoft](https://visualstudio.microsoft.com/ru/)
        and select Visual C++ compiler in opt-ins
      
        Or install mingw if you have any experience with this.
     
    - For Linux/MacOS:
        
        Google "your distro install g++"
      
---

### General usage:
```
Usage: native-obfuscator [-ahV] [--debug] [-b=<blackListFile>]
                         [--custom-lib-dir=<customLibraryDirectory>]
                         [-l=<librariesDirectory>] [-p=<platform>]
                         [--plain-lib-name=<libraryName>] [-w=<whiteListFile>]
                         <jarFile> <outputDirectory>
Transpiles .jar file into .cpp files and generates output .jar file
      <jarFile>           Jar file to transpile
      <outputDirectory>   Output directory
  -a, --annotations       Use annotations to ignore/include native obfuscation
  -b, --black-list=<blackListFile>
                          File with a list of blacklist classes/methods for
                            transpilation
      --custom-lib-dir=<customLibraryDirectory>
                          Custom library directory for LoaderUnpack
      --debug             Enable generation of debug .jar file (non-executable)
  -h, --help              Show this help message and exit.
  -l, --libraries=<librariesDirectory>
                          Directory for dependent libraries
  -p, --platform=<platform>
                          Target platform: hotspot - standard standalone
                            HotSpot JRE, std_java - java standard, android -
                            for Android builds (w/o DefineClass)
      --plain-lib-name=<libraryName>
                          Plain library name for LoaderPlain
  -V, --version           Print version information and exit.
  -w, --white-list=<whiteListFile>
                          File with a list of whitelist classes/methods for
                            transpilation
```

#### Arguments:
`<jarFile>` - input .jar file to obfuscate

`<outputDirectory>` - output directory where C++/new .jar file where be created

`-l <librariesDirectory>` - directory where dependant libraries should be, optional, but preferable

`-p <platform>` - JVM platform to run library on

Three options are available:
 - `hotspot`: will use HotSpot JVM internals and should work with most obfuscators (even with stack trace checking as well)
 - `std_java`: will use only minor JVM internals that must be available on all JVMs
 - `android`: use this method when building library for Android. Will use no JVM internals, as well as no DefineClass for hidden methods (obfuscators that rely on stack for string/name obfuscator will not work due to the fact that some methods will not be hidden)

`-a` - enable annotation processing

To use annotations for black/whitelisting methods/classes as `native` you can add the following library to your project:

`com.github.radioegor146.native-obfuscator:annotations:master-SNAPSHOT`

Also, you need to add [JitPack](https://jitpack.io) to your repositories.

You can add `@Native` annotation to include classes/methods to the native obfuscation process and add `@NotNative` annotation to ignore methods in classes marked as `@Native`

Whitelist/Blacklist has higher priority than annotations.

`-w <whiteList>` - path to .txt file for whitelist of methods and classes if required

`-b <blackList>` - path to a .txt file for a blacklist of methods and classes if required

Both of them should come in such form:
```
<class>
<class>#<method name>#<method descriptor>
mypackage/myotherpackage/Class1
mypackage/myotherpackage/Class1#doSomething!()V
mypackage/myotherpackage/Class1$SubClass#doOther!(I)V
```
It uses internal names of classes and method descriptors for filtering (you can read more about it by googling "java internal class names" or "java method descriptors")

Also, you can use a wildcard matcher like these:
```
mypackage/myotherpackage/*
mypackage/myotherpackagewithnested/**
mypackage/myotherpackage/*/Class1
mypackage/myotherpackagewithnested/**/Class1
mypackage/myotherpackage/Class*
```
`*` matches a single entry (divided by `/`) in the class/package name

`**` matches all entries in class/package name


`--plain-lib-name` - if you ship your .jar separately from the result native libraries, or you use it for Android, you can specify the name of the native library that it will try to search while using.

`--custom-lib-dir` - if you want to set custom directory for storing libraries inside the jar

If you want to ship your .jar with native libraries in it, you should omit that argument, and after building native files, add them in the form of
```
x64-windows.dll
x64-linux.so
x86-windows.dll
x64-macos.dylib
arm64-linux.so
arm64-windows.dll
```
to the directory of the .jar file that this tool will print in `stdout` (by default `native0/` or custom if `--custom-lib-dir` is present)

#### Basic usage:
1. Transpile your code using `java -jar native-obfuscator.jar <input jar> <output directory>`
2. Run `cmake .` in the result `cpp` directory
3. Add changes to .cpp code if necessary
4. Run `cmake --build . --config Release` in result `cpp` directory to build .so/.dll file
5. Copy result .dll/.so from `build/libs/` to the path specified in the previous paragraph.
6. Run created .jar `java -jar <output jar>` and enjoy!

---

### Building the tool by yourself
1. Run `gradlew assemble` to force gradle not to run tests after the build

---

### Tests
You need to have [Krakatau](https://github.com/Storyyeller/Krakatau) installed to your PATH, because test suite is using `krak2` for some tests

1. Run `gradlew build` to assemble and run full test suite

This tool uses tests from [huzpsb/JavaObfuscatorTest](https://github.com/huzpsb/JavaObfuscatorTest)

---

In case of any problems, feel free to open an issue or contact me at [re146.dev](https://re146.dev)

### Stargazers over time

[![Stargazers over time](https://starchart.cc/radioegor146/native-obfuscator.svg?variant=adaptive)](https://starchart.cc/radioegor146/native-obfuscator)
