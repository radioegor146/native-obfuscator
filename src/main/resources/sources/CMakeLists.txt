cmake_minimum_required(VERSION 3.8)
project($projectname)
set(CMAKE_CXX_STANDARD 17)

set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY build/${CMAKE_BUILD_TYPE}/lib)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY build/${CMAKE_BUILD_TYPE}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY build/${CMAKE_BUILD_TYPE}/bin)

if("${CMAKE_CXX_COMPILER_ID}" STREQUAL "GNU")
    if (CMAKE_CXX_COMPILER_VERSION VERSION_LESS 6.0)
        message(FATAL_ERROR "GCC version must be at least 6.0!")
    endif()
elseif ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "MSVC")
else()
    message(WARNING "You are using an unsupported compiler! Compilation has only been tested with MSVC and GCC.")
endif()

if ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "MSVC")
	set(CMAKE_CXX_FLAGS /MP8)     #multicore compilation
	set(CMAKE_CXX_FLAGS /wd4102)  #unused labels warn
	set(CMAKE_CXX_FLAGS /EHsc)    #exception handling
endif()

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

set(CLASS_FILES $classfiles)
set(MAIN_FILES $mainfiles)
add_library($projectname SHARED ${CLASS_FILES} ${MAIN_FILES})
