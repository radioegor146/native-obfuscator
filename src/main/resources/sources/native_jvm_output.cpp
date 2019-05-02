#include "native_jvm.hpp"
#include "native_jvm_output.hpp"

$includes

namespace native_jvm {

    typedef void (* reg_method)(JNIEnv *,jvmtiEnv *);

    reg_method reg_methods[$class_count];

    void register_for_class(JNIEnv *env, jclass, jint id) {
        jvmtiEnv *jvmti_env = nullptr;
        JavaVM *vm = nullptr;
        env->GetJavaVM(&vm);
        vm->GetEnv((void **)&jvmti_env, JVMTI_VERSION);

        reg_methods[id](env, jvmti_env);
    }

    void prepare_lib(JNIEnv *env, jvmtiEnv *jvmti_env) {
        utils::init_utils(env);
        if (env->ExceptionCheck())
            return;

$register_code

        char method_name[] = "registerNativesForClass";
        char method_desc[] = "(I)V";
        JNINativeMethod loader_methods[] = {
            { (char *) method_name, (char *) method_desc, (void *)&register_for_class }
        };
        env->RegisterNatives(env->FindClass("$native_dir/Loader"), loader_methods, 1);
    }
}

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    jvmtiEnv *jvmti_env = nullptr;
    vm->GetEnv((void **)&env, JNI_VERSION_1_8);
    vm->GetEnv((void **)&jvmti_env, JVMTI_VERSION);
    native_jvm::prepare_lib(env, jvmti_env);
    return JNI_VERSION_1_8;
}