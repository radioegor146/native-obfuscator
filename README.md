# native-obfuscator
Java .class to .cpp converter for use with JNI

Currently, supports only Java 8

Warning: blacklist/whitelist usage is recommended, because this tool slows down code significantly (like do not obfuscate full minecraft .jar)

Also, this tool does not particulary obfuscates your code, it just transpiles it to native. Remember to use protectors like VMProtect, Themida or obfuscator-llvm (in case of clang usage)

---

### To run this tool you need to have this installed:
1. JDK 8

    - For Windows:
        
        I recommend downloading Oracle JDK 8, though you need to have some login credentials on Oracle.
    - For Linux/MacOS:
    
        Google "your distro install jdk 8", and install required packages
2. CMake
   
    - For Windows:
     
        Download latest release from https://cmake.org/download/
    
    - For Linux/MacOS:
    
        As well, google "your distro install cmake", and install required package (default - `apt/yum/brew install cmake`)
3. C++/C compiler toolchain

    - For Windows:
    
        Download free version of MSVS: https://visualstudio.microsoft.com/ru/
        and select Visual C++ compiler in opt-ins
      
        Or install mingw, if you had any experience with this
     
    - For Linux/MacOS:
        
        Google "your distro install g++"
      
---

### General usage:
```
Usage: native-obfuscator [-hV] [-b=<blackListFile>] [-l=<librariesDirectory>]
                         [--plain-lib-name=<libraryName>] [-w=<whiteListFile>]
                         <jarFile> <outputDirectory>
Transpiles .jar file into .cpp files and generates output .jar file
      <jarFile>           Jar file to transpile
      <outputDirectory>   Output directory
  -b, --black-list=<blackListFile>
                          File with list of blacklist classes/methods for
                            transpilation
  -h, --help              Show this help message and exit.
  -l, --libraries=<librariesDirectory>
                          Directory for dependent libraries
      --plain-lib-name=<libraryName>
                          Plain library name for LoaderPlain
  -V, --version           Print version information and exit.
  -w, --white-list=<whiteListFile>
                          File with list of whitelist classes/methods for
                            transpilation
```

#### Arguments:
`<jarFile>` - input .jar file to obfuscate

`<outputDirectory>` - output directory where C++/new .jar file where be created

`-l <librariesDirectory>` - directory where dependant libraries should be, optional, but preferable

`-p <platform>` - JVM platform on what library will be executed

Two options are available:
    - hotspot: will use HotSpot JVM internals and should work with most obfuscators (even with stack trace checking as well)
    - std_java: will use only minor JVM internals that are available on Android as well. Use only this option if you want to run your library on Android

`-a` - enable annotation processing

You can add `@Native` annotation to include classes/methods to the native obfuscation process, and add `@NotNative` annotation to ignore methods in classes marked as `@Native`

Whitelist/Blacklist has higher priority than annotations

`-w <whiteList>` - path to .txt file for whitelist of methods and classes if required

`-b <blackList>` - path to .txt file for blacklist of methods and classes if required

Both of them should come in such form:
```
<class>
<class>#<method name>#<method descriptor>
mypackage/myotherpackage/Class1
mypackage/myotherpackage/Class1#doSomething!()V
mypackage/myotherpackage/Class1$SubClass#doOther!(I)V
```
It uses internal names of classes and method descriptors for filtering (you can read more about it by googling "java internal class names" or "java method descriptors")

`--plain-lib-name` - if you ship your .jar in separate from result native libraries, or you use it for Android, you can specify the name of native library that it will try to search while using.

If you want to ship your .jar with native libraries in it, you should omit that argument, and after building native files add them in form of
```
x64-windows.dll
x64-linux.so
x86-windows.dll
x64-macos.dylib
arm64-linux.so
arm64-windows.dll
```
to the directory of .jar file that this tool will print in `stdout` (by default `native0/`)

#### Basic usage:
1. Transpile your code using `java -jar native-obfuscator.jar <input jar> <output directory>`
2. Run `cmake .` in result `cpp` directory
3. Add changes to .cpp code if necessary
4. Run `cmake --build . --config Release` in result `cpp` directory to build .so/.dll file
5. Copy result .dll/.so from `build/libs/` to the specified in previous paragraph path.
6. Run created .jar `java -jar <output jar>` and enjoy!

---

### Building the tool by yourself
1. Run `gradlew assemble` to force gradle to not run tests after build

---

In case of any problems feel free to open issue or contact me at https://1488.me
