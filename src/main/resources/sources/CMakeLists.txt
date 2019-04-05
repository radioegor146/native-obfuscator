cmake_minimum_required(VERSION 3.6)
project(native_jvm)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS /MP8)     #msvc++ compiler - multicore compilation
set(CMAKE_CXX_FLAGS /wd4102)  #msvc++ compiler - unused labels warn
set(CMAKE_CXX_FLAGS /EHsc)    #msvc++ compiler - exception handling

set(CLASS_FILES $classfiles)
set(MAIN_FILES $mainfiles)
add_library(native_jvm ${CLASS_FILES} ${MAIN_FILES})