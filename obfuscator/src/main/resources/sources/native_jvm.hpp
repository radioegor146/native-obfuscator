#include "jni.h"
#include <cmath>
#include <cstring>
#include <string>
#include <cstdio>
#include <unordered_set>
#include <mutex>
#include <initializer_list>

#ifndef NATIVE_JVM_HPP_GUARD

#define NATIVE_JVM_HPP_GUARD

namespace native_jvm::utils {

    void init_utils(JNIEnv *env);

    void throw_re(JNIEnv *env, const char *exception_class, const char *error, int line);

    jobjectArray create_multidim_array(JNIEnv *env, jobject classloader, jint count, jint required_count,
        const char *class_name, int line, std::initializer_list<jint> sizes, int dim_index = 0);

    template <int sort>
    jarray create_array_value(JNIEnv* env, jint size);

    template <int sort>
    jarray create_multidim_array_value(JNIEnv *env, jint count, jint required_count,
        const char *name, int line, std::initializer_list<jint> sizes, int dim_index = 0) {
        if (required_count == 0) {
            env->FatalError("required_count == 0");
            return nullptr;
        }
        jint current_size = sizes.begin()[dim_index];
        if (current_size < 0) {
            throw_re(env, "java/lang/NegativeArraySizeException", "MULTIANEWARRAY size < 0", line);
            return nullptr;
        }
        if (count == 1) {
            return create_array_value<sort>(env, current_size);
        }
        jobjectArray result_array = nullptr;
        if (jclass clazz = env->FindClass((std::string(count - 1, '[') + std::string(name)).c_str())) {
            result_array = env->NewObjectArray(current_size, clazz, nullptr);
            if (env->ExceptionCheck()) {
                return nullptr;
            }
            env->DeleteLocalRef(clazz);
        }
        else
            return nullptr;

        if (required_count == 1) {
            return result_array;
        }

        for (jint i = 0; i < current_size; i++) {
            jarray inner_array = create_multidim_array_value<sort>(env, count - 1, required_count - 1,
                name, line, sizes, dim_index + 1);
            if (env->ExceptionCheck()) {
                env->DeleteLocalRef(result_array);
                return nullptr;
            }
            env->SetObjectArrayElement(result_array, i, inner_array);
            env->DeleteLocalRef(inner_array);
            if (env->ExceptionCheck()) {
                env->DeleteLocalRef(result_array);
                return nullptr;
            }
        }
        return result_array;
    }

    jobject link_call_site(JNIEnv *env, jobject caller_obj, jobject bootstrap_method_obj,
            jobject name_obj, jobject type_obj, jobject static_arguments, jobject appendix_result);

    jclass find_class_wo_static(JNIEnv *env, jobject classloader, jstring class_name);

    jclass get_class_from_object(JNIEnv *env, jobject object);

    jobject get_classloader_from_class(JNIEnv *env, jclass clazz);

    jobject get_lookup(JNIEnv *env, jclass clazz);

    void bastore(JNIEnv *env, jarray array, jint index, jint value);
    jbyte baload(JNIEnv *env, jarray array, jint index);

    void clear_refs(JNIEnv *env, std::unordered_set<jobject> &refs);

    jstring get_interned(JNIEnv *env, jstring value);
}

#endif