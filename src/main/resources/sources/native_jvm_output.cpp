#include "native_jvm.hpp"
#include "native_jvm_output.hpp"

$includes

namespace native_jvm {

	void register_all_classes(JNIEnv *env) {
$register_code
	}
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	JNIEnv *env = NULL;
	vm->GetEnv((void **)&env, JNI_VERSION_1_8);
	native_jvm::register_all_classes(env);
	return JNI_VERSION_1_8;
}