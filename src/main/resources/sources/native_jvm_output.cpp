#include "native_jvm.hpp"
#include "native_jvm_output.hpp"

$includes

namespace native_jvm {

	void register_all_classes(JNIEnv *env, jvmtiEnv *jvmti_env) {
$register_code
	}
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	JNIEnv *env = nullptr;
	jvmtiEnv *jvmti_env = nullptr;
	vm->GetEnv((void **)&env, JNI_VERSION_1_8);
	vm->GetEnv((void **)&jvmti_env, JVMTI_VERSION);
	native_jvm::register_all_classes(env, jvmti_env);
	return JNI_VERSION_1_8;
}