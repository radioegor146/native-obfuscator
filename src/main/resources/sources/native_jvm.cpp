#include "native_jvm.hpp"
#include <limits>

namespace native_jvm::utils {

    union __fi_conv { 
        jfloat m_jfloat; 
        jint m_jint; 
    };

    jint cfi(jfloat f) { 
        __fi_conv fi; 
        fi.m_jfloat = f;
        return fi.m_jint; 
    }

    jfloat cif(jint i) { 
        __fi_conv fi; 
        fi.m_jint = i; 
        return fi.m_jfloat; 
    }


    union __dl_conv { 
        jdouble m_jdouble; 
        jlong m_jlong; 
    };

    jlong cdl(jdouble d) { 
        __dl_conv dl; 
        dl.m_jdouble = d; 
        return dl.m_jlong;
    }

    jdouble cld(jlong l) { 
        __dl_conv dl; 
        dl.m_jlong = l; 
        return dl.m_jdouble; 
    }

    jclass boolean_array_class;
    jmethodID string_intern_method;
    jclass no_class_def_found_class;
    jmethodID ncdf_init_method;
    jclass throwable_class;
    jmethodID get_message_method;
    jmethodID init_cause_method;

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
    }

    template <>
    jarray create_array_value<1>(JNIEnv *env, jint size) {
        return env->NewBooleanArray(size);
    }

    template <>
    jarray create_array_value<2>(JNIEnv *env, jint size) {
        return env->NewCharArray(size);
    }

    template <>
    jarray create_array_value<3>(JNIEnv *env, jint size) {
        return env->NewByteArray(size);
    }

    template <>
    jarray create_array_value<4>(JNIEnv *env, jint size) {
        return env->NewShortArray(size);
    }

    template <>
    jarray create_array_value<5>(JNIEnv *env, jint size) {
        return env->NewIntArray(size);
    }

    template <>
    jarray create_array_value<6>(JNIEnv *env, jint size) {
        return env->NewFloatArray(size);
    }

    template <>
    jarray create_array_value<7>(JNIEnv *env, jint size) {
        return env->NewLongArray(size);
    }

    template <>
    jarray create_array_value<8>(JNIEnv *env, jint size) {
        return env->NewDoubleArray(size);
    }

    jobjectArray create_multidim_array(JNIEnv *env, jint count, jint required_count, jint *sizes, const char *class_name, int line) {
        if (required_count == 0)
            return nullptr;
        if (*sizes < 0) {
            throw_re(env, "java/lang/NegativeArraySizeException", "MULTIANEWARRAY size < 0", line);
            return nullptr;
        }
        jobjectArray result_array = nullptr;
        if (jclass clazz = env->FindClass((std::string(count - 1, '[') + "L" + std::string(class_name) + ";").c_str())) {
            result_array = env->NewObjectArray(*sizes, clazz, nullptr);
            env->DeleteLocalRef(clazz);
        }
        else
            return nullptr;

        for (jint i = 0; i < *sizes; i++) {
            jobjectArray inner_array = create_multidim_array(env, count - 1, required_count - 1,
                sizes + 1, class_name, line);
            if (env->ExceptionCheck())
                return nullptr;
            env->SetObjectArrayElement(result_array, i, inner_array);
            if (env->ExceptionCheck())
                return nullptr;
            env->DeleteLocalRef(inner_array);
        }
        return result_array;
    }

    jclass find_class_wo_static(JNIEnv *env, const char *class_name) {
        jclass thread_class = env->FindClass("java/lang/Thread");
        if (env->ExceptionCheck())
            return nullptr;
        jmethodID current_thread_method = env->GetStaticMethodID(thread_class, "currentThread", "()Ljava/lang/Thread;");
        if (env->ExceptionCheck())
            return nullptr;
        jobject current_thread = env->CallStaticObjectMethod(
            thread_class, 
            current_thread_method
        );
        if (env->ExceptionCheck())
            return nullptr;
        jmethodID get_context_classloader_method = env->GetMethodID(thread_class, "getContextClassLoader", "()Ljava/lang/ClassLoader;");
        jobject classloader = env->CallObjectMethod(
            current_thread,
            get_context_classloader_method
        );
        if (env->ExceptionCheck())
            return nullptr;
        jclass classloader_class = env->FindClass("java/lang/ClassLoader");
        if (env->ExceptionCheck())
            return nullptr;
        jstring class_name_string = env->NewStringUTF(class_name);
        env->DeleteLocalRef(current_thread);
        if (env->ExceptionCheck())
            return nullptr;
        jmethodID load_class_method = env->GetMethodID(classloader_class, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
        if (env->ExceptionCheck())
            return nullptr;
        jclass clazz = (jclass) env->CallObjectMethod(
            classloader,
            load_class_method,
            class_name_string
        );
        if (env->ExceptionCheck()) {
            env->DeleteLocalRef(classloader);
            env->DeleteLocalRef(classloader_class);
            env->DeleteLocalRef(class_name_string);
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
            env->DeleteLocalRef(exception);
            env->DeleteLocalRef(details);
            return nullptr;
        }
        env->DeleteLocalRef(classloader);
        env->DeleteLocalRef(classloader_class);
        env->DeleteLocalRef(class_name_string);
        return clazz;
    }

    void throw_re(JNIEnv *env, const char *exception_class, const char *error, int line) {
        jclass exception_class_ptr = env->FindClass(exception_class);
        if (!exception_class_ptr)
            return;
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


    jlong cast_dl(jdouble value) {
        if (std::isnan(value))
            return 0;
        int exponent;
        std::frexp(value, &exponent);
        if (std::isfinite(value) && exponent <= 63)
            return (jlong) value;
        return std::signbit(value) ? std::numeric_limits<jlong>::min() : std::numeric_limits<jlong>::max();
    }

    jlong cast_fl(jfloat value) {
        if (std::isnan(value))
            return 0;
        int exponent;
        std::frexp(value, &exponent);
        if (std::isfinite(value) && exponent <= 63)
            return (jlong) value;
        return std::signbit(value) ? std::numeric_limits<jlong>::min() : std::numeric_limits<jlong>::max();
    }

    jint cast_di(jdouble value) {
        if (std::isnan(value))
            return 0;
        int exponent;
        std::frexp(value, &exponent);
        if (std::isfinite(value) && exponent <= 31)
            return (jint) value;
        return std::signbit(value) ? std::numeric_limits<jint>::min() : std::numeric_limits<jint>::max();
    }

    jint cast_fi(jfloat value) {
        if (std::isnan(value))
          return 0;
        int exponent;
        std::frexp(value, &exponent);
        if (std::isfinite(value) && exponent <= 31)
          return (jint) value;
        return std::signbit(value) ? std::numeric_limits<jint>::min() : std::numeric_limits<jint>::max();
    }

    void clear_refs(JNIEnv *env, std::unordered_set<jobject> &refs) {
        for (jobject ref : refs)
            if (env->GetObjectRefType(ref) == JNILocalRefType)
                env->DeleteLocalRef(ref);
        refs.clear();
    }

    jstring get_interned(JNIEnv *env, jstring value) {
        jstring result = (jstring) env->CallObjectMethod(value, string_intern_method);
        if (env->ExceptionCheck())
            return nullptr;
        return result;
    }
}