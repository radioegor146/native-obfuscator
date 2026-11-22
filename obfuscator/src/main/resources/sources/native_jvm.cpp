/*
 * Emitted runtime of native-obfuscator.
 *
 * Licensed under GPL-3.0 WITH an Output Exception: you may link, embed,
 * compile and distribute this file in your own programs under terms of
 * your choice. See the "Output Exception" section in the project's
 * LICENSE for the full terms.
 *
 * https://github.com/radioegor146/native-obfuscator#licensing
 */

#include "native_jvm.hpp"
#include <algorithm>

namespace native_jvm::utils {

    jclass boolean_array_class;
    jmethodID string_intern_method;
    jclass class_class;
    jmethodID get_classloader_method;
    jmethodID class_for_name_method;
    jmethodID get_component_type_method;
    jclass reflect_array_class;
    jmethodID new_array_method;
    jclass object_class;
    jmethodID get_class_method;
    jclass classloader_class;
    jmethodID load_class_method;
    jclass no_class_def_found_class;
    jmethodID ncdf_init_method;
    jclass throwable_class;
    jmethodID get_message_method;
    jmethodID init_cause_method;
    jclass methodhandles_lookup_class;
    jmethodID lookup_init_method;
#ifdef USE_HOTSPOT
    jclass methodhandle_natives_class;
    jmethodID link_call_site_method;
    bool is_jvm11_link_call_site;
#endif

    void init_utils(JNIEnv *env) {
        jclass clazz = env->FindClass("[Z");
        if (env->ExceptionCheck())
            return;
        boolean_array_class = (jclass) env->NewGlobalRef(clazz);
        env->DeleteLocalRef(clazz);

        jclass string_clazz = env->FindClass("java/lang/String");
        if (env->ExceptionCheck())
            return;
        string_intern_method = env->GetMethodID(string_clazz, "intern", "()Ljava/lang/String;");
        if (env->ExceptionCheck())
            return;
        env->DeleteLocalRef(string_clazz);

        jclass _class_class = env->FindClass("java/lang/Class");
        if (env->ExceptionCheck())
            return;
        class_class = (jclass) env->NewGlobalRef(_class_class);
        env->DeleteLocalRef(_class_class);

        get_classloader_method = env->GetMethodID(class_class, "getClassLoader", "()Ljava/lang/ClassLoader;");
        if (env->ExceptionCheck())
            return;
        class_for_name_method = env->GetStaticMethodID(class_class, "forName",
            "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
        if (env->ExceptionCheck())
            return;
        get_component_type_method = env->GetMethodID(class_class, "getComponentType", "()Ljava/lang/Class;");
        if (env->ExceptionCheck())
            return;
        jclass array_class = env->FindClass("java/lang/reflect/Array");
        if (env->ExceptionCheck())
            return;
        reflect_array_class = (jclass) env->NewGlobalRef(array_class);
        env->DeleteLocalRef(array_class);
        if (reflect_array_class == nullptr)
            return;
        new_array_method = env->GetStaticMethodID(reflect_array_class, "newInstance",
            "(Ljava/lang/Class;[I)Ljava/lang/Object;");
        if (env->ExceptionCheck())
            return;

        jclass _object_class = env->FindClass("java/lang/Object");
        if (env->ExceptionCheck())
            return;
        object_class = (jclass) env->NewGlobalRef(_object_class);
        env->DeleteLocalRef(_object_class);

        get_class_method = env->GetMethodID(object_class, "getClass", "()Ljava/lang/Class;");
        if (env->ExceptionCheck())
            return;

        jclass _classloader_class = env->FindClass("java/lang/ClassLoader");
        if (env->ExceptionCheck())
            return;
        classloader_class = (jclass) env->NewGlobalRef(_classloader_class);
        env->DeleteLocalRef(_classloader_class);

        load_class_method = env->GetMethodID(classloader_class, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
        if (env->ExceptionCheck())
            return;

        jclass _no_class_def_found_class = env->FindClass("java/lang/NoClassDefFoundError");
        if (env->ExceptionCheck())
            return;
        no_class_def_found_class = (jclass) env->NewGlobalRef(_no_class_def_found_class);
        env->DeleteLocalRef(_no_class_def_found_class);

        ncdf_init_method = env->GetMethodID(no_class_def_found_class, "<init>", "(Ljava/lang/String;)V");
        if (env->ExceptionCheck())
            return;

        jclass _throwable_class = env->FindClass("java/lang/Throwable");
        if (env->ExceptionCheck())
            return;
        throwable_class = (jclass) env->NewGlobalRef(_throwable_class);
        env->DeleteLocalRef(_throwable_class);

        get_message_method = env->GetMethodID(throwable_class, "getMessage", "()Ljava/lang/String;");
        if (env->ExceptionCheck())
            return;

        init_cause_method = env->GetMethodID(throwable_class, "initCause",
                                            "(Ljava/lang/Throwable;)Ljava/lang/Throwable;");
        if (env->ExceptionCheck())
            return;

        jclass _methodhandles_lookup_class = env->FindClass("java/lang/invoke/MethodHandles$Lookup");
        if (env->ExceptionCheck())
            return;
        methodhandles_lookup_class = (jclass) env->NewGlobalRef(_methodhandles_lookup_class);
        env->DeleteLocalRef(_methodhandles_lookup_class);

        lookup_init_method = env->GetMethodID(methodhandles_lookup_class, "<init>", "(Ljava/lang/Class;)V");
        if (env->ExceptionCheck())
            return;

#ifdef USE_HOTSPOT
        jclass _methodhandle_natives_class = env->FindClass("java/lang/invoke/MethodHandleNatives");
        if (env->ExceptionCheck())
            return;
        methodhandle_natives_class = (jclass) env->NewGlobalRef(_methodhandle_natives_class);
        env->DeleteLocalRef(_methodhandle_natives_class);

        link_call_site_method = env->GetStaticMethodID(methodhandle_natives_class, "linkCallSite",
            "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");
        is_jvm11_link_call_site = false;
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
            link_call_site_method = env->GetStaticMethodID(methodhandle_natives_class, "linkCallSite",
                "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");
            is_jvm11_link_call_site = true;
            if (env->ExceptionCheck())
                return;
        }
#endif
    }

#ifdef USE_HOTSPOT
    jobject link_call_site(JNIEnv *env, jobject caller_obj, jobject bootstrap_method_obj,
        jobject name_obj, jobject type_obj, jobject static_arguments, jobject appendix_result) {
        if (is_jvm11_link_call_site) {
            return env->CallStaticObjectMethod(methodhandle_natives_class, link_call_site_method, caller_obj, 0,
                bootstrap_method_obj, name_obj, type_obj, static_arguments, appendix_result);
        }
        return env->CallStaticObjectMethod(methodhandle_natives_class, link_call_site_method, caller_obj,
            bootstrap_method_obj, name_obj, type_obj, static_arguments, appendix_result);
    }
#endif

    static jclass complete_class_lookup(JNIEnv *env, jclass clazz);

    jarray create_multidim_array(JNIEnv *env, jobject classloader, const char *descriptor, int line,
        std::initializer_list<jint> sizes) {
        // Even a zero-length outer dimension does not suppress a negative inner dimension.
        for (jint size : sizes) {
            if (size < 0) {
                throw_re(env, "java/lang/NegativeArraySizeException", "MULTIANEWARRAY size < 0", line);
                return nullptr;
            }
        }

        // Leave one more array dimension than the component type passed to Array.newInstance.
        std::string binary_name(descriptor + sizes.size() - 1);
        std::replace(binary_name.begin(), binary_name.end(), '/', '.');
        jstring name = env->NewStringUTF(binary_name.c_str());
        if (name == nullptr) {
            return nullptr;
        }
        jclass array_class = complete_class_lookup(env, (jclass) env->CallStaticObjectMethod(
            class_class, class_for_name_method, name, JNI_FALSE, classloader));
        env->DeleteLocalRef(name);
        if (array_class == nullptr) {
            return nullptr;
        }

        jclass component = (jclass) env->CallObjectMethod(array_class, get_component_type_method);
        bool lookup_failed = env->ExceptionCheck();
        env->DeleteLocalRef(array_class);
        if (lookup_failed) {
            return nullptr;
        }
        jsize dimensions = static_cast<jsize>(sizes.size());
        jintArray lengths = env->NewIntArray(dimensions);
        if (lengths == nullptr) {
            env->DeleteLocalRef(component);
            return nullptr;
        }

        env->SetIntArrayRegion(lengths, 0, dimensions, sizes.begin());
        jarray result = nullptr;
        if (!env->ExceptionCheck()) {
            result = (jarray) env->CallStaticObjectMethod(reflect_array_class, new_array_method, component, lengths);
        }
        env->DeleteLocalRef(lengths);
        env->DeleteLocalRef(component);
        return result;
    }

    static jclass complete_class_lookup(JNIEnv *env, jclass clazz) {
        if (env->ExceptionCheck()) {
            jthrowable exception = env->ExceptionOccurred();
            env->ExceptionClear();
            jobject details = env->CallObjectMethod(
                exception,
                get_message_method
            );
            if (env->ExceptionCheck()) {
                env->DeleteLocalRef(exception);
                return nullptr;
            }
            jobject new_exception = env->NewObject(no_class_def_found_class,
                ncdf_init_method,
                details);
            if (env->ExceptionCheck()) {
                env->DeleteLocalRef(exception);
                env->DeleteLocalRef(details);
                return nullptr;
            }
            env->CallVoidMethod(new_exception, init_cause_method, exception);
            if (env->ExceptionCheck()) {
                env->DeleteLocalRef(new_exception);
                env->DeleteLocalRef(exception);
                env->DeleteLocalRef(details);
                return nullptr;
            }
            env->Throw((jthrowable) new_exception);
            env->DeleteLocalRef(new_exception);
            env->DeleteLocalRef(exception);
            env->DeleteLocalRef(details);
            return nullptr;
        }
        return clazz;
    }

    jclass find_class_wo_static(JNIEnv *env, jobject classloader, jstring class_name_string) {
        return complete_class_lookup(env, (jclass) env->CallObjectMethod(
            classloader, load_class_method, class_name_string));
    }

    void throw_re(JNIEnv *env, const char *exception_class, const char *error, int line) {
        jclass exception_class_ptr = env->FindClass(exception_class);
        if (env->ExceptionCheck()) {
            return;
        }
        env->ThrowNew(exception_class_ptr, ("\"" + std::string(error) + "\" on " + std::to_string(line)).c_str());
        env->DeleteLocalRef(exception_class_ptr);
    }

    void bastore(JNIEnv *env, jarray array, jint index, jint value) {
        if (env->IsInstanceOf(array, boolean_array_class))
            env->SetBooleanArrayRegion((jbooleanArray) array, index, 1, (jboolean*) (&value));
        else
            env->SetByteArrayRegion((jbyteArray) array, index, 1, (jbyte*) (&value));
    }

    jbyte baload(JNIEnv *env, jarray array, jint index) {
        jbyte ret_value;
        if (env->IsInstanceOf(array, boolean_array_class))
            env->GetBooleanArrayRegion((jbooleanArray) array, index, 1, (jboolean*) (&ret_value));
        else
            env->GetByteArrayRegion((jbyteArray) array, index, 1, (jbyte*) (&ret_value));
        return ret_value;
    }

    jclass get_class_from_object(JNIEnv *env, jobject object) {
        jobject result_class = env->CallObjectMethod(object, get_class_method);
        if (env->ExceptionCheck()) {
            return nullptr;
        }
        return (jclass) result_class;
    }

    jobject get_classloader_from_class(JNIEnv *env, jclass clazz) {
        jobject result_classloader = env->CallObjectMethod(clazz, get_classloader_method);
        if (env->ExceptionCheck()) {
            return nullptr;
        }
        return result_classloader;
    }

    jobject get_lookup(JNIEnv *env, jclass clazz) {
        jobject lookup = env->NewObject(methodhandles_lookup_class, lookup_init_method, clazz);
        if (env->ExceptionCheck()) {
            return nullptr;
        }
        return lookup;
    }

    jstring get_interned(JNIEnv *env, jstring value) {
        jstring result = (jstring) env->CallObjectMethod(value, string_intern_method);
        if (env->ExceptionCheck())
            return nullptr;
        return result;
    }
}
