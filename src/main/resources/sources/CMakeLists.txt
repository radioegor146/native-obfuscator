cmake_minimum_required(VERSION 3.5)
project($projectname)
set(CMAKE_CXX_STANDARD 17)

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "MSVC")
	set(CMAKE_CXX_FLAGS /MP8)     #multicore compilation
	set(CMAKE_CXX_FLAGS /wd4102)  #unused labels warn
	set(CMAKE_CXX_FLAGS /EHsc)    #exception handling
endif()

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

set(CLASS_FILES $classfiles)
set(MAIN_FILES $mainfiles)
add_library(native_jvm SHARED ${CLASS_FILES} ${MAIN_FILES})
