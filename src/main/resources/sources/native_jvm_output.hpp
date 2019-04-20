#include "native_jvm.hpp"

#ifndef NATIVE_JVM_OUTPUT_HPP_GUARD

#define NATIVE_JVM_OUTPUT_HPP_GUARD

namespace native_jvm {
	void register_all_classes(JNIEnv *env, jvmtiEnv *jvmti_env);
}

#endif