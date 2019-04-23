#include "native_jvm.hpp"

#ifndef NATIVE_JVM_OUTPUT_HPP_GUARD

#define NATIVE_JVM_OUTPUT_HPP_GUARD

namespace native_jvm {
    void prepare_lib(JNIEnv *env, jvmtiEnv *jvmti_env);
}

#endif